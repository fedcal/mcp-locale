package com.server.weather.mcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.server.weather.exception.WeatherServiceException;
import com.server.weather.formatter.WeatherFormatter;
import com.server.weather.model.Alert;
import com.server.weather.model.ForecastBundle;
import com.server.weather.service.WeatherService;

@Component
@ConditionalOnProperty(name = "weather.mcp-stdio-enabled", havingValue = "true", matchIfMissing = true)
public class McpStdIoRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(McpStdIoRunner.class);

    private final WeatherService weatherService;
    private final WeatherFormatter formatter;
    private final ObjectMapper mapper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "mcp-stdio-loop"));

    public McpStdIoRunner(WeatherService weatherService, WeatherFormatter formatter, ObjectMapper mapper) {
        this.weatherService = weatherService;
        this.formatter = formatter;
        this.mapper = mapper;
    }

    @Override
    public void run(String... args) {
        log.info("Avvio MCP stdio bridge (Java).");
        executor.submit(this::loop);
    }

    private void loop() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                if (line.startsWith("Content-Length:")) {
                    int length = parseContentLength(line);
                    reader.readLine(); // consume empty line
                    char[] buffer = new char[length];
                    int read = reader.read(buffer, 0, length);
                    if (read > 0) {
                        processPayload(new String(buffer, 0, read));
                    }
                } else {
                    processPayload(line);
                }
            }
        } catch (IOException ioException) {
            log.warn("Loop MCP stdio terminato", ioException);
        }
    }

    private int parseContentLength(String headerLine) {
        try {
            return Integer.parseInt(headerLine.substring("Content-Length:".length()).trim());
        } catch (NumberFormatException ex) {
            log.warn("Header Content-Length non valido: {}", headerLine);
            return 0;
        }
    }

    private void processPayload(String payload) {
        JsonNode id = null;
        try {
            JsonNode request = mapper.readTree(payload);
            id = request.path("id");
            String method = request.path("method").asText();
            JsonNode params = request.path("params");

            switch (method) {
                case "initialize" -> sendResult(id, Map.of(
                        "protocolVersion", "2024-11-05",
                        "capabilities", Map.of("tools", Map.of(), "resources", Map.of(), "prompts", Map.of()),
                        "serverInfo", Map.of("name", "weather-java-spring", "version", "0.1.0")));
                case "ping" -> sendResult(id, Map.of());
                case "tools/list" -> sendResult(id, buildToolsList());
                case "tools/call" -> sendResult(id, handleToolCall(params));
                default -> sendError(id, -32601, "Metodo non supportato: " + method);
            }
        } catch (WeatherServiceException serviceError) {
            sendError(id, -32000, serviceError.getMessage());
        } catch (Exception generic) {
            sendError(id, -32000, "Errore durante l'elaborazione MCP: " + generic.getMessage());
        }
    }

    private Map<String, Object> buildToolsList() {
        Map<String, Object> alertsTool = Map.of(
                "name", "get_alerts",
                "description", "Allerte meteo per uno stato USA (NWS).",
                "inputSchema", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "state", Map.of("type", "string", "description", "Codice stato (es. CA, NY)")
                        ),
                        "required", List.of("state")));

        Map<String, Object> forecastTool = Map.of(
                "name", "get_forecast",
                "description", "Forecast puntuale per coordinate.",
                "inputSchema", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "latitude", Map.of("type", "number", "description", "Latitudine in decimali"),
                                "longitude", Map.of("type", "number", "description", "Longitudine in decimali"),
                                "periods", Map.of("type", "integer", "description", "Numero di periodi opzionale")
                        ),
                        "required", List.of("latitude", "longitude")));

        return Map.of("tools", List.of(alertsTool, forecastTool));
    }

    private Map<String, Object> handleToolCall(JsonNode params) {
        String name = params.path("name").asText();
        JsonNode arguments = params.path("arguments");

        return switch (name) {
            case "get_alerts" -> {
                String state = arguments.path("state").asText();
                List<Alert> alerts = weatherService.alertsForState(state);
                yield Map.of("content", List.of(Map.of("type", "text", "text", formatter.formatAlerts(alerts))));
            }
            case "get_forecast" -> {
                double latitude = arguments.path("latitude").asDouble();
                double longitude = arguments.path("longitude").asDouble();
                Integer periods = arguments.has("periods") && !arguments.get("periods").isNull()
                        ? arguments.get("periods").asInt()
                        : null;
                ForecastBundle bundle = weatherService.forecastForCoordinates(latitude, longitude, periods);
                yield Map.of("content", List.of(Map.of("type", "text", "text", formatter.formatForecast(bundle))));
            }
            default -> throw new WeatherServiceException("Tool non supportato: " + name);
        };
    }

    private void sendResult(JsonNode id, Object result) {
        ObjectNode response = mapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        if (id != null && !id.isMissingNode()) {
            response.set("id", id);
        }
        response.set("result", mapper.valueToTree(result));
        write(response);
    }

    private void sendError(JsonNode id, int code, String message) {
        ObjectNode response = mapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        if (id != null && !id.isMissingNode()) {
            response.set("id", id);
        }
        ObjectNode error = mapper.createObjectNode();
        error.put("code", code);
        error.put("message", message);
        response.set("error", error);
        write(response);
    }

    private void write(ObjectNode response) {
        try {
            System.out.println(mapper.writeValueAsString(response));
            System.out.flush();
        } catch (IOException e) {
            log.warn("Impossibile scrivere la risposta MCP", e);
        }
    }
}

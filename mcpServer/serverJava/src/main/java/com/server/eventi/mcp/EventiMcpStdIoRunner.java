package com.server.eventi.mcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
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
import com.server.eventi.service.EventiService;

@Component
@ConditionalOnProperty(name = "eventi.mcp-stdio-enabled", havingValue = "true", matchIfMissing = true)
public class EventiMcpStdIoRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(EventiMcpStdIoRunner.class);

    private final EventiService service;
    private final ObjectMapper mapper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "eventi-mcp-stdio"));

    public EventiMcpStdIoRunner(EventiService service, ObjectMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Override
    public void run(String... args) {
        log.info("Avvio MCP stdio (eventi-amici).");
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
                    reader.readLine(); // empty
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
            log.warn("Loop MCP eventi terminato", ioException);
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
                case "initialize" -> sendResult(id, initPayload());
                case "ping" -> sendResult(id, mapper.createObjectNode());
                case "tools/list" -> sendResult(id, toolsList());
                case "tools/call" -> sendResult(id, handleToolCall(params));
                default -> sendError(id, -32601, "Metodo non supportato: " + method);
            }
        } catch (IllegalArgumentException badInput) {
            sendError(id, -32602, badInput.getMessage());
        } catch (Exception generic) {
            sendError(id, -32000, "Errore MCP eventi: " + generic.getMessage());
        }
    }

    private ObjectNode initPayload() {
        ObjectNode node = mapper.createObjectNode();
        node.put("protocolVersion", "2024-11-05");
        node.set("capabilities", mapper.createObjectNode().putObject("tools"));
        ObjectNode info = mapper.createObjectNode();
        info.put("name", "eventi-amici-java");
        info.put("version", "0.1.0");
        node.set("serverInfo", info);
        return node;
    }

    private ObjectNode toolsList() {
        ObjectNode response = mapper.createObjectNode();
        var tools = mapper.createArrayNode();
        tools.add(tool("create_event", "Crea un evento", obj(
                prop("name", "string", "Nome evento"),
                prop("date", "string", "Data/ora testuale"),
                prop("location", "string", "Luogo"),
                prop("budget", "number", "Budget totale", false),
                prop("notes", "string", "Note", false)
        ), List.of("name", "date", "location")));

        tools.add(tool("add_participant", "Aggiunge partecipante", obj(
                prop("event_id", "string", "ID evento"),
                prop("name", "string", "Nome partecipante"),
                prop("intolerances", "array", "Intolleranze/allergie", false),
                prop("preferences", "array", "Preferenze cucina", false),
                prop("weight", "number", "Peso per split", false)
        ), List.of("event_id", "name")));

        tools.add(tool("update_preferences", "Aggiorna preferenze partecipante", obj(
                prop("event_id", "string", "ID evento"),
                prop("participant_id", "string", "ID partecipante"),
                prop("intolerances", "array", "Intolleranze/allergie", false),
                prop("preferences", "array", "Preferenze cucina", false),
                prop("weight", "number", "Peso per split", false)
        ), List.of("event_id", "participant_id")));

        tools.add(tool("event_summary", "Riepilogo evento e partecipanti", obj(
                prop("event_id", "string", "ID evento")
        ), List.of("event_id")));

        tools.add(tool("suggest_restaurants", "Suggerimenti ristoranti compatibili", obj(
                prop("event_id", "string", "ID evento"),
                prop("limit", "integer", "Numero massimo", false)
        ), List.of("event_id")));

        tools.add(tool("split_bill", "Divide il conto", obj(
                prop("event_id", "string", "ID evento"),
                prop("total_amount", "number", "Importo totale"),
                prop("mode", "string", "equal|weighted", false)
        ), List.of("event_id", "total_amount")));

        response.set("tools", tools);
        return response;
    }

    private ObjectNode tool(String name, String description, ObjectNode schema, List<String> required) {
        ObjectNode t = mapper.createObjectNode();
        t.put("name", name);
        t.put("description", description);
        schema.set("required", mapper.valueToTree(required));
        t.set("inputSchema", schema);
        return t;
    }

    private ObjectNode obj(ObjectNode... props) {
        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");
        ObjectNode properties = mapper.createObjectNode();
        for (ObjectNode prop : props) {
            properties.set(prop.path("_name").asText(), prop.without("_name"));
        }
        schema.set("properties", properties);
        return schema;
    }

    private ObjectNode prop(String name, String type, String description) {
        return prop(name, type, description, true);
    }

    private ObjectNode prop(String name, String type, String description, boolean required) {
        ObjectNode p = mapper.createObjectNode();
        p.put("_name", name);
        p.put("type", type);
        p.put("description", description);
        if (!required) {
            p.put("nullable", true);
        }
        return p;
    }

    private ObjectNode handleToolCall(JsonNode params) {
        String name = params.path("name").asText();
        JsonNode args = params.path("arguments");
        return switch (name) {
            case "create_event" -> respondText(createEvent(args));
            case "add_participant" -> respondText(addParticipant(args));
            case "update_preferences" -> respondText(updateParticipant(args));
            case "event_summary" -> respondText(eventSummary(args));
            case "suggest_restaurants" -> respondText(suggest(args));
            case "split_bill" -> respondText(split(args));
            default -> throw new IllegalArgumentException("Tool non supportato: " + name);
        };
    }

    private String createEvent(JsonNode args) {
        String name = args.path("name").asText();
        String date = args.path("date").asText();
        String location = args.path("location").asText();
        BigDecimal budget = args.has("budget") && !args.get("budget").isNull() ? args.get("budget").decimalValue() : null;
        String notes = args.has("notes") && !args.get("notes").isNull() ? args.get("notes").asText() : null;
        var ev = service.createEvent(name, date, location, budget, notes);
        return "Evento creato: %s (id=%s) a %s il %s. Budget: %s %s".formatted(
                ev.getName(), ev.getId(), ev.getLocation(), ev.getDateTime(), ev.getBudget() != null ? ev.getBudget() : "n.d.", ev.getCurrency());
    }

    private String addParticipant(JsonNode args) {
        UUID eventId = UUID.fromString(args.path("event_id").asText());
        String name = args.path("name").asText();
        List<String> intolerances = jsonArrayToList(args.path("intolerances"));
        List<String> preferences = jsonArrayToList(args.path("preferences"));
        Double weight = args.has("weight") && !args.get("weight").isNull() ? args.get("weight").asDouble() : null;
        var p = service.addParticipant(eventId, name, intolerances, preferences, weight);
        return "Aggiunto %s (id=%s) a evento %s.".formatted(p.getName(), p.getId(), eventId);
    }

    private String updateParticipant(JsonNode args) {
        UUID eventId = UUID.fromString(args.path("event_id").asText());
        UUID participantId = UUID.fromString(args.path("participant_id").asText());
        List<String> intolerances = jsonArrayToList(args.path("intolerances"));
        List<String> preferences = jsonArrayToList(args.path("preferences"));
        Double weight = args.has("weight") && !args.get("weight").isNull() ? args.get("weight").asDouble() : null;
        var p = service.updateParticipant(eventId, participantId, intolerances, preferences, weight);
        return "Aggiornato %s (id=%s) per evento %s.".formatted(p.getName(), p.getId(), eventId);
    }

    private String eventSummary(JsonNode args) {
        UUID eventId = UUID.fromString(args.path("event_id").asText());
        var ev = service.getEvent(eventId);
        var participants = service.listParticipants(eventId);
        StringBuilder sb = new StringBuilder();
        sb.append("Evento: ").append(ev.getName()).append(" (").append(ev.getId()).append(")\n")
                .append("Data: ").append(ev.getDateTime()).append("\n")
                .append("Luogo: ").append(ev.getLocation()).append("\n")
                .append("Budget: ").append(ev.getBudget() != null ? ev.getBudget() : "n.d.").append(" ").append(ev.getCurrency()).append("\n")
                .append("Note: ").append(ev.getNotes() != null ? ev.getNotes() : "-").append("\n")
                .append("Partecipanti:\n");
        if (participants.isEmpty()) {
            sb.append("Nessuno.");
        } else {
            for (var p : participants) {
                sb.append("- ").append(p.getName()).append(" (intolleranze: ").append(p.getIntolerances() == null || p.getIntolerances().isBlank() ? "nessuna" : p.getIntolerances())
                        .append(", preferenze: ").append(p.getPreferences() == null || p.getPreferences().isBlank() ? "nessuna" : p.getPreferences())
                        .append(", peso: ").append(p.getWeight()).append(")\n");
            }
        }
        return sb.toString().trim();
    }

    private String suggest(JsonNode args) {
        UUID eventId = UUID.fromString(args.path("event_id").asText());
        Integer limit = args.has("limit") && !args.get("limit").isNull() ? args.get("limit").asInt() : null;
        var suggestions = service.suggestRestaurants(eventId, limit);
        if (suggestions.isEmpty()) {
            return "Nessun suggerimento disponibile.";
        }
        StringBuilder sb = new StringBuilder();
        for (var s : suggestions) {
            sb.append(s.name()).append(" (").append(s.cuisine()).append(", ").append(s.priceLevel()).append(") - supporta: ")
                    .append(s.supports()).append(" - luogo: ").append(s.location()).append("\n");
        }
        return sb.toString().trim();
    }

    private String split(JsonNode args) {
        UUID eventId = UUID.fromString(args.path("event_id").asText());
        double totalAmount = args.path("total_amount").asDouble();
        String mode = args.has("mode") && !args.get("mode").isNull() ? args.get("mode").asText().toLowerCase(Locale.ROOT) : "equal";
        return service.splitBill(eventId, totalAmount, mode);
    }

    private List<String> jsonArrayToList(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return List.of();
        }
        var list = new java.util.ArrayList<String>();
        node.forEach(n -> list.add(n.asText()));
        return list;
    }

    private ObjectNode respondText(String text) {
        ObjectNode response = mapper.createObjectNode();
        var content = mapper.createArrayNode();
        ObjectNode entry = mapper.createObjectNode();
        entry.put("type", "text");
        entry.put("text", text);
        content.add(entry);
        response.set("content", content);
        return response;
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

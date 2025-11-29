package com.server.weather.client;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.server.weather.config.WeatherClientProperties;
import com.server.weather.exception.InvalidLocationException;
import com.server.weather.exception.UpstreamServiceException;
import com.server.weather.model.Alert;
import com.server.weather.model.ForecastPeriod;
import com.server.weather.model.Gridpoint;

@Component
public class NwsClient {

    private final WebClient webClient;
    private final Duration timeout;

    public NwsClient(WebClient.Builder builder, WeatherClientProperties properties) {
        this.timeout = properties.getTimeout();
        this.webClient = builder
                .baseUrl(properties.getApiBase())
                .defaultHeader(HttpHeaders.USER_AGENT, properties.getUserAgent())
                .defaultHeader(HttpHeaders.ACCEPT, "application/geo+json")
                .build();
    }

    public List<Alert> fetchAlerts(String state) {
        JsonNode data = getJson(webClient.get().uri("/alerts/active/area/{state}", state));
        List<Alert> alerts = new ArrayList<>();
        JsonNode features = data.path("features");
        if (features.isArray()) {
            for (JsonNode feature : features) {
                JsonNode props = feature.path("properties");
                alerts.add(new Alert(
                        props.path("event").asText("Unknown"),
                        props.path("areaDesc").asText("Unknown"),
                        props.path("severity").asText("Unknown"),
                        props.path("description").asText("No description available"),
                        props.path("instruction").asText("No specific instructions provided")));
            }
        }
        return alerts;
    }

    public Gridpoint resolveGridpoint(double latitude, double longitude) {
        JsonNode data = getJson(webClient.get().uri("/points/{lat},{lon}", latitude, longitude));
        JsonNode props = data.path("properties");
        JsonNode forecastNode = props.path("forecast");
        if (forecastNode.isMissingNode() || forecastNode.isNull()) {
            throw new InvalidLocationException("Coordinate valide ma NWS non ha restituito il forecast URL.");
        }
        return new Gridpoint(
                forecastNode.asText(),
                props.path("gridId").asText(null),
                props.path("gridX").isInt() ? props.get("gridX").asInt() : null,
                props.path("gridY").isInt() ? props.get("gridY").asInt() : null);
    }

    public List<ForecastPeriod> fetchForecast(String forecastUrl) {
        JsonNode data = getJson(webClient.get().uri(URI.create(forecastUrl)));
        List<ForecastPeriod> periods = new ArrayList<>();
        JsonNode periodNodes = data.path("properties").path("periods");
        if (periodNodes.isArray()) {
            for (JsonNode node : periodNodes) {
                periods.add(new ForecastPeriod(
                        node.path("name").asText("Unknown period"),
                        node.has("temperature")
                                ? node.path("temperature").asInt() + "°" + node.path("temperatureUnit").asText("")
                                : "?",
                        (node.path("windSpeed").asText("?") + " " + node.path("windDirection").asText("")).trim(),
                        node.path("detailedForecast").asText("Nessuna descrizione disponibile.")));
            }
        }
        return periods;
    }

    private JsonNode getJson(WebClient.RequestHeadersSpec<?> request) {
        try {
            JsonNode body = request.retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(timeout);
            if (body == null) {
                throw new UpstreamServiceException("Risposta vuota da NWS.");
            }
            return body;
        } catch (WebClientResponseException.NotFound notFound) {
            throw new InvalidLocationException("Risorsa non trovata su NWS.", notFound);
        } catch (WebClientResponseException httpError) {
            throw new UpstreamServiceException(
                    "Errore HTTP %d verso NWS.".formatted(httpError.getStatusCode().value()),
                    httpError);
        } catch (WebClientRequestException requestError) {
            throw new UpstreamServiceException("Errore di rete verso NWS: " + requestError.getMessage(), requestError);
        } catch (Exception generic) {
            throw new UpstreamServiceException("Errore inatteso parlando con NWS.", generic);
        }
    }
}

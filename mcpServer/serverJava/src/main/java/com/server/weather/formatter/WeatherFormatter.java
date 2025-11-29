package com.server.weather.formatter;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.server.weather.model.Alert;
import com.server.weather.model.ForecastBundle;
import com.server.weather.model.ForecastPeriod;

@Component
public class WeatherFormatter {

    public String formatAlerts(List<Alert> alerts) {
        if (alerts.isEmpty()) {
            return "Nessuna allerta attiva per questo stato.";
        }
        return alerts.stream()
                .map(alert -> String.join("\n",
                        "Evento: " + alert.event(),
                        "Area: " + alert.area(),
                        "Severita': " + alert.severity(),
                        "Descrizione: " + alert.description(),
                        "Istruzioni: " + alert.instruction()))
                .collect(Collectors.joining("\n---\n"));
    }

    public String formatForecast(ForecastBundle bundle) {
        StringBuilder header = new StringBuilder();
        if (bundle.gridpoint().gridId() != null) {
            header.append("Ufficio: ").append(bundle.gridpoint().gridId());
        }
        if (bundle.gridpoint().gridX() != null && bundle.gridpoint().gridY() != null) {
            if (header.length() > 0) {
                header.append(" | ");
            }
            header.append("Grid point: ")
                    .append(bundle.gridpoint().gridX())
                    .append(",")
                    .append(bundle.gridpoint().gridY());
        }

        if (bundle.periods().isEmpty()) {
            String fallback = "Nessun periodo di forecast disponibile.";
            return header.length() == 0 ? fallback : header + "\n" + fallback;
        }

        String body = bundle.periods().stream()
                .map(this::formatPeriod)
                .collect(Collectors.joining("\n---\n"));
        return header.length() == 0 ? body : header + "\n" + body;
    }

    private String formatPeriod(ForecastPeriod period) {
        return String.join("\n",
                period.name(),
                "Temperatura: " + period.temperature(),
                "Vento: " + period.wind(),
                "Previsione: " + period.detailedForecast());
    }
}

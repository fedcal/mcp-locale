package com.server.weather.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.server.weather.client.NwsClient;
import com.server.weather.config.WeatherClientProperties;
import com.server.weather.exception.WeatherServiceException;
import com.server.weather.model.Alert;
import com.server.weather.model.ForecastBundle;
import com.server.weather.model.ForecastPeriod;
import com.server.weather.model.Gridpoint;

@Service
public class WeatherService {

    private final NwsClient nwsClient;
    private final WeatherClientProperties properties;

    public WeatherService(NwsClient nwsClient, WeatherClientProperties properties) {
        this.nwsClient = nwsClient;
        this.properties = properties;
    }

    public List<Alert> alertsForState(String state) {
        if (state == null || state.isBlank()) {
            throw new WeatherServiceException("Il codice dello stato non puo' essere vuoto.");
        }
        String normalized = state.strip().toUpperCase(Locale.US);
        if (normalized.length() != 2) {
            throw new WeatherServiceException("Il codice dello stato deve contenere due lettere (es. CA, NY).");
        }
        return nwsClient.fetchAlerts(normalized);
    }

    public ForecastBundle forecastForCoordinates(double latitude, double longitude, Integer periods) {
        int limit = periods != null ? periods : properties.getForecastPeriods();
        if (limit <= 0) {
            throw new WeatherServiceException("Il numero di periodi richiesto deve essere maggiore di zero.");
        }
        Gridpoint gridpoint = nwsClient.resolveGridpoint(latitude, longitude);
        List<ForecastPeriod> forecasts = nwsClient.fetchForecast(gridpoint.forecastUrl());
        List<ForecastPeriod> trimmed = forecasts.size() > limit ? forecasts.subList(0, limit) : forecasts;
        return new ForecastBundle(gridpoint, trimmed);
    }
}

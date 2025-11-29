package com.server.weather.model;

public record ForecastPeriod(
        String name,
        String temperature,
        String wind,
        String detailedForecast) {
}

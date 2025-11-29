package com.server.weather.model;

public record Gridpoint(
        String forecastUrl,
        String gridId,
        Integer gridX,
        Integer gridY) {
}

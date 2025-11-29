package com.server.weather.model;

public record Alert(
        String event,
        String area,
        String severity,
        String description,
        String instruction) {
}

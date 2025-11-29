package com.server.weather.model;

import java.util.List;

public record ForecastBundle(
        Gridpoint gridpoint,
        List<ForecastPeriod> periods) {
}

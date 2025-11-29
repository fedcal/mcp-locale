package com.server.weather.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.server.weather.model.Alert;
import com.server.weather.model.ForecastBundle;
import com.server.weather.service.WeatherService;

@RestController
@RequestMapping(path = "/api/weather", produces = MediaType.APPLICATION_JSON_VALUE)
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/alerts/{state}")
    public List<Alert> getAlerts(@PathVariable String state) {
        return weatherService.alertsForState(state);
    }

    @GetMapping("/forecast")
    public ForecastBundle getForecast(
            @RequestParam("lat") double latitude,
            @RequestParam("lon") double longitude,
            @RequestParam(value = "periods", required = false) Integer periods) {
        return weatherService.forecastForCoordinates(latitude, longitude, periods);
    }
}

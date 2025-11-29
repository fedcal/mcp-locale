package com.server.weather.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.server.weather.exception.InvalidLocationException;
import com.server.weather.exception.UpstreamServiceException;
import com.server.weather.exception.WeatherServiceException;

@RestControllerAdvice
public class WeatherExceptionHandler {

    @ExceptionHandler(InvalidLocationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidLocation(InvalidLocationException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(WeatherServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleServiceErrors(WeatherServiceException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(UpstreamServiceException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Map<String, String> handleUpstreamErrors(UpstreamServiceException exception) {
        return Map.of("error", exception.getMessage());
    }
}

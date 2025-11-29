package com.server.weather.exception;

public class InvalidLocationException extends WeatherServiceException {
    public InvalidLocationException(String message) {
        super(message);
    }

    public InvalidLocationException(String message, Throwable cause) {
        super(message, cause);
    }
}

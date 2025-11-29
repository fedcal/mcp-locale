package com.server.weather.exception;

public class UpstreamServiceException extends WeatherServiceException {
    public UpstreamServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public UpstreamServiceException(String message) {
        super(message);
    }
}

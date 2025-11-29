package com.server.weather.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "weather")
public class WeatherClientProperties {

    private String apiBase = "https://api.weather.gov";
    private String userAgent = "weather-java/1.0";
    private Duration timeout = Duration.ofSeconds(30);
    private int forecastPeriods = 5;
    private boolean mcpStdioEnabled = true;

    public String getApiBase() {
        return apiBase;
    }

    public void setApiBase(String apiBase) {
        this.apiBase = apiBase;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public int getForecastPeriods() {
        return forecastPeriods;
    }

    public void setForecastPeriods(int forecastPeriods) {
        this.forecastPeriods = forecastPeriods;
    }

    public boolean isMcpStdioEnabled() {
        return mcpStdioEnabled;
    }

    public void setMcpStdioEnabled(boolean mcpStdioEnabled) {
        this.mcpStdioEnabled = mcpStdioEnabled;
    }
}

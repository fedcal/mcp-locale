package com.server.eventi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "eventi")
public class EventProperties {

    private String defaultCurrency = "EUR";
    private int suggestionLimit = 5;

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public int getSuggestionLimit() {
        return suggestionLimit;
    }

    public void setSuggestionLimit(int suggestionLimit) {
        this.suggestionLimit = suggestionLimit;
    }
}

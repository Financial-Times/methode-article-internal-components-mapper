package com.ft.methodearticleinternalcomponentsmapper.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConnectionConfiguration {

    private final int numberOfConnectionAttempts;
    private final int timeoutMultiplier;

    public ConnectionConfiguration(@JsonProperty("numberOfConnectionAttempts") int numberOfConnectionAttempts,
                                   @JsonProperty("timeoutMultiplier") int timeoutMultiplier) {
        this.numberOfConnectionAttempts = numberOfConnectionAttempts;
        this.timeoutMultiplier = timeoutMultiplier;
    }

    public int getNumberOfConnectionAttempts() {
        return numberOfConnectionAttempts;
    }

    public int getTimeoutMultiplier() {
        return timeoutMultiplier;
    }
}

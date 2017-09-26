package com.ft.methodearticleinternalcomponentsmapper.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.jerseyhttpwrapper.config.EndpointConfiguration;
import com.google.common.base.Objects;

public class ConcordanceApiConfiguration {

    private final EndpointConfiguration endpointConfiguration;

    private final ConnectionConfiguration connectionConfiguration;

    public ConcordanceApiConfiguration(@JsonProperty("endpointConfiguration") EndpointConfiguration endpointConfiguration,
                                       @JsonProperty("connectionConfig") ConnectionConfiguration connectionConfiguration) {
        this.endpointConfiguration = endpointConfiguration;
        this.connectionConfiguration = connectionConfiguration;
    }

    public EndpointConfiguration getEndpointConfiguration() { return endpointConfiguration; }

    public ConnectionConfiguration getConnectionConfiguration() { return connectionConfiguration; }

    protected Objects.ToStringHelper toStringHelper() {
        return Objects
                .toStringHelper(this)
                .add("endpointConfiguration", endpointConfiguration)
                .add("connectionConfig", connectionConfiguration);
    }

    @Override
    public String toString() {
        return toStringHelper().toString();
    }
}

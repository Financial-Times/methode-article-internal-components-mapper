package com.ft.methodearticleinternalcomponentsmapper.health;

import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

public class RemoteServiceHealthCheck extends AdvancedHealthCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteServiceHealthCheck.class);

    private final int severity;
    private final String businessImpact;
    private final String panicGuideUrl;
    private final String serviceName;
    private final Client client;
    private final String hostHeader;
    private final URI remoteServiceUri;

    public RemoteServiceHealthCheck(
            String serviceName,
            Client client,
            String hostName,
            int port,
            String path,
            String hostHeader,
            int severity,
            String businessImpact,
            String panicGuideUrl
    ) {
        super(String.format("%s is up and running", serviceName));
        this.serviceName = serviceName;
        this.client = client;
        this.hostHeader = hostHeader;
        this.severity = severity;
        this.businessImpact = businessImpact;
        this.panicGuideUrl = panicGuideUrl;

        this.remoteServiceUri = UriBuilder.fromPath(path)
                .scheme("http")
                .host(hostName)
                .port(port)
                .build();
    }

    public AdvancedResult checkAdvanced() throws Exception {
        ClientResponse response = null;
        try {
            response = client.resource(remoteServiceUri).header("Host", hostHeader).get(ClientResponse.class);
            if (response.getStatus() != 200) {
                String message = String.format("Unexpected status : %s", response.getStatus());
                return reportUnhealthy(message);
            }
            return AdvancedResult.healthy();
        } catch (Throwable e) {
            String message = getName() + ": " + "Exception during remote service check call, " + e.getLocalizedMessage();
            return reportUnhealthy(message);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private AdvancedResult reportUnhealthy(String message) {
        LOGGER.warn(getName() + ": " + message);
        return AdvancedResult.error(this, message);
    }

    @Override
    protected int severity() {
        return severity;
    }

    @Override
    protected String businessImpact() {
        return businessImpact;
    }

    @Override
    protected String technicalSummary() {
        return String.format("This service is not able to connect to %s at %s", serviceName, remoteServiceUri);
    }

    @Override
    protected String panicGuideUrl() {
        return panicGuideUrl;
    }
}

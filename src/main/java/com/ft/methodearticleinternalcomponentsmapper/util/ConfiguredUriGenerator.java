package com.ft.methodearticleinternalcomponentsmapper.util;

import com.google.common.net.HostAndPort;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class ConfiguredUriGenerator implements ApiUriGenerator {

    private final String apiHost;
    private final int apiPort;

    public ConfiguredUriGenerator(String apiHost) {
        HostAndPort hostAndPort = HostAndPort.fromString(apiHost);
        this.apiHost = hostAndPort.getHostText();
        this.apiPort = hostAndPort.getPortOrDefault(-1);
    }

    @Override
    public String resolve(String relativePath) {
        URI uri = UriBuilder.fromUri(relativePath)
                .host(apiHost)
                .port(validatePort(apiPort))
                .scheme(getScheme(apiPort))
                .build();
        return uri.toString();
    }

    private int validatePort(int port) {
        return port == 80 || port == 443 ? -1 : port;
    }

    private String getScheme(int port) {
        return port == 443 || port == 8443 ? "https" : "http"; // assume http unless we're specifying port 443 which must be https
    }
}

package com.ft.methodearticleinternalcomponentsmapper.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Content {
    private String uuid;
    private String type;

    public Content(@JsonProperty("uuid") String uuid,
                   @JsonProperty("type") String type) {
        this.uuid = uuid;
        this.type = type;
    }

    public String getType() {
        return type;
    }


    public String getUuid() {
        return uuid;
    }
}

package com.ft.methodearticleinternalcomponentsmapper.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Block {
    private String key;
    private String valueXML;
    private String type;

    public Block(@JsonProperty("key") String key, @JsonProperty("valueXML") String valueXML, @JsonProperty("type") String type) {
        this.key = key;
        this.valueXML = valueXML;
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValueXML() {
        return valueXML;
    }

    public void setValueXML(String valueXML) {
        this.valueXML = valueXML;
    }
}

package com.ft.methodearticleinternalcomponentsmapper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Summary {

    private final String bodyXML;

    private Summary(@JsonProperty("bodyXML") final String bodyXML) {
        this.bodyXML = bodyXML;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getBodyXML() {
        return this.bodyXML;
    }

    public static Summary.Builder builder() {
        return new Summary.Builder();
    }

    public static class Builder {

        private String bodyXML;

        public Summary.Builder withBodyXML(String bodyXML) {
            this.bodyXML = bodyXML;
            return this;
        }

        public Summary.Builder withValuesFrom(Summary summary) {
            return this.withBodyXML(summary.bodyXML);
        }

        public Summary build() {
            return new Summary(this.bodyXML);
        }
    }
}

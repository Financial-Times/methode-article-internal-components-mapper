package com.ft.methodearticleinternalcomponentsmapper.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EomFile {

    public static final String SOURCE_ATTR_XPATH =
        "/ObjectMetadata//EditorialNotes/Sources/Source/SourceCode";

    public static final String OVERRIDE_ORIGINAL_ATTR_XPATH =
        "/ObjectMetadata/EditorialNotes/OverrideOriginal";

    private final String uuid;
    private final String type;

    private final byte[] value;
    private final String attributes;
    private final String workflowStatus;
    private final String systemAttributes;
    private final String usageTickets;
    private final URI webUrl;

    public EomFile(@JsonProperty("uuid") String uuid,
                   @JsonProperty("type") String type,
                   @JsonProperty("value") byte[] bytes,
                   @JsonProperty("attributes") String attributes,
                   @JsonProperty("workflowStatus") String workflowStatus,
                   @JsonProperty("systemAttributes") String systemAttributes,
                   @JsonProperty("usageTickets") String usageTickets,
                   @JsonProperty("webUrl") URI webUrl) {
        this.uuid = uuid;
        this.type = type;
        this.value = bytes;
        this.attributes = attributes;
        this.workflowStatus = workflowStatus;
        this.systemAttributes = systemAttributes;
        this.usageTickets = usageTickets;
        this.webUrl = webUrl;
    }

    public String getUuid() {
        return uuid;
    }

    public String getType() {
        return type;
    }

    @SuppressWarnings(value = "EI_EXPOSE_REP")
    public byte[] getValue() {
        return value;
    }

    public String getAttributes() {
        return attributes;
    }

    public String getWorkflowStatus() {
        return workflowStatus;
    }

    public String getSystemAttributes() {
        return systemAttributes;
    }

    public String getUsageTickets() {
        return usageTickets;
    }


    public URI getWebUrl() {
        return webUrl;
    }

    public static class Builder {
        private String uuid;
        private String type;
        private byte[] value;
        private String attributes;
        private String workflowStatus;
        private String systemAttributes;
        private String usageTickets;
        private URI webUrl;

        public Builder withUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        @SuppressWarnings(value = "EI_EXPOSE_REP")
        public Builder withValue(byte[] value) {
            this.value = value;
            return this;
        }

        public Builder withAttributes(String attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder withWorkflowStatus(String workflowStatus) {
            this.workflowStatus = workflowStatus;
            return this;
        }

        public Builder withSystemAttributes(String systemAttributes) {
            this.systemAttributes = systemAttributes;
            return this;
        }

        public Builder withUsageTickets(String usageTickets) {
            this.usageTickets = usageTickets;
            return this;
        }

        public Builder withWebUrl(URI webUrl) {
            this.webUrl = webUrl;
            return this;
        }

        public Builder withValuesFrom(EomFile eomFile) {
            return withUuid(eomFile.getUuid())
                    .withType(eomFile.getType())
                    .withValue(eomFile.getValue())
                    .withAttributes(eomFile.getAttributes())
                    .withWorkflowStatus(eomFile.getWorkflowStatus())
                    .withSystemAttributes(eomFile.getSystemAttributes())
                    .withUsageTickets(eomFile.getUsageTickets())
                    .withWebUrl(eomFile.getWebUrl());
        }

        public EomFile build() {
            return new EomFile(uuid, type, value, attributes, workflowStatus, systemAttributes, usageTickets, webUrl);
        }
    }
}

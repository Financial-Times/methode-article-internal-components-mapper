package com.ft.methodearticleinternalcomponentsmapper.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.UUID;

import javax.validation.constraints.NotNull;

public class InternalComponents {
    private Topper topper;

    private String uuid;
    private Date lastModified;
    private String publishReference;

    public InternalComponents(@JsonProperty("topper") Topper topper,
                              @JsonProperty("uuid") String uuid,
                              @JsonProperty("lastModified") Date lastModified,
                              @JsonProperty("publishReference") String publishReference) {
        this.topper = topper;
        this.uuid = uuid;
        this.lastModified = lastModified;
        this.publishReference = publishReference;
    }

    public Topper getTopper() {
        return topper;
    }

    public String getUuid() {
        return uuid;
    }

    @NotNull
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'",
            timezone = "UTC"
    )
    public Date getLastModified() {
        return lastModified;
    }

    public String getPublishReference() {
        return publishReference;
    }

    public static InternalComponents.Builder builder() {
        return new InternalComponents.Builder();
    }

    public static class Builder {
        private String uuid;
        private String publishReference;
        private Date lastModified;

        private Topper topper;

        public Builder() {
        }

        public InternalComponents.Builder withUuid(UUID uuid) {
            this.uuid = uuid.toString();
            return this;
        }

        public InternalComponents.Builder withPublishReference(String publishReference) {
            this.publishReference = publishReference;
            return this;
        }

        public InternalComponents.Builder withLastModified(Date lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public InternalComponents.Builder withTopper(Topper topper) {
            this.topper = topper;
            return this;
        }

        public InternalComponents build() {
            return new InternalComponents(topper, uuid, lastModified, publishReference);
        }
    }
}

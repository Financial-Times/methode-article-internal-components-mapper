package com.ft.methodearticleinternalcomponentsmapper.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EomAssetType {

    private final String uuid;
    private final String type;
    private final String sourceCode;
    private final String errorMessage;

    private EomAssetType(@JsonProperty("uuid") String uuid,
                         @JsonProperty("type") String type,
                         @JsonProperty("sourceCode") String sourceCode,
                         @JsonProperty("error") String errorMessage) {
        this.uuid = uuid;
        this.type = type;
        this.sourceCode = sourceCode;
        this.errorMessage = errorMessage;
    }

    public String getUuid() {
        return uuid;
    }

    public String getType() {
        return type;
    }

    public String getSourceCode() {
        return sourceCode;
    }


    public String getErrorMessage() {
        return errorMessage;
    }

    public static class Builder {

        private String uuid = "";
        private String type = "";
        private String sourceCode = "";
        private String errorMessage = "";

        public Builder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder sourceCode(Optional<String> sourceCode) {
            if (sourceCode.isPresent())
                this.sourceCode = sourceCode.get();
            return this;
        }

        public Builder error(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public EomAssetType build() {
            return new EomAssetType(uuid, type, sourceCode, errorMessage);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((errorMessage == null) ? 0 : errorMessage.hashCode());
        result = prime * result
                + ((sourceCode == null) ? 0 : sourceCode.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EomAssetType other = (EomAssetType) obj;

        return Objects.equals(this.errorMessage, other.errorMessage)
                && Objects.equals(this.sourceCode, other.sourceCode)
                && Objects.equals(this.type, other.type)
                && Objects.equals(this.uuid, other.uuid);

    }
}

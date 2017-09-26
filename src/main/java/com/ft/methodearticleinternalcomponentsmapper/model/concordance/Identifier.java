package com.ft.methodearticleinternalcomponentsmapper.model.concordance;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ToStringBuilder;

import static java.util.Objects.requireNonNull;

public class Identifier {

    private String authority;
    private String identifierValue;

    public Identifier(@JsonProperty("authority") String authority, @JsonProperty("identifierValue") String identifierValue) {
        this.authority = requireNonNull(authority);
        this.identifierValue = requireNonNull(identifierValue);
    }

    public String getAuthority() {
        return authority;
    }

    public String getIdentifierValue() {
        return identifierValue;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("authority", authority)
                .append("identifierValue", identifierValue)
                .toString();
    }
}

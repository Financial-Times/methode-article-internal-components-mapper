package com.ft.methodearticleinternalcomponentsmapper.model.concordance;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Concordance {

    private ConceptView concept;
    private Identifier identifier;

    public Concordance(
            @JsonProperty("concept") ConceptView concept,
            @JsonProperty("identifier") Identifier identifier
    ) {
        this.concept = concept;
        this.identifier = identifier;
    }

    @JsonProperty
    public ConceptView getConcept() {
        return concept;
    }

    @JsonProperty
    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("concept", concept)
                .append("identifier", identifier)
                .toString();
    }
}

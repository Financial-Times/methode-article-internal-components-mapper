package com.ft.methodearticleinternalcomponentsmapper.model.concordance;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;

public class Concordances {

    private List<Concordance> concordances;

    public Concordances(@JsonProperty("concordances") List<Concordance> concordances) {
        this.concordances = concordances;
    }

    public List<Concordance> getConcordances() {
        return concordances;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("concordances", concordances)
                .toString();
    }
}

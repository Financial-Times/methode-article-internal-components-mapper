package com.ft.methodearticleinternalcomponentsmapper.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.Objects;

public class AlternativeTitles {

    private final String shortTeaser;

    public static Builder builder() {
        return new Builder();
    }

    private AlternativeTitles(@JsonProperty("shortTeaser") String shortTeaser) {
        this.shortTeaser = shortTeaser;
    }

    public String getShortTeaser() {
        return shortTeaser;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("shortTeaser", shortTeaser).toString();
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (o.getClass() != AlternativeTitles.class)) {
            return false;
        }

        final AlternativeTitles that = (AlternativeTitles) o;

        return Objects.equals(this.shortTeaser, that.shortTeaser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortTeaser);
    }

    public static class Builder {

        private String shortTeaser;

        public Builder withShortTeaser(String title) {
            this.shortTeaser = title;
            return this;
        }

        public Builder withValuesFrom(AlternativeTitles titles) {
            return withShortTeaser(titles.getShortTeaser());
        }

        public AlternativeTitles build() {
            return new AlternativeTitles(shortTeaser);
        }
    }
}

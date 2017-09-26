package com.ft.methodearticleinternalcomponentsmapper.model;


import com.fasterxml.jackson.annotation.JsonProperty;

public class Topper {

    private String headline;
    private String standfirst;
    private String backgroundColour;
    private String layout;

    public Topper(@JsonProperty("headline") String headline,
                  @JsonProperty("standfirst") String standfirst,
                  @JsonProperty("backgroundColour") String backgroundColour,
                  @JsonProperty("layout") String layout) {
        this.headline = headline;
        this.standfirst = standfirst;
        this.backgroundColour = backgroundColour;
        this.layout = layout;
    }

    public String getHeadline() {
        return headline;
    }

    public String getStandfirst() {
        return standfirst;
    }

    public String getBackgroundColour() {
        return backgroundColour;
    }

    public String getLayout() {
        return layout;
    }

}

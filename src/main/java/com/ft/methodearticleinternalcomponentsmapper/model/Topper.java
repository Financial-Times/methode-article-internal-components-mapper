package com.ft.methodearticleinternalcomponentsmapper.model;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Topper {
    private String theme;
    private String backgroundColour;
    private String headline;
    private String standfirst;
    private List<Image> images;

    public Topper(@JsonProperty("theme")String theme,
                  @JsonProperty("backgroundColour")String backgroundColour,
                  @JsonProperty("image")List<Image> images,
                  @JsonProperty("headline")String headline,
                  @JsonProperty("standfirst")String standfirst) {
        this.theme = theme;
        this.backgroundColour = backgroundColour;
        this.headline = headline;
        this.standfirst = standfirst;
        this.images = images;
    }

    public String getTheme() {
        return theme;
    }

    public String getBackgroundColour() {
        return backgroundColour;
    }

    public String getHeadline() {
        return headline;
    }

    public String getStandfirst() {
        return standfirst;
    }

    public List<Image> getImages() {
        return images;
    }
}

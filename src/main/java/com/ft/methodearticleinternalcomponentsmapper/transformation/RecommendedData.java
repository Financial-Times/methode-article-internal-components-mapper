package com.ft.methodearticleinternalcomponentsmapper.transformation;


import java.util.LinkedList;
import java.util.List;

public class RecommendedData {

    private String title;
    private String intro;
    private List<Link> links;

    public RecommendedData () {
        this.links = new LinkedList<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void addLink(String title, String address) {
        this.links.add(new Link(title, address));
    }

    public static class Link {
        String address;
        String title;

        public Link(String title, String address) {
            this.title = title;
            this.address = address;
        }
    }
}
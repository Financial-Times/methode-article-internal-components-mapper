package com.ft.methodearticleinternalcomponentsmapper.transformation;

import java.util.LinkedList;
import java.util.List;

public class BlockquoteData {

    private List<String> paragraphs;
    private String cite;

    public BlockquoteData() {
        paragraphs = new LinkedList<>();
    }

    public void addParagraph(String paragraph) {
        this.paragraphs.add(paragraph);
    }

    public List<String> getParagraphs() {
        return paragraphs;
    }

    public String getCite() {
        return cite;
    }

    public void setCite(String cite) {
        this.cite = cite;
    }
}

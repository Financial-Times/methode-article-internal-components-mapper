package com.ft.methodearticleinternalcomponentsmapper.transformation;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class InteractiveGraphicsMatcher {

    private final List<Pattern> allowedPatterns;

    public InteractiveGraphicsMatcher(final List<String> interactiveGraphicsWhiteList) {
        this.allowedPatterns = compile(interactiveGraphicsWhiteList);
    }

    public boolean matches(final String s) {
        for (final Pattern pattern : allowedPatterns) {
            if (pattern.matcher(s).matches()) {
                return true;
            }
        }
        return false;
    }

    private List<Pattern> compile(List<String> rules) {
        List<Pattern> patterns = new LinkedList<>();
        for (final String rule : rules) {
            patterns.add(Pattern.compile(rule));
        }
        return patterns;
    }
}

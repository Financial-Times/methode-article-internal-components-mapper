package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;
import com.google.common.base.Strings;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import java.util.HashMap;
import java.util.Map;

public class InteractiveGraphicHandler extends BaseXMLEventHandler {

    private static final String A = "a";
    private static final String HREF = "href";
    private static final String SRC = "src";
    private static final String DATA_ASSET_TYPE = "data-asset-type";
    private static final String INTERACTIVE_GRAPHIC = "interactive-graphic";
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String DATA_WIDTH = "data-width";
    private static final String DATA_HEIGHT = "data-height";

    private final InteractiveGraphicsMatcher matcher;
    private final XMLEventHandler fallbackHandler;

    public InteractiveGraphicHandler(final InteractiveGraphicsMatcher interactiveGraphicsMatcher,
            final XMLEventHandler fallbackHandler) {
        this.matcher = interactiveGraphicsMatcher;
        this.fallbackHandler = fallbackHandler;
    }

    @Override
    public void handleStartElementEvent(final StartElement event,
                                        final XMLEventReader xmlEventReader,
                                        final BodyWriter eventWriter,
                                        final BodyProcessingContext bodyProcessingContext) throws XMLStreamException {
        final String url = extractUrl(event);
        if (Strings.isNullOrEmpty(url) ||
                !matcher.matches(url)) {
            fallbackHandler.handleStartElementEvent(event, xmlEventReader, eventWriter, bodyProcessingContext);
            return;
        }
        final Map<String, String> attributesToAdd = new HashMap<>();
        attributesToAdd.put(HREF, url);
        attributesToAdd.put(DATA_ASSET_TYPE, INTERACTIVE_GRAPHIC);
        final String width = extractAttribute(WIDTH, event);
        if (!Strings.isNullOrEmpty(width)) {
            attributesToAdd.put(DATA_WIDTH, width);
        }
        final String height = extractAttribute(HEIGHT, event);
        if (!Strings.isNullOrEmpty(height)) {
            attributesToAdd.put(DATA_HEIGHT, height);
        }
        skipUntilMatchingEndTag(event.getName().toString(), xmlEventReader);
        eventWriter.writeStartTag(A, attributesToAdd);
        eventWriter.writeEndTag(A);
    }

    private String extractUrl(StartElement event) {
        return extractAttribute(SRC, event);
    }

    private String extractAttribute(String measure, StartElement event) {
        Attribute attribute = event.getAttributeByName(QName.valueOf(measure));
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }
}

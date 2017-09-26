package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;
import org.apache.commons.lang.StringUtils;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RecommendedXMLEventHandler extends BaseXMLEventHandler {

    private static final String RECOMMENDED_TAG = "recommended";
    private static final String RECOMMENDED_TITLE_TAG = "recommended-title";
    private static final String INTRO_TAG = "p";
    private static final String LIST_TAG = "ul";
    private static final String LIST_ITEM_TAG = "li";
    private static final String ANCHOR_TAG = "a";
    private static final String HREF_ATTRIBUTE = "href";

    private RecommendedXMLParser recommendedXMLParser;

    public RecommendedXMLEventHandler(RecommendedXMLParser parser) {
        this.recommendedXMLParser = parser;
    }

    @Override
    public void handleStartElementEvent(StartElement startElement,
                                        XMLEventReader xmlEventReader,
                                        BodyWriter eventWriter,
                                        BodyProcessingContext bodyProcessingContext) throws XMLStreamException {
        RecommendedData recommendedData = recommendedXMLParser.parseElementData(startElement, xmlEventReader, bodyProcessingContext);
        if (recommendedData.getLinks().size() > 0) {
            eventWriter.writeStartTag(RECOMMENDED_TAG, noAttributes());
            writeRecommendedTitle(eventWriter, recommendedData);
            writeIntro(eventWriter, recommendedData);
            writeList(eventWriter, recommendedData);
            eventWriter.writeEndTag(RECOMMENDED_TAG);
        }
    }

    private void writeList(BodyWriter eventWriter, RecommendedData recommendedData) {
        eventWriter.writeStartTag(LIST_TAG, noAttributes());
        for (RecommendedData.Link link : recommendedData.getLinks()) {
            eventWriter.writeStartTag(LIST_ITEM_TAG, noAttributes());
            Map<String, String> attributes = new HashMap<>();
            attributes.put(HREF_ATTRIBUTE, link.address);
            writeAnchor(eventWriter, link.title, attributes);
            eventWriter.writeEndTag(LIST_ITEM_TAG);
        }
        eventWriter.writeEndTag(LIST_TAG);
    }

    private Map<String, String> noAttributes() {
        return Collections.emptyMap();
    }


    private void writeRecommendedTitle(BodyWriter eventWriter, RecommendedData recommendedData) {
        writeElement(eventWriter, recommendedData.getTitle(), RECOMMENDED_TITLE_TAG, noAttributes());
    }

    private void writeIntro(BodyWriter eventWriter, RecommendedData recommendedData) {
        writeElementIfDataNotEmpty(eventWriter, recommendedData.getIntro(), INTRO_TAG, noAttributes());
    }

    private void writeAnchor(BodyWriter eventWriter, String title, Map<String, String> attributes) {
        writeElement(eventWriter, title, ANCHOR_TAG, attributes);
    }

    private void writeElement(BodyWriter eventWriter, String dataField, String elementName, Map<String, String> attributes) {
        eventWriter.writeStartTag(elementName, attributes);
        if (StringUtils.isNotEmpty(dataField)) {
            eventWriter.writeRaw(dataField);
        }
        eventWriter.writeEndTag(elementName);
    }

    private void writeElementIfDataNotEmpty(BodyWriter eventWriter, String dataField, String elementName, Map<String, String> attributes) {
        if (StringUtils.isNotEmpty(dataField)) {
            eventWriter.writeStartTag(elementName, attributes);
            eventWriter.writeRaw(dataField);
            eventWriter.writeEndTag(elementName);
        }
    }
}
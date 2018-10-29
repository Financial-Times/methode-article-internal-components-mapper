package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;

import java.util.HashMap;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import static com.google.common.base.Preconditions.checkArgument;

public class DynamicContentXMLEventHandler extends BaseXMLEventHandler {

    private static final String FT_CONTENT_TAG = "ft-content";
    private static final String DYNAMIC_CONTENT_TYPE = "http://www.ft.com/ontology/content/DynamicContent";

    private DynamicContentXMLParser dynamicContentXMLParser;

    public DynamicContentXMLEventHandler(DynamicContentXMLParser dynamicContentXMLParser) {
        checkArgument(dynamicContentXMLParser != null, "dynamicContentXMLParser cannot be null");
        this.dynamicContentXMLParser = dynamicContentXMLParser;
    }

    @Override
    public void handleStartElementEvent(StartElement event,
                                        XMLEventReader xmlEventReader,
                                        BodyWriter eventWriter,
                                        BodyProcessingContext bodyProcessingContext) throws XMLStreamException {
        DynamicContentData dynamicContentData = dynamicContentXMLParser.parseElementData(event, xmlEventReader, bodyProcessingContext);
        if (!dynamicContentData.isAllRequiredDataPresent()) {
            return;
        }

        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("type", DYNAMIC_CONTENT_TYPE);
        attributes.put("id", dynamicContentData.getUuid());
        attributes.put("data-embedded", "true");

        eventWriter.writeStartTag(FT_CONTENT_TAG, attributes);
        eventWriter.writeEndTag(FT_CONTENT_TAG);
    }

}

package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class StripByPredefinedAttributesAndValuesEventHandler extends BaseXMLEventHandler {

    private final XMLEventHandler fallbackEventHandler;
    private final ElementNameAndAttributeValueMatcher matcher;
    private final List<String> attributeValuesList;

    public StripByPredefinedAttributesAndValuesEventHandler(final XMLEventHandler fallbackEventHandler, final ElementNameAndAttributeValueMatcher matcher, List<String> attributeValuesList) {
        checkArgument(fallbackEventHandler != null, "fallbackEventHandler cannot be null");
        checkArgument(matcher != null, "matcher cannot be null");
        checkArgument(attributeValuesList != null, "attributeValuesList cannot be null");
        this.fallbackEventHandler = fallbackEventHandler;
        this.matcher = matcher;
        this.attributeValuesList = attributeValuesList;
    }

    @Override
    public void handleStartElementEvent(final StartElement event, final XMLEventReader xmlEventReader, final BodyWriter eventWriter,
                                        final BodyProcessingContext bodyProcessingContext) throws XMLStreamException {

        final String nameToMatch = event.getName().getLocalPart();

        if (matcher.matchesElementNameAndAttributeValueCriteria(attributeValuesList, event)) {
            skipUntilMatchingEndTag(nameToMatch, xmlEventReader);
        } else {
            fallbackEventHandler.handleStartElementEvent(event, xmlEventReader, eventWriter, bodyProcessingContext);
        }
    }

    @Override // Only called where the start tag used the fallback event handler
    public void handleEndElementEvent(final EndElement event, final XMLEventReader xmlEventReader, final BodyWriter eventWriter) throws XMLStreamException {
        fallbackEventHandler.handleEndElementEvent(event, xmlEventReader, eventWriter);
    }

}


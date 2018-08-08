package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import java.util.Map;

public class MultipleHandlersXMLEventHandler extends BaseXMLEventHandler {

    private Map<String, XMLEventHandler> mappedHandlers;
    private XMLEventHandler fallbackHandler;
    private String attributeName;

    public MultipleHandlersXMLEventHandler(Map<String, XMLEventHandler> mappedHandlers, XMLEventHandler fallbackHandler, String attributeName) {
        this.fallbackHandler = fallbackHandler;
        this.attributeName = attributeName;
        this.mappedHandlers = mappedHandlers;
    }

    @Override
    public void handleStartElementEvent(StartElement event, XMLEventReader xmlEventReader, BodyWriter eventWriter, BodyProcessingContext bodyProcessingContext) throws XMLStreamException {
        Attribute attribute = event.getAttributeByName(new QName(attributeName));
        if (attribute == null) {
            fallbackHandler.handleStartElementEvent(event, xmlEventReader, eventWriter, bodyProcessingContext);
            return;
        }
        XMLEventHandler handler = mappedHandlers.get(attribute.getValue());
        if (handler == null) {
            fallbackHandler.handleStartElementEvent(event, xmlEventReader, eventWriter, bodyProcessingContext);
            return;
        }
        handler.handleStartElementEvent(event, xmlEventReader, eventWriter, bodyProcessingContext);
    }

    @Override
    public void handleEndElementEvent(EndElement event, XMLEventReader xmlEventReader, BodyWriter eventWriter) throws XMLStreamException {
        fallbackHandler.handleEndElementEvent(event, xmlEventReader, eventWriter);
    }
}

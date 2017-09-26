package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The element for which the event was triggered is replaced by a substitute element and the name of the element that
 * was replaced becomes the value of an attribute for the substitute element. All of the original attributes are kept.
 */
public class ReplaceElementXMLEventHandler extends BaseXMLEventHandler {

    private String substituteElement;
    private String attributeName;

    /**
     * @param substituteElement the element that will replace the original element
     * @param attributeName     the attribute which will hold the value of the original element
     */
    public ReplaceElementXMLEventHandler(String substituteElement, String attributeName) {
        this.substituteElement = substituteElement;
        this.attributeName = attributeName;
    }

    @Override
    public void handleStartElementEvent(StartElement startElement, XMLEventReader xmlEventReader, BodyWriter eventWriter, BodyProcessingContext bodyProcessingContext) throws XMLStreamException {
        Iterator attrIterator = startElement.getAttributes();
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put(attributeName, startElement.getName().getLocalPart());
        while (attrIterator.hasNext()) {
            Attribute attribute = (Attribute) attrIterator.next();
            attributes.put(attribute.getName().getLocalPart(), attribute.getValue());
        }

        eventWriter.writeStartTag(substituteElement, attributes);
    }

    @Override
    public void handleEndElementEvent(EndElement endElement, XMLEventReader xmlEventReader, BodyWriter eventWriter) throws XMLStreamException {
        eventWriter.writeEndTag(substituteElement);
    }
}

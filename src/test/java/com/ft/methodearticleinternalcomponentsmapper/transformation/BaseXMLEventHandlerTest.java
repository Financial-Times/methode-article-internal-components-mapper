package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.xml.XMLEventReaderFactory;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.ri.evt.AttributeEventImpl;
import org.codehaus.stax2.ri.evt.CharactersEventImpl;
import org.codehaus.stax2.ri.evt.CommentEventImpl;
import org.codehaus.stax2.ri.evt.EndElementEventImpl;
import org.codehaus.stax2.ri.evt.EntityReferenceEventImpl;
import org.codehaus.stax2.ri.evt.StartElementEventImpl;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseXMLEventHandlerTest {

    protected Characters getCharacters(String characterData) {
        return new CharactersEventImpl(null, characterData, false);
    }

    protected EndElement getEndElement(String elementName) {
        return new EndElementEventImpl(null, new QName(elementName), null);
    }

    protected StartElement getStartElement(String elementName) {
        return StartElementEventImpl.construct(null, new QName(elementName), null, null, null);
    }

    protected EntityReference getEntityReference(String entityReferenceName) {
        return new EntityReferenceEventImpl(null, entityReferenceName);
    }

    protected Comment getComment(String text) {
        return new CommentEventImpl(null, text);
    }


    /**
     * This StartElement is not representative of the start elements that are created by our XMLReader.
     * The correct start element is the new one below getCompactStartElement.
     *
     * @Deprecated
     */
    protected StartElement getStartElementWithAttributes(String elementName, Map<String, String> attributes) {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        for (String key : attributes.keySet()) {
            attributeList.add(getAttribute(key, attributes.get(key)));
        }
        return StartElementEventImpl.construct(null, new QName(elementName), attributeList.iterator(), null, null);
    }

    protected StartElement getCompactStartElement(String xmlString, String matchingStartElement) throws XMLStreamException {
        XMLEventReaderFactory xmlEventReaderFactory = new XMLEventReaderFactory((XMLInputFactory2) XMLInputFactory2.newInstance());
        XMLEventReader reader = xmlEventReaderFactory.createXMLEventReader(xmlString);
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement() && matchingStartElement.equals(event.asStartElement().getName().toString())) {
                return event.asStartElement();
            }
        }
        return null;
    }

    private Attribute getAttribute(String name, String value) {
        return new AttributeEventImpl(null, new QName(name), value, false);
    }

}

package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;
import com.ft.methodearticleinternalcomponentsmapper.exception.BodyTransformationException;
import com.ft.methodearticleinternalcomponentsmapper.util.ApiUriGenerator;
import com.ft.methodearticleinternalcomponentsmapper.util.UriBuilder;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;

public class RewriteLinkXMLEventHandler extends BaseXMLEventHandler {

    private static final String TYPE_ATTRIBUTE_NAME = "type";
    private static final String ID_ATTRIBUTE_NAME = "id";
    private static final String URL_ATTRIBUTE_NAME = "url";

    private static final String TYPE_ATTR_MISSING_MESSAGE = "Type attribute is missing from a %s element";
    private static final String ID_ATTR_MISSING_MESSAGE = "Id attribute is missing from a %s element";

    private final UriBuilder uriBuilder;
    private final String rewriteElementName;
    private final ApiUriGenerator apiUriGenerator;

    public RewriteLinkXMLEventHandler(String rewriteElementName, UriBuilder uriBuilder, ApiUriGenerator apiUriGenerator) {
        this.rewriteElementName = rewriteElementName;
        this.uriBuilder = uriBuilder;
        this.apiUriGenerator = apiUriGenerator;
    }

    public String getRewriteElementName() {
        return rewriteElementName;
    }

    @Override
    public void handleStartElementEvent(final StartElement event, final XMLEventReader xmlEventReader, final BodyWriter eventWriter,
                                        final BodyProcessingContext bodyProcessingContext) throws XMLStreamException {
        String elementName = event.getName().getLocalPart();

        if (hasAttribute(event, TYPE_ATTRIBUTE_NAME)) {
            final Map<String, String> remaining = removeFtContentAttributes(event);

            final String type = getAttribute(event, TYPE_ATTRIBUTE_NAME, String.format(TYPE_ATTR_MISSING_MESSAGE, elementName));
            final String id = getAttribute(event, ID_ATTRIBUTE_NAME, String.format(ID_ATTR_MISSING_MESSAGE, elementName));

            try {
                final String mergedUrl = uriBuilder.mergeUrl(type, id);

                final Map<String, String> rewrittenAttributes = new LinkedHashMap<>();
                rewrittenAttributes.put(TYPE_ATTRIBUTE_NAME, type);
                rewrittenAttributes.put(URL_ATTRIBUTE_NAME, apiUriGenerator.resolve(mergedUrl));
                rewrittenAttributes.putAll(remaining);
                eventWriter.writeStartTag(getRewriteElementName(), rewrittenAttributes);

            } catch (final Exception e) {
                throw new BodyTransformationException("Failed to rewrite the body", e);
            }
        } else {
            // ft-related uses an untyped url for arbitrary destinations
            final String url = getAttribute(event, URL_ATTRIBUTE_NAME, "Missing url atribute on un-typed link");
            eventWriter.writeStartTag(getRewriteElementName(), singletonMap(URL_ATTRIBUTE_NAME, url));
        }
    }

    @Override
    public void handleEndElementEvent(final EndElement event, final XMLEventReader xmlEventReader, final BodyWriter eventWriter) throws XMLStreamException {
        eventWriter.writeEndTag(getRewriteElementName());
    }

    private String getAttribute(final StartElement event, final String name, final String missingValueErrorMessage) {
        final Attribute namedAttribute = event.getAttributeByName(new QName(name));
        if (namedAttribute == null) {
            throw new BodyTransformationException(missingValueErrorMessage);
        }

        final String value = namedAttribute.getValue();
        if (value == null || value.trim().isEmpty()) {
            throw new BodyTransformationException(missingValueErrorMessage);
        }

        return value;
    }

    private boolean hasAttribute(final StartElement event, final String name) {
        final Attribute namedAttribute = event.getAttributeByName(new QName(name));
        return namedAttribute != null;

    }

    private Map<String, String> removeFtContentAttributes(final StartElement event) {
        final Map<String, String> existing = getValidAttributesAndValues(event);
        if (existing == null) {
            return new LinkedHashMap<>();
        } else {
            existing.remove(ID_ATTRIBUTE_NAME);
            existing.remove(TYPE_ATTRIBUTE_NAME);
            existing.remove(URL_ATTRIBUTE_NAME);
            return existing;
        }
    }
}

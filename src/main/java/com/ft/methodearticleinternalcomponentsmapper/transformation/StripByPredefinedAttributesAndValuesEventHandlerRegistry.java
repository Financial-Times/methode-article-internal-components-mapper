package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.xml.eventhandlers.PlainTextHtmlEntityReferenceEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.RetainXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandlerRegistry;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import java.util.ArrayList;
import java.util.List;

public class StripByPredefinedAttributesAndValuesEventHandlerRegistry extends XMLEventHandlerRegistry {

    public StripByPredefinedAttributesAndValuesEventHandlerRegistry() {
        // Default is to check all tags for channel attribute if there is no channel the element is retained
        // If the channel value is either FTcom or !Financial Times the elements are retained
        // If the value is something else the element is removed.

        List<String> channelAttributes = new ArrayList<>();
        channelAttributes.add("FTcom");
        channelAttributes.add("!Financial Times");

        List<String> classAttributes = new ArrayList<>();
        classAttributes.add("@notes");

        XMLEventHandler retainXMLEventHandler = new RetainXMLEventHandler();

        registerDefaultEventHandler(retainElementAndContentsIfValueMatches(channelAttributes, stripElementsAndContentsIfValueMatches(classAttributes, retainXMLEventHandler)));

        registerCharactersEventHandler(new RetainXMLEventHandler());
        registerEntityReferenceEventHandler(new PlainTextHtmlEntityReferenceEventHandler());

        registerStartAndEndElementEventHandler(new RetainXMLEventHandler(), "body");

    }

    public static ElementNameAndAttributeValueMatcher attributeNameMatchesAndValueIsInList(final String attributeName, final List<String> attributesValuesList, final boolean attributeValueMatches) {
        return new ElementNameAndAttributeValueMatcher() {

            @Override
            public boolean matchesElementNameAndAttributeValueCriteria(List<String> attributeValueList, StartElement startElement) {
                final Attribute attribute = startElement.getAttributeByName(new QName(attributeName));
                if (attribute == null) {
                    return false;
                }

                final String startElementAttributeValue = attribute.getValue();
                boolean matchesAttributeValueCriteria = attributeValueMatches;
                for (String attributeValue : attributesValuesList) {
                    if (startElementAttributeValue.equals(attributeValue)) {
                        matchesAttributeValueCriteria = !attributeValueMatches;
                        return matchesAttributeValueCriteria;
                    }
                }
                return matchesAttributeValueCriteria;
            }
        };
    }

    public XMLEventHandler retainElementAndContentsIfValueMatches(List<String> channelAttributes, XMLEventHandler retainXMLEventHandler) {
        return new StripByPredefinedAttributesAndValuesEventHandler(retainXMLEventHandler, attributeNameMatchesAndValueIsInList("channel", channelAttributes, true), channelAttributes);
    }

    public XMLEventHandler stripElementsAndContentsIfValueMatches(List<String> classAttributes, XMLEventHandler retainXMLEventHandler) {
        return new StripByPredefinedAttributesAndValuesEventHandler(retainXMLEventHandler, attributeNameMatchesAndValueIsInList("class", classAttributes, false), classAttributes);
    }

}

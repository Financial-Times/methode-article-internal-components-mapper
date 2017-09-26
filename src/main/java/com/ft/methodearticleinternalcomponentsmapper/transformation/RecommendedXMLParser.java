package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLParser;
import com.ft.bodyprocessing.xml.eventhandlers.UnexpectedElementStructureException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

public class RecommendedXMLParser extends BaseXMLParser<RecommendedData> {

    private static final String RECOMMENDED_TAG = "recommended";
    private static final String RECOMMENDED_TITLE_TAG = "recommended-title";
    private static final String PARAGRAPH_TAG = "p";
    private static final String ANCHOR_ELEMENT = "a";
    private static final String HREF_ATTRIBUTE = "href";

    public RecommendedXMLParser() {
        super(RECOMMENDED_TAG);
    }

    @Override
    public boolean doesTriggerElementContainAllDataNeeded() {
        return false;
    }

    @Override
    public RecommendedData createDataBeanInstance() {
        return new RecommendedData();
    }

    @Override
    protected void populateBean(RecommendedData recommendedData, StartElement nextStartElement, XMLEventReader xmlEventReader, BodyProcessingContext bodyProcessingContext) throws UnexpectedElementStructureException {
        QName elementName = nextStartElement.getName();

        if (isElementNamed(elementName, RECOMMENDED_TITLE_TAG)) {
            recommendedData.setTitle(parseRawContent(RECOMMENDED_TITLE_TAG, xmlEventReader));
        } else if (isElementNamed(elementName, PARAGRAPH_TAG)) {
            recommendedData.setIntro(parseRawContent(PARAGRAPH_TAG, xmlEventReader));
        } else if (isElementNamed(elementName, ANCHOR_ELEMENT)) {
            Attribute hrefAttribute = nextStartElement.getAttributeByName(new QName(HREF_ATTRIBUTE));
            String headline = parseRawContent(ANCHOR_ELEMENT, xmlEventReader);
            if (validHref(hrefAttribute) && validHeadline(headline)) {
                recommendedData.addLink(headline, hrefAttribute.getValue());
            }
        }
    }

    private boolean validHref(Attribute hrefAttribute) {
        return hrefAttribute != null && !hrefAttribute.getValue().trim().isEmpty();
    }

    private boolean validHeadline(String headline) {
        return headline != null && !headline.trim().isEmpty();
    }
}
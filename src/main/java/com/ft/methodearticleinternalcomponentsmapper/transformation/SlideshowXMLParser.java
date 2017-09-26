package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLParser;
import com.ft.bodyprocessing.xml.eventhandlers.UnexpectedElementStructureException;
import com.ft.bodyprocessing.xml.eventhandlers.XmlParser;
import org.apache.commons.lang.StringUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import java.util.ArrayList;
import java.util.List;

public class SlideshowXMLParser extends BaseXMLParser<SlideshowData> implements XmlParser<SlideshowData> {

    private static final String DEFAULT_ELEMENT_NAME = "a";
    private static final QName HREF_QNAME = QName.valueOf("href");
    private static final String UUID_KEY = "uuid";
    private static final QName TITLE_QNAME = QName.valueOf("title");

    public SlideshowXMLParser() {
        super(DEFAULT_ELEMENT_NAME);
    }

    @Override
    public SlideshowData createDataBeanInstance() {
        return new SlideshowData();
    }

    @Override
    public void populateBean(SlideshowData dataBean, StartElement nextStartElement, XMLEventReader xmlEventReader,
                             BodyProcessingContext bodyProcessingContext) throws UnexpectedElementStructureException {
        Attribute hrefElement = nextStartElement.getAttributeByName(HREF_QNAME);
        // Ensure the element contains an HREF attribute
        if (hrefElement != null) {
            String[] attributesSides = StringUtils.splitPreserveAllTokens(hrefElement.getValue(), "?");
            // Ensure that the href contains at least 1 query parameter
            if (attributesSides.length == 2) {
                // Split all query (key/value) parameters found
                String[] attributes = StringUtils.splitPreserveAllTokens(attributesSides[1], "&");

                // Search for the UUID (key/value) parameter, ignore all others
                for (String attribute : attributes) {
                    String[] keyValue = StringUtils.splitPreserveAllTokens(attribute, "=");
                    if (UUID_KEY.equalsIgnoreCase(keyValue[0])) {
                        // ensure there's a key AND a value for the UUID before populating the bean with the UUID data
                        if (keyValue.length == 2) {
                            dataBean.setUuid(keyValue[1]);
                            dataBean.setQueryParams(exceptForUuid(attributes));
                        }
                    }
                }
            }
        }

        Attribute titleElement = nextStartElement.getAttributeByName(TITLE_QNAME);
        if (titleElement != null) {
            dataBean.setTitle(titleElement.getValue());
        }
    }

    private List<String> exceptForUuid(String[] attributes) {
        List<String> attributesExceptForUuid = new ArrayList<>();
        for (String attribute : attributes) {
            String[] keyValue = StringUtils.splitPreserveAllTokens(attribute, "=");
            if (!UUID_KEY.equalsIgnoreCase(keyValue[0])) {
                attributesExceptForUuid.add(attribute);
            }
        }
        return attributesExceptForUuid;
    }

    @Override
    public boolean doesTriggerElementContainAllDataNeeded() {
        return false;
    }

    @Override
    public void transformFieldContentToStructuredFormat(SlideshowData dataBean,
                                                        BodyProcessingContext bodyProcessingContext) {
    }
}

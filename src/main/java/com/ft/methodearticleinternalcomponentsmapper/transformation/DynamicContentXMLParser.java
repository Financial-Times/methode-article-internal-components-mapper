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

public class DynamicContentXMLParser extends BaseXMLParser<DynamicContentData> implements XmlParser<DynamicContentData> {

    private static final String START_ELEMENT_NAME = "a";
    private static final String HREF_QNAME = "href";
    private static final String UUID_PARAM = "uuid";

    public DynamicContentXMLParser() {
        super(START_ELEMENT_NAME);
    }

    @Override
    public boolean doesTriggerElementContainAllDataNeeded() {
        return false;
    }

    @Override
    public DynamicContentData createDataBeanInstance() {
        return new DynamicContentData();
    }

    @Override
    protected void populateBean(DynamicContentData dataBean,
                                StartElement nextStartElement,
                                XMLEventReader xmlEventReader,
                                BodyProcessingContext bodyProcessingContext) throws UnexpectedElementStructureException {
        Attribute hrefElement = nextStartElement.getAttributeByName(QName.valueOf(HREF_QNAME));
        if (hrefElement == null) {
            return;
        }

        String[] splitHref = StringUtils.splitPreserveAllTokens(hrefElement.getValue(), "?");
        if (splitHref.length != 2) {
            return;
        }

        String queryParams = splitHref[1];
        String[] splitQueryParams = StringUtils.splitPreserveAllTokens(queryParams, "&");

        for (String queryParam : splitQueryParams) {
            String[] splitQueryParam = StringUtils.splitPreserveAllTokens(queryParam, "=");
            if (splitQueryParam.length == 2 || UUID_PARAM.equals(splitQueryParam[0])) {
                dataBean.setUuid(splitQueryParam[1]);
            }
        }
    }

    @Override
    public void transformFieldContentToStructuredFormat(DynamicContentData dataBean, BodyProcessingContext bodyProcessingContext) {

    }
}

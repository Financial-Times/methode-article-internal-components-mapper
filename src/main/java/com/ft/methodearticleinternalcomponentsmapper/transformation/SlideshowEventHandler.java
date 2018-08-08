package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XmlParser;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class SlideshowEventHandler extends BaseXMLEventHandler {

    private static final String A_TAG_NAME = "a";
    private static final String HREF_ATTRIBUTE_NAME = "href";

    public static final String DATA_ASSET_TYPE = "data-asset-type";
    public static final String SLIDESHOW = "slideshow";
    public static final String TITLE = "title";
    public static final String DATA_EMBEDDED = "data-embedded";
    public static final String YEP = "true";

    private XmlParser<SlideshowData> slideshowXMLParser;
    private String slideshowUrlTemplate;

    protected SlideshowEventHandler(XmlParser<SlideshowData> slideshowXMLParser,
                                    String slideshowUrlTemplate) {

        checkArgument(slideshowXMLParser != null, "slideshowXMLParser cannot be null");
        checkArgument(slideshowUrlTemplate != null, "slideshowUrlTemplate cannot be null");

        this.slideshowXMLParser = slideshowXMLParser;
        this.slideshowUrlTemplate = slideshowUrlTemplate;
    }

    @Override
    public void handleStartElementEvent(final StartElement event, final XMLEventReader xmlEventReader, final BodyWriter eventWriter,
                                        final BodyProcessingContext bodyProcessingContext) throws XMLStreamException {
        SlideshowData dataBean = parseElementData(event, xmlEventReader, bodyProcessingContext);
        if (dataBean.isAllRequiredDataPresent()) {
            transformFieldContentToStructuredFormat(dataBean, bodyProcessingContext);
            writeSlideshowElement(eventWriter, dataBean);
        }
    }

    private void transformFieldContentToStructuredFormat(SlideshowData dataBean, BodyProcessingContext bodyProcessingContext) {
        slideshowXMLParser.transformFieldContentToStructuredFormat(dataBean, bodyProcessingContext);
    }

    private SlideshowData parseElementData(StartElement startElement, XMLEventReader xmlEventReader,
                                           BodyProcessingContext bodyProcessingContext) throws XMLStreamException {
        return slideshowXMLParser.parseElementData(startElement, xmlEventReader, bodyProcessingContext);
    }

    private void writeSlideshowElement(BodyWriter eventWriter, SlideshowData dataBean) {
        eventWriter.writeStartTag(A_TAG_NAME, getValidAttributes(dataBean));
        eventWriter.writeEndTag(A_TAG_NAME);
    }

    private Map<String, String> getValidAttributes(SlideshowData dataBean) {
        Map<String, String> validAttributes = new HashMap<>();

        String slideshowUrl = String.format(slideshowUrlTemplate, dataBean.getUuid());
        validAttributes.put(HREF_ATTRIBUTE_NAME, slideshowUrl);
        validAttributes.put(DATA_ASSET_TYPE, SLIDESHOW);
        validAttributes.put(DATA_EMBEDDED, YEP); // If we know it's a slideshow, it's embedded. Otherwise it's just a link.
        if (StringUtils.isNotEmpty(dataBean.getTitle())) {
            validAttributes.put(TITLE, dataBean.getTitle());
        }

        return ImmutableMap.copyOf(validAttributes);
    }

}

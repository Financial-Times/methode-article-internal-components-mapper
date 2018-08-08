package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import java.util.HashMap;

import static com.google.common.base.Preconditions.checkArgument;

public class DynamicContentXMLEventHandler extends BaseXMLEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicContentXMLEventHandler.class);

    private static final String FT_CONTENT_TAG = "ft-content";

    private DynamicContentXMLParser dynamicContentXMLParser;

    public DynamicContentXMLEventHandler(DynamicContentXMLParser dynamicContentXMLParser) {
        checkArgument(dynamicContentXMLParser != null, "slideshowXMLParser cannot be null");
        this.dynamicContentXMLParser = dynamicContentXMLParser;
    }

    @Override
    public void handleStartElementEvent(StartElement event,
                                        XMLEventReader xmlEventReader,
                                        BodyWriter eventWriter,
                                        BodyProcessingContext bodyProcessingContext) throws XMLStreamException {
        DynamicContentData dynamicContentData = dynamicContentXMLParser.parseElementData(event, xmlEventReader, bodyProcessingContext);
        if (!dynamicContentData.isAllRequiredDataPresent()) {
            return;
        }

        String apiHost = getApiHostFromContext(bodyProcessingContext);

        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("type", "http://www.ft.com/ontology/content/DynamicContent");
        attributes.put("url", String.format("http://%s/content/%s", apiHost, dynamicContentData.getUuid()));
        attributes.put("data-embedded", "true");

        eventWriter.writeStartTag(FT_CONTENT_TAG, attributes);
        eventWriter.writeEndTag(FT_CONTENT_TAG);
    }

    private String getApiHostFromContext(final BodyProcessingContext bodyProcessingContext) {
        if (!(bodyProcessingContext instanceof MappedDataBodyProcessingContext)) {
            LOGGER.warn("No mapped data available in body processing context. Cannot retrieve apiHost");
            return null;
        }

        final MappedDataBodyProcessingContext mappedDataBodyProcessingContext = (MappedDataBodyProcessingContext) bodyProcessingContext;
        final String apiHost = mappedDataBodyProcessingContext.get("apiHost", String.class);

        if (apiHost == null) {
            LOGGER.warn("No apiHost found in the context mapped data");
        }

        return apiHost;
    }
}

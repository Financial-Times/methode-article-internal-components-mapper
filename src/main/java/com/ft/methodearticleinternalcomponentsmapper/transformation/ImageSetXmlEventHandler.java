package com.ft.methodearticleinternalcomponentsmapper.transformation;


import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;
import com.ft.uuidutils.GenerateV3UUID;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import java.util.HashMap;

public class ImageSetXmlEventHandler extends BaseXMLEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageSetXmlEventHandler.class);

    private static final String ID_ATTRIBUTE = "id";
    private static final String FT_CONTENT_TAG = "content";

    @Override
    public void handleStartElementEvent(StartElement event, XMLEventReader xmlEventReader, BodyWriter eventWriter,
                                        BodyProcessingContext bodyProcessingContext) throws XMLStreamException {
        final String uuid = getUuidFromContext(bodyProcessingContext);
        final String imageID = getIdFromImageSet(event);

        if (StringUtils.isNotEmpty(uuid) && StringUtils.isNotEmpty(imageID)) {
            final String generatedUUID = GenerateV3UUID.singleDigested(uuid + imageID).toString();

            HashMap<String, String> attributes = new HashMap<>();
            attributes.put("type", "http://www.ft.com/ontology/content/ImageSet");
            attributes.put("id", generatedUUID);
            attributes.put("data-embedded", "true");

            eventWriter.writeStartTag(FT_CONTENT_TAG, attributes);
            eventWriter.writeEndTag(FT_CONTENT_TAG);
        }

        skipUntilMatchingEndTag(event.getName().getLocalPart(), xmlEventReader);
    }

    private String getUuidFromContext(final BodyProcessingContext bodyProcessingContext) {
        if (!(bodyProcessingContext instanceof MappedDataBodyProcessingContext)) {
            LOGGER.warn("No mapped data available in body processing context. Cannot retrieve host article UUID");
            return null;
        }

        final MappedDataBodyProcessingContext mappedDataBodyProcessingContext = (MappedDataBodyProcessingContext) bodyProcessingContext;
        final String uuid = mappedDataBodyProcessingContext.get("uuid", String.class);

        if (uuid == null) {
            LOGGER.warn("No host article UUID found in the context mapped data");
        }

        return uuid;
    }

    private String getIdFromImageSet(StartElement event) {
        final Attribute idAttribute = event.getAttributeByName(new QName(ID_ATTRIBUTE));
        if (idAttribute == null || StringUtils.isBlank(idAttribute.getValue())) {
            LOGGER.warn("No id attribute or blank for {} required to generate uuid", event.getName().getLocalPart());
            return null;
        }

        return idAttribute.getValue();
    }

}

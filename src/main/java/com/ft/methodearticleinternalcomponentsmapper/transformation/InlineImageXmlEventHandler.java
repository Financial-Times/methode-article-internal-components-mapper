package com.ft.methodearticleinternalcomponentsmapper.transformation;


import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;
import com.ft.uuidutils.DeriveUUID;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ft.uuidutils.DeriveUUID.Salts.IMAGE_SET;

public class InlineImageXmlEventHandler extends BaseXMLEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(InlineImageXmlEventHandler.class);

    private static final String CONTENT_TAG = "ft-content";
    private static final String FILE_REF_ATTRIBUTE = "fileref";
    private static final String IMAGE_SET_TYPE = "http://www.ft.com/ontology/content/ImageSet";
    private static final String UUID_REGEX = ".*uuid=([0-9a-f]{8}\\-[0-9a-f]{4}\\-[0-9a-f]{4}\\-[0-9a-f]{4}\\-[0-9a-f]{12}).*";
    private static final Pattern UUID_REGEX_PATTERN = Pattern.compile(UUID_REGEX);
    private static final String DEFAULT_ATTRIBUTE_DATA_EMBEDDED = "data-embedded";

    @Override
    public void handleStartElementEvent(StartElement event, XMLEventReader xmlEventReader, BodyWriter eventWriter,
                                        BodyProcessingContext bodyProcessingContext) throws XMLStreamException {
        String uuid = getUuidForImage(event);

        if (StringUtils.isNotEmpty(uuid)) {
            String imageSetUuid = DeriveUUID.with(IMAGE_SET).from(UUID.fromString(uuid)).toString();

            HashMap<String, String> attributes = new HashMap<>();
            attributes.put("id", imageSetUuid);
            attributes.put("type", IMAGE_SET_TYPE);
            attributes.put(DEFAULT_ATTRIBUTE_DATA_EMBEDDED, "true");

            eventWriter.writeStartTag(CONTENT_TAG, attributes);
            eventWriter.writeEndTag(CONTENT_TAG);
        }
        skipUntilMatchingEndTag(event.getName().getLocalPart(), xmlEventReader);
    }

    private String getUuidForImage(StartElement event) {
        Attribute fileReferenceAttribute = event.getAttributeByName(new QName(FILE_REF_ATTRIBUTE));
        if (fileReferenceAttribute == null || StringUtils.isBlank(fileReferenceAttribute.getValue())) {
            LOGGER.info("No fileref or blank attribute present for {} required for getting uuid",
                    event.getName().getLocalPart());
            return null;
        }
        String fileReferenceValue = fileReferenceAttribute.getValue();

        Matcher uuidMatcher = UUID_REGEX_PATTERN.matcher(fileReferenceValue);
        if (uuidMatcher.matches()) {
            return uuidMatcher.group(1);
        }

        LOGGER.warn("Image uuid could not be parsed from fileref attribute: {}", fileReferenceValue);
        return null;
    }
}

package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;
import com.google.common.base.Strings;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MethodeVideoXmlEventHandler extends AbstractVideoXmlEventHandler {

    private static final String UUID_REGEX = "[0-9a-f]{8}\\-[0-9a-f]{4}\\-[0-9a-f]{4}\\-[0-9a-f]{4}\\-[0-9a-f]{12}";
    private static final Pattern UUID_PATTERN = Pattern.compile(UUID_REGEX);

    private final String videoIdAttributeName;

    public MethodeVideoXmlEventHandler(String videoIdAttributeName, XMLEventHandler fallbackHandler) {
        super(fallbackHandler);
        this.videoIdAttributeName = videoIdAttributeName;
    }

    @Override
    public String extractVideoId(StartElement event) {
        Attribute attribute = event.getAttributeByName(QName.valueOf(videoIdAttributeName));
        if (attribute == null) {
            return null;
        }
        String videoId = event.getAttributeByName(QName.valueOf(videoIdAttributeName)).getValue();
        return Strings.isNullOrEmpty(videoId) ? null : getAsUUID(videoId);
    }

    private String getAsUUID(String videoId) {
        Matcher matcher = UUID_PATTERN.matcher(videoId);
        return matcher.matches() ? videoId : UUID.nameUUIDFromBytes(videoId.getBytes()).toString();
    }
}

package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;
import com.google.common.base.Strings;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContentVideoXmlEventHandler extends AbstractVideoXmlEventHandler {

    private static final String UUID_REGEX = "_([0-9a-f]{8}\\-[0-9a-f]{4}\\-[0-9a-f]{4}\\-[0-9a-f]{4}\\-[0-9a-f]{12})\\.xml";
    private static final Pattern UUID_PATTERN = Pattern.compile(UUID_REGEX);

    private String videoIdHolderAttribute;

    public ContentVideoXmlEventHandler(String videoIdHolderAttribute, XMLEventHandler fallbackHandler) {
        super(fallbackHandler);
        this.videoIdHolderAttribute = videoIdHolderAttribute;
    }

    @Override
    public String extractVideoId(StartElement event) {
        Attribute attribute = event.getAttributeByName(QName.valueOf(videoIdHolderAttribute));
        if (attribute == null) {
            return null;
        }
        String videoIdHolder = event.getAttributeByName(QName.valueOf(videoIdHolderAttribute)).getValue();
        return getVideoUuid(videoIdHolder);
    }

    private String getVideoUuid(String videoIdHolder) {
        if (Strings.isNullOrEmpty(videoIdHolder)) {
            return null;
        }

        Matcher matcher = UUID_PATTERN.matcher(videoIdHolder);
        return matcher.find() ? matcher.group(1) : null;
    }
}

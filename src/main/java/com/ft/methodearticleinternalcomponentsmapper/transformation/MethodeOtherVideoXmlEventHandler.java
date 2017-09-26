package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.richcontent.RichContentItem;
import com.ft.bodyprocessing.richcontent.Video;
import com.ft.bodyprocessing.richcontent.VideoMatcher;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import java.util.HashMap;
import java.util.Map;

public class MethodeOtherVideoXmlEventHandler extends BaseXMLEventHandler {
    private static final String NEW_ELEMENT = "a";
    private static final String NEW_ELEMENT_ATTRIBUTE = "href";
    private static final String DATA_EMBEDDED = "data-embedded";
    private static final String TRUE = "true";
    private static final String DATA_ASSET_TYPE = "data-asset-type";
    private static final String VIDEO = "video";

    private final XMLEventHandler fallbackHandler;
    private VideoMatcher videoMatcher;

    public MethodeOtherVideoXmlEventHandler(XMLEventHandler fallbackHandler, VideoMatcher videoMatcher) {
        this.fallbackHandler = fallbackHandler;
        this.videoMatcher = videoMatcher;
    }

    @Override
    public void handleStartElementEvent(StartElement event, XMLEventReader xmlEventReader, BodyWriter eventWriter,
                                        BodyProcessingContext bodyProcessingContext) throws XMLStreamException {

        Attribute srcAttribute = event.asStartElement().getAttributeByName(QName.valueOf("src"));

        if(srcAttribute == null){
            fallbackHandler.handleStartElementEvent(event, xmlEventReader, eventWriter, bodyProcessingContext);
            return;
        }

        Video video = convertToVideo(srcAttribute);

        if(video==null) {
            fallbackHandler.handleStartElementEvent(event, xmlEventReader, eventWriter, bodyProcessingContext);
            return;
        }

        Map<String, String> attributesToAdd = new HashMap<>();
        attributesToAdd.put(NEW_ELEMENT_ATTRIBUTE, video.getUrl());
        attributesToAdd.put(DATA_EMBEDDED, TRUE);
        attributesToAdd.put(DATA_ASSET_TYPE, VIDEO);

        eventWriter.writeStartTag(NEW_ELEMENT, attributesToAdd);
        eventWriter.writeEndTag(NEW_ELEMENT);
    }

    public Video convertToVideo(Attribute srcAttribute) {
        String videoLink =  srcAttribute.getValue();
        RichContentItem attachment = new RichContentItem(videoLink, null);
        Video video = videoMatcher.filterVideo(attachment);
        return video;
    }

    @Override
    public void handleEndElementEvent(EndElement event, XMLEventReader xmlEventReader, BodyWriter eventWriter) throws XMLStreamException {
        //only a fallback one should hit this code.
        fallbackHandler.handleEndElementEvent(event, xmlEventReader, eventWriter);
    }

}

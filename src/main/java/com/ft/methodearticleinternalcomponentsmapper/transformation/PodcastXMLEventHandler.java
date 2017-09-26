package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;

import javax.ws.rs.core.UriBuilder;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PodcastXMLEventHandler extends BaseXMLEventHandler {

	private static final String DATA_ASSET_TYPE = "data-asset-type";
	private static final String DATA_EMBEDDED = "data-embedded";
	private static final String TITLE = "title";
	private static final String PODCAST = "podcast";
	private static final String TRUE = "true";
	private BaseXMLEventHandler fallbackHandler;
    private static final String EMBED_REGEX = "(embedLink)(\\()([^\\)]+)(\\))";
    private static final String ANCHOR_TAG = "a";
    private static final String ANCHOR_HREF = "href";


    public PodcastXMLEventHandler(BaseXMLEventHandler fallbackHandler) {
        this.fallbackHandler = fallbackHandler;
    }

    @Override
    public void handleStartElementEvent(StartElement event, XMLEventReader xmlEventReader, BodyWriter eventWriter, BodyProcessingContext bodyProcessingContext) throws XMLStreamException {
        Attribute attribute = event.getAttributeByName(QName.valueOf("type"));
        if (attribute == null || !attribute.getValue().equals("text/javascript")){
            fallbackHandler.handleStartElementEvent(event, xmlEventReader, eventWriter, bodyProcessingContext);
            return;
        }

        String tagbody = getTheCharsToTheEndOfTag(event.getName().toString(), xmlEventReader);


        Pattern pattern = Pattern.compile(EMBED_REGEX);
        Pattern pattern2 = Pattern.compile(EMBED_REGEX);
        Matcher matcher = pattern.matcher(tagbody);
        if(!matcher.find()){
            return;  //by default this ignores this whole block regardless of the fallback and the content
        }

        String match = matcher.group(0);
        Matcher matcher2 = pattern2.matcher(match);
        if (matcher2.find()) {
            if(matcher2.groupCount() < 4){
                return;
            }
            String listOfValues = matcher2.group(3);
            String[] arrOfValues = listOfValues.split(",");
            String podcastAddress = arrOfValues[0].replaceAll("'", "");
            String podcastId = arrOfValues[1].replaceAll("'", "");
			String podcastTitle = arrOfValues[4].replaceAll("'", "");
            URI uri = UriBuilder.fromPath("p")
                    .host(podcastAddress)
                    .path(podcastId)
                    .scheme("http")
                    .build();
            Map<String, String> attributesToAdd = new HashMap<>();
			attributesToAdd.put(ANCHOR_HREF, uri.toString());
			attributesToAdd.put(DATA_ASSET_TYPE, PODCAST);
			attributesToAdd.put(DATA_EMBEDDED, TRUE);
			attributesToAdd.put(TITLE, podcastTitle);
            eventWriter.writeStartTag(ANCHOR_TAG, attributesToAdd);
            eventWriter.writeEndTag(ANCHOR_TAG);
        }
    }

    private String getTheCharsToTheEndOfTag(String primaryElementName, XMLEventReader reader) throws XMLStreamException {

        String characters = "";
        int primaryOpenElementNameCount = 1;
        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (nextEvent.isCharacters()) {
                characters = characters + nextEvent.asCharacters().getData();
            }
            if (nextEvent.isEndElement()) {
                EndElement newEndElement = nextEvent.asEndElement();
                if ((primaryElementName).equals(newEndElement.getName().getLocalPart())) {
                    if (primaryOpenElementNameCount == 1) {
                        return characters;
                    }
                    primaryOpenElementNameCount--;
                }
            }
        }
        throw new BodyProcessingException("Reached end without encountering closing primary tag : " + primaryElementName);
    }

    @Override
    public void handleEndElementEvent(EndElement event, XMLEventReader xmlEventReader, BodyWriter eventWriter) throws XMLStreamException {
        //don't do anything
    }
}

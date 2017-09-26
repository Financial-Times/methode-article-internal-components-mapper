package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PodcastXMLEventHandlerTest extends BaseXMLEventHandlerTest  {

    private PodcastXMLEventHandler eventHandler;

    @Mock private BaseXMLEventHandler fallbackHandler;
    @Mock private XMLEventReader mockXmlEventReader;
    @Mock private BodyWriter mockBodyWriter;
    @Mock private BodyProcessingContext mockBodyProcessingContext;
    @Mock private XMLEvent mockXmlEvent;
    @Mock private Characters mockCharacters;

    private static final String NEW_ELEMENT = "script";
    private static final String TARGETED_CLASS = "type";
    private static final String TARGETED_CLASS_ATTRIBUTE = "text/javascript";
    private static final String INCORRECT_TARGETED_CLASS_ATTRIBUTE = "img";
    private static final String ANCHOR_TAG = "a";
    private static final String ANCHOR_HREF = "href";
    private static final String PODCAST_ADDRESS = "http://podcast.ft.com";
    private static final String PODCAST_ID = "2463";
    private static final String TEXT = "<![CDATA[ */window.onload=function(){embedLink('podcast.ft.com','2463','18','lucy060115.mp3','Golden Flannel of the year award'," +
                                        "'Under Tim Cook’s leadership, Apple succumbed to drivel, says Lucy Kellaway','ep_2463','share_2463');}/* ]]> */";
	private static final String DATA_ASSET_TYPE = "data-asset-type";
	private static final String DATA_EMBEDDED = "data-embedded";
	private static final String TITLE = "title";
	private static final String PODCAST = "podcast";
	private static final String TRUE = "true";

    @Before
    public void setup() throws Exception  {
        eventHandler = new PodcastXMLEventHandler(fallbackHandler);
    }

    @Test
    public void shouldUseFallbackHandlerIfStartElementHasNoAttributes() throws Exception {
        StartElement startElement = getStartElement(NEW_ELEMENT);
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(fallbackHandler).handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
    }

    @Test
    public void shouldUseFallbackHandlerIfStartElementsTypeIsNotTextAndJavascript() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(TARGETED_CLASS, INCORRECT_TARGETED_CLASS_ATTRIBUTE);
        StartElement startElement = getStartElementWithAttributes(NEW_ELEMENT, attributes);
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(fallbackHandler).handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
    }

    @Test
    public void shouldWriteTransformedElementsAndContentToWriter() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(TARGETED_CLASS, TARGETED_CLASS_ATTRIBUTE);
        attributes.put(ANCHOR_TAG, ANCHOR_HREF);
        StartElement startElement = getStartElementWithAttributes(NEW_ELEMENT, attributes);
        EndElement endElement = getEndElement(NEW_ELEMENT);

        String href = PODCAST_ADDRESS + "/p/" + PODCAST_ID;
        Map<String, String> attributesToAdd = new HashMap<>();
        attributesToAdd.put(ANCHOR_HREF, href);
		attributesToAdd.put(DATA_ASSET_TYPE, PODCAST);
		attributesToAdd.put(DATA_EMBEDDED, TRUE);
		attributesToAdd.put(TITLE, "Golden Flannel of the year award");

        getTheCharsToTheEndOfTag(endElement);

        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockBodyWriter).writeStartTag(ANCHOR_TAG, attributesToAdd);
        verify(mockBodyWriter).writeEndTag(ANCHOR_TAG);
    }

    private void getTheCharsToTheEndOfTag(EndElement endElement) throws Exception {
        when(mockXmlEventReader.hasNext()).thenReturn(true).thenReturn(true);
        when(mockXmlEventReader.nextEvent()).thenReturn(mockXmlEvent).thenReturn(endElement);
        when(mockXmlEvent.isCharacters()).thenReturn(true);
        when(mockXmlEvent.asCharacters()).thenReturn(mockCharacters);
        when(mockCharacters.getData()).thenReturn(TEXT);
        when(mockXmlEvent.isEndElement()).thenReturn(false);
    }

}

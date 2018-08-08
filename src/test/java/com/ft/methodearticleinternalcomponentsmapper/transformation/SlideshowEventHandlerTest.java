package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XmlParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SlideshowEventHandlerTest extends BaseXMLEventHandlerTest {

    private SlideshowEventHandler eventHandler;

    private static final String START_ELEMENT_TAG = "a";
    private static final String HREF_ATTRIBUTE_NAME = "href";
    private static final String SLIDESHOW_URL_TEMPLATE = "https://www.ft.com/content/%s";
    private static final String TITLE_STRING = "Type title";

    @Mock private XmlParser<SlideshowData> mockXmlParser;
    @Mock private XMLEventReader mockXMLEventReader;
    @Mock private BodyWriter mockBodyWriter;
    @Mock private BodyProcessingContext mockBodyProcessingContext;
    @Mock private SlideshowData mockSlideshowData;

    @Before
    public void setup(){
        eventHandler = new SlideshowEventHandler(mockXmlParser, SLIDESHOW_URL_TEMPLATE);
    }

    @Test
    public void shouldNotWriteIfNotAllValidDataIsPresent() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(HREF_ATTRIBUTE_NAME, SLIDESHOW_URL_TEMPLATE);
        StartElement startElement = getStartElement(START_ELEMENT_TAG);

        when(mockXmlParser.parseElementData(startElement, mockXMLEventReader, mockBodyProcessingContext)).thenReturn(mockSlideshowData);
        when(mockSlideshowData.isAllRequiredDataPresent()).thenReturn(false);

        eventHandler.handleStartElementEvent(startElement, mockXMLEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockBodyWriter, times(0)).writeStartTag(START_ELEMENT_TAG, attributes);
        verify(mockBodyWriter, times(0)).writeEndTag(START_ELEMENT_TAG);
    }

    @Test
    public void shouldWriteTransformedElementsToWriter() throws Exception{
        Map<String, String> attributes = new HashMap<>();
        attributes.put(SlideshowEventHandler.DATA_ASSET_TYPE, SlideshowEventHandler.SLIDESHOW);
        attributes.put(SlideshowEventHandler.DATA_EMBEDDED, SlideshowEventHandler.YEP);
        attributes.put(SlideshowEventHandler.TITLE, TITLE_STRING);
        attributes.put(HREF_ATTRIBUTE_NAME, String.format(SLIDESHOW_URL_TEMPLATE, "f6062cbc-155a-11e5-9509-00144feabdc0"));
        StartElement startElement = getStartElement(START_ELEMENT_TAG);

        when(mockXmlParser.parseElementData(startElement, mockXMLEventReader, mockBodyProcessingContext)).thenReturn(mockSlideshowData);
        when(mockSlideshowData.getTitle()).thenReturn(TITLE_STRING);
        when(mockSlideshowData.isAllRequiredDataPresent()).thenReturn(true);
        when(mockSlideshowData.getUuid()).thenReturn("f6062cbc-155a-11e5-9509-00144feabdc0");

        eventHandler.handleStartElementEvent(startElement, mockXMLEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockBodyWriter).writeStartTag(START_ELEMENT_TAG, attributes);
        verify(mockBodyWriter).writeEndTag(START_ELEMENT_TAG);
    }
}

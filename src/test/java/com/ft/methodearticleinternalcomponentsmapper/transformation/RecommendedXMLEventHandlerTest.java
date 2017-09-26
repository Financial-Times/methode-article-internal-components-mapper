package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import org.codehaus.stax2.XMLEventReader2;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.stream.events.StartElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RecommendedXMLEventHandlerTest extends BaseXMLEventHandlerTest {

    private static final String RECOMMENDED_TAG = "recommended";
    private static final String RECOMMENDED_TITLE_TAG = "recommended-title";
    private static final String INTRO_TAG = "p";
    private static final String LIST_TAG = "ul";
    private static final String LIST_ITEM_TAG = "li";
    private static final String ANCHOR_TAG = "a";

    @Mock
    private BodyWriter mockBodyWriter;
    @Mock
    private XMLEventReader2 mockXmlEventReader;
    @Mock
    private BodyProcessingContext mockBodyProcessingContext;
    @Mock
    private RecommendedXMLParser mockRecommendedXMLParser;
    @Mock
    private RecommendedData mockRecommendedData;

    private RecommendedXMLEventHandler eventHandler;

    @Before
    public void setUp() throws Exception {
        eventHandler = new RecommendedXMLEventHandler(mockRecommendedXMLParser);
    }

    @Test
    public void testShouldNotWriteRecommendedTagIfNoLinksArePresent() throws Exception {
        StartElement startElement = getStartElementWithAttributes(RECOMMENDED_TAG, noAttributes());
        when(mockRecommendedXMLParser.parseElementData(startElement, mockXmlEventReader, mockBodyProcessingContext)).thenReturn(mockRecommendedData);
        when(mockRecommendedData.getLinks()).thenReturn(Collections.emptyList());

        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);

        verify(mockBodyWriter, times(0)).writeStartTag(RECOMMENDED_TAG, noAttributes());
        verify(mockBodyWriter, times(0)).writeEndTag(RECOMMENDED_TAG);
    }

    @Test
    public void testShouldWriteRecommendedTitleTagIfTitleIsEmpty() throws Exception {
        StartElement startElement = getStartElementWithAttributes(RECOMMENDED_TAG, noAttributes());
        when(mockRecommendedXMLParser.parseElementData(startElement, mockXmlEventReader, mockBodyProcessingContext)).thenReturn(mockRecommendedData);
        when(mockRecommendedData.getLinks()).thenReturn(Collections.singletonList(new RecommendedData.Link("", "")));

        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);

        verify(mockBodyWriter).writeStartTag(RECOMMENDED_TITLE_TAG, noAttributes());
        verify(mockBodyWriter).writeEndTag(RECOMMENDED_TITLE_TAG);
    }

    @Test
    public void testShouldWriteRecommendedTitleTagIfTitleIsNotEmpty() throws Exception {
        StartElement startElement = getStartElementWithAttributes(RECOMMENDED_TAG, noAttributes());
        when(mockRecommendedXMLParser.parseElementData(startElement, mockXmlEventReader, mockBodyProcessingContext)).thenReturn(mockRecommendedData);
        when(mockRecommendedData.getLinks()).thenReturn(Collections.singletonList(new RecommendedData.Link("", "")));
        when(mockRecommendedData.getTitle()).thenReturn("Title");

        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);

        verify(mockBodyWriter).writeStartTag(RECOMMENDED_TITLE_TAG, noAttributes());
        verify(mockBodyWriter).writeEndTag(RECOMMENDED_TITLE_TAG);
    }

    @Test
    public void testShouldNotWriteIntroTagIfIntroTextIsMissing() throws Exception {
        StartElement startElement = getStartElementWithAttributes(RECOMMENDED_TAG, noAttributes());
        when(mockRecommendedXMLParser.parseElementData(startElement, mockXmlEventReader, mockBodyProcessingContext)).thenReturn(mockRecommendedData);
        when(mockRecommendedData.getLinks()).thenReturn(Collections.singletonList(new RecommendedData.Link("", "")));
        when(mockRecommendedData.getIntro()).thenReturn("");

        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);

        verify(mockBodyWriter, times(0)).writeStartTag(INTRO_TAG, noAttributes());
        verify(mockBodyWriter, times(0)).writeEndTag(INTRO_TAG);
    }

    @Test
    public void testShouldWriteIntroTagIfIntroTextIsPresent() throws Exception {
        StartElement startElement = getStartElementWithAttributes(RECOMMENDED_TAG, noAttributes());
        when(mockRecommendedXMLParser.parseElementData(startElement, mockXmlEventReader, mockBodyProcessingContext)).thenReturn(mockRecommendedData);
        when(mockRecommendedData.getLinks()).thenReturn(Collections.singletonList(new RecommendedData.Link("", "")));
        when(mockRecommendedData.getIntro()).thenReturn("Intro");

        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);

        verify(mockBodyWriter).writeStartTag(INTRO_TAG, noAttributes());
        verify(mockBodyWriter).writeEndTag(INTRO_TAG);
    }

    @Test
    public void testShouldWriteLinksList() throws Exception {
        StartElement startElement = getStartElementWithAttributes(RECOMMENDED_TAG, noAttributes());
        when(mockRecommendedXMLParser.parseElementData(startElement, mockXmlEventReader, mockBodyProcessingContext)).thenReturn(mockRecommendedData);
        when(mockRecommendedData.getLinks()).thenReturn(Arrays.asList(
                new RecommendedData.Link("Title1", "address1"),
                new RecommendedData.Link("", "address2")));

        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);

        verify(mockBodyWriter).writeStartTag(LIST_TAG, noAttributes());
        verify(mockBodyWriter).writeEndTag(LIST_TAG);
        verify(mockBodyWriter, times(2)).writeStartTag(LIST_ITEM_TAG, noAttributes());
        verify(mockBodyWriter, times(2)).writeEndTag(LIST_ITEM_TAG);
        verify(mockBodyWriter).writeStartTag(ANCHOR_TAG, Collections.singletonMap("href", "address1"));
        verify(mockBodyWriter).writeRaw("Title1");
        verify(mockBodyWriter).writeStartTag(ANCHOR_TAG, Collections.singletonMap("href", "address2"));
        verify(mockBodyWriter, times(0)).writeRaw("");
        verify(mockBodyWriter, times(2)).writeEndTag(ANCHOR_TAG);
    }

    private Map<String, String> noAttributes() {
        return Collections.emptyMap();
    }
}
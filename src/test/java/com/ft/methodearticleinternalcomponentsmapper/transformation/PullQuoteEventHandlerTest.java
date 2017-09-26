package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.writer.BodyWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PullQuoteEventHandlerTest extends BaseXMLEventHandlerTest {

    private PullQuoteEventHandler eventHandler;

    private static final String INCORRECT_ELEMENT = "a";
    private static final String PULL_QUOTE_ELEMENT = "web-pull-quote";
    private static final String TRANSFORMED_PULL_QUOTE = "pull-quote";
    private static final String PULL_QUOTE_TEXT = "text";
    private static final String PULL_QUOTE_SOURCE = "source";
    public static final String PARAGRAPH_TAG = "p";

    @Mock
    private XMLEventReader mockXmlEventReader;
    @Mock
    private BodyWriter mockBodyWriter;
    @Mock
    private BodyProcessingContext mockBodyProcessingContext;
    @Mock
    private PullQuoteXMLParser mockPullQuoteXMLParser;
    @Mock
    private PullQuoteData mockPullQuoteData;

    @Before
    public void setUp() {
        eventHandler = new PullQuoteEventHandler(mockPullQuoteXMLParser);
    }

    @Test(expected = BodyProcessingException.class)
    public void shouldThrowBodyProcessingExceptionIfOpeningTagIsNotPullQuote() throws Exception {
        StartElement startElement = getStartElement(INCORRECT_ELEMENT);
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
    }

    @Test
    public void shouldNotTransformContentIfAllValidDataIsNotPresent() throws Exception {
        StartElement startElement = getStartElementWithAttributes(PULL_QUOTE_ELEMENT, noAttributes());
        when(mockPullQuoteXMLParser.parseElementData(startElement, mockXmlEventReader,
                mockBodyProcessingContext)).thenReturn(mockPullQuoteData);
        when(mockPullQuoteData.isAllRequiredDataPresent()).thenReturn(false);
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockBodyWriter, times(0)).writeStartTag(PULL_QUOTE_ELEMENT, noAttributes());
        verify(mockBodyWriter, times(0)).writeEndTag(PULL_QUOTE_ELEMENT);
    }

    @Test
    public void shouldNotWriteTransformedContentIfAllValidDataIsNotPresentAfterTransformation() throws Exception {
        StartElement startElement = getStartElementWithAttributes(PULL_QUOTE_ELEMENT, noAttributes());
        when(mockPullQuoteXMLParser.parseElementData(startElement, mockXmlEventReader,
                mockBodyProcessingContext)).thenReturn(mockPullQuoteData);
        when(mockPullQuoteData.isAllRequiredDataPresent()).thenReturn(true).thenReturn(false);
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockBodyWriter, times(0)).writeStartTag(PULL_QUOTE_ELEMENT, noAttributes());
        verify(mockBodyWriter, times(0)).writeEndTag(PULL_QUOTE_ELEMENT);
    }

    @Test
    public void shouldWriteTransformedElementsToWriterWhenPtags1() throws Exception {
        when(mockBodyWriter.isPTagCurrentlyOpen()).thenReturn(false);
        shouldWriteTransformedElementsToWriter();
        verify(mockBodyWriter, never()).writeStartTag(PARAGRAPH_TAG, noAttributes());
        verify(mockBodyWriter, never()).writeEndTag(PARAGRAPH_TAG);
    }

    @Test
    public void shouldWriteTransformedElementsToWriterWhenPtags2() throws Exception {
        when(mockBodyWriter.isPTagCurrentlyOpen()).thenReturn(true);
        shouldWriteTransformedElementsToWriter();
        verify(mockBodyWriter).writeStartTag(PARAGRAPH_TAG, noAttributes());
        verify(mockBodyWriter).writeEndTag(PARAGRAPH_TAG);
    }

    private void shouldWriteTransformedElementsToWriter() throws Exception {
        StartElement startElement = getStartElementWithAttributes(PULL_QUOTE_ELEMENT, noAttributes());
        when(mockPullQuoteXMLParser.parseElementData(startElement, mockXmlEventReader,
                mockBodyProcessingContext)).thenReturn(mockPullQuoteData);
        when(mockPullQuoteData.isAllRequiredDataPresent()).thenReturn(true).thenReturn(true);
        when(mockPullQuoteData.getQuoteText()).thenReturn(PULL_QUOTE_TEXT);
        when(mockPullQuoteData.getQuoteSource()).thenReturn(PULL_QUOTE_SOURCE);
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockBodyWriter).writeStartTag(TRANSFORMED_PULL_QUOTE, noAttributes());
        verify(mockBodyWriter).writeRaw(mockPullQuoteData.getQuoteText());
        verify(mockBodyWriter).writeRaw(mockPullQuoteData.getQuoteSource());
        verify(mockBodyWriter).writeEndTag(TRANSFORMED_PULL_QUOTE);
    }

    private Map<String, String> noAttributes() {
        return Collections.emptyMap();
    }
}

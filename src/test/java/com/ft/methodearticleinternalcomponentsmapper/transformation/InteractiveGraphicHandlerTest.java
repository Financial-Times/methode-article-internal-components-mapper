package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InteractiveGraphicHandlerTest extends BaseXMLEventHandlerTest {

    @Mock
    private XMLEventHandler fallbackHandler;
    @Mock
    private XMLEventReader mockXmlEventReader;
    @Mock
    private BodyProcessingContext mockBodyProcessingContext;

    private static final String IFRAME = "iframe";
    private static final String SRC = "src";
    private static final String A = "a";
    private static final String HREF = "href";
    private static final String DATA_ASSET_TYPE = "data-asset-type";
    private static final String INTERACTIVE_GRAPHIC = "interactive-graphic";

    @Test
    public void shouldFallbackWhenSrcNull() throws Exception {
        final InteractiveGraphicsMatcher mockedMatcher = mock(InteractiveGraphicsMatcher.class);
        final InteractiveGraphicHandler eventHandler = new InteractiveGraphicHandler(mockedMatcher, fallbackHandler);
        StartElement startElement = getStartElement(IFRAME);
        final BodyWriter mockBodyWriter = mock(BodyWriter.class);
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(fallbackHandler).handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
    }

    @Test
    public void shouldTransoformMatched() throws Exception {
        final InteractiveGraphicsMatcher mockedMatcher = mock(InteractiveGraphicsMatcher.class);
        final InteractiveGraphicHandler eventHandler = new InteractiveGraphicHandler(mockedMatcher, fallbackHandler);
        when(mockedMatcher.matches(anyString())).thenReturn(true);

        final String url = "http://interactive.ft.com/this-is-a-valid-url";
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(SRC, url);
        final Map<String, String> transformedAttributes = new HashMap<>();
        transformedAttributes.put(HREF, url);
        transformedAttributes.put(DATA_ASSET_TYPE, INTERACTIVE_GRAPHIC);
        StartElement startElement = getStartElementWithAttributes(IFRAME, attributes);
        final BodyWriter mockBodyWriter = mock(BodyWriter.class);
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockBodyWriter).writeStartTag(A, transformedAttributes);
        verify(mockBodyWriter).writeEndTag(A);
    }

    @Test
    public void shouldNotTransoformNotMatched() throws Exception {
        final InteractiveGraphicsMatcher mockedMatcher = mock(InteractiveGraphicsMatcher.class);
        final InteractiveGraphicHandler eventHandler = new InteractiveGraphicHandler(mockedMatcher, fallbackHandler);
        when(mockedMatcher.matches(anyString())).thenReturn(false);
        final String url = "http://interactive.ft.com/this-is-an-invalid-url";
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(SRC, url);
        StartElement startElement = getStartElementWithAttributes(IFRAME, attributes);
        final BodyWriter mockBodyWriter = mock(BodyWriter.class);

        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);

        verify(mockBodyWriter, never()).writeStartTag(eq(A), anyMap());
        verify(mockBodyWriter, never()).writeEndTag(A);
    }

    @Test
    public void shouldTransformWidthAndHeight() throws Exception {
        final Map<String, String> attributes = new HashMap<>();
        final String url = "http://interactive.ft.com/this-is-a-valid-url";
        attributes.put(SRC, url);
        attributes.put("width", "670");
        attributes.put("height", "920");
        final Map<String, String> transformedAttributes = new HashMap<>();
        transformedAttributes.put(HREF, url);
        transformedAttributes.put(DATA_ASSET_TYPE, INTERACTIVE_GRAPHIC);
        transformedAttributes.put("data-width", "670");
        transformedAttributes.put("data-height", "920");
        StartElement startElement = getStartElementWithAttributes(IFRAME, attributes);
        final BodyWriter mockBodyWriter = mock(BodyWriter.class);
        final InteractiveGraphicsMatcher mockedMatcher = mock(InteractiveGraphicsMatcher.class);
        when(mockedMatcher.matches(anyString())).thenReturn(true);
        final InteractiveGraphicHandler eventHandler = new InteractiveGraphicHandler(mockedMatcher, fallbackHandler);

        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);

        verify(mockBodyWriter).writeStartTag(A, transformedAttributes);
        verify(mockBodyWriter).writeEndTag(A);
    }
}

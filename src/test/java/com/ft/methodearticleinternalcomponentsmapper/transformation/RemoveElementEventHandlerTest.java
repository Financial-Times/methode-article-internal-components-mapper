package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.StAXTransformingBodyProcessor;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.RetainXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandlerRegistry;
import com.google.common.collect.ImmutableMap;
import org.codehaus.stax2.XMLEventReader2;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

import static com.ft.methodearticleinternalcomponentsmapper.transformation.MethodeBodyTransformationXMLEventHandlerRegistry.attributeNameMatcher;
import static com.ft.methodearticleinternalcomponentsmapper.transformation.MethodeBodyTransformationXMLEventHandlerRegistry.caselessMatcher;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(value = MockitoJUnitRunner.class)
public class RemoveElementEventHandlerTest extends BaseXMLEventHandlerTest {
    private BaseXMLEventHandler eventHandler;

    @Mock
    private XMLEventReader2 mockXmlEventReader;
    @Mock
    private BodyWriter eventWriter;
    @Mock
    private BodyProcessingContext mockBodyProcessingContext;
    @Mock
    private XMLEventHandler fallbackEventHandler;

    @Before
    public void setup() throws Exception {
        eventHandler = new RemoveElementEventHandler(fallbackEventHandler, attributeNameMatcher("channel"));
        Characters text = getCharacters("text");
        when(mockXmlEventReader.peek()).thenReturn(text);
        when(mockXmlEventReader.next()).thenReturn(text);
    }

    @Test
    public void testRemoveUnwantedSlideshow() throws Exception {
        final XMLEventHandler fallback = new RetainXMLEventHandler();
        final RemoveElementEventHandler unit = new RemoveElementEventHandler(fallback, caselessMatcher("type", "slideshow"));
        final XMLEventHandlerRegistry registry = new XMLEventHandlerRegistry() {{
            this.registerStartElementEventHandler(unit, "p", "a");
        }};
        final StAXTransformingBodyProcessor processor = new StAXTransformingBodyProcessor(registry);
        final String xml = "<p><a type=\"slideshow\" href=\"http://example.com/\">Some content</a> and some unlinked content</p>";
        final String actual = processor.process(xml, null);
        assertEquals("<p/>", actual);
    }

    @Test
    public void testRetainWantedAnchor() throws Exception {
        final XMLEventHandler fallback = new RetainXMLEventHandler();
        final RemoveElementEventHandler unit = new RemoveElementEventHandler(fallback, caselessMatcher("type", "slideshow"));
        final XMLEventHandlerRegistry registry = new XMLEventHandlerRegistry() {{
            this.registerCharactersEventHandler(fallback);
            this.registerStartAndEndElementEventHandler(unit, "p", "a");
        }};
        final StAXTransformingBodyProcessor processor = new StAXTransformingBodyProcessor(registry);
        final String xml = "<p><a type=\"leopard\" href=\"http://example.com/\">Some content</a> and some unlinked content</p>";
        final String actual = processor.process(xml, null);
        assertEquals(xml, actual);
    }

    @Test
    public void shouldRemoveStartTagAndContentsUpToMatchingEndTagIfStrikeoutAttributePresent() throws Exception {
        ImmutableMap<String, String> attributesMap = ImmutableMap.of("channel", "!");
        StartElement startElement = getStartElementWithAttributes("p", attributesMap);
        when(mockXmlEventReader.hasNext()).thenReturn(true, true, false);
        //"<p channel="!">Some text in <i>italics</i> and some not</p>"
        when(mockXmlEventReader.nextEvent()).thenReturn(getCharacters("Some text in "), getStartElement("i"),
                getCharacters("italics"), getEndElement("i"),
                getCharacters(" and some note"), getEndElement("p"));
        when(mockXmlEventReader.peek()).thenReturn(getCharacters("Some text in "), getStartElement("i"),
                getCharacters("italics"), getEndElement("i"),
                getCharacters(" and some note"), getEndElement("p"));
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, eventWriter, mockBodyProcessingContext);
        verifyZeroInteractions(eventWriter);
    }

    @Test
    public void shouldUseFallbackHandlerForStartTagIfNoStrikeoutAttribute() throws Exception {
        ImmutableMap<String, String> attributesMap = ImmutableMap.of("title", "!");
        StartElement startElement = getStartElementWithAttributes("p", attributesMap);
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, eventWriter, mockBodyProcessingContext);
        verify(fallbackEventHandler).handleStartElementEvent(startElement, mockXmlEventReader, eventWriter, mockBodyProcessingContext);
    }

    @Test
    public void shouldUseFallbackHandlerForStartTagIfNoAttributes() throws Exception {
        StartElement startElement = getStartElement("p");
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, eventWriter, mockBodyProcessingContext);
        verify(fallbackEventHandler).handleStartElementEvent(startElement, mockXmlEventReader, eventWriter, mockBodyProcessingContext);
    }

    @Test
    public void shouldAlwaysUseFallBackHandlerForEndTag() throws Exception {
        EndElement endElement = getEndElement("p");
        eventHandler.handleEndElementEvent(endElement, mockXmlEventReader, eventWriter);
        verify(fallbackEventHandler).handleEndElementEvent(endElement, mockXmlEventReader, eventWriter);
    }
}

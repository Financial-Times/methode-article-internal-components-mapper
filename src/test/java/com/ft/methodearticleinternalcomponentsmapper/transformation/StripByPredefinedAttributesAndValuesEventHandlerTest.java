package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;
import org.codehaus.stax2.XMLEventReader2;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static com.ft.methodearticleinternalcomponentsmapper.transformation.StripByPredefinedAttributesAndValuesEventHandlerRegistry.attributeNameMatchesAndValueIsInList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;


@RunWith(value=MockitoJUnitRunner.class)
public class StripByPredefinedAttributesAndValuesEventHandlerTest extends BaseXMLEventHandlerTest {
    private BaseXMLEventHandler eventHandler;

    @Mock private XMLEventReader2 mockXmlEventReader;
    @Mock private BodyWriter eventWriter;
    @Mock private StringWriter mockStringWriter;
    @Mock private BodyProcessingContext mockBodyProcessingContext;
    @Mock private XMLEventHandler fallbackEventHandler;
    @Mock private FieldTransformer mockTransformer;

    @Before
    public void setup() throws Exception {
        List<String> channelAttributes = new ArrayList<>();
        channelAttributes.add("FTcom");
        channelAttributes.add("!Financial Times");
        eventHandler = new StripByPredefinedAttributesAndValuesEventHandler(fallbackEventHandler, attributeNameMatchesAndValueIsInList("channel", channelAttributes, true), channelAttributes);
    }

    @Test
    public void shouldUseFallbackHandlerForStartTagIfAttributesDoNotMatch() throws Exception {
        StartElement startElement = getCompactStartElement("<p class=\"\"></p>", "p");
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

    @Test
    public void shouldIgnoreStartTagAndContentsUpToMatchingEndTagIfChannelAttributesAreEmpty() throws Exception {
        StartElement startElement = getCompactStartElement("<span channel=\"\">Some text in <i>italics</i> and some not</span>", "span");
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, eventWriter, mockBodyProcessingContext);
        verifyZeroInteractions(fallbackEventHandler);
        verifyZeroInteractions(eventWriter);
    }

    @Test
    public void shouldIgnoreStartTagAndContentsUpToMatchingEndTagIfChannelAttributesDoNotMatchRetainableValues() throws Exception {
        StartElement startElement = getCompactStartElement("<span channel=\"!\">Some text in <i>italics</i> and some not</span>", "span");
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, eventWriter, mockBodyProcessingContext);
        verifyZeroInteractions(fallbackEventHandler);
        verifyZeroInteractions(eventWriter);
    }

    @Test
     public void shouldRetainStartTagAndContentsUpToMatchingEndTagIfChannelAttributesIsFTcom() throws Exception {
        StartElement startElement = getCompactStartElement("<b channel=\"FTcom\">Some text in <i>italics</i> and some not</b>", "b");
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, eventWriter, mockBodyProcessingContext);
        verify(fallbackEventHandler).handleStartElementEvent(startElement, mockXmlEventReader, eventWriter, mockBodyProcessingContext);
    }

    @Test
    public void shouldRetainStartTagAndContentsUpToMatchingEndTagIfChannelAttributesIsNotFinancialTimes() throws Exception {
        StartElement startElement = getCompactStartElement("<table channel=\"!Financial Times\"><tr><td></td></tr></table>", "table");
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, eventWriter, mockBodyProcessingContext);
        verify(fallbackEventHandler).handleStartElementEvent(startElement, mockXmlEventReader, eventWriter, mockBodyProcessingContext);
    }

    @Test
    public void shouldRetainStartTagAndContentsUpToMatchingEndTagIfContentIsVideo() throws Exception {
        StartElement startElement = getCompactStartElement("<p channel=\"FTcom\"><iframe src=\"http://www.youtube.com/watch?v=wDcAQrgn50o\"></iframe></p>\"", "p");
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, eventWriter, mockBodyProcessingContext);
        verify(fallbackEventHandler).handleStartElementEvent(startElement, mockXmlEventReader, eventWriter, mockBodyProcessingContext);
    }
}
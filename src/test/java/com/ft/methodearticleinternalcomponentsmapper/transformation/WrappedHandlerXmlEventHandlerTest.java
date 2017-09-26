package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.writer.HTML5VoidElementHandlingXMLBodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class WrappedHandlerXmlEventHandlerTest extends BaseXMLEventHandlerTest {


    @Mock private XMLEventReader mockXmlEventReader;
    @Mock private BodyWriter mockBodyWriter;
    @Mock private BodyProcessingContext mockBodyProcessingContext;
    @Mock private XMLEventHandler mockEventHandlerToBeWrapped;

    private WrappedHandlerXmlEventHandler wrappedXmlEventHandler;
    private StartElement startElementEvent;

    @Before
    public void setUp() throws Exception {
        String timelineString = "<body><timeline-image fileref=\"/FT/Graphics/Online/Master_2048x1152/Martin/mas_Microsoft-Surface-tablet--566x318.jpg?uuid=213bb10c-71fe-11e2-8104-002128161462\" height=\"1152\" tmx=\"566 318 164 92\" width=\"2048\" xtransform=\" scale(0.2897527 0.2897527)\"></timeline-image></body>";
        startElementEvent = getCompactStartElement(timelineString, "timeline-image");

        wrappedXmlEventHandler = new WrappedHandlerXmlEventHandler(mockEventHandlerToBeWrapped);
    }

    @Test
    public void shouldCallWrappedEventHandler() throws Exception {
        wrappedXmlEventHandler.handleStartElementEvent(startElementEvent, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockBodyWriter).writeStartTag("timeline-image", noAttributes());
        verify(mockEventHandlerToBeWrapped).handleStartElementEvent(eq(startElementEvent), eq(mockXmlEventReader), any(HTML5VoidElementHandlingXMLBodyWriter.class), eq(mockBodyProcessingContext));
        verify(mockBodyWriter).writeEndTag("timeline-image");
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionIfIOExceptionIsThrown() throws Exception {
        doThrow(new IOException()).when(mockEventHandlerToBeWrapped).handleStartElementEvent(eq(startElementEvent), eq(mockXmlEventReader), any(HTML5VoidElementHandlingXMLBodyWriter.class), eq(mockBodyProcessingContext));
        wrappedXmlEventHandler.handleStartElementEvent(startElementEvent, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);


    }

    private Map<String, String> noAttributes() {
        return Collections.emptyMap();
    }
}
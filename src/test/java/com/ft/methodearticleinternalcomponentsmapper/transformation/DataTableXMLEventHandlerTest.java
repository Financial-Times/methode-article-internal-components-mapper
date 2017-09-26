package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.StripElementAndContentsXMLEventHandler;
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
public class DataTableXMLEventHandlerTest extends BaseXMLEventHandlerTest {

    private DataTableXMLEventHandler eventHandler;

    @Mock
    private StripElementAndContentsXMLEventHandler mockFallBackHandler;
    @Mock
    private XMLEventReader mockXmlEventReader;
    @Mock
    private BodyWriter mockBodyWriter;
    @Mock
    private BodyProcessingContext mockBodyProcessingContext;
    @Mock
    private DataTableXMLParser mockDataTableXMLParser;
    @Mock
    private DataTableData mockDataTableData;

    private static final String DATA_TABLE_BODY = "body";
    private static final String DATA_TABLE_ATTRIBUTE_VALUE = "data-table";
    private static final String DATA_TABLE_ATTRIBUTE_NAME = "class";
    private static final String DATA_TABLE_HTML_ELEMENT_NAME = "table";
    private static final String INCORRECT_HTML_ELEMENT_NAME = "div";
    private static final String P_TAG = "p";

    @Before
    public void setup() throws Exception {
        eventHandler = new DataTableXMLEventHandler(mockDataTableXMLParser, mockFallBackHandler);
    }

    @Test
    public void shouldUseFallbackHandlerIfStartElementIsIncorrectType() throws Exception {
        StartElement startElement = getStartElement(INCORRECT_HTML_ELEMENT_NAME);
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockFallBackHandler).handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
    }

    @Test
    public void shouldExitIfRequiredDataIsNotPresent() throws Exception {
        StartElement startElement = getStartElementWithAttributes(DATA_TABLE_HTML_ELEMENT_NAME, dataTableClass());
        when(mockDataTableXMLParser.parseElementData(startElement, mockXmlEventReader, mockBodyProcessingContext)).thenReturn(mockDataTableData);
        when(mockDataTableData.isAllRequiredDataPresent()).thenReturn(false);
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockDataTableXMLParser, times(0)).transformFieldContentToStructuredFormat(mockDataTableData, mockBodyProcessingContext);
        verify(mockBodyWriter, times(0)).writeStartTag(DATA_TABLE_HTML_ELEMENT_NAME, dataTableClass());
    }

    @Test
    public void shouldNotWriteTransformedTableElementsToWriterInsideOfPTags() throws Exception {
        StartElement startElement = getStartElementWithAttributes(DATA_TABLE_HTML_ELEMENT_NAME, dataTableClass());
        when(mockDataTableXMLParser.parseElementData(startElement, mockXmlEventReader, mockBodyProcessingContext)).thenReturn(mockDataTableData);
        when(mockDataTableData.isAllRequiredDataPresent()).thenReturn(true);
        when(mockBodyWriter.isPTagCurrentlyOpen()).thenReturn(true);
        when(mockDataTableData.getBody()).thenReturn(DATA_TABLE_BODY);
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockBodyWriter).writeEndTag(P_TAG);
        verify(mockBodyWriter).writeStartTag(DATA_TABLE_HTML_ELEMENT_NAME, dataTableClass());
        verify(mockBodyWriter).writeRaw(mockDataTableData.getBody());
        verify(mockBodyWriter).writeEndTag(DATA_TABLE_HTML_ELEMENT_NAME);
        verify(mockBodyWriter).writeStartTag(P_TAG, null);
    }

    @Test
    public void shouldWriteTransformedTableElementsToWriter() throws Exception {
        StartElement startElement = getStartElementWithAttributes(DATA_TABLE_HTML_ELEMENT_NAME, dataTableClass());
        when(mockDataTableXMLParser.parseElementData(startElement, mockXmlEventReader, mockBodyProcessingContext)).thenReturn(mockDataTableData);
        when(mockDataTableData.isAllRequiredDataPresent()).thenReturn(true);
        when(mockDataTableData.getBody()).thenReturn(DATA_TABLE_BODY);
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockBodyWriter).writeStartTag(DATA_TABLE_HTML_ELEMENT_NAME, dataTableClass());
        verify(mockBodyWriter).writeRaw(mockDataTableData.getBody());
        verify(mockBodyWriter).writeEndTag(DATA_TABLE_HTML_ELEMENT_NAME);
    }

    private Map<String, String> dataTableClass() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(DATA_TABLE_ATTRIBUTE_NAME, DATA_TABLE_ATTRIBUTE_VALUE);
        return attributes;
    }
}

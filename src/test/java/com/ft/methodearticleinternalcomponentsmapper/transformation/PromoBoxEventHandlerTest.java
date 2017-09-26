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
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PromoBoxEventHandlerTest extends BaseXMLEventHandlerTest {

    public static final String RELATED_ARTICLE_CLASS = "related-article";
    public static final String CLASS_ARTICLE = "class";
    private PromoBoxEventHandler eventHandler;

    private static final String BIG_NUMBER_ELEMENT = "big-number";
    private static final String PROMO_BOX_ELEMENT = "promo-box";
    private static final String HEADLINE_VALUE = "headline";
    private static final String INTRO_VALUE = "intro";
    private static final String TITLE_VALUE = "title";
    private static final String LINK_VALUE = "<a href=\"http://www.ft.com/cms/s/0/0bdf4bb6-6676-11e4-8bf6-00144feabdc0.html\"/>";
    private static final String METHODE_PROMO_BOX_ELEMENT = "promo-box";
    private static final String INCORRECT_ELEMENT = "a";
    public static final String PARAGRAPH_TAG = "p";

    @Mock
    private XMLEventReader mockXmlEventReader;
    @Mock
    private BodyWriter mockBodyWriter;
    @Mock
    private BodyProcessingContext mockBodyProcessingContext;
    @Mock
    private PromoBoxXMLParser mockPromoBoxXMLParser;
    @Mock
    private PromoBoxData mockPromoBoxData;

    @Before
    public void setUp() {
        eventHandler = new PromoBoxEventHandler(mockPromoBoxXMLParser);
    }

    @Test(expected = BodyProcessingException.class)
    public void shouldThrowBodyProcessingExceptionIfOpeningTagIsNotPromoBox() throws Exception {
        StartElement startElement = getStartElementWithAttributes(INCORRECT_ELEMENT, attributeClassEqualToNumberComponent());
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
    }

    @Test
    public void shouldNotTransformContentIfBigNumberAndAllValidDataIsNotPresent() throws Exception {
        StartElement startElement = getStartElementWithAttributes(METHODE_PROMO_BOX_ELEMENT, attributeClassEqualToNumberComponent());
        when(mockPromoBoxXMLParser.parseElementData(startElement, mockXmlEventReader,
                mockBodyProcessingContext)).thenReturn(mockPromoBoxData);
        when(mockPromoBoxData.isValidBigNumberData()).thenReturn(false);
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockBodyWriter, never()).writeStartTag(BIG_NUMBER_ELEMENT, noAttributes());
        verify(mockBodyWriter, never()).writeEndTag(BIG_NUMBER_ELEMENT);
    }

    @Test
    public void shouldWriteTransformedBigNumberElementsToWriterOutsideClosedPtags() throws Exception {
        when(mockBodyWriter.isPTagCurrentlyOpen()).thenReturn(false);
        shouldWriteTransformedBigNumberElementsToWriter();
        verify(mockBodyWriter, never()).writeStartTag(PARAGRAPH_TAG, noAttributes());
        verify(mockBodyWriter, never()).writeEndTag(PARAGRAPH_TAG);
    }

    @Test
    public void shouldClosePtagsAndWriteTransformedBigNumberElementsToWriterOutsideOfPtags() throws Exception {
        when(mockBodyWriter.isPTagCurrentlyOpen()).thenReturn(true);
        shouldWriteTransformedBigNumberElementsToWriter();
        verify(mockBodyWriter).writeStartTag(PARAGRAPH_TAG, noAttributes());
        verify(mockBodyWriter).writeEndTag(PARAGRAPH_TAG);
    }

    @Test
    public void shouldWriteTransformedPromoBoxElementsToWriterWithTwoElements() throws Exception {
        shouldWriteTransformedPromoBoxElementsToWriter();
        verify(mockBodyWriter).writeEndTag(PROMO_BOX_ELEMENT);
    }

    @Test
    public void shouldWriteTransformedPromoBoxElementsToWriterWithTwoElementsOutsideClosedPtags() throws Exception {
        when(mockBodyWriter.isPTagCurrentlyOpen()).thenReturn(false);
        shouldWriteTransformedPromoBoxElementsToWriter();
        verify(mockBodyWriter, never()).writeEndTag(PARAGRAPH_TAG);
        verify(mockBodyWriter).writeEndTag(PROMO_BOX_ELEMENT);
        verify(mockBodyWriter, never()).writeStartTag(PARAGRAPH_TAG, noAttributes());
    }

    @Test
    public void shouldClosePtagsAndWriteTransformedPromoBoxElementsToWriterWithTwoElementsOutsideOfPtags() throws Exception {
        when(mockBodyWriter.isPTagCurrentlyOpen()).thenReturn(true);
        shouldWriteTransformedPromoBoxElementsToWriter();
        verify(mockBodyWriter).writeEndTag(PARAGRAPH_TAG);
        verify(mockBodyWriter).writeEndTag(PROMO_BOX_ELEMENT);
        verify(mockBodyWriter).writeStartTag(PARAGRAPH_TAG, noAttributes());
    }

    @Test
    public void shouldWriteTransformedPromoBoxElementsToWriterWithFourElements() throws Exception {
        when(mockPromoBoxData.getTitle()).thenReturn(TITLE_VALUE);
        when(mockPromoBoxData.getLink()).thenReturn(LINK_VALUE);
        shouldWriteTransformedPromoBoxElementsToWriter();
        verify(mockBodyWriter).writeRaw(mockPromoBoxData.getTitle());
        verify(mockBodyWriter).writeRaw(mockPromoBoxData.getLink());
        verify(mockBodyWriter).writeEndTag(PROMO_BOX_ELEMENT);
    }

    @Test
    public void shouldWriteTransformedPromoBoxElementsToWriterWithFourElementsOutsideClosedPtags() throws Exception {
        when(mockBodyWriter.isPTagCurrentlyOpen()).thenReturn(false);
        when(mockPromoBoxData.getTitle()).thenReturn(TITLE_VALUE);
        when(mockPromoBoxData.getLink()).thenReturn(LINK_VALUE);
        shouldWriteTransformedPromoBoxElementsToWriter();
        verify(mockBodyWriter, never()).writeEndTag(PARAGRAPH_TAG);
        verify(mockBodyWriter).writeRaw(mockPromoBoxData.getTitle());
        verify(mockBodyWriter).writeRaw(mockPromoBoxData.getLink());
        verify(mockBodyWriter).writeEndTag(PROMO_BOX_ELEMENT);
        verify(mockBodyWriter, never()).writeStartTag(PARAGRAPH_TAG, noAttributes());
    }

    @Test
    public void shouldClosePtagsAndWriteTransformedPromoBoxElementsToWriterWithFourElementsOutsideOfPtags() throws Exception {
        when(mockBodyWriter.isPTagCurrentlyOpen()).thenReturn(true);
        when(mockPromoBoxData.getTitle()).thenReturn(TITLE_VALUE);
        when(mockPromoBoxData.getLink()).thenReturn(LINK_VALUE);
        shouldWriteTransformedPromoBoxElementsToWriter();
        verify(mockBodyWriter).writeEndTag(PARAGRAPH_TAG);
        verify(mockBodyWriter).writeRaw(mockPromoBoxData.getTitle());
        verify(mockBodyWriter).writeRaw(mockPromoBoxData.getLink());
        verify(mockBodyWriter).writeEndTag(PROMO_BOX_ELEMENT);
        verify(mockBodyWriter).writeStartTag(PARAGRAPH_TAG, noAttributes());
    }

    public void shouldWriteTransformedBigNumberElementsToWriter() throws Exception {
        StartElement startElement = getStartElementWithAttributes(METHODE_PROMO_BOX_ELEMENT, attributeClassEqualToNumberComponent());
        when(mockPromoBoxXMLParser.parseElementData(startElement, mockXmlEventReader,
                mockBodyProcessingContext)).thenReturn(mockPromoBoxData);
        when(mockPromoBoxData.isValidBigNumberData()).thenReturn(true);
        when(mockPromoBoxData.getHeadline()).thenReturn(HEADLINE_VALUE);
        when(mockPromoBoxData.getIntro()).thenReturn(INTRO_VALUE);
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockBodyWriter).writeStartTag(BIG_NUMBER_ELEMENT, noAttributes());
        verify(mockBodyWriter).writeRaw(mockPromoBoxData.getHeadline());
        verify(mockBodyWriter).writeRaw(mockPromoBoxData.getIntro());
        verify(mockBodyWriter).writeEndTag(BIG_NUMBER_ELEMENT);
    }

    public void shouldWriteTransformedPromoBoxElementsToWriter() throws Exception {
        StartElement startElement = getStartElementWithAttributes(METHODE_PROMO_BOX_ELEMENT, new HashMap<String, String>());
        when(mockPromoBoxXMLParser.parseElementData(startElement, mockXmlEventReader,
                mockBodyProcessingContext)).thenReturn(mockPromoBoxData);
        when(mockPromoBoxData.isValidPromoBoxData()).thenReturn(true);
        when(mockPromoBoxData.getHeadline()).thenReturn(HEADLINE_VALUE);
        when(mockPromoBoxData.getIntro()).thenReturn(INTRO_VALUE);
        eventHandler.handleStartElementEvent(startElement, mockXmlEventReader, mockBodyWriter, mockBodyProcessingContext);
        verify(mockBodyWriter).writeStartTag(eq(PROMO_BOX_ELEMENT), anyMap());
        verify(mockBodyWriter).writeRaw(mockPromoBoxData.getHeadline());
        verify(mockBodyWriter).writeRaw(mockPromoBoxData.getIntro());
    }


    @Test
    public void shouldPassThroughClassAttributeOnPromoBox() throws Exception {
        when(mockBodyWriter.isPTagCurrentlyOpen()).thenReturn(true);
        when(mockPromoBoxData.getTitle()).thenReturn(TITLE_VALUE);
        when(mockPromoBoxData.getLink()).thenReturn(LINK_VALUE);
        when(mockPromoBoxData.getClassName()).thenReturn(RELATED_ARTICLE_CLASS);
        shouldWriteTransformedPromoBoxElementsToWriter();

        verify(mockBodyWriter).writeStartTag(PROMO_BOX_ELEMENT, singletonMap(CLASS_ARTICLE, RELATED_ARTICLE_CLASS));
    }


    private Map<String, String> noAttributes() {
        return Collections.emptyMap();
    }

    private Map<String, String> attributeClassEqualToNumberComponent() {
        Map<String, String> attributeClassEqualToNumberComponent = new HashMap<>();
        attributeClassEqualToNumberComponent.put(PromoBoxEventHandler.PROMO_CLASS_ATTRIBUTE,
                PromoBoxEventHandler.NUMBERS_COMPONENT_CLASS);
        return attributeClassEqualToNumberComponent;
    }
}

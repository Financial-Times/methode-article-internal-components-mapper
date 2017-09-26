package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.writer.HTML5VoidElementHandlingXMLBodyWriter;
import com.ft.bodyprocessing.xml.StAXTransformingBodyProcessor;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLParser;
import com.ft.bodyprocessing.xml.eventhandlers.XmlParser;
import org.apache.commons.lang.StringUtils;
import org.codehaus.stax2.XMLOutputFactory2;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

public class PromoBoxXMLParser extends BaseXMLParser<PromoBoxData> implements XmlParser<PromoBoxData> {

	private static final String PROMO_INTRO = "promo-intro";
	private static final String PROMO_HEADLINE = "promo-headline";
	private static final String PROMO_BOX = "promo-box";
	private static final String WEB_MASTER = "web-master";
	private static final String PROMO_LINK = "promo-link";
	private static final String PROMO_IMAGE = "promo-image";
	private static final String PROMO_TITLE = "promo-title";

	private StAXTransformingBodyProcessor stAXTransformingBodyProcessor;
	private InlineImageXmlEventHandler inlineImageXmlEventHandler;

	public PromoBoxXMLParser(StAXTransformingBodyProcessor stAXTransformingBodyProcessor,
							 InlineImageXmlEventHandler inlineImageXmlEventHandler) {
		super(PROMO_BOX);
		checkNotNull(stAXTransformingBodyProcessor, "The StAXTransformingBodyProcessor cannot be null.");
		this.stAXTransformingBodyProcessor = stAXTransformingBodyProcessor;
		this.inlineImageXmlEventHandler = inlineImageXmlEventHandler;
	}

	@Override
	public void transformFieldContentToStructuredFormat(PromoBoxData promoBoxData, BodyProcessingContext bodyProcessingContext) {
		// TODO Remove this method when possible, as it is now deprecated.
		throw new IllegalStateException("This method should no longer be called.");
	}

	@Override
	public PromoBoxData createDataBeanInstance() {
		return new PromoBoxData();
	}

	private String transformRawContentToStructuredFormat(String unprocessedContent, BodyProcessingContext bodyProcessingContext) {
		if (!StringUtils.isBlank(unprocessedContent)) {
			return stAXTransformingBodyProcessor.process(unprocessedContent, bodyProcessingContext);
		}
		return "";
	}

	@Override
	protected void populateBean(PromoBoxData promoBoxData, StartElement nextStartElement,
								XMLEventReader xmlEventReader, BodyProcessingContext bodyProcessingContext) {
		// look for either promo-headline or promo-intro
		final QName elementName = nextStartElement.getName();
		
		if (isElementNamed(elementName, PROMO_HEADLINE)) {
			promoBoxData.setHeadline(transformRawContentToStructuredFormat(
			        parseRawContent(PROMO_HEADLINE, xmlEventReader), bodyProcessingContext));
		}
		if (isElementNamed(elementName, PROMO_INTRO)) {
			promoBoxData.setIntro(transformRawContentToStructuredFormat(
					parseRawContent(PROMO_INTRO, xmlEventReader), bodyProcessingContext));
		}
		if (isElementNamed(elementName, PROMO_LINK)) {
			promoBoxData.setLink(parseRawContent(PROMO_LINK, xmlEventReader));
		}
		if (isElementNamed(elementName, PROMO_IMAGE) || isElementNamed(elementName, WEB_MASTER)) {
			try {
				HTML5VoidElementHandlingXMLBodyWriter writer = new HTML5VoidElementHandlingXMLBodyWriter((XMLOutputFactory2) XMLOutputFactory2.newInstance());
				inlineImageXmlEventHandler.handleStartElementEvent(nextStartElement, xmlEventReader, writer, bodyProcessingContext);
				promoBoxData.setImageHtml(writer.asString());
			} catch (XMLStreamException | IOException e) {
				throw new BodyProcessingException(e);
			}
		}
		if (isElementNamed(elementName, PROMO_TITLE)) {
			promoBoxData.setTitle(transformRawContentToStructuredFormat(
			        parseRawContent(PROMO_TITLE, xmlEventReader), bodyProcessingContext));
		}
	}

	@Override
	public boolean doesTriggerElementContainAllDataNeeded() {
		return false;
	}

}

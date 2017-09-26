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

public class PullQuoteXMLParser extends BaseXMLParser<PullQuoteData> implements XmlParser<PullQuoteData> {

	private static final String QUOTE_SOURCE = "web-pull-quote-source";
	private static final String QUOTE_TEXT = "web-pull-quote-text";
	private static final String PULL_QUOTE = "web-pull-quote";
    private static final String WEB_MASTER = "web-master";

	private StAXTransformingBodyProcessor stAXTransformingBodyProcessor;
    private InlineImageXmlEventHandler inlineImageXmlEventHandler;

	public PullQuoteXMLParser(StAXTransformingBodyProcessor stAXTransformingBodyProcessor, InlineImageXmlEventHandler inlineImageXmlEventHandler) {
		super(PULL_QUOTE);
        this.inlineImageXmlEventHandler = inlineImageXmlEventHandler;
        checkNotNull(stAXTransformingBodyProcessor, "The StAXTransformingBodyProcessor cannot be null.");
		this.stAXTransformingBodyProcessor = stAXTransformingBodyProcessor;
	}

	@Override
	public void transformFieldContentToStructuredFormat(PullQuoteData pullQuoteData, BodyProcessingContext bodyProcessingContext) {
		// TODO Remove this method.
		throw new IllegalStateException("This method should no longer be called.");
	}

	@Override
	public PullQuoteData createDataBeanInstance() {
		return new PullQuoteData();
	}

	private String transformRawContentToStructuredFormat(String unprocessedContent, BodyProcessingContext bodyProcessingContext) {
		if (!StringUtils.isBlank(unprocessedContent)) {
			return stAXTransformingBodyProcessor.process(unprocessedContent, bodyProcessingContext);
		}
		return "";
	}

	@Override
	protected void populateBean(PullQuoteData pullQuoteData, StartElement nextStartElement,
								XMLEventReader xmlEventReader, BodyProcessingContext bodyProcessingContext) {

        final QName elementName = nextStartElement.getName();

		// look for either web-pull-quote-text or web-pull-quote-source
		if (isElementNamed(elementName, QUOTE_TEXT)) {
			pullQuoteData.setQuoteText(transformRawContentToStructuredFormat(parseRawContent(QUOTE_TEXT, xmlEventReader), bodyProcessingContext));
		}
		if (isElementNamed(elementName, QUOTE_SOURCE)) {
			pullQuoteData.setQuoteSource(transformRawContentToStructuredFormat(parseRawContent(QUOTE_SOURCE, xmlEventReader), bodyProcessingContext));
		}

        if(isElementNamed(elementName, WEB_MASTER)) {
            try {
                HTML5VoidElementHandlingXMLBodyWriter writer = new HTML5VoidElementHandlingXMLBodyWriter((XMLOutputFactory2) XMLOutputFactory2.newInstance());
                inlineImageXmlEventHandler.handleStartElementEvent(nextStartElement, xmlEventReader, writer, bodyProcessingContext);
                pullQuoteData.setImageHtml(writer.asString());
            } catch (XMLStreamException | IOException e) {
                throw new BodyProcessingException(e);
            }
        }

	}

	@Override
	public boolean doesTriggerElementContainAllDataNeeded() {
		return false;
	}

}

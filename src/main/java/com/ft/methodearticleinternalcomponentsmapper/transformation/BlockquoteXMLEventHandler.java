package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.TransactionIdBodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BlockquoteXMLEventHandler extends BaseXMLEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockquoteXMLEventHandler.class);

    private static final String BLOCKQUOTE_TAG = "blockquote";
    private static final String PARAGRAPH_TAG = "p";
    private static final String CITE_TAG = "cite";

    private BlockquoteXMLParser blockquoteXMLParser;

    public BlockquoteXMLEventHandler(BlockquoteXMLParser blockquoteXMLParser) {
        this.blockquoteXMLParser = blockquoteXMLParser;
    }

    @Override
    public void handleStartElementEvent(StartElement event, XMLEventReader xmlEventReader, BodyWriter eventWriter, BodyProcessingContext bodyProcessingContext) throws XMLStreamException {
        BlockquoteData blockquoteData = blockquoteXMLParser.parseElementData(event, xmlEventReader, bodyProcessingContext);

        List<String> paragraphs = blockquoteData.getParagraphs();
        if (!paragraphs.isEmpty()) {
            eventWriter.writeStartTag(BLOCKQUOTE_TAG, noAttributes());
            for (String paragraph : paragraphs) {
                eventWriter.writeStartTag(PARAGRAPH_TAG, noAttributes());
                eventWriter.writeRaw(paragraph);
                eventWriter.writeEndTag(PARAGRAPH_TAG);
            }
            if (!StringUtils.isBlank(blockquoteData.getCite())) {
                eventWriter.writeStartTag(CITE_TAG, noAttributes());
                eventWriter.writeRaw(blockquoteData.getCite());
                eventWriter.writeEndTag(CITE_TAG);
            }
            eventWriter.writeEndTag(BLOCKQUOTE_TAG);
        } else {
            if (bodyProcessingContext instanceof TransactionIdBodyProcessingContext) {
                TransactionIdBodyProcessingContext ctx = (TransactionIdBodyProcessingContext) bodyProcessingContext;
                LOGGER.warn("Removing blockquote because it does not have any valid paragraph tags. transactionId {}", ctx.getTransactionId());
            }
        }
    }

    private Map<String, String> noAttributes() {
        return Collections.emptyMap();
    }

}

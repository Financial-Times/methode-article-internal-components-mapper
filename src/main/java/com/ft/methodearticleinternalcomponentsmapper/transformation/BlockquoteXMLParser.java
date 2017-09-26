package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.xml.StAXTransformingBodyProcessor;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLParser;
import com.ft.bodyprocessing.xml.eventhandlers.UnexpectedElementStructureException;
import org.apache.commons.lang.StringUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import static com.google.common.base.Preconditions.checkNotNull;

public class BlockquoteXMLParser extends BaseXMLParser<BlockquoteData> {

    private static final String BLOCKQUOTE_TAG = "blockquote";
    private static final String PARAGRAPH_TAG = "p";
    private static final String CITE_TAG = "cite";

    private StAXTransformingBodyProcessor stAXTransformingBodyProcessor;

    public BlockquoteXMLParser(StAXTransformingBodyProcessor stAXTransformingBodyProcessor) {
        super(BLOCKQUOTE_TAG);

        checkNotNull(stAXTransformingBodyProcessor, "The StAXTransformingBodyProcessor cannot be null.");
        this.stAXTransformingBodyProcessor = stAXTransformingBodyProcessor;
    }

    @Override
    public boolean doesTriggerElementContainAllDataNeeded() {
        return false;
    }

    @Override
    public BlockquoteData createDataBeanInstance() {
        return new BlockquoteData();
    }

    @Override
    protected void populateBean(BlockquoteData blockquoteData, StartElement nextStartElement, XMLEventReader xmlEventReader, BodyProcessingContext bodyProcessingContext) throws UnexpectedElementStructureException {
        QName elementName = nextStartElement.getName();

        if(isElementNamed(elementName, PARAGRAPH_TAG)) {
            String content = transformRawContentToStructuredFormat(parseRawContent(PARAGRAPH_TAG, xmlEventReader), bodyProcessingContext);
            if (!StringUtils.isBlank(content)) {
                blockquoteData.addParagraph(content);
            }
        } else if (isElementNamed(elementName, CITE_TAG)) {
            String content = transformRawContentToStructuredFormat(parseRawContent(CITE_TAG, xmlEventReader), bodyProcessingContext);
            if (!StringUtils.isBlank(content)) {
                blockquoteData.setCite(content);
            }
        }
    }

    private String transformRawContentToStructuredFormat(String unprocessedContent, BodyProcessingContext bodyProcessingContext) {
        if (!StringUtils.isBlank(unprocessedContent)) {
            return stAXTransformingBodyProcessor.process(unprocessedContent, bodyProcessingContext);
        }
        return "";
    }
}

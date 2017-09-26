package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.xml.StAXTransformingBodyProcessor;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLParser;
import com.ft.bodyprocessing.xml.eventhandlers.UnexpectedElementStructureException;
import com.ft.bodyprocessing.xml.eventhandlers.XmlParser;
import org.apache.commons.lang.StringUtils;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import static com.google.common.base.Preconditions.checkNotNull;

public class DataTableXMLParser extends BaseXMLParser<DataTableData> implements XmlParser<DataTableData> {

	private static final String START_ELEMENT_NAME = "table";
	private StAXTransformingBodyProcessor stAXTransformingBodyProcessor;

	public DataTableXMLParser(StAXTransformingBodyProcessor stAXTransformingBodyProcessor) {
		super(START_ELEMENT_NAME);

		checkNotNull(stAXTransformingBodyProcessor, "The StAXTransformingBodyProcessor cannot be null.");
		this.stAXTransformingBodyProcessor = stAXTransformingBodyProcessor;
	}

	@Override
	public DataTableData createDataBeanInstance() {
		return new DataTableData();
	}

	@Override
	public void populateBean(DataTableData dataTableData, StartElement nextStartElement, XMLEventReader xmlEventReader,
							 BodyProcessingContext bodyProcessingContext) throws UnexpectedElementStructureException {
		if (isElementNamed(nextStartElement.getName(), START_ELEMENT_NAME)) {
			dataTableData.setBody(transformRawContentToStructuredFormat(parseRawContent(START_ELEMENT_NAME, xmlEventReader, nextStartElement), bodyProcessingContext));
		}
	}

	@Override
	public void transformFieldContentToStructuredFormat(DataTableData dataTableData, BodyProcessingContext bodyProcessingContext) {
		//TODO method to be removed as it is now deprecated.
		throw new IllegalStateException("This method should no longer be used.");
	}

	@Override
	public boolean doesTriggerElementContainAllDataNeeded() {
		return true;
	}

	private String transformRawContentToStructuredFormat(String unprocessedContent, BodyProcessingContext bodyProcessingContext) {
		if (!StringUtils.isBlank(unprocessedContent)) {
			return stAXTransformingBodyProcessor.process(unprocessedContent, bodyProcessingContext);
		}
		return "";
	}

}

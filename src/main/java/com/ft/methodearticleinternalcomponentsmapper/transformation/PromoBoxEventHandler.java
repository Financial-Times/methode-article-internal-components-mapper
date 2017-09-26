package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import java.util.Collections;
import java.util.Map;

import static java.util.Collections.singletonMap;

public class PromoBoxEventHandler extends BaseXMLEventHandler {

	private static final String BIG_NUMBER_ELEMENT = "big-number";
	private static final String PROMO_BOX_ELEMENT = "promo-box";
	private static final String BIG_NUMBER_HEADLINE = "big-number-headline";
	private static final String BIG_NUMBER_INTRO = "big-number-intro";
	private static final String PROMO_BOX_TITLE = "promo-title";
	private static final String PROMO_BOX_HEADLINE = "promo-headline";
	private static final String PROMO_BOX_INTRO = "promo-intro";
	private static final String PROMO_BOX_LINK = "promo-link";
	private static final String PROMO_BOX_IMAGE = "promo-image";
	private static final String PROMO_BOX = "promo-box";
	public static final String NUMBERS_COMPONENT_CLASS = "numbers-component";
	public static final String PROMO_CLASS_ATTRIBUTE = "class";
	public static final String PARAGRAPH_TAG = "p";

	private final PromoBoxXMLParser promoBoxXMLParser;

	public PromoBoxEventHandler(PromoBoxXMLParser promoBoxXMLParser) {
		this.promoBoxXMLParser = promoBoxXMLParser;
	}

	@Override
	public void handleStartElementEvent(StartElement startElement, XMLEventReader xmlEventReader, BodyWriter eventWriter,
										BodyProcessingContext bodyProcessingContext) throws XMLStreamException {

        // Confirm that the startEvent is of the correct type
        if (isPromoBox(startElement)) {

            // Parse the xml needed to create a bean
            PromoBoxData dataBean = parseElementData(startElement, xmlEventReader, bodyProcessingContext);

            // Add asset to the context and create the aside element if all required data is present
            if (promoBoxIsValidBigNumber(startElement, dataBean)) {
                if (eventWriter.isPTagCurrentlyOpen()) {
                    eventWriter.writeEndTag(PARAGRAPH_TAG);
                    writeBigNumber(eventWriter, dataBean);
                    eventWriter.writeStartTag(PARAGRAPH_TAG, noAttributes());
                }
                else {
                    writeBigNumber(eventWriter, dataBean);
                }

            }// Add asset to the context and create the aside element if all required data is present
            else if (dataBean.isValidPromoBoxData()) {
                if (eventWriter.isPTagCurrentlyOpen()) {
                    eventWriter.writeEndTag(PARAGRAPH_TAG);
                    writePromoBox(eventWriter, dataBean);
                    eventWriter.writeStartTag(PARAGRAPH_TAG, noAttributes());
                }
                else {
                    writePromoBox(eventWriter, dataBean);
                }
            }
        }
        else {
            throw new BodyProcessingException("event must correspond to " + PROMO_BOX + " tag");
        }
    }

    private void writePromoBox(BodyWriter eventWriter, PromoBoxData dataBean) {

        if(Strings.isNullOrEmpty(dataBean.getClassName())) {
            eventWriter.writeStartTag(PROMO_BOX_ELEMENT, noAttributes());
        } else {
            eventWriter.writeStartTag(PROMO_BOX_ELEMENT, singletonMap(PROMO_CLASS_ATTRIBUTE, dataBean.getClassName()));
        }

        writePromoBoxElement(eventWriter, dataBean);
        eventWriter.writeEndTag(PROMO_BOX_ELEMENT);
    }

    private void writeBigNumber(BodyWriter eventWriter, PromoBoxData dataBean) {
        eventWriter.writeStartTag(BIG_NUMBER_ELEMENT, noAttributes());
        writeBigNumberElement(eventWriter, dataBean);
        eventWriter.writeEndTag(BIG_NUMBER_ELEMENT);
    }

    private boolean promoBoxIsValidBigNumber(StartElement startElement, PromoBoxData dataBean) {
		Attribute classAttribute = startElement.getAttributeByName(new QName(PROMO_CLASS_ATTRIBUTE));
		return isNumbersComponent(classAttribute) && dataBean.isValidBigNumberData();
	}

	private void writeBigNumberElement(BodyWriter eventWriter, PromoBoxData dataBean) {
		writeElementIfDataNotEmpty(eventWriter, dataBean.getHeadline(), BIG_NUMBER_HEADLINE);
		writeElementIfDataNotEmpty(eventWriter, dataBean.getIntro(), BIG_NUMBER_INTRO);
	}

	private void writePromoBoxElement(BodyWriter eventWriter, PromoBoxData dataBean) {
		writeElementIfDataNotEmpty(eventWriter, dataBean.getTitle(), PROMO_BOX_TITLE);
		writeElementIfDataNotEmpty(eventWriter, dataBean.getHeadline(), PROMO_BOX_HEADLINE);
		writeElementIfDataNotEmpty(eventWriter, dataBean.getImageHtml(), PROMO_BOX_IMAGE);
		writeElementIfDataNotEmpty(eventWriter, dataBean.getIntro(), PROMO_BOX_INTRO);
		writeElementIfDataNotEmpty(eventWriter, dataBean.getLink(), PROMO_BOX_LINK);
	}

	private void writeElementIfDataNotEmpty(BodyWriter eventWriter, String dataField, String elementName) {
		if (StringUtils.isNotEmpty(dataField)) {
			eventWriter.writeStartTag(elementName, noAttributes());
			eventWriter.writeRaw(dataField);
			eventWriter.writeEndTag(elementName);
		}
	}

	private Map<String, String> noAttributes() {
		return Collections.emptyMap();
	}

	protected boolean isPromoBox(StartElement event) {
		return event.getName().getLocalPart().toLowerCase().equals(PROMO_BOX);
	}

	private PromoBoxData parseElementData(StartElement startElement, XMLEventReader xmlEventReader,
										  BodyProcessingContext bodyProcessingContext) throws XMLStreamException {
        PromoBoxData result = promoBoxXMLParser.parseElementData(startElement, xmlEventReader, bodyProcessingContext);

        Attribute attribute = startElement.getAttributeByName(QName.valueOf("class"));
        if(attribute!=null) {
            String className = attribute.getValue();
            result.setClassName(className);
        }

        return result;
	}

	private boolean isNumbersComponent(Attribute classAttribute) {
		return classAttribute != null && NUMBERS_COMPONENT_CLASS.equals(classAttribute.getValue());
	}
}

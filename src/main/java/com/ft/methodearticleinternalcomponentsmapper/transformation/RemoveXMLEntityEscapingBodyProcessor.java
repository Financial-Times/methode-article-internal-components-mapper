package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.BodyProcessor;
import org.apache.commons.lang.StringEscapeUtils;

/*
 * If using an XML-based body processor upstream, this body processor can be used to 
 * unescape any escaped characters (e.g. if you don't want XML in the end)
 */
public class RemoveXMLEntityEscapingBodyProcessor implements BodyProcessor {

	@Override
	public String process(String body, BodyProcessingContext bodyProcessingContext) throws BodyProcessingException {
		if (body != null) {
			return StringEscapeUtils.unescapeXml(body);
		}
		return null;
	}

}

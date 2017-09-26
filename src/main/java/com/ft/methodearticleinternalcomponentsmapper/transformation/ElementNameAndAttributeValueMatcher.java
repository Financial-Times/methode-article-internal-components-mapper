package com.ft.methodearticleinternalcomponentsmapper.transformation;

import javax.xml.stream.events.StartElement;
import java.util.List;

public interface ElementNameAndAttributeValueMatcher {

    boolean matchesElementNameAndAttributeValueCriteria(List<String> attributeValueList, StartElement startElement);
}

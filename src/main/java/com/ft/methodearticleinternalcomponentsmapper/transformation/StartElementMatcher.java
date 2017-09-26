package com.ft.methodearticleinternalcomponentsmapper.transformation;

import javax.xml.stream.events.StartElement;

public interface StartElementMatcher {
	boolean matches(StartElement element);
}

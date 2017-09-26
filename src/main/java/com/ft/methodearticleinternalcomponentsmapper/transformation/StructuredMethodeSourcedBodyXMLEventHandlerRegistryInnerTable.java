package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.xml.eventhandlers.RetainWithSpecificAttributesXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.RetainWithoutAttributesXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.StripElementAndContentsXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.StripXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandlerRegistry;

import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class StructuredMethodeSourcedBodyXMLEventHandlerRegistryInnerTable extends XMLEventHandlerRegistry {

    private Map<String, XMLEventHandler> startElementEventHandlersWrapper = new HashMap<>();
    private Map<String, XMLEventHandler> endElementEventHandlersWrapper = new HashMap<>();

    private MethodeBodyTransformationXMLEventHandlerRegistry handlerRegistry;

    public StructuredMethodeSourcedBodyXMLEventHandlerRegistryInnerTable(MethodeBodyTransformationXMLEventHandlerRegistry structuredBodyXMLEventHandlerRegistry) {
        this.handlerRegistry = structuredBodyXMLEventHandlerRegistry;

        this.registerStartAndEndElementEventHandlerWrapper(new StripElementAndContentsXMLEventHandler(), "videoPlayer", "web-pull-quote", "plainHtml", "web-background-news", "promo-box");

        this.registerStartAndEndElementEventHandlerWrapper(new RetainWithSpecificAttributesXMLEventHandler("colspan", "rowspan"),
                "th", "td");

        this.registerStartAndEndElementEventHandlerWrapper(new RetainWithoutAttributesXMLEventHandler(),
                "thead", "tbody",
                "tfoot", "caption", "tr");

        this.registerStartAndEndElementEventHandlerWrapper(new StripXMLEventHandler(), "table");
    }

    @Override
    public XMLEventHandler getEventHandler(StartElement event) {
        XMLEventHandler eventHandler = this.startElementEventHandlersWrapper.get(event.asStartElement().getName().getLocalPart().toLowerCase());
        if (eventHandler == null) {
            eventHandler = handlerRegistry.getEventHandler(event.asStartElement());
        }
        return eventHandler;
    }

    @Override
    public XMLEventHandler getEventHandler(EndElement event) {
        XMLEventHandler eventHandler = this.endElementEventHandlersWrapper.get(event.asEndElement().getName().getLocalPart().toLowerCase());
        if (eventHandler == null) {
            eventHandler = handlerRegistry.getEventHandler(event.asEndElement());
        }
        return eventHandler;
    }

    @Override
    public XMLEventHandler getEventHandler(Characters event) {
        return handlerRegistry.getEventHandler(event);
    }

    private void registerStartAndEndElementEventHandlerWrapper(XMLEventHandler
                                                                       startEndElementEventHandler, String... names) {
        checkNotNull(startEndElementEventHandler, "startEndElementEventHandler cannot be null");
        checkNotNull(names, "names cannot be null");
        if (names.length == 0) {
            throw new IllegalArgumentException("names cannot be empty");
        }

        for (String name : names) {
            this.startElementEventHandlersWrapper.put(name.toLowerCase(), startEndElementEventHandler);
            this.endElementEventHandlersWrapper.put(name.toLowerCase(), startEndElementEventHandler);
        }
    }
}

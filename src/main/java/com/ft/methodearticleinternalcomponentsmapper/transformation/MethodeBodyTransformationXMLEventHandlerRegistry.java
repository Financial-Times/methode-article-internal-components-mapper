package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.richcontent.VideoMatcher;
import com.ft.bodyprocessing.xml.StAXTransformingBodyProcessor;
import com.ft.bodyprocessing.xml.eventhandlers.LinkTagXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.PlainTextHtmlEntityReferenceEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.RetainWithSpecificAttributesXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.RetainWithoutAttributesXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.RetainXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.SimpleTransformBlockElementEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.SimpleTransformTagXmlEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.StripElementAndContentsXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.StripXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandlerRegistry;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

public class MethodeBodyTransformationXMLEventHandlerRegistry extends XMLEventHandlerRegistry {

    public MethodeBodyTransformationXMLEventHandlerRegistry(final VideoMatcher videoMatcher,
                                                            final InteractiveGraphicsMatcher interactiveGraphicsMatcher) {
        //default is to skip events but leave content - anything not configured below will be handled via this
        registerDefaultEventHandler(new StripXMLEventHandler());
        registerCharactersEventHandler(new RetainXMLEventHandler());
        registerEntityReferenceEventHandler(new PlainTextHtmlEntityReferenceEventHandler());

        // want to be sure to keep the wrapping node
        registerStartAndEndElementEventHandler(new RetainXMLEventHandler(), "body", "concept");

        //rich content
        InlineImageXmlEventHandler inlineImageXmlEventHandler = new InlineImageXmlEventHandler();
        registerStartAndEndElementEventHandler(new PullQuoteEventHandler(new PullQuoteXMLParser(new StAXTransformingBodyProcessor(this), inlineImageXmlEventHandler)), "web-pull-quote");
        registerStartAndEndElementEventHandler(new PromoBoxEventHandler(new PromoBoxXMLParser(new StAXTransformingBodyProcessor(this), inlineImageXmlEventHandler)), "promo-box");
        registerStartAndEndElementEventHandler(new DataTableXMLEventHandler(new DataTableXMLParser(new StAXTransformingBodyProcessor(new StructuredMethodeSourcedBodyXMLEventHandlerRegistryInnerTable(this))), new StripElementAndContentsXMLEventHandler()), "table");
        registerStartElementEventHandler(new BlockquoteXMLEventHandler(new BlockquoteXMLParser(new StAXTransformingBodyProcessor(this))), "blockquote");

        registerStartAndEndElementEventHandler(new MethodeVideoXmlEventHandler("videoid", new StripElementAndContentsXMLEventHandler()), "videoPlayer");
        registerStartAndEndElementEventHandler(new ContentVideoXmlEventHandler("href", new StripElementAndContentsXMLEventHandler()), "content");
        registerStartAndEndElementEventHandler(
                new MethodeOtherVideoXmlEventHandler(
                        new InteractiveGraphicHandler(
                                interactiveGraphicsMatcher,
                                new StripElementAndContentsXMLEventHandler()
                        ),
                        videoMatcher
                ),
                "iframe"
        );
        registerStartAndEndElementEventHandler(new PodcastXMLEventHandler(new StripElementAndContentsXMLEventHandler()), "script");
        registerStartAndEndElementEventHandler(new RetainWithSpecificAttributesXMLEventHandler("src", "alt", "width", "height"), "img");

        //timelines
        registerStartAndEndElementEventHandler(new RetainXMLEventHandler(),
                "timeline", "timeline-header", "timeline-credits",
                "timeline-sources", "timeline-byline", "timeline-item", "timeline-date", "timeline-title",
                "timeline-body"
        );


        // strip html5 tags whose bodies we don't want
        registerStartElementEventHandler(new StripElementAndContentsXMLEventHandler(),
                "applet", "audio", "base", "basefont", "button", "canvas", "caption", "col",
                "colgroup", "command", "datalist", "del", "dir", "embed", "fieldset", "form",
                "frame", "frameset", "head", "input", "keygen", "label", "legend",
                "link", "map", "menu", "meta", "nav", "noframes", "noscript", "object",
                "optgroup", "option", "output", "param", "progress", "rp", "rt", "ruby",
                "s", "select", "source", "strike", "style", "tbody",
                "td", "textarea", "tfoot", "th", "thead", "tr", "track", "video", "wbr", "cite"
        );
        // strip methode tags whose bodies we don't want
        registerStartElementEventHandler(new StripElementAndContentsXMLEventHandler(),
                "annotation", "byline", "editor-choice", "headline", "inlineDwc", "interactive-chart",
                "lead-body", "lead-text", "ln", "photo", "photo-caption", "photo-group",
                "plainHtml", "promo-headline", "promo-image", "promo-intro",
                "promo-link", "promo-title", "promobox-body", "pull-quote", "pull-quote-header",
                "pull-quote-text",
                "readthrough", "short-body", "skybox-body", "stories",
                "story", "strap", "videoObject", "web-alt-picture", "web-background-news",
                "web-background-news-header", "web-background-news-text",
                "web-picture", "web-pull-quote-source", "web-pull-quote-text",
                "web-skybox-picture", "web-subhead", "web-thumbnail", "xref", "xrefs"
        );

        registerStartAndEndElementEventHandler(new SimpleTransformTagXmlEventHandler("h3", "class", "ft-subhead"), "subhead");
        registerStartAndEndElementEventHandler(new SimpleTransformBlockElementEventHandler(new StAXTransformingBodyProcessor(this), "ft-timeline"), "timeline");
        registerStartAndEndElementEventHandler(new ReplaceElementXMLEventHandler("div", "class"), "layout-set", "layout", "layout-slot");

        registerStartElementEventHandler(new RecommendedXMLEventHandler(new RecommendedXMLParser()), "recommended");
        registerStartAndEndElementEventHandler(new ImageSetXmlEventHandler(), "image-set");
        registerStartAndEndElementEventHandler(new InlineImageXmlEventHandler(), "web-inline-picture");
        registerStartAndEndElementEventHandler(new WrappedHandlerXmlEventHandler(new InlineImageXmlEventHandler()), "timeline-image");
        registerStartAndEndElementEventHandler(new SimpleTransformTagXmlEventHandler("strong"), "b");
        registerStartAndEndElementEventHandler(new SimpleTransformTagXmlEventHandler("em"), "i");
        registerStartAndEndElementEventHandler(new RetainWithoutAttributesXMLEventHandler(),
                "strong", "em", "sub", "sup", "br",
                "h1", "h2", "h3", "h4", "h5", "h6",
                "ol", "ul", "li", "p", "hr"
        );

        // Handle slideshows, i.e. where have <a type="slideshow">
        // For these elements if the attribute is missing use the fallback handler
        registerStartAndEndElementEventHandler(new SlideshowEventHandler(new SlideshowXMLParser(), new LinkTagXMLEventHandler("title", "alt"), caselessMatcher("type", "slideshow")), "a");
        registerEndElementEventHandler(new LinkTagXMLEventHandler(), "a");
    }

    public static StartElementMatcher caselessMatcher(final String attributeName, final String attributeValue) {
        return new StartElementMatcher() {
            @Override
            public boolean matches(final StartElement element) {
                final Attribute channel = element.getAttributeByName(new QName(attributeName));
                return (channel == null || !attributeValue.equalsIgnoreCase(channel.getValue())) ? false : true;
            }
        };
    }

    public static StartElementMatcher attributeNameMatcher(final String attributeName) {
        return new StartElementMatcher() {
            @Override
            public boolean matches(final StartElement element) {
                final Attribute channel = element.getAttributeByName(new QName(attributeName));
                return (channel == null) ? false : true;
            }
        };
    }
}

package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.BodyProcessor;
import com.ft.bodyprocessing.BodyProcessorChain;
import com.ft.bodyprocessing.html.Html5SelfClosingTagBodyProcessor;
import com.ft.bodyprocessing.regex.RegexRemoverBodyProcessor;
import com.ft.bodyprocessing.regex.RegexReplacerBodyProcessor;
import com.ft.bodyprocessing.richcontent.VideoMatcher;
import com.ft.bodyprocessing.xml.StAXTransformingBodyProcessor;
import com.ft.bodyprocessing.xml.dom.DOMTransformingBodyProcessor;
import com.ft.bodyprocessing.xml.dom.XPathHandler;
import com.ft.methodearticleinternalcomponentsmapper.clients.ConcordanceApiClient;
import com.ft.methodearticleinternalcomponentsmapper.clients.DocumentStoreApiClient;
import com.ft.methodearticleinternalcomponentsmapper.transformation.xslt.ModularXsltBodyProcessor;
import com.ft.methodearticleinternalcomponentsmapper.transformation.xslt.XsltFile;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class BodyProcessingFieldTransformerFactory implements FieldTransformerFactory {

    private DocumentStoreApiClient documentStoreApiClient;
    private VideoMatcher videoMatcher;
    private InteractiveGraphicsMatcher interactiveGraphicsMatcher;
    private final Map<String, XPathHandler> xpathHandlers;
    private final Map<String, String> contentTypeTemplates;
    private final String apiHost;

    public BodyProcessingFieldTransformerFactory(final DocumentStoreApiClient documentStoreApiClient,
                                                 final VideoMatcher videoMatcher,
                                                 final InteractiveGraphicsMatcher interactiveGraphicsMatcher,
                                                 final Map<String, String> contentTypeTemplates,
                                                 final String apiHost,
                                                 ConcordanceApiClient concordanceApiClient) {
        this.documentStoreApiClient = documentStoreApiClient;
        this.videoMatcher = videoMatcher;
        this.interactiveGraphicsMatcher = interactiveGraphicsMatcher;
        this.contentTypeTemplates = contentTypeTemplates;
        this.apiHost = apiHost;
        xpathHandlers = ImmutableMap.of("//company", new TearSheetLinksTransformer(concordanceApiClient));
    }

    @Override
    public FieldTransformer newInstance() {
        BodyProcessorChain bodyProcessorChain = new BodyProcessorChain(bodyProcessors());
        return new BodyProcessingFieldTransformer(bodyProcessorChain);
    }

    private List<BodyProcessor> bodyProcessors() {
        return asList(
                stripByAttributesAndValuesBodyProcessor(),
                new RegexRemoverBodyProcessor("<(p|li|h[1-6])[^/>]*>(\\s|(<br\\s*/>))*</(p|li|h[1-6])>"),
                new RegexRemoverBodyProcessor("<ul[^/]*>\\s*</ul>"),
                new DOMTransformingBodyProcessor(xpathHandlers),
                stAXTransformingBodyProcessor(),
                new MethodeLinksBodyProcessor(documentStoreApiClient),
                new ModularXsltBodyProcessor(xslts()),
                ftTagsLinksRewriteBodyProcessor(),
                new RegexRemoverBodyProcessor("(<p>)(\\s|(<br\\s*/>))*(</p>)"),
                new RegexReplacerBodyProcessor("</p>(\\r?\\n)+<p>", "</p>" + System.lineSeparator() + "<p>"),
                new RegexReplacerBodyProcessor("</p> +<p>", "</p><p>"),
                new Html5SelfClosingTagBodyProcessor()
        );
    }

    private XsltFile[] xslts() {
        try {
            String related = loadResource("xslt/related.xslt");
            return new XsltFile[]{new XsltFile("related", related)};
        } catch (IOException e) {
            throw new BodyProcessingException(e);
        }
    }

    private String loadResource(String name) throws IOException {
        return Resources.toString(Resources.getResource(this.getClass(), name), Charsets.UTF_8);
    }

    private BodyProcessor stAXTransformingBodyProcessor() {
        return new StAXTransformingBodyProcessor(
                new MethodeBodyTransformationXMLEventHandlerRegistry(videoMatcher, interactiveGraphicsMatcher)
        );
    }

    private BodyProcessor stripByAttributesAndValuesBodyProcessor() {
        return new StAXTransformingBodyProcessor(new StripByPredefinedAttributesAndValuesEventHandlerRegistry());
    }

    private BodyProcessor ftTagsLinksRewriteBodyProcessor() {
        return new StAXTransformingBodyProcessor(new ModelBodyFTTagsLinkRewriteXmlEventHandlerRegistry(contentTypeTemplates, apiHost));
    }
}

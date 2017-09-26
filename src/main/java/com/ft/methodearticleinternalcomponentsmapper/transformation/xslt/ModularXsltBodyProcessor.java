package com.ft.methodearticleinternalcomponentsmapper.transformation.xslt;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.BodyProcessor;
import com.google.common.base.Charsets;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * <p>Applies a list of XSLT files giving priority to templates in the files listed last. Also includes the
 * <a href="https://en.wikipedia.org/wiki/Identity_transform#Using_XSLT">identity transform</a> by default, without which
 * XSLT would strip all markup by default. Instead, all markup is retained.</p>
 * <p>
 * <p><code>&lt;xsl:apply-templates /&gt;</code> in any file can trigger rules in any XSLT, but <i>later rules will
 * have a greater tendency to fire</i>.</p>
 * <p>
 * <p>How? Component sheets are <code>import</code>ed into an empty top level stylesheet. The w3C has a detailed
 * <a href="http://www.w3.org/TR/xslt#import">explanation of import semantics</a> and this (and <code>mode</code>)
 * provides all the template priority ordering behaviour you will ever want.</p>
 * <p>
 * <p>The identity transform is added first.</p>
 * <p>
 * <p>Imports are resolved through the {@link URIResolver} interface.</p>
 *
 * @author Simon Gibbs
 */
public class ModularXsltBodyProcessor implements BodyProcessor {

    private final Map<String, String> files;

    public ModularXsltBodyProcessor(XsltFile... xslts) {
        files = new LinkedHashMap<>();

        // It is important that this goes first (lowest priority) as it provides the default "echo the input to output" behaviour
        files.put(XsltFile.IDENTITY_TRANSFORM.getName(), XsltFile.IDENTITY_TRANSFORM.getContent());

        for (XsltFile file : xslts) {
            files.put(file.getName(), file.getContent());
        }

    }

    @Override
    public String process(String body, BodyProcessingContext bodyProcessingContext) throws BodyProcessingException {

        StringBuilder compositeXslt = new StringBuilder("<xsl:transform xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">");

        for (Map.Entry<String, String> file : files.entrySet()) {
            String xslImport = String.format("<xsl:import href=\"%s\" />", file.getKey());
            compositeXslt.append(xslImport);
        }
        // switch off boilerplate and pretty printing
        compositeXslt.append("<xsl:output method=\"xml\" indent=\"no\" omit-xml-declaration=\"yes\" />");
        compositeXslt.append("</xsl:transform>");

        try {


            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setURIResolver(new ModuleUriResolver(files));

            Source xsltSource = asSource(compositeXslt.toString());

            Transformer transformer = factory.newTransformer(xsltSource);

            ByteArrayOutputStream resultBuffer = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(resultBuffer);

            transformer.transform(asSource(body), result);

            return new String(resultBuffer.toByteArray(), Charsets.UTF_8);


        } catch (TransformerConfigurationException e) {
            throw new BodyProcessingException("Failed to set up XSLT");
        } catch (TransformerException e) {
            throw new BodyProcessingException("Failed to execute composite XSLT");
        }

    }


    /**
     * Resolves keys from the map of "XSLT files" to the text of those files.
     * <p>
     * Because there is a separation between the names used on disk and in the map
     * this resolution amounts simply to retrieving from the map based on the key.
     * <p>
     * Three XLST functions are routed through here: <code>document()</code>, <code>xsl:import</code>,
     * and <code>xsl:include</code>. We use, and support, <code>xsl:import</code> only, but you could
     * get XML from anywhere in theory.
     */
    private class ModuleUriResolver implements URIResolver {

        private final Map<String, String> files;

        public ModuleUriResolver(Map<String, String> files) {
            this.files = files;
        }

        @Override
        public Source resolve(String href, String base) throws TransformerException {
            String file = files.get(href);
            return asSource(file);
        }
    }

    private StreamSource asSource(String text) {
        return new StreamSource(new ByteArrayInputStream(text.getBytes(Charsets.UTF_8)));
    }
}

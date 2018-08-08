package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.BodyProcessor;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class DynamicContentExtractorBodyProcessor implements BodyProcessor {

    private static final String DYNAMIC_CONTENT = "//p/a[@type='DynamicContent']";

    @Override
    public String process(String body, BodyProcessingContext bodyProcessingContext) throws BodyProcessingException {
        if (StringUtils.isBlank(body)) {
            return body;
        }

        try {
            DocumentBuilder documentBuilder = getDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource(new StringReader(body)));
            XPath xPath = XPathFactory.newInstance().newXPath();

            extractDynamicContent(document, xPath);

            body = serializeBody(document);
        } catch (ParserConfigurationException | SAXException | IOException | TransformerException | XPathExpressionException e) {
            throw new BodyProcessingException(e);
        }

        return body;
    }

    private void extractDynamicContent(Document document, XPath xPath) throws XPathExpressionException {
        NodeList dynamicContentList = (NodeList) xPath.compile(DYNAMIC_CONTENT).evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < dynamicContentList.getLength(); i++) {
            Node dynamicContent = dynamicContentList.item(i);
            Node paragraphNode = dynamicContent.getParentNode();
            Node paragraphParentNode = paragraphNode.getParentNode();
            Node dynamicContentCopy = dynamicContent.cloneNode(true);
            paragraphParentNode.insertBefore(dynamicContentCopy, paragraphNode);
            paragraphNode.removeChild(dynamicContent);
        }
    }

    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        return builderFactory.newDocumentBuilder();
    }

    private String serializeBody(Document document) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");

        DOMSource domSource = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);

        transformer.transform(domSource, result);

        writer.flush();
        return writer.toString();
    }
}

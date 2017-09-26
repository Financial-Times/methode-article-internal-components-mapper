package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.api.util.transactionid.TransactionIdUtils;
import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.BodyProcessor;
import com.ft.bodyprocessing.TransactionIdBodyProcessingContext;
import com.ft.jerseyhttpwrapper.ResilientClient;
import com.ft.methodearticleinternalcomponentsmapper.exception.DocumentStoreApiUnmarshallingException;
import com.ft.methodearticleinternalcomponentsmapper.exception.DocumentStoreApiInvalidRequestException;
import com.ft.methodearticleinternalcomponentsmapper.exception.DocumentStoreApiUnavailableException;
import com.ft.methodearticleinternalcomponentsmapper.exception.TransformationException;
import com.ft.methodearticleinternalcomponentsmapper.model.Content;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sun.jersey.api.client.ClientResponse.Status.getFamilyByStatusCode;

public class MethodeLinksBodyProcessor implements BodyProcessor {

    static final String BASE_CONTENT_TYPE = "http://www.ft.com/ontology/content/";
    static final String DEFAULT_CONTENT_TYPE = "http://www.ft.com/ontology/content/Content";

    private static final String CONTENT_TAG = "content";
    private static final String ANCHOR_PREFIX = "#";
    private static final String TYPE = "type";
    private static final String UUID_REGEX = ".*([0-9a-f]{8}\\-[0-9a-f]{4}\\-[0-9a-f]{4}\\-[0-9a-f]{4}\\-[0-9a-f]{12}).*";
    private static final Pattern UUID_REGEX_PATTERN = Pattern.compile(UUID_REGEX);
    private static final String UUID_PARAM_REGEX = ".*uuid=" + UUID_REGEX;
    private static final Pattern UUID_PARAM_REGEX_PATTERN = Pattern.compile(UUID_PARAM_REGEX);
    private static final String FT_COM_URL_REGEX = "^https*:\\/\\/www.ft.com\\/.*";
    private static final Pattern FT_COM_URL_REGEX_PATTERN = Pattern.compile(FT_COM_URL_REGEX);

    private ResilientClient documentStoreApiClient;
    private URI uri;

    public MethodeLinksBodyProcessor(ResilientClient documentStoreApiClient, URI uri) {
        this.documentStoreApiClient = documentStoreApiClient;
        this.uri = uri;
    }

    @Override
    public String process(String body, BodyProcessingContext bodyProcessingContext) throws BodyProcessingException {
        if (StringUtils.isBlank(body)) {
            return body;
        }
        try {
            final DocumentBuilder documentBuilder = getDocumentBuilder();
            final Document document = documentBuilder.parse(new InputSource(new StringReader(body)));

            final Map<Node, String> aTagsToCheck = new HashMap<>();
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final NodeList aTags = (NodeList) xpath.evaluate("//a[count(ancestor::promo-link)=0]", document, XPathConstants.NODESET);
            for (int i = 0; i < aTags.getLength(); i++) {
                final Element aTag = (Element) aTags.item(i);

                if (isRemovable(aTag)) {
                    removeATag(aTag);
                } else {
                    Optional<String> optionalUuid = extractUuid(aTag);
                    optionalUuid.ifPresent(uuid -> aTagsToCheck.put(aTag, uuid));
                }
            }

            if (!(bodyProcessingContext instanceof TransactionIdBodyProcessingContext)) {
                throw new IllegalStateException("bodyProcessingContext should provide transaction id.");
            }

            TransactionIdBodyProcessingContext transactionIdBodyProcessingContext = (TransactionIdBodyProcessingContext) bodyProcessingContext;
            String transactionId = transactionIdBodyProcessingContext.getTransactionId();
            if (StringUtils.isBlank(transactionId)) {
                throw new IllegalStateException("bodyProcessingContext should provide transaction id.");
            }
            final List<Content> content = getContentFromDocumentStore(aTagsToCheck, transactionId);
            processATags(aTagsToCheck, content);
            return serializeBody(document);
        } catch (Exception e) {
            throw new BodyProcessingException(e);
        }
    }

    /**
     * We remove <code>&lt;a&gt;</code> tags
     * <ul>
     * <li>with blank hrefs (which are either invalid, or refer to the current document);</li>
     * <li>with hrefs that contain only a fragment identifier (a part of the current document);</li>
     * <li>with no non-whitespace content <i>and</i> no <code>data-*</code> attributes.</li>
     * </ul>
     *
     * @param aTag the tag
     * @return true if removable, otherwise false.
     */
    private boolean isRemovable(final Node aTag) {
        final String href = getHref(aTag);

        if (href.isEmpty() || href.startsWith(ANCHOR_PREFIX)) {
            return true;
        }

        NodeList children = aTag.getChildNodes();
        int len = children.getLength();

        StringBuilder textContent = new StringBuilder();
        for (int i = 0; i < len; i++) {
            Node n = children.item(i);

            switch (n.getNodeType()) {
                case Node.TEXT_NODE:
                case Node.CDATA_SECTION_NODE:
                    textContent.append(n.getTextContent());
                    break;

                case Node.COMMENT_NODE:
                    break;

                default: // any other node type
                    return false;
            }
        }

        if (!textContent.toString().trim().isEmpty()) {
            return false;
        }

        NamedNodeMap attributes = aTag.getAttributes();
        len = attributes.getLength();

        for (int i = 0; i < len; i++) {
            if (attributes.item(i).getNodeName().startsWith("data-")) {
                return false;
            }
        }

        return true;
    }

    private List<Content> getContentFromDocumentStore(Map<Node, String> tags, String transactionId) {
        if (tags.isEmpty()) {
            return Collections.emptyList();
        }

        Collection<String> uuids = tags.values();
        URI documentsUri = UriBuilder.fromUri(uri).queryParam("mget", true).build();
        ClientResponse clientResponse = null;
        try {
            clientResponse = documentStoreApiClient.resource(documentsUri)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .header(TransactionIdUtils.TRANSACTION_ID_HEADER, transactionId)
                    .header("Host", "document-store-api")
                    .post(ClientResponse.class, uuids);

            int responseStatusCode = clientResponse.getStatus();
            Family statusFamily = getFamilyByStatusCode(responseStatusCode);

            if (statusFamily == Family.SERVER_ERROR) {
                String msg = String.format("Document Store API returned %s", responseStatusCode);
                throw new DocumentStoreApiUnavailableException(msg);
            } else if (statusFamily == Family.CLIENT_ERROR) {
                String msg = String.format("Document Store API returned %s", responseStatusCode);
                throw new DocumentStoreApiInvalidRequestException(msg);
            }

            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            String jsonAsString = clientResponse.getEntity(String.class);
            Content[] returnedContent = mapper.readValue(jsonAsString, Content[].class);

            return Arrays.asList(returnedContent);
        } catch (ClientHandlerException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw new DocumentStoreApiUnavailableException(e);
            }
            throw e;
        } catch (IOException e) {
            throw new DocumentStoreApiUnmarshallingException("Failed to parse content received from Document Store API", e);
        } finally {
            if (clientResponse != null) {
                clientResponse.close();
            }
        }
    }

    private String serializeBody(Document document) {
        final DOMSource domSource = new DOMSource(document);
        final StringWriter writer = new StringWriter();
        final StreamResult result = new StreamResult(writer);
        final TransformerFactory tf = TransformerFactory.newInstance();
        try {
            final Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty("omit-xml-declaration", "yes");
            transformer.setOutputProperty("standalone", "yes");
            transformer.transform(domSource, result);
            writer.flush();
            return writer.toString();
        } catch (TransformationException | TransformerException e) {
            throw new BodyProcessingException(e);
        }
    }

    private void processATags(Map<Node, String> aTags, List<Content> content) {
        for (Node aTag : aTags.keySet()) {
            Optional<Content> matchingContent = getMatchingContent(content, aTags.get(aTag));
            if (matchingContent.isPresent()) {
                replaceLinkToContentPresentInDocumentStore(aTag, matchingContent.get());
            } else if (isConvertibleToAssetOnFtCom(aTag)) {
                transformLinkToAssetOnFtCom(aTag, aTags.get(aTag));
            }
        }
    }

    private Optional<Content> getMatchingContent(List<Content> content, String uuid) {
        return content.stream().filter(c -> c.getUuid().equals(uuid)).findFirst();
    }

    private void replaceLinkToContentPresentInDocumentStore(Node node, Content content) {
        Element newElement = node.getOwnerDocument().createElement(CONTENT_TAG);
        newElement.setAttribute("id", content.getUuid());

        if (!StringUtils.isBlank(content.getType())) {
            newElement.setAttribute("type", BASE_CONTENT_TYPE + content.getType());
        } else {
            newElement.setAttribute("type", DEFAULT_CONTENT_TYPE);
        }

        Optional<String> nodeValue = getTitleAttributeIfExists(node);
        nodeValue.ifPresent(s -> newElement.setAttribute("title", s));
        newElement.setTextContent(node.getTextContent());
        node.getParentNode().replaceChild(newElement, node);
    }

    private Optional<String> getTitleAttributeIfExists(Node node) {
        if (getAttribute(node, "title") != null) {
            String nodeValue = getAttribute(node, "title").getNodeValue();
            return Optional.ofNullable(nodeValue);
        }
        return Optional.empty();
    }

    private boolean isConvertibleToAssetOnFtCom(Node node) {
        String href = getHref(node);
        Matcher matcher = FT_COM_URL_REGEX_PATTERN.matcher(href);
        if (matcher.matches()) {
            return true;
        } else if (href.startsWith("/")) { // i.e. it's a relative path in Methode with a UUID param
            matcher = UUID_PARAM_REGEX_PATTERN.matcher(href);
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }

    private void transformLinkToAssetOnFtCom(Node aTag, String uuid) {
        String oldHref = getHref(aTag);
        String newHref;

        Matcher matcher = FT_COM_URL_REGEX_PATTERN.matcher(oldHref);
        if (matcher.matches()) {
            URI ftAssetUri = URI.create(oldHref);
            String path = ftAssetUri.getPath();

            if (path.startsWith("/intl")) {
                newHref = ftAssetUri.resolve(path.substring(5)).toString();
            } else {
                if (isSlideshowUrl(aTag)) {
                    newHref = oldHref;
                } else {
                    // do this to get rid of query params and fragment identifiers from the url
                    newHref = ftAssetUri.resolve(path).toString();
                }
            }
        } else {
            newHref = "http://www.ft.com/cms/s/" + uuid + ".html";
        }

        getAttribute(aTag, "href").setNodeValue(newHref);

        // We might have added a type attribute to identify the type of content this links to.
        // If so, it should be removed, because it is not HTML5 compliant.
        removeTypeAttributeIfPresent(aTag);
    }

    private boolean isSlideshowUrl(Node aTag) {
        return getAttribute(aTag, TYPE) != null && getAttribute(aTag, TYPE).getNodeValue().equals("slideshow");
    }

    private void removeTypeAttributeIfPresent(Node aTag) {
        if (getAttribute(aTag, TYPE) != null) {
            aTag.getAttributes().removeNamedItem(TYPE);
        }
    }

    private Node getAttribute(Node aTag, String attributeName) {
        return aTag.getAttributes().getNamedItem(attributeName);
    }

    private Optional<String> extractUuid(Node node) {
        return extractUuid(getHref(node));
    }

    private Optional<String> extractUuid(String href) {
        Matcher matcher = UUID_REGEX_PATTERN.matcher(href);
        if (matcher.matches()) {
            return Optional.ofNullable(matcher.group(1));
        }
        return Optional.empty();
    }

    private String getHref(Node aTag) {
        final NamedNodeMap attributes = aTag.getAttributes();
        final Node hrefAttr = attributes.getNamedItem("href");
        return hrefAttr == null ? "" : hrefAttr.getNodeValue();
    }

    /**
     * Strips out a tag.
     * If the child content of the tag is empty or only whitespace, it is removed;
     * any other child content of the tag is preserved in place.
     *
     * @param aTag the tag
     */
    private void removeATag(Element aTag) {
        Node parentNode = aTag.getParentNode();

        if (!aTag.getTextContent().trim().isEmpty()) {
            NodeList children = aTag.getChildNodes();
            Node n = children.item(0);
            while (n != null) {
                aTag.removeChild(n);
                parentNode.insertBefore(n, aTag);
                n = children.item(0);
            }
        }
        parentNode.removeChild(aTag);
    }

    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        return documentBuilderFactory.newDocumentBuilder();
    }
}

package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessor;
import com.ft.methodearticleinternalcomponentsmapper.clients.DocumentStoreApiClient;
import com.ft.methodearticleinternalcomponentsmapper.exception.DocumentStoreApiException;
import com.ft.methodearticleinternalcomponentsmapper.exception.DocumentStoreApiUnavailableException;
import com.ft.methodearticleinternalcomponentsmapper.exception.InvalidMethodeContentException;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleNotEligibleForPublishException;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeMarkedDeletedException;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeMissingFieldException;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleInternalComponentsMapperException;
import com.ft.methodearticleinternalcomponentsmapper.exception.TransformationException;
import com.ft.methodearticleinternalcomponentsmapper.model.AlternativeStandfirsts;
import com.ft.methodearticleinternalcomponentsmapper.model.AlternativeTitles;
import com.ft.methodearticleinternalcomponentsmapper.model.Block;
import com.ft.methodearticleinternalcomponentsmapper.model.Design;
import com.ft.methodearticleinternalcomponentsmapper.model.EomFile;
import com.ft.methodearticleinternalcomponentsmapper.model.Image;
import com.ft.methodearticleinternalcomponentsmapper.model.InternalComponents;
import com.ft.methodearticleinternalcomponentsmapper.model.Summary;
import com.ft.methodearticleinternalcomponentsmapper.model.TableOfContents;
import com.ft.methodearticleinternalcomponentsmapper.model.Topper;
import com.ft.methodearticleinternalcomponentsmapper.validation.MethodeArticleValidator;
import com.ft.methodearticleinternalcomponentsmapper.validation.PublishingStatus;
import com.ft.uuidutils.DeriveUUID;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.ft.methodearticleinternalcomponentsmapper.model.EomFile.SOURCE_ATTR_XPATH;
import static com.ft.methodearticleinternalcomponentsmapper.model.EomFile.OVERRIDE_ORIGINAL_ATTR_XPATH ;
import static com.ft.methodearticleinternalcomponentsmapper.transformation.InternalComponentsMapper.Type.CONTENT_PACKAGE;
import static com.ft.uuidutils.DeriveUUID.Salts.IMAGE_SET;

public class InternalComponentsMapper {

    private static final Logger LOG = LoggerFactory.getLogger(InternalComponentsMapper.class);

    private static final String SUMMARY_TAG_XPATH = "/doc/lead/lead-components/lead-summary";
    private static final String SHORT_TEASER_TAG_XPATH = "/doc/lead/lead-headline/skybox-headline";
    private static final String PROMOTIONAL_TITLE_VARIANT_TAG_XPATH = "/doc/lead/web-index-headline-variant/ln";
    private static final String PROMOTIONAL_STANDFIRST_VARIANT_TAG_XPATH = "/doc/lead/web-stand-first-variant/p";
    private static final String XPATH_GUID = "ObjectMetadata/WiresIndexing/serviceid";
    private static final String XPATH_POST_ID = "ObjectMetadata/WiresIndexing/ref_field";
    private static final String XPATH_LIST_ITEM_TYPE = "ObjectMetadata/WiresIndexing/category";
    private static final String XPATH_CONTENT_PACKAGE = "/ObjectMetadata/OutputChannels/DIFTcom/isContentPackage";
    private static final String XPATH_ARTICLE_IMAGE = "/ObjectMetadata/OutputChannels/DIFTcom/DIFTcomArticleImage";
    private static final String XPATH_DESIGN_THEME_OLD = "/doc/lead/lead-components/content-package/@design-theme";
    private static final String XPATH_DESIGN_THEME = "/ObjectMetadata/OutputChannels/DIFTcom/DesignTheme";
    private static final String XPATH_DESIGN_LAYOUT = "/ObjectMetadata/OutputChannels/DIFTcom/DesignLayout";
    private static final String XPATH_PUSH_NOTIFICATION_COHORT = "/ObjectMetadata/OutputChannels/DIFTcom/pushNotification";
    private static final String XPATH_PUSH_NOTIFICATION_TEXT = "/doc/lead/push-notification-text/ln";
    private static final String BLOCKS_XPATH = "/doc/blocks//block";
    private static final String XPATH_ORIGINAL_UUID = "ObjectMetadata/EditorialNotes/OriginalUUID";

    private static final String NO_PICTURE_FLAG = "No picture";
    private static final String DEFAULT_IMAGE_ATTRIBUTE_DATA_EMBEDDED = "data-embedded";
    private static final String IMAGE_SET_TYPE = "http://www.ft.com/ontology/content/ImageSet";
    private static final String BODY_TAG_XPATH = "/doc/story/text/body";
    private static final String DEFAULT_DESIGN_THEME = "basic";
    private static final String DEFAULT_DESIGN_LAYOUT = "default";
    private static final String START_BODY = "<body";
    private static final String END_BODY = "</body>";
    private static final String EMPTY_VALIDATED_BODY = "<body></body>";
    private static final String PUSH_NOTIFICATION_COHORT_NONE = "None";
    private static final String BLOCK_TYPE = "html-block";

    private static final Set<String> BLOG_CATEGORIES =
            ImmutableSet.of("blog", "webchat-live-blogs", "webchat-live-qa", "webchat-markets-live", "fastft");

    private static final String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
    private static final Pattern UUID_PATTERN = Pattern.compile(UUID_REGEX);

    private final FieldTransformer bodyTransformer;
    private final BodyProcessor htmlFieldProcessor;
    private final BlogUuidResolver blogUuidResolver;
    private final DocumentStoreApiClient documentStoreApiClient;
    private final Map<String, MethodeArticleValidator> articleValidators;
    private final String apiHost;

    public InternalComponentsMapper(FieldTransformer bodyTransformer,
                                    BodyProcessor htmlFieldProcessor,
                                    BlogUuidResolver blogUuidResolver,
                                    DocumentStoreApiClient documentStoreApiClient,
                                    Map<String, MethodeArticleValidator> articleValidators,
                                    String apiHost) {
        this.bodyTransformer = bodyTransformer;
        this.htmlFieldProcessor = htmlFieldProcessor;
        this.blogUuidResolver = blogUuidResolver;
        this.documentStoreApiClient = documentStoreApiClient;
        this.articleValidators = articleValidators;
        this.apiHost = apiHost;
    }

    public InternalComponents map(EomFile eomFile, String transactionId, Date lastModified, boolean preview) {
        try {
            UUID uuid = UUID.fromString(eomFile.getUuid());
            final XPath xPath = XPathFactory.newInstance().newXPath();
            final Document attributesDocument = getAttributesDocument(eomFile);
            final Document valueDocument = getValueDocument(eomFile);

            String sourceCode = xPath.evaluate(SOURCE_ATTR_XPATH, attributesDocument);
            if (!SourceCode.FT.equals(sourceCode) && !SourceCode.CONTENT_PLACEHOLDER.equals(sourceCode) && !SourceCode.DYNAMIC_CONTENT.equals(sourceCode)) {
                throw new MethodeArticleNotEligibleForPublishException(uuid);
            }

            String overrideOriginalStr = xPath.evaluate(OVERRIDE_ORIGINAL_ATTR_XPATH, attributesDocument);
            if (sourceCode.equals(SourceCode.CONTENT_PLACEHOLDER) && overrideOriginalStr.equals("false")) {
                throw new MethodeArticleInternalComponentsMapperException("Could not override internal content for CPH because OverrideOriginal is not set to true");
            }


            final String type = determineType(xPath, attributesDocument, sourceCode);

            Boolean previewParam = SourceCode.FT.equals(sourceCode) || SourceCode.DYNAMIC_CONTENT.equals(sourceCode) ? preview : null;
            PublishingStatus status = articleValidators.get(sourceCode).getPublishingStatus(eomFile, transactionId, previewParam);
            switch (status) {
                case INELIGIBLE:
                    throw new MethodeArticleNotEligibleForPublishException(uuid);
                case DELETED:
                    throw new MethodeMarkedDeletedException(uuid, type);
            }

            final Design design = extractDesign(xPath, valueDocument, attributesDocument);
            final TableOfContents tableOfContents = extractTableOfContents(xPath, valueDocument);
            final List<Image> leadImages = extractImages(xPath, valueDocument, "/doc/lead/lead-image-set/lead-image-");
            final Topper topper = extractTopper(xPath, valueDocument);
            final String unpublishedContentDescription = extractUnpublishedContentDescription(xPath, valueDocument);
            final AlternativeTitles alternativeTitles = AlternativeTitles.builder()
                    .withShortTeaser(Strings.nullToEmpty(xPath.evaluate(SHORT_TEASER_TAG_XPATH, valueDocument)).trim())
                    .withPromotionalTitleVariant(Strings.nullToEmpty(xPath.evaluate(PROMOTIONAL_TITLE_VARIANT_TAG_XPATH, valueDocument)).trim())
                    .build();
            final AlternativeStandfirsts alternativeStandfirsts = AlternativeStandfirsts.builder()
                    .withPromotionalStandfirstVariant(Strings.nullToEmpty(xPath.evaluate(PROMOTIONAL_STANDFIRST_VARIANT_TAG_XPATH, valueDocument)).trim())
                    .build();
            final Summary summary = extractSummary(xPath, valueDocument, transactionId, uuid.toString());
            final String pushNotificationsCohort = extractPushNotificationsCohort(xPath, attributesDocument);
            final String pushNotificationsText = extractPushNotificationsText(xPath, valueDocument);
            final List<Block> blocks = getBlocks(xPath, valueDocument, type, transactionId);

            InternalComponents.Builder internalComponentsBuilder = InternalComponents.builder()
                    .withUuid(uuid.toString())
                    .withPublishReference(transactionId)
                    .withLastModified(lastModified)
                    .withDesign(design)
                    .withTableOfContents(tableOfContents)
                    .withTopper(topper)
                    .withLeadImages(leadImages)
                    .withUnpublishedContentDescription(unpublishedContentDescription)
                    .withAlternativeTitles(alternativeTitles)
                    .withAlternativeStandfirsts(alternativeStandfirsts)
                    .withSummary(summary)
                    .withPushNotificationsCohort(pushNotificationsCohort)
                    .withPushNotificationsText(pushNotificationsText)
                    .withBlocks(blocks);

            if (isContentPlaceholder(sourceCode)) {
                String replacedUuid = getReplacementUuidForContentPlaceholder(uuid.toString(), transactionId, xPath, attributesDocument);
                return internalComponentsBuilder.withUuid(replacedUuid).build();
            }

            if (SourceCode.DYNAMIC_CONTENT.equals(sourceCode)) {
                return internalComponentsBuilder.build();
            }

            String sourceBodyXML = retrieveField(xPath, BODY_TAG_XPATH, valueDocument);
            final String transformedBodyXML = transformBody(xPath, sourceBodyXML, attributesDocument, valueDocument, transactionId, uuid, preview);

            return internalComponentsBuilder
                    .withXMLBody(transformedBodyXML)
                    .build();
        } catch (ParserConfigurationException | SAXException | XPathExpressionException | TransformerException | IOException e) {
            throw new TransformationException(e);
        }
    }

    private boolean isContentPlaceholder(String source) {
        return SourceCode.CONTENT_PLACEHOLDER.equals(source);
    }

    private String getReplacementUuidForContentPlaceholder(String cphUuid, String transactionId, XPath xPath,
                                                           Document attributesDocument) throws XPathExpressionException {
        String originalUuid = extractOriginalUuid(cphUuid, xPath, attributesDocument);

        if (Strings.isNullOrEmpty(originalUuid)) {
            if (isBlog(xPath, attributesDocument)) {
                return resolveBlogPlaceholderUuid(attributesDocument, transactionId, cphUuid, xPath);
            }
            //if it's not a blog it means that we are dealing with an external CPH and we keep the provided uuid
        } else {
            return resolvePlaceholderUuid(originalUuid, cphUuid, transactionId);
        }
        return cphUuid;
    }

    private String extractOriginalUuid(String cphUuid, XPath xPath, Document attributesDocument) throws XPathExpressionException {
        String originalUuid = xPath.evaluate(XPATH_ORIGINAL_UUID, attributesDocument);
        if (!Strings.isNullOrEmpty(originalUuid)) {
            if (!UUID_PATTERN.matcher(originalUuid).matches()) {
                String errMsg = String.format("CPH with uuid: %s doesn't contain a valid OriginalUUID: %s.", cphUuid, originalUuid);
                LOG.error(errMsg);
                throw new TransformationException(errMsg);
            }
        }
        return originalUuid;
    }

    private boolean isBlog(XPath xPath, Document attributesDocument) throws XPathExpressionException {
        return BLOG_CATEGORIES.contains(extractListItemWiredIndexType(xPath, attributesDocument));
    }

    private String resolveBlogPlaceholderUuid(Document attributesDocument, String transactionId, String uuid, XPath xPath) throws XPathExpressionException {
        String referenceId = extractRefField(xPath, attributesDocument, uuid);
        String guid = extractServiceId(xPath, attributesDocument, uuid);
        return blogUuidResolver.resolveUuid(guid, referenceId, transactionId);
    }

    private String resolvePlaceholderUuid(String originalUuid, String cphUuid, String transactionId) {
        try {
            if (!documentStoreApiClient.isUUIDPresent(originalUuid, transactionId)) {
                String errMsg = String.format("Failed to process CPH: %s. The OriginalUUID: %s does not exist in UPP.", cphUuid, originalUuid);
                LOG.error(errMsg);
                throw new TransformationException(errMsg);
            }
            return originalUuid;
        } catch (DocumentStoreApiException e) {
            String errMsg = String.format("Failed to process CPH: %s. %s", cphUuid, e.getMessage());
            LOG.error(errMsg);
            throw new TransformationException(errMsg, e);
        }
    }

    private String retrieveField(XPath xpath, String expression, Document eomFileDocument) throws TransformerException, XPathExpressionException {
        final Node node = (Node) xpath.evaluate(expression, eomFileDocument, XPathConstants.NODE);
        return getNodeAsString(node);
    }

    private String getNodeAsString(Node node) throws TransformerException {
        return convertNodeToStringReturningEmptyIfNull(node);
    }

    private String convertNodeToStringReturningEmptyIfNull(Node node) throws TransformerException {
        StringWriter writer = new StringWriter();
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.toString();
    }

    private String transformBody(XPath xpath, String sourceBodyXML, Document attributesDocument, Document valueDocument, String transactionId, UUID uuid, boolean preview) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, TransformerException {
        TransformationMode mode = preview ? TransformationMode.PREVIEW : TransformationMode.PUBLISH;
        String sourceCode = xpath.evaluate(SOURCE_ATTR_XPATH, attributesDocument);
        final String type = determineType(xpath, attributesDocument, sourceCode);

        final String transformedBody = transformField(sourceBodyXML, bodyTransformer, transactionId, Maps.immutableEntry("uuid", uuid.toString()), Maps.immutableEntry("apiHost", apiHost));
        final String validatedTransformedBody = validateBody(mode, type, transformedBody, uuid);
        final String postProcessedTransformedBody = putMainImageReferenceInBodyXml(xpath, attributesDocument, generateMainImageUuid(xpath, valueDocument), validatedTransformedBody);

        return postProcessedTransformedBody;
    }

    private String determineType(final XPath xpath, final Document attributesDocument, String sourceCode) throws XPathExpressionException {
        final String isContentPackage = xpath.evaluate(XPATH_CONTENT_PACKAGE, attributesDocument);
        if (Boolean.TRUE.toString().equalsIgnoreCase(isContentPackage)) {
            return CONTENT_PACKAGE;
        }

        if (sourceCode.equals(SourceCode.DYNAMIC_CONTENT)) {
            return Type.DYNAMIC_CONTENT;
        }

        return Type.ARTICLE;
    }

    private String transformField(final String originalFieldAsString,
                                  final FieldTransformer transformer,
                                  final String transactionId,
                                  final Map.Entry<String, Object>... contextData) {

        String transformedField = "";
        if (!Strings.isNullOrEmpty(originalFieldAsString)) {
            transformedField = transformer.transform(originalFieldAsString, transactionId, contextData);
        }
        return transformedField;
    }

    private String validateBody(final TransformationMode mode,
                                final String type,
                                final String transformedBody,
                                final UUID uuid) {
        if (!Strings.isNullOrEmpty(transformedBody) && !Strings.isNullOrEmpty(unwrapBody(transformedBody))) {
            return transformedBody;
        }

        if (TransformationMode.PREVIEW.equals(mode)) {
            return EMPTY_VALIDATED_BODY;
        }

        if (CONTENT_PACKAGE.equals(type)) {
            return EMPTY_VALIDATED_BODY;
        }

        throw new InvalidMethodeContentException(uuid.toString(), "Not a valid Methode article for publication - transformed article body is blank");
    }

    private String unwrapBody(String wrappedBody) {
        if (!(wrappedBody.startsWith(START_BODY) && wrappedBody.endsWith(END_BODY))) {
            throw new IllegalArgumentException("can't unwrap a string that is not a wrapped body");
        }

        int index = wrappedBody.indexOf('>', START_BODY.length()) + 1;
        return wrappedBody.substring(index, wrappedBody.length() - END_BODY.length()).trim();
    }

    private String generateMainImageUuid(XPath xpath, Document eomFileDocument) throws XPathExpressionException {
        final String imageUuid = StringUtils.substringAfter(xpath.evaluate("/doc/lead/lead-images/web-master/@fileref", eomFileDocument), "uuid=");
        if (!Strings.isNullOrEmpty(imageUuid)) {
            return DeriveUUID.with(IMAGE_SET).from(UUID.fromString(imageUuid)).toString();
        }
        return null;
    }

    private String putMainImageReferenceInBodyXml(XPath xpath, Document attributesDocument, String mainImageUUID, String body) throws XPathExpressionException,
            TransformerException, ParserConfigurationException, SAXException, IOException {

        if (mainImageUUID != null) {

            InputSource inputSource = new InputSource();
            inputSource.setCharacterStream(new StringReader(body));

            Element bodyNode = getDocumentBuilder()
                    .parse(inputSource)
                    .getDocumentElement();
            final String flag = xpath.evaluate(XPATH_ARTICLE_IMAGE, attributesDocument);
            if (!NO_PICTURE_FLAG.equalsIgnoreCase(flag)) {
                return putMainImageReferenceInBodyNode(bodyNode, mainImageUUID);
            }
        }
        return body;
    }

    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        return documentBuilderFactory.newDocumentBuilder();
    }

    private String putMainImageReferenceInBodyNode(Node bodyNode, String mainImageUUID) throws TransformerException {
        Element newElement = bodyNode.getOwnerDocument().createElement("ft-content");
        newElement.setAttribute("url", String.format("http://%s/content/%s", apiHost, mainImageUUID));
        newElement.setAttribute("type", IMAGE_SET_TYPE);
        newElement.setAttribute(DEFAULT_IMAGE_ATTRIBUTE_DATA_EMBEDDED, "true");
        bodyNode.insertBefore(newElement, bodyNode.getFirstChild());
        return getNodeAsHTML5String(bodyNode);
    }

    private Design extractDesign(final XPath xPath, final Document valueDoc, final Document attributesDoc) throws XPathExpressionException {
        final String designThemeOld = Strings.nullToEmpty(xPath.evaluate(XPATH_DESIGN_THEME_OLD, valueDoc)).trim().toLowerCase();
        String designTheme = Strings.nullToEmpty(xPath.evaluate(XPATH_DESIGN_THEME, attributesDoc)).trim().toLowerCase();
        if (designTheme.isEmpty()) {
            designTheme = designThemeOld;
        }
        if (designTheme.isEmpty()) {
            designTheme = DEFAULT_DESIGN_THEME;
        }
        String designLayout = Strings.nullToEmpty(xPath.evaluate(XPATH_DESIGN_LAYOUT, attributesDoc)).trim().toLowerCase();
        if (designLayout.isEmpty()) {
            designLayout = DEFAULT_DESIGN_LAYOUT;
        }
        return new Design(designTheme, designLayout);
    }

    private TableOfContents extractTableOfContents(final XPath xPath, final Document eomFileDoc) throws XPathExpressionException {
        final String sequence = Strings.nullToEmpty(xPath.evaluate("/doc/lead/lead-components/content-package/@sequence", eomFileDoc)).trim();
        final String labelType = Strings.nullToEmpty(xPath.evaluate("/doc/lead/lead-components/content-package/@label", eomFileDoc)).trim();

        if (Strings.isNullOrEmpty(sequence) && Strings.isNullOrEmpty(labelType)) {
            return null;
        }

        return new TableOfContents(sequence, labelType);
    }

    private Topper extractTopper(final XPath xpath, final Document eomFileDoc) throws XPathExpressionException {
        final String topperBasePath = "/doc/lead/lead-components/topper";

        final String layout = Strings.nullToEmpty(xpath.evaluate(topperBasePath + "/@layout", eomFileDoc)).trim();

        //a topper is valid only if the theme attribute is present. Since layout is the new value for theme, we need to check both
        if (Strings.isNullOrEmpty(layout)) {
            return null;
        }

        final String headline = Strings.nullToEmpty(xpath.evaluate(topperBasePath + "/topper-headline", eomFileDoc)).trim();
        final String standfirst = Strings.nullToEmpty(xpath.evaluate(topperBasePath + "/topper-standfirst", eomFileDoc)).trim();

        final String backgroundColour = Strings.nullToEmpty(xpath.evaluate(topperBasePath + "/@background-colour", eomFileDoc)).trim();

        return new Topper(
                headline,
                standfirst,
                backgroundColour,
                layout);
    }

    private String extractUnpublishedContentDescription(final XPath xpath, final Document eomFileDoc) throws XPathExpressionException, TransformerException {
        final Node contentPackageNextNode = (Node) xpath.evaluate("/doc/lead/lead-components/content-package/content-package-next", eomFileDoc, XPathConstants.NODE);
        if (contentPackageNextNode == null) {
            return null;
        }

        final String contentPackageNext = getNodeAsHTML5String(contentPackageNextNode.getFirstChild());
        if (Strings.isNullOrEmpty(contentPackageNext)) {
            return null;
        }

        final String unpublishedContentDescription = contentPackageNext.trim();
        return Strings.isNullOrEmpty(unpublishedContentDescription) ? null : unpublishedContentDescription;
    }

    private List<Image> extractImages(XPath xpath, Document doc, String basePath) throws XPathExpressionException {
        String[] labels = new String[]{"square", "standard", "wide"};
        List<Image> images = new ArrayList<>();

        for (String label : labels) {
            String id = getImageId(xpath, doc, basePath + label);
            if (Strings.isNullOrEmpty(id)) {
                continue;
            }
            images.add(new Image(id, label));
        }

        return images;
    }

    private String getImageId(XPath xpath, Document doc, String topperImgBasePath) throws XPathExpressionException {
        String topperImageId = null;
        String imageFileRef = Strings.nullToEmpty(xpath.evaluate(topperImgBasePath + "/@fileref", doc)).trim();
        if (imageFileRef.contains("uuid=")) {
            topperImageId = imageFileRef.substring(imageFileRef.lastIndexOf("uuid=") + "uuid=".length());
        }
        return topperImageId;
    }

    private String getNodeAsHTML5String(final Node node) throws TransformerException {
        String nodeAsString = convertNodeToString(node);
        return htmlFieldProcessor.process(nodeAsString, null);
    }

    private String convertNodeToString(final Node node) throws TransformerException {
        final StringWriter writer = new StringWriter();
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.toString();
    }

    private Document getValueDocument(EomFile eomFile) throws ParserConfigurationException, SAXException, IOException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(new ByteArrayInputStream(eomFile.getValue()));
    }

    private Document getAttributesDocument(EomFile eomFile) throws ParserConfigurationException, SAXException, IOException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(new InputSource(new StringReader(eomFile.getAttributes())));
    }

    private String getNodeValueAsString(Node node) throws TransformerException {
        String nodeAsString = convertNodeToStringReturningEmptyIfNull(node);
        return nodeAsString.replace("<" + node.getNodeName() + ">", "").replace("</" + node.getNodeName() + ">", "")
                .replace("<" + node.getNodeName() + "/>", "");
    }

    private String extractListItemWiredIndexType(XPath xPath, Document attributesDocument) throws XPathExpressionException {
        return xPath.evaluate(XPATH_LIST_ITEM_TYPE, attributesDocument);
    }

    private String extractServiceId(XPath xPath, Document attributesDocument, String uuid) throws XPathExpressionException {
        final String serviceId = xPath.evaluate(XPATH_GUID, attributesDocument);
        if (Strings.isNullOrEmpty(serviceId)) {
            throw new MethodeMissingFieldException(uuid, "serviceid");
        }
        return serviceId;
    }

    private String extractRefField(XPath xPath, Document attributesDocument, String uuid) throws XPathExpressionException {
        final String refField = xPath.evaluate(XPATH_POST_ID, attributesDocument);
        if (Strings.isNullOrEmpty(refField)) {
            throw new MethodeMissingFieldException(uuid, "ref_field");
        }
        return refField;
    }

    private String extractPushNotificationsCohort(XPath xpath, Document attributesDocument) throws XPathExpressionException {
        String pushNotificationsCohort = Strings.nullToEmpty(xpath.evaluate(XPATH_PUSH_NOTIFICATION_COHORT, attributesDocument));
        if (Strings.isNullOrEmpty(pushNotificationsCohort) || pushNotificationsCohort.equals(PUSH_NOTIFICATION_COHORT_NONE)) {
            return null;
        }

        return pushNotificationsCohort.toLowerCase().replace("_", "-");
    }

    private Summary extractSummary(XPath xpath, Document eomFile, String transactionId, String uuid) throws TransformerException, XPathExpressionException {
        final String bodyXML = retrieveField(xpath, SUMMARY_TAG_XPATH, eomFile);
        if (Strings.isNullOrEmpty(bodyXML)) {
            return null;
        }
        final String transformedBodyXML = transformField("<body>" + bodyXML + "</body>", bodyTransformer, transactionId, Maps.immutableEntry("uuid", uuid));
        String displayPosition = Strings.emptyToNull(xpath.evaluate(SUMMARY_TAG_XPATH + "/@display-position", eomFile).trim());

        return Summary.builder().withBodyXML(transformedBodyXML).withDisplayPosition(displayPosition).build();
    }

    private String extractPushNotificationsText(XPath xPath, Document valueDocument) throws XPathExpressionException {
        String pushNotificationsText = Strings.nullToEmpty(xPath.evaluate(XPATH_PUSH_NOTIFICATION_TEXT, valueDocument)).trim();
        if (Strings.isNullOrEmpty(pushNotificationsText)) {
            return null;
        }

        return pushNotificationsText;
    }

    private List<Block> getBlocks(XPath xpath, Document value, String type, String txID) throws XPathExpressionException, TransformerException {
        if (!Type.DYNAMIC_CONTENT.equals(type)) {
            return null;
        }
        List<Block> resultedBlocks = new ArrayList<>();

        NodeList xmlBlocks = (NodeList) xpath.compile(BLOCKS_XPATH).evaluate(value, XPathConstants.NODESET);
        for (int i = 0; i < xmlBlocks.getLength(); i++) {
            Node currentBlock = xmlBlocks.item(i);
            Node keyNode = (Node) xpath.evaluate("block-name", currentBlock, XPathConstants.NODE);
            Node valueXMLNode = (Node) xpath.evaluate("block-html-value", currentBlock, XPathConstants.NODE);

            String key = getNodeValueAsString(keyNode);
            String valueXML = getNodeValueAsString(valueXMLNode);
            String transformedValueXML = bodyTransformer.transform("<body>" + valueXML + "</body>", txID);
            String valueXMLWithoutBodyTags = transformedValueXML.replace("<body>", "").replace("</body>", "");

            resultedBlocks.add(new Block(key, valueXMLWithoutBodyTags, BLOCK_TYPE));
        }

        return resultedBlocks;
    }

    enum TransformationMode {
        PUBLISH,
        PREVIEW
    }

    interface Type {
        String CONTENT_PACKAGE = "ContentPackage";
        String ARTICLE = "Article";
        String DYNAMIC_CONTENT = "DynamicContent";
    }

    public interface SourceCode {
        String FT = "FT";
        String CONTENT_PLACEHOLDER = "ContentPlaceholder";
        String DYNAMIC_CONTENT = "DynamicContent";
    }
}

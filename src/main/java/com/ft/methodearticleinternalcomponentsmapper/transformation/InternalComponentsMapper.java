package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.google.common.base.Strings;

import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleMarkedDeletedException;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleNotEligibleForPublishException;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleHasNoInternalComponentsException;
import com.ft.methodearticleinternalcomponentsmapper.exception.TransformationException;
import com.ft.methodearticleinternalcomponentsmapper.model.EomFile;
import com.ft.methodearticleinternalcomponentsmapper.model.Image;
import com.ft.methodearticleinternalcomponentsmapper.model.InternalComponents;
import com.ft.methodearticleinternalcomponentsmapper.model.Topper;
import com.ft.methodearticleinternalcomponentsmapper.validation.MethodeArticleValidator;
import com.ft.methodearticleinternalcomponentsmapper.validation.PublishingStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class InternalComponentsMapper {

    private static Logger LOGGER = LoggerFactory.getLogger(InternalComponentsMapper.class);

    private MethodeArticleValidator methodeArticleValidator;

    public InternalComponentsMapper(MethodeArticleValidator methodeArticleValidator) {
        this.methodeArticleValidator = methodeArticleValidator;
    }

    public InternalComponents map(EomFile eomFile, String transactionId, Date lastModified, boolean preview) {

        PublishingStatus status = methodeArticleValidator.getPublishingStatus(eomFile, transactionId, preview);
        UUID uuid = UUID.fromString(eomFile.getUuid());
        switch (status) {
            case INELIGIBLE:
                throw new MethodeArticleNotEligibleForPublishException(uuid);
            case DELETED:
                throw new MethodeArticleMarkedDeletedException(uuid);
        }

        try {
            Document eomFileDocument = getEomFileDocument(eomFile);

            return InternalComponents.builder()
                    .withUuid(uuid)
                    .withPublishReference(transactionId)
                    .withLastModified(lastModified)
                    .withTopper(extractTopper(eomFileDocument, uuid))
                    .build();
        } catch (ParserConfigurationException | SAXException | XPathExpressionException | IOException e) {
            throw new TransformationException(e);
        }
    }

    private Topper extractTopper(Document eomFileDoc, UUID uuid) throws XPathExpressionException {
        final XPath xpath = XPathFactory.newInstance().newXPath();

        String topperBasePath = "/doc/lead/lead-components/topper";
        if (topperIsMissing(eomFileDoc, xpath, topperBasePath)) {
            LOGGER.info("Article {} does not have a topper element", uuid);
            throw new MethodeArticleHasNoInternalComponentsException(uuid);
        }
        String topperTheme = Strings.nullToEmpty(xpath.evaluate(topperBasePath + "/@theme", eomFileDoc)).trim();
        String topperBackgroundColour = Strings.nullToEmpty(xpath.evaluate(topperBasePath + "/@bgcolor", eomFileDoc)).trim();
        String topperHeadline = Strings.nullToEmpty(xpath.evaluate(topperBasePath + "/topper-headline", eomFileDoc)).trim();
        String topperStandfirst = Strings.nullToEmpty(xpath.evaluate(topperBasePath + "/topper-standfirst", eomFileDoc)).trim();

        return new Topper(topperTheme, topperBackgroundColour, buildImages(xpath, eomFileDoc, topperBasePath), topperHeadline, topperStandfirst);
    }


    private boolean topperIsMissing(Document eomFileDoc, XPath xpath, String topperBasePath) throws XPathExpressionException {
        // if the theme attribute is present, the topper is valid
        return Strings.nullToEmpty(xpath.evaluate(topperBasePath + "/@theme", eomFileDoc)).trim().isEmpty();
    }

    private List<Image> buildImages(XPath xpath, Document doc, String topperBasePath) throws XPathExpressionException {
        String[] labels = new String[]{"square", "standard", "wide"};
        List<Image> images = new ArrayList<>();

        for (String l : labels) {
            String imgBasePath = topperBasePath + "/topper-images/topper-image-" + l;
            String id = getTopperImageId(xpath, doc, imgBasePath);
            if (Strings.isNullOrEmpty(id)) {
                continue;
            }
            images.add(new Image(id, l));
        }

        return images;
    }

    private String getTopperImageId(XPath xpath, Document doc, String topperImgBasePath) throws XPathExpressionException {
        String topperImageId = null;
        String imageFileRef = Strings.nullToEmpty(xpath.evaluate(topperImgBasePath + "/@fileref", doc)).trim();
        if (imageFileRef.contains("uuid=")) {
            topperImageId = imageFileRef.substring(imageFileRef.lastIndexOf("uuid=") + "uuid=".length());
        }
        return topperImageId;
    }

    private Document getEomFileDocument(EomFile eomFile) throws ParserConfigurationException, SAXException, IOException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(new ByteArrayInputStream(eomFile.getValue()));
    }
}

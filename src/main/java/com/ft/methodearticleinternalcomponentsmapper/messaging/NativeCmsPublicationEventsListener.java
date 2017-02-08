package com.ft.methodearticleinternalcomponentsmapper.messaging;

import static com.ft.methodearticleinternalcomponentsmapper.model.EomFile.SOURCE_ATTR_XPATH;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.message.consumer.MessageListener;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleInternalComponentsMapperException;
import com.ft.methodearticleinternalcomponentsmapper.model.EomFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.function.Predicate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class NativeCmsPublicationEventsListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(NativeCmsPublicationEventsListener.class);

    private final MessageProducingInternalComponentsMapper msgProducingArticleMapper;
    private final ObjectMapper objectMapper;
    private final Predicate<Message> messageFilter;

    public NativeCmsPublicationEventsListener(ObjectMapper objectMapper, MessageProducingInternalComponentsMapper msgProducingArticleMapper, String systemCode) {
        this.objectMapper = objectMapper;
        this.msgProducingArticleMapper = msgProducingArticleMapper;

        this.messageFilter = systemIDFilter(systemCode).and(contentTypeFilter(objectMapper));
    }

    @Override
    public boolean onMessage(Message message, String transactionId) {
        if (!messageFilter.test(message)) {
            LOG.info("Skip message");
            LOG.debug("Skip message {}", message);
            return true;
        }

        LOG.info("Process message");
        try {
            EomFile methodeContent = objectMapper.reader(EomFile.class).readValue(message.getMessageBody());
            msgProducingArticleMapper.mapInternalComponents(methodeContent, transactionId, message.getMessageTimestamp());
        } catch (IOException e) {
            throw new MethodeArticleInternalComponentsMapperException("Unable to process message", e);
        }
        return true;
    }

    private Predicate<Message> systemIDFilter(String systemCode) {
        return msg -> (SystemId.systemIdFromCode(systemCode).equals(msg.getOriginSystemId()));
    }

    private Predicate<Message> contentTypeFilter(ObjectMapper objectMapper) {
        return msg -> {
            EomFile eomFile = null;
            try {
                eomFile = objectMapper.reader(EomFile.class).readValue(msg.getMessageBody());
            } catch (IOException e) {
                LOG.warn("Message filter failure", e);
                return false;
            }
            return isValidType(eomFile.getType())
                    && isValidSource(eomFile.getAttributes());
        };
    }

    private boolean isValidType(String type) {
        return "EOM::CompoundStory".equals(type);
    }

    private boolean isValidSource(String attributes) {
        String sourceCode = null;

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();
            Document attributesDocument = db.parse(new InputSource(new StringReader(attributes)));

            XPath xpath = XPathFactory.newInstance().newXPath();

            sourceCode = xpath.evaluate(SOURCE_ATTR_XPATH, attributesDocument);
        } catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException e) {
            LOG.warn("Unable to obtain EOMFile source", e);
            // and fall through, to return false
        }

        return "FT".equals(sourceCode);
    }
}

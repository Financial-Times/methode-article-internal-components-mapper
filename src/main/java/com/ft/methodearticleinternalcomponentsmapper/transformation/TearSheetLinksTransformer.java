package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.xml.dom.XPathHandler;
import com.ft.methodearticleinternalcomponentsmapper.clients.ConcordanceApiClient;
import com.ft.methodearticleinternalcomponentsmapper.exception.ConcordanceApiException;
import com.ft.methodearticleinternalcomponentsmapper.model.concordance.Concordance;
import com.ft.methodearticleinternalcomponentsmapper.model.concordance.Concordances;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TearSheetLinksTransformer implements XPathHandler {
    private static final Logger LOG = LoggerFactory.getLogger(TearSheetLinksTransformer.class);

    private static final String TME_AUTHORITY = "http://api.ft.com/system/FT-TME";
    private static final String FT_CONCEPT_TAG = "ft-concept";
    private static final String COMPANY_TYPE = "http://www.ft.com/ontology/company/PublicCompany";
    private static final Pattern CONCEPT_UUID = Pattern.compile(
            ".*/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})$",
            Pattern.CASE_INSENSITIVE);

    private final ConcordanceApiClient client;

    public TearSheetLinksTransformer(ConcordanceApiClient client) {
        this.client = client;
    }

    @Override
    public void handle(Document document, NodeList nodes) {
        List<String> identifierValues = new ArrayList<>();
        int len = nodes.getLength();
        if (len > 0) {
            for (int i = len - 1; i >= 0; i--) {
                Element el = (Element) nodes.item(i);
                // this is because the previous processor changes attribute
                // names to be all lower case, makes the handler less
                // dependent on processor order
                String id = StringUtils.isNotBlank(el.getAttribute("CompositeId")) ? el.getAttribute("CompositeId")
                        : el.getAttribute("compositeid");
                if (StringUtils.isNotBlank(id)) {
                    identifierValues.add(id);
                }
            }
            try {
                Concordances concordances = client.getConcordancesByIdentifierValues(identifierValues);
                if (concordancesArePresent(concordances)) {
                    transformTearSheetLink(concordances.getConcordances(), nodes);
                } else {
                    identifierValues.forEach(item -> LOG.warn("Composite Id is not concorded CompositeId=" + item));
                }
            } catch (ConcordanceApiException e) {
                LOG.warn("Unable to retrieve concordances for identifier values: " + identifierValues, e);
            }
        }
    }

    private boolean concordancesArePresent(Concordances concordances) {
        return concordances != null && concordances.getConcordances() != null && !concordances.getConcordances().isEmpty();
    }

    private void transformTearSheetLink(List<Concordance> concordances, NodeList nodes) {
        int len = nodes.getLength();
        for (int i = len - 1; i >= 0; i--) {
            Element el = (Element) nodes.item(i);
            // this is because the previous processor changes attributes to be
            // all lower case, makes the handler less dependent on processor order
            String id = StringUtils.isNotBlank(el.getAttribute("CompositeId")) ? el.getAttribute("CompositeId")
                    : el.getAttribute("compositeid");
            if (StringUtils.isNotBlank(id)) {
                String conceptApiUrl = getConcordanceByTMEId(concordances, id);
                if (StringUtils.isNotBlank(conceptApiUrl)) {
                    Element newElement = el.getOwnerDocument().createElement(FT_CONCEPT_TAG);
                    newElement.setAttribute("id", getConceptIdFromUrl(conceptApiUrl));
                    newElement.setAttribute("type", COMPANY_TYPE);
                    newElement.setTextContent(el.getTextContent());
                    el.getParentNode().replaceChild(newElement, el);
                } else {
                    LOG.warn("Composite Id is not concorded CompositeId=" + id);
                }
            }
        }
    }

    private String getConcordanceByTMEId(List<Concordance> concordances, String TMEId) {
        Optional<Concordance> concordance = concordances.stream()
                .filter(item -> item.getIdentifier().getAuthority().equals(TME_AUTHORITY)
                        && TMEId.equals(item.getIdentifier().getIdentifierValue()))
                .findFirst();
        if (concordance.isPresent()) {
            return concordance.get().getConcept().getApiUrl();
        }
        return null;
    }

    private String getConceptIdFromUrl(String apiUrl) {
        Matcher m = CONCEPT_UUID.matcher(apiUrl);
        if (m.matches()) {
            return m.group(1);
        }

        throw new IllegalArgumentException("url did not contain a concept UUID");
    }
}

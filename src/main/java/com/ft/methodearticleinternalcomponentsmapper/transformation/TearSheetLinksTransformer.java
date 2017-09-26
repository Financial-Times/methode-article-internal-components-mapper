package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.xml.dom.XPathHandler;
import com.ft.methodearticleinternalcomponentsmapper.model.concordance.Concordance;
import com.ft.methodearticleinternalcomponentsmapper.model.concordance.Concordances;
import com.sun.jersey.api.client.Client;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.ws.rs.core.UriBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class TearSheetLinksTransformer implements XPathHandler {
    private static final Logger LOG = LoggerFactory.getLogger(TearSheetLinksTransformer.class);

    private final static String TME_AUTHORITY = "http://api.ft.com/system/FT-TME";
    private final static String CONCEPT_TAG = "concept";
    private final static String COMPANY_TYPE = "http://www.ft.com/ontology/company/PublicCompany";
    private static final Pattern CONCEPT_UUID = Pattern.compile(
            ".*/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})$",
            Pattern.CASE_INSENSITIVE);

    private final Client client;
    private final URI concordanceAPI;

    public TearSheetLinksTransformer(Client client, URI concordanceAPI) {
        this.client = client;
        this.concordanceAPI = concordanceAPI;
    }

    @Override
    public void handle(Document document, NodeList nodes) {
        int len = nodes.getLength();
        if (len > 0) {
            UriBuilder builder = UriBuilder.fromUri(concordanceAPI);
            try {
                builder.queryParam("authority", URLEncoder.encode(TME_AUTHORITY, "UTF-8"));
                for (int i = len - 1; i >= 0; i--) {
                    Element el = (Element) nodes.item(i);
                    // this is because the previous processor changes attribute
                    // names to be all lower case, makes the handler less
                    // dependent on processor order
                    String id = StringUtils.isNotBlank(el.getAttribute("CompositeId")) ? el.getAttribute("CompositeId")
                            : el.getAttribute("compositeid");
                    if (StringUtils.isNotBlank(id)) {
                        builder.queryParam("identifierValue", URLEncoder.encode(id, "UTF-8"));
                    }
                }

            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Encoding for concordance call failed  ");
            }

            URI concordanceApiQuery = builder.buildFromEncoded();
            Concordances responseConcordances = client.resource(concordanceApiQuery)
                    .header("Host", "public-concordances-api").get(Concordances.class);
            if (responseConcordances != null && responseConcordances.getConcordances() != null
                    && !responseConcordances.getConcordances().isEmpty()) {

                transformTearSheetLink(responseConcordances.getConcordances(), nodes);
            } else {
                List<String> identifiers = URLEncodedUtils.parse(concordanceApiQuery, "UTF-8").stream()
                        .filter(item -> item.getName().equals("identifierValue")).map(NameValuePair::getValue)
                        .collect(Collectors.toList());
                identifiers.forEach(item -> LOG.warn("Composite Id is not concorded CompositeId=" + item));
            }
        }
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
                    Element newElement = el.getOwnerDocument().createElement(CONCEPT_TAG);
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

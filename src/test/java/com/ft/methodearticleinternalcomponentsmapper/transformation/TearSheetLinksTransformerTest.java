package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.methodearticleinternalcomponentsmapper.clients.ConcordanceApiClient;
import com.ft.methodearticleinternalcomponentsmapper.exception.ConcordanceApiException;
import com.ft.methodearticleinternalcomponentsmapper.model.concordance.ConceptView;
import com.ft.methodearticleinternalcomponentsmapper.model.concordance.Concordance;
import com.ft.methodearticleinternalcomponentsmapper.model.concordance.Concordances;
import com.ft.methodearticleinternalcomponentsmapper.model.concordance.Identifier;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class TearSheetLinksTransformerTest {
    private final static String TME_AUTHORITY = "http://api.ft.com/system/FT-TME";
    private static final String TME_ID_1 = "tmeid1";
    private static final String TME_ID_2 = "tmeid2";
    private static final String ORG_ID = UUID.randomUUID().toString();
    private static final String API_URL2 = "http://api.ft.com/organisations/" + ORG_ID;

    private static final String BODY_1 = "<body> <p>Some text</p></body>";

    private static final String BODY_2 =
            "<body>" + "<p>Some text</p>" + "<p><company  DICoSEDOL=\"2297907\" CompositeId=\"" + TME_ID_1
                    + "\"  >not concorded company name</company></p>" + "</body>";
    private static final String BODY_3 =
            "<body>" + "<p>Some text</p>" + "<p><company  DICoSEDOL=\"2297907\" CompositeId=\"" + TME_ID_2
                    + "\" >concorded company name</company></p>" + "</body>";
    private static final String BODY_4 =
            "<body>" + "<p>Some text</p>" + "<p><company  DICoSEDOL=\"2297907\" CompositeId=\"" + TME_ID_1
                    + "\"  > not concorded company name</company></p>"
                    + "<p><company  DICoSEDOL=\"2297907\" CompositeId=\"" + TME_ID_2
                    + "\"  >concorded company name</company></p>" + "</body>";


    private Document doc;
    private NodeList nodes;
    private DocumentBuilder db;
    private TearSheetLinksTransformer toTest;
    private ConcordanceApiClient client = mock(ConcordanceApiClient.class);
    private Identifier identifier = new Identifier(TME_AUTHORITY, TME_ID_2);
    private ConceptView concept = new ConceptView(API_URL2, API_URL2);
    private Concordances concordances;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        toTest = new TearSheetLinksTransformer(client);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        db = dbf.newDocumentBuilder();

        Concordance concordance = new Concordance(concept, identifier);
        concordances = new Concordances(Collections.singletonList(concordance));
    }

    @Test
    public void shouldNotChangeBodyIfNoCompanyTags() throws Exception {
        doc = db.parse(new InputSource(new StringReader(BODY_1)));
        nodes = getNodeList(doc);
        toTest.handle(doc, nodes);
        String actual = serializeDocument(doc);
        assertThat(actual, equalTo(BODY_1));

        verifyZeroInteractions(client);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldNotChangeBodyIfConcordanceApiThrowsConcordanceApiException() throws Exception {
        doc = db.parse(new InputSource(new StringReader(BODY_2)));
        nodes = getNodeList(doc);
        when(client.getConcordancesByIdentifierValues(anyList())).thenThrow(new ConcordanceApiException("Error"));
        toTest.handle(doc, nodes);
        String actual = serializeDocument(doc);
        Diff diff = new Diff(BODY_2, actual);
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        XMLAssert.assertXMLEqual(diff, true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldNotChangeBodyIfConcordanceApiReturnsNull() throws Exception {
        doc = db.parse(new InputSource(new StringReader(BODY_2)));
        nodes = getNodeList(doc);
        when(client.getConcordancesByIdentifierValues(anyList())).thenReturn(null);
        toTest.handle(doc, nodes);
        String actual = serializeDocument(doc);
        Diff diff = new Diff(BODY_2, actual);
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        XMLAssert.assertXMLEqual(diff, true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldNotChangeBodyIfConcordanceApiReturnsNullList() throws Exception {
        doc = db.parse(new InputSource(new StringReader(BODY_2)));
        nodes = getNodeList(doc);
        when(client.getConcordancesByIdentifierValues(anyList())).thenReturn(new Concordances(null));
        toTest.handle(doc, nodes);
        String actual = serializeDocument(doc);
        Diff diff = new Diff(BODY_2, actual);
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        XMLAssert.assertXMLEqual(diff, true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldNotChangeBodyIfConcordanceApiReturnsEmptyList() throws Exception {
        doc = db.parse(new InputSource(new StringReader(BODY_2)));
        nodes = getNodeList(doc);
        when(client.getConcordancesByIdentifierValues(anyList())).thenReturn(new Concordances(Collections.emptyList()));
        toTest.handle(doc, nodes);
        String actual = serializeDocument(doc);
        Diff diff = new Diff(BODY_2, actual);
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        XMLAssert.assertXMLEqual(diff, true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldTransformCompanyIfCompanyTMEIdIsConcorded() throws Exception {
        doc = db.parse(new InputSource(new StringReader(BODY_3)));
        nodes = getNodeList(doc);
        when(client.getConcordancesByIdentifierValues(anyList())).thenReturn(concordances);
        toTest.handle(doc, nodes);
        String actual = serializeDocument(doc);

        String expectedBody = "<body>" + "<p>Some text</p>"
                + "<p><ft-concept type=\"http://www.ft.com/ontology/company/PublicCompany\" id=\"" + ORG_ID
                + "\">concorded company name</ft-concept></p>" + "</body>";

        Diff diff = new Diff(expectedBody, actual);
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        XMLAssert.assertXMLEqual(diff, true);
    }


    @Test
    @SuppressWarnings("unchecked")
    public void shouldTransformMultipleCompanies() throws Exception {
        doc = db.parse(new InputSource(new StringReader(BODY_4)));
        nodes = getNodeList(doc);
        when(client.getConcordancesByIdentifierValues(anyList())).thenReturn(concordances);
        toTest.handle(doc, nodes);
        String actual = serializeDocument(doc);

        String expectedBody = "<body>" + "<p>Some text</p>"
                + "<p><company CompositeId=\"tmeid1\" DICoSEDOL=\"2297907\"> not concorded company name</company></p>"
                + "<p><ft-concept type=\"http://www.ft.com/ontology/company/PublicCompany\" id=\"" + ORG_ID
                + "\">concorded company name</ft-concept></p>" + "</body>";

        Diff diff = new Diff(expectedBody, actual);
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        XMLAssert.assertXMLEqual(diff, true);
    }


    private NodeList getNodeList(Document doc) throws Exception {
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();
        return nodes = (NodeList) xp.evaluate("//company", doc, XPathConstants.NODESET);
    }

    private String serializeDocument(Document document) {
        DOMSource domSource = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);

        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty("omit-xml-declaration", "yes");
            transformer.setOutputProperty("standalone", "yes");
            transformer.transform(domSource, result);
            writer.flush();
            String body = writer.toString();
            return body;
        } catch (TransformerException e) {
            throw new BodyProcessingException(e);
        }
    }
}

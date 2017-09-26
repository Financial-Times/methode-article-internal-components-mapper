package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.DefaultTransactionIdBodyProcessingContext;
import com.ft.jerseyhttpwrapper.ResilientClient;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import static com.ft.methodetesting.xml.XmlMatcher.identicalXmlTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MethodeLinksBodyProcessorTest {

    private static final String TRANSACTION_ID = "tid_test";

    @Mock
    private ResilientClient documentStoreApiClient;
    @Mock
    private WebResource.Builder builder;
    @Mock
    private ClientHandlerException clientHandlerException;
    @Mock
    private ClientResponse clientResponse;
    @Mock
    private BodyProcessingContext bodyProcessingContext;
    @Mock
    private WebResource webResource;

    private String uuid = UUID.randomUUID().toString();
    private MethodeLinksBodyProcessor bodyProcessor;

    @Before
    public void setup() throws Exception {
        bodyProcessor = new MethodeLinksBodyProcessor(documentStoreApiClient, new URI("www.document-store-api.com"));
        when(documentStoreApiClient.resource(any(URI.class))).thenReturn(webResource);
        when(webResource.accept(MediaType.APPLICATION_JSON_TYPE)).thenReturn(builder);
        when(builder.type(MediaType.APPLICATION_JSON_TYPE)).thenReturn(builder);
        when(builder.header(anyString(), anyObject())).thenReturn(builder);
        when(builder.post(eq(ClientResponse.class), anyObject())).thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(200);
        when(clientResponse.getEntity(String.class)).thenReturn("[]");
    }

    @Test
    public void shouldReturnUnchangedBodyIfBodyIsNull() {
        String processedBody = bodyProcessor.process(null, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));
        assertThat(processedBody, is(nullValue()));
    }

    @Test
    public void shouldReturnUnchangedBodyIfBodyIsEmpty() {
        String processedBody = bodyProcessor.process("", new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));
        assertThat(processedBody, is(""));
    }

    @Test
    public void shouldReturnUnchangedBodyIfBodyIsWhitespace() {
        String processedBody = bodyProcessor.process(" ", new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));
        assertThat(processedBody, is(" "));
    }

    @Test(expected = BodyProcessingException.class)
    public void shouldThrowBodyProcessingExceptionWhenTransactionIdIsNull() {
        when(clientResponse.getStatus()).thenReturn(200);
        when(clientResponse.getEntity(String.class)).thenReturn("[{\"uuid\":\"" + uuid + "\", \"type\": \"Article\"}]");

        String body = "<body><a href=\"http://www.url.com/" + uuid + "\" title=\"Some absurd text here\"> Link Text</a></body>";
        bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(null));
    }

    @Test(expected = BodyProcessingException.class)
    public void shouldThrowBodyProcessingExceptionWhenTransactionIdIsEmpty() {
        when(clientResponse.getStatus()).thenReturn(200);
        when(clientResponse.getEntity(String.class)).thenReturn("[{\"uuid\":\"" + uuid + "\", \"type\": \"Article\"}]");

        String body = "<body><a href=\"http://www.url.com/" + uuid + "\" title=\"Some absurd text here\"> Link Text</a></body>";
        bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(" "));
    }

    @Test(expected = BodyProcessingException.class)
    public void shouldThrowBodyProcessingExceptionWhenBodyProcessingContextIsOfWrongType() {
        when(clientResponse.getStatus()).thenReturn(200);
        when(clientResponse.getEntity(String.class)).thenReturn("[{\"uuid\":\"" + uuid + "\", \"type\": \"Article\"}]");

        String body = "<body><a href=\"http://www.url.com/" + uuid + "\" title=\"Some absurd text here\"> Link Text</a></body>";
        bodyProcessor.process(body, bodyProcessingContext);
    }

    @Test(expected = BodyProcessingException.class)
    public void shouldThrowBodyProcessingExceptionWhenDocumentStoreUnavailable() {
        when(builder.post(eq(ClientResponse.class), anyObject())).thenThrow(clientHandlerException);
        when(clientHandlerException.getCause()).thenReturn(new IOException());

        String body = "<body><a href=\"" + uuid + "\" title=\"Some absurd text here\"> Link Text</a></body>";
        bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));
    }

    @Test(expected = BodyProcessingException.class)
    public void shouldThrowBodyProcessingExceptionWhenClientFailsToProcessTheRequestOrResponse() {
        when(builder.post(eq(ClientResponse.class), anyObject())).thenThrow(clientHandlerException);

        String body = "<body><a href=\"" + uuid + "\" title=\"Some absurd text here\"> Link Text</a></body>";
        bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));
    }

    @Test(expected = BodyProcessingException.class)
    public void shouldThrowBodyProcessingExceptionWhenDocumentStoreReturns5XX() {
        when(clientResponse.getStatus()).thenReturn(503);

        String body = "<body><a href=\"" + uuid + "\" title=\"Some absurd text here\"> Link Text</a></body>";
        bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));
    }

    @Test(expected = BodyProcessingException.class)
    public void shouldThrowBodyProcessingExceptionWhenDocumentStoreReturns4XX() {
        when(clientResponse.getStatus()).thenReturn(404);

        String body = "<body><a href=\"" + uuid + "\" title=\"Some absurd text here\"> Link Text</a></body>";
        bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));
    }

    @Test(expected = BodyProcessingException.class)
    public void shouldThrowBodyProcessingExceptionForFailureToParseJsonFromDocumentStore() {
        when(clientResponse.getStatus()).thenReturn(200);
        when(clientResponse.getEntity(String.class)).thenReturn("Not a json");

        String body = "<body><a href=\"" + uuid + "\" title=\"Some absurd text here\"> Link Text</a></body>";
        bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));
    }

    @Test
    public void shouldReplaceNodeWhenHrefContainsUuidPresentInDocumentStore() {
        when(clientResponse.getStatus()).thenReturn(200);
        when(clientResponse.getEntity(String.class)).thenReturn("[{\"uuid\":\"" + uuid + "\", \"type\": \"Article\"}]");

        String body = "<body><a href=\"http://www.url.com/" + uuid + "\" title=\"Some absurd text here\"> Link Text</a></body>";
        String processedBody = bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));

        assertThat(processedBody, is(identicalXmlTo("<body><content id=\"" + uuid + "\" title=\"Some absurd text here\" type=\"" + MethodeLinksBodyProcessor.BASE_CONTENT_TYPE + "Article\"> Link Text</content></body>")));
    }

    @Test
    public void shouldSetContentTypeToDefaultValueWhenTypeIsMissingFromDocumentStoreResponse() {
        when(clientResponse.getStatus()).thenReturn(200);
        when(clientResponse.getEntity(String.class)).thenReturn("[{\"uuid\":\"" + uuid + "\"}]");

        String body = "<body><a href=\"http://www.url.com/" + uuid + "\" title=\"Some absurd text here\"> Link Text</a></body>";
        String processedBody = bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));

        assertThat(processedBody, is(identicalXmlTo("<body><content id=\"" + uuid + "\" title=\"Some absurd text here\" type=\"" + MethodeLinksBodyProcessor.DEFAULT_CONTENT_TYPE + "\"> Link Text</content></body>")));
    }

    @Test
    public void shouldReplaceNodeWhenHrefContainsOnlyUuidPresentInDocumentStore() {
        when(clientResponse.getStatus()).thenReturn(200);
        when(clientResponse.getEntity(String.class)).thenReturn("[{\"uuid\":\"" + uuid + "\", \"type\": \"Article\"}]");

        String body = "<body><a href=\"" + uuid + "\" title=\"Some absurd text here\"> Link Text</a></body>";
        String processedBody = bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));

        assertThat(processedBody, is(identicalXmlTo("<body><content id=\"" + uuid + "\" title=\"Some absurd text here\" type=\"" + MethodeLinksBodyProcessor.BASE_CONTENT_TYPE + "Article\"> Link Text</content></body>")));
    }

    @Test
    public void shouldReplaceNodeAndNotAddTitleIfTitleIsMissing() {
        when(clientResponse.getStatus()).thenReturn(200);
        when(clientResponse.getEntity(String.class)).thenReturn("[{\"uuid\":\"" + uuid + "\", \"type\": \"Article\"}]");

        String body = "<body><a href=\"" + uuid + "\"> Link Text</a></body>";
        String processedBody = bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));

        assertThat(processedBody, is(identicalXmlTo("<body><content id=\"" + uuid + "\" type=\"" + MethodeLinksBodyProcessor.BASE_CONTENT_TYPE + "Article\"> Link Text</content></body>")));
    }

    @Test
    public void shouldReplaceNodeWhenHrefContainsMethodePathWithUuidPresentInDocumentStore() {
        when(clientResponse.getStatus()).thenReturn(200);
        when(clientResponse.getEntity(String.class)).thenReturn("[{\"uuid\":\"" + uuid + "\", \"type\": \"Article\"}]");

        String body = "<body><a href=\"/Content/2007/Path/To/Methode/Article.xml?uuid=" + uuid + "\" title=\"Some absurd text here\"> Link Text</a></body>";
        String processedBody = bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));

        assertThat(processedBody, is(identicalXmlTo("<body><content id=\"" + uuid + "\" title=\"Some absurd text here\" type=\"" + MethodeLinksBodyProcessor.BASE_CONTENT_TYPE + "Article\"> Link Text</content></body>")));
    }

    @Test
    public void shouldNotReplaceNodeWhenItsALinkThatWillNotBeInTheDocumentStore() {
        String body = "<body><a href=\"http://www.ft.com/cms/s/" + uuid + ".html\" title=\"Some absurd text here\"> Link Text</a></body>";
        String processedBody = bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));

        assertThat(processedBody, is(identicalXmlTo("<body><a href=\"http://www.ft.com/cms/s/" + uuid + ".html\" title=\"Some absurd text here\"> Link Text</a></body>")));
    }

    @Test
    public void shouldStripIntlFromHrefValueWhenItsALinkThatWillNotBeInTheDocumentStore() {
        String body = "<body><a href=\"http://www.ft.com/intl/cms/s/" + uuid + ".html\" title=\"Some absurd text here\"> Link Text</a></body>";
        String processedBody = bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));

        assertThat(processedBody, is(identicalXmlTo("<body><a href=\"http://www.ft.com/cms/s/" + uuid + ".html\" title=\"Some absurd text here\"> Link Text</a></body>")));
    }

    @Test
    public void shouldConvertMethodeLinksWhenItsALinkThatWillNotBeInTheDocumentStoreToFullFledgedWebsiteLinks() {
        String body = "<body><a href=\"/FT Production/Slideshows/gallery.xml;uuid=" + uuid + "\" title=\"Some absurd text here\"> Link Text</a></body>";
        String processedBody = bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));

        assertThat(processedBody, is(identicalXmlTo("<body><a href=\"http://www.ft.com/cms/s/" + uuid + ".html\" title=\"Some absurd text here\"> Link Text</a></body>")));
    }

    @Test
    public void shouldReplaceNodeWhenHrefContainsExternalLinkWithUuidPresentInDocumentStore() {
        when(clientResponse.getStatus()).thenReturn(200);
        when(clientResponse.getEntity(String.class)).thenReturn("[{\"uuid\":\"" + uuid + "\", \"type\": \"Article\"}]");

        String body = "<body><a href=\"http://www.external.com/" + uuid + "\"> Link Text</a></body>";
        String processedBody = bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));

        assertThat(processedBody, is(identicalXmlTo("<body><content id=\"" + uuid + "\" type=\"" + MethodeLinksBodyProcessor.BASE_CONTENT_TYPE + "Article\"> Link Text</content></body>")));
    }

    @Test
    public void shouldPreserveExternalLinksWithUuidThatWillNotBePresentDocumentStore() {
        String body = "<body><a href=\"http://www.external.com/" + uuid + "\" title=\"Some absurd text here\"> Link Text</a></body>";
        String processedBody = bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));

        assertThat(processedBody, is(identicalXmlTo("<body><a href=\"http://www.external.com/" + uuid + "\" title=\"Some absurd text here\"> Link Text</a></body>")));
    }

    @Test
    public void shouldPreserveSlideshowLinksAndStripTypeAttribute() {
        String body = "<body><a href=\"http://www.ft.com/content" + uuid + "\" type=\"slideshow\"> Link Text</a></body>";
        String processedBody = bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));

        assertThat(processedBody, is(identicalXmlTo("<body><a href=\"http://www.ft.com/content" + uuid + "\"> Link Text</a></body>")));
    }

    @Test
    public void shouldRemoveTypeAttributeFromInternalLinks() {
        String body = "<body><a href=\"http://www.ft.com/content/" + uuid + "\" type=\"some-type\"> Link Text</a></body>";
        String processedBody = bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));

        assertThat(processedBody, is(identicalXmlTo("<body><a href=\"http://www.ft.com/content/" + uuid + "\"> Link Text</a></body>")));
    }

    @Test
    public void shouldRemoveNodeIfATagHasNoHrefAttribute() {
        String body = "<body><a title=\"Some absurd text here\">Link Text</a></body>";
        String processedBody = bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));

        assertThat(processedBody, is(identicalXmlTo("<body>Link Text</body>")));
    }

    @Test
    public void shouldRemoveNodeIfATagHasHrefThatStartsWithAnchorPrefix() {
        String body = "<body><a href=\"#http://www.ft.com/content/add666f2-cd78-11e4-a15a-00144feab7de\" title=\"Some absurd text here\">Link Text</a></body>";
        String processedBody = bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));

        assertThat(processedBody, is(identicalXmlTo("<body>Link Text</body>")));
    }

    @Test
    public void shouldRemoveNodeIfATagHasNoHrefAttributeWhilePreservingAllItsChildren() {
        String body = "<body><a title=\"Some absurd text here\"><strong>first child</strong> Second child <![CDATA[CDATASection]]></a></body>";
        String processedBody = bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));

        assertThat(processedBody, is(identicalXmlTo("<body><strong>first child</strong> Second child <![CDATA[CDATASection]]></body>")));
    }

    @Test
    public void shouldRemoveNodeIfATagHasNoHrefAttributeWhilePreservingText() {
        String body = "<body><a title=\"Some absurd text here\">Some text</a></body>";
        String processedBody = bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));

        assertThat(processedBody, is(identicalXmlTo("<body>Some text</body>")));
    }

    @Test
    public void thatWhitespaceOnlyLinksAreRemoved() {
        String body = "<body>Foo <a href=\"http://www.ft.com/intl/cms/s/0/12345.html\" title=\"Test link containing only whitespace\">\n</a> bar</body>";
        String processedBody = bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));

        assertThat(processedBody, is(identicalXmlTo("<body>Foo  bar</body>")));
    }

    @Test
    public void thatCommentOnlyLinksAreRemoved() {
        String body = "<body><a href=\"http://www.ft.com/intl/cms/s/0/12345.html\" title=\"Test link containing only comments\"><!-- Comment --></a></body>";
        String processedBody = bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));

        assertThat(processedBody, is(identicalXmlTo("<body></body>")));
    }

    @Test
    public void thatEmptyLinksWithDataAttributesArePreserved() {
        String body = "<body>Foo <a data-asset-type=\"slideshow\" data-embedded=\"true\" href=\"http://www.ft.com/intl/cms/s/0/12345.html\" title=\"Test link with tag content\"></a> bar</body>";
        String processedBody = bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));

        assertThat(processedBody, is(identicalXmlTo(body)));
    }

    @Test
    public void thatWhitespaceAndChildTagsOnlyLinksArePreserved() {
        String body = "<body>Foo <a href=\"http://www.ft.com/intl/cms/s/0/12345.html\" title=\"Test link with tag content\"><img src=\"http://localhost/\"/>\n</a> bar</body>";
        String processedBody = bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));

        assertThat(processedBody, is(identicalXmlTo(body)));
    }

    @Test
    public void thatCDATASectionOnlyLinksArePreserved() {
        String body = "<body><a title=\"Some absurd text here\"><![CDATA[CDATASection]]></a></body>";
        String processedBody = bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));

        assertThat(processedBody, is(identicalXmlTo("<body><![CDATA[CDATASection]]></body>")));
    }

    @Test
    public void thatEmptyLinksWithinPromoBoxesArePreserved() {
        String body = "<body>Foo <promo-box><promo-link><p><a title=\"Test Promo Link\" href=\"http://www.ft.com/cms/s/0/0bdf4bb6-6676-11e4-8bf6-00144feabdc0.html\"/></p></promo-link></promo-box> bar</body>";
        String processedBody = bodyProcessor.process(body, new DefaultTransactionIdBodyProcessingContext(TRANSACTION_ID));

        assertThat(processedBody, is(identicalXmlTo(body)));
    }

}

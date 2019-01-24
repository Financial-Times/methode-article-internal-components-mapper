package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.richcontent.RichContentItem;
import com.ft.bodyprocessing.richcontent.Video;
import com.ft.bodyprocessing.richcontent.VideoMatcher;
import com.ft.methodearticleinternalcomponentsmapper.clients.ConcordanceApiClient;
import com.ft.methodearticleinternalcomponentsmapper.clients.DocumentStoreApiClient;
import com.ft.methodearticleinternalcomponentsmapper.model.Content;
import com.ft.methodearticleinternalcomponentsmapper.model.concordance.Concordances;
import com.ft.uuidutils.DeriveUUID;
import com.ft.uuidutils.GenerateV3UUID;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.ft.methodetesting.xml.XmlMatcher.identicalXmlTo;
import static com.ft.uuidutils.DeriveUUID.Salts.IMAGE_SET;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BodyProcessingFieldTransformerFactoryTest {

    private static final String KITCHEN_SINK_ASSET1_UUID = "d586f60a-5be5-11e2-bf31-00144feab49a";
    private static final String KITCHEN_SINK_ASSET2_UUID = "e78a8668-c997-11e1-aae2-002128161462";
    private static final String KITCHEN_SINK_ASSET3_UUID = "9b9fed88-d986-11e2-bce1-002128161462";
    private static final String KITCHEN_SINK_ASSET4_UUID = "f3b60ad0-acda-11e2-a7c4-002128161462";
    private static final String KITCHEN_SINK_ASSET5_UUID = "a04cb831-6373-4548-ad77-2c43431d6552";
    private static final String AUDIO_UUID = "e3ae0465-e935-47d9-8273-dc3180b47046";
    private static final List<String> INTERACTIVE_GRAPHICS_URLS = Arrays.asList(
            "http://ft.cartodb.com/viz/be9b3ea0-4fc3-11e4-8181-0e853d047bba/embed_map",
            "http://ig.ft.com/features/2014-07-21_oilProd/v3/",
            "http://www.ft.com/ig/widgets/profiles-tiled-layout/1.1.0/index.html?id=0AnE0wMr-SY-LdENVcHdXNDI5dkFKV1FicnZ0RUMweXc",
            "http://www.ft.com/ig/widgets/sortable-table/v1/widget/index.html?key=1EbhZ99KsC8xd0Aj4UN6DnrZfjWAvsaaUn2AK4IGHC_o",
            "http://interactive.ftdata.co.uk/features/2013-04-11_renminbiUpdate/index.html"
    );

    private static final Map<String, String> contentTypeTemplates;

    static {
        contentTypeTemplates = new HashMap<>();
        contentTypeTemplates.put("http://www.ft.com/ontology/content/Article", "/content/{{id}}");
        contentTypeTemplates.put("http://www.ft.com/ontology/content/ImageSet", "/content/{{id}}");
        contentTypeTemplates.put("http://www.ft.com/ontology/content/MediaResource", "/content/{{id}}");
        contentTypeTemplates.put("http://www.ft.com/ontology/content/Video", "/content/{{id}}");
        contentTypeTemplates.put("http://www.ft.com/ontology/company/PublicCompany", "/organisations/{{id}}");
        contentTypeTemplates.put("http://www.ft.com/ontology/content/ContentPackage", "/content/{{id}}");
        contentTypeTemplates.put("http://www.ft.com/ontology/content/Content", "/content/{{id}}");
        contentTypeTemplates.put("http://www.ft.com/ontology/content/Image", "/content/{{id}}");
        contentTypeTemplates.put("http://www.ft.com/ontology/content/Audio", "/content/{{id}}");
    }

    private static final String apiHost = "api.ft.com";
    private static final String CANONICAL_URL_TEMPLATE = "https://www.ft.com/content/%s";
    private static final String TRANSACTION_ID = "tid_test";

    private static final String FIRST_EMBEDDED_IMAGE_SET_ID = "U11603507121721xBE";
    private static final String SECOND_EMBEDDED_IMAGE_SET_ID = "U11703507121721xBE";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private FieldTransformer bodyTransformer;

    @Mock
    private DocumentStoreApiClient documentStoreApiClient;
    @Mock
    private VideoMatcher videoMatcher;
    @Mock
    private InteractiveGraphicsMatcher interactiveGraphicsMatcher;
    @Mock
    private ConcordanceApiClient concordanceApiClient;

    private Video exampleYouTubeVideo;
    private Video exampleVimeoVideo;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() throws Exception {
        exampleVimeoVideo = new Video();
        exampleVimeoVideo.setUrl("https://www.vimeo.com/77761436");
        exampleVimeoVideo.setEmbedded(true);

        exampleYouTubeVideo = new Video();
        exampleYouTubeVideo.setUrl("https://www.youtube.com/watch?v=OTT5dQcarl0");
        exampleYouTubeVideo.setEmbedded(true);

        bodyTransformer = new BodyProcessingFieldTransformerFactory(documentStoreApiClient, videoMatcher,
                interactiveGraphicsMatcher, contentTypeTemplates, apiHost, concordanceApiClient, CANONICAL_URL_TEMPLATE).newInstance();
        when(documentStoreApiClient.getContentForUuids(anyCollection(), anyString())).thenReturn(Collections.emptyList());
        when(concordanceApiClient.getConcordancesByIdentifierValues(anyList())).thenReturn(new Concordances(Collections.emptyList()));
    }

    // This is our thorough test of a complicated article that can be created in Methode. Has all the special characters and components. Do not remove!
    @Test
    @SuppressWarnings("unchecked")
    public void kitchenSinkArticleShouldBeTransformedAccordingToRules() {
        List<Content> content = new ArrayList<>();
        content.add(new Content(KITCHEN_SINK_ASSET1_UUID, "Article"));
        content.add(new Content(KITCHEN_SINK_ASSET2_UUID, "Article"));
        content.add(new Content(KITCHEN_SINK_ASSET3_UUID, "Article"));
        content.add(new Content(KITCHEN_SINK_ASSET4_UUID, "Article"));
        content.add(new Content(KITCHEN_SINK_ASSET5_UUID, "Article"));
        when(documentStoreApiClient.getContentForUuids(anyCollection(), anyString())).thenReturn(content);

        String originalBody = readFromFile("body/kitchen_sink_article_body.xml");

        String expectedTransformedBody = readFromFile("body/expected_transformed_kitchen_sink_article_body.xml");

        checkTransformation(originalBody, expectedTransformedBody);
    }

    @Test
    public void shouldThrowExceptionIfBodyNull() {
        expectedException.expect(BodyProcessingException.class);
        expectedException.expect(hasProperty("message", equalTo("Body is null")));
        checkTransformation(null, "");
    }

    @Test
    public void paraWithOnlyNewlineShouldBeRemoved() {
        checkTransformationToEmpty("<p>\n</p>");
    }

    @Test
    public void paraWithAllElementsRemovedShouldBeRemoved() {
        checkTransformationToEmpty("<p><canvas>Canvas is removed</canvas></p>");
    }

    @Test
    public void encodedNbspShouldBeReplacedWithSpace() {
        checkTransformation("<body>This is a sentence .</body>",
                String.format("<body>This is a sentence%s.</body>", String.valueOf('\u00A0')));
    }

    @Test
    public void pullQuotesShouldBeReplacedWithAppropriateTags() {
        String pullQuoteFromMethode = "<body><p>patelka</p><web-pull-quote align=\"left\" channel=\"FTcom\">&lt;\n" +
                "\t<table align=\"left\" cellpadding=\"6px\" width=\"170px\">\n" +
                "\t\t<tr>\n" +
                "\t\t\t<td>\n" +
                "\t\t\t\t<web-pull-quote-text>\n" +
                "\t\t\t\t\t<p>It suits the extremists to encourage healthy eating.</p>\n" +
                "\t\t\t\t</web-pull-quote-text>\n" +
                "\t\t\t</td>\n" +
                "\t\t</tr>\n" +
                "\t\t<tr>\n" +
                "\t\t\t<td>\n" +
                "\t\t\t\t<web-pull-quote-source>source1</web-pull-quote-source>\n" +
                "\t\t\t</td>\n" +
                "\t\t</tr>\n" +
                "\t</table>&gt;\n" +
                "</web-pull-quote></body>";

        String processedPullQuote = "<body><p>patelka</p><pull-quote>" +
                "<pull-quote-text><p>It suits the extremists to encourage healthy eating.</p></pull-quote-text>" +
                "<pull-quote-source>source1</pull-quote-source>" +
                "</pull-quote></body>";

        checkTransformation(pullQuoteFromMethode, processedPullQuote);
    }

    @Test
    public void pullQuotesWrittenOutsidePtags() {
        String pullQuoteFromMethode = "<body><p>patelka</p><p><web-pull-quote align=\"left\" channel=\"FTcom\">&lt;\n" +
                "\t<table align=\"left\" cellpadding=\"6px\" width=\"170px\">\n" +
                "\t\t<tr>\n" +
                "\t\t\t<td>\n" +
                "\t\t\t\t<web-pull-quote-text>\n" +
                "\t\t\t\t\t<p>It suits the extremists to encourage healthy eating.</p>\n" +
                "\t\t\t\t</web-pull-quote-text>\n" +
                "\t\t\t</td>\n" +
                "\t\t</tr>\n" +
                "\t\t<tr>\n" +
                "\t\t\t<td>\n" +
                "\t\t\t\t<web-pull-quote-source>source1</web-pull-quote-source>\n" +
                "\t\t\t</td>\n" +
                "\t\t</tr>\n" +
                "\t</table>&gt;\n" +
                "</web-pull-quote></p></body>";

        String processedPullQuote = "<body><p>patelka</p><pull-quote>" +
                "<pull-quote-text><p>It suits the extremists to encourage healthy eating.</p></pull-quote-text>" +
                "<pull-quote-source>source1</pull-quote-source>" +
                "</pull-quote></body>";

        checkTransformation(pullQuoteFromMethode, processedPullQuote);
    }


    @Test
    public void markupInsidePullQuotesShouldBeTransformed() {
        String pullQuoteFromMethode = "<body><p>patelka</p><web-pull-quote align=\"left\" channel=\"FTcom\">&lt;\n" +
                "\t<table align=\"left\" cellpadding=\"6px\" width=\"170px\">\n" +
                "\t\t<tr>\n" +
                "\t\t\t<td>\n" +
                "\t\t\t\t<web-pull-quote-text>\n" +
                "\t\t\t\t\t<p>It suits the extremists to encourage <b>healthy</b> eating.</p>\n" +
                "\t\t\t\t</web-pull-quote-text>\n" +
                "\t\t\t</td>\n" +
                "\t\t</tr>\n" +
                "\t\t<tr>\n" +
                "\t\t\t<td>\n" +
                "\t\t\t\t<web-pull-quote-source><b>source1</b></web-pull-quote-source>\n" +
                "\t\t\t</td>\n" +
                "\t\t</tr>\n" +
                "\t</table>&gt;\n" +
                "</web-pull-quote></body>";

        String processedPullQuote = "<body><p>patelka</p><pull-quote>" +
                "<pull-quote-text><p>It suits the extremists to encourage <strong>healthy</strong> eating.</p></pull-quote-text>" +
                "<pull-quote-source><strong>source1</strong></pull-quote-source>" +
                "</pull-quote></body>";

        checkTransformation(pullQuoteFromMethode, processedPullQuote);
    }

    @Test
    public void pullQuotesShouldReturnEmptySrcIfDummySource() {
        String pullQuoteFromMethode = "<body><p>patelka</p><web-pull-quote align=\"left\" channel=\"FTcom\">&lt;\n" +
                "\t<table align=\"left\" cellpadding=\"6px\" width=\"170px\">\n" +
                "\t\t<tr>\n" +
                "\t\t\t<td>\n" +
                "\t\t\t\t<web-pull-quote-text>\n" +
                "\t\t\t\t\t<p>It suits the extremists to encourage healthy eating.</p>\n" +
                "\t\t\t\t</web-pull-quote-text>\n" +
                "\t\t\t</td>\n" +
                "\t\t</tr>\n" +
                "\t\t<tr>\n" +
                "\t\t\t<td>\n" +
                "\t\t\t\t<web-pull-quote-source><?EM-dummyText [Insert source here]?></web-pull-quote-source>\n" +
                "\t\t\t</td>\n" +
                "\t\t</tr>\n" +
                "\t</table>&gt;\n" +
                "</web-pull-quote></body>";

        String processedPullQuote = "<body><p>patelka</p><pull-quote>" +
                "<pull-quote-text><p>It suits the extremists to encourage healthy eating.</p></pull-quote-text>" +
                "</pull-quote></body>";

        checkTransformation(pullQuoteFromMethode, processedPullQuote);
    }

    @Test
    public void pullQuotesShouldOmitNonExistentTxtTagsifWithEmptyRow() {
        String pullQuoteFromMethode = "<body><p>patelka</p><web-pull-quote align=\"left\" channel=\"FTcom\">&lt;\n" +
                "\t<table align=\"left\" cellpadding=\"6px\" width=\"170px\">\n" +
                "\t\t<tr>\n" +
                "\t\t\t<td>\n" +
                "\t\t\t\t<web-pull-quote-source>It suits the extremists to encourage healthy eating.</web-pull-quote-source>\n" +
                "\t\t\t</td>\n" +
                "\t\t</tr>\n" +
                "\t</table>&gt;\n" +
                "</web-pull-quote></body>";

        String processedPullQuote = "<body><p>patelka</p><pull-quote>" +
                "<pull-quote-source>It suits the extremists to encourage healthy eating.</pull-quote-source>" +
                "</pull-quote></body>";

        checkTransformation(pullQuoteFromMethode, processedPullQuote);
    }

    @Test
    public void pullQuotesShouldOmitNonExistentSrcTags() {
        String pullQuoteFromMethode = "<body><p>patelka</p><web-pull-quote align=\"left\" channel=\"FTcom\">&lt;\n" +
                "\t<table align=\"left\" cellpadding=\"6px\" width=\"170px\">\n" +
                "\t\t<tr>\n" +
                "\t\t\t<td>\n" +
                "\t\t\t\t<web-pull-quote-text>\n" +
                "\t\t\t\t\t<p>It suits the extremists to encourage healthy eating.</p>\n" +
                "\t\t\t\t</web-pull-quote-text>\n" +
                "\t\t\t</td>\n" +
                "\t\t</tr>\n" +
                "\t\t<tr>\n" +
                "\t\t\t<td>\n" +
                "\t\t\t</td>\n" +
                "\t\t</tr>\n" +
                "\t</table>&gt;\n" +
                "</web-pull-quote></body>";

        String processedPullQuote = "<body><p>patelka</p><pull-quote>" +
                "<pull-quote-text><p>It suits the extremists to encourage healthy eating.</p></pull-quote-text>" +
                "</pull-quote></body>";

        checkTransformation(pullQuoteFromMethode, processedPullQuote);
    }


    @Test
    public void shouldNotBarfOnTwoPullQuotes() {
        String pullQuoteFromMethode = "<body><p>patelka</p><web-pull-quote align=\"left\" channel=\"FTcom\">&lt;\n" +
                "\t<table align=\"left\" cellpadding=\"6px\" width=\"170px\">\n" +
                "\t\t<tr>\n" +
                "\t\t\t<td>\n" +
                "\t\t\t\t<web-pull-quote-text>\n" +
                "\t\t\t\t\t<p>It suits the extremists to encourage healthy eating.</p>\n" +
                "\t\t\t\t</web-pull-quote-text>\n" +
                "\t\t\t</td>\n" +
                "\t\t</tr>\n" +
                "\t\t<tr>\n" +
                "\t\t\t<td>\n" +
                "\t\t\t\t<web-pull-quote-source>source1</web-pull-quote-source>\n" +
                "\t\t\t</td>\n" +
                "\t\t</tr>\n" +
                "\t</table>&gt;\n" +
                "</web-pull-quote>" +
                "<p>" +
                "<web-pull-quote align=\"left\" channel=\"FTcom\">" +
                "\t<table align=\"left\" cellpadding=\"6px\" width=\"170px\">\n" +
                "\t\t<tr>\n" +
                "\t\t\t<td>\n" +
                "\t\t\t\t<web-pull-quote-text>\n" +
                "\t\t\t\t\t<p>It suits the people to encourage drinking.</p>\n" +
                "\t\t\t\t</web-pull-quote-text>\n" +
                "\t\t\t</td>\n" +
                "\t\t</tr>\n" +
                "\t\t<tr>\n" +
                "\t\t\t<td>\n" +
                "\t\t\t\t<web-pull-quote-source>source2</web-pull-quote-source>\n" +
                "\t\t\t</td>\n" +
                "\t\t</tr>\n" +
                "\t</table>&gt;\n" +
                "</web-pull-quote>" +
                "</p></body>";

        String processedPullQuote = "<body><p>patelka</p><pull-quote>" +
                "<pull-quote-text><p>It suits the extremists to encourage healthy eating.</p></pull-quote-text>" +
                "<pull-quote-source>source1</pull-quote-source>" +
                "</pull-quote>" +
                "<pull-quote>" +
                "<pull-quote-text><p>It suits the people to encourage drinking.</p></pull-quote-text>" +
                "<pull-quote-source>source2</pull-quote-source>" +
                "</pull-quote>" +
                "</body>";

        checkTransformation(pullQuoteFromMethode, processedPullQuote);
    }

    @Test
    public void bigNumbersShouldBeReplacedWithAppopriateTags() {
        String bigNumberFromMethode = "<body><p>patelka</p><promo-box class=\"numbers-component\" align=\"right\">" +
                "<table width=\"170px\" align=\"left\" cellpadding=\"6px\"><tr><td><promo-headline><p class=\"title\">£350m</p>\n" +
                "</promo-headline>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr><td><promo-intro><p>Cost of the rights expected to increase by one-third — or about <b>£350m</b> a year — although some anticipate inflation of up to 70%</p>\n" +
                "</promo-intro>\n" +
                "</td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</promo-box></body>";

        String processedBigNumber = "<body><p>patelka</p><big-number>" +
                "<big-number-headline><p>£350m</p></big-number-headline>" +
                "<big-number-intro><p>Cost of the rights expected to increase by one-third — or about <strong>£350m</strong> a year — although some anticipate inflation of up to 70%</p></big-number-intro>" +
                "</big-number></body>";

        checkTransformation(bigNumberFromMethode, processedBigNumber);
    }

    @Test
    public void bigNumbersShouldReturnEmptyHeadlineIfHeadlineIsEmpty() {
        String bigNumberFromMethode = "<body><p>patelka</p><promo-box class=\"numbers-component\" align=\"right\">" +
                "<table width=\"170px\" align=\"left\" cellpadding=\"6px\"><tr><td><promo-headline>\n" +
                "</promo-headline>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr><td><promo-intro><p>Cost of the rights expected to increase by one-third — or about £350m a year — although some anticipate inflation of up to 70%</p>\n" +
                "</promo-intro>\n" +
                "</td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</promo-box></body>";

        String processedBigNumber = "<body><p>patelka</p><big-number>" +
                "<big-number-intro><p>Cost of the rights expected to increase by one-third — or about £350m a year — although some anticipate inflation of up to 70%</p></big-number-intro>" +
                "</big-number></body>";

        checkTransformation(bigNumberFromMethode, processedBigNumber);
    }

    @Test
    public void nonClassNumbersComponentIsPromoBoxAndImagePreservedIfPresent() {
        String promoBoxFromMethode = "<body><p>This is the beginning of a sentence.<promo-box align=\"left\">" +
                "<table align=\"left\" cellpadding=\"6px\" width=\"170px\"><tr><td>" +
                "<promo-title><p><a href=\"http://www.ft.com/reports/ft-500-2011\" title=\"www.ft.com\">FT 500</a></p></promo-title>" +
                "</td></tr><tr><td><promo-headline><p>Headline</p></promo-headline></td></tr><tr><td>" +
                "<promo-image uuid=\"432b5632-9e79-11e0-9469-00144feabdc0\" fileref=\"/FT/Graphics/Online/Secondary_%26_Triplet_167x96/2011/06/SEC_ft500.jpg?uuid=432b5632-9e79-11e0-9469-00144feabdc0\"/>" +
                "</td></tr><tr><td><promo-intro><p>The risers and fallers in our annual list of the world’s biggest companies</p></promo-intro>" +
                "</td></tr><tr><td><promo-link><p><a href=\"http://www.ft.com/cms/s/0/0bdf4bb6-6676-11e4-8bf6-00144feabdc0.html\"/></p></promo-link>" +
                "</td></tr></table></promo-box>This is the end of the sentence.</p></body>";

        String processedPromoBox = "<body><promo-box><promo-title><p>" +
                "<a href=\"http://www.ft.com/reports/ft-500-2011\" title=\"www.ft.com\">FT 500</a></p></promo-title>" +
                "<promo-headline><p>Headline</p></promo-headline><promo-image>" +
                "<ft-content data-embedded=\"true\" url=\"http://api.ft.com/content/432b5632-9e79-11e0-0a0f-978e959e1689\" type=\"http://www.ft.com/ontology/content/ImageSet\"></ft-content></promo-image>" +
                "<promo-intro><p>The risers and fallers in our annual list of the world’s biggest companies</p></promo-intro><promo-link>" +
                "<p><a href=\"http://www.ft.com/cms/s/0/0bdf4bb6-6676-11e4-8bf6-00144feabdc0.html\"></a></p></promo-link></promo-box>" +
                "<p>This is the beginning of a sentence.This is the end of the sentence.</p></body>";

        checkTransformation(promoBoxFromMethode, processedPromoBox);
    }

    @Test
    public void nonClassNumbersComponentIsPromoBoxAndTitleRemovedIfDummyText() {
        String promoBoxFromMethode = "<body><p>This is the beginning of a sentence.<promo-box align=\"right\" channel=\"FTcom\"><table width=\"156px\" align=\"right\" cellpadding=\"4px\"><tr><td align=\"left\"><promo-title><p><?EM-dummyText Sidebar title ?>\n" +
                "</p>\n" +
                "</promo-title>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr><td align=\"left\"><promo-headline><p>Labour attacks ministerial role of former HSBC chairman</p>\n" +
                "</promo-headline>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr><td><web-master xtransform=\"scale(0.1538 0.1538)\" tmx=\"2048 1152 315 177\" width=\"2048\" height=\"1152\" fileref=\"/FT/Graphics/Online/Master_2048x1152/Martin/butterfly-2048x1152.jpg?uuid=17ee1f24-ff46-11e2-9b3b-002128161462\" dtxInsert=\"Web Master\" id=\"U112075852229295\"/>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr><td align=\"left\"><promo-intro><p>The revelations about HSBC’s Swiss operations reverberated around Westminster on bold <b>Monday</b>, with Labour claiming the coalition was alerted in 2010 to strikeout <span channel=\"!\">alleged </span>malpractice at the bank and took no action.</p>\n" +
                "<p><a dtxInsert=\"Sidebar - right aligned\" href=\"/FT/Content/World%20News/Stories/Live/hsbcpoltix.uk.9.xml?uuid=2f9b640c-b056-11e4-a2cc-00144feab7de\">Continue reading</a>" +
                "</p>\n" +
                "</promo-intro>\n" +
                "</td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</promo-box>This is the end of the sentence.</p></body>";

        String processedPromoBox = "<body><promo-box>" +
                "<promo-headline><p>Labour attacks ministerial role of former HSBC chairman</p></promo-headline><promo-image>" +
                "<ft-content data-embedded=\"true\" url=\"http://api.ft.com/content/17ee1f24-ff46-11e2-055d-97bbf262bf2b\" type=\"http://www.ft.com/ontology/content/ImageSet\"></ft-content></promo-image>" +
                "<promo-intro><p>The revelations about HSBC’s Swiss operations reverberated around Westminster on bold <strong>Monday</strong>, with Labour claiming the coalition was alerted in 2010 to strikeout malpractice at the bank and took no action.</p>\n" +
                "<p><a href=\"https://www.ft.com/content/2f9b640c-b056-11e4-a2cc-00144feab7de\">Continue reading</a></p></promo-intro>" +
                "</promo-box><p>This is the beginning of a sentence.This is the end of the sentence.</p></body>";

        checkTransformation(promoBoxFromMethode, processedPromoBox);
    }

    @Test
    public void nonClassNumbersComponentIsPromoBoxAndBIsConvertedToStrong() {
        String promoBoxFromMethode = "<body><p>This is the beginning of a sentence.<promo-box align=\"left\">" +
                "<table align=\"left\" cellpadding=\"6px\" width=\"170px\"><tr><td>" +
                "<promo-title><p><a href=\"http://www.ft.com/reports/ft-500-2011\" title=\"www.ft.com\">FT 500</a></p></promo-title>" +
                "</td></tr><tr><td><promo-headline><p>Headline</p></promo-headline></td></tr><tr><td>" +
                "<promo-image uuid=\"432b5632-9e79-11e0-9469-00144feabdc0\" fileref=\"/FT/Graphics/Online/Secondary_%26_Triplet_167x96/2011/06/SEC_ft500.jpg?uuid=432b5632-9e79-11e0-9469-00144feabdc0\"/>" +
                "</td></tr><tr><td><promo-intro><p>The risers and fallers in our <b>annual</b> list of the world’s biggest companies</p></promo-intro>" +
                "</td></tr><tr><td><promo-link><p><a href=\"http://www.ft.com/cms/s/0/0bdf4bb6-6676-11e4-8bf6-00144feabdc0.html\"/></p></promo-link>" +
                "</td></tr></table></promo-box>This is the end of the sentence.</p></body>";

        String processedPromoBox = "<body><promo-box><promo-title><p>" +
                "<a href=\"http://www.ft.com/reports/ft-500-2011\" title=\"www.ft.com\">FT 500</a></p></promo-title>" +
                "<promo-headline><p>Headline</p></promo-headline><promo-image>" +
                "<ft-content data-embedded=\"true\" url=\"http://api.ft.com/content/432b5632-9e79-11e0-0a0f-978e959e1689\" type=\"http://www.ft.com/ontology/content/ImageSet\"></ft-content></promo-image>" +
                "<promo-intro><p>The risers and fallers in our <strong>annual</strong> list of the world’s biggest companies</p></promo-intro><promo-link>" +
                "<p><a href=\"http://www.ft.com/cms/s/0/0bdf4bb6-6676-11e4-8bf6-00144feabdc0.html\"></a></p></promo-link></promo-box>" +
                "<p>This is the beginning of a sentence.This is the end of the sentence.</p></body>";

        checkTransformation(promoBoxFromMethode, processedPromoBox);
    }

    @Test
    public void nonClassNumbersComponentIsOmittedIfNoValuesPresent() {
        String promoBoxFromMethode = "<body><p>patelka</p><promo-box align=\"left\">" +
                "<table align=\"left\" cellpadding=\"6px\" width=\"170px\"><tr><td>" +
                "<promo-title><p></p></promo-title>" +
                "</td></tr><tr><td><promo-headline><p></p></promo-headline></td></tr><tr><td>" +
                "</td></tr><tr><td><promo-intro><p></p></promo-intro>" +
                "</td></tr><tr><td><promo-link><p></p></promo-link>" +
                "</td></tr></table></promo-box></body>";

        String processedPromoBox = "<body><p>patelka</p></body>";

        checkTransformation(promoBoxFromMethode, processedPromoBox);
    }

    @Test
    public void nonClassNumbersComponentIsPromoBoxEvenWhenTitleEmpty() {
        String promoBoxFromMethode = "<body><p>This is the beginning of a sentence.<promo-box align=\"left\">" +
                "<table align=\"left\" cellpadding=\"6px\" width=\"170px\"><tr><td>" +
                "<promo-title><p></p></promo-title>" +
                "</td></tr><tr><td><promo-headline><p>Headline</p></promo-headline></td></tr><tr><td>" +
                "<promo-image uuid=\"432b5632-9e79-11e0-9469-00144feabdc0\" fileref=\"/FT/Graphics/Online/Secondary_%26_Triplet_167x96/2011/06/SEC_ft500.jpg?uuid=432b5632-9e79-11e0-9469-00144feabdc0\"/>" +
                "</td></tr><tr><td><promo-intro><p>The risers and fallers in our annual list of the world’s biggest companies</p></promo-intro>" +
                "</td></tr><tr><td><promo-link><p><a href=\"http://www.ft.com/cms/s/0/0bdf4bb6-6676-11e4-8bf6-00144feabdc0.html\"/></p></promo-link>" +
                "</td></tr></table></promo-box>This is the end of the sentence.</p></body>";

        String processedPromoBox = "<body><promo-box>" +
                "<promo-headline><p>Headline</p></promo-headline><promo-image>" +
                "<ft-content data-embedded=\"true\" url=\"http://api.ft.com/content/432b5632-9e79-11e0-0a0f-978e959e1689\" type=\"http://www.ft.com/ontology/content/ImageSet\"></ft-content></promo-image>" +
                "<promo-intro><p>The risers and fallers in our annual list of the world’s biggest companies</p></promo-intro><promo-link>" +
                "<p><a href=\"http://www.ft.com/cms/s/0/0bdf4bb6-6676-11e4-8bf6-00144feabdc0.html\"></a></p></promo-link></promo-box>" +
                "<p>This is the beginning of a sentence.This is the end of the sentence.</p></body>";

        checkTransformation(promoBoxFromMethode, processedPromoBox);
    }

    @Test
    public void nonClassNumbersComponentIsPromoBoxEvenWhenTitleMissing() {
        String promoBoxFromMethode = "<body><p>This is the beginning of a sentence.<promo-box align=\"left\">" +
                "<table align=\"left\" cellpadding=\"6px\" width=\"170px\"><tr><td>" +
                "</td></tr><tr><td><promo-headline><p>Headline</p></promo-headline></td></tr><tr><td>" +
                "<promo-image uuid=\"432b5632-9e79-11e0-9469-00144feabdc0\" fileref=\"/FT/Graphics/Online/Secondary_%26_Triplet_167x96/2011/06/SEC_ft500.jpg?uuid=432b5632-9e79-11e0-9469-00144feabdc0\"/>" +
                "</td></tr><tr><td><promo-intro><p>The risers and fallers in our annual list of the world’s biggest companies</p></promo-intro>" +
                "</td></tr><tr><td><promo-link><p><a href=\"http://www.ft.com/cms/s/0/0bdf4bb6-6676-11e4-8bf6-00144feabdc0.html\"/></p></promo-link>" +
                "</td></tr></table></promo-box>This is the end of the sentence.</p></body>";

        String processedPromoBox = "<body><promo-box>" +
                "<promo-headline><p>Headline</p></promo-headline><promo-image>" +
                "<ft-content data-embedded=\"true\" url=\"http://api.ft.com/content/432b5632-9e79-11e0-0a0f-978e959e1689\" type=\"http://www.ft.com/ontology/content/ImageSet\"></ft-content></promo-image>" +
                "<promo-intro><p>The risers and fallers in our annual list of the world’s biggest companies</p></promo-intro><promo-link>" +
                "<p><a href=\"http://www.ft.com/cms/s/0/0bdf4bb6-6676-11e4-8bf6-00144feabdc0.html\"></a></p></promo-link></promo-box>" +
                "<p>This is the beginning of a sentence.This is the end of the sentence.</p></body>";

        checkTransformation(promoBoxFromMethode, processedPromoBox);
    }

    @Test
    public void nonClassNumbersComponentIsPromoBoxAndImageNotPreservedIfNotFileRefEmpty() {
        String promoBoxFromMethode = "<body><p>This is the beginning of a sentence.<promo-box align=\"left\">" +
                "<table align=\"left\" cellpadding=\"6px\" width=\"170px\"><tr><td>" +
                "<promo-title><p><a href=\"http://www.ft.com/reports/ft-500-2011\" title=\"www.ft.com\">FT 500</a></p></promo-title>" +
                "</td></tr><tr><td><promo-headline><p>Headline</p></promo-headline></td></tr><tr><td>" +
                "<promo-image fileref=\"\"/>" +
                "</td></tr><tr><td><promo-intro><p>The risers and fallers in our annual list of the world’s biggest companies</p></promo-intro>" +
                "</td></tr><tr><td><promo-link><p><a href=\"http://www.ft.com/cms/s/0/0bdf4bb6-6676-11e4-8bf6-00144feabdc0.html\"/></p></promo-link>" +
                "</td></tr></table></promo-box>This is the end of the sentence.</p></body>";

        String processedPromoBox = "<body><promo-box><promo-title><p>" +
                "<a href=\"http://www.ft.com/reports/ft-500-2011\" title=\"www.ft.com\">FT 500</a></p></promo-title>" +
                "<promo-headline><p>Headline</p></promo-headline>" +
                "<promo-intro><p>The risers and fallers in our annual list of the world’s biggest companies</p></promo-intro><promo-link>" +
                "<p><a href=\"http://www.ft.com/cms/s/0/0bdf4bb6-6676-11e4-8bf6-00144feabdc0.html\"></a></p></promo-link></promo-box>" +
                "<p>This is the beginning of a sentence.This is the end of the sentence.</p></body>";

        checkTransformation(promoBoxFromMethode, processedPromoBox);
    }

    @Test
    public void nonClassNumbersComponentIsPromoBoxAndImageNotPreservedIfNotPresent() {
        String promoBoxFromMethode = "<body><p>This is the beginning of a sentence.<promo-box align=\"left\">" +
                "<table align=\"left\" cellpadding=\"6px\" width=\"170px\"><tr><td>" +
                "<promo-title><p><a href=\"http://www.ft.com/reports/ft-500-2011\" title=\"www.ft.com\">FT 500</a></p></promo-title>" +
                "</td></tr><tr><td><promo-headline><p>Headline</p></promo-headline></td></tr><tr><td>" +
                "</td></tr><tr><td><promo-intro><p>The risers and fallers in our annual list of the world’s biggest companies</p></promo-intro>" +
                "</td></tr><tr><td><promo-link><p><a href=\"http://www.ft.com/cms/s/0/0bdf4bb6-6676-11e4-8bf6-00144feabdc0.html\"/></p></promo-link>" +
                "</td></tr></table></promo-box>This is the end of the sentence.</p></body>";

        String processedPromoBox = "<body><promo-box><promo-title><p>" +
                "<a href=\"http://www.ft.com/reports/ft-500-2011\" title=\"www.ft.com\">FT 500</a></p></promo-title>" +
                "<promo-headline><p>Headline</p></promo-headline>" +
                "<promo-intro><p>The risers and fallers in our annual list of the world’s biggest companies</p></promo-intro><promo-link>" +
                "<p><a href=\"http://www.ft.com/cms/s/0/0bdf4bb6-6676-11e4-8bf6-00144feabdc0.html\"></a></p></promo-link></promo-box>" +
                "<p>This is the beginning of a sentence.This is the end of the sentence.</p></body>";

        checkTransformation(promoBoxFromMethode, processedPromoBox);
    }

    @Test
    public void shouldNotBarfOnTwoPromoBoxes() {
        String bigNumberFromMethode = "<body><p>A big number!</p>\n" +
                "<p><promo-box align=\"left\" class=\"numbers-component\"><table width=\"170px\" align=\"left\" cellpadding=\"6px\"><tr><td><promo-headline><p class=\"title\">£350M</p>\n" +
                "</promo-headline>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr><td><promo-intro><p>The cost of eating at Leon and Tossed every single day.</p>\n" +
                "</promo-intro>\n" +
                "</td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</promo-box></p>\n" +
                "<p></p>\n" +
                "<p></p>\n" +
                "<p>A big number right aligned.</p>\n" +
                "<p><promo-box align=\"right\" class=\"numbers-component\"><table width=\"170px\" align=\"right\" cellpadding=\"6px\"><tr><td><promo-headline><p>52p</p>\n" +
                "</promo-headline>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr><td><promo-intro><p>The weekly saving made by making your own lunch.</p>\n" +
                "</promo-intro>\n" +
                "</td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</promo-box>\n" +
                "</p>\n" +
                "</body>";

        String processedBigNumber = "<body><p>A big number!</p>\n" +
                "<big-number><big-number-headline><p>£350M</p></big-number-headline>" +
                "<big-number-intro><p>The cost of eating at Leon and Tossed every single day.</p></big-number-intro>" +
                "</big-number>\n\n\n" +
                "<p>A big number right aligned.</p>\n" +
                "<big-number>" +
                "<big-number-headline><p>52p</p></big-number-headline>" +
                "<big-number-intro><p>The weekly saving made by making your own lunch.</p></big-number-intro>" +
                "</big-number>\n" +
                "</body>";

        checkTransformation(bigNumberFromMethode, processedBigNumber);
    }

    @Test
    public void shouldTransformDataTableWithDifferentFormatting() {
        String dataTableFromMethode = "<body><div><web-table><table class=\"data-table\" width=\"100%\"><caption>Table (Falcon Style)</caption>\n" +
                "<thead><tr><th>Column A</th>\n" +
                "<th>Column B</th>\n" +
                "<th><b>Column C</b></th>\n" +
                "<th>Column D</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody><tr><td>0</td>\n" +
                "<td>1</td>\n" +
                "<td><b>2</b></td>\n" +
                "<td>3</td>\n" +
                "</tr>\n" +
                "<tr><td>4</td>\n" +
                "<td>5</td>\n" +
                "<td>6</td>\n" +
                "<td>7</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</web-table>\n" +
                "</div></body>";

        String processedDataTable = "<body><table class=\"data-table\"><caption>Table (Falcon Style)</caption>\n" +
                "<thead><tr><th>Column A</th>\n" +
                "<th>Column B</th>\n" +
                "<th><strong>Column C</strong></th>\n" +
                "<th>Column D</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody><tr><td>0</td>\n" +
                "<td>1</td>\n" +
                "<td><strong>2</strong></td>\n" +
                "<td>3</td>\n" +
                "</tr>\n" +
                "<tr><td>4</td>\n" +
                "<td>5</td>\n" +
                "<td>6</td>\n" +
                "<td>7</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n\n</body>";

        checkTransformation(dataTableFromMethode, processedDataTable);
    }

    @Test
    public void shouldTransformDataTable() {
        String dataTableFromMethode = "<body><div><table class=\"data-table\" border=\"\" cellspacing=\"\" cellpadding=\"\" " +
                "id=\"U1817116616509jH\" width=\"100%\"><caption id=\"k63G\"><span id=\"U181711661650mIC\">KarCrash Q1  02/2014- period from to 09/2014</span>\n" +
                "</caption>\n" +
                "<tr><th width=\"25%\">Sales</th>\n" +
                "<th width=\"25%\">Net profit</th>\n" +
                "<th width=\"25%\">Earnings per share</th>\n" +
                "<th width=\"25%\">Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">324↑ ↓324</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">453↑ ↓435</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">123↑ ↓989</td>\n" +
                "<td width=\"25%\" align=\"center\" valign=\"middle\">748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</div></body>";

        String processedDataTable = "<body><table class=\"data-table\" id=\"U1817116616509jH\">" +
                "<caption>KarCrash Q1  02/2014- period from to 09/2014\n" +
                "</caption>\n" +
                "<tr><th>Sales</th>\n" +
                "<th>Net profit</th>\n" +
                "<th>Earnings per share</th>\n" +
                "<th>Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td>€</td>\n" +
                "<td>€</td>\n" +
                "<td>€</td>\n" +
                "<td>€</td>\n" +
                "</tr>\n" +
                "<tr><td>324↑ ↓324</td>\n" +
                "<td>453↑ ↓435</td>\n" +
                "<td>123↑ ↓989</td>\n" +
                "<td>748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>\n</body>";

        checkTransformation(dataTableFromMethode, processedDataTable);
    }

    @Test
    public void shouldTransformDataTableInsideOfPTags() {
        String dataTableFromMethode = "<body><p>The following data table" +
                "<div><table class=\"data-table\" border=\"\" cellspacing=\"\" cellpadding=\"\" " +
                "id=\"U1817116616509jH\" width=\"100%\"><caption id=\"k63G\"><span id=\"U181711661650mIC\">KarCrash Q1  02/2014- period from to 09/2014</span>\n" +
                "</caption>\n" +
                "<tr><th width=\"25%\">Sales</th>\n" +
                "<th width=\"25%\">Net profit</th>\n" +
                "<th width=\"25%\">Earnings per share</th>\n" +
                "<th width=\"25%\">Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">324↑ ↓324</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">453↑ ↓435</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">123↑ ↓989</td>\n" +
                "<td width=\"25%\" align=\"center\" valign=\"middle\">748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>" +
                "</div> shows some data</p>" +
                "</body>";

        String processedDataTable = "<body><p>The following data table</p>" +
                "<table class=\"data-table\" id=\"U1817116616509jH\">" +
                "<caption>KarCrash Q1  02/2014- period from to 09/2014\n" +
                "</caption>\n" +
                "<tr><th>Sales</th>\n" +
                "<th>Net profit</th>\n" +
                "<th>Earnings per share</th>\n" +
                "<th>Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td>€</td>\n" +
                "<td>€</td>\n" +
                "<td>€</td>\n" +
                "<td>€</td>\n" +
                "</tr>\n" +
                "<tr><td>324↑ ↓324</td>\n" +
                "<td>453↑ ↓435</td>\n" +
                "<td>123↑ ↓989</td>\n" +
                "<td>748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>" +
                "<p> shows some data</p>" +
                "</body>";

        checkTransformation(dataTableFromMethode, processedDataTable);
    }

    @Test
    public void shouldNotTransformTable() {
        String tableFromMethode = "<body><div><table class=\"pseudo-data-table\" border=\"\" cellspacing=\"\" cellpadding=\"\" " +
                "id=\"U1817116616509jH\" width=\"100%\"><caption id=\"k63G\"><span id=\"U181711661650mIC\">KarCrash Q1  02/2014- period from to 09/2014</span>\n" +
                "</caption>\n" +
                "<tr><th width=\"25%\">Sales</th>\n" +
                "<th width=\"25%\">Net profit</th>\n" +
                "<th width=\"25%\">Earnings per share</th>\n" +
                "<th width=\"25%\">Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">324↑ ↓324</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">453↑ ↓435</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">123↑ ↓989</td>\n" +
                "<td width=\"25%\" align=\"center\" valign=\"middle\">748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</div></body>";

        String processedTable = "<body>\n</body>";

        checkTransformation(tableFromMethode, processedTable);
    }

    @Test
    public void shouldRetainAllValidAttributesForTableTag() {
    	String dataTableFromMethode = "<body><p>The following data table" +
                "<div><table class=\"data-table\" id=\"U1817116616509jH\" data-name=\"\"" +
                " data-table-theme=\"auto\" data-table-collapse-rownum=\"\" data-table-layout-smallscreen=\"horizontal-scroll\" data-table-layout-largescreen=\"auto\">" +
				"<caption id=\"k63G\"><span id=\"U181711661650mIC\">KarCrash Q1  02/2014- period from to 09/2014</span>\n" +
                "</caption>\n" +
                "<tr><th width=\"25%\">Sales</th>\n" +
                "<th width=\"25%\">Net profit</th>\n" +
                "<th width=\"25%\">Earnings per share</th>\n" +
                "<th data-column-width=\"70%\"\n>Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">324↑ ↓324</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">453↑ ↓435</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">123↑ ↓989</td>\n" +
                "<td width=\"25%\" align=\"center\" valign=\"middle\">748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>" +
                "</div> shows some data</p>" +
                "</body>";

        String processedDataTable = "<body><p>The following data table</p>" +
                "<table class=\"data-table\" id=\"U1817116616509jH\"" +
				" data-name=\"\" data-table-theme=\"auto\" data-table-collapse-rownum=\"\" data-table-layout-smallscreen=\"horizontal-scroll\" data-table-layout-largescreen=\"auto\">" +
                "<caption>KarCrash Q1  02/2014- period from to 09/2014\n" +
                "</caption>\n" +
                "<tr><th>Sales</th>\n" +
                "<th>Net profit</th>\n" +
                "<th>Earnings per share</th>\n" +
                "<th data-column-width=\"70%\"\n>Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td>€</td>\n" +
                "<td>€</td>\n" +
                "<td>€</td>\n" +
                "<td>€</td>\n" +
                "</tr>\n" +
                "<tr><td>324↑ ↓324</td>\n" +
                "<td>453↑ ↓435</td>\n" +
                "<td>123↑ ↓989</td>\n" +
                "<td>748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>" +
                "<p> shows some data</p>" +
                "</body>";

        checkTransformation(dataTableFromMethode, processedDataTable);
    }

    @Test
    public void shouldRetainOnlyValidAttributesForTableTag() {
    	String dataTableFromMethode = "<body><p>The following data table" +
                "<div><table class=\"data-table\" id=\"U1817116616509jH\" width=\"100%\" align=\"right\" data-name=\"\"" +
                " data-table-theme=\"auto\" border=\"1\" data-table-collapse-rownum=\"\" data-table-layout-smallscreen=\"horizontal-scroll\" data-table-layout-largescreen=\"auto\">" +
				"<caption id=\"k63G\"><span id=\"U181711661650mIC\">KarCrash Q1  02/2014- period from to 09/2014</span>\n" +
                "</caption>\n" +
                "<tr><th width=\"25%\">Sales</th>\n" +
                "<th width=\"25%\">Net profit</th>\n" +
                "<th width=\"25%\">Earnings per share</th>\n" +
                "<th data-column-width=\"70%\"\n>Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">324↑ ↓324</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">453↑ ↓435</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">123↑ ↓989</td>\n" +
                "<td width=\"25%\" align=\"center\" valign=\"middle\">748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>" +
                "</div> shows some data</p>" +
                "</body>";

        String processedDataTable = "<body><p>The following data table</p>" +
                "<table class=\"data-table\" id=\"U1817116616509jH\"" +
				" data-name=\"\" data-table-theme=\"auto\" data-table-collapse-rownum=\"\" data-table-layout-smallscreen=\"horizontal-scroll\" data-table-layout-largescreen=\"auto\">" +
                "<caption>KarCrash Q1  02/2014- period from to 09/2014\n" +
                "</caption>\n" +
                "<tr><th>Sales</th>\n" +
                "<th>Net profit</th>\n" +
                "<th>Earnings per share</th>\n" +
                "<th data-column-width=\"70%\"\n>Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td>€</td>\n" +
                "<td>€</td>\n" +
                "<td>€</td>\n" +
                "<td>€</td>\n" +
                "</tr>\n" +
                "<tr><td>324↑ ↓324</td>\n" +
                "<td>453↑ ↓435</td>\n" +
                "<td>123↑ ↓989</td>\n" +
                "<td>748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>" +
                "<p> shows some data</p>" +
                "</body>";

        checkTransformation(dataTableFromMethode, processedDataTable);
    }

    @Test
    public void shouldRetainAllValidAttributesForThTag() {
        String dataTableFromMethode = "<body><p>The following data table" +
                "<div><table class=\"data-table\" border=\"\" cellspacing=\"\" cellpadding=\"\" " +
                "id=\"U1817116616509jH\" width=\"100%\"><caption id=\"k63G\"><span id=\"U181711661650mIC\">KarCrash Q1  02/2014- period from to 09/2014</span>\n" +
                "</caption>\n" +
                "<tr><th width=\"25%\">Sales</th>\n" +
                "<th width=\"25%\">Net profit</th>\n" +
                "<th width=\"25%\">Earnings per share</th>\n" +
                "<th id=\"name\" headers=\"name\" scope=\"col\" data-name=\"\" data-column-type=\"string\"\n " +
				"colspan=\"2\" rowspan=\"2\" " +
				"    data-column-sortable=\"false\"\n" +
				"    data-column-default-sort=\"none\"" +
				"    data-column-hidden=\"none\"\n" +
				"    data-column-width=\"70%\"\n" +
				"    data-column-filter-type=\"none\">Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">324↑ ↓324</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">453↑ ↓435</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">123↑ ↓989</td>\n" +
                "<td width=\"25%\" align=\"center\" valign=\"middle\">748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>" +
                "</div> shows some data</p>" +
                "</body>";

        String processedDataTable = "<body><p>The following data table</p>" +
                "<table class=\"data-table\" id=\"U1817116616509jH\">" +
                "<caption>KarCrash Q1  02/2014- period from to 09/2014\n" +
                "</caption>\n" +
                "<tr><th>Sales</th>\n" +
                "<th>Net profit</th>\n" +
                "<th>Earnings per share</th>\n" +
                "<th id=\"name\" headers=\"name\" scope=\"col\" data-name=\"\" data-column-type=\"string\"\n " +
				"colspan=\"2\" rowspan=\"2\" " +
				"    data-column-sortable=\"false\"\n" +
				"    data-column-default-sort=\"none\"" +
				"    data-column-hidden=\"none\"\n" +
				"    data-column-width=\"70%\"\n" +
				"    data-column-filter-type=\"none\">Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td>€</td>\n" +
                "<td>€</td>\n" +
                "<td>€</td>\n" +
                "<td>€</td>\n" +
                "</tr>\n" +
                "<tr><td>324↑ ↓324</td>\n" +
                "<td>453↑ ↓435</td>\n" +
                "<td>123↑ ↓989</td>\n" +
                "<td>748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>" +
                "<p> shows some data</p>" +
                "</body>";

        checkTransformation(dataTableFromMethode, processedDataTable);
    }

    @Test
    public void shouldRetainOnlyValidAttributesForThTag() {
        String dataTableFromMethode = "<body><p>The following data table" +
                "<div><table class=\"data-table\" border=\"\" cellspacing=\"\" cellpadding=\"\" " +
                "id=\"U1817116616509jH\" width=\"100%\"><caption id=\"k63G\"><span id=\"U181711661650mIC\">KarCrash Q1  02/2014- period from to 09/2014</span>\n" +
                "</caption>\n" +
                "<tr><th width=\"25%\">Sales</th>\n" +
                "<th width=\"25%\">Net profit</th>\n" +
                "<th width=\"25%\">Earnings per share</th>\n" +
                "<th id=\"name\" headers=\"name\" scope=\"col\" data-name=\"\" data-column-type=\"string\"\n " +
				"colspan=\"2\" rowspan=\"2\" align=\"right\" height=\"100\"" +
				"    data-column-sortable=\"false\"\n" +
				"    data-column-default-sort=\"none\"" +
				"    data-column-hidden=\"none\"\n" +
				"    data-column-width=\"70%\"\n" +
				"    data-column-filter-type=\"none\">Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">324↑ ↓324</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">453↑ ↓435</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">123↑ ↓989</td>\n" +
                "<td width=\"25%\" align=\"center\" valign=\"middle\">748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>" +
                "</div> shows some data</p>" +
                "</body>";

        String processedDataTable = "<body><p>The following data table</p>" +
                "<table class=\"data-table\" id=\"U1817116616509jH\">" +
                "<caption>KarCrash Q1  02/2014- period from to 09/2014\n" +
                "</caption>\n" +
                "<tr><th>Sales</th>\n" +
                "<th>Net profit</th>\n" +
                "<th>Earnings per share</th>\n" +
                "<th id=\"name\" headers=\"name\" scope=\"col\" data-name=\"\" data-column-type=\"string\"\n " +
				"colspan=\"2\" rowspan=\"2\" " +
				"    data-column-sortable=\"false\"\n" +
				"    data-column-default-sort=\"none\"" +
				"    data-column-hidden=\"none\"\n" +
				"    data-column-width=\"70%\"\n" +
				"    data-column-filter-type=\"none\">Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td>€</td>\n" +
                "<td>€</td>\n" +
                "<td>€</td>\n" +
                "<td>€</td>\n" +
                "</tr>\n" +
                "<tr><td>324↑ ↓324</td>\n" +
                "<td>453↑ ↓435</td>\n" +
                "<td>123↑ ↓989</td>\n" +
                "<td>748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>" +
                "<p> shows some data</p>" +
                "</body>";

        checkTransformation(dataTableFromMethode, processedDataTable);
    }

    @Test
    public void shouldRetainAllValidAttributesForTdTag() {
    	String dataTableFromMethode = "<body><p>The following data table" +
                "<div><table class=\"data-table\" id=\"U1817116616509jH\" width=\"100%\" align=\"right\" data-name=\"\"" +
                " data-table-theme=\"auto\" border=\"1\" data-table-collapse-rownum=\"\" data-table-layout-smallscreen=\"horizontal-scroll\" data-table-layout-largescreen=\"auto\">" +
				"<caption id=\"k63G\" data-heading-level=\"auto\"><span id=\"U181711661650mIC\">KarCrash Q1  02/2014- period from to 09/2014</span>\n" +
                "</caption>\n" +
                "<tr><th width=\"25%\">Sales</th>\n" +
                "<th width=\"25%\">Net profit</th>\n" +
                "<th width=\"25%\">Earnings per share</th>\n" +
                "<th data-column-width=\"70%\"\n>Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td colspan=\"2\" rowspan=\"2\" id=\"name\" headers=\"name\" data-name=\"\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">324↑ ↓324</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">453↑ ↓435</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">123↑ ↓989</td>\n" +
                "<td width=\"25%\" align=\"center\" valign=\"middle\">748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>" +
                "</div> shows some data</p>" +
                "</body>";

        String processedDataTable = "<body><p>The following data table</p>" +
                "<table class=\"data-table\" id=\"U1817116616509jH\"" +
				" data-name=\"\" data-table-theme=\"auto\" data-table-collapse-rownum=\"\" data-table-layout-smallscreen=\"horizontal-scroll\" data-table-layout-largescreen=\"auto\">" +
                "<caption data-heading-level=\"auto\">KarCrash Q1  02/2014- period from to 09/2014\n" +
                "</caption>\n" +
                "<tr><th>Sales</th>\n" +
                "<th>Net profit</th>\n" +
                "<th>Earnings per share</th>\n" +
                "<th data-column-width=\"70%\"\n>Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td>€</td>\n" +
                "<td colspan=\"2\" rowspan=\"2\" id=\"name\" headers=\"name\" data-name=\"\">€</td>\n" +
                "<td>€</td>\n" +
                "<td>€</td>\n" +
                "</tr>\n" +
                "<tr><td>324↑ ↓324</td>\n" +
                "<td>453↑ ↓435</td>\n" +
                "<td>123↑ ↓989</td>\n" +
                "<td>748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>" +
                "<p> shows some data</p>" +
                "</body>";

        checkTransformation(dataTableFromMethode, processedDataTable);
    }

    @Test
    public void shouldRetainOnlyValidAttributesForTdTag() {
    	String dataTableFromMethode = "<body><p>The following data table" +
                "<div><table class=\"data-table\" id=\"U1817116616509jH\" width=\"100%\" align=\"right\" data-name=\"\"" +
                " data-table-theme=\"auto\" border=\"1\" data-table-collapse-rownum=\"\" data-table-layout-smallscreen=\"horizontal-scroll\" data-table-layout-largescreen=\"auto\">" +
				"<caption id=\"k63G\" data-heading-level=\"auto\"><span id=\"U181711661650mIC\">KarCrash Q1  02/2014- period from to 09/2014</span>\n" +
                "</caption>\n" +
                "<tr><th width=\"25%\">Sales</th>\n" +
                "<th width=\"25%\">Net profit</th>\n" +
                "<th width=\"25%\">Earnings per share</th>\n" +
                "<th data-column-width=\"70%\"\n>Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td colspan=\"2\" rowspan=\"2\" id=\"name\" headers=\"name\" data-name=\"\" align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">324↑ ↓324</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">453↑ ↓435</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">123↑ ↓989</td>\n" +
                "<td width=\"25%\" align=\"center\" valign=\"middle\">748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>" +
                "</div> shows some data</p>" +
                "</body>";

        String processedDataTable = "<body><p>The following data table</p>" +
                "<table class=\"data-table\" id=\"U1817116616509jH\"" +
				" data-name=\"\" data-table-theme=\"auto\" data-table-collapse-rownum=\"\" data-table-layout-smallscreen=\"horizontal-scroll\" data-table-layout-largescreen=\"auto\">" +
                "<caption data-heading-level=\"auto\">KarCrash Q1  02/2014- period from to 09/2014\n" +
                "</caption>\n" +
                "<tr><th>Sales</th>\n" +
                "<th>Net profit</th>\n" +
                "<th>Earnings per share</th>\n" +
                "<th data-column-width=\"70%\"\n>Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td>€</td>\n" +
                "<td colspan=\"2\" rowspan=\"2\" id=\"name\" headers=\"name\" data-name=\"\">€</td>\n" +
                "<td>€</td>\n" +
                "<td>€</td>\n" +
                "</tr>\n" +
                "<tr><td>324↑ ↓324</td>\n" +
                "<td>453↑ ↓435</td>\n" +
                "<td>123↑ ↓989</td>\n" +
                "<td>748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>" +
                "<p> shows some data</p>" +
                "</body>";

        checkTransformation(dataTableFromMethode, processedDataTable);
    }

    @Test
    public void shouldRetainOnlyValidAttributesForTrTag() {
    	String dataTableFromMethode = "<body><p>The following data table" +
                "<div><table class=\"data-table\" id=\"U1817116616509jH\" width=\"100%\" align=\"right\" data-name=\"\"" +
                " data-table-theme=\"auto\" border=\"1\" data-table-collapse-rownum=\"\" data-table-layout-smallscreen=\"horizontal-scroll\" data-table-layout-largescreen=\"auto\">" +
				"<caption id=\"k63G\" data-heading-level=\"auto\"><span id=\"U181711661650mIC\">KarCrash Q1  02/2014- period from to 09/2014</span>\n" +
                "</caption>\n" +
                "<tr id=\"k365\" data-name=\"\"><th width=\"25%\">Sales</th>\n" +
                "<th width=\"25%\">Net profit</th>\n" +
                "<th width=\"25%\">Earnings per share</th>\n" +
                "<th data-column-width=\"70%\"\n>Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td colspan=\"2\" rowspan=\"2\" id=\"name\" headers=\"name\" data-name=\"\" align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">324↑ ↓324</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">453↑ ↓435</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">123↑ ↓989</td>\n" +
                "<td width=\"25%\" align=\"center\" valign=\"middle\">748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>" +
                "</div> shows some data</p>" +
                "</body>";

        String processedDataTable = "<body><p>The following data table</p>" +
                "<table class=\"data-table\" id=\"U1817116616509jH\"" +
				" data-name=\"\" data-table-theme=\"auto\" data-table-collapse-rownum=\"\" data-table-layout-smallscreen=\"horizontal-scroll\" data-table-layout-largescreen=\"auto\">" +
                "<caption data-heading-level=\"auto\">KarCrash Q1  02/2014- period from to 09/2014\n" +
                "</caption>\n" +
                "<tr id=\"k365\" data-name=\"\"><th>Sales</th>\n" +
                "<th>Net profit</th>\n" +
                "<th>Earnings per share</th>\n" +
                "<th data-column-width=\"70%\"\n>Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td>€</td>\n" +
                "<td colspan=\"2\" rowspan=\"2\" id=\"name\" headers=\"name\" data-name=\"\">€</td>\n" +
                "<td>€</td>\n" +
                "<td>€</td>\n" +
                "</tr>\n" +
                "<tr><td>324↑ ↓324</td>\n" +
                "<td>453↑ ↓435</td>\n" +
                "<td>123↑ ↓989</td>\n" +
                "<td>748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>" +
                "<p> shows some data</p>" +
                "</body>";

        checkTransformation(dataTableFromMethode, processedDataTable);
    }

    @Test
    public void shouldRetainAttributesForCaptionColColgroupTags() {
    	String dataTableFromMethode = "<body><p>The following data table" +
                "<div><table class=\"data-table\" id=\"U1817116616509jH\" width=\"100%\" align=\"right\" data-name=\"\"" +
                " data-table-theme=\"auto\" border=\"1\" data-table-collapse-rownum=\"\" data-table-layout-smallscreen=\"horizontal-scroll\" data-table-layout-largescreen=\"auto\">" +
				"<caption id=\"k63G\" data-heading-level=\"auto\"><span id=\"U181711661650mIC\">KarCrash Q1  02/2014- period from to 09/2014</span>\n" +
                "</caption>\n" +
				"<colgroup span=\"2\" data-name=\"\" data-column-type=\"string\" data-column-sortable=\"false\" data-column-default-sort=\"none\" " +
				"data-column-hidden=\"none\" data-column-width=\"100%\">\n" +
				"<col span=\"2\" data-name=\"\" data-column-type=\"string\" data-column-sortable=\"false\" data-column-default-sort=\"none\" " +
				"data-column-hidden=\"none\" data-column-width=\"100%\"></col>\n" +
				"</colgroup>" +
                "<tr><th width=\"25%\">Sales</th>\n" +
                "<th width=\"25%\">Net profit</th>\n" +
                "<th width=\"25%\">Earnings per share</th>\n" +
                "<th data-column-width=\"70%\"\n>Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">324↑ ↓324</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">453↑ ↓435</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">123↑ ↓989</td>\n" +
                "<td width=\"25%\" align=\"center\" valign=\"middle\">748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>" +
                "</div> shows some data</p>" +
                "</body>";

        String processedDataTable = "<body><p>The following data table</p>" +
                "<table class=\"data-table\" id=\"U1817116616509jH\"" +
				" data-name=\"\" data-table-theme=\"auto\" data-table-collapse-rownum=\"\" data-table-layout-smallscreen=\"horizontal-scroll\" data-table-layout-largescreen=\"auto\">" +
                "<caption data-heading-level=\"auto\">KarCrash Q1  02/2014- period from to 09/2014\n" +
                "</caption>\n" +
				"<colgroup span=\"2\" data-name=\"\" data-column-type=\"string\" data-column-sortable=\"false\" data-column-default-sort=\"none\" " +
				"data-column-hidden=\"none\" data-column-width=\"100%\">\n" +
				"<col span=\"2\" data-name=\"\" data-column-type=\"string\" data-column-sortable=\"false\" data-column-default-sort=\"none\" " +
				"data-column-hidden=\"none\" data-column-width=\"100%\"></col>\n" +
				"</colgroup>" +
                "<tr><th>Sales</th>\n" +
                "<th>Net profit</th>\n" +
                "<th>Earnings per share</th>\n" +
                "<th data-column-width=\"70%\"\n>Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td>€</td>\n" +
                "<td>€</td>\n" +
                "<td>€</td>\n" +
                "<td>€</td>\n" +
                "</tr>\n" +
                "<tr><td>324↑ ↓324</td>\n" +
                "<td>453↑ ↓435</td>\n" +
                "<td>123↑ ↓989</td>\n" +
                "<td>748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>" +
                "<p> shows some data</p>" +
                "</body>";

        checkTransformation(dataTableFromMethode, processedDataTable);
    }

    @Test
    public void shouldStripEmptyCaptionTag() {
    	String dataTableFromMethode = "<body><p>The following data table" +
                "<div><table class=\"data-table\" id=\"U1817116616509jH\" width=\"100%\" align=\"right\" data-name=\"\"" +
                " data-table-theme=\"auto\" border=\"1\" data-table-collapse-rownum=\"\" data-table-layout-smallscreen=\"horizontal-scroll\" data-table-layout-largescreen=\"auto\">" +
				"<caption>" +
                "</caption>" +
                "<tr><th width=\"25%\">Sales</th>\n" +
                "<th width=\"25%\">Net profit</th>\n" +
                "<th width=\"25%\">Earnings per share</th>\n" +
                "<th data-column-width=\"70%\"\n>Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">€</td>\n" +
                "</tr>\n" +
                "<tr><td align=\"center\" width=\"25%\" valign=\"middle\">324↑ ↓324</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">453↑ ↓435</td>\n" +
                "<td align=\"center\" width=\"25%\" valign=\"middle\">123↑ ↓989</td>\n" +
                "<td width=\"25%\" align=\"center\" valign=\"middle\">748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>" +
                "</div> shows some data</p>" +
                "</body>";

        String processedDataTable = "<body><p>The following data table</p>" +
                "<table class=\"data-table\" id=\"U1817116616509jH\"" +
				" data-name=\"\" data-table-theme=\"auto\" data-table-collapse-rownum=\"\" data-table-layout-smallscreen=\"horizontal-scroll\" data-table-layout-largescreen=\"auto\">" +
                "<tr><th>Sales</th>\n" +
                "<th>Net profit</th>\n" +
                "<th>Earnings per share</th>\n" +
                "<th data-column-width=\"70%\"\n>Dividend</th>\n" +
                "</tr>\n" +
                "<tr><td>€</td>\n" +
                "<td>€</td>\n" +
                "<td>€</td>\n" +
                "<td>€</td>\n" +
                "</tr>\n" +
                "<tr><td>324↑ ↓324</td>\n" +
                "<td>453↑ ↓435</td>\n" +
                "<td>123↑ ↓989</td>\n" +
                "<td>748↑ ↓986</td>\n" +
                "</tr>\n" +
                "</table>" +
                "<p> shows some data</p>" +
                "</body>";

        checkTransformation(dataTableFromMethode, processedDataTable);
    }

    @Test
    public void promoBoxWithPromoTitleThatIsEmptyIsBigNumber() {
        String bigNumberFromMethode = "<body><p>patelka</p><p><promo-box class=\"numbers-component\" align=\"left\">&lt;<table width=\"170px\" align=\"left\" cellpadding=\"6px\"><tr><td><promo-title>" +
                "</promo-title>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr><td><promo-headline><p>£350m</p>\n" +
                "</promo-headline>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr><td><promo-intro><p>Cost of the rights expected to increase by one-third — or about £350m a year — although some anticipate inflation of up to 70%</p>\n" +
                "</promo-intro>\n" +
                "</td>\n" +
                "</tr>\n" +
                "</table>&gt;</promo-box></p></body>";

        String processedBigNumber = "<body><p>patelka</p><big-number>" +
                "<big-number-headline><p>£350m</p></big-number-headline>" +
                "<big-number-intro><p>Cost of the rights expected to increase by one-third — or about £350m a year — although some anticipate inflation of up to 70%</p></big-number-intro>" +
                "</big-number></body>";

        checkTransformation(bigNumberFromMethode, processedBigNumber);
    }

    @Test
    public void emptyBigNumbersShouldBeOmitted() {
        String bigNumberFromMethode = "<body><p>patelka</p><promo-box class=\"numbers-component\" align=\"right\">" +
                "<table width=\"170px\" align=\"left\" cellpadding=\"6px\"><tr><td><promo-headline><p class=\"title\"></p>\n" +
                "</promo-headline>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr><td><promo-intro><p></p>\n" +
                "</promo-intro>\n" +
                "</td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</promo-box></body>";

        String processedBigNumber = "<body><p>patelka</p></body>";

        checkTransformation(bigNumberFromMethode, processedBigNumber);
    }

    @Test
    public void emptyPullQuotesShouldNotBeWritten() {
        String pullQuoteFromMethode = "<body><p>patelka</p><web-pull-quote align=\"left\" channel=\"FTcom\">&lt;\n" +
                "\t<table align=\"left\" cellpadding=\"6px\" width=\"170px\">\n" +
                "\t\t<tr>\n" +
                "\t\t\t<td>\n" +
                "\t\t\t\t<web-pull-quote-text>\n" +
                "\t\t\t\t</web-pull-quote-text>\n" +
                "\t\t\t</td>\n" +
                "\t\t</tr>\n" +
                "\t\t<tr>\n" +
                "\t\t\t<td>\n" +
                "\t\t\t\t<web-pull-quote-source></web-pull-quote-source>\n" +
                "\t\t\t</td>\n" +
                "\t\t</tr>\n" +
                "\t</table>&gt;\n" +
                "</web-pull-quote></body>";

        String processedPullQuote = "<body><p>patelka</p></body>";

        checkTransformation(pullQuoteFromMethode, processedPullQuote);
    }

    @Test
    public void pullQuotesWithNoSourceShouldNotBeWritten() {
        String pullQuoteFromMethode = "<body><p>patelka</p><web-pull-quote align=\"left\" channel=\"FTcom\">&lt;\n" +
                "\t<table align=\"left\" cellpadding=\"6px\" width=\"170px\">\n" +
                "\t\t<tr>\n" +
                "\t\t\t<td>\n" +
                "\t\t\t\t<web-pull-quote-text>It suits the extremists to encourage healthy eating.\n" +
                "\t\t\t\t</web-pull-quote-text>\n" +
                "\t\t\t</td>\n" +
                "\t\t</tr>\n" +
                "\t\t<tr>\n" +
                "\t\t\t<td>\n" +
                "\t\t\t\t<web-pull-quote-source></web-pull-quote-source>\n" +
                "\t\t\t</td>\n" +
                "\t\t</tr>\n" +
                "\t</table>&gt;\n" +
                "</web-pull-quote></body>";

        String processedPullQuote = "<body><p>patelka</p><pull-quote>" +
                "<pull-quote-text>It suits the extremists to encourage healthy eating.</pull-quote-text>" +
                "</pull-quote>" +
                "</body>";

        checkTransformation(pullQuoteFromMethode, processedPullQuote);
    }

    @Test
    public void slideshowShouldBeConvertedToPointToSlideshowOnWebsite() {
        String slideshowFromMethode = "<body><p>Embedded Slideshow</p>" +
                "<p><a type=\"slideshow\" dtxInsert=\"slideshow\" href=\"/FT/Content/Companies/Stories/Live/PlainSlideshow.gallery.xml?uuid=49336a18-051c-11e3-98a0-002128161462\">" +
                "<DIHeadlineCopy>One typical, bog-standard slideshow headline update 2</DIHeadlineCopy></a></p></body>";

        String processedSlideshow = "<body><p>Embedded Slideshow</p>" +
                "<p><a data-asset-type=\"slideshow\" data-embedded=\"true\" href=\"https://www.ft.com/content/49336a18-051c-11e3-98a0-002128161462\"></a></p></body>";

        checkTransformation(slideshowFromMethode, processedSlideshow);
    }

    @Test
    public void slideshowWithEmptyHeadlineShouldBeConvertedToPointToSlideshowOnWebsite() {
        String slideshowFromMethode = "<body><p>Embedded Slideshow</p>" +
                "<p><a type=\"slideshow\" dtxInsert=\"slideshow\" href=\"/FT/Content/Companies/Stories/Live/PlainSlideshow.gallery.xml?uuid=49336a18-051c-11e3-98a0-002128161462\">" +
                "<DIHeadlineCopy/></a></p></body>";

        String processedSlideshow = "<body><p>Embedded Slideshow</p>" +
                "<p><a data-asset-type=\"slideshow\" data-embedded=\"true\" href=\"https://www.ft.com/content/49336a18-051c-11e3-98a0-002128161462\"></a></p></body>";

        checkTransformation(slideshowFromMethode, processedSlideshow);
    }

    @Test
    public void shouldNotBarfOnTwoSlideshows() {
        String slideshowFromMethode = "<body><p>Embedded Slideshow</p>" +
                "<p><a type=\"slideshow\" dtxInsert=\"slideshow\" href=\"/FT/Content/Companies/Stories/Live/PlainSlideshow.gallery.xml?uuid=49336a18-051c-11e3-98a0-002128161462\">" +
                "<DIHeadlineCopy>One typical, bog-standard slideshow headline update 1</DIHeadlineCopy></a></p>" +
                "<p><a type=\"slideshow\" dtxInsert=\"slideshow\" href=\"/FT/Content/Companies/Stories/Live/PlainSlideshow.gallery.xml?uuid=49336a18-051c-11e3-98a0-001234567890\">" +
                "<DIHeadlineCopy>One typical, bog-standard slideshow headline update 2</DIHeadlineCopy></a></p></body>";

        String processedSlideshow = "<body><p>Embedded Slideshow</p>" +
                "<p><a data-asset-type=\"slideshow\" data-embedded=\"true\" href=\"https://www.ft.com/content/49336a18-051c-11e3-98a0-002128161462\"></a></p>" +
                "<p><a data-asset-type=\"slideshow\" data-embedded=\"true\" href=\"https://www.ft.com/content/49336a18-051c-11e3-98a0-001234567890\"></a></p>" +
                "</body>";

        checkTransformation(slideshowFromMethode, processedSlideshow);
    }

    @Test
    public void timelineShouldBeRetained() {
        String timelineFromMethode = "<body><p>Intro text</p>" +
                "<timeline><timeline-header>The battle for Simandou</timeline-header>\n" +
                "<timeline-credits>AFP, Bloomberg, Shawn Curry, Company handouts</timeline-credits>\n" +
                "<timeline-sources>FT Research</timeline-sources>\n" +
                "<timeline-byline>Tom Burgis, Callum Locke, Katie Carnie, Steve Bernard</timeline-byline>\n" +
                "<timeline-item>\n<timeline-image fileref=\"/FT/Graphics/Online/Master_2048x1152/Martin/mas_Microsoft-Surface-tablet--566x318.jpg?uuid=213bb10c-71fe-11e2-8104-002128161462\" height=\"1152\" tmx=\"566 318 164 92\" width=\"2048\" xtransform=\" scale(0.2897527 0.2897527)\"></timeline-image>\n" +
                "<timeline-date>1997-01-01 00:00:00</timeline-date>\n" +
                "<timeline-title>1997</timeline-title>\n" +
                "<timeline-body><p>Rio Tinto is granted rights to explore the Simandou deposit</p>\n</timeline-body>\n</timeline-item>\n" +
                "</timeline></body>";

        String processedTimeline = "<body><p>Intro text</p>" +
                "<ft-timeline><timeline-header>The battle for Simandou</timeline-header>\n" +
                "<timeline-credits>AFP, Bloomberg, Shawn Curry, Company handouts</timeline-credits>\n" +
                "<timeline-sources>FT Research</timeline-sources>\n" +
                "<timeline-byline>Tom Burgis, Callum Locke, Katie Carnie, Steve Bernard</timeline-byline>\n" +
                "<timeline-item>\n<timeline-image><ft-content data-embedded=\"true\" url=\"http://api.ft.com/content/213bb10c-71fe-11e2-1f62-97bbf262bf2b\" type=\"http://www.ft.com/ontology/content/ImageSet\"></ft-content></timeline-image>\n" +
                "<timeline-date>1997-01-01 00:00:00</timeline-date>\n" +
                "<timeline-title>1997</timeline-title>\n" +
                "<timeline-body><p>Rio Tinto is granted rights to explore the Simandou deposit</p>\n</timeline-body>\n</timeline-item>" +
                "</ft-timeline></body>";

        checkTransformation(timelineFromMethode, processedTimeline);
    }


    @Test
    public void timelineShouldBeWrittenOutsideOfPtags() {
        String timelineFromMethode = "<body><p>Intro text</p><p>" +
                "<timeline><timeline-header>The battle for Simandou</timeline-header>\n" +
                "<timeline-credits>AFP, Bloomberg, Shawn Curry, Company handouts</timeline-credits>\n" +
                "<timeline-sources>FT Research</timeline-sources>\n" +
                "<timeline-byline>Tom Burgis, Callum Locke, Katie Carnie, Steve Bernard</timeline-byline>\n" +
                "<timeline-item>\n<timeline-image fileref=\"/FT/Graphics/Online/Master_2048x1152/Martin/mas_Microsoft-Surface-tablet--566x318.jpg?uuid=213bb10c-71fe-11e2-8104-002128161462\" height=\"1152\" tmx=\"566 318 164 92\" width=\"2048\" xtransform=\" scale(0.2897527 0.2897527)\"></timeline-image>\n" +
                "<timeline-date>1997-01-01 00:00:00</timeline-date>\n" +
                "<timeline-title>1997</timeline-title>\n" +
                "<timeline-body><p>Rio Tinto is granted rights to explore the Simandou deposit</p>\n</timeline-body>\n</timeline-item>\n" +
                "</timeline></p></body>";

        String processedTimeline = "<body><p>Intro text</p>" +
                "<ft-timeline><timeline-header>The battle for Simandou</timeline-header>\n" +
                "<timeline-credits>AFP, Bloomberg, Shawn Curry, Company handouts</timeline-credits>\n" +
                "<timeline-sources>FT Research</timeline-sources>\n" +
                "<timeline-byline>Tom Burgis, Callum Locke, Katie Carnie, Steve Bernard</timeline-byline>\n" +
                "<timeline-item>\n<timeline-image><ft-content data-embedded=\"true\" url=\"http://api.ft.com/content/213bb10c-71fe-11e2-1f62-97bbf262bf2b\" type=\"http://www.ft.com/ontology/content/ImageSet\"></ft-content></timeline-image>\n" +
                "<timeline-date>1997-01-01 00:00:00</timeline-date>\n" +
                "<timeline-title>1997</timeline-title>\n" +
                "<timeline-body><p>Rio Tinto is granted rights to explore the Simandou deposit</p>\n</timeline-body>\n</timeline-item>" +
                "</ft-timeline></body>";

        checkTransformation(timelineFromMethode, processedTimeline);
    }

    @Test
    public void shouldProcessPodcastsCorrectly() {
        String podcastFromMethode = "<body><script type=\"text/javascript\" src=\"http://podcast.ft.com/embed.js\">\n" +
                "</script><script type=\"text/javascript\">/* <![CDATA[ */window.onload=function(){embedLink('podcast.ft.com','2463','18','lucy060115.mp3','Golden Flannel of the year award','Under Tim Cook’s leadership, Apple succumbed to drivel, says Lucy Kellaway','ep_2463','share_2463');}/* ]]> */\n" +
                "</script></body>";
        String processedPodcast = "<body><a data-asset-type=\"podcast\" data-embedded=\"true\" href=\"http://podcast.ft.com/p/2463\" title=\"Golden Flannel of the year award\"></a></body>";
        checkTransformation(podcastFromMethode, processedPodcast);
    }

    @Test
    public void shouldProcessMultiplePodcastsCorrectly() {
        String podcastFromMethode = "<body><script type=\"text/javascript\" src=\"http://podcast.ft.com/embed.js\">\n" +
                "</script><script type=\"text/javascript\">/* <![CDATA[ */window.onload=function(){embedLink('podcast.ft.com','2463','18','lucy060115.mp3','Golden Flannel of the year award','Under Tim Cook’s leadership, Apple succumbed to drivel, says Lucy Kellaway','ep_2463','share_2463');}/* ]]> */\n</script>" +
                "<script type=\"text/javascript\" src=\"http://podcast.ft.com/embed.js\">\n" +
                "</script><script type=\"text/javascript\">/* <![CDATA[ */window.onload=function(){embedLink('podcast.ft.com','2463','18','lucy060115.mp3','Golden Flannel of the year award 2','Under Tim Cook’s leadership, Apple succumbed to drivel, says Lucy Kellaway','ep_2463','share_2463');}/* ]]> */\n" +
                "</script></body>";
        String processedPodcast = "<body><a data-asset-type=\"podcast\" data-embedded=\"true\" href=\"http://podcast.ft.com/p/2463\" title=\"Golden Flannel of the year award\"></a>" +
                "<a data-asset-type=\"podcast\" data-embedded=\"true\" href=\"http://podcast.ft.com/p/2463\" title=\"Golden Flannel of the year award 2\"></a></body>";
        checkTransformation(podcastFromMethode, processedPodcast);
    }

    @Test
    public void brightcove_shouldProcessVideoTagCorrectly() {
        String videoTextfromMethode = "<body>" +
                "<videoPlayer videoID=\"3920663836001\">" +
                "<web-inline-picture id=\"U2113113643377jlC\" width=\"150\" fileref=\"/FT/Graphics/Online/Z_Undefined/FT-video-story.jpg?uuid=91b39ae8-ccff-11e1-92c1-00144feabdc0\" tmx=\"150 100 150 100\"/>\n" +
                "</videoPlayer>" +
                "</body>";
        String processedVideoText = "<body><ft-content url=\"http://api.ft.com/content/28533356-911a-3352-a3cf-06f688157c58\" data-embedded=\"true\" type=\"http://www.ft.com/ontology/content/Video\"></ft-content></body>";
        checkTransformation(videoTextfromMethode, processedVideoText);
    }

    @Test
    public void brightcove_shouldFallbackWhenVideoTagErrors() {
        String videoTextfromMethode = "<body><videoPlayer><web-inline-picture id=\"U2113113643377jlC\" width=\"150\" fileref=\"/FT/Graphics/Online/Z_Undefined/FT-video-story.jpg?uuid=91b39ae8-ccff-11e1-92c1-00144feabdc0\" tmx=\"150 100 150 100\"/>\n" +
                "</videoPlayer></body>";
        String processedVideoText = "<body></body>";
        checkTransformation(videoTextfromMethode, processedVideoText);
    }

    @Test
    public void brightcove_shouldFallbackWhenVideoTagErrors2() {
        String videoTextfromMethode = "<body><videoPlayer videoID=\"\"><web-inline-picture id=\"U2113113643377jlC\" width=\"150\" fileref=\"/FT/Graphics/Online/Z_Undefined/FT-video-story.jpg?uuid=91b39ae8-ccff-11e1-92c1-00144feabdc0\" tmx=\"150 100 150 100\"/>\n" +
                "</videoPlayer></body>";
        String processedVideoText = "<body></body>";
        checkTransformation(videoTextfromMethode, processedVideoText);
    }


    @Test
    public void shouldProcessVimeoTagCorrectly() {
        String videoTextfromMethode = "<body><p align=\"left\" channel=\"FTcom\">Vimeo Video<iframe height=\"245\" frameborder=\"0\" allowfullscreen=\"\" src=\"http://player.vimeo.com/video/77761436\" width=\"600\"></iframe></p></body>";
        String processedVideoText = "<body><p>Vimeo Video<a href=\"https://www.vimeo.com/77761436\" data-embedded=\"true\" data-asset-type=\"video\"></a></p></body>";
        when(videoMatcher.filterVideo(any(RichContentItem.class))).thenReturn(exampleVimeoVideo);
        checkTransformation(videoTextfromMethode, processedVideoText);
    }

    @Test
    public void shouldProcessVimeoTagWithNoProtocolCorrectly() {
        String videoTextfromMethode = "<body><p align=\"left\" channel=\"FTcom\">Vimeo Video<iframe height=\"245\" frameborder=\"0\" allowfullscreen=\"\" src=\"//player.vimeo.com/video/77761436\" width=\"600\"></iframe></p></body>";
        String processedVideoText = "<body><p>Vimeo Video<a href=\"https://www.vimeo.com/77761436\" data-embedded=\"true\" data-asset-type=\"video\"></a></p></body>";
        when(videoMatcher.filterVideo(any(RichContentItem.class))).thenReturn(exampleVimeoVideo);
        checkTransformation(videoTextfromMethode, processedVideoText);
    }

    @Test
    public void shouldProcessYouTubeVideoCorrectly_withPChannel() {
        String videoTextfromMethode = "<body><p align=\"left\" channel=\"FTcom\">Youtube Video<iframe height=\"245\" frameborder=\"0\" allowfullscreen=\"\" src=\"http://www.youtube.com/embed/OTT5dQcarl0\" width=\"600\"></iframe></p></body>";
        String processedVideoText = "<body><p>Youtube Video<a href=\"https://www.youtube.com/watch?v=OTT5dQcarl0\" data-embedded=\"true\" data-asset-type=\"video\"></a></p></body>";
        when(videoMatcher.filterVideo(any(RichContentItem.class))).thenReturn(exampleYouTubeVideo);
        checkTransformation(videoTextfromMethode, processedVideoText);
    }

    @Test
    public void shouldProcessYouTubeVideoCorrectly_withNoPChannel() {
        String videoTextfromMethode = "<body><p>Youtube Video<iframe height=\"245\" frameborder=\"0\" allowfullscreen=\"\" src=\"http://www.youtube.com/embed/OTT5dQcarl0\" width=\"600\"></iframe></p></body>";
        String processedVideoText = "<body><p>Youtube Video<a href=\"https://www.youtube.com/watch?v=OTT5dQcarl0\" data-embedded=\"true\" data-asset-type=\"video\"></a></p></body>";
        when(videoMatcher.filterVideo(any(RichContentItem.class))).thenReturn(exampleYouTubeVideo);
        checkTransformation(videoTextfromMethode, processedVideoText);
    }

    @Test
    public void shouldProcessYouTubeVideoWithHttpsCorrectly() {
        String videoTextfromMethode = "<body><p align=\"left\" channel=\"FTcom\">Youtube Video<iframe height=\"245\" frameborder=\"0\" allowfullscreen=\"\" src=\"https://www.youtube.com/embed/OTT5dQcarl0\" width=\"600\"></iframe></p></body>";
        String processedVideoText = "<body><p>Youtube Video<a href=\"https://www.youtube.com/watch?v=OTT5dQcarl0\" data-embedded=\"true\" data-asset-type=\"video\"></a></p></body>";
        when(videoMatcher.filterVideo(any(RichContentItem.class))).thenReturn(exampleYouTubeVideo);
        checkTransformation(videoTextfromMethode, processedVideoText);
    }

    @Test
    public void shouldProcessInteractiveGraphics() {
        when(interactiveGraphicsMatcher.matches(anyString())).thenReturn(true);
        for (final String url : INTERACTIVE_GRAPHICS_URLS) {
            final String nativeText = "<body><p>The table below:</p><p><iframe allowTransparency=\"true\" frameborder=\"0\" scrolling=\"no\" src=\"" + url + "\"/></p></body>";
            final String processedText = "<body><p>The table below:</p><p><a data-asset-type=\"interactive-graphic\" href=\"" + url + "\"></a></p></body>";
            checkTransformation(nativeText, processedText);
        }
    }

    @Test
    public void shouldProcessInteractiveGraphicsWidthAndHeight() {
        final String nativeText = "<body><p>The table below:</p><p><iframe allowTransparency=\"true\" frameborder=\"0\" height=\"670\" scrolling=\"no\" src=\"http://www.ft.com/ig/widgets/profiles-tiled-layout/1.1.0/index.html?id=0AnE0wMr-SY-LdENVcHdXNDI5dkFKV1FicnZ0RUMweXc\" width=\"980\"/></p></body>";
        final String processedText = "<body><p>The table below:</p><p><a data-asset-type=\"interactive-graphic\" href=\"http://www.ft.com/ig/widgets/profiles-tiled-layout/1.1.0/index.html?id=0AnE0wMr-SY-LdENVcHdXNDI5dkFKV1FicnZ0RUMweXc\" data-width=\"980\" data-height=\"670\"></a></p></body>";
        when(interactiveGraphicsMatcher.matches(anyString())).thenReturn(true);
        checkTransformation(nativeText, processedText);
    }

    @Test
    public void shouldNotProcessOtherIframes() {
        String videoTextfromMethode = "<body><p align=\"left\" channel=\"FTcom\"><iframe height=\"245\" frameborder=\"0\" allowfullscreen=\"\" src=\"http://www.bbc.co.uk/video/OTT5dQcarl0\" width=\"600\"></iframe></p></body>";
        String processedVideoText = "<body></body>";
        checkTransformation(videoTextfromMethode, processedVideoText);
    }


    @Test
    public void shouldNotProcessOtherIframes_withTextInbetween() {
        String videoTextfromMethode = "<body><p channel=\"FTcom\">Video<iframe height=\"245\" frameborder=\"0\" allowfullscreen=\"\" src=\"http://www.notyoutube.com/junk\" width=\"600\"></iframe></p></body>";
        String processedVideoText = "<body><p>Video</p></body>";
        checkTransformation(videoTextfromMethode, processedVideoText);
    }

    @Test
    public void shouldNotProcessOtherScriptTags() {
        String podcastFromMethode = "<body><script type=\"text/javascript\">/* <![CDATA[ */window.onload=function(){alert('Something!')}/* ]]> */\n" +
                "\"</script></body>";
        String processedPodcast = "<body></body>";
        checkTransformation(podcastFromMethode, processedPodcast);
    }

    @Test
    public void shouldNotProcessOtherScriptTags2() {
        String podcastFromMethode = "<body><script type=\"text/javascript\" src=\"http://someStyleSheet\"></script></body>";
        String processedPodcast = "<body></body>";
        checkTransformation(podcastFromMethode, processedPodcast);
    }

    @Test
    public void removeExcessSpacesBetweenParagraphBlocks() {
        String expectedSentence = String.format("<body><p>This</p>\n<p>is</p>\n<p>a test</p></body>");
        checkTransformation("<body><p>This</p>\n\n\n\n<p>is</p>\n<p>a test</p></body>", expectedSentence);
    }

    @Test
    public void removeEmptyParagraphs() {
        String expectedSentence = String.format("<body><p>This one is not empty</p>" +
                "<p>This should not be removed <br/></p></body>");
        checkTransformation("<body><p>This one is not empty</p><p>This should not be removed <br/></p><p><br/></p><p> <br/> </p></body>", expectedSentence);
    }

    @Test
    public void shouldTransformSubheadIntoH3ClassFtSubhead() {
        String subheadFromMethode = "<body><p>his is some normal text.</p><p>More text</p><subhead>This is a subhead.</subhead><p>This is some more normal text.</p><subhead>This is another subhead.</subhead></body>";
        String processedSubhead = "<body><p>his is some normal text.</p><p>More text</p><h3 class=\"ft-subhead\">This is a subhead.</h3><p>This is some more normal text.</p><h3 class=\"ft-subhead\">This is another subhead.</h3></body>";
        checkTransformation(subheadFromMethode, processedSubhead);
    }

    @Test
    public void shouldRemoveElementsAndContentWithChannelAttributeThatAreStrikeouts() {
        String contentWithStrikeouts = "<body><b channel=\"Financial Times\">Should be removed</b><b>Should Stay</b><p channel=\"!Strikeout\">Should be removed</p><p>Text inside normal p tag should remain</p></body>";
        String transformedContent = "<body><strong>Should Stay</strong><p>Text inside normal p tag should remain</p></body>";
        checkTransformation(contentWithStrikeouts, transformedContent);
    }

    @Test
    public void shouldRetainElementsAndContentWithChannelAttributesThatAreNotStrikeouts() {
        String contentWithStrikeouts = "<body><p channel=\"FTcom\">Random Text<iframe src=\"http://www.youtube.com/embed/OTT5dQcarl0\"></iframe></p><b channel=\"!Financial Times\">Not Financial Times</b></body>";
        String transformedContent = "<body><p>Random Text<a href=\"https://www.youtube.com/watch?v=OTT5dQcarl0\" data-asset-type=\"video\" data-embedded=\"true\"/></p><strong>Not Financial Times</strong></body>";
        when(videoMatcher.filterVideo(any(RichContentItem.class))).thenReturn(exampleYouTubeVideo);
        checkTransformation(contentWithStrikeouts, transformedContent);
    }

    @Test
    public void testShouldRetainImgTagAndValidAttributes() throws Exception {
        String contentExternalImage = "<body><img src=\"someImage.jpg\" alt=\"someAltText\" width=\"200\" height=\"200\" align=\"left\"/></body>";
        String transformedContent = "<body><img src=\"someImage.jpg\" alt=\"someAltText\" width=\"200\" height=\"200\"/></body>";
        checkTransformation(contentExternalImage, transformedContent);
    }

    @Test
    public void shouldStripAllAnnotationTagsFromContent() throws Exception {
        String contentWithAnnotation = "<body><p>This is <annotation c=\"roddamm\" cd=\"20150224170716\">A new annotation </annotation>annotated</p></body>";
        String transformedContent = "<body><p>This is annotated</p></body>";
        checkTransformation(contentWithAnnotation, transformedContent);
    }

    @Test
    public void shouldStripAllNotesFromContent() throws Exception {
        String contentWithNotes = "<body><p><span class=\"@notes\">Test notes</span>This text should remain</p></body>";
        String transformedContent = "<body><p>This text should remain</p></body>";
        checkTransformation(contentWithNotes, transformedContent);
    }

    @Test
    public void shouldTransformLayoutToDiv() {
        String contentWithNotes = "<body><layout data-layout-name=\"tbd\" data-layout-width=\"full-bleed\"><p>Content</p></layout></body>";
        String transformedContent = "<body><div class=\"layout\" data-layout-name=\"tbd\" data-layout-width=\"full-bleed\"><p>Content</p></div></body>";
        checkTransformation(contentWithNotes, transformedContent);
    }

    @Test
    public void shouldTransformLayoutWithoutContentToDiv() {
        String contentWithNotes = "<body><layout data-layout-name=\"tbd\" data-layout-width=\"full-bleed\"></layout></body>";
        String transformedContent = "<body><div class=\"layout\" data-layout-name=\"tbd\" data-layout-width=\"full-bleed\"></div></body>";
        checkTransformation(contentWithNotes, transformedContent);
    }

    @Test
    public void shouldTransformLayoutSlotToDiv() {
        String contentWithNotes = "<body><layout-slot><p>Content</p></layout-slot><p>Content</p><layout-slot><p>Content</p></layout-slot></body>";
        String transformedContent = "<body><div class=\"layout-slot\"><p>Content</p></div><p>Content</p><div class=\"layout-slot\"><p>Content</p></div></body>";
        checkTransformation(contentWithNotes, transformedContent);
    }

    @Test
    public void shouldTransformLayoutSlotWithoutContentToDiv() {
        String contentWithNotes = "<body><layout-slot></layout-slot><layout-slot></layout-slot></body>";
        String transformedContent = "<body><div class=\"layout-slot\"/><div class=\"layout-slot\"/></body>";
        checkTransformation(contentWithNotes, transformedContent);
    }

    @Test
    public void shouldTransformLayoutWithLayoutSlotsWithoutContentToDiv() {
        String contentWithNotes = "<body><layout data-layout-name=\"tbd\" data-layout-width=\"full-bleed\"><layout-slot></layout-slot><layout-slot></layout-slot></layout></body>";
        String transformedContent = "<body><div class=\"layout\" data-layout-name=\"tbd\" data-layout-width=\"full-bleed\"><div class=\"layout-slot\"/><div class=\"layout-slot\"/></div></body>";
        checkTransformation(contentWithNotes, transformedContent);
    }

    @Test
    public void shouldTransformLayoutWithLayoutSlotsWithContentToDiv() {
        String contentWithNotes = "<body><p>Content</p><layout data-layout-name=\"tbd\" data-layout-width=\"full-bleed\"><layout-slot><p>Content</p></layout-slot><layout-slot><p>Content</p></layout-slot></layout></body>";
        String transformedContent = "<body><p>Content</p><div class=\"layout\" data-layout-name=\"tbd\" data-layout-width=\"full-bleed\"><div class=\"layout-slot\"><p>Content</p></div><div class=\"layout-slot\"><p>Content</p></div></div></body>";
        checkTransformation(contentWithNotes, transformedContent);
    }

    @Test
    public void shouldTransformLayoutSetToDiv() {
        String contentWithNotes = "<body><layout-set></layout-set></body>";
        String transformedContent = "<body><div class=\"layout-set\"></div></body>";
        checkTransformation(contentWithNotes, transformedContent);
    }

    @Test
    public void shouldTransformLayoutSetWithLayoutAndLayoutSlotToDiv() {
        String contentWithNotes = "<body><layout-set><layout data-layout-name=\"tbd\" data-layout-width=\"full-bleed\"><layout-slot><p>Content</p></layout-slot></layout></layout-set></body>";
        String transformedContent = "<body><div class=\"layout-set\"><div class=\"layout\" data-layout-name=\"tbd\" data-layout-width=\"full-bleed\"><div class=\"layout-slot\"><p>Content</p></div></div></div></body>";
        checkTransformation(contentWithNotes, transformedContent);
    }

    @Test
    public void shouldTransformLayoutSetWithLayoutAndLayoutSlotWithoutContentToDiv() {
        String contentWithNotes = "<body><layout-set><layout data-layout-name=\"tbd\" data-layout-width=\"full-bleed\"><layout-slot></layout-slot></layout></layout-set></body>";
        String transformedContent = "<body><div class=\"layout-set\"><div class=\"layout\" data-layout-name=\"tbd\" data-layout-width=\"full-bleed\"><div class=\"layout-slot\"></div></div></div></body>";
        checkTransformation(contentWithNotes, transformedContent);
    }

    @Test
    public void shouldTransformLayoutComponentWithInlineImage() {
        String contentWithNotes = "<body><layout><layout-slot data-slot-width=\"tbd\">" +
                "<web-inline-picture fileref=\"/FT/Graphics/Online/Table_square/2015/08/southwark%20crown%20court_20150803163439.jpg?uuid=8924d91e-39fd-11e5-bbd1-b37bc06f590c\" tmx=\"120 120 120 120\"/>" +
                "</layout-slot><layout-slot>" +
                "<h3>Header text</h3>" +
                "<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed feugiat turpis at massa tristique sagittis. </p>" +
                "</layout-slot></layout></body>";
        String transformedContent = "<body><div class=\"layout\"><div class=\"layout-slot\" data-slot-width=\"tbd\">" +
                "<ft-content data-embedded=\"true\" url=\"http://api.ft.com/content/8924d91e-39fd-11e5-25b7-24e11a1bf245\" type=\"http://www.ft.com/ontology/content/ImageSet\"></ft-content>" +
                "</div><div class=\"layout-slot\">" +
                "<h3>Header text</h3>" +
                "<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed feugiat turpis at massa tristique sagittis. </p>" +
                "</div></div></body>";
        checkTransformation(contentWithNotes, transformedContent);
    }

    @Test
    public void shouldTransformLayoutComponentWithTextListItems() {
        String contentWithNotes = "<body><layout><layout-slot data-slot-width=\"tbd\">" +
                "<ul>" +
                "<li>List item</li>" +
                "<li>List item</li>" +
                "<li>List item</li>" +
                "</ul>" +
                "</layout-slot><layout-slot>" +
                "<h3>Header text</h3>" +
                "<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed feugiat turpis at massa tristique sagittis. </p>" +
                "</layout-slot></layout></body>";
        String transformedContent = "<body><div class=\"layout\"><div class=\"layout-slot\" data-slot-width=\"tbd\">" +
                "<ul>" +
                "<li>List item</li>" +
                "<li>List item</li>" +
                "<li>List item</li>" +
                "</ul>" +
                "</div><div class=\"layout-slot\">" +
                "<h3>Header text</h3>" +
                "<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed feugiat turpis at massa tristique sagittis. </p>" +
                "</div></div></body>";
        checkTransformation(contentWithNotes, transformedContent);
    }

    @Test
    public void shouldUseArticleUuidAndImageSetId() throws Exception {
        final String bodyWithImageSets = readFromFile("body/embedded_image_set_body.xml");
        final String articleUuid = UUID.randomUUID().toString();

        final String transformedBody =
                bodyTransformer.transform(
                        bodyWithImageSets,
                        TRANSACTION_ID,
                        Maps.immutableEntry("uuid", articleUuid));

        final UUID firstImageSetUuid = GenerateV3UUID.singleDigested(articleUuid + FIRST_EMBEDDED_IMAGE_SET_ID);
        final UUID secondImageSetUuid = GenerateV3UUID.singleDigested(articleUuid + SECOND_EMBEDDED_IMAGE_SET_ID);

        assertThat(transformedBody, containsString(firstImageSetUuid.toString()));
        assertThat(transformedBody, containsString(secondImageSetUuid.toString()));
    }

    @Test
    public void shouldKeepAnchorTagsFromRecommended() {
        String originalRecommendedContent = "<body><recommended><recommended-title/><ul><li><a href=\"link\">Headline</a></li></ul></recommended></body>";
        String transformedContent = "<body><recommended><recommended-title/><ul><li><a href=\"link\">Headline</a></li></ul></recommended></body>";
        checkTransformation(originalRecommendedContent, transformedContent);
    }

    @Test
    public void shouldRemoveEmptyIntroFromRecommended() {
        String originalRecommendedContent = "<body><recommended><p></p><ul><li><a href=\"link\">Headline</a></li></ul></recommended></body>";
        String transformedContent = "<body><recommended><recommended-title/><ul><li><a href=\"link\">Headline</a></li></ul></recommended></body>";
        checkTransformation(originalRecommendedContent, transformedContent);
    }

    @Test
    public void shouldRemoveDummyTextIntroFromRecommended() {
        String originalRecommendedContent = "<body><recommended><p><?EM-dummyText [Headline]?></p><ul><li><a href=\"link\">Headline</a></li></ul></recommended></body>";
        String transformedContent = "<body><recommended><recommended-title/><ul><li><a href=\"link\">Headline</a></li></ul></recommended></body>";
        checkTransformation(originalRecommendedContent, transformedContent);
    }

    @Test
    public void shouldKeepIntroIfNotEmptyFromRecommended() {
        String originalRecommendedContent = "<body><recommended><p>Intro</p><ul><li><a href=\"link\">Headline</a></li></ul></recommended></body>";
        String transformedContent = "<body><recommended><recommended-title/><p>Intro</p><ul><li><a href=\"link\">Headline</a></li></ul></recommended></body>";
        checkTransformation(originalRecommendedContent, transformedContent);
    }

    @Test
    public void shouldAddMissingRecommendedTitleFromRecommended() {
        String originalRecommendedContent = "<body><recommended><p></p><ul><li><a href=\"link\">Headline</a></li></ul></recommended></body>";
        String transformedContent = "<body><recommended><recommended-title/><ul><li><a href=\"link\">Headline</a></li></ul></recommended></body>";
        checkTransformation(originalRecommendedContent, transformedContent);
    }

    @Test
    public void shouldNotAddRecommendedIfAnchorsAreMissingFromRecommended() {
        String originalRecommendedContent = "<body><recommended></recommended></body>";
        String transformedContent = "<body></body>";
        checkTransformation(originalRecommendedContent, transformedContent);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldTransformMethodeLinkFromRecommended() {
        when(documentStoreApiClient.getContentForUuids(anyCollection(), anyString()))
                .thenReturn(Collections.singletonList(new Content(KITCHEN_SINK_ASSET5_UUID, "Article")));

        String originalRecommendedContent = "<body><recommended><ul><li><a href=\"/FT/Content/Companies/Stories/Live/GB/New%20UUID%20Generation%20for%20image-sets/rj/Article01%20without%20imageset.xml?uuid=" + KITCHEN_SINK_ASSET5_UUID + "\">Internal articles’s title added by methode automatically</a></li></ul></recommended></body>";
        String transformedContent = "<body><recommended><recommended-title/><ul><li><ft-content type=\"http://www.ft.com/ontology/content/Article\" url=\"http://api.ft.com/content/" + KITCHEN_SINK_ASSET5_UUID + "\">Internal articles’s title added by methode automatically</ft-content></li></ul></recommended></body>";
        checkTransformation(originalRecommendedContent, transformedContent);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldTransformManualInternalLinkFromRecommended() {
        when(documentStoreApiClient.getContentForUuids(anyCollection(), anyString()))
                .thenReturn(Collections.singletonList(new Content(KITCHEN_SINK_ASSET5_UUID, "Article")));

        String originalRecommendedContent = "<body><recommended><ul><li><a href=\"http://www.ft.com/content/" + KITCHEN_SINK_ASSET5_UUID + "\">Manually added FT article’s manual title</a></li></ul></recommended></body>";
        String transformedContent = "<body><recommended><recommended-title/><ul><li><ft-content type=\"http://www.ft.com/ontology/content/Article\" url=\"http://api.ft.com/content/" + KITCHEN_SINK_ASSET5_UUID + "\">Manually added FT article’s manual title</ft-content></li></ul></recommended></body>";
        checkTransformation(originalRecommendedContent, transformedContent);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void shouldTransformManualAudioLinkFromRecommended() {
        when(documentStoreApiClient.getContentForUuids(anyCollection(), anyString()))
                .thenReturn(Collections.singletonList(new Content(AUDIO_UUID, "Audio")));

        String originalRecommendedContent = "<body><recommended><ul><li><a href=\"http://www.ft.com/content/" + AUDIO_UUID + "\">Manually added FT audio’s manual title</a></li></ul></recommended></body>";
        String transformedContent = "<body><recommended><recommended-title/><ul><li><ft-content type=\"http://www.ft.com/ontology/content/Audio\" url=\"http://api.ft.com/content/" + AUDIO_UUID + "\">Manually added FT audio’s manual title</ft-content></li></ul></recommended></body>";
        checkTransformation(originalRecommendedContent, transformedContent);
    }

    @Test
    public void shouldKeepExternalLinkFromRecommended() {
        String originalRecommendedContent = "<body><recommended><ul><li><a href=\"http://example.com/manually/added/document1.pdf\">External link’s manually added title</a></li></ul></recommended></body>";
        String transformedContent = "<body><recommended><recommended-title/><ul><li><a href=\"http://example.com/manually/added/document1.pdf\">External link’s manually added title</a></li></ul></recommended></body>";
        checkTransformation(originalRecommendedContent, transformedContent);
    }

    @Test
    public void shouldRemoveListItemIfAnchorHeadlineContainsDummyTextFromRecommended() {
        String originalRecommendedContent = "<body><recommended><recommended-title/><ul><li><a href=\"link\">Headline</a></li><li><a href=\"link\"><?EM-dummyText [Headline]?></a></li></ul></recommended></body>";
        String transformedContent = "<body><recommended><recommended-title/><ul><li><a href=\"link\">Headline</a></li></ul></recommended></body>";
        checkTransformation(originalRecommendedContent, transformedContent);
    }

    @Test
    public void shouldRemoveListItemIfAnchorHeadlineIsEmptyFromRecommended() {
        String originalRecommendedContent = "<body><recommended><recommended-title/><ul><li><a href=\"link\">Headline</a></li><li><a href=\"link\"/></li></ul></recommended></body>";
        String transformedContent = "<body><recommended><recommended-title/><ul><li><a href=\"link\">Headline</a></li></ul></recommended></body>";
        checkTransformation(originalRecommendedContent, transformedContent);
    }

    @Test
    public void shouldRemoveListItemIfAnchorIsEmptyFromRecommended() {
        String originalRecommendedContent = "<body><recommended><ul><li><a href=\"http://example.com/manually/added/document1.pdf\">External link’s manually added title</a></li><li><a/></li></ul></recommended></body>";
        String transformedContent = "<body><recommended><recommended-title/><ul><li><a href=\"http://example.com/manually/added/document1.pdf\">External link’s manually added title</a></li></ul></recommended></body>";
        checkTransformation(originalRecommendedContent, transformedContent);
    }

    @Test
    public void shouldRemoveListItemIfAnchorIsMissingHrefFromRecommended() {
        String originalRecommendedContent = "<body><recommended><ul><li><a href=\"http://example.com/manually/added/document1.pdf\">External link’s manually added title</a></li><li><a>Some text</a></li></ul></recommended></body>";
        String transformedContent = "<body><recommended><recommended-title/><ul><li><a href=\"http://example.com/manually/added/document1.pdf\">External link’s manually added title</a></li></ul></recommended></body>";
        checkTransformation(originalRecommendedContent, transformedContent);
    }

    @Test
    public void shouldRemoveListItemIfAnchorHrefIsEmptyFromRecommended() {
        String originalRecommendedContent = "<body><recommended><ul><li><a href=\"link\">Valid</a></li><li><a href=\" \">Invalid</a></li></ul></recommended></body>";
        String transformedContent = "<body><recommended><recommended-title/><ul><li><a href=\"link\">Valid</a></li></ul></recommended></body>";
        checkTransformation(originalRecommendedContent, transformedContent);
    }

    @Test
    public void shouldKeepBlockquoteWithValidParagraph() {
        String originalContent = "<body><blockquote><p>Quoted text</p></blockquote></body>";
        String transformedContent = "<body><blockquote><p>Quoted text</p></blockquote></body>";
        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldKeepCiteTagInsideBlockquote() {
        String originalContent = "<body><blockquote><p>Quoted text</p><cite>Cite text</cite></blockquote></body>";
        String transformedContent = "<body><blockquote><p>Quoted text</p><cite>Cite text</cite></blockquote></body>";
        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldKeepAsideTag(){
        String originalContent = "<body><aside><h4>Epcot Center</h4><p>The Epcot Center is a theme park in Disney World, Florida.</p></aside></body>";
        String transformedContent ="<body><aside><h4>Epcot Center</h4><p>The Epcot Center is a theme park in Disney World, Florida.</p></aside></body>";
        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldKeepSTag(){
        String originalContent = "<body><p><s>My article is the best</s></p></body>";
        String transformedContent = "<body><p><s>My article is the best</s></p></body>";
        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldRemoveCiteTagOutsideBlockquote() {
        String originalContent = "<body><blockquote><p>Quoted text</p></blockquote><cite>Cite text</cite></body>";
        String transformedContent = "<body><blockquote><p>Quoted text</p></blockquote></body>";
        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldRemoveBlockquoteWithoutParagraphs() {
        String originalContent = "<body><blockquote><cite>Cite text</cite></blockquote></body>";
        String transformedContent = "<body/>";
        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldRemoveEmptyParagraphFromBlockquote() {
        String originalContent = "<body><blockquote><p></p><p>Quoted text</p><cite>Cite text</cite></blockquote></body>";
        String transformedContent = "<body><blockquote><p>Quoted text</p><cite>Cite text</cite></blockquote></body>";
        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldRemoveDummyTextParagraphFromBlockquote() {
        String originalContent = "<body><blockquote><p><?EM-dummyText [Quote]?></p><p>Quoted text</p><cite>Cite text</cite></blockquote></body>";
        String transformedContent = "<body><blockquote><p>Quoted text</p><cite>Cite text</cite></blockquote></body>";
        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldRemoveWhitespaceParagraphFromBlockquote() {
        String originalContent = "<body><blockquote><p> </p><p>Quoted text</p><cite>Cite text</cite></blockquote></body>";
        String transformedContent = "<body><blockquote><p>Quoted text</p><cite>Cite text</cite></blockquote></body>";
        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldRemoveEmptyCiteFromBlockquote() {
        String originalContent = "<body><blockquote><p>Quoted text</p><cite></cite></blockquote></body>";
        String transformedContent = "<body><blockquote><p>Quoted text</p></blockquote></body>";
        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldRemoveDummyTextCiteFromBlockquote() {
        String originalContent = "<body><blockquote><p>Quoted text</p><cite><?EM-dummyText [Quote]?></cite></blockquote></body>";
        String transformedContent = "<body><blockquote><p>Quoted text</p></blockquote></body>";
        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldRemoveWhitespaceCiteFromBlockquote() {
        String originalContent = "<body><blockquote><p>Quoted text</p><cite>  </cite></blockquote></body>";
        String transformedContent = "<body><blockquote><p>Quoted text</p></blockquote></body>";
        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void thatImagesAreExtractedFromParagraphs() {
        String articleUuid = "edf0d3db-6497-406a-9d40-79176b0ffadb";
        String imageSetId = "U32503569610592JkB";
        String generatedUuid = GenerateV3UUID.singleDigested(articleUuid + imageSetId).toString();
        String webInlinePictureUuid = "9b646f00-2045-11e8-a895-1ba1f72c2c11";
        String inlineImageUuid = DeriveUUID.with(IMAGE_SET).from(UUID.fromString(webInlinePictureUuid)).toString();

        String originalContent = "<body>" +
                "<p><image-set id=\"" + imageSetId + "\"><image-small/><image-medium/><image-large/></image-set></p>" +
                "<p><web-inline-picture fileref=\"/FT/Graphics/Online/Charts/2018/03/Italy_elections_provisional_results-column_chart-ft-web-thememed-700x500-1520234395_20180305071956.png?uuid=" + webInlinePictureUuid + "\" id=\"U32511615782384khF\"/></p>" +
                "<p><img src=\"img1\"/></p>" +
                "<p><img src=\"img2\"/>Lorem ipsum</p>" +
                "<p><a href=\"\"><img src=\"img3\"/>" +
                "<img src=\"img4\"/></a></p>" +
                "<p><a href=\"http://www.url.com\">Lorem ipsum<img src=\"img5\"/></a></p>" +
                "</body>";

        String transformedContent = "<body>" +
                "<ft-content data-embedded=\"true\" type=\"http://www.ft.com/ontology/content/ImageSet\" url=\"http://" + apiHost + "/content/" + generatedUuid + "\"></ft-content>" +
                "<ft-content type=\"http://www.ft.com/ontology/content/ImageSet\" url=\"http://" + apiHost + "/content/" + inlineImageUuid + "\" data-embedded=\"true\"></ft-content>" +
                "<img src=\"img1\"/>" +
                "<img src=\"img2\"/>" +
                "<p>Lorem ipsum</p>" +
                "<img src=\"img3\"/>" +
                "<img src=\"img4\"/>" +
                "<img src=\"img5\"/>" +
                "<p><a href=\"http://www.url.com\">Lorem ipsum</a></p>" +
                "</body>";

        checkTransformation(originalContent, transformedContent, Maps.immutableEntry("uuid", articleUuid));
    }

    @Test
    public void thatPromoBoxesAreExtractedFromParagraphs() {
        String originalContent = "<body>" +
                "<p><b>" +
                "<promo-box align=\"left\">&lt;" +
                "<table width=\"170px\" align=\"left\" cellpadding=\"6px\">" +
                "<tr><td><promo-title><p>In depth</p></promo-title></td></tr>" +
                "<tr><td><promo-headline><p><a href=\"http://www.ft.com/intl/indepth/climatechange\" title=\"Climate change in depth - FT.com\">Climate change</a></p></promo-headline></td></tr>" +
                "<tr><td><promo-image fileref=\"/FT/Graphics/Online/Secondary_%26_Triplet_167x96/2011/10/SEC_POWE.jpg?uuid=5e542eaa-f042-11e0-96d2-00144feab49a\" tmx=\"167 96 167 96\"/></td></tr>" +
                "<tr><td><promo-intro><p>The latest news and analysis on the world’s changing climate and the political moves afoot to tackle the problem</p></promo-intro></td></tr>" +
                "</table>&gt;" +
                "</promo-box>" +
                "Who wins and who loses?</b></p>" +
                "</body>";

        String transformedContent = "<body>" +
                "<promo-box>" +
                "<promo-title><p>In depth</p></promo-title>" +
                "<promo-headline><p><a href=\"http://www.ft.com/intl/indepth/climatechange\" title=\"Climate change in depth - FT.com\">Climate change</a></p></promo-headline>" +
                "<promo-image><ft-content type=\"http://www.ft.com/ontology/content/ImageSet\" url=\"http://" + apiHost + "/content/5e542eaa-f042-11e0-08b4-978e959e1fd3\" data-embedded=\"true\"></ft-content></promo-image>" +
                "<promo-intro><p>The latest news and analysis on the world’s changing climate and the political moves afoot to tackle the problem</p></promo-intro>" +
                "</promo-box>" +
                "<p><strong>Who wins and who loses?</strong></p>" +
                "</body>";

        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldRemoveEmptyElementsInsideParagraph() {
        String originalContent = "<body>" +
                "<p><strong></strong> lorem ipsum</p>" +
                "<p><strong> </strong></p>" +
                "<p><b></b><b></b>doler sit amet</p>" +
                "<p></p>" +
                "<em></em>" +
                "<span> </span>" +
                "</body>";
        String transformedContent = "<body>" +
                "<p> lorem ipsum</p>" +
                "<p>doler sit amet</p>" +
                "</body>";
        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldRemoveLineBreakBetweenParagraphs() {
        String originalContent = "<body>" +
                "<p>lorem ipsum</p>" +
                "<br />" +
                "<br/>" +
                " <br/> " +
                "<p>doler sit amet</p>" +
                "<p>doler sit amet</p>" +
                "</body>";
        String transformedContent = "<body>" +
                "<p>lorem ipsum</p>" +
                "<p>doler sit amet</p>" +
                "<p>doler sit amet</p>" +
                "</body>";

        checkTransformation(originalContent, transformedContent);
    }


    @Test
    public void shouldReplaceThreeDots() {
        String originalContent = "<body>Here is a text with three dots...</body>";
        String transformedContent = "<body>Here is a text with three dots\u2026</body>";

        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldReplaceThreeInterSpacedDots() {
        String originalContent = "<body>Here is a text with three dots. . .</body>";
        String transformedContent = "<body>Here is a text with three dots\u2026</body>";

        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldReplaceTwoConsecutiveHyphens() {
        String originalContent = "<body>Here is a text with two consecutive -- hyphens</body>";
        String transformedContent = "<body>Here is a text with two consecutive \u2013 hyphens</body>";

        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldReplaceThreeConsecutiveHyphens() {
        String originalContent = "<body>Here is a text with three consecutive --- hyphens</body>";
        String transformedContent = "<body>Here is a text with three consecutive \u2014 hyphens</body>";

        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldTransformPodcastPromoWithBothTitleAndDescription() {
        String originalContent = "<body><podcast-promo episode-uuid=\"16ec6a72-d5db-4322-96c6-314b051eb978\">" +
                "<h2>Both title and description fields populated</h2>\n" +
                "<p>Description here</p>\n" +
                "</podcast-promo></body>";
        String transformedContent = "<body><podcast-promo id=\"16ec6a72-d5db-4322-96c6-314b051eb978\">" +
                "<h2>Both title and description fields populated</h2>" +
                "<p>Description here</p>" +
                "</podcast-promo></body>";
        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldTransformPodcastPromoWithTitleButNoDescription() {
        String originalContent = "<body><podcast-promo episode-uuid=\"16ec6a72-d5db-4322-96c6-314b051eb978\">" +
                "<h2>Title but no description variant</h2>\n" +
                "<p><?EM-dummyText Podcast promo description (optional)?></p>\n" +
                "</podcast-promo></body>";
        String transformedContent = "<body><podcast-promo id=\"16ec6a72-d5db-4322-96c6-314b051eb978\">" +
                "<h2>Title but no description variant</h2>" +
                "</podcast-promo></body>";
        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldTransformPodcastPromoWithDescriptionButNoTitle() {
        String originalContent = "<body><podcast-promo episode-uuid=\"16ec6a72-d5db-4322-96c6-314b051eb978\">" +
                "<h2><?EM-dummyText Podcast promo title (optional)?></h2>\n" +
                "<p>Description but no title variant</p>\n" +
                "</podcast-promo></body>";
        String transformedContent = "<body><podcast-promo id=\"16ec6a72-d5db-4322-96c6-314b051eb978\">" +
                "<p>Description but no title variant</p>" +
                "</podcast-promo></body>";
        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldTransformPodcastPromoWithNoTitleAndNoDescription() {
        String originalContent = "<body><podcast-promo episode-uuid=\"16ec6a72-d5db-4322-96c6-314b051eb978\">" +
                "<h2><?EM-dummyText Podcast promo title (optional)?></h2>\n" +
                "<p><?EM-dummyText Podcast promo description (optional)?></p>\n" +
                "</podcast-promo></body>";
        String transformedContent = "<body><podcast-promo id=\"16ec6a72-d5db-4322-96c6-314b051eb978\"></podcast-promo></body>";
        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldSkipTransformationForPodcastPromoWithInvalidEpisodeUuid() {
        String originalContent = "<body><podcast-promo episode-uuid=\"16ec6a72-d5db-4322-96c6314051eb\">" +
                "<h2>Podcast promo with an invalid episode uuid</h2>\n" +
                "<p><?EM-dummyText Podcast promo description (optional)?></p>\n" +
                "</podcast-promo></body>";
        String transformedContent = "<body></body>";
        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldSkipTransformationForPodcastPromoWithMissingEpisodeUuid() {
        String originalContent = "<body><podcast-promo>" +
                "<h2>Podcast promo with no episode uuid</h2>\n" +
                "<p><?EM-dummyText Podcast promo description (optional)?></p>\n" +
                "</podcast-promo></body>";
        String transformedContent = "<body></body>";
        checkTransformation(originalContent, transformedContent);
    }

    @Test
    public void shouldTransformDynamicContent() {
        String dynamicContent = "<body><p>Embedded Dynamic Content</p>" +
                "<p><a type=\"DynamicContent\" dtxInsert=\"Interactive Graphic Link\" href=\"/FT/Content/World%20News/Stories/WebPublished/test%20ig%20story.xml?uuid=d02886fc-58ff-11e8-9859-6668838a4c10\">Interactive Graphic</a>" +
                "</p></body>";
        String processedDynamicContent = "<body><p>Embedded Dynamic Content</p>" +
                "<ft-content type=\"http://www.ft.com/ontology/content/DynamicContent\" data-embedded=\"true\" url=\"http://test.api.ft.com/content/d02886fc-58ff-11e8-9859-6668838a4c10\"></ft-content></body>";

        checkTransformation(dynamicContent, processedDynamicContent, Maps.immutableEntry("apiHost", "test.api.ft.com"));
    }

    @Test
    public void shouldExtractDynamicContentFromParagraphs() {
        String dynamicContent = "<body><p>Embedded Dynamic Content</p><p>" +
                "<a type=\"DynamicContent\" dtxInsert=\"Interactive Graphic Link\" href=\"/FT/Content/World%20News/Stories/WebPublished/test%20ig%20story.xml?uuid=d02886fc-58ff-11e8-9859-6668838a4c10\">Interactive Graphic</a>" +
                "<a type=\"DynamicContent\" dtxInsert=\"Interactive Graphic Link\" href=\"/FT/Content/World%20News/Stories/WebPublished/test%20ig%20story.xml?uuid=f1655aa4-6320-11e8-a39d-4df188287fff\">Interactive Graphic</a>" +
                "</p></body>";
        String processedDynamicContent = "<body><p>Embedded Dynamic Content</p>" +
                "<ft-content type=\"http://www.ft.com/ontology/content/DynamicContent\" data-embedded=\"true\" url=\"http://test.api.ft.com/content/d02886fc-58ff-11e8-9859-6668838a4c10\"></ft-content>" +
                "<ft-content type=\"http://www.ft.com/ontology/content/DynamicContent\" data-embedded=\"true\" url=\"http://test.api.ft.com/content/f1655aa4-6320-11e8-a39d-4df188287fff\"></ft-content>" +
                "</body>";

        checkTransformation(dynamicContent, processedDynamicContent, Maps.immutableEntry("apiHost", "test.api.ft.com"));
    }

    @Test
    public void shouldNotWriteDynamicContentWithMissingHref() {
        String dynamicContent = "<body><p>Embedded Dynamic Content</p><p>" +
                "<a type=\"DynamicContent\" dtxInsert=\"Interactive Graphic Link\">Interactive Graphic</a></p></body>";
        String processedDynamicContent = "<body><p>Embedded Dynamic Content</p></body>";

        checkTransformation(dynamicContent, processedDynamicContent, Maps.immutableEntry("apiHost", "test.api.ft.com"));
    }

    @Test
    public void shouldNotWriteDynamicContentIfHrefIsMissingQueryParams() {
        String dynamicContent = "<body><p>Embedded Dynamic Content</p><p>" +
                "<a type=\"DynamicContent\" dtxInsert=\"Interactive Graphic Link\" href=\"/FT/Content/World%20News/Stories/WebPublished/test%20ig%20story.xml\">Interactive Graphic</a>" +
                "</p></body>";
        String processedDynamicContent = "<body><p>Embedded Dynamic Content</p></body>";

        checkTransformation(dynamicContent, processedDynamicContent, Maps.immutableEntry("apiHost", "test.api.ft.com"));
    }

    private void checkTransformation(String originalBody, String expectedTransformedBody, Map.Entry<String, Object>... contextData) {
        String actualTransformedBody = bodyTransformer.transform(originalBody, TRANSACTION_ID, contextData);

        System.out.println("TRANSFORMED BODY:\n" + actualTransformedBody);

        assertThat(actualTransformedBody, is(identicalXmlTo(expectedTransformedBody)));
    }

    private void checkTransformationToEmpty(String originalBody) {
        String actualTransformedBody = bodyTransformer.transform(originalBody, TRANSACTION_ID);
        assertThat(actualTransformedBody, is(""));
    }

    private String readFromFile(String resourceName) {
        String bodyFromFile;
        try {
            bodyFromFile = Resources.toString(BodyProcessingFieldTransformerFactoryTest.class.getClassLoader().getResource(resourceName), Charsets.UTF_8);

            // because what we get back from the API uses UNIX line encodings, but when working locally on Windows, the expected file will have \r\n
            if (System.getProperty("line.separator").equals("\r\n")) {
                bodyFromFile = bodyFromFile.replace("\r", "");
            }

        } catch (IOException e) {
            throw new RuntimeException("Unexpected error reading in file", e);
        }

        return bodyFromFile;
    }

}

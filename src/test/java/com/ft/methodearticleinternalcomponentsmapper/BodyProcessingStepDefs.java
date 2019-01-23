package com.ft.methodearticleinternalcomponentsmapper;

import com.ft.bodyprocessing.richcontent.ConvertParameters;
import com.ft.bodyprocessing.richcontent.VideoMatcher;
import com.ft.bodyprocessing.richcontent.VideoSiteConfiguration;
import com.ft.bodyprocessing.xml.eventhandlers.TransformingEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandler;
import com.ft.methodearticleinternalcomponentsmapper.clients.ConcordanceApiClient;
import com.ft.methodearticleinternalcomponentsmapper.clients.DocumentStoreApiClient;
import com.ft.methodearticleinternalcomponentsmapper.model.Content;
import com.ft.methodearticleinternalcomponentsmapper.model.concordance.ConceptView;
import com.ft.methodearticleinternalcomponentsmapper.model.concordance.Concordance;
import com.ft.methodearticleinternalcomponentsmapper.model.concordance.Concordances;
import com.ft.methodearticleinternalcomponentsmapper.model.concordance.Identifier;
import com.ft.methodearticleinternalcomponentsmapper.transformation.BodyProcessingFieldTransformerFactory;
import com.ft.methodearticleinternalcomponentsmapper.transformation.FieldTransformer;
import com.ft.methodearticleinternalcomponentsmapper.transformation.InteractiveGraphicsMatcher;
import com.ft.methodearticleinternalcomponentsmapper.transformation.MethodeBodyTransformationXMLEventHandlerRegistry;
import com.google.common.collect.ImmutableList;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang.RandomStringUtils;
import org.codehaus.stax2.ri.evt.EntityReferenceEventImpl;
import org.codehaus.stax2.ri.evt.StartElementEventImpl;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;
import org.custommonkey.xmlunit.XMLAssert;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BodyProcessingStepDefs {

    private String methodeBodyText;
    private String transformedBodyText;

    private FieldTransformer bodyTransformer;

    private static final String TEXT = "Some text in between tags";
    private static final String API_HOST = "test.api.ft.com";

    private MethodeBodyTransformationXMLEventHandlerRegistry registry;

    private static final String TRANSACTION_ID = randomChars(10);
    private Map<String, String> rulesAndHandlers;

    private static final List<String> T = Collections.singletonList("t");
    private static final List<String> NONE = Collections.emptyList();

    private static final String CONVERT_FROM_PARAMETER = "start";
    private static final String CONVERTED_TO_PARAMETER = "t";
    private static final String CONVERSION_TEMPLATE = "%ss";
    private static final ConvertParameters CONVERT_PARAMETERS = new ConvertParameters(CONVERT_FROM_PARAMETER, CONVERTED_TO_PARAMETER, CONVERSION_TEMPLATE);
    private static final List<ConvertParameters> CONVERT_PARAMETERS_LIST = ImmutableList.of(CONVERT_PARAMETERS);

    private static final String CANONICAL_URL_TEMPLATE = "https://www.ft.com/content/%s";
    private final static String TME_AUTHORITY = "http://api.ft.com/system/FT-TME";
    private static final String TME_ID_CONCORDED = "TnN0ZWluX09OX0ZvcnR1bmVDb21wYW55X0M=-T04=";
    private static final String TME_ID_NOT_CONCORDED = "notconcorded";
    private static final String API_URL_CONCORDED = "http://api.ft.com/organisations/704a3225-9b5c-3b4f-93c7-8e6a6993bfb0";
    private static final String CONTENT_STORE_UUID = "fbbee07f-5054-4a42-b596-64e0625d19a6";
    private static final Identifier identifier = new Identifier(TME_AUTHORITY, TME_ID_CONCORDED);
    private static final ConceptView concept = new ConceptView(API_URL_CONCORDED, API_URL_CONCORDED);


    public static List<VideoSiteConfiguration> VIDEO_CONFIGS = Arrays.asList(
            new VideoSiteConfiguration("https?://www.youtube.com/watch\\?v=(?<id>[A-Za-z0-9_-]+)", "https://www.youtube.com/watch?v=%s", true, T, null, true),
            new VideoSiteConfiguration("https?://www.youtube.com/embed/(?<id>[A-Za-z0-9_-]+)", "https://www.youtube.com/watch?v=%s", false, NONE, CONVERT_PARAMETERS_LIST, true),
            new VideoSiteConfiguration("https?://youtu.be/(?<id>[A-Za-z0-9_-]+)", "https://www.youtube.com/watch?v=%s", false, T, null, true),
            new VideoSiteConfiguration("https?://www.vimeo.com/(?<id>[0-9]+)", null, false, NONE, null, true),
            new VideoSiteConfiguration("//player.vimeo.com/video/(?<id>[0-9]+)", "https://www.vimeo.com/%s", true, NONE, null, true),
            new VideoSiteConfiguration("https?://video.ft.com/(?<id>[0-9]+)/", null, false, NONE, null, true)
    );

    private static final List<String> INTERACTIVE_GRAPHICS_RULES = Arrays.asList(
            "http:\\/\\/interactive.ftdata.co.uk\\/(?!_other\\/ben\\/twitter).*",
            "http:\\/\\/(www.)?ft.com\\/ig\\/(?!widgets\\/widgetBrowser\\/audio).*",
            "http:\\/\\/ig.ft.com\\/features.*",
            "http:\\/\\/ft.cartodb.com.*"
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

    private static String randomChars(int howMany) {
        return RandomStringUtils.randomAlphanumeric(howMany).toLowerCase();
    }

    @Before
    @SuppressWarnings("unchecked")
    public void setup() throws Exception {
        DocumentStoreApiClient documentStoreApiClient = mock(DocumentStoreApiClient.class);
        ConcordanceApiClient concordanceApiClient = mock(ConcordanceApiClient.class);
        VideoMatcher videoMatcher = new VideoMatcher(VIDEO_CONFIGS);
        InteractiveGraphicsMatcher interactiveGraphicsMatcher = new InteractiveGraphicsMatcher(INTERACTIVE_GRAPHICS_RULES);
        registry = new MethodeBodyTransformationXMLEventHandlerRegistry(videoMatcher, interactiveGraphicsMatcher, CANONICAL_URL_TEMPLATE);
        Concordance concordance = new Concordance(concept, identifier);
        Concordances concordancesResponse = new Concordances(Collections.singletonList(concordance));
        Concordances concordancesEmpty = new Concordances(new ArrayList<>());

        rulesAndHandlers = new HashMap<>();
        rulesAndHandlers.put("STRIP ELEMENT AND CONTENTS", "StripElementAndContentsXMLEventHandler");
        rulesAndHandlers.put("STRIP ELEMENT AND LEAVE CONTENT", "StripXMLEventHandler");
        rulesAndHandlers.put("RETAIN ELEMENT AND REMOVE ATTRIBUTES", "RetainWithoutAttributesXMLEventHandler");
        rulesAndHandlers.put("TRANSFORM THE TAG", "SimpleTransformTagXmlEventHandler");
        rulesAndHandlers.put("CONVERT HTML ENTITY TO UNICODE", "PlainTextHtmlEntityReferenceEventHandler");
        rulesAndHandlers.put("STRIP ELEMENT AND LEAVE CONTENT BY DEFAULT", "StripXMLEventHandler");
        rulesAndHandlers.put("TRANSFORM THE WEB-PULL-QUOTE TO PULL-QUOTE", "PullQuoteEventHandler");
        rulesAndHandlers.put("TRANSFORM WEB-PULL-QUOTE W/ IMAGE TO PULL-QUOTE", "PullQuoteEventHandler");
        rulesAndHandlers.put("TRANSFORM TAG IF BIG NUMBER", "PromoBoxEventHandler");
        rulesAndHandlers.put("TRANSFORM TAG IF PROMO BOX", "PromoBoxEventHandler");
        rulesAndHandlers.put("TRANSFORM TAG IF PROMO BOX WITH MASTER IMAGE", "PromoBoxEventHandler");
        rulesAndHandlers.put("RETAIN ELEMENT AND REMOVE FORMATTING ATTRIBUTES", "DataTableXMLEventHandler");
        rulesAndHandlers.put("TRANSFORM THE SCRIPT ELEMENT TO PODCAST", "PodcastXMLEventHandler");
        rulesAndHandlers.put("TRANSFORM THE TAG TO VIDEO", "MethodeVideoXmlEventHandler");
        rulesAndHandlers.put("TRANSFORM THE NEXT TAG TO VIDEO", "MethodeVideoXmlEventHandler");
        rulesAndHandlers.put("TRANSFORM THE LINK TAG TO VIDEO", "ContentVideoXmlEventHandler");
        rulesAndHandlers.put("TRANSFORM INTERACTIVE GRAPHICS", "MethodeOtherVideoXmlEventHandler");
        rulesAndHandlers.put("TRANSFORM OTHER VIDEO TYPES", "MethodeOtherVideoXmlEventHandler");
        rulesAndHandlers.put("WRAP AND TRANSFORM A INLINE IMAGE", "WrappedHandlerXmlEventHandler");
        rulesAndHandlers.put("REPLACE BLOCK ELEMENT TAG", "SimpleTransformBlockElementEventHandler");

        when(concordanceApiClient.getConcordancesByIdentifierValues(Collections.singletonList(TME_ID_NOT_CONCORDED))).thenReturn(concordancesEmpty);
        when(concordanceApiClient.getConcordancesByIdentifierValues(Arrays.asList(TME_ID_CONCORDED, TME_ID_NOT_CONCORDED))).thenReturn(concordancesResponse);
        when(concordanceApiClient.getConcordancesByIdentifierValues(Collections.singletonList(TME_ID_CONCORDED))).thenReturn(concordancesResponse);

        List<Content> content = Collections.singletonList(new Content(CONTENT_STORE_UUID, "Article"));
        when(documentStoreApiClient.getContentForUuids(anyCollection(), anyString())).thenReturn(content);

        bodyTransformer = new BodyProcessingFieldTransformerFactory(documentStoreApiClient, videoMatcher,
                interactiveGraphicsMatcher, contentTypeTemplates, API_HOST, concordanceApiClient,
                CANONICAL_URL_TEMPLATE).newInstance();
    }

    @Given("^the Methode body contains (.+) the transformer will (.+) and the replacement tag will be (.+)$")
    public void the_methode_body_contains_transforms_into(String tagname, String rule, String replacement) throws Throwable {
        assertTagIsRegisteredToTransform(rule, tagname, replacement);
    }

    @Given("^the Methode body has (.+) the transformer will (.+)$")
    public void the_methode_body_contains(String tagname, String rule) throws Throwable {
        assertTagIsRegistered(tagname, rule);
    }

    @Given("^I have a body text in Methode XML format containing (.+)$")
    public void i_have_body_text_in_methode_xml_format_containing(String tagname) throws Throwable {
        methodeBodyText = "<" + tagname + " title=\"title\">Text</" + tagname + ">";
    }

    @Given("^I have a body (.+?)$")
    public void I_have_a_body(String html) throws Throwable {
        methodeBodyText = "<body>" + html + "</body>";
    }

    @Given("^there are empty paragraphs in the body$")
    public void there_are_empty_paragraphs() throws Throwable {
        // no op!
    }

    @When("^I transform it into our Content Store format$")
    public void i_transform_it_into_our_content_store_format() throws Throwable {
        transformedBodyText = bodyTransformer.transform(methodeBodyText, TRANSACTION_ID);
    }

    @When("^I transform it$")
    public void I_transform_it() throws Throwable {
        transformedBodyText = bodyTransformer.transform(methodeBodyText, TRANSACTION_ID);
    }

    @Then("^it is left unmodified$")
    public void it_is_left_unmodified() {
        assertThat(transformedBodyText, equalToIgnoringCase(methodeBodyText));
    }

    @Then("^the start tag (.+) should have been removed$")
    public void the_start_tag_should_have_been_removed(String tagname) throws Throwable {
        assertThat("start tag wasn't removed", transformedBodyText, not(containsString("<" + tagname + ">")));
    }

    @Then("^the end tag (.+) should have been removed$")
    public void the_end_tag_should_have_been_removed(String tagname) throws Throwable {
        assertThat("end tag wasn't removed", transformedBodyText, not(containsString("</" + tagname + ">")));
    }

    @Then("^the text inside should not have been removed$")
    public void the_text_inside_should_not_have_been_removed() throws Throwable {
        assertThat("Text was removed", transformedBodyText, containsString("Text"));
    }

    @And("^the text inside should have been removed$")
    public void the_text_inside_should_have_been_removed() throws Throwable {
        assertThat("Text was removed", transformedBodyText, not(containsString("Text")));
    }

    @Then("^the start tag (.+) should have been replaced by (.+)$")
    public void the_start_tag_tagname_should_have_been_replaced_by_replacement(String tagname, String replacement) throws Throwable {
        assertThat("start tag was removed", transformedBodyText, containsString("<" + replacement + ">"));
    }

    @And("^the end tag (.+) should have been replaced by (.+)$")
    public void the_end_tag_tagname_should_have_been_replaced_by_replacement(String tagname, String replacement) throws Throwable {
        assertThat("end tag was removed", transformedBodyText, containsString("</" + replacement + ">"));
    }

    @Then("^the start tag (.+) should be present$")
    public void the_start_tag_tagname_should_be_present(String tagname) throws Throwable {
        assertThat("start tag was removed", transformedBodyText, containsString("<" + tagname + ">"));
    }

    @And("^the end tag (.+) should be present$")
    public void the_end_tag_tagname_should_be_present(String tagname) throws Throwable {
        assertThat("end tag was removed", transformedBodyText, containsString("</" + tagname + ">"));
    }

    @Given("^I have a? \".*\" in a Methode XML body like (.*)$")
    public void I_have_body_text_in_Methode_XML_like_before(String body) throws Throwable {
        methodeBodyText = body;
    }

    @Given("^I have a rule to (.+) and an entity (.+)$")
    public void I_have_a_rule_and_an_entity(String rule, String entity) throws Throwable {
        String handler = rulesAndHandlers.get(rule);
        String entitybasic = entity.substring(1, entity.length() - 1);
        EntityReferenceEventImpl event = new EntityReferenceEventImpl(null, entitybasic);
        XMLEventHandler eventHandler = registry.getEventHandler(event);
        assertThat("The handler is incorrect", eventHandler.getClass().getSimpleName(), equalTo(handler));
    }

    @Given("^the before tag (.+) and the after tag (.+) adheres to the (.+) rule$")
    public void before_and_after_tag_name_adheres_to_rule(String name, String aftername, String rule) throws Throwable {
        assertTagIsRegisteredToTransform(rule, name, aftername);
    }

    @Then("^the body should be like (.*)$")
    public void the_body_should_be_like_after(String after) throws Throwable {
        assertThat("the body was not transformed as expected", transformedBodyText, is(after));
    }

    @Given("^an entity reference (.+)$")
    public void An_entity_reference_entity(String entity) throws Throwable {
        methodeBodyText = "<body>" + entity + "</body>";
    }

    @Then("^the entity should be replace by unicode codepoint (.+)$")
    public void the_entity_should_be_replace_by_unicode_codepoint_codepoint(String codepoint) throws Throwable {
        final int codePointInt = Integer.decode(codepoint);
        final char[] chars = Character.toChars(codePointInt);
        final String expected = "<body>" + new String(chars) + "</body>";
        assertThat(transformedBodyText, is(expected));
    }

    @Then("^it is transformed the entity (.+) should be replaced by the unicode codepoint (.+)$")
    public void the_entity_should_be_replace_by_unicode_codepoint(String entity, String codepoint) throws Throwable {
        int codePointInt = Integer.decode(codepoint);
        char[] chars = Character.toChars(codePointInt);
        String expected = "<body>" + TEXT + new String(chars) + "</body>";
        methodeBodyText = "<body>" + TEXT + entity + "</body>";
        transformedBodyText = bodyTransformer.transform(methodeBodyText, TRANSACTION_ID);
        assertThat(transformedBodyText, is(expected));
    }

    @Given("^I have an? \".*\" in a Methode article body like (.*)$")
    public void I_have_something_in_a_Methode_article_body_like(String body) throws Throwable {
        methodeBodyText = body;
    }

    @Given("^the tag (.+) adheres to the (.+)$")
    public void tag_name_adheres_to_rule(String name, String rule) throws Throwable {
        assertTagIsRegistered(name, rule);
    }

    @When("^it is transformed, (.+) becomes (.+)$")
    public void the_before_becomes_after(String before, String after) throws Throwable {
        transformedBodyText = bodyTransformer.transform(wrapped(before), TRANSACTION_ID);

        Diff diff = new Diff(wrapped(after), transformedBodyText);
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        XMLAssert.assertXMLEqual("transformed body does not match expected text", diff, true);
    }

    private String wrapped(String bodyMarkUp) {
        return String.format("<body>%s</body>", bodyMarkUp);
    }

    @Then("^the hyperlink should be like (.*)$")
    public void the_hyperlink_should_be_like_after(String after) throws Throwable {
        Diff difference = new Diff(transformedBodyText, after);
        assertThat(String.format("the hyperlink was not transformed as expected, it was: [%s]", difference.toString()), difference.identical());
    }

    private void assertTagIsRegisteredToTransform(String rule, String before, String after) {
        XMLEventHandler eventHandler = null;

        eventHandler = assertTagIsRegistered(before, rule);

        if (eventHandler instanceof TransformingEventHandler) {
            assertThat("The replacement tag is not registered properly", ((TransformingEventHandler) eventHandler).getNewElement(), equalTo(after));
        } else {
            assertThat("The transformer is not of type TransformableEvent", false);
        }

    }

    private XMLEventHandler assertTagIsRegistered(String name, String rule) {
        String handler = rulesAndHandlers.get(rule);
        StartElementEventImpl startElement = StartElementEventImpl.construct(null, new QName(name), null, null, null);
        XMLEventHandler eventHandler = registry.getEventHandler(startElement);
        assertThat("handler incorrect", handler, equalTo(eventHandler.getClass().getSimpleName()));
        return eventHandler;
    }

    @Given("^There is a company tag in a Methode article body$")
    public void There_is_a_company_tag_in_a_Methode_article_body() throws Throwable {
        // no op!
    }

}
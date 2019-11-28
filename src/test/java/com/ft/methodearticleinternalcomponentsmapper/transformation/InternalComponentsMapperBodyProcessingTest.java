package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessor;
import com.ft.bodyprocessing.html.Html5SelfClosingTagBodyProcessor;
import com.ft.common.FileUtils;
import com.ft.methodearticleinternalcomponentsmapper.clients.DocumentStoreApiClient;
import com.ft.methodearticleinternalcomponentsmapper.exception.InvalidMethodeContentException;
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

import com.google.common.collect.Maps;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InternalComponentsMapperBodyProcessingTest {
    private static final String ARTICLE_TEMPLATE = FileUtils.readFile("article/article_value.xml.mustache");
    private static final String SYSTEM_ATTRIBUTES_TEMPLATE = FileUtils.readFile("article/article_system_attributes.xml.mustache");
    private static final String ATTRIBUTES_TEMPLATE = FileUtils.readFile("article/article_attributes.xml.mustache");

    private static final String PLACEHOLDER_ARTICLE_IMAGE = "articleImage";
    private static final String PLACEHOLDER_MAINIMAGE = "mainImageUuid";
    private static final String PLACEHOLDER_IMAGE_SET_UUID = "imageSetID";

    private static final String PLACEHOLDER_IS_CONTENT_PACKAGE = "isContentPackage";
    private static final String PLACEHOLDER_CONTENT_PACKAGE = "contentPackage";
    private static final String PLACEHOLDER_CONTENT_PACKAGE_DESC = "contentPackageDesc";
    private static final String PLACEHOLDER_CONTENT_PACKAGE_LIST_HREF = "contentPackageListHref";

    private static final String PLACEHOLDER_SUMMARY = "summary";
    private static final String PLACEHOLDER_SUMMARY_DISPLAY_POSITION = "displayPosition";
    private static final String PLACEHOLDER_SUMMARY_DISPLAY_POSITION_AUTO = "auto";
    private static final String PLACEHOLDER_SOURCE_CODE = "sourceCode";

    private static final String EOM_COMPOUND_STORY = "EOM::CompoundStory";
    private static final String EOM_STORY = "EOM::Story";

    private static final String IMAGE_SET_UUID = "U116035516646705FC";

    private static final String TRANSFORMED_BODY = "<body><p>some other random text</p></body>";
    private static final String EMPTY_BODY = "<body></body>";

    private static final String API_HOST = "test.api.ft.com";
    private static final String TRANSACTION_ID = "tid_test";
    private static final Date LAST_MODIFIED = new Date();
    private static final UUID uuid = UUID.randomUUID();

    private EomFile standardEomFile;
    private Map<String, Object> valuePlaceholdersValues;
    private Map<String, Object> systemAttributesPlaceholdersValues;
    private Map<String, Object> attributesPlaceholdersValues;

    private InternalComponents standardExpectedContent;

    private FieldTransformer bodyTransformer;
    private DocumentStoreApiClient documentStoreApiClient;
    private InternalComponentsMapper eomFileProcessor;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        bodyTransformer = mock(FieldTransformer.class);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(TRANSFORMED_BODY);

        BodyProcessor htmlFieldProcessor = spy(new Html5SelfClosingTagBodyProcessor());

        valuePlaceholdersValues = new HashMap<>();

        systemAttributesPlaceholdersValues = new HashMap<>();
        systemAttributesPlaceholdersValues.put("channel", "FTcom");

        attributesPlaceholdersValues = new HashMap<>();
        attributesPlaceholdersValues.put(PLACEHOLDER_SOURCE_CODE, InternalComponentsMapper.SourceCode.FT);
        attributesPlaceholdersValues.put(PLACEHOLDER_IS_CONTENT_PACKAGE, "false");

        standardEomFile = createStandardEomFile(uuid, EOM_COMPOUND_STORY, valuePlaceholdersValues, systemAttributesPlaceholdersValues, attributesPlaceholdersValues);
        standardExpectedContent = createStandardExpectedContent();

        BlogUuidResolver blogUuidResolver = mock(BlogUuidResolver.class);

        MethodeArticleValidator methodeArticleValidator = mock(MethodeArticleValidator.class);
        MethodeArticleValidator methodeContentPlaceholderValidator = mock(MethodeArticleValidator.class);
        when(methodeArticleValidator.getPublishingStatus(any(), any(), anyBoolean())).thenReturn(PublishingStatus.VALID);
        when(methodeContentPlaceholderValidator.getPublishingStatus(any(), any(), anyBoolean())).thenReturn(PublishingStatus.VALID);

        Map<String, MethodeArticleValidator> articleValidators = new HashMap<>();
        articleValidators.put(InternalComponentsMapper.SourceCode.FT, methodeArticleValidator);
        articleValidators.put(InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER, methodeContentPlaceholderValidator);

        documentStoreApiClient = mock(DocumentStoreApiClient.class);

        eomFileProcessor = new InternalComponentsMapper(bodyTransformer, htmlFieldProcessor, blogUuidResolver, documentStoreApiClient, articleValidators, API_HOST);
    }

    @Test
    public void shouldTransformArticleBodyOnPublish() {
        final EomFile eomFile = createStandardEomFile(uuid, EOM_COMPOUND_STORY, valuePlaceholdersValues, systemAttributesPlaceholdersValues, attributesPlaceholdersValues);

        final InternalComponents expectedContent = InternalComponents.builder()
                .withValuesFrom(standardExpectedContent)
                .withXMLBody(TRANSFORMED_BODY).build();

        InternalComponents content = eomFileProcessor.map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);

        verify(bodyTransformer).transform(anyString(), eq(TRANSACTION_ID), eq(Maps.immutableEntry("uuid", eomFile.getUuid())), eq(Maps.immutableEntry("apiHost", API_HOST)));
        assertThat(content.getBodyXML(), equalTo(expectedContent.getBodyXML()));
    }

    @Test
    public void shouldTransformSummaryBodyToo() {
        valuePlaceholdersValues.put(PLACEHOLDER_SUMMARY, true);
        valuePlaceholdersValues.put(PLACEHOLDER_SUMMARY_DISPLAY_POSITION, PLACEHOLDER_SUMMARY_DISPLAY_POSITION_AUTO);
        final EomFile eomFile = createStandardEomFile(uuid, EOM_COMPOUND_STORY, valuePlaceholdersValues, systemAttributesPlaceholdersValues, attributesPlaceholdersValues);

        final InternalComponents expectedContent = InternalComponents.builder()
                .withValuesFrom(standardExpectedContent)
                .withXMLBody(TRANSFORMED_BODY)
                .withSummary(Summary.builder().withBodyXML(TRANSFORMED_BODY).build())
                .build();

        InternalComponents content = eomFileProcessor.map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);

        verify(bodyTransformer).transform(anyString(), eq(TRANSACTION_ID), eq(Maps.immutableEntry("uuid", eomFile.getUuid())), eq(Maps.immutableEntry("apiHost", API_HOST)));
        verify(bodyTransformer).transform(anyString(), eq(TRANSACTION_ID), eq(Maps.immutableEntry("uuid", eomFile.getUuid())));
        assertThat(content.getBodyXML(), equalTo(expectedContent.getBodyXML()));
        assertThat(content.getSummary().getBodyXML(), equalTo(expectedContent.getSummary().getBodyXML()));
    }

    @Test
    public void shouldTransformSummaryBodyForContentPlaceholder() {
        valuePlaceholdersValues.put(PLACEHOLDER_SUMMARY, true);
        valuePlaceholdersValues.put(PLACEHOLDER_SUMMARY_DISPLAY_POSITION, PLACEHOLDER_SUMMARY_DISPLAY_POSITION_AUTO);
        attributesPlaceholdersValues.put(PLACEHOLDER_SOURCE_CODE, InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER);
        final EomFile eomFile = createStandardEomFile(uuid, EOM_COMPOUND_STORY, valuePlaceholdersValues, systemAttributesPlaceholdersValues, attributesPlaceholdersValues);

        String blogUuid = UUID.randomUUID().toString();
        BlogUuidResolver blogUuidResolver = mock(BlogUuidResolver.class);
        when(blogUuidResolver.resolveUuid("http://ftalphaville.ft.com/?p=2193913", "2193913", TRANSACTION_ID)).thenReturn(blogUuid);
        MethodeArticleValidator methodeArticleValidator = mock(MethodeArticleValidator.class);
        MethodeArticleValidator methodeContentPlaceholderValidator = mock(MethodeArticleValidator.class);
        when(methodeArticleValidator.getPublishingStatus(any(), any(), anyBoolean())).thenReturn(PublishingStatus.VALID);
        when(methodeContentPlaceholderValidator.getPublishingStatus(any(), any(), anyBoolean())).thenReturn(PublishingStatus.VALID);
        Map<String, MethodeArticleValidator> articleValidators = new HashMap<>();
        articleValidators.put(InternalComponentsMapper.SourceCode.FT, methodeArticleValidator);
        articleValidators.put(InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER, methodeContentPlaceholderValidator);
        BodyProcessor htmlFieldProcessor = spy(new Html5SelfClosingTagBodyProcessor());

        InternalComponentsMapper eomFileProcessor = new InternalComponentsMapper(bodyTransformer, htmlFieldProcessor, blogUuidResolver, documentStoreApiClient, articleValidators, API_HOST);

        final InternalComponents expectedContent = InternalComponents.builder()
                .withValuesFrom(standardExpectedContent)
                .withUuid(blogUuid)
                .withSummary(Summary.builder().withBodyXML(TRANSFORMED_BODY).build())
                .withXMLBody(null)
                .build();

        InternalComponents content = eomFileProcessor.map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);

        verify(bodyTransformer, times(1)).transform(anyString(), eq(TRANSACTION_ID), eq(Maps.immutableEntry("uuid", eomFile.getUuid())));
        assertThat(content.getSummary().getBodyXML(), equalTo(expectedContent.getSummary().getBodyXML()));
    }

    @Test
    public void shouldAllowBodyWithAttributes() {
        final EomFile eomFile = new EomFile.Builder()
                .withValuesFrom(standardEomFile)
                .build();

        String expectedBody = "<body id=\"some-random-value\"><foo/></body>";
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(expectedBody);

        final InternalComponents expectedContent = InternalComponents.builder()
                .withValuesFrom(standardExpectedContent)
                .withXMLBody(expectedBody).build();

        InternalComponents content = eomFileProcessor.map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);

        verify(bodyTransformer).transform(anyString(), eq(TRANSACTION_ID), eq(Maps.immutableEntry("uuid", eomFile.getUuid())), eq(Maps.immutableEntry("apiHost", API_HOST)));
        assertThat(content.getBodyXML(), equalTo(expectedContent.getBodyXML()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfBodyTagIsMissingFromTransformedBody() {
        final EomFile eomFile = createStandardEomFile(uuid, EOM_STORY, valuePlaceholdersValues, systemAttributesPlaceholdersValues, attributesPlaceholdersValues);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn("<p>some other random text</p>");
        eomFileProcessor.map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);
    }

    @Test(expected = InvalidMethodeContentException.class)
    public void shouldThrowExceptionIfBodyIsNull() {
        final EomFile eomFile = createStandardEomFile(uuid, EOM_STORY, valuePlaceholdersValues, systemAttributesPlaceholdersValues, attributesPlaceholdersValues);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(null);
        eomFileProcessor.map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);
    }

    @Test(expected = InvalidMethodeContentException.class)
    public void shouldThrowExceptionIfBodyIsEmpty() {
        final EomFile eomFile = createStandardEomFile(uuid, EOM_STORY, valuePlaceholdersValues, systemAttributesPlaceholdersValues, attributesPlaceholdersValues);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn("");
        eomFileProcessor.map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);
    }

    @Test(expected = InvalidMethodeContentException.class)
    public void shouldThrowExceptionIfTransformedBodyIsBlank() {
        final EomFile eomFile = createStandardEomFile(uuid, EOM_STORY, valuePlaceholdersValues, systemAttributesPlaceholdersValues, attributesPlaceholdersValues);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn("<body> \n \n \n </body>");
        eomFileProcessor.map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);
    }

    @Test(expected = InvalidMethodeContentException.class)
    public void shouldThrowExceptionIfTransformedBodyIsEmpty() {
        final EomFile eomFile = createStandardEomFile(uuid, EOM_STORY, valuePlaceholdersValues, systemAttributesPlaceholdersValues, attributesPlaceholdersValues);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(EMPTY_BODY);
        eomFileProcessor.map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);
    }

    @Test
    public void thatPreviewEmptyTransformedBodyIsAllowed() {
        final EomFile eomFile = createStandardEomFile(uuid, EOM_STORY, valuePlaceholdersValues, systemAttributesPlaceholdersValues, attributesPlaceholdersValues);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(EMPTY_BODY);
        InternalComponents actual = eomFileProcessor.map(eomFile, TRANSACTION_ID, new Date(), true);
        assertThat(actual.getBodyXML(), is(equalTo(EMPTY_BODY)));
    }

    @Test
    public void thatContentPackageNullBodyIsAllowed() {
        final EomFile eomFile = createEomFileWithRandomContentPackage();

        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(null);
        InternalComponents actual = eomFileProcessor.map(eomFile, TRANSACTION_ID, new Date(), false);
        assertThat(actual.getBodyXML(), is(equalTo(EMPTY_BODY)));
    }

    @Test
    public void thatContentPackageEmptyBodyIsAllowed() {
        final EomFile eomFile = createEomFileWithRandomContentPackage();

        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn("");
        InternalComponents actual = eomFileProcessor.map(eomFile, TRANSACTION_ID, new Date(), false);
        assertThat(actual.getBodyXML(), is(equalTo(EMPTY_BODY)));
    }

    @Test
    public void thatContentPackageBlankTransformedBodyIsAllowed() {
        final EomFile eomFile = createEomFileWithRandomContentPackage();

        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn("<body> \n \n \n </body>");
        InternalComponents actual = eomFileProcessor.map(eomFile, TRANSACTION_ID, new Date(), false);
        assertThat(actual.getBodyXML(), is(equalTo(EMPTY_BODY)));
    }

    @Test
    public void shouldAddPublishReferenceToTransformedBody() {
        final String reference = "some unstructured reference";

        final EomFile eomFile = new EomFile.Builder()
                .withValuesFrom(standardEomFile)
                .build();

        final InternalComponents expectedContent = InternalComponents.builder()
                .withValuesFrom(standardExpectedContent)
                .withPublishReference(reference)
                .withXMLBody(TRANSFORMED_BODY).build();

        InternalComponents content = eomFileProcessor.map(eomFile, reference, LAST_MODIFIED, false);

        assertThat(content.getBodyXML(), equalTo(expectedContent.getBodyXML()));
    }

    @Test
    public void testMainImageReferenceIsPutInBodyWhenPresentAndPrimarySizeFlag() throws Exception {
        String expectedTransformedBody = "<body><ft-content data-embedded=\"true\" type=\"http://www.ft.com/ontology/content/ImageSet\" url=\"http://test.api.ft.com/content/%s\"></ft-content>" +
                "                <p>random text for now</p>" +
                "            </body>";
        testMainImageReferenceIsPutInBodyWithMetadataFlag("Primary size",
                expectedTransformedBody);
    }

    @Test
    public void testMainImageReferenceIsPutInBodyWhenPresentAndArticleSizeFlag() throws Exception {
        String expectedTransformedBody = "<body><ft-content data-embedded=\"true\" type=\"http://www.ft.com/ontology/content/ImageSet\" url=\"http://test.api.ft.com/content/%s\"></ft-content>" +
                "                <p>random text for now</p>" +
                "            </body>";
        testMainImageReferenceIsPutInBodyWithMetadataFlag("Article size",
                expectedTransformedBody);
    }

    @Test
    public void testMainImageReferenceIsNotPutInBodyWhenPresentButNoPictureFlag() throws Exception {
        String expectedTransformedBody = "<body>" +
                "                <p>random text for now</p>" +
                "            </body>";
        testMainImageReferenceIsPutInBodyWithMetadataFlag("No picture", expectedTransformedBody);
    }

    @Test
    public void testMainImageReferenceIsNotPutInBodyWhenMissing() throws Exception {
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).then(returnsFirstArg());
        final EomFile eomFile = createStandardEomFile(uuid, EOM_COMPOUND_STORY, valuePlaceholdersValues, systemAttributesPlaceholdersValues, attributesPlaceholdersValues);

        InternalComponents content = eomFileProcessor.map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);

        String expectedBody = "<body>" +
                "                <p>random text for now</p>" +
                "            </body>";
        assertThat(content.getBodyXML(), equalToIgnoringWhiteSpace(expectedBody));
    }

    @Test(expected = InvalidMethodeContentException.class)
    public void thatTransformationFailsIfThereIsNoBody() throws Exception {
        String value = FileUtils.readFile("article/article_value_with_no_body.xml");
        final EomFile eomFile = new EomFile.Builder()
                .withValuesFrom(standardEomFile)
                .withValue(value.getBytes(UTF_8))
                .build();

        eomFileProcessor.map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);
    }

    @Test
    public void testImageSet() {
        String expectedUUID = UUID.nameUUIDFromBytes(IMAGE_SET_UUID.getBytes(UTF_8)).toString();
        String expectedBody = "<body>"
                + "<p>random text for now</p>"
                + "<ft-content type=\"http://www.ft.com/ontology/content/ImageSet\" url=\"http://test.api.ft.com/content/" + expectedUUID + "\" data-embedded=\"true\"></ft-content>"
                + "</body>";
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(expectedBody);

        attributesPlaceholdersValues.put(PLACEHOLDER_IMAGE_SET_UUID, IMAGE_SET_UUID);
        EomFile eomFile = createStandardEomFile(uuid, EOM_COMPOUND_STORY, valuePlaceholdersValues, systemAttributesPlaceholdersValues, attributesPlaceholdersValues);
        InternalComponents content = eomFileProcessor.map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);

        assertThat(content.getBodyXML(), equalToIgnoringWhiteSpace(expectedBody));
    }

    @Test
    public void testImageSetPreview() {
        String expectedUUID = UUID.nameUUIDFromBytes(IMAGE_SET_UUID.getBytes(UTF_8)).toString();
        String expectedBody = "<body>"
                + "<p>random text for now</p>"
                + "<ft-content type=\"http://www.ft.com/ontology/content/ImageSet\" url=\"http://test.api.ft.com/content/" + expectedUUID + "\" data-embedded=\"true\"></ft-content>"
                + "</body>";
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(expectedBody);

        attributesPlaceholdersValues.put(PLACEHOLDER_IMAGE_SET_UUID, IMAGE_SET_UUID);
        EomFile eomFile = createStandardEomFile(uuid, EOM_COMPOUND_STORY, valuePlaceholdersValues, systemAttributesPlaceholdersValues, attributesPlaceholdersValues);
        InternalComponents content = eomFileProcessor.map(eomFile, TRANSACTION_ID, new Date(), true);

        assertThat(content.getBodyXML(), equalToIgnoringWhiteSpace(expectedBody));
    }

    private void testMainImageReferenceIsPutInBodyWithMetadataFlag(String articleImagePlaceholderValue, String expectedTransformedBody) {
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).then(returnsFirstArg());
        final UUID imageUuid = UUID.randomUUID();
        final UUID expectedMainImageUuid = DeriveUUID.with(DeriveUUID.Salts.IMAGE_SET).from(imageUuid);

        valuePlaceholdersValues.put(PLACEHOLDER_MAINIMAGE, imageUuid);
        attributesPlaceholdersValues.put(PLACEHOLDER_ARTICLE_IMAGE, articleImagePlaceholderValue);

        final EomFile eomFile = createStandardEomFile(uuid, EOM_COMPOUND_STORY, valuePlaceholdersValues, systemAttributesPlaceholdersValues, attributesPlaceholdersValues);
        InternalComponents content = eomFileProcessor.map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);

        String expectedBody = String.format(expectedTransformedBody, expectedMainImageUuid);
        assertThat(content.getBodyXML(), equalToIgnoringWhiteSpace(expectedBody));
    }

    private EomFile createEomFileWithRandomContentPackage() {
        Map<String, Object> valuePlaceholdersValues = new HashMap<>();
        valuePlaceholdersValues.put(PLACEHOLDER_CONTENT_PACKAGE, true);
        valuePlaceholdersValues.put(PLACEHOLDER_CONTENT_PACKAGE_DESC, "cp");
        valuePlaceholdersValues.put(PLACEHOLDER_CONTENT_PACKAGE_LIST_HREF, "<a href=\"/FT/Content/Content%20Package/Live/content-package-test.dwc?uuid=" + UUID.randomUUID().toString() + "\"/>");
        Map<String, Object> attributesPlaceholdersValues = new HashMap<>();
        attributesPlaceholdersValues.put(PLACEHOLDER_SOURCE_CODE, InternalComponentsMapper.SourceCode.FT);
        attributesPlaceholdersValues.put(PLACEHOLDER_IS_CONTENT_PACKAGE, "true");

        return createStandardEomFile(uuid, EOM_COMPOUND_STORY, valuePlaceholdersValues, systemAttributesPlaceholdersValues, attributesPlaceholdersValues);
    }

    private EomFile createStandardEomFile(UUID uuid, String eomType, Map<String, Object> valuePlaceholdersValues,
                                          Map<String, Object> systemAttributesPlaceholdersValues,
                                          Map<String, Object> attributesPlaceholdersValues) {
        return new EomFile.Builder()
                .withUuid(uuid.toString())
                .withType(eomType)
                .withValue(buildEomFileValue(valuePlaceholdersValues))
                .withSystemAttributes(buildEomFileSystemAttributes(systemAttributesPlaceholdersValues))
                .withAttributes(buildEomFileAttributes(attributesPlaceholdersValues))
                .withWorkflowStatus("Stories/WebReady")
                .withWebUrl(null)
                .build();
    }

    private static byte[] buildEomFileValue(Map<String, Object> valuePlaceholdersValues) {
        Template mustache = Mustache.compiler().escapeHTML(false).compile(ARTICLE_TEMPLATE);
        return mustache.execute(valuePlaceholdersValues).getBytes(UTF_8);
    }

    private static String buildEomFileSystemAttributes(Map<String, Object> systemAttributesPlaceholdersValues) {
        Template mustache = Mustache.compiler().escapeHTML(false).compile(SYSTEM_ATTRIBUTES_TEMPLATE);
        return mustache.execute(systemAttributesPlaceholdersValues);
    }

    private static String buildEomFileAttributes(Map<String, Object> attributesPlaceholdersValues) {
        Template mustache = Mustache.compiler().escapeHTML(false).compile(ATTRIBUTES_TEMPLATE);
        return mustache.execute(attributesPlaceholdersValues);
    }

    private InternalComponents createStandardExpectedContent() {
        return InternalComponents.builder()
                .withDesign(new Design("basic", "default"))
                .withTableOfContents(new TableOfContents("sequence", "labelType"))
                .withTopper(new Topper("headline", "standfirst", "bgColor", "layout"))
                .withLeadImages(Arrays.asList(new Image("img1", "type1"), new Image("img2", "type1"), new Image("img3", "type2")))
                .withUnpublishedContentDescription("the next awesome article")
                .withXMLBody(TRANSFORMED_BODY)
                .withUuid(uuid.toString())
                .withPublishReference(TRANSACTION_ID)
                .withLastModified(LAST_MODIFIED)
                .build();
    }
}

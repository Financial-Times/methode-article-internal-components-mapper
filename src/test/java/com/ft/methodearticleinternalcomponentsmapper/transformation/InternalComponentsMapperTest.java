package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.html.Html5SelfClosingTagBodyProcessor;
import com.ft.common.FileUtils;
import com.ft.methodearticleinternalcomponentsmapper.clients.DocumentStoreApiClient;
import com.ft.methodearticleinternalcomponentsmapper.exception.DocumentStoreApiException;
import com.ft.methodearticleinternalcomponentsmapper.exception.InvalidMethodeContentException;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleNotEligibleForPublishException;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleInternalComponentsMapperException;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeMarkedDeletedException;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeMissingFieldException;
import com.ft.methodearticleinternalcomponentsmapper.exception.TransformationException;
import com.ft.methodearticleinternalcomponentsmapper.exception.UuidResolverException;
import com.ft.methodearticleinternalcomponentsmapper.model.Design;
import com.ft.methodearticleinternalcomponentsmapper.model.EomFile;
import com.ft.methodearticleinternalcomponentsmapper.model.Image;
import com.ft.methodearticleinternalcomponentsmapper.model.InternalComponents;
import com.ft.methodearticleinternalcomponentsmapper.validation.MethodeArticleValidator;
import com.ft.methodearticleinternalcomponentsmapper.validation.PublishingStatus;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InternalComponentsMapperTest {

    private static final String ATTRIBUTES_TEMPLATE = FileUtils.readFile("article/article_attributes.xml.mustache");
    private static final String ARTICLE_WITH_ALL_COMPONENTS = FileUtils.readFile("article/article_with_all_components.xml.mustache");

    private static final String TRANSFORMED_BODY = "<body><p>some other random text</p></body>";
    private static final String BLOCKS_VALUE_IS_SET = "<body>x-value</body>";
    private static final String BLOCKS_VALUE_IS_EMPTY = "<body></body>";
    private static final String BLOCKS_KEY_IS_EMPTY = "";
    private static final String BLOCKS_KEY_IS_SET = "x";
    private static final String EOM_TYPE_COMPOUND_STORY = "EOM::CompoundStory";

    private static final String ATTRIBUTE_PUSH_NOTIFICATIONS_COHORT_UK = "UK_breaking_news";
    private static final String EXPECTED_PUSH_NOTIFICATIONS_COHORT_UK = "uk-breaking-news";
    private static final String ATTRIBUTE_PUSH_NOTIFICATIONS_COHORT_GLOBAL = "Global_breaking_news";
    private static final String EXPECTED_PUSH_NOTIFICATIONS_COHORT_GLOBAL = "global-breaking-news";
    private static final String ATTRIBUTE_PUSH_NOTIFICATIONS_COHORT_NONE = "None";
    private static final String VALUE_PUSH_NOTIFICATIONS_TEXT = "My push notification text";
    private static final String VALUE_PUSH_NOTIFICATIONS_IS_NULL = "<?EM-dummyText [Push notification text]?>";

    private static final String PLACEHOLDER_PUSH_NOTIFICATIONS_COHORT = "pushNotificationsCohort";
    private static final String PLACEHOLDER_PUSH_NOTIFICATIONS_TEXT = "push-notification-text";

    private static final String API_HOST = "test.api.ft.com";
    private static final String ARTICLE_UUID = UUID.randomUUID().toString();
    private static final String BLOG_UUID = UUID.randomUUID().toString();
    private static final String TX_ID = "tid_test";
    private static final Date LAST_MODIFIED = new Date();

    private EomFile eomFile;
    private Map<String, Object> valuePlaceholdersValues;
    private Map<String, Object> attributesPlaceholdersValues;

    private FieldTransformer bodyTransformer;
    private BlogUuidResolver blogUuidResolver;
    private DocumentStoreApiClient documentStoreApiClient;

    private MethodeArticleValidator methodeArticleValidator;

    private InternalComponentsMapper internalComponentsMapper;

    private static byte[] buildEomFileValue(Map<String, Object> valuePlaceholdersValues) {
        Template mustache = Mustache.compiler().escapeHTML(false).compile(ARTICLE_WITH_ALL_COMPONENTS);
        return mustache.execute(valuePlaceholdersValues).getBytes(UTF_8);
    }

    private static String buildEomFileAttributes(Map<String, Object> attributesPlaceholdersValues) {
        Template mustache = Mustache.compiler().escapeHTML(false).compile(ATTRIBUTES_TEMPLATE);
        return mustache.execute(attributesPlaceholdersValues);
    }

    @Before
    public void setUp() {
        eomFile = mock(EomFile.class);

        valuePlaceholdersValues = new HashMap<>();

        attributesPlaceholdersValues = new HashMap<>();
        attributesPlaceholdersValues.put("isContentPackage", "false");
        attributesPlaceholdersValues.put("sourceCode", InternalComponentsMapper.SourceCode.FT);

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);

        bodyTransformer = mock(FieldTransformer.class);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(TRANSFORMED_BODY);

        blogUuidResolver = mock(BlogUuidResolver.class);
        when(blogUuidResolver.resolveUuid("http://ftalphaville.ft.com/?p=2193913", "2193913", TX_ID)).thenReturn(BLOG_UUID);

        Html5SelfClosingTagBodyProcessor htmlFieldProcessor = spy(new Html5SelfClosingTagBodyProcessor());

        methodeArticleValidator = mock(MethodeArticleValidator.class);
        MethodeArticleValidator methodeContentPlaceholderValidator = mock(MethodeArticleValidator.class);
        when(methodeArticleValidator.getPublishingStatus(any(), any(), anyBoolean())).thenReturn(PublishingStatus.VALID);
        when(methodeContentPlaceholderValidator.getPublishingStatus(any(), any(), anyBoolean())).thenReturn(PublishingStatus.VALID);

        Map<String, MethodeArticleValidator> articleValidators = new HashMap<>();
        articleValidators.put(InternalComponentsMapper.SourceCode.FT, methodeArticleValidator);
        articleValidators.put(InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER, methodeContentPlaceholderValidator);
        articleValidators.put(InternalComponentsMapper.SourceCode.DYNAMIC_CONTENT, methodeArticleValidator);

        documentStoreApiClient = mock(DocumentStoreApiClient.class);

        internalComponentsMapper = new InternalComponentsMapper(bodyTransformer, htmlFieldProcessor, blogUuidResolver, documentStoreApiClient, articleValidators, API_HOST);
    }

    @Test
    public void thatContentPlaceholderWithOriginalUUIDIsResolved() {
        attributesPlaceholdersValues.put("originalUUID", BLOG_UUID);
        attributesPlaceholdersValues.put("sourceCode", InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER);

        when(documentStoreApiClient.isUUIDPresent(BLOG_UUID, TX_ID)).thenReturn(true);

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);
        InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        assertThat(actual.getUuid(), equalTo(BLOG_UUID));
        assertThat(actual.getLastModified(), equalTo(LAST_MODIFIED));
        assertThat(actual.getPublishReference(), equalTo(TX_ID));
        verify(documentStoreApiClient).isUUIDPresent(BLOG_UUID, TX_ID);
    }

    @Test(expected = TransformationException.class)
    public void thatContentPlaceholderWithInvalidOriginalUUIDThrowsException() {
        attributesPlaceholdersValues.put("originalUUID", "invalidUUID");
        attributesPlaceholdersValues.put("sourceCode", InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER);

        when(documentStoreApiClient.isUUIDPresent(BLOG_UUID, TX_ID)).thenReturn(true);

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);
        internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
    }

    @Test(expected = TransformationException.class)
    public void thatContentPlaceholderWithOriginalUUIDMissingFromDocumentStoreThrowsException() {
        attributesPlaceholdersValues.put("originalUUID", BLOG_UUID);
        attributesPlaceholdersValues.put("sourceCode", InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER);

        when(documentStoreApiClient.isUUIDPresent(BLOG_UUID, TX_ID)).thenReturn(false);

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);
        internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
    }

    @Test(expected = TransformationException.class)
    public void thatContentPlaceholderWithOriginalUUIDThrowsExceptionWhenDocumentStoreApiThrowsException() {
        attributesPlaceholdersValues.put("originalUUID", BLOG_UUID);
        attributesPlaceholdersValues.put("sourceCode", InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER);

        when(documentStoreApiClient.isUUIDPresent(BLOG_UUID, TX_ID)).thenThrow(new DocumentStoreApiException("Failed to call document store"));

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);
        internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
    }

    @Test
    public void thatContentPlaceholderUuidWithMissingCategoryDoesntGetResolved() {
        String serviceId = "http://ftalphaville.ft.com/?p=2193913";
        String ref_field = "2193913";

        attributesPlaceholdersValues.put("serviceid", serviceId);
        attributesPlaceholdersValues.put("ref_field", ref_field);
        attributesPlaceholdersValues.put("sourceCode", InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER);

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);
        InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        assertThat(actual.getUuid(), equalTo(ARTICLE_UUID));
        assertThat(actual.getLastModified(), equalTo(LAST_MODIFIED));
        assertThat(actual.getPublishReference(), equalTo(TX_ID));
        verify(blogUuidResolver, times(0)).resolveUuid(anyString(), anyString(), anyString());
    }

    @Test
    public void thatContentPlaceholderUuidWithNonBlogCategoryDoesntGetResolved() {
        String serviceId = "http://ftalphaville.ft.com/?p=2193913";
        String ref_field = "2193913";
        String category = "notblog";

        attributesPlaceholdersValues.put("serviceid", serviceId);
        attributesPlaceholdersValues.put("ref_field", ref_field);
        attributesPlaceholdersValues.put("category", category);
        attributesPlaceholdersValues.put("sourceCode", InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER);

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);
        InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        assertThat(actual.getUuid(), equalTo(ARTICLE_UUID));
        assertThat(actual.getLastModified(), equalTo(LAST_MODIFIED));
        assertThat(actual.getPublishReference(), equalTo(TX_ID));
        verify(blogUuidResolver, times(0)).resolveUuid(anyString(), anyString(), anyString());
    }


    @Test(expected = MethodeArticleInternalComponentsMapperException.class)
    public void testInternalContentIsNotUpdatedWhenOverrideOriginalIsNotTrue() {

        Map<String, Object> attributesTemplateValues = new HashMap<>();
        attributesTemplateValues.put("sourceCode", InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER);
        attributesTemplateValues.put("overrideOriginal", "false");

        Map<String, Object> templateValues = new HashMap<>();

        final EomFile eomFile = createEomFile(templateValues, attributesTemplateValues);
        internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
    }

    @Test
    public void testInternalContentIsUpdatedWhenOverrideOriginalIsTrue() {

        Map<String, Object> attributesTemplateValues = new HashMap<>();
        attributesTemplateValues.put("sourceCode", InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER);
        attributesTemplateValues.put("overrideOriginal", "true");

        Map<String, Object> templateValues = new HashMap<>();

        final EomFile eomFile = createEomFile(templateValues, attributesTemplateValues);

        InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        assertThat(actual.getUuid(), equalTo(ARTICLE_UUID));
    }

    @Test
    public void thatBlogContentPlaceholderUuidIsResolved() {
        contentPlaceholderWithBlogCategory("blog");
    }

    @Test
    public void thatWebchatLiveBlogsContentPlaceholderUuidIsResolved() {
        contentPlaceholderWithBlogCategory("webchat-live-blogs");
    }

    @Test
    public void thatWebchatLiveQaBlogsContentPlaceholderUuidIsResolved() {
        contentPlaceholderWithBlogCategory("webchat-live-qa");
    }

    @Test
    public void thatWebchatMarketsLiveQaBlogsContentPlaceholderUuidIsResolved() {
        contentPlaceholderWithBlogCategory("webchat-markets-live");
    }

    @Test
    public void thatFastftBlogsContentPlaceholderUuidIsResolved() {
        contentPlaceholderWithBlogCategory("fastft");
    }

    private void contentPlaceholderWithBlogCategory(String blogCategory) {
        String serviceId = "http://ftalphaville.ft.com/?p=2193913";
        String ref_field = "2193913";

        attributesPlaceholdersValues.put("sourceCode", InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER);
        attributesPlaceholdersValues.put("serviceid", serviceId);
        attributesPlaceholdersValues.put("ref_field", ref_field);
        attributesPlaceholdersValues.put("category", blogCategory);

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);
        InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        assertThat(actual.getUuid(), equalTo(BLOG_UUID));
        assertThat(actual.getLastModified(), equalTo(LAST_MODIFIED));
        assertThat(actual.getPublishReference(), equalTo(TX_ID));
        verify(blogUuidResolver, times(1)).resolveUuid(anyString(), anyString(), anyString());
    }

    @Test(expected = UuidResolverException.class)
    public void thatExceptionIsThrownWhenContentPlaceholderUuidCantBeResolved() {
        String serviceId = "http://ftalphaville.ft.com/?p=2193913";
        String ref_field = "2193913";
        String category = "blog";

        attributesPlaceholdersValues.put("sourceCode", InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER);
        attributesPlaceholdersValues.put("serviceid", serviceId);
        attributesPlaceholdersValues.put("ref_field", ref_field);
        attributesPlaceholdersValues.put("category", category);

        when(blogUuidResolver.resolveUuid(anyString(), anyString(), anyString())).thenThrow(new UuidResolverException("Can't resolve uuid"));

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);
        internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
    }

    @Test(expected = MethodeMissingFieldException.class)
    public void thatExceptionIsThrownForContentPlaceholderWhenRefFieldIsEmpty() {
        String serviceId = "http://ftalphaville.ft.com/?p=2193913";
        String ref_field = "";
        String category = "blog";

        attributesPlaceholdersValues.put("sourceCode", InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER);
        attributesPlaceholdersValues.put("serviceid", serviceId);
        attributesPlaceholdersValues.put("ref_field", ref_field);
        attributesPlaceholdersValues.put("category", category);

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);
        internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
    }

    @Test(expected = MethodeMissingFieldException.class)
    public void thatExceptionIsThrownForContentPlaceholderWhenRefFieldIsMissing() {
        String serviceId = "http://ftalphaville.ft.com/?p=2193913";
        String category = "blog";

        attributesPlaceholdersValues.put("sourceCode", InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER);
        attributesPlaceholdersValues.put("serviceid", serviceId);
        attributesPlaceholdersValues.put("category", category);

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);
        internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
    }

    @Test(expected = MethodeMissingFieldException.class)
    public void thatExceptionIsThrownForContentPlaceholderWhenServiceIdIsEmpty() {
        String serviceId = "";
        String ref_field = "2193913";
        String category = "blog";

        attributesPlaceholdersValues.put("sourceCode", InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER);
        attributesPlaceholdersValues.put("serviceid", serviceId);
        attributesPlaceholdersValues.put("ref_field", ref_field);
        attributesPlaceholdersValues.put("category", category);

        when(blogUuidResolver.resolveUuid(anyString(), anyString(), anyString())).thenThrow(new UuidResolverException("Can't resolve uuid"));

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);
        internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
    }

    @Test(expected = MethodeMissingFieldException.class)
    public void thatExceptionIsThrownForContentPlaceholderWhenServiceIdIsMissing() {
        String ref_field = "2193913";
        String category = "blog";

        attributesPlaceholdersValues.put("sourceCode", InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER);
        attributesPlaceholdersValues.put("ref_field", ref_field);
        attributesPlaceholdersValues.put("category", category);

        when(blogUuidResolver.resolveUuid(anyString(), anyString(), anyString())).thenThrow(new UuidResolverException("Can't resolve uuid"));

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);
        internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
    }

    @Test
    public void thatMapsContentPlaceholders() {
        attributesPlaceholdersValues.put("sourceCode", InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER);
        final EomFile eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);

        InternalComponents content = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
        assertNull(content.getBodyXML());
    }

    @Test
    public void testMapsDynamicContent() {
        Map<String, Object> attributesTemplateValues = new HashMap<>();
        attributesTemplateValues.put("sourceCode", "DynamicContent");
        attributesTemplateValues.put("workflowStatus", "Stories/WebReady");

        Map<String, Object> valueTemplateValues = new HashMap<>();
        eomFile = createDynamicContent(valueTemplateValues, attributesTemplateValues);

        internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, true);
    }

    @Test
    public void testMapsFTContent() {
        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);

        internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, true);
    }

    @Test(expected = MethodeArticleNotEligibleForPublishException.class)
    public void thatExceptionIsThrownWhenSourceCodeNotFTOrContentPlaceholder() {
        attributesPlaceholdersValues.put("sourceCode", "THIS_IS_NOT_A_GOOD_SOURCE_CODE");
        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);

        internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
    }

    @Test(expected = MethodeArticleNotEligibleForPublishException.class)
    public void thatArticleIneligibleForPublishThrowsException() {
        when(methodeArticleValidator.getPublishingStatus(any(), any(), anyBoolean()))
                .thenReturn(PublishingStatus.INELIGIBLE);

        internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
    }

    @Test(expected = MethodeMarkedDeletedException.class)
    public void thatArticleMarkedAsDeletedThrowsException() {
        when(methodeArticleValidator.getPublishingStatus(any(), any(), anyBoolean()))
                .thenReturn(PublishingStatus.DELETED);

        internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
    }

    @Test
    public void testDesignThemeFromOldSource() {
        final String oldDesignTheme = "extra";
        valuePlaceholdersValues.put("contentPackage", true);
        valuePlaceholdersValues.put("oldDesignTheme", oldDesignTheme);
        valuePlaceholdersValues.put("tableOfContentsSequence", "tableOfContentsSequence");
        valuePlaceholdersValues.put("tableOfContentsLabelType", "tableOfContentsLabelType");

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);
        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        final Design design = actual.getDesign();
        assertThat(design, is(notNullValue()));
        assertThat(design.getTheme(), is(oldDesignTheme));
    }

    @Test
    public void testDesignThemeFromNewSourcePrioritisedOverOldOne() {
        final String oldDesignTheme = "extra";
        valuePlaceholdersValues.put("contentPackage", true);
        valuePlaceholdersValues.put("oldDesignTheme", oldDesignTheme);
        valuePlaceholdersValues.put("tableOfContentsSequence", "tableOfContentsSequence");
        valuePlaceholdersValues.put("tableOfContentsLabelType", "tableOfContentsLabelType");

        final String designTheme = "extra";
        attributesPlaceholdersValues.put("designTheme", designTheme);

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);
        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        final Design design = actual.getDesign();
        assertThat(design, is(notNullValue()));
        assertThat(design.getTheme(), is(designTheme));
    }

    @Test
    public void testDesignThemeFromNoSource() {
        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        final Design design = actual.getDesign();
        assertThat(design, is(notNullValue()));
        assertThat(design.getTheme(), is("basic"));
    }

    @Test
    public void testDesignLayout() {
        final String designLayout = "wide";
        attributesPlaceholdersValues.put("designLayout", designLayout);

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);
        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        final Design design = actual.getDesign();
        assertThat(design, is(notNullValue()));
        assertThat(design.getLayout(), is(designLayout));
    }

    @Test
    public void testDesignLayoutFromNoSource() {
        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        final Design design = actual.getDesign();
        assertThat(design, is(notNullValue()));
        assertThat(design.getLayout(), is("default"));
    }

    @Test
    public void testTableOfContents() {
        final String tableOfContentsSequence = "exact-order";
        final String tableOfContentsLabelType = "part-number";

        valuePlaceholdersValues.put("contentPackage", true);
        valuePlaceholdersValues.put("oldDesignTheme", "oldDesignTheme");
        valuePlaceholdersValues.put("tableOfContentsSequence", tableOfContentsSequence);
        valuePlaceholdersValues.put("tableOfContentsLabelType", tableOfContentsLabelType);

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);
        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        assertThat(actual.getTableOfContents(), is(notNullValue()));
        assertThat(actual.getTableOfContents().getSequence(), is(tableOfContentsSequence));
        assertThat(actual.getTableOfContents().getLabelType(), is(tableOfContentsLabelType));
    }

    @Test
    public void testTableOfContentsNullIfBothSequenceAndLabelEmpty() {
        valuePlaceholdersValues.put("contentPackage", true);
        valuePlaceholdersValues.put("oldDesignTheme", "oldDesignTheme");
        valuePlaceholdersValues.put("tableOfContentsSequence", "");
        valuePlaceholdersValues.put("tableOfContentsLabelType", "");

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);
        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        assertThat(actual.getTableOfContents(), is(nullValue()));
    }

    @Test
    public void testLeadImages() {
        final String squareImageUUID = UUID.randomUUID().toString();
        final String standardImageUUID = UUID.randomUUID().toString();
        final String wideImageUUID = UUID.randomUUID().toString();

        valuePlaceholdersValues.put("leadImageSet", true);
        valuePlaceholdersValues.put("squareImageUUID", squareImageUUID);
        valuePlaceholdersValues.put("standardImageUUID", standardImageUUID);
        valuePlaceholdersValues.put("wideImageUUID", wideImageUUID);

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);
        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        final List<Image> leadImages = actual.getLeadImages();
        assertThat(leadImages, is(notNullValue()));
        assertThat(leadImages.size(), is(3));
        assertThat(leadImages.get(0).getId(), is(squareImageUUID));
        assertThat(leadImages.get(1).getId(), is(standardImageUUID));
        assertThat(leadImages.get(2).getId(), is(wideImageUUID));
    }

    @Test
    public void thatValidArticleWithTopperIsMappedCorrectly() {
        String backgroundColour = "fooBackground";
        String layout = "barColor";
        String headline = "foobar headline";
        String standfirst = "foobar standfirst";

        valuePlaceholdersValues.put("topper", true);
        valuePlaceholdersValues.put("topperBackgroundColour", backgroundColour);
        valuePlaceholdersValues.put("topperLayout", layout);
        valuePlaceholdersValues.put("topperHeadline", headline);
        valuePlaceholdersValues.put("topperStandfirst", standfirst);

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);
        InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        assertThat(actual.getUuid(), equalTo(ARTICLE_UUID));
        assertThat(actual.getLastModified(), equalTo(LAST_MODIFIED));
        assertThat(actual.getPublishReference(), equalTo(TX_ID));

        assertThat(actual.getTopper().getBackgroundColour(), equalTo(backgroundColour));
        assertThat(actual.getTopper().getLayout(), equalTo(layout));
        assertThat(actual.getTopper().getStandfirst(), equalTo(standfirst));
        assertThat(actual.getTopper().getHeadline(), equalTo(headline));
    }

    @Test
    public void thatValidArticleWithTopperButEmptyStandfirstAndHeadlineIsMappedCorrectly() {
        String backgroundColour = "fooBackground";
        String layout = "barColor";

        valuePlaceholdersValues.put("topper", true);
        valuePlaceholdersValues.put("topperBackgroundColour", backgroundColour);
        valuePlaceholdersValues.put("topperLayout", layout);
        valuePlaceholdersValues.put("topperHeadline", "");
        valuePlaceholdersValues.put("topperStandfirst", "");

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);
        InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        assertThat(actual.getUuid(), equalTo(ARTICLE_UUID));
        assertThat(actual.getLastModified(), equalTo(LAST_MODIFIED));
        assertThat(actual.getPublishReference(), equalTo(TX_ID));

        assertThat(actual.getTopper().getBackgroundColour(), equalTo(backgroundColour));
        assertThat(actual.getTopper().getLayout(), equalTo(layout));
        assertThat(actual.getTopper().getStandfirst(), equalTo(""));
        assertThat(actual.getTopper().getHeadline(), equalTo(""));
    }

    @Test
    public void testValidArticleWithMissingTopperLayoutWillHaveNoTopper() {
        String backgroundColour = "fooBackground";

        valuePlaceholdersValues.put("topper", true);
        valuePlaceholdersValues.put("topperBackgroundColour", backgroundColour);
        valuePlaceholdersValues.put("topperLayout", "");
        valuePlaceholdersValues.put("topperHeadline", "");
        valuePlaceholdersValues.put("topperStandfirst", "");

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);
        InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        assertThat(actual.getTopper(), is(nullValue()));
    }

    @Test
    public void testNullContentPackageNextIsNullUpcomingDesc() {
        valuePlaceholdersValues.put("contentPackage", true);
        valuePlaceholdersValues.put("oldDesignTheme", "");
        valuePlaceholdersValues.put("tableOfContentsSequence", "");
        valuePlaceholdersValues.put("tableOfContentsLabelType", "");
        valuePlaceholdersValues.put("contentPackageNext", null);

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);

        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
        assertThat(actual.getUnpublishedContentDescription(), is(nullValue()));
    }

    @Test
    public void testEmptyContentPackageNextIsNullUpcomingDesc() {
        valuePlaceholdersValues.put("contentPackage", true);
        valuePlaceholdersValues.put("oldDesignTheme", "");
        valuePlaceholdersValues.put("tableOfContentsSequence", "");
        valuePlaceholdersValues.put("tableOfContentsLabelType", "");
        valuePlaceholdersValues.put("contentPackageNext", "");

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);

        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
        assertThat(actual.getUnpublishedContentDescription(), is(nullValue()));
    }

    @Test
    public void testBlankContentPackageNextIsNullUpcomingDesc() {
        valuePlaceholdersValues.put("contentPackage", true);
        valuePlaceholdersValues.put("oldDesignTheme", "");
        valuePlaceholdersValues.put("tableOfContentsSequence", "");
        valuePlaceholdersValues.put("tableOfContentsLabelType", "");
        valuePlaceholdersValues.put("contentPackageNext", "\t \r");

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);

        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
        assertThat(actual.getUnpublishedContentDescription(), is(nullValue()));
    }

    @Test
    public void testDummyContentPackageNextIsNullUpcomingDesc() {
        valuePlaceholdersValues.put("contentPackage", true);
        valuePlaceholdersValues.put("oldDesignTheme", "");
        valuePlaceholdersValues.put("tableOfContentsSequence", "");
        valuePlaceholdersValues.put("tableOfContentsLabelType", "");
        valuePlaceholdersValues.put("contentPackageNext", "<?EM-dummyText ... coming next ... ?>");

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);

        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
        assertThat(actual.getUnpublishedContentDescription(), is(nullValue()));
    }

    @Test
    public void testUnformattedContentPackageNextIsTrimmed() {
        final String unformattedContentPackageNext = " This is a unformatted description of the upcoming content ";
        valuePlaceholdersValues.put("contentPackage", true);
        valuePlaceholdersValues.put("oldDesignTheme", "");
        valuePlaceholdersValues.put("tableOfContentsSequence", "");
        valuePlaceholdersValues.put("tableOfContentsLabelType", "");
        valuePlaceholdersValues.put("contentPackageNext", unformattedContentPackageNext);

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);

        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
        assertThat(actual.getUnpublishedContentDescription(), is(unformattedContentPackageNext.trim()));
    }

    @Test
    public void testFormattedContentPackageNextIsPreserved() {
        final String formattedContentPackageNext = "<p>This is a unformatted <em>description</em> of the upcoming content</p>";
        valuePlaceholdersValues.put("contentPackage", true);
        valuePlaceholdersValues.put("oldDesignTheme", "");
        valuePlaceholdersValues.put("tableOfContentsSequence", "");
        valuePlaceholdersValues.put("tableOfContentsLabelType", "");
        valuePlaceholdersValues.put("contentPackageNext", formattedContentPackageNext);

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);

        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
        assertThat(actual.getUnpublishedContentDescription(), is(formattedContentPackageNext.trim()));
    }

    @Test
    public void testSummaryDisplayPosition() {
        final String displayPosition = "auto";
        valuePlaceholdersValues.put("summary", true);
        valuePlaceholdersValues.put("displayPosition", displayPosition);
        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);

        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        assertThat(actual.getSummary().getDisplayPosition(), equalTo(displayPosition));
    }

    @Test
    public void testSummaryDisplayPositionEmptyGiveNull() {
        valuePlaceholdersValues.put("summary", true);
        valuePlaceholdersValues.put("displayPosition", "");

        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);
        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        assertThat(actual.getSummary().getDisplayPosition(), equalTo(null));
    }

    @Test
    public void shouldTransformPushNotificationsCohortUK() {
        testPushNotificationsCohort(ATTRIBUTE_PUSH_NOTIFICATIONS_COHORT_UK, EXPECTED_PUSH_NOTIFICATIONS_COHORT_UK);
    }

    @Test
    public void shouldTransformPushNotificationsCohortGlobal() {
        testPushNotificationsCohort(ATTRIBUTE_PUSH_NOTIFICATIONS_COHORT_GLOBAL, EXPECTED_PUSH_NOTIFICATIONS_COHORT_GLOBAL);
    }

    @Test
    public void shouldTransformPushNotificationsCohortNone() {
        testPushNotificationsCohort(ATTRIBUTE_PUSH_NOTIFICATIONS_COHORT_NONE, null);
    }

    @Test
    public void testNotificationsTextIsSet() {
        testPushNotificationsText(VALUE_PUSH_NOTIFICATIONS_TEXT, VALUE_PUSH_NOTIFICATIONS_TEXT);
    }

    @Test
    public void testNotificationsTextIsEmpty() {
        testPushNotificationsText("", null);
    }

    @Test
    public void testNotificationsTextIsNull() {
        testPushNotificationsText(null, null);
    }

    @Test
    public void testNotificationsTextIsDummyText() {
        testPushNotificationsText(VALUE_PUSH_NOTIFICATIONS_IS_NULL, null);
    }

    @Test
    public void testBlocksIsSet() {
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(BLOCKS_KEY_IS_SET);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(BLOCKS_VALUE_IS_SET);
        Map<String, Object> templateValues = new HashMap<>();
        templateValues.put("blocks", Boolean.TRUE);
        templateValues.put("block-1", Boolean.TRUE);
        templateValues.put("block-name-1", "x");
        templateValues.put("block-html-value-1", "x-value");

        Map<String, Object> attributesTemplateValues = new HashMap<>();
        attributesTemplateValues.put("sourceCode", "DynamicContent");

        final EomFile eomFile = createDynamicContent(templateValues, attributesTemplateValues);
        final InternalComponents internalComponents = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
        assertThat(internalComponents.getBlocks(), notNullValue());
        assertThat(internalComponents.getBlocks().size(), equalTo(1));
        assertThat(internalComponents.getBlocks().get(0).getKey(), equalTo("x"));
        assertThat(internalComponents.getBlocks().get(0).getValueXML(), equalTo("x-value"));
        assertThat(internalComponents.getBlocks().get(0).getType(), equalTo("html-block"));
    }

    @Test
    public void testBlocksKeyIsEmptyValueIsSet() {
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(BLOCKS_KEY_IS_EMPTY);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(BLOCKS_VALUE_IS_SET);
        Map<String, Object> templateValues = new HashMap<>();
        templateValues.put("blocks", Boolean.TRUE);
        templateValues.put("block-1", Boolean.TRUE);
        templateValues.put("block-name-1", "");
        templateValues.put("block-html-value-1", "x-value");

        Map<String, Object> attributesTemplateValues = new HashMap<>();
        attributesTemplateValues.put("sourceCode", "DynamicContent");

        final EomFile eomFile = createDynamicContent(templateValues, attributesTemplateValues);
        final InternalComponents internalComponents = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
        assertThat(internalComponents.getBlocks(), notNullValue());
        assertThat(internalComponents.getBlocks().size(), equalTo(1));
        assertThat(internalComponents.getBlocks().get(0).getKey(), equalTo(""));
        assertThat(internalComponents.getBlocks().get(0).getValueXML(), equalTo("x-value"));
        assertThat(internalComponents.getBlocks().get(0).getType(), equalTo("html-block"));
    }

    @Test(expected = InvalidMethodeContentException.class)
    public void testBlocksThrowsExceptionIfKeyIsSetAndValueIsEmpty() {
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(BLOCKS_VALUE_IS_EMPTY);
        internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
    }

    @Test(expected = InvalidMethodeContentException.class)
    public void testBlocksThrowsExceptionIfBlocksIsEmpty() {
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(BLOCKS_KEY_IS_EMPTY);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(BLOCKS_VALUE_IS_EMPTY);
        internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
    }


    private void testPushNotificationsCohort(String attributePushNotificationsCohort, String expectedPushNotificationsCohort) {
        attributesPlaceholdersValues.put(PLACEHOLDER_PUSH_NOTIFICATIONS_COHORT, attributePushNotificationsCohort);
        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);

        InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
        assertThat(actual.getPushNotificationsCohort(), equalTo(expectedPushNotificationsCohort));
    }

    private void testPushNotificationsText(String valuePushNotificationsText, String expectedPushNotificationsText) {
        valuePlaceholdersValues.put(PLACEHOLDER_PUSH_NOTIFICATIONS_TEXT, valuePushNotificationsText);
        eomFile = createEomFile(valuePlaceholdersValues, attributesPlaceholdersValues);

        InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
        assertThat(actual.getPushNotificationsText(), equalTo(expectedPushNotificationsText));
    }

    private EomFile createEomFile(Map<String, Object> valuePlaceholdersValues,
                                  Map<String, Object> attributesPlaceholdersValues) {
        return new EomFile.Builder()
                .withUuid(ARTICLE_UUID)
                .withType(EOM_TYPE_COMPOUND_STORY)
                .withValue(buildEomFileValue(valuePlaceholdersValues))
                .withAttributes(buildEomFileAttributes(attributesPlaceholdersValues))
                .withWorkflowStatus("Stories/WebReady")
                .withWebUrl(null)
                .build();
    }

    private EomFile createDynamicContent(Map<String, Object> templateValues,
                                         Map<String, Object> attributesTemplateValues) {
        return new EomFile.Builder()
                .withUuid(ARTICLE_UUID)
                .withType(EOM_TYPE_COMPOUND_STORY)
                .withValue(buildEomFileValue(templateValues))
                .withAttributes(buildEomFileAttributes(attributesTemplateValues))
                .withWorkflowStatus("Stories/WebReady")
                .withWebUrl(null)
                .build();
    }
}

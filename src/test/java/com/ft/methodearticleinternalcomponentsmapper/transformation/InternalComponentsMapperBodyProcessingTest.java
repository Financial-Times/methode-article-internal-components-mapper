package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessor;
import com.ft.bodyprocessing.html.Html5SelfClosingTagBodyProcessor;
import com.ft.common.FileUtils;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
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

    private static final String TRANSACTION_ID = "tid_test";

    private static final String TRANSFORMED_BODY = "<body><p>some other random text</p></body>";
    private static final String EMPTY_BODY = "<body></body>";
    private static final Date LAST_MODIFIED = new Date();

    private static final String TEMPLATE_PLACEHOLDER_MAINIMAGE = "mainImageUuid";
    private static final String TEMPLATE_PLACEHOLDER_IMAGE_SET_UUID = "imageSetID";
    private static final String TEMPLATE_PLACEHOLDER_CONTENT_PACKAGE = "contentPackage";
    private static final String TEMPLATE_PLACEHOLDER_CONTENT_PACKAGE_DESC = "contentPackageDesc";
    private static final String TEMPLATE_PLACEHOLDER_CONTENT_PACKAGE_LIST_HREF = "contentPackageListHref";

    private static final String IMAGE_SET_UUID = "U116035516646705FC";
    private static final String SUMMARY = "summary";

    private static String ATTRIBUTES = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<!DOCTYPE ObjectMetadata SYSTEM \"/SysConfig/Classify/FTStories/classify.dtd\">" +
            "<ObjectMetadata>" +
            "    <OutputChannels>" +
            "        <DIFTcom>" +
            "            <isContentPackage>{{isContentPackage}}</isContentPackage>" +
            "            <DIFTcomArticleImage>{{articleImage}}</DIFTcomArticleImage>" +
            "        </DIFTcom>" +
            "    </OutputChannels>" +
            "    <EditorialNotes>" +
            "        <Sources>" +
            "           <Source>" +
            "               <SourceCode>{{sourceCode}}</SourceCode>" +
            "           </Source>" +
            "        </Sources>" +
            "    </EditorialNotes>" +
            "</ObjectMetadata>";

    private final UUID uuid = UUID.randomUUID();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private FieldTransformer bodyTransformer;
    private BodyProcessor htmlFieldProcessor;

    private EomFile standardEomFile;
    private InternalComponents standardExpectedContent;

    private MethodeArticleValidator methodeArticleValidator;
    private MethodeArticleValidator methodeContentPlaceholderValidator;

    private InternalComponentsMapper eomFileProcessor;

    public static EomFile createStandardEomFileWithMainImage(UUID uuid,
                                                             UUID mainImageUuid,
                                                             String articleImageMetadataFlag) {
        Map<String, Object> templateValues = new HashMap<>();
        templateValues.put(TEMPLATE_PLACEHOLDER_MAINIMAGE, mainImageUuid);
        return new EomFile.Builder()
                .withUuid(uuid.toString())
                .withType("EOM:CompoundStory")
                .withValue(buildEomFileValue(templateValues))
                .withAttributes(ATTRIBUTES
                        .replaceFirst("\\{\\{articleImage\\}\\}", articleImageMetadataFlag)
                        .replaceFirst("\\{\\{sourceCode\\}\\}", InternalComponentsMapper.SourceCode.FT))
                .build();
    }

    private static byte[] buildEomFileValue(Map<String, Object> templatePlaceholdersValues) {
        Template mustache = Mustache.compiler().escapeHTML(false).compile(ARTICLE_TEMPLATE);
        return mustache.execute(templatePlaceholdersValues).getBytes(UTF_8);
    }

    private static String buildEomFileSystemAttributes(String channel) {
        Template mustache = Mustache.compiler().compile(SYSTEM_ATTRIBUTES_TEMPLATE);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("channel", channel);
        return mustache.execute(attributes);
    }

    @Before
    public void setUp() throws Exception {
        bodyTransformer = mock(FieldTransformer.class);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(TRANSFORMED_BODY);

        htmlFieldProcessor = spy(new Html5SelfClosingTagBodyProcessor());

        standardEomFile = createStandardEomFile(uuid, InternalComponentsMapper.SourceCode.FT);
        standardExpectedContent = createStandardExpectedContent();

        methodeArticleValidator = mock(MethodeArticleValidator.class);
        methodeContentPlaceholderValidator = mock(MethodeArticleValidator.class);
        when(methodeArticleValidator.getPublishingStatus(any(), any(), anyBoolean())).thenReturn(PublishingStatus.VALID);
        when(methodeContentPlaceholderValidator.getPublishingStatus(any(), any(), anyBoolean())).thenReturn(PublishingStatus.VALID);

        Map<String, MethodeArticleValidator> articleValidators = new HashMap<>();
        articleValidators.put(InternalComponentsMapper.SourceCode.FT, methodeArticleValidator);
        articleValidators.put(InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER, methodeContentPlaceholderValidator);

        eomFileProcessor = new InternalComponentsMapper(bodyTransformer, htmlFieldProcessor, articleValidators);
    }

    @Test
    public void shouldTransformArticleBodyOnPublish() {
        final EomFile eomFile = new EomFile.Builder()
                .withValuesFrom(createStandardEomFile(uuid, InternalComponentsMapper.SourceCode.FT))
                .build();

        final InternalComponents expectedContent = InternalComponents.builder()
                .withValuesFrom(standardExpectedContent)
                .withXMLBody(TRANSFORMED_BODY).build();

        InternalComponents content = eomFileProcessor.map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);

        verify(bodyTransformer).transform(anyString(), eq(TRANSACTION_ID), eq(Maps.immutableEntry("uuid", eomFile.getUuid())));
        assertThat(content.getBodyXML(), equalTo(expectedContent.getBodyXML()));
    }

    @Test
    public void shouldTransformSummaryBodyToo() {
        final EomFile eomFile = new EomFile.Builder()
                .withValuesFrom(createStandardEomFileWithSummary())
                .build();

        final InternalComponents expectedContent = InternalComponents.builder()
                .withValuesFrom(standardExpectedContent)
                .withXMLBody(TRANSFORMED_BODY)
                .withSummary(Summary.builder().withBodyXML(TRANSFORMED_BODY).build())
                .build();

        InternalComponents content = eomFileProcessor.map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);

        verify(bodyTransformer, times(2)).transform(anyString(), eq(TRANSACTION_ID), eq(Maps.immutableEntry("uuid", eomFile.getUuid())));
        assertThat(content.getBodyXML(), equalTo(expectedContent.getBodyXML()));
        assertThat(content.getSummary().getBodyXML(), equalTo(expectedContent.getBodyXML()));
    }

    @Test
    public void shouldContentPlaceholderBodyShouldBeMissingOnPublish() {
        final EomFile eomFile = new EomFile.Builder()
                .withValuesFrom(createStandardEomFile(uuid, InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER))
                .build();

        InternalComponents content = eomFileProcessor.map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);
        assertNull(content.getBodyXML());
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

        verify(bodyTransformer).transform(anyString(), eq(TRANSACTION_ID), eq(Maps.immutableEntry("uuid", eomFile.getUuid())));
        assertThat(content.getBodyXML(), equalTo(expectedContent.getBodyXML()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfBodyTagIsMissingFromTransformedBody() {
        final EomFile eomFile = createEomStoryFile(uuid);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn("<p>some other random text</p>");
        eomFileProcessor.map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);
    }

    @Test(expected = InvalidMethodeContentException.class)
    public void shouldThrowExceptionIfBodyIsNull() {
        final EomFile eomFile = createEomStoryFile(uuid);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(null);
        eomFileProcessor.map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);
    }

    @Test(expected = InvalidMethodeContentException.class)
    public void shouldThrowExceptionIfBodyIsEmpty() {
        final EomFile eomFile = createEomStoryFile(uuid);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn("");
        eomFileProcessor.map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);
    }

    @Test(expected = InvalidMethodeContentException.class)
    public void shouldThrowExceptionIfTransformedBodyIsBlank() {
        final EomFile eomFile = createEomStoryFile(uuid);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn("<body> \n \n \n </body>");
        eomFileProcessor.map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);
    }

    @Test(expected = InvalidMethodeContentException.class)
    public void shouldThrowExceptionIfTransformedBodyIsEmpty() {
        final EomFile eomFile = createEomStoryFile(uuid);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(EMPTY_BODY);
        eomFileProcessor.map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);
    }

    @Test
    public void thatPreviewEmptyTransformedBodyIsAllowed() {
        final EomFile eomFile = createEomStoryFile(uuid);
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

    private EomFile createEomFileWithRandomContentPackage() {
        return createStandardEomFileWithContentPackage(
                uuid,
                "<a href=\"/FT/Content/Content%20Package/Live/content-package-test.dwc?uuid=" + UUID.randomUUID().toString() + "\"/>");
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
        String expectedTransformedBody = "<body><ft-content data-embedded=\"true\" type=\"http://www.ft.com/ontology/content/ImageSet\" url=\"http://api.ft.com/content/%s\"></ft-content>" +
                "                <p>random text for now</p>" +
                "            </body>";
        testMainImageReferenceIsPutInBodyWithMetadataFlag("Primary size",
                expectedTransformedBody);
    }

    @Test
    public void testMainImageReferenceIsPutInBodyWhenPresentAndArticleSizeFlag() throws Exception {
        String expectedTransformedBody = "<body><ft-content data-embedded=\"true\" type=\"http://www.ft.com/ontology/content/ImageSet\" url=\"http://api.ft.com/content/%s\"></ft-content>" +
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
        final EomFile eomFile = createStandardEomFile(uuid, InternalComponentsMapper.SourceCode.FT);

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
                + "<ft-content type=\"http://www.ft.com/ontology/content/ImageSet\" url=\"http://api.ft.com/content/" + expectedUUID + "\" data-embedded=\"true\"></ft-content>"
                + "</body>";
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(expectedBody);

        EomFile eomFile = createStandardEomFileWithImageSet(IMAGE_SET_UUID);
        InternalComponents content = eomFileProcessor.map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);

        assertThat(content.getBodyXML(), equalToIgnoringWhiteSpace(expectedBody));
    }

    @Test
    public void testImageSetPreview() {
        String expectedUUID = UUID.nameUUIDFromBytes(IMAGE_SET_UUID.getBytes(UTF_8)).toString();
        String expectedBody = "<body>"
                + "<p>random text for now</p>"
                + "<ft-content type=\"http://www.ft.com/ontology/content/ImageSet\" url=\"http://api.ft.com/content/" + expectedUUID + "\" data-embedded=\"true\"></ft-content>"
                + "</body>";
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(expectedBody);

        EomFile eomFile = createStandardEomFileWithImageSet(IMAGE_SET_UUID);
        InternalComponents content = eomFileProcessor.map(eomFile, TRANSACTION_ID, new Date(), true);

        assertThat(content.getBodyXML(), equalToIgnoringWhiteSpace(expectedBody));
    }

    private void testMainImageReferenceIsPutInBodyWithMetadataFlag(String articleImageMetadataFlag, String expectedTransformedBody) {
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).then(returnsFirstArg());
        final UUID imageUuid = UUID.randomUUID();
        final UUID expectedMainImageUuid = DeriveUUID.with(DeriveUUID.Salts.IMAGE_SET).from(imageUuid);
        final EomFile eomFile = createStandardEomFileWithMainImage(uuid, imageUuid,
                articleImageMetadataFlag);
        InternalComponents content = eomFileProcessor
                .map(eomFile, TRANSACTION_ID, LAST_MODIFIED, false);

        String expectedBody = String.format(expectedTransformedBody, expectedMainImageUuid);
        assertThat(content.getBodyXML(), equalToIgnoringWhiteSpace(expectedBody));
    }

    private EomFile createEomStoryFile(UUID uuid) {
        return createStandardEomFile(uuid, "EOM::Story", InternalComponentsMapper.SourceCode.FT, Collections.emptyMap());
    }

    private EomFile createStandardEomFile(UUID uuid, String sourceCode) {
        return createStandardEomFile(uuid, "EOM::CompoundStory", sourceCode, Collections.emptyMap());
    }

    private EomFile createStandardEomFileWithContentPackage(UUID uuid, String contentPackageHref) {
        Map<String, Object> templateValues = new HashMap<>();
        templateValues.put(TEMPLATE_PLACEHOLDER_CONTENT_PACKAGE, Boolean.TRUE);
        templateValues.put(TEMPLATE_PLACEHOLDER_CONTENT_PACKAGE_DESC, "cp");
        templateValues.put(TEMPLATE_PLACEHOLDER_CONTENT_PACKAGE_LIST_HREF, contentPackageHref);
        return new EomFile.Builder()
                .withUuid(uuid.toString())
                .withType("EOM::CompoundStory")
                .withValue(buildEomFileValue(templateValues))
                .withAttributes(ATTRIBUTES
                        .replaceFirst("\\{\\{isContentPackage\\}\\}", "true")
                        .replaceFirst("\\{\\{sourceCode\\}\\}", InternalComponentsMapper.SourceCode.FT))
                .withSystemAttributes(buildEomFileSystemAttributes("FTcom"))
                .withWorkflowStatus("Stories/WebReady")
                .withWebUrl(null)
                .build();
    }

    private EomFile createStandardEomFileWithImageSet(String imageSetID) {
        return createStandardEomFile(uuid, "EOM::CompoundStory", InternalComponentsMapper.SourceCode.FT,
                Collections.singletonMap(TEMPLATE_PLACEHOLDER_IMAGE_SET_UUID, imageSetID));
    }

    private EomFile createStandardEomFileWithSummary() {
        return createStandardEomFile(uuid, "EOM::CompoundStory", InternalComponentsMapper.SourceCode.FT,
                Collections.singletonMap(SUMMARY, true));
    }

    private EomFile createStandardEomFile(UUID uuid, String eomType, String sourceCode, Map<String, Object> templateValues) {
        return new EomFile.Builder()
                .withUuid(uuid.toString())
                .withType(eomType)
                .withValue(buildEomFileValue(templateValues))
                .withAttributes(ATTRIBUTES
                        .replaceFirst("\\{\\{isContentPackage\\}\\}", "false")
                        .replaceFirst("\\{\\{sourceCode\\}\\}", sourceCode))
                .withSystemAttributes(buildEomFileSystemAttributes("FTcom"))
                .withWorkflowStatus("Stories/WebReady")
                .withWebUrl(null)
                .build();
    }

    private InternalComponents createStandardExpectedContent() {
        return InternalComponents.builder()
                .withDesign(new Design("theme"))
                .withTableOfContents(new TableOfContents("sequence", "labelType"))
                .withTopper(new Topper("headline", "standfirst", "bgColor", "layout"))
                .withLeadImages(Arrays.asList(new Image("img1", "type1"), new Image("img2", "type1"), new Image("img3", "type2")))
                .withUnpublishedContentDescription("the next awesome article")
                .withXMLBody("<body><p>some other random text</p></body>")
                .withUuid(uuid.toString())
                .withPublishReference(TRANSACTION_ID)
                .withLastModified(LAST_MODIFIED)
                .build();
    }
}

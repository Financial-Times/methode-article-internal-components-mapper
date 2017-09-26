package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.html.Html5SelfClosingTagBodyProcessor;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleMarkedDeletedException;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleNotEligibleForPublishException;
import com.ft.methodearticleinternalcomponentsmapper.model.AlternativeTitles;
import com.ft.methodearticleinternalcomponentsmapper.model.Design;
import com.ft.methodearticleinternalcomponentsmapper.model.EomFile;
import com.ft.methodearticleinternalcomponentsmapper.model.Image;
import com.ft.methodearticleinternalcomponentsmapper.model.InternalComponents;
import com.ft.methodearticleinternalcomponentsmapper.model.TableOfContents;
import com.ft.methodearticleinternalcomponentsmapper.model.Topper;
import com.ft.methodearticleinternalcomponentsmapper.validation.MethodeArticleValidator;
import com.ft.methodearticleinternalcomponentsmapper.validation.PublishingStatus;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InternalComponentsMapperTest {

    private static final String ARTICLE_UUID = UUID.randomUUID().toString();
    private static final String TX_ID = "tid_test";
    private static final Date LAST_MODIFIED = new Date();

    private static final String ARTICLE_WITH_ALL_COMPONENTS = readFile("article/article_with_all_components.xml.mustache");
    private static final String ARTICLE_WITH_TOPPER = readFile("article/article_with_topper.xml.mustache");

    private static final String TRANSFORMED_BODY = "<body><p>some other random text</p></body>";

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

    private EomFile eomFile;
    private FieldTransformer bodyTransformer;
    private Html5SelfClosingTagBodyProcessor htmlFieldProcessor;

    private MethodeArticleValidator methodeArticleValidator;
    private MethodeArticleValidator methodeContentPlaceholderValidator;

    private InternalComponentsMapper internalComponentsMapper;

    @Before
    public void setUp() {
        eomFile = mock(EomFile.class);
        when(eomFile.getUuid()).thenReturn(ARTICLE_UUID);
        when(eomFile.getAttributes()).thenReturn(ATTRIBUTES
                .replaceFirst("\\{\\{articleImage\\}\\}", "Article size")
                .replaceFirst("\\{\\{sourceCode\\}\\}", InternalComponentsMapper.SourceCode.FT));

        bodyTransformer = mock(FieldTransformer.class);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(TRANSFORMED_BODY);

        htmlFieldProcessor = spy(new Html5SelfClosingTagBodyProcessor());

        methodeArticleValidator = mock(MethodeArticleValidator.class);
        methodeContentPlaceholderValidator = mock(MethodeArticleValidator.class);
        when(methodeArticleValidator.getPublishingStatus(any(), any(), anyBoolean())).thenReturn(PublishingStatus.VALID);
        when(methodeContentPlaceholderValidator.getPublishingStatus(any(), any(), anyBoolean())).thenReturn(PublishingStatus.VALID);

        Map<String, MethodeArticleValidator> articleValidators = new HashMap<>();
        articleValidators.put(InternalComponentsMapper.SourceCode.FT, methodeArticleValidator);
        articleValidators.put(InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER, methodeContentPlaceholderValidator);

        internalComponentsMapper = new InternalComponentsMapper(bodyTransformer, htmlFieldProcessor, articleValidators);
    }

    @Test
    public void thatValidArticleWithTopperIsMappedCorrectly() throws Exception {
        String backgroundColour = "fooBackground";
        String layout = "barColor";
        String headline = "foobar headline";
        String standfirst = "foobar standfirst";

        eomFile = new EomFile.Builder()
                .withUuid(ARTICLE_UUID)
                .withType("EOM::CompoundStory")
                .withAttributes(ATTRIBUTES
                        .replaceFirst("\\{\\{articleImage\\}\\}", "Article size")
                        .replaceFirst("\\{\\{sourceCode\\}\\}", InternalComponentsMapper.SourceCode.FT))
                .withValue(buildTopperOnlyEomFileValue(backgroundColour, layout, headline, standfirst))
                .build();

        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(TRANSFORMED_BODY);

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
    public void thatValidArticleWithTopperButEmptyStandfirstAndHeadlineIsMappedCorrectly() throws Exception {
        String backgroundColour = "fooBackground";
        String layout = "barColor";

        eomFile = new EomFile.Builder()
                .withUuid(ARTICLE_UUID)
                .withType("EOM::CompoundStory")
                .withAttributes(ATTRIBUTES
                        .replaceFirst("\\{\\{articleImage\\}\\}", "Article size")
                        .replaceFirst("\\{\\{sourceCode\\}\\}", InternalComponentsMapper.SourceCode.FT))
                .withValue(buildTopperOnlyEomFileValue(backgroundColour, layout, "", ""))
                .build();

        when(methodeArticleValidator.getPublishingStatus(eq(eomFile), eq(TX_ID), anyBoolean())).thenReturn(PublishingStatus.VALID);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(TRANSFORMED_BODY);

        InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        assertThat(actual.getUuid(), equalTo(ARTICLE_UUID));
        assertThat(actual.getLastModified(), equalTo(LAST_MODIFIED));
        assertThat(actual.getPublishReference(), equalTo(TX_ID));

        assertThat(actual.getTopper().getBackgroundColour(), equalTo(backgroundColour));
        assertThat(actual.getTopper().getLayout(), equalTo(layout));
        assertThat(actual.getTopper().getStandfirst(), equalTo(""));
        assertThat(actual.getTopper().getHeadline(), equalTo(""));
    }

    @Test(expected = MethodeArticleMarkedDeletedException.class)
    public void thatArticleMarkedAsDeletedThrowsException() throws Exception {
        when(methodeArticleValidator.getPublishingStatus(eq(eomFile), eq(TX_ID), anyBoolean()))
                .thenReturn(PublishingStatus.DELETED);

        internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
    }

    @Test(expected = MethodeArticleNotEligibleForPublishException.class)
    public void thatArticleIneligibleForPublishThrowsException() throws Exception {
        when(methodeArticleValidator.getPublishingStatus(eq(eomFile), eq(TX_ID), anyBoolean()))
                .thenReturn(PublishingStatus.INELIGIBLE);

        internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
    }

    @Test
    public void testValidArticleWithAllComponentsIsMappedCorrectly() {
        final String squareImg = UUID.randomUUID().toString();
        final String standardImg = UUID.randomUUID().toString();
        final String wideImg = UUID.randomUUID().toString();
        final String designTheme = "extra";
        final String sequence = "exact-order";
        final String labelType = "part-number";
        final String backgroundColour = "auto";
        final String layout = "split-text-left";
        final String headline = "Topper headline";
        final String standfirst = "Topper standfirst";
        final String contentPackageNext = "<p>Content package coming next text</p>";
        final String skyboxHeadline = "sample skybox headline";

        eomFile = new EomFile.Builder()
                .withUuid(ARTICLE_UUID)
                .withType("EOM::CompoundStory")
                .withAttributes(ATTRIBUTES
                        .replaceFirst("\\{\\{articleImage\\}\\}", "Article size")
                        .replaceFirst("\\{\\{sourceCode\\}\\}", InternalComponentsMapper.SourceCode.FT))
                .withValue(buildEomFileValue(squareImg, standardImg, wideImg, designTheme, sequence,
                        labelType, backgroundColour, layout, headline, standfirst, contentPackageNext, skyboxHeadline))
                .build();

        when(methodeArticleValidator.getPublishingStatus(eq(eomFile), eq(TX_ID), anyBoolean())).thenReturn(PublishingStatus.VALID);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(TRANSFORMED_BODY);

        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getUuid(), is(ARTICLE_UUID));
        assertThat(actual.getLastModified(), is(LAST_MODIFIED));
        assertThat(actual.getPublishReference(), is(TX_ID));

        final Design design = actual.getDesign();
        assertThat(design, is(notNullValue()));
        assertThat(design.getTheme(), is(designTheme));

        final TableOfContents tableOfContents = actual.getTableOfContents();
        assertThat(tableOfContents, is(notNullValue()));
        assertThat(tableOfContents.getSequence(), is(sequence));
        assertThat(tableOfContents.getLabelType(), is(labelType));

        final List<Image> leadImages = actual.getLeadImages();
        assertThat(leadImages, is(notNullValue()));
        assertThat(leadImages.size(), is(3));
        assertThat(leadImages.get(0).getId(), is(squareImg));
        assertThat(leadImages.get(1).getId(), is(standardImg));
        assertThat(leadImages.get(2).getId(), is(wideImg));

        final Topper topper = actual.getTopper();
        assertThat(topper, is(notNullValue()));
        assertThat(topper.getLayout(), is(layout));
        assertThat(topper.getBackgroundColour(), is(backgroundColour));
        assertThat(topper.getHeadline(), is(headline));
        assertThat(topper.getStandfirst(), is(standfirst));

        final AlternativeTitles alternativeTitles = actual.getAlternativeTitles();
        assertThat(alternativeTitles.getShortTeaser(), is(skyboxHeadline));

        assertThat(actual.getUnpublishedContentDescription(), is(contentPackageNext));
    }

    @Test
    public void testValidArticleWithMissingTopperLayoutWillHaveNoTopper() {
        final String squareImg = UUID.randomUUID().toString();
        final String standardImg = UUID.randomUUID().toString();
        final String wideImg = UUID.randomUUID().toString();
        final String designTheme = "extra";
        final String sequence = "exact-order";
        final String labelType = "part-number";

        eomFile = new EomFile.Builder()
                .withUuid(ARTICLE_UUID)
                .withType("EOM::CompoundStory")
                .withAttributes(ATTRIBUTES
                        .replaceFirst("\\{\\{articleImage\\}\\}", "Article size")
                        .replaceFirst("\\{\\{sourceCode\\}\\}", InternalComponentsMapper.SourceCode.FT))
                .withValue(buildEomFileValue(squareImg, standardImg, wideImg, designTheme, sequence,
                        labelType, "auto", "", "Topper Headline", "Topper standfirst", null, ""))
                .build();

        when(methodeArticleValidator.getPublishingStatus(eq(eomFile), eq(TX_ID), anyBoolean())).thenReturn(PublishingStatus.VALID);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(TRANSFORMED_BODY);

        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getUuid(), is(ARTICLE_UUID));
        assertThat(actual.getLastModified(), is(LAST_MODIFIED));
        assertThat(actual.getPublishReference(), is(TX_ID));

        final Design design = actual.getDesign();
        assertThat(design, is(notNullValue()));
        assertThat(design.getTheme(), is(designTheme));

        final TableOfContents tableOfContents = actual.getTableOfContents();
        assertThat(tableOfContents, is(notNullValue()));
        assertThat(tableOfContents.getSequence(), is(sequence));
        assertThat(tableOfContents.getLabelType(), is(labelType));

        final List<Image> leadImages = actual.getLeadImages();
        assertThat(leadImages, is(notNullValue()));
        assertThat(leadImages.size(), is(3));
        assertThat(leadImages.get(0).getId(), is(squareImg));
        assertThat(leadImages.get(1).getId(), is(standardImg));
        assertThat(leadImages.get(2).getId(), is(wideImg));

        assertThat(actual.getTopper(), is(nullValue()));
    }

    @Test
    public void testNullContentPackageNextIsNullUpcomingDesc() throws Exception {
        eomFile = buildEomFileWithContentPackageNext(null);

        when(methodeArticleValidator.getPublishingStatus(eq(eomFile), eq(TX_ID), anyBoolean())).thenReturn(PublishingStatus.VALID);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(TRANSFORMED_BODY);

        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
        assertThat(actual.getUnpublishedContentDescription(), is(nullValue()));
    }

    @Test
    public void testEmptyContentPackageNextIsNullUpcomingDesc() throws Exception {
        eomFile = buildEomFileWithContentPackageNext("");

        when(methodeArticleValidator.getPublishingStatus(eq(eomFile), eq(TX_ID), anyBoolean())).thenReturn(PublishingStatus.VALID);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(TRANSFORMED_BODY);

        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
        assertThat(actual.getUnpublishedContentDescription(), is(nullValue()));
    }

    @Test
    public void testBlankContentPackageNextIsNullUpcomingDesc() throws Exception {
        eomFile = buildEomFileWithContentPackageNext("\t \r");

        when(methodeArticleValidator.getPublishingStatus(eq(eomFile), eq(TX_ID), anyBoolean())).thenReturn(PublishingStatus.VALID);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(TRANSFORMED_BODY);

        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
        assertThat(actual.getUnpublishedContentDescription(), is(nullValue()));
    }

    @Test
    public void testDummyContentPackageNextIsNullUpcomingDesc() throws Exception {
        eomFile = buildEomFileWithContentPackageNext("<?EM-dummyText ... coming next ... ?>");

        when(methodeArticleValidator.getPublishingStatus(eq(eomFile), eq(TX_ID), anyBoolean())).thenReturn(PublishingStatus.VALID);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(TRANSFORMED_BODY);

        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
        assertThat(actual.getUnpublishedContentDescription(), is(nullValue()));
    }

    @Test
    public void testUnformattedContentPackageNextIsTrimmed() throws Exception {
        final String unformattedContentPackageNext = " This is a unformatted description of the upcoming content ";
        eomFile = buildEomFileWithContentPackageNext(unformattedContentPackageNext);

        when(methodeArticleValidator.getPublishingStatus(eq(eomFile), eq(TX_ID), anyBoolean())).thenReturn(PublishingStatus.VALID);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(TRANSFORMED_BODY);

        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
        assertThat(actual.getUnpublishedContentDescription(), is(unformattedContentPackageNext.trim()));
    }

    @Test
    public void testFormattedContentPackageNextIsPreserved() throws Exception {
        final String formattedContentPackageNext = "<p>This is a unformatted <em>description</em> of the upcoming content</p>";
        eomFile = buildEomFileWithContentPackageNext(formattedContentPackageNext);

        when(methodeArticleValidator.getPublishingStatus(eq(eomFile), eq(TX_ID), anyBoolean())).thenReturn(PublishingStatus.VALID);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(TRANSFORMED_BODY);

        final InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
        assertThat(actual.getUnpublishedContentDescription(), is(formattedContentPackageNext.trim()));
    }

    @Test
    public void thatContentPlaceholderIsTransformed() throws Exception {
        String backgroundColour = "fooBackground";
        String layout = "barColor";

        eomFile = new EomFile.Builder()
                .withUuid(ARTICLE_UUID)
                .withType("EOM::CompoundStory")
                .withAttributes(ATTRIBUTES
                        .replaceFirst("\\{\\{articleImage\\}\\}", "Article size")
                        .replaceFirst("\\{\\{sourceCode\\}\\}", InternalComponentsMapper.SourceCode.CONTENT_PLACEHOLDER))
                .withValue(buildTopperOnlyEomFileValue(backgroundColour, layout, "", ""))
                .build();

        when(methodeContentPlaceholderValidator.getPublishingStatus(eq(eomFile), eq(TX_ID), eq(Boolean.FALSE))).thenReturn(PublishingStatus.VALID);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(TRANSFORMED_BODY);

        InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        assertThat(actual.getUuid(), equalTo(ARTICLE_UUID));
        assertThat(actual.getLastModified(), equalTo(LAST_MODIFIED));
        assertThat(actual.getPublishReference(), equalTo(TX_ID));
    }

    @Test (expected = MethodeArticleNotEligibleForPublishException.class)
    public void thatExceptionIsThrownWhenSourceCodeNotFTOrContentPlaceholder() throws Exception {
        String backgroundColour = "fooBackground";
        String layout = "barColor";

        eomFile = new EomFile.Builder()
                .withUuid(ARTICLE_UUID)
                .withType("EOM::CompoundStory")
                .withAttributes(ATTRIBUTES
                        .replaceFirst("\\{\\{articleImage\\}\\}", "Article size")
                        .replaceFirst("\\{\\{sourceCode\\}\\}", "FastFT"))
                .withValue(buildTopperOnlyEomFileValue(backgroundColour, layout, "", ""))
                .build();

        when(methodeContentPlaceholderValidator.getPublishingStatus(eq(eomFile), eq(TX_ID), eq(Boolean.FALSE))).thenReturn(PublishingStatus.VALID);
        when(bodyTransformer.transform(anyString(), anyString(), anyVararg())).thenReturn(TRANSFORMED_BODY);

        internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
    }

    private byte[] buildTopperOnlyEomFileValue(
            String backgroundColour,
            String layout,
            String headline,
            String standfirst) {

        Template mustache = Mustache.compiler().escapeHTML(false).compile(ARTICLE_WITH_TOPPER);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("backgroundColour", backgroundColour);
        attributes.put("layout", layout);
        attributes.put("headline", headline);
        attributes.put("standfirst", standfirst);

        return mustache.execute(attributes).getBytes(UTF_8);
    }

    private EomFile buildEomFileWithContentPackageNext(String contentPackageNext) {
        return new EomFile.Builder()
                .withUuid(ARTICLE_UUID)
                .withType("EOM::CompoundStory")
                .withAttributes(ATTRIBUTES
                        .replaceFirst("\\{\\{articleImage\\}\\}", "Article size")
                        .replaceFirst("\\{\\{sourceCode\\}\\}", InternalComponentsMapper.SourceCode.FT))
                .withValue(buildEomFileValue(
                        "squareId",
                        "standardId",
                        "wideId",
                        "theme",
                        "seq",
                        "lblType",
                        "colour",
                        "layout",
                        "headline",
                        "standfirst",
                        contentPackageNext,
                        "skyboxHeadline"))
                .build();
    }

    private byte[] buildEomFileValue(
            String squareImg,
            String standardImg,
            String wideImg,
            String designTheme,
            String sequence,
            String labelType,
            String backgroundColour,
            String layout,
            String headline,
            String standfirst,
            String contentPackageNext,
            String skyboxHeadline) {
        Template mustache = Mustache.compiler().escapeHTML(false).compile(ARTICLE_WITH_ALL_COMPONENTS);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("squareImageUUID", squareImg);
        attributes.put("standardImageUUID", standardImg);
        attributes.put("wideImageUUID", wideImg);
        attributes.put("backgroundColour", backgroundColour);
        attributes.put("designTheme", designTheme);
        attributes.put("sequence", sequence);
        attributes.put("labelType", labelType);
        attributes.put("backgroundColour", backgroundColour);
        attributes.put("layout", layout);
        attributes.put("headline", headline);
        attributes.put("standfirst", standfirst);
        attributes.put("contentPackageNext", contentPackageNext);
        attributes.put("skyboxHeadline", skyboxHeadline);

        return mustache.execute(attributes).getBytes(UTF_8);
    }

    private static String readFile(final String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(
                    InternalComponentsMapperTest.class
                            .getClassLoader()
                            .getResource(path)
                            .toURI())),
                    "UTF-8"
            );
        } catch (IOException | URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }
}
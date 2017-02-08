package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleHasNoInternalComponentsException;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleMarkedDeletedException;
import com.ft.methodearticleinternalcomponentsmapper.exception.MethodeArticleNotEligibleForPublishException;
import com.ft.methodearticleinternalcomponentsmapper.model.EomFile;
import com.ft.methodearticleinternalcomponentsmapper.model.InternalComponents;
import com.ft.methodearticleinternalcomponentsmapper.validation.MethodeArticleValidator;
import com.ft.methodearticleinternalcomponentsmapper.validation.PublishingStatus;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InternalComponentsMapperTest {

    private static final String UUID = java.util.UUID.randomUUID().toString();
    private static final String TX_ID = "tid_test";
    private static final Date LAST_MODIFIED = new Date();

    private static final String ARTICLE_WITH_TOPPER = readFile("article/article_with_topper.xml.mustache");
    private static final String ARTICLE_WITH_NO_TOPPER = readFile("article/article_with_no_topper.xml");
    private static final String ARTICLE_WITH_EMPTY_TOPPER = readFile("article/article_with_empty_topper.xml");

    @Mock
    EomFile eomFile;

    @Mock
    private MethodeArticleValidator methodeArticleValidator;

    @InjectMocks
    private InternalComponentsMapper internalComponentsMapper;

    @Before
    public void setUp() {
        when(eomFile.getUuid()).thenReturn(UUID);
    }

    @Test
    public void thatValidArticleWithInternalComponentsIsMappedCorrectly() throws Exception {
        String backgroundColor = "fooBackground";
        String theme = "barColor";
        String headline = "foobar headline";
        String standfirst = "foobaz standfirst";
        String squareImageUUID = java.util.UUID.randomUUID().toString();
        String wideImageUUID = java.util.UUID.randomUUID().toString();
        String standardImageUUID = java.util.UUID.randomUUID().toString();
        eomFile = new EomFile.Builder()
                .withUuid(UUID)
                .withType("EOM::CompoundStory")
                .withValue(buildEomFileValue(backgroundColor, theme, headline, standfirst, squareImageUUID, standardImageUUID, wideImageUUID))
                .build();

        when(methodeArticleValidator.getPublishingStatus(eq(eomFile), eq(TX_ID), anyBoolean()))
                .thenReturn(PublishingStatus.VALID);

        InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        assertThat(actual.getUuid(), equalTo(UUID));
        assertThat(actual.getLastModified(), equalTo(LAST_MODIFIED));
        assertThat(actual.getPublishReference(), equalTo(TX_ID));

        assertThat(actual.getTopper().getBackgroundColor(), equalTo(backgroundColor));
        assertThat(actual.getTopper().getTheme(), equalTo(theme));
        assertThat(actual.getTopper().getStandfirst(), equalTo(standfirst));
        assertThat(actual.getTopper().getHeadline(), equalTo(headline));
        assertThat(actual.getTopper().getImages().get(0).getId(), equalTo(squareImageUUID));
        assertThat(actual.getTopper().getImages().get(1).getId(), equalTo(standardImageUUID));
        assertThat(actual.getTopper().getImages().get(2).getId(), equalTo(wideImageUUID));
    }

    @Test
    public void thatValidArticleWithTopperButEmptyStandfirstAndHeadlineIsMappedCorrectly() throws Exception {
        String bgColor = "fooBackground";
        String theme = "barColor";
        String squareImageUUID = java.util.UUID.randomUUID().toString();
        String wideImageUUID = java.util.UUID.randomUUID().toString();
        String standardImageUUID = java.util.UUID.randomUUID().toString();
        eomFile = new EomFile.Builder()
                .withUuid(UUID)
                .withType("EOM::CompoundStory")
                .withValue(buildEomFileValue(bgColor, theme, "", "", squareImageUUID, standardImageUUID, wideImageUUID))
                .build();

        when(methodeArticleValidator.getPublishingStatus(eq(eomFile), eq(TX_ID), anyBoolean()))
                .thenReturn(PublishingStatus.VALID);

        InternalComponents actual = internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);

        assertThat(actual.getUuid(), equalTo(UUID));
        assertThat(actual.getLastModified(), equalTo(LAST_MODIFIED));
        assertThat(actual.getPublishReference(), equalTo(TX_ID));

        assertThat(actual.getTopper().getBgColor(), equalTo(bgColor));
        assertThat(actual.getTopper().getTheme(), equalTo(theme));
        assertThat(actual.getTopper().getStandfirst(), equalTo(""));
        assertThat(actual.getTopper().getHeadline(), equalTo(""));
        assertThat(actual.getTopper().getImages().get(0).getId(), equalTo(squareImageUUID));
        assertThat(actual.getTopper().getImages().get(1).getId(), equalTo(standardImageUUID));
        assertThat(actual.getTopper().getImages().get(2).getId(), equalTo(wideImageUUID));
    }

    @Test(expected = MethodeArticleHasNoInternalComponentsException.class)
    public void thatValidArticleWithoutInternalComponentsThrowsException() throws Exception {
        eomFile = new EomFile.Builder()
                .withUuid(UUID)
                .withType("EOM::CompoundStory")
                .withValue(ARTICLE_WITH_NO_TOPPER.getBytes())
                .build();

        when(methodeArticleValidator.getPublishingStatus(eq(eomFile), eq(TX_ID), anyBoolean()))
                .thenReturn(PublishingStatus.VALID);

        internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
    }

    @Test(expected = MethodeArticleHasNoInternalComponentsException.class)
    public void thatValidArticleWithEmptyInternalComponentsThrowsException() throws Exception {
        eomFile = new EomFile.Builder()
                .withUuid(UUID)
                .withType("EOM::CompoundStory")
                .withValue(ARTICLE_WITH_EMPTY_TOPPER.getBytes())
                .build();

        when(methodeArticleValidator.getPublishingStatus(eq(eomFile), eq(TX_ID), anyBoolean()))
                .thenReturn(PublishingStatus.VALID);

        internalComponentsMapper.map(eomFile, TX_ID, LAST_MODIFIED, false);
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

    private byte[] buildEomFileValue(
            String backgroundColor, String theme, String headline, String standfirst,
            String squareImg, String standardImg, String wideImg) {

        Template mustache = Mustache.compiler().escapeHTML(false).compile(ARTICLE_WITH_TOPPER);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("backgroundColor", backgroundColor);
        attributes.put("theme", theme);
        attributes.put("headline", headline);
        attributes.put("standfirst", standfirst);
        attributes.put("squareImageUUID", squareImg);
        attributes.put("standardImageUUID", standardImg);
        attributes.put("wideImageUUID", wideImg);

        return mustache.execute(attributes).getBytes(UTF_8);
    }
}
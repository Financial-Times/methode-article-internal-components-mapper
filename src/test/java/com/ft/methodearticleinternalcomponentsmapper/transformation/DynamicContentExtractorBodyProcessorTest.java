package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import org.junit.Before;
import org.junit.Test;

import static com.ft.methodetesting.xml.XmlMatcher.identicalXmlTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DynamicContentExtractorBodyProcessorTest {

    private BodyProcessingContext bodyProcessingContext;

    private DynamicContentExtractorBodyProcessor dynamicContentExtractorBodyProcessor;

    @Before
    public void setUp() {
        dynamicContentExtractorBodyProcessor = new DynamicContentExtractorBodyProcessor();
        bodyProcessingContext = new BodyProcessingContext() {
        };
    }

    @Test
    public void testProcess_ExtractDynamicContent() {
        String body = "<body><p>Embedded Dynamic Content</p><p>" +
                "<a type=\"DynamicContent\" dtxInsert=\"Interactive Graphic Link\" href=\"/FT/Content/World%20News/Stories/WebPublished/test%20ig%20story.xml?uuid=d02886fc-58ff-11e8-9859-6668838a4c10\">Interactive Graphic</a>" +
                "Lorem ipsum</p></body>";
        String expected = "<body><p>Embedded Dynamic Content</p>" +
                "<a type=\"DynamicContent\" dtxInsert=\"Interactive Graphic Link\" href=\"/FT/Content/World%20News/Stories/WebPublished/test%20ig%20story.xml?uuid=d02886fc-58ff-11e8-9859-6668838a4c10\">Interactive Graphic</a>" +
                "<p>Lorem ipsum</p></body>";

        String result = dynamicContentExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, is(identicalXmlTo(expected)));
    }

    @Test
    public void testProcess_ExtractMultipleDynamicContent() {
        String body = "<body><p>Embedded Dynamic Content</p><p>" +
                "<a type=\"DynamicContent\" dtxInsert=\"Interactive Graphic Link\" href=\"/FT/Content/World%20News/Stories/WebPublished/test%20ig%20story.xml?uuid=d02886fc-58ff-11e8-9859-6668838a4c10\">Interactive Graphic</a>" +
                "<a type=\"DynamicContent\" dtxInsert=\"Interactive Graphic Link\" href=\"/FT/Content/World%20News/Stories/WebPublished/test%20ig%20story.xml?uuid=f1655aa4-6320-11e8-a39d-4df188287fff\">Interactive Graphic</a>" +
                "Lorem ipsum</p></body>";
        String expected = "<body><p>Embedded Dynamic Content</p>" +
                "<a type=\"DynamicContent\" dtxInsert=\"Interactive Graphic Link\" href=\"/FT/Content/World%20News/Stories/WebPublished/test%20ig%20story.xml?uuid=d02886fc-58ff-11e8-9859-6668838a4c10\">Interactive Graphic</a>" +
                "<a type=\"DynamicContent\" dtxInsert=\"Interactive Graphic Link\" href=\"/FT/Content/World%20News/Stories/WebPublished/test%20ig%20story.xml?uuid=f1655aa4-6320-11e8-a39d-4df188287fff\">Interactive Graphic</a>" +
                "<p>Lorem ipsum</p></body>";

        String result = dynamicContentExtractorBodyProcessor.process(body, bodyProcessingContext);

        assertThat(result, is(identicalXmlTo(expected)));
    }
}

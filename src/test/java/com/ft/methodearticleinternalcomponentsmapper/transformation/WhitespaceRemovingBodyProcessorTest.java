package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.methodearticleinternalcomponentsmapper.transformation.WhitespaceRemovingBodyProcessor;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class WhitespaceRemovingBodyProcessorTest {

	private static final BodyProcessingContext bodyProcessingContext = null;
	
	private WhitespaceRemovingBodyProcessor bodyProcessor;
	
	@Before
	public void setup() {
		bodyProcessor = new WhitespaceRemovingBodyProcessor();
	}
	
	@Test
	public void shouldRemoveTrailingWhitespace() {
		String result = bodyProcessor.process("text ", bodyProcessingContext);
		assertThat("whitespace not trimmed", result, is(equalTo("text")));
	}
	
	@Test
	public void shouldRemoveInitialWhitespace() {
		String result = bodyProcessor.process("\ntext", bodyProcessingContext);
		assertThat("whitespace not trimmed", result, is(equalTo("text")));
	}
	
	@Test
	public void shouldNotRemoveInternalWhitespace() {
		String result = bodyProcessor.process("text more text ", bodyProcessingContext);
		assertThat("whitespace not trimmed", result, is(equalTo("text more text")));
	}
	
	@Test
	public void shouldReturnNullForNullBody() {
		String result = bodyProcessor.process(null, bodyProcessingContext);
		assertThat("whitespace not trimmed", result, is(equalTo(null)));
		
	}
}

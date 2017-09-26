package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.methodearticleinternalcomponentsmapper.transformation.RemoveXMLEntityEscapingBodyProcessor;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class RemoveXMLEntityEscapingBodyProcessorTest {
private static final BodyProcessingContext bodyProcessingContext = null;
	
	private RemoveXMLEntityEscapingBodyProcessor bodyProcessor;
	
	@Before
	public void setup() {
		bodyProcessor = new RemoveXMLEntityEscapingBodyProcessor();
	}
	
	@Test
	public void shouldUnencodeAmpersand() {
		String result = bodyProcessor.process("Standard &amp; Poor", bodyProcessingContext);
		assertThat(result, is(equalTo("Standard & Poor")));
	}
	
	@Test
	public void shouldUnencodeLessThan() {
		String result = bodyProcessor.process("Big &lt; Gigantic", bodyProcessingContext);
		assertThat(result, is(equalTo("Big < Gigantic")));
	}
	
	@Test
	public void shouldUnencodeGreaterThan() {
		String result = bodyProcessor.process("Big &gt; Little", bodyProcessingContext);
		assertThat(result, is(equalTo("Big > Little")));
	}
	
	@Test
	public void shouldUnencodeApostrophe() {
		String result = bodyProcessor.process("Harry&apos;s game", bodyProcessingContext);
		assertThat(result, is(equalTo("Harry's game")));
	}
	
	@Test
	public void shouldUnencodeDoubleQuote() {
		String result = bodyProcessor.process("he said this: &quot;", bodyProcessingContext);
		assertThat(result, is(equalTo("he said this: \"")));
	}
	
	@Test
	public void shouldReturnNullForNullBody() {
		String result = bodyProcessor.process(null, bodyProcessingContext);
		assertThat(result, is(equalTo(null)));
		
	}
}

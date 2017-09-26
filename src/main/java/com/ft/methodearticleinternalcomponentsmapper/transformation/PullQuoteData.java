package com.ft.methodearticleinternalcomponentsmapper.transformation;

import org.apache.commons.lang.StringUtils;

public class PullQuoteData {

	private String quoteText;
	private String quoteSource;
    private String imageHtml;

    public String getQuoteText() {
		return quoteText;
	}

	public String getQuoteSource() {
		return quoteSource;
	}

	public void setQuoteText(String quoteText) {
		this.quoteText = quoteText;
	}

	public void setQuoteSource(String quoteSource) {
		this.quoteSource = quoteSource;
	}

    public void setImageHtml(String imageHtml) {
        this.imageHtml = imageHtml;
    }

    public String getImageHtml() {
        return imageHtml;
    }


    public boolean isAllRequiredDataPresent() {
		return containsValidData(this.quoteText) || containsValidData(this.quoteSource);
	}

	protected boolean containsValidData(String data) {
		return !StringUtils.isBlank(data);
	}
}

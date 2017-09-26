package com.ft.methodearticleinternalcomponentsmapper.transformation;

import org.apache.commons.lang.StringUtils;

public class DataTableData {

	private String body;

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public boolean isAllRequiredDataPresent() {
		return containsValidData(this.body);
	}

	protected boolean containsValidData(String data) {
		return !StringUtils.isBlank(data);
	}
}

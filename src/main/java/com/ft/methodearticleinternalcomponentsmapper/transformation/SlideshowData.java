package com.ft.methodearticleinternalcomponentsmapper.transformation;

import org.apache.commons.lang.StringUtils;

import java.util.List;

public class SlideshowData {
    private String uuid;
    private List<String> queryParams;
    private String title;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isAllRequiredDataPresent() {
        return containsValidData(this.uuid);
    }

    protected boolean containsValidData(String data) {
        return !StringUtils.isBlank(data);
    }

    public void setQueryParams(List<String> queryParams) {
        this.queryParams = queryParams;
    }

    public List<String> getQueryParams() {
        return queryParams;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

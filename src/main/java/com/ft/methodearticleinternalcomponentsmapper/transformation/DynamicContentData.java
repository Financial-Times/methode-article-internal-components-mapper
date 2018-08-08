package com.ft.methodearticleinternalcomponentsmapper.transformation;

import org.apache.commons.lang.StringUtils;

public class DynamicContentData {

    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isAllRequiredDataPresent() {
        return !StringUtils.isBlank(uuid);
    }
}

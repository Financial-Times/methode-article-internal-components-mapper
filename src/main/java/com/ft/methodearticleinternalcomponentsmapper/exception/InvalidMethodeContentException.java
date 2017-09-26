package com.ft.methodearticleinternalcomponentsmapper.exception;


public class InvalidMethodeContentException extends RuntimeException {
    private final String uuid;

    public InvalidMethodeContentException(String uuid, String reason) {
        super(reason);
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}

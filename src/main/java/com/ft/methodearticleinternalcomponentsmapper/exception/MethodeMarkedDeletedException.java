package com.ft.methodearticleinternalcomponentsmapper.exception;

import java.util.UUID;

public class MethodeMarkedDeletedException extends RuntimeException {
    private UUID uuid;
    private String type;

    public MethodeMarkedDeletedException(UUID uuid, String type) {
        super(String.format("Story of type %s has been marked as deleted %s", type, uuid));
        this.uuid = uuid;
        this.type = type;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getType() {
        return type;
    }
}

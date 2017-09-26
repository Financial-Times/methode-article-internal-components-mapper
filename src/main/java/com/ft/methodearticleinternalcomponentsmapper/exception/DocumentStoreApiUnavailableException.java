package com.ft.methodearticleinternalcomponentsmapper.exception;

public class DocumentStoreApiUnavailableException extends RuntimeException {

    private static final long serialVersionUID = 52256633187754667L;

    public DocumentStoreApiUnavailableException(String message) {
        super(message);
    }

    public DocumentStoreApiUnavailableException(Throwable throwable) {
        super(throwable);
    }

}

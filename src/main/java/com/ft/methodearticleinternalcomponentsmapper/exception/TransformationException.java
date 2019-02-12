package com.ft.methodearticleinternalcomponentsmapper.exception;


public class TransformationException extends RuntimeException {

    public TransformationException(Throwable cause) {
        super(cause);
    }

    public TransformationException(String message) {
        super(message);
    }

    public TransformationException(String message, Throwable cause) {
        super(message, cause);
    }
}

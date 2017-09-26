package com.ft.methodearticleinternalcomponentsmapper.exception;

import java.util.UUID;

public class MethodeArticleUnsupportedSourceCodeException extends RuntimeException {

    public MethodeArticleUnsupportedSourceCodeException(UUID uuid) {
        super(String.format("Article has unsupported SourceCode for the internal publishing flow, uuid=%s", uuid));
    }
}

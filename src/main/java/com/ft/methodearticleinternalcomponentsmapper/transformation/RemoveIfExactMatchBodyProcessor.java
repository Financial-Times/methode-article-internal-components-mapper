package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.BodyProcessingException;
import com.ft.bodyprocessing.BodyProcessor;

public class RemoveIfExactMatchBodyProcessor implements BodyProcessor {

    private final String stringToMatch;

    public RemoveIfExactMatchBodyProcessor(String stringToMatch) {
        this.stringToMatch = stringToMatch;
    }


    @Override
    public String process(String body, BodyProcessingContext bodyProcessingContext) throws BodyProcessingException {
        if (body != null && body.equals(stringToMatch)) {
            return "";
        }
        return body;
    }

}

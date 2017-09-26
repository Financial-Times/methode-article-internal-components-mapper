package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.BodyProcessorChain;

import java.util.Map;

public class BodyProcessingFieldTransformer implements FieldTransformer {

    private final BodyProcessorChain bodyProcessorChain;

    public BodyProcessingFieldTransformer(BodyProcessorChain bodyProcessorChain) {
        this.bodyProcessorChain = bodyProcessorChain;
    }

    @Override
    public String transform(String originalBody, String transactionId, Map.Entry<String, Object>... contextData) {
        return bodyProcessorChain.process(
            originalBody,
            new MappedDataBodyProcessingContext(transactionId, contextData));
    }

}
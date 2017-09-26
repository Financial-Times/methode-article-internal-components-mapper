package com.ft.methodearticleinternalcomponentsmapper.transformation;

import java.util.Map;

public interface FieldTransformer {

    String transform(
            String originalField,
            String transactionId,
            Map.Entry<String, Object>... contextData);

}

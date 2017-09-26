package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.bodyprocessing.DefaultTransactionIdBodyProcessingContext;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MappedDataBodyProcessingContext extends DefaultTransactionIdBodyProcessingContext {

  private final Map<String, Object> dataMap;

  public MappedDataBodyProcessingContext(
      final String transactionId,
      final Entry<String, Object>... contextData) {
    super(transactionId);
    this.dataMap =
        Arrays
            .stream(contextData)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }
  
  public <T> T get(final String contextKey, final Class<T> expectedType) {
    final Object data = dataMap.get(contextKey);
    return expectedType.cast(data);
  }

}
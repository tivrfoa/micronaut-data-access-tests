package io.micronaut.validation.validator.extractors;

import javax.validation.valueextraction.ValueExtractor;

@FunctionalInterface
public interface SimpleValueReceiver extends ValueExtractor.ValueReceiver {
   @Override
   default void iterableValue(String nodeName, Object object) {
   }

   @Override
   default void indexedValue(String nodeName, int i, Object object) {
   }

   @Override
   default void keyedValue(String nodeName, Object key, Object object) {
   }
}

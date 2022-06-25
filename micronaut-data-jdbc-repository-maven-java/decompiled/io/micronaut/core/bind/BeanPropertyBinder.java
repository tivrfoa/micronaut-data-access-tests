package io.micronaut.core.bind;

import io.micronaut.core.beans.BeanMap;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public interface BeanPropertyBinder extends ArgumentBinder<Object, Map<CharSequence, ? super Object>> {
   <T2> T2 bind(Class<T2> type, Set<? extends Entry<? extends CharSequence, Object>> source) throws ConversionErrorException;

   <T2> T2 bind(T2 object, ArgumentConversionContext<T2> context, Set<? extends Entry<? extends CharSequence, Object>> source);

   <T2> T2 bind(T2 object, Set<? extends Entry<? extends CharSequence, Object>> source) throws ConversionErrorException;

   default <T2> T2 bind(Class<T2> type, Map<? extends CharSequence, Object> source) throws ConversionErrorException {
      return this.bind(type, source.entrySet());
   }

   default <T2> T2 bind(T2 object, ArgumentConversionContext<T2> context, Map<? extends CharSequence, Object> source) {
      return this.bind(object, context, source.entrySet());
   }

   default <T2> T2 bind(T2 object, Map<? extends CharSequence, Object> source) throws ConversionErrorException {
      return this.bind(object, source.entrySet());
   }

   default <T2> T2 bind(T2 object, Object source) throws ConversionErrorException {
      return this.bind(object, BeanMap.of(source).entrySet());
   }
}

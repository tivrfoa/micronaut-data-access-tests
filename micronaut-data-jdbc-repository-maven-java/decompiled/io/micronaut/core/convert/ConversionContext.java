package io.micronaut.core.convert;

import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.TypeVariableResolver;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.ArrayUtils;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public interface ConversionContext extends AnnotationMetadataProvider, TypeVariableResolver, ErrorsContext {
   ConversionContext DEFAULT = new ConversionContext() {
   };
   ArgumentConversionContext<Boolean> BOOLEAN = ImmutableArgumentConversionContext.of(Argument.BOOLEAN);
   ArgumentConversionContext<Integer> INT = ImmutableArgumentConversionContext.of(Argument.INT);
   ArgumentConversionContext<Long> LONG = ImmutableArgumentConversionContext.of(Argument.LONG);
   ArgumentConversionContext<String> STRING = ImmutableArgumentConversionContext.of(Argument.STRING);
   ArgumentConversionContext<List<String>> LIST_OF_STRING = ImmutableArgumentConversionContext.of(Argument.LIST_OF_STRING);
   ArgumentConversionContext<Map> MAP = ImmutableArgumentConversionContext.of(Argument.of(Map.class));

   @Override
   default Map<String, Argument<?>> getTypeVariables() {
      return Collections.emptyMap();
   }

   default Locale getLocale() {
      return Locale.getDefault();
   }

   default Charset getCharset() {
      return StandardCharsets.UTF_8;
   }

   default <T> ArgumentConversionContext<T> with(Argument<T> argument) {
      final ConversionContext childContext = of(argument);
      final ConversionContext thisContext = this;
      return new DefaultArgumentConversionContext(argument, thisContext.getLocale(), thisContext.getCharset()) {
         @Override
         public <T extends Annotation> T synthesize(Class<T> annotationClass) {
            T annotation = childContext.synthesize(annotationClass);
            return (T)(annotation == null ? thisContext.synthesize(annotationClass) : annotation);
         }

         @Override
         public Annotation[] synthesizeAll() {
            return ArrayUtils.concat((Annotation[])childContext.synthesizeAll(), (Annotation[])thisContext.synthesizeAll());
         }

         @Override
         public Annotation[] synthesizeDeclared() {
            return ArrayUtils.concat((Annotation[])childContext.synthesizeDeclared(), (Annotation[])thisContext.synthesizeDeclared());
         }

         @Override
         public void reject(Exception exception) {
            thisContext.reject(exception);
         }

         @Override
         public void reject(Object value, Exception exception) {
            thisContext.reject(value, exception);
         }

         @Override
         public Iterator<ConversionError> iterator() {
            return thisContext.iterator();
         }

         @Override
         public Optional<ConversionError> getLastError() {
            return thisContext.getLastError();
         }
      };
   }

   static ConversionContext of(Map<String, Argument<?>> typeVariables) {
      return new ConversionContext() {
         @Override
         public Map<String, Argument<?>> getTypeVariables() {
            return typeVariables;
         }
      };
   }

   static <T> ArgumentConversionContext<T> of(Argument<T> argument) {
      return of(argument, null, null);
   }

   static <T> ArgumentConversionContext<T> of(Class<T> argument) {
      ArgumentUtils.requireNonNull("argument", (T)argument);
      return of(Argument.of(argument), null, null);
   }

   static <T> ArgumentConversionContext of(Argument<T> argument, @Nullable Locale locale) {
      return of(argument, locale, null);
   }

   static <T> ArgumentConversionContext<T> of(Argument<T> argument, @Nullable Locale locale, @Nullable Charset charset) {
      ArgumentUtils.requireNonNull("argument", argument);
      Charset finalCharset = charset != null ? charset : StandardCharsets.UTF_8;
      Locale finalLocale = locale != null ? locale : Locale.getDefault();
      return new DefaultArgumentConversionContext<>(argument, finalLocale, finalCharset);
   }
}

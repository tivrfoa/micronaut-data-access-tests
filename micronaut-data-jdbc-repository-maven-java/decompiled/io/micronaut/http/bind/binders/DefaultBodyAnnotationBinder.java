package io.micronaut.http.bind.binders;

import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Body;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DefaultBodyAnnotationBinder<T> implements BodyArgumentBinder<T> {
   protected final ConversionService<?> conversionService;

   public DefaultBodyAnnotationBinder(ConversionService conversionService) {
      this.conversionService = conversionService;
   }

   @Override
   public Class<Body> getAnnotationType() {
      return Body.class;
   }

   public ArgumentBinder.BindingResult<T> bind(ArgumentConversionContext<T> context, HttpRequest<?> source) {
      Optional<String> bodyComponent = context.getAnnotationMetadata().stringValue(Body.class);
      if (bodyComponent.isPresent()) {
         Optional<ConvertibleValues> body = source.getBody(ConvertibleValues.class);
         if (body.isPresent()) {
            ConvertibleValues values = (ConvertibleValues)body.get();
            String component = (String)bodyComponent.get();
            if (!values.contains(component)) {
               component = NameUtils.hyphenate(component);
            }

            Optional<T> value = values.get(component, context);
            return this.newResult((T)value.orElse(null), context);
         } else {
            return ArgumentBinder.BindingResult.EMPTY;
         }
      } else {
         Optional<?> body = source.getBody();
         if (!body.isPresent()) {
            return ArgumentBinder.BindingResult.EMPTY;
         } else {
            Object o = body.get();
            Optional<T> converted = this.conversionService.convert(o, context);
            return this.newResult((T)converted.orElse(null), context);
         }
      }
   }

   private ArgumentBinder.BindingResult<T> newResult(T converted, ArgumentConversionContext<T> context) {
      final Optional<ConversionError> lastError = context.getLastError();
      return lastError.isPresent() ? new ArgumentBinder.BindingResult<T>() {
         @Override
         public Optional<T> getValue() {
            return Optional.empty();
         }

         @Override
         public List<ConversionError> getConversionErrors() {
            return Collections.singletonList(lastError.get());
         }
      } : () -> Optional.ofNullable(converted);
   }
}

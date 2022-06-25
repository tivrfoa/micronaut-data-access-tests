package io.micronaut.core.bind.annotation;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import java.lang.annotation.Annotation;
import java.util.Optional;

public abstract class AbstractAnnotatedArgumentBinder<A extends Annotation, T, S> implements AnnotatedArgumentBinder<A, T, S> {
   private static final String DEFAULT_VALUE_MEMBER = "defaultValue";
   private final ConversionService<?> conversionService;

   protected AbstractAnnotatedArgumentBinder(ConversionService<?> conversionService) {
      this.conversionService = conversionService;
   }

   protected ArgumentBinder.BindingResult<T> doBind(ArgumentConversionContext<T> context, ConvertibleValues<?> values, String annotationValue) {
      return this.doBind(context, values, annotationValue, ArgumentBinder.BindingResult.EMPTY);
   }

   protected ArgumentBinder.BindingResult<T> doBind(
      ArgumentConversionContext<T> context, ConvertibleValues<?> values, String annotationValue, ArgumentBinder.BindingResult<T> defaultResult
   ) {
      return this.doConvert(this.doResolve(context, values, annotationValue), context, defaultResult);
   }

   @Nullable
   protected Object doResolve(ArgumentConversionContext<T> context, ConvertibleValues<?> values, String annotationValue) {
      Object value = this.resolveValue(context, values, annotationValue);
      if (value == null) {
         String fallbackName = this.getFallbackFormat(context.getArgument());
         if (!annotationValue.equals(fallbackName)) {
            value = this.resolveValue(context, values, fallbackName);
         }
      }

      return value;
   }

   protected String getFallbackFormat(Argument argument) {
      return NameUtils.hyphenate(argument.getName());
   }

   private Object resolveValue(ArgumentConversionContext<T> context, ConvertibleValues<?> values, String annotationValue) {
      Argument<T> argument = context.getArgument();
      if (StringUtils.isEmpty(annotationValue)) {
         annotationValue = argument.getName();
      }

      return values.get(annotationValue, context)
         .orElseGet(
            () -> this.conversionService
                  .convert(argument.getAnnotationMetadata().stringValue(Bindable.class, "defaultValue").orElse(null), context)
                  .orElse(null)
         );
   }

   protected ArgumentBinder.BindingResult<T> doConvert(Object value, ArgumentConversionContext<T> context) {
      return this.doConvert(value, context, ArgumentBinder.BindingResult.EMPTY);
   }

   protected ArgumentBinder.BindingResult<T> doConvert(Object value, ArgumentConversionContext<T> context, ArgumentBinder.BindingResult<T> defaultResult) {
      if (value == null) {
         return defaultResult;
      } else {
         Optional<T> result = this.conversionService.convert(value, context);
         return result.isPresent() && context.getArgument().getType() == Optional.class ? () -> (Optional<T>)result.get() : () -> result;
      }
   }
}

package io.micronaut.http.bind.binders;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.annotation.AbstractAnnotatedArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.QueryValue;
import java.util.Optional;

public class ParameterAnnotationBinder<T>
   extends AbstractAnnotatedArgumentBinder<QueryValue, T, HttpRequest<?>>
   implements AnnotatedRequestArgumentBinder<QueryValue, T> {
   QueryValueArgumentBinder<T> queryValueArgumentBinder;

   public ParameterAnnotationBinder(ConversionService<?> conversionService) {
      super(conversionService);
      this.queryValueArgumentBinder = new QueryValueArgumentBinder<>(conversionService);
   }

   @Override
   public Class<QueryValue> getAnnotationType() {
      return QueryValue.class;
   }

   public ArgumentBinder.BindingResult<T> bind(ArgumentConversionContext<T> context, HttpRequest<?> source) {
      Argument<T> argument = context.getArgument();
      HttpMethod httpMethod = source.getMethod();
      boolean permitsRequestBody = HttpMethod.permitsRequestBody(httpMethod);
      AnnotationMetadata annotationMetadata = argument.getAnnotationMetadata();
      boolean hasAnnotation = annotationMetadata.hasAnnotation(QueryValue.class);
      String parameterName = argument.getName();
      ArgumentBinder.BindingResult<T> result = this.queryValueArgumentBinder.bind(context, source);
      Optional<T> val = result.getValue();
      if (!val.isPresent() && !hasAnnotation) {
         result = this.doBind(context, source.getAttributes(), parameterName, ArgumentBinder.BindingResult.UNSATISFIED);
      }

      Argument<?> argumentType;
      if (argument.getType() == Optional.class) {
         argumentType = (Argument)argument.getFirstTypeVariable().orElse(argument);
      } else {
         argumentType = argument;
      }

      if (!result.getValue().isPresent() && !hasAnnotation && permitsRequestBody) {
         Optional<ConvertibleValues> body = source.getBody(ConvertibleValues.class);
         if (!body.isPresent()) {
            if (source.getBody().isPresent()) {
               Optional<String> text = source.getBody(String.class);
               if (text.isPresent()) {
                  return this.doConvert(text.get(), context);
               }
            }

            return ArgumentBinder.BindingResult.UNSATISFIED;
         }

         result = this.doBind(context, (ConvertibleValues<?>)body.get(), parameterName);
         if (!result.getValue().isPresent()) {
            if (ClassUtils.isJavaLangType(argumentType.getType())) {
               return Optional::empty;
            }

            return () -> source.getBody(argumentType);
         }
      }

      return result;
   }
}

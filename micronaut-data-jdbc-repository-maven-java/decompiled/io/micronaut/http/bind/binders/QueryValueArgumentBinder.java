package io.micronaut.http.bind.binders;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.annotation.AbstractAnnotatedArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.format.Format;
import io.micronaut.core.convert.value.ConvertibleMultiValues;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.uri.UriMatchInfo;
import io.micronaut.http.uri.UriMatchVariable;
import java.util.Collections;
import java.util.Optional;

public class QueryValueArgumentBinder<T>
   extends AbstractAnnotatedArgumentBinder<QueryValue, T, HttpRequest<?>>
   implements AnnotatedRequestArgumentBinder<QueryValue, T> {
   private final ConversionService<?> conversionService;

   public QueryValueArgumentBinder(ConversionService<?> conversionService) {
      super(conversionService);
      this.conversionService = conversionService;
   }

   @Override
   public Class<QueryValue> getAnnotationType() {
      return QueryValue.class;
   }

   public ArgumentBinder.BindingResult<T> bind(ArgumentConversionContext<T> context, HttpRequest<?> source) {
      ConvertibleMultiValues<String> parameters = source.getParameters();
      Argument<T> argument = context.getArgument();
      AnnotationMetadata annotationMetadata = argument.getAnnotationMetadata();
      boolean hasAnnotation = annotationMetadata.hasAnnotation(QueryValue.class);
      HttpMethod httpMethod = source.getMethod();
      boolean permitsRequestBody = HttpMethod.permitsRequestBody(httpMethod);
      ArgumentBinder.BindingResult<T> result;
      if (!hasAnnotation && permitsRequestBody) {
         result = ArgumentBinder.BindingResult.EMPTY;
      } else {
         Optional<T> multiValueConversion;
         if (annotationMetadata.hasAnnotation(Format.class)) {
            multiValueConversion = this.conversionService.convert(parameters, context);
         } else {
            multiValueConversion = Optional.empty();
         }

         if (multiValueConversion.isPresent()) {
            result = () -> multiValueConversion;
         } else {
            String parameterName = (String)annotationMetadata.stringValue(QueryValue.class).orElse(argument.getName());
            boolean bindAll = source.getAttribute(HttpAttributes.ROUTE_MATCH, UriMatchInfo.class).map(umi -> {
               UriMatchVariable uriMatchVariable = (UriMatchVariable)umi.getVariableMap().get(parameterName);
               return uriMatchVariable != null && uriMatchVariable.isExploded();
            }).orElse(false);
            if (bindAll) {
               Object value;
               if (Iterable.class.isAssignableFrom(argument.getType())) {
                  value = this.doResolve(context, parameters, parameterName);
                  if (value == null) {
                     value = Collections.emptyList();
                  }
               } else {
                  value = parameters.asMap();
               }

               result = this.doConvert(value, context);
            } else {
               result = this.doBind(context, parameters, parameterName);
            }
         }
      }

      return result;
   }
}

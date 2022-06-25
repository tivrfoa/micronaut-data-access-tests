package io.micronaut.http.client.bind.binders;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleMultiValues;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.bind.AnnotatedClientArgumentRequestBinder;
import io.micronaut.http.client.bind.ClientRequestUriContext;
import io.micronaut.http.uri.UriMatchVariable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class QueryValueClientArgumentRequestBinder implements AnnotatedClientArgumentRequestBinder<QueryValue> {
   private final ConversionService<?> conversionService;

   public QueryValueClientArgumentRequestBinder(ConversionService<?> conversionService) {
      this.conversionService = conversionService;
   }

   @NonNull
   @Override
   public Class<QueryValue> getAnnotationType() {
      return QueryValue.class;
   }

   @Override
   public void bind(
      @NonNull ArgumentConversionContext<Object> context,
      @NonNull ClientRequestUriContext uriContext,
      @NonNull Object value,
      @NonNull MutableHttpRequest<?> request
   ) {
      String parameterName = (String)context.getAnnotationMetadata()
         .stringValue(QueryValue.class)
         .filter(StringUtils::isNotEmpty)
         .orElse(context.getArgument().getName());
      UriMatchVariable uriVariable = (UriMatchVariable)uriContext.getUriTemplate()
         .getVariables()
         .stream()
         .filter(v -> v.getName().equals(parameterName))
         .findFirst()
         .orElse(null);
      if (uriVariable != null) {
         if (uriVariable.isExploded()) {
            uriContext.setPathParameter(parameterName, value);
         } else {
            String convertedValue = (String)this.conversionService
               .convert(value, ConversionContext.STRING.with(context.getAnnotationMetadata()))
               .filter(StringUtils::isNotEmpty)
               .orElse(null);
            if (convertedValue != null) {
               uriContext.setPathParameter(parameterName, convertedValue);
            } else {
               uriContext.setPathParameter(parameterName, value);
            }
         }
      } else {
         ArgumentConversionContext<ConvertibleMultiValues> conversionContext = context.with(
            Argument.of(ConvertibleMultiValues.class, context.getArgument().getName(), context.getAnnotationMetadata())
         );
         Optional<ConvertibleMultiValues<String>> multiValues = this.conversionService.convert(value, conversionContext).map(values -> values);
         if (multiValues.isPresent()) {
            Map<String, List<String>> queryParameters = uriContext.getQueryParameters();
            ((ConvertibleMultiValues)multiValues.get()).forEach((k, v) -> {
               if (queryParameters.containsKey(k)) {
                  ((List)queryParameters.get(k)).addAll(v);
               } else {
                  queryParameters.put(k, v);
               }

            });
         } else {
            this.conversionService
               .convert(value, ConversionContext.STRING.with(context.getAnnotationMetadata()))
               .ifPresent(v -> uriContext.addQueryParameter(parameterName, v));
         }
      }

   }
}

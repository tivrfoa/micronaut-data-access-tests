package io.micronaut.http.bind.binders;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.annotation.AbstractAnnotatedArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleMultiValues;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.uri.UriMatchInfo;
import io.micronaut.http.uri.UriMatchVariable;
import java.util.Collections;
import java.util.Optional;

public class PathVariableAnnotationBinder<T>
   extends AbstractAnnotatedArgumentBinder<PathVariable, T, HttpRequest<?>>
   implements AnnotatedRequestArgumentBinder<PathVariable, T> {
   public PathVariableAnnotationBinder(ConversionService<?> conversionService) {
      super(conversionService);
   }

   @Override
   public Class<PathVariable> getAnnotationType() {
      return PathVariable.class;
   }

   public ArgumentBinder.BindingResult<T> bind(ArgumentConversionContext<T> context, HttpRequest<?> source) {
      ConvertibleMultiValues<String> parameters = source.getParameters();
      Argument<T> argument = context.getArgument();
      AnnotationMetadata annotationMetadata = argument.getAnnotationMetadata();
      boolean hasAnnotation = annotationMetadata.hasAnnotation(PathVariable.class);
      String parameterName = (String)annotationMetadata.stringValue(PathVariable.class).orElse(argument.getName());
      Optional<UriMatchInfo> matchInfo = source.getAttribute(HttpAttributes.ROUTE_MATCH, UriMatchInfo.class);
      boolean bindAll = matchInfo.flatMap(
            umi -> umi.getVariables().stream().filter(v -> v.getName().equals(parameterName)).findFirst().map(UriMatchVariable::isExploded)
         )
         .orElse(false);
      ArgumentBinder.BindingResult<T> result;
      if (hasAnnotation && matchInfo.isPresent()) {
         ConvertibleValues<Object> variableValues = ConvertibleValues.of(((UriMatchInfo)matchInfo.get()).getVariableValues());
         if (bindAll) {
            Object value;
            if (Iterable.class.isAssignableFrom(argument.getType())) {
               value = this.doResolve(context, variableValues, parameterName);
               if (value == null) {
                  value = Collections.emptyList();
               }
            } else {
               value = parameters.asMap();
            }

            result = this.doConvert(value, context);
         } else {
            result = this.doBind(context, variableValues, parameterName);
         }
      } else {
         result = ArgumentBinder.BindingResult.EMPTY;
      }

      return result;
   }
}

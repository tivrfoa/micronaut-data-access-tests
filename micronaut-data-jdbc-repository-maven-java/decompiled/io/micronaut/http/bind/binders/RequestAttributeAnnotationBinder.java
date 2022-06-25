package io.micronaut.http.bind.binders;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.annotation.AbstractAnnotatedArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.RequestAttribute;

public class RequestAttributeAnnotationBinder<T>
   extends AbstractAnnotatedArgumentBinder<RequestAttribute, T, HttpRequest<?>>
   implements AnnotatedRequestArgumentBinder<RequestAttribute, T> {
   public RequestAttributeAnnotationBinder(ConversionService<?> conversionService) {
      super(conversionService);
   }

   @Override
   public Class<RequestAttribute> getAnnotationType() {
      return RequestAttribute.class;
   }

   public ArgumentBinder.BindingResult<T> bind(ArgumentConversionContext<T> argument, HttpRequest<?> source) {
      MutableConvertibleValues<Object> parameters = source.getAttributes();
      AnnotationMetadata annotationMetadata = argument.getAnnotationMetadata();
      String parameterName = (String)annotationMetadata.stringValue(RequestAttribute.class).orElse(argument.getArgument().getName());
      return this.doBind(argument, parameters, parameterName, ArgumentBinder.BindingResult.UNSATISFIED);
   }

   @Override
   protected String getFallbackFormat(Argument argument) {
      return NameUtils.hyphenate(NameUtils.capitalize(argument.getName()), false);
   }
}

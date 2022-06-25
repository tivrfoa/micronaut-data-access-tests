package io.micronaut.http.bind.binders;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.annotation.AbstractAnnotatedArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleMultiValues;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Header;

public class HeaderAnnotationBinder<T> extends AbstractAnnotatedArgumentBinder<Header, T, HttpRequest<?>> implements AnnotatedRequestArgumentBinder<Header, T> {
   public HeaderAnnotationBinder(ConversionService<?> conversionService) {
      super(conversionService);
   }

   public ArgumentBinder.BindingResult<T> bind(ArgumentConversionContext<T> argument, HttpRequest<?> source) {
      ConvertibleMultiValues<String> parameters = source.getHeaders();
      AnnotationMetadata annotationMetadata = argument.getAnnotationMetadata();
      String parameterName = (String)annotationMetadata.stringValue(Header.class).orElse(argument.getArgument().getName());
      return this.doBind(argument, parameters, parameterName);
   }

   @Override
   public Class<Header> getAnnotationType() {
      return Header.class;
   }

   @Override
   protected String getFallbackFormat(Argument argument) {
      return NameUtils.hyphenate(NameUtils.capitalize(argument.getName()), false);
   }
}

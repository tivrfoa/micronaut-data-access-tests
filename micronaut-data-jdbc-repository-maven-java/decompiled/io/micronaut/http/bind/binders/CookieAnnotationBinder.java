package io.micronaut.http.bind.binders;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.annotation.AbstractAnnotatedArgumentBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.CookieValue;
import io.micronaut.http.cookie.Cookie;

public class CookieAnnotationBinder<T>
   extends AbstractAnnotatedArgumentBinder<CookieValue, T, HttpRequest<?>>
   implements AnnotatedRequestArgumentBinder<CookieValue, T> {
   public CookieAnnotationBinder(ConversionService<?> conversionService) {
      super(conversionService);
   }

   @Override
   public Class<CookieValue> getAnnotationType() {
      return CookieValue.class;
   }

   public ArgumentBinder.BindingResult<T> bind(ArgumentConversionContext<T> argument, HttpRequest<?> source) {
      ConvertibleValues<Cookie> parameters = source.getCookies();
      AnnotationMetadata annotationMetadata = argument.getAnnotationMetadata();
      String parameterName = (String)annotationMetadata.stringValue(CookieValue.class).orElse(argument.getArgument().getName());
      return this.doBind(argument, parameters, parameterName);
   }

   @Override
   protected String getFallbackFormat(Argument argument) {
      return NameUtils.hyphenate(argument.getName());
   }
}

package io.micronaut.http.client.bind.binders;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.client.bind.AnnotatedClientRequestBinder;
import io.micronaut.http.client.bind.ClientRequestUriContext;

public class HeaderClientRequestBinder implements AnnotatedClientRequestBinder<Header> {
   @Override
   public void bind(
      @NonNull MethodInvocationContext<Object, Object> context, @NonNull ClientRequestUriContext uriContext, @NonNull MutableHttpRequest<?> request
   ) {
      for(AnnotationValue<Header> headerAnnotation : context.getAnnotationValuesByType(Header.class)) {
         String headerName = (String)headerAnnotation.stringValue("name").orElse(null);
         String headerValue = (String)headerAnnotation.stringValue().orElse(null);
         MutableHttpHeaders headers = request.getHeaders();
         if (StringUtils.isNotEmpty(headerName) && StringUtils.isNotEmpty(headerValue) && !headers.contains(headerName)) {
            headers.set(headerName, headerValue);
         }
      }

   }

   @NonNull
   @Override
   public Class<Header> getAnnotationType() {
      return Header.class;
   }
}

package io.micronaut.http.client.bind.binders;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.RequestAttribute;
import io.micronaut.http.client.bind.AnnotatedClientRequestBinder;
import io.micronaut.http.client.bind.ClientRequestUriContext;

public class AttributeClientRequestBinder implements AnnotatedClientRequestBinder<RequestAttribute> {
   @Override
   public void bind(
      @NonNull MethodInvocationContext<Object, Object> context, @NonNull ClientRequestUriContext uriContext, @NonNull MutableHttpRequest<?> request
   ) {
      for(AnnotationValue<RequestAttribute> attributeAnnotation : context.getAnnotationValuesByType(RequestAttribute.class)) {
         String attributeName = (String)attributeAnnotation.stringValue("name").orElse(null);
         Object attributeValue = attributeAnnotation.getValue(Object.class).orElse(null);
         if (StringUtils.isNotEmpty(attributeName) && attributeValue != null) {
            request.setAttribute(attributeName, attributeValue);
         }
      }

   }

   @NonNull
   @Override
   public Class<RequestAttribute> getAnnotationType() {
      return RequestAttribute.class;
   }
}

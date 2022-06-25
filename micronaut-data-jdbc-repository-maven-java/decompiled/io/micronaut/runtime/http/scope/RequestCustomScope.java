package io.micronaut.runtime.http.scope;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.scope.AbstractConcurrentCustomScope;
import io.micronaut.context.scope.BeanCreationContext;
import io.micronaut.context.scope.CreatedBean;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.http.context.event.HttpRequestTerminatedEvent;
import io.micronaut.inject.BeanIdentifier;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
class RequestCustomScope extends AbstractConcurrentCustomScope<RequestScope> implements ApplicationEventListener<HttpRequestTerminatedEvent> {
   public static final String SCOPED_BEANS_ATTRIBUTE = "io.micronaut.http.SCOPED_BEANS";

   public RequestCustomScope() {
      super(RequestScope.class);
   }

   @Override
   public void close() {
      ServerRequestContext.currentRequest().ifPresent(this::destroyBeans);
   }

   @Override
   public boolean isRunning() {
      return ServerRequestContext.currentRequest().isPresent();
   }

   public void onApplicationEvent(HttpRequestTerminatedEvent event) {
      this.destroyBeans(event.getSource());
   }

   @NonNull
   @Override
   protected Map<BeanIdentifier, CreatedBean<?>> getScopeMap(boolean forCreation) {
      HttpRequest<Object> request = (HttpRequest)ServerRequestContext.currentRequest().orElse(null);
      if (request != null) {
         return this.getRequestAttributeMap(request, forCreation);
      } else {
         throw new IllegalStateException("No request present");
      }
   }

   @NonNull
   @Override
   protected <T> CreatedBean<T> doCreate(@NonNull BeanCreationContext<T> creationContext) {
      HttpRequest<Object> request = (HttpRequest)ServerRequestContext.currentRequest().orElse(null);
      CreatedBean<T> createdBean = super.doCreate(creationContext);
      T bean = createdBean.bean();
      if (bean instanceof RequestAware) {
         ((RequestAware)bean).setRequest(request);
      }

      return createdBean;
   }

   private void destroyBeans(HttpRequest<?> request) {
      ArgumentUtils.requireNonNull("request", request);
      ConcurrentHashMap<BeanIdentifier, CreatedBean<?>> requestScopedBeans = this.getRequestAttributeMap(request, false);
      if (requestScopedBeans != null) {
         this.destroyScope(requestScopedBeans);
      }

   }

   private <T> ConcurrentHashMap<BeanIdentifier, CreatedBean<?>> getRequestAttributeMap(HttpRequest<T> httpRequest, boolean create) {
      MutableConvertibleValues<Object> attrs = httpRequest.getAttributes();
      Object o = attrs.getValue("io.micronaut.http.SCOPED_BEANS");
      if (o instanceof ConcurrentHashMap) {
         return (ConcurrentHashMap<BeanIdentifier, CreatedBean<?>>)o;
      } else if (create) {
         ConcurrentHashMap<BeanIdentifier, CreatedBean<?>> scopedBeans = new ConcurrentHashMap(5);
         attrs.put("io.micronaut.http.SCOPED_BEANS", scopedBeans);
         return scopedBeans;
      } else {
         return null;
      }
   }
}

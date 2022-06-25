package io.micronaut.http.filter;

import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import org.reactivestreams.Publisher;

@Deprecated
public abstract class OncePerRequestHttpServerFilter implements HttpServerFilter {
   @Override
   public final Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
      String attributeKey = getKey(this.getClass());
      MutableConvertibleValues<Object> attrs = request.getAttributes();
      attrs.put(attributeKey, true);
      return this.doFilterOnce(request, chain);
   }

   @Deprecated
   public static String getKey(Class<? extends OncePerRequestHttpServerFilter> filterClass) {
      return "micronaut.once." + filterClass.getSimpleName();
   }

   protected abstract Publisher<MutableHttpResponse<?>> doFilterOnce(HttpRequest<?> request, ServerFilterChain chain);
}

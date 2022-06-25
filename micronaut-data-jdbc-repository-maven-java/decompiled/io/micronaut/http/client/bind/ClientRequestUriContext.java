package io.micronaut.http.client.bind;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.uri.UriMatchTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientRequestUriContext {
   private final Map<String, Object> pathParameters;
   private final Map<String, List<String>> queryParameters;
   private final UriMatchTemplate uriTemplate;

   @Internal
   public ClientRequestUriContext(UriMatchTemplate uriTemplate, Map<String, Object> pathParameters, Map<String, List<String>> queryParameters) {
      this.uriTemplate = uriTemplate;
      this.pathParameters = pathParameters;
      this.queryParameters = queryParameters;
   }

   public UriMatchTemplate getUriTemplate() {
      return this.uriTemplate;
   }

   public Map<String, Object> getPathParameters() {
      return this.pathParameters;
   }

   public Map<String, List<String>> getQueryParameters() {
      return this.queryParameters;
   }

   public void addQueryParameter(String name, String value) {
      List<String> values = (List)this.queryParameters.computeIfAbsent(name, k -> new ArrayList());
      values.add(value);
   }

   public void setQueryParameter(String name, List<String> values) {
      this.queryParameters.put(name, values);
   }

   public void setPathParameter(String name, Object value) {
      this.pathParameters.put(name, value);
   }
}

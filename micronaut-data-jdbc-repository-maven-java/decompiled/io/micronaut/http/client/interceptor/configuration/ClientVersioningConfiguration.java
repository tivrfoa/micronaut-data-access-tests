package io.micronaut.http.client.interceptor.configuration;

import io.micronaut.context.annotation.Parameter;
import java.util.ArrayList;
import java.util.List;

public class ClientVersioningConfiguration {
   public static final String PREFIX = "micronaut.http.client.versioning";
   public static final String DEFAULT = "default";
   private List<String> headerNames = new ArrayList();
   private List<String> parameterNames = new ArrayList();
   private final String clientName;

   ClientVersioningConfiguration(@Parameter String clientName) {
      this.clientName = clientName;
   }

   public String getClientName() {
      return this.clientName;
   }

   public List<String> getHeaders() {
      return this.headerNames;
   }

   public List<String> getParameters() {
      return this.parameterNames;
   }

   public void setHeaders(List<String> headerNames) {
      this.headerNames = headerNames;
   }

   public void setParameters(List<String> parameterNames) {
      this.parameterNames = parameterNames;
   }
}

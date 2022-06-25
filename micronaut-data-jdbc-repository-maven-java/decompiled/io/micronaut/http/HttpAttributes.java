package io.micronaut.http;

public enum HttpAttributes implements CharSequence {
   PRINCIPAL("micronaut.AUTHENTICATION"),
   ERROR("micronaut.http.error"),
   ROUTE("micronaut.http.route"),
   ROUTE_MATCH("micronaut.http.route.match"),
   ROUTE_INFO("micronaut.http.route.info"),
   URI_TEMPLATE("micronaut.http.route.template"),
   METHOD_NAME("micronaut.http.method.name"),
   SERVICE_ID("micronaut.http.serviceId"),
   MEDIA_TYPE_CODEC("micronaut.http.mediaType.codec"),
   INVOCATION_CONTEXT("micronaut.http.invocationContext"),
   EXCEPTION("micronaut.http.exception"),
   X509_CERTIFICATE("javax.servlet.request.X509Certificate"),
   AVAILABLE_HTTP_METHODS("micronaut.http.route.availableHttpMethods");

   private final String name;

   private HttpAttributes(String name) {
      this.name = name;
   }

   public int length() {
      return this.name.length();
   }

   public char charAt(int index) {
      return this.name.charAt(index);
   }

   public CharSequence subSequence(int start, int end) {
      return this.name.subSequence(start, end);
   }

   public String toString() {
      return this.name;
   }

   private static class Constants {
      public static final String PREFIX = "micronaut.http";
   }
}

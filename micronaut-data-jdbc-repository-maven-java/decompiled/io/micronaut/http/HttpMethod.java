package io.micronaut.http;

public enum HttpMethod implements CharSequence {
   OPTIONS,
   GET,
   HEAD,
   POST,
   PUT,
   DELETE,
   TRACE,
   CONNECT,
   PATCH,
   CUSTOM;

   public int length() {
      return this.name().length();
   }

   public char charAt(int index) {
      return this.name().charAt(index);
   }

   public CharSequence subSequence(int start, int end) {
      return this.name().subSequence(start, end);
   }

   public static boolean requiresRequestBody(HttpMethod method) {
      return method != null && (method.equals(POST) || method.equals(PUT) || method.equals(PATCH));
   }

   public static boolean permitsRequestBody(HttpMethod method) {
      return method != null && (requiresRequestBody(method) || method.equals(OPTIONS) || method.equals(DELETE) || method.equals(CUSTOM));
   }

   public static HttpMethod parse(String httpMethodName) {
      HttpMethod httpMethod = parseString(httpMethodName);
      if (httpMethod != null) {
         return httpMethod;
      } else {
         httpMethodName = httpMethodName.toUpperCase();
         httpMethod = parseString(httpMethodName);
         return httpMethod != null ? httpMethod : CUSTOM;
      }
   }

   private static HttpMethod parseString(String httpMethodName) {
      switch(httpMethodName) {
         case "OPTIONS":
         case "options":
            return OPTIONS;
         case "GET":
         case "get":
            return GET;
         case "HEAD":
         case "head":
            return HEAD;
         case "POST":
         case "post":
            return POST;
         case "PUT":
         case "put":
            return PUT;
         case "DELETE":
         case "delete":
            return DELETE;
         case "TRACE":
         case "trace":
            return TRACE;
         case "CONNECT":
         case "connect":
            return CONNECT;
         case "PATCH":
         case "patch":
            return PATCH;
         default:
            return null;
      }
   }
}

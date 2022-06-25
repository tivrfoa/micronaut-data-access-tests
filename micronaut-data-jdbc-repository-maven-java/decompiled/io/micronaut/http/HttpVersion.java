package io.micronaut.http;

public enum HttpVersion {
   HTTP_1_0,
   HTTP_1_1,
   HTTP_2_0;

   public static HttpVersion valueOf(double v) {
      if (v == 1.0) {
         return HTTP_1_0;
      } else if (v == 1.1) {
         return HTTP_1_1;
      } else if (v == 2.0) {
         return HTTP_2_0;
      } else {
         throw new IllegalArgumentException("Invalid HTTP version: " + v);
      }
   }
}

package io.netty.handler.codec.http;

import io.netty.util.AsciiString;

public enum HttpStatusClass {
   INFORMATIONAL(100, 200, "Informational"),
   SUCCESS(200, 300, "Success"),
   REDIRECTION(300, 400, "Redirection"),
   CLIENT_ERROR(400, 500, "Client Error"),
   SERVER_ERROR(500, 600, "Server Error"),
   UNKNOWN(0, 0, "Unknown Status") {
      @Override
      public boolean contains(int code) {
         return code < 100 || code >= 600;
      }
   };

   private final int min;
   private final int max;
   private final AsciiString defaultReasonPhrase;

   public static HttpStatusClass valueOf(int code) {
      if (INFORMATIONAL.contains(code)) {
         return INFORMATIONAL;
      } else if (SUCCESS.contains(code)) {
         return SUCCESS;
      } else if (REDIRECTION.contains(code)) {
         return REDIRECTION;
      } else if (CLIENT_ERROR.contains(code)) {
         return CLIENT_ERROR;
      } else {
         return SERVER_ERROR.contains(code) ? SERVER_ERROR : UNKNOWN;
      }
   }

   public static HttpStatusClass valueOf(CharSequence code) {
      if (code != null && code.length() == 3) {
         char c0 = code.charAt(0);
         return isDigit(c0) && isDigit(code.charAt(1)) && isDigit(code.charAt(2)) ? valueOf(digit(c0) * 100) : UNKNOWN;
      } else {
         return UNKNOWN;
      }
   }

   private static int digit(char c) {
      return c - 48;
   }

   private static boolean isDigit(char c) {
      return c >= '0' && c <= '9';
   }

   private HttpStatusClass(int min, int max, String defaultReasonPhrase) {
      this.min = min;
      this.max = max;
      this.defaultReasonPhrase = AsciiString.cached(defaultReasonPhrase);
   }

   public boolean contains(int code) {
      return code >= this.min && code < this.max;
   }

   AsciiString defaultReasonPhrase() {
      return this.defaultReasonPhrase;
   }
}

package org.flywaydb.core.internal.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

public class UrlUtils {
   public static String toFilePath(URL url) {
      String filePath = new File(decodeURL(url.getPath().replace("+", "%2b"))).getAbsolutePath();
      return filePath.endsWith("/") ? filePath.substring(0, filePath.length() - 1) : filePath;
   }

   public static String decodeURL(String url) {
      try {
         return URLDecoder.decode(url, "UTF-8");
      } catch (UnsupportedEncodingException var2) {
         throw new IllegalStateException("Can never happen", var2);
      }
   }

   private UrlUtils() {
   }
}

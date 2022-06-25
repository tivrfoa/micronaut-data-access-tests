package io.micronaut.http.util;

import io.micronaut.http.HttpMessage;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Optional;

public class HttpUtil {
   public static boolean isFormData(HttpRequest<?> request) {
      Optional<MediaType> opt = request.getContentType();
      if (!opt.isPresent()) {
         return false;
      } else {
         MediaType contentType = (MediaType)opt.get();
         return contentType.equals(MediaType.APPLICATION_FORM_URLENCODED_TYPE) || contentType.equals(MediaType.MULTIPART_FORM_DATA_TYPE);
      }
   }

   public static Optional<Charset> resolveCharset(HttpMessage<?> request) {
      try {
         Optional<Charset> contentTypeCharset = request.getContentType().map(contentType -> {
            Optional<String> charset = contentType.getParameters().get("charset");
            if (charset.isPresent()) {
               try {
                  return Charset.forName((String)charset.get());
               } catch (Exception var3) {
                  return StandardCharsets.UTF_8;
               }
            } else {
               return null;
            }
         });
         return contentTypeCharset.isPresent() ? contentTypeCharset : request.getHeaders().findFirst("Accept-Charset").map(text -> {
            int len = text.length();
            if (len != 0 && (len != 1 || text.charAt(0) != '*')) {
               if (text.indexOf(59) > -1) {
                  text = text.split(";")[0];
               }

               if (text.indexOf(44) > -1) {
                  text = text.split(",")[0];
               }

               try {
                  return Charset.forName(text);
               } catch (Exception var3) {
                  return StandardCharsets.UTF_8;
               }
            } else {
               return StandardCharsets.UTF_8;
            }
         });
      } catch (UnsupportedCharsetException var2) {
         return Optional.empty();
      }
   }
}

package io.micronaut.http.server.types.files;

import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.server.types.CustomizableResponseTypeException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;

public class StreamedFile implements FileCustomizableResponseType {
   private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
   private final MediaType mediaType;
   private final String name;
   private final long lastModified;
   private final InputStream inputStream;
   private final long length;
   private String attachmentName;

   public StreamedFile(InputStream inputStream, MediaType mediaType) {
      this(inputStream, mediaType, Instant.now().toEpochMilli());
   }

   public StreamedFile(InputStream inputStream, MediaType mediaType, long lastModified) {
      this(inputStream, mediaType, lastModified, -1L);
   }

   public StreamedFile(InputStream inputStream, MediaType mediaType, long lastModified, long contentLength) {
      this.mediaType = mediaType;
      this.name = null;
      this.lastModified = lastModified;
      this.inputStream = inputStream;
      this.length = contentLength;
   }

   public StreamedFile(URL url) {
      String path = url.getPath();
      int idx = path.lastIndexOf(File.separatorChar);
      this.name = idx > -1 ? path.substring(idx + 1) : path;
      this.mediaType = MediaType.forFilename(this.name);

      try {
         URLConnection con = url.openConnection();
         this.lastModified = con.getLastModified();
         this.inputStream = con.getInputStream();
         this.length = con.getContentLengthLong();
      } catch (IOException var5) {
         throw new CustomizableResponseTypeException("Could not open a connection to the URL: " + path, var5);
      }
   }

   @Override
   public long getLastModified() {
      return this.lastModified;
   }

   @Override
   public long getLength() {
      return this.length;
   }

   @Override
   public MediaType getMediaType() {
      return this.mediaType;
   }

   public InputStream getInputStream() {
      return this.inputStream;
   }

   public StreamedFile attach(String attachmentName) {
      this.attachmentName = attachmentName;
      return this;
   }

   @Override
   public void process(MutableHttpResponse<?> response) {
      if (this.attachmentName != null) {
         response.header("Content-Disposition", buildAttachmentHeader(this.attachmentName));
      }

   }

   static String buildAttachmentHeader(String attachmentName) {
      return "attachment; filename=\"" + sanitizeAscii(attachmentName) + "\"; filename*=utf-8''" + encodeRfc6987(attachmentName);
   }

   private static String sanitizeAscii(String s) {
      StringBuilder builder = new StringBuilder(s.length());

      for(int i = 0; i < s.length(); ++i) {
         char c = s.charAt(i);
         if (c >= ' ' && c < 127 && c != '"') {
            builder.append(c);
         }
      }

      return builder.toString();
   }

   static String encodeRfc6987(String s) {
      StringBuilder uriBuilder = new StringBuilder();

      for(int i = 0; i < s.length(); ++i) {
         char c = s.charAt(i);
         if (c < 128) {
            if (dontNeedEncoding(c)) {
               uriBuilder.append(c);
            } else {
               appendEncoded(uriBuilder, c);
            }
         } else if (c < 2048) {
            appendEncoded(uriBuilder, 192 | c >> 6);
            appendEncoded(uriBuilder, 128 | c & '?');
         } else if (Character.isSurrogate(c)) {
            if (!Character.isHighSurrogate(c)) {
               appendEncoded(uriBuilder, 63);
            } else {
               if (++i == s.length()) {
                  appendEncoded(uriBuilder, 63);
                  break;
               }

               writeUtf8Surrogate(uriBuilder, c, s.charAt(i));
            }
         } else {
            appendEncoded(uriBuilder, 224 | c >> 12);
            appendEncoded(uriBuilder, 128 | c >> 6 & 63);
            appendEncoded(uriBuilder, 128 | c & '?');
         }
      }

      return uriBuilder.toString();
   }

   private static boolean dontNeedEncoding(char ch) {
      return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9' || ch == '-' || ch == '_' || ch == '.' || ch == '*' || ch == '~';
   }

   private static void appendEncoded(StringBuilder uriBuilder, int b) {
      uriBuilder.append('%').append(HEX_DIGITS[b >> 4 & 15]).append(HEX_DIGITS[b & 15]);
   }

   private static void writeUtf8Surrogate(StringBuilder uriBuilder, char c, char c2) {
      if (!Character.isLowSurrogate(c2)) {
         appendEncoded(uriBuilder, 63);
         appendEncoded(uriBuilder, Character.isHighSurrogate(c2) ? 63 : c2);
      } else {
         int codePoint = Character.toCodePoint(c, c2);
         appendEncoded(uriBuilder, 240 | codePoint >> 18);
         appendEncoded(uriBuilder, 128 | codePoint >> 12 & 63);
         appendEncoded(uriBuilder, 128 | codePoint >> 6 & 63);
         appendEncoded(uriBuilder, 128 | codePoint & 63);
      }
   }
}

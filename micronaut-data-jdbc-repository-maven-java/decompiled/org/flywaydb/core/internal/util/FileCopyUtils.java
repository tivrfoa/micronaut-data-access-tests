package org.flywaydb.core.internal.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

public class FileCopyUtils {
   public static String copyToString(Reader in) throws IOException {
      StringWriter out = new StringWriter();
      copy(in, out);
      String str = out.toString();
      return str.startsWith("\ufeff") ? str.substring(1) : str;
   }

   public static String copyToString(InputStream in, Charset encoding) throws IOException {
      ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
      copy(in, out);
      return out.toString(encoding.name());
   }

   public static void copy(Reader in, Writer out) throws IOException {
      try {
         char[] buffer = new char[4096];

         int bytesRead;
         while((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
         }

         out.flush();
      } finally {
         IOUtils.close(in);
         IOUtils.close(out);
      }

   }

   public static int copy(InputStream in, OutputStream out) throws IOException {
      int var5;
      try {
         int byteCount = 0;

         int bytesRead;
         for(byte[] buffer = new byte[4096]; (bytesRead = in.read(buffer)) != -1; byteCount += bytesRead) {
            out.write(buffer, 0, bytesRead);
         }

         out.flush();
         var5 = byteCount;
      } finally {
         IOUtils.close(in);
         IOUtils.close(out);
      }

      return var5;
   }

   private FileCopyUtils() {
   }
}

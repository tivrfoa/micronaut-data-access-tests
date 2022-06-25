package io.micronaut.core.io;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.StringUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

@Internal
class UrlReadable implements Readable {
   private final URL url;

   UrlReadable(URL url) {
      ArgumentUtils.requireNonNull("url", url);
      this.url = url;
   }

   @NonNull
   @Override
   public InputStream asInputStream() throws IOException {
      URLConnection con = this.url.openConnection();
      con.setUseCaches(true);

      try {
         return con.getInputStream();
      } catch (IOException var3) {
         if (con instanceof HttpURLConnection) {
            ((HttpURLConnection)con).disconnect();
         }

         throw var3;
      }
   }

   @Override
   public boolean exists() {
      try {
         String protocol = this.url.getProtocol();
         if (!StringUtils.isNotEmpty(protocol) || !"file".equalsIgnoreCase(protocol) && !protocol.startsWith("vfs")) {
            URLConnection con = this.url.openConnection();
            con.setUseCaches(true);
            boolean isHttp = con instanceof HttpURLConnection;
            if (isHttp) {
               HttpURLConnection httpURLConnection = (HttpURLConnection)con;
               httpURLConnection.setRequestMethod("HEAD");
               int code = httpURLConnection.getResponseCode();
               if (code == 200) {
                  return true;
               }

               if (code == 404) {
                  return false;
               }
            }

            if (con.getContentLengthLong() >= 0L) {
               return true;
            } else if (isHttp) {
               ((HttpURLConnection)con).disconnect();
               return false;
            } else {
               this.asInputStream().close();
               return true;
            }
         } else {
            try {
               return new File(this.url.toURI().getSchemeSpecificPart()).exists();
            } catch (URISyntaxException var6) {
               return new File(this.url.getFile()).exists();
            }
         }
      } catch (IOException var7) {
         return false;
      }
   }

   @Override
   public String getName() {
      return this.url.getPath();
   }
}

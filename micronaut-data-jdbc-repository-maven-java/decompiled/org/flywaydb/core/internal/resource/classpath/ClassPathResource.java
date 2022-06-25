package org.flywaydb.core.internal.resource.classpath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Objects;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.util.UrlUtils;

public class ClassPathResource extends LoadableResource {
   private static final Log LOG = LogFactory.getLog(ClassPathResource.class);
   private final String fileNameWithAbsolutePath;
   private final String fileNameWithRelativePath;
   private final ClassLoader classLoader;
   private final Charset encoding;
   private final boolean detectEncoding;
   private final String parentURL;

   public ClassPathResource(Location location, String fileNameWithAbsolutePath, ClassLoader classLoader, Charset encoding) {
      this(location, fileNameWithAbsolutePath, classLoader, encoding, false, "");
   }

   public ClassPathResource(Location location, String fileNameWithAbsolutePath, ClassLoader classLoader, Charset encoding, String parentURL) {
      this(location, fileNameWithAbsolutePath, classLoader, encoding, false, parentURL);
   }

   public ClassPathResource(
      Location location, String fileNameWithAbsolutePath, ClassLoader classLoader, Charset encoding, Boolean detectEncoding, String parentURL
   ) {
      this.fileNameWithAbsolutePath = fileNameWithAbsolutePath;
      this.fileNameWithRelativePath = location == null ? fileNameWithAbsolutePath : location.getPathRelativeToThis(fileNameWithAbsolutePath);
      this.classLoader = classLoader;
      this.encoding = encoding;
      this.detectEncoding = detectEncoding;
      this.parentURL = parentURL;
   }

   @Override
   public String getRelativePath() {
      return this.fileNameWithRelativePath;
   }

   @Override
   public String getAbsolutePath() {
      return this.fileNameWithAbsolutePath;
   }

   @Override
   public String getAbsolutePathOnDisk() {
      URL url = this.getUrl();
      if (url == null) {
         throw new FlywayException("Unable to find resource on disk: " + this.fileNameWithAbsolutePath);
      } else {
         return new File(UrlUtils.decodeURL(url.getPath())).getAbsolutePath();
      }
   }

   private URL getUrl() {
      try {
         Enumeration<URL> urls = this.classLoader.getResources(this.fileNameWithAbsolutePath);

         while(urls.hasMoreElements()) {
            URL url = (URL)urls.nextElement();
            if (url.getPath() != null && url.getPath().contains(this.parentURL)) {
               return url;
            }
         }

         return null;
      } catch (IOException var3) {
         throw new FlywayException(var3);
      }
   }

   @Override
   public Reader read() {
      InputStream inputStream = null;

      try {
         Enumeration<URL> urls = this.classLoader.getResources(this.fileNameWithAbsolutePath);

         while(urls.hasMoreElements()) {
            URL url = (URL)urls.nextElement();
            if (url.getPath() != null && url.getPath().contains(this.parentURL)) {
               inputStream = url.openStream();
               break;
            }
         }
      } catch (IOException var4) {
         throw new FlywayException(var4);
      }

      if (inputStream == null) {
         throw new FlywayException("Unable to obtain inputstream for resource: " + this.fileNameWithAbsolutePath);
      } else {
         Charset charset = this.encoding;
         return new InputStreamReader(inputStream, charset.newDecoder());
      }
   }

   @Override
   public String getFilename() {
      return this.fileNameWithAbsolutePath.substring(this.fileNameWithAbsolutePath.lastIndexOf("/") + 1);
   }

   public boolean exists() {
      return this.getUrl() != null;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ClassPathResource that = (ClassPathResource)o;
         return this.fileNameWithAbsolutePath.equals(that.fileNameWithAbsolutePath) && this.parentURL.equals(that.parentURL);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.fileNameWithAbsolutePath, this.parentURL});
   }
}

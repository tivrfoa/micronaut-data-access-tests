package org.flywaydb.core.internal.scanner.classpath;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

public class JarFileClassPathLocationScanner implements ClassPathLocationScanner {
   private static final Log LOG = LogFactory.getLog(JarFileClassPathLocationScanner.class);
   private final String separator;

   @Override
   public Set<String> findResourceNames(String location, URL locationUrl) {
      JarFile jarFile;
      try {
         jarFile = this.getJarFromUrl(locationUrl);
      } catch (IOException var15) {
         LOG.warn("Unable to determine jar from url (" + locationUrl + "): " + var15.getMessage());
         return Collections.emptySet();
      }

      Set var5;
      try {
         String prefix = jarFile.getName().toLowerCase(Locale.ENGLISH).endsWith(".war") ? "WEB-INF/classes/" : "";
         var5 = this.findResourceNamesFromJarFile(jarFile, prefix, location);
      } finally {
         try {
            jarFile.close();
         } catch (IOException var13) {
         }

      }

      return var5;
   }

   private JarFile getJarFromUrl(URL locationUrl) throws IOException {
      URLConnection con = locationUrl.openConnection();
      if (con instanceof JarURLConnection) {
         JarURLConnection jarCon = (JarURLConnection)con;
         jarCon.setUseCaches(false);
         return jarCon.getJarFile();
      } else {
         String urlFile = locationUrl.getFile();
         int separatorIndex = urlFile.indexOf(this.separator);
         if (separatorIndex != -1) {
            String jarFileUrl = urlFile.substring(0, separatorIndex);
            if (jarFileUrl.startsWith("file:")) {
               try {
                  return new JarFile(new URL(jarFileUrl).toURI().getSchemeSpecificPart());
               } catch (URISyntaxException var7) {
                  return new JarFile(jarFileUrl.substring("file:".length()));
               }
            } else {
               return new JarFile(jarFileUrl);
            }
         } else {
            return new JarFile(urlFile);
         }
      }
   }

   private Set<String> findResourceNamesFromJarFile(JarFile jarFile, String prefix, String location) {
      String toScan = prefix + location + (location.endsWith("/") ? "" : "/");
      Set<String> resourceNames = new TreeSet();
      Enumeration<JarEntry> entries = jarFile.entries();

      while(entries.hasMoreElements()) {
         String entryName = ((JarEntry)entries.nextElement()).getName();
         if (entryName.startsWith(toScan)) {
            resourceNames.add(entryName.substring(prefix.length()));
         }
      }

      return resourceNames;
   }

   JarFileClassPathLocationScanner(String separator) {
      this.separator = separator;
   }
}

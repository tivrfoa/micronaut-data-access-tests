package org.flywaydb.core.internal.scanner;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.scanner.classpath.ClassPathScanner;

public class ClasspathClassScanner {
   private final ResourceNameCache resourceNameCache = new ResourceNameCache();
   private final LocationScannerCache locationScannerCache = new LocationScannerCache();
   private final ClassLoader classLoader;

   public ClasspathClassScanner(ClassLoader classLoader) {
      this.classLoader = classLoader;
   }

   public List<String> scanForType(String location, Class<?> classType, boolean errorOnNotFound) {
      ClassPathScanner<?> s = new ClassPathScanner(
         classType,
         this.classLoader,
         Charset.defaultCharset(),
         new Location("classpath:" + location),
         this.resourceNameCache,
         this.locationScannerCache,
         errorOnNotFound
      );
      List<String> discoveredTypes = new ArrayList();

      for(LoadableResource resource : s.scanForResources()) {
         if (resource.getAbsolutePath().endsWith(".class")) {
            discoveredTypes.add(this.toClassName(resource.getAbsolutePath()));
         }
      }

      return discoveredTypes;
   }

   private String toClassName(String resourceName) {
      String nameWithDots = resourceName.replace("/", ".");
      return nameWithDots.substring(0, nameWithDots.length() - ".class".length());
   }
}

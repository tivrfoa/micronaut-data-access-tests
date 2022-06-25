package io.micronaut.flyway.graalvm;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import io.micronaut.core.annotation.Internal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.scanner.LocationScannerCache;
import org.flywaydb.core.internal.scanner.ResourceNameCache;
import org.flywaydb.core.internal.scanner.classpath.ResourceAndClassScanner;

@Internal
@TargetClass(
   className = "org.flywaydb.core.internal.scanner.Scanner"
)
final class ScannerSubstitutions {
   @Alias
   private List<LoadableResource> resources = new ArrayList();
   @Alias
   private List<Class<?>> classes = new ArrayList();
   @Alias
   private HashMap<String, LoadableResource> relativeResourceMap = new HashMap();

   @Substitute
   public ScannerSubstitutions(
      Class<?> implementedInterface,
      Collection<Location> locations,
      ClassLoader classLoader,
      Charset encoding,
      boolean detectEncoding,
      boolean stream,
      ResourceNameCache resourceNameCache,
      LocationScannerCache locationScannerCache,
      boolean throwOnMissingLocations
   ) {
      ResourceAndClassScanner scanner = new MicronautPathLocationScanner(locations);
      Collection resources = scanner.scanForResources();
      this.resources.addAll(resources);
      Collection scanForClasses = scanner.scanForClasses();
      this.classes.addAll(scanForClasses);

      for(LoadableResource resource : this.resources) {
         this.relativeResourceMap.put(resource.getRelativePath().toLowerCase(), resource);
      }

   }
}

package io.micronaut.flyway.graalvm;

import io.micronaut.context.condition.Condition;
import io.micronaut.core.annotation.Internal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.resource.classpath.ClassPathResource;
import org.flywaydb.core.internal.scanner.classpath.ResourceAndClassScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
final class MicronautPathLocationScanner implements ResourceAndClassScanner {
   private static final Logger LOG = LoggerFactory.getLogger(Condition.class);
   private static final String LOCATION_SEPARATOR = "/";
   private static List<String> applicationMigrationFiles;
   private final Collection<LoadableResource> scannedResources = new ArrayList();

   public MicronautPathLocationScanner(Collection<Location> locations) {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      for(String migrationFile : applicationMigrationFiles) {
         if (this.canHandleMigrationFile(locations, migrationFile)) {
            LOG.debug("Loading %{}", migrationFile);
            this.scannedResources.add(new ClassPathResource(null, migrationFile, classLoader, StandardCharsets.UTF_8));
         }
      }

   }

   @Override
   public Collection<LoadableResource> scanForResources() {
      return this.scannedResources;
   }

   @Override
   public Collection<Class<?>> scanForClasses() {
      return Collections.emptyList();
   }

   public static void setApplicationMigrationFiles(List<String> applicationMigrationFiles) {
      MicronautPathLocationScanner.applicationMigrationFiles = applicationMigrationFiles;
   }

   private boolean canHandleMigrationFile(Collection<Location> locations, String migrationFile) {
      for(Location location : locations) {
         String locationPath = location.getPath();
         if (!locationPath.endsWith("/")) {
            locationPath = locationPath + "/";
         }

         if (migrationFile.startsWith(locationPath)) {
            return true;
         }

         LOG.debug("Migration file '{}' will be ignored because it does not start with '{}'", migrationFile, locationPath);
      }

      return false;
   }
}

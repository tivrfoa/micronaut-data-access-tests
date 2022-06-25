package org.flywaydb.core.internal.scanner;

import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.flywaydb.core.api.ClassProvider;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException;
import org.flywaydb.core.internal.scanner.classpath.ClassPathScanner;
import org.flywaydb.core.internal.scanner.classpath.ResourceAndClassScanner;
import org.flywaydb.core.internal.scanner.cloud.s3.AwsS3Scanner;
import org.flywaydb.core.internal.scanner.filesystem.FileSystemScanner;
import org.flywaydb.core.internal.util.FeatureDetector;
import org.flywaydb.core.internal.util.StringUtils;

public class Scanner<I> implements ResourceProvider, ClassProvider<I> {
   private static final Log LOG = LogFactory.getLog(Scanner.class);
   private final List<LoadableResource> resources = new ArrayList();
   private final List<Class<? extends I>> classes = new ArrayList();
   private final HashMap<String, LoadableResource> relativeResourceMap = new HashMap();
   private HashMap<String, LoadableResource> absoluteResourceMap = null;

   public Scanner(
      Class<I> implementedInterface,
      Collection<Location> locations,
      ClassLoader classLoader,
      Charset encoding,
      boolean detectEncoding,
      boolean stream,
      ResourceNameCache resourceNameCache,
      LocationScannerCache locationScannerCache,
      boolean throwOnMissingLocations
   ) {
      FileSystemScanner fileSystemScanner = new FileSystemScanner(encoding, stream, detectEncoding, throwOnMissingLocations);
      FeatureDetector detector = new FeatureDetector(classLoader);
      boolean aws = detector.isAwsAvailable();
      boolean gcs = detector.isGCSAvailable();
      long cloudMigrationCount = 0L;

      for(Location location : locations) {
         if (location.isFileSystem()) {
            this.resources.addAll(fileSystemScanner.scanForResources(location));
         } else {
            if (location.isGCS()) {
               throw new FlywayTeamsUpgradeRequiredException("Google Cloud Storage");
            }

            if (location.isAwsS3()) {
               if (aws) {
                  Collection<LoadableResource> awsResources = new AwsS3Scanner(encoding, throwOnMissingLocations).scanForResources(location);
                  this.resources.addAll(awsResources);
                  cloudMigrationCount += awsResources.stream().filter(r -> r.getFilename().endsWith(".sql")).count();
               } else {
                  LOG.error("Can't read location " + location + "; AWS SDK not found");
               }
            } else {
               ResourceAndClassScanner<I> resourceAndClassScanner = new ClassPathScanner<>(
                  implementedInterface, classLoader, encoding, location, resourceNameCache, locationScannerCache, throwOnMissingLocations
               );
               this.resources.addAll(resourceAndClassScanner.scanForResources());
               this.classes.addAll(resourceAndClassScanner.scanForClasses());
            }
         }
      }

      if (cloudMigrationCount > 100L) {
         throw new FlywayTeamsUpgradeRequiredException("Cloud locations with more than 100 migrations");
      } else {
         for(LoadableResource resource : this.resources) {
            this.relativeResourceMap.put(resource.getRelativePath().toLowerCase(), resource);
         }

      }
   }

   @Override
   public LoadableResource getResource(String name) {
      LoadableResource loadedResource = (LoadableResource)this.relativeResourceMap.get(name.toLowerCase());
      if (loadedResource != null) {
         return loadedResource;
      } else {
         if (Paths.get(name).isAbsolute()) {
            if (this.absoluteResourceMap == null) {
               this.absoluteResourceMap = new HashMap();

               for(LoadableResource resource : this.resources) {
                  this.absoluteResourceMap.put(resource.getAbsolutePathOnDisk().toLowerCase(), resource);
               }
            }

            loadedResource = (LoadableResource)this.absoluteResourceMap.get(name.toLowerCase());
            if (loadedResource != null) {
               return loadedResource;
            }
         }

         return null;
      }
   }

   @Override
   public Collection<LoadableResource> getResources(String prefix, String... suffixes) {
      List<LoadableResource> result = new ArrayList();

      for(LoadableResource resource : this.resources) {
         String fileName = resource.getFilename();
         if (StringUtils.startsAndEndsWith(fileName, prefix, suffixes)) {
            result.add(resource);
         } else {
            LOG.debug("Filtering out resource: " + resource.getAbsolutePath() + " (filename: " + fileName + ")");
         }
      }

      return result;
   }

   @Override
   public Collection<Class<? extends I>> getClasses() {
      return Collections.unmodifiableCollection(this.classes);
   }
}

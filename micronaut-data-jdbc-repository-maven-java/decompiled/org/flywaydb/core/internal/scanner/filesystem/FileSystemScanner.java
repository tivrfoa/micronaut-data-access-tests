package org.flywaydb.core.internal.scanner.filesystem;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.resource.filesystem.FileSystemResource;
import org.flywaydb.core.internal.sqlscript.SqlScriptMetadata;

public class FileSystemScanner {
   private static final Log LOG = LogFactory.getLog(FileSystemScanner.class);
   private final Charset defaultEncoding;
   private final boolean detectEncoding;
   private final boolean throwOnMissingLocations;
   private boolean stream = false;

   public FileSystemScanner(Charset encoding, boolean stream, boolean detectEncoding, boolean throwOnMissingLocations) {
      this.defaultEncoding = encoding;
      this.detectEncoding = detectEncoding;
      this.throwOnMissingLocations = throwOnMissingLocations;
   }

   public Collection<LoadableResource> scanForResources(Location location) {
      String path = location.getRootPath();
      LOG.debug("Scanning for filesystem resources at '" + path + "'");
      File dir = new File(path);
      if (!dir.exists()) {
         if (this.throwOnMissingLocations) {
            throw new FlywayException("Failed to find filesystem location:" + path + ".");
         } else {
            LOG.error("Skipping filesystem location:" + path + " (not found).");
            return Collections.emptyList();
         }
      } else if (!dir.canRead()) {
         if (this.throwOnMissingLocations) {
            throw new FlywayException("Failed to find filesystem location:" + path + " (not readable).");
         } else {
            LOG.error("Skipping filesystem location:" + path + " (not readable).");
            return Collections.emptyList();
         }
      } else if (!dir.isDirectory()) {
         if (this.throwOnMissingLocations) {
            throw new FlywayException("Failed to find filesystem location:" + path + " (not a directory).");
         } else {
            LOG.error("Skipping filesystem location:" + path + " (not a directory).");
            return Collections.emptyList();
         }
      } else {
         Set<LoadableResource> resources = new TreeSet();

         for(String resourceName : this.findResourceNamesFromFileSystem(path, new File(path))) {
            boolean detectEncodingForThisResource = this.detectEncoding;
            if (location.matchesPath(resourceName)) {
               Charset encoding = this.defaultEncoding;
               String encodingBlurb = "";
               if (new File(resourceName + ".conf").exists()) {
                  LoadableResource metadataResource = new FileSystemResource(location, resourceName + ".conf", this.defaultEncoding, false);
                  SqlScriptMetadata metadata = SqlScriptMetadata.fromResource(metadataResource, null);
                  if (metadata.encoding() != null) {
                     encoding = Charset.forName(metadata.encoding());
                     detectEncodingForThisResource = false;
                     encodingBlurb = " (with overriding encoding " + encoding + ")";
                  }
               }

               resources.add(new FileSystemResource(location, resourceName, encoding, detectEncodingForThisResource, this.stream));
               LOG.debug("Found filesystem resource: " + resourceName + encodingBlurb);
            }
         }

         return resources;
      }
   }

   private Set<String> findResourceNamesFromFileSystem(String scanRootLocation, File folder) {
      LOG.debug("Scanning for resources in path: " + folder.getPath() + " (" + scanRootLocation + ")");
      Set<String> resourceNames = new TreeSet();
      File[] files = folder.listFiles();

      for(File file : files) {
         if (file.canRead()) {
            if (file.isDirectory()) {
               if (file.isHidden()) {
                  LOG.debug("Skipping hidden directory: " + file.getAbsolutePath());
               } else {
                  resourceNames.addAll(this.findResourceNamesFromFileSystem(scanRootLocation, file));
               }
            } else {
               resourceNames.add(file.getPath());
            }
         }
      }

      return resourceNames;
   }
}

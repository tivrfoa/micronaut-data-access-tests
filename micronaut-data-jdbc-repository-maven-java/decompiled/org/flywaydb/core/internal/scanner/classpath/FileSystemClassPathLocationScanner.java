package org.flywaydb.core.internal.scanner.classpath;

import java.io.File;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.util.UrlUtils;

public class FileSystemClassPathLocationScanner implements ClassPathLocationScanner {
   private static final Log LOG = LogFactory.getLog(FileSystemClassPathLocationScanner.class);

   @Override
   public Set<String> findResourceNames(String location, URL locationUrl) {
      String filePath = UrlUtils.toFilePath(locationUrl);
      File folder = new File(filePath);
      if (!folder.isDirectory()) {
         LOG.debug("Skipping path as it is not a directory: " + filePath);
         return new TreeSet();
      } else {
         String classPathRootOnDisk = filePath.substring(0, filePath.length() - location.length());
         if (!classPathRootOnDisk.endsWith(File.separator)) {
            classPathRootOnDisk = classPathRootOnDisk + File.separator;
         }

         LOG.debug("Scanning starting at classpath root in filesystem: " + classPathRootOnDisk);
         return this.findResourceNamesFromFileSystem(classPathRootOnDisk, location, folder);
      }
   }

   Set<String> findResourceNamesFromFileSystem(String classPathRootOnDisk, String scanRootLocation, File folder) {
      LOG.debug("Scanning for resources in path: " + folder.getPath() + " (" + scanRootLocation + ")");
      Set<String> resourceNames = new TreeSet();
      File[] files = folder.listFiles();

      for(File file : files) {
         if (file.canRead()) {
            if (file.isDirectory()) {
               if (file.isHidden()) {
                  LOG.debug("Skipping hidden directory: " + file.getAbsolutePath());
               } else {
                  resourceNames.addAll(this.findResourceNamesFromFileSystem(classPathRootOnDisk, scanRootLocation, file));
               }
            } else {
               resourceNames.add(this.toResourceNameOnClasspath(classPathRootOnDisk, file));
            }
         }
      }

      return resourceNames;
   }

   private String toResourceNameOnClasspath(String classPathRootOnDisk, File file) {
      String fileName = file.getAbsolutePath().replace("\\", "/");
      return fileName.substring(classPathRootOnDisk.length());
   }
}

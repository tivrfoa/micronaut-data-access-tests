package org.flywaydb.core.internal.scanner.classpath.jboss;

import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.scanner.classpath.ClassPathLocationScanner;
import org.flywaydb.core.internal.util.UrlUtils;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

public class JBossVFSv3ClassPathLocationScanner implements ClassPathLocationScanner {
   private static final Log LOG = LogFactory.getLog(JBossVFSv3ClassPathLocationScanner.class);

   @Override
   public Set<String> findResourceNames(String location, URL locationUrl) {
      String filePath = UrlUtils.toFilePath(locationUrl);
      String classPathRootOnDisk = filePath.substring(0, filePath.length() - location.length());
      if (!classPathRootOnDisk.endsWith("/")) {
         classPathRootOnDisk = classPathRootOnDisk + "/";
      }

      LOG.debug("Scanning starting at classpath root on JBoss VFS: " + classPathRootOnDisk);
      Set<String> resourceNames = new TreeSet();

      try {
         for(VirtualFile file : VFS.getChild(filePath).getChildrenRecursively(new VirtualFileFilter() {
            public boolean accepts(VirtualFile file) {
               return file.isFile();
            }
         })) {
            resourceNames.add(file.getPathName().substring(classPathRootOnDisk.length()));
         }
      } catch (IOException var9) {
         LOG.warn("Unable to scan classpath root (" + classPathRootOnDisk + ") using JBoss VFS: " + var9.getMessage());
      }

      return resourceNames;
   }
}

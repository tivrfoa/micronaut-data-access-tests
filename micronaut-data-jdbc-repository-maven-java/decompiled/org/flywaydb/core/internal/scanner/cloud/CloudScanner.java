package org.flywaydb.core.internal.scanner.cloud;

import java.nio.charset.Charset;
import java.util.Collection;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.resource.LoadableResource;

public abstract class CloudScanner {
   protected Charset encoding;

   public CloudScanner(Charset encoding) {
      this.encoding = encoding;
   }

   public abstract Collection<LoadableResource> scanForResources(Location var1);

   protected String getPrefix(String bucketName, String path) {
      String relativePathToBucket = path.substring(bucketName.length());
      if (relativePathToBucket.startsWith("/")) {
         relativePathToBucket = relativePathToBucket.substring(1);
      }

      return relativePathToBucket.isEmpty() ? null : relativePathToBucket;
   }

   protected String getBucketName(Location location) {
      int index = location.getPath().indexOf("/");
      return index >= 0 ? location.getPath().substring(0, location.getPath().indexOf("/")) : location.getPath();
   }
}

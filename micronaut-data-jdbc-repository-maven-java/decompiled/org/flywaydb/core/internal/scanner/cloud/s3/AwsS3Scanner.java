package org.flywaydb.core.internal.scanner.cloud.s3;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.S3ClientFactory;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.resource.s3.AwsS3Resource;
import org.flywaydb.core.internal.scanner.cloud.CloudScanner;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request.Builder;

public class AwsS3Scanner extends CloudScanner {
   private static final Log LOG = LogFactory.getLog(AwsS3Scanner.class);
   private final boolean throwOnMissingLocations;

   public AwsS3Scanner(Charset encoding, boolean throwOnMissingLocations) {
      super(encoding);
      this.throwOnMissingLocations = throwOnMissingLocations;
   }

   @Override
   public Collection<LoadableResource> scanForResources(Location location) {
      String bucketName = this.getBucketName(location);
      String prefix = this.getPrefix(bucketName, location.getPath());
      S3Client s3Client = S3ClientFactory.getClient();

      try {
         Builder builder = ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix);
         ListObjectsV2Request request = (ListObjectsV2Request)builder.build();
         ListObjectsV2Response listObjectResult = s3Client.listObjectsV2(request);
         return this.getLoadableResources(bucketName, listObjectResult);
      } catch (SdkClientException var8) {
         if (this.throwOnMissingLocations) {
            throw new FlywayException("Could not access s3 location:" + bucketName + prefix + " due to error: " + var8.getMessage());
         } else {
            LOG.error("Skipping s3 location:" + bucketName + prefix + " due to error: " + var8.getMessage());
            return Collections.emptyList();
         }
      }
   }

   private Collection<LoadableResource> getLoadableResources(String bucketName, ListObjectsV2Response listObjectResult) {
      List<S3Object> objectSummaries = listObjectResult.contents();
      Set<LoadableResource> resources = new TreeSet();

      for(S3Object objectSummary : objectSummaries) {
         LOG.debug("Found Amazon S3 resource: " + bucketName.concat("/").concat(objectSummary.key()));
         resources.add(new AwsS3Resource(bucketName, objectSummary, this.encoding));
      }

      return resources;
   }
}

package org.flywaydb.core.internal.resource.s3;

import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.S3ClientFactory;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resource.LoadableResource;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.GetObjectRequest.Builder;

public class AwsS3Resource extends LoadableResource {
   private static final Log LOG = LogFactory.getLog(AwsS3Resource.class);
   private final String bucketName;
   private final S3Object s3ObjectSummary;
   private final Charset encoding;

   @Override
   public Reader read() {
      S3Client s3 = S3ClientFactory.getClient();

      try {
         Builder builder = GetObjectRequest.builder().bucket(this.bucketName).key(this.s3ObjectSummary.key());
         GetObjectRequest request = (GetObjectRequest)builder.build();
         ResponseInputStream o = s3.getObject(request);
         return Channels.newReader(Channels.newChannel(o), this.encoding.name());
      } catch (AwsServiceException var5) {
         LOG.error(var5.getMessage(), var5);
         throw new FlywayException("Failed to get object from s3: " + var5.getMessage(), var5);
      }
   }

   @Override
   public String getAbsolutePath() {
      return this.bucketName.concat("/").concat(this.s3ObjectSummary.key());
   }

   @Override
   public String getAbsolutePathOnDisk() {
      return this.getAbsolutePath();
   }

   @Override
   public String getFilename() {
      return this.s3ObjectSummary.key().substring(this.s3ObjectSummary.key().lastIndexOf(47) + 1);
   }

   @Override
   public String getRelativePath() {
      return this.getAbsolutePath();
   }

   public AwsS3Resource(String bucketName, S3Object s3ObjectSummary, Charset encoding) {
      this.bucketName = bucketName;
      this.s3ObjectSummary = s3ObjectSummary;
      this.encoding = encoding;
   }
}

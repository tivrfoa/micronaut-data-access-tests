package org.flywaydb.core.api.configuration;

import software.amazon.awssdk.services.s3.S3Client;

public class S3ClientFactory {
   private static S3Client client = null;

   public static S3Client getClient() {
      return client != null ? client : S3Client.create();
   }

   private S3ClientFactory() {
   }

   public static void setClient(S3Client client) {
      S3ClientFactory.client = client;
   }
}

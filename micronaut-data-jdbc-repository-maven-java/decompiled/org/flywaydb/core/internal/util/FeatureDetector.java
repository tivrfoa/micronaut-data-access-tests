package org.flywaydb.core.internal.util;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

public final class FeatureDetector {
   private static final Log LOG = LogFactory.getLog(FeatureDetector.class);
   private final ClassLoader classLoader;
   private Boolean apacheCommonsLoggingAvailable;
   private Boolean log4J2Available;
   private Boolean slf4jAvailable;
   private Boolean jbossVFSv2Available;
   private Boolean jbossVFSv3Available;
   private Boolean osgiFrameworkAvailable;
   private Boolean awsAvailable;
   private Boolean gcsAvailable;

   public FeatureDetector(ClassLoader classLoader) {
      this.classLoader = classLoader;
   }

   public boolean isApacheCommonsLoggingAvailable() {
      if (this.apacheCommonsLoggingAvailable == null) {
         this.apacheCommonsLoggingAvailable = ClassUtils.isPresent("org.apache.commons.logging.Log", this.classLoader);
      }

      return this.apacheCommonsLoggingAvailable;
   }

   public boolean isLog4J2Available() {
      if (this.log4J2Available == null) {
         this.log4J2Available = ClassUtils.isPresent("org.apache.logging.log4j.Logger", this.classLoader);
      }

      return this.log4J2Available;
   }

   public boolean isSlf4jAvailable() {
      if (this.slf4jAvailable == null) {
         this.slf4jAvailable = ClassUtils.isPresent("org.slf4j.Logger", this.classLoader)
            && ClassUtils.isPresent("org.slf4j.impl.StaticLoggerBinder", this.classLoader);
         this.slf4jAvailable = this.slf4jAvailable | ClassUtils.isImplementationPresent("org.slf4j.spi.SLF4JServiceProvider", this.classLoader);
      }

      return this.slf4jAvailable;
   }

   public boolean isJBossVFSv2Available() {
      if (this.jbossVFSv2Available == null) {
         this.jbossVFSv2Available = ClassUtils.isPresent("org.jboss.virtual.VFS", this.classLoader);
         LOG.debug("JBoss VFS v2 available: " + this.jbossVFSv2Available);
      }

      return this.jbossVFSv2Available;
   }

   public boolean isJBossVFSv3Available() {
      if (this.jbossVFSv3Available == null) {
         this.jbossVFSv3Available = ClassUtils.isPresent("org.jboss.vfs.VFS", this.classLoader);
         LOG.debug("JBoss VFS v3 available: " + this.jbossVFSv3Available);
      }

      return this.jbossVFSv3Available;
   }

   public boolean isOsgiFrameworkAvailable() {
      if (this.osgiFrameworkAvailable == null) {
         ClassLoader classLoader = FeatureDetector.class.getClassLoader();
         this.osgiFrameworkAvailable = ClassUtils.isPresent("org.osgi.framework.Bundle", classLoader);
         LOG.debug("OSGi framework available: " + this.osgiFrameworkAvailable);
      }

      return this.osgiFrameworkAvailable;
   }

   public boolean isAwsAvailable() {
      if (this.awsAvailable == null) {
         this.awsAvailable = ClassUtils.isPresent("software.amazon.awssdk.services.s3.S3Client", this.classLoader);
         LOG.debug("AWS SDK available: " + this.awsAvailable);
      }

      return this.awsAvailable;
   }

   public boolean isGCSAvailable() {
      if (this.gcsAvailable == null) {
         this.gcsAvailable = ClassUtils.isPresent("com.google.cloud.storage.Storage", this.classLoader);
         LOG.debug("Google Cloud Storage available: " + this.gcsAvailable);
      }

      return this.gcsAvailable;
   }
}

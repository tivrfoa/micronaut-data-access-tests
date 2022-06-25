package io.micronaut.flyway.graalvm;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import io.micronaut.core.annotation.Internal;

@Substitute
@Internal
@TargetClass(
   className = "org.flywaydb.core.internal.util.FeatureDetector"
)
final class FeatureDetectorSubstitutions {
   @Substitute
   public FeatureDetectorSubstitutions(ClassLoader classLoader) {
   }

   @Substitute
   public boolean isApacheCommonsLoggingAvailable() {
      return false;
   }

   @Substitute
   public boolean isLog4J2Available() {
      return false;
   }

   @Substitute
   public boolean isSlf4jAvailable() {
      return true;
   }

   @Substitute
   public boolean isJBossVFSv2Available() {
      return false;
   }

   @Substitute
   public boolean isJBossVFSv3Available() {
      return false;
   }

   @Substitute
   public boolean isOsgiFrameworkAvailable() {
      return false;
   }

   @Substitute
   public boolean isAwsAvailable() {
      return false;
   }

   @Substitute
   public boolean isGCSAvailable() {
      return false;
   }
}

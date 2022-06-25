package org.flywaydb.core.internal.license;

public class FlywayTeamsUpgradeMessage {
   public static String generate(String detectedFeature, String usageMessage) {
      return "Detected "
         + detectedFeature
         + ". Upgrade to "
         + Edition.ENTERPRISE
         + " to "
         + usageMessage
         + ". Try "
         + Edition.ENTERPRISE
         + " for free: "
         + "https://rd.gt/2VzHpkY";
   }
}

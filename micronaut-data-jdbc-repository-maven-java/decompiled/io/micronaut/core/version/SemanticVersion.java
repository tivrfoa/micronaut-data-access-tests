package io.micronaut.core.version;

public class SemanticVersion implements Comparable<SemanticVersion> {
   private static final int PARTS_MIN = 3;
   private final Integer major;
   private final Integer minor;
   private final Integer patch;
   private final String version;

   public SemanticVersion(String version) {
      this.version = version;
      String[] parts = version.replace('_', '.').replace('-', '.').split("\\.");
      if (parts.length >= 3) {
         try {
            this.major = Integer.valueOf(parts[0]);
            this.minor = Integer.valueOf(parts[1]);
            this.patch = Integer.valueOf(parts[2]);
         } catch (NumberFormatException var4) {
            throw new IllegalArgumentException("Version number is not semantic [" + version + "]! Should be in the format d.d.d. See https://semver.org");
         }
      } else {
         throw new IllegalArgumentException("Version number is not semantic. Should be in the format d.d.d. See https://semver.org");
      }
   }

   public String getVersion() {
      return this.version;
   }

   public int compareTo(SemanticVersion o) {
      int majorCompare = this.major.compareTo(o.major);
      if (majorCompare != 0) {
         return majorCompare;
      } else {
         int minorCompare = this.minor.compareTo(o.minor);
         if (minorCompare != 0) {
            return minorCompare;
         } else {
            int patchCompare = this.patch.compareTo(o.patch);
            return patchCompare != 0 ? patchCompare : 0;
         }
      }
   }

   public static boolean isAtLeastMajorMinor(String version, int majorVersion, int minorVersion) {
      SemanticVersion semanticVersion = new SemanticVersion(version);
      return isAtLeastMajorMinorImpl(semanticVersion, majorVersion, minorVersion);
   }

   public static boolean isAtLeast(String version, String requiredVersion) {
      if (version != null) {
         SemanticVersion thisVersion = new SemanticVersion(version);
         SemanticVersion otherVersion = new SemanticVersion(requiredVersion);
         return thisVersion.compareTo(otherVersion) != -1;
      } else {
         return false;
      }
   }

   private static boolean isAtLeastMajorMinorImpl(SemanticVersion version, int majorVersion, int minorVersion) {
      return version != null && version.major >= majorVersion && version.minor >= minorVersion;
   }
}

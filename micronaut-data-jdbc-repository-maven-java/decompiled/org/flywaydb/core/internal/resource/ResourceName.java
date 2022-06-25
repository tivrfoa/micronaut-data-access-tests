package org.flywaydb.core.internal.resource;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;

public class ResourceName {
   private final String prefix;
   private final String version;
   private final String separator;
   private final String description;
   private final String rawDescription;
   private final String suffix;
   private final boolean isValid;
   private final String validityMessage;

   public static ResourceName invalid(String message) {
      return new ResourceName(null, null, null, null, null, null, false, message);
   }

   public String getPrefix() {
      if (!this.isValid) {
         throw new FlywayException("Cannot access prefix of invalid ResourceNameParseResult\r\n" + this.validityMessage);
      } else {
         return this.prefix;
      }
   }

   private boolean isVersioned() {
      return !"".equals(this.version);
   }

   public MigrationVersion getVersion() {
      return this.isVersioned() ? MigrationVersion.fromVersion(this.version) : null;
   }

   public String getDescription() {
      if (!this.isValid) {
         throw new FlywayException("Cannot access description of invalid ResourceNameParseResult\r\n" + this.validityMessage);
      } else {
         return this.description;
      }
   }

   public String getSuffix() {
      if (!this.isValid) {
         throw new FlywayException("Cannot access suffix of invalid ResourceNameParseResult\r\n" + this.validityMessage);
      } else {
         return this.suffix;
      }
   }

   public String getFilenameWithoutSuffix() {
      if (!this.isValid) {
         throw new FlywayException("Cannot access name of invalid ResourceNameParseResult\r\n" + this.validityMessage);
      } else {
         return "".equals(this.description) ? this.prefix + this.version : this.prefix + this.version + this.separator + this.description;
      }
   }

   public String getFilename() {
      if (!this.isValid) {
         throw new FlywayException("Cannot access name of invalid ResourceNameParseResult\r\n" + this.validityMessage);
      } else {
         return "".equals(this.description)
            ? this.prefix + this.version + this.suffix
            : this.prefix + this.version + this.separator + this.rawDescription + this.suffix;
      }
   }

   public boolean isValid() {
      return this.isValid;
   }

   public String getValidityMessage() {
      return this.validityMessage;
   }

   public ResourceName(
      String prefix, String version, String separator, String description, String rawDescription, String suffix, boolean isValid, String validityMessage
   ) {
      this.prefix = prefix;
      this.version = version;
      this.separator = separator;
      this.description = description;
      this.rawDescription = rawDescription;
      this.suffix = suffix;
      this.isValid = isValid;
      this.validityMessage = validityMessage;
   }
}

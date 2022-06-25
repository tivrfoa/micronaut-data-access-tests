package org.flywaydb.core.internal.license;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.util.FileCopyUtils;

public class VersionPrinter {
   private static final Log LOG = LogFactory.getLog(VersionPrinter.class);
   public static final String VERSION = readVersion();
   public static Edition EDITION = Edition.COMMUNITY;

   public static String getVersion() {
      return VERSION;
   }

   public static void printVersion() {
      printVersionOnly();
      LOG.info("See what's new here: https://flywaydb.org/documentation/learnmore/releaseNotes#" + VERSION);
      LOG.info("");
   }

   public static void printVersionOnly() {
      LOG.info(EDITION + " " + VERSION + " by Redgate");
   }

   private static String readVersion() {
      try {
         return FileCopyUtils.copyToString(
            VersionPrinter.class.getClassLoader().getResourceAsStream("org/flywaydb/core/internal/version.txt"), StandardCharsets.UTF_8
         );
      } catch (IOException var1) {
         throw new FlywayException("Unable to read Flyway version: " + var1.getMessage(), var1);
      }
   }

   private VersionPrinter() {
   }
}

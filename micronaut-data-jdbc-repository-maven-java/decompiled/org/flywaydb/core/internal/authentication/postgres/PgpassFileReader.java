package org.flywaydb.core.internal.authentication.postgres;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.authentication.ExternalAuthFileReader;

public class PgpassFileReader implements ExternalAuthFileReader {
   private static final Log LOG = LogFactory.getLog(PgpassFileReader.class);

   @Override
   public List<String> getAllContents() {
      List<String> fileContents = new ArrayList();
      String pgpassFilePath = this.getPgpassFilePath();
      if (pgpassFilePath == null) {
         return fileContents;
      } else {
         LOG.debug("Found pgpass file '" + pgpassFilePath + "'.");

         try {
            fileContents.add(new String(Files.readAllBytes(Paths.get(pgpassFilePath))));
         } catch (IOException var4) {
            LOG.debug("Unable to read from pgpass file '" + pgpassFilePath + "'.");
         }

         return fileContents;
      }
   }

   public String getPgpassFilePath() {
      String pgpassEnvPath = System.getenv("PGPASSFILE");
      if (pgpassEnvPath != null) {
         return pgpassEnvPath;
      } else {
         boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
         File pgpassFile;
         if (isWindows) {
            pgpassFile = new File(System.getenv("APPDATA"), "postgresql\\pgpass.conf");
         } else {
            pgpassFile = new File(System.getProperty("user.home"), ".pgpass");
         }

         return pgpassFile.exists() ? pgpassFile.getAbsolutePath() : null;
      }
   }
}

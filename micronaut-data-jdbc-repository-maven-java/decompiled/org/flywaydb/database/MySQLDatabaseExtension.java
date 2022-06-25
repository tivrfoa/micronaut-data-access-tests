package org.flywaydb.database;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.extensibility.PluginMetadata;
import org.flywaydb.core.internal.util.FileCopyUtils;

public class MySQLDatabaseExtension implements PluginMetadata {
   @Override
   public String getDescription() {
      return "MySQL database support " + readVersion() + " by Redgate";
   }

   private static String readVersion() {
      try {
         return FileCopyUtils.copyToString(
            MySQLDatabaseExtension.class.getClassLoader().getResourceAsStream("org/flywaydb/database/version.txt"), StandardCharsets.UTF_8
         );
      } catch (IOException var1) {
         throw new FlywayException("Unable to read extension version: " + var1.getMessage(), var1);
      }
   }
}

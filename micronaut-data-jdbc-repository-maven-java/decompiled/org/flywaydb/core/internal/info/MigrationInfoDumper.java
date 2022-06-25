package org.flywaydb.core.internal.info;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.internal.util.AsciiTable;
import org.flywaydb.core.internal.util.DateUtils;

public class MigrationInfoDumper {
   public static String dumpToAsciiTable(MigrationInfo[] migrationInfos) {
      List<String> columns = Arrays.asList("Category", "Version", "Description", "Type", "Installed On", "State");
      List<List<String>> rows = new ArrayList();

      for(MigrationInfo migrationInfo : migrationInfos) {
         List<String> row = Arrays.asList(
            getCategory(migrationInfo),
            getVersionStr(migrationInfo),
            migrationInfo.getDescription(),
            migrationInfo.getType().name(),
            DateUtils.formatDateAsIsoString(migrationInfo.getInstalledOn()),
            migrationInfo.getState().getDisplayName()
         );
         rows.add(row);
      }

      return new AsciiTable(columns, rows, true, "", "No migrations found").render();
   }

   static String getCategory(MigrationInfo migrationInfo) {
      if (migrationInfo.getType().isSynthetic()) {
         return "";
      } else {
         return migrationInfo.getVersion() == null ? "Repeatable" : "Versioned";
      }
   }

   private static String getVersionStr(MigrationInfo migrationInfo) {
      return migrationInfo.getVersion() == null ? "" : migrationInfo.getVersion().toString();
   }

   private MigrationInfoDumper() {
   }
}

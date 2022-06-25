package org.flywaydb.authentication.mysql;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.authentication.ExternalAuthFileReader;

public class MySQLOptionFileReader implements ExternalAuthFileReader {
   private static final Log LOG = LogFactory.getLog(MySQLOptionFileReader.class);
   public final List<String> optionFiles = new ArrayList();
   private final List<String> encryptedOptionFiles = new ArrayList();

   @Override
   public List<String> getAllContents() {
      List<String> fileContents = new ArrayList();
      return fileContents;
   }

   public void populateOptionFiles() {
      boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
      if (isWindows) {
         String winDir = System.getenv("WINDIR");
         this.addIfOptionFileExists(winDir + "\\my.ini", false);
         this.addIfOptionFileExists(winDir + "\\my.cnf", false);
         this.addIfOptionFileExists("C:\\my.ini", false);
         this.addIfOptionFileExists("C:\\my.cnf", false);
         String mysqlHome = System.getenv("MYSQL_HOME");
         if (mysqlHome != null) {
            this.addIfOptionFileExists(mysqlHome + "\\my.ini", false);
            this.addIfOptionFileExists(mysqlHome + "\\my.cnf", false);
         }

         String appdata = System.getenv("APPDATA");
         this.addIfOptionFileExists(appdata + "\\MySQL\\.mylogin.cnf", true);
      } else {
         this.addIfOptionFileExists("/etc/my.cnf", false);
         this.addIfOptionFileExists("/etc/mysql/my.cnf", false);
         String mysqlHome = System.getenv("MYSQL_HOME");
         if (mysqlHome != null) {
            this.addIfOptionFileExists(mysqlHome + "/my.cnf", false);
         }

         String userHome = System.getProperty("user.home");
         this.addIfOptionFileExists(userHome + "/.my.cnf", true);
         this.addIfOptionFileExists(userHome + "/.mylogin.cnf", true);
      }

   }

   private void addIfOptionFileExists(String optionFilePath, boolean encrypted) {
      File optionFile = new File(optionFilePath);
      if (optionFile.exists()) {
         this.optionFiles.add(optionFile.getAbsolutePath());
         if (encrypted) {
            this.encryptedOptionFiles.add(optionFile.getAbsolutePath());
         }

      }
   }
}

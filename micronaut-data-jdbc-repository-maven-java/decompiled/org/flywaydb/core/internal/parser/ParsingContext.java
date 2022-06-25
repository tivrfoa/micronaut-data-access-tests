package org.flywaydb.core.internal.parser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.resource.ResourceName;

public class ParsingContext {
   private static final Log LOG = LogFactory.getLog(ParsingContext.class);
   private static final String DEFAULT_SCHEMA_PLACEHOLDER = "defaultSchema";
   private static final String USER_PLACEHOLDER = "user";
   private static final String DATABASE_PLACEHOLDER = "database";
   private static final String TIMESTAMP_PLACEHOLDER = "timestamp";
   private static final String FILENAME_PLACEHOLDER = "filename";
   private static final String WORKING_DIRECTORY_PLACEHOLDER = "workingDirectory";
   private static final String TABLE_PLACEHOLDER = "table";
   private final Map<String, String> placeholders = new HashMap();
   private Database database;

   private String generateName(String name, Configuration configuration) {
      return "flyway" + configuration.getPlaceholderSeparator() + name;
   }

   public void populate(Database database, Configuration configuration) {
      this.setDatabase(database);
      String defaultSchemaName = configuration.getDefaultSchema();
      String[] schemaNames = configuration.getSchemas();
      Schema currentSchema = this.getCurrentSchema(database);
      String catalog = database.getCatalog();
      String currentUser = this.getCurrentUser(database);
      if (defaultSchemaName == null) {
         if (schemaNames.length > 0) {
            defaultSchemaName = schemaNames[0];
         } else {
            defaultSchemaName = currentSchema.getName();
         }
      }

      if (defaultSchemaName != null) {
         this.placeholders.put(this.generateName("defaultSchema", configuration), defaultSchemaName);
      }

      if (catalog != null) {
         this.placeholders.put(this.generateName("database", configuration), catalog);
      }

      this.placeholders.put(this.generateName("user", configuration), currentUser);
      this.placeholders.put(this.generateName("timestamp", configuration), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
      this.placeholders.put(this.generateName("workingDirectory", configuration), System.getProperty("user.dir"));
      this.placeholders.put(this.generateName("table", configuration), configuration.getTable());
   }

   public void updateFilenamePlaceholder(ResourceName resourceName, Configuration configuration) {
      String filenamePlaceholder = this.generateName("filename", configuration);
      if (resourceName.isValid()) {
         this.placeholders.put(filenamePlaceholder, resourceName.getFilename());
      } else {
         this.placeholders.remove(filenamePlaceholder);
      }

   }

   private Schema getCurrentSchema(Database database) {
      try {
         return database.getMainConnection().getCurrentSchema();
      } catch (FlywayException var3) {
         LOG.debug("Could not get schema for defaultSchema placeholder.");
         return null;
      }
   }

   private String getCurrentUser(Database database) {
      try {
         return database.getCurrentUser();
      } catch (FlywayException var3) {
         LOG.debug("Could not get user for user placeholder.");
         return null;
      }
   }

   public Map<String, String> getPlaceholders() {
      return this.placeholders;
   }

   public Database getDatabase() {
      return this.database;
   }

   public void setDatabase(Database database) {
      this.database = database;
   }
}

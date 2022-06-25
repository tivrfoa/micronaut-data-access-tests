package org.flywaydb.core.internal.sqlscript;

import java.util.HashMap;
import java.util.Map;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.PlaceholderReplacingReader;

public class SqlScriptMetadata {
   private static final Log LOG = LogFactory.getLog(SqlScriptMetadata.class);
   private static final String EXECUTE_IN_TRANSACTION = "executeInTransaction";
   private static final String ENCODING = "encoding";
   private static final String PLACEHOLDER_REPLACEMENT = "placeholderReplacement";
   private static final String SHOULD_EXECUTE = "shouldExecute";
   private final Boolean executeInTransaction;
   private final String encoding;
   private final boolean placeholderReplacement;
   private boolean shouldExecute;

   private SqlScriptMetadata(Map<String, String> metadata) {
      metadata = new HashMap(metadata);
      this.executeInTransaction = ConfigUtils.removeBoolean(metadata, "executeInTransaction");
      this.encoding = (String)metadata.remove("encoding");
      this.placeholderReplacement = Boolean.parseBoolean((String)metadata.getOrDefault("placeholderReplacement", "true"));
      metadata.remove("placeholderReplacement");
      this.shouldExecute = true;
      if (metadata.containsKey("shouldExecute")) {
         throw new FlywayTeamsUpgradeRequiredException("shouldExecute");
      } else {
         ConfigUtils.checkConfigurationForUnrecognisedProperties(metadata, null);
      }
   }

   public Boolean executeInTransaction() {
      return this.executeInTransaction;
   }

   public String encoding() {
      return this.encoding;
   }

   public boolean placeholderReplacement() {
      return this.placeholderReplacement;
   }

   public boolean shouldExecute() {
      return this.shouldExecute;
   }

   public static boolean isMultilineBooleanExpression(String line) {
      return !line.startsWith("shouldExecute") && (line.contains("==") || line.contains("!="));
   }

   public static SqlScriptMetadata fromResource(LoadableResource resource, Parser parser) {
      if (resource != null) {
         LOG.debug("Found script configuration: " + resource.getFilename());
         return parser == null
            ? new SqlScriptMetadata(ConfigUtils.loadConfigurationFromReader(resource.read()))
            : new SqlScriptMetadata(
               ConfigUtils.loadConfigurationFromReader(PlaceholderReplacingReader.create(parser.configuration, parser.parsingContext, resource.read()))
            );
      } else {
         return new SqlScriptMetadata(new HashMap());
      }
   }

   public static LoadableResource getMetadataResource(ResourceProvider resourceProvider, LoadableResource resource) {
      return resourceProvider == null ? null : resourceProvider.getResource(resource.getRelativePath() + ".conf");
   }
}

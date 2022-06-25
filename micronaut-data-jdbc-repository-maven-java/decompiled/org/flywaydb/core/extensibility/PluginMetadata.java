package org.flywaydb.core.extensibility;

import java.util.List;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;

public interface PluginMetadata extends Plugin {
   default String getHelpText() {
      StringBuilder result = new StringBuilder();
      String indent = "    ";
      String description = this.getDescription();
      List<ConfigurationParameter> configurationParameters = this.getConfigurationParameters();
      List<ConfigurationParameter> flags = this.getFlags();
      String example = this.getExample();
      String documentationLink = this.getDocumentationLink();
      if (description != null) {
         result.append("Description:\n").append(indent).append(description).append("\n\n");
      }

      int padSize = 0;
      if (configurationParameters != null) {
         padSize = configurationParameters.stream().mapToInt(px -> px.name.length()).max().orElse(0) + 2;
      }

      if (flags != null) {
         padSize = Math.max(padSize, flags.stream().mapToInt(px -> px.name.length()).max().orElse(0) + 2);
      }

      if (configurationParameters != null) {
         result.append("Configuration parameters: (Format: -key=value)\n");

         for(ConfigurationParameter p : configurationParameters) {
            result.append(indent).append(StringUtils.rightPad(p.name.substring("flyway.".length()), padSize, ' ')).append(p.description);
            if (p.required) {
               result.append(" [REQUIRED]");
            }

            result.append("\n");
         }

         result.append("\n");
      }

      if (flags != null) {
         result.append("Flags:\n");

         for(ConfigurationParameter p : flags) {
            result.append(indent).append(StringUtils.rightPad(p.name, padSize, ' ')).append(p.description);
            if (p.required) {
               result.append(" [REQUIRED]");
            }

            result.append("\n");
         }

         result.append("\n");
      }

      if (example != null) {
         result.append("Example:\n").append(indent).append(example).append("\n\n");
      }

      if (documentationLink != null) {
         result.append("Online documentation: ").append(documentationLink).append("\n");
      }

      return result.toString();
   }

   default String getDescription() {
      return null;
   }

   default List<ConfigurationParameter> getConfigurationParameters() {
      return null;
   }

   default List<ConfigurationParameter> getFlags() {
      return null;
   }

   default String getExample() {
      return null;
   }

   default List<Pair<String, String>> getUsage() {
      return null;
   }

   default String getDocumentationLink() {
      return null;
   }
}

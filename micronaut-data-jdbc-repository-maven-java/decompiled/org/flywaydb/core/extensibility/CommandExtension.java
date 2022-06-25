package org.flywaydb.core.extensibility;

import java.util.List;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;

public interface CommandExtension extends PluginMetadata {
   boolean handlesCommand(String var1);

   default String getCommandForFlag(String flag) {
      return null;
   }

   boolean handlesParameter(String var1);

   OperationResult handle(String var1, Configuration var2, List<String> var3) throws FlywayException;
}

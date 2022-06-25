package org.flywaydb.core.extensibility;

import java.util.Map;

public interface ConfigurationExtension extends Plugin {
   void extractParametersFromConfiguration(Map<String, String> var1);

   String getConfigurationParameterFromEnvironmentVariable(String var1);
}

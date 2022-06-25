package org.flywaydb.core.extensibility;

import java.util.Map;
import org.flywaydb.core.api.configuration.ClassicConfiguration;

public interface ConfigurationProvider<T extends ConfigurationExtension> extends Plugin {
   Map<String, String> getConfiguration(T var1, ClassicConfiguration var2) throws Exception;

   Class<T> getConfigurationExtensionClass();
}

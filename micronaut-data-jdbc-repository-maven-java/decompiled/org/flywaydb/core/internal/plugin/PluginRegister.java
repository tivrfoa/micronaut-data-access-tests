package org.flywaydb.core.internal.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.extensibility.Plugin;

public class PluginRegister {
   private static final Log LOG = LogFactory.getLog(PluginRegister.class);
   private static final ClassLoader CLASS_LOADER = new PluginRegister().getClass().getClassLoader();
   public static final List<Plugin> REGISTERED_PLUGINS = new ArrayList();
   private static boolean hasRegisteredPlugins = false;

   public static void registerPlugins() {
      synchronized(REGISTERED_PLUGINS) {
         if (!hasRegisteredPlugins) {
            for(Plugin plugin : ServiceLoader.load(Plugin.class, CLASS_LOADER)) {
               REGISTERED_PLUGINS.add(plugin);
            }

            hasRegisteredPlugins = true;
         }
      }
   }

   private static List<Plugin> getPlugins() {
      if (!hasRegisteredPlugins) {
         registerPlugins();
      }

      return REGISTERED_PLUGINS;
   }

   public static <T extends Plugin> List<T> getPlugins(Class<T> clazz) {
      return (List<T>)getPlugins().stream().filter(clazz::isInstance).collect(Collectors.toList());
   }

   public static <T extends Plugin> T getPlugin(Class<T> clazz) {
      return (T)getPlugins().stream().filter(p -> p.getClass().getCanonicalName().equals(clazz.getCanonicalName())).findFirst().orElse(null);
   }

   private PluginRegister() {
   }
}

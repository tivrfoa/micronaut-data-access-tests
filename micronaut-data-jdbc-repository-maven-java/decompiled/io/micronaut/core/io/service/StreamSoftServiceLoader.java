package io.micronaut.core.io.service;

import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.util.CollectionUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.Set;
import java.util.stream.Stream;

@Deprecated
public class StreamSoftServiceLoader {
   public static <T> Stream<ServiceDefinition<T>> loadParallel(Class<T> serviceType, ClassLoader classLoader) {
      String name = serviceType.getName();

      Enumeration<URL> serviceConfigs;
      try {
         serviceConfigs = classLoader.getResources("META-INF/services/" + name);
      } catch (IOException var5) {
         throw new ServiceConfigurationError("Failed to load resources for service: " + name, var5);
      }

      Set<URL> urlSet = CollectionUtils.enumerationToSet(serviceConfigs);
      return ((Stream)urlSet.stream().parallel()).flatMap(url -> {
         List<String> lines = new ArrayList();

         try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            Throwable var3x = null;

            try {
               for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                  if (line.length() != 0 && line.charAt(0) != '#') {
                     int i = line.indexOf(35);
                     if (i > -1) {
                        line = line.substring(0, i);
                     }

                     lines.add(line);
                  }
               }
            } catch (Throwable var14) {
               var3x = var14;
               throw var14;
            } finally {
               if (reader != null) {
                  if (var3x != null) {
                     try {
                        reader.close();
                     } catch (Throwable var13) {
                        var3x.addSuppressed(var13);
                     }
                  } else {
                     reader.close();
                  }
               }

            }
         } catch (IOException var16) {
            throw new ServiceConfigurationError("Failed to load resources for URL: " + url, var16);
         }

         return lines.stream();
      }).map(serviceName -> {
         Class<T> loadedClass = (Class)ClassUtils.forName(serviceName, classLoader).orElse(null);
         return new DefaultServiceDefinition(name, loadedClass);
      });
   }

   public static <T> Stream<T> loadPresentParallel(Class<T> serviceType, ClassLoader classLoader) {
      return loadParallel(serviceType, classLoader).filter(ServiceDefinition::isPresent).map(ServiceDefinition::load);
   }
}

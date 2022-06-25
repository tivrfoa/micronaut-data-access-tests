package io.micronaut.core.graal;

import com.oracle.svm.core.annotate.AutomaticFeature;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.io.IOUtils;
import io.micronaut.core.io.service.SoftServiceLoader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import org.graalvm.nativeimage.hosted.Feature.BeforeAnalysisAccess;

@AutomaticFeature
final class ServiceLoaderFeature implements Feature {
   public void beforeAnalysis(BeforeAnalysisAccess access) {
      this.configureForReflection(access);
      StaticServiceDefinitions staticServiceDefinitions = this.buildStaticServiceDefinitions(access);

      for(Set<String> typeNameSet : staticServiceDefinitions.serviceTypeMap.values()) {
         for(String typeName : typeNameSet) {
            try {
               Class<?> c = access.findClassByName(typeName);
               if (c != null) {
                  RuntimeReflection.registerForReflectiveInstantiation(new Class[]{c});
                  RuntimeReflection.register(new Class[]{c});
               }
            } catch (NoClassDefFoundError var9) {
            }
         }
      }

      ImageSingletons.add(StaticServiceDefinitions.class, staticServiceDefinitions);
   }

   @NonNull
   private StaticServiceDefinitions buildStaticServiceDefinitions(BeforeAnalysisAccess access) {
      StaticServiceDefinitions staticServiceDefinitions = new StaticServiceDefinitions();
      String path = "META-INF/micronaut/";

      try {
         Enumeration<URL> micronautResources = access.getApplicationClassLoader().getResources("META-INF/micronaut/");

         while(micronautResources.hasMoreElements()) {
            Set<String> servicePaths = new HashSet();
            URL url = (URL)micronautResources.nextElement();
            URI uri = url.toURI();
            boolean isFileScheme = "file".equals(uri.getScheme());
            if (isFileScheme) {
               Path p = Paths.get(uri);
               uri = p.getParent().getParent().toUri();
            }

            IOUtils.eachFile(uri, "META-INF/micronaut/", servicePathx -> {
               if (Files.isDirectory(servicePathx, new LinkOption[0])) {
                  String serviceName = servicePathx.toString();
                  if (isFileScheme) {
                     int i = serviceName.indexOf("META-INF/micronaut/");
                     if (i > -1) {
                        serviceName = serviceName.substring(i);
                     }
                  }

                  if (serviceName.startsWith("META-INF/micronaut/")) {
                     servicePaths.add(serviceName);
                  }
               }

            });

            for(String servicePath : servicePaths) {
               IOUtils.eachFile(uri, servicePath, serviceTypePath -> {
                  if (Files.isRegularFile(serviceTypePath, new LinkOption[0])) {
                     Set<String> serviceTypeNames = (Set)staticServiceDefinitions.serviceTypeMap.computeIfAbsent(servicePath, key -> new HashSet());
                     String serviceTypeName = serviceTypePath.getFileName().toString();
                     serviceTypeNames.add(serviceTypeName);
                  }

               });
            }
         }
      } catch (URISyntaxException | IOException var11) {
      }

      return staticServiceDefinitions;
   }

   private void configureForReflection(BeforeAnalysisAccess access) {
      Collection<GraalReflectionConfigurer> configurers = new ArrayList();
      SoftServiceLoader.load(GraalReflectionConfigurer.class, access.getApplicationClassLoader()).collectAll(configurers);
      GraalReflectionConfigurer.ReflectionConfigurationContext context = new GraalReflectionConfigurer.ReflectionConfigurationContext() {
         @Override
         public Class<?> findClassByName(@NonNull String name) {
            return access.findClassByName(name);
         }

         @Override
         public void register(Class<?>... types) {
            RuntimeReflection.register(types);
         }

         @Override
         public void register(Method... methods) {
            RuntimeReflection.register(methods);
         }

         @Override
         public void register(Field... fields) {
            RuntimeReflection.register(fields);
         }

         @Override
         public void register(Constructor<?>... constructors) {
            RuntimeReflection.register(constructors);
         }
      };

      for(GraalReflectionConfigurer configurer : configurers) {
         RuntimeClassInitialization.initializeAtBuildTime(new Class[]{configurer.getClass()});
         configurer.configure(context);
      }

   }
}

package io.micronaut.core.io.service;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.io.IOUtils;
import io.micronaut.core.optim.StaticOptimizations;
import io.micronaut.core.reflect.ClassUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SoftServiceLoader<S> implements Iterable<ServiceDefinition<S>> {
   public static final String META_INF_SERVICES = "META-INF/services";
   private static final Map<String, SoftServiceLoader.StaticServiceLoader<?>> STATIC_SERVICES = (Map<String, SoftServiceLoader.StaticServiceLoader<?>>)StaticOptimizations.get(
         SoftServiceLoader.Optimizations.class
      )
      .map(SoftServiceLoader.Optimizations::getServiceLoaders)
      .orElse(Collections.emptyMap());
   private final Class<S> serviceType;
   private final ClassLoader classLoader;
   private final Map<String, ServiceDefinition<S>> loadedServices = new LinkedHashMap();
   private final Iterator<ServiceDefinition<S>> unloadedServices;
   private final Predicate<String> condition;

   private SoftServiceLoader(Class<S> serviceType, ClassLoader classLoader) {
      this(serviceType, classLoader, name -> true);
   }

   private SoftServiceLoader(Class<S> serviceType, ClassLoader classLoader, Predicate<String> condition) {
      this.serviceType = serviceType;
      this.classLoader = classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;
      this.unloadedServices = (Iterator<ServiceDefinition<S>>)(STATIC_SERVICES.containsKey(serviceType.getName())
         ? new SoftServiceLoader.StaticServicesLoaderIterator()
         : new SoftServiceLoader.ServiceLoaderIterator());
      this.condition = condition == null ? name -> true : condition;
   }

   public static <S> SoftServiceLoader<S> load(Class<S> service) {
      return load(service, SoftServiceLoader.class.getClassLoader());
   }

   public static <S> SoftServiceLoader<S> load(Class<S> service, ClassLoader loader) {
      return new SoftServiceLoader<>(service, loader);
   }

   public static <S> SoftServiceLoader<S> load(Class<S> service, ClassLoader loader, Predicate<String> condition) {
      return new SoftServiceLoader<>(service, loader, condition);
   }

   public Optional<ServiceDefinition<S>> first() {
      Iterator<ServiceDefinition<S>> i = this.iterator();
      return i.hasNext() ? Optional.of(i.next()) : Optional.empty();
   }

   public Optional<ServiceDefinition<S>> firstOr(String alternative, ClassLoader classLoader) {
      Iterator<ServiceDefinition<S>> i = this.iterator();
      if (i.hasNext()) {
         return Optional.of(i.next());
      } else {
         Class<S> alternativeClass = (Class)ClassUtils.forName(alternative, classLoader).orElse(null);
         return alternativeClass != null ? Optional.of(this.createService(alternative, alternativeClass)) : Optional.empty();
      }
   }

   public void collectAll(@NonNull Collection<S> values, @Nullable Predicate<S> predicate) {
      String name = this.serviceType.getName();
      SoftServiceLoader.StaticServiceLoader<?> serviceLoader = (SoftServiceLoader.StaticServiceLoader)STATIC_SERVICES.get(name);
      if (serviceLoader != null) {
         this.collectStaticServices(values, predicate, serviceLoader);
      } else {
         this.collectDynamicServices(values, predicate, name);
      }

   }

   private void collectDynamicServices(Collection<S> values, Predicate<S> predicate, String name) {
      SoftServiceLoader.ServiceCollector<S> collector = newCollector(name, this.condition, this.classLoader, className -> {
         try {
            Class<S> loadedClass = Class.forName(className, false, this.classLoader);
            S result = (S)loadedClass.getDeclaredConstructor().newInstance();
            return predicate != null && !predicate.test(result) ? null : result;
         } catch (ClassNotFoundException | NoSuchMethodException | NoClassDefFoundError var5) {
            return null;
         } catch (Exception var6) {
            throw new SoftServiceLoader.ServiceLoadingException(var6);
         }
      });
      collector.collect(values);
   }

   private void collectStaticServices(Collection<S> values, Predicate<S> predicate, SoftServiceLoader.StaticServiceLoader<S> loader) {
      values.addAll(loader.load(predicate));
   }

   public void collectAll(@NonNull Collection<S> values) {
      this.collectAll(values, null);
   }

   @NonNull
   public Iterator<ServiceDefinition<S>> iterator() {
      return new Iterator<ServiceDefinition<S>>() {
         final Iterator<ServiceDefinition<S>> loaded = SoftServiceLoader.this.loadedServices.values().iterator();

         public boolean hasNext() {
            return this.loaded.hasNext() ? true : SoftServiceLoader.this.unloadedServices.hasNext();
         }

         public ServiceDefinition<S> next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else if (this.loaded.hasNext()) {
               return (ServiceDefinition<S>)this.loaded.next();
            } else if (SoftServiceLoader.this.unloadedServices.hasNext()) {
               ServiceDefinition<S> nextService = (ServiceDefinition)SoftServiceLoader.this.unloadedServices.next();
               SoftServiceLoader.this.loadedServices.put(nextService.getName(), nextService);
               return nextService;
            } else {
               throw new ServiceConfigurationError("Bug in iterator");
            }
         }
      };
   }

   @Deprecated
   protected ServiceDefinition<S> newService(String name, Optional<Class> loadedClass) {
      return new DefaultServiceDefinition<>(name, (Class<S>)loadedClass.orElse(null));
   }

   private ServiceDefinition<S> createService(String name, Class<S> loadedClass) {
      return new DefaultServiceDefinition<>(name, loadedClass);
   }

   private static Set<String> computeServiceTypeNames(URI uri, String path) {
      Set<String> typeNames = new HashSet();
      IOUtils.eachFile(uri, path, currentPath -> {
         if (Files.isRegularFile(currentPath, new LinkOption[0])) {
            String typeName = currentPath.getFileName().toString();
            typeNames.add(typeName);
         }

      });
      return typeNames;
   }

   public static <S> SoftServiceLoader.ServiceCollector<S> newCollector(
      String serviceName, Predicate<String> lineCondition, ClassLoader classLoader, Function<String, S> transformer
   ) {
      return new SoftServiceLoader.DefaultServiceCollector<>(serviceName, lineCondition, classLoader, transformer);
   }

   private static class DefaultServiceCollector<S> extends SoftServiceLoader.RecursiveActionValuesCollector<S> implements SoftServiceLoader.ServiceCollector<S> {
      private final String serviceName;
      private final Predicate<String> lineCondition;
      private final ClassLoader classLoader;
      private final Function<String, S> transformer;
      private final List<SoftServiceLoader.RecursiveActionValuesCollector<S>> tasks = new LinkedList();

      public DefaultServiceCollector(String serviceName, Predicate<String> lineCondition, ClassLoader classLoader, Function<String, S> transformer) {
         this.serviceName = serviceName;
         this.lineCondition = lineCondition;
         this.classLoader = classLoader;
         this.transformer = transformer;
      }

      private static URI normalizeFilePath(String path, URI uri) {
         Path p = Paths.get(uri);
         if (p.endsWith(path)) {
            Path subpath = Paths.get(path);

            for(int i = 0; i < subpath.getNameCount(); ++i) {
               p = p.getParent();
            }

            uri = p.toUri();
         }

         return uri;
      }

      protected void compute() {
         try {
            Enumeration<URL> serviceConfigs = this.classLoader.getResources("META-INF/services/" + this.serviceName);

            while(serviceConfigs.hasMoreElements()) {
               URL url = (URL)serviceConfigs.nextElement();
               SoftServiceLoader.UrlServicesLoader<S> task = new SoftServiceLoader.UrlServicesLoader<>(url, this.lineCondition, this.transformer);
               this.tasks.add(task);
               task.fork();
            }

            String path = "META-INF/micronaut/" + this.serviceName;
            Enumeration<URL> micronautResources = this.classLoader.getResources(path);
            Set<URI> uniqueURIs = new LinkedHashSet();

            while(micronautResources.hasMoreElements()) {
               URL url = (URL)micronautResources.nextElement();
               URI uri = url.toURI();
               uniqueURIs.add(uri);
            }

            for(URI uri : uniqueURIs) {
               String scheme = uri.getScheme();
               if ("file".equals(scheme)) {
                  uri = normalizeFilePath(path, uri);
               }

               if (!"resource".equals(scheme) || !uri.toString().contains("#")) {
                  SoftServiceLoader.MicronautMetaServicesLoader<S> task = new SoftServiceLoader.MicronautMetaServicesLoader<>(uri, path, this.transformer);
                  this.tasks.add(task);
                  task.fork();
               }
            }

         } catch (URISyntaxException | IOException var9) {
            throw new ServiceConfigurationError("Failed to load resources for service: " + this.serviceName, var9);
         }
      }

      @Override
      public void collect(Collection<S> values) {
         ForkJoinPool.commonPool().invoke(this);

         for(SoftServiceLoader.RecursiveActionValuesCollector<S> task : this.tasks) {
            task.join();
            task.collect(values);
         }

      }
   }

   private static final class MicronautMetaServicesLoader<S> extends SoftServiceLoader.RecursiveActionValuesCollector<S> {
      private final URI uri;
      private final transient Function<String, S> transformer;
      private final List<SoftServiceLoader.ServiceInstanceLoader<S>> tasks = new LinkedList();
      private final String path;

      private MicronautMetaServicesLoader(URI uri, String path, Function<String, S> transformer) {
         this.uri = uri;
         this.path = path;
         this.transformer = transformer;
      }

      @Override
      public void collect(Collection<S> values) {
         for(SoftServiceLoader.ServiceInstanceLoader<S> task : this.tasks) {
            task.join();
            task.collect(values);
         }

      }

      protected void compute() {
         for(String typeName : SoftServiceLoader.computeServiceTypeNames(this.uri, this.path)) {
            SoftServiceLoader.ServiceInstanceLoader<S> task = new SoftServiceLoader.ServiceInstanceLoader<>(typeName, this.transformer);
            this.tasks.add(task);
            task.fork();
         }

      }
   }

   public static final class Optimizations {
      private final Map<String, SoftServiceLoader.StaticServiceLoader<?>> serviceLoaders;

      public Optimizations(Map<String, SoftServiceLoader.StaticServiceLoader<?>> serviceLoaders) {
         this.serviceLoaders = serviceLoaders;
      }

      public Map<String, SoftServiceLoader.StaticServiceLoader<?>> getServiceLoaders() {
         return this.serviceLoaders;
      }
   }

   private abstract static class RecursiveActionValuesCollector<S> extends RecursiveAction {
      private RecursiveActionValuesCollector() {
      }

      public abstract void collect(Collection<S> values);
   }

   public interface ServiceCollector<S> {
      void collect(Collection<S> values);

      default void collect(Consumer<? super S> consumer) {
         List<S> values = new ArrayList();
         this.collect(values);
         values.forEach(e -> {
            if (e != null) {
               consumer.accept(e);
            }

         });
      }
   }

   private static final class ServiceInstanceLoader<S> extends SoftServiceLoader.RecursiveActionValuesCollector<S> {
      private final String className;
      private final Function<String, S> transformer;
      private S result;
      private Throwable throwable;

      public ServiceInstanceLoader(String className, Function<String, S> transformer) {
         this.className = className;
         this.transformer = transformer;
      }

      protected void compute() {
         try {
            this.result = (S)this.transformer.apply(this.className);
         } catch (Throwable var2) {
            this.throwable = var2;
         }

      }

      @Override
      public void collect(Collection<S> values) {
         if (this.throwable != null) {
            throw new SoftServiceLoader.ServiceLoadingException("Failed to load a service: " + this.throwable.getMessage(), this.throwable);
         } else {
            if (this.result != null && !values.contains(this.result)) {
               values.add(this.result);
            }

         }
      }
   }

   private final class ServiceLoaderIterator implements Iterator<ServiceDefinition<S>> {
      private Enumeration<URL> serviceConfigs = null;
      private Iterator<String> unprocessed = null;

      private ServiceLoaderIterator() {
      }

      public boolean hasNext() {
         if (this.serviceConfigs == null) {
            String name = SoftServiceLoader.this.serviceType.getName();

            try {
               this.serviceConfigs = SoftServiceLoader.this.classLoader.getResources("META-INF/services/" + name);
            } catch (IOException var16) {
               throw new ServiceConfigurationError("Failed to load resources for service: " + name, var16);
            }
         }

         while(this.unprocessed == null || !this.unprocessed.hasNext()) {
            if (!this.serviceConfigs.hasMoreElements()) {
               return false;
            }

            URL url = (URL)this.serviceConfigs.nextElement();

            try {
               BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
               Throwable var3 = null;

               try {
                  List<String> lines = new LinkedList();

                  while(true) {
                     String line = reader.readLine();
                     if (line == null) {
                        this.unprocessed = lines.iterator();
                        break;
                     }

                     if (line.length() != 0 && line.charAt(0) != '#' && SoftServiceLoader.this.condition.test(line)) {
                        int i = line.indexOf(35);
                        if (i > -1) {
                           line = line.substring(0, i);
                        }

                        lines.add(line);
                     }
                  }
               } catch (Throwable var17) {
                  var3 = var17;
                  throw var17;
               } finally {
                  if (reader != null) {
                     if (var3 != null) {
                        try {
                           reader.close();
                        } catch (Throwable var15) {
                           var3.addSuppressed(var15);
                        }
                     } else {
                        reader.close();
                     }
                  }

               }
            } catch (UncheckedIOException | IOException var19) {
            }
         }

         return this.unprocessed.hasNext();
      }

      public ServiceDefinition<S> next() {
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         } else {
            String nextName = (String)this.unprocessed.next();

            try {
               Class<S> loadedClass = Class.forName(nextName, false, SoftServiceLoader.this.classLoader);
               return SoftServiceLoader.this.createService(nextName, loadedClass);
            } catch (ClassNotFoundException | NoClassDefFoundError var3) {
               return SoftServiceLoader.this.createService(nextName, null);
            }
         }
      }
   }

   private static class ServiceLoadingException extends RuntimeException {
      public ServiceLoadingException(String message, Throwable cause) {
         super(message, cause);
      }

      public ServiceLoadingException(Throwable cause) {
         super(cause);
      }
   }

   public static final class StaticDefinition<S> implements ServiceDefinition<S> {
      private static final Lookup LOOKUP = MethodHandles.publicLookup();
      private static final MethodType VOID_TYPE = MethodType.methodType(Void.TYPE);
      private final String name;
      private final Supplier<S> value;

      private StaticDefinition(String name, Supplier<S> value) {
         this.name = name;
         this.value = value;
      }

      public static <S> SoftServiceLoader.StaticDefinition<S> of(String name, Class<S> value) {
         return new SoftServiceLoader.StaticDefinition<>(name, () -> doCreate(value));
      }

      public static <S> SoftServiceLoader.StaticDefinition<S> of(String name, Supplier<S> value) {
         return new SoftServiceLoader.StaticDefinition<>(name, value);
      }

      @Override
      public boolean isPresent() {
         return true;
      }

      @Override
      public String getName() {
         return this.name;
      }

      @Override
      public S load() {
         return (S)this.value.get();
      }

      private static <S> S doCreate(Class<S> clazz) {
         try {
            return (S)(Object)LOOKUP.findConstructor(clazz, VOID_TYPE).invoke();
         } catch (Throwable var2) {
            throw new SoftServiceLoader.ServiceLoadingException(var2);
         }
      }
   }

   public interface StaticServiceLoader<S> {
      Stream<SoftServiceLoader.StaticDefinition<S>> findAll(Predicate<String> predicate);

      default List<S> load(Predicate<S> predicate) {
         return this.load(n -> true, predicate);
      }

      default List<S> load(Predicate<String> condition, Predicate<S> predicate) {
         return (List<S>)this.findAll(condition).map(ServiceDefinition::load).filter(s -> predicate == null || predicate.test(s)).collect(Collectors.toList());
      }
   }

   private final class StaticServicesLoaderIterator implements Iterator<ServiceDefinition<S>> {
      Iterator<SoftServiceLoader.StaticDefinition<S>> iterator;

      private StaticServicesLoaderIterator() {
      }

      private void ensureIterator() {
         if (this.iterator == null) {
            SoftServiceLoader.StaticServiceLoader<S> staticServiceLoader = (SoftServiceLoader.StaticServiceLoader)SoftServiceLoader.STATIC_SERVICES
               .get(SoftServiceLoader.this.serviceType.getName());
            this.iterator = staticServiceLoader.findAll(
                  s -> SoftServiceLoader.this.condition == null || SoftServiceLoader.this.condition.test(s.getClass().getName())
               )
               .iterator();
         }

      }

      public boolean hasNext() {
         this.ensureIterator();
         return this.iterator.hasNext();
      }

      public ServiceDefinition<S> next() {
         this.ensureIterator();
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         } else {
            return (ServiceDefinition<S>)this.iterator.next();
         }
      }
   }

   private static final class UrlServicesLoader<S> extends SoftServiceLoader.RecursiveActionValuesCollector<S> {
      private final URL url;
      private final Predicate<String> lineCondition;
      private final Function<String, S> transformer;
      private final List<SoftServiceLoader.ServiceInstanceLoader<S>> tasks = new LinkedList();

      public UrlServicesLoader(URL url, Predicate<String> lineCondition, Function<String, S> transformer) {
         this.url = url;
         this.lineCondition = lineCondition;
         this.transformer = transformer;
      }

      protected void compute() {
         try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.url.openStream()));
            Throwable var2 = null;

            try {
               while(true) {
                  String line = reader.readLine();
                  if (line == null) {
                     break;
                  }

                  if (line.length() != 0 && line.charAt(0) != '#' && this.lineCondition.test(line)) {
                     int i = line.indexOf(35);
                     if (i > -1) {
                        line = line.substring(0, i);
                     }

                     SoftServiceLoader.ServiceInstanceLoader<S> task = new SoftServiceLoader.ServiceInstanceLoader<>(line, this.transformer);
                     this.tasks.add(task);
                     task.fork();
                  }
               }
            } catch (Throwable var14) {
               var2 = var14;
               throw var14;
            } finally {
               if (reader != null) {
                  if (var2 != null) {
                     try {
                        reader.close();
                     } catch (Throwable var13) {
                        var2.addSuppressed(var13);
                     }
                  } else {
                     reader.close();
                  }
               }

            }
         } catch (UncheckedIOException | IOException var16) {
         }

      }

      @Override
      public void collect(Collection<S> values) {
         for(SoftServiceLoader.ServiceInstanceLoader<S> task : this.tasks) {
            task.join();
            task.collect(values);
         }

      }
   }
}

package io.micronaut.runtime;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.DefaultApplicationContextBuilder;
import io.micronaut.context.banner.MicronautBanner;
import io.micronaut.context.banner.ResourceBanner;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.naming.Described;
import io.micronaut.runtime.exceptions.ApplicationStartupException;
import io.micronaut.runtime.server.EmbeddedServer;
import java.io.PrintStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Micronaut extends DefaultApplicationContextBuilder implements ApplicationContextBuilder {
   private static final String BANNER_NAME = "micronaut-banner.txt";
   private static final Logger LOG = LoggerFactory.getLogger(Micronaut.class);
   private static final String SHUTDOWN_MONITOR_THREAD = "micronaut-shutdown-monitor-thread";
   private Map<Class<? extends Throwable>, Function<Throwable, Integer>> exitHandlers = new LinkedHashMap();

   protected Micronaut() {
   }

   @NonNull
   @Override
   public ApplicationContext start() {
      long start = System.nanoTime();
      this.printBanner();
      ApplicationContext applicationContext = super.build();

      try {
         applicationContext.start();
         Optional<EmbeddedApplication> embeddedContainerBean = applicationContext.findBean(EmbeddedApplication.class);
         embeddedContainerBean.ifPresent(embeddedApplication -> {
            try {
               embeddedApplication.start();
               boolean ex = false;
               if (embeddedApplication instanceof Described) {
                  if (LOG.isInfoEnabled()) {
                     long took = elapsedMillis(start);
                     String desc = ((Described)embeddedApplication).getDescription();
                     LOG.info("Startup completed in {}ms. Server Running: {}", took, desc);
                  }

                  ex = embeddedApplication.isServer();
               } else if (embeddedApplication instanceof EmbeddedServer) {
                  EmbeddedServer embeddedServer = (EmbeddedServer)embeddedApplication;
                  if (LOG.isInfoEnabled()) {
                     long took = elapsedMillis(start);
                     URL url = embeddedServer.getURL();
                     LOG.info("Startup completed in {}ms. Server Running: {}", took, url);
                  }

                  ex = embeddedServer.isKeepAlive();
               } else {
                  if (LOG.isInfoEnabled()) {
                     long took = elapsedMillis(start);
                     LOG.info("Startup completed in {}ms.", took);
                  }

                  ex = embeddedApplication.isServer();
               }

               Thread mainThread = Thread.currentThread();
               boolean finalKeepAlive = ex;
               CountDownLatch countDownLatch = new CountDownLatch(1);
               Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                  if (LOG.isInfoEnabled()) {
                     LOG.info("Embedded Application shutting down");
                  }

                  if (embeddedApplication.isRunning()) {
                     embeddedApplication.stop();
                     countDownLatch.countDown();
                     if (finalKeepAlive) {
                        mainThread.interrupt();
                     }
                  }

               }));
               if (ex) {
                  new Thread(() -> {
                     try {
                        if (!embeddedApplication.isRunning()) {
                           countDownLatch.countDown();
                           Thread.sleep(1000L);
                        }
                     } catch (InterruptedException var3x) {
                     }

                  }, "micronaut-shutdown-monitor-thread").start();
                  boolean interrupted = false;

                  while(true) {
                     try {
                        countDownLatch.await();
                        break;
                     } catch (InterruptedException var11) {
                        interrupted = true;
                     }
                  }

                  if (interrupted) {
                     Thread.currentThread().interrupt();
                  }

                  if (LOG.isInfoEnabled()) {
                     LOG.info("Embedded Application shutting down");
                  }
               }

               if (embeddedApplication.isForceExit()) {
                  System.exit(0);
               }
            } catch (Throwable var12) {
               this.handleStartupException(applicationContext.getEnvironment(), var12);
            }

         });
         if (LOG.isInfoEnabled() && !embeddedContainerBean.isPresent()) {
            LOG.info("No embedded container found. Running as CLI application");
         }

         return applicationContext;
      } catch (Throwable var5) {
         this.handleStartupException(applicationContext.getEnvironment(), var5);
         return null;
      }
   }

   private static long elapsedMillis(long startNanos) {
      return TimeUnit.MILLISECONDS.convert(System.nanoTime() - startNanos, TimeUnit.NANOSECONDS);
   }

   @NonNull
   public Micronaut include(@Nullable String... configurations) {
      return (Micronaut)super.include(configurations);
   }

   @NonNull
   public Micronaut exclude(@Nullable String... configurations) {
      return (Micronaut)super.exclude(configurations);
   }

   @NonNull
   public Micronaut banner(boolean isEnabled) {
      return (Micronaut)super.banner(isEnabled);
   }

   @NonNull
   public Micronaut classes(@Nullable Class... classes) {
      if (classes != null) {
         for(Class aClass : classes) {
            this.packages(aClass.getPackage().getName());
         }
      }

      return this;
   }

   @NonNull
   public Micronaut properties(@Nullable Map<String, Object> properties) {
      return (Micronaut)super.properties(properties);
   }

   @NonNull
   public Micronaut singletons(Object... beans) {
      return (Micronaut)super.singletons(beans);
   }

   @NonNull
   public Micronaut propertySources(@Nullable PropertySource... propertySources) {
      return (Micronaut)super.propertySources(propertySources);
   }

   @NonNull
   public Micronaut environmentPropertySource(boolean environmentPropertySource) {
      return (Micronaut)super.environmentPropertySource(environmentPropertySource);
   }

   @NonNull
   public Micronaut environmentVariableIncludes(@Nullable String... environmentVariables) {
      return (Micronaut)super.environmentVariableIncludes(environmentVariables);
   }

   @NonNull
   public Micronaut environmentVariableExcludes(@Nullable String... environmentVariables) {
      return (Micronaut)super.environmentVariableExcludes(environmentVariables);
   }

   @NonNull
   public Micronaut mainClass(Class mainClass) {
      return (Micronaut)super.mainClass(mainClass);
   }

   @NonNull
   public Micronaut classLoader(ClassLoader classLoader) {
      return (Micronaut)super.classLoader(classLoader);
   }

   @NonNull
   public Micronaut args(@Nullable String... args) {
      return (Micronaut)super.args(args);
   }

   @NonNull
   public Micronaut environments(@Nullable String... environments) {
      return (Micronaut)super.environments(environments);
   }

   @NonNull
   public Micronaut defaultEnvironments(@Nullable String... environments) {
      return (Micronaut)super.defaultEnvironments(environments);
   }

   @NonNull
   public Micronaut packages(@Nullable String... packages) {
      return (Micronaut)super.packages(packages);
   }

   public <T extends Throwable> Micronaut mapError(Class<T> exception, Function<T, Integer> mapper) {
      this.exitHandlers.put(exception, mapper);
      return this;
   }

   public static Micronaut build(String... args) {
      return new Micronaut().args(args);
   }

   public static ApplicationContext run(String... args) {
      return run(new Class[0], args);
   }

   public static ApplicationContext run(Class cls, String... args) {
      return run(new Class[]{cls}, args);
   }

   public static ApplicationContext run(Class[] classes, String... args) {
      return new Micronaut().classes(classes).args(args).start();
   }

   protected void handleStartupException(Environment environment, Throwable exception) {
      Function<Throwable, Integer> exitCodeMapper = (Function)this.exitHandlers.computeIfAbsent(exception.getClass(), exceptionType -> throwable -> 1);
      Integer code = (Integer)exitCodeMapper.apply(exception);
      if (code > 0 && !environment.getActiveNames().contains("test")) {
         if (LOG.isErrorEnabled()) {
            LOG.error("Error starting Micronaut server: " + exception.getMessage(), exception);
         }

         System.exit(code);
      }

      throw new ApplicationStartupException("Error starting Micronaut server: " + exception.getMessage(), exception);
   }

   private void printBanner() {
      if (this.isBannerEnabled()) {
         PrintStream out = System.out;
         Optional<URL> resource = this.getResourceLoader().getResource("micronaut-banner.txt");
         if (resource.isPresent()) {
            new ResourceBanner((URL)resource.get(), out).print();
         } else {
            new MicronautBanner(out).print();
         }

      }
   }
}

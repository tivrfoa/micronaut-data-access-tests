package io.micronaut.http.netty.channel;

import io.micronaut.context.BeanLocator;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
@Internal
@BootstrapContextCompatible
public class DefaultEventLoopGroupRegistry implements EventLoopGroupRegistry {
   private static final Logger LOG = LoggerFactory.getLogger(DefaultEventLoopGroupRegistry.class);
   private final EventLoopGroupFactory eventLoopGroupFactory;
   private final BeanLocator beanLocator;
   private final Map<EventLoopGroup, EventLoopGroupConfiguration> eventLoopGroups = new ConcurrentHashMap();

   public DefaultEventLoopGroupRegistry(EventLoopGroupFactory eventLoopGroupFactory, BeanLocator beanLocator) {
      this.eventLoopGroupFactory = eventLoopGroupFactory;
      this.beanLocator = beanLocator;
   }

   @PreDestroy
   void shutdown() {
      this.eventLoopGroups.forEach((eventLoopGroup, configuration) -> {
         try {
            long quietPeriod = configuration.getShutdownQuietPeriod().toMillis();
            long timeout = configuration.getShutdownTimeout().toMillis();
            eventLoopGroup.shutdownGracefully(quietPeriod, timeout, TimeUnit.MILLISECONDS);
         } catch (Throwable var6) {
            if (LOG.isWarnEnabled()) {
               LOG.warn("Error shutting down EventLoopGroup: {}", var6.getMessage(), var6);
            }
         }

      });
      this.eventLoopGroups.clear();
   }

   @EachBean(EventLoopGroupConfiguration.class)
   @Bean
   @BootstrapContextCompatible
   protected EventLoopGroup eventLoopGroup(EventLoopGroupConfiguration configuration) {
      String executor = (String)configuration.getExecutorName().orElse(null);
      EventLoopGroup eventLoopGroup;
      if (executor != null) {
         eventLoopGroup = (EventLoopGroup)this.beanLocator
            .findBean(Executor.class, Qualifiers.byName(executor))
            .map(
               executorService -> this.eventLoopGroupFactory
                     .createEventLoopGroup(configuration.getNumThreads(), executorService, (Integer)configuration.getIoRatio().orElse(null))
            )
            .orElseThrow(() -> new ConfigurationException("No executor service configured for name: " + executor));
      } else {
         ThreadFactory threadFactory = (ThreadFactory)this.beanLocator
            .findBean(ThreadFactory.class, Qualifiers.byName(configuration.getName()))
            .orElseGet(() -> new DefaultThreadFactory(configuration.getName() + "-" + DefaultThreadFactory.toPoolName(NioEventLoopGroup.class)));
         eventLoopGroup = this.eventLoopGroupFactory.createEventLoopGroup(configuration, threadFactory);
      }

      this.eventLoopGroups.put(eventLoopGroup, configuration);
      return eventLoopGroup;
   }

   @Singleton
   @Requires(
      missingProperty = "micronaut.netty.event-loops.default"
   )
   @Primary
   @BootstrapContextCompatible
   @Bean(
      typed = {EventLoopGroup.class}
   )
   protected EventLoopGroup defaultEventLoopGroup(@Named("netty") ThreadFactory threadFactory) {
      EventLoopGroupConfiguration configuration = new DefaultEventLoopGroupConfiguration();
      EventLoopGroup eventLoopGroup = this.eventLoopGroupFactory.createEventLoopGroup(configuration, threadFactory);
      this.eventLoopGroups.put(eventLoopGroup, configuration);
      return eventLoopGroup;
   }

   @NonNull
   @Override
   public EventLoopGroup getDefaultEventLoopGroup() {
      return this.beanLocator.getBean(EventLoopGroup.class);
   }

   @Override
   public Optional<EventLoopGroup> getEventLoopGroup(@NonNull String name) {
      ArgumentUtils.requireNonNull("name", name);
      return "default".equals(name)
         ? this.beanLocator.findBean(EventLoopGroup.class)
         : this.beanLocator.findBean(EventLoopGroup.class, Qualifiers.byName(name));
   }

   @Override
   public Optional<EventLoopGroupConfiguration> getEventLoopGroupConfiguration(@NonNull String name) {
      ArgumentUtils.requireNonNull("name", name);
      return this.beanLocator.findBean(EventLoopGroupConfiguration.class, Qualifiers.byName(name));
   }
}

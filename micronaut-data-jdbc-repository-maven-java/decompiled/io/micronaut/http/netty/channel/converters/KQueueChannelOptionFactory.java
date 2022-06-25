package io.micronaut.http.netty.channel.converters;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.TypeConverter;
import io.micronaut.core.convert.TypeConverterRegistrar;
import io.micronaut.http.netty.channel.KQueueAvailabilityCondition;
import io.netty.channel.ChannelOption;
import io.netty.channel.kqueue.AcceptFilter;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueChannelOption;
import io.netty.channel.unix.UnixChannelOption;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;

@Internal
@Singleton
@Requires(
   classes = {KQueue.class},
   condition = KQueueAvailabilityCondition.class
)
public class KQueueChannelOptionFactory implements ChannelOptionFactory, TypeConverterRegistrar {
   @Override
   public ChannelOption<?> channelOption(String name) {
      return DefaultChannelOptionFactory.channelOption(name, KQueueChannelOption.class, UnixChannelOption.class);
   }

   @Override
   public Object convertValue(ChannelOption<?> option, Object value, Environment env) {
      return DefaultChannelOptionFactory.convertValue(option, KQueueChannelOption.class, value, env);
   }

   @Override
   public void register(ConversionService<?> conversionService) {
      conversionService.addConverter(Map.class, AcceptFilter.class, (TypeConverter)((map, targetType, context) -> {
         Object filterName = map.get("filterName");
         Object filterArgs = map.get("filterArgs");
         return filterName != null && filterArgs != null ? Optional.of(new AcceptFilter(filterName.toString(), filterArgs.toString())) : Optional.empty();
      }));
   }

   static {
      KQueueChannelOption.SO_ACCEPTFILTER.name();
   }
}

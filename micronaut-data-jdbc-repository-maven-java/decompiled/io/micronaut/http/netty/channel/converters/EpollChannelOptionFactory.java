package io.micronaut.http.netty.channel.converters;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Internal;
import io.micronaut.http.netty.channel.EpollAvailabilityCondition;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.unix.UnixChannelOption;
import jakarta.inject.Singleton;

@Internal
@Singleton
@Requires(
   classes = {Epoll.class},
   condition = EpollAvailabilityCondition.class
)
public class EpollChannelOptionFactory implements ChannelOptionFactory {
   @Override
   public ChannelOption<?> channelOption(String name) {
      return DefaultChannelOptionFactory.channelOption(name, EpollChannelOption.class, UnixChannelOption.class);
   }

   @Override
   public Object convertValue(ChannelOption<?> option, Object value, Environment env) {
      return DefaultChannelOptionFactory.convertValue(option, EpollChannelOption.class, value, env);
   }

   static {
      EpollChannelOption.EPOLL_MODE.name();
   }
}

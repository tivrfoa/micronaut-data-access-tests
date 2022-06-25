package io.micronaut.http.netty.channel.converters;

import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Internal;
import io.netty.channel.ChannelOption;

@Internal
public interface ChannelOptionFactory {
   default ChannelOption<?> channelOption(String name) {
      return ChannelOption.valueOf(name);
   }

   default Object convertValue(ChannelOption<?> option, Object value, Environment env) {
      return DefaultChannelOptionFactory.convertValue(option, ChannelOption.class, value, env);
   }
}

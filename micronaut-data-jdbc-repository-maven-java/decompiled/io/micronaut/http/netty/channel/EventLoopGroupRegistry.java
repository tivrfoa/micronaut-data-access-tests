package io.micronaut.http.netty.channel;

import io.micronaut.core.annotation.NonNull;
import io.netty.channel.EventLoopGroup;
import java.util.Optional;

public interface EventLoopGroupRegistry {
   Optional<EventLoopGroup> getEventLoopGroup(@NonNull String name);

   @NonNull
   EventLoopGroup getDefaultEventLoopGroup();

   Optional<EventLoopGroupConfiguration> getEventLoopGroupConfiguration(@NonNull String name);
}

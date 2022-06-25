package io.micronaut.http.netty.channel;

import io.micronaut.core.annotation.NonNull;
import io.netty.channel.ChannelPipeline;

@FunctionalInterface
public interface ChannelPipelineListener {
   @NonNull
   ChannelPipeline onConnect(@NonNull ChannelPipeline pipeline);
}

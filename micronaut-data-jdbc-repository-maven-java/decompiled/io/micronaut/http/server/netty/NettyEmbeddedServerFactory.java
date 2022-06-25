package io.micronaut.http.server.netty;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.server.netty.configuration.NettyHttpServerConfiguration;
import io.micronaut.http.ssl.ServerSslConfiguration;

public interface NettyEmbeddedServerFactory {
   @NonNull
   NettyEmbeddedServer build(@NonNull NettyHttpServerConfiguration configuration);

   @NonNull
   default NettyEmbeddedServer build(@NonNull NettyHttpServerConfiguration configuration, @Nullable ServerSslConfiguration sslConfiguration) {
      return this.build(configuration, null);
   }
}

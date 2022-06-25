package io.micronaut.http.server.netty;

import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.RequestBinderRegistry;
import io.micronaut.http.server.binding.RequestArgumentSatisfier;
import jakarta.inject.Singleton;
import java.util.Optional;

@Primary
@Singleton
@Internal
public class NettyRequestArgumentSatisfier extends RequestArgumentSatisfier {
   public NettyRequestArgumentSatisfier(RequestBinderRegistry requestBinderRegistry) {
      super(requestBinderRegistry);
   }

   @Override
   protected Optional<Object> getValueForArgument(Argument argument, HttpRequest<?> request, boolean satisfyOptionals) {
      if (request instanceof NettyHttpRequest) {
         NettyHttpRequest nettyHttpRequest = (NettyHttpRequest)request;
         nettyHttpRequest.setBodyRequired(true);
      }

      return super.getValueForArgument(argument, request, satisfyOptionals);
   }
}

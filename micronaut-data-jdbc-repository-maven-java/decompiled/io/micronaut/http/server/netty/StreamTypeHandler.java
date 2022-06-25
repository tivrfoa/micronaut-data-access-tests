package io.micronaut.http.server.netty;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.server.netty.types.NettyCustomizableResponseTypeHandler;
import io.micronaut.http.server.netty.types.stream.NettyStreamedCustomizableResponseType;
import io.micronaut.http.server.types.CustomizableResponseTypeException;
import io.netty.channel.ChannelHandlerContext;
import java.io.InputStream;
import java.util.Arrays;

@Internal
class StreamTypeHandler implements NettyCustomizableResponseTypeHandler<Object> {
   private static final Class<?>[] SUPPORTED_TYPES = new Class[]{NettyStreamedCustomizableResponseType.class, InputStream.class};

   @Override
   public void handle(Object object, HttpRequest<?> request, MutableHttpResponse<?> response, ChannelHandlerContext context) {
      NettyStreamedCustomizableResponseType type;
      if (object instanceof InputStream) {
         type = () -> (InputStream)object;
      } else {
         if (!(object instanceof NettyStreamedCustomizableResponseType)) {
            throw new CustomizableResponseTypeException("StreamTypeHandler only supports InputStream or StreamedCustomizableResponseType types");
         }

         type = (NettyStreamedCustomizableResponseType)object;
      }

      type.process(response);
      type.write(request, response, context);
      context.read();
   }

   @Override
   public boolean supports(Class<?> type) {
      return Arrays.stream(SUPPORTED_TYPES).anyMatch(aClass -> aClass.isAssignableFrom(type));
   }

   @Override
   public int getOrder() {
      return 100;
   }
}

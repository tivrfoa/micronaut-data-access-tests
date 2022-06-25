package io.micronaut.http.server.netty;

import io.micronaut.core.annotation.Internal;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostMultipartRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.ReferenceCountUtil;
import java.nio.charset.Charset;

@Internal
class MicronautHttpPostMultipartRequestDecoder extends HttpPostMultipartRequestDecoder {
   MicronautHttpPostMultipartRequestDecoder(HttpDataFactory factory, HttpRequest request, Charset charset) {
      super(factory, request, charset);
   }

   @Override
   public void destroy() {
      super.destroy();
      InterfaceHttpData data = this.currentPartialHttpData();
      if (data != null && data.refCnt() != 0) {
         ReferenceCountUtil.safeRelease(data);
      }

   }
}

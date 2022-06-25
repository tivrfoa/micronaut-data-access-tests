package io.micronaut.http.server.netty.types.stream;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpVersion;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.netty.NettyMutableHttpResponse;
import io.micronaut.http.server.netty.NettyHttpRequest;
import io.micronaut.http.server.netty.types.NettyCustomizableResponseType;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
public interface NettyStreamedCustomizableResponseType extends NettyCustomizableResponseType {
   Logger LOG = LoggerFactory.getLogger(NettyStreamedCustomizableResponseType.class);

   InputStream getInputStream();

   @Override
   default void write(HttpRequest<?> request, MutableHttpResponse<?> response, ChannelHandlerContext context) {
      if (response instanceof NettyMutableHttpResponse) {
         NettyMutableHttpResponse nettyResponse = (NettyMutableHttpResponse)response;
         DefaultHttpResponse finalResponse = new DefaultHttpResponse(
            nettyResponse.getNettyHttpVersion(), nettyResponse.getNettyHttpStatus(), nettyResponse.getNettyHeaders()
         );
         HttpVersion httpVersion = request.getHttpVersion();
         boolean isHttp2 = httpVersion == HttpVersion.HTTP_2_0;
         if (request instanceof NettyHttpRequest) {
            ((NettyHttpRequest)request).prepareHttp2ResponseIfNecessary(finalResponse);
         }

         InputStream inputStream = this.getInputStream();
         context.write(finalResponse, context.voidPromise());
         if (inputStream != null) {
            ChannelFutureListener closeListener = future -> {
               try {
                  inputStream.close();
               } catch (IOException var3x) {
                  LOG.warn("An error occurred closing an input stream", var3x);
               }

            };
            HttpChunkedInput chunkedInput = new HttpChunkedInput(new ChunkedStream(inputStream));
            context.writeAndFlush(chunkedInput).addListener(closeListener);
         } else {
            context.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
         }

      } else {
         throw new IllegalArgumentException("Unsupported response type. Not a Netty response: " + response);
      }
   }

   @Override
   default void process(MutableHttpResponse<?> response) {
      response.header(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
   }
}

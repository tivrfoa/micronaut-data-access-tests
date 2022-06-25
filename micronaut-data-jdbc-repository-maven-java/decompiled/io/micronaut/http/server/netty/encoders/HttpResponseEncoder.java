package io.micronaut.http.server.netty.encoders;

import io.micronaut.buffer.netty.NettyByteBufferFactory;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.io.Writable;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.netty.NettyMutableHttpResponse;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.runtime.http.codec.TextPlainCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
@ChannelHandler.Sharable
public class HttpResponseEncoder extends MessageToMessageEncoder<MutableHttpResponse<?>> {
   public static final String ID = "micronaut-http-encoder";
   private static final Logger LOG = LoggerFactory.getLogger(HttpResponseEncoder.class);
   private final MediaTypeCodecRegistry mediaTypeCodecRegistry;
   private final HttpServerConfiguration serverConfiguration;

   public HttpResponseEncoder(MediaTypeCodecRegistry mediaTypeCodecRegistry, HttpServerConfiguration serverConfiguration) {
      this.mediaTypeCodecRegistry = mediaTypeCodecRegistry;
      this.serverConfiguration = serverConfiguration;
   }

   protected void encode(ChannelHandlerContext context, MutableHttpResponse<?> response, List<Object> out) {
      Optional<MediaType> specifiedMediaType = response.getContentType();
      MediaType responseMediaType = (MediaType)specifiedMediaType.orElse(MediaType.APPLICATION_JSON_TYPE);
      this.applyConfiguredHeaders(response.getHeaders());
      Optional<?> responseBody = response.getBody();
      if (responseBody.isPresent()) {
         Object body = responseBody.get();
         if (specifiedMediaType.isPresent()) {
            Optional<MediaTypeCodec> registeredCodec = this.mediaTypeCodecRegistry.findCodec(responseMediaType, body.getClass());
            if (registeredCodec.isPresent()) {
               MediaTypeCodec codec = (MediaTypeCodec)registeredCodec.get();
               response = this.encodeBodyWithCodec(response, body, codec, responseMediaType, context);
            }
         }

         Optional<MediaTypeCodec> registeredCodec = this.mediaTypeCodecRegistry.findCodec(MediaType.APPLICATION_JSON_TYPE, body.getClass());
         if (registeredCodec.isPresent()) {
            MediaTypeCodec codec = (MediaTypeCodec)registeredCodec.get();
            response = this.encodeBodyWithCodec(response, body, codec, responseMediaType, context);
         }

         MediaTypeCodec defaultCodec = new TextPlainCodec(this.serverConfiguration.getDefaultCharset());
         response = this.encodeBodyWithCodec(response, body, defaultCodec, responseMediaType, context);
      }

      if (response instanceof NettyMutableHttpResponse) {
         out.add(((NettyMutableHttpResponse)response).toHttpResponse());
      } else {
         HttpHeaders nettyHeaders = new DefaultHttpHeaders();

         for(Entry<String, List<String>> header : response.getHeaders()) {
            nettyHeaders.add((String)header.getKey(), (Iterable<?>)header.getValue());
         }

         Object b = response.getBody().orElse(null);
         ByteBuf body = b instanceof ByteBuf ? (ByteBuf)b : Unpooled.buffer(0);
         FullHttpResponse nettyResponse = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.valueOf(response.status().getCode(), response.status().getReason()),
            body,
            nettyHeaders,
            EmptyHttpHeaders.INSTANCE
         );
         out.add(nettyResponse);
      }

   }

   private void applyConfiguredHeaders(MutableHttpHeaders headers) {
      if (this.serverConfiguration.isDateHeader() && !headers.contains("Date")) {
         headers.date(LocalDateTime.now());
      }

      this.serverConfiguration.getServerHeader().ifPresent(server -> {
         if (!headers.contains("Server")) {
            headers.add(HttpHeaderNames.SERVER, server);
         }

      });
   }

   private MutableHttpResponse<?> encodeBodyWithCodec(
      MutableHttpResponse<?> response, Object body, MediaTypeCodec codec, MediaType mediaType, ChannelHandlerContext context
   ) {
      ByteBuf byteBuf = this.encodeBodyAsByteBuf(body, codec, context, response);
      int len = byteBuf.readableBytes();
      MutableHttpHeaders headers = response.getHeaders();
      if (!headers.contains("Content-Type")) {
         headers.add(HttpHeaderNames.CONTENT_TYPE, mediaType);
      }

      headers.remove("Content-Length");
      headers.add(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(len));
      this.setBodyContent(response, byteBuf);
      return response;
   }

   private MutableHttpResponse<?> setBodyContent(MutableHttpResponse response, Object bodyContent) {
      return response.body(bodyContent);
   }

   private ByteBuf encodeBodyAsByteBuf(Object body, MediaTypeCodec codec, ChannelHandlerContext context, MutableHttpResponse response) {
      ByteBuf byteBuf;
      if (body instanceof ByteBuf) {
         byteBuf = (ByteBuf)body;
      } else if (body instanceof ByteBuffer) {
         ByteBuffer byteBuffer = (ByteBuffer)body;
         Object nativeBuffer = byteBuffer.asNativeBuffer();
         if (nativeBuffer instanceof ByteBuf) {
            byteBuf = (ByteBuf)nativeBuffer;
         } else {
            byteBuf = Unpooled.wrappedBuffer(byteBuffer.asNioBuffer());
         }
      } else if (body instanceof byte[]) {
         byteBuf = Unpooled.wrappedBuffer((byte[])body);
      } else if (body instanceof Writable) {
         byteBuf = context.alloc().ioBuffer(128);
         ByteBufOutputStream outputStream = new ByteBufOutputStream(byteBuf);
         Writable writable = (Writable)body;

         try {
            writable.writeTo(outputStream, response.getCharacterEncoding());
         } catch (IOException var9) {
            if (LOG.isErrorEnabled()) {
               LOG.error(var9.getMessage());
            }
         }
      } else {
         if (LOG.isDebugEnabled()) {
            LOG.debug("Encoding emitted response object [{}] using codec: {}", body, codec);
         }

         byteBuf = codec.encode(body, new NettyByteBufferFactory(context.alloc())).asNativeBuffer();
      }

      return byteBuf;
   }
}

package io.micronaut.http.netty.stream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2EventAdapter;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2Stream;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.internal.ObjectUtil;
import java.util.concurrent.atomic.AtomicInteger;

public class StreamingInboundHttp2ToHttpAdapter extends Http2EventAdapter {
   protected final Http2Connection connection;
   protected final boolean validateHttpHeaders;
   private final int maxContentLength;
   private final Http2Connection.PropertyKey messageKey;
   private final boolean propagateSettings;
   private final Http2Connection.PropertyKey dataReadKey;

   public StreamingInboundHttp2ToHttpAdapter(Http2Connection connection, int maxContentLength, boolean validateHttpHeaders, boolean propagateSettings) {
      if (maxContentLength <= 0) {
         throw new IllegalArgumentException("maxContentLength: " + maxContentLength + " (expected: > 0)");
      } else {
         this.connection = ObjectUtil.checkNotNull(connection, "connection");
         this.maxContentLength = maxContentLength;
         this.validateHttpHeaders = validateHttpHeaders;
         this.propagateSettings = propagateSettings;
         this.messageKey = connection.newKey();
         this.dataReadKey = connection.newKey();
      }
   }

   public StreamingInboundHttp2ToHttpAdapter(Http2Connection connection, int maxContentLength) {
      this(connection, maxContentLength, true, true);
   }

   protected final void removeMessage(Http2Stream stream) {
      stream.removeProperty(this.messageKey);
   }

   protected final HttpMessage getMessage(Http2Stream stream) {
      return stream.getProperty(this.messageKey);
   }

   protected final void putMessage(Http2Stream stream, HttpMessage message) {
      stream.setProperty(this.dataReadKey, new AtomicInteger(0));
      stream.setProperty(this.messageKey, message);
   }

   @Override
   public void onStreamRemoved(Http2Stream stream) {
      this.removeMessage(stream);
   }

   protected void fireChannelRead(ChannelHandlerContext ctx, HttpContent msg, Http2Stream stream) {
      ctx.fireChannelRead(msg);
   }

   protected void fireChannelRead(ChannelHandlerContext ctx, HttpMessage msg, Http2Stream stream) {
      if (this.connection.isServer()) {
         ChannelHandlerContext context = ctx.pipeline().context("flow-control-handler");
         if (context != null) {
            context.fireChannelRead(msg);
         } else {
            ctx.fireChannelRead(msg);
         }
      } else {
         ctx.fireChannelRead(msg);
      }

   }

   protected HttpMessage newMessage(ChannelHandlerContext ctx, Http2Stream stream, Http2Headers headers, boolean validateHttpHeaders) throws Http2Exception {
      return (HttpMessage)(this.connection.isServer()
         ? HttpConversionUtil.toHttpRequest(stream.id(), headers, validateHttpHeaders)
         : HttpConversionUtil.toHttpResponse(stream.id(), headers, validateHttpHeaders));
   }

   protected HttpMessage processHeadersBegin(ChannelHandlerContext ctx, Http2Stream stream, Http2Headers headers, boolean allowAppend, boolean appendToTrailer) throws Http2Exception {
      HttpMessage msg = this.getMessage(stream);
      if (msg == null) {
         msg = this.newMessage(ctx, stream, headers, this.validateHttpHeaders);
         this.putMessage(stream, msg);
      } else if (allowAppend) {
         HttpConversionUtil.addHttp2ToHttpHeaders(stream.id(), headers, msg.headers(), HttpVersion.HTTP_1_1, appendToTrailer, msg instanceof HttpRequest);
      } else {
         msg = null;
      }

      return msg;
   }

   private void processHeadersEnd(ChannelHandlerContext ctx, Http2Stream stream, HttpMessage msg, boolean endOfStream) {
      if (endOfStream) {
         Object var6;
         if (this.connection.isServer()) {
            HttpRequest existing = (HttpRequest)msg;
            var6 = new DefaultFullHttpRequest(
               HttpVersion.HTTP_1_1, existing.method(), existing.uri(), Unpooled.EMPTY_BUFFER, existing.headers(), EmptyHttpHeaders.INSTANCE
            );
         } else {
            HttpResponse existing = (HttpResponse)msg;
            var6 = new DefaultFullHttpResponse(
               existing.protocolVersion(), existing.status(), Unpooled.EMPTY_BUFFER, existing.headers(), EmptyHttpHeaders.INSTANCE
            );
         }

         HttpUtil.setContentLength((HttpMessage)var6, 0L);
         this.fireChannelRead(ctx, (HttpMessage)var6, stream);
      } else {
         if (!msg.headers().contains(HttpHeaderNames.CONTENT_LENGTH)) {
            HttpUtil.setTransferEncodingChunked(msg, true);
         }

         this.fireChannelRead(ctx, msg, stream);
      }

   }

   @Override
   public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream) throws Http2Exception {
      Http2Stream stream = this.connection.stream(streamId);
      HttpMessage msg = this.getMessage(stream);
      if (msg == null) {
         throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Data Frame received for unknown stream id %d", streamId);
      } else {
         AtomicInteger dataRead = this.getDataRead(stream);
         int dataReadableBytes = data.readableBytes();
         int readSoFar = dataRead.getAndAdd(dataReadableBytes);
         if (readSoFar > this.maxContentLength - dataReadableBytes) {
            throw Http2Exception.connectionError(
               Http2Error.INTERNAL_ERROR, "Content length exceeded max of %d for stream id %d", this.maxContentLength, streamId
            );
         } else {
            if (endOfStream) {
               if (dataReadableBytes > 0) {
                  DefaultLastHttpContent content = new DefaultLastHttp2Content(data.retain(), stream);
                  this.fireChannelRead(ctx, content, stream);
               } else {
                  this.fireChannelRead(ctx, new DefaultLastHttp2Content(Unpooled.EMPTY_BUFFER, stream), stream);
               }
            } else {
               DefaultHttp2Content content = new DefaultHttp2Content(data.retain(), stream);
               this.fireChannelRead(ctx, content, stream);
            }

            return dataReadableBytes + padding;
         }
      }
   }

   private AtomicInteger getDataRead(Http2Stream stream) {
      Object demand = stream.getProperty(this.dataReadKey);
      if (demand instanceof AtomicInteger) {
         return (AtomicInteger)demand;
      } else {
         AtomicInteger newValue = new AtomicInteger(0);
         stream.setProperty(this.dataReadKey, newValue);
         return newValue;
      }
   }

   @Override
   public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding, boolean endOfStream) throws Http2Exception {
      Http2Stream stream = this.connection.stream(streamId);
      HttpMessage msg = this.processHeadersBegin(ctx, stream, headers, true, true);
      if (msg != null) {
         this.processHeadersEnd(ctx, stream, msg, endOfStream);
      }

   }

   @Override
   public void onHeadersRead(
      ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency, short weight, boolean exclusive, int padding, boolean endOfStream
   ) throws Http2Exception {
      Http2Stream stream = this.connection.stream(streamId);
      HttpMessage msg = this.processHeadersBegin(ctx, stream, headers, true, true);
      if (msg != null) {
         if (streamDependency != 0) {
            msg.headers().setInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_DEPENDENCY_ID.text(), streamDependency);
         }

         msg.headers().setShort(HttpConversionUtil.ExtensionHeaderNames.STREAM_WEIGHT.text(), weight);
         this.processHeadersEnd(ctx, stream, msg, endOfStream);
      }

   }

   @Override
   public void onRstStreamRead(ChannelHandlerContext ctx, int streamId, long errorCode) {
      Http2Stream stream = this.connection.stream(streamId);
      HttpMessage msg = this.getMessage(stream);
      if (msg != null) {
         this.onRstStreamRead(stream, msg);
      }

      stream.close();
   }

   @Override
   public void onPushPromiseRead(ChannelHandlerContext ctx, int streamId, int promisedStreamId, Http2Headers headers, int padding) throws Http2Exception {
      Http2Stream promisedStream = this.connection.stream(promisedStreamId);
      if (headers.status() == null) {
         headers.status(HttpResponseStatus.OK.codeAsText());
      }

      HttpMessage msg = this.processHeadersBegin(ctx, promisedStream, headers, false, false);
      if (msg == null) {
         throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Push Promise Frame received for pre-existing stream id %d", promisedStreamId);
      } else {
         msg.headers().setInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_PROMISE_ID.text(), streamId);
         msg.headers().setShort(HttpConversionUtil.ExtensionHeaderNames.STREAM_WEIGHT.text(), (short)16);
         this.processHeadersEnd(ctx, promisedStream, msg, false);
      }
   }

   @Override
   public void onSettingsRead(ChannelHandlerContext ctx, Http2Settings settings) throws Http2Exception {
      if (this.propagateSettings) {
         ctx.fireChannelRead(settings);
      }

   }

   protected void onRstStreamRead(Http2Stream stream, HttpMessage msg) {
      this.removeMessage(stream);
   }
}

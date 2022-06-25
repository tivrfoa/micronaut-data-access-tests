package io.micronaut.http.server.netty.types.files;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.SupplierUtil;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.netty.NettyMutableHttpResponse;
import io.micronaut.http.server.netty.NettyHttpRequest;
import io.micronaut.http.server.netty.SmartHttpContentCompressor;
import io.micronaut.http.server.netty.types.NettyFileCustomizableResponseType;
import io.micronaut.http.server.types.CustomizableResponseTypeException;
import io.micronaut.http.server.types.files.FileCustomizableResponseType;
import io.micronaut.http.server.types.files.SystemFile;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.Http2StreamChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetectorFactory;
import io.netty.util.ResourceLeakTracker;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
public class NettySystemFileCustomizableResponseType extends SystemFile implements NettyFileCustomizableResponseType {
   private static final int LENGTH_8K = 8192;
   private static final Logger LOG = LoggerFactory.getLogger(NettySystemFileCustomizableResponseType.class);
   protected Optional<FileCustomizableResponseType> delegate = Optional.empty();

   public NettySystemFileCustomizableResponseType(File file) {
      super(file);
      if (!file.canRead()) {
         throw new CustomizableResponseTypeException("Could not find file");
      }
   }

   public NettySystemFileCustomizableResponseType(SystemFile delegate) {
      this(delegate.getFile());
      this.delegate = Optional.of(delegate);
   }

   @Override
   public long getLastModified() {
      return this.delegate.map(FileCustomizableResponseType::getLastModified).orElse(super.getLastModified());
   }

   @Override
   public MediaType getMediaType() {
      return (MediaType)this.delegate.map(FileCustomizableResponseType::getMediaType).orElse(super.getMediaType());
   }

   @Override
   public void process(MutableHttpResponse response) {
      response.header("Content-Length", String.valueOf(this.getLength()));
      this.delegate.ifPresent(type -> type.process(response));
   }

   @Override
   public void write(HttpRequest<?> request, MutableHttpResponse<?> response, ChannelHandlerContext context) {
      if (response instanceof NettyMutableHttpResponse) {
         NettyMutableHttpResponse nettyResponse = (NettyMutableHttpResponse)response;
         DefaultHttpResponse finalResponse = new DefaultHttpResponse(
            nettyResponse.getNettyHttpVersion(), nettyResponse.getNettyHttpStatus(), nettyResponse.getNettyHeaders()
         );
         if (request instanceof NettyHttpRequest) {
            ((NettyHttpRequest)request).prepareHttp2ResponseIfNecessary(finalResponse);
         }

         context.write(finalResponse, context.voidPromise());
         NettySystemFileCustomizableResponseType.FileHolder file = new NettySystemFileCustomizableResponseType.FileHolder(this.getFile());
         if (context.pipeline().get(SslHandler.class) == null
            && context.pipeline().<SmartHttpContentCompressor>get(SmartHttpContentCompressor.class).shouldSkip(finalResponse)
            && !(context.channel() instanceof Http2StreamChannel)) {
            context.write(new DefaultFileRegion(file.raf.getChannel(), 0L, this.getLength()), context.newProgressivePromise()).addListener(file);
            context.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
         } else {
            try {
               HttpChunkedInput chunkedInput = new HttpChunkedInput(new ChunkedFile(file.raf, 0L, this.getLength(), 8192));
               context.writeAndFlush(chunkedInput, context.newProgressivePromise()).addListener(file);
            } catch (IOException var8) {
               throw new CustomizableResponseTypeException("Could not read file", var8);
            }
         }

      } else {
         throw new IllegalArgumentException("Unsupported response type. Not a Netty response: " + response);
      }
   }

   private static final class FileHolder implements ChannelFutureListener {
      private static final Supplier<ResourceLeakDetector<RandomAccessFile>> LEAK_DETECTOR = SupplierUtil.memoized(
         () -> ResourceLeakDetectorFactory.instance().newResourceLeakDetector(RandomAccessFile.class)
      );
      final RandomAccessFile raf;
      final long length;
      private final ResourceLeakTracker<RandomAccessFile> tracker;
      private final File file;

      FileHolder(File file) {
         this.file = file;

         try {
            this.raf = new RandomAccessFile(file, "r");
         } catch (FileNotFoundException var4) {
            throw new CustomizableResponseTypeException("Could not find file", var4);
         }

         this.tracker = ((ResourceLeakDetector)LEAK_DETECTOR.get()).track(this.raf);

         try {
            this.length = this.raf.length();
         } catch (IOException var3) {
            this.close();
            throw new CustomizableResponseTypeException("Could not determine file length", var3);
         }
      }

      public void operationComplete(@NonNull ChannelFuture future) throws Exception {
         this.close();
      }

      void close() {
         try {
            this.raf.close();
         } catch (IOException var2) {
            NettySystemFileCustomizableResponseType.LOG.warn("An error occurred closing the file reference: " + this.file.getAbsolutePath(), var2);
         }

         if (this.tracker != null) {
            this.tracker.close(this.raf);
         }

      }
   }
}

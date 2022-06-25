package io.micronaut.http.server.netty.types.files;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.netty.NettyMutableHttpResponse;
import io.micronaut.http.server.netty.NettyHttpRequest;
import io.micronaut.http.server.netty.configuration.NettyHttpServerConfiguration;
import io.micronaut.http.server.netty.types.NettyCustomizableResponseTypeHandler;
import io.micronaut.http.server.netty.types.NettyFileCustomizableResponseType;
import io.micronaut.http.server.types.CustomizableResponseTypeException;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.http.server.types.files.SystemFile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import java.io.File;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

@Internal
public class FileTypeHandler implements NettyCustomizableResponseTypeHandler<Object> {
   private static final String[] ENTITY_HEADERS = new String[]{
      "Allow",
      "Content-Encoding",
      "Content-Language",
      "Content-Length",
      "Content-Location",
      "Content-MD5",
      "Content-Range",
      "Content-Type",
      "Expires",
      "Last-Modified"
   };
   private static final Class<?>[] SUPPORTED_TYPES = new Class[]{File.class, StreamedFile.class, NettyFileCustomizableResponseType.class, SystemFile.class};
   private final NettyHttpServerConfiguration.FileTypeHandlerConfiguration configuration;

   public FileTypeHandler(NettyHttpServerConfiguration.FileTypeHandlerConfiguration configuration) {
      this.configuration = configuration;
   }

   @Override
   public void handle(Object obj, HttpRequest<?> request, MutableHttpResponse<?> response, ChannelHandlerContext context) {
      NettyFileCustomizableResponseType type;
      if (obj instanceof File) {
         type = new NettySystemFileCustomizableResponseType((File)obj);
      } else if (obj instanceof NettyFileCustomizableResponseType) {
         type = (NettyFileCustomizableResponseType)obj;
      } else if (obj instanceof StreamedFile) {
         type = new NettyStreamedFileCustomizableResponseType((StreamedFile)obj);
      } else {
         if (!(obj instanceof SystemFile)) {
            throw new CustomizableResponseTypeException("FileTypeHandler only supports File or FileCustomizableResponseType types");
         }

         type = new NettySystemFileCustomizableResponseType((SystemFile)obj);
      }

      long lastModified = type.getLastModified();
      ZonedDateTime ifModifiedSince = request.getHeaders().getDate("If-Modified-Since");
      if (ifModifiedSince != null) {
         long ifModifiedSinceDateSeconds = ifModifiedSince.toEpochSecond();
         long fileLastModifiedSeconds = lastModified / 1000L;
         if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
            FullHttpResponse nettyResponse = this.notModified(response);
            if (request instanceof NettyHttpRequest) {
               ((NettyHttpRequest)request).prepareHttp2ResponseIfNecessary(nettyResponse);
            }

            context.writeAndFlush(nettyResponse);
            return;
         }
      }

      if (!response.getHeaders().contains("Content-Type")) {
         response.header("Content-Type", type.getMediaType().toString());
      }

      this.setDateAndCacheHeaders(response, lastModified);
      type.process(response);
      type.write(request, response, context);
      context.read();
   }

   @Override
   public boolean supports(Class<?> type) {
      return Arrays.stream(SUPPORTED_TYPES).anyMatch(aClass -> aClass.isAssignableFrom(type));
   }

   protected void setDateAndCacheHeaders(MutableHttpResponse response, long lastModified) {
      MutableHttpHeaders headers = response.getHeaders();
      LocalDateTime now = LocalDateTime.now();
      headers.date(now);
      LocalDateTime cacheSeconds = now.plus((long)this.configuration.getCacheSeconds(), ChronoUnit.SECONDS);
      if (response.header("Expires") == null) {
         headers.expires(cacheSeconds);
      }

      if (response.header("Cache-Control") == null) {
         NettyHttpServerConfiguration.FileTypeHandlerConfiguration.CacheControlConfiguration cacheConfig = this.configuration.getCacheControl();
         StringBuilder header = new StringBuilder(cacheConfig.getPublic() ? "public" : "private")
            .append(", max-age=")
            .append(this.configuration.getCacheSeconds());
         response.header("Cache-Control", header.toString());
      }

      if (response.header("Last-Modified") == null) {
         headers.lastModified(lastModified);
      }

   }

   protected void setDateHeader(MutableHttpResponse response) {
      MutableHttpHeaders headers = response.getHeaders();
      LocalDateTime now = LocalDateTime.now();
      headers.date(now);
   }

   private static void copyNonEntityHeaders(MutableHttpResponse<?> from, MutableHttpResponse to) {
      from.getHeaders().forEachValue((header, value) -> {
         if (Arrays.binarySearch(ENTITY_HEADERS, header) < 0) {
            to.getHeaders().add(header, value);
         }

      });
   }

   private FullHttpResponse notModified(MutableHttpResponse<?> originalResponse) {
      MutableHttpResponse response = HttpResponse.notModified();
      copyNonEntityHeaders(originalResponse, response);
      this.setDateHeader(response);
      return ((NettyMutableHttpResponse)response).toFullHttpResponse();
   }
}

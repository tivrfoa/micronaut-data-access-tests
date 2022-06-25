package io.micronaut.http.server.netty.types.files;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.server.netty.types.NettyFileCustomizableResponseType;
import io.micronaut.http.server.netty.types.stream.NettyStreamedCustomizableResponseType;
import io.micronaut.http.server.types.files.StreamedFile;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

@Internal
public class NettyStreamedFileCustomizableResponseType extends StreamedFile implements NettyFileCustomizableResponseType, NettyStreamedCustomizableResponseType {
   private final Optional<StreamedFile> delegate;

   public NettyStreamedFileCustomizableResponseType(InputStream inputStream, String name) {
      super(inputStream, MediaType.forFilename(name));
      this.delegate = Optional.empty();
   }

   public NettyStreamedFileCustomizableResponseType(InputStream inputStream, MediaType mediaType) {
      super(inputStream, mediaType);
      this.delegate = Optional.empty();
   }

   public NettyStreamedFileCustomizableResponseType(URL url) {
      super(url);
      this.delegate = Optional.empty();
   }

   public NettyStreamedFileCustomizableResponseType(StreamedFile delegate) {
      super(delegate.getInputStream(), delegate.getMediaType(), delegate.getLastModified(), delegate.getLength());
      this.delegate = Optional.of(delegate);
   }

   @Override
   public void process(MutableHttpResponse response) {
      long length = this.getLength();
      if (length > -1L) {
         response.header(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(length));
      } else {
         response.header(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
      }

      this.delegate.ifPresent(type -> type.process(response));
   }
}

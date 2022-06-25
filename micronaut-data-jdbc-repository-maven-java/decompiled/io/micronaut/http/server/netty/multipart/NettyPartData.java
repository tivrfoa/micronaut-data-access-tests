package io.micronaut.http.server.netty.multipart;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.functional.ThrowingSupplier;
import io.micronaut.http.MediaType;
import io.micronaut.http.multipart.PartData;
import io.micronaut.http.server.netty.HttpDataReference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufUtil;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Supplier;

@Internal
public class NettyPartData implements PartData {
   private final Supplier<Optional<MediaType>> mediaTypeSupplier;
   private final ThrowingSupplier<ByteBuf, IOException> byteBufSupplier;

   public NettyPartData(HttpDataReference httpData, HttpDataReference.Component component) {
      this(httpData::getContentType, component::getByteBuf);
   }

   public NettyPartData(Supplier<Optional<MediaType>> mediaTypeSupplier, ThrowingSupplier<ByteBuf, IOException> byteBufSupplier) {
      this.mediaTypeSupplier = mediaTypeSupplier;
      this.byteBufSupplier = byteBufSupplier;
   }

   @Override
   public InputStream getInputStream() throws IOException {
      return new ByteBufInputStream(this.getByteBuf(), true);
   }

   @Override
   public byte[] getBytes() throws IOException {
      ByteBuf byteBuf = this.getByteBuf();

      byte[] var2;
      try {
         var2 = ByteBufUtil.getBytes(byteBuf);
      } finally {
         byteBuf.release();
      }

      return var2;
   }

   @Override
   public ByteBuffer getByteBuffer() throws IOException {
      ByteBuf byteBuf = this.getByteBuf();

      ByteBuffer var2;
      try {
         var2 = byteBuf.nioBuffer();
      } finally {
         byteBuf.release();
      }

      return var2;
   }

   @Override
   public Optional<MediaType> getContentType() {
      return (Optional<MediaType>)this.mediaTypeSupplier.get();
   }

   public ByteBuf getByteBuf() throws IOException {
      return this.byteBufSupplier.get();
   }
}

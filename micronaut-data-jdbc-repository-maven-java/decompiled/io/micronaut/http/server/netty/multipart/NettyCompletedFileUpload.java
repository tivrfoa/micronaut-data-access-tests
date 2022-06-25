package io.micronaut.http.server.netty.multipart;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.util.SupplierUtil;
import io.micronaut.http.MediaType;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetectorFactory;
import io.netty.util.ResourceLeakTracker;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Supplier;

@Internal
public class NettyCompletedFileUpload implements CompletedFileUpload {
   private static final Supplier<ResourceLeakDetector<NettyCompletedFileUpload>> RESOURCE_LEAK_DETECTOR = SupplierUtil.memoized(
      () -> ResourceLeakDetectorFactory.instance().newResourceLeakDetector(NettyCompletedFileUpload.class)
   );
   private final FileUpload fileUpload;
   private final boolean controlRelease;
   private final ResourceLeakTracker<NettyCompletedFileUpload> tracker;

   public NettyCompletedFileUpload(FileUpload fileUpload) {
      this(fileUpload, true);
   }

   public NettyCompletedFileUpload(FileUpload fileUpload, boolean controlRelease) {
      this.fileUpload = fileUpload;
      this.controlRelease = controlRelease;
      if (controlRelease) {
         fileUpload.retain();
         this.tracker = ((ResourceLeakDetector)RESOURCE_LEAK_DETECTOR.get()).track(this);
      } else {
         this.tracker = null;
      }

   }

   @Override
   public InputStream getInputStream() throws IOException {
      if (this.fileUpload.isInMemory()) {
         ByteBuf byteBuf = this.fileUpload.getByteBuf();
         if (byteBuf == null) {
            throw new IOException("The input stream has already been released");
         } else {
            return new ByteBufInputStream(byteBuf, this.controlRelease);
         }
      } else {
         File file = this.fileUpload.getFile();
         if (file == null) {
            throw new IOException("The input stream has already been released");
         } else {
            return new NettyFileUploadInputStream(this.fileUpload, this.controlRelease);
         }
      }
   }

   @Override
   public byte[] getBytes() throws IOException {
      ByteBuf byteBuf = this.fileUpload.getByteBuf();
      if (byteBuf == null) {
         throw new IOException("The bytes have already been released");
      } else {
         byte[] var2;
         try {
            var2 = ByteBufUtil.getBytes(byteBuf);
         } finally {
            this.discard();
         }

         return var2;
      }
   }

   @Override
   public ByteBuffer getByteBuffer() throws IOException {
      ByteBuf byteBuf = this.fileUpload.getByteBuf();
      if (byteBuf == null) {
         throw new IOException("The byte buffer has already been released");
      } else {
         ByteBuffer var2;
         try {
            var2 = byteBuf.nioBuffer();
         } finally {
            this.discard();
         }

         return var2;
      }
   }

   @Override
   public Optional<MediaType> getContentType() {
      return Optional.of(new MediaType(this.fileUpload.getContentType(), NameUtils.extension(this.fileUpload.getFilename())));
   }

   @Override
   public String getName() {
      return this.fileUpload.getName();
   }

   @Override
   public String getFilename() {
      return this.fileUpload.getFilename();
   }

   @Override
   public long getSize() {
      return this.fileUpload.length();
   }

   @Override
   public long getDefinedSize() {
      return this.fileUpload.definedLength();
   }

   @Override
   public boolean isComplete() {
      return this.fileUpload.isCompleted();
   }

   @Override
   public final void discard() {
      if (this.controlRelease) {
         this.fileUpload.release();
      }

      if (this.tracker != null) {
         this.tracker.close(this);
      }

   }
}

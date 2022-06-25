package io.micronaut.http.server.netty.multipart;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.netty.handler.codec.http.multipart.FileUpload;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@Internal
class NettyFileUploadInputStream extends FileInputStream {
   @NonNull
   private final FileUpload file;
   private final boolean releaseOnClose;
   private final AtomicBoolean closed = new AtomicBoolean();

   NettyFileUploadInputStream(@NonNull FileUpload file, boolean releaseOnClose) throws IOException {
      super(file.getFile());
      this.file = file;
      this.releaseOnClose = releaseOnClose;
   }

   public void close() throws IOException {
      try {
         super.close();
      } finally {
         if (this.releaseOnClose && this.closed.compareAndSet(false, true)) {
            this.file.release();
         }

      }

   }
}

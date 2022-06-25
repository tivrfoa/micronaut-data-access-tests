package io.micronaut.http.server.netty;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.MediaType;
import io.micronaut.http.multipart.StreamingFileUpload;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpData;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Sinks;

@Internal
public class HttpDataReference {
   private static final Logger LOG = LoggerFactory.getLogger(HttpDataReference.class);
   final AtomicReference<Sinks.Many<Object>> subject = new AtomicReference();
   final AtomicReference<StreamingFileUpload> upload = new AtomicReference();
   private final HttpData data;
   private final AtomicReference<RandomAccessFile> fileAccess = new AtomicReference();
   private final AtomicLong position = new AtomicLong(0L);
   private final List<HttpDataReference.Component> components = new ArrayList();

   HttpDataReference(HttpData data) {
      this.data = data;
      data.retain();
   }

   public Optional<MediaType> getContentType() {
      return this.data instanceof FileUpload ? Optional.of(MediaType.of(((FileUpload)this.data).getContentType())) : Optional.empty();
   }

   HttpDataReference.Component addComponent() throws IOException {
      long readable = this.readableBytes(this.data);
      long offset = this.position.getAndUpdate(p -> readable);
      int length = (int)(readable - offset);
      if (length == 0) {
         return null;
      } else {
         HttpDataReference.Component component = new HttpDataReference.Component(length, offset);
         this.components.add(component);
         if (!this.data.isInMemory()) {
            AtomicReference<IOException> error = new AtomicReference();
            this.fileAccess.getAndUpdate(channel -> {
               if (channel == null) {
                  try {
                     return new RandomAccessFile(this.data.getFile(), "r");
                  } catch (IOException var4x) {
                     error.set(var4x);
                  }
               }

               return channel;
            });
            IOException exception = (IOException)error.get();
            if (exception != null) {
               throw exception;
            }
         }

         return component;
      }
   }

   void removeComponent(int index) {
      HttpDataReference.Component component = (HttpDataReference.Component)this.components.get(index);
      this.components.remove(index);
      this.updateComponentOffsets(index);
      this.position.getAndUpdate(offset -> offset - (long)component.length);
   }

   private long readableBytes(HttpData httpData) throws IOException {
      if (httpData.isInMemory()) {
         ByteBuf byteBuf = httpData.getByteBuf();
         return byteBuf != null ? (long)byteBuf.readableBytes() : 0L;
      } else {
         return httpData.length();
      }
   }

   private void updateComponentOffsets(int index) {
      int size = this.components.size();
      if (size > index) {
         HttpDataReference.Component c = (HttpDataReference.Component)this.components.get(index);
         if (index == 0) {
            c.offset = 0L;
            ++index;
         }

         for(int i = index; i < size; ++i) {
            HttpDataReference.Component prev = (HttpDataReference.Component)this.components.get(i - 1);
            HttpDataReference.Component cur = (HttpDataReference.Component)this.components.get(i);
            cur.offset = (long)prev.length;
         }

      }
   }

   void destroy() {
      this.fileAccess.getAndUpdate(channel -> {
         if (channel != null) {
            try {
               channel.close();
            } catch (IOException var2) {
               LOG.warn("Error closing file channel for disk file upload", var2);
            }
         }

         return null;
      });
      this.data.release();
   }

   public final class Component {
      private final int length;
      private long offset;

      private Component(int length, long offset) {
         this.length = length;
         this.offset = offset;
      }

      private ByteBuf createDelegate(ByteBuf byteBuf, BiPredicate<ByteBuf, Integer> onRelease) {
         return (ByteBuf)(byteBuf == null ? Unpooled.EMPTY_BUFFER : new ByteBufDelegate(byteBuf) {
            @Override
            public boolean release() {
               return onRelease.test(byteBuf, 1);
            }

            @Override
            public boolean release(int decrement) {
               return onRelease.test(byteBuf, decrement);
            }
         });
      }

      public ByteBuf getByteBuf() throws IOException {
         if (this.length == 0) {
            return Unpooled.EMPTY_BUFFER;
         } else if (HttpDataReference.this.data.isInMemory()) {
            ByteBuf byteBuf = HttpDataReference.this.data.getByteBuf();
            int index = HttpDataReference.this.components.indexOf(this);
            if (byteBuf instanceof CompositeByteBuf) {
               CompositeByteBuf compositeByteBuf = (CompositeByteBuf)byteBuf;
               return this.createDelegate(compositeByteBuf.internalComponent(index), (buf, count) -> {
                  compositeByteBuf.removeComponent(index);
                  HttpDataReference.this.removeComponent(index);
                  return true;
               });
            } else {
               return this.createDelegate(byteBuf, (buf, count) -> {
                  try {
                     ByteBuf currentBuffer = HttpDataReference.this.data.getByteBuf();
                     if (currentBuffer instanceof CompositeByteBuf) {
                        ((CompositeByteBuf)currentBuffer).removeComponent(index);
                     } else {
                        HttpDataReference.this.data.delete();
                     }
                  } catch (IOException var5) {
                  }

                  HttpDataReference.this.removeComponent(index);
                  return true;
               });
            }
         } else {
            byte[] data = new byte[this.length];
            ((RandomAccessFile)HttpDataReference.this.fileAccess.get()).getChannel().read(ByteBuffer.wrap(data), this.offset);
            return Unpooled.wrappedBuffer(data);
         }
      }
   }
}

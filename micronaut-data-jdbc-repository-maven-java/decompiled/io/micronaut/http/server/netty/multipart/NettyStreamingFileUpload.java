package io.micronaut.http.server.netty.multipart;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.publisher.AsyncSingleResultPublisher;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.util.functional.ThrowingSupplier;
import io.micronaut.http.MediaType;
import io.micronaut.http.multipart.MultipartException;
import io.micronaut.http.multipart.PartData;
import io.micronaut.http.multipart.StreamingFileUpload;
import io.micronaut.http.server.HttpServerConfiguration;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.FileUpload;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Internal
public class NettyStreamingFileUpload implements StreamingFileUpload {
   private static final Logger LOG = LoggerFactory.getLogger(NettyStreamingFileUpload.class);
   private FileUpload fileUpload;
   private final ExecutorService ioExecutor;
   private final HttpServerConfiguration.MultipartConfiguration configuration;
   private final Flux<PartData> subject;

   public NettyStreamingFileUpload(
      FileUpload httpData, HttpServerConfiguration.MultipartConfiguration multipartConfiguration, ExecutorService ioExecutor, Flux<PartData> subject
   ) {
      this.configuration = multipartConfiguration;
      this.fileUpload = httpData;
      this.ioExecutor = ioExecutor;
      this.subject = subject;
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
   public Publisher<Boolean> transferTo(String location) {
      String baseDirectory = (String)this.configuration.getLocation().map(File::getAbsolutePath).orElse(DiskFileUpload.baseDirectory);
      File file = baseDirectory == null ? this.createTemp(location) : new File(baseDirectory, location);
      return this.transferTo(file);
   }

   @Override
   public Publisher<Boolean> transferTo(File destination) {
      return this.transferTo((ThrowingSupplier<OutputStream, IOException>)(() -> Files.newOutputStream(destination.toPath())));
   }

   @Override
   public Publisher<Boolean> transferTo(OutputStream outputStream) {
      return this.transferTo((ThrowingSupplier<OutputStream, IOException>)(() -> outputStream));
   }

   @Override
   public Publisher<Boolean> delete() {
      return new AsyncSingleResultPublisher(this.ioExecutor, () -> {
         this.fileUpload.delete();
         return true;
      });
   }

   protected File createTemp(String location) {
      try {
         return Files.createTempFile("FUp_", ".tmp_" + location).toFile();
      } catch (IOException var3) {
         throw new MultipartException("Unable to create temp file: " + var3.getMessage(), var3);
      }
   }

   @Override
   public void subscribe(Subscriber<? super PartData> s) {
      this.subject.subscribe(s);
   }

   @Override
   public void discard() {
      this.fileUpload.release();
   }

   private Publisher<Boolean> transferTo(ThrowingSupplier<OutputStream, IOException> outputStreamSupplier) {
      return Mono.create(emitter -> this.subject.subscribeOn(Schedulers.fromExecutorService(this.ioExecutor)).subscribe(new Subscriber<PartData>() {
            Subscription subscription;
            OutputStream outputStream;

            @Override
            public void onSubscribe(Subscription s) {
               this.subscription = s;
               this.subscription.request(1L);

               try {
                  this.outputStream = (OutputStream)outputStreamSupplier.get();
               } catch (IOException var3) {
                  this.handleError(var3);
               }

            }

            public void onNext(PartData o) {
               try {
                  this.outputStream.write(o.getBytes());
                  this.subscription.request(1L);
               } catch (IOException var3) {
                  this.handleError(var3);
               }

            }

            @Override
            public void onError(Throwable t) {
               emitter.error(t);

               try {
                  if (this.outputStream != null) {
                     this.outputStream.close();
                  }
               } catch (IOException var3) {
                  if (NettyStreamingFileUpload.LOG.isWarnEnabled()) {
                     NettyStreamingFileUpload.LOG.warn("Failed to close file stream : " + NettyStreamingFileUpload.this.fileUpload.getName());
                  }
               }

            }

            @Override
            public void onComplete() {
               try {
                  this.outputStream.close();
                  emitter.success(true);
               } catch (IOException var2) {
                  if (NettyStreamingFileUpload.LOG.isWarnEnabled()) {
                     NettyStreamingFileUpload.LOG.warn("Failed to close file stream : " + NettyStreamingFileUpload.this.fileUpload.getName());
                  }

                  emitter.success(false);
               }

            }

            private void handleError(Throwable t) {
               this.subscription.cancel();
               this.onError(new MultipartException("Error transferring file: " + NettyStreamingFileUpload.this.fileUpload.getName(), t));
            }
         })).flux();
   }
}

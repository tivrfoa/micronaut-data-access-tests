package io.micronaut.http.multipart;

import java.io.File;
import java.io.OutputStream;
import org.reactivestreams.Publisher;

public interface StreamingFileUpload extends FileUpload, Publisher<PartData> {
   @Deprecated
   Publisher<Boolean> transferTo(String location);

   Publisher<Boolean> transferTo(File destination);

   default Publisher<Boolean> transferTo(OutputStream outputStream) {
      throw new UnsupportedOperationException("StreamingFileUpload doesn't support transferTo OutputStream");
   }

   Publisher<Boolean> delete();
}

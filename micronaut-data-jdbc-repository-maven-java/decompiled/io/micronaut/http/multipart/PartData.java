package io.micronaut.http.multipart;

import io.micronaut.http.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Optional;

public interface PartData {
   InputStream getInputStream() throws IOException;

   byte[] getBytes() throws IOException;

   ByteBuffer getByteBuffer() throws IOException;

   Optional<MediaType> getContentType();
}

package io.micronaut.inject.writer;

import io.micronaut.core.annotation.Internal;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Files;

@Internal
class FileBackedGeneratedFile implements GeneratedFile {
   private final File file;

   FileBackedGeneratedFile(File file) {
      this.file = file;
   }

   @Override
   public URI toURI() {
      return this.file.toURI();
   }

   @Override
   public String getName() {
      return this.file.getName();
   }

   @Override
   public InputStream openInputStream() throws IOException {
      this.file.getParentFile().mkdirs();
      return Files.newInputStream(this.file.toPath());
   }

   @Override
   public OutputStream openOutputStream() throws IOException {
      this.file.getParentFile().mkdirs();
      return Files.newOutputStream(this.file.toPath());
   }

   @Override
   public Reader openReader() throws IOException {
      this.file.getParentFile().mkdirs();
      return Files.newBufferedReader(this.file.toPath());
   }

   @Override
   public CharSequence getTextContent() throws IOException {
      return this.file.exists() ? new String(Files.readAllBytes(this.file.toPath())) : null;
   }

   @Override
   public Writer openWriter() throws IOException {
      this.file.getParentFile().mkdirs();
      return Files.newBufferedWriter(this.file.toPath());
   }
}

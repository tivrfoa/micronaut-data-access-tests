package io.micronaut.inject.writer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;

public interface GeneratedFile {
   URI toURI();

   String getName();

   InputStream openInputStream() throws IOException;

   OutputStream openOutputStream() throws IOException;

   Reader openReader() throws IOException;

   CharSequence getTextContent() throws IOException;

   Writer openWriter() throws IOException;
}

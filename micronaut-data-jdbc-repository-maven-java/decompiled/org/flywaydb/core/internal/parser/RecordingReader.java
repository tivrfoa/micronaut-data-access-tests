package org.flywaydb.core.internal.parser;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

public class RecordingReader extends FilterReader {
   private boolean paused;
   private final Recorder recorder;

   RecordingReader(Recorder recorder, Reader in) {
      super(in);
      this.recorder = recorder;
   }

   public int read() throws IOException {
      int read = super.read();
      if (read != -1 && !this.paused) {
         this.recorder.record((char)read);
      }

      return read;
   }

   public void mark(int readAheadLimit) throws IOException {
      this.paused = true;
      super.mark(readAheadLimit);
   }

   public void reset() throws IOException {
      super.reset();
      this.paused = false;
   }
}

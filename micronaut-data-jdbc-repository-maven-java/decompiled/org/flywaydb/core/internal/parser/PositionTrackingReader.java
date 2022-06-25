package org.flywaydb.core.internal.parser;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

public class PositionTrackingReader extends FilterReader {
   private final PositionTracker tracker;
   private boolean paused;

   PositionTrackingReader(PositionTracker tracker, Reader in) {
      super(in);
      this.tracker = tracker;
   }

   public int read() throws IOException {
      int read = super.read();
      if (read != -1 && !this.paused) {
         this.tracker.nextPos();
         char c = (char)read;
         if (c == '\n') {
            this.tracker.linefeed();
         } else if (c == '\r') {
            this.tracker.carriageReturn();
         } else {
            if (!Character.isWhitespace(c)) {
               this.tracker.nextColIgnoringWhitespace();
            }

            this.tracker.nextCol();
         }
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

package org.flywaydb.core.internal.parser;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

public class UnboundedReadAheadReader extends FilterReader {
   protected ArrayList<int[]> buffers = new ArrayList();
   private int readIndex = 0;
   private int markIndex = -1;
   private int currentBuffersSize = 0;
   private static final int bufferSize = 512;

   protected UnboundedReadAheadReader(Reader in) {
      super(in);
   }

   public void mark(int readAheadLimit) throws IOException {
      this.markIndex = this.readIndex;
   }

   public void reset() throws IOException {
      this.readIndex = this.markIndex;
      this.freeBuffers();
      this.markIndex = -1;
   }

   public int read() throws IOException {
      if (this.readIndex < this.currentBuffersSize) {
         return this.getValue(this.readIndex++);
      } else {
         int read = this.in.read();
         if (this.markIndex != -1) {
            this.setValue(read);
            ++this.readIndex;
            ++this.currentBuffersSize;
         }

         return read;
      }
   }

   private int getValue(int index) {
      int buffersIndex = index / 512;
      int buffersOffset = index - buffersIndex * 512;
      return ((int[])this.buffers.get(buffersIndex))[buffersOffset];
   }

   private void setValue(int value) {
      int buffersIndex = this.readIndex / 512;
      int buffersOffset = this.readIndex - buffersIndex * 512;
      if (buffersOffset == 0) {
         this.buffers.add(new int[512]);
      }

      ((int[])this.buffers.get(buffersIndex))[buffersOffset] = value;
   }

   private void freeBuffers() {
      int buffersToRemove = this.markIndex / 512 - 1;

      for(int i = 0; i < buffersToRemove; ++i) {
         this.buffers.remove(0);
         this.readIndex -= 512;
         this.currentBuffersSize -= 512;
      }

   }
}

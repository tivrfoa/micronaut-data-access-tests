package org.flywaydb.core.internal.util;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

public class BomStrippingReader extends FilterReader {
   private static final int EMPTY_STREAM = -1;

   public BomStrippingReader(Reader in) {
      super(in);
   }

   public int read() throws IOException {
      int c = super.read();
      return c != -1 && BomFilter.isBom((char)c) ? super.read() : c;
   }
}

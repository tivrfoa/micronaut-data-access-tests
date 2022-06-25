package org.flywaydb.core.internal.parser;

public class PositionTracker {
   private int pos = 0;
   private int line = 1;
   private int col = 1;
   private int colIgnoringWhitespace = 1;
   private int markPos = 0;
   private int markLine = 1;
   private int markCol = 1;
   private int markColIgnoringWhitespace = 1;

   public void nextPos() {
      ++this.pos;
   }

   public void nextCol() {
      ++this.col;
   }

   public void nextColIgnoringWhitespace() {
      ++this.colIgnoringWhitespace;
   }

   public void linefeed() {
      ++this.line;
      this.col = 1;
      this.colIgnoringWhitespace = 1;
   }

   public void carriageReturn() {
      this.col = 1;
      this.colIgnoringWhitespace = 1;
   }

   public void mark() {
      this.markPos = this.pos;
      this.markLine = this.line;
      this.markCol = this.col;
      this.markColIgnoringWhitespace = this.colIgnoringWhitespace;
   }

   public void reset() {
      this.pos = this.markPos;
      this.line = this.markLine;
      this.col = this.markCol;
      this.colIgnoringWhitespace = this.markColIgnoringWhitespace;
   }

   public int getPos() {
      return this.pos;
   }

   public int getLine() {
      return this.line;
   }

   public int getCol() {
      return this.col;
   }

   public int getColIgnoringWhitespace() {
      return this.colIgnoringWhitespace;
   }
}

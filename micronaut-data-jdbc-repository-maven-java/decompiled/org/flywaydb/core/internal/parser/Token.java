package org.flywaydb.core.internal.parser;

public class Token {
   private final TokenType type;
   private final int pos;
   private final int line;
   private final int col;
   private final String text;
   private final String rawText;
   private final int parensDepth;

   public Token(TokenType type, int pos, int line, int col, String text, String rawText, int parensDepth) {
      this.type = type;
      this.pos = pos;
      this.line = line;
      this.col = col;
      this.text = text;
      this.rawText = rawText;
      this.parensDepth = parensDepth;
   }

   public TokenType getType() {
      return this.type;
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

   public String getText() {
      return this.text;
   }

   public String getRawText() {
      return this.rawText;
   }

   public int getParensDepth() {
      return this.parensDepth;
   }
}

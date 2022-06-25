package ch.qos.logback.core.subst;

public class Token {
   public static final Token START_TOKEN = new Token(Token.Type.START, null);
   public static final Token CURLY_LEFT_TOKEN = new Token(Token.Type.CURLY_LEFT, null);
   public static final Token CURLY_RIGHT_TOKEN = new Token(Token.Type.CURLY_RIGHT, null);
   public static final Token DEFAULT_SEP_TOKEN = new Token(Token.Type.DEFAULT, null);
   Token.Type type;
   String payload;

   public Token(Token.Type type, String payload) {
      this.type = type;
      this.payload = payload;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Token token = (Token)o;
         if (this.type != token.type) {
            return false;
         } else {
            if (this.payload != null) {
               if (!this.payload.equals(token.payload)) {
                  return false;
               }
            } else if (token.payload != null) {
               return false;
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.type != null ? this.type.hashCode() : 0;
      return 31 * result + (this.payload != null ? this.payload.hashCode() : 0);
   }

   public String toString() {
      String result = "Token{type=" + this.type;
      if (this.payload != null) {
         result = result + ", payload='" + this.payload + '\'';
      }

      return result + '}';
   }

   public static enum Type {
      LITERAL,
      START,
      CURLY_LEFT,
      CURLY_RIGHT,
      DEFAULT;
   }
}

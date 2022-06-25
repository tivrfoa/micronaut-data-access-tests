package org.flywaydb.core.internal.database.cockroachdb;

import java.io.IOException;
import java.util.List;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParserContext;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.parser.PeekingReader;
import org.flywaydb.core.internal.parser.Token;
import org.flywaydb.core.internal.parser.TokenType;

public class CockroachDBParser extends Parser {
   public CockroachDBParser(Configuration configuration, ParsingContext parsingContext) {
      super(configuration, parsingContext, 3);
   }

   @Override
   protected char getAlternativeStringLiteralQuote() {
      return '$';
   }

   @Override
   protected Token handleAlternativeStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
      String dollarQuote = (char)reader.read() + reader.readUntilIncluding('$');
      reader.swallowUntilExcluding(dollarQuote);
      reader.swallow(dollarQuote.length());
      return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
   }

   @Override
   protected Boolean detectCanExecuteInTransaction(String simplifiedStatement, List<Token> keywords) {
      return false;
   }
}

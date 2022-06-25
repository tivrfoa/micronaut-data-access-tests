package org.flywaydb.core.internal.database.derby;

import java.io.IOException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParserContext;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.parser.PeekingReader;
import org.flywaydb.core.internal.parser.Token;
import org.flywaydb.core.internal.parser.TokenType;

public class DerbyParser extends Parser {
   public DerbyParser(Configuration configuration, ParsingContext parsingContext) {
      super(configuration, parsingContext, 3);
   }

   @Override
   protected char getAlternativeStringLiteralQuote() {
      return '$';
   }

   @Override
   protected Token handleAlternativeStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
      reader.swallow(2);
      reader.swallowUntilExcluding("$$");
      reader.swallow(2);
      return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
   }
}

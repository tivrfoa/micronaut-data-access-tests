package org.flywaydb.core.internal.database.snowflake;

import java.io.IOException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParserContext;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.parser.PeekingReader;
import org.flywaydb.core.internal.parser.Token;
import org.flywaydb.core.internal.parser.TokenType;

public class SnowflakeParser extends Parser {
   private final String ALTERNATIVE_QUOTE = "$$";

   public SnowflakeParser(Configuration configuration, ParsingContext parsingContext) {
      super(configuration, parsingContext, 2);
   }

   @Override
   protected boolean isAlternativeStringLiteral(String peek) {
      return peek.startsWith("$$") ? true : super.isAlternativeStringLiteral(peek);
   }

   @Override
   protected Token handleAlternativeStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
      reader.swallow("$$".length());
      reader.swallowUntilExcluding("$$");
      reader.swallow("$$".length());
      return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
   }

   @Override
   protected boolean isSingleLineComment(String peek, ParserContext context, int col) {
      return peek.startsWith("--") || peek.startsWith("//");
   }
}

package org.flywaydb.core.internal.database.informix;

import java.io.IOException;
import java.util.List;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParserContext;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.parser.PeekingReader;
import org.flywaydb.core.internal.parser.Token;

public class InformixParser extends Parser {
   public InformixParser(Configuration configuration, ParsingContext parsingContext) {
      super(configuration, parsingContext, 2);
   }

   @Override
   protected void adjustBlockDepth(ParserContext context, List<Token> tokens, Token keyword, PeekingReader reader) throws IOException {
      int lastKeywordIndex = this.getLastKeywordIndex(tokens);
      if (lastKeywordIndex >= 0) {
         String current = keyword.getText();
         if ("FUNCTION".equals(current) || "PROCEDURE".equals(current)) {
            String previous = ((Token)tokens.get(lastKeywordIndex)).getText();
            if ("CREATE".equals(previous) || "DBA".equals(previous)) {
               context.increaseBlockDepth(previous);
            } else if ("END".equals(previous)) {
               context.decreaseBlockDepth();
            }
         }

      }
   }
}

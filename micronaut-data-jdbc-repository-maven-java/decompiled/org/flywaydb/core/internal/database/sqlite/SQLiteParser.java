package org.flywaydb.core.internal.database.sqlite;

import java.io.IOException;
import java.util.List;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParserContext;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.parser.PeekingReader;
import org.flywaydb.core.internal.parser.Token;

public class SQLiteParser extends Parser {
   public SQLiteParser(Configuration configuration, ParsingContext parsingContext) {
      super(configuration, parsingContext, 3);
   }

   @Override
   protected char getAlternativeIdentifierQuote() {
      return '`';
   }

   @Override
   protected Boolean detectCanExecuteInTransaction(String simplifiedStatement, List<Token> keywords) {
      return "PRAGMA FOREIGN_KEYS".equals(simplifiedStatement) ? false : null;
   }

   @Override
   protected void adjustBlockDepth(ParserContext context, List<Token> tokens, Token keyword, PeekingReader reader) throws IOException {
      String lastKeyword = keyword.getText();
      if ("BEGIN".equals(lastKeyword) || "CASE".equals(lastKeyword)) {
         context.increaseBlockDepth(lastKeyword);
      } else if ("END".equals(lastKeyword)) {
         context.decreaseBlockDepth();
      }

   }
}

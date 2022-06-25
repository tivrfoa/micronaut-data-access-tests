package org.flywaydb.core.internal.database.redshift;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParserContext;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.parser.PeekingReader;
import org.flywaydb.core.internal.parser.Token;
import org.flywaydb.core.internal.parser.TokenType;

public class RedshiftParser extends Parser {
   private static final Pattern CREATE_LIBRARY_REGEX = Pattern.compile("^(CREATE|DROP) LIBRARY");
   private static final Pattern CREATE_EXTERNAL_TABLE_REGEX = Pattern.compile("^CREATE EXTERNAL TABLE");
   private static final Pattern VACUUM_REGEX = Pattern.compile("^VACUUM");
   private static final Pattern ALTER_TABLE_APPEND_FROM_REGEX = Pattern.compile("^ALTER TABLE( .*)? APPEND FROM");
   private static final Pattern ALTER_TABLE_ALTER_COLUMN_REGEX = Pattern.compile("^ALTER TABLE( .*)? ALTER COLUMN");

   public RedshiftParser(Configuration configuration, ParsingContext parsingContext) {
      super(configuration, parsingContext, 3);
   }

   @Override
   protected char getAlternativeStringLiteralQuote() {
      return '$';
   }

   @Override
   protected Boolean detectCanExecuteInTransaction(String simplifiedStatement, List<Token> keywords) {
      return !CREATE_LIBRARY_REGEX.matcher(simplifiedStatement).matches()
            && !CREATE_EXTERNAL_TABLE_REGEX.matcher(simplifiedStatement).matches()
            && !VACUUM_REGEX.matcher(simplifiedStatement).matches()
            && !ALTER_TABLE_APPEND_FROM_REGEX.matcher(simplifiedStatement).matches()
            && !ALTER_TABLE_ALTER_COLUMN_REGEX.matcher(simplifiedStatement).matches()
         ? null
         : false;
   }

   @Override
   protected Token handleAlternativeStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
      String dollarQuote = (char)reader.read() + reader.readUntilIncluding('$');
      reader.swallowUntilExcluding(dollarQuote);
      reader.swallow(dollarQuote.length());
      return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
   }
}

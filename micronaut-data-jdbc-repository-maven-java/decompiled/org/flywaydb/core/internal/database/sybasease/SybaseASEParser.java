package org.flywaydb.core.internal.database.sybasease;

import java.io.IOException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParserContext;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.parser.PeekingReader;
import org.flywaydb.core.internal.sqlscript.Delimiter;

public class SybaseASEParser extends Parser {
   public SybaseASEParser(Configuration configuration, ParsingContext parsingContext) {
      super(configuration, parsingContext, 3);
   }

   @Override
   protected Delimiter getDefaultDelimiter() {
      return Delimiter.GO;
   }

   @Override
   protected boolean isDelimiter(String peek, ParserContext context, int col, int colIgnoringWhitespace) {
      return peek.length() >= 2
         && (peek.charAt(0) == 'G' || peek.charAt(0) == 'g')
         && (peek.charAt(1) == 'O' || peek.charAt(1) == 'o')
         && (peek.length() == 2 || Character.isWhitespace(peek.charAt(2)));
   }

   @Override
   protected String readKeyword(PeekingReader reader, Delimiter delimiter, ParserContext context) throws IOException {
      return "" + (char)reader.read() + reader.readKeywordPart(null, context);
   }
}

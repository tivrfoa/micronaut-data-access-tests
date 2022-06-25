package org.flywaydb.core.internal.database.db2;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParserContext;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.parser.PeekingReader;
import org.flywaydb.core.internal.parser.Token;
import org.flywaydb.core.internal.parser.TokenType;

public class DB2Parser extends Parser {
   private static final String COMMENT_DIRECTIVE = "--#";
   private static final String SET_TERMINATOR_DIRECTIVE = "--#SET TERMINATOR ";
   private static final List<String> CONTROL_FLOW_KEYWORDS = Arrays.asList("LOOP", "CASE", "DO", "REPEAT", "IF");
   private static final Pattern CREATE_IF_NOT_EXISTS = Pattern.compile(".*CREATE\\s([^\\s]+\\s){0,2}IF\\sNOT\\sEXISTS");
   private static final Pattern DROP_IF_EXISTS = Pattern.compile(".*DROP\\s([^\\s]+\\s){0,2}IF\\sEXISTS");

   public DB2Parser(Configuration configuration, ParsingContext parsingContext) {
      super(configuration, parsingContext, "--#".length());
   }

   @Override
   protected void adjustBlockDepth(ParserContext context, List<Token> tokens, Token keyword, PeekingReader reader) throws IOException {
      boolean previousTokenIsKeyword = !tokens.isEmpty() && ((Token)tokens.get(tokens.size() - 1)).getType() == TokenType.KEYWORD;
      int lastKeywordIndex = this.getLastKeywordIndex(tokens);
      String previousKeyword = lastKeywordIndex >= 0 ? ((Token)tokens.get(lastKeywordIndex)).getText() : null;
      lastKeywordIndex = this.getLastKeywordIndex(tokens, lastKeywordIndex);
      String previousPreviousToken = lastKeywordIndex >= 0 ? ((Token)tokens.get(lastKeywordIndex)).getText() : null;
      if ((!"BEGIN".equals(keyword.getText()) || "ROW".equals(previousKeyword) && previousPreviousToken != null && !"EACH".equals(previousPreviousToken))
         && !CONTROL_FLOW_KEYWORDS.contains(keyword.getText())) {
         if ("END".equals(keyword.getText()) && !"ROW".equals(previousKeyword)
            || this.doTokensMatchPattern(tokens, keyword, CREATE_IF_NOT_EXISTS)
            || this.doTokensMatchPattern(tokens, keyword, DROP_IF_EXISTS)) {
            context.decreaseBlockDepth();
         }
      } else if (!previousTokenIsKeyword || !"END".equals(previousKeyword)) {
         context.increaseBlockDepth(keyword.getText());
      }

   }

   @Override
   protected void resetDelimiter(ParserContext context) {
   }

   @Override
   protected boolean isCommentDirective(String peek) {
      return peek.startsWith("--#");
   }

   @Override
   protected Token handleCommentDirective(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
      if ("--#SET TERMINATOR ".equals(reader.peek("--#SET TERMINATOR ".length()))) {
         reader.swallow("--#SET TERMINATOR ".length());
         String delimiter = reader.readUntilExcluding('\n', '\r');
         return new Token(TokenType.NEW_DELIMITER, pos, line, col, delimiter.trim(), delimiter, context.getParensDepth());
      } else {
         reader.swallowUntilExcluding('\n', '\r');
         return new Token(TokenType.COMMENT, pos, line, col, null, null, context.getParensDepth());
      }
   }
}

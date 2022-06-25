package org.flywaydb.database.mysql;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParserContext;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.parser.PeekingReader;
import org.flywaydb.core.internal.parser.StatementType;
import org.flywaydb.core.internal.parser.Token;
import org.flywaydb.core.internal.parser.TokenType;

public class MySQLParser extends Parser {
   private static final char ALTERNATIVE_SINGLE_LINE_COMMENT = '#';
   private static final Pattern STORED_PROGRAM_REGEX = Pattern.compile("^CREATE\\s(((DEFINER\\s@\\s)?(PROCEDURE|FUNCTION|EVENT))|TRIGGER)", 2);
   private static final StatementType STORED_PROGRAM_STATEMENT = new StatementType();

   public MySQLParser(Configuration configuration, ParsingContext parsingContext) {
      super(configuration, parsingContext, 8);
   }

   @Override
   protected void resetDelimiter(ParserContext context) {
   }

   @Override
   protected Token handleKeyword(PeekingReader reader, ParserContext context, int pos, int line, int col, String keyword) throws IOException {
      if ("DELIMITER".equalsIgnoreCase(keyword)) {
         String text = reader.readUntilExcluding('\n', '\r').trim();
         return new Token(TokenType.NEW_DELIMITER, pos, line, col, text, text, context.getParensDepth());
      } else {
         return super.handleKeyword(reader, context, pos, line, col, keyword);
      }
   }

   @Override
   protected char getIdentifierQuote() {
      return '`';
   }

   @Override
   protected char getAlternativeStringLiteralQuote() {
      return '"';
   }

   @Override
   protected boolean isSingleLineComment(String peek, ParserContext context, int col) {
      return super.isSingleLineComment(peek, context, col) || peek.charAt(0) == '#' && !this.isDelimiter(peek, context, col, 0);
   }

   @Override
   protected Token handleStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
      reader.swallow();
      reader.swallowUntilIncludingWithEscape('\'', true, '\\');
      return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
   }

   @Override
   protected Token handleAlternativeStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
      reader.swallow();
      reader.swallowUntilIncludingWithEscape('"', true, '\\');
      return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
   }

   @Override
   protected Token handleCommentDirective(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
      reader.swallow(2);
      String text = reader.readUntilExcluding("*/");
      reader.swallow(2);
      return new Token(TokenType.MULTI_LINE_COMMENT_DIRECTIVE, pos, line, col, text, text, context.getParensDepth());
   }

   @Override
   protected boolean isCommentDirective(String text) {
      return text.length() >= 8
         && text.charAt(0) == '/'
         && text.charAt(1) == '*'
         && text.charAt(2) == '!'
         && Character.isDigit(text.charAt(3))
         && Character.isDigit(text.charAt(4))
         && Character.isDigit(text.charAt(5))
         && Character.isDigit(text.charAt(6))
         && Character.isDigit(text.charAt(7));
   }

   @Override
   protected StatementType detectStatementType(String simplifiedStatement, ParserContext context, PeekingReader reader) {
      return STORED_PROGRAM_REGEX.matcher(simplifiedStatement).matches()
         ? STORED_PROGRAM_STATEMENT
         : super.detectStatementType(simplifiedStatement, context, reader);
   }

   @Override
   protected boolean shouldAdjustBlockDepth(ParserContext context, List<Token> tokens, Token token) {
      TokenType tokenType = token.getType();
      if (TokenType.DELIMITER.equals(tokenType) || ";".equals(token.getText())) {
         return true;
      } else if (TokenType.EOF.equals(tokenType)) {
         return true;
      } else {
         Token lastToken = getPreviousToken(tokens, context.getParensDepth());
         return lastToken != null && lastToken.getType() == TokenType.KEYWORD ? true : super.shouldAdjustBlockDepth(context, tokens, token);
      }
   }

   private boolean doesDelimiterEndFunction(List<Token> tokens, Token delimiter) {
      if (tokens.size() < 2) {
         return false;
      } else if (((Token)tokens.get(tokens.size() - 1)).getParensDepth() != delimiter.getParensDepth() + 1) {
         return false;
      } else {
         Token previousToken = getPreviousToken(tokens, delimiter.getParensDepth());
         return previousToken != null && ("IF".equals(previousToken.getText()) || "REPEAT".equals(previousToken.getText()));
      }
   }

   @Override
   protected void adjustBlockDepth(ParserContext context, List<Token> tokens, Token keyword, PeekingReader reader) {
      String keywordText = keyword.getText();
      int parensDepth = keyword.getParensDepth();
      if ("BEGIN".equalsIgnoreCase(keywordText) && context.getStatementType() == STORED_PROGRAM_STATEMENT) {
         context.increaseBlockDepth(Integer.toString(parensDepth));
      }

      if (context.getBlockDepth() > 0
         && lastTokenIs(tokens, parensDepth, "END")
         && !"IF".equalsIgnoreCase(keywordText)
         && !"LOOP".equalsIgnoreCase(keywordText)) {
         String initiator = context.getBlockInitiator();
         if (initiator.equals("") || initiator.equals(keywordText) || "AS".equalsIgnoreCase(keywordText) || initiator.equals(Integer.toString(parensDepth))) {
            context.decreaseBlockDepth();
         }
      }

      if ((";".equals(keywordText) || TokenType.DELIMITER.equals(keyword.getType()) || TokenType.EOF.equals(keyword.getType()))
         && context.getBlockDepth() > 0
         && this.doesDelimiterEndFunction(tokens, keyword)) {
         context.decreaseBlockDepth();
      }

   }
}

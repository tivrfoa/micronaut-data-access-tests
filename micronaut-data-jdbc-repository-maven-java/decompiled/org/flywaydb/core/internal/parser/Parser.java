package org.flywaydb.core.internal.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.api.resource.Resource;
import org.flywaydb.core.internal.resource.ResourceName;
import org.flywaydb.core.internal.resource.ResourceNameParser;
import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.ParsedSqlStatement;
import org.flywaydb.core.internal.sqlscript.SqlScriptMetadata;
import org.flywaydb.core.internal.sqlscript.SqlStatement;
import org.flywaydb.core.internal.sqlscript.SqlStatementIterator;
import org.flywaydb.core.internal.util.BomStrippingReader;
import org.flywaydb.core.internal.util.IOUtils;

public abstract class Parser {
   private static final Log LOG = LogFactory.getLog(Parser.class);
   public final Configuration configuration;
   private final int peekDepth;
   private final char identifierQuote;
   private final char alternativeIdentifierQuote;
   private final char alternativeStringLiteralQuote;
   private final Set<String> validKeywords;
   public final ParsingContext parsingContext;

   protected Parser(Configuration configuration, ParsingContext parsingContext, int peekDepth) {
      this.configuration = configuration;
      this.peekDepth = peekDepth;
      this.identifierQuote = this.getIdentifierQuote();
      this.alternativeIdentifierQuote = this.getAlternativeIdentifierQuote();
      this.alternativeStringLiteralQuote = this.getAlternativeStringLiteralQuote();
      this.validKeywords = this.getValidKeywords();
      this.parsingContext = parsingContext;
   }

   protected Delimiter getDefaultDelimiter() {
      return Delimiter.SEMICOLON;
   }

   protected char getIdentifierQuote() {
      return '"';
   }

   protected char getAlternativeIdentifierQuote() {
      return '\u0000';
   }

   protected char getAlternativeStringLiteralQuote() {
      return '\u0000';
   }

   protected char getOpeningIdentifierSymbol() {
      return '\u0000';
   }

   protected char getClosingIdentifierSymbol() {
      return '\u0000';
   }

   protected Set<String> getValidKeywords() {
      return null;
   }

   protected boolean supportsPeekingMultipleLines() {
      return true;
   }

   public final SqlStatementIterator parse(LoadableResource resource) {
      return this.parse(resource, null);
   }

   public final SqlStatementIterator parse(LoadableResource resource, SqlScriptMetadata metadata) {
      PositionTracker tracker = new PositionTracker();
      Recorder recorder = new Recorder();
      ParserContext context = new ParserContext(this.getDefaultDelimiter());
      String filename = resource.getFilename();
      LOG.debug("Parsing " + filename + " ...");
      ResourceName result = new ResourceNameParser(this.configuration).parse(filename);
      this.parsingContext.updateFilenamePlaceholder(result, this.configuration);
      PeekingReader peekingReader = new PeekingReader(
         new RecordingReader(
            recorder,
            new PositionTrackingReader(
               tracker, this.replacePlaceholders(new BomStrippingReader(new UnboundedReadAheadReader(new BufferedReader(resource.read(), 4096))), metadata)
            )
         ),
         this.supportsPeekingMultipleLines()
      );
      return new Parser.ParserSqlStatementIterator(peekingReader, resource, recorder, tracker, context);
   }

   protected Reader replacePlaceholders(Reader reader, SqlScriptMetadata metadata) {
      return (Reader)(!this.configuration.isPlaceholderReplacement() || metadata != null && !metadata.placeholderReplacement()
         ? reader
         : PlaceholderReplacingReader.create(this.configuration, this.parsingContext, reader));
   }

   protected SqlStatement getNextStatement(Resource resource, PeekingReader reader, Recorder recorder, PositionTracker tracker, ParserContext context) {
      this.resetDelimiter(context);
      context.setStatementType(StatementType.UNKNOWN);
      int statementLine = tracker.getLine();
      int statementCol = tracker.getCol();

      try {
         List<Token> tokens = new ArrayList();
         List<Token> keywords = new ArrayList();
         int statementPos = -1;
         recorder.start();
         int nonCommentPartPos = -1;
         int nonCommentPartLine = -1;
         int nonCommentPartCol = -1;
         StatementType statementType = StatementType.UNKNOWN;
         Boolean canExecuteInTransaction = null;
         String simplifiedStatement = "";

         while(true) {
            Token token = this.readToken(reader, tracker, context);
            if (token != null) {
               TokenType tokenType = token.getType();
               if (tokenType != TokenType.NEW_DELIMITER) {
                  if (!this.shouldDiscard(token, nonCommentPartPos >= 0)) {
                     if (this.shouldAdjustBlockDepth(context, tokens, token)) {
                        if (tokenType == TokenType.KEYWORD) {
                           keywords.add(token);
                        }

                        this.adjustBlockDepth(context, tokens, token, reader);
                     }

                     int parensDepth = token.getParensDepth();
                     int blockDepth = context.getBlockDepth();
                     if (TokenType.EOF == tokenType || TokenType.DELIMITER == tokenType && parensDepth == 0 && blockDepth == 0) {
                        String sql = recorder.stop();
                        if (TokenType.EOF != tokenType || !sql.trim().isEmpty() && !tokens.isEmpty() && nonCommentPartPos >= 0) {
                           if (canExecuteInTransaction == null) {
                              canExecuteInTransaction = this.determineCanExecuteInTransaction(simplifiedStatement, keywords, true);
                           }

                           if (TokenType.EOF != tokenType || parensDepth <= 0 && blockDepth <= 0) {
                              return this.createStatement(
                                 reader,
                                 recorder,
                                 statementPos,
                                 statementLine,
                                 statementCol,
                                 nonCommentPartPos,
                                 nonCommentPartLine,
                                 nonCommentPartCol,
                                 statementType,
                                 canExecuteInTransaction,
                                 context.getDelimiter(),
                                 sql.trim()
                              );
                           } else {
                              throw new FlywayException("Incomplete statement at line " + statementLine + " col " + statementCol + ": " + sql);
                           }
                        } else {
                           return null;
                        }
                     }

                     if (tokens.isEmpty() || tokens.stream().allMatch(t -> t.getType() == TokenType.BLANK_LINES || t.getType() == TokenType.COMMENT)) {
                        nonCommentPartPos = -1;
                        nonCommentPartLine = -1;
                        nonCommentPartCol = -1;
                        statementPos = token.getPos();
                        statementLine = token.getLine();
                        statementCol = token.getCol();
                     }

                     tokens.add(token);
                     recorder.confirm();
                     if (nonCommentPartPos < 0 && TokenType.COMMENT != tokenType && TokenType.DELIMITER != tokenType && TokenType.BLANK_LINES != tokenType) {
                        nonCommentPartPos = token.getPos();
                        nonCommentPartLine = token.getLine();
                        nonCommentPartCol = token.getCol();
                     }

                     if (keywords.size() <= this.getTransactionalDetectionCutoff()
                        && tokenType == TokenType.KEYWORD
                        && parensDepth == 0
                        && (statementType == StatementType.UNKNOWN || canExecuteInTransaction == null)) {
                        if (!simplifiedStatement.isEmpty()) {
                           simplifiedStatement = simplifiedStatement + " ";
                        }

                        simplifiedStatement = simplifiedStatement + token.getText().toUpperCase(Locale.ENGLISH);
                        if (statementType == StatementType.UNKNOWN) {
                           if (keywords.size() > this.getTransactionalDetectionCutoff()) {
                              statementType = StatementType.GENERIC;
                           } else {
                              statementType = this.detectStatementType(simplifiedStatement, context, reader);
                              context.setStatementType(statementType);
                           }

                           this.adjustDelimiter(context, statementType);
                        }

                        if (canExecuteInTransaction == null) {
                           canExecuteInTransaction = this.determineCanExecuteInTransaction(simplifiedStatement, keywords, null);
                        }
                     }
                  } else {
                     tokens.clear();
                     recorder.start();
                     statementLine = tracker.getLine();
                     statementCol = tracker.getCol();
                     simplifiedStatement = "";
                  }
               } else {
                  if (!tokens.isEmpty() && nonCommentPartPos >= 0) {
                     String sql = recorder.stop();
                     throw new FlywayException("Delimiter changed inside statement at line " + statementLine + " col " + statementCol + ": " + sql);
                  }

                  context.setDelimiter(new Delimiter(token.getText(), false));
                  tokens.clear();
                  recorder.start();
                  statementLine = tracker.getLine();
                  statementCol = tracker.getCol();
                  simplifiedStatement = "";
               }
            } else if (tokens.isEmpty()) {
               recorder.start();
               statementLine = tracker.getLine();
               statementCol = tracker.getCol();
               simplifiedStatement = "";
            } else {
               recorder.confirm();
            }
         }
      } catch (Exception var22) {
         IOUtils.close(reader);
         throw new FlywayException(
            "Unable to parse statement in "
               + resource.getAbsolutePath()
               + " at line "
               + statementLine
               + " col "
               + statementCol
               + ". See "
               + "https://rd.gt/3ipi7Pm"
               + " for more information: "
               + var22.getMessage(),
            var22
         );
      }
   }

   protected boolean shouldAdjustBlockDepth(ParserContext context, List<Token> tokens, Token token) {
      return token.getType() == TokenType.KEYWORD && token.getParensDepth() == 0;
   }

   protected boolean shouldDiscard(Token token, boolean nonCommentPartSeen) {
      return token.getType() == TokenType.DELIMITER && !nonCommentPartSeen;
   }

   protected void resetDelimiter(ParserContext context) {
      context.setDelimiter(this.getDefaultDelimiter());
   }

   protected void adjustDelimiter(ParserContext context, StatementType statementType) {
   }

   protected int getTransactionalDetectionCutoff() {
      return 10;
   }

   protected void adjustBlockDepth(ParserContext context, List<Token> tokens, Token keyword, PeekingReader reader) throws IOException {
   }

   protected int getLastKeywordIndex(List<Token> tokens) {
      return this.getLastKeywordIndex(tokens, tokens.size());
   }

   protected int getLastKeywordIndex(List<Token> tokens, int endIndex) {
      for(int i = endIndex - 1; i >= 0; --i) {
         Token token = (Token)tokens.get(i);
         if (token.getType() == TokenType.KEYWORD) {
            return i;
         }
      }

      return -1;
   }

   protected static Token getPreviousToken(List<Token> tokens, int parensDepth) {
      for(int i = tokens.size() - 1; i >= 0; --i) {
         Token previousToken = (Token)tokens.get(i);
         if (previousToken.getParensDepth() == parensDepth && previousToken.getType() != TokenType.COMMENT && previousToken.getType() != TokenType.BLANK_LINES) {
            return previousToken;
         }
      }

      return null;
   }

   protected static boolean lastTokenIs(List<Token> tokens, int parensDepth, String tokenText) {
      Token previousToken = getPreviousToken(tokens, parensDepth);
      return previousToken == null ? false : tokenText.equals(previousToken.getText());
   }

   protected static boolean lastTokenIsOnLine(List<Token> tokens, int parensDepth, int line) {
      Token previousToken = getPreviousToken(tokens, parensDepth);
      if (previousToken == null) {
         return false;
      } else {
         return previousToken.getLine() == line;
      }
   }

   protected static boolean tokenAtIndexIs(List<Token> tokens, int index, String tokenText) {
      return ((Token)tokens.get(index)).getText().equals(tokenText);
   }

   protected boolean doTokensMatchPattern(List<Token> previousTokens, Token current, Pattern regex) {
      ArrayList<String> tokenStrings = new ArrayList();
      tokenStrings.add(current.getText());

      for(int i = previousTokens.size() - 1; i >= 0; --i) {
         Token prevToken = (Token)previousTokens.get(i);
         if (prevToken.getParensDepth() != current.getParensDepth()) {
            break;
         }

         if (prevToken.getType() == TokenType.KEYWORD) {
            tokenStrings.add(prevToken.getText());
         }
      }

      StringBuilder builder = new StringBuilder();

      for(int i = tokenStrings.size() - 1; i >= 0; --i) {
         builder.append((String)tokenStrings.get(i));
         if (i != 0) {
            builder.append(" ");
         }
      }

      return regex.matcher(builder.toString()).matches();
   }

   protected ParsedSqlStatement createStatement(
      PeekingReader reader,
      Recorder recorder,
      int statementPos,
      int statementLine,
      int statementCol,
      int nonCommentPartPos,
      int nonCommentPartLine,
      int nonCommentPartCol,
      StatementType statementType,
      boolean canExecuteInTransaction,
      Delimiter delimiter,
      String sql
   ) throws IOException {
      return new ParsedSqlStatement(statementPos, statementLine, statementCol, sql, delimiter, canExecuteInTransaction);
   }

   protected StatementType detectStatementType(String simplifiedStatement, ParserContext context, PeekingReader reader) {
      return StatementType.UNKNOWN;
   }

   private Boolean determineCanExecuteInTransaction(String simplifiedStatement, List<Token> keywords, Boolean defaultValue) {
      if (keywords.size() > this.getTransactionalDetectionCutoff()) {
         return true;
      } else {
         Boolean canExecuteInTransaction = this.detectCanExecuteInTransaction(simplifiedStatement, keywords);
         if (canExecuteInTransaction == null) {
            canExecuteInTransaction = defaultValue;
         }

         return canExecuteInTransaction;
      }
   }

   protected Boolean detectCanExecuteInTransaction(String simplifiedStatement, List<Token> keywords) {
      return true;
   }

   private Token readToken(PeekingReader reader, PositionTracker tracker, ParserContext context) throws IOException {
      int pos = tracker.getPos();
      int line = tracker.getLine();
      int col = tracker.getCol();
      int colIgnoringWhitepace = tracker.getColIgnoringWhitespace();
      String peek = reader.peek(this.peekDepth);
      if (peek == null) {
         return new Token(TokenType.EOF, pos, line, col, null, null, 0);
      } else {
         char c = peek.charAt(0);
         if (this.isAlternativeStringLiteral(peek)) {
            return this.handleAlternativeStringLiteral(reader, context, pos, line, col);
         } else if (c == '\'') {
            return this.handleStringLiteral(reader, context, pos, line, col);
         } else if (c == '(') {
            context.increaseParensDepth();
            reader.swallow();
            return null;
         } else if (c == ')') {
            context.decreaseParensDepth();
            reader.swallow();
            return null;
         } else if (c == this.identifierQuote || c == this.alternativeIdentifierQuote) {
            reader.swallow();
            String text = reader.readUntilExcludingWithEscape(c, true);
            if (reader.peek('.')) {
               text = this.readAdditionalIdentifierParts(reader, c, context.getDelimiter(), context);
            }

            return new Token(TokenType.IDENTIFIER, pos, line, col, text, text, context.getParensDepth());
         } else if (this.isCommentDirective(peek)) {
            return this.handleCommentDirective(reader, context, pos, line, col);
         } else if (this.isSingleLineComment(peek, context, col)) {
            String text = reader.readUntilExcluding('\n', '\r');
            return new Token(TokenType.COMMENT, pos, line, col, text, text, context.getParensDepth());
         } else if (peek.startsWith("/*")) {
            int commentDepth = 0;
            reader.swallow(2);
            StringBuilder text = new StringBuilder(reader.readUntilExcluding("*/", "/*"));

            while(reader.peek("/*") || commentDepth > 0) {
               if (reader.peek("/*")) {
                  ++commentDepth;
               } else {
                  --commentDepth;
               }

               reader.swallow(2);
               text.append(reader.readUntilExcluding("*/", "/*"));
            }

            reader.swallow(2);
            return new Token(TokenType.COMMENT, pos, line, col, text.toString(), text.toString(), context.getParensDepth());
         } else if (Character.isDigit(c)) {
            String text = reader.readNumeric();
            return new Token(TokenType.NUMERIC, pos, line, col, text, text, context.getParensDepth());
         } else if (peek.startsWith("B'") || peek.startsWith("E'") || peek.startsWith("X'")) {
            reader.swallow(2);
            reader.swallowUntilIncludingWithEscape('\'', true, '\\');
            return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
         } else if (peek.startsWith("U&'")) {
            reader.swallow(3);
            reader.swallowUntilIncludingWithEscape('\'', true);
            return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
         } else if (this.isDelimiter(peek, context, col, colIgnoringWhitepace)) {
            return this.handleDelimiter(reader, context, pos, line, col);
         } else if (this.isOpeningIdentifier(c)) {
            String text = this.readIdentifier(reader);
            return new Token(TokenType.IDENTIFIER, pos, line, col, text, text, context.getParensDepth());
         } else if (this.isLetter(c, context)) {
            String text = this.readKeyword(reader, context.getDelimiter(), context);
            if (reader.peek('.')) {
               text = text + this.readAdditionalIdentifierParts(reader, this.identifierQuote, context.getDelimiter(), context);
            }

            return !this.isKeyword(text)
               ? new Token(TokenType.IDENTIFIER, pos, line, col, text, text, context.getParensDepth())
               : this.handleKeyword(reader, context, pos, line, col, text);
         } else if (c == ' ' || c == '\r' || c == 160) {
            reader.swallow();
            return null;
         } else if (Character.isWhitespace(c)) {
            String text = reader.readWhitespace();
            return containsAtLeast(text, '\n', 2) ? new Token(TokenType.BLANK_LINES, pos, line, col, text, text, context.getParensDepth()) : null;
         } else {
            String text = "" + (char)reader.read();
            return new Token(TokenType.SYMBOL, pos, line, col, text, text, context.getParensDepth());
         }
      }
   }

   protected String readKeyword(PeekingReader reader, Delimiter delimiter, ParserContext context) throws IOException {
      return "" + (char)reader.read() + reader.readKeywordPart(delimiter, context);
   }

   protected String readIdentifier(PeekingReader reader) throws IOException {
      return "" + (char)reader.read() + reader.readUntilIncluding(this.getClosingIdentifierSymbol());
   }

   protected Token handleDelimiter(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
      String text = context.getDelimiter().getDelimiter();
      reader.swallow(text.length());
      return new Token(TokenType.DELIMITER, pos, line, col, text, text, context.getParensDepth());
   }

   protected boolean isAlternativeStringLiteral(String peek) {
      return this.alternativeStringLiteralQuote != 0 && peek.charAt(0) == this.alternativeStringLiteralQuote;
   }

   protected boolean isDelimiter(String peek, ParserContext context, int col, int colIgnoringWhitespace) {
      return peek.startsWith(context.getDelimiter().getDelimiter());
   }

   protected boolean isLetter(char c, ParserContext context) {
      return c == '_' || context.isLetter(c);
   }

   private boolean isOpeningIdentifier(char c) {
      return c == this.getOpeningIdentifierSymbol();
   }

   protected boolean isSingleLineComment(String peek, ParserContext context, int col) {
      return peek.startsWith("--");
   }

   protected boolean isKeyword(String text) {
      for(int i = 0; i < text.length(); ++i) {
         char c = text.charAt(i);
         if (!Character.isLetter(c) && c != '_') {
            return false;
         }
      }

      return this.validKeywords != null ? this.validKeywords.contains(text) : true;
   }

   private String readAdditionalIdentifierParts(PeekingReader reader, char quote, Delimiter delimiter, ParserContext context) throws IOException {
      String result = "";
      reader.swallow();
      result = result + ".";
      if (reader.peek(quote)) {
         reader.swallow();
         result = result + reader.readUntilExcludingWithEscape(quote, true);
      } else {
         result = result + reader.readKeywordPart(delimiter, context);
      }

      if (reader.peek('.')) {
         reader.swallow();
         result = result + ".";
         if (reader.peek(quote)) {
            reader.swallow();
            result = result + reader.readUntilExcludingWithEscape(quote, true);
         } else {
            result = result + reader.readKeywordPart(delimiter, context);
         }
      }

      return result;
   }

   private List<Token> discardBlankLines(List<Token> tokens) {
      List<Token> nonBlankLinesTokens = new ArrayList(tokens);

      while(((Token)nonBlankLinesTokens.get(0)).getType() == TokenType.BLANK_LINES) {
         nonBlankLinesTokens.remove(0);
      }

      while(((Token)nonBlankLinesTokens.get(nonBlankLinesTokens.size() - 1)).getType() == TokenType.BLANK_LINES) {
         nonBlankLinesTokens.remove(nonBlankLinesTokens.size() - 1);
      }

      return nonBlankLinesTokens;
   }

   protected boolean isCommentDirective(String peek) {
      return false;
   }

   protected Token handleCommentDirective(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
      return null;
   }

   protected Token handleStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
      reader.swallow();
      reader.swallowUntilIncludingWithEscape('\'', true);
      return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
   }

   protected Token handleAlternativeStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
      return null;
   }

   protected Token handleKeyword(PeekingReader reader, ParserContext context, int pos, int line, int col, String keyword) throws IOException {
      return new Token(TokenType.KEYWORD, pos, line, col, keyword.toUpperCase(Locale.ENGLISH), keyword, context.getParensDepth());
   }

   private static boolean containsAtLeast(String str, char c, int min) {
      if (min > str.length()) {
         return false;
      } else {
         int count = 0;

         for(int i = 0; i < str.length(); ++i) {
            if (str.charAt(i) == c && ++count >= min) {
               return true;
            }
         }

         return false;
      }
   }

   public class ParserSqlStatementIterator implements SqlStatementIterator {
      private final PeekingReader peekingReader;
      private final LoadableResource resource;
      private final Recorder recorder;
      private final PositionTracker tracker;
      private final ParserContext context;
      private SqlStatement nextStatement;

      public ParserSqlStatementIterator(
         PeekingReader peekingReader, LoadableResource resource, Recorder recorder, PositionTracker tracker, ParserContext context
      ) {
         this.peekingReader = peekingReader;
         this.resource = resource;
         this.recorder = recorder;
         this.tracker = tracker;
         this.context = context;
         this.nextStatement = Parser.this.getNextStatement(resource, peekingReader, recorder, tracker, context);
      }

      @Override
      public void close() {
         IOUtils.close(this.peekingReader);
      }

      public boolean hasNext() {
         return this.nextStatement != null;
      }

      public SqlStatement next() {
         if (this.nextStatement == null) {
            throw new NoSuchElementException("No more statements in " + this.resource.getFilename());
         } else {
            SqlStatement result = this.nextStatement;
            this.nextStatement = Parser.this.getNextStatement(this.resource, this.peekingReader, this.recorder, this.tracker, this.context);
            return result;
         }
      }

      public void remove() {
         throw new UnsupportedOperationException("remove");
      }
   }
}

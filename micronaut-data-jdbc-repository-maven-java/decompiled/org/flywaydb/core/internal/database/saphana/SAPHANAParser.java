package org.flywaydb.core.internal.database.saphana;

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

public class SAPHANAParser extends Parser {
   private static final StatementType FUNCTION_OR_PROCEDURE_STATEMENT = new StatementType();
   private static final Pattern FUNCTION_OR_PROCEDURE_REGEX = Pattern.compile("^CREATE(\\sOR\\sREPLACE)?\\s(FUNCTION|PROCEDURE)");
   private static final StatementType ANONYMOUS_BLOCK_STATEMENT = new StatementType();
   private static final Pattern ANONYMOUS_BLOCK_REGEX = Pattern.compile("^DO.*BEGIN");

   public SAPHANAParser(Configuration configuration, ParsingContext parsingContext) {
      super(configuration, parsingContext, 2);
   }

   @Override
   protected StatementType detectStatementType(String simplifiedStatement, ParserContext context, PeekingReader reader) {
      if (FUNCTION_OR_PROCEDURE_REGEX.matcher(simplifiedStatement).matches()) {
         return FUNCTION_OR_PROCEDURE_STATEMENT;
      } else {
         return ANONYMOUS_BLOCK_REGEX.matcher(simplifiedStatement).matches()
            ? ANONYMOUS_BLOCK_STATEMENT
            : super.detectStatementType(simplifiedStatement, context, reader);
      }
   }

   @Override
   protected boolean shouldAdjustBlockDepth(ParserContext context, List<Token> tokens, Token token) {
      TokenType tokenType = token.getType();
      return context.getStatementType() != FUNCTION_OR_PROCEDURE_STATEMENT && context.getStatementType() != ANONYMOUS_BLOCK_STATEMENT
            || TokenType.EOF != tokenType && TokenType.DELIMITER != tokenType
         ? super.shouldAdjustBlockDepth(context, tokens, token)
         : true;
   }

   @Override
   protected void adjustBlockDepth(ParserContext context, List<Token> tokens, Token keyword, PeekingReader reader) throws IOException {
      int parensDepth = keyword.getParensDepth();
      if (!"BEGIN".equals(keyword.getText())
         && !"CASE".equals(keyword.getText())
         && !"DO".equals(keyword.getText())
         && (!"IF".equals(keyword.getText()) || lastTokenIs(tokens, parensDepth, "END"))) {
         if (this.doTokensMatchPattern(tokens, keyword, FUNCTION_OR_PROCEDURE_REGEX)) {
            context.increaseBlockDepth("FUNCTION_OR_PROCEDURE_REGEX");
         } else if ("END".equals(keyword.getText())) {
            context.decreaseBlockDepth();
         }
      } else {
         context.increaseBlockDepth(keyword.getText());
      }

      TokenType tokenType = keyword.getType();
      if ((context.getStatementType() == FUNCTION_OR_PROCEDURE_STATEMENT || context.getStatementType() == ANONYMOUS_BLOCK_STATEMENT)
         && (TokenType.EOF == tokenType || TokenType.DELIMITER == tokenType)
         && context.getBlockDepth() == 1
         && lastTokenIs(tokens, parensDepth, "END")) {
         context.decreaseBlockDepth();
      }
   }
}

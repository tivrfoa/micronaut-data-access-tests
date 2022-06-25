package org.flywaydb.core.internal.parser;

public enum TokenType {
   KEYWORD,
   IDENTIFIER,
   NUMERIC,
   STRING,
   COMMENT,
   MULTI_LINE_COMMENT_DIRECTIVE,
   PARENS_OPEN,
   PARENS_CLOSE,
   DELIMITER,
   NEW_DELIMITER,
   SYMBOL,
   BLANK_LINES,
   EOF,
   COPY_DATA;
}

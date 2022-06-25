package org.flywaydb.core.internal.parser;

import java.security.InvalidParameterException;
import java.util.Stack;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.sqlscript.Delimiter;

public class ParserContext {
   private int parensDepth = 0;
   private int blockDepth = 0;
   private final Stack<String> blockInitiators = new Stack();
   private String lastClosedBlockInitiator = null;
   private Delimiter delimiter;
   private StatementType statementType;

   public ParserContext(Delimiter delimiter) {
      this.delimiter = delimiter;
   }

   public void increaseParensDepth() {
      ++this.parensDepth;
   }

   public void decreaseParensDepth() {
      --this.parensDepth;
   }

   public int getParensDepth() {
      return this.parensDepth;
   }

   public String getLastClosedBlockInitiator() {
      return this.lastClosedBlockInitiator;
   }

   public void increaseBlockDepth(String blockInitiator) {
      this.blockInitiators.push(blockInitiator);
      ++this.blockDepth;
   }

   public void decreaseBlockDepth() {
      if (this.blockDepth == 0) {
         throw new FlywayException("Flyway parsing bug: unable to decrease block depth below 0");
      } else {
         --this.blockDepth;
         this.lastClosedBlockInitiator = (String)this.blockInitiators.pop();
      }
   }

   public int getBlockDepth() {
      return this.blockDepth;
   }

   public String getBlockInitiator() {
      return this.blockInitiators.size() > 0 ? (String)this.blockInitiators.peek() : "";
   }

   public Delimiter getDelimiter() {
      return this.delimiter;
   }

   public void setDelimiter(Delimiter delimiter) {
      this.delimiter = delimiter;
   }

   public StatementType getStatementType() {
      return this.statementType;
   }

   public void setStatementType(StatementType statementType) {
      if (statementType == null) {
         throw new InvalidParameterException("statementType must be non-null");
      } else {
         this.statementType = statementType;
      }
   }

   public boolean isLetter(char c) {
      if (Character.isLetter(c)) {
         return true;
      } else {
         return this.getStatementType() != StatementType.UNKNOWN ? this.statementType.treatAsIfLetter(c) : false;
      }
   }
}

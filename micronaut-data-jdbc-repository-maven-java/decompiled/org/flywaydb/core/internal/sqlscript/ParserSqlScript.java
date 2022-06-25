package org.flywaydb.core.internal.sqlscript;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.parser.Parser;

public class ParserSqlScript implements SqlScript {
   private static final Log LOG = LogFactory.getLog(ParserSqlScript.class);
   protected final List<SqlStatement> sqlStatements = new ArrayList();
   private int sqlStatementCount;
   private boolean nonTransactionalStatementFound;
   protected final LoadableResource resource;
   private final SqlScriptMetadata metadata;
   protected final Parser parser;
   private final boolean mixed;
   private boolean parsed;

   public ParserSqlScript(Parser parser, LoadableResource resource, LoadableResource metadataResource, boolean mixed) {
      this.resource = resource;
      this.metadata = SqlScriptMetadata.fromResource(metadataResource, parser);
      this.parser = parser;
      this.mixed = mixed;
   }

   protected void parse() {
      SqlStatementIterator sqlStatementIterator = this.parser.parse(this.resource, this.metadata);

      try {
         boolean transactionalStatementFound = false;

         while(sqlStatementIterator.hasNext()) {
            SqlStatement sqlStatement = (SqlStatement)sqlStatementIterator.next();
            this.sqlStatements.add(sqlStatement);
            ++this.sqlStatementCount;
            if (sqlStatement.canExecuteInTransaction()) {
               transactionalStatementFound = true;
            } else {
               this.nonTransactionalStatementFound = true;
            }

            if (!this.mixed && transactionalStatementFound && this.nonTransactionalStatementFound && this.metadata.executeInTransaction() == null) {
               throw new FlywayException(
                  "Detected both transactional and non-transactional statements within the same migration (even though mixed is false). Offending statement found at line "
                     + sqlStatement.getLineNumber()
                     + ": "
                     + sqlStatement.getSql()
                     + (sqlStatement.canExecuteInTransaction() ? "" : " [non-transactional]")
               );
            }

            if (LOG.isDebugEnabled()) {
               LOG.debug(
                  "Found statement at line "
                     + sqlStatement.getLineNumber()
                     + ": "
                     + sqlStatement.getSql()
                     + (sqlStatement.canExecuteInTransaction() ? "" : " [non-transactional]")
               );
            }
         }
      } catch (Throwable var5) {
         if (sqlStatementIterator != null) {
            try {
               sqlStatementIterator.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }
         }

         throw var5;
      }

      if (sqlStatementIterator != null) {
         sqlStatementIterator.close();
      }

      this.parsed = true;
   }

   @Override
   public void validate() {
      if (!this.parsed) {
         this.parse();
      }

   }

   @Override
   public SqlStatementIterator getSqlStatements() {
      this.validate();
      final Iterator<SqlStatement> iterator = this.sqlStatements.iterator();
      return new SqlStatementIterator() {
         @Override
         public void close() {
         }

         public boolean hasNext() {
            return iterator.hasNext();
         }

         public SqlStatement next() {
            return (SqlStatement)iterator.next();
         }

         public void remove() {
            iterator.remove();
         }
      };
   }

   @Override
   public int getSqlStatementCount() {
      this.validate();
      return this.sqlStatementCount;
   }

   @Override
   public final LoadableResource getResource() {
      return this.resource;
   }

   @Override
   public boolean executeInTransaction() {
      Boolean executeInTransactionOverride = this.metadata.executeInTransaction();
      if (executeInTransactionOverride != null) {
         LOG.debug("Using executeInTransaction=" + executeInTransactionOverride + " from script configuration");
         return executeInTransactionOverride;
      } else {
         this.validate();
         return !this.nonTransactionalStatementFound;
      }
   }

   @Override
   public boolean shouldExecute() {
      return this.metadata.shouldExecute();
   }

   public int compareTo(SqlScript o) {
      return this.resource.getRelativePath().compareTo(o.getResource().getRelativePath());
   }
}

package io.micronaut.data.jdbc.config;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.naming.Named;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.runtime.config.SchemaGenerate;
import java.util.ArrayList;
import java.util.List;

@EachProperty(
   value = "datasources",
   primary = "default"
)
public class DataJdbcConfiguration implements Named {
   public static final String PREFIX = "datasources";
   private SchemaGenerate schemaGenerate = SchemaGenerate.NONE;
   private boolean batchGenerate = false;
   private Dialect dialect = Dialect.ANSI;
   private List<String> packages = new ArrayList(3);
   private final String name;
   private boolean transactionPerOperation = true;
   private boolean allowConnectionPerOperation;

   public DataJdbcConfiguration(@Parameter String name) {
      this.name = name;
   }

   public SchemaGenerate getSchemaGenerate() {
      return this.schemaGenerate;
   }

   public void setSchemaGenerate(SchemaGenerate schemaGenerate) {
      if (schemaGenerate != null) {
         this.schemaGenerate = schemaGenerate;
      }

   }

   public boolean isBatchGenerate() {
      return this.batchGenerate;
   }

   public void setBatchGenerate(boolean batchGenerate) {
      this.batchGenerate = batchGenerate;
   }

   public List<String> getPackages() {
      return this.packages;
   }

   public void setPackages(List<String> packages) {
      if (packages != null) {
         this.packages = packages;
      }

   }

   public Dialect getDialect() {
      return this.dialect;
   }

   public void setDialect(Dialect dialect) {
      this.dialect = dialect;
   }

   @NonNull
   @Override
   public String getName() {
      return this.name;
   }

   public boolean isTransactionPerOperation() {
      return this.transactionPerOperation;
   }

   public void setTransactionPerOperation(boolean transactionPerOperation) {
      this.transactionPerOperation = transactionPerOperation;
   }

   public boolean isAllowConnectionPerOperation() {
      return this.allowConnectionPerOperation;
   }

   public void setAllowConnectionPerOperation(boolean allowConnectionPerOperation) {
      this.allowConnectionPerOperation = allowConnectionPerOperation;
   }
}

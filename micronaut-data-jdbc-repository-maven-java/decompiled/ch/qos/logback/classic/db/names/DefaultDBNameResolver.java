package ch.qos.logback.classic.db.names;

public class DefaultDBNameResolver implements DBNameResolver {
   @Override
   public <N extends Enum<?>> String getTableName(N tableName) {
      return tableName.toString().toLowerCase();
   }

   @Override
   public <N extends Enum<?>> String getColumnName(N columnName) {
      return columnName.toString().toLowerCase();
   }
}

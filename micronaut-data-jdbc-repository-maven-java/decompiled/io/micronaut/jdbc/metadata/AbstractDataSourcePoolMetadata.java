package io.micronaut.jdbc.metadata;

import javax.sql.DataSource;

public abstract class AbstractDataSourcePoolMetadata<T extends DataSource> implements DataSourcePoolMetadata<T> {
   private final T dataSource;

   protected AbstractDataSourcePoolMetadata(T dataSource) {
      this.dataSource = dataSource;
   }

   @Override
   public Float getUsage() {
      Integer maxSize = this.getMax();
      Integer currentSize = this.getActive();
      if (maxSize == null || currentSize == null) {
         return null;
      } else if (maxSize < 0) {
         return -1.0F;
      } else {
         return currentSize == 0 ? 0.0F : (float)currentSize.intValue() / (float)maxSize.intValue();
      }
   }

   @Override
   public T getDataSource() {
      return this.dataSource;
   }
}

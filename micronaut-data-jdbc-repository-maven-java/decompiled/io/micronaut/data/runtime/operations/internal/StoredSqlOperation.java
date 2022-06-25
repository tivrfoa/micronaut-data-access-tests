package io.micronaut.data.runtime.operations.internal;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanWrapper;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.PersistentPropertyPath;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.model.query.builder.sql.SqlQueryBuilder;
import io.micronaut.data.model.runtime.QueryParameterBinding;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Collectors;

@Internal
public class StoredSqlOperation extends DBOperation {
   protected final List<QueryParameterBinding> queryParameterBindings;
   protected final boolean isOptimisticLock;
   protected final String[] expandableQueryParts;
   protected final boolean expandableQuery;
   protected final SqlQueryBuilder queryBuilder;

   protected StoredSqlOperation(
      SqlQueryBuilder queryBuilder,
      String query,
      @Nullable String[] expandableQueryParts,
      List<QueryParameterBinding> queryParameterBindings,
      boolean isOptimisticLock
   ) {
      super(query, queryBuilder.dialect());
      this.queryBuilder = queryBuilder;
      Objects.requireNonNull(query, "Query cannot be null");
      Objects.requireNonNull(this.dialect, "Dialect cannot be null");
      this.queryParameterBindings = queryParameterBindings;
      this.isOptimisticLock = isOptimisticLock;
      this.expandableQueryParts = expandableQueryParts;
      this.expandableQuery = expandableQueryParts != null
         && expandableQueryParts.length > 1
         && queryParameterBindings.stream().anyMatch(QueryParameterBinding::isExpandable);
      if (this.expandableQuery && expandableQueryParts.length != queryParameterBindings.size() + 1) {
         throw new IllegalStateException(
            "Expandable query parts size should be the same as parameters size + 1. "
               + expandableQueryParts.length
               + " != 1 + "
               + queryParameterBindings.size()
               + " "
               + query
               + " "
               + Arrays.toString(expandableQueryParts)
         );
      }
   }

   @Override
   public boolean isOptimisticLock() {
      return this.isOptimisticLock;
   }

   @Override
   public <T> Map<QueryParameterBinding, Object> collectAutoPopulatedPreviousValues(RuntimePersistentEntity<T> persistentEntity, T entity) {
      return this.queryParameterBindings.isEmpty()
         ? null
         : (Map)this.queryParameterBindings.stream().filter(b -> b.isAutoPopulated() && b.isRequiresPreviousPopulatedValue()).map(b -> {
            if (b.getPropertyPath() == null) {
               throw new IllegalStateException("Missing property path for query parameter: " + b);
            } else {
               Object value = entity;
   
               for(String property : b.getPropertyPath()) {
                  if (value == null) {
                     break;
                  }
   
                  value = BeanWrapper.getWrapper(value).getRequiredProperty(property, Argument.OBJECT_ARGUMENT);
               }
   
               return new SimpleEntry(b, value);
            }
         }).filter(e -> e.getValue() != null).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
   }

   public <T> void checkForParameterToBeExpanded(RuntimePersistentEntity<T> persistentEntity, T entity) {
      if (this.expandableQuery) {
         String positionalParameterFormat = this.queryBuilder.positionalParameterFormat();
         StringBuilder q = new StringBuilder(this.expandableQueryParts[0]);
         int queryParamIndex = 1;
         int inx = 1;

         for(QueryParameterBinding parameter : this.queryParameterBindings) {
            if (!parameter.isExpandable()) {
               q.append(String.format(positionalParameterFormat, inx++));
            } else {
               int size = Math.max(1, this.getQueryParameterValueSize(parameter, persistentEntity, entity));

               for(int k = 0; k < size; ++k) {
                  q.append(String.format(positionalParameterFormat, inx++));
                  if (k + 1 != size) {
                     q.append(",");
                  }
               }
            }

            q.append(this.expandableQueryParts[queryParamIndex++]);
         }

         this.query = q.toString();
      }

   }

   protected <T> int getQueryParameterValueSize(QueryParameterBinding parameter, RuntimePersistentEntity<T> persistentEntity, T entity) {
      String[] stringPropertyPath = parameter.getRequiredPropertyPath();
      PersistentPropertyPath propertyPath = persistentEntity.getPropertyPath(stringPropertyPath);
      if (propertyPath == null) {
         throw new IllegalStateException("Unrecognized path: " + String.join(".", stringPropertyPath));
      } else {
         return this.sizeOf(propertyPath.getPropertyValue(entity));
      }
   }

   @Override
   public <T, Cnt, PS> void setParameters(
      OpContext<Cnt, PS> context,
      Cnt connection,
      PS stmt,
      RuntimePersistentEntity<T> persistentEntity,
      T entity,
      Map<QueryParameterBinding, Object> previousValues
   ) {
      int index = context.shiftIndex(0);

      for(QueryParameterBinding binding : this.queryParameterBindings) {
         String[] stringPropertyPath = binding.getRequiredPropertyPath();
         PersistentPropertyPath pp = persistentEntity.getPropertyPath(stringPropertyPath);
         if (pp == null) {
            throw new IllegalStateException("Unrecognized path: " + String.join(".", stringPropertyPath));
         }

         if (!binding.isAutoPopulated() || !binding.isRequiresPreviousPopulatedValue()) {
            Object value = pp.getPropertyValue(entity);
            RuntimePersistentProperty<?> property = (RuntimePersistentProperty)pp.getProperty();
            DataType type = property.getDataType();
            if (value == null && type == DataType.ENTITY) {
               RuntimePersistentEntity<?> referencedEntity = context.getEntity(property.getType());
               RuntimePersistentProperty<?> identity = referencedEntity.getIdentity();
               if (identity == null) {
                  throw new IllegalStateException("Cannot set an entity value without identity: " + referencedEntity);
               }

               property = identity;
               type = identity.getDataType();
            }

            value = context.convert(connection, value, property);
            index = this.setStatementParameter(context, stmt, index, type, value, this.dialect, binding.isExpandable());
         } else if (previousValues != null) {
            Object previousValue = previousValues.get(binding);
            if (previousValue != null) {
               index = this.setStatementParameter(context, stmt, index, pp.getProperty().getDataType(), previousValue, this.dialect, binding.isExpandable());
            }
         }
      }

   }

   private <PS> int setStatementParameter(
      OpContext<?, PS> context, PS preparedStatement, int index, DataType dataType, Object value, Dialect dialect, boolean isExpandable
   ) {
      if (this.expandableQuery) {
         List<Object> values = isExpandable ? this.expandValue(value, dataType) : Collections.singletonList(value);
         if (values != null && values.isEmpty()) {
            value = null;
            values = null;
         }

         if (values != null) {
            for(Object v : values) {
               context.setStatementParameter(preparedStatement, index, dataType, v, dialect);
               ++index;
            }

            return index;
         }

         context.setStatementParameter(preparedStatement, index, dataType, value, dialect);
      } else {
         context.setStatementParameter(preparedStatement, index, dataType, value, dialect);
      }

      return index + 1;
   }

   List<Object> expandValue(Object value, DataType dataType) {
      if (value == null || dataType.isArray() && dataType != DataType.BYTE_ARRAY || value instanceof byte[]) {
         return null;
      } else if (value instanceof Iterable) {
         return CollectionUtils.iterableToList((Iterable<Object>)value);
      } else if (!value.getClass().isArray()) {
         return null;
      } else {
         int len = Array.getLength(value);
         if (len == 0) {
            return Collections.emptyList();
         } else {
            List<Object> list = new ArrayList(len);

            for(int j = 0; j < len; ++j) {
               Object o = Array.get(value, j);
               list.add(o);
            }

            return list;
         }
      }
   }

   protected int sizeOf(Object value) {
      if (value == null) {
         return 1;
      } else if (value instanceof Collection) {
         return ((Collection)value).size();
      } else if (!(value instanceof Iterable)) {
         return value.getClass().isArray() ? Array.getLength(value) : 1;
      } else {
         int i = 0;

         for(Object ignored : (Iterable)value) {
            ++i;
         }

         return i;
      }
   }

   public SqlQueryBuilder getQueryBuilder() {
      return this.queryBuilder;
   }
}

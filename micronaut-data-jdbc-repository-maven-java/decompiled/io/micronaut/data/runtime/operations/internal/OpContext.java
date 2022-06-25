package io.micronaut.data.runtime.operations.internal;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.model.runtime.RuntimeEntityRegistry;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;

@Internal
public interface OpContext<Cnt, PS> {
   RuntimeEntityRegistry getRuntimeEntityRegistry();

   <T> RuntimePersistentEntity<T> getEntity(@NonNull Class<T> type);

   int shiftIndex(int index);

   void setStatementParameter(PS preparedStatement, int index, DataType dataType, Object value, Dialect dialect);

   Object convert(Cnt connection, Object value, RuntimePersistentProperty<?> property);

   Object convert(Class<?> converterClass, Cnt connection, Object value, @Nullable Argument<?> argument);
}

package io.micronaut.data.runtime.mapper.sql;

import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.runtime.convert.DataConversionService;
import io.micronaut.data.runtime.mapper.DTOMapper;
import io.micronaut.data.runtime.mapper.ResultReader;
import io.micronaut.http.codec.MediaTypeCodec;

public class SqlDTOMapper<T, S, R> extends DTOMapper<T, S, R> implements SqlTypeMapper<S, R> {
   public SqlDTOMapper(RuntimePersistentEntity<T> persistentEntity, ResultReader<S, String> resultReader, DataConversionService<?> conversionService) {
      this(persistentEntity, resultReader, null, conversionService);
   }

   public SqlDTOMapper(
      RuntimePersistentEntity<T> persistentEntity, ResultReader<S, String> resultReader, MediaTypeCodec jsonCodec, DataConversionService<?> conversionService
   ) {
      super(persistentEntity, resultReader, jsonCodec, conversionService);
   }

   public SqlDTOMapper(
      RuntimePersistentEntity<T> persistentEntity,
      RuntimePersistentEntity<?> dtoEntity,
      ResultReader<S, String> resultReader,
      MediaTypeCodec jsonCodec,
      DataConversionService<?> conversionService
   ) {
      super(persistentEntity, dtoEntity, resultReader, jsonCodec, conversionService);
   }

   @Override
   public boolean hasNext(S resultSet) {
      return this.getResultReader().next(resultSet);
   }
}

package io.micronaut.data.runtime.mapper;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;
import io.micronaut.data.runtime.convert.DataConversionService;
import io.micronaut.http.codec.MediaTypeCodec;

public class DTOMapper<T, S, R> implements BeanIntrospectionMapper<S, R> {
   private final RuntimePersistentEntity<T> persistentEntity;
   private final RuntimePersistentEntity<?> dtoEntity;
   private final ResultReader<S, String> resultReader;
   @Nullable
   private final MediaTypeCodec jsonCodec;
   private final DataConversionService<?> conversionService;

   public DTOMapper(RuntimePersistentEntity<T> persistentEntity, ResultReader<S, String> resultReader, DataConversionService<?> conversionService) {
      this(persistentEntity, resultReader, null, conversionService);
   }

   public DTOMapper(
      RuntimePersistentEntity<T> persistentEntity,
      ResultReader<S, String> resultReader,
      @Nullable MediaTypeCodec jsonCodec,
      DataConversionService<?> conversionService
   ) {
      this(persistentEntity, persistentEntity, resultReader, jsonCodec, conversionService);
   }

   public DTOMapper(
      RuntimePersistentEntity<T> persistentEntity,
      RuntimePersistentEntity<?> dtoEntity,
      ResultReader<S, String> resultReader,
      @Nullable MediaTypeCodec jsonCodec,
      DataConversionService<?> conversionService
   ) {
      this.conversionService = conversionService;
      ArgumentUtils.requireNonNull("persistentEntity", persistentEntity);
      ArgumentUtils.requireNonNull("resultReader", resultReader);
      this.persistentEntity = persistentEntity;
      this.dtoEntity = dtoEntity;
      this.resultReader = resultReader;
      this.jsonCodec = jsonCodec;
   }

   public DataConversionService<?> getConversionService() {
      return this.conversionService;
   }

   @Nullable
   @Override
   public Object read(@NonNull S object, @NonNull String name) throws ConversionErrorException {
      RuntimePersistentProperty<?> pp = this.persistentEntity.getPropertyByName(name);
      if (pp == null && this.persistentEntity == this.dtoEntity) {
         throw new DataAccessException("DTO projection defines a property [" + name + "] that doesn't exist on root entity: " + this.persistentEntity.getName());
      } else {
         pp = this.dtoEntity.getPropertyByName(name);
         if (pp == null) {
            throw new DataAccessException("DTO projection doesn't defines a property [" + name + "] on DTO entity: " + this.dtoEntity.getName());
         } else {
            return this.read(object, pp);
         }
      }
   }

   @Nullable
   @Override
   public Object read(@NonNull S object, @NonNull Argument<?> argument) {
      RuntimePersistentProperty<T> pp = this.persistentEntity.getPropertyByName(argument.getName());
      if (pp == null) {
         DataType type = (DataType)argument.getAnnotationMetadata()
            .enumValue(TypeDef.class, "type", DataType.class)
            .orElseGet(() -> DataType.forType(argument.getType()));
         return this.read(object, argument.getName(), type);
      } else {
         return this.read(object, pp);
      }
   }

   @Nullable
   public Object read(@NonNull S resultSet, @NonNull RuntimePersistentProperty<?> property) {
      String propertyName = property.getPersistedName();
      DataType dataType = property.getDataType();
      if (dataType == DataType.JSON && this.jsonCodec != null) {
         String data = this.resultReader.readString(resultSet, propertyName);
         return this.jsonCodec.decode(property.getArgument(), data);
      } else {
         return this.read(resultSet, propertyName, dataType);
      }
   }

   @Nullable
   public Object read(@NonNull S resultSet, @NonNull String persistedName, @NonNull DataType dataType) {
      return this.resultReader.readDynamic(resultSet, persistedName, dataType);
   }

   public PersistentEntity getPersistentEntity() {
      return this.persistentEntity;
   }

   public ResultReader<S, String> getResultReader() {
      return this.resultReader;
   }
}

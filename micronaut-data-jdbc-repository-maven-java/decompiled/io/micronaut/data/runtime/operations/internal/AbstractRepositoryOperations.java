package io.micronaut.data.runtime.operations.internal;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.event.EntityEventListener;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.exceptions.OptimisticLockException;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.PersistentPropertyPath;
import io.micronaut.data.model.query.JoinPath;
import io.micronaut.data.model.runtime.AttributeConverterRegistry;
import io.micronaut.data.model.runtime.RuntimeEntityRegistry;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;
import io.micronaut.data.model.runtime.convert.AttributeConverter;
import io.micronaut.data.runtime.convert.DataConversionService;
import io.micronaut.data.runtime.date.DateTimeProvider;
import io.micronaut.data.runtime.event.DefaultEntityEventContext;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.MediaTypeCodec;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Internal
public abstract class AbstractRepositoryOperations<Cnt, PS> implements ApplicationContextProvider, OpContext<Cnt, PS> {
   protected final MediaTypeCodec jsonCodec;
   protected final EntityEventListener<Object> entityEventRegistry;
   protected final DateTimeProvider dateTimeProvider;
   protected final RuntimeEntityRegistry runtimeEntityRegistry;
   protected final DataConversionService<?> conversionService;
   protected final AttributeConverterRegistry attributeConverterRegistry;
   private final Map<Class, RuntimePersistentProperty> idReaders = new ConcurrentHashMap(10);

   protected AbstractRepositoryOperations(
      List<MediaTypeCodec> codecs,
      DateTimeProvider<Object> dateTimeProvider,
      RuntimeEntityRegistry runtimeEntityRegistry,
      DataConversionService<?> conversionService,
      AttributeConverterRegistry attributeConverterRegistry
   ) {
      this.dateTimeProvider = dateTimeProvider;
      this.runtimeEntityRegistry = runtimeEntityRegistry;
      this.entityEventRegistry = runtimeEntityRegistry.getEntityEventListener();
      this.jsonCodec = this.resolveJsonCodec(codecs);
      this.conversionService = conversionService;
      this.attributeConverterRegistry = attributeConverterRegistry;
   }

   public DataConversionService<?> getConversionService() {
      return this.conversionService;
   }

   @Override
   public ApplicationContext getApplicationContext() {
      return this.runtimeEntityRegistry.getApplicationContext();
   }

   private MediaTypeCodec resolveJsonCodec(List<MediaTypeCodec> codecs) {
      return CollectionUtils.isNotEmpty(codecs)
         ? (MediaTypeCodec)codecs.stream().filter(c -> c.getMediaTypes().contains(MediaType.APPLICATION_JSON_TYPE)).findFirst().orElse(null)
         : null;
   }

   @NonNull
   @Override
   public final <T> RuntimePersistentEntity<T> getEntity(@NonNull Class<T> type) {
      return this.runtimeEntityRegistry.getEntity(type);
   }

   @Override
   public RuntimeEntityRegistry getRuntimeEntityRegistry() {
      return this.runtimeEntityRegistry;
   }

   protected <T> T triggerPostLoad(@NonNull T entity, RuntimePersistentEntity<T> pe, AnnotationMetadata annotationMetadata) {
      DefaultEntityEventContext<T> event = new DefaultEntityEventContext<>(pe, entity);
      this.entityEventRegistry.postLoad(event);
      return event.getEntity();
   }

   @Override
   public int shiftIndex(int i) {
      return i + 1;
   }

   @NonNull
   protected final RuntimePersistentProperty<Object> getIdReader(@NonNull Object o) {
      Class<Object> type = o.getClass();
      RuntimePersistentProperty beanProperty = (RuntimePersistentProperty)this.idReaders.get(type);
      if (beanProperty == null) {
         RuntimePersistentEntity<Object> entity = this.getEntity(type);
         RuntimePersistentProperty<Object> identity = entity.getIdentity();
         if (identity == null) {
            throw new DataAccessException("Entity has no ID: " + entity.getName());
         }

         beanProperty = identity;
         this.idReaders.put(type, identity);
      }

      return beanProperty;
   }

   protected void checkOptimisticLocking(int expected, Number received) {
      if (received.intValue() != expected) {
         throw new OptimisticLockException("Execute update returned unexpected row count. Expected: " + expected + " got: " + received);
      }
   }

   protected boolean isOnlySingleEndedJoins(RuntimePersistentEntity<?> rootPersistentEntity, Set<JoinPath> joinFetchPaths) {
      return joinFetchPaths.isEmpty()
         || joinFetchPaths.stream()
            .flatMap(
               jp -> {
                  PersistentPropertyPath propertyPath = rootPersistentEntity.getPropertyPath(jp.getPath());
                  if (propertyPath == null) {
                     return Stream.empty();
                  } else {
                     return propertyPath.getProperty() instanceof Association
                        ? Stream.concat(propertyPath.getAssociations().stream(), Stream.of((Association)propertyPath.getProperty()))
                        : propertyPath.getAssociations().stream();
                  }
               }
            )
            .allMatch(association -> association.getKind() == Relation.Kind.EMBEDDED || association.getKind().isSingleEnded());
   }

   @Override
   public Object convert(Cnt connection, Object value, RuntimePersistentProperty<?> property) {
      AttributeConverter<Object, Object> converter = property.getConverter();
      return converter != null
         ? converter.convertToPersistedValue(value, this.createTypeConversionContext(connection, property, property.getArgument()))
         : value;
   }

   @Override
   public Object convert(Class<?> converterClass, Cnt connection, Object value, @Nullable Argument<?> argument) {
      if (converterClass == null) {
         return value;
      } else {
         AttributeConverter<Object, Object> converter = this.attributeConverterRegistry.getConverter(converterClass);
         ConversionContext conversionContext = this.createTypeConversionContext(connection, null, argument);
         return converter.convertToPersistedValue(value, conversionContext);
      }
   }

   protected abstract ConversionContext createTypeConversionContext(
      Cnt connection, @Nullable RuntimePersistentProperty<?> property, @Nullable Argument<?> argument
   );
}

package io.micronaut.data.runtime.mapper.sql;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.beans.BeanWrapper;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.reflect.exception.InstantiationException;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.annotation.Embeddable;
import io.micronaut.data.annotation.EmbeddedId;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.Embedded;
import io.micronaut.data.model.PersistentAssociationPath;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.naming.NamingStrategy;
import io.micronaut.data.model.query.JoinPath;
import io.micronaut.data.model.runtime.RuntimeAssociation;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;
import io.micronaut.data.model.runtime.convert.AttributeConverter;
import io.micronaut.data.runtime.convert.DataConversionService;
import io.micronaut.data.runtime.mapper.ResultReader;
import io.micronaut.http.codec.MediaTypeCodec;
import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import javax.validation.constraints.NotNull;

@Internal
public final class SqlResultEntityTypeMapper<RS, R> implements SqlTypeMapper<RS, R> {
   private final RuntimePersistentEntity<R> entity;
   private final ResultReader<RS, String> resultReader;
   private final Map<String, JoinPath> joinPaths;
   private final String startingPrefix;
   private final MediaTypeCodec jsonCodec;
   private final DataConversionService<?> conversionService;
   private final BiFunction<RuntimePersistentEntity<Object>, Object, Object> eventListener;
   private boolean callNext = true;

   public SqlResultEntityTypeMapper(
      String prefix,
      @NonNull RuntimePersistentEntity<R> entity,
      @NonNull ResultReader<RS, String> resultReader,
      @Nullable MediaTypeCodec jsonCodec,
      DataConversionService<?> conversionService
   ) {
      this(entity, resultReader, Collections.emptySet(), prefix, jsonCodec, conversionService, null);
   }

   public SqlResultEntityTypeMapper(
      @NonNull RuntimePersistentEntity<R> entity,
      @NonNull ResultReader<RS, String> resultReader,
      @Nullable Set<JoinPath> joinPaths,
      @Nullable MediaTypeCodec jsonCodec,
      DataConversionService<?> conversionService
   ) {
      this(entity, resultReader, joinPaths, null, jsonCodec, conversionService, null);
   }

   public SqlResultEntityTypeMapper(
      @NonNull RuntimePersistentEntity<R> entity,
      @NonNull ResultReader<RS, String> resultReader,
      @Nullable Set<JoinPath> joinPaths,
      @Nullable MediaTypeCodec jsonCodec,
      @Nullable BiFunction<RuntimePersistentEntity<Object>, Object, Object> loadListener,
      DataConversionService<?> conversionService
   ) {
      this(entity, resultReader, joinPaths, null, jsonCodec, conversionService, loadListener);
   }

   private SqlResultEntityTypeMapper(
      @NonNull RuntimePersistentEntity<R> entity,
      @NonNull ResultReader<RS, String> resultReader,
      @Nullable Set<JoinPath> joinPaths,
      String startingPrefix,
      @Nullable MediaTypeCodec jsonCodec,
      DataConversionService<?> conversionService,
      @Nullable BiFunction<RuntimePersistentEntity<Object>, Object, Object> eventListener
   ) {
      this.conversionService = conversionService;
      ArgumentUtils.requireNonNull("entity", entity);
      ArgumentUtils.requireNonNull("resultReader", resultReader);
      this.entity = entity;
      this.jsonCodec = jsonCodec;
      this.resultReader = resultReader;
      this.eventListener = eventListener;
      if (CollectionUtils.isNotEmpty(joinPaths)) {
         this.joinPaths = new HashMap(joinPaths.size());

         for(JoinPath joinPath : joinPaths) {
            this.joinPaths.put(joinPath.getPath(), joinPath);
         }
      } else {
         this.joinPaths = Collections.emptyMap();
      }

      this.startingPrefix = startingPrefix;
   }

   public DataConversionService<?> getConversionService() {
      return this.conversionService;
   }

   @NonNull
   public RuntimePersistentEntity<R> getEntity() {
      return this.entity;
   }

   @NonNull
   public ResultReader<RS, String> getResultReader() {
      return this.resultReader;
   }

   @NonNull
   @Override
   public R map(@NonNull RS rs, @NonNull Class<R> type) throws DataAccessException {
      R entityInstance = this.readEntity(rs, SqlResultEntityTypeMapper.MappingContext.of(this.entity, this.startingPrefix), null, null);
      if (entityInstance == null) {
         throw new DataAccessException("Unable to map result to entity of type [" + type.getName() + "]. Missing result data.");
      } else {
         return this.triggerPostLoad(this.entity, entityInstance);
      }
   }

   @Nullable
   @Override
   public Object read(@NonNull RS resultSet, @NonNull String name) {
      RuntimePersistentProperty<R> property = this.entity.getPropertyByName(name);
      if (property == null) {
         throw new DataAccessException("DTO projection defines a property [" + name + "] that doesn't exist on root entity: " + this.entity.getName());
      } else {
         DataType dataType = property.getDataType();
         String columnName = property.getPersistedName();
         return this.resultReader.readDynamic(resultSet, columnName, dataType);
      }
   }

   @Nullable
   @Override
   public Object read(@NonNull RS resultSet, @NonNull Argument<?> argument) {
      RuntimePersistentProperty<R> property = this.entity.getPropertyByName(argument.getName());
      DataType dataType;
      String columnName;
      if (property == null) {
         dataType = (DataType)argument.getAnnotationMetadata()
            .enumValue(TypeDef.class, "type", DataType.class)
            .orElseGet(() -> DataType.forType(argument.getType()));
         columnName = argument.getName();
      } else {
         dataType = property.getDataType();
         columnName = property.getPersistedName();
      }

      return this.resultReader.readDynamic(resultSet, columnName, dataType);
   }

   @Override
   public boolean hasNext(RS resultSet) {
      if (this.callNext) {
         return this.resultReader.next(resultSet);
      } else {
         boolean var2;
         try {
            var2 = true;
         } finally {
            this.callNext = true;
         }

         return var2;
      }
   }

   public SqlResultEntityTypeMapper.PushingMapper<RS, R> readOneWithJoins() {
      return new SqlResultEntityTypeMapper.PushingMapper<RS, R>() {
         final SqlResultEntityTypeMapper.MappingContext<R> ctx = SqlResultEntityTypeMapper.MappingContext.of(
            SqlResultEntityTypeMapper.this.entity, SqlResultEntityTypeMapper.this.startingPrefix
         );
         R entityInstance;

         @Override
         public void processRow(RS row) {
            if (this.entityInstance == null) {
               Object id = SqlResultEntityTypeMapper.this.readEntityId(row, this.ctx);
               this.entityInstance = SqlResultEntityTypeMapper.this.readEntity(row, this.ctx, null, id);
            } else {
               SqlResultEntityTypeMapper.this.readChildren(row, this.entityInstance, null, this.ctx);
            }

         }

         @Override
         public R getResult() {
            if (this.entityInstance == null) {
               return null;
            } else if (!SqlResultEntityTypeMapper.this.joinPaths.isEmpty()) {
               this.entityInstance = (R)SqlResultEntityTypeMapper.this.setChildrenAndTriggerPostLoad(this.entityInstance, this.ctx, null);
               return this.entityInstance;
            } else {
               return SqlResultEntityTypeMapper.this.triggerPostLoad(SqlResultEntityTypeMapper.this.entity, this.entityInstance);
            }
         }
      };
   }

   public SqlResultEntityTypeMapper.PushingMapper<RS, List<R>> readAllWithJoins() {
      return new SqlResultEntityTypeMapper.PushingMapper<RS, List<R>>() {
         final Map<Object, SqlResultEntityTypeMapper.MappingContext<R>> processed = new LinkedHashMap();

         @Override
         public void processRow(RS row) {
            SqlResultEntityTypeMapper.MappingContext<R> ctx = SqlResultEntityTypeMapper.MappingContext.of(
               SqlResultEntityTypeMapper.this.entity, SqlResultEntityTypeMapper.this.startingPrefix
            );
            Object id = SqlResultEntityTypeMapper.this.readEntityId(row, ctx);
            if (id == null) {
               throw new IllegalStateException("Entity doesn't have an id!");
            } else {
               SqlResultEntityTypeMapper.MappingContext<R> prevCtx = (SqlResultEntityTypeMapper.MappingContext)this.processed.get(id);
               if (prevCtx != null) {
                  SqlResultEntityTypeMapper.this.readChildren(row, prevCtx.entity, null, prevCtx);
               } else {
                  ctx.entity = SqlResultEntityTypeMapper.this.readEntity(row, ctx, null, id);
                  this.processed.put(id, ctx);
               }

            }
         }

         public List<R> getResult() {
            List<R> values = new ArrayList(this.processed.size());

            for(Entry<Object, SqlResultEntityTypeMapper.MappingContext<R>> e : this.processed.entrySet()) {
               SqlResultEntityTypeMapper.MappingContext<R> ctx = (SqlResultEntityTypeMapper.MappingContext)e.getValue();
               R entityInstance = (R)SqlResultEntityTypeMapper.this.setChildrenAndTriggerPostLoad(ctx.entity, ctx, null);
               values.add(entityInstance);
            }

            return values;
         }
      };
   }

   private void readChildren(RS rs, Object instance, Object parent, SqlResultEntityTypeMapper.MappingContext<R> ctx) {
      if (ctx.manyAssociations != null) {
         Object id = this.readEntityId(rs, ctx);
         SqlResultEntityTypeMapper.MappingContext associatedCtx = (SqlResultEntityTypeMapper.MappingContext)ctx.manyAssociations.get(id);
         if (associatedCtx == null) {
            associatedCtx = ctx.copy();
            R entity = this.readEntity(rs, associatedCtx, parent, id);
            Objects.requireNonNull(id);
            ctx.associate(associatedCtx, id, entity);
         } else {
            this.readChildren(rs, instance, parent, associatedCtx);
         }

      } else {
         if (ctx.associations != null) {
            for(Entry<Association, SqlResultEntityTypeMapper.MappingContext> e : ctx.associations.entrySet()) {
               SqlResultEntityTypeMapper.MappingContext associationCtx = (SqlResultEntityTypeMapper.MappingContext)e.getValue();
               RuntimeAssociation runtimeAssociation = (RuntimeAssociation)e.getKey();
               Object in = instance != null && runtimeAssociation.getKind().isSingleEnded() ? runtimeAssociation.getProperty().get(instance) : null;
               this.readChildren(rs, in, instance, associationCtx);
            }
         }

      }
   }

   private Object setChildrenAndTriggerPostLoad(Object instance, SqlResultEntityTypeMapper.MappingContext<?> ctx, Object parent) {
      if (ctx.manyAssociations != null) {
         List<Object> values = new ArrayList(ctx.manyAssociations.size());

         for(SqlResultEntityTypeMapper.MappingContext associationCtx : ctx.manyAssociations.values()) {
            values.add(this.setChildrenAndTriggerPostLoad(associationCtx.entity, associationCtx, parent));
         }

         return values;
      } else {
         if (ctx.associations != null) {
            for(Entry<Association, SqlResultEntityTypeMapper.MappingContext> e : ctx.associations.entrySet()) {
               SqlResultEntityTypeMapper.MappingContext associationCtx = (SqlResultEntityTypeMapper.MappingContext)e.getValue();
               RuntimeAssociation runtimeAssociation = (RuntimeAssociation)e.getKey();
               BeanProperty beanProperty = runtimeAssociation.getProperty();
               if (runtimeAssociation.getKind().isSingleEnded() && (associationCtx.manyAssociations == null || associationCtx.manyAssociations.isEmpty())) {
                  Object value = beanProperty.get(instance);
                  Object newValue = this.setChildrenAndTriggerPostLoad(value, associationCtx, instance);
                  if (newValue != value) {
                     instance = this.setProperty(beanProperty, instance, newValue);
                  }
               } else {
                  Object newValue = this.setChildrenAndTriggerPostLoad(null, associationCtx, instance);
                  newValue = this.resultReader.convertRequired(newValue == null ? new ArrayList() : newValue, beanProperty.getType());
                  instance = this.setProperty(beanProperty, instance, newValue);
               }
            }
         }

         if (instance != null && (ctx.association == null || ctx.jp != null)) {
            if (parent != null && ctx.association != null && ctx.association.isBidirectional()) {
               PersistentAssociationPath inverse = (PersistentAssociationPath)ctx.association.getInversePathSide().orElseThrow(IllegalStateException::new);
               Association association = inverse.getAssociation();
               if (association.getKind().isSingleEnded()) {
                  Object inverseInstance = inverse.getPropertyValue(instance);
                  if (inverseInstance != parent) {
                     instance = inverse.setPropertyValue(instance, parent);
                  }
               }
            }

            this.triggerPostLoad(ctx.persistentEntity, instance);
         }

         return instance;
      }
   }

   private <X, Y> X setProperty(BeanProperty<X, Y> beanProperty, X x, Y y) {
      if (beanProperty.isReadOnly()) {
         return beanProperty.withValue(x, y);
      } else {
         beanProperty.set(x, y);
         return x;
      }
   }

   @Nullable
   private <K> K readEntity(RS rs, SqlResultEntityTypeMapper.MappingContext<K> ctx, @Nullable Object parent, @Nullable Object resolveId) {
      RuntimePersistentEntity<K> persistentEntity = ctx.persistentEntity;
      BeanIntrospection<K> introspection = persistentEntity.getIntrospection();
      RuntimePersistentProperty<K>[] constructorArguments = persistentEntity.getConstructorArguments();

      try {
         RuntimePersistentProperty<K> identity = persistentEntity.getIdentity();
         boolean isAssociation = ctx.association != null;
         boolean isEmbedded = ctx.association instanceof Embedded;
         boolean nullableEmbedded = isEmbedded && ctx.association.isOptional();
         Object id = resolveId == null ? this.readEntityId(rs, ctx) : resolveId;
         if (id == null && !isEmbedded && isAssociation) {
            return null;
         } else {
            K entity;
            if (ArrayUtils.isEmpty(constructorArguments)) {
               entity = introspection.instantiate();
            } else {
               int len = constructorArguments.length;
               Object[] args = new Object[len];

               for(int i = 0; i < len; ++i) {
                  RuntimePersistentProperty<K> prop = constructorArguments[i];
                  if (prop == null) {
                     throw new DataAccessException("Constructor argument [" + constructorArguments[i].getName() + "] must have an associated getter.");
                  }

                  if (prop instanceof Association) {
                     RuntimeAssociation entityAssociation = (RuntimeAssociation)prop;
                     if (prop instanceof Embedded) {
                        args[i] = this.readEntity(rs, ctx.embedded((Embedded)prop), null, null);
                     } else {
                        Relation.Kind kind = entityAssociation.getKind();
                        boolean isInverse = parent != null && isAssociation && ctx.association.getOwner() == entityAssociation.getAssociatedEntity();
                        if (isInverse && kind.isSingleEnded()) {
                           args[i] = parent;
                        } else {
                           SqlResultEntityTypeMapper.MappingContext<K> joinCtx = ctx.join(this.joinPaths, entityAssociation);
                           Object resolvedId = null;
                           if (!entityAssociation.isForeignKey()) {
                              resolvedId = this.readEntityId(rs, ctx.path(entityAssociation));
                           }

                           if (kind.isSingleEnded()) {
                              if (joinCtx.jp != null && (resolvedId != null || entityAssociation.isForeignKey())) {
                                 args[i] = this.<K>readEntity(rs, joinCtx, null, resolvedId);
                              } else {
                                 args[i] = this.buildIdOnlyEntity(rs, ctx.path(entityAssociation), resolvedId);
                              }
                           } else if (entityAssociation.getProperty().isReadOnly()) {
                              args[i] = this.resultReader.convertRequired(new ArrayList(0), entityAssociation.getProperty().getType());
                              if (joinCtx.jp != null) {
                                 SqlResultEntityTypeMapper.MappingContext<K> associatedCtx = joinCtx.copy();
                                 if (resolvedId == null) {
                                    resolvedId = this.readEntityId(rs, associatedCtx);
                                 }

                                 Object associatedEntity = null;
                                 if (resolvedId != null || entityAssociation.isForeignKey()) {
                                    associatedEntity = this.<K>readEntity(rs, associatedCtx, null, resolvedId);
                                 }

                                 if (associatedEntity != null) {
                                    joinCtx.associate(associatedCtx, resolvedId, associatedEntity);
                                 }
                              }
                           }
                        }
                     }
                  } else {
                     Object v;
                     if (resolveId != null && prop.equals(identity)) {
                        v = resolveId;
                     } else {
                        v = this.readProperty(rs, ctx, prop);
                        if (v == null) {
                           if (!prop.isOptional() && !nullableEmbedded) {
                              AnnotationMetadata entityAnnotationMetadata = ctx.persistentEntity.getAnnotationMetadata();
                              if (!entityAnnotationMetadata.hasAnnotation(Embeddable.class) && !entityAnnotationMetadata.hasAnnotation(EmbeddedId.class)) {
                                 throw new DataAccessException(
                                    "Null value read for non-null constructor argument [" + prop.getName() + "] of type: " + persistentEntity.getName()
                                 );
                              }

                              return null;
                           }

                           args[i] = null;
                           continue;
                        }
                     }

                     args[i] = this.convert(prop, v);
                  }
               }

               if (nullableEmbedded && args.length > 0 && Arrays.stream(args).allMatch(Objects::isNull)) {
                  return null;
               }

               entity = introspection.instantiate(args);
            }

            if (id != null && identity != null) {
               BeanProperty<K, Object> idProperty = identity.getProperty();
               entity = (K)this.convertAndSetWithValue(entity, identity, idProperty, id);
            }

            RuntimePersistentProperty<K> version = persistentEntity.getVersion();
            if (version != null) {
               Object v = this.readProperty(rs, ctx, version);
               if (v != null) {
                  entity = (K)this.convertAndSetWithValue(entity, version, version.getProperty(), v);
               }
            }

            Iterator var29 = persistentEntity.getPersistentProperties().iterator();

            while(true) {
               RuntimePersistentProperty<K> rpp;
               while(true) {
                  if (!var29.hasNext()) {
                     return entity;
                  }

                  rpp = (RuntimePersistentProperty)var29.next();
                  if (!rpp.isReadOnly()) {
                     if (!rpp.isConstructorArgument()) {
                        break;
                     }

                     if (rpp instanceof Association) {
                        Association a = (Association)rpp;
                        Relation.Kind kind = a.getKind();
                        if (kind.isSingleEnded()) {
                           continue;
                        }
                        break;
                     }
                  }
               }

               BeanProperty<K, Object> property = rpp.getProperty();
               if (rpp instanceof Association) {
                  Association entityAssociation = (Association)rpp;
                  if (rpp instanceof Embedded) {
                     Object value = this.readEntity(rs, ctx.embedded((Embedded)rpp), parent == null ? entity : parent, null);
                     entity = this.setProperty(property, entity, value);
                  } else {
                     boolean isInverse = parent != null
                        && entityAssociation.getKind().isSingleEnded()
                        && isAssociation
                        && ctx.association.getOwner() == entityAssociation.getAssociatedEntity();
                     if (isInverse) {
                        entity = this.setProperty(property, entity, parent);
                     } else {
                        SqlResultEntityTypeMapper.MappingContext<K> joinCtx = ctx.join(this.joinPaths, entityAssociation);
                        Object associatedId = null;
                        if (!entityAssociation.isForeignKey()) {
                           associatedId = this.readEntityId(rs, ctx.path(entityAssociation));
                           if (associatedId == null) {
                              continue;
                           }
                        }

                        if (joinCtx.jp != null) {
                           if (entityAssociation.getKind().isSingleEnded()) {
                              Object associatedEntity = this.<K>readEntity(rs, joinCtx, entity, associatedId);
                              entity = this.setProperty(property, entity, associatedEntity);
                           } else {
                              SqlResultEntityTypeMapper.MappingContext<K> associatedCtx = joinCtx.copy();
                              if (associatedId == null) {
                                 associatedId = this.readEntityId(rs, associatedCtx);
                              }

                              Object associatedEntity = this.<K>readEntity(rs, associatedCtx, entity, associatedId);
                              if (associatedEntity != null) {
                                 Objects.requireNonNull(associatedId);
                                 joinCtx.associate(associatedCtx, associatedId, associatedEntity);
                              }
                           }
                        } else if (entityAssociation.getKind().isSingleEnded() && !entityAssociation.isForeignKey()) {
                           Object value = this.buildIdOnlyEntity(rs, ctx.path(entityAssociation), associatedId);
                           entity = this.setProperty(property, entity, value);
                        }
                     }
                  }
               } else {
                  Object v = this.readProperty(rs, ctx, rpp);
                  if (v != null) {
                     entity = (K)this.convertAndSetWithValue(entity, rpp, property, v);
                  }
               }
            }
         }
      } catch (InstantiationException var25) {
         throw new DataAccessException("Error instantiating entity [" + persistentEntity.getName() + "]: " + var25.getMessage(), var25);
      }
   }

   private <K> Object readProperty(RS rs, SqlResultEntityTypeMapper.MappingContext<K> ctx, RuntimePersistentProperty<K> prop) {
      String columnName = ctx.namingStrategy.mappedName(ctx.embeddedPath, prop);
      if (ctx.prefix != null && ctx.prefix.length() != 0) {
         columnName = ctx.prefix + columnName;
      }

      Object result = this.resultReader.readDynamic(rs, columnName, prop.getDataType());
      AttributeConverter<Object, Object> converter = prop.getConverter();
      return converter != null ? converter.convertToEntityValue(result, ConversionContext.of(prop.getArgument())) : result;
   }

   private <K> K triggerPostLoad(RuntimePersistentEntity<?> persistentEntity, K entity) {
      K finalEntity;
      if (this.eventListener != null && persistentEntity.hasPostLoadEventListeners()) {
         finalEntity = (K)this.eventListener.apply(persistentEntity, entity);
      } else {
         finalEntity = entity;
      }

      return finalEntity;
   }

   @Nullable
   private <K> Object readEntityId(RS rs, SqlResultEntityTypeMapper.MappingContext<K> ctx) {
      RuntimePersistentProperty<K> identity = ctx.persistentEntity.getIdentity();
      if (identity == null) {
         return null;
      } else {
         return identity instanceof Embedded ? this.readEntity(rs, ctx.embedded((Embedded)identity), null, null) : this.readProperty(rs, ctx, identity);
      }
   }

   private Object convertAndSetWithValue(Object entity, RuntimePersistentProperty<?> rpp, BeanProperty property, Object v) {
      return this.setProperty(property, entity, this.convert(rpp, v));
   }

   private Object convert(RuntimePersistentProperty<?> rpp, Object v) {
      Class<?> propertyType = rpp.getType();
      if (v instanceof Array) {
         try {
            v = ((Array)v).getArray();
         } catch (SQLException var5) {
            throw new DataAccessException("Error getting an array value: " + var5.getMessage(), var5);
         }
      }

      if (propertyType.isInstance(v)) {
         return v;
      } else {
         if (this.jsonCodec != null && rpp.getDataType() == DataType.JSON) {
            try {
               return this.jsonCodec.decode(rpp.getArgument(), v.toString());
            } catch (Exception var6) {
            }
         }

         return this.resultReader.convertRequired(v, rpp.getArgument());
      }
   }

   private <K> K buildIdOnlyEntity(RS rs, SqlResultEntityTypeMapper.MappingContext<K> ctx, Object resolvedId) {
      RuntimePersistentProperty<K> identity = ctx.persistentEntity.getIdentity();
      if (identity != null) {
         BeanIntrospection<K> associatedIntrospection = ctx.persistentEntity.getIntrospection();
         Argument<?>[] constructorArgs = associatedIntrospection.getConstructorArguments();
         if (constructorArgs.length == 0) {
            Object associated = associatedIntrospection.instantiate();
            if (resolvedId == null) {
               resolvedId = this.readEntityId(rs, ctx);
            }

            BeanWrapper.getWrapper(associated).setProperty(identity.getName(), resolvedId);
            return (K)associated;
         }

         if (constructorArgs.length == 1) {
            Argument<?> arg = constructorArgs[0];
            if (arg.getName().equals(identity.getName()) && arg.getType() == identity.getType()) {
               if (resolvedId == null) {
                  resolvedId = this.readEntityId(rs, ctx);
               }

               return associatedIntrospection.instantiate(this.resultReader.convertRequired(resolvedId, identity.getType()));
            }
         }
      }

      return null;
   }

   public RuntimePersistentEntity<R> getPersistentEntity() {
      return this.entity;
   }

   private static final class MappingContext<E> {
      private final RuntimePersistentEntity<E> rootPersistentEntity;
      private final RuntimePersistentEntity<E> persistentEntity;
      private final NamingStrategy namingStrategy;
      private final String prefix;
      private final JoinPath jp;
      private final List<Association> joinPath;
      private final List<Association> embeddedPath;
      private final Association association;
      private Map<Object, SqlResultEntityTypeMapper.MappingContext> manyAssociations;
      private Map<Association, SqlResultEntityTypeMapper.MappingContext> associations;
      private E entity;

      private MappingContext(
         RuntimePersistentEntity rootPersistentEntity,
         RuntimePersistentEntity persistentEntity,
         NamingStrategy namingStrategy,
         String prefix,
         JoinPath jp,
         List<Association> joinPath,
         List<Association> embeddedPath,
         Association association
      ) {
         this.rootPersistentEntity = rootPersistentEntity;
         this.persistentEntity = persistentEntity;
         this.namingStrategy = namingStrategy;
         this.prefix = prefix;
         this.jp = jp;
         this.joinPath = joinPath;
         this.embeddedPath = embeddedPath;
         this.association = association;
      }

      public static <K> SqlResultEntityTypeMapper.MappingContext<K> of(RuntimePersistentEntity<K> persistentEntity, String prefix) {
         return new SqlResultEntityTypeMapper.MappingContext<>(
            persistentEntity, persistentEntity, persistentEntity.getNamingStrategy(), prefix, null, Collections.emptyList(), Collections.emptyList(), null
         );
      }

      public <K> SqlResultEntityTypeMapper.MappingContext<K> embedded(Embedded embedded) {
         if (this.associations == null) {
            this.associations = new LinkedHashMap();
         }

         return (SqlResultEntityTypeMapper.MappingContext<K>)this.associations.computeIfAbsent(embedded, e -> this.embeddedAssociation(embedded));
      }

      public <K> SqlResultEntityTypeMapper.MappingContext<K> path(Association association) {
         RuntimePersistentEntity<K> associatedEntity = (RuntimePersistentEntity)association.getAssociatedEntity();
         return new SqlResultEntityTypeMapper.MappingContext<>(
            this.rootPersistentEntity,
            associatedEntity,
            this.namingStrategy,
            this.prefix,
            this.jp,
            this.joinPath,
            associated(this.embeddedPath, association),
            association
         );
      }

      public <K> SqlResultEntityTypeMapper.MappingContext<K> join(Map<String, JoinPath> joinPaths, Association association) {
         if (this.associations == null) {
            this.associations = new LinkedHashMap();
         }

         return (SqlResultEntityTypeMapper.MappingContext<K>)this.associations.computeIfAbsent(association, a -> this.joinAssociation(joinPaths, association));
      }

      public <K> SqlResultEntityTypeMapper.MappingContext<K> associate(
         SqlResultEntityTypeMapper.MappingContext<K> ctx, @NotNull Object associationId, @NotNull Object entity
      ) {
         ctx.entity = (K)entity;
         if (this.manyAssociations == null) {
            this.manyAssociations = new LinkedHashMap();
         }

         this.manyAssociations.put(associationId, ctx);
         return ctx;
      }

      private <K> SqlResultEntityTypeMapper.MappingContext<K> copy() {
         return new SqlResultEntityTypeMapper.MappingContext<>(
            this.rootPersistentEntity, this.persistentEntity, this.namingStrategy, this.prefix, this.jp, this.joinPath, this.embeddedPath, this.association
         );
      }

      private <K> SqlResultEntityTypeMapper.MappingContext<K> joinAssociation(Map<String, JoinPath> joinPaths, Association association) {
         JoinPath jp = this.findJoinPath(joinPaths, association);
         RuntimePersistentEntity<K> associatedEntity = (RuntimePersistentEntity)association.getAssociatedEntity();
         return new SqlResultEntityTypeMapper.MappingContext<>(
            this.rootPersistentEntity,
            associatedEntity,
            associatedEntity.getNamingStrategy(),
            jp == null ? this.prefix : (String)jp.getAlias().orElse(this.prefix),
            jp,
            associated(this.joinPath, association),
            Collections.emptyList(),
            association
         );
      }

      private <K> SqlResultEntityTypeMapper.MappingContext<K> embeddedAssociation(Embedded embedded) {
         RuntimePersistentEntity<K> associatedEntity = (RuntimePersistentEntity)embedded.getAssociatedEntity();
         return new SqlResultEntityTypeMapper.MappingContext<>(
            this.rootPersistentEntity,
            associatedEntity,
            (NamingStrategy)associatedEntity.findNamingStrategy().orElse(this.namingStrategy),
            this.prefix,
            this.jp,
            this.joinPath,
            associated(this.embeddedPath, embedded),
            embedded
         );
      }

      private JoinPath findJoinPath(Map<String, JoinPath> joinPaths, Association association) {
         JoinPath jp = null;
         if (!joinPaths.isEmpty()) {
            String path = this.asPath(this.joinPath, this.embeddedPath, association);
            jp = (JoinPath)joinPaths.get(path);
            if (jp == null) {
               path = this.asPath(this.joinPath, association);
               jp = (JoinPath)joinPaths.get(path);
               if (jp == null) {
                  RuntimePersistentProperty<E> identity = this.rootPersistentEntity.getIdentity();
                  if (identity instanceof Embedded) {
                     path = identity.getName() + "." + path;
                  }

                  jp = (JoinPath)joinPaths.get(path);
               }
            }
         }

         if (jp == null) {
            return null;
         } else {
            String alias = (String)jp.getAlias().orElse(null);
            if (alias == null) {
               alias = association.getAliasName();
               if (!this.embeddedPath.isEmpty()) {
                  StringBuilder sb = this.prefix == null ? new StringBuilder() : new StringBuilder(this.prefix);

                  for(Association embedded : this.embeddedPath) {
                     sb.append(embedded.getName());
                     sb.append('_');
                  }

                  sb.append(alias);
                  alias = sb.toString();
               } else {
                  alias = this.prefix == null ? alias : this.prefix + alias;
               }
            }

            return new JoinPath(jp.getPath(), jp.getAssociationPath(), jp.getJoinType(), alias);
         }
      }

      private String asPath(List<Association> joinPath, List<Association> embeddedPath, PersistentProperty property) {
         if (joinPath.isEmpty() && embeddedPath.isEmpty()) {
            return property.getName();
         } else {
            StringJoiner joiner = new StringJoiner(".");

            for(Association association : joinPath) {
               joiner.add(association.getName());
            }

            for(Association association : embeddedPath) {
               joiner.add(association.getName());
            }

            joiner.add(property.getName());
            return joiner.toString();
         }
      }

      private String asPath(List<Association> associations, PersistentProperty property) {
         if (associations.isEmpty()) {
            return property.getName();
         } else {
            StringJoiner joiner = new StringJoiner(".");

            for(Association association : associations) {
               joiner.add(association.getName());
            }

            joiner.add(property.getName());
            return joiner.toString();
         }
      }

      private static List<Association> associated(List<Association> associations, Association association) {
         List<Association> newAssociations = new ArrayList(associations.size() + 1);
         newAssociations.addAll(associations);
         newAssociations.add(association);
         return newAssociations;
      }
   }

   public interface PushingMapper<RS, R> {
      void processRow(RS row);

      R getResult();
   }
}

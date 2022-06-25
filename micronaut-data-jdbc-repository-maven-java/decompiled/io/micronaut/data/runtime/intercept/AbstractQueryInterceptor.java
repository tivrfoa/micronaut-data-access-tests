package io.micronaut.data.runtime.intercept;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanWrapper;
import io.micronaut.core.beans.exceptions.IntrospectionException;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.MutableArgumentValue;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.exceptions.EmptyResultException;
import io.micronaut.data.intercept.DataInterceptor;
import io.micronaut.data.intercept.RepositoryMethodKey;
import io.micronaut.data.intercept.annotation.DataMethod;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.Sort;
import io.micronaut.data.model.runtime.AbstractPreparedDataOperation;
import io.micronaut.data.model.runtime.BatchOperation;
import io.micronaut.data.model.runtime.DefaultStoredDataOperation;
import io.micronaut.data.model.runtime.DeleteBatchOperation;
import io.micronaut.data.model.runtime.DeleteOperation;
import io.micronaut.data.model.runtime.EntityInstanceOperation;
import io.micronaut.data.model.runtime.EntityOperation;
import io.micronaut.data.model.runtime.InsertBatchOperation;
import io.micronaut.data.model.runtime.InsertOperation;
import io.micronaut.data.model.runtime.PagedQuery;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.model.runtime.StoredQuery;
import io.micronaut.data.model.runtime.UpdateBatchOperation;
import io.micronaut.data.model.runtime.UpdateOperation;
import io.micronaut.data.operations.RepositoryOperations;
import io.micronaut.data.runtime.query.DefaultPagedQueryResolver;
import io.micronaut.data.runtime.query.DefaultPreparedQueryResolver;
import io.micronaut.data.runtime.query.DefaultStoredQueryResolver;
import io.micronaut.data.runtime.query.PagedQueryResolver;
import io.micronaut.data.runtime.query.PreparedQueryResolver;
import io.micronaut.data.runtime.query.StoredQueryResolver;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractQueryInterceptor<T, R> implements DataInterceptor<T, R> {
   protected final RepositoryOperations operations;
   protected final PreparedQueryResolver preparedQueryResolver;
   private final ConcurrentMap<RepositoryMethodKey, StoredQuery> countQueries = new ConcurrentHashMap(50);
   private final ConcurrentMap<RepositoryMethodKey, StoredQuery> queries = new ConcurrentHashMap(50);
   private final StoredQueryResolver storedQueryResolver;
   private final PagedQueryResolver pagedQueryResolver;

   protected AbstractQueryInterceptor(@NonNull RepositoryOperations operations) {
      ArgumentUtils.requireNonNull("operations", operations);
      this.operations = operations;
      this.storedQueryResolver = (StoredQueryResolver)(operations instanceof StoredQueryResolver
         ? (StoredQueryResolver)operations
         : new DefaultStoredQueryResolver() {
            protected RepositoryOperations getHintsCapableRepository() {
               return operations;
            }
         });
      this.preparedQueryResolver = (PreparedQueryResolver)(operations instanceof PreparedQueryResolver
         ? (PreparedQueryResolver)operations
         : new DefaultPreparedQueryResolver() {
            @Override
            protected ConversionService getConversionService() {
               return operations.getConversionService();
            }
         });
      this.pagedQueryResolver = (PagedQueryResolver)(operations instanceof PagedQueryResolver
         ? (PagedQueryResolver)operations
         : new DefaultPagedQueryResolver());
   }

   protected Map<String, Object> getParameterValueMap(MethodInvocationContext<?, ?> context) {
      Argument<?>[] arguments = context.getArguments();
      Object[] parameterValues = context.getParameterValues();
      Map<String, Object> valueMap = new LinkedHashMap(arguments.length);

      for(int i = 0; i < parameterValues.length; ++i) {
         Object parameterValue = parameterValues[i];
         Argument arg = arguments[i];
         valueMap.put(arg.getAnnotationMetadata().stringValue(Parameter.class).orElseGet(arg::getName), parameterValue);
      }

      return valueMap;
   }

   protected Argument<?> getReturnType(MethodInvocationContext<?, ?> context) {
      return context.getReturnType().asArgument();
   }

   @Nullable
   protected final Object convertOne(MethodInvocationContext<?, ?> context, @Nullable Object o) {
      Argument<?> argumentType = this.getReturnType(context);
      Class<?> type = argumentType.getType();
      if (o == null) {
         if (type == Optional.class) {
            return Optional.empty();
         } else if (!argumentType.isDeclaredNonNull() && (argumentType.isNullable() || context.getReturnType().asArgument().isNullable())) {
            return null;
         } else {
            throw new EmptyResultException();
         }
      } else {
         boolean isOptional = false;
         if (type == Optional.class) {
            argumentType = (Argument)argumentType.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
            type = argumentType.getType();
            isOptional = true;
         }

         if (!type.isInstance(o)) {
            o = this.operations.getConversionService().convert(o, argumentType).orElseThrow(() -> new IllegalStateException("Unexpected return type: " + o));
         }

         return isOptional ? Optional.of(o) : o;
      }
   }

   protected final PreparedQuery<?, ?> prepareQuery(RepositoryMethodKey key, MethodInvocationContext<T, R> context) {
      return this.prepareQuery(key, context, null);
   }

   protected final <RT> PreparedQuery<?, RT> prepareQuery(RepositoryMethodKey methodKey, MethodInvocationContext<T, R> context, Class<RT> resultType) {
      return this.prepareQuery(methodKey, context, resultType, false);
   }

   protected final <RT> PreparedQuery<?, RT> prepareQuery(
      RepositoryMethodKey methodKey, MethodInvocationContext<T, R> context, Class<RT> resultType, boolean isCount
   ) {
      this.validateNullArguments(context);
      StoredQuery<?, RT> storedQuery = this.findStoreQuery(methodKey, context, resultType, isCount);
      Pageable pageable = storedQuery.hasPageable() ? this.getPageable(context) : Pageable.UNPAGED;
      return this.preparedQueryResolver.resolveQuery(context, storedQuery, pageable);
   }

   private <E, RT> StoredQuery<E, RT> findStoreQuery(MethodInvocationContext<?, ?> context, boolean isCount) {
      RepositoryMethodKey key = new RepositoryMethodKey(context.getTarget(), context.getExecutableMethod());
      return this.findStoreQuery(key, context, null, isCount);
   }

   private <E, RT> StoredQuery<E, RT> findStoreQuery(
      RepositoryMethodKey methodKey, MethodInvocationContext<?, ?> context, Class<RT> resultType, boolean isCount
   ) {
      StoredQuery<E, RT> storedQuery = (StoredQuery)this.queries.get(methodKey);
      if (storedQuery == null) {
         Class<E> rootEntity = (Class)context.classValue(DataMethod.NAME, "rootEntity")
            .orElseThrow(() -> new IllegalStateException("No root entity present in method"));
         if (resultType == null) {
            resultType = (Class)context.classValue(DataMethod.NAME, "resultType").orElse(rootEntity);
         }

         storedQuery = this.storedQueryResolver.resolveQuery(context, rootEntity, resultType);
         this.queries.put(methodKey, storedQuery);
      }

      return storedQuery;
   }

   protected final PreparedQuery<?, Number> prepareCountQuery(RepositoryMethodKey methodKey, @NonNull MethodInvocationContext<T, R> context) {
      StoredQuery storedQuery = (StoredQuery)this.countQueries.get(methodKey);
      if (storedQuery == null) {
         Class rootEntity = this.getRequiredRootEntity(context);
         storedQuery = this.storedQueryResolver.resolveCountQuery(context, rootEntity, Long.class);
         this.countQueries.put(methodKey, storedQuery);
      }

      Pageable pageable = storedQuery.hasPageable() ? this.getPageable(context) : Pageable.UNPAGED;
      return this.preparedQueryResolver.resolveCountQuery(context, storedQuery, pageable);
   }

   @NonNull
   protected <E> Class<E> getRequiredRootEntity(MethodInvocationContext context) {
      Class aClass = (Class)context.classValue(DataMethod.NAME, "rootEntity").orElse(null);
      if (aClass != null) {
         return aClass;
      } else {
         AnnotationValue<Annotation> ann = context.getDeclaredAnnotation(DataMethod.NAME);
         if (ann != null) {
            aClass = (Class)ann.classValue("rootEntity").orElse(null);
            if (aClass != null) {
               return aClass;
            }
         }

         throw new IllegalStateException("No root entity present in method");
      }
   }

   protected <RT> RT getEntityParameter(MethodInvocationContext<?, ?> context, @NonNull Class<RT> type) {
      return this.getRequiredParameterInRole(context, "entity", type);
   }

   protected <RT> Iterable<RT> getEntitiesParameter(MethodInvocationContext<?, ?> context, @NonNull Class<RT> type) {
      return this.getRequiredParameterInRole(context, "entities", Iterable.class);
   }

   protected <RT> Optional<RT> findEntityParameter(MethodInvocationContext<?, ?> context, @NonNull Class<RT> type) {
      return this.getParameterInRole(context, "entity", type);
   }

   protected <RT> Optional<Iterable<RT>> findEntitiesParameter(MethodInvocationContext<?, ?> context, @NonNull Class<RT> type) {
      return this.getParameterInRole(context, "entities", Iterable.class);
   }

   private <RT> RT getRequiredParameterInRole(MethodInvocationContext<?, ?> context, @NonNull String role, @NonNull Class<RT> type) {
      return (RT)this.getParameterInRole(context, role, type).orElseThrow(() -> new IllegalStateException("Cannot find parameter with role: " + role));
   }

   private <RT> Optional<RT> getParameterInRole(MethodInvocationContext<?, ?> context, @NonNull String role, @NonNull Class<RT> type) {
      return context.stringValue(DataMethod.NAME, role).flatMap(name -> {
         RT parameterValue = null;
         Map<String, MutableArgumentValue<?>> params = context.getParameters();
         MutableArgumentValue<?> arg = (MutableArgumentValue)params.get(name);
         if (arg != null) {
            Object o = arg.getValue();
            if (o != null) {
               if (type.isInstance(o)) {
                  parameterValue = (RT)o;
               } else {
                  parameterValue = (RT)this.operations.getConversionService().convert(o, type).orElse(null);
               }
            }
         }

         return Optional.ofNullable(parameterValue);
      });
   }

   @NonNull
   protected Pageable getPageable(MethodInvocationContext<?, ?> context) {
      Pageable pageable = (Pageable)this.getParameterInRole(context, "pageable", Pageable.class).orElse(null);
      if (pageable == null) {
         Sort sort = (Sort)this.getParameterInRole(context, "sort", Sort.class).orElse(null);
         if (sort != null) {
            int max = context.intValue(DataMethod.NAME, "pageSize").orElse(-1);
            int pageIndex = context.intValue(DataMethod.NAME, "pageIndex").orElse(0);
            if (max > 0) {
               pageable = Pageable.from(pageIndex, max, sort);
            } else {
               pageable = Pageable.from(sort);
            }
         } else {
            int max = context.intValue(DataMethod.NAME, "pageSize").orElse(-1);
            if (max > -1) {
               return Pageable.from(0, max);
            }
         }
      }

      return pageable != null ? pageable : Pageable.UNPAGED;
   }

   protected boolean isNullable(@NonNull AnnotationMetadata metadata) {
      return metadata.getDeclaredAnnotationNames().stream().anyMatch(n -> NameUtils.getSimpleName(n).equalsIgnoreCase("nullable"));
   }

   @NonNull
   protected Object getRequiredEntity(MethodInvocationContext<T, ?> context) {
      String entityParam = (String)context.stringValue(DataMethod.NAME, "entity").orElseThrow(() -> new IllegalStateException("No entity parameter specified"));
      Object o = context.getParameterValueMap().get(entityParam);
      if (o == null) {
         throw new IllegalArgumentException("Entity argument cannot be null");
      } else {
         return o;
      }
   }

   private <RT> void storeInParameterValues(
      MethodInvocationContext<T, R> context,
      StoredQuery<?, RT> storedQuery,
      Map<String, Object> namedValues,
      Object index,
      String argument,
      Map parameterValues
   ) {
      if (namedValues.containsKey(argument)) {
         parameterValues.put(index, namedValues.get(argument));
      } else {
         int i = argument.indexOf(46);
         if (i <= -1) {
            for(Argument a : context.getArguments()) {
               String n = (String)a.getAnnotationMetadata().stringValue(Parameter.class).orElse(a.getName());
               if (n.equals(argument)) {
                  parameterValues.put(index, namedValues.get(a.getName()));
                  return;
               }
            }

            throw new IllegalArgumentException("Missing query arguments: " + argument);
         }

         String argumentName = argument.substring(0, i);
         Object o = namedValues.get(argumentName);
         if (o != null) {
            try {
               BeanWrapper<Object> wrapper = BeanWrapper.getWrapper(o);
               String prop = argument.substring(i + 1);
               Object val = wrapper.getRequiredProperty(prop, Object.class);
               parameterValues.put(index, val);
            } catch (IntrospectionException var13) {
               throw new DataAccessException("Embedded value [" + o + "] should be annotated with introspected");
            }
         }
      }

   }

   @NonNull
   protected Object instantiateEntity(@NonNull Class<?> rootEntity, @NonNull Map<String, Object> parameterValues) {
      PersistentEntity entity = this.operations.getEntity(rootEntity);
      BeanIntrospection<?> introspection = BeanIntrospection.getIntrospection(rootEntity);
      Argument<?>[] constructorArguments = introspection.getConstructorArguments();
      Object instance;
      if (!ArrayUtils.isNotEmpty(constructorArguments)) {
         instance = introspection.instantiate();
      } else {
         Object[] arguments = new Object[constructorArguments.length];

         for(int i = 0; i < constructorArguments.length; ++i) {
            Argument<?> argument = constructorArguments[i];
            String argumentName = argument.getName();
            Object v = parameterValues.get(argumentName);
            AnnotationMetadata argMetadata = argument.getAnnotationMetadata();
            if (v == null && !PersistentProperty.isNullableMetadata(argMetadata)) {
               PersistentProperty prop = entity.getPropertyByName(argumentName);
               if (prop == null || prop.isRequired()) {
                  throw new IllegalArgumentException("Argument [" + argumentName + "] cannot be null");
               }
            }

            arguments[i] = v;
         }

         instance = introspection.instantiate(arguments);
      }

      BeanWrapper<Object> wrapper = BeanWrapper.getWrapper(instance);

      for(PersistentProperty prop : entity.getPersistentProperties()) {
         if (!prop.isReadOnly() && !prop.isGenerated()) {
            String propName = prop.getName();
            if (parameterValues.containsKey(propName)) {
               Object v = parameterValues.get(propName);
               if (v == null && !prop.isOptional()) {
                  throw new IllegalArgumentException("Argument [" + propName + "] cannot be null");
               }

               wrapper.setProperty(propName, v);
            } else if (prop.isRequired()) {
               Optional<Object> p = wrapper.getProperty(propName, Object.class);
               if (!p.isPresent()) {
                  throw new IllegalArgumentException("Argument [" + propName + "] cannot be null");
               }
            }
         }
      }

      return instance;
   }

   @Deprecated
   @Nullable
   protected Number convertNumberArgumentIfNecessary(Number number, Argument<?> argument) {
      Argument<?> firstTypeVar = (Argument)argument.getFirstTypeVariable().orElse(Argument.of(Long.class));
      Class<?> type = firstTypeVar.getType();
      if (type != Object.class && type != Void.class) {
         if (number == null) {
            number = 0;
         }

         return !type.isInstance(number)
            ? (Number)this.operations
               .getConversionService()
               .convert(number, firstTypeVar)
               .orElseThrow(() -> new IllegalStateException("Unsupported number type for return type: " + firstTypeVar))
            : number;
      } else {
         return null;
      }
   }

   @NonNull
   protected <E> PagedQuery<E> getPagedQuery(@NonNull MethodInvocationContext context) {
      return this.pagedQueryResolver.resolveQuery(context, this.getRequiredRootEntity(context), this.getPageable(context));
   }

   @NonNull
   protected <E> InsertBatchOperation<E> getInsertBatchOperation(@NonNull MethodInvocationContext context, @NonNull Iterable<E> iterable) {
      Class<E> rootEntity = this.getRequiredRootEntity(context);
      return this.getInsertBatchOperation(context, rootEntity, iterable);
   }

   @NonNull
   protected <E> InsertBatchOperation<E> getInsertBatchOperation(@NonNull MethodInvocationContext context, Class<E> rootEntity, @NonNull Iterable<E> iterable) {
      return new AbstractQueryInterceptor.DefaultInsertBatchOperation<>(context, rootEntity, iterable);
   }

   protected <E> InsertOperation<E> getInsertOperation(@NonNull MethodInvocationContext context) {
      E o = (E)this.getRequiredEntity(context);
      return new AbstractQueryInterceptor.DefaultInsertOperation<>(context, o);
   }

   protected <E> UpdateOperation<E> getUpdateOperation(@NonNull MethodInvocationContext<T, ?> context) {
      return this.getUpdateOperation(context, (E)this.getRequiredEntity(context));
   }

   protected <E> UpdateOperation<E> getUpdateOperation(@NonNull MethodInvocationContext<T, ?> context, E entity) {
      return new AbstractQueryInterceptor.DefaultUpdateOperation<>(context, entity);
   }

   @NonNull
   protected <E> UpdateBatchOperation<E> getUpdateAllBatchOperation(
      @NonNull MethodInvocationContext<T, ?> context, Class<E> rootEntity, @NonNull Iterable<E> iterable
   ) {
      return new AbstractQueryInterceptor.DefaultUpdateBatchOperation<>(context, rootEntity, iterable);
   }

   protected <E> DeleteOperation<E> getDeleteOperation(@NonNull MethodInvocationContext<T, ?> context, @NonNull E entity) {
      return new AbstractQueryInterceptor.DefaultDeleteOperation<>(context, entity);
   }

   @NonNull
   protected <E> DeleteBatchOperation<E> getDeleteAllBatchOperation(@NonNull MethodInvocationContext<T, ?> context) {
      Class<E> rootEntity = this.getRequiredRootEntity(context);
      return new AbstractQueryInterceptor.DefaultDeleteAllBatchOperation<>(context, rootEntity);
   }

   @NonNull
   protected <E> DeleteBatchOperation<E> getDeleteBatchOperation(@NonNull MethodInvocationContext<T, ?> context, @NonNull Iterable<E> iterable) {
      Class<E> rootEntity = this.getRequiredRootEntity(context);
      return this.getDeleteBatchOperation(context, rootEntity, iterable);
   }

   @NonNull
   protected <E> DeleteBatchOperation<E> getDeleteBatchOperation(
      @NonNull MethodInvocationContext<T, ?> context, Class<E> rootEntity, @NonNull Iterable<E> iterable
   ) {
      return new AbstractQueryInterceptor.DefaultDeleteBatchOperation<>(context, rootEntity, iterable);
   }

   protected <E> InsertOperation<E> getInsertOperation(@NonNull MethodInvocationContext<T, ?> context, E entity) {
      return new AbstractQueryInterceptor.DefaultInsertOperation<>(context, entity);
   }

   protected final void validateNullArguments(MethodInvocationContext<T, R> context) {
      Object[] parameterValues = context.getParameterValues();

      for(int i = 0; i < parameterValues.length; ++i) {
         Object o = parameterValues[i];
         if (o == null && !context.getArguments()[i].isNullable()) {
            throw new IllegalArgumentException(
               "Argument [" + context.getArguments()[i].getName() + "] value is null and the method parameter is not declared as nullable"
            );
         }
      }

   }

   protected int count(Iterable<?> iterable) {
      if (iterable instanceof Collection) {
         return ((Collection)iterable).size();
      } else {
         Iterator<?> iterator = iterable.iterator();
         int i = 0;

         while(iterator.hasNext()) {
            ++i;
            iterator.next();
         }

         return i;
      }
   }

   protected boolean isNumber(@Nullable Class<?> type) {
      if (type == null) {
         return false;
      } else {
         return type.isPrimitive()
            ? ClassUtils.getPrimitiveType(type.getName()).map(aClass -> Number.class.isAssignableFrom(ReflectionUtils.getWrapperType(aClass))).orElse(false)
            : Number.class.isAssignableFrom(type);
      }
   }

   private abstract class AbstractEntityInstanceOperation<E>
      extends AbstractQueryInterceptor<T, R>.AbstractEntityOperation<E>
      implements EntityInstanceOperation<E> {
      private final E entity;

      AbstractEntityInstanceOperation(MethodInvocationContext<?, ?> method, E entity) {
         super(method, entity.getClass());
         this.entity = entity;
      }

      @NonNull
      @Override
      public E getEntity() {
         return this.entity;
      }
   }

   private abstract class AbstractEntityOperation<E> extends AbstractPreparedDataOperation<E> implements EntityOperation<E> {
      protected final MethodInvocationContext<?, ?> method;
      protected final Class<E> rootEntity;
      protected StoredQuery<E, ?> storedQuery;

      AbstractEntityOperation(MethodInvocationContext<?, ?> method, Class<E> rootEntity) {
         super(method, new DefaultStoredDataOperation<>(method.getExecutableMethod()));
         this.method = method;
         this.rootEntity = rootEntity;
      }

      @Override
      public StoredQuery<E, ?> getStoredQuery() {
         if (this.storedQuery == null) {
            String queryString = (String)this.method.stringValue(Query.class).orElse(null);
            if (queryString == null) {
               return null;
            }

            this.storedQuery = AbstractQueryInterceptor.this.findStoreQuery(this.method, false);
         }

         return this.storedQuery;
      }

      @Override
      public <RT1> Optional<RT1> getParameterInRole(@NonNull String role, @NonNull Class<RT1> type) {
         return AbstractQueryInterceptor.this.getParameterInRole(this.method, role, type);
      }

      @NonNull
      @Override
      public Class<E> getRootEntity() {
         return this.rootEntity;
      }

      @NonNull
      @Override
      public Class<?> getRepositoryType() {
         return this.method.getTarget().getClass();
      }

      @NonNull
      @Override
      public String getName() {
         return this.method.getMethodName();
      }
   }

   private class DefaultBatchOperation<E> extends AbstractQueryInterceptor<T, R>.AbstractEntityOperation<E> implements BatchOperation<E> {
      protected final Iterable<E> iterable;

      public DefaultBatchOperation(MethodInvocationContext<?, ?> method, @NonNull Class<E> rootEntity, Iterable<E> iterable) {
         super(method, rootEntity);
         this.iterable = iterable;
      }

      public Iterator<E> iterator() {
         return this.iterable.iterator();
      }
   }

   private class DefaultDeleteAllBatchOperation<E> extends AbstractQueryInterceptor<T, R>.DefaultBatchOperation<E> implements DeleteBatchOperation<E> {
      DefaultDeleteAllBatchOperation(MethodInvocationContext<?, ?> method, @NonNull Class<E> rootEntity) {
         super(method, rootEntity, Collections.emptyList());
      }

      @Override
      public boolean all() {
         return true;
      }

      @Override
      public List<DeleteOperation<E>> split() {
         throw new IllegalStateException("Split is not supported for delete all operation!");
      }
   }

   private class DefaultDeleteBatchOperation<E> extends AbstractQueryInterceptor<T, R>.DefaultBatchOperation<E> implements DeleteBatchOperation<E> {
      DefaultDeleteBatchOperation(MethodInvocationContext<?, ?> method, @NonNull Class<E> rootEntity, Iterable<E> iterable) {
         super(method, rootEntity, iterable);
      }

      @Override
      public List<DeleteOperation<E>> split() {
         List<DeleteOperation<E>> deletes = new ArrayList(10);

         for(E e : this.iterable) {
            deletes.add(AbstractQueryInterceptor.this.new DefaultDeleteOperation(this.method, e));
         }

         return deletes;
      }
   }

   private class DefaultDeleteOperation<E> extends AbstractQueryInterceptor<T, R>.AbstractEntityInstanceOperation<E> implements DeleteOperation<E> {
      DefaultDeleteOperation(MethodInvocationContext<?, ?> method, E entity) {
         super(method, entity);
      }
   }

   private class DefaultInsertBatchOperation<E> extends AbstractQueryInterceptor<T, R>.DefaultBatchOperation<E> implements InsertBatchOperation<E> {
      DefaultInsertBatchOperation(MethodInvocationContext<?, ?> method, @NonNull Class<E> rootEntity, Iterable<E> iterable) {
         super(method, rootEntity, iterable);
      }

      @Override
      public List<InsertOperation<E>> split() {
         List<InsertOperation<E>> inserts = new ArrayList(10);

         for(E e : this.iterable) {
            inserts.add(AbstractQueryInterceptor.this.new DefaultInsertOperation(this.method, e));
         }

         return inserts;
      }
   }

   private final class DefaultInsertOperation<E> extends AbstractQueryInterceptor<T, R>.AbstractEntityOperation<E> implements InsertOperation<E> {
      private final E entity;

      DefaultInsertOperation(MethodInvocationContext<?, ?> method, E entity) {
         super(method, entity.getClass());
         this.entity = entity;
      }

      @Override
      public E getEntity() {
         return this.entity;
      }
   }

   private class DefaultUpdateBatchOperation<E> extends AbstractQueryInterceptor<T, R>.DefaultBatchOperation<E> implements UpdateBatchOperation<E> {
      DefaultUpdateBatchOperation(MethodInvocationContext<?, ?> method, @NonNull Class<E> rootEntity, Iterable<E> iterable) {
         super(method, rootEntity, iterable);
      }

      @Override
      public List<UpdateOperation<E>> split() {
         List<UpdateOperation<E>> updates = new ArrayList(10);

         for(E e : this.iterable) {
            updates.add(AbstractQueryInterceptor.this.new DefaultUpdateOperation(this.method, e));
         }

         return updates;
      }
   }

   private final class DefaultUpdateOperation<E> extends AbstractQueryInterceptor<T, R>.AbstractEntityOperation<E> implements UpdateOperation<E> {
      private final E entity;

      DefaultUpdateOperation(MethodInvocationContext<?, ?> method, E entity) {
         super(method, entity.getClass());
         this.entity = entity;
      }

      @Override
      public E getEntity() {
         return this.entity;
      }
   }
}

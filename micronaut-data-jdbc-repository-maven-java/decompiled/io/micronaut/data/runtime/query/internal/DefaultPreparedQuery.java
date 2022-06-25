package io.micronaut.data.runtime.query.internal;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.MutableArgumentValue;
import io.micronaut.data.intercept.annotation.DataMethod;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.runtime.DefaultStoredDataOperation;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.model.runtime.StoredQuery;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Internal
public final class DefaultPreparedQuery<E, RT> extends DefaultStoredDataOperation<RT> implements DelegateStoredQuery<E, RT>, PreparedQuery<E, RT> {
   private static final String DATA_METHOD_ANN_NAME = DataMethod.class.getName();
   private final Pageable pageable;
   private final StoredQuery<E, RT> storedQuery;
   private final String query;
   private final boolean dto;
   private final MethodInvocationContext<?, ?> context;
   private final ConversionService<? extends ConversionService> conversionService;

   public DefaultPreparedQuery(
      MethodInvocationContext<?, ?> context,
      StoredQuery<E, RT> storedQuery,
      String finalQuery,
      @NonNull Pageable pageable,
      boolean dtoProjection,
      ConversionService<?> conversionService
   ) {
      super(context);
      this.context = context;
      this.query = finalQuery;
      this.storedQuery = storedQuery;
      this.pageable = pageable;
      this.dto = dtoProjection;
      this.conversionService = conversionService;
   }

   public MethodInvocationContext<?, ?> getContext() {
      return this.context;
   }

   @Override
   public Class<E> getRootEntity() {
      return this.storedQuery.getRootEntity();
   }

   @Override
   public Map<String, Object> getQueryHints() {
      return this.storedQuery.getQueryHints();
   }

   @Override
   public StoredQuery<E, RT> getStoredQueryDelegate() {
      return this.storedQuery;
   }

   @Override
   public <RT1> Optional<RT1> getParameterInRole(@NonNull String role, @NonNull Class<RT1> type) {
      return this.context.stringValue(DATA_METHOD_ANN_NAME, role).flatMap(name -> {
         RT1 parameterValue = null;
         Map<String, MutableArgumentValue<?>> params = this.context.getParameters();
         MutableArgumentValue<?> arg = (MutableArgumentValue)params.get(name);
         if (arg != null) {
            Object o = arg.getValue();
            if (o != null) {
               if (type.isInstance(o)) {
                  parameterValue = (RT1)o;
               } else {
                  parameterValue = (RT1)this.conversionService.convert(o, type).orElse(null);
               }
            }
         }

         return Optional.ofNullable(parameterValue);
      });
   }

   @Override
   public Class<?> getRepositoryType() {
      return this.context.getTarget().getClass();
   }

   @NonNull
   @Override
   public Map<String, Object> getParameterValues() {
      return Collections.emptyMap();
   }

   @Override
   public Object[] getParameterArray() {
      return this.context.getParameterValues();
   }

   @Override
   public Argument[] getArguments() {
      return this.context.getArguments();
   }

   @NonNull
   @Override
   public Pageable getPageable() {
      return this.storedQuery.isCount() ? Pageable.UNPAGED : this.pageable;
   }

   @Override
   public boolean isDtoProjection() {
      return this.dto;
   }

   @NonNull
   @Override
   public String getQuery() {
      return this.query;
   }

   @NonNull
   @Override
   public ConvertibleValues<Object> getAttributes() {
      return this.context.getAttributes();
   }

   @NonNull
   @Override
   public Optional<Object> getAttribute(CharSequence name) {
      return this.context.getAttribute(name);
   }

   @NonNull
   @Override
   public <T> Optional<T> getAttribute(CharSequence name, Class<T> type) {
      return this.context.getAttribute(name, type);
   }
}

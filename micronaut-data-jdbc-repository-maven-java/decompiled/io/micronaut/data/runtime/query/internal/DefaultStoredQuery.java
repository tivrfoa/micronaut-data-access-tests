package io.micronaut.data.runtime.query.internal;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.QueryHint;
import io.micronaut.data.annotation.RepositoryConfiguration;
import io.micronaut.data.intercept.annotation.DataMethod;
import io.micronaut.data.intercept.annotation.DataMethodQueryParameter;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.query.JoinPath;
import io.micronaut.data.model.query.builder.sql.SqlQueryBuilder;
import io.micronaut.data.model.runtime.DefaultStoredDataOperation;
import io.micronaut.data.model.runtime.QueryParameterBinding;
import io.micronaut.data.model.runtime.StoredQuery;
import io.micronaut.data.operations.HintsCapableRepository;
import io.micronaut.inject.ExecutableMethod;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Internal
public final class DefaultStoredQuery<E, RT> extends DefaultStoredDataOperation<RT> implements StoredQuery<E, RT> {
   private static final String DATA_METHOD_ANN_NAME = DataMethod.class.getName();
   private static final int[] EMPTY_INT_ARRAY = new int[0];
   @NonNull
   private final Class<RT> resultType;
   @NonNull
   private final Class<E> rootEntity;
   @NonNull
   private final String query;
   private final String[] queryParts;
   private final ExecutableMethod<?, ?> method;
   private final boolean isDto;
   private final boolean isOptimisticLock;
   private final boolean isNative;
   private final boolean isNumericPlaceHolder;
   private final boolean hasPageable;
   private final AnnotationMetadata annotationMetadata;
   private final boolean isCount;
   private final DataType[] indexedDataTypes;
   private final boolean hasResultConsumer;
   private Map<String, Object> queryHints;
   private Set<JoinPath> joinFetchPaths = null;
   private final List<StoredQueryParameter> queryParameters;

   public DefaultStoredQuery(
      @NonNull ExecutableMethod<?, ?> method,
      @NonNull Class<RT> resultType,
      @NonNull Class<E> rootEntity,
      @NonNull String query,
      boolean isCount,
      HintsCapableRepository repositoryOperations
   ) {
      super(method);
      this.resultType = ReflectionUtils.getWrapperType(resultType);
      this.rootEntity = rootEntity;
      this.annotationMetadata = method.getAnnotationMetadata();
      this.isNative = method.isTrue(Query.class, "nativeQuery");
      this.hasResultConsumer = method.stringValue(DATA_METHOD_ANN_NAME, "sqlMappingFunction").isPresent();
      this.isNumericPlaceHolder = method.classValue(RepositoryConfiguration.class, "queryBuilder").map(c -> c == SqlQueryBuilder.class).orElse(false);
      this.hasPageable = method.stringValue(DATA_METHOD_ANN_NAME, "pageable").isPresent()
         || method.stringValue(DATA_METHOD_ANN_NAME, "sort").isPresent()
         || method.intValue(DATA_METHOD_ANN_NAME, "pageSize").orElse(-1) > -1;
      if (isCount) {
         this.query = (String)method.stringValue(Query.class, "rawCountQuery").orElse(query);
         this.queryParts = method.stringValues(DataMethod.class, "expandableCountQuery");
      } else {
         this.query = (String)method.stringValue(Query.class, "rawQuery").orElse(query);
         this.queryParts = method.stringValues(DataMethod.class, "expandableQuery");
      }

      this.method = method;
      this.isDto = method.isTrue(DATA_METHOD_ANN_NAME, "dto");
      this.isOptimisticLock = method.isTrue(DATA_METHOD_ANN_NAME, "optimisticLock");
      this.isCount = isCount;
      AnnotationValue<DataMethod> annotation = this.annotationMetadata.getAnnotation(DataMethod.class);
      if (method.hasAnnotation(QueryHint.class)) {
         List<AnnotationValue<QueryHint>> values = method.getAnnotationValuesByType(QueryHint.class);
         this.queryHints = new HashMap(values.size());

         for(AnnotationValue<QueryHint> value : values) {
            String n = (String)value.stringValue("name").orElse(null);
            String v = (String)value.stringValue("value").orElse(null);
            if (StringUtils.isNotEmpty(n) && StringUtils.isNotEmpty(v)) {
               this.queryHints.put(n, v);
            }
         }
      }

      Map<String, Object> queryHints = repositoryOperations.getQueryHints(this);
      if (queryHints != Collections.EMPTY_MAP) {
         if (this.queryHints != null) {
            this.queryHints.putAll(queryHints);
         } else {
            this.queryHints = queryHints;
         }
      }

      if (this.isNumericPlaceHolder) {
         this.indexedDataTypes = (DataType[])this.annotationMetadata
            .getValue(DataMethod.class, "parameterTypeDefs", DataType[].class)
            .orElse(DataType.EMPTY_DATA_TYPE_ARRAY);
      } else {
         this.indexedDataTypes = null;
      }

      if (annotation == null) {
         this.queryParameters = Collections.emptyList();
      } else {
         List<AnnotationValue<DataMethodQueryParameter>> params = annotation.getAnnotations("parameters", DataMethodQueryParameter.class);
         List<StoredQueryParameter> queryParameters = new ArrayList(params.size());

         for(AnnotationValue<DataMethodQueryParameter> av : params) {
            String[] propertyPath = av.stringValues("propertyPath");
            if (propertyPath.length == 0) {
               propertyPath = (String[])av.stringValue("property").map(property -> new String[]{property}).orElse(null);
            }

            String[] parameterBindingPath = av.stringValues("parameterBindingPath");
            if (parameterBindingPath.length == 0) {
               parameterBindingPath = null;
            }

            queryParameters.add(
               new StoredQueryParameter(
                  (String)av.stringValue("name").orElse(null),
                  this.isNumericPlaceHolder ? (DataType)av.enumValue("dataType", DataType.class).orElse(DataType.OBJECT) : null,
                  av.intValue("parameterIndex").orElse(-1),
                  parameterBindingPath,
                  propertyPath,
                  av.booleanValue("autoPopulated").orElse(false),
                  av.booleanValue("requiresPreviousPopulatedValue").orElse(false),
                  (Class<?>)av.classValue("converter").orElse(null),
                  av.booleanValue("expandable").orElse(false),
                  queryParameters
               )
            );
         }

         this.queryParameters = queryParameters;
      }

   }

   @Override
   public List<QueryParameterBinding> getQueryBindings() {
      return this.queryParameters;
   }

   @NonNull
   @Override
   public Set<JoinPath> getJoinFetchPaths() {
      if (this.joinFetchPaths == null) {
         Set<JoinPath> set = (Set)this.method.getAnnotationValuesByType(Join.class).stream().filter(this::isJoinFetch).map(av -> {
            String path = (String)av.stringValue().orElseThrow(() -> new IllegalStateException("Should not include annotations without a value definition"));
            String alias = (String)av.stringValue("alias").orElse(null);
            return new JoinPath(path, new Association[0], Join.Type.DEFAULT, alias);
         }).collect(Collectors.toSet());
         this.joinFetchPaths = set.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(set);
      }

      return this.joinFetchPaths;
   }

   public ExecutableMethod<?, ?> getMethod() {
      return this.method;
   }

   @Override
   public boolean isSingleResult() {
      return !this.isCount() && this.getJoinFetchPaths().isEmpty();
   }

   @Override
   public boolean hasResultConsumer() {
      return this.hasResultConsumer;
   }

   private boolean isJoinFetch(AnnotationValue<Join> av) {
      if (!av.stringValue().isPresent()) {
         return false;
      } else {
         Optional<String> type = av.stringValue("type");
         return !type.isPresent() || ((String)type.get()).contains("FETCH");
      }
   }

   @Override
   public boolean isCount() {
      return this.isCount;
   }

   @NonNull
   @Override
   public DataType[] getIndexedParameterTypes() {
      return this.indexedDataTypes == null ? DataType.EMPTY_DATA_TYPE_ARRAY : this.indexedDataTypes;
   }

   @NonNull
   @Override
   public int[] getIndexedParameterBinding() {
      return EMPTY_INT_ARRAY;
   }

   @NonNull
   @Override
   public Map<String, Object> getQueryHints() {
      return this.queryHints != null ? this.queryHints : Collections.emptyMap();
   }

   @Override
   public boolean isNative() {
      return this.isNative;
   }

   @Override
   public boolean useNumericPlaceholders() {
      return this.isNumericPlaceHolder;
   }

   @Override
   public boolean isDtoProjection() {
      return this.isDto;
   }

   @NonNull
   @Override
   public Class<RT> getResultType() {
      return this.resultType;
   }

   @NonNull
   @Override
   public DataType getResultDataType() {
      return this.isCount
         ? DataType.LONG
         : (DataType)this.annotationMetadata.enumValue(DATA_METHOD_ANN_NAME, "resultDataType", DataType.class).orElse(DataType.OBJECT);
   }

   @Override
   public Optional<Class<?>> getEntityIdentifierType() {
      return this.annotationMetadata.classValue(DATA_METHOD_ANN_NAME, "idType");
   }

   @NonNull
   @Override
   public Class<E> getRootEntity() {
      return this.rootEntity;
   }

   @Override
   public boolean hasPageable() {
      return this.hasPageable;
   }

   @NonNull
   @Override
   public String getQuery() {
      return this.query;
   }

   @Override
   public String[] getExpandableQueryParts() {
      return this.queryParts;
   }

   @NonNull
   @Override
   public String getName() {
      return this.method.getMethodName();
   }

   @NonNull
   @Override
   public Class<?>[] getArgumentTypes() {
      return this.method.getArgumentTypes();
   }

   @Override
   public boolean isOptimisticLock() {
      return this.isOptimisticLock;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DefaultStoredQuery<?, ?> that = (DefaultStoredQuery)o;
         return this.resultType.equals(that.resultType) && this.method.equals(that.method);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.resultType, this.method});
   }
}

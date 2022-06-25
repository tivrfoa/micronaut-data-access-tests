package io.micronaut.data.model.query;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.Sort;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface QueryModel extends Criteria {
   @NonNull
   QueryModel idEq(Object parameter);

   @NonNull
   QueryModel versionEq(Object parameter);

   @NonNull
   QueryModel isEmpty(@NonNull String propertyName);

   @NonNull
   QueryModel isNotEmpty(@NonNull String propertyName);

   @NonNull
   QueryModel isNull(@NonNull String propertyName);

   @NonNull
   QueryModel isTrue(@NonNull String propertyName);

   @NonNull
   QueryModel isFalse(@NonNull String propertyName);

   @NonNull
   QueryModel isNotNull(String propertyName);

   @NonNull
   QueryModel eq(String propertyName, Object parameter);

   @NonNull
   QueryModel ne(@NonNull String propertyName, @NonNull Object parameter);

   @NonNull
   QueryModel between(@NonNull String propertyName, @NonNull Object start, @NonNull Object finish);

   @NonNull
   QueryModel gte(@NonNull String property, @NonNull Object parameter);

   @NonNull
   QueryModel ge(@NonNull String property, @NonNull Object parameter);

   @NonNull
   QueryModel gt(@NonNull String property, @NonNull Object parameter);

   @NonNull
   QueryModel lte(@NonNull String property, @NonNull Object parameter);

   @NonNull
   QueryModel le(@NonNull String property, @NonNull Object parameter);

   @NonNull
   QueryModel lt(@NonNull String property, @NonNull Object parameter);

   @NonNull
   QueryModel like(@NonNull String propertyName, @NonNull Object parameter);

   @NonNull
   QueryModel startsWith(@NonNull String propertyName, @NonNull Object parameter);

   @NonNull
   QueryModel endsWith(@NonNull String propertyName, @NonNull Object parameter);

   @NonNull
   QueryModel contains(@NonNull String propertyName, @NonNull Object parameter);

   @NonNull
   QueryModel ilike(@NonNull String propertyName, @NonNull Object parameter);

   @NonNull
   QueryModel rlike(@NonNull String propertyName, @NonNull Object parameter);

   @NonNull
   QueryModel and(@NonNull Criteria other);

   @NonNull
   QueryModel or(@NonNull Criteria other);

   @NonNull
   QueryModel not(@NonNull Criteria other);

   @NonNull
   QueryModel inList(@NonNull String propertyName, @NonNull QueryModel subquery);

   @NonNull
   QueryModel inList(@NonNull String propertyName, @NonNull Object parameter);

   @NonNull
   QueryModel notIn(@NonNull String propertyName, @NonNull QueryModel subquery);

   @NonNull
   QueryModel sizeEq(@NonNull String propertyName, @NonNull Object size);

   @NonNull
   QueryModel sizeGt(@NonNull String propertyName, @NonNull Object size);

   @NonNull
   QueryModel sizeGe(@NonNull String propertyName, @NonNull Object size);

   @NonNull
   QueryModel sizeLe(@NonNull String propertyName, @NonNull Object size);

   @NonNull
   QueryModel sizeLt(@NonNull String propertyName, @NonNull Object size);

   @NonNull
   QueryModel sizeNe(@NonNull String propertyName, @NonNull Object size);

   @NonNull
   QueryModel eqProperty(@NonNull String propertyName, @NonNull String otherPropertyName);

   @NonNull
   QueryModel neProperty(@NonNull String propertyName, @NonNull String otherPropertyName);

   @NonNull
   QueryModel gtProperty(@NonNull String propertyName, @NonNull String otherPropertyName);

   @NonNull
   QueryModel geProperty(@NonNull String propertyName, @NonNull String otherPropertyName);

   @NonNull
   QueryModel ltProperty(@NonNull String propertyName, @NonNull String otherPropertyName);

   @NonNull
   QueryModel leProperty(String propertyName, @NonNull String otherPropertyName);

   @NonNull
   QueryModel allEq(@NonNull Map<String, Object> propertyValues);

   @NonNull
   QueryModel eqAll(@NonNull String propertyName, @NonNull Criteria propertyValue);

   @NonNull
   QueryModel gtAll(@NonNull String propertyName, @NonNull Criteria propertyValue);

   @NonNull
   QueryModel ltAll(@NonNull String propertyName, @NonNull Criteria propertyValue);

   @NonNull
   QueryModel geAll(@NonNull String propertyName, @NonNull Criteria propertyValue);

   @NonNull
   QueryModel leAll(@NonNull String propertyName, @NonNull Criteria propertyValue);

   @NonNull
   QueryModel gtSome(@NonNull String propertyName, @NonNull Criteria propertyValue);

   @NonNull
   QueryModel geSome(@NonNull String propertyName, @NonNull Criteria propertyValue);

   @NonNull
   QueryModel ltSome(@NonNull String propertyName, @NonNull Criteria propertyValue);

   @NonNull
   QueryModel leSome(@NonNull String propertyName, @NonNull Criteria propertyValue);

   Collection<JoinPath> getJoinPaths();

   @NonNull
   PersistentEntity getPersistentEntity();

   @NonNull
   QueryModel.Junction getCriteria();

   @NonNull
   List<QueryModel.Projection> getProjections();

   Optional<JoinPath> getJoinPath(String path);

   @NonNull
   @Deprecated
   JoinPath join(String path, Association association, @NonNull Join.Type joinType, @Nullable String alias);

   @NonNull
   default JoinPath join(String path, @NonNull Join.Type joinType, @Nullable String alias) {
      return this.join(path, joinType, alias);
   }

   @NonNull
   default JoinPath join(@NonNull Association association, @NonNull Join.Type joinType) {
      if (this.getPersistentEntity() != association.getOwner()) {
         throw new IllegalArgumentException("The association " + association + " must be owned by: " + this.getPersistentEntity());
      } else {
         return this.join(association.getName(), association, joinType, null);
      }
   }

   @NonNull
   default JoinPath join(@NonNull Association association) {
      return this.join(association.getName(), association, Join.Type.DEFAULT, null);
   }

   @NonNull
   ProjectionList projections();

   @NonNull
   QueryModel add(@NonNull QueryModel.Criterion criterion);

   QueryModel max(int max);

   QueryModel offset(long offset);

   default Sort getSort() {
      return Sort.unsorted();
   }

   @NonNull
   QueryModel sort(@NonNull Sort sort);

   @NonNull
   static QueryModel from(@NonNull PersistentEntity entity) {
      ArgumentUtils.requireNonNull("entity", entity);
      return new DefaultQuery(entity);
   }

   int getMax();

   long getOffset();

   void forUpdate();

   boolean isForUpdate();

   public static class AvgProjection extends QueryModel.PropertyProjection {
      public AvgProjection(String propertyName) {
         super(propertyName);
      }
   }

   public static class Between extends QueryModel.PropertyCriterion {
      private String property;
      private Object from;
      private Object to;

      public Between(String property, Object from, Object to) {
         super(property, from);
         this.property = property;
         this.from = from;
         this.to = to;
      }

      @Override
      public String getProperty() {
         return this.property;
      }

      public Object getFrom() {
         return this.from;
      }

      public Object getTo() {
         return this.to;
      }
   }

   public static class Conjunction extends QueryModel.Junction {
   }

   public static class Contains extends QueryModel.PropertyCriterion {
      public Contains(String name, Object expression) {
         super(name, expression);
      }
   }

   public static class CountDistinctProjection extends QueryModel.PropertyProjection {
      public CountDistinctProjection(String property) {
         super(property);
      }
   }

   public static class CountProjection extends QueryModel.Projection {
   }

   public interface Criterion {
   }

   public static class Disjunction extends QueryModel.Junction {
   }

   public static class DistinctProjection extends QueryModel.Projection {
   }

   public static class DistinctPropertyProjection extends QueryModel.PropertyProjection {
      public DistinctPropertyProjection(String propertyName) {
         super(propertyName);
      }
   }

   public static class EndsWith extends QueryModel.PropertyCriterion {
      public EndsWith(String name, Object expression) {
         super(name, expression);
      }
   }

   public static class Equals extends QueryModel.PropertyCriterion {
      public Equals(String name, Object parameter) {
         super(name, parameter);
      }
   }

   public static class EqualsAll extends QueryModel.SubqueryCriterion {
      public EqualsAll(String name, QueryModel value) {
         super(name, value);
      }
   }

   public static class EqualsProperty extends QueryModel.PropertyComparisonCriterion {
      public EqualsProperty(String property, String otherProperty) {
         super(property, otherProperty);
      }
   }

   public static class Exists implements QueryModel.Criterion {
      private QueryModel subquery;

      public Exists(QueryModel subquery) {
         this.subquery = subquery;
      }

      public QueryModel getSubquery() {
         return this.subquery;
      }
   }

   public static class GreaterThan extends QueryModel.PropertyCriterion {
      public GreaterThan(String name, Object value) {
         super(name, value);
      }
   }

   public static class GreaterThanAll extends QueryModel.SubqueryCriterion {
      public GreaterThanAll(String name, QueryModel value) {
         super(name, value);
      }
   }

   public static class GreaterThanEquals extends QueryModel.PropertyCriterion {
      public GreaterThanEquals(String name, Object value) {
         super(name, value);
      }
   }

   public static class GreaterThanEqualsAll extends QueryModel.SubqueryCriterion {
      public GreaterThanEqualsAll(String name, QueryModel value) {
         super(name, value);
      }
   }

   public static class GreaterThanEqualsProperty extends QueryModel.PropertyComparisonCriterion {
      public GreaterThanEqualsProperty(String property, String otherProperty) {
         super(property, otherProperty);
      }
   }

   public static class GreaterThanEqualsSome extends QueryModel.SubqueryCriterion {
      public GreaterThanEqualsSome(String name, QueryModel value) {
         super(name, value);
      }
   }

   public static class GreaterThanProperty extends QueryModel.PropertyComparisonCriterion {
      public GreaterThanProperty(String property, String otherProperty) {
         super(property, otherProperty);
      }
   }

   public static class GreaterThanSome extends QueryModel.SubqueryCriterion {
      public GreaterThanSome(String name, QueryModel value) {
         super(name, value);
      }
   }

   public static class GroupPropertyProjection extends QueryModel.PropertyProjection {
      public GroupPropertyProjection(String property) {
         super(property);
      }
   }

   public static class ILike extends QueryModel.Like {
      public ILike(String name, Object expression) {
         super(name, expression);
      }
   }

   public static class IdEquals extends QueryModel.PropertyCriterion {
      private static final String ID = "id";

      public IdEquals(Object value) {
         super("id", value);
      }
   }

   public static class IdProjection extends QueryModel.Projection {
   }

   public static class In extends QueryModel.PropertyCriterion {
      private QueryModel subquery;

      public In(String name, Object parameter) {
         super(name, parameter);
      }

      public In(String name, QueryModel subquery) {
         super(name, subquery);
         this.subquery = subquery;
      }

      public String getName() {
         return this.getProperty();
      }

      @Nullable
      public QueryModel getSubquery() {
         return this.subquery;
      }
   }

   public static class IsEmpty extends QueryModel.PropertyNameCriterion {
      public IsEmpty(String name) {
         super(name);
      }
   }

   public static class IsFalse extends QueryModel.PropertyNameCriterion {
      public IsFalse(String name) {
         super(name);
      }
   }

   public static class IsNotEmpty extends QueryModel.PropertyNameCriterion {
      public IsNotEmpty(String name) {
         super(name);
      }
   }

   public static class IsNotNull extends QueryModel.PropertyNameCriterion {
      public IsNotNull(String name) {
         super(name);
      }
   }

   public static class IsNull extends QueryModel.PropertyNameCriterion {
      public IsNull(String name) {
         super(name);
      }
   }

   public static class IsTrue extends QueryModel.PropertyNameCriterion {
      public IsTrue(String name) {
         super(name);
      }
   }

   public abstract static class Junction implements QueryModel.Criterion {
      private List<QueryModel.Criterion> criteria = new ArrayList();

      protected Junction() {
      }

      public Junction(List<QueryModel.Criterion> criteria) {
         this.criteria = criteria;
      }

      public QueryModel.Junction add(QueryModel.Criterion c) {
         if (c != null) {
            this.criteria.add(c);
         }

         return this;
      }

      public List<QueryModel.Criterion> getCriteria() {
         return this.criteria;
      }

      public boolean isEmpty() {
         return this.criteria.isEmpty();
      }
   }

   public static class LessThan extends QueryModel.PropertyCriterion {
      public LessThan(String name, Object value) {
         super(name, value);
      }
   }

   public static class LessThanAll extends QueryModel.SubqueryCriterion {
      public LessThanAll(String name, QueryModel value) {
         super(name, value);
      }
   }

   public static class LessThanEquals extends QueryModel.PropertyCriterion {
      public LessThanEquals(String name, Object value) {
         super(name, value);
      }
   }

   public static class LessThanEqualsAll extends QueryModel.SubqueryCriterion {
      public LessThanEqualsAll(String name, QueryModel value) {
         super(name, value);
      }
   }

   public static class LessThanEqualsProperty extends QueryModel.PropertyComparisonCriterion {
      public LessThanEqualsProperty(String property, String otherProperty) {
         super(property, otherProperty);
      }
   }

   public static class LessThanEqualsSome extends QueryModel.SubqueryCriterion {
      public LessThanEqualsSome(String name, QueryModel value) {
         super(name, value);
      }
   }

   public static class LessThanProperty extends QueryModel.PropertyComparisonCriterion {
      public LessThanProperty(String property, String otherProperty) {
         super(property, otherProperty);
      }
   }

   public static class LessThanSome extends QueryModel.SubqueryCriterion {
      public LessThanSome(String name, QueryModel value) {
         super(name, value);
      }
   }

   public static class Like extends QueryModel.PropertyCriterion {
      public Like(String name, Object expression) {
         super(name, expression);
      }
   }

   public static class LiteralProjection extends QueryModel.Projection {
      private final Object value;

      public LiteralProjection(Object value) {
         this.value = value;
      }

      public Object getValue() {
         return this.value;
      }
   }

   public static class MaxProjection extends QueryModel.PropertyProjection {
      public MaxProjection(String propertyName) {
         super(propertyName);
      }
   }

   public static class MinProjection extends QueryModel.PropertyProjection {
      public MinProjection(String propertyName) {
         super(propertyName);
      }
   }

   public static class Negation extends QueryModel.Junction {
   }

   public static class NotEquals extends QueryModel.PropertyCriterion {
      public NotEquals(String name, Object value) {
         super(name, value);
      }
   }

   public static class NotEqualsAll extends QueryModel.SubqueryCriterion {
      public NotEqualsAll(String name, QueryModel value) {
         super(name, value);
      }
   }

   public static class NotEqualsProperty extends QueryModel.PropertyComparisonCriterion {
      public NotEqualsProperty(String property, String otherProperty) {
         super(property, otherProperty);
      }
   }

   public static class NotExists implements QueryModel.Criterion {
      private QueryModel subquery;

      public NotExists(QueryModel subquery) {
         this.subquery = subquery;
      }

      public QueryModel getSubquery() {
         return this.subquery;
      }
   }

   public static class NotIn extends QueryModel.PropertyCriterion {
      private QueryModel subquery;

      public NotIn(String name, Object parameter) {
         super(name, parameter);
      }

      public NotIn(String name, QueryModel subquery) {
         super(name, subquery);
         this.subquery = subquery;
      }

      public String getName() {
         return this.getProperty();
      }

      public QueryModel getSubquery() {
         return this.subquery;
      }
   }

   public static class Projection {
   }

   public static class PropertyComparisonCriterion extends QueryModel.PropertyNameCriterion {
      final String otherProperty;

      protected PropertyComparisonCriterion(String property, String otherProperty) {
         super(property);
         this.otherProperty = otherProperty;
      }

      public String getOtherProperty() {
         return this.otherProperty;
      }
   }

   public static class PropertyCriterion extends QueryModel.PropertyNameCriterion {
      protected Object value;
      private boolean ignoreCase = false;

      public PropertyCriterion(String name, Object value) {
         super(name);
         this.value = value;
      }

      public Object getValue() {
         return this.value;
      }

      public void setValue(Object v) {
         this.value = v;
      }

      public boolean isIgnoreCase() {
         return this.ignoreCase;
      }

      public QueryModel.PropertyCriterion ignoreCase(boolean ignoreCase) {
         this.ignoreCase = ignoreCase;
         return this;
      }
   }

   public static class PropertyNameCriterion implements QueryModel.Criterion {
      protected String name;

      public PropertyNameCriterion(String name) {
         this.name = name;
      }

      public String getProperty() {
         return this.name;
      }
   }

   public static class PropertyProjection extends QueryModel.Projection {
      private String propertyName;
      private String alias;

      public PropertyProjection(String propertyName) {
         this.propertyName = propertyName;
      }

      public String getPropertyName() {
         return this.propertyName;
      }

      public QueryModel.PropertyProjection aliased() {
         this.alias = this.propertyName;
         return this;
      }

      public void setAlias(String alias) {
         this.alias = alias;
      }

      public Optional<String> getAlias() {
         return Optional.ofNullable(this.alias);
      }
   }

   public static class RLike extends QueryModel.Like {
      public RLike(String name, Object expression) {
         super(name, expression);
      }
   }

   public static class Regex extends QueryModel.PropertyCriterion {
      public Regex(String name, Object expression) {
         super(name, expression);
      }
   }

   public static class SizeEquals extends QueryModel.PropertyCriterion {
      public SizeEquals(String name, Object parameter) {
         super(name, parameter);
      }
   }

   public static class SizeGreaterThan extends QueryModel.PropertyCriterion {
      public SizeGreaterThan(String name, Object parameter) {
         super(name, parameter);
      }
   }

   public static class SizeGreaterThanEquals extends QueryModel.PropertyCriterion {
      public SizeGreaterThanEquals(String name, Object parameter) {
         super(name, parameter);
      }
   }

   public static class SizeLessThan extends QueryModel.PropertyCriterion {
      public SizeLessThan(String name, Object parameter) {
         super(name, parameter);
      }
   }

   public static class SizeLessThanEquals extends QueryModel.PropertyCriterion {
      public SizeLessThanEquals(String name, Object parameter) {
         super(name, parameter);
      }
   }

   public static class SizeNotEquals extends QueryModel.PropertyCriterion {
      public SizeNotEquals(String name, Object parameter) {
         super(name, parameter);
      }
   }

   public static class StartsWith extends QueryModel.PropertyCriterion {
      public StartsWith(String name, Object expression) {
         super(name, expression);
      }
   }

   public static class SubqueryCriterion extends QueryModel.PropertyCriterion {
      public SubqueryCriterion(String name, QueryModel value) {
         super(name, value);
      }

      public QueryModel getValue() {
         return (QueryModel)super.getValue();
      }
   }

   public static class SumProjection extends QueryModel.PropertyProjection {
      public SumProjection(String propertyName) {
         super(propertyName);
      }
   }

   public static class VersionEquals extends QueryModel.PropertyCriterion {
      private static final String VERSION = "version";

      public VersionEquals(Object value) {
         super("version", value);
      }
   }
}

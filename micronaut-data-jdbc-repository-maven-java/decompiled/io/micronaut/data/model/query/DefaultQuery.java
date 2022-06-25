package io.micronaut.data.model.query;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.PersistentPropertyPath;
import io.micronaut.data.model.Sort;
import io.micronaut.data.model.query.factory.Restrictions;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class DefaultQuery implements QueryModel {
   private final PersistentEntity entity;
   private final QueryModel.Junction criteria = new QueryModel.Conjunction();
   private final DefaultProjectionList projections = new DefaultProjectionList();
   private final Map<String, JoinPath> joinPaths = new HashMap(2);
   private int max = -1;
   private long offset = 0L;
   private Sort sort = Sort.unsorted();
   private boolean forUpdate;

   protected DefaultQuery(@NonNull PersistentEntity entity) {
      ArgumentUtils.requireNonNull("entity", entity);
      this.entity = entity;
   }

   @Override
   public Collection<JoinPath> getJoinPaths() {
      return Collections.unmodifiableCollection(this.joinPaths.values());
   }

   public AssociationQuery createQuery(String associationName) {
      PersistentProperty property = (PersistentProperty)this.entity.getPropertyByPath(associationName).orElse(null);
      if (!(property instanceof Association)) {
         throw new IllegalArgumentException(
            "Cannot query association [" + associationName + "] of class [" + this.entity + "]. The specified property is not an association."
         );
      } else {
         Association association = (Association)property;
         return new AssociationQuery(associationName, association);
      }
   }

   @NonNull
   @Override
   public PersistentEntity getPersistentEntity() {
      return this.entity;
   }

   @Override
   public QueryModel.Junction getCriteria() {
      return this.criteria;
   }

   @NonNull
   @Override
   public List<QueryModel.Projection> getProjections() {
      return this.projections.getProjectionList();
   }

   @Override
   public Optional<JoinPath> getJoinPath(String path) {
      return path != null ? Optional.ofNullable(this.joinPaths.get(path)) : Optional.empty();
   }

   @Override
   public JoinPath join(@NonNull String path, @NonNull Join.Type joinType, String alias) {
      PersistentEntity entity = this.getEntity();
      PersistentPropertyPath propertyPath = entity.getPropertyPath(path);
      if (propertyPath == null) {
         throw new IllegalArgumentException("Invalid association path. Element [" + path + "] is not an association for [" + entity + "]");
      } else {
         Association[] associationPath;
         if (propertyPath.getProperty() instanceof Association) {
            associationPath = (Association[])Stream.concat(propertyPath.getAssociations().stream(), Stream.of(propertyPath.getProperty()))
               .toArray(x$0 -> new Association[x$0]);
         } else {
            associationPath = (Association[])propertyPath.getAssociations().toArray(new Association[0]);
         }

         JoinPath jp = new JoinPath(path, associationPath, joinType, alias);
         this.joinPaths.put(path, jp);
         return jp;
      }
   }

   @NonNull
   @Override
   public JoinPath join(String path, Association association, @NonNull Join.Type joinType, @Nullable String alias) {
      return this.join(path, joinType, alias);
   }

   @Override
   public ProjectionList projections() {
      return this.projections;
   }

   @NonNull
   @Override
   public QueryModel add(@NonNull QueryModel.Criterion criterion) {
      ArgumentUtils.requireNonNull("criterion", criterion);
      QueryModel.Junction currentJunction = this.criteria;
      this.add(currentJunction, criterion);
      return this;
   }

   private void add(QueryModel.Junction currentJunction, QueryModel.Criterion criterion) {
      this.addToJunction(currentJunction, criterion);
   }

   public PersistentEntity getEntity() {
      return this.entity;
   }

   public QueryModel.Junction disjunction() {
      QueryModel.Junction currentJunction = this.criteria;
      return this.disjunction(currentJunction);
   }

   public QueryModel.Junction conjunction() {
      QueryModel.Junction currentJunction = this.criteria;
      return this.conjunction(currentJunction);
   }

   public QueryModel.Junction negation() {
      QueryModel.Junction currentJunction = this.criteria;
      return this.negation(currentJunction);
   }

   private QueryModel.Junction negation(QueryModel.Junction currentJunction) {
      QueryModel.Negation dis = new QueryModel.Negation();
      currentJunction.add(dis);
      return dis;
   }

   public DefaultQuery max(int max) {
      this.max = max;
      return this;
   }

   @Override
   public int getMax() {
      return this.max;
   }

   @Override
   public long getOffset() {
      return this.offset;
   }

   @Override
   public void forUpdate() {
      this.forUpdate = true;
   }

   @Override
   public boolean isForUpdate() {
      return this.forUpdate;
   }

   public DefaultQuery offset(long offset) {
      this.offset = offset;
      return this;
   }

   @Override
   public Sort getSort() {
      return this.sort;
   }

   @NonNull
   @Override
   public QueryModel sort(@NonNull Sort sort) {
      ArgumentUtils.requireNonNull("sort", sort);
      this.sort = sort;
      return this;
   }

   @NonNull
   public DefaultQuery eq(@NonNull String property, @NonNull Object parameter) {
      this.criteria.add(Restrictions.eq(property, parameter));
      return this;
   }

   @NonNull
   public DefaultQuery allEq(@NonNull Map<String, Object> values) {
      QueryModel.Junction conjunction = this.conjunction();

      for(String property : values.keySet()) {
         Object value = values.get(property);
         conjunction.add(Restrictions.eq(property, value));
      }

      return this;
   }

   @NonNull
   @Override
   public QueryModel eqAll(@NonNull String propertyName, @NonNull Criteria propertyValue) {
      return null;
   }

   @NonNull
   @Override
   public QueryModel gtAll(@NonNull String propertyName, @NonNull Criteria propertyValue) {
      return null;
   }

   @NonNull
   @Override
   public QueryModel ltAll(@NonNull String propertyName, @NonNull Criteria propertyValue) {
      return null;
   }

   @NonNull
   @Override
   public QueryModel geAll(@NonNull String propertyName, @NonNull Criteria propertyValue) {
      return null;
   }

   @NonNull
   @Override
   public QueryModel leAll(@NonNull String propertyName, @NonNull Criteria propertyValue) {
      return null;
   }

   @NonNull
   @Override
   public QueryModel gtSome(@NonNull String propertyName, @NonNull Criteria propertyValue) {
      return null;
   }

   @NonNull
   @Override
   public QueryModel geSome(@NonNull String propertyName, @NonNull Criteria propertyValue) {
      return null;
   }

   @NonNull
   @Override
   public QueryModel ltSome(@NonNull String propertyName, @NonNull Criteria propertyValue) {
      return null;
   }

   @NonNull
   @Override
   public QueryModel leSome(@NonNull String propertyName, @NonNull Criteria propertyValue) {
      return null;
   }

   public DefaultQuery versionEq(@NonNull Object value) {
      this.criteria.add(Restrictions.versionEq(value));
      return this;
   }

   @NonNull
   public DefaultQuery isEmpty(@NonNull String property) {
      this.criteria.add(Restrictions.isEmpty(property));
      return this;
   }

   @NonNull
   public DefaultQuery isNotEmpty(@NonNull String property) {
      this.criteria.add(Restrictions.isNotEmpty(property));
      return this;
   }

   @NonNull
   public DefaultQuery isNull(@NonNull String property) {
      this.criteria.add(Restrictions.isNull(property));
      return this;
   }

   @NonNull
   @Override
   public QueryModel isTrue(@NonNull String propertyName) {
      this.criteria.add(Restrictions.isTrue(propertyName));
      return this;
   }

   @NonNull
   @Override
   public QueryModel isFalse(@NonNull String propertyName) {
      this.criteria.add(Restrictions.isFalse(propertyName));
      return this;
   }

   @NonNull
   public DefaultQuery isNotNull(@NonNull String property) {
      this.criteria.add(Restrictions.isNotNull(property));
      return this;
   }

   @NonNull
   public DefaultQuery idEq(@NonNull Object value) {
      this.criteria.add(Restrictions.idEq(value));
      return this;
   }

   @NonNull
   @Override
   public QueryModel ne(@NonNull String propertyName, @NonNull Object parameter) {
      this.criteria.add(Restrictions.ne(propertyName, parameter));
      return this;
   }

   @NonNull
   public DefaultQuery gt(@NonNull String property, @NonNull Object value) {
      this.criteria.add(Restrictions.gt(property, value));
      return this;
   }

   public DefaultQuery gte(String property, Object value) {
      this.criteria.add(Restrictions.gte(property, value));
      return this;
   }

   public DefaultQuery lte(String property, Object value) {
      this.criteria.add(Restrictions.lte(property, value));
      return this;
   }

   public DefaultQuery ge(String property, Object value) {
      return this.gte(property, value);
   }

   public DefaultQuery le(String property, Object value) {
      return this.lte(property, value);
   }

   public DefaultQuery lt(String property, Object value) {
      this.criteria.add(Restrictions.lt(property, value));
      return this;
   }

   @NonNull
   public DefaultQuery like(@NonNull String propertyName, @NonNull Object parameter) {
      this.criteria.add(Restrictions.like(propertyName, parameter));
      return this;
   }

   @NonNull
   @Override
   public QueryModel startsWith(@NonNull String propertyName, @NonNull Object parameter) {
      this.criteria.add(Restrictions.startsWith(propertyName, parameter));
      return this;
   }

   @NonNull
   @Override
   public QueryModel endsWith(@NonNull String propertyName, @NonNull Object parameter) {
      this.criteria.add(Restrictions.endsWith(propertyName, parameter));
      return this;
   }

   @NonNull
   @Override
   public QueryModel contains(@NonNull String propertyName, @NonNull Object parameter) {
      this.criteria.add(Restrictions.contains(propertyName, parameter));
      return this;
   }

   @NonNull
   public DefaultQuery ilike(@NonNull String propertyName, @NonNull Object parameter) {
      this.criteria.add(Restrictions.ilike(propertyName, parameter));
      return this;
   }

   @NonNull
   public DefaultQuery rlike(@NonNull String propertyName, @NonNull Object parameter) {
      this.criteria.add(Restrictions.rlike(propertyName, parameter));
      return this;
   }

   @NonNull
   @Override
   public QueryModel and(@NonNull Criteria other) {
      return this;
   }

   @NonNull
   @Override
   public QueryModel or(@NonNull Criteria other) {
      return this;
   }

   @NonNull
   @Override
   public QueryModel not(@NonNull Criteria other) {
      return this;
   }

   @NonNull
   public DefaultQuery inList(@NonNull String propertyName, @NonNull QueryModel subquery) {
      this.criteria.add(Restrictions.in(propertyName, subquery));
      return this;
   }

   public DefaultQuery inList(String property, Object values) {
      this.criteria.add(Restrictions.in(property, values));
      return this;
   }

   @NonNull
   public DefaultQuery notIn(@NonNull String propertyName, @NonNull QueryModel subquery) {
      this.criteria.add(Restrictions.notIn(propertyName, subquery));
      return this;
   }

   @NonNull
   public DefaultQuery sizeEq(@NonNull String propertyName, @NonNull Object size) {
      this.criteria.add(Restrictions.sizeEq(propertyName, size));
      return this;
   }

   @NonNull
   public DefaultQuery sizeGt(@NonNull String propertyName, @NonNull Object size) {
      this.criteria.add(Restrictions.sizeGt(propertyName, size));
      return this;
   }

   @NonNull
   public DefaultQuery sizeGe(@NonNull String propertyName, @NonNull Object size) {
      this.criteria.add(Restrictions.sizeGe(propertyName, size));
      return this;
   }

   @NonNull
   public DefaultQuery sizeLe(@NonNull String propertyName, @NonNull Object size) {
      this.criteria.add(Restrictions.sizeLe(propertyName, size));
      return this;
   }

   @NonNull
   public DefaultQuery sizeLt(@NonNull String propertyName, @NonNull Object size) {
      this.criteria.add(Restrictions.sizeLt(propertyName, size));
      return this;
   }

   @NonNull
   public DefaultQuery sizeNe(@NonNull String propertyName, @NonNull Object size) {
      this.criteria.add(Restrictions.sizeNe(propertyName, size));
      return this;
   }

   @NonNull
   public DefaultQuery eqProperty(@NonNull String propertyName, @NonNull String otherPropertyName) {
      this.criteria.add(Restrictions.eqProperty(propertyName, otherPropertyName));
      return this;
   }

   @NonNull
   public DefaultQuery neProperty(@NonNull String propertyName, @NonNull String otherPropertyName) {
      this.criteria.add(Restrictions.neProperty(propertyName, otherPropertyName));
      return this;
   }

   @NonNull
   public DefaultQuery gtProperty(@NonNull String propertyName, @NonNull String otherPropertyName) {
      this.criteria.add(Restrictions.gtProperty(propertyName, otherPropertyName));
      return this;
   }

   @NonNull
   public DefaultQuery geProperty(@NonNull String propertyName, @NonNull String otherPropertyName) {
      this.criteria.add(Restrictions.geProperty(propertyName, otherPropertyName));
      return this;
   }

   @NonNull
   public DefaultQuery ltProperty(@NonNull String propertyName, @NonNull String otherPropertyName) {
      this.criteria.add(Restrictions.ltProperty(propertyName, otherPropertyName));
      return this;
   }

   @NonNull
   public DefaultQuery leProperty(String propertyName, @NonNull String otherPropertyName) {
      this.criteria.add(Restrictions.leProperty(propertyName, otherPropertyName));
      return this;
   }

   public DefaultQuery between(String property, Object start, Object end) {
      this.criteria.add(Restrictions.between(property, start, end));
      return this;
   }

   public DefaultQuery and(QueryModel.Criterion a, QueryModel.Criterion b) {
      Objects.requireNonNull(a, "Left hand side of AND cannot be null");
      Objects.requireNonNull(b, "Right hand side of AND cannot be null");
      this.criteria.add(Restrictions.and(a, b));
      return this;
   }

   public DefaultQuery or(QueryModel.Criterion a, QueryModel.Criterion b) {
      Objects.requireNonNull(a, "Left hand side of AND cannot be null");
      Objects.requireNonNull(b, "Right hand side of AND cannot be null");
      this.criteria.add(Restrictions.or(a, b));
      return this;
   }

   private QueryModel.Junction disjunction(QueryModel.Junction currentJunction) {
      QueryModel.Disjunction dis = new QueryModel.Disjunction();
      currentJunction.add(dis);
      return dis;
   }

   private QueryModel.Junction conjunction(QueryModel.Junction currentJunction) {
      QueryModel.Conjunction con = new QueryModel.Conjunction();
      currentJunction.add(con);
      return con;
   }

   private void addToJunction(QueryModel.Junction currentJunction, QueryModel.Criterion criterion) {
      if (criterion instanceof QueryModel.PropertyCriterion) {
         QueryModel.PropertyCriterion pc = (QueryModel.PropertyCriterion)criterion;
         Object value = pc.getValue();
         pc.setValue(value);
      }

      if (criterion instanceof QueryModel.Junction) {
         QueryModel.Junction j = (QueryModel.Junction)criterion;
         QueryModel.Junction newj;
         if (j instanceof QueryModel.Disjunction) {
            newj = this.disjunction(currentJunction);
         } else if (j instanceof QueryModel.Negation) {
            newj = this.negation(currentJunction);
         } else {
            newj = this.conjunction(currentJunction);
         }

         for(QueryModel.Criterion c : j.getCriteria()) {
            this.addToJunction(newj, c);
         }
      } else {
         currentJunction.add(criterion);
      }

   }
}

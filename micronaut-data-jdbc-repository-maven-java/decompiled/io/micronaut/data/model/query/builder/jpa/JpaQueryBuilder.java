package io.micronaut.data.model.query.builder.jpa;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.Embedded;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.query.JoinPath;
import io.micronaut.data.model.query.QueryModel;
import io.micronaut.data.model.query.builder.AbstractSqlLikeQueryBuilder;
import io.micronaut.data.model.query.builder.QueryBuilder;
import io.micronaut.data.model.query.builder.QueryResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Internal
public class JpaQueryBuilder extends AbstractSqlLikeQueryBuilder implements QueryBuilder {
   public JpaQueryBuilder() {
      this.addCriterionHandler(QueryModel.EqualsAll.class, (ctx, criterion) -> this.handleSubQuery(ctx, criterion, " = ALL ("));
      this.addCriterionHandler(QueryModel.NotEqualsAll.class, (queryState, criterion) -> this.handleSubQuery(queryState, criterion, " != ALL ("));
      this.addCriterionHandler(QueryModel.GreaterThanAll.class, (queryState, criterion) -> this.handleSubQuery(queryState, criterion, " > ALL ("));
      this.addCriterionHandler(QueryModel.GreaterThanSome.class, (queryState, criterion) -> this.handleSubQuery(queryState, criterion, " > SOME ("));
      this.addCriterionHandler(QueryModel.GreaterThanEqualsAll.class, (queryState, criterion) -> this.handleSubQuery(queryState, criterion, " >= ALL ("));
      this.addCriterionHandler(QueryModel.GreaterThanEqualsSome.class, (queryState, criterion) -> this.handleSubQuery(queryState, criterion, " >= SOME ("));
      this.addCriterionHandler(QueryModel.LessThanAll.class, (queryState, criterion) -> this.handleSubQuery(queryState, criterion, " < ALL ("));
      this.addCriterionHandler(QueryModel.LessThanSome.class, (queryState, criterion) -> this.handleSubQuery(queryState, criterion, " < SOME ("));
      this.addCriterionHandler(QueryModel.LessThanEqualsAll.class, (queryState, criterion) -> this.handleSubQuery(queryState, criterion, " <= ALL ("));
      this.addCriterionHandler(QueryModel.LessThanEqualsSome.class, (queryState, criterion) -> this.handleSubQuery(queryState, criterion, " <= SOME ("));
   }

   @Override
   protected String quote(String persistedName) {
      return persistedName;
   }

   @Override
   public String getAliasName(PersistentEntity entity) {
      return (String)entity.getAnnotationMetadata().stringValue(MappedEntity.class, "alias").orElseGet(() -> entity.getDecapitalizedName() + "_");
   }

   @Override
   protected String[] buildJoin(
      String alias,
      JoinPath joinPath,
      String joinType,
      StringBuilder target,
      Map<String, String> appliedJoinPaths,
      AbstractSqlLikeQueryBuilder.QueryState queryState
   ) {
      Association[] associationPath = joinPath.getAssociationPath();
      if (ArrayUtils.isEmpty(associationPath)) {
         throw new IllegalArgumentException("Invalid association path [" + joinPath.getPath() + "]");
      } else {
         List<Association> joinAssociationsPath = new ArrayList(associationPath.length);
         String[] joinAliases = new String[associationPath.length];
         StringJoiner pathSoFar = new StringJoiner(".");
         List<String> aliases = new ArrayList();

         for(int i = 0; i < associationPath.length; ++i) {
            Association association = associationPath[i];
            pathSoFar.add(association.getName());
            if (association instanceof Embedded) {
               joinAssociationsPath.add(association);
            } else {
               String currentPath = pathSoFar.toString();
               String existingAlias = (String)appliedJoinPaths.get(currentPath);
               if (existingAlias != null) {
                  joinAliases[i] = existingAlias;
                  aliases.add(existingAlias);
               } else {
                  int finalI = i;
                  JoinPath joinPathToUse = (JoinPath)queryState.getQueryModel()
                     .getJoinPath(currentPath)
                     .orElseGet(
                        () -> new JoinPath(
                              currentPath,
                              (Association[])Arrays.copyOfRange(associationPath, 0, finalI + 1),
                              joinPath.getJoinType(),
                              (String)joinPath.getAlias().orElse(null)
                           )
                     );
                  String currentAlias = this.getAliasName(joinPathToUse);
                  joinAliases[i] = currentAlias;
                  String lastJoinAlias = aliases.isEmpty() ? alias : CollectionUtils.last(aliases);
                  target.append(joinType).append(lastJoinAlias).append('.').append(association.getName()).append(' ').append(joinAliases[i]);
                  aliases.add(currentAlias);
               }

               joinAssociationsPath.clear();
            }
         }

         return joinAliases;
      }
   }

   @Override
   protected String getTableName(PersistentEntity entity) {
      return entity.getName();
   }

   @Override
   protected String getColumnName(PersistentProperty persistentProperty) {
      return persistentProperty.getName();
   }

   @Override
   protected void selectAllColumns(AbstractSqlLikeQueryBuilder.QueryState queryState, StringBuilder queryBuffer) {
      queryBuffer.append(queryState.getRootAlias());
   }

   @Override
   protected void selectAllColumns(PersistentEntity entity, String alias, StringBuilder queryBuffer) {
      queryBuffer.append(alias);
   }

   @Override
   protected void appendProjectionRowCount(StringBuilder queryString, String logicalName) {
      queryString.append("COUNT").append('(').append(logicalName).append(')');
   }

   @Override
   protected final boolean computePropertyPaths() {
      return false;
   }

   @Override
   protected boolean isAliasForBatch() {
      return true;
   }

   @Override
   protected AbstractSqlLikeQueryBuilder.Placeholder formatParameter(int index) {
      String n = "p" + index;
      return new AbstractSqlLikeQueryBuilder.Placeholder(":" + n, n);
   }

   @Override
   public String resolveJoinType(Join.Type jt) {
      String joinType;
      switch(jt) {
         case LEFT:
            joinType = " LEFT JOIN ";
            break;
         case LEFT_FETCH:
            joinType = " LEFT JOIN FETCH ";
            break;
         case RIGHT:
            joinType = " RIGHT JOIN ";
            break;
         case RIGHT_FETCH:
            joinType = " RIGHT JOIN FETCH ";
            break;
         case INNER:
         case FETCH:
            joinType = " JOIN FETCH ";
            break;
         default:
            joinType = " JOIN ";
      }

      return joinType;
   }

   @Nullable
   @Override
   public QueryResult buildInsert(AnnotationMetadata repositoryMetadata, PersistentEntity entity) {
      return null;
   }

   @NonNull
   @Override
   protected StringBuilder appendDeleteClause(StringBuilder queryString) {
      return queryString.append("DELETE ");
   }

   @NonNull
   @Override
   public QueryResult buildPagination(@NonNull Pageable pageable) {
      throw new UnsupportedOperationException("JPA-QL does not support pagination in query definitions");
   }
}

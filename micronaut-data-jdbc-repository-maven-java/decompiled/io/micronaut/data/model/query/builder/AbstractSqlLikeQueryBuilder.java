package io.micronaut.data.model.query.builder;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.annotation.DataTransformer;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Where;
import io.micronaut.data.annotation.repeatable.WhereSpecifications;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.Embedded;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.PersistentPropertyPath;
import io.micronaut.data.model.Sort;
import io.micronaut.data.model.jpa.criteria.impl.LiteralExpression;
import io.micronaut.data.model.naming.NamingStrategy;
import io.micronaut.data.model.query.AssociationQuery;
import io.micronaut.data.model.query.BindingParameter;
import io.micronaut.data.model.query.JoinPath;
import io.micronaut.data.model.query.QueryModel;
import io.micronaut.data.model.query.QueryParameter;
import io.micronaut.data.model.query.builder.sql.Dialect;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

public abstract class AbstractSqlLikeQueryBuilder implements QueryBuilder {
   public static final String ORDER_BY_CLAUSE = " ORDER BY ";
   protected static final String SELECT_CLAUSE = "SELECT ";
   protected static final String AS_CLAUSE = " AS ";
   protected static final String FROM_CLAUSE = " FROM ";
   protected static final String WHERE_CLAUSE = " WHERE ";
   protected static final char COMMA = ',';
   protected static final char CLOSE_BRACKET = ')';
   protected static final char OPEN_BRACKET = '(';
   protected static final char SPACE = ' ';
   protected static final char DOT = '.';
   protected static final String NOT_CLAUSE = " NOT";
   protected static final String AND = "AND";
   protected static final String LOGICAL_AND = " AND ";
   protected static final String UPDATE_CLAUSE = "UPDATE ";
   protected static final String DELETE_CLAUSE = "DELETE ";
   protected static final String OR = "OR";
   protected static final String LOGICAL_OR = " OR ";
   protected static final String FUNCTION_COUNT = "COUNT";
   protected static final String AVG = "AVG";
   protected static final String DISTINCT = "DISTINCT";
   protected static final String SUM = "SUM";
   protected static final String MIN = "MIN";
   protected static final String MAX = "MAX";
   protected static final String COUNT_DISTINCT = "COUNT(DISTINCT";
   protected static final String IS_NOT_NULL = " IS NOT NULL ";
   protected static final String IS_EMPTY = " IS EMPTY ";
   protected static final String IS_NOT_EMPTY = " IS NOT EMPTY ";
   protected static final String IS_NULL = " IS NULL ";
   protected static final String EQUALS_TRUE = " = TRUE ";
   protected static final String EQUALS_FALSE = " = FALSE ";
   protected static final String GREATER_THAN_OR_EQUALS = " >= ";
   protected static final String LESS_THAN_OR_EQUALS = " <= ";
   protected static final String LESS_THAN = " < ";
   protected static final String GREATER_THAN = " > ";
   protected static final String EQUALS = " = ";
   protected static final String NOT_EQUALS = " != ";
   protected static final String ALIAS_REPLACE = "@.";
   protected static final String ALIAS_REPLACE_QUOTED = "@\\.";
   protected final Map<Class, AbstractSqlLikeQueryBuilder.CriterionHandler> queryHandlers = new HashMap(30);

   public AbstractSqlLikeQueryBuilder() {
      this.addCriterionHandler(AssociationQuery.class, this::handleAssociationCriteria);
      this.addCriterionHandler(QueryModel.Negation.class, (ctx, negation) -> {
         ctx.query().append(" NOT").append('(');
         this.handleJunction(ctx, negation);
         ctx.query().append(')');
      });
      this.addCriterionHandler(QueryModel.Conjunction.class, (ctx, conjunction) -> {
         ctx.query().append('(');
         this.handleJunction(ctx, conjunction);
         ctx.query().append(')');
      });
      this.addCriterionHandler(QueryModel.Disjunction.class, (ctx, disjunction) -> {
         ctx.query().append('(');
         this.handleJunction(ctx, disjunction);
         ctx.query().append(')');
      });
      this.addCriterionHandler(QueryModel.Equals.class, this.optionalCaseValueComparison(" = "));
      this.addCriterionHandler(QueryModel.NotEquals.class, this.optionalCaseValueComparison(" != "));
      this.addCriterionHandler(QueryModel.EqualsProperty.class, this.comparison("="));
      this.addCriterionHandler(QueryModel.NotEqualsProperty.class, this.comparison("!="));
      this.addCriterionHandler(QueryModel.GreaterThanProperty.class, this.comparison(">"));
      this.addCriterionHandler(QueryModel.GreaterThanEqualsProperty.class, this.comparison(">="));
      this.addCriterionHandler(QueryModel.LessThanProperty.class, this.comparison("<"));
      this.addCriterionHandler(QueryModel.LessThanEqualsProperty.class, this.comparison("<="));
      this.addCriterionHandler(QueryModel.IsNull.class, this.expression(" IS NULL "));
      this.addCriterionHandler(QueryModel.IsTrue.class, this.expression(" = TRUE "));
      this.addCriterionHandler(QueryModel.IsFalse.class, this.expression(" = FALSE "));
      this.addCriterionHandler(QueryModel.IsNotNull.class, this.expression(" IS NOT NULL "));
      this.addCriterionHandler(
         QueryModel.IsEmpty.class, (ctx, isEmpty) -> this.appendEmptyExpression(ctx, " IS NULL OR ", " = '' ", " IS EMPTY ", isEmpty.getProperty())
      );
      this.addCriterionHandler(QueryModel.IsNotEmpty.class, (ctx, isNotEmpty) -> {
         if (this.getDialect() == Dialect.ORACLE) {
            AbstractSqlLikeQueryBuilder.QueryPropertyPath propertyPath = ctx.getRequiredProperty(isNotEmpty.getProperty(), QueryModel.IsEmpty.class);
            StringBuilder whereClause = ctx.query();
            if (propertyPath.getProperty().isAssignable(CharSequence.class)) {
               this.appendPropertyRef(whereClause, propertyPath);
               whereClause.append(" IS NOT NULL ");
            } else {
               this.appendPropertyRef(whereClause, propertyPath);
               whereClause.append(" IS NOT EMPTY ");
            }
         } else {
            this.appendEmptyExpression(ctx, " IS NOT NULL AND ", " <> '' ", " IS NOT EMPTY ", isNotEmpty.getProperty());
         }

      });
      this.addCriterionHandler(
         QueryModel.IdEquals.class,
         (ctx, idEquals) -> {
            StringBuilder whereClause = ctx.query();
            PersistentEntity persistentEntity = ctx.getPersistentEntity();
            if (persistentEntity.hasCompositeIdentity()) {
               for(PersistentProperty prop : persistentEntity.getCompositeIdentity()) {
                  this.appendCriteriaForOperator(whereClause, ctx, null, this.asQueryPropertyPath(ctx.getCurrentTableAlias(), prop), idEquals.getValue(), " = ");
                  whereClause.append(" AND ");
               }
   
               whereClause.setLength(whereClause.length() - " AND ".length());
            } else {
               if (!persistentEntity.hasIdentity()) {
                  throw new IllegalStateException("No ID found for entity: " + persistentEntity.getName());
               }
   
               this.appendCriteriaForOperator(
                  whereClause, ctx, ctx.getRequiredProperty(persistentEntity.getIdentity().getName(), idEquals.getClass()), idEquals.getValue(), " = "
               );
            }
   
         }
      );
      this.addCriterionHandler(QueryModel.VersionEquals.class, (ctx, criterion) -> {
         PersistentProperty prop = ctx.getPersistentEntity().getVersion();
         if (prop == null) {
            throw new IllegalStateException("No Version found for entity: " + ctx.getPersistentEntity().getName());
         } else {
            this.appendCriteriaForOperator(ctx.query(), ctx, this.asQueryPropertyPath(ctx.getCurrentTableAlias(), prop), criterion.getValue(), " = ");
         }
      });
      this.addCriterionHandler(QueryModel.GreaterThan.class, this.valueComparison(" > "));
      this.addCriterionHandler(QueryModel.LessThanEquals.class, this.valueComparison(" <= "));
      this.addCriterionHandler(QueryModel.GreaterThanEquals.class, this.valueComparison(" >= "));
      this.addCriterionHandler(QueryModel.LessThan.class, this.valueComparison(" < "));
      this.addCriterionHandler(QueryModel.Like.class, this.valueComparison(" like "));
      this.addCriterionHandler(QueryModel.ILike.class, this.caseInsensitiveValueComparison(" like "));
      this.addCriterionHandler(QueryModel.Between.class, (ctx, between) -> {
         AbstractSqlLikeQueryBuilder.QueryPropertyPath prop = ctx.getRequiredProperty(between);
         StringBuilder whereClause = ctx.query();
         whereClause.append('(');
         this.appendPropertyRef(whereClause, prop);
         whereClause.append(" >= ");
         this.appendPlaceholderOrLiteral(ctx, prop, between.getFrom());
         whereClause.append(" AND ");
         this.appendPropertyRef(whereClause, prop);
         whereClause.append(" <= ");
         this.appendPlaceholderOrLiteral(ctx, prop, between.getTo());
         whereClause.append(')');
      });
      this.addCriterionHandler(QueryModel.StartsWith.class, this.valueComparison(this::formatStartsWithBeginning, this::formatEndsWith));
      this.addCriterionHandler(QueryModel.Contains.class, this.valueComparison(this::formatStartsWith, this::formatEndsWith));
      this.addCriterionHandler(QueryModel.EndsWith.class, this.valueComparison(this::formatStartsWith, this::formEndsWithEnd));
      this.addCriterionHandler(QueryModel.In.class, (ctx, inQuery) -> {
         AbstractSqlLikeQueryBuilder.QueryPropertyPath propertyPath = ctx.getRequiredProperty(inQuery.getProperty(), QueryModel.In.class);
         StringBuilder whereClause = ctx.query();
         this.appendPropertyRef(whereClause, propertyPath);
         whereClause.append(" IN (");
         Object value = inQuery.getValue();
         if (value instanceof BindingParameter) {
            ctx.pushParameter((BindingParameter)value, this.newBindingContext(propertyPath.propertyPath).expandable());
         } else {
            this.asLiterals(ctx.query(), value);
         }

         whereClause.append(')');
      });
      this.addCriterionHandler(QueryModel.NotIn.class, (ctx, inQuery) -> {
         AbstractSqlLikeQueryBuilder.QueryPropertyPath propertyPath = ctx.getRequiredProperty(inQuery.getProperty(), QueryModel.In.class);
         StringBuilder whereClause = ctx.query();
         this.appendPropertyRef(whereClause, propertyPath);
         whereClause.append(" NOT IN (");
         Object value = inQuery.getValue();
         if (value instanceof BindingParameter) {
            ctx.pushParameter((BindingParameter)value, this.newBindingContext(propertyPath.propertyPath).expandable());
         } else {
            this.asLiterals(ctx.query(), value);
         }

         whereClause.append(')');
      });
   }

   protected Dialect getDialect() {
      return Dialect.ANSI;
   }

   private void asLiterals(StringBuilder sb, @Nullable Object value) {
      if (value instanceof Iterable) {
         Iterator iterator = ((Iterable)value).iterator();

         while(iterator.hasNext()) {
            Object o = iterator.next();
            sb.append(this.asLiteral(o));
            if (iterator.hasNext()) {
               sb.append(",");
            }
         }
      } else if (value instanceof Object[]) {
         Object[] objects = value;

         for(int i = 0; i < objects.length; ++i) {
            Object o = objects[i];
            sb.append(this.asLiteral(o));
            if (i + 1 != objects.length) {
               sb.append(",");
            }
         }
      } else {
         sb.append(this.asLiteral(value));
      }

   }

   @NonNull
   protected String asLiteral(@Nullable Object value) {
      if (value instanceof LiteralExpression) {
         value = ((LiteralExpression)value).getValue();
      }

      if (value == null) {
         return "NULL";
      } else if (value instanceof Number) {
         return Long.toString(((Number)value).longValue());
      } else {
         return value instanceof Boolean ? value.toString().toUpperCase(Locale.ROOT) : "'" + value + "'";
      }
   }

   private <T extends QueryModel.PropertyCriterion> AbstractSqlLikeQueryBuilder.CriterionHandler<T> valueComparison(
      Supplier<String> prefix, Supplier<String> suffix
   ) {
      return (ctx, propertyCriterion) -> {
         AbstractSqlLikeQueryBuilder.QueryPropertyPath propertyPath = ctx.getRequiredProperty(propertyCriterion);
         this.appendPropertyRef(ctx.query(), propertyPath);
         ctx.query().append((String)prefix.get());
         this.appendPlaceholderOrLiteral(ctx, propertyPath, propertyCriterion.getValue());
         ctx.query().append((String)suffix.get());
      };
   }

   private <T extends QueryModel.PropertyCriterion> AbstractSqlLikeQueryBuilder.CriterionHandler<T> valueComparison(String op) {
      return (ctx, propertyCriterion) -> {
         AbstractSqlLikeQueryBuilder.QueryPropertyPath prop = ctx.getRequiredProperty(propertyCriterion);
         this.appendCriteriaForOperator(ctx.query(), ctx, prop, propertyCriterion.getValue(), op);
      };
   }

   private <T extends QueryModel.PropertyCriterion> AbstractSqlLikeQueryBuilder.CriterionHandler<T> optionalCaseValueComparison(String op) {
      return (ctx, propertyCriterion) -> {
         if (propertyCriterion.isIgnoreCase()) {
            this.appendCaseInsensitiveCriterion(ctx, propertyCriterion, op);
         } else {
            this.valueComparison(op).handle(ctx, propertyCriterion);
         }

      };
   }

   private <T extends QueryModel.PropertyCriterion> AbstractSqlLikeQueryBuilder.CriterionHandler<T> caseInsensitiveValueComparison(String op) {
      return (ctx, propertyCriterion) -> this.appendCaseInsensitiveCriterion(ctx, propertyCriterion, op);
   }

   private <T extends QueryModel.PropertyComparisonCriterion> AbstractSqlLikeQueryBuilder.CriterionHandler<T> comparison(String operator) {
      return (ctx, comparisonCriterion) -> this.appendPropertyComparison(ctx, comparisonCriterion, operator);
   }

   private <T extends QueryModel.PropertyNameCriterion> AbstractSqlLikeQueryBuilder.CriterionHandler<T> expression(String expression) {
      return (ctx, expressionCriterion) -> {
         this.appendPropertyRef(ctx.query(), ctx.getRequiredProperty(expressionCriterion));
         ctx.query().append(expression);
      };
   }

   private AbstractSqlLikeQueryBuilder.QueryPropertyPath asQueryPropertyPath(String tableAlias, PersistentProperty persistentProperty) {
      return new AbstractSqlLikeQueryBuilder.QueryPropertyPath(this.asPersistentPropertyPath(persistentProperty), tableAlias);
   }

   private PersistentPropertyPath asPersistentPropertyPath(PersistentProperty persistentProperty) {
      return PersistentPropertyPath.of(Collections.emptyList(), persistentProperty, persistentProperty.getName());
   }

   protected String formEndsWithEnd() {
      return ")";
   }

   protected String formatStartsWithBeginning() {
      return " LIKE CONCAT(";
   }

   protected String formatEndsWith() {
      return ",'%')";
   }

   protected String formatStartsWith() {
      return " LIKE CONCAT('%',";
   }

   private void appendEmptyExpression(
      AbstractSqlLikeQueryBuilder.CriteriaContext ctx, String charSequencePrefix, String charSequenceSuffix, String listSuffix, String name
   ) {
      AbstractSqlLikeQueryBuilder.QueryPropertyPath propertyPath = ctx.getRequiredProperty(name, QueryModel.IsEmpty.class);
      StringBuilder whereClause = ctx.query();
      if (propertyPath.getProperty().isAssignable(CharSequence.class)) {
         this.appendPropertyRef(whereClause, propertyPath);
         whereClause.append(charSequencePrefix);
         this.appendPropertyRef(whereClause, propertyPath);
         whereClause.append(charSequenceSuffix);
      } else {
         this.appendPropertyRef(whereClause, propertyPath);
         whereClause.append(listSuffix);
      }

   }

   @Override
   public QueryResult buildQuery(@NonNull AnnotationMetadata annotationMetadata, @NonNull QueryModel query) {
      ArgumentUtils.requireNonNull("annotationMetadata", annotationMetadata);
      ArgumentUtils.requireNonNull("query", query);
      AbstractSqlLikeQueryBuilder.QueryState queryState = this.newQueryState(query, true, true);
      List<JoinPath> joinPaths = new ArrayList(query.getJoinPaths());
      joinPaths.sort((o1, o2) -> Comparator.comparingInt(String::length).thenComparing(String::compareTo).compare(o1.getPath(), o2.getPath()));

      for(JoinPath joinPath : joinPaths) {
         queryState.applyJoin(joinPath);
      }

      StringBuilder select = new StringBuilder("SELECT ");
      this.buildSelectClause(query, queryState, select);
      this.appendForUpdate(AbstractSqlLikeQueryBuilder.QueryPosition.AFTER_TABLE_NAME, query, select);
      queryState.getQuery().insert(0, select);
      QueryModel.Junction criteria = query.getCriteria();
      if (!criteria.isEmpty()
         || annotationMetadata.hasStereotype(WhereSpecifications.class)
         || queryState.getEntity().getAnnotationMetadata().hasStereotype(WhereSpecifications.class)) {
         this.buildWhereClause(annotationMetadata, criteria, queryState);
      }

      this.appendOrder(query, queryState);
      this.appendForUpdate(AbstractSqlLikeQueryBuilder.QueryPosition.END_OF_QUERY, query, queryState.getQuery());
      return QueryResult.of(
         queryState.getFinalQuery(),
         queryState.getQueryParts(),
         queryState.getParameterBindings(),
         queryState.getAdditionalRequiredParameters(),
         query.getMax(),
         query.getOffset()
      );
   }

   protected abstract String getTableName(PersistentEntity entity);

   protected String getUnescapedTableName(PersistentEntity entity) {
      return entity.getPersistedName();
   }

   protected String getAliasName(PersistentEntity entity) {
      return (String)entity.getAnnotationMetadata().stringValue(MappedEntity.class, "alias").orElseGet(() -> this.getTableName(entity) + "_");
   }

   public String getAliasName(JoinPath joinPath) {
      return (String)joinPath.getAlias().orElseGet(() -> {
         String joinPathAlias = this.getPathOnlyAliasName(joinPath);
         PersistentEntity owner = joinPath.getAssociationPath()[0].getOwner();
         String ownerAlias = this.getAliasName(owner);
         return ownerAlias.endsWith("_") && joinPathAlias.startsWith("_") ? ownerAlias + joinPathAlias.substring(1) : ownerAlias + joinPathAlias;
      });
   }

   @NonNull
   protected String getPathOnlyAliasName(JoinPath joinPath) {
      return (String)joinPath.getAlias().orElseGet(() -> {
         String p = joinPath.getPath().replace('.', '_');
         return NamingStrategy.DEFAULT.mappedName(p) + "_";
      });
   }

   protected abstract String[] buildJoin(
      String alias,
      JoinPath joinPath,
      String joinType,
      StringBuilder stringBuilder,
      Map<String, String> appliedJoinPaths,
      AbstractSqlLikeQueryBuilder.QueryState queryState
   );

   protected abstract String getColumnName(PersistentProperty persistentProperty);

   protected abstract void selectAllColumns(AbstractSqlLikeQueryBuilder.QueryState queryState, StringBuilder queryBuffer);

   protected abstract void selectAllColumns(PersistentEntity entity, String alias, StringBuilder queryBuffer);

   private AbstractSqlLikeQueryBuilder.QueryState newQueryState(@NonNull QueryModel query, boolean allowJoins, boolean useAlias) {
      return new AbstractSqlLikeQueryBuilder.QueryState(query, allowJoins, useAlias);
   }

   private void buildSelectClause(QueryModel query, AbstractSqlLikeQueryBuilder.QueryState queryState, StringBuilder queryString) {
      String logicalName = queryState.getRootAlias();
      PersistentEntity entity = queryState.getEntity();
      this.buildSelect(queryState, queryString, query.getProjections(), logicalName, entity);
      String tableName = this.getTableName(entity);
      queryString.append(" FROM ").append(tableName).append(this.getTableAsKeyword()).append(logicalName);
   }

   protected boolean shouldEscape(@NonNull PersistentEntity entity) {
      return entity.getAnnotationMetadata().booleanValue(MappedEntity.class, "escape").orElse(true);
   }

   protected String getTableAsKeyword() {
      return " AS ";
   }

   protected String quote(String persistedName) {
      return "\"" + persistedName + "\"";
   }

   private void buildSelect(
      AbstractSqlLikeQueryBuilder.QueryState queryState,
      StringBuilder queryString,
      List<QueryModel.Projection> projectionList,
      String tableAlias,
      PersistentEntity entity
   ) {
      if (projectionList.isEmpty()) {
         this.selectAllColumns(queryState, queryString);
      } else {
         Iterator i = projectionList.iterator();

         while(i.hasNext()) {
            QueryModel.Projection projection = (QueryModel.Projection)i.next();
            if (projection instanceof QueryModel.LiteralProjection) {
               queryString.append(this.asLiteral(((QueryModel.LiteralProjection)projection).getValue()));
            } else if (projection instanceof QueryModel.CountProjection) {
               this.appendProjectionRowCount(queryString, tableAlias);
            } else if (projection instanceof QueryModel.DistinctProjection) {
               queryString.append("DISTINCT(").append(tableAlias).append(')');
            } else if (projection instanceof QueryModel.IdProjection) {
               if (entity.hasCompositeIdentity()) {
                  for(PersistentProperty identity : entity.getCompositeIdentity()) {
                     this.appendPropertyProjection(queryString, this.asQueryPropertyPath(queryState.getRootAlias(), identity));
                     queryString.append(',');
                  }

                  queryString.setLength(queryString.length() - 1);
               } else {
                  if (!entity.hasIdentity()) {
                     throw new IllegalArgumentException("Cannot query on ID with entity that has no ID");
                  }

                  PersistentProperty identity = entity.getIdentity();
                  if (identity == null) {
                     throw new IllegalArgumentException("Cannot query on ID with entity that has no ID");
                  }

                  this.appendPropertyProjection(queryString, this.asQueryPropertyPath(queryState.getRootAlias(), identity));
               }
            } else if (projection instanceof QueryModel.PropertyProjection) {
               QueryModel.PropertyProjection pp = (QueryModel.PropertyProjection)projection;
               String alias = (String)pp.getAlias().orElse(null);
               if (projection instanceof QueryModel.AvgProjection) {
                  this.appendFunctionProjection(queryState.getEntity(), "AVG", pp, tableAlias, queryString);
               } else if (projection instanceof QueryModel.DistinctPropertyProjection) {
                  this.appendFunctionProjection(queryState.getEntity(), "DISTINCT", pp, tableAlias, queryString);
               } else if (projection instanceof QueryModel.SumProjection) {
                  this.appendFunctionProjection(queryState.getEntity(), "SUM", pp, tableAlias, queryString);
               } else if (projection instanceof QueryModel.MinProjection) {
                  this.appendFunctionProjection(queryState.getEntity(), "MIN", pp, tableAlias, queryString);
               } else if (projection instanceof QueryModel.MaxProjection) {
                  this.appendFunctionProjection(queryState.getEntity(), "MAX", pp, tableAlias, queryString);
               } else if (projection instanceof QueryModel.CountDistinctProjection) {
                  this.appendFunctionProjection(queryState.getEntity(), "COUNT(DISTINCT", pp, tableAlias, queryString);
                  queryString.append(')');
               } else {
                  String propertyName = pp.getPropertyName();
                  PersistentPropertyPath propertyPath = entity.getPropertyPath(propertyName);
                  if (propertyPath == null) {
                     throw new IllegalArgumentException("Cannot project on non-existent property: " + propertyName);
                  }

                  PersistentProperty property = propertyPath.getProperty();
                  if (property instanceof Association && !(property instanceof Embedded)) {
                     if (!queryState.isJoined(propertyPath.getPath())) {
                        queryString.setLength(queryString.length() - 1);
                        continue;
                     }

                     String joinAlias = queryState.computeAlias(propertyPath.getPath());
                     this.selectAllColumns(((Association)property).getAssociatedEntity(), joinAlias, queryString);
                  } else {
                     this.appendPropertyProjection(queryString, this.findProperty(queryState, propertyName, null));
                  }
               }

               if (alias != null) {
                  queryString.append(" AS ").append(alias);
               }
            }

            if (i.hasNext()) {
               queryString.append(',');
            }
         }
      }

   }

   private void appendPropertyProjection(StringBuilder sb, AbstractSqlLikeQueryBuilder.QueryPropertyPath propertyPath) {
      if (!this.computePropertyPaths()) {
         sb.append(propertyPath.getTableAlias()).append('.').append(propertyPath.getPath());
      } else {
         String tableAlias = propertyPath.getTableAlias();
         boolean escape = propertyPath.shouldEscape();
         NamingStrategy namingStrategy = propertyPath.getNamingStrategy();
         int length = sb.length();
         this.traversePersistentProperties(propertyPath.getAssociations(), propertyPath.getProperty(), (associations, property) -> {
            String columnName = namingStrategy.mappedName(associations, property);
            if (escape) {
               columnName = this.quote(columnName);
            }

            sb.append(tableAlias).append('.').append(columnName).append(',');
         });
         int newLength = sb.length();
         if (length != newLength) {
            sb.setLength(newLength - 1);
         }

      }
   }

   private void appendFunctionProjection(
      PersistentEntity entity, String functionName, QueryModel.PropertyProjection propertyProjection, String tableAlias, StringBuilder queryString
   ) {
      PersistentPropertyPath propertyPath = entity.getPropertyPath(propertyProjection.getPropertyName());
      if (propertyPath == null) {
         throw new IllegalArgumentException("Cannot project on non-existent property: " + propertyProjection.getPropertyName());
      } else {
         String columnName;
         if (this.computePropertyPaths()) {
            columnName = entity.getNamingStrategy().mappedName(propertyPath.getAssociations(), propertyPath.getProperty());
            if (this.shouldEscape(entity)) {
               columnName = this.quote(columnName);
            }
         } else {
            columnName = propertyPath.getPath();
         }

         queryString.append(functionName).append('(').append(tableAlias).append('.').append(columnName).append(')');
      }
   }

   protected abstract void appendProjectionRowCount(StringBuilder queryString, String logicalName);

   private void handleAssociationCriteria(AbstractSqlLikeQueryBuilder.CriteriaContext ctx, AssociationQuery associationQuery) {
      final AbstractSqlLikeQueryBuilder.QueryState queryState = ctx.getQueryState();
      Association association = associationQuery.getAssociation();
      if (association != null) {
         final String associationPath = associationQuery.getPath();
         AbstractSqlLikeQueryBuilder.CriteriaContext associatedContext = new AbstractSqlLikeQueryBuilder.CriteriaContext() {
            @Override
            public String getCurrentTableAlias() {
               return ctx.getCurrentTableAlias();
            }

            @Override
            public AbstractSqlLikeQueryBuilder.QueryState getQueryState() {
               return ctx.getQueryState();
            }

            @Override
            public PersistentEntity getPersistentEntity() {
               return ctx.getPersistentEntity();
            }

            @Override
            public AbstractSqlLikeQueryBuilder.QueryPropertyPath getRequiredProperty(String name, Class<?> criterionClazz) {
               if (StringUtils.isNotEmpty(associationPath)) {
                  name = associationPath + '.' + name;
               }

               return AbstractSqlLikeQueryBuilder.this.findPropertyInternal(
                  queryState, this.getPersistentEntity(), this.getCurrentTableAlias(), name, criterionClazz
               );
            }
         };
         this.handleJunction(associatedContext, associationQuery.getCriteria());
      }
   }

   private void buildWhereClause(AnnotationMetadata annotationMetadata, QueryModel.Junction criteria, AbstractSqlLikeQueryBuilder.QueryState queryState) {
      StringBuilder queryClause = queryState.getQuery();
      if (!criteria.isEmpty()) {
         queryClause.append(" WHERE ");
         if (criteria instanceof QueryModel.Negation) {
            queryClause.append(" NOT");
         }

         AbstractSqlLikeQueryBuilder.CriteriaContext ctx = new AbstractSqlLikeQueryBuilder.CriteriaContext() {
            @Override
            public String getCurrentTableAlias() {
               return queryState.getRootAlias();
            }

            @Override
            public AbstractSqlLikeQueryBuilder.QueryState getQueryState() {
               return queryState;
            }

            @Override
            public PersistentEntity getPersistentEntity() {
               return queryState.getEntity();
            }

            @Override
            public AbstractSqlLikeQueryBuilder.QueryPropertyPath getRequiredProperty(String name, Class<?> criterionClazz) {
               return AbstractSqlLikeQueryBuilder.this.findProperty(queryState, name, criterionClazz);
            }
         };
         queryClause.append('(');
         this.handleJunction(ctx, criteria);
         String queryStr = queryClause.toString();
         String additionalWhere = this.buildAdditionalWhereString(queryState.getRootAlias(), queryState.getEntity(), annotationMetadata);
         if (StringUtils.isNotEmpty(additionalWhere)) {
            StringBuffer additionalWhereBuilder = new StringBuffer();
            Matcher matcher = QueryBuilder.VARIABLE_PATTERN.matcher(additionalWhere);

            while(matcher.find()) {
               String name = matcher.group(3);
               String placeholder = queryState.addAdditionalRequiredParameter(name);
               matcher.appendReplacement(additionalWhereBuilder, placeholder);
            }

            matcher.appendTail(additionalWhereBuilder);
            additionalWhere = additionalWhereBuilder.toString();
         }

         if (queryStr.endsWith(" WHERE (")) {
            if (StringUtils.isNotEmpty(additionalWhere)) {
               queryClause.append(additionalWhere).append(')');
            }
         } else {
            if (StringUtils.isNotEmpty(additionalWhere)) {
               queryClause.append(" AND ").append('(').append(additionalWhere).append(')');
            }

            queryClause.append(')');
         }
      } else {
         String additionalWhereString = this.buildAdditionalWhereString(queryState.getRootAlias(), queryState.getEntity(), annotationMetadata);
         if (StringUtils.isNotEmpty(additionalWhereString)) {
            queryClause.append(" WHERE ").append('(').append(additionalWhereString).append(')');
         }
      }

   }

   private String buildAdditionalWhereString(String alias, PersistentEntity entity, AnnotationMetadata annotationMetadata) {
      String whereStr = this.resolveWhereForAnnotationMetadata(alias, annotationMetadata);
      return StringUtils.isNotEmpty(whereStr) ? whereStr : this.resolveWhereForAnnotationMetadata(alias, entity.getAnnotationMetadata());
   }

   private String resolveWhereForAnnotationMetadata(String alias, AnnotationMetadata annotationMetadata) {
      return (String)annotationMetadata.getAnnotationValuesByType(Where.class)
         .stream()
         .map(av -> (String)av.stringValue().orElse(null))
         .map(val -> this.replaceAlias(alias, val))
         .filter(StringUtils::isNotEmpty)
         .collect(Collectors.joining(" AND "));
   }

   private void appendOrder(QueryModel query, AbstractSqlLikeQueryBuilder.QueryState queryState) {
      List<Sort.Order> orders = query.getSort().getOrderBy();
      if (!orders.isEmpty()) {
         StringBuilder buff = queryState.getQuery();
         buff.append(" ORDER BY ");
         Iterator<Sort.Order> i = orders.iterator();

         while(i.hasNext()) {
            Sort.Order order = (Sort.Order)i.next();
            AbstractSqlLikeQueryBuilder.QueryPropertyPath propertyPath = this.findProperty(queryState, order.getProperty(), Sort.Order.class);
            String currentAlias = propertyPath.getTableAlias();
            if (currentAlias != null) {
               buff.append(currentAlias).append('.');
            }

            if (this.computePropertyPaths()) {
               buff.append(propertyPath.getColumnName()).append(' ').append(order.getDirection().toString());
            } else {
               buff.append(propertyPath.getPath()).append(' ').append(order.getDirection().toString());
            }

            if (i.hasNext()) {
               buff.append(",");
            }
         }
      }

   }

   protected void appendForUpdate(AbstractSqlLikeQueryBuilder.QueryPosition queryPosition, QueryModel query, StringBuilder queryBuilder) {
      if (query.isForUpdate() && !this.supportsForUpdate()) {
         throw new IllegalStateException("For update not supported for current query builder: " + this.getClass().getSimpleName());
      }
   }

   private void handleJunction(AbstractSqlLikeQueryBuilder.CriteriaContext ctx, QueryModel.Junction criteria) {
      StringBuilder whereClause = ctx.query();
      int length = whereClause.length();
      String operator = criteria instanceof QueryModel.Conjunction ? " AND " : " OR ";

      for(QueryModel.Criterion criterion : criteria.getCriteria()) {
         AbstractSqlLikeQueryBuilder.CriterionHandler<QueryModel.Criterion> criterionHandler = (AbstractSqlLikeQueryBuilder.CriterionHandler)this.queryHandlers
            .get(criterion.getClass());
         if (criterionHandler == null) {
            throw new IllegalArgumentException("Queries of type " + criterion.getClass().getSimpleName() + " are not supported by this implementation");
         }

         int beforeHandleLength = whereClause.length();
         criterionHandler.handle(ctx, criterion);
         if (beforeHandleLength != whereClause.length()) {
            whereClause.append(operator);
         }
      }

      int newLength = whereClause.length();
      if (newLength != length) {
         whereClause.setLength(newLength - operator.length());
      }

   }

   private void appendCriteriaForOperator(
      StringBuilder whereClause,
      AbstractSqlLikeQueryBuilder.PropertyParameterCreator propertyParameterCreator,
      AbstractSqlLikeQueryBuilder.QueryPropertyPath propertyPath,
      Object value,
      String operator
   ) {
      this.appendCriteriaForOperator(whereClause, propertyParameterCreator, propertyPath.propertyPath, propertyPath, value, operator);
   }

   private void appendCriteriaForOperator(
      StringBuilder whereClause,
      AbstractSqlLikeQueryBuilder.PropertyParameterCreator propertyParameterCreator,
      PersistentPropertyPath parameterPropertyPath,
      AbstractSqlLikeQueryBuilder.QueryPropertyPath propertyPath,
      Object value,
      String operator
   ) {
      if (value instanceof BindingParameter) {
         BindingParameter bindingParameter = (BindingParameter)value;
         boolean computePropertyPaths = this.computePropertyPaths();
         if (!computePropertyPaths) {
            this.appendPropertyRef(whereClause, propertyPath);
            whereClause.append(operator);
            propertyParameterCreator.pushParameter(bindingParameter, this.newBindingContext(parameterPropertyPath, propertyPath.propertyPath));
            return;
         }

         String currentAlias = propertyPath.getTableAlias();
         NamingStrategy namingStrategy = propertyPath.getNamingStrategy();
         boolean shouldEscape = propertyPath.shouldEscape();
         int length = whereClause.length();
         this.traversePersistentProperties(
            propertyPath.getAssociations(),
            propertyPath.getProperty(),
            (associations, property) -> {
               String readTransformer = (String)this.getDataTransformerReadValue(currentAlias, property).orElse(null);
               if (readTransformer != null) {
                  whereClause.append(readTransformer);
               } else {
                  if (currentAlias != null) {
                     whereClause.append(currentAlias).append('.');
                  }
   
                  String columnName = namingStrategy.mappedName(associations, property);
                  if (shouldEscape) {
                     columnName = this.quote(columnName);
                  }
   
                  whereClause.append(columnName);
               }
   
               whereClause.append(operator);
               propertyParameterCreator.pushParameter(
                  bindingParameter, this.newBindingContext(parameterPropertyPath, PersistentPropertyPath.of(associations, property))
               );
               whereClause.append(" AND ");
            }
         );
         int newLength = whereClause.length();
         if (newLength != length) {
            whereClause.setLength(newLength - " AND ".length());
         }
      } else {
         this.appendPropertyRef(whereClause, propertyPath);
         whereClause.append(operator).append(this.asLiteral(value));
      }

   }

   private void appendPropertyRef(StringBuilder sb, AbstractSqlLikeQueryBuilder.QueryPropertyPath propertyPath) {
      String tableAlias = propertyPath.getTableAlias();
      String readTransformer = (String)this.getDataTransformerReadValue(tableAlias, propertyPath.getProperty()).orElse(null);
      if (readTransformer != null) {
         sb.append(readTransformer);
      } else {
         if (tableAlias != null) {
            sb.append(tableAlias).append('.');
         }

         boolean computePropertyPaths = this.computePropertyPaths();
         if (computePropertyPaths) {
            sb.append(propertyPath.getColumnName());
         } else {
            sb.append(propertyPath.getPath());
         }

      }
   }

   private void appendCaseInsensitiveCriterion(AbstractSqlLikeQueryBuilder.CriteriaContext ctx, QueryModel.PropertyCriterion criterion, String operator) {
      AbstractSqlLikeQueryBuilder.QueryPropertyPath propertyPath = ctx.getRequiredProperty(criterion);
      StringBuilder whereClause = ctx.query();
      whereClause.append("lower(");
      this.appendPropertyRef(whereClause, propertyPath);
      whereClause.append(")").append(operator).append("lower(");
      this.appendPlaceholderOrLiteral(ctx, propertyPath, criterion.getValue());
      whereClause.append(")");
   }

   private void appendPlaceholderOrLiteral(
      AbstractSqlLikeQueryBuilder.CriteriaContext ctx, AbstractSqlLikeQueryBuilder.QueryPropertyPath propertyPath, Object value
   ) {
      if (value instanceof BindingParameter) {
         ctx.pushParameter((BindingParameter)value, this.newBindingContext(propertyPath.propertyPath));
      } else {
         ctx.query().append(this.asLiteral(value));
      }
   }

   protected void handleSubQuery(AbstractSqlLikeQueryBuilder.CriteriaContext ctx, QueryModel.SubqueryCriterion subqueryCriterion, String comparisonExpression) {
      AbstractSqlLikeQueryBuilder.QueryPropertyPath propertyPath = ctx.getRequiredProperty(subqueryCriterion.getProperty(), QueryModel.In.class);
      StringBuilder whereClause = ctx.query();
      this.appendPropertyRef(whereClause, propertyPath);
      whereClause.append(comparisonExpression);
      whereClause.append(')');
   }

   private void buildUpdateStatement(AbstractSqlLikeQueryBuilder.QueryState queryState, Map<String, Object> propertiesToUpdate) {
      StringBuilder queryString = queryState.getQuery();
      queryString.append(' ').append("SET").append(' ');
      List<Entry<AbstractSqlLikeQueryBuilder.QueryPropertyPath, Object>> update = (List)propertiesToUpdate.entrySet()
         .stream()
         .map(e -> {
            AbstractSqlLikeQueryBuilder.QueryPropertyPath propertyPathx = this.findProperty(queryState, (String)e.getKey(), null);
            if (propertyPathx.getProperty() instanceof Association && ((Association)propertyPathx.getProperty()).isForeignKey()) {
               throw new IllegalArgumentException("Foreign key associations cannot be updated as part of a batch update statement");
            } else {
               return new SimpleEntry(propertyPathx, e.getValue());
            }
         })
         .filter(e -> !(e.getValue() instanceof QueryParameter) || !((AbstractSqlLikeQueryBuilder.QueryPropertyPath)e.getKey()).getProperty().isGenerated())
         .collect(Collectors.toList());
      int length = queryString.length();
      if (!this.computePropertyPaths()) {
         for(Entry<AbstractSqlLikeQueryBuilder.QueryPropertyPath, Object> entry : update) {
            AbstractSqlLikeQueryBuilder.QueryPropertyPath propertyPath = (AbstractSqlLikeQueryBuilder.QueryPropertyPath)entry.getKey();
            PersistentProperty prop = propertyPath.getProperty();
            String tableAlias = propertyPath.getTableAlias();
            if (tableAlias != null) {
               queryString.append(tableAlias).append('.');
            }

            queryString.append(propertyPath.getPath()).append('=');
            if (entry.getValue() instanceof BindingParameter) {
               this.appendUpdateSetParameter(
                  queryString,
                  tableAlias,
                  prop,
                  () -> queryState.pushParameter((BindingParameter)entry.getValue(), this.newBindingContext(propertyPath.propertyPath))
               );
            } else {
               queryString.append(this.asLiteral(entry.getValue()));
            }

            queryString.append(',');
         }
      } else {
         NamingStrategy namingStrategy = queryState.getEntity().getNamingStrategy();

         for(Entry<AbstractSqlLikeQueryBuilder.QueryPropertyPath, Object> entry : update) {
            AbstractSqlLikeQueryBuilder.QueryPropertyPath propertyPath = (AbstractSqlLikeQueryBuilder.QueryPropertyPath)entry.getKey();
            if (entry.getValue() instanceof BindingParameter) {
               this.traversePersistentProperties(
                  propertyPath.getAssociations(),
                  propertyPath.getProperty(),
                  (associations, property) -> {
                     String tableAliasx = propertyPath.getTableAlias();
                     if (tableAliasx != null) {
                        queryString.append(tableAliasx).append('.');
                     }
   
                     String columnName = namingStrategy.mappedName(associations, property);
                     if (queryState.escape) {
                        columnName = this.quote(columnName);
                     }
   
                     queryString.append(columnName).append('=');
                     this.appendUpdateSetParameter(
                        queryString,
                        tableAliasx,
                        property,
                        () -> queryState.pushParameter(
                              (BindingParameter)entry.getValue(),
                              this.newBindingContext(
                                 propertyPath.propertyPath, PersistentPropertyPath.of(associations, property, this.asPath(associations, property))
                              )
                           )
                     );
                     queryString.append(',');
                  }
               );
            } else {
               String tableAlias = propertyPath.getTableAlias();
               if (tableAlias != null) {
                  queryString.append(tableAlias).append('.');
               }

               queryString.append(propertyPath.getPath()).append('=');
               queryString.append(this.asLiteral(entry.getValue()));
               queryString.append(',');
            }
         }
      }

      int newLength = queryString.length();
      if (length != newLength) {
         queryString.setLength(newLength - 1);
      }

   }

   protected boolean isExpandEmbedded() {
      return false;
   }

   protected void appendUpdateSetParameter(StringBuilder sb, String alias, PersistentProperty prop, Runnable appendParameter) {
      Optional<String> dataTransformerWriteValue = this.getDataTransformerWriteValue(alias, prop);
      if (dataTransformerWriteValue.isPresent()) {
         this.appendTransformed(sb, (String)dataTransformerWriteValue.get(), appendParameter);
      } else {
         appendParameter.run();
      }

   }

   protected void appendTransformed(StringBuilder sb, String transformed, Runnable appendParameter) {
      int parameterPosition = transformed.indexOf("?");
      if (parameterPosition > -1) {
         if (transformed.lastIndexOf("?") != parameterPosition) {
            throw new IllegalStateException("Only one parameter placeholder is allowed!");
         }

         sb.append(transformed, 0, parameterPosition);
         appendParameter.run();
         sb.append(transformed.substring(parameterPosition + 1));
      } else {
         sb.append(transformed);
      }

   }

   private void appendPropertyComparison(
      AbstractSqlLikeQueryBuilder.CriteriaContext ctx, QueryModel.PropertyComparisonCriterion comparisonCriterion, String operator
   ) {
      StringBuilder sb = ctx.query();
      this.appendPropertyRef(sb, ctx.getRequiredProperty(comparisonCriterion.getProperty(), comparisonCriterion.getClass()));
      sb.append(operator);
      this.appendPropertyRef(sb, ctx.getRequiredProperty(comparisonCriterion.getOtherProperty(), comparisonCriterion.getClass()));
   }

   @NonNull
   private AbstractSqlLikeQueryBuilder.QueryPropertyPath findProperty(AbstractSqlLikeQueryBuilder.QueryState queryState, String name, Class criterionType) {
      return this.findPropertyInternal(queryState, queryState.getEntity(), queryState.getRootAlias(), name, criterionType);
   }

   private AbstractSqlLikeQueryBuilder.QueryPropertyPath findPropertyInternal(
      AbstractSqlLikeQueryBuilder.QueryState queryState, PersistentEntity entity, String tableAlias, String name, Class criterionType
   ) {
      PersistentPropertyPath propertyPath = entity.getPropertyPath(name);
      if (propertyPath != null) {
         if (propertyPath.getAssociations().isEmpty()) {
            return new AbstractSqlLikeQueryBuilder.QueryPropertyPath(propertyPath, tableAlias);
         }

         Association joinAssociation = null;
         StringJoiner joinPathJoiner = new StringJoiner(".");
         String lastJoinAlias = null;

         for(Association association : propertyPath.getAssociations()) {
            joinPathJoiner.add(association.getName());
            if (!(association instanceof Embedded)) {
               if (joinAssociation == null) {
                  joinAssociation = association;
               } else if (association != joinAssociation.getAssociatedEntity().getIdentity()) {
                  if (!queryState.isAllowJoins()) {
                     throw new IllegalArgumentException("Joins cannot be used in a DELETE or UPDATE operation");
                  }

                  String joinStringPath = joinPathJoiner.toString();
                  if (!queryState.isJoined(joinStringPath)) {
                     throw new IllegalArgumentException("Property is not joined at path: " + joinStringPath);
                  }

                  lastJoinAlias = this.joinInPath(queryState, joinStringPath);
                  joinAssociation = association;
               } else {
                  joinAssociation = null;
               }
            }
         }

         PersistentProperty property = propertyPath.getProperty();
         if (joinAssociation != null) {
            if (property != joinAssociation.getAssociatedEntity().getIdentity() || joinAssociation.isForeignKey()) {
               String joinStringPath = joinPathJoiner.toString();
               if (!queryState.isJoined(joinStringPath)) {
                  throw new IllegalArgumentException("Property is not joined at path: " + joinStringPath);
               }

               if (lastJoinAlias == null) {
                  lastJoinAlias = this.joinInPath(queryState, joinPathJoiner.toString());
               }
            }

            if (lastJoinAlias != null) {
               return new AbstractSqlLikeQueryBuilder.QueryPropertyPath(
                  new PersistentPropertyPath(Collections.emptyList(), property, property.getName()), lastJoinAlias
               );
            }
         }
      } else if ("id".equals(name) && entity.getIdentity() != null) {
         return new AbstractSqlLikeQueryBuilder.QueryPropertyPath(
            new PersistentPropertyPath(Collections.emptyList(), entity.getIdentity(), entity.getIdentity().getName()), queryState.getRootAlias()
         );
      }

      if (propertyPath == null) {
         if (criterionType != null && criterionType != Sort.Order.class) {
            throw new IllegalArgumentException("Cannot use [" + criterionType.getSimpleName() + "] criterion on non-existent property path: " + name);
         } else {
            throw new IllegalArgumentException("Cannot order on non-existent property path: " + name);
         }
      } else {
         return new AbstractSqlLikeQueryBuilder.QueryPropertyPath(propertyPath, tableAlias);
      }
   }

   private String joinInPath(AbstractSqlLikeQueryBuilder.QueryState queryState, String joinStringPath) {
      QueryModel queryModel = queryState.getQueryModel();
      JoinPath joinPath = (JoinPath)queryModel.getJoinPath(joinStringPath).orElse(null);
      if (joinPath == null) {
         joinPath = queryModel.join(joinStringPath, Join.Type.DEFAULT, null);
      }

      if (queryState.isAllowJoins()) {
         return queryState.applyJoin(joinPath);
      } else {
         throw new IllegalArgumentException("Joins are not allowed for batch update queries");
      }
   }

   protected abstract boolean computePropertyPaths();

   @Override
   public QueryResult buildUpdate(@NonNull AnnotationMetadata annotationMetadata, @NonNull QueryModel query, @NonNull List<String> propertiesToUpdate) {
      return this.buildUpdate(
         annotationMetadata,
         query,
         (Map<String, Object>)propertiesToUpdate.stream().collect(Collectors.toMap(prop -> prop, QueryParameter::new, (a, b) -> a, () -> new LinkedHashMap()))
      );
   }

   @Override
   public QueryResult buildUpdate(@NonNull AnnotationMetadata annotationMetadata, @NonNull QueryModel query, @NonNull Map<String, Object> propertiesToUpdate) {
      if (propertiesToUpdate.isEmpty()) {
         throw new IllegalArgumentException("No properties specified to update");
      } else {
         PersistentEntity entity = query.getPersistentEntity();
         AbstractSqlLikeQueryBuilder.QueryState queryState = this.newQueryState(query, false, this.isAliasForBatch());
         StringBuilder queryString = queryState.getQuery();
         String tableAlias = queryState.getRootAlias();
         String tableName = this.getTableName(entity);
         queryString.append("UPDATE ").append(tableName);
         if (tableAlias != null) {
            queryString.append(' ').append(tableAlias);
         }

         this.buildUpdateStatement(queryState, propertiesToUpdate);
         this.buildWhereClause(annotationMetadata, query.getCriteria(), queryState);
         return QueryResult.of(
            queryState.getFinalQuery(), queryState.getQueryParts(), queryState.getParameterBindings(), queryState.getAdditionalRequiredParameters()
         );
      }
   }

   @Override
   public QueryResult buildDelete(@NonNull AnnotationMetadata annotationMetadata, @NonNull QueryModel query) {
      PersistentEntity entity = query.getPersistentEntity();
      AbstractSqlLikeQueryBuilder.QueryState queryState = this.newQueryState(query, false, this.isAliasForBatch());
      StringBuilder queryString = queryState.getQuery();
      String tableAlias = queryState.getRootAlias();
      StringBuilder buffer = this.appendDeleteClause(queryString);
      String tableName = this.getTableName(entity);
      buffer.append(tableName).append(' ');
      if (tableAlias != null) {
         buffer.append(this.getTableAsKeyword()).append(tableAlias);
      }

      this.buildWhereClause(annotationMetadata, query.getCriteria(), queryState);
      return QueryResult.of(
         queryState.getFinalQuery(), queryState.getQueryParts(), queryState.getParameterBindings(), queryState.getAdditionalRequiredParameters()
      );
   }

   protected abstract boolean isAliasForBatch();

   @NonNull
   protected StringBuilder appendDeleteClause(StringBuilder queryString) {
      return queryString.append("DELETE ").append(" FROM ");
   }

   @NonNull
   @Override
   public QueryResult buildOrderBy(@NonNull PersistentEntity entity, @NonNull Sort sort) {
      return this.buildOrderBy("", entity, sort);
   }

   @NonNull
   public QueryResult buildOrderBy(String query, @NonNull PersistentEntity entity, @NonNull Sort sort) {
      ArgumentUtils.requireNonNull("entity", entity);
      ArgumentUtils.requireNonNull("sort", sort);
      List<Sort.Order> orders = sort.getOrderBy();
      if (CollectionUtils.isEmpty(orders)) {
         throw new IllegalArgumentException("Sort is empty");
      } else {
         StringBuilder buff = new StringBuilder(" ORDER BY ");
         Iterator<Sort.Order> i = orders.iterator();

         while(i.hasNext()) {
            Sort.Order order = (Sort.Order)i.next();
            String property = order.getProperty();
            PersistentPropertyPath path = entity.getPropertyPath(property);
            if (path == null) {
               throw new IllegalArgumentException("Cannot sort on non-existent property path: " + property);
            }

            boolean ignoreCase = order.isIgnoreCase();
            if (ignoreCase) {
               buff.append("LOWER(");
            }

            if (path.getAssociations().isEmpty()) {
               buff.append(this.getAliasName(entity));
            } else {
               StringJoiner joiner = new StringJoiner(".");

               for(Association association : path.getAssociations()) {
                  joiner.add(association.getName());
               }

               String joinAlias = this.getAliasName(
                  new JoinPath(joiner.toString(), (Association[])path.getAssociations().toArray(new Association[0]), Join.Type.DEFAULT, null)
               );
               if (this.computePropertyPaths()) {
                  buff.append(joinAlias);
               } else if (!query.contains(" " + joinAlias + " ") && !query.endsWith(" " + joinAlias)) {
                  buff.append(this.getAliasName(entity)).append('.');
                  StringJoiner pathJoiner = new StringJoiner(".");

                  for(Association association : path.getAssociations()) {
                     pathJoiner.add(association.getName());
                  }

                  buff.append(pathJoiner);
               } else {
                  buff.append(joinAlias);
               }
            }

            buff.append('.');
            if (!this.computePropertyPaths()) {
               buff.append(path.getProperty().getName());
            } else {
               buff.append(this.getColumnName(path.getProperty()));
            }

            if (ignoreCase) {
               buff.append(")");
            }

            buff.append(' ').append(order.getDirection());
            if (i.hasNext()) {
               buff.append(",");
            }
         }

         return QueryResult.of(buff.toString(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
      }
   }

   protected String asPath(List<Association> associations, PersistentProperty property) {
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

   protected void traversePersistentProperties(PersistentProperty property, BiConsumer<List<Association>, PersistentProperty> consumer) {
      this.traversePersistentProperties(Collections.emptyList(), property, consumer);
   }

   protected void traversePersistentProperties(PersistentEntity persistentEntity, BiConsumer<List<Association>, PersistentProperty> consumer) {
      if (persistentEntity.getIdentity() != null) {
         this.traversePersistentProperties(Collections.emptyList(), persistentEntity.getIdentity(), consumer);
      }

      if (persistentEntity.getVersion() != null) {
         this.traversePersistentProperties(Collections.emptyList(), persistentEntity.getVersion(), consumer);
      }

      for(PersistentProperty property : persistentEntity.getPersistentProperties()) {
         this.traversePersistentProperties(Collections.emptyList(), property, consumer);
      }

   }

   protected void traversePersistentProperties(
      PersistentEntity persistentEntity, boolean includeIdentity, boolean includeVersion, BiConsumer<List<Association>, PersistentProperty> consumer
   ) {
      if (includeIdentity && persistentEntity.getIdentity() != null) {
         this.traversePersistentProperties(Collections.emptyList(), persistentEntity.getIdentity(), consumer);
      }

      if (includeVersion && persistentEntity.getVersion() != null) {
         this.traversePersistentProperties(Collections.emptyList(), persistentEntity.getVersion(), consumer);
      }

      for(PersistentProperty property : persistentEntity.getPersistentProperties()) {
         this.traversePersistentProperties(Collections.emptyList(), property, consumer);
      }

   }

   private void traversePersistentProperties(
      List<Association> associations, PersistentProperty property, BiConsumer<List<Association>, PersistentProperty> consumerProperty
   ) {
      if (property instanceof Embedded) {
         Embedded embedded = (Embedded)property;
         PersistentEntity embeddedEntity = embedded.getAssociatedEntity();
         Collection<? extends PersistentProperty> embeddedProperties = embeddedEntity.getPersistentProperties();
         List<Association> newAssociations = new ArrayList(associations);
         newAssociations.add((Association)property);

         for(PersistentProperty embeddedProperty : embeddedProperties) {
            this.traversePersistentProperties(newAssociations, embeddedProperty, consumerProperty);
         }
      } else if (property instanceof Association) {
         Association association = (Association)property;
         if (association.isForeignKey()) {
            return;
         }

         List<Association> newAssociations = new ArrayList(associations);
         newAssociations.add((Association)property);
         PersistentEntity associatedEntity = association.getAssociatedEntity();
         PersistentProperty assocIdentity = associatedEntity.getIdentity();
         if (assocIdentity == null) {
            throw new IllegalStateException("Identity cannot be missing for: " + associatedEntity);
         }

         if (assocIdentity instanceof Association) {
            this.traversePersistentProperties(newAssociations, assocIdentity, consumerProperty);
         } else {
            consumerProperty.accept(newAssociations, assocIdentity);
         }
      } else {
         consumerProperty.accept(associations, property);
      }

   }

   private Optional<String> getDataTransformerValue(String alias, PersistentProperty prop, String val) {
      return prop.getAnnotationMetadata().stringValue(DataTransformer.class, val).map(v -> this.replaceAlias(alias, v));
   }

   private String replaceAlias(String alias, String v) {
      return v.replaceAll("@\\.", alias == null ? "" : alias + ".");
   }

   private BindingParameter.BindingContext newBindingContext(@Nullable PersistentPropertyPath ref, @Nullable PersistentPropertyPath persistentPropertyPath) {
      return BindingParameter.BindingContext.create().incomingMethodParameterProperty(ref).outgoingQueryParameterProperty(persistentPropertyPath);
   }

   private BindingParameter.BindingContext newBindingContext(@Nullable PersistentPropertyPath ref) {
      return BindingParameter.BindingContext.create().incomingMethodParameterProperty(ref).outgoingQueryParameterProperty(ref);
   }

   protected Optional<String> getDataTransformerReadValue(String alias, PersistentProperty prop) {
      return this.getDataTransformerValue(alias, prop, "read");
   }

   protected Optional<String> getDataTransformerWriteValue(String alias, PersistentProperty prop) {
      return this.getDataTransformerValue(alias, prop, "write");
   }

   protected abstract AbstractSqlLikeQueryBuilder.Placeholder formatParameter(int index);

   public abstract String resolveJoinType(Join.Type jt);

   protected <T extends QueryModel.Criterion> void addCriterionHandler(Class<T> clazz, AbstractSqlLikeQueryBuilder.CriterionHandler<T> handler) {
      this.queryHandlers.put(clazz, handler);
   }

   protected interface CriteriaContext extends AbstractSqlLikeQueryBuilder.PropertyParameterCreator {
      String getCurrentTableAlias();

      AbstractSqlLikeQueryBuilder.QueryState getQueryState();

      PersistentEntity getPersistentEntity();

      AbstractSqlLikeQueryBuilder.QueryPropertyPath getRequiredProperty(String name, Class<?> criterionClazz);

      @Override
      default void pushParameter(@NotNull BindingParameter bindingParameter, @NotNull BindingParameter.BindingContext bindingContext) {
         this.getQueryState().pushParameter(bindingParameter, bindingContext);
      }

      default AbstractSqlLikeQueryBuilder.QueryPropertyPath getRequiredProperty(QueryModel.PropertyNameCriterion propertyCriterion) {
         return this.getRequiredProperty(propertyCriterion.getProperty(), propertyCriterion.getClass());
      }

      default StringBuilder query() {
         return this.getQueryState().getQuery();
      }
   }

   protected interface CriterionHandler<T extends QueryModel.Criterion> {
      void handle(AbstractSqlLikeQueryBuilder.CriteriaContext context, T criterion);
   }

   public static final class Placeholder {
      private final String name;
      private final String key;

      public Placeholder(String name, String key) {
         this.name = name;
         this.key = key;
      }

      public String toString() {
         return this.name;
      }

      public String getName() {
         return this.name;
      }

      public String getKey() {
         return this.key;
      }
   }

   private interface PropertyParameterCreator {
      void pushParameter(@NotNull BindingParameter bindingParameter, @NotNull BindingParameter.BindingContext bindingContext);
   }

   protected static enum QueryPosition {
      AFTER_TABLE_NAME,
      END_OF_QUERY;
   }

   protected class QueryPropertyPath {
      private final PersistentPropertyPath propertyPath;
      private final String tableAlias;

      public QueryPropertyPath(@NotNull PersistentPropertyPath propertyPath, @Nullable String tableAlias) {
         this.propertyPath = propertyPath;
         this.tableAlias = tableAlias;
      }

      @NonNull
      public List<Association> getAssociations() {
         return this.propertyPath.getAssociations();
      }

      @NonNull
      public PersistentProperty getProperty() {
         return this.propertyPath.getProperty();
      }

      @NonNull
      public String getPath() {
         return this.propertyPath.getPath();
      }

      @Nullable
      public String getTableAlias() {
         return this.tableAlias;
      }

      public String getColumnName() {
         String columnName = this.getNamingStrategy().mappedName(this.propertyPath.getAssociations(), this.propertyPath.getProperty());
         return this.shouldEscape() ? AbstractSqlLikeQueryBuilder.this.quote(columnName) : columnName;
      }

      public NamingStrategy getNamingStrategy() {
         return this.propertyPath.getNamingStrategy();
      }

      public boolean shouldEscape() {
         return AbstractSqlLikeQueryBuilder.this.shouldEscape(
            (PersistentEntity)this.propertyPath.findPropertyOwner().orElse(this.propertyPath.getProperty().getOwner())
         );
      }
   }

   @Internal
   protected final class QueryState implements AbstractSqlLikeQueryBuilder.PropertyParameterCreator {
      private final String rootAlias;
      private final Map<String, String> appliedJoinPaths = new HashMap();
      private final AtomicInteger position = new AtomicInteger(0);
      private final Map<String, String> additionalRequiredParameters = new LinkedHashMap();
      private final List<QueryParameterBinding> parameterBindings;
      private final StringBuilder query = new StringBuilder();
      private final List<String> queryParts = new ArrayList();
      private final boolean allowJoins;
      private final QueryModel queryObject;
      private final boolean escape;
      private final PersistentEntity entity;

      private QueryState(QueryModel query, boolean allowJoins, boolean useAlias) {
         this.allowJoins = allowJoins;
         this.queryObject = query;
         this.entity = query.getPersistentEntity();
         this.escape = AbstractSqlLikeQueryBuilder.this.shouldEscape(this.entity);
         this.rootAlias = useAlias ? AbstractSqlLikeQueryBuilder.this.getAliasName(this.entity) : null;
         this.parameterBindings = new ArrayList(this.entity.getPersistentPropertyNames().size());
      }

      @Nullable
      public String getRootAlias() {
         return this.rootAlias;
      }

      public PersistentEntity getEntity() {
         return this.entity;
      }

      public String addAdditionalRequiredParameter(@NonNull String name) {
         AbstractSqlLikeQueryBuilder.Placeholder placeholder = this.newParameter();
         this.additionalRequiredParameters.put(placeholder.key, name);
         return placeholder.name;
      }

      public String getFinalQuery() {
         if (this.query.length() > 0) {
            this.queryParts.add(this.query.toString());
            this.query.setLength(0);
         }

         StringBuilder sb = new StringBuilder((String)this.queryParts.get(0));
         int i = 1;

         for(int k = 1; k < this.queryParts.size(); ++k) {
            AbstractSqlLikeQueryBuilder.Placeholder placeholder = AbstractSqlLikeQueryBuilder.this.formatParameter(i++);
            sb.append(placeholder.name);
            sb.append((String)this.queryParts.get(k));
         }

         return sb.toString();
      }

      public List<String> getQueryParts() {
         return this.queryParts;
      }

      public StringBuilder getQuery() {
         return this.query;
      }

      public boolean isAllowJoins() {
         return this.allowJoins;
      }

      public QueryModel getQueryModel() {
         return this.queryObject;
      }

      private AbstractSqlLikeQueryBuilder.Placeholder newParameter() {
         return AbstractSqlLikeQueryBuilder.this.formatParameter(this.position.incrementAndGet());
      }

      public String applyJoin(@NonNull JoinPath jp) {
         String joinAlias = (String)this.appliedJoinPaths.get(jp.getPath());
         if (joinAlias != null) {
            return joinAlias;
         } else {
            Optional<JoinPath> ojp = this.getQueryModel().getJoinPath(jp.getPath());
            if (ojp.isPresent()) {
               jp = (JoinPath)ojp.get();
            }

            StringBuilder stringBuilder = this.getQuery();
            Join.Type jt = jp.getJoinType();
            String joinType = AbstractSqlLikeQueryBuilder.this.resolveJoinType(jt);
            String[] associationAlias = AbstractSqlLikeQueryBuilder.this.buildJoin(
               this.getRootAlias(), jp, joinType, stringBuilder, this.appliedJoinPaths, this
            );
            Association[] associationArray = jp.getAssociationPath();
            StringJoiner associationPath = new StringJoiner(".");
            String lastAlias = null;

            for(int i = 0; i < associationAlias.length; ++i) {
               associationPath.add(associationArray[i].getName());
               String computedAlias = associationAlias[i];
               this.appliedJoinPaths.put(associationPath.toString(), computedAlias);
               lastAlias = computedAlias;
            }

            return lastAlias;
         }
      }

      @NonNull
      public String computeAlias(String associationPath) {
         if (this.appliedJoinPaths.containsKey(associationPath)) {
            return (String)this.appliedJoinPaths.get(associationPath);
         } else {
            int i = associationPath.indexOf(46);
            if (i > -1) {
               String p = associationPath.substring(0, i);
               if (this.appliedJoinPaths.containsKey(p)) {
                  return (String)this.appliedJoinPaths.get(p) + '.' + associationPath.substring(i + 1);
               }
            }

            return this.getRootAlias() + '.' + associationPath;
         }
      }

      public boolean isJoined(String associationPath) {
         for(String joinPath : this.appliedJoinPaths.keySet()) {
            if (joinPath.startsWith(associationPath)) {
               return true;
            }
         }

         return this.appliedJoinPaths.containsKey(associationPath);
      }

      public boolean shouldEscape() {
         return this.escape;
      }

      @NotNull
      public Map<String, String> getAdditionalRequiredParameters() {
         return this.additionalRequiredParameters;
      }

      public List<QueryParameterBinding> getParameterBindings() {
         return this.parameterBindings;
      }

      @Override
      public void pushParameter(@NotNull BindingParameter bindingParameter, @NotNull BindingParameter.BindingContext bindingContext) {
         AbstractSqlLikeQueryBuilder.Placeholder placeholder = this.newParameter();
         bindingContext = bindingContext.index(this.position.get() + 1).name(placeholder.getKey());
         this.parameterBindings.add(bindingParameter.bind(bindingContext));
         this.queryParts.add(this.query.toString());
         this.query.setLength(0);
      }
   }
}

package io.micronaut.data.model.query.builder.sql;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Index;
import io.micronaut.data.annotation.Indexes;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.exceptions.MappingException;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.Embedded;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.PersistentPropertyPath;
import io.micronaut.data.model.naming.NamingStrategy;
import io.micronaut.data.model.query.JoinPath;
import io.micronaut.data.model.query.QueryModel;
import io.micronaut.data.model.query.builder.AbstractSqlLikeQueryBuilder;
import io.micronaut.data.model.query.builder.QueryBuilder;
import io.micronaut.data.model.query.builder.QueryParameterBinding;
import io.micronaut.data.model.query.builder.QueryResult;
import java.lang.annotation.Annotation;
import java.sql.Blob;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SqlQueryBuilder extends AbstractSqlLikeQueryBuilder implements QueryBuilder, SqlQueryConfiguration.DialectConfiguration {
   public static final String DEFAULT_POSITIONAL_PARAMETER_MARKER = "?";
   public static final String STANDARD_FOR_UPDATE_CLAUSE = " FOR UPDATE";
   public static final String SQL_SERVER_FOR_UPDATE_CLAUSE = " WITH (UPDLOCK, ROWLOCK)";
   private static final String ANN_JOIN_TABLE = "io.micronaut.data.jdbc.annotation.JoinTable";
   private static final String ANN_JOIN_COLUMNS = "io.micronaut.data.jdbc.annotation.JoinColumns";
   private static final String BLANK_SPACE = " ";
   private static final String SEQ_SUFFIX = "_seq";
   private static final String INSERT_INTO = "INSERT INTO ";
   private static final String JDBC_REPO_ANNOTATION = "io.micronaut.data.jdbc.annotation.JdbcRepository";
   private final Dialect dialect;
   private final Map<Dialect, SqlQueryBuilder.DialectConfig> perDialectConfig = new HashMap(3);
   private Pattern positionalParameterPattern;

   @Creator
   public SqlQueryBuilder(AnnotationMetadata annotationMetadata) {
      if (annotationMetadata != null) {
         this.dialect = (Dialect)annotationMetadata.enumValue("io.micronaut.data.jdbc.annotation.JdbcRepository", "dialect", Dialect.class)
            .orElseGet(() -> (Dialect)annotationMetadata.enumValue(Repository.class, "dialect", Dialect.class).orElse(Dialect.ANSI));
         AnnotationValue<SqlQueryConfiguration> annotation = annotationMetadata.getAnnotation(SqlQueryConfiguration.class);
         if (annotation != null) {
            for(AnnotationValue<SqlQueryConfiguration.DialectConfiguration> dialectConfig : annotation.getAnnotations(
               "value", SqlQueryConfiguration.DialectConfiguration.class
            )) {
               dialectConfig.enumValue("dialect", Dialect.class).ifPresent(dialect -> {
                  SqlQueryBuilder.DialectConfig dc = new SqlQueryBuilder.DialectConfig();
                  this.perDialectConfig.put(dialect, dc);
                  dialectConfig.stringValue("positionalParameterFormat").ifPresent(format -> dc.positionalFormatter = format);
                  dialectConfig.booleanValue("escapeQueries").ifPresent(escape -> dc.escapeQueries = escape);
               });
            }
         }
      } else {
         this.dialect = Dialect.ANSI;
      }

   }

   public SqlQueryBuilder() {
      this.dialect = Dialect.ANSI;
   }

   public SqlQueryBuilder(Dialect dialect) {
      ArgumentUtils.requireNonNull("dialect", dialect);
      this.dialect = dialect;
   }

   @Override
   public Dialect getDialect() {
      return this.dialect;
   }

   @Override
   protected boolean shouldEscape(@NonNull PersistentEntity entity) {
      SqlQueryBuilder.DialectConfig config = (SqlQueryBuilder.DialectConfig)this.perDialectConfig.get(this.dialect);
      return config != null && config.escapeQueries != null ? config.escapeQueries : super.shouldEscape(entity);
   }

   @Override
   protected String asLiteral(Object value) {
      if ((this.dialect == Dialect.SQL_SERVER || this.dialect == Dialect.ORACLE) && value instanceof Boolean) {
         return (Boolean)value ? "1" : "0";
      } else {
         return super.asLiteral(value);
      }
   }

   @Override
   public boolean shouldAliasProjections() {
      return false;
   }

   @Override
   protected boolean isExpandEmbedded() {
      return true;
   }

   @NonNull
   public String buildBatchCreateTableStatement(@NonNull PersistentEntity... entities) {
      return (String)Arrays.stream(entities)
         .flatMap(entity -> Stream.of(this.buildCreateTableStatements(entity)))
         .collect(Collectors.joining(System.getProperty("line.separator")));
   }

   @NonNull
   public String buildBatchDropTableStatement(@NonNull PersistentEntity... entities) {
      return (String)Arrays.stream(entities).flatMap(entity -> Stream.of(this.buildDropTableStatements(entity))).collect(Collectors.joining("\n"));
   }

   @NonNull
   public String[] buildDropTableStatements(@NonNull PersistentEntity entity) {
      String tableName = this.getTableName(entity);
      boolean escape = this.shouldEscape(entity);
      String sql = "DROP TABLE " + tableName;
      Collection<Association> foreignKeyAssociations = this.getJoinTableAssociations(entity);
      List<String> dropStatements = new ArrayList();

      for(Association association : foreignKeyAssociations) {
         AnnotationMetadata associationMetadata = association.getAnnotationMetadata();
         NamingStrategy namingStrategy = entity.getNamingStrategy();
         String joinTableName = (String)associationMetadata.stringValue("io.micronaut.data.jdbc.annotation.JoinTable", "name")
            .orElseGet(() -> namingStrategy.mappedName(association));
         dropStatements.add("DROP TABLE " + (escape ? this.quote(joinTableName) : joinTableName) + ";");
      }

      dropStatements.add(sql);
      return (String[])dropStatements.toArray(new String[0]);
   }

   @NonNull
   public String buildJoinTableInsert(@NonNull PersistentEntity entity, @NonNull Association association) {
      if (!isForeignKeyWithJoinTable(association)) {
         throw new IllegalArgumentException("Join table inserts can only be built for foreign key associations that are mapped with a join table.");
      } else {
         Optional<Association> inverseSide = association.getInverseSide().map(Function.identity());
         Association owningAssociation = (Association)inverseSide.orElse(association);
         AnnotationMetadata annotationMetadata = owningAssociation.getAnnotationMetadata();
         NamingStrategy namingStrategy = entity.getNamingStrategy();
         String joinTableName = (String)annotationMetadata.stringValue("io.micronaut.data.jdbc.annotation.JoinTable", "name")
            .orElseGet(() -> namingStrategy.mappedName(association));
         List<String> leftJoinColumns = this.resolveJoinTableJoinColumns(annotationMetadata, true, entity, namingStrategy);
         List<String> rightJoinColumns = this.resolveJoinTableJoinColumns(annotationMetadata, false, association.getAssociatedEntity(), namingStrategy);
         boolean escape = this.shouldEscape(entity);
         String columns = (String)Stream.concat(leftJoinColumns.stream(), rightJoinColumns.stream())
            .map(columnName -> escape ? this.quote(columnName) : columnName)
            .collect(Collectors.joining(","));
         String placeholders = (String)IntStream.range(0, leftJoinColumns.size() + rightJoinColumns.size()).mapToObj(i -> "?").collect(Collectors.joining(","));
         return "INSERT INTO " + this.quote(joinTableName) + " (" + columns + ") VALUES (" + placeholders + ")";
      }
   }

   public static boolean isForeignKeyWithJoinTable(@NonNull Association association) {
      return association.isForeignKey() && !association.getAnnotationMetadata().stringValue(Relation.class, "mappedBy").isPresent();
   }

   @NonNull
   public String[] buildCreateTableStatements(@NonNull PersistentEntity entity) {
      ArgumentUtils.requireNonNull("entity", entity);
      String unescapedTableName = this.getUnescapedTableName(entity);
      String tableName = this.getTableName(entity);
      boolean escape = this.shouldEscape(entity);
      PersistentProperty identity = entity.getIdentity();
      List<String> createStatements = new ArrayList();
      String schema = (String)entity.getAnnotationMetadata().stringValue(MappedEntity.class, "schema").orElse(null);
      if (StringUtils.isNotEmpty(schema)) {
         if (escape) {
            schema = this.quote(schema);
         }

         createStatements.add("CREATE SCHEMA " + schema + ";");
      }

      Collection<Association> foreignKeyAssociations = this.getJoinTableAssociations(entity);
      NamingStrategy namingStrategy = entity.getNamingStrategy();
      if (CollectionUtils.isNotEmpty(foreignKeyAssociations)) {
         for(Association association : foreignKeyAssociations) {
            StringBuilder joinTableBuilder = new StringBuilder("CREATE TABLE ");
            PersistentEntity associatedEntity = association.getAssociatedEntity();
            Optional<Association> inverseSide = association.getInverseSide().map(Function.identity());
            Association owningAssociation = (Association)inverseSide.orElse(association);
            AnnotationMetadata annotationMetadata = owningAssociation.getAnnotationMetadata();
            String joinTableName = (String)annotationMetadata.stringValue("io.micronaut.data.jdbc.annotation.JoinTable", "name")
               .orElseGet(() -> namingStrategy.mappedName(association));
            if (escape) {
               joinTableName = this.quote(joinTableName);
            }

            joinTableBuilder.append(joinTableName).append(" (");
            List<PersistentPropertyPath> leftProperties = new ArrayList();
            List<PersistentPropertyPath> rightProperties = new ArrayList();
            boolean isAssociationOwner = !inverseSide.isPresent();
            List<String> leftJoinTableColumns = this.resolveJoinTableJoinColumns(annotationMetadata, isAssociationOwner, entity, namingStrategy);
            List<String> rightJoinTableColumns = this.resolveJoinTableJoinColumns(
               annotationMetadata, !isAssociationOwner, association.getAssociatedEntity(), namingStrategy
            );
            this.traversePersistentProperties(
               entity.getIdentity(), (associations, property) -> leftProperties.add(PersistentPropertyPath.of(associations, property, ""))
            );
            this.traversePersistentProperties(
               associatedEntity.getIdentity(), (associations, property) -> rightProperties.add(PersistentPropertyPath.of(associations, property, ""))
            );
            if (leftJoinTableColumns.size() == leftProperties.size()) {
               for(int i = 0; i < leftJoinTableColumns.size(); ++i) {
                  PersistentPropertyPath pp = (PersistentPropertyPath)leftProperties.get(i);
                  String columnName = (String)leftJoinTableColumns.get(i);
                  if (escape) {
                     columnName = this.quote(columnName);
                  }

                  joinTableBuilder.append(this.addTypeToColumn(pp.getProperty(), columnName, true)).append(',');
               }
            } else {
               for(PersistentPropertyPath pp : leftProperties) {
                  String columnName = namingStrategy.mappedJoinTableColumn(entity, pp.getAssociations(), pp.getProperty());
                  if (escape) {
                     columnName = this.quote(columnName);
                  }

                  joinTableBuilder.append(this.addTypeToColumn(pp.getProperty(), columnName, true)).append(',');
               }
            }

            if (rightJoinTableColumns.size() == rightProperties.size()) {
               for(int i = 0; i < rightJoinTableColumns.size(); ++i) {
                  PersistentPropertyPath pp = (PersistentPropertyPath)rightProperties.get(i);
                  String columnName = (String)rightJoinTableColumns.get(i);
                  if (escape) {
                     columnName = this.quote(columnName);
                  }

                  joinTableBuilder.append(this.addTypeToColumn(pp.getProperty(), columnName, true)).append(',');
               }
            } else {
               for(PersistentPropertyPath pp : rightProperties) {
                  String columnName = namingStrategy.mappedJoinTableColumn(entity, pp.getAssociations(), pp.getProperty());
                  if (escape) {
                     columnName = this.quote(columnName);
                  }

                  joinTableBuilder.append(this.addTypeToColumn(pp.getProperty(), columnName, true)).append(',');
               }
            }

            joinTableBuilder.setLength(joinTableBuilder.length() - 1);
            joinTableBuilder.append(")");
            if (this.dialect != Dialect.ORACLE) {
               joinTableBuilder.append(';');
            }

            createStatements.add(joinTableBuilder.toString());
         }
      }

      boolean generatePkAfterColumns = false;
      List<String> primaryColumnsName = new ArrayList();
      List<String> columns = new ArrayList();
      if (identity != null) {
         List<PersistentPropertyPath> ids = new ArrayList();
         this.traversePersistentProperties(identity, (associations, property) -> ids.add(PersistentPropertyPath.of(associations, property, "")));
         if (ids.size() > 1) {
            generatePkAfterColumns = true;
         }

         boolean finalGeneratePkAfterColumns = generatePkAfterColumns;

         for(PersistentPropertyPath pp : ids) {
            String column = namingStrategy.mappedName(pp.getAssociations(), pp.getProperty());
            if (escape) {
               column = this.quote(column);
            }

            primaryColumnsName.add(column);
            column = this.addTypeToColumn(pp.getProperty(), column, this.isRequired(pp.getAssociations(), pp.getProperty()));
            if (this.isNotForeign(pp.getAssociations())) {
               column = this.addGeneratedStatementToColumn(pp.getProperty(), column, !finalGeneratePkAfterColumns);
            }

            columns.add(column);
         }
      }

      PersistentProperty version = entity.getVersion();
      if (version != null) {
         String column = namingStrategy.mappedName(Collections.emptyList(), version);
         if (escape) {
            column = this.quote(column);
         }

         column = this.addTypeToColumn(version, column, true);
         columns.add(column);
      }

      BiConsumer<List<Association>, PersistentProperty> addColumn = (associations, property) -> {
         String column = namingStrategy.mappedName(associations, property);
         if (escape) {
            column = this.quote(column);
         }

         column = this.addTypeToColumn(property, column, this.isRequired(associations, property));
         if (this.isNotForeign(associations)) {
            column = this.addGeneratedStatementToColumn(property, column, false);
         }

         columns.add(column);
      };

      for(PersistentProperty prop : entity.getPersistentProperties()) {
         this.traversePersistentProperties(prop, addColumn);
      }

      StringBuilder builder = new StringBuilder("CREATE TABLE ").append(tableName).append(" (");
      builder.append(String.join(",", columns));
      if (generatePkAfterColumns) {
         builder.append(", PRIMARY KEY(").append(String.join(",", primaryColumnsName)).append(')');
      }

      if (this.dialect == Dialect.ORACLE) {
         builder.append(")");
      } else {
         builder.append(");");
      }

      if (identity != null && identity.isGenerated()) {
         GeneratedValue.Type idGeneratorType = (GeneratedValue.Type)identity.getAnnotationMetadata()
            .enumValue(GeneratedValue.class, GeneratedValue.Type.class)
            .orElseGet(() -> this.selectAutoStrategy(identity));
         boolean isSequence = idGeneratorType == GeneratedValue.Type.SEQUENCE;
         String generatedDefinition = (String)identity.getAnnotationMetadata().stringValue(GeneratedValue.class, "definition").orElse(null);
         if (generatedDefinition != null) {
            createStatements.add(generatedDefinition);
         } else if (isSequence) {
            boolean isSqlServer = this.dialect == Dialect.SQL_SERVER;
            String sequenceName = this.quote(unescapedTableName + "_seq");
            String createSequenceStmt = "CREATE SEQUENCE " + sequenceName;
            if (isSqlServer) {
               createSequenceStmt = createSequenceStmt + " AS BIGINT";
            }

            createSequenceStmt = createSequenceStmt + " MINVALUE 1 START WITH 1";
            if (this.dialect == Dialect.ORACLE) {
               createSequenceStmt = createSequenceStmt + " NOCACHE NOCYCLE";
            } else if (isSqlServer) {
               createSequenceStmt = createSequenceStmt + " INCREMENT BY 1";
            }

            createStatements.add(createSequenceStmt);
         }
      }

      createStatements.add(builder.toString());
      this.addIndexes(entity, tableName, createStatements);
      return (String[])createStatements.toArray(new String[0]);
   }

   private void addIndexes(PersistentEntity entity, String tableName, List<String> createStatements) {
      String indexes = this.createIndexes(entity, tableName);
      if (indexes.length() > 0) {
         createStatements.add(indexes);
      }

   }

   private String createIndexes(PersistentEntity entity, String tableName) {
      StringBuilder indexBuilder = new StringBuilder();
      Optional<List<AnnotationValue<Index>>> indexes = entity.findAnnotation(Indexes.class).map(idxes -> idxes.getAnnotations("value", Index.class));
      Stream.of(indexes)
         .flatMap(o -> (Stream)o.map(Stream::of).orElseGet(Stream::empty))
         .flatMap(Collection::stream)
         .forEach(index -> this.addIndex(indexBuilder, new SqlQueryBuilder.IndexConfiguration(index, tableName)));
      return indexBuilder.toString();
   }

   private void addIndex(StringBuilder indexBuilder, SqlQueryBuilder.IndexConfiguration config) {
      indexBuilder.append("CREATE ")
         .append((String)config.index.booleanValue("unique").map(isUnique -> isUnique ? "UNIQUE " : "").orElse(""))
         .append("INDEX ")
         .append(
            (String)config.index
               .stringValue("name")
               .orElse(String.format("idx_%s%s", this.prepareNames(config.tableName), this.makeTransformedColumnList(this.provideColumnList(config))))
         )
         .append(
            " ON "
               + (String)Optional.ofNullable(config.tableName).orElseThrow(() -> new NullPointerException("Table name cannot be null"))
               + " ("
               + this.provideColumnList(config)
         );
      if (this.dialect == Dialect.ORACLE) {
         indexBuilder.append(")");
      } else {
         indexBuilder.append(");");
      }

   }

   private String provideColumnList(SqlQueryBuilder.IndexConfiguration config) {
      return String.join(", ", (String[])config.index.getValues().get("columns"));
   }

   private String makeTransformedColumnList(String columnList) {
      return (String)Arrays.stream(this.prepareNames(columnList).split(",")).map(col -> "_" + col).collect(Collectors.joining());
   }

   private String prepareNames(String columnList) {
      return (String)columnList.chars()
         .mapToObj(c -> String.valueOf((char)c))
         .filter(x -> !x.equals(" "))
         .filter(x -> !x.equals("\""))
         .map(String::toLowerCase)
         .collect(Collectors.joining());
   }

   private boolean isRequired(List<Association> associations, PersistentProperty property) {
      Association foreignAssociation = null;

      for(Association association : associations) {
         if (!association.isRequired()) {
            return false;
         }

         if (association.getKind() != Relation.Kind.EMBEDDED && foreignAssociation == null) {
            foreignAssociation = association;
         }
      }

      return foreignAssociation != null ? foreignAssociation.isRequired() : property.isRequired();
   }

   private boolean isNotForeign(List<Association> associations) {
      for(Association association : associations) {
         if (association.getKind() != Relation.Kind.EMBEDDED) {
            return false;
         }
      }

      return true;
   }

   @Override
   protected String getTableAsKeyword() {
      return " ";
   }

   private String addGeneratedStatementToColumn(PersistentProperty prop, String column, boolean isPk) {
      if (prop.isGenerated()) {
         GeneratedValue.Type type = (GeneratedValue.Type)prop.getAnnotationMetadata()
            .enumValue(GeneratedValue.class, GeneratedValue.Type.class)
            .orElse(GeneratedValue.Type.AUTO);
         if (type == GeneratedValue.Type.AUTO) {
            if (prop.getDataType() == DataType.UUID) {
               type = GeneratedValue.Type.UUID;
            } else if (this.dialect == Dialect.ORACLE) {
               type = GeneratedValue.Type.SEQUENCE;
            } else {
               type = GeneratedValue.Type.IDENTITY;
            }
         }

         boolean addPkBefore = this.dialect != Dialect.H2 && this.dialect != Dialect.ORACLE;
         if (isPk && addPkBefore) {
            column = column + " PRIMARY KEY";
         }

         switch(this.dialect) {
            case POSTGRES:
               if (type == GeneratedValue.Type.SEQUENCE) {
                  column = column + " NOT NULL";
               } else if (type == GeneratedValue.Type.IDENTITY) {
                  if (isPk) {
                     column = column + " GENERATED ALWAYS AS IDENTITY";
                  } else {
                     column = column + " NOT NULL";
                  }
               } else if (type == GeneratedValue.Type.UUID) {
                  column = column + " NOT NULL DEFAULT uuid_generate_v4()";
               }
               break;
            case SQL_SERVER:
               if (type == GeneratedValue.Type.UUID) {
                  column = column + " NOT NULL DEFAULT newid()";
               } else if (type == GeneratedValue.Type.SEQUENCE) {
                  if (isPk) {
                     column = column + " NOT NULL";
                  }
               } else {
                  column = column + " IDENTITY(1,1) NOT NULL";
               }
               break;
            case ORACLE:
               if (type == GeneratedValue.Type.UUID) {
                  column = column + " NOT NULL DEFAULT SYS_GUID()";
               } else if (type == GeneratedValue.Type.IDENTITY) {
                  if (isPk) {
                     column = column + " GENERATED ALWAYS AS IDENTITY";
                  } else {
                     column = column + " NOT NULL";
                  }
               } else {
                  column = column + " NOT NULL";
               }
               break;
            default:
               if (type == GeneratedValue.Type.UUID) {
                  if (this.dialect != Dialect.MYSQL) {
                     column = column + " NOT NULL DEFAULT random_uuid()";
                  } else {
                     column = column + " NOT NULL";
                  }
               } else {
                  column = column + " AUTO_INCREMENT";
               }
         }

         if (isPk && !addPkBefore) {
            column = column + " PRIMARY KEY";
         }
      }

      return column;
   }

   @NonNull
   private List<String> resolveJoinTableJoinColumns(
      AnnotationMetadata annotationMetadata, boolean associationOwner, PersistentEntity entity, NamingStrategy namingStrategy
   ) {
      List<String> joinColumns = this.getJoinedColumns(annotationMetadata, associationOwner, "name");
      if (!joinColumns.isEmpty()) {
         return joinColumns;
      } else {
         List<String> columns = new ArrayList();
         this.traversePersistentProperties(
            entity.getIdentity(), (associations, property) -> columns.add(namingStrategy.mappedJoinTableColumn(entity, associations, property))
         );
         return columns;
      }
   }

   @NonNull
   private List<String> resolveJoinTableAssociatedColumns(
      AnnotationMetadata annotationMetadata, boolean associationOwner, PersistentEntity entity, NamingStrategy namingStrategy
   ) {
      List<String> joinColumns = this.getJoinedColumns(annotationMetadata, associationOwner, "referencedColumnName");
      if (!joinColumns.isEmpty()) {
         return joinColumns;
      } else {
         PersistentProperty identity = entity.getIdentity();
         if (identity == null) {
            throw new MappingException("Cannot have a foreign key association without an ID on entity: " + entity.getName());
         } else {
            List<String> columns = new ArrayList();
            this.traversePersistentProperties(identity, (associations, property) -> {
               String columnName = namingStrategy.mappedName(associations, property);
               columns.add(columnName);
            });
            return columns;
         }
      }
   }

   @NonNull
   private List<String> getJoinedColumns(AnnotationMetadata annotationMetadata, boolean associationOwner, String columnType) {
      AnnotationValue<Annotation> joinTable = annotationMetadata.getAnnotation("io.micronaut.data.jdbc.annotation.JoinTable");
      return joinTable != null
         ? (List)joinTable.getAnnotations(associationOwner ? "joinColumns" : "inverseJoinColumns")
            .stream()
            .map(ann -> (String)ann.stringValue(columnType).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList())
         : Collections.emptyList();
   }

   @NonNull
   private Collection<Association> getJoinTableAssociations(PersistentEntity persistentEntity) {
      return (Collection<Association>)Stream.concat(Stream.of(persistentEntity.getIdentity()), persistentEntity.getPersistentProperties().stream())
         .flatMap(this::flatMapEmbedded)
         .filter(p -> {
            if (p instanceof Association) {
               Association a = (Association)p;
               return isForeignKeyWithJoinTable(a);
            } else {
               return false;
            }
         })
         .map(p -> (Association)p)
         .collect(Collectors.toList());
   }

   @Override
   protected void selectAllColumns(AbstractSqlLikeQueryBuilder.QueryState queryState, StringBuilder queryBuffer) {
      PersistentEntity entity = queryState.getEntity();
      this.selectAllColumns(entity, queryState.getRootAlias(), queryBuffer);
      QueryModel queryModel = queryState.getQueryModel();
      Collection<JoinPath> allPaths = queryModel.getJoinPaths();
      if (CollectionUtils.isNotEmpty(allPaths)) {
         Collection<JoinPath> joinPaths = (Collection)allPaths.stream().filter(jp -> {
            Join.Type jt = jp.getJoinType();
            return jt.name().contains("FETCH");
         }).collect(Collectors.toList());
         if (CollectionUtils.isNotEmpty(joinPaths)) {
            for(JoinPath joinPath : joinPaths) {
               Association association = joinPath.getAssociation();
               if (!(association instanceof Embedded)) {
                  PersistentEntity associatedEntity = association.getAssociatedEntity();
                  NamingStrategy namingStrategy = associatedEntity.getNamingStrategy();
                  String aliasName = this.getAliasName(joinPath);
                  String joinPathAlias = this.getPathOnlyAliasName(joinPath);
                  queryBuffer.append(',');
                  boolean includeIdentity = false;
                  if (association.isForeignKey()) {
                     includeIdentity = true;
                  }

                  this.traversePersistentProperties(
                     associatedEntity,
                     includeIdentity,
                     true,
                     (propertyAssociations, prop) -> {
                        String columnName;
                        if (this.computePropertyPaths()) {
                           columnName = namingStrategy.mappedName(propertyAssociations, prop);
                        } else {
                           columnName = this.asPath(propertyAssociations, prop);
                        }
   
                        queryBuffer.append(aliasName)
                           .append('.')
                           .append(queryState.shouldEscape() ? this.quote(columnName) : columnName)
                           .append(" AS ")
                           .append(joinPathAlias)
                           .append(columnName)
                           .append(',');
                     }
                  );
                  queryBuffer.setLength(queryBuffer.length() - 1);
               }
            }
         }
      }

   }

   @Override
   public void selectAllColumns(PersistentEntity entity, String alias, StringBuilder sb) {
      if (this.canUseWildcardForSelect(entity)) {
         if (alias != null) {
            sb.append(alias).append('.');
         }

         sb.append("*");
      } else {
         boolean escape = this.shouldEscape(entity);
         NamingStrategy namingStrategy = entity.getNamingStrategy();
         int length = sb.length();
         this.traversePersistentProperties(entity, (associations, property) -> {
            String transformed = (String)this.getDataTransformerReadValue(alias, property).orElse(null);
            if (transformed != null) {
               sb.append(transformed).append(" AS ").append(property.getPersistedName());
            } else {
               String column = namingStrategy.mappedName(associations, property);
               if (escape) {
                  column = this.quote(column);
               }

               sb.append(alias).append('.').append(column);
            }

            sb.append(',');
         });
         int newLength = sb.length();
         if (newLength == length) {
            if (alias != null) {
               sb.append(alias).append('.');
            }

            sb.append("*");
         } else {
            sb.setLength(newLength - 1);
         }

      }
   }

   private boolean canUseWildcardForSelect(PersistentEntity entity) {
      return Stream.concat(Stream.of(entity.getIdentity()), entity.getPersistentProperties().stream()).flatMap(this::flatMapEmbedded).noneMatch(pp -> {
         if (pp instanceof Association) {
            Association association = (Association)pp;
            return !association.isForeignKey();
         } else {
            return true;
         }
      });
   }

   private Stream<? extends PersistentProperty> flatMapEmbedded(PersistentProperty pp) {
      if (pp instanceof Embedded) {
         Embedded embedded = (Embedded)pp;
         PersistentEntity embeddedEntity = embedded.getAssociatedEntity();
         return embeddedEntity.getPersistentProperties().stream().flatMap(this::flatMapEmbedded);
      } else {
         return Stream.of(pp);
      }
   }

   @Override
   public String resolveJoinType(Join.Type jt) {
      String joinType;
      switch(jt) {
         case LEFT:
         case LEFT_FETCH:
            joinType = " LEFT JOIN ";
            break;
         case RIGHT:
         case RIGHT_FETCH:
            joinType = " RIGHT JOIN ";
            break;
         case OUTER:
            joinType = " FULL OUTER JOIN ";
            break;
         default:
            joinType = " INNER JOIN ";
      }

      return joinType;
   }

   @NonNull
   @Override
   public QueryResult buildInsert(AnnotationMetadata repositoryMetadata, PersistentEntity entity) {
      boolean escape = this.shouldEscape(entity);
      String unescapedTableName = this.getUnescapedTableName(entity);
      NamingStrategy namingStrategy = entity.getNamingStrategy();
      Collection<? extends PersistentProperty> persistentProperties = entity.getPersistentProperties();
      List<QueryParameterBinding> parameterBindings = new ArrayList();
      List<String> columns = new ArrayList();
      List<String> values = new ArrayList();

      for(PersistentProperty prop : persistentProperties) {
         if (!prop.isGenerated()) {
            this.traversePersistentProperties(prop, (associations, property) -> {
               this.addWriteExpression(values, prop);
               final String key = String.valueOf(values.size());
               final String[] path = this.asStringPath(associations, property);
               parameterBindings.add(new QueryParameterBinding() {
                  @Override
                  public String getKey() {
                     return key;
                  }

                  @Override
                  public DataType getDataType() {
                     return property.getDataType();
                  }

                  @Override
                  public String[] getPropertyPath() {
                     return path;
                  }
               });
               String columnName = namingStrategy.mappedName(associations, property);
               if (escape) {
                  columnName = this.quote(columnName);
               }

               columns.add(columnName);
            });
         }
      }

      final PersistentProperty version = entity.getVersion();
      if (version != null) {
         this.addWriteExpression(values, version);
         final String key = String.valueOf(values.size());
         parameterBindings.add(new QueryParameterBinding() {
            @Override
            public String getKey() {
               return key;
            }

            @Override
            public DataType getDataType() {
               return version.getDataType();
            }

            @Override
            public String[] getPropertyPath() {
               return new String[]{version.getName()};
            }
         });
         String columnName = namingStrategy.mappedName(Collections.emptyList(), version);
         if (escape) {
            columnName = this.quote(columnName);
         }

         columns.add(columnName);
      }

      PersistentProperty identity = entity.getIdentity();
      if (identity != null) {
         this.traversePersistentProperties(
            identity,
            (associations, property) -> {
               boolean isSequence = false;
               if (this.isNotForeign(associations)) {
                  Optional<AnnotationValue<GeneratedValue>> generated = property.findAnnotation(GeneratedValue.class);
                  if (generated.isPresent()) {
                     GeneratedValue.Type idGeneratorType = (GeneratedValue.Type)generated.flatMap(av -> av.enumValue(GeneratedValue.Type.class))
                        .orElseGet(() -> this.selectAutoStrategy(property));
                     if (idGeneratorType == GeneratedValue.Type.SEQUENCE) {
                        isSequence = true;
                     } else if (this.dialect != Dialect.MYSQL || property.getDataType() != DataType.UUID) {
                        return;
                     }
                  }
               }
   
               if (isSequence) {
                  values.add(this.getSequenceStatement(unescapedTableName, property));
               } else {
                  this.addWriteExpression(values, property);
                  final String var12x = String.valueOf(values.size());
                  final String[] var14x = this.asStringPath(associations, property);
                  parameterBindings.add(new QueryParameterBinding() {
                     @Override
                     public String getKey() {
                        return key;
                     }
   
                     @Override
                     public DataType getDataType() {
                        return property.getDataType();
                     }
   
                     @Override
                     public String[] getPropertyPath() {
                        return path;
                     }
                  });
               }
   
               String var13x = namingStrategy.mappedName(associations, property);
               if (escape) {
                  var13x = this.quote(var13x);
               }
   
               columns.add(var13x);
            }
         );
      }

      String builder = "INSERT INTO "
         + this.getTableName(entity)
         + " ("
         + String.join(",", columns)
         + ')'
         + " VALUES ("
         + String.join(String.valueOf(','), values)
         + ')';
      return QueryResult.of(builder, Collections.emptyList(), parameterBindings, Collections.emptyMap());
   }

   private String[] asStringPath(List<Association> associations, PersistentProperty property) {
      if (associations.isEmpty()) {
         return new String[]{property.getName()};
      } else {
         List<String> path = new ArrayList(associations.size() + 1);

         for(Association association : associations) {
            path.add(association.getName());
         }

         path.add(property.getName());
         return (String[])path.toArray(new String[0]);
      }
   }

   private String getSequenceStatement(String unescapedTableName, PersistentProperty property) {
      String sequenceName = this.resolveSequenceName(property, unescapedTableName);
      switch(this.dialect) {
         case POSTGRES:
            return "nextval('" + sequenceName + "')";
         case SQL_SERVER:
            return "NEXT VALUE FOR " + this.quote(sequenceName);
         case ORACLE:
            return this.quote(sequenceName) + ".nextval";
         default:
            throw new IllegalStateException("Cannot generate a sequence for dialect: " + this.dialect);
      }
   }

   private String resolveSequenceName(PersistentProperty identity, String unescapedTableName) {
      return (String)identity.getAnnotationMetadata()
         .stringValue(GeneratedValue.class, "ref")
         .map(n -> StringUtils.isEmpty(n) ? unescapedTableName + "_seq" : n)
         .orElseGet(() -> unescapedTableName + "_seq");
   }

   @NonNull
   @Override
   public QueryResult buildPagination(@NonNull Pageable pageable) {
      int size = pageable.getSize();
      if (size > 0) {
         StringBuilder builder = new StringBuilder(" ");
         long from = pageable.getOffset();
         switch(this.dialect) {
            case POSTGRES:
               builder.append("LIMIT ").append(size).append(" ");
               if (from != 0L) {
                  builder.append("OFFSET ").append(from);
               }
               break;
            case SQL_SERVER:
               if (from == 0L) {
                  builder.append("OFFSET ").append(0).append(" ROWS ");
               }
            case ORACLE:
            case ANSI:
            default:
               if (from != 0L) {
                  builder.append("OFFSET ").append(from).append(" ROWS ");
               }

               builder.append("FETCH NEXT ").append(size).append(" ROWS ONLY ");
               break;
            case H2:
            case MYSQL:
               if (from == 0L) {
                  builder.append("LIMIT ").append(size);
               } else {
                  builder.append("LIMIT ").append(from).append(',').append(size);
               }
         }

         return QueryResult.of(builder.toString(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
      } else {
         return QueryResult.of("", Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
      }
   }

   @Override
   protected String getAliasName(PersistentEntity entity) {
      return entity.getAliasName();
   }

   @Override
   public String getTableName(PersistentEntity entity) {
      boolean escape = this.shouldEscape(entity);
      String tableName = entity.getPersistedName();
      String schema = (String)entity.getAnnotationMetadata().stringValue(MappedEntity.class, "schema").orElse(null);
      if (StringUtils.isNotEmpty(schema)) {
         return escape ? this.quote(schema) + '.' + this.quote(tableName) : schema + '.' + tableName;
      } else {
         return escape ? this.quote(tableName) : tableName;
      }
   }

   @Override
   protected String formatStartsWith() {
      return this.dialect == Dialect.ORACLE ? " LIKE '%' || " : super.formatStartsWith();
   }

   @Override
   protected String formEndsWithEnd() {
      return this.dialect == Dialect.ORACLE ? " " : super.formEndsWithEnd();
   }

   @Override
   protected String formatEndsWith() {
      return this.dialect == Dialect.ORACLE ? " || '%'" : super.formatEndsWith();
   }

   @Override
   protected String formatStartsWithBeginning() {
      return this.dialect == Dialect.ORACLE ? " LIKE " : super.formatStartsWithBeginning();
   }

   private boolean addWriteExpression(List<String> values, PersistentProperty property) {
      DataType dt = property.getDataType();
      String transformer = (String)this.getDataTransformerWriteValue(null, property).orElse(null);
      if (transformer != null) {
         return values.add(transformer);
      } else if (dt == DataType.JSON) {
         switch(this.dialect) {
            case POSTGRES:
               return values.add("to_json(" + this.formatParameter(values.size() + 1).getName() + "::json)");
            case SQL_SERVER:
            case ORACLE:
            default:
               return values.add(this.formatParameter(values.size() + 1).getName());
            case H2:
               return values.add(this.formatParameter(values.size() + 1).getName() + " FORMAT JSON");
            case MYSQL:
               return values.add("CONVERT(" + this.formatParameter(values.size() + 1).getName() + " USING UTF8MB4)");
         }
      } else {
         return values.add(this.formatParameter(values.size() + 1).getName());
      }
   }

   @Override
   protected void appendUpdateSetParameter(StringBuilder sb, String alias, PersistentProperty prop, Runnable appendParameter) {
      String transformed = (String)this.getDataTransformerWriteValue(alias, prop).orElse(null);
      if (transformed != null) {
         this.appendTransformed(sb, transformed, appendParameter);
      } else {
         if (prop.getDataType() == DataType.JSON) {
            switch(this.dialect) {
               case POSTGRES:
                  sb.append("to_json(");
                  appendParameter.run();
                  sb.append("::json)");
                  break;
               case SQL_SERVER:
               case ORACLE:
               default:
                  super.appendUpdateSetParameter(sb, alias, prop, appendParameter);
                  break;
               case H2:
                  appendParameter.run();
                  sb.append(" FORMAT JSON");
                  break;
               case MYSQL:
                  sb.append("CONVERT(");
                  appendParameter.run();
                  sb.append(" USING UTF8MB4)");
            }
         } else {
            super.appendUpdateSetParameter(sb, alias, prop, appendParameter);
         }

      }
   }

   @Override
   protected String[] buildJoin(
      final String alias,
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
         String joinAlias = alias;

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
                  joinAlias = existingAlias;
               } else {
                  PersistentEntity associatedEntity = association.getAssociatedEntity();
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
                  joinAliases[i] = this.getAliasName(joinPathToUse);
                  String currentJoinAlias = joinAliases[i];
                  this.buildJoin(
                     joinType,
                     target,
                     queryState,
                     joinAssociationsPath,
                     joinAlias,
                     association,
                     associatedEntity,
                     (PersistentEntity)this.findOwner(joinAssociationsPath, association).orElseGet(queryState::getEntity),
                     currentJoinAlias
                  );
                  joinAlias = currentJoinAlias;
               }

               joinAssociationsPath.clear();
            }
         }

         return joinAliases;
      }
   }

   private void buildJoin(
      String joinType,
      StringBuilder sb,
      AbstractSqlLikeQueryBuilder.QueryState queryState,
      List<Association> joinAssociationsPath,
      String joinAlias,
      Association association,
      PersistentEntity associatedEntity,
      PersistentEntity associationOwner,
      String currentJoinAlias
   ) {
      boolean escape = this.shouldEscape(associationOwner);
      String mappedBy = (String)association.getAnnotationMetadata().stringValue(Relation.class, "mappedBy").orElse(null);
      if (association.getKind() == Relation.Kind.MANY_TO_MANY || association.isForeignKey() && StringUtils.isEmpty(mappedBy)) {
         PersistentProperty identity = associatedEntity.getIdentity();
         if (identity == null) {
            throw new IllegalArgumentException("Associated entity [" + associatedEntity.getName() + "] defines no ID. Cannot join.");
         }

         PersistentProperty associatedId = associationOwner.getIdentity();
         if (associatedId == null) {
            throw new MappingException("Cannot join on entity [" + associationOwner.getName() + "] that has no declared ID");
         }

         Optional<Association> inverseSide = association.getInverseSide().map(Function.identity());
         Association owningAssociation = (Association)inverseSide.orElse(association);
         boolean isAssociationOwner = !association.getInverseSide().isPresent();
         NamingStrategy namingStrategy = associationOwner.getNamingStrategy();
         AnnotationMetadata annotationMetadata = owningAssociation.getAnnotationMetadata();
         List<String> ownerJoinColumns = this.resolveJoinTableAssociatedColumns(annotationMetadata, isAssociationOwner, associationOwner, namingStrategy);
         List<String> ownerJoinTableColumns = this.resolveJoinTableJoinColumns(annotationMetadata, isAssociationOwner, associationOwner, namingStrategy);
         List<String> associationJoinColumns = this.resolveJoinTableAssociatedColumns(annotationMetadata, !isAssociationOwner, associatedEntity, namingStrategy);
         List<String> associationJoinTableColumns = this.resolveJoinTableJoinColumns(annotationMetadata, !isAssociationOwner, associatedEntity, namingStrategy);
         if (escape) {
            ownerJoinColumns = (List)ownerJoinColumns.stream().map(this::quote).collect(Collectors.toList());
            ownerJoinTableColumns = (List)ownerJoinTableColumns.stream().map(this::quote).collect(Collectors.toList());
            associationJoinColumns = (List)associationJoinColumns.stream().map(this::quote).collect(Collectors.toList());
            associationJoinTableColumns = (List)associationJoinTableColumns.stream().map(this::quote).collect(Collectors.toList());
         }

         String joinTableName = (String)annotationMetadata.stringValue("io.micronaut.data.jdbc.annotation.JoinTable", "name")
            .orElseGet(() -> namingStrategy.mappedName(association));
         String joinTableAlias = (String)annotationMetadata.stringValue("io.micronaut.data.jdbc.annotation.JoinTable", "alias")
            .orElseGet(() -> currentJoinAlias + joinTableName + "_");
         this.join(
            sb,
            queryState.getQueryModel(),
            joinType,
            escape ? this.quote(joinTableName) : joinTableName,
            joinTableAlias,
            joinAlias,
            ownerJoinColumns,
            ownerJoinTableColumns
         );
         sb.append(' ');
         this.join(
            sb,
            queryState.getQueryModel(),
            joinType,
            this.getTableName(associatedEntity),
            currentJoinAlias,
            joinTableAlias,
            associationJoinTableColumns,
            associationJoinColumns
         );
      } else if (StringUtils.isNotEmpty(mappedBy)) {
         PersistentProperty ownerIdentity = associationOwner.getIdentity();
         if (ownerIdentity == null) {
            throw new IllegalArgumentException("Associated entity [" + associationOwner + "] defines no ID. Cannot join.");
         }

         PersistentPropertyPath mappedByPropertyPath = associatedEntity.getPropertyPath(mappedBy);
         if (mappedByPropertyPath == null) {
            throw new MappingException(
               "Foreign key association with mappedBy references a property that doesn't exist [" + mappedBy + "] of entity: " + associatedEntity.getName()
            );
         }

         this.join(
            sb,
            joinType,
            queryState,
            associatedEntity,
            associationOwner,
            joinAlias,
            currentJoinAlias,
            joinAssociationsPath,
            ownerIdentity,
            mappedByPropertyPath.getAssociations(),
            mappedByPropertyPath.getProperty()
         );
      } else {
         PersistentProperty associatedProperty = association.getAssociatedEntity().getIdentity();
         if (associatedProperty == null) {
            throw new IllegalArgumentException("Associated entity [" + association.getAssociatedEntity().getName() + "] defines no ID. Cannot join.");
         }

         this.join(
            sb,
            joinType,
            queryState,
            associatedEntity,
            associationOwner,
            joinAlias,
            currentJoinAlias,
            joinAssociationsPath,
            association,
            Collections.emptyList(),
            associatedProperty
         );
      }

   }

   private void join(
      StringBuilder sb,
      String joinType,
      AbstractSqlLikeQueryBuilder.QueryState queryState,
      PersistentEntity associatedEntity,
      PersistentEntity associationOwner,
      String leftTableAlias,
      String rightTableAlias,
      List<Association> leftPropertyAssociations,
      PersistentProperty leftProperty,
      List<Association> rightPropertyAssociations,
      PersistentProperty rightProperty
   ) {
      boolean escape = this.shouldEscape(associationOwner);
      List<String> onLeftColumns = new ArrayList();
      List<String> onRightColumns = new ArrayList();
      Association association = null;
      if (leftProperty instanceof Association) {
         association = (Association)leftProperty;
      } else if (rightProperty instanceof Association) {
         association = (Association)rightProperty;
      }

      if (association != null) {
         Optional<Association> inverse = association.getInverseSide().map(Function.identity());
         Association owner = (Association)inverse.orElse(association);
         boolean isOwner = leftProperty == owner;
         AnnotationValue<Annotation> joinColumnsHolder = owner.getAnnotationMetadata().getAnnotation("io.micronaut.data.jdbc.annotation.JoinColumns");
         if (joinColumnsHolder != null) {
            onLeftColumns.addAll(
               (Collection)joinColumnsHolder.getAnnotations("value")
                  .stream()
                  .map(ann -> (String)ann.stringValue(isOwner ? "name" : "referencedColumnName").orElse(null))
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList())
            );
            onRightColumns.addAll(
               (Collection)joinColumnsHolder.getAnnotations("value")
                  .stream()
                  .map(ann -> (String)ann.stringValue(isOwner ? "referencedColumnName" : "name").orElse(null))
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList())
            );
         }
      }

      if (onLeftColumns.isEmpty()) {
         this.traversePersistentProperties(leftProperty, (associations, p) -> {
            String column = leftProperty.getOwner().getNamingStrategy().mappedName(this.merge(leftPropertyAssociations, associations), p);
            onLeftColumns.add(column);
         });
         if (onLeftColumns.isEmpty()) {
            throw new MappingException("Cannot join on entity [" + leftProperty.getOwner().getName() + "] that has no declared ID");
         }
      }

      if (onRightColumns.isEmpty()) {
         this.traversePersistentProperties(rightProperty, (associations, p) -> {
            String column = rightProperty.getOwner().getNamingStrategy().mappedName(this.merge(rightPropertyAssociations, associations), p);
            onRightColumns.add(column);
         });
      }

      this.join(
         sb,
         queryState.getQueryModel(),
         joinType,
         this.getTableName(associatedEntity),
         rightTableAlias,
         leftTableAlias,
         escape ? (List)onLeftColumns.stream().map(this::quote).collect(Collectors.toList()) : onLeftColumns,
         escape ? (List)onRightColumns.stream().map(this::quote).collect(Collectors.toList()) : onRightColumns
      );
   }

   private Optional<PersistentEntity> findOwner(List<Association> associations, PersistentProperty property) {
      PersistentEntity owner = property.getOwner();
      if (!owner.isEmbeddable()) {
         return Optional.of(owner);
      } else {
         ListIterator<Association> listIterator = associations.listIterator(associations.size());

         while(listIterator.hasPrevious()) {
            Association association = (Association)listIterator.previous();
            if (!association.getOwner().isEmbeddable()) {
               return Optional.of(association.getOwner());
            }
         }

         return Optional.empty();
      }
   }

   private void join(
      StringBuilder sb,
      QueryModel queryModel,
      String joinType,
      String tableName,
      String tableAlias,
      String onTableName,
      String onTableColumn,
      String tableColumnName
   ) {
      sb.append(joinType).append(tableName).append(' ').append(tableAlias);
      this.appendForUpdate(AbstractSqlLikeQueryBuilder.QueryPosition.AFTER_TABLE_NAME, queryModel, sb);
      sb.append(" ON ").append(onTableName).append('.').append(onTableColumn).append('=').append(tableAlias).append('.').append(tableColumnName);
   }

   private void join(
      StringBuilder builder,
      QueryModel queryModel,
      String joinType,
      String tableName,
      String tableAlias,
      String onTableName,
      List<String> onLeftColumns,
      List<String> onRightColumns
   ) {
      if (onLeftColumns.size() != onRightColumns.size()) {
         throw new IllegalStateException(
            "Un-matching join columns size: " + onLeftColumns.size() + " != " + onRightColumns.size() + " " + onLeftColumns + ", " + onRightColumns
         );
      } else {
         builder.append(joinType).append(tableName).append(' ').append(tableAlias);
         this.appendForUpdate(AbstractSqlLikeQueryBuilder.QueryPosition.AFTER_TABLE_NAME, queryModel, builder);
         builder.append(" ON ");

         for(int i = 0; i < onLeftColumns.size(); ++i) {
            String leftColumn = (String)onLeftColumns.get(i);
            String rightColumn = (String)onRightColumns.get(i);
            builder.append(onTableName).append('.').append(leftColumn).append('=').append(tableAlias).append('.').append(rightColumn);
            if (i + 1 != onLeftColumns.size()) {
               builder.append(" AND ");
            }
         }

      }
   }

   private <T> List<T> merge(List<T> left, List<T> right) {
      if (left.isEmpty()) {
         return right;
      } else if (right.isEmpty()) {
         return left;
      } else {
         List<T> associations = new ArrayList(left.size() + right.size());
         associations.addAll(left);
         associations.addAll(right);
         return associations;
      }
   }

   @Override
   protected String quote(String persistedName) {
      switch(this.dialect) {
         case SQL_SERVER:
            return '[' + persistedName + ']';
         case ORACLE:
            return '"' + persistedName.toUpperCase(Locale.ENGLISH) + '"';
         case H2:
         case MYSQL:
            return '`' + persistedName + '`';
         default:
            return '"' + persistedName + '"';
      }
   }

   @Override
   public String getColumnName(PersistentProperty persistentProperty) {
      return persistentProperty.getPersistedName();
   }

   @Override
   protected void appendProjectionRowCount(StringBuilder queryString, String logicalName) {
      queryString.append("COUNT").append('(').append('*').append(')');
   }

   @Override
   protected void appendForUpdate(AbstractSqlLikeQueryBuilder.QueryPosition queryPosition, QueryModel query, StringBuilder queryBuilder) {
      if (query.isForUpdate()) {
         boolean isSqlServer = Dialect.SQL_SERVER.equals(this.dialect);
         if (isSqlServer && queryPosition.equals(AbstractSqlLikeQueryBuilder.QueryPosition.AFTER_TABLE_NAME)
            || !isSqlServer && queryPosition.equals(AbstractSqlLikeQueryBuilder.QueryPosition.END_OF_QUERY)) {
            queryBuilder.append(isSqlServer ? " WITH (UPDLOCK, ROWLOCK)" : " FOR UPDATE");
         }
      }

   }

   @Override
   protected final boolean computePropertyPaths() {
      return true;
   }

   @Override
   protected boolean isAliasForBatch() {
      return false;
   }

   @Override
   public AbstractSqlLikeQueryBuilder.Placeholder formatParameter(int index) {
      SqlQueryBuilder.DialectConfig dialectConfig = (SqlQueryBuilder.DialectConfig)this.perDialectConfig.get(this.dialect);
      return dialectConfig != null && dialectConfig.positionalFormatter != null
         ? new AbstractSqlLikeQueryBuilder.Placeholder(String.format(dialectConfig.positionalFormatter, index), String.valueOf(index))
         : new AbstractSqlLikeQueryBuilder.Placeholder("?", String.valueOf(index));
   }

   protected GeneratedValue.Type selectAutoStrategy(PersistentProperty property) {
      if (property.getDataType() == DataType.UUID) {
         return GeneratedValue.Type.UUID;
      } else {
         return this.dialect == Dialect.ORACLE ? GeneratedValue.Type.SEQUENCE : GeneratedValue.Type.AUTO;
      }
   }

   private String addTypeToColumn(PersistentProperty prop, String column, boolean required) {
      if (prop instanceof Association) {
         throw new IllegalStateException("Association is not supported here");
      } else {
         AnnotationMetadata annotationMetadata = prop.getAnnotationMetadata();
         String definition = (String)annotationMetadata.stringValue(MappedProperty.class, "definition").orElse(null);
         DataType dataType = prop.getDataType();
         if (definition != null) {
            return column + " " + definition;
         } else {
            OptionalInt precision = annotationMetadata.intValue("javax.persistence.Column", "precision");
            OptionalInt scale = annotationMetadata.intValue("javax.persistence.Column", "scale");
            switch(dataType) {
               case STRING:
                  int stringLength = ((OptionalInt)annotationMetadata.findAnnotation("javax.validation.constraints.Size$List")
                        .flatMap(v -> v.getValue(AnnotationValue.class))
                        .map(v -> v.intValue("max"))
                        .orElseGet(() -> annotationMetadata.intValue("javax.persistence.Column", "length")))
                     .orElse(255);
                  column = column + " VARCHAR(" + stringLength + ")";
                  if (required) {
                     column = column + " NOT NULL";
                  }
                  break;
               case UUID:
                  if (this.dialect == Dialect.ORACLE || this.dialect == Dialect.MYSQL) {
                     column = column + " VARCHAR(36)";
                  } else if (this.dialect == Dialect.SQL_SERVER) {
                     column = column + " UNIQUEIDENTIFIER";
                  } else {
                     column = column + " UUID";
                  }

                  if (required) {
                     column = column + " NOT NULL";
                  }
                  break;
               case BOOLEAN:
                  if (this.dialect == Dialect.ORACLE) {
                     column = column + " NUMBER(3)";
                  } else if (this.dialect == Dialect.SQL_SERVER) {
                     column = column + " BIT NOT NULL";
                  } else {
                     column = column + " BOOLEAN";
                     if (required) {
                        column = column + " NOT NULL";
                     }
                  }
                  break;
               case TIMESTAMP:
                  if (this.dialect == Dialect.ORACLE) {
                     column = column + " TIMESTAMP";
                     if (required) {
                        column = column + " NOT NULL";
                     }
                  } else if (this.dialect == Dialect.SQL_SERVER) {
                     column = column + " DATETIME2";
                     if (required) {
                        column = column + " NOT NULL";
                     }
                  } else if (this.dialect == Dialect.MYSQL) {
                     column = column + " TIMESTAMP(6) DEFAULT NOW(6)";
                  } else {
                     column = column + " TIMESTAMP";
                     if (required) {
                        column = column + " NOT NULL";
                     }
                  }
                  break;
               case DATE:
                  column = column + " DATE";
                  if (required) {
                     column = column + " NOT NULL";
                  }
                  break;
               case LONG:
                  if (this.dialect == Dialect.ORACLE) {
                     column = column + " NUMBER(19)";
                  } else {
                     column = column + " BIGINT";
                  }

                  if (required) {
                     column = column + " NOT NULL";
                  }
                  break;
               case CHARACTER:
                  column = column + " CHAR(1)";
                  if (required) {
                     column = column + " NOT NULL";
                  }
                  break;
               case INTEGER:
                  if (precision.isPresent()) {
                     String numericName = this.dialect == Dialect.ORACLE ? "NUMBER" : "NUMERIC";
                     column = column + " " + numericName + "(" + precision.getAsInt() + ")";
                  } else if (this.dialect == Dialect.ORACLE) {
                     column = column + " NUMBER(10)";
                  } else if (this.dialect == Dialect.POSTGRES) {
                     column = column + " INTEGER";
                  } else {
                     column = column + " INT";
                  }

                  if (required) {
                     column = column + " NOT NULL";
                  }
                  break;
               case BIGDECIMAL:
                  if (precision.isPresent()) {
                     if (scale.isPresent()) {
                        String numericName = this.dialect == Dialect.ORACLE ? "NUMBER" : "NUMERIC";
                        column = column + " " + numericName + "(" + precision.getAsInt() + "," + scale.getAsInt() + ")";
                     } else {
                        column = column + " FLOAT(" + precision.getAsInt() + ")";
                     }
                  } else if (this.dialect == Dialect.ORACLE) {
                     column = column + " FLOAT(126)";
                  } else {
                     column = column + " DECIMAL";
                  }

                  if (required) {
                     column = column + " NOT NULL";
                  }
                  break;
               case FLOAT:
                  if (precision.isPresent()) {
                     if (scale.isPresent()) {
                        String numericName = this.dialect == Dialect.ORACLE ? "NUMBER" : "NUMERIC";
                        column = column + " " + numericName + "(" + precision.getAsInt() + "," + scale.getAsInt() + ")";
                     } else {
                        column = column + " FLOAT(" + precision.getAsInt() + ")";
                     }
                  } else if (this.dialect == Dialect.ORACLE || this.dialect == Dialect.SQL_SERVER) {
                     column = column + " FLOAT(53)";
                  } else if (this.dialect == Dialect.POSTGRES) {
                     column = column + " REAL";
                  } else {
                     column = column + " FLOAT";
                  }

                  if (required) {
                     column = column + " NOT NULL";
                  }
                  break;
               case BYTE_ARRAY:
                  if (this.dialect == Dialect.POSTGRES) {
                     column = column + " BYTEA";
                  } else if (this.dialect == Dialect.SQL_SERVER) {
                     column = column + " VARBINARY(MAX)";
                  } else if (this.dialect == Dialect.ORACLE) {
                     column = column + " BLOB";
                  } else {
                     column = column + " BLOB";
                  }

                  if (required) {
                     column = column + " NOT NULL";
                  }
                  break;
               case DOUBLE:
                  if (precision.isPresent()) {
                     if (scale.isPresent()) {
                        String numericName = this.dialect == Dialect.ORACLE ? "NUMBER" : "NUMERIC";
                        column = column + " " + numericName + "(" + precision.getAsInt() + "," + scale.getAsInt() + ")";
                     } else {
                        column = column + " FLOAT(" + precision.getAsInt() + ")";
                     }
                  } else if (this.dialect == Dialect.ORACLE) {
                     column = column + " FLOAT(23)";
                  } else if (this.dialect != Dialect.MYSQL && this.dialect != Dialect.H2) {
                     column = column + " DOUBLE PRECISION";
                  } else {
                     column = column + " DOUBLE";
                  }

                  if (required) {
                     column = column + " NOT NULL";
                  }
                  break;
               case SHORT:
               case BYTE:
                  if (this.dialect == Dialect.ORACLE) {
                     column = column + " NUMBER(5)";
                  } else if (this.dialect == Dialect.POSTGRES) {
                     column = column + " SMALLINT";
                  } else {
                     column = column + " TINYINT";
                  }

                  if (required) {
                     column = column + " NOT NULL";
                  }
                  break;
               case JSON:
                  switch(this.dialect) {
                     case POSTGRES:
                        column = column + " JSONB";
                        break;
                     case SQL_SERVER:
                        column = column + " NVARCHAR(MAX)";
                        break;
                     case ORACLE:
                        column = column + " CLOB";
                        break;
                     default:
                        column = column + " JSON";
                  }

                  if (required) {
                     column = column + " NOT NULL";
                  }
                  break;
               case STRING_ARRAY:
               case CHARACTER_ARRAY:
                  if (this.dialect == Dialect.H2) {
                     column = column + " ARRAY";
                  } else {
                     column = column + " VARCHAR(255) ARRAY";
                  }

                  if (required) {
                     column = column + " NOT NULL";
                  }
                  break;
               case SHORT_ARRAY:
                  if (this.dialect == Dialect.H2) {
                     column = column + " ARRAY";
                  } else if (this.dialect == Dialect.POSTGRES) {
                     column = column + " SMALLINT ARRAY";
                  } else {
                     column = column + " TINYINT ARRAY";
                  }

                  if (required) {
                     column = column + " NOT NULL";
                  }
                  break;
               case INTEGER_ARRAY:
                  if (this.dialect == Dialect.H2) {
                     column = column + " ARRAY";
                  } else if (this.dialect == Dialect.POSTGRES) {
                     column = column + " INTEGER ARRAY";
                  } else {
                     column = column + " INT ARRAY";
                  }

                  if (required) {
                     column = column + " NOT NULL";
                  }
                  break;
               case LONG_ARRAY:
                  if (this.dialect == Dialect.H2) {
                     column = column + " ARRAY";
                  } else {
                     column = column + " BIGINT ARRAY";
                  }

                  if (required) {
                     column = column + " NOT NULL";
                  }
                  break;
               case FLOAT_ARRAY:
                  if (this.dialect == Dialect.H2) {
                     column = column + " ARRAY";
                  } else if (this.dialect == Dialect.POSTGRES) {
                     column = column + " REAL ARRAY";
                  } else {
                     column = column + " FLOAT ARRAY";
                  }

                  if (required) {
                     column = column + " NOT NULL";
                  }
                  break;
               case DOUBLE_ARRAY:
                  if (this.dialect == Dialect.H2) {
                     column = column + " ARRAY";
                  } else if (this.dialect == Dialect.POSTGRES) {
                     column = column + " DOUBLE PRECISION ARRAY";
                  } else {
                     column = column + " DOUBLE ARRAY";
                  }

                  if (required) {
                     column = column + " NOT NULL";
                  }
                  break;
               case BOOLEAN_ARRAY:
                  if (this.dialect == Dialect.H2) {
                     column = column + " ARRAY";
                  } else {
                     column = column + " BOOLEAN ARRAY";
                  }

                  if (required) {
                     column = column + " NOT NULL";
                  }
                  break;
               default:
                  if (prop.isEnum()) {
                     column = column + " VARCHAR(255)";
                     if (required) {
                        column = column + " NOT NULL";
                     }
                  } else if (prop.isAssignable(Clob.class)) {
                     if (this.dialect == Dialect.POSTGRES) {
                        column = column + " TEXT";
                     } else {
                        column = column + " CLOB";
                     }

                     if (required) {
                        column = column + " NOT NULL";
                     }
                  } else {
                     if (!prop.isAssignable(Blob.class)) {
                        throw new MappingException(
                           "Unable to create table column for property ["
                              + prop.getName()
                              + "] of entity ["
                              + prop.getOwner().getName()
                              + "] with unknown data type: "
                              + dataType
                        );
                     }

                     if (this.dialect == Dialect.POSTGRES) {
                        column = column + " BYTEA";
                     } else {
                        column = column + " BLOB";
                     }

                     if (required) {
                        column = column + " NOT NULL";
                     }
                  }
            }

            return column;
         }
      }
   }

   @Override
   public boolean supportsForUpdate() {
      return true;
   }

   @Override
   public Dialect dialect() {
      return this.dialect;
   }

   @Override
   public String positionalParameterFormat() {
      SqlQueryBuilder.DialectConfig dialectConfig = (SqlQueryBuilder.DialectConfig)this.perDialectConfig.get(this.dialect);
      return dialectConfig != null && dialectConfig.positionalFormatter != null ? dialectConfig.positionalFormatter : "?";
   }

   public Pattern positionalParameterPattern() {
      if (this.positionalParameterPattern == null) {
         String positionalParameterFormat = this.positionalParameterFormat();
         boolean messageFormat = positionalParameterFormat.endsWith("%s");
         if (messageFormat) {
            String pattern = positionalParameterFormat.substring(0, positionalParameterFormat.length() - 2);
            pattern = Pattern.quote(pattern) + "\\d";
            this.positionalParameterPattern = Pattern.compile(pattern);
         } else {
            this.positionalParameterPattern = Pattern.compile(Pattern.quote(positionalParameterFormat));
         }
      }

      return this.positionalParameterPattern;
   }

   @Override
   public boolean escapeQueries() {
      SqlQueryBuilder.DialectConfig dialectConfig = (SqlQueryBuilder.DialectConfig)this.perDialectConfig.get(this.dialect);
      return dialectConfig != null && dialectConfig.escapeQueries != null ? dialectConfig.escapeQueries : true;
   }

   public Class<? extends Annotation> annotationType() {
      return SqlQueryConfiguration.DialectConfiguration.class;
   }

   private static class DialectConfig {
      Boolean escapeQueries;
      String positionalFormatter;

      private DialectConfig() {
      }
   }

   private static class IndexConfiguration {
      AnnotationValue<?> index;
      String tableName;

      public IndexConfiguration(AnnotationValue<?> index, String tableName) {
         this.index = index;
         this.tableName = tableName;
      }
   }
}

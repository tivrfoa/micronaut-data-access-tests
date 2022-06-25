package io.micronaut.data.jdbc.config;

import io.micronaut.context.BeanLocator;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.context.exceptions.NoSuchBeanException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.model.query.builder.sql.SqlQueryBuilder;
import io.micronaut.data.model.runtime.RuntimeEntityRegistry;
import io.micronaut.data.runtime.config.DataSettings;
import io.micronaut.data.runtime.config.SchemaGenerate;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.transaction.jdbc.DelegatingDataSource;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Context
@Internal
public class SchemaGenerator {
   private final List<DataJdbcConfiguration> configurations;

   public SchemaGenerator(List<DataJdbcConfiguration> configurations) {
      this.configurations = configurations == null ? Collections.emptyList() : configurations;
   }

   @PostConstruct
   public void createSchema(BeanLocator beanLocator) {
      RuntimeEntityRegistry runtimeEntityRegistry = beanLocator.getBean(RuntimeEntityRegistry.class);

      for(DataJdbcConfiguration configuration : this.configurations) {
         Dialect dialect = configuration.getDialect();
         SchemaGenerate schemaGenerate = configuration.getSchemaGenerate();
         if (schemaGenerate != null && schemaGenerate != SchemaGenerate.NONE) {
            String name = configuration.getName();
            List<String> packages = configuration.getPackages();
            Collection<BeanIntrospection<Object>> introspections;
            if (CollectionUtils.isNotEmpty(packages)) {
               introspections = BeanIntrospector.SHARED.findIntrospections(MappedEntity.class, (String[])packages.toArray(new String[0]));
            } else {
               introspections = BeanIntrospector.SHARED.findIntrospections(MappedEntity.class);
            }

            PersistentEntity[] entities = (PersistentEntity[])introspections.stream()
               .filter(i -> !i.getBeanType().getName().contains("$"))
               .filter(i -> !Modifier.isAbstract(i.getBeanType().getModifiers()))
               .map(beanIntrospection -> runtimeEntityRegistry.getEntity(beanIntrospection.getBeanType()))
               .toArray(x$0 -> new PersistentEntity[x$0]);
            if (ArrayUtils.isNotEmpty(entities)) {
               DataSource dataSource = DelegatingDataSource.unwrapDataSource(beanLocator.getBean(DataSource.class, Qualifiers.byName(name)));

               try {
                  try {
                     Connection connection = dataSource.getConnection();
                     Throwable var13 = null;

                     try {
                        SqlQueryBuilder builder = new SqlQueryBuilder(dialect);
                        if (dialect.allowBatch() && configuration.isBatchGenerate()) {
                           switch(schemaGenerate) {
                              case CREATE_DROP:
                                 try {
                                    String sql = builder.buildBatchDropTableStatement(entities);
                                    if (DataSettings.QUERY_LOG.isDebugEnabled()) {
                                       DataSettings.QUERY_LOG.debug("Dropping Tables: \n{}", sql);
                                    }

                                    PreparedStatement ps = connection.prepareStatement(sql);
                                    Throwable var175 = null;

                                    try {
                                       ps.executeUpdate();
                                    } catch (Throwable var156) {
                                       var175 = var156;
                                       throw var156;
                                    } finally {
                                       if (ps != null) {
                                          if (var175 != null) {
                                             try {
                                                ps.close();
                                             } catch (Throwable var155) {
                                                var175.addSuppressed(var155);
                                             }
                                          } else {
                                             ps.close();
                                          }
                                       }

                                    }
                                 } catch (SQLException var159) {
                                    if (DataSettings.QUERY_LOG.isTraceEnabled()) {
                                       DataSettings.QUERY_LOG.trace("Drop Unsuccessful: " + var159.getMessage());
                                    }
                                 }
                              case CREATE:
                                 String sql = builder.buildBatchCreateTableStatement(entities);
                                 if (DataSettings.QUERY_LOG.isDebugEnabled()) {
                                    DataSettings.QUERY_LOG.debug("Creating Tables: \n{}", sql);
                                 }

                                 PreparedStatement ps = connection.prepareStatement(sql);
                                 Throwable var176 = null;

                                 try {
                                    ps.executeUpdate();
                                 } catch (Throwable var154) {
                                    var176 = var154;
                                    throw var154;
                                 } finally {
                                    if (ps != null) {
                                       if (var176 != null) {
                                          try {
                                             ps.close();
                                          } catch (Throwable var149) {
                                             var176.addSuppressed(var149);
                                          }
                                       } else {
                                          ps.close();
                                       }
                                    }

                                 }
                           }
                        } else {
                           switch(schemaGenerate) {
                              case CREATE_DROP:
                                 for(PersistentEntity entity : entities) {
                                    try {
                                       String[] statements = builder.buildDropTableStatements(entity);

                                       for(String sql : statements) {
                                          if (DataSettings.QUERY_LOG.isDebugEnabled()) {
                                             DataSettings.QUERY_LOG.debug("Dropping Table: \n{}", sql);
                                          }

                                          PreparedStatement ps = connection.prepareStatement(sql);
                                          Throwable var25 = null;

                                          try {
                                             ps.executeUpdate();
                                          } catch (Throwable var153) {
                                             var25 = var153;
                                             throw var153;
                                          } finally {
                                             if (ps != null) {
                                                if (var25 != null) {
                                                   try {
                                                      ps.close();
                                                   } catch (Throwable var152) {
                                                      var25.addSuppressed(var152);
                                                   }
                                                } else {
                                                   ps.close();
                                                }
                                             }

                                          }
                                       }
                                    } catch (SQLException var163) {
                                       if (DataSettings.QUERY_LOG.isTraceEnabled()) {
                                          DataSettings.QUERY_LOG.trace("Drop Unsuccessful: " + var163.getMessage());
                                       }
                                    }
                                 }
                              case CREATE:
                                 for(PersistentEntity entity : entities) {
                                    String[] sql = builder.buildCreateTableStatements(entity);

                                    for(String stmt : sql) {
                                       if (DataSettings.QUERY_LOG.isDebugEnabled()) {
                                          DataSettings.QUERY_LOG.debug("Executing CREATE statement: \n{}", stmt);
                                       }

                                       try {
                                          PreparedStatement ps = connection.prepareStatement(stmt);
                                          Throwable var184 = null;

                                          try {
                                             ps.executeUpdate();
                                          } catch (Throwable var151) {
                                             var184 = var151;
                                             throw var151;
                                          } finally {
                                             if (ps != null) {
                                                if (var184 != null) {
                                                   try {
                                                      ps.close();
                                                   } catch (Throwable var150) {
                                                      var184.addSuppressed(var150);
                                                   }
                                                } else {
                                                   ps.close();
                                                }
                                             }

                                          }
                                       } catch (SQLException var161) {
                                          if (DataSettings.QUERY_LOG.isWarnEnabled()) {
                                             DataSettings.QUERY_LOG.warn("CREATE Statement Unsuccessful: " + var161.getMessage());
                                          }
                                       }
                                    }
                                 }
                           }
                        }
                     } catch (Throwable var164) {
                        var13 = var164;
                        throw var164;
                     } finally {
                        if (connection != null) {
                           if (var13 != null) {
                              try {
                                 connection.close();
                              } catch (Throwable var148) {
                                 var13.addSuppressed(var148);
                              }
                           } else {
                              connection.close();
                           }
                        }

                     }
                  } catch (SQLException var166) {
                     throw new DataAccessException("Unable to create database schema: " + var166.getMessage(), var166);
                  }
               } catch (NoSuchBeanException var167) {
                  throw new ConfigurationException(
                     "No DataSource configured for setting [datasources" + name + "]. Ensure the DataSource is configured correctly and try again.", var167
                  );
               }
            }
         }
      }

   }
}

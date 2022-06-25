## Micronaut 3.5.2 Documentation

- [User Guide](https://docs.micronaut.io/3.5.2/guide/index.html)
- [API Reference](https://docs.micronaut.io/3.5.2/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/3.5.2/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)
---

## Feature flyway documentation

- [Micronaut Flyway Database Migration documentation](https://micronaut-projects.github.io/micronaut-flyway/latest/guide/index.html)

- [https://flywaydb.org/](https://flywaydb.org/)


## Feature jdbc-hikari documentation

- [Micronaut Hikari JDBC Connection Pool documentation](https://micronaut-projects.github.io/micronaut-sql/latest/guide/index.html#jdbc)


## Feature http-client documentation

- [Micronaut HTTP Client documentation](https://docs.micronaut.io/latest/guide/index.html#httpClient)


## Feature testcontainers documentation

- [https://www.testcontainers.org/](https://www.testcontainers.org/)


## Feature data-jdbc documentation

- [Micronaut Data JDBC documentation](https://micronaut-projects.github.io/micronaut-data/latest/guide/index.html#jdbc)


## MySQL Docker Image

```
[main] INFO  🐳 [mysql:8] - Pulling docker image: mysql:8. Please be patient; this may take some time but only needs to be done once.
```

## Test Database ?!

```
09:45:48.834 [main] INFO  o.f.c.i.d.base.BaseDatabaseType - Database: jdbc:mysql://localhost:49154/test (MySQL 8.0)
09:45:48.919 [main] INFO  o.f.core.internal.command.DbValidate - Successfully validated 1 migration (execution time 00:00.029s)
09:45:48.965 [main] INFO  o.f.c.i.s.JdbcTableSchemaHistory - Creating Schema History table `test`.`flyway_schema_history` ...
09:45:49.069 [main] INFO  o.f.core.internal.command.DbMigrate - Current version of schema `test`: << Empty Schema >>
09:45:49.086 [main] INFO  o.f.core.internal.command.DbMigrate - Migrating schema `test` to version "1 - schema"
09:45:49.111 [main] WARN  o.f.c.i.s.DefaultSqlScriptExecutor - DB: Unknown table 'test.genre' (SQL State: 42S02 - Error Code: 1051)
09:45:49.183 [main] INFO  o.f.core.internal.command.DbMigrate - Successfully applied 1 migration to schema `test`, now at version v1 (execution time 00:00.125s)
09:45:49.838 [main] INFO  com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Shutdown initiated...
09:45:50.274 [main] INFO  com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Shutdown completed.
```

## Errors and Warnings

```
09:43:56.692 [main] WARN  o.t.u.TestcontainersConfiguration - Attempted to read Testcontainers configuration file at file:/home/lesco/.testcontainers.properties but the file was not found. Exception message: FileNotFoundException: /home/lesco/.testcontainers.properties (No such file or directory)
```

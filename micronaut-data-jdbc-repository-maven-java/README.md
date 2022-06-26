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

## Testing

```
curl -X "POST" "http://localhost:8080/genres" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{ "name": "music" }'
```


```
 __  __ _                                  _
|  \/  (_) ___ _ __ ___  _ __   __ _ _   _| |_
| |\/| | |/ __| '__/ _ \| '_ \ / _` | | | | __|
| |  | | | (__| | | (_) | | | | (_| | |_| | |_
|_|  |_|_|\___|_|  \___/|_| |_|\__,_|\__,_|\__|
  Micronaut (v3.5.2)

11:37:02.879 [main] INFO  com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Starting...
11:37:03.599 [main] INFO  com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Start completed.
11:37:03.669 [main] INFO  i.m.flyway.AbstractFlywayMigration - Running migrations for database with qualifier [default]
11:37:03.675 [main] INFO  o.f.c.i.license.VersionPrinter - Flyway Community Edition 8.5.8 by Redgate
11:37:03.676 [main] INFO  o.f.c.i.license.VersionPrinter - See what's new here: https://flywaydb.org/documentation/learnmore/releaseNotes#8.5.8
11:37:03.676 [main] INFO  o.f.c.i.license.VersionPrinter -
11:37:03.742 [main] INFO  o.f.c.i.d.base.BaseDatabaseType - Database: jdbc:mysql://localhost:3306/micronaut (MySQL 8.0)
11:37:03.866 [main] INFO  o.f.core.internal.command.DbValidate - Successfully validated 1 migration (execution time 00:00.033s)
11:37:03.910 [main] INFO  o.f.c.i.s.JdbcTableSchemaHistory - Creating Schema History table `micronaut`.`flyway_schema_history` ...
11:37:04.049 [main] INFO  o.f.core.internal.command.DbMigrate - Current version of schema `micronaut`: << Empty Schema >>
11:37:04.062 [main] INFO  o.f.core.internal.command.DbMigrate - Migrating schema `micronaut` to version "1 - schema"
11:37:04.082 [main] WARN  o.f.c.i.s.DefaultSqlScriptExecutor - DB: Unknown table 'micronaut.genre' (SQL State: 42S02 - Error Code: 1051)
11:37:04.165 [main] INFO  o.f.core.internal.command.DbMigrate - Successfully applied 1 migration to schema `micronaut`, now at version v1 (execution time 00:00.128s)
11:37:04.225 [main] DEBUG io.micronaut.data.query - Dropping Table:
DROP TABLE `genre`
11:37:04.254 [main] DEBUG io.micronaut.data.query - Executing CREATE statement:
CREATE TABLE `genre` (`id` BIGINT PRIMARY KEY AUTO_INCREMENT,`name` VARCHAR(255) NOT NULL);
11:37:04.912 [main] INFO  io.micronaut.runtime.Micronaut - Startup completed in 3145ms. Server Running: http://localhost:8080
11:37:52.000 [io-executor-thread-2] DEBUG io.micronaut.data.query - Executing SQL query: INSERT INTO `genre` (`name`) VALUES (?)
11:37:52.001 [io-executor-thread-2] TRACE io.micronaut.data.query - Binding parameter at position 1 to value music with data type: STRING
```

### How Querying works

```
curl localhost:8080/genres/list
[{"id":1,"name":"music"},{"id":2,"name":"ok"}]
```

```
[io-executor-thread-2] DEBUG io.micronaut.data.query - Executing Query: SELECT genre_.`id`,genre_.`name` FROM `genre` genre_ LIMIT 100
[io-executor-thread-2] DEBUG io.micronaut.data.query - Executing Query: SELECT COUNT(*) FROM `genre` genre_
```

```
mysql> select * from genre;
+----+-------+
| id | name  |
+----+-------+
|  1 | music |
|  2 | ok    |
+----+-------+
```

## Annotation Processor FTW!!!

```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.10.1:compile (default-compile) on project demo: Compilation failure
[ERROR] /home/lesco/dev/Java/micronaut/data-access/micronaut-data-jdbc-repository-maven-java/src/main/java/com/example/GenreRepository.java:[27,10] Unable to implement Repository method: GenreRepository.update(Long id,double value1). Cannot update non-existent property: value1
```

## Updating

### Creating some data

```shell
curl -X "POST" "http://localhost:8080/genres" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{ "name": "music", "value": 10, "country": "Brazil" }'
```

```shell
http localhost:8080/genres/list
```

```json
[
    {
        "country": "Brazil",
        "id": 1,
        "name": "music",
        "value": 10.0
    }
]
```

### Without id

```shell
curl -X "PUT" "http://localhost:8080/genres" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{ "name": "movie", "value": 3.5 }'
```

It does nothing, because id is null and there is no record in the database for it.

```txt
[io-executor-thread-8] DEBUG io.micronaut.data.query - Executing SQL query: UPDATE `genre` SET `name`=?,`value`=?,`country`=? WHERE (`id` = ?)
[io-executor-thread-8] TRACE io.micronaut.data.query - Binding parameter at position 1 to value movie with data type: STRING
[io-executor-thread-8] TRACE io.micronaut.data.query - Binding parameter at position 2 to value 3.5 with data type: DOUBLE
[io-executor-thread-8] TRACE io.micronaut.data.query - Binding parameter at position 3 to value null with data type: STRING
[io-executor-thread-8] TRACE io.micronaut.data.query - Binding parameter at position 4 to value null with data type: LONG
```

### With id

```shell
curl -X "PUT" "http://localhost:8080/genres" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{ "id": 1, "name": "movie", "value": 3.5 }'

Caused by: java.sql.SQLIntegrityConstraintViolationException: Column 'country' cannot be null
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:117)
```

### Update just value

```shell
curl -X "PUT" "http://localhost:8080/genres" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{ "id": 1, "value": 3.5 }'
```

```json
{
  "message": "Bad Request",
  "_links": {
    "self": {
      "href": "/genres",
      "templated": false
    }
  },
  "_embedded": {
    "errors": [
      {
        "message": "genre.name: must not be null"
      }
    ]
  }
}
```
## Using MapResultSet with Micronaut!!!

```shell
http localhost:8080/genres/listGenres
```

```json
[
    {
        "country": "Brazil",
        "id": 1,
        "name": "music",
        "value": 10.0
    }
]
```

## Testing Join/Relationships

http :8080/person/list

```shell
curl -X "POST" "http://localhost:8080/person"      -H 'Content-Type: application/json; charset=Ttf-8'      -d $'{ "name": "Leandro", "bornTimestamp": "2022-06-26T17:05:49", "wakeUpTime": "07:02:35" }'
```

```txt
[io-executor-thread-2] DEBUG io.micronaut.data.query - Executing SQL query: INSERT INTO `person` (`name`,`born_timestamp`,`wakeup_time`,`id`) VALUES (?,?,?,?)
[io-executor-thread-2] TRACE io.micronaut.data.query - Binding parameter at position 1 to value Leandro with data type: STRING
[io-executor-thread-2] TRACE io.micronaut.data.query - Binding parameter at position 2 to value 2022-06-26 14:05:49.0 with data type: TIMESTAMP
[io-executor-thread-2] TRACE io.micronaut.data.query - Binding parameter at position 3 to value 07:02:35 with data type: DATE
[io-executor-thread-2] TRACE io.micronaut.data.query - Binding parameter at position 4 to value 0 with data type: INTEGER
```

http :8080/person/list

```txt
[io-executor-thread-2] DEBUG io.micronaut.data.query - Executing Query: SELECT person_.`id`,person_.`name`,person_.`born_timestamp`,person_.`wakeup_time` FROM `person` person_ LIMIT 100
[io-executor-thread-1] ERROR i.m.http.server.RouteExecutor - Unexpected error occurred: Specified value [Thu Jan 01 00:00:00 BRT 1970] is not of the correct type: class java.sql.Time

java.lang.IllegalArgumentException: Specified value [Thu Jan 01 00:00:00 BRT 1970] is not of the correct type: class java.sql.Time
	at io.micronaut.inject.beans.AbstractInitializableBeanIntrospection$BeanPropertyImpl.set(AbstractInitializableBeanIntrospection.java:446)
	at io.micronaut.data.runtime.mapper.sql.SqlResultEntityTypeMapper.setProperty(SqlResultEntityTypeMapper.java:389)
	at io.micronaut.data.runtime.mapper.sql.SqlResultEntityTypeMapper.convertAndSetWithValue(SqlResultEntityTypeMapper.java:606)
	at io.micronaut.data.runtime.mapper.sql.SqlResultEntityTypeMapper.readEntity(SqlResultEntityTypeMapper.java:560)
	at io.micronaut.data.runtime.mapper.sql.SqlResultEntityTypeMapper.map(SqlResultEntityTypeMapper.java:195)
	at io.micronaut.data.jdbc.operations.DefaultJdbcRepositoryOperations$1.tryAdvance(DefaultJdbcRepositoryOperations.java:458)
	at java.base/java.util.Spliterator.forEachRemaining(Spliterator.java:332)
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509)
	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:499)
	at java.base/java.util.stream.ReduceOps$ReduceOp.evaluateSequential(ReduceOps.java:921)
	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
	at java.base/java.util.stream.ReferencePipeline.collect(ReferencePipeline.java:682)
	at io.micronaut.data.jdbc.operations.DefaultJdbcRepositoryOperations.lambda$findAll$6(DefaultJdbcRepositoryOperations.java:531)
	at io.micronaut.data.jdbc.operations.DefaultJdbcRepositoryOperations.lambda$executeRead$18(DefaultJdbcRepositoryOperations.java:709)
	at io.micronaut.transaction.support.AbstractSynchronousStateTransactionManager.execute(AbstractSynchronousStateTransactionManager.java:146)
	at io.micronaut.transaction.support.AbstractSynchronousStateTransactionManager.executeRead(AbstractSynchronousStateTransactionManager.java:162)
	at io.micronaut.transaction.support.AbstractSynchronousTransactionManager.executeRead(AbstractSynchronousTransactionManager.java:133)
	at io.micronaut.data.jdbc.operations.DefaultJdbcRepositoryOperations.executeRead(DefaultJdbcRepositoryOperations.java:709)
	at io.micronaut.data.jdbc.operations.DefaultJdbcRepositoryOperations.findAll(DefaultJdbcRepositoryOperations.java:529)
	at io.micronaut.data.runtime.intercept.DefaultFindPageInterceptor.intercept(DefaultFindPageInterceptor.java:55)
	at io.micronaut.data.intercept.DataIntroductionAdvice.intercept(DataIntroductionAdvice.java:135)
	at io.micronaut.data.intercept.DataIntroductionAdvice.intercept(DataIntroductionAdvice.java:98)
	at io.micronaut.aop.chain.MethodInterceptorChain.proceed(MethodInterceptorChain.java:137)
	at io.micronaut.validation.ValidatingInterceptor.intercept(ValidatingInterceptor.java:143)
	at io.micronaut.aop.chain.MethodInterceptorChain.proceed(MethodInterceptorChain.java:137)
	at com.example.PersonRepository$Intercepted.findAll(Unknown Source)
	at com.example.PersonController.list(PersonController.java:37)
	at com.example.$PersonController$Definition$Intercepted.$$access$$list(Unknown Source)
	at com.example.$PersonController$Definition$Exec.dispatch(Unknown Source)
	at io.micronaut.context.AbstractExecutableMethodsDefinition$DispatchedExecutableMethod.invoke(AbstractExecutableMethodsDefinition.java:378)
	at io.micronaut.aop.chain.MethodInterceptorChain.proceed(MethodInterceptorChain.java:128)
	at io.micronaut.validation.ValidatingInterceptor.intercept(ValidatingInterceptor.java:143)
	at io.micronaut.aop.chain.MethodInterceptorChain.proceed(MethodInterceptorChain.java:137)
	at com.example.$PersonController$Definition$Intercepted.list(Unknown Source)
	at com.example.$PersonController$Definition$Exec.dispatch(Unknown Source)
	at io.micronaut.context.AbstractExecutableMethodsDefinition$DispatchedExecutableMethod.invoke(AbstractExecutableMethodsDefinition.java:378)
	at io.micronaut.context.DefaultBeanContext$4.invoke(DefaultBeanContext.java:592)
	at io.micronaut.web.router.AbstractRouteMatch.execute(AbstractRouteMatch.java:303)
	at io.micronaut.web.router.RouteMatch.execute(RouteMatch.java:111)
	at io.micronaut.http.context.ServerRequestContext.with(ServerRequestContext.java:103)
	at io.micronaut.http.server.RouteExecutor.lambda$executeRoute$14(RouteExecutor.java:659)
```

### I made id in person auto_increment

```shell
curl -X "POST" "http://localhost:8080/person"      -H 'Content-Type: application/json; charset=Ttf-8'      -d $'{ "name": "Leandro", "bornTimestamp": "2022-06-26T17:05:49", "wakeUpTime": "07:02:35" }'
```

http :8080/person/list


package io.micronaut.transaction.jdbc;

import io.micronaut.context.annotation.EachBean;
import io.micronaut.core.annotation.Internal;
import java.sql.Connection;
import javax.sql.DataSource;

@EachBean(DataSource.class)
@TransactionalConnectionAdvice
@Internal
public interface TransactionalConnection extends Connection {
}

package io.micronaut.data.jdbc.convert;

import io.micronaut.core.convert.ConversionContext;
import java.sql.Connection;

public interface JdbcConversionContext extends ConversionContext {
   Connection getConnection();
}

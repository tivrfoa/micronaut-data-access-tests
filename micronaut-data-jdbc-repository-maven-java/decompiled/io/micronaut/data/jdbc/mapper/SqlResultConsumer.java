package io.micronaut.data.jdbc.mapper;

import io.micronaut.data.runtime.mapper.ResultConsumer;
import java.sql.ResultSet;

@FunctionalInterface
public interface SqlResultConsumer<T> extends ResultConsumer<T, ResultSet> {
   String ROLE = "sqlMappingFunction";
}

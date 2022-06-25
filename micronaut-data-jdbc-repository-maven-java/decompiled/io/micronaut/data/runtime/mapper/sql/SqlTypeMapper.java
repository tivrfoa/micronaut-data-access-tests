package io.micronaut.data.runtime.mapper.sql;

import io.micronaut.data.runtime.mapper.TypeMapper;

public interface SqlTypeMapper<RS, R> extends TypeMapper<RS, R> {
   boolean hasNext(RS resultSet);
}

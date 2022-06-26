package com.example;

import java.sql.Timestamp;
import java.util.List;

import javax.validation.constraints.NotBlank;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.PageableRepository;

@JdbcRepository(dialect = Dialect.MYSQL) 
public interface PersonRepository extends PageableRepository<Person, Long> {

    Person save(@NonNull @NotBlank String name, Timestamp bornTimestamp);

    Person update(Person person);

    @Join(value = "addresses", type = Join.Type.FETCH)
    @Join(value = "phones", type = Join.Type.FETCH)
    List<Person> list();
}
package com.example;

import java.sql.ResultSet;
import java.util.List;

import javax.transaction.Transactional;

import io.github.tivrfoa.mapresultset.api.Query;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.jdbc.runtime.JdbcOperations;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

@JdbcRepository(dialect = Dialect.MYSQL) 
public abstract class GenreDao implements CrudRepository<Genre, Long> {

    @Query
    final String listGenres = """
            select id, name, value, country
            from genre
            """;

    private final JdbcOperations jdbcOperations;

    public GenreDao(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Transactional
    public List<Genre> listGenres() {
        return jdbcOperations.prepareStatement(listGenres, statement -> {
            ResultSet resultSet = statement.executeQuery(listGenres);
            return MapResultSet.listGenres(resultSet);
        });
    }
}

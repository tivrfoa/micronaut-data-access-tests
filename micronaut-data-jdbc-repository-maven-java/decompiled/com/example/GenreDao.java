package com.example;

import com.github.tivrfoa.mapresultset.api.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.jdbc.runtime.JdbcOperations;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.sql.ResultSet;
import java.util.List;
import javax.transaction.Transactional;

@JdbcRepository(
   dialect = Dialect.MYSQL
)
public abstract class GenreDao implements CrudRepository<Genre, Long> {
   @Query
   final String listGenres = "select id, name, value, country\nfrom genre\n";
   private final JdbcOperations jdbcOperations;

   public GenreDao(JdbcOperations jdbcOperations) {
      this.jdbcOperations = jdbcOperations;
   }

   @Transactional
   public List<Genre> listGenres() {
      return this.jdbcOperations.prepareStatement("select id, name, value, country\nfrom genre\n", statement -> {
         ResultSet resultSet = statement.executeQuery("select id, name, value, country\nfrom genre\n");
         return MapResultSet.listGenres(resultSet);
      });
   }
}

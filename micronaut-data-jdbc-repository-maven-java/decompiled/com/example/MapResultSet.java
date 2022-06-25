package com.example;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MapResultSet {
   public static ListGenresRecords listGenres(ResultSet rs) throws SQLException {
      ListGenresRecords records = new ListGenresRecords();

      while(rs.next()) {
         Genre obj1 = new Genre();
         obj1.setCountry(rs.getString("country"));
         obj1.setName(rs.getString("name"));
         obj1.setId(rs.getLong("id"));
         obj1.setValue(rs.getDouble("value"));
         records.getListGenre().add(obj1);
      }

      return records;
   }
}

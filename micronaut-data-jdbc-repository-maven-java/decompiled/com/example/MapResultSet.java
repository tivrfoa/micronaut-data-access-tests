package com.example;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MapResultSet {
   public static List<Genre> listGenres(ResultSet rs) throws SQLException {
      List<Genre> list = new ArrayList();

      while(rs.next()) {
         Genre obj0 = new Genre();
         obj0.setCountry(rs.getString("country"));
         obj0.setName(rs.getString("name"));
         obj0.setId(rs.getLong("id"));
         obj0.setValue(rs.getDouble("value"));
         list.add(obj0);
      }

      return list;
   }
}

package org.flywaydb.core.internal.util;

import java.util.ArrayList;
import java.util.List;

public class AsciiTable {
   private static final String DEFAULT_COLUMN_NAME = "(No column name)";
   private final List<String> columns;
   private final List<List<String>> rows;
   private final boolean printHeader;
   private final String nullText;
   private final String emptyText;

   public AsciiTable(List<String> columns, List<List<String>> rows, boolean printHeader, String nullText, String emptyText) {
      this.columns = ensureValidColumns(columns);
      this.rows = rows;
      this.printHeader = printHeader;
      this.nullText = nullText;
      this.emptyText = emptyText;
   }

   private static List<String> ensureValidColumns(List<String> columns) {
      List<String> validColumns = new ArrayList();

      for(String column : columns) {
         validColumns.add(column != null ? column : "(No column name)");
      }

      return validColumns;
   }

   public String render() {
      List<Integer> widths = new ArrayList();

      for(String column : this.columns) {
         widths.add(column.length());
      }

      for(List<String> row : this.rows) {
         for(int i = 0; i < row.size(); ++i) {
            widths.set(i, Math.max(widths.get(i), this.getValue(row, i).length()));
         }
      }

      StringBuilder ruler = new StringBuilder("+");

      for(Integer width : widths) {
         ruler.append("-").append(StringUtils.trimOrPad("", width, '-')).append("-+");
      }

      ruler.append("\n");
      StringBuilder result = new StringBuilder();
      if (this.printHeader) {
         StringBuilder header = new StringBuilder("|");

         for(int i = 0; i < widths.size(); ++i) {
            header.append(" ").append(StringUtils.trimOrPad((String)this.columns.get(i), widths.get(i), ' ')).append(" |");
         }

         header.append("\n");
         result.append(ruler);
         result.append(header);
      }

      result.append(ruler);
      if (this.rows.isEmpty()) {
         result.append("| ").append(StringUtils.trimOrPad(this.emptyText, ruler.length() - 5)).append(" |\n");
      } else {
         for(List<String> row : this.rows) {
            StringBuilder r = new StringBuilder("|");

            for(int i = 0; i < widths.size(); ++i) {
               r.append(" ").append(StringUtils.trimOrPad(this.getValue(row, i), widths.get(i), ' ')).append(" |");
            }

            r.append("\n");
            result.append(r);
         }
      }

      result.append(ruler);
      return result.toString();
   }

   private String getValue(List<String> row, int i) {
      String value = (String)row.get(i);
      if (value == null) {
         value = this.nullText;
      }

      return value;
   }
}

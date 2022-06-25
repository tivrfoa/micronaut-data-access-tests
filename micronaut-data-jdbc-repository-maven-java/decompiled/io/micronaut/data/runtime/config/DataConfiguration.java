package io.micronaut.data.runtime.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.util.StringUtils;
import java.util.regex.Pattern;

@ConfigurationProperties("micronaut.data")
public class DataConfiguration implements DataSettings {
   @ConfigurationProperties("pageable")
   public static class PageableConfiguration {
      public static final int DEFAULT_MAX_PAGE_SIZE = 100;
      public static final boolean DEFAULT_SORT_IGNORE_CASE = false;
      public static final String DEFAULT_SORT_PARAMETER = "sort";
      public static final String DEFAULT_SIZE_PARAMETER = "size";
      public static final String DEFAULT_PAGE_PARAMETER = "page";
      public static final String PREFIX = "pageable";
      private int maxPageSize = 100;
      private Integer defaultPageSize = null;
      private boolean sortIgnoreCase = false;
      private String sortParameterName = "sort";
      private String sizeParameterName = "size";
      private String pageParameterName = "page";
      private Pattern sortDelimiter = Pattern.compile(",");

      public boolean isSortIgnoreCase() {
         return this.sortIgnoreCase;
      }

      public void setSortIgnoreCase(boolean sortIgnoreCase) {
         this.sortIgnoreCase = sortIgnoreCase;
      }

      public Pattern getSortDelimiterPattern() {
         return this.sortDelimiter;
      }

      public void setSortDelimiter(String sortDelimiter) {
         if (StringUtils.isNotEmpty(sortDelimiter)) {
            this.sortDelimiter = Pattern.compile(Pattern.quote(sortDelimiter));
         }

      }

      public int getMaxPageSize() {
         return this.maxPageSize;
      }

      public void setMaxPageSize(int maxPageSize) {
         this.maxPageSize = maxPageSize;
      }

      public int getDefaultPageSize() {
         return this.defaultPageSize == null ? this.maxPageSize : this.defaultPageSize;
      }

      public void setDefaultPageSize(int defaultPageSize) {
         this.defaultPageSize = defaultPageSize;
      }

      public String getSortParameterName() {
         return this.sortParameterName;
      }

      public void setSortParameterName(String sortParameterName) {
         if (StringUtils.isNotEmpty(sortParameterName)) {
            this.sortParameterName = sortParameterName;
         }

      }

      public String getSizeParameterName() {
         return this.sizeParameterName;
      }

      public void setSizeParameterName(String sizeParameterName) {
         if (StringUtils.isNotEmpty(sizeParameterName)) {
            this.sizeParameterName = sizeParameterName;
         }

      }

      public String getPageParameterName() {
         return this.pageParameterName;
      }

      public void setPageParameterName(String pageParameterName) {
         if (StringUtils.isNotEmpty(this.sizeParameterName)) {
            this.pageParameterName = pageParameterName;
         }

      }
   }
}

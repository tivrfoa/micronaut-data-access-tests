package org.flywaydb.core.api;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Location implements Comparable<Location> {
   private static final String CLASSPATH_PREFIX = "classpath:";
   public static final String FILESYSTEM_PREFIX = "filesystem:";
   private static final String AWS_S3_PREFIX = "s3:";
   private static final String GCS_PREFIX = "gcs:";
   private final String prefix;
   private String rawPath;
   private String rootPath;
   private Pattern pathRegex = null;

   public Location(String descriptor) {
      String normalizedDescriptor = descriptor.trim();
      if (normalizedDescriptor.contains(":")) {
         this.prefix = normalizedDescriptor.substring(0, normalizedDescriptor.indexOf(":") + 1);
         this.rawPath = normalizedDescriptor.substring(normalizedDescriptor.indexOf(":") + 1);
      } else {
         this.prefix = "classpath:";
         this.rawPath = normalizedDescriptor;
      }

      if (this.isClassPath()) {
         if (this.rawPath.startsWith("/")) {
            this.rawPath = this.rawPath.substring(1);
         }

         if (this.rawPath.endsWith("/")) {
            this.rawPath = this.rawPath.substring(0, this.rawPath.length() - 1);
         }

         this.processRawPath();
      } else if (this.isFileSystem()) {
         this.processRawPath();
         this.rootPath = new File(this.rootPath).getPath();
         if (this.pathRegex == null) {
            this.rawPath = new File(this.rawPath).getPath();
         }
      } else if (!this.isAwsS3() && !this.isGCS()) {
         throw new FlywayException("Unknown prefix for location (should be one of filesystem:, classpath:, gcs:, or s3:): " + normalizedDescriptor);
      }

      if (this.rawPath.endsWith(File.separator)) {
         this.rawPath = this.rawPath.substring(0, this.rawPath.length() - 1);
      }

   }

   private void processRawPath() {
      if (!this.rawPath.contains("*") && !this.rawPath.contains("?")) {
         this.rootPath = this.rawPath;
      } else {
         String separator = this.isFileSystem() ? File.separator : "/";
         String escapedSeparator = separator.replace("\\", "\\\\").replace("/", "\\/");
         String[] pathSplit = this.rawPath.split("[\\\\/]");
         StringBuilder rootPart = new StringBuilder();
         StringBuilder patternPart = new StringBuilder();
         boolean endsInFile = false;
         boolean skipSeparator = false;
         boolean inPattern = false;

         for(String pathPart : pathSplit) {
            endsInFile = false;
            if (pathPart.contains("*") || pathPart.contains("?")) {
               inPattern = true;
            }

            if (inPattern) {
               if (skipSeparator) {
                  skipSeparator = false;
               } else {
                  patternPart.append("/");
               }

               String regex;
               if ("**".equals(pathPart)) {
                  regex = "([^/]+/)*?";
                  skipSeparator = true;
               } else {
                  endsInFile = pathPart.contains(".");
                  regex = pathPart.replace(".", "\\.");
                  regex = regex.replace("?", "[^/]");
                  regex = regex.replace("*", "[^/]+?");
               }

               patternPart.append(regex);
            } else {
               rootPart.append(separator).append(pathPart);
            }
         }

         this.rootPath = rootPart.length() > 0 ? rootPart.substring(1) : "";
         String pattern = patternPart.substring(1);
         pattern = pattern.replace("/", escapedSeparator);
         if (rootPart.length() > 0) {
            pattern = this.rootPath.replace(separator, escapedSeparator) + escapedSeparator + pattern;
         }

         if (!endsInFile) {
            pattern = pattern + escapedSeparator + "(?<relpath>.*)";
         }

         this.pathRegex = Pattern.compile(pattern);
      }

   }

   public boolean matchesPath(String path) {
      return this.pathRegex == null ? true : this.pathRegex.matcher(path).matches();
   }

   public String getPathRelativeToThis(String path) {
      if (this.pathRegex != null && this.pathRegex.pattern().contains("?<relpath>")) {
         Matcher matcher = this.pathRegex.matcher(path);
         if (matcher.matches()) {
            String relPath = matcher.group("relpath");
            if (relPath != null && relPath.length() > 0) {
               return relPath;
            }
         }
      }

      return this.rootPath.length() > 0 ? path.substring(this.rootPath.length() + 1) : path;
   }

   public boolean isClassPath() {
      return "classpath:".equals(this.prefix);
   }

   public boolean isFileSystem() {
      return "filesystem:".equals(this.prefix);
   }

   public boolean isAwsS3() {
      return "s3:".equals(this.prefix);
   }

   public boolean isGCS() {
      return "gcs:".equals(this.prefix);
   }

   public boolean isParentOf(Location other) {
      if (this.pathRegex != null || other.pathRegex != null) {
         return false;
      } else if (this.isClassPath() && other.isClassPath()) {
         return (other.getDescriptor() + "/").startsWith(this.getDescriptor() + "/");
      } else {
         return this.isFileSystem() && other.isFileSystem()
            ? (other.getDescriptor() + File.separator).startsWith(this.getDescriptor() + File.separator)
            : false;
      }
   }

   public String getPath() {
      return this.rawPath;
   }

   public String getDescriptor() {
      return this.prefix + this.rawPath;
   }

   public int compareTo(Location o) {
      return this.getDescriptor().compareTo(o.getDescriptor());
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Location location = (Location)o;
         return this.getDescriptor().equals(location.getDescriptor());
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.getDescriptor().hashCode();
   }

   public String toString() {
      return this.getDescriptor();
   }

   public String getPrefix() {
      return this.prefix;
   }

   public String getRootPath() {
      return this.rootPath;
   }

   public Pattern getPathRegex() {
      return this.pathRegex;
   }
}

package org.flywaydb.core.internal.resource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.util.Pair;

public class ResourceNameParser {
   private final Configuration configuration;
   private final List<Pair<String, ResourceType>> prefixes;

   public ResourceNameParser(Configuration configuration) {
      this.configuration = configuration;
      this.prefixes = this.populatePrefixes(configuration);
   }

   public ResourceName parse(String resourceName) {
      return this.parse(resourceName, this.configuration.getSqlMigrationSuffixes());
   }

   public ResourceName parse(String resourceName, String[] suffixes) {
      Pair<String, String> suffixResult = this.stripSuffix(resourceName, suffixes);
      Pair<String, ResourceType> prefix = this.findPrefix((String)suffixResult.getLeft(), this.prefixes);
      if (prefix != null) {
         Pair<String, String> prefixResult = this.stripPrefix((String)suffixResult.getLeft(), (String)prefix.getLeft());
         String name = (String)prefixResult.getRight();
         Pair<String, String> splitName = this.splitAtSeparator(name, this.configuration.getSqlMigrationSeparator());
         boolean isValid = true;
         String validationMessage = "";
         String exampleDescription = "".equals(splitName.getRight()) ? "description" : (String)splitName.getRight();
         if (!ResourceType.isVersioned((ResourceType)prefix.getRight())) {
            if (!"".equals(splitName.getLeft())) {
               isValid = false;
               validationMessage = "Invalid repeatable migration / callback name format: "
                  + resourceName
                  + " (It cannot contain a version and should look like this: "
                  + (String)prefixResult.getLeft()
                  + this.configuration.getSqlMigrationSeparator()
                  + exampleDescription
                  + (String)suffixResult.getRight()
                  + ")";
            }
         } else if ("".equals(splitName.getLeft())) {
            isValid = false;
            validationMessage = "Invalid versioned migration name format: "
               + resourceName
               + " (It must contain a version and should look like this: "
               + (String)prefixResult.getLeft()
               + "1.2"
               + this.configuration.getSqlMigrationSeparator()
               + exampleDescription
               + (String)suffixResult.getRight()
               + ")";
         } else {
            try {
               MigrationVersion.fromVersion((String)splitName.getLeft());
            } catch (Exception var12) {
               isValid = false;
               validationMessage = "Invalid versioned migration name format: "
                  + resourceName
                  + " (could not recognise version number "
                  + (String)splitName.getLeft()
                  + ")";
            }
         }

         String description = ((String)splitName.getRight()).replace("_", " ");
         return new ResourceName(
            (String)prefixResult.getLeft(),
            (String)splitName.getLeft(),
            this.configuration.getSqlMigrationSeparator(),
            description,
            (String)splitName.getRight(),
            (String)suffixResult.getRight(),
            isValid,
            validationMessage
         );
      } else {
         return ResourceName.invalid("Unrecognised migration name format: " + resourceName);
      }
   }

   private Pair<String, ResourceType> findPrefix(String nameWithoutSuffix, List<Pair<String, ResourceType>> prefixes) {
      for(Pair<String, ResourceType> prefix : prefixes) {
         if (nameWithoutSuffix.startsWith((String)prefix.getLeft())) {
            return prefix;
         }
      }

      return null;
   }

   private Pair<String, String> stripSuffix(String name, String[] suffixes) {
      for(String suffix : suffixes) {
         if (name.endsWith(suffix)) {
            return Pair.of(name.substring(0, name.length() - suffix.length()), suffix);
         }
      }

      return Pair.of(name, "");
   }

   private Pair<String, String> stripPrefix(String fileName, String prefix) {
      return fileName.startsWith(prefix) ? Pair.of(prefix, fileName.substring(prefix.length())) : null;
   }

   private Pair<String, String> splitAtSeparator(String name, String separator) {
      int separatorIndex = name.indexOf(separator);
      return separatorIndex >= 0 ? Pair.of(name.substring(0, separatorIndex), name.substring(separatorIndex + separator.length())) : Pair.of(name, "");
   }

   private List<Pair<String, ResourceType>> populatePrefixes(Configuration configuration) {
      List<Pair<String, ResourceType>> prefixes = new ArrayList();
      prefixes.add(Pair.of(configuration.getSqlMigrationPrefix(), ResourceType.MIGRATION));
      prefixes.add(Pair.of(configuration.getRepeatableSqlMigrationPrefix(), ResourceType.REPEATABLE_MIGRATION));

      for(Event event : Event.values()) {
         prefixes.add(Pair.of(event.getId(), ResourceType.CALLBACK));
      }

      Comparator<Pair<String, ResourceType>> prefixComparator = (p1, p2) -> ((String)p2.getLeft()).length() - ((String)p1.getLeft()).length();
      prefixes.sort(prefixComparator);
      return prefixes;
   }
}

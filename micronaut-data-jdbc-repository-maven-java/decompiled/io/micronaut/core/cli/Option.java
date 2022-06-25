package io.micronaut.core.cli;

public class Option {
   private String name;
   private String description;

   public Option(String name, String description) {
      if (name != null && name.length() != 0) {
         this.name = name;
         this.description = description == null ? "" : description;
      } else {
         throw new IllegalArgumentException("illegal option specified");
      }
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Option option = (Option)o;
         return this.name.equals(option.name);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.name.hashCode();
   }
}

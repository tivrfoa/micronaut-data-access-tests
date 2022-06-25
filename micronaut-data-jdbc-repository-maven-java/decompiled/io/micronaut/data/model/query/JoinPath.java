package io.micronaut.data.model.query;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.PersistentProperty;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class JoinPath {
   private final String path;
   private final Association[] associationPath;
   private final Join.Type joinType;
   private final String alias;

   public JoinPath(@NonNull String path, @NonNull Association[] associationPath, @NonNull Join.Type joinType, @Nullable String alias) {
      this.path = path;
      this.associationPath = associationPath;
      this.joinType = joinType;
      this.alias = alias;
   }

   public Optional<String> getAlias() {
      return Optional.ofNullable(this.alias);
   }

   public String toString() {
      return this.path;
   }

   @NonNull
   public Association getAssociation() {
      return this.associationPath[this.associationPath.length - 1];
   }

   public Association[] getAssociationPath() {
      return this.associationPath;
   }

   @NonNull
   public String getPath() {
      return this.path;
   }

   @NonNull
   public Join.Type getJoinType() {
      return this.joinType;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         JoinPath joinPath = (JoinPath)o;
         return this.path.equals(joinPath.path);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.path});
   }

   public static JoinPath of(Association... associationPath) {
      if (ArrayUtils.isEmpty(associationPath)) {
         throw new IllegalArgumentException("Association path cannot be empty");
      } else {
         String path = (String)Arrays.stream(associationPath).map(PersistentProperty::getName).collect(Collectors.joining("."));
         return new JoinPath(path, associationPath, Join.Type.DEFAULT, null);
      }
   }

   public static JoinPath of(String alias, Association... associationPath) {
      if (ArrayUtils.isEmpty(associationPath)) {
         throw new IllegalArgumentException("Association path cannot be empty");
      } else {
         String path = (String)Arrays.stream(associationPath).map(PersistentProperty::getName).collect(Collectors.joining("."));
         return new JoinPath(path, associationPath, Join.Type.DEFAULT, alias);
      }
   }
}

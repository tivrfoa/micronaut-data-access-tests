package io.micronaut.management.endpoint.env;

import io.micronaut.core.util.SupplierUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;

public final class EnvironmentFilterSpecification {
   private static final Supplier<List<Pattern>> LEGACY_MASK_PATTERNS = SupplierUtil.memoized(
      () -> (List)Stream.of(".*password.*", ".*credential.*", ".*certificate.*", ".*key.*", ".*secret.*", ".*token.*")
            .map(s -> Pattern.compile(s, 2))
            .collect(Collectors.toList())
   );
   private boolean allMasked;
   private final List<Predicate<String>> exclusions = new ArrayList();

   EnvironmentFilterSpecification() {
      this.allMasked = true;
   }

   @NotNull
   public EnvironmentFilterSpecification maskAll() {
      this.allMasked = true;
      return this;
   }

   @NotNull
   public EnvironmentFilterSpecification maskNone() {
      this.allMasked = false;
      return this;
   }

   @NotNull
   public EnvironmentFilterSpecification exclude(@NotNull Predicate<String> keyPredicate) {
      this.exclusions.add(keyPredicate);
      return this;
   }

   @NotNull
   public EnvironmentFilterSpecification exclude(@NotNull String... keys) {
      if (keys.length > 0) {
         if (keys.length == 1) {
            this.exclusions.add((Predicate)name -> keys[0].equals(name));
         } else {
            List<String> keysList = Arrays.asList(keys);
            this.exclusions.add(keysList::contains);
         }
      }

      return this;
   }

   @NotNull
   public EnvironmentFilterSpecification exclude(@NotNull Pattern... keyPatterns) {
      if (keyPatterns.length > 0) {
         if (keyPatterns.length == 1) {
            this.exclusions.add((Predicate)name -> keyPatterns[0].matcher(name).matches());
         } else {
            this.exclusions.add((Predicate)name -> Arrays.stream(keyPatterns).anyMatch(pattern -> pattern.matcher(name).matches()));
         }
      }

      return this;
   }

   @NotNull
   public EnvironmentFilterSpecification legacyMasking() {
      this.allMasked = false;

      for(Pattern pattern : (List)LEGACY_MASK_PATTERNS.get()) {
         this.exclude(pattern);
      }

      return this;
   }

   EnvironmentFilterSpecification.FilterResult test(String key) {
      for(Predicate<String> exclusion : this.exclusions) {
         if (exclusion.test(key)) {
            return this.allMasked ? EnvironmentFilterSpecification.FilterResult.PLAIN : EnvironmentFilterSpecification.FilterResult.MASK;
         }
      }

      return this.allMasked ? EnvironmentFilterSpecification.FilterResult.MASK : EnvironmentFilterSpecification.FilterResult.PLAIN;
   }

   static enum FilterResult {
      HIDE,
      MASK,
      PLAIN;
   }
}

package io.micronaut.core.util;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Internal
public final class EnvironmentProperties {
   private static final char[] DOT_DASH = new char[]{'.', '-'};
   private final EnvironmentProperties delegate;
   private final Map<String, List<String>> cache = new HashMap();

   private EnvironmentProperties(EnvironmentProperties delegate) {
      this.delegate = delegate;
   }

   public static EnvironmentProperties of(@NonNull Map<String, List<String>> preComputed) {
      EnvironmentProperties current = new EnvironmentProperties(null);
      current.cache.putAll(preComputed);
      return current;
   }

   @NonNull
   public Map<String, List<String>> asMap() {
      return Collections.unmodifiableMap(this.cache);
   }

   public static EnvironmentProperties fork(EnvironmentProperties delegate) {
      return new EnvironmentProperties(delegate);
   }

   public static EnvironmentProperties empty() {
      return new EnvironmentProperties(null);
   }

   public List<String> findPropertyNamesForEnvironmentVariable(String env) {
      if (this.delegate != null) {
         List<String> result = (List)this.delegate.cache.get(env);
         if (result != null) {
            return result;
         }
      }

      return (List<String>)this.cache.computeIfAbsent(env, EnvironmentProperties::computePropertiesFor);
   }

   private static List<String> computePropertiesFor(String env) {
      env = env.toLowerCase(Locale.ENGLISH);
      List<Integer> separatorIndexList = new ArrayList();
      char[] propertyArr = env.toCharArray();

      for(int i = 0; i < propertyArr.length; ++i) {
         if (propertyArr[i] == '_') {
            separatorIndexList.add(i);
         }
      }

      if (separatorIndexList.isEmpty()) {
         return Collections.singletonList(env);
      } else {
         int[] separatorIndexes = separatorIndexList.stream().mapToInt(Integer::intValue).toArray();
         int separatorCount = separatorIndexes.length;
         int[] halves = new int[separatorCount];
         byte[] separator = new byte[separatorCount];
         int permutations = (int)Math.pow(2.0, (double)separatorCount);

         for(int i = 0; i < halves.length; ++i) {
            int start = i == 0 ? permutations : halves[i - 1];
            halves[i] = start / 2;
         }

         String[] properties = new String[permutations];

         for(int i = 0; i < permutations; ++i) {
            int round = i + 1;

            for(int s = 0; s < separatorCount; ++s) {
               propertyArr[separatorIndexes[s]] = DOT_DASH[separator[s]];
               if (round % halves[s] == 0) {
                  separator[s] = (byte)(separator[s] ^ 1);
               }
            }

            properties[i] = new String(propertyArr);
         }

         return Arrays.asList(properties);
      }
   }
}

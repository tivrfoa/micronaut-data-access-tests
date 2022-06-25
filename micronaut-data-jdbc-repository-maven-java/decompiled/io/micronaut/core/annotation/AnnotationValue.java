package io.micronaut.core.annotation;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import java.lang.annotation.Annotation;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnnotationValue<A extends Annotation> implements AnnotationValueResolver {
   private final String annotationName;
   private final ConvertibleValues<Object> convertibleValues;
   private final Map<CharSequence, Object> values;
   private final Map<String, Object> defaultValues;
   private final Function<Object, Object> valueMapper;
   private final RetentionPolicy retentionPolicy;
   private final List<AnnotationValue<?>> stereotypes;

   @Internal
   public AnnotationValue(String annotationName, Map<CharSequence, Object> values) {
      this(annotationName, values, Collections.emptyMap());
   }

   @Internal
   public AnnotationValue(String annotationName, Map<CharSequence, Object> values, RetentionPolicy retentionPolicy) {
      this(annotationName, values, Collections.emptyMap(), retentionPolicy, null);
   }

   @Internal
   public AnnotationValue(String annotationName, Map<CharSequence, Object> values, RetentionPolicy retentionPolicy, List<AnnotationValue<?>> stereotypes) {
      this(annotationName, values, Collections.emptyMap(), retentionPolicy, stereotypes);
   }

   @Internal
   public AnnotationValue(String annotationName, Map<CharSequence, Object> values, Map<String, Object> defaultValues) {
      this(annotationName, values, defaultValues, RetentionPolicy.RUNTIME, null);
   }

   @Internal
   public AnnotationValue(String annotationName, Map<CharSequence, Object> values, Map<String, Object> defaultValues, RetentionPolicy retentionPolicy) {
      this(annotationName, values, defaultValues, retentionPolicy, null);
   }

   @Internal
   public AnnotationValue(
      String annotationName,
      Map<CharSequence, Object> values,
      Map<String, Object> defaultValues,
      RetentionPolicy retentionPolicy,
      List<AnnotationValue<?>> stereotypes
   ) {
      this.annotationName = annotationName;
      this.convertibleValues = this.newConvertibleValues(values);
      this.values = values;
      this.defaultValues = defaultValues != null ? defaultValues : Collections.emptyMap();
      this.valueMapper = null;
      this.retentionPolicy = retentionPolicy != null ? retentionPolicy : RetentionPolicy.RUNTIME;
      this.stereotypes = stereotypes;
   }

   @Internal
   public AnnotationValue(String annotationName) {
      this(annotationName, Collections.emptyMap(), Collections.emptyMap());
   }

   @Internal
   public AnnotationValue(String annotationName, ConvertibleValues<Object> convertibleValues) {
      this.annotationName = annotationName;
      this.convertibleValues = convertibleValues;
      Map<String, Object> existing = convertibleValues.asMap();
      this.values = new HashMap(existing);
      this.defaultValues = Collections.emptyMap();
      this.valueMapper = null;
      this.retentionPolicy = RetentionPolicy.RUNTIME;
      this.stereotypes = null;
   }

   @Internal
   protected AnnotationValue(
      AnnotationValue<A> target, Map<String, Object> defaultValues, ConvertibleValues<Object> convertibleValues, Function<Object, Object> valueMapper
   ) {
      this.annotationName = target.annotationName;
      this.defaultValues = defaultValues != null ? defaultValues : target.defaultValues;
      this.values = target.values;
      this.convertibleValues = convertibleValues;
      this.valueMapper = valueMapper;
      this.retentionPolicy = RetentionPolicy.RUNTIME;
      this.stereotypes = null;
   }

   @NonNull
   public final RetentionPolicy getRetentionPolicy() {
      return this.retentionPolicy;
   }

   @Nullable
   public List<AnnotationValue<?>> getStereotypes() {
      return this.stereotypes;
   }

   @NonNull
   public Map<String, String> getProperties(@NonNull String member) {
      return this.getProperties(member, "name");
   }

   public Map<String, String> getProperties(@NonNull String member, String keyMember) {
      ArgumentUtils.requireNonNull("keyMember", keyMember);
      if (StringUtils.isNotEmpty(member)) {
         List<AnnotationValue<Annotation>> values = this.getAnnotations(member);
         if (CollectionUtils.isNotEmpty(values)) {
            Map<String, String> props = new LinkedHashMap(values.size());

            for(AnnotationValue<Annotation> av : values) {
               String name = (String)av.stringValue(keyMember).orElse(null);
               if (StringUtils.isNotEmpty(name)) {
                  av.stringValue("value", this.valueMapper).ifPresent(v -> {
                     String var10000 = (String)props.put(name, v);
                  });
               }
            }

            return Collections.unmodifiableMap(props);
         }
      }

      return Collections.emptyMap();
   }

   @Override
   public <E extends Enum> Optional<E> enumValue(@NonNull String member, @NonNull Class<E> enumType) {
      return this.enumValue(member, enumType, this.valueMapper);
   }

   public <E extends Enum> Optional<E> enumValue(@NonNull String member, @NonNull Class<E> enumType, Function<Object, Object> valueMapper) {
      ArgumentUtils.requireNonNull("enumType", enumType);
      if (StringUtils.isNotEmpty(member)) {
         Object o = this.getRawSingleValue(member, valueMapper);
         if (o != null) {
            return convertToEnum(enumType, o);
         }
      }

      return Optional.empty();
   }

   @Override
   public <E extends Enum> E[] enumValues(@NonNull String member, @NonNull Class<E> enumType) {
      ArgumentUtils.requireNonNull("enumType", enumType);
      if (StringUtils.isNotEmpty(member)) {
         Object rawValue = this.values.get(member);
         return (E[])resolveEnumValues(enumType, rawValue);
      } else {
         return (E[])((Enum[])Array.newInstance(enumType, 0));
      }
   }

   @NonNull
   @Override
   public Optional<Class<?>> classValue() {
      return this.classValue("value");
   }

   @Override
   public Optional<Class<?>> classValue(@NonNull String member) {
      return this.classValue(member, this.valueMapper);
   }

   @Override
   public <T> Optional<Class<? extends T>> classValue(@NonNull String member, @NonNull Class<T> requiredType) {
      ArgumentUtils.requireNonNull("requiredType", (T)requiredType);
      if (StringUtils.isNotEmpty(member)) {
         Object o = this.getRawSingleValue(member, this.valueMapper);
         if (o instanceof AnnotationClassValue) {
            Class<?> t = (Class)((AnnotationClassValue)o).getType().orElse(null);
            if (t != null && requiredType.isAssignableFrom(t)) {
               return Optional.of(t);
            }

            return Optional.empty();
         }

         if (o instanceof Class) {
            Class t = (Class)o;
            if (requiredType.isAssignableFrom(t)) {
               return Optional.of(t);
            }

            return Optional.empty();
         }

         if (o != null) {
            Class t = (Class)ClassUtils.forName(o.toString(), this.getClass().getClassLoader()).orElse(null);
            if (t != null && requiredType.isAssignableFrom(t)) {
               return Optional.of(t);
            }
         }
      }

      return Optional.empty();
   }

   public Optional<Class<?>> classValue(@NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      if (StringUtils.isNotEmpty(member)) {
         Object o = this.getRawSingleValue(member, valueMapper);
         if (o instanceof AnnotationClassValue) {
            return ((AnnotationClassValue)o).getType();
         }

         if (o instanceof Class) {
            return Optional.of((Class)o);
         }
      }

      return Optional.empty();
   }

   @NonNull
   @Override
   public String[] stringValues(@NonNull String member) {
      Function<Object, Object> valueMapper = this.valueMapper;
      return this.stringValues(member, valueMapper);
   }

   @Override
   public boolean[] booleanValues(String member) {
      Object v = this.values.get(member);
      if (v != null) {
         if (v instanceof boolean[]) {
            return (boolean[])v;
         }

         if (v instanceof Boolean) {
            return new boolean[]{(Boolean)v};
         }

         String[] strings = resolveStringValues(v, this.valueMapper);
         if (ArrayUtils.isNotEmpty(strings)) {
            boolean[] booleans = new boolean[strings.length];

            for(int i = 0; i < strings.length; ++i) {
               String string = strings[i];
               booleans[i] = Boolean.parseBoolean(string);
            }

            return booleans;
         }
      }

      return ArrayUtils.EMPTY_BOOLEAN_ARRAY;
   }

   @Override
   public byte[] byteValues(String member) {
      Object v = this.values.get(member);
      if (v != null) {
         if (v instanceof byte[]) {
            return (byte[])v;
         }

         if (v instanceof Number) {
            return new byte[]{((Number)v).byteValue()};
         }

         String[] strings = resolveStringValues(v, this.valueMapper);
         if (ArrayUtils.isNotEmpty(strings)) {
            byte[] bytes = new byte[strings.length];

            for(int i = 0; i < strings.length; ++i) {
               String string = strings[i];
               bytes[i] = Byte.parseByte(string);
            }

            return bytes;
         }
      }

      return ArrayUtils.EMPTY_BYTE_ARRAY;
   }

   @Override
   public char[] charValues(String member) {
      Object v = this.values.get(member);
      if (v != null) {
         if (v instanceof char[]) {
            return (char[])v;
         }

         if (v instanceof Character[]) {
            Character[] v2 = (Character[])v;
            char[] chars = new char[v2.length];

            for(int i = 0; i < v2.length; ++i) {
               Character character = v2[i];
               chars[i] = character;
            }

            return chars;
         }

         if (v instanceof Character) {
            return new char[]{(Character)v};
         }
      }

      return ArrayUtils.EMPTY_CHAR_ARRAY;
   }

   @Override
   public int[] intValues(String member) {
      Object v = this.values.get(member);
      if (v != null) {
         if (v instanceof int[]) {
            return (int[])v;
         }

         if (v instanceof Number) {
            return new int[]{((Number)v).intValue()};
         }

         String[] strings = resolveStringValues(v, this.valueMapper);
         if (ArrayUtils.isNotEmpty(strings)) {
            int[] integers = new int[strings.length];

            for(int i = 0; i < strings.length; ++i) {
               String string = strings[i];
               integers[i] = Integer.parseInt(string);
            }

            return integers;
         }
      }

      return ArrayUtils.EMPTY_INT_ARRAY;
   }

   @Override
   public double[] doubleValues(String member) {
      Object v = this.values.get(member);
      if (v != null) {
         if (v instanceof double[]) {
            return (double[])v;
         }

         if (v instanceof Number) {
            return new double[]{((Number)v).doubleValue()};
         }

         String[] strings = resolveStringValues(v, this.valueMapper);
         if (ArrayUtils.isNotEmpty(strings)) {
            double[] doubles = new double[strings.length];

            for(int i = 0; i < strings.length; ++i) {
               String string = strings[i];
               doubles[i] = Double.parseDouble(string);
            }

            return doubles;
         }
      }

      return ArrayUtils.EMPTY_DOUBLE_ARRAY;
   }

   @Override
   public long[] longValues(String member) {
      Object v = this.values.get(member);
      if (v != null) {
         if (v instanceof long[]) {
            return (long[])v;
         }

         if (v instanceof Number) {
            return new long[]{((Number)v).longValue()};
         }

         String[] strings = resolveStringValues(v, this.valueMapper);
         if (ArrayUtils.isNotEmpty(strings)) {
            long[] longs = new long[strings.length];

            for(int i = 0; i < strings.length; ++i) {
               String string = strings[i];
               longs[i] = Long.parseLong(string);
            }

            return longs;
         }
      }

      return ArrayUtils.EMPTY_LONG_ARRAY;
   }

   @Override
   public float[] floatValues(String member) {
      Object v = this.values.get(member);
      if (v != null) {
         if (v instanceof float[]) {
            return (float[])v;
         }

         if (v instanceof Number) {
            return new float[]{((Number)v).floatValue()};
         }

         String[] strings = resolveStringValues(v, this.valueMapper);
         if (ArrayUtils.isNotEmpty(strings)) {
            float[] floats = new float[strings.length];

            for(int i = 0; i < strings.length; ++i) {
               String string = strings[i];
               floats[i] = Float.parseFloat(string);
            }

            return floats;
         }
      }

      return ArrayUtils.EMPTY_FLOAT_ARRAY;
   }

   @Override
   public short[] shortValues(String member) {
      Object v = this.values.get(member);
      if (v != null) {
         if (v instanceof short[]) {
            return (short[])v;
         }

         if (v instanceof Number) {
            return new short[]{((Number)v).shortValue()};
         }

         String[] strings = resolveStringValues(v, this.valueMapper);
         if (ArrayUtils.isNotEmpty(strings)) {
            short[] shorts = new short[strings.length];

            for(int i = 0; i < strings.length; ++i) {
               String string = strings[i];
               shorts[i] = Short.parseShort(string);
            }

            return shorts;
         }
      }

      return ArrayUtils.EMPTY_SHORT_ARRAY;
   }

   public String[] stringValues(@NonNull String member, Function<Object, Object> valueMapper) {
      if (StringUtils.isNotEmpty(member)) {
         Object o = this.values.get(member);
         String[] strs = resolveStringValues(o, valueMapper);
         if (strs != null) {
            return strs;
         }
      }

      return StringUtils.EMPTY_STRING_ARRAY;
   }

   @Override
   public Class<?>[] classValues(@NonNull String member) {
      if (StringUtils.isNotEmpty(member)) {
         Object o = this.values.get(member);
         Class<?>[] type = resolveClassValues(o);
         if (type != null) {
            return type;
         }
      }

      return ReflectionUtils.EMPTY_CLASS_ARRAY;
   }

   @NonNull
   @Override
   public AnnotationClassValue<?>[] annotationClassValues(@NonNull String member) {
      if (StringUtils.isNotEmpty(member)) {
         Object o = this.values.get(member);
         if (o instanceof AnnotationClassValue) {
            return new AnnotationClassValue[]{(AnnotationClassValue)o};
         }

         if (o instanceof AnnotationClassValue[]) {
            return (AnnotationClassValue[])o;
         }
      }

      return AnnotationClassValue.EMPTY_ARRAY;
   }

   @Override
   public Optional<AnnotationClassValue<?>> annotationClassValue(@NonNull String member) {
      if (StringUtils.isNotEmpty(member)) {
         Object o = this.values.get(member);
         if (o instanceof AnnotationClassValue) {
            return Optional.of((AnnotationClassValue)o);
         }

         if (o instanceof AnnotationClassValue[]) {
            AnnotationClassValue[] a = (AnnotationClassValue[])o;
            if (a.length > 0) {
               return Optional.of(a[0]);
            }
         }
      }

      return Optional.empty();
   }

   @Override
   public OptionalInt intValue(@NonNull String member) {
      return this.intValue(member, this.valueMapper);
   }

   public OptionalInt intValue(@NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      if (StringUtils.isNotEmpty(member)) {
         Object o = this.getRawSingleValue(member, valueMapper);
         if (o instanceof Number) {
            return OptionalInt.of(((Number)o).intValue());
         }

         if (o instanceof CharSequence) {
            try {
               return OptionalInt.of(Integer.parseInt(o.toString()));
            } catch (NumberFormatException var5) {
               return OptionalInt.empty();
            }
         }
      }

      return OptionalInt.empty();
   }

   @Override
   public Optional<Byte> byteValue(String member) {
      if (StringUtils.isNotEmpty(member)) {
         Object o = this.getRawSingleValue(member, this.valueMapper);
         if (o instanceof Number) {
            return Optional.of(((Number)o).byteValue());
         }

         if (o instanceof CharSequence) {
            try {
               return Optional.of(Byte.parseByte(o.toString()));
            } catch (NumberFormatException var4) {
               return Optional.empty();
            }
         }
      }

      return Optional.empty();
   }

   @Override
   public Optional<Character> charValue(String member) {
      if (StringUtils.isNotEmpty(member)) {
         Object o = this.getRawSingleValue(member, this.valueMapper);
         if (o instanceof Character) {
            return Optional.of((Character)o);
         }
      }

      return Optional.empty();
   }

   @Override
   public OptionalInt intValue() {
      return this.intValue("value");
   }

   @Override
   public OptionalLong longValue(@NonNull String member) {
      return this.longValue(member, null);
   }

   public OptionalLong longValue(@NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      if (StringUtils.isNotEmpty(member)) {
         Object o = this.getRawSingleValue(member, valueMapper);
         if (o instanceof Number) {
            return OptionalLong.of(((Number)o).longValue());
         }

         if (o instanceof CharSequence) {
            try {
               return OptionalLong.of(Long.parseLong(o.toString()));
            } catch (NumberFormatException var5) {
               return OptionalLong.empty();
            }
         }
      }

      return OptionalLong.empty();
   }

   @Override
   public Optional<Short> shortValue(@NonNull String member) {
      return this.shortValue(member, null);
   }

   public Optional<Short> shortValue(@NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      if (StringUtils.isNotEmpty(member)) {
         Object o = this.getRawSingleValue(member, valueMapper);
         if (o instanceof Number) {
            return Optional.of(((Number)o).shortValue());
         }

         if (o instanceof CharSequence) {
            try {
               return Optional.of(Short.parseShort(o.toString()));
            } catch (NumberFormatException var5) {
               return Optional.empty();
            }
         }
      }

      return Optional.empty();
   }

   public Optional<Boolean> booleanValue(@NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      if (StringUtils.isNotEmpty(member)) {
         Object o = this.getRawSingleValue(member, valueMapper);
         if (o instanceof Boolean) {
            return Optional.of((Boolean)o);
         }

         if (o instanceof CharSequence) {
            return Optional.of(StringUtils.isTrue(o.toString()));
         }
      }

      return Optional.empty();
   }

   @Override
   public OptionalDouble doubleValue(@NonNull String member) {
      return this.doubleValue(member, this.valueMapper);
   }

   public OptionalDouble doubleValue(@NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      if (StringUtils.isNotEmpty(member)) {
         Object o = this.getRawSingleValue(member, valueMapper);
         if (o instanceof Number) {
            return OptionalDouble.of(((Number)o).doubleValue());
         }

         if (o instanceof CharSequence) {
            try {
               return OptionalDouble.of(Double.parseDouble(o.toString()));
            } catch (NumberFormatException var5) {
               return OptionalDouble.empty();
            }
         }
      }

      return OptionalDouble.empty();
   }

   @Override
   public Optional<Float> floatValue(String member) {
      return this.floatValue(member, this.valueMapper);
   }

   public Optional<Float> floatValue(@NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      if (StringUtils.isNotEmpty(member)) {
         Object o = this.getRawSingleValue(member, valueMapper);
         if (o instanceof Number) {
            return Optional.of(((Number)o).floatValue());
         }

         if (o instanceof CharSequence) {
            try {
               return Optional.of(Float.parseFloat(o.toString()));
            } catch (NumberFormatException var5) {
               return Optional.empty();
            }
         }
      }

      return Optional.empty();
   }

   @Override
   public OptionalDouble doubleValue() {
      return this.doubleValue("value");
   }

   @Override
   public Optional<String> stringValue(@NonNull String member) {
      if (StringUtils.isNotEmpty(member)) {
         Object o = this.getRawSingleValue(member, this.valueMapper);
         if (o != null) {
            return Optional.of(o.toString());
         }
      }

      return Optional.empty();
   }

   public Optional<String> stringValue(@NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      if (StringUtils.isNotEmpty(member)) {
         Object o = this.getRawSingleValue(member, valueMapper);
         if (o != null) {
            return Optional.of(o.toString());
         }
      }

      return Optional.empty();
   }

   @Override
   public Optional<String> stringValue() {
      return this.stringValue("value");
   }

   @Override
   public Optional<Boolean> booleanValue(@NonNull String member) {
      return this.booleanValue(member, null);
   }

   @Override
   public final boolean isPresent(CharSequence member) {
      return StringUtils.isNotEmpty(member) ? this.values.containsKey(member) : false;
   }

   @Override
   public boolean isTrue() {
      return this.isTrue("value");
   }

   @Override
   public boolean isTrue(String member) {
      return this.isTrue(member, this.valueMapper);
   }

   public boolean isTrue(@NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      if (StringUtils.isNotEmpty(member)) {
         Object o = this.getRawSingleValue(member, valueMapper);
         if (o instanceof Boolean) {
            return (Boolean)o;
         }

         if (o != null) {
            return StringUtils.isTrue(o.toString());
         }
      }

      return false;
   }

   @Override
   public boolean isFalse() {
      return !this.isTrue("value");
   }

   @Override
   public boolean isFalse(String member) {
      return !this.isTrue(member);
   }

   @NonNull
   public final String getAnnotationName() {
      return this.annotationName;
   }

   public final boolean contains(String member) {
      return this.isPresent(member);
   }

   @NonNull
   public final Set<CharSequence> getMemberNames() {
      return this.values.keySet();
   }

   @NonNull
   @Override
   public Map<CharSequence, Object> getValues() {
      return Collections.unmodifiableMap(this.values);
   }

   @NonNull
   public ConvertibleValues<Object> getConvertibleValues() {
      return this.convertibleValues;
   }

   @Override
   public <T> Optional<T> get(CharSequence member, ArgumentConversionContext<T> conversionContext) {
      Optional<T> result = this.convertibleValues.get(member, conversionContext);
      if (!result.isPresent()) {
         Object dv = this.defaultValues.get(member.toString());
         if (dv != null) {
            return ConversionService.SHARED.convert(dv, conversionContext);
         }
      }

      return result;
   }

   public <T> Optional<T> getValue(ArgumentConversionContext<T> conversionContext) {
      return this.get("value", conversionContext);
   }

   public final <T> Optional<T> getValue(Argument<T> argument) {
      return this.getValue(ConversionContext.of(argument));
   }

   public final <T> Optional<T> getValue(Class<T> type) {
      return this.getValue(ConversionContext.of(type));
   }

   @NonNull
   public final <T> T getRequiredValue(Class<T> type) {
      return this.getRequiredValue("value", type);
   }

   @NonNull
   public final <T> T getRequiredValue(String member, Class<T> type) {
      return (T)this.get(member, ConversionContext.of(type))
         .orElseThrow(() -> new IllegalStateException("No value available for annotation member @" + this.annotationName + "[" + member + "] of type: " + type));
   }

   @NonNull
   public final <T extends Annotation> List<AnnotationValue<T>> getAnnotations(String member, Class<T> type) {
      ArgumentUtils.requireNonNull("type", (T)type);
      String typeName = type.getName();
      ArgumentUtils.requireNonNull("member", (T)member);
      Object v = this.values.get(member);
      AnnotationValue[] values = null;
      if (v instanceof AnnotationValue) {
         values = new AnnotationValue[]{(AnnotationValue)v};
      } else if (v instanceof AnnotationValue[]) {
         values = (AnnotationValue[])v;
      }

      if (ArrayUtils.isNotEmpty(values)) {
         List<AnnotationValue<T>> list = new ArrayList(values.length);

         for(AnnotationValue value : values) {
            if (value != null && value.getAnnotationName().equals(typeName)) {
               list.add(value);
            }
         }

         return list;
      } else {
         return Collections.emptyList();
      }
   }

   @NonNull
   public final <T extends Annotation> List<AnnotationValue<T>> getAnnotations(String member) {
      ArgumentUtils.requireNonNull("member", (T)member);
      Object v = this.values.get(member);
      if (v instanceof AnnotationValue) {
         return Collections.singletonList((AnnotationValue)v);
      } else if (v instanceof AnnotationValue[]) {
         return Arrays.asList((AnnotationValue[])v);
      } else {
         if (v instanceof Collection) {
            Iterator<?> i = ((Collection)v).iterator();
            if (i.hasNext()) {
               Object o = i.next();
               if (o instanceof AnnotationValue) {
                  return new ArrayList((Collection)v);
               }
            }
         }

         return Collections.emptyList();
      }
   }

   @NonNull
   public final <T extends Annotation> Optional<AnnotationValue<T>> getAnnotation(String member, Class<T> type) {
      ArgumentUtils.requireNonNull("type", (T)type);
      String typeName = type.getName();
      ArgumentUtils.requireNonNull("member", (T)member);
      Object v = this.values.get(member);
      if (v instanceof AnnotationValue) {
         AnnotationValue<T> av = (AnnotationValue)v;
         return av.getAnnotationName().equals(typeName) ? Optional.of(av) : Optional.empty();
      } else if (v instanceof AnnotationValue[]) {
         AnnotationValue[] values = (AnnotationValue[])v;
         if (ArrayUtils.isNotEmpty(values)) {
            AnnotationValue value = values[0];
            if (value.getAnnotationName().equals(typeName)) {
               return Optional.of(value);
            }
         }

         return Optional.empty();
      } else {
         return Optional.empty();
      }
   }

   @NonNull
   public final <T extends Annotation> Optional<AnnotationValue<T>> getAnnotation(@NonNull String member) {
      ArgumentUtils.requireNonNull("member", (T)member);
      Object v = this.values.get(member);
      if (v instanceof AnnotationValue) {
         AnnotationValue<T> av = (AnnotationValue)v;
         return Optional.of(av);
      } else if (v instanceof AnnotationValue[]) {
         AnnotationValue[] values = (AnnotationValue[])v;
         if (ArrayUtils.isNotEmpty(values)) {
            AnnotationValue value = values[0];
            return Optional.of(value);
         } else {
            return Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }

   public String toString() {
      return this.values.isEmpty()
         ? "@" + this.annotationName
         : "@"
            + this.annotationName
            + "("
            + (String)this.values.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining(", "))
            + ")";
   }

   public int hashCode() {
      return 31 * this.annotationName.hashCode() + AnnotationUtil.calculateHashCode(this.getValues());
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (!AnnotationValue.class.isInstance(obj)) {
         return false;
      } else {
         AnnotationValue other = (AnnotationValue)AnnotationValue.class.cast(obj);
         if (!this.annotationName.equals(other.getAnnotationName())) {
            return false;
         } else {
            Map<CharSequence, Object> otherValues = other.getValues();
            Map<CharSequence, Object> values = this.getValues();
            if (values.size() != otherValues.size()) {
               return false;
            } else {
               for(Entry<CharSequence, Object> member : values.entrySet()) {
                  Object value = member.getValue();
                  Object otherValue = otherValues.get(member.getKey());
                  if (!AnnotationUtil.areEqual(value, otherValue)) {
                     return false;
                  }
               }

               return true;
            }
         }
      }
   }

   public static <T extends Annotation> AnnotationValueBuilder<T> builder(String annotationName) {
      return new AnnotationValueBuilder<>(annotationName);
   }

   public static <T extends Annotation> AnnotationValueBuilder<T> builder(String annotationName, RetentionPolicy retentionPolicy) {
      return new AnnotationValueBuilder<>(annotationName, retentionPolicy);
   }

   public static <T extends Annotation> AnnotationValueBuilder<T> builder(Class<T> annotation) {
      return new AnnotationValueBuilder<>(annotation);
   }

   public static <T extends Annotation> AnnotationValueBuilder<T> builder(@NonNull AnnotationValue<T> annotation, @Nullable RetentionPolicy retentionPolicy) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      return new AnnotationValueBuilder<>(annotation, retentionPolicy);
   }

   @Internal
   @Nullable
   public static String[] resolveStringValues(@Nullable Object value, @Nullable Function<Object, Object> valueMapper) {
      if (value == null) {
         return null;
      } else {
         if (valueMapper != null) {
            value = valueMapper.apply(value);
         }

         if (value instanceof CharSequence) {
            return new String[]{value.toString()};
         } else if (value instanceof String[]) {
            String[] existing = (String[])value;
            return (String[])Arrays.copyOf(existing, existing.length);
         } else if (value != null) {
            if (value.getClass().isArray()) {
               int len = Array.getLength(value);
               String[] newArray = new String[len];

               for(int i = 0; i < newArray.length; ++i) {
                  Object entry = Array.get(value, i);
                  if (entry != null) {
                     newArray[i] = entry.toString();
                  }
               }

               return newArray;
            } else {
               return new String[]{value.toString()};
            }
         } else {
            return null;
         }
      }
   }

   @Internal
   @NonNull
   public static <E extends Enum> E[] resolveEnumValues(@NonNull Class<E> enumType, @Nullable Object rawValue) {
      if (rawValue == null) {
         return (E[])((Enum[])Array.newInstance(enumType, 0));
      } else {
         List<E> list = new ArrayList();
         if (rawValue.getClass().isArray()) {
            int len = Array.getLength(rawValue);

            for(int i = 0; i < len; ++i) {
               convertToEnum(enumType, Array.get(rawValue, i)).ifPresent(list::add);
            }
         } else if (rawValue instanceof Iterable) {
            for(Object o : (Iterable)rawValue) {
               convertToEnum(enumType, o).ifPresent(list::add);
            }
         } else if (enumType.isAssignableFrom(rawValue.getClass())) {
            list.add((Enum)rawValue);
         } else {
            convertToEnum(enumType, rawValue).ifPresent(list::add);
         }

         return (E[])list.toArray((Enum[])Array.newInstance(enumType, 0));
      }
   }

   @Internal
   public static String[] resolveStringArray(String[] strs, @Nullable Function<Object, Object> valueMapper) {
      if (valueMapper == null) {
         return strs;
      } else {
         String[] newStrs = new String[strs.length];

         for(int i = 0; i < strs.length; ++i) {
            String str = strs[i];
            newStrs[i] = valueMapper.apply(str).toString();
         }

         return newStrs;
      }
   }

   @Internal
   @Nullable
   public static Class<?>[] resolveClassValues(@Nullable Object value) {
      if (value == null) {
         return null;
      } else {
         if (value instanceof AnnotationClassValue) {
            Class<?> type = (Class)((AnnotationClassValue)value).getType().orElse(null);
            if (type != null) {
               return new Class[]{type};
            }
         } else if (value instanceof AnnotationValue[]) {
            AnnotationValue[] array = (AnnotationValue[])value;
            int len = array.length;
            if (len > 0) {
               if (len == 1) {
                  return array[0].classValues();
               }

               return (Class<?>[])Arrays.stream(array).flatMap(annotationValue -> Stream.of(annotationValue.classValues())).toArray(x$0 -> new Class[x$0]);
            }
         } else {
            if (value instanceof AnnotationValue) {
               return ((AnnotationValue)value).classValues();
            }

            if (value instanceof Object[]) {
               Object[] values = value;
               if (values instanceof Class[]) {
                  return (Class[])values;
               }

               return (Class<?>[])Arrays.stream(values).flatMap(o -> {
                  if (o instanceof AnnotationClassValue) {
                     Optional<? extends Class<?>> type = ((AnnotationClassValue)o).getType();
                     return (Stream)type.map(Stream::of).orElse(Stream.empty());
                  } else {
                     return o instanceof Class ? Stream.of((Class)o) : Stream.empty();
                  }
               }).toArray(x$0 -> new Class[x$0]);
            }

            if (value instanceof Class) {
               return new Class[]{(Class)value};
            }
         }

         return null;
      }
   }

   private ConvertibleValues<Object> newConvertibleValues(Map<CharSequence, Object> values) {
      return CollectionUtils.isEmpty(values) ? ConvertibleValues.EMPTY : ConvertibleValues.of(values);
   }

   @Nullable
   private Object getRawSingleValue(@NonNull String member, Function<Object, Object> valueMapper) {
      Object rawValue = this.values.get(member);
      if (rawValue != null) {
         if (rawValue.getClass().isArray()) {
            int len = Array.getLength(rawValue);
            if (len > 0) {
               rawValue = Array.get(rawValue, 0);
            }
         } else if (rawValue instanceof Iterable) {
            Iterator i = ((Iterable)rawValue).iterator();
            if (i.hasNext()) {
               rawValue = i.next();
            }
         }
      }

      return valueMapper != null && rawValue instanceof String ? valueMapper.apply(rawValue) : rawValue;
   }

   private static <T extends Enum> Optional<T> convertToEnum(Class<T> enumType, Object o) {
      if (enumType.isInstance(o)) {
         return Optional.of((Enum)o);
      } else {
         try {
            T t = Enum.valueOf(enumType, o.toString());
            return Optional.of(t);
         } catch (IllegalArgumentException var3) {
            return Optional.empty();
         }
      }
   }
}

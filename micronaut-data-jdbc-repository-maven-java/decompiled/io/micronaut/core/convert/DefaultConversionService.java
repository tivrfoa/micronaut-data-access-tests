package io.micronaut.core.convert;

import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.convert.converters.MultiValuesConverterFactory;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.core.convert.format.Format;
import io.micronaut.core.convert.format.FormattingTypeConverter;
import io.micronaut.core.convert.format.ReadableBytesTypeConverter;
import io.micronaut.core.convert.value.ConvertibleMultiValues;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.convert.value.ConvertibleValuesMap;
import io.micronaut.core.io.IOUtils;
import io.micronaut.core.io.buffer.ReferenceCounted;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.clhm.ConcurrentLinkedHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.TimeZone;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class DefaultConversionService implements ConversionService<DefaultConversionService> {
   private static final int CACHE_MAX = 150;
   private static final TypeConverter UNCONVERTIBLE = (object, targetType, context) -> Optional.empty();
   private final Map<DefaultConversionService.ConvertiblePair, TypeConverter> typeConverters = new ConcurrentHashMap();
   private final Map<DefaultConversionService.ConvertiblePair, TypeConverter> converterCache = new ConcurrentLinkedHashMap.Builder<DefaultConversionService.ConvertiblePair, TypeConverter>(
         
      )
      .maximumWeightedCapacity(150L)
      .build();

   public DefaultConversionService() {
      this.registerDefaultConverters();
   }

   @Override
   public <T> Optional<T> convert(Object object, Class<T> targetType, ConversionContext context) {
      if (object == null || targetType == null || context == null) {
         return Optional.empty();
      } else if (targetType == Object.class) {
         return Optional.of(object);
      } else {
         targetType = targetType.isPrimitive() ? ReflectionUtils.getWrapperType(targetType) : targetType;
         if (targetType.isInstance(object) && !Iterable.class.isInstance(object) && !Map.class.isInstance(object)) {
            return Optional.of(object);
         } else {
            Class<?> sourceType = object.getClass();
            AnnotationMetadata annotationMetadata = context.getAnnotationMetadata();
            if (annotationMetadata.hasStereotype(Format.class)) {
               Optional<String> formattingAnn = annotationMetadata.getAnnotationNameByStereotype(Format.class);
               String formattingAnnotation = (String)formattingAnn.orElse(null);
               DefaultConversionService.ConvertiblePair pair = new DefaultConversionService.ConvertiblePair(sourceType, targetType, formattingAnnotation);
               TypeConverter typeConverter = (TypeConverter)this.converterCache.get(pair);
               if (typeConverter == null) {
                  typeConverter = this.findTypeConverter(sourceType, targetType, formattingAnnotation);
                  if (typeConverter == null) {
                     return Optional.empty();
                  }

                  this.converterCache.put(pair, typeConverter);
                  if (typeConverter == UNCONVERTIBLE) {
                     return Optional.empty();
                  }

                  return typeConverter.convert(object, targetType, context);
               }

               if (typeConverter != UNCONVERTIBLE) {
                  return typeConverter.convert(object, targetType, context);
               }
            } else {
               DefaultConversionService.ConvertiblePair pair = new DefaultConversionService.ConvertiblePair(sourceType, targetType, null);
               TypeConverter typeConverter = (TypeConverter)this.converterCache.get(pair);
               if (typeConverter == null) {
                  typeConverter = this.findTypeConverter(sourceType, targetType, null);
                  if (typeConverter == null) {
                     this.converterCache.put(pair, UNCONVERTIBLE);
                     return Optional.empty();
                  }

                  this.converterCache.put(pair, typeConverter);
                  if (typeConverter == UNCONVERTIBLE) {
                     return Optional.empty();
                  }

                  return typeConverter.convert(object, targetType, context);
               }

               if (typeConverter != UNCONVERTIBLE) {
                  return typeConverter.convert(object, targetType, context);
               }
            }

            return Optional.empty();
         }
      }
   }

   @Override
   public <S, T> boolean canConvert(Class<S> sourceType, Class<T> targetType) {
      DefaultConversionService.ConvertiblePair pair = new DefaultConversionService.ConvertiblePair(sourceType, targetType, null);
      TypeConverter typeConverter = (TypeConverter)this.converterCache.get(pair);
      if (typeConverter == null) {
         typeConverter = this.findTypeConverter(sourceType, targetType, null);
         if (typeConverter != null) {
            this.converterCache.put(pair, typeConverter);
            return typeConverter != UNCONVERTIBLE;
         } else {
            return false;
         }
      } else {
         return typeConverter != UNCONVERTIBLE;
      }
   }

   public <S, T> DefaultConversionService addConverter(Class<S> sourceType, Class<T> targetType, TypeConverter<S, T> typeConverter) {
      DefaultConversionService.ConvertiblePair pair = this.newPair(sourceType, targetType, typeConverter);
      this.typeConverters.put(pair, typeConverter);
      this.converterCache.put(pair, typeConverter);
      return this;
   }

   public <S, T> DefaultConversionService addConverter(Class<S> sourceType, Class<T> targetType, Function<S, T> function) {
      DefaultConversionService.ConvertiblePair pair = new DefaultConversionService.ConvertiblePair(sourceType, targetType);
      TypeConverter<S, T> typeConverter = TypeConverter.of(sourceType, targetType, function);
      this.typeConverters.put(pair, typeConverter);
      this.converterCache.put(pair, typeConverter);
      return this;
   }

   protected void registerDefaultConverters() {
      Function primitiveArrayToWrapperArray = ArrayUtils::toWrapperArray;
      this.addConverter(double[].class, Double[].class, primitiveArrayToWrapperArray);
      this.addConverter(byte[].class, Byte[].class, primitiveArrayToWrapperArray);
      this.addConverter(short[].class, Short[].class, primitiveArrayToWrapperArray);
      this.addConverter(boolean[].class, Boolean[].class, primitiveArrayToWrapperArray);
      this.addConverter(int[].class, Integer[].class, primitiveArrayToWrapperArray);
      this.addConverter(float[].class, Float[].class, primitiveArrayToWrapperArray);
      this.addConverter(double[].class, Double[].class, primitiveArrayToWrapperArray);
      this.addConverter(char[].class, Character[].class, primitiveArrayToWrapperArray);
      Function<Object[], Object> wrapperArrayToPrimitiveArray = ArrayUtils::toPrimitiveArray;
      this.addConverter(Double[].class, double[].class, wrapperArrayToPrimitiveArray);
      this.addConverter(Integer[].class, int[].class, wrapperArrayToPrimitiveArray);
      this.addConverter(Object.class, List.class, (TypeConverter)((object, targetType, context) -> {
         Optional<Argument<?>> firstTypeVariable = context.getFirstTypeVariable();
         Argument<?> argument = (Argument)firstTypeVariable.orElse(Argument.OBJECT_ARGUMENT);
         Optional converted = this.convert(object, context.with(argument));
         return converted.isPresent() ? Optional.of(Collections.singletonList(converted.get())) : Optional.empty();
      }));
      this.addConverter(CharSequence.class, Class.class, (TypeConverter)((object, targetType, context) -> {
         ClassLoader classLoader = targetType.getClassLoader();
         if (classLoader == null) {
            classLoader = DefaultConversionService.class.getClassLoader();
         }

         return ClassUtils.forName(object.toString(), classLoader);
      }));
      this.addConverter(AnnotationClassValue.class, Class.class, (TypeConverter)((object, targetType, context) -> object.getType()));
      this.addConverter(AnnotationClassValue.class, Object.class, (TypeConverter)((object, targetType, context) -> {
         if (targetType.equals(Class.class)) {
            return object.getType();
         } else if (CharSequence.class.isAssignableFrom(targetType)) {
            return Optional.of(object.getName());
         } else {
            Optional i = object.getInstance();
            return i.isPresent() && targetType.isInstance(i.get()) ? i : Optional.empty();
         }
      }));
      this.addConverter(AnnotationClassValue[].class, Class.class, (TypeConverter)((object, targetType, context) -> {
         if (object.length > 0) {
            AnnotationClassValue o = object[0];
            if (o != null) {
               return o.getType();
            }
         }

         return Optional.empty();
      }));
      this.addConverter(AnnotationClassValue[].class, Class[].class, (TypeConverter)((object, targetType, context) -> {
         List<Class> classes = new ArrayList(object.length);

         for(AnnotationClassValue<?> annotationClassValue : object) {
            if (annotationClassValue != null) {
               Optional<? extends Class<?>> type = annotationClassValue.getType();
               if (type.isPresent()) {
                  classes.add(type.get());
               }
            }
         }

         return Optional.of(classes.toArray(new Class[0]));
      }));
      this.addConverter(URI.class, URL.class, uri -> {
         try {
            return uri.toURL();
         } catch (MalformedURLException var2x) {
            return null;
         }
      });
      this.addConverter(InputStream.class, String.class, (TypeConverter)((object, targetType, context) -> {
         BufferedReader reader = new BufferedReader(new InputStreamReader(object));

         try {
            return Optional.of(IOUtils.readText(reader));
         } catch (IOException var5) {
            context.reject(var5);
            return Optional.empty();
         }
      }));
      this.addConverter(
         CharSequence.class, byte[].class, (TypeConverter)((object, targetType, context) -> Optional.of(object.toString().getBytes(context.getCharset())))
      );
      this.addConverter(
         Integer.class, byte[].class, (TypeConverter)((object, targetType, context) -> Optional.of(ByteBuffer.allocate(4).putInt(object).array()))
      );
      this.addConverter(
         Character.class, byte[].class, (TypeConverter)((object, targetType, context) -> Optional.of(ByteBuffer.allocate(4).putChar(object).array()))
      );
      this.addConverter(Long.class, byte[].class, (TypeConverter)((object, targetType, context) -> Optional.of(ByteBuffer.allocate(8).putLong(object).array())));
      this.addConverter(
         Short.class, byte[].class, (TypeConverter)((object, targetType, context) -> Optional.of(ByteBuffer.allocate(2).putShort(object).array()))
      );
      this.addConverter(
         Double.class, byte[].class, (TypeConverter)((object, targetType, context) -> Optional.of(ByteBuffer.allocate(8).putDouble(object).array()))
      );
      this.addConverter(
         Float.class, byte[].class, (TypeConverter)((object, targetType, context) -> Optional.of(ByteBuffer.allocate(4).putFloat(object).array()))
      );
      this.addConverter(InputStream.class, Number.class, (TypeConverter)((object, targetType, context) -> {
         Optional<String> convert = this.convert(object, String.class, context);
         return convert.isPresent() ? convert.flatMap(val -> this.convert(val, targetType, context)) : Optional.empty();
      }));
      this.addConverter(Reader.class, String.class, (TypeConverter)((object, targetType, context) -> {
         BufferedReader reader = object instanceof BufferedReader ? (BufferedReader)object : new BufferedReader(object);

         try {
            return Optional.of(IOUtils.readText(reader));
         } catch (IOException var5) {
            context.reject(var5);
            return Optional.empty();
         }
      }));
      this.addConverter(
         CharSequence.class,
         File.class,
         (TypeConverter)((object, targetType, context) -> StringUtils.isEmpty(object) ? Optional.empty() : Optional.of(new File(object.toString())))
      );
      this.addConverter(String[].class, Enum.class, (TypeConverter)((object, targetType, context) -> {
         if (object != null && object.length != 0) {
            StringJoiner joiner = new StringJoiner("");

            for(String string : object) {
               joiner.add(string);
            }

            String val = joiner.toString();
            return this.convert(val, targetType, context);
         } else {
            return Optional.empty();
         }
      }));
      this.addConverter(String[].class, CharSequence.class, (TypeConverter)((object, targetType, context) -> {
         if (object != null && object.length != 0) {
            StringJoiner joiner = new StringJoiner("");

            for(String string : object) {
               joiner.add(string);
            }

            return this.convert(joiner.toString(), targetType, context);
         } else {
            return Optional.empty();
         }
      }));
      this.addConverter(CharSequence.class, Number.class, new ReadableBytesTypeConverter());
      this.addConverter(CharSequence.class, Date.class, (TypeConverter)((object, targetType, context) -> {
         if (StringUtils.isEmpty(object)) {
            return Optional.empty();
         } else {
            try {
               SimpleDateFormat format = this.resolveFormat(context);
               return Optional.of(format.parse(object.toString()));
            } catch (ParseException var5) {
               context.reject(object, var5);
               return Optional.empty();
            }
         }
      }));
      this.addConverter(Date.class, CharSequence.class, (TypeConverter)((object, targetType, context) -> {
         SimpleDateFormat format = this.resolveFormat(context);
         return Optional.of(format.format(object));
      }));
      this.addConverter(CharSequence.class, Path.class, (TypeConverter)((object, targetType, context) -> {
         if (StringUtils.isEmpty(object)) {
            return Optional.empty();
         } else {
            try {
               return Optional.ofNullable(Paths.get(object.toString()));
            } catch (Exception var4) {
               context.reject("Invalid path [" + object + " ]: " + var4.getMessage(), var4);
               return Optional.empty();
            }
         }
      }));
      this.addConverter(CharSequence.class, Integer.class, (TypeConverter)((object, targetType, context) -> {
         if (StringUtils.isEmpty(object)) {
            return Optional.empty();
         } else {
            try {
               Integer converted = Integer.valueOf(object.toString());
               return Optional.of(converted);
            } catch (NumberFormatException var4) {
               context.reject(object, var4);
               return Optional.empty();
            }
         }
      }));
      this.addConverter(CharSequence.class, BigInteger.class, (TypeConverter)((object, targetType, context) -> {
         if (StringUtils.isEmpty(object)) {
            return Optional.empty();
         } else {
            try {
               BigInteger converted = new BigInteger(object.toString());
               return Optional.of(converted);
            } catch (NumberFormatException var4) {
               context.reject(object, var4);
               return Optional.empty();
            }
         }
      }));
      this.addConverter(CharSequence.class, Float.class, (TypeConverter)((object, targetType, context) -> {
         if (StringUtils.isEmpty(object)) {
            return Optional.empty();
         } else {
            try {
               Float converted = Float.valueOf(object.toString());
               return Optional.of(converted);
            } catch (NumberFormatException var4) {
               context.reject(object, var4);
               return Optional.empty();
            }
         }
      }));
      this.addConverter(CharSequence.class, Double.class, (TypeConverter)((object, targetType, context) -> {
         if (StringUtils.isEmpty(object)) {
            return Optional.empty();
         } else {
            try {
               Double converted = Double.valueOf(object.toString());
               return Optional.of(converted);
            } catch (NumberFormatException var4) {
               context.reject(object, var4);
               return Optional.empty();
            }
         }
      }));
      this.addConverter(CharSequence.class, Long.class, (TypeConverter)((object, targetType, context) -> {
         if (StringUtils.isEmpty(object)) {
            return Optional.empty();
         } else {
            try {
               Long converted = Long.valueOf(object.toString());
               return Optional.of(converted);
            } catch (NumberFormatException var4) {
               context.reject(object, var4);
               return Optional.empty();
            }
         }
      }));
      this.addConverter(CharSequence.class, Short.class, (TypeConverter)((object, targetType, context) -> {
         if (StringUtils.isEmpty(object)) {
            return Optional.empty();
         } else {
            try {
               Short converted = Short.valueOf(object.toString());
               return Optional.of(converted);
            } catch (NumberFormatException var4) {
               context.reject(object, var4);
               return Optional.empty();
            }
         }
      }));
      this.addConverter(CharSequence.class, Byte.class, (TypeConverter)((object, targetType, context) -> {
         if (StringUtils.isEmpty(object)) {
            return Optional.empty();
         } else {
            try {
               Byte converted = Byte.valueOf(object.toString());
               return Optional.of(converted);
            } catch (NumberFormatException var4) {
               context.reject(object, var4);
               return Optional.empty();
            }
         }
      }));
      this.addConverter(CharSequence.class, BigDecimal.class, (TypeConverter)((object, targetType, context) -> {
         if (StringUtils.isEmpty(object)) {
            return Optional.empty();
         } else {
            try {
               BigDecimal converted = new BigDecimal(object.toString());
               return Optional.of(converted);
            } catch (NumberFormatException var4) {
               context.reject(object, var4);
               return Optional.empty();
            }
         }
      }));
      this.addConverter(CharSequence.class, Boolean.class, (TypeConverter)((object, targetType, context) -> {
         String booleanString = object.toString().toLowerCase(Locale.ENGLISH);
         switch(booleanString) {
            case "yes":
            case "y":
            case "on":
            case "true":
               return Optional.of(Boolean.TRUE);
            default:
               return Optional.of(Boolean.FALSE);
         }
      }));
      this.addConverter(CharSequence.class, URL.class, (TypeConverter)((object, targetType, context) -> {
         if (StringUtils.isEmpty(object)) {
            return Optional.empty();
         } else {
            try {
               String spec = object.toString();
               if (!spec.contains("://")) {
                  spec = "http://" + spec;
               }

               return Optional.of(new URL(spec));
            } catch (MalformedURLException var4) {
               context.reject(object, var4);
               return Optional.empty();
            }
         }
      }));
      this.addConverter(CharSequence.class, URI.class, (TypeConverter)((object, targetType, context) -> {
         if (StringUtils.isEmpty(object)) {
            return Optional.empty();
         } else {
            try {
               return Optional.of(new URI(object.toString()));
            } catch (URISyntaxException var4) {
               context.reject(object, var4);
               return Optional.empty();
            }
         }
      }));
      this.addConverter(CharSequence.class, Locale.class, object -> StringUtils.parseLocale(object.toString()));
      this.addConverter(CharSequence.class, UUID.class, (TypeConverter)((object, targetType, context) -> {
         if (StringUtils.isEmpty(object)) {
            return Optional.empty();
         } else {
            try {
               return Optional.of(UUID.fromString(object.toString()));
            } catch (IllegalArgumentException var4) {
               context.reject(object, var4);
               return Optional.empty();
            }
         }
      }));
      this.addConverter(CharSequence.class, Currency.class, (TypeConverter)((object, targetType, context) -> {
         if (StringUtils.isEmpty(object)) {
            return Optional.empty();
         } else {
            try {
               return Optional.of(Currency.getInstance(object.toString()));
            } catch (IllegalArgumentException var4) {
               context.reject(object, var4);
               return Optional.empty();
            }
         }
      }));
      this.addConverter(
         CharSequence.class,
         TimeZone.class,
         (TypeConverter)((object, targetType, context) -> StringUtils.isEmpty(object) ? Optional.empty() : Optional.of(TimeZone.getTimeZone(object.toString())))
      );
      this.addConverter(CharSequence.class, Charset.class, (TypeConverter)((object, targetType, context) -> {
         if (StringUtils.isEmpty(object)) {
            return Optional.empty();
         } else {
            try {
               return Optional.of(Charset.forName(object.toString()));
            } catch (UnsupportedCharsetException | IllegalCharsetNameException var4) {
               context.reject(object, var4);
               return Optional.empty();
            }
         }
      }));
      this.addConverter(CharSequence.class, Character.class, (TypeConverter)((object, targetType, context) -> {
         String str = object.toString();
         return str.length() == 1 ? Optional.of(str.charAt(0)) : Optional.empty();
      }));
      this.addConverter(CharSequence.class, Object[].class, (TypeConverter)((object, targetType, context) -> {
         if (object instanceof AnnotationClassValue && targetType.equals(AnnotationClassValue[].class)) {
            AnnotationClassValue[] array = new AnnotationClassValue[]{(AnnotationClassValue)object};
            return Optional.of(array);
         } else {
            String str = object.toString();
            String[] strings = str.split(",");
            Class<?> componentType = ReflectionUtils.getWrapperType(targetType.getComponentType());
            Object newArray = Array.newInstance(componentType, strings.length);

            for(int i = 0; i < strings.length; ++i) {
               String string = strings[i];
               Optional<?> converted = this.convert(string, componentType);
               if (converted.isPresent()) {
                  Array.set(newArray, i, converted.get());
               }
            }

            return Optional.of(newArray);
         }
      }));
      this.addConverter(CharSequence.class, int[].class, (TypeConverter)((object, targetType, context) -> {
         String str = object.toString();
         String[] strings = str.split(",");
         Object newArray = Array.newInstance(Integer.TYPE, strings.length);

         for(int i = 0; i < strings.length; ++i) {
            String string = strings[i];
            Optional<?> converted = this.convert(string, Integer.TYPE);
            if (converted.isPresent()) {
               Array.set(newArray, i, converted.get());
            }
         }

         return Optional.of((int[])newArray);
      }));
      this.addConverter(String.class, char[].class, (TypeConverter)((object, targetType, context) -> Optional.of(object.toCharArray())));
      this.addConverter(Object[].class, String[].class, (TypeConverter)((object, targetType, context) -> {
         String[] strings = new String[object.length];

         for(int i = 0; i < object.length; ++i) {
            Object o = object[i];
            if (o != null) {
               strings[i] = o.toString();
            }
         }

         return Optional.of(strings);
      }));
      this.addConverter(CharSequence.class, Enum.class, (TypeConverter)((object, targetType, context) -> {
         if (StringUtils.isEmpty(object)) {
            return Optional.empty();
         } else {
            String stringValue = object.toString();

            try {
               Enum val = Enum.valueOf(targetType, stringValue);
               return Optional.of(val);
            } catch (IllegalArgumentException var8) {
               try {
                  Enum valx = Enum.valueOf(targetType, NameUtils.environmentName(stringValue));
                  return Optional.of(valx);
               } catch (Exception var7) {
                  Optional<Enum> valOpt = Arrays.stream(targetType.getEnumConstants()).filter(val -> val.toString().equals(stringValue)).findFirst();
                  if (valOpt.isPresent()) {
                     return valOpt;
                  } else {
                     context.reject(object, var8);
                     return Optional.empty();
                  }
               }
            }
         }
      }));
      this.addConverter(Object.class, String.class, (TypeConverter)((object, targetType, context) -> Optional.of(object.toString())));
      this.addConverter(Number.class, Number.class, (TypeConverter)((object, targetType, context) -> {
         Class targetNumberType = ReflectionUtils.getWrapperType(targetType);
         if (targetNumberType.isInstance(object)) {
            return Optional.of(object);
         } else if (targetNumberType == Integer.class) {
            return Optional.of(object.intValue());
         } else if (targetNumberType == Long.class) {
            return Optional.of(object.longValue());
         } else if (targetNumberType == Short.class) {
            return Optional.of(object.shortValue());
         } else if (targetNumberType == Byte.class) {
            return Optional.of(object.byteValue());
         } else if (targetNumberType == Float.class) {
            return Optional.of(object.floatValue());
         } else if (targetNumberType == Double.class) {
            return Optional.of(object.doubleValue());
         } else if (targetNumberType == BigInteger.class) {
            return object instanceof BigDecimal ? Optional.of(((BigDecimal)object).toBigInteger()) : Optional.of(BigInteger.valueOf(object.longValue()));
         } else {
            return targetNumberType == BigDecimal.class ? Optional.of(new BigDecimal(object.toString())) : Optional.empty();
         }
      }));
      this.addConverter(CharSequence.class, Iterable.class, (TypeConverter)((object, targetType, context) -> {
         Optional<Argument<?>> typeVariable = context.getFirstTypeVariable();
         Argument<?> componentType = (Argument)typeVariable.orElse(Argument.OBJECT_ARGUMENT);
         ConversionContext newContext = context.with(componentType);
         Class<?> targetComponentType = ReflectionUtils.getWrapperType(componentType.getType());
         String[] strings = object.toString().split(",");
         List list = new ArrayList();

         for(String string : strings) {
            Optional converted = this.convert(string, targetComponentType, newContext);
            if (converted.isPresent()) {
               list.add(converted.get());
            }
         }

         return CollectionUtils.convertCollection(targetType, list);
      }));
      TypeConverter<Object, Optional> objectToOptionalConverter = (object, targetType, context) -> {
         Optional<Argument<?>> typeVariable = context.getFirstTypeVariable();
         Argument<?> componentType = (Argument)typeVariable.orElse(Argument.OBJECT_ARGUMENT);
         Class<?> targetComponentType = ReflectionUtils.getWrapperType(componentType.getType());
         ConversionContext newContext = context.with(componentType).with(context.getAnnotationMetadata());
         Optional converted = this.convert(object, targetComponentType, newContext);
         return converted.isPresent() ? Optional.of(converted) : Optional.of(Optional.empty());
      };
      this.addConverter(Object.class, Optional.class, objectToOptionalConverter);
      this.addConverter(Object.class, OptionalInt.class, (TypeConverter)((object, targetType, context) -> {
         Optional<Integer> converted = this.convert(object, Integer.class, context);
         return (Optional)converted.map(integer -> Optional.of(OptionalInt.of(integer))).orElseGet(() -> Optional.of(OptionalInt.empty()));
      }));
      this.addConverter(Object.class, OptionalLong.class, (TypeConverter)((object, targetType, context) -> {
         Optional<Long> converted = this.convert(object, Long.class, context);
         return (Optional)converted.map(longValue -> Optional.of(OptionalLong.of(longValue))).orElseGet(() -> Optional.of(OptionalLong.empty()));
      }));
      this.addConverter(Iterable.class, String.class, (TypeConverter)((object, targetType, context) -> Optional.of(CollectionUtils.toString(object))));
      this.addConverter(
         Iterable.class,
         Object.class,
         (TypeConverter)((object, targetType, context) -> {
            if (Optional.class.isAssignableFrom(targetType)) {
               return objectToOptionalConverter.convert(object, targetType, context);
            } else {
               Iterator<?> i = object.iterator();
               int count = 0;
   
               Object value;
               for(value = null; i.hasNext(); value = i.next()) {
                  if (count > 0) {
                     context.reject(
                        object,
                        new ConversionErrorException(
                           Argument.of(targetType),
                           new IllegalArgumentException("Cannot convert an iterable with more than 1 value to a non collection object")
                        )
                     );
                     return Optional.empty();
                  }
   
                  ++count;
               }
   
               return this.convert(value, targetType, context);
            }
         })
      );
      this.addConverter(Iterable.class, Iterable.class, (TypeConverter)((object, targetType, context) -> {
         if (ConvertibleValues.class.isAssignableFrom(targetType)) {
            return object instanceof ConvertibleValues ? Optional.of(object) : Optional.empty();
         } else {
            Optional<Argument<?>> typeVariable = context.getFirstTypeVariable();
            Argument<?> componentType = (Argument)typeVariable.orElse(Argument.OBJECT_ARGUMENT);
            Class<?> targetComponentType = ReflectionUtils.getWrapperType(componentType.getType());
            if (targetType.isInstance(object) && targetComponentType == Object.class) {
               return Optional.of(object);
            } else {
               List list = new ArrayList();
               ConversionContext newContext = context.with(componentType);

               for(Object o : object) {
                  Optional<?> converted = this.convert(o, targetComponentType, newContext);
                  if (converted.isPresent()) {
                     list.add(converted.get());
                  }
               }

               return CollectionUtils.convertCollection(targetType, list);
            }
         }
      }));
      this.addConverter(Object[].class, String.class, (TypeConverter)((object, targetType, context) -> Optional.of(ArrayUtils.toString(object))));
      this.addConverter(Object[].class, Object[].class, (TypeConverter)((object, targetType, context) -> {
         Class<?> targetComponentType = targetType.getComponentType();
         List results = new ArrayList();

         for(Object o : object) {
            Optional<?> converted = this.convert(o, targetComponentType, context);
            if (converted.isPresent()) {
               results.add(converted.get());
            }
         }

         return Optional.of(results.toArray(Array.newInstance(targetComponentType, results.size())));
      }));
      this.addConverter(Iterable.class, Object[].class, (TypeConverter)((object, targetType, context) -> {
         Class<?> targetComponentType = targetType.getComponentType();
         List results = new ArrayList();

         for(Object o : object) {
            Optional<?> converted = this.convert(o, targetComponentType, context);
            if (converted.isPresent()) {
               results.add(converted.get());
            }
         }

         return Optional.of(results.toArray(Array.newInstance(targetComponentType, results.size())));
      }));
      this.addConverter(
         Object[].class, Iterable.class, (TypeConverter)((object, targetType, context) -> this.convert(Arrays.asList(object), targetType, context))
      );
      this.addConverter(Object.class, Object[].class, (TypeConverter)((object, targetType, context) -> {
         Class<?> targetComponentType = targetType.getComponentType();
         Optional<?> converted = this.convert(object, targetComponentType);
         if (converted.isPresent()) {
            Object[] result = Array.newInstance(targetComponentType, 1);
            result[0] = converted.get();
            return Optional.of(result);
         } else {
            return Optional.empty();
         }
      }));
      this.addConverter(
         Map.class,
         Map.class,
         (TypeConverter)((object, targetType, context) -> {
            Argument<?> keyArgument = (Argument)context.getTypeVariable("K").orElse(Argument.of(String.class, "K"));
            boolean isProperties = targetType.equals(Properties.class);
            Argument<?> valArgument = (Argument)context.getTypeVariable("V")
               .orElseGet(() -> isProperties ? Argument.of(String.class, "V") : Argument.of(Object.class, "V"));
            Class keyType = keyArgument.getType();
            Class valueType = valArgument.getType();
            ConversionContext keyContext = context.with(keyArgument);
            ConversionContext valContext = context.with(valArgument);
            Map newMap = (Map)(isProperties ? new Properties() : new LinkedHashMap());
            Iterator var12 = object.entrySet().iterator();
   
            while(true) {
               Object key;
               Object value;
               while(true) {
                  while(true) {
                     if (!var12.hasNext()) {
                        return Optional.of(newMap);
                     }
   
                     Object o = var12.next();
                     Entry entry = (Entry)o;
                     key = entry.getKey();
                     value = entry.getValue();
                     if (keyType.isInstance(key)) {
                        break;
                     }
   
                     Optional convertedKey = this.convert(key, keyType, keyContext);
                     if (convertedKey.isPresent()) {
                        key = convertedKey.get();
                        break;
                     }
                  }
   
                  if (valueType.isInstance(value) && !(value instanceof Map) && !(value instanceof Collection)) {
                     break;
                  }
   
                  Optional converted = this.convert(value, valueType, valContext);
                  if (converted.isPresent()) {
                     value = converted.get();
                     break;
                  }
               }
   
               newMap.put(key, value);
            }
         })
      );
      this.addConverter(Map.class, ConvertibleValues.class, (TypeConverter)((object, targetType, context) -> Optional.of(new ConvertibleValuesMap(object))));
      this.addConverter(io.micronaut.core.io.buffer.ByteBuffer.class, byte[].class, (TypeConverter)((object, targetType, context) -> {
         byte[] result = object.toByteArray();
         ((ReferenceCounted)object).release();
         return Optional.of(result);
      }));
      this.addConverter(ConvertibleMultiValues.class, Iterable.class, new MultiValuesConverterFactory.MultiValuesToIterableConverter(this));
      this.addConverter(ConvertibleMultiValues.class, Map.class, new MultiValuesConverterFactory.MultiValuesToMapConverter(this));
      this.addConverter(ConvertibleMultiValues.class, Object.class, new MultiValuesConverterFactory.MultiValuesToObjectConverter(this));
      this.addConverter(Iterable.class, ConvertibleMultiValues.class, new MultiValuesConverterFactory.IterableToMultiValuesConverter(this));
      this.addConverter(Map.class, ConvertibleMultiValues.class, new MultiValuesConverterFactory.MapToMultiValuesConverter(this));
      this.addConverter(Object.class, ConvertibleMultiValues.class, new MultiValuesConverterFactory.ObjectToMultiValuesConverter(this));
   }

   protected <T> TypeConverter findTypeConverter(Class<?> sourceType, Class<T> targetType, String formattingAnnotation) {
      TypeConverter typeConverter = UNCONVERTIBLE;
      List<Class> sourceHierarchy = ClassUtils.resolveHierarchy(sourceType);
      List<Class> targetHierarchy = ClassUtils.resolveHierarchy(targetType);

      for(Class sourceSuperType : sourceHierarchy) {
         for(Class targetSuperType : targetHierarchy) {
            DefaultConversionService.ConvertiblePair pair = new DefaultConversionService.ConvertiblePair(sourceSuperType, targetSuperType, formattingAnnotation);
            typeConverter = (TypeConverter)this.typeConverters.get(pair);
            if (typeConverter != null) {
               this.converterCache.put(pair, typeConverter);
               return typeConverter;
            }
         }
      }

      boolean hasFormatting = formattingAnnotation != null;
      if (hasFormatting) {
         for(Class sourceSuperType : sourceHierarchy) {
            for(Class targetSuperType : targetHierarchy) {
               DefaultConversionService.ConvertiblePair pair = new DefaultConversionService.ConvertiblePair(sourceSuperType, targetSuperType);
               typeConverter = (TypeConverter)this.typeConverters.get(pair);
               if (typeConverter != null) {
                  this.converterCache.put(pair, typeConverter);
                  return typeConverter;
               }
            }
         }
      }

      return typeConverter;
   }

   private SimpleDateFormat resolveFormat(ConversionContext context) {
      AnnotationMetadata annotationMetadata = context.getAnnotationMetadata();
      Optional<String> format = annotationMetadata.stringValue(Format.class);
      return (SimpleDateFormat)format.map(pattern -> new SimpleDateFormat(pattern, context.getLocale()))
         .orElseGet(() -> new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", context.getLocale()));
   }

   private <S, T> DefaultConversionService.ConvertiblePair newPair(Class<S> sourceType, Class<T> targetType, TypeConverter<S, T> typeConverter) {
      DefaultConversionService.ConvertiblePair pair;
      if (typeConverter instanceof FormattingTypeConverter) {
         pair = new DefaultConversionService.ConvertiblePair(sourceType, targetType, ((FormattingTypeConverter)typeConverter).annotationType().getName());
      } else {
         pair = new DefaultConversionService.ConvertiblePair(sourceType, targetType);
      }

      return pair;
   }

   private final class ConvertiblePair {
      final Class source;
      final Class target;
      final String formattingAnnotation;

      ConvertiblePair(Class source, Class target) {
         this(source, target, null);
      }

      ConvertiblePair(Class source, Class target, String formattingAnnotation) {
         this.source = source;
         this.target = target;
         this.formattingAnnotation = formattingAnnotation;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            DefaultConversionService.ConvertiblePair pair = (DefaultConversionService.ConvertiblePair)o;
            if (!this.source.equals(pair.source)) {
               return false;
            } else if (!this.target.equals(pair.target)) {
               return false;
            } else {
               return this.formattingAnnotation != null ? this.formattingAnnotation.equals(pair.formattingAnnotation) : pair.formattingAnnotation == null;
            }
         } else {
            return false;
         }
      }

      public int hashCode() {
         int result = this.source.hashCode();
         result = 31 * result + this.target.hashCode();
         return 31 * result + (this.formattingAnnotation != null ? this.formattingAnnotation.hashCode() : 0);
      }
   }
}

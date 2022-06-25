package io.micronaut.core.convert.converters;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanConstructor;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.beans.BeanWrapper;
import io.micronaut.core.beans.exceptions.IntrospectionException;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.format.Format;
import io.micronaut.core.convert.format.FormattingTypeConverter;
import io.micronaut.core.convert.value.ConvertibleMultiValues;
import io.micronaut.core.convert.value.MutableConvertibleMultiValuesMap;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MultiValuesConverterFactory {
   public static final String FORMAT_CSV = "csv";
   public static final String FORMAT_SSV = "ssv";
   public static final String FORMAT_PIPES = "pipes";
   public static final String FORMAT_MULTI = "multi";
   public static final String FORMAT_DEEP_OBJECT = "deepobject";
   private static final Character CSV_DELIMITER = ',';
   private static final Character SSV_DELIMITER = ' ';
   private static final Character PIPES_DELIMITER = '|';

   private static String normalizeFormatName(String value) {
      return value.toLowerCase().replaceAll("[-_]", "");
   }

   private static Map<String, String> getSeparatedMapParameters(
      ConvertibleMultiValues<String> parameters, String name, String defaultValue, Character delimiter
   ) {
      List<String> paramValues = parameters.getAll(name);
      if (paramValues.isEmpty() && defaultValue != null) {
         paramValues.add(defaultValue);
      }

      Map<String, String> values = new HashMap();

      for(String value : paramValues) {
         List<String> delimited = splitByDelimiter(value, delimiter);

         for(int i = 1; i < delimited.size(); i += 2) {
            values.put(delimited.get(i - 1), delimited.get(i));
         }
      }

      return values;
   }

   private static Map<String, String> getMultiMapParameters(ConvertibleMultiValues<String> parameters) {
      return (Map<String, String>)parameters.asMap()
         .entrySet()
         .stream()
         .filter(v -> !((List)v.getValue()).isEmpty())
         .collect(Collectors.toMap(Entry::getKey, v -> (String)((List)v.getValue()).get(0)));
   }

   private static Map<String, String> getDeepObjectMapParameters(ConvertibleMultiValues<String> parameters, String name) {
      Map<String, List<String>> paramValues = parameters.asMap();
      Map<String, String> values = new HashMap();

      for(Entry<String, List<String>> param : paramValues.entrySet()) {
         String key = (String)param.getKey();
         if (key.startsWith(name)
            && key.length() > name.length()
            && key.charAt(name.length()) == '['
            && key.charAt(key.length() - 1) == ']'
            && !((List)param.getValue()).isEmpty()) {
            String mapKey = key.substring(name.length() + 1, key.length() - 1);
            values.put(mapKey, ((List)param.getValue()).get(0));
         }
      }

      return values;
   }

   private static List<String> splitByDelimiter(String value, Character delimiter) {
      List<String> result = new ArrayList();
      int startI = 0;

      for(int i = 0; i < value.length(); ++i) {
         if (value.charAt(i) == delimiter) {
            result.add(value.substring(startI, i));
            startI = i + 1;
         }
      }

      if (startI != 0) {
         result.add(value.substring(startI));
      }

      return result;
   }

   private static String joinStrings(Iterable<String> strings, Character delimiter) {
      if (strings == null) {
         return "";
      } else {
         StringBuilder builder = new StringBuilder();
         boolean first = true;

         for(String value : strings) {
            if (value != null) {
               if (!first) {
                  builder.append(delimiter);
               } else {
                  first = false;
               }

               builder.append(value);
            }
         }

         return builder.toString();
      }
   }

   private abstract static class AbstractConverterFromMultiValues<T> implements FormattingTypeConverter<ConvertibleMultiValues, T, Format> {
      protected ConversionService<?> conversionService;

      AbstractConverterFromMultiValues(ConversionService<?> conversionService) {
         this.conversionService = conversionService;
      }

      public Optional<T> convert(ConvertibleMultiValues object, Class<T> targetType, ConversionContext conversionContext) {
         if (!(conversionContext instanceof ArgumentConversionContext)) {
            return Optional.empty();
         } else {
            ArgumentConversionContext<T> context = (ArgumentConversionContext)conversionContext;
            String format = (String)conversionContext.getAnnotationMetadata().getValue(Format.class, String.class).orElse(null);
            if (format == null) {
               return Optional.empty();
            } else {
               String name = (String)conversionContext.getAnnotationMetadata().getValue(Bindable.class, String.class).orElse(context.getArgument().getName());
               String defaultValue = (String)conversionContext.getAnnotationMetadata().getValue(Bindable.class, "defaultValue", String.class).orElse(null);
               String var9 = MultiValuesConverterFactory.normalizeFormatName(format);
               switch(var9) {
                  case "csv":
                     return this.retrieveSeparatedValue(context, name, object, defaultValue, MultiValuesConverterFactory.CSV_DELIMITER);
                  case "ssv":
                     return this.retrieveSeparatedValue(context, name, object, defaultValue, MultiValuesConverterFactory.SSV_DELIMITER);
                  case "pipes":
                     return this.retrieveSeparatedValue(context, name, object, defaultValue, MultiValuesConverterFactory.PIPES_DELIMITER);
                  case "multi":
                     return this.retrieveMultiValue(context, name, object);
                  case "deepobject":
                     return this.retrieveDeepObjectValue(context, name, object);
                  default:
                     return Optional.empty();
               }
            }
         }
      }

      protected abstract Optional<T> retrieveSeparatedValue(
         ArgumentConversionContext<T> conversionContext,
         String name,
         ConvertibleMultiValues<String> parameters,
         @Nullable String defaultValue,
         Character delimiter
      );

      protected abstract Optional<T> retrieveMultiValue(ArgumentConversionContext<T> conversionContext, String name, ConvertibleMultiValues<String> parameters);

      protected abstract Optional<T> retrieveDeepObjectValue(
         ArgumentConversionContext<T> conversionContext, String name, ConvertibleMultiValues<String> parameters
      );

      @Override
      public Class<Format> annotationType() {
         return Format.class;
      }
   }

   public abstract static class AbstractConverterToMultiValues<T> implements FormattingTypeConverter<T, ConvertibleMultiValues, Format> {
      protected ConversionService<?> conversionService;

      public AbstractConverterToMultiValues(ConversionService<?> conversionService) {
         this.conversionService = conversionService;
      }

      @Override
      public Optional<ConvertibleMultiValues> convert(T object, Class<ConvertibleMultiValues> targetType, ConversionContext conversionContext) {
         if (targetType.isAssignableFrom(MutableConvertibleMultiValuesMap.class) && conversionContext instanceof ArgumentConversionContext) {
            ArgumentConversionContext<Object> context = (ArgumentConversionContext)conversionContext;
            String format = (String)conversionContext.getAnnotationMetadata().getValue(Format.class, String.class).orElse(null);
            if (format == null) {
               return Optional.empty();
            } else {
               String name = (String)conversionContext.getAnnotationMetadata().getValue(Bindable.class, String.class).orElse(context.getArgument().getName());
               MutableConvertibleMultiValuesMap<String> parameters = new MutableConvertibleMultiValuesMap();
               if (object == null) {
                  return Optional.of(parameters);
               } else {
                  String var8 = MultiValuesConverterFactory.normalizeFormatName(format);
                  switch(var8) {
                     case "csv":
                        this.addSeparatedValues(context, name, object, parameters, MultiValuesConverterFactory.CSV_DELIMITER);
                        break;
                     case "ssv":
                        this.addSeparatedValues(context, name, object, parameters, MultiValuesConverterFactory.SSV_DELIMITER);
                        break;
                     case "pipes":
                        this.addSeparatedValues(context, name, object, parameters, MultiValuesConverterFactory.PIPES_DELIMITER);
                        break;
                     case "multi":
                        this.addMutliValues(context, name, object, parameters);
                        break;
                     case "deepobject":
                        this.addDeepObjectValues(context, name, object, parameters);
                        break;
                     default:
                        return Optional.empty();
                  }

                  return Optional.of(parameters);
               }
            }
         } else {
            return Optional.empty();
         }
      }

      protected abstract void addSeparatedValues(
         ArgumentConversionContext<Object> context, String name, T object, MutableConvertibleMultiValuesMap<String> parameters, Character delimiter
      );

      protected abstract void addMutliValues(
         ArgumentConversionContext<Object> context, String name, T object, MutableConvertibleMultiValuesMap<String> parameters
      );

      protected abstract void addDeepObjectValues(
         ArgumentConversionContext<Object> context, String name, T object, MutableConvertibleMultiValuesMap<String> parameters
      );

      @Override
      public Class<Format> annotationType() {
         return Format.class;
      }
   }

   public static class IterableToMultiValuesConverter extends MultiValuesConverterFactory.AbstractConverterToMultiValues<Iterable> {
      public IterableToMultiValuesConverter(ConversionService<?> conversionService) {
         super(conversionService);
      }

      private void processValues(ArgumentConversionContext<Object> context, Iterable object, Consumer<String> consumer) {
         ArgumentConversionContext<String> conversionContext = ConversionContext.STRING
            .with(
               (AnnotationMetadata)context.getFirstTypeVariable()
                  .map(AnnotationMetadataProvider::getAnnotationMetadata)
                  .orElse(AnnotationMetadata.EMPTY_METADATA)
            );

         for(Object value : object) {
            this.conversionService.convert(value, conversionContext).ifPresent(v -> consumer.accept(v));
         }

      }

      protected void addSeparatedValues(
         ArgumentConversionContext<Object> context, String name, Iterable object, MutableConvertibleMultiValuesMap<String> parameters, Character delimiter
      ) {
         List<String> strings = new ArrayList();
         this.processValues(context, object, v -> strings.add(v));
         parameters.add(name, MultiValuesConverterFactory.joinStrings(strings, delimiter));
      }

      protected void addMutliValues(
         ArgumentConversionContext<Object> context, String name, Iterable object, MutableConvertibleMultiValuesMap<String> parameters
      ) {
         this.processValues(context, object, v -> parameters.add(name, v));
      }

      protected void addDeepObjectValues(
         ArgumentConversionContext<Object> context, String name, Iterable object, MutableConvertibleMultiValuesMap<String> parameters
      ) {
         ArgumentConversionContext<String> conversionContext = ConversionContext.STRING
            .with(
               (AnnotationMetadata)context.getFirstTypeVariable()
                  .map(AnnotationMetadataProvider::getAnnotationMetadata)
                  .orElse(AnnotationMetadata.EMPTY_METADATA)
            );
         int i = 0;

         for(Object value : object) {
            String stringValue = (String)this.conversionService.convert(value, conversionContext).orElse("");
            parameters.add(name + "[" + i + "]", stringValue);
            ++i;
         }

      }
   }

   public static class MapToMultiValuesConverter extends MultiValuesConverterFactory.AbstractConverterToMultiValues<Map> {
      public MapToMultiValuesConverter(ConversionService<?> conversionService) {
         super(conversionService);
      }

      private void processValues(ArgumentConversionContext<Object> context, Map object, BiConsumer<String, String> consumer) {
         Argument<?>[] typeParameters = context.getTypeParameters();
         ArgumentConversionContext<String> keyConversionContext = ConversionContext.STRING
            .with(typeParameters.length > 0 ? typeParameters[0].getAnnotationMetadata() : AnnotationMetadata.EMPTY_METADATA);
         ArgumentConversionContext<String> valueConversionContext = ConversionContext.STRING
            .with(typeParameters.length > 1 ? typeParameters[1].getAnnotationMetadata() : AnnotationMetadata.EMPTY_METADATA);

         for(Entry<Object, Object> value : object.entrySet()) {
            this.conversionService
               .convert(value.getValue(), valueConversionContext)
               .ifPresent(v -> this.conversionService.convert(value.getKey(), keyConversionContext).ifPresent(k -> consumer.accept(k, v)));
         }

      }

      protected void addSeparatedValues(
         ArgumentConversionContext<Object> context, String name, Map object, MutableConvertibleMultiValuesMap<String> parameters, Character delimiter
      ) {
         List<String> values = new ArrayList();
         this.processValues(context, object, (k, v) -> {
            values.add(k);
            values.add(v);
         });
         parameters.add(name, MultiValuesConverterFactory.joinStrings(values, delimiter));
      }

      protected void addMutliValues(ArgumentConversionContext<Object> context, String name, Map object, MutableConvertibleMultiValuesMap<String> parameters) {
         this.processValues(context, object, parameters::add);
      }

      protected void addDeepObjectValues(
         ArgumentConversionContext<Object> context, String name, Map object, MutableConvertibleMultiValuesMap<String> parameters
      ) {
         this.processValues(context, object, (k, v) -> parameters.add(name + "[" + k + "]", v));
      }
   }

   public static class MultiValuesToIterableConverter extends MultiValuesConverterFactory.AbstractConverterFromMultiValues<Iterable> {
      public MultiValuesToIterableConverter(ConversionService<?> conversionService) {
         super(conversionService);
      }

      @Override
      protected Optional<Iterable> retrieveSeparatedValue(
         ArgumentConversionContext<Iterable> conversionContext,
         String name,
         ConvertibleMultiValues<String> parameters,
         String defaultValue,
         Character delimiter
      ) {
         List<String> values = parameters.getAll(name);
         if (values.isEmpty() && defaultValue != null) {
            values.add(defaultValue);
         }

         List<String> result = new ArrayList(values.size());

         for(String value : values) {
            result.addAll(MultiValuesConverterFactory.splitByDelimiter(value, delimiter));
         }

         return this.convertValues(conversionContext, result);
      }

      @Override
      protected Optional<Iterable> retrieveMultiValue(
         ArgumentConversionContext<Iterable> conversionContext, String name, ConvertibleMultiValues<String> parameters
      ) {
         List<String> values = parameters.getAll(name);
         return this.convertValues(conversionContext, values);
      }

      @Override
      protected Optional<Iterable> retrieveDeepObjectValue(
         ArgumentConversionContext<Iterable> conversionContext, String name, ConvertibleMultiValues<String> parameters
      ) {
         List<String> values = new ArrayList();
         int i = 0;

         while(true) {
            String key = name + '[' + i + ']';
            String value = (String)parameters.get(key);
            if (value == null) {
               return this.convertValues(conversionContext, values);
            }

            values.add(value);
            ++i;
         }
      }

      private Optional<Iterable> convertValues(ArgumentConversionContext<Iterable> context, List<String> values) {
         Argument<?> typeArgument = (Argument)context.getArgument().getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
         List convertedValues;
         if (typeArgument.getType().isAssignableFrom(String.class)) {
            convertedValues = values;
         } else {
            ArgumentConversionContext<?> argumentConversionContext = ConversionContext.of(typeArgument);
            convertedValues = new ArrayList(values.size());

            for(String value : values) {
               this.conversionService.convert(value, argumentConversionContext).ifPresent(convertedValues::add);
            }
         }

         return CollectionUtils.convertCollection(context.getArgument().getType(), convertedValues);
      }
   }

   public static class MultiValuesToMapConverter extends MultiValuesConverterFactory.AbstractConverterFromMultiValues<Map> {
      public MultiValuesToMapConverter(ConversionService<?> conversionService) {
         super(conversionService);
      }

      @Override
      protected Optional<Map> retrieveSeparatedValue(
         ArgumentConversionContext<Map> conversionContext, String name, ConvertibleMultiValues<String> parameters, String defaultValue, Character delimiter
      ) {
         Map<String, String> values = MultiValuesConverterFactory.getSeparatedMapParameters(parameters, name, defaultValue, delimiter);
         return this.convertValues(conversionContext, values);
      }

      @Override
      protected Optional<Map> retrieveMultiValue(ArgumentConversionContext<Map> conversionContext, String name, ConvertibleMultiValues<String> parameters) {
         Map<String, String> values = MultiValuesConverterFactory.getMultiMapParameters(parameters);
         return this.convertValues(conversionContext, values);
      }

      @Override
      protected Optional<Map> retrieveDeepObjectValue(ArgumentConversionContext<Map> conversionContext, String name, ConvertibleMultiValues<String> parameters) {
         Map<String, String> values = MultiValuesConverterFactory.getDeepObjectMapParameters(parameters, name);
         return this.convertValues(conversionContext, values);
      }

      private Optional<Map> convertValues(ArgumentConversionContext<Map> context, Map<String, String> values) {
         if (!context.getArgument().getType().isAssignableFrom(values.getClass())) {
            return Optional.empty();
         } else {
            Argument<?>[] typeArguments = context.getTypeParameters();
            Argument<?> keyArgument = typeArguments.length > 0 ? typeArguments[0] : Argument.OBJECT_ARGUMENT;
            Argument<?> valueArgument = typeArguments.length > 1 ? typeArguments[1] : Argument.OBJECT_ARGUMENT;
            Map convertedValues;
            if (keyArgument.getType().isAssignableFrom(String.class) && valueArgument.getType().isAssignableFrom(String.class)) {
               convertedValues = values;
            } else {
               ArgumentConversionContext<?> keyContext = ConversionContext.of(keyArgument);
               ArgumentConversionContext<?> valueContext = ConversionContext.of(valueArgument);
               convertedValues = new HashMap();

               for(Entry<String, String> entry : values.entrySet()) {
                  Object value = this.conversionService.convert(entry.getValue(), valueContext).orElse(null);
                  if (value != null) {
                     Object key = this.conversionService.convert(entry.getKey(), keyContext).orElse(null);
                     if (key != null) {
                        convertedValues.put(key, value);
                     }
                  }
               }
            }

            return Optional.of(convertedValues);
         }
      }
   }

   public static class MultiValuesToObjectConverter extends MultiValuesConverterFactory.AbstractConverterFromMultiValues<Object> {
      public MultiValuesToObjectConverter(ConversionService<?> conversionService) {
         super(conversionService);
      }

      @Override
      protected Optional<Object> retrieveSeparatedValue(
         ArgumentConversionContext<Object> conversionContext, String name, ConvertibleMultiValues<String> parameters, String defaultValue, Character delimiter
      ) {
         Map<String, String> values = MultiValuesConverterFactory.getSeparatedMapParameters(parameters, name, defaultValue, delimiter);
         return this.convertValues(conversionContext, values);
      }

      @Override
      protected Optional<Object> retrieveMultiValue(ArgumentConversionContext<Object> conversionContext, String name, ConvertibleMultiValues<String> parameters) {
         Map<String, String> values = MultiValuesConverterFactory.getMultiMapParameters(parameters);
         return this.convertValues(conversionContext, values);
      }

      @Override
      protected Optional<Object> retrieveDeepObjectValue(
         ArgumentConversionContext<Object> conversionContext, String name, ConvertibleMultiValues<String> parameters
      ) {
         Map<String, String> values = MultiValuesConverterFactory.getDeepObjectMapParameters(parameters, name);
         return this.convertValues(conversionContext, values);
      }

      private Optional<Object> convertValues(ArgumentConversionContext<Object> context, Map<String, String> values) {
         try {
            BeanIntrospection introspection = BeanIntrospection.getIntrospection(context.getArgument().getType());
            BeanConstructor<?> constructor = introspection.getConstructor();
            Argument<?>[] constructorArguments = constructor.getArguments();
            Object[] constructorParameters = new Object[constructorArguments.length];

            for(int i = 0; i < constructorArguments.length; ++i) {
               Argument<?> argument = constructorArguments[i];
               String name = (String)argument.getAnnotationMetadata().getValue(Bindable.class, String.class).orElse(argument.getName());
               constructorParameters[i] = this.conversionService.convert(values.get(name), ConversionContext.of(argument)).orElse(null);
            }

            Object result = constructor.instantiate(constructorParameters);
            BeanWrapper<Object> wrapper = BeanWrapper.getWrapper(result);

            for(BeanProperty<Object, Object> property : wrapper.getBeanProperties()) {
               String name = property.getName();
               if (!property.isReadOnly() && values.containsKey(name)) {
                  this.conversionService.convert(values.get(name), ConversionContext.of(property.asArgument())).ifPresent(v -> property.set(result, v));
               }
            }

            return Optional.of(result);
         } catch (IntrospectionException var12) {
            context.reject(values, var12);
            return Optional.empty();
         }
      }
   }

   public static class ObjectToMultiValuesConverter extends MultiValuesConverterFactory.AbstractConverterToMultiValues<Object> {
      public ObjectToMultiValuesConverter(ConversionService<?> conversionService) {
         super(conversionService);
      }

      private void processValues(ArgumentConversionContext<Object> context, Object object, BiConsumer<String, String> consumer) {
         BeanWrapper<Object> beanWrapper;
         try {
            beanWrapper = BeanWrapper.getWrapper(object);
         } catch (IntrospectionException var9) {
            context.reject(object, var9);
            return;
         }

         for(BeanProperty<Object, Object> property : beanWrapper.getBeanProperties()) {
            String key = (String)property.getValue(Bindable.class, String.class).orElse(property.getName());
            ArgumentConversionContext<String> conversionContext = ConversionContext.STRING.with(property.getAnnotationMetadata());
            this.conversionService.convert(property.get(object), conversionContext).ifPresent(value -> consumer.accept(key, value));
         }

      }

      @Override
      protected void addSeparatedValues(
         ArgumentConversionContext<Object> context, String name, Object object, MutableConvertibleMultiValuesMap<String> parameters, Character delimiter
      ) {
         List<String> values = new ArrayList();
         this.processValues(context, object, (k, v) -> {
            values.add(k);
            values.add(v);
         });
         parameters.add(name, MultiValuesConverterFactory.joinStrings(values, delimiter));
      }

      @Override
      protected void addMutliValues(ArgumentConversionContext<Object> context, String name, Object object, MutableConvertibleMultiValuesMap<String> parameters) {
         this.processValues(context, object, parameters::add);
      }

      @Override
      protected void addDeepObjectValues(
         ArgumentConversionContext<Object> context, String name, Object object, MutableConvertibleMultiValuesMap<String> parameters
      ) {
         this.processValues(context, object, (k, v) -> parameters.add(name + "[" + k + "]", v));
      }
   }
}

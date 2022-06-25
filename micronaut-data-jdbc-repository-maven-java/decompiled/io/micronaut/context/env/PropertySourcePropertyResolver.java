package io.micronaut.context.env;

import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.format.MapFormat;
import io.micronaut.core.io.socket.SocketUtils;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.naming.conventions.StringConvention;
import io.micronaut.core.optim.StaticOptimizations;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.EnvironmentProperties;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.value.MapPropertyResolver;
import io.micronaut.core.value.PropertyResolver;
import io.micronaut.core.value.ValueException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;

public class PropertySourcePropertyResolver implements PropertyResolver, AutoCloseable {
   private static final Logger LOG = ClassUtils.getLogger(PropertySourcePropertyResolver.class);
   private static final EnvironmentProperties CURRENT_ENV = (EnvironmentProperties)StaticOptimizations.get(EnvironmentProperties.class)
      .orElseGet(EnvironmentProperties::empty);
   private static final Pattern DOT_PATTERN = Pattern.compile("\\.");
   private static final String RANDOM_PREFIX = "\\s?random\\.(\\S+?)";
   private static final String RANDOM_UPPER_LIMIT = "(\\(-?\\d+(\\.\\d+)?\\))";
   private static final String RANDOM_RANGE = "(\\[-?\\d+(\\.\\d+)?,\\s?-?\\d+(\\.\\d+)?])";
   private static final Pattern RANDOM_PATTERN = Pattern.compile(
      "\\$\\{\\s?random\\.(\\S+?)((\\(-?\\d+(\\.\\d+)?\\))|(\\[-?\\d+(\\.\\d+)?,\\s?-?\\d+(\\.\\d+)?]))?\\}"
   );
   private static final Object NO_VALUE = new Object();
   private static final PropertySourcePropertyResolver.PropertyCatalog[] CONVENTIONS = new PropertySourcePropertyResolver.PropertyCatalog[]{
      PropertySourcePropertyResolver.PropertyCatalog.GENERATED, PropertySourcePropertyResolver.PropertyCatalog.RAW
   };
   protected final ConversionService<?> conversionService;
   protected final PropertyPlaceholderResolver propertyPlaceholderResolver;
   protected final Map<String, PropertySource> propertySources = new ConcurrentHashMap(10);
   protected final Map<String, Object>[] catalog = new Map[58];
   protected final Map<String, Object>[] rawCatalog = new Map[58];
   protected final Map<String, Object>[] nonGenerated = new Map[58];
   private final Random random = new Random();
   private final Map<String, Boolean> containsCache = new ConcurrentHashMap(20);
   private final Map<String, Object> resolvedValueCache = new ConcurrentHashMap(20);
   private final EnvironmentProperties environmentProperties = EnvironmentProperties.fork(CURRENT_ENV);

   public PropertySourcePropertyResolver(ConversionService<?> conversionService) {
      this.conversionService = conversionService;
      this.propertyPlaceholderResolver = new DefaultPropertyPlaceholderResolver(this, conversionService);
   }

   public PropertySourcePropertyResolver() {
      this(ConversionService.SHARED);
   }

   public PropertySourcePropertyResolver(PropertySource... propertySources) {
      this(ConversionService.SHARED);
      if (propertySources != null) {
         for(PropertySource propertySource : propertySources) {
            this.addPropertySource(propertySource);
         }
      }

   }

   public PropertySourcePropertyResolver addPropertySource(@Nullable PropertySource propertySource) {
      if (propertySource != null) {
         this.processPropertySource(propertySource, propertySource.getConvention());
      }

      return this;
   }

   public PropertySourcePropertyResolver addPropertySource(String name, @Nullable Map<String, ? super Object> values) {
      return CollectionUtils.isNotEmpty(values) ? this.addPropertySource(PropertySource.of(name, values)) : this;
   }

   @Override
   public boolean containsProperty(@Nullable String name) {
      if (StringUtils.isEmpty(name)) {
         return false;
      } else {
         Boolean result = (Boolean)this.containsCache.get(name);
         if (result == null) {
            for(PropertySourcePropertyResolver.PropertyCatalog convention : CONVENTIONS) {
               Map<String, Object> entries = this.resolveEntriesForKey(name, false, convention);
               if (entries != null && entries.containsKey(name)) {
                  result = true;
                  break;
               }
            }

            if (result == null) {
               result = false;
            }

            this.containsCache.put(name, result);
         }

         return result;
      }
   }

   @Override
   public boolean containsProperties(@Nullable String name) {
      if (!StringUtils.isEmpty(name)) {
         for(PropertySourcePropertyResolver.PropertyCatalog propertyCatalog : CONVENTIONS) {
            Map<String, Object> entries = this.resolveEntriesForKey(name, false, propertyCatalog);
            if (entries != null) {
               if (entries.containsKey(name)) {
                  return true;
               }

               String finalName = name + ".";

               for(String key : entries.keySet()) {
                  if (key.startsWith(finalName)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   @NonNull
   @Override
   public Collection<String> getPropertyEntries(@NonNull String name) {
      if (!StringUtils.isEmpty(name)) {
         Map<String, Object> entries = this.resolveEntriesForKey(name, false, PropertySourcePropertyResolver.PropertyCatalog.NORMALIZED);
         if (entries != null) {
            String prefix = name + '.';
            return (Collection<String>)entries.keySet().stream().filter(k -> k.startsWith(prefix)).map(k -> {
               String withoutPrefix = k.substring(prefix.length());
               int i = withoutPrefix.indexOf(46);
               return i > -1 ? withoutPrefix.substring(0, i) : withoutPrefix;
            }).collect(Collectors.toSet());
         }
      }

      return Collections.emptySet();
   }

   @NonNull
   @Override
   public Map<String, Object> getProperties(String name, StringConvention keyFormat) {
      if (!StringUtils.isEmpty(name)) {
         Map<String, Object> entries = this.resolveEntriesForKey(
            name,
            false,
            keyFormat == StringConvention.RAW ? PropertySourcePropertyResolver.PropertyCatalog.RAW : PropertySourcePropertyResolver.PropertyCatalog.GENERATED
         );
         if (entries != null) {
            if (keyFormat == null) {
               keyFormat = StringConvention.RAW;
            }

            return this.resolveSubMap(name, entries, ConversionContext.MAP, keyFormat, MapFormat.MapTransformation.FLAT);
         } else {
            entries = this.resolveEntriesForKey(name, false, PropertySourcePropertyResolver.PropertyCatalog.GENERATED);
            if (keyFormat == null) {
               keyFormat = StringConvention.RAW;
            }

            return entries == null
               ? Collections.emptyMap()
               : this.resolveSubMap(name, entries, ConversionContext.MAP, keyFormat, MapFormat.MapTransformation.FLAT);
         }
      } else {
         return Collections.emptyMap();
      }
   }

   @Override
   public <T> Optional<T> getProperty(@NonNull String name, @NonNull ArgumentConversionContext<T> conversionContext) {
      if (StringUtils.isEmpty(name)) {
         return Optional.empty();
      } else {
         Objects.requireNonNull(conversionContext, "Conversion context should not be null");
         Class<T> requiredType = conversionContext.getArgument().getType();
         boolean cacheableType = ClassUtils.isJavaLangType(requiredType);
         Object cached = cacheableType ? this.resolvedValueCache.get(this.cacheKey(name, requiredType)) : null;
         if (cached != null) {
            return cached == NO_VALUE ? Optional.empty() : Optional.of(cached);
         } else {
            Map<String, Object> entries = this.resolveEntriesForKey(name, false, PropertySourcePropertyResolver.PropertyCatalog.GENERATED);
            if (entries == null) {
               entries = this.resolveEntriesForKey(name, false, PropertySourcePropertyResolver.PropertyCatalog.RAW);
            }

            if (entries != null) {
               Object value = entries.get(name);
               if (value == null) {
                  value = entries.get(this.normalizeName(name));
                  if (value == null && name.indexOf(91) == -1) {
                     Map<String, Object> rawEntries = this.resolveEntriesForKey(name, false, PropertySourcePropertyResolver.PropertyCatalog.RAW);
                     value = rawEntries != null ? rawEntries.get(name) : null;
                     if (value != null) {
                        entries = rawEntries;
                     }
                  }
               }

               if (value == null) {
                  int i = name.indexOf(91);
                  if (i > -1 && name.endsWith("]")) {
                     String newKey = name.substring(0, i);
                     value = entries.get(newKey);
                     String index = name.substring(i + 1, name.length() - 1);
                     if (value != null) {
                        if (StringUtils.isNotEmpty(index)) {
                           if (value instanceof List) {
                              try {
                                 value = ((List)value).get(Integer.valueOf(index));
                              } catch (NumberFormatException var13) {
                              }
                           } else if (value instanceof Map) {
                              try {
                                 value = ((Map)value).get(index);
                              } catch (NumberFormatException var12) {
                              }
                           }
                        }
                     } else if (StringUtils.isNotEmpty(index)) {
                        String subKey = newKey + '.' + index;
                        value = entries.get(subKey);
                     }
                  }
               }

               if (value != null) {
                  value = this.resolvePlaceHoldersIfNecessary(value);
                  Optional<T> converted;
                  if (requiredType.isInstance(value) && !CollectionUtils.isIterableOrMap(requiredType)) {
                     converted = Optional.of(value);
                  } else {
                     converted = this.conversionService.convert(value, conversionContext);
                  }

                  if (LOG.isTraceEnabled()) {
                     if (converted.isPresent()) {
                        LOG.trace("Resolved value [{}] for property: {}", converted.get(), name);
                     } else {
                        LOG.trace("Resolved value [{}] cannot be converted to type [{}] for property: {}", value, conversionContext.getArgument(), name);
                     }
                  }

                  if (cacheableType) {
                     this.resolvedValueCache.put(this.cacheKey(name, requiredType), converted.orElse(NO_VALUE));
                  }

                  return converted;
               }

               if (cacheableType) {
                  this.resolvedValueCache.put(this.cacheKey(name, requiredType), NO_VALUE);
                  return Optional.empty();
               }

               if (Properties.class.isAssignableFrom(requiredType)) {
                  Properties properties = this.resolveSubProperties(name, entries, conversionContext);
                  return Optional.of(properties);
               }

               if (Map.class.isAssignableFrom(requiredType)) {
                  Map<String, Object> subMap = this.resolveSubMap(name, entries, conversionContext);
                  if (!subMap.isEmpty()) {
                     return this.conversionService.convert(subMap, requiredType, conversionContext);
                  }

                  return Optional.of(subMap);
               }

               if (PropertyResolver.class.isAssignableFrom(requiredType)) {
                  Map<String, Object> subMap = this.resolveSubMap(name, entries, conversionContext);
                  return Optional.of(new MapPropertyResolver(subMap, this.conversionService));
               }
            }

            if (LOG.isTraceEnabled()) {
               LOG.trace("No value found for property: {}", name);
            }

            requiredType = conversionContext.getArgument().getType();
            if (Properties.class.isAssignableFrom(requiredType)) {
               return Optional.of(new Properties());
            } else {
               return Map.class.isAssignableFrom(requiredType) ? Optional.of(Collections.emptyMap()) : Optional.empty();
            }
         }
      }
   }

   @NonNull
   private <T> String cacheKey(@NonNull String name, Class<T> requiredType) {
      return name + '|' + requiredType.getSimpleName();
   }

   public Map<String, Object> getAllProperties(StringConvention keyConvention, MapFormat.MapTransformation transformation) {
      Map<String, Object> map = new HashMap();
      boolean isNested = transformation == MapFormat.MapTransformation.NESTED;
      Arrays.stream(this.catalog).filter(Objects::nonNull).map(Map::entrySet).flatMap(Collection::stream).forEach(entry -> {
         String k = keyConvention.format((String)entry.getKey());
         Object value = this.resolvePlaceHoldersIfNecessary(entry.getValue());
         Map finalMap = map;
         int index = k.indexOf(46);
         if (index != -1 && isNested) {
            String[] keys = DOT_PATTERN.split(k);

            for(int i = 0; i < keys.length - 1; ++i) {
               if (!finalMap.containsKey(keys[i])) {
                  finalMap.put(keys[i], new HashMap());
               }

               Object next = finalMap.get(keys[i]);
               if (next instanceof Map) {
                  finalMap = (Map)next;
               }
            }

            finalMap.put(keys[keys.length - 1], value);
         } else {
            map.put(k, value);
         }

      });
      return map;
   }

   protected Properties resolveSubProperties(String name, Map<String, Object> entries, ArgumentConversionContext<?> conversionContext) {
      Properties properties = new Properties();
      AnnotationMetadata annotationMetadata = conversionContext.getAnnotationMetadata();
      StringConvention keyConvention = (StringConvention)annotationMetadata.enumValue(MapFormat.class, "keyFormat", StringConvention.class).orElse(null);
      if (keyConvention == StringConvention.RAW) {
         entries = this.resolveEntriesForKey(name, false, PropertySourcePropertyResolver.PropertyCatalog.RAW);
      }

      String prefix = name + '.';
      entries.entrySet().stream().filter(map -> ((String)map.getKey()).startsWith(prefix)).forEach(entry -> {
         Object value = entry.getValue();
         if (value != null) {
            String key = ((String)entry.getKey()).substring(prefix.length());
            key = keyConvention != null ? keyConvention.format(key) : key;
            properties.put(key, this.resolvePlaceHoldersIfNecessary(value.toString()));
         }

      });
      return properties;
   }

   protected Map<String, Object> resolveSubMap(String name, Map<String, Object> entries, ArgumentConversionContext<?> conversionContext) {
      AnnotationMetadata annotationMetadata = conversionContext.getAnnotationMetadata();
      StringConvention keyConvention = (StringConvention)annotationMetadata.enumValue(MapFormat.class, "keyFormat", StringConvention.class).orElse(null);
      if (keyConvention == StringConvention.RAW) {
         entries = this.resolveEntriesForKey(name, false, PropertySourcePropertyResolver.PropertyCatalog.RAW);
      }

      MapFormat.MapTransformation transformation = (MapFormat.MapTransformation)annotationMetadata.enumValue(
            MapFormat.class, "transformation", MapFormat.MapTransformation.class
         )
         .orElse(MapFormat.MapTransformation.NESTED);
      return this.resolveSubMap(name, entries, conversionContext, keyConvention, transformation);
   }

   @NonNull
   protected Map<String, Object> resolveSubMap(
      String name,
      Map<String, Object> entries,
      ArgumentConversionContext<?> conversionContext,
      @Nullable StringConvention keyConvention,
      MapFormat.MapTransformation transformation
   ) {
      Argument<?> valueType = (Argument)conversionContext.getTypeVariable("V").orElse(Argument.OBJECT_ARGUMENT);
      boolean valueTypeIsList = List.class.isAssignableFrom(valueType.getType());
      Map<String, Object> subMap = new LinkedHashMap(entries.size());
      String prefix = name + '.';

      for(Entry<String, Object> entry : entries.entrySet()) {
         String key = (String)entry.getKey();
         if ((!valueTypeIsList || !key.contains("[") || !key.endsWith("]")) && key.startsWith(prefix)) {
            String subMapKey = key.substring(prefix.length());
            Object value = this.resolvePlaceHoldersIfNecessary(entry.getValue());
            if (transformation == MapFormat.MapTransformation.FLAT) {
               subMapKey = keyConvention != null ? keyConvention.format(subMapKey) : subMapKey;
               value = this.conversionService.convert(value, valueType).orElse(null);
               subMap.put(subMapKey, value);
            } else {
               this.processSubmapKey(subMap, subMapKey, value, keyConvention);
            }
         }
      }

      return subMap;
   }

   protected void processPropertySource(PropertySource properties, PropertySource.PropertyConvention convention) {
      this.propertySources.put(properties.getName(), properties);
      synchronized(this.catalog) {
         for(String property : properties) {
            if (LOG.isTraceEnabled()) {
               LOG.trace("Processing property key {}", property);
            }

            Object value = properties.get(property);
            if (value instanceof CharSequence) {
               value = this.processRandomExpressions(convention, property, (CharSequence)value);
            } else if (value instanceof List) {
               ListIterator i = ((List)value).listIterator();

               while(i.hasNext()) {
                  Object o = i.next();
                  if (o instanceof CharSequence) {
                     CharSequence newValue = this.processRandomExpressions(convention, property, (CharSequence)o);
                     if (newValue != o) {
                        i.set(newValue);
                     }
                  }
               }
            }

            List<String> resolvedProperties = this.resolvePropertiesForConvention(property, convention);
            boolean first = true;

            for(String resolvedProperty : resolvedProperties) {
               int i = resolvedProperty.indexOf(91);
               if (i > -1) {
                  String propertyName = resolvedProperty.substring(0, i);
                  Map<String, Object> entries = this.resolveEntriesForKey(propertyName, true, PropertySourcePropertyResolver.PropertyCatalog.GENERATED);
                  if (entries != null) {
                     entries.put(resolvedProperty, value);
                     this.expandProperty(resolvedProperty.substring(i), val -> entries.put(propertyName, val), () -> entries.get(propertyName), value);
                  }

                  if (first) {
                     Map<String, Object> normalized = this.resolveEntriesForKey(
                        resolvedProperty, true, PropertySourcePropertyResolver.PropertyCatalog.NORMALIZED
                     );
                     if (normalized != null) {
                        normalized.put(propertyName, value);
                     }

                     first = false;
                  }
               } else {
                  Map<String, Object> entries = this.resolveEntriesForKey(resolvedProperty, true, PropertySourcePropertyResolver.PropertyCatalog.GENERATED);
                  if (entries != null) {
                     if (value instanceof List || value instanceof Map) {
                        this.collapseProperty(resolvedProperty, entries, value);
                     }

                     entries.put(resolvedProperty, value);
                  }

                  if (first) {
                     Map<String, Object> normalized = this.resolveEntriesForKey(
                        resolvedProperty, true, PropertySourcePropertyResolver.PropertyCatalog.NORMALIZED
                     );
                     if (normalized != null) {
                        normalized.put(resolvedProperty, value);
                     }

                     first = false;
                  }
               }
            }

            Map<String, Object> rawEntries = this.resolveEntriesForKey(property, true, PropertySourcePropertyResolver.PropertyCatalog.RAW);
            if (rawEntries != null) {
               rawEntries.put(property, value);
            }
         }

      }
   }

   private void expandProperty(String property, Consumer<Object> containerSet, Supplier<Object> containerGet, Object actualValue) {
      if (StringUtils.isEmpty(property)) {
         containerSet.accept(actualValue);
      } else {
         int i = property.indexOf(91);
         int li = property.indexOf(93);
         if (i == 0 && li > -1) {
            String propertyIndex = property.substring(1, li);
            String propertyRest = property.substring(li + 1);
            Object container = containerGet.get();
            if (StringUtils.isDigits(propertyIndex)) {
               Integer number = Integer.valueOf(propertyIndex);
               List list;
               if (container instanceof List) {
                  list = (List)container;
               } else {
                  list = new ArrayList(10);
                  containerSet.accept(list);
               }

               this.fill(list, number, null);
               this.expandProperty(propertyRest, val -> list.set(number, val), () -> list.get(number), actualValue);
            } else {
               Map map;
               if (container instanceof Map) {
                  map = (Map)container;
               } else {
                  map = new LinkedHashMap(10);
                  containerSet.accept(map);
               }

               this.expandProperty(propertyRest, val -> map.put(propertyIndex, val), () -> map.get(propertyIndex), actualValue);
            }
         } else if (property.startsWith(".")) {
            String propertyName;
            String propertyRest;
            if (i > -1) {
               propertyName = property.substring(1, i);
               propertyRest = property.substring(i);
            } else {
               propertyName = property.substring(1);
               propertyRest = "";
            }

            Object v = containerGet.get();
            Map map;
            if (v instanceof Map) {
               map = (Map)v;
            } else {
               map = new LinkedHashMap(10);
               containerSet.accept(map);
            }

            this.expandProperty(propertyRest, val -> map.put(propertyName, val), () -> map.get(propertyName), actualValue);
         }

      }
   }

   private void collapseProperty(String prefix, Map<String, Object> entries, Object value) {
      if (value instanceof List) {
         for(int i = 0; i < ((List)value).size(); ++i) {
            Object item = ((List)value).get(i);
            if (item != null) {
               this.collapseProperty(prefix + "[" + i + "]", entries, item);
            }
         }

         entries.put(prefix, value);
      } else if (value instanceof Map) {
         for(Entry<?, ?> entry : ((Map)value).entrySet()) {
            Object key = entry.getKey();
            if (key instanceof CharSequence) {
               this.collapseProperty(prefix + "." + ((CharSequence)key).toString(), entries, entry.getValue());
            }
         }
      } else {
         entries.put(prefix, value);
      }

   }

   private CharSequence processRandomExpressions(PropertySource.PropertyConvention convention, String property, CharSequence str) {
      if (convention != PropertySource.PropertyConvention.ENVIRONMENT_VARIABLE && str.toString().contains(this.propertyPlaceholderResolver.getPrefix())) {
         StringBuffer newValue = new StringBuffer();
         Matcher matcher = RANDOM_PATTERN.matcher(str);

         boolean hasRandoms;
         String randomValue;
         for(hasRandoms = false; matcher.find(); matcher.appendReplacement(newValue, randomValue)) {
            hasRandoms = true;
            String type = matcher.group(1).trim().toLowerCase();
            String range = matcher.group(2);
            if (range != null) {
               range = range.substring(1, range.length() - 1);
            }

            switch(type) {
               case "port":
                  randomValue = String.valueOf(SocketUtils.findAvailableTcpPort());
                  break;
               case "int":
               case "integer":
                  randomValue = String.valueOf(range == null ? this.random.nextInt() : this.getNextIntegerInRange(range, property));
                  break;
               case "long":
                  randomValue = String.valueOf(range == null ? this.random.nextLong() : this.getNextLongInRange(range, property));
                  break;
               case "float":
                  randomValue = String.valueOf(range == null ? this.random.nextFloat() : this.getNextFloatInRange(range, property));
                  break;
               case "shortuuid":
                  randomValue = UUID.randomUUID().toString().substring(25, 35);
                  break;
               case "uuid":
                  randomValue = UUID.randomUUID().toString();
                  break;
               case "uuid2":
                  randomValue = UUID.randomUUID().toString().replace("-", "");
                  break;
               default:
                  throw new ConfigurationException("Invalid random expression " + matcher.group(0) + " for property: " + property);
            }
         }

         if (hasRandoms) {
            matcher.appendTail(newValue);
            return newValue.toString();
         }
      }

      return str;
   }

   protected Map<String, Object> resolveEntriesForKey(String name, boolean allowCreate) {
      return this.resolveEntriesForKey(name, allowCreate, null);
   }

   protected Map<String, Object> resolveEntriesForKey(
      String name, boolean allowCreate, @Nullable PropertySourcePropertyResolver.PropertyCatalog propertyCatalog
   ) {
      if (name.length() == 0) {
         return null;
      } else {
         Map<String, Object>[] catalog = this.getCatalog(propertyCatalog);
         Map<String, Object> entries = null;
         char firstChar = name.charAt(0);
         if (Character.isLetter(firstChar)) {
            int index = firstChar - 'A';
            if (index < catalog.length && index >= 0) {
               entries = catalog[index];
               if (allowCreate && entries == null) {
                  entries = new LinkedHashMap(5);
                  catalog[index] = entries;
               }
            }
         }

         return entries;
      }
   }

   private Map<String, Object>[] getCatalog(@Nullable PropertySourcePropertyResolver.PropertyCatalog propertyCatalog) {
      propertyCatalog = propertyCatalog != null ? propertyCatalog : PropertySourcePropertyResolver.PropertyCatalog.GENERATED;
      Map<String, Object>[] catalog;
      switch(propertyCatalog) {
         case RAW:
            catalog = this.rawCatalog;
            break;
         case NORMALIZED:
            catalog = this.nonGenerated;
            break;
         default:
            catalog = this.catalog;
      }

      return catalog;
   }

   protected void resetCaches() {
      this.containsCache.clear();
      this.resolvedValueCache.clear();
   }

   private void processSubmapKey(Map<String, Object> map, String key, Object value, @Nullable StringConvention keyConvention) {
      int index = key.indexOf(46);
      boolean hasKeyConvention = keyConvention != null;
      if (index == -1) {
         key = hasKeyConvention ? keyConvention.format(key) : key;
         map.put(key, value);
      } else {
         String mapKey = key.substring(0, index);
         mapKey = hasKeyConvention ? keyConvention.format(mapKey) : mapKey;
         if (!map.containsKey(mapKey)) {
            map.put(mapKey, new LinkedHashMap());
         }

         Object v = map.get(mapKey);
         if (v instanceof Map) {
            Map<String, Object> nestedMap = (Map)v;
            String nestedKey = key.substring(index + 1);
            this.processSubmapKey(nestedMap, nestedKey, value, keyConvention);
         } else {
            map.put(mapKey, v);
         }
      }

   }

   private String normalizeName(String name) {
      return name.replace('-', '.');
   }

   private Object resolvePlaceHoldersIfNecessary(Object value) {
      if (value instanceof CharSequence) {
         return this.propertyPlaceholderResolver.resolveRequiredPlaceholders(value.toString());
      } else {
         if (value instanceof List) {
            List<?> list = (List)value;
            List<?> newList = new ArrayList(list);
            ListIterator i = newList.listIterator();

            while(i.hasNext()) {
               Object o = i.next();
               if (o instanceof CharSequence) {
                  i.set(this.resolvePlaceHoldersIfNecessary(o));
               } else if (o instanceof Map) {
                  Map<?, ?> submap = (Map)o;
                  Map<Object, Object> newMap = new LinkedHashMap(submap.size());

                  for(Entry<?, ?> entry : submap.entrySet()) {
                     Object k = entry.getKey();
                     Object v = entry.getValue();
                     newMap.put(k, this.resolvePlaceHoldersIfNecessary(v));
                  }

                  i.set(newMap);
               }
            }

            value = newList;
         }

         return value;
      }
   }

   private List<String> resolvePropertiesForConvention(String property, PropertySource.PropertyConvention convention) {
      return convention == PropertySource.PropertyConvention.ENVIRONMENT_VARIABLE
         ? this.environmentProperties.findPropertyNamesForEnvironmentVariable(property)
         : Collections.singletonList(NameUtils.hyphenate(property, true));
   }

   private void fill(List list, Integer toIndex, Object value) {
      if (toIndex >= list.size()) {
         for(int i = list.size(); i <= toIndex; ++i) {
            list.add(i, value);
         }
      }

   }

   private int getNextIntegerInRange(String range, String property) {
      try {
         String[] tokens = range.split(",");
         int lowerBound = Integer.parseInt(tokens[0]);
         if (tokens.length == 1) {
            return lowerBound >= 0 ? 1 : -1 * this.random.nextInt(Math.abs(lowerBound));
         } else {
            int upperBound = Integer.parseInt(tokens[1]);
            return lowerBound + (int)(Math.random() * (double)(upperBound - lowerBound));
         }
      } catch (NumberFormatException var6) {
         throw new ValueException("Invalid range: `" + range + "` found for type Integer while parsing property: " + property, var6);
      }
   }

   private long getNextLongInRange(String range, String property) {
      try {
         String[] tokens = range.split(",");
         long lowerBound = Long.parseLong(tokens[0]);
         if (tokens.length == 1) {
            return (long)(Math.random() * (double)lowerBound);
         } else {
            long upperBound = Long.parseLong(tokens[1]);
            return lowerBound + (long)(Math.random() * (double)(upperBound - lowerBound));
         }
      } catch (NumberFormatException var8) {
         throw new ValueException("Invalid range: `" + range + "` found for type Long while parsing property: " + property, var8);
      }
   }

   private float getNextFloatInRange(String range, String property) {
      try {
         String[] tokens = range.split(",");
         float lowerBound = Float.parseFloat(tokens[0]);
         if (tokens.length == 1) {
            return (float)(Math.random() * (double)lowerBound);
         } else {
            float upperBound = Float.parseFloat(tokens[1]);
            return lowerBound + (float)(Math.random() * (double)(upperBound - lowerBound));
         }
      } catch (NumberFormatException var6) {
         throw new ValueException("Invalid range: `" + range + "` found for type Float while parsing property: " + property, var6);
      }
   }

   public void close() throws Exception {
      if (this.propertyPlaceholderResolver instanceof AutoCloseable) {
         ((AutoCloseable)this.propertyPlaceholderResolver).close();
      }

   }

   protected static enum PropertyCatalog {
      RAW,
      NORMALIZED,
      GENERATED;
   }
}

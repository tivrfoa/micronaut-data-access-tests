package io.micronaut.validation.validator.constraints;

import io.micronaut.context.BeanContext;
import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.beans.BeanWrapper;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.clhm.ConcurrentLinkedHashMap;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.inject.qualifiers.TypeArgumentQualifier;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.chrono.HijrahDate;
import java.time.chrono.JapaneseDate;
import java.time.chrono.MinguoDate;
import java.time.chrono.ThaiBuddhistDate;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.concurrent.atomic.DoubleAdder;
import javax.validation.ValidationException;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NegativeOrZero;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

@Singleton
@Introspected
public class DefaultConstraintValidators implements ConstraintValidatorRegistry {
   private final Map<DefaultConstraintValidators.ValidatorKey, ConstraintValidator> validatorCache = new ConcurrentLinkedHashMap.Builder<DefaultConstraintValidators.ValidatorKey, ConstraintValidator>(
         
      )
      .initialCapacity(10)
      .maximumWeightedCapacity(40L)
      .build();
   private final ConstraintValidator<AssertFalse, Boolean> assertFalseValidator = (value, annotationMetadata, context) -> value == null || !value;
   private final ConstraintValidator<AssertTrue, Boolean> assertTrueValidator = (value, annotationMetadata, context) -> value == null || value;
   private final DecimalMaxValidator<CharSequence> decimalMaxValidatorCharSequence = (value, bigDecimal) -> new BigDecimal(value.toString())
         .compareTo(bigDecimal);
   private final DecimalMaxValidator<Number> decimalMaxValidatorNumber = DefaultConstraintValidators::compareNumber;
   private final DecimalMinValidator<CharSequence> decimalMinValidatorCharSequence = (value, bigDecimal) -> new BigDecimal(value.toString())
         .compareTo(bigDecimal);
   private final DecimalMinValidator<Number> decimalMinValidatorNumber = DefaultConstraintValidators::compareNumber;
   private final DigitsValidator<Number> digitsValidatorNumber = value -> value instanceof BigDecimal ? (BigDecimal)value : new BigDecimal(value.toString());
   private final DigitsValidator<CharSequence> digitsValidatorCharSequence = value -> new BigDecimal(value.toString());
   private final ConstraintValidator<Max, Number> maxNumberValidator = (value, annotationMetadata, context) -> {
      if (value == null) {
         return true;
      } else {
         Long max = (Long)annotationMetadata.getValue(Long.class).orElseThrow(() -> new ValidationException("@Max annotation specified without value"));
         if (value instanceof BigInteger) {
            return ((BigInteger)value).compareTo(BigInteger.valueOf(max)) <= 0;
         } else if (value instanceof BigDecimal) {
            return ((BigDecimal)value).compareTo(BigDecimal.valueOf(max)) <= 0;
         } else {
            return value.longValue() <= max;
         }
      }
   };
   private final ConstraintValidator<Min, Number> minNumberValidator = (value, annotationMetadata, context) -> {
      if (value == null) {
         return true;
      } else {
         Long max = (Long)annotationMetadata.getValue(Long.class).orElseThrow(() -> new ValidationException("@Min annotation specified without value"));
         if (value instanceof BigInteger) {
            return ((BigInteger)value).compareTo(BigInteger.valueOf(max)) >= 0;
         } else if (value instanceof BigDecimal) {
            return ((BigDecimal)value).compareTo(BigDecimal.valueOf(max)) >= 0;
         } else {
            return value.longValue() >= max;
         }
      }
   };
   private final ConstraintValidator<Negative, Number> negativeNumberValidator = (value, annotationMetadata, context) -> {
      if (value == null) {
         return true;
      } else if (value instanceof BigDecimal) {
         return ((BigDecimal)value).signum() < 0;
      } else if (value instanceof BigInteger) {
         return ((BigInteger)value).signum() < 0;
      } else if (!(value instanceof Double) && !(value instanceof Float) && !(value instanceof DoubleAdder) && !(value instanceof DoubleAccumulator)) {
         return value.longValue() < 0L;
      } else {
         return value.doubleValue() < 0.0;
      }
   };
   private final ConstraintValidator<NegativeOrZero, Number> negativeOrZeroNumberValidator = (value, annotationMetadata, context) -> {
      if (value == null) {
         return true;
      } else if (value instanceof BigDecimal) {
         return ((BigDecimal)value).signum() <= 0;
      } else if (value instanceof BigInteger) {
         return ((BigInteger)value).signum() <= 0;
      } else if (!(value instanceof Double) && !(value instanceof Float) && !(value instanceof DoubleAdder) && !(value instanceof DoubleAccumulator)) {
         return value.longValue() <= 0L;
      } else {
         return value.doubleValue() <= 0.0;
      }
   };
   private final ConstraintValidator<Positive, Number> positiveNumberValidator = (value, annotationMetadata, context) -> {
      if (value == null) {
         return true;
      } else if (value instanceof BigDecimal) {
         return ((BigDecimal)value).signum() > 0;
      } else if (value instanceof BigInteger) {
         return ((BigInteger)value).signum() > 0;
      } else if (!(value instanceof Double) && !(value instanceof Float) && !(value instanceof DoubleAdder) && !(value instanceof DoubleAccumulator)) {
         return value.longValue() > 0L;
      } else {
         return value.doubleValue() > 0.0;
      }
   };
   private final ConstraintValidator<PositiveOrZero, Number> positiveOrZeroNumberValidator = (value, annotationMetadata, context) -> {
      if (value == null) {
         return true;
      } else if (value instanceof BigDecimal) {
         return ((BigDecimal)value).signum() >= 0;
      } else if (value instanceof BigInteger) {
         return ((BigInteger)value).signum() >= 0;
      } else if (!(value instanceof Double) && !(value instanceof Float) && !(value instanceof DoubleAdder) && !(value instanceof DoubleAccumulator)) {
         return value.longValue() >= 0L;
      } else {
         return value.doubleValue() >= 0.0;
      }
   };
   private final ConstraintValidator<NotBlank, CharSequence> notBlankValidator = (value, annotationMetadata, context) -> StringUtils.isNotEmpty(value)
         && value.toString().trim().length() > 0;
   private final ConstraintValidator<NotNull, Object> notNullValidator = (value, annotationMetadata, context) -> value != null;
   private final ConstraintValidator<Null, Object> nullValidator = (value, annotationMetadata, context) -> value == null;
   private final ConstraintValidator<NotEmpty, byte[]> notEmptyByteArrayValidator = (value, annotationMetadata, context) -> value != null && value.length > 0;
   private final ConstraintValidator<NotEmpty, char[]> notEmptyCharArrayValidator = (value, annotationMetadata, context) -> value != null && value.length > 0;
   private final ConstraintValidator<NotEmpty, boolean[]> notEmptyBooleanArrayValidator = (value, annotationMetadata, context) -> value != null
         && value.length > 0;
   private final ConstraintValidator<NotEmpty, double[]> notEmptyDoubleArrayValidator = (value, annotationMetadata, context) -> value != null
         && value.length > 0;
   private final ConstraintValidator<NotEmpty, float[]> notEmptyFloatArrayValidator = (value, annotationMetadata, context) -> value != null && value.length > 0;
   private final ConstraintValidator<NotEmpty, int[]> notEmptyIntArrayValidator = (value, annotationMetadata, context) -> value != null && value.length > 0;
   private final ConstraintValidator<NotEmpty, long[]> notEmptyLongArrayValidator = (value, annotationMetadata, context) -> value != null && value.length > 0;
   private final ConstraintValidator<NotEmpty, Object[]> notEmptyObjectArrayValidator = (value, annotationMetadata, context) -> value != null
         && value.length > 0;
   private final ConstraintValidator<NotEmpty, short[]> notEmptyShortArrayValidator = (value, annotationMetadata, context) -> value != null && value.length > 0;
   private final ConstraintValidator<NotEmpty, CharSequence> notEmptyCharSequenceValidator = (value, annotationMetadata, context) -> StringUtils.isNotEmpty(
         value
      );
   private final ConstraintValidator<NotEmpty, Collection> notEmptyCollectionValidator = (value, annotationMetadata, context) -> CollectionUtils.isNotEmpty(
         value
      );
   private final ConstraintValidator<NotEmpty, Map> notEmptyMapValidator = (value, annotationMetadata, context) -> CollectionUtils.isNotEmpty(value);
   private final SizeValidator<Object[]> sizeObjectArrayValidator = value -> value.length;
   private final SizeValidator<byte[]> sizeByteArrayValidator = value -> value.length;
   private final SizeValidator<char[]> sizeCharArrayValidator = value -> value.length;
   private final SizeValidator<boolean[]> sizeBooleanArrayValidator = value -> value.length;
   private final SizeValidator<double[]> sizeDoubleArrayValidator = value -> value.length;
   private final SizeValidator<float[]> sizeFloatArrayValidator = value -> value.length;
   private final SizeValidator<int[]> sizeIntArrayValidator = value -> value.length;
   private final SizeValidator<long[]> sizeLongArrayValidator = value -> value.length;
   private final SizeValidator<short[]> sizeShortArrayValidator = value -> value.length;
   private final SizeValidator<CharSequence> sizeCharSequenceValidator = CharSequence::length;
   private final SizeValidator<Collection> sizeCollectionValidator = Collection::size;
   private final SizeValidator<Map> sizeMapValidator = Map::size;
   private final ConstraintValidator<Past, TemporalAccessor> pastTemporalAccessorConstraintValidator = (value, annotationMetadata, context) -> {
      if (value == null) {
         return true;
      } else {
         Comparable comparable = this.getNow(value, context.getClockProvider().getClock());
         return comparable.compareTo(value) > 0;
      }
   };
   private final ConstraintValidator<Past, Date> pastDateConstraintValidator = (value, annotationMetadata, context) -> {
      if (value == null) {
         return true;
      } else {
         Comparable comparable = Date.from(context.getClockProvider().getClock().instant());
         return comparable.compareTo(value) > 0;
      }
   };
   private final ConstraintValidator<PastOrPresent, TemporalAccessor> pastOrPresentTemporalAccessorConstraintValidator = (value, annotationMetadata, context) -> {
      if (value == null) {
         return true;
      } else {
         Comparable comparable = this.getNow(value, context.getClockProvider().getClock());
         return comparable.compareTo(value) >= 0;
      }
   };
   private final ConstraintValidator<PastOrPresent, Date> pastOrPresentDateConstraintValidator = (value, annotationMetadata, context) -> {
      if (value == null) {
         return true;
      } else {
         Comparable comparable = Date.from(context.getClockProvider().getClock().instant());
         return comparable.compareTo(value) >= 0;
      }
   };
   private final ConstraintValidator<Future, TemporalAccessor> futureTemporalAccessorConstraintValidator = (value, annotationMetadata, context) -> {
      if (value == null) {
         return true;
      } else {
         Comparable comparable = this.getNow(value, context.getClockProvider().getClock());
         return comparable.compareTo(value) < 0;
      }
   };
   private final ConstraintValidator<Future, Date> futureDateConstraintValidator = (value, annotationMetadata, context) -> {
      if (value == null) {
         return true;
      } else {
         Comparable comparable = Date.from(context.getClockProvider().getClock().instant());
         return comparable.compareTo(value) < 0;
      }
   };
   private final ConstraintValidator<FutureOrPresent, TemporalAccessor> futureOrPresentTemporalAccessorConstraintValidator = (value, annotationMetadata, context) -> {
      if (value == null) {
         return true;
      } else {
         Comparable comparable = this.getNow(value, context.getClockProvider().getClock());
         return comparable.compareTo(value) <= 0;
      }
   };
   private final ConstraintValidator<FutureOrPresent, Date> futureOrPresentDateConstraintValidator = (value, annotationMetadata, context) -> {
      if (value == null) {
         return true;
      } else {
         Comparable comparable = Date.from(context.getClockProvider().getClock().instant());
         return comparable.compareTo(value) <= 0;
      }
   };
   @Nullable
   private final BeanContext beanContext;
   private final Map<DefaultConstraintValidators.ValidatorKey, ConstraintValidator> localValidators;

   public DefaultConstraintValidators() {
      this(null);
   }

   @Inject
   protected DefaultConstraintValidators(@Nullable BeanContext beanContext) {
      this.beanContext = beanContext;
      BeanWrapper<DefaultConstraintValidators> wrapper = (BeanWrapper)BeanWrapper.findWrapper(DefaultConstraintValidators.class, this).orElse(null);
      if (wrapper != null) {
         Collection<BeanProperty<DefaultConstraintValidators, Object>> beanProperties = wrapper.getBeanProperties();
         Map<DefaultConstraintValidators.ValidatorKey, ConstraintValidator> validatorMap = new HashMap(beanProperties.size());

         for(BeanProperty<DefaultConstraintValidators, Object> property : beanProperties) {
            if (ConstraintValidator.class.isAssignableFrom(property.getType())) {
               Argument[] typeParameters = property.asArgument().getTypeParameters();
               if (ArrayUtils.isNotEmpty(typeParameters)) {
                  int len = typeParameters.length;
                  wrapper.getProperty(property.getName(), ConstraintValidator.class)
                     .ifPresent(
                        constraintValidator -> {
                           if (len == 2) {
                              Class targetType = ReflectionUtils.getWrapperType(typeParameters[1].getType());
                              DefaultConstraintValidators.ValidatorKey key = new DefaultConstraintValidators.ValidatorKey(
                                 typeParameters[0].getType(), targetType
                              );
                              validatorMap.put(key, constraintValidator);
                           } else if (len == 1) {
                              if (constraintValidator instanceof SizeValidator) {
                                 DefaultConstraintValidators.ValidatorKey var7x = new DefaultConstraintValidators.ValidatorKey(
                                    Size.class, typeParameters[0].getType()
                                 );
                                 validatorMap.put(var7x, constraintValidator);
                              } else if (constraintValidator instanceof DigitsValidator) {
                                 DefaultConstraintValidators.ValidatorKey var8x = new DefaultConstraintValidators.ValidatorKey(
                                    Digits.class, typeParameters[0].getType()
                                 );
                                 validatorMap.put(var8x, constraintValidator);
                              } else if (constraintValidator instanceof DecimalMaxValidator) {
                                 DefaultConstraintValidators.ValidatorKey key = new DefaultConstraintValidators.ValidatorKey(
                                    DecimalMax.class, typeParameters[0].getType()
                                 );
                                 validatorMap.put(key, constraintValidator);
                              } else if (constraintValidator instanceof DecimalMinValidator) {
                                 DefaultConstraintValidators.ValidatorKey key = new DefaultConstraintValidators.ValidatorKey(
                                    DecimalMin.class, typeParameters[0].getType()
                                 );
                                 validatorMap.put(key, constraintValidator);
                              }
                           }
      
                        }
                     );
               }
            }
         }

         validatorMap.put(new DefaultConstraintValidators.ValidatorKey(Pattern.class, CharSequence.class), new PatternValidator());
         validatorMap.put(new DefaultConstraintValidators.ValidatorKey(Email.class, CharSequence.class), new EmailValidator());
         this.localValidators = validatorMap;
      } else {
         this.localValidators = Collections.emptyMap();
      }

   }

   @NonNull
   @Override
   public <A extends Annotation, T> Optional<ConstraintValidator<A, T>> findConstraintValidator(@NonNull Class<A> constraintType, @NonNull Class<T> targetType) {
      ArgumentUtils.requireNonNull("constraintType", (T)constraintType);
      ArgumentUtils.requireNonNull("targetType", (T)targetType);
      DefaultConstraintValidators.ValidatorKey key = new DefaultConstraintValidators.ValidatorKey(constraintType, targetType);
      targetType = ReflectionUtils.getWrapperType(targetType);
      ConstraintValidator constraintValidator = (ConstraintValidator)this.localValidators.get(key);
      if (constraintValidator != null) {
         return Optional.of(constraintValidator);
      } else {
         constraintValidator = (ConstraintValidator)this.validatorCache.get(key);
         if (constraintValidator != null) {
            return Optional.of(constraintValidator);
         } else {
            Qualifier<ConstraintValidator> qualifier = Qualifiers.byTypeArguments(constraintType, ReflectionUtils.getWrapperType(targetType));
            Class[] finalTypeArguments = new Class[]{constraintType, targetType};
            Optional<ConstraintValidator> local = this.localValidators.entrySet().stream().filter(entry -> {
               DefaultConstraintValidators.ValidatorKey k = (DefaultConstraintValidators.ValidatorKey)entry.getKey();
               return TypeArgumentQualifier.areTypesCompatible(finalTypeArguments, Arrays.asList(k.constraintType, k.targetType));
            }).map(Entry::getValue).findFirst();
            if (local.isPresent()) {
               this.validatorCache.put(key, local.get());
               return local;
            } else {
               if (this.beanContext != null) {
                  ConstraintValidator cv = (ConstraintValidator)this.beanContext
                     .findBean(ConstraintValidator.class, qualifier)
                     .orElse(ConstraintValidator.VALID);
                  this.validatorCache.put(key, cv);
                  if (cv != ConstraintValidator.VALID) {
                     return Optional.of(cv);
                  }
               } else {
                  ConstraintValidator cv = (ConstraintValidator)this.findLocalConstraintValidator(constraintType, targetType).orElse(ConstraintValidator.VALID);
                  this.validatorCache.put(key, cv);
                  if (cv != ConstraintValidator.VALID) {
                     return Optional.of(cv);
                  }
               }

               return Optional.empty();
            }
         }
      }
   }

   public ConstraintValidator<AssertFalse, Boolean> getAssertFalseValidator() {
      return this.assertFalseValidator;
   }

   public ConstraintValidator<AssertTrue, Boolean> getAssertTrueValidator() {
      return this.assertTrueValidator;
   }

   public DecimalMaxValidator<CharSequence> getDecimalMaxValidatorCharSequence() {
      return this.decimalMaxValidatorCharSequence;
   }

   public DecimalMaxValidator<Number> getDecimalMaxValidatorNumber() {
      return this.decimalMaxValidatorNumber;
   }

   public DecimalMinValidator<CharSequence> getDecimalMinValidatorCharSequence() {
      return this.decimalMinValidatorCharSequence;
   }

   public DecimalMinValidator<Number> getDecimalMinValidatorNumber() {
      return this.decimalMinValidatorNumber;
   }

   public DigitsValidator<Number> getDigitsValidatorNumber() {
      return this.digitsValidatorNumber;
   }

   public DigitsValidator<CharSequence> getDigitsValidatorCharSequence() {
      return this.digitsValidatorCharSequence;
   }

   public ConstraintValidator<Max, Number> getMaxNumberValidator() {
      return this.maxNumberValidator;
   }

   public ConstraintValidator<Min, Number> getMinNumberValidator() {
      return this.minNumberValidator;
   }

   public ConstraintValidator<Negative, Number> getNegativeNumberValidator() {
      return this.negativeNumberValidator;
   }

   public ConstraintValidator<NegativeOrZero, Number> getNegativeOrZeroNumberValidator() {
      return this.negativeOrZeroNumberValidator;
   }

   public ConstraintValidator<Positive, Number> getPositiveNumberValidator() {
      return this.positiveNumberValidator;
   }

   public ConstraintValidator<PositiveOrZero, Number> getPositiveOrZeroNumberValidator() {
      return this.positiveOrZeroNumberValidator;
   }

   public ConstraintValidator<NotBlank, CharSequence> getNotBlankValidator() {
      return this.notBlankValidator;
   }

   public ConstraintValidator<NotNull, Object> getNotNullValidator() {
      return this.notNullValidator;
   }

   public ConstraintValidator<Null, Object> getNullValidator() {
      return this.nullValidator;
   }

   public ConstraintValidator<NotEmpty, byte[]> getNotEmptyByteArrayValidator() {
      return this.notEmptyByteArrayValidator;
   }

   public ConstraintValidator<NotEmpty, char[]> getNotEmptyCharArrayValidator() {
      return this.notEmptyCharArrayValidator;
   }

   public ConstraintValidator<NotEmpty, boolean[]> getNotEmptyBooleanArrayValidator() {
      return this.notEmptyBooleanArrayValidator;
   }

   public ConstraintValidator<NotEmpty, double[]> getNotEmptyDoubleArrayValidator() {
      return this.notEmptyDoubleArrayValidator;
   }

   public ConstraintValidator<NotEmpty, float[]> getNotEmptyFloatArrayValidator() {
      return this.notEmptyFloatArrayValidator;
   }

   public ConstraintValidator<NotEmpty, int[]> getNotEmptyIntArrayValidator() {
      return this.notEmptyIntArrayValidator;
   }

   public ConstraintValidator<NotEmpty, long[]> getNotEmptyLongArrayValidator() {
      return this.notEmptyLongArrayValidator;
   }

   public ConstraintValidator<NotEmpty, Object[]> getNotEmptyObjectArrayValidator() {
      return this.notEmptyObjectArrayValidator;
   }

   public ConstraintValidator<NotEmpty, short[]> getNotEmptyShortArrayValidator() {
      return this.notEmptyShortArrayValidator;
   }

   public ConstraintValidator<NotEmpty, CharSequence> getNotEmptyCharSequenceValidator() {
      return this.notEmptyCharSequenceValidator;
   }

   public ConstraintValidator<NotEmpty, Collection> getNotEmptyCollectionValidator() {
      return this.notEmptyCollectionValidator;
   }

   public ConstraintValidator<NotEmpty, Map> getNotEmptyMapValidator() {
      return this.notEmptyMapValidator;
   }

   public SizeValidator<Object[]> getSizeObjectArrayValidator() {
      return this.sizeObjectArrayValidator;
   }

   public SizeValidator<byte[]> getSizeByteArrayValidator() {
      return this.sizeByteArrayValidator;
   }

   public SizeValidator<char[]> getSizeCharArrayValidator() {
      return this.sizeCharArrayValidator;
   }

   public SizeValidator<boolean[]> getSizeBooleanArrayValidator() {
      return this.sizeBooleanArrayValidator;
   }

   public SizeValidator<double[]> getSizeDoubleArrayValidator() {
      return this.sizeDoubleArrayValidator;
   }

   public SizeValidator<float[]> getSizeFloatArrayValidator() {
      return this.sizeFloatArrayValidator;
   }

   public SizeValidator<int[]> getSizeIntArrayValidator() {
      return this.sizeIntArrayValidator;
   }

   public SizeValidator<long[]> getSizeLongArrayValidator() {
      return this.sizeLongArrayValidator;
   }

   public SizeValidator<short[]> getSizeShortArrayValidator() {
      return this.sizeShortArrayValidator;
   }

   public SizeValidator<CharSequence> getSizeCharSequenceValidator() {
      return this.sizeCharSequenceValidator;
   }

   public SizeValidator<Collection> getSizeCollectionValidator() {
      return this.sizeCollectionValidator;
   }

   public SizeValidator<Map> getSizeMapValidator() {
      return this.sizeMapValidator;
   }

   public ConstraintValidator<Past, TemporalAccessor> getPastTemporalAccessorConstraintValidator() {
      return this.pastTemporalAccessorConstraintValidator;
   }

   public ConstraintValidator<Past, Date> getPastDateConstraintValidator() {
      return this.pastDateConstraintValidator;
   }

   public ConstraintValidator<PastOrPresent, TemporalAccessor> getPastOrPresentTemporalAccessorConstraintValidator() {
      return this.pastOrPresentTemporalAccessorConstraintValidator;
   }

   public ConstraintValidator<PastOrPresent, Date> getPastOrPresentDateConstraintValidator() {
      return this.pastOrPresentDateConstraintValidator;
   }

   public ConstraintValidator<Future, TemporalAccessor> getFutureTemporalAccessorConstraintValidator() {
      return this.futureTemporalAccessorConstraintValidator;
   }

   public ConstraintValidator<Future, Date> getFutureDateConstraintValidator() {
      return this.futureDateConstraintValidator;
   }

   public ConstraintValidator<FutureOrPresent, TemporalAccessor> getFutureOrPresentTemporalAccessorConstraintValidator() {
      return this.futureOrPresentTemporalAccessorConstraintValidator;
   }

   public ConstraintValidator<FutureOrPresent, Date> getFutureOrPresentDateConstraintValidator() {
      return this.futureOrPresentDateConstraintValidator;
   }

   protected <A extends Annotation, T> Optional<ConstraintValidator> findLocalConstraintValidator(
      @NonNull Class<A> constraintType, @NonNull Class<T> targetType
   ) {
      return Optional.empty();
   }

   private Comparable<? extends TemporalAccessor> getNow(TemporalAccessor value, Clock clock) {
      if (!(value instanceof Comparable)) {
         throw new IllegalArgumentException("TemporalAccessor value must be comparable");
      } else if (value instanceof LocalDateTime) {
         return LocalDateTime.now(clock);
      } else if (value instanceof Instant) {
         return Instant.now(clock);
      } else if (value instanceof ZonedDateTime) {
         return ZonedDateTime.now(clock);
      } else if (value instanceof OffsetDateTime) {
         return OffsetDateTime.now(clock);
      } else if (value instanceof LocalDate) {
         return LocalDate.now(clock);
      } else if (value instanceof LocalTime) {
         return LocalTime.now(clock);
      } else if (value instanceof OffsetTime) {
         return OffsetTime.now(clock);
      } else if (value instanceof MonthDay) {
         return MonthDay.now(clock);
      } else if (value instanceof Year) {
         return Year.now(clock);
      } else if (value instanceof YearMonth) {
         return YearMonth.now(clock);
      } else if (value instanceof HijrahDate) {
         return HijrahDate.now(clock);
      } else if (value instanceof JapaneseDate) {
         return JapaneseDate.now(clock);
      } else if (value instanceof ThaiBuddhistDate) {
         return ThaiBuddhistDate.now(clock);
      } else if (value instanceof MinguoDate) {
         return MinguoDate.now(clock);
      } else {
         throw new IllegalArgumentException("TemporalAccessor value type not supported: " + value.getClass());
      }
   }

   private static int compareNumber(@NonNull Number value, @NonNull BigDecimal bigDecimal) {
      int result;
      if (value instanceof BigDecimal) {
         result = ((BigDecimal)value).compareTo(bigDecimal);
      } else if (value instanceof BigInteger) {
         result = new BigDecimal((BigInteger)value).compareTo(bigDecimal);
      } else {
         result = BigDecimal.valueOf(value.doubleValue()).compareTo(bigDecimal);
      }

      return result;
   }

   protected final class ValidatorKey<A extends Annotation, T> {
      private final Class<A> constraintType;
      private final Class<T> targetType;

      public ValidatorKey(@NonNull Class<A> constraintType, @NonNull Class<T> targetType) {
         this.constraintType = constraintType;
         this.targetType = targetType;
      }

      public Class<A> getConstraintType() {
         return this.constraintType;
      }

      public Class<T> getTargetType() {
         return this.targetType;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            DefaultConstraintValidators.ValidatorKey<?, ?> key = (DefaultConstraintValidators.ValidatorKey)o;
            return this.constraintType.equals(key.constraintType) && this.targetType.equals(key.targetType);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.constraintType, this.targetType});
      }
   }
}

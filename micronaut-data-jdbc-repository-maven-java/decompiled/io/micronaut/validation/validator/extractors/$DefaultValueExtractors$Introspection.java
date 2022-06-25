package io.micronaut.validation.validator.extractors;

import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import javax.validation.valueextraction.ValueExtractor;

// $FF: synthetic class
@Generated
final class $DefaultValueExtractors$Introspection extends AbstractInitializableBeanIntrospection {
   private static final AnnotationMetadata $FIELD_CONSTRUCTOR_ANNOTATION_METADATA = new DefaultAnnotationMetadata(
      AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
      Collections.EMPTY_MAP,
      Collections.EMPTY_MAP,
      AnnotationUtil.internMapOf("javax.inject.Inject", Collections.EMPTY_MAP),
      Collections.EMPTY_MAP,
      false,
      true
   );
   private static final Argument[] $CONSTRUCTOR_ARGUMENTS = new Argument[]{
      Argument.of(
         BeanContext.class,
         "beanContext",
         new DefaultAnnotationMetadata(
            AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
            Collections.EMPTY_MAP,
            Collections.EMPTY_MAP,
            AnnotationUtil.internMapOf("javax.annotation.Nullable", Collections.EMPTY_MAP),
            Collections.EMPTY_MAP,
            false,
            true
         ),
         null
      )
   };
   private static final AbstractInitializableBeanIntrospection.BeanPropertyRef[] $PROPERTIES_REFERENCES = new AbstractInitializableBeanIntrospection.BeanPropertyRef[]{
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            UnwrapByDefaultValueExtractor.class,
            "optionalValueExtractor",
            null,
            Argument.ofTypeVariable(Optional.class, "T", null, Argument.ofTypeVariable(Object.class, "T"))
         ),
         0,
         -1,
         1,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(UnwrapByDefaultValueExtractor.class, "optionalIntValueExtractor", null, Argument.ofTypeVariable(OptionalInt.class, "T")),
         2,
         -1,
         3,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(UnwrapByDefaultValueExtractor.class, "optionalLongValueExtractor", null, Argument.ofTypeVariable(OptionalLong.class, "T")),
         4,
         -1,
         5,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(UnwrapByDefaultValueExtractor.class, "optionalDoubleValueExtractor", null, Argument.ofTypeVariable(OptionalDouble.class, "T")),
         6,
         -1,
         7,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ValueExtractor.class,
            "iterableValueExtractor",
            null,
            Argument.ofTypeVariable(Iterable.class, "T", null, Argument.ofTypeVariable(Object.class, "T"))
         ),
         8,
         -1,
         9,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ValueExtractor.class,
            "mapValueExtractor",
            null,
            Argument.ofTypeVariable(Map.class, "T", null, Argument.ofTypeVariable(Object.class, "K"), Argument.ofTypeVariable(Object.class, "V"))
         ),
         10,
         -1,
         11,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(ValueExtractor.class, "objectArrayValueExtractor", null, Argument.of(Object[].class, "T")), 12, -1, 13, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(ValueExtractor.class, "intArrayValueExtractor", null, Argument.of(int[].class, "T")), 14, -1, 15, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(ValueExtractor.class, "byteArrayValueExtractor", null, Argument.of(byte[].class, "T")), 16, -1, 17, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(ValueExtractor.class, "charArrayValueExtractor", null, Argument.of(char[].class, "T")), 18, -1, 19, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(ValueExtractor.class, "booleanArrayValueExtractor", null, Argument.of(boolean[].class, "T")), 20, -1, 21, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(ValueExtractor.class, "doubleArrayValueExtractor", null, Argument.of(double[].class, "T")), 22, -1, 23, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(ValueExtractor.class, "floatArrayValueExtractor", null, Argument.of(float[].class, "T")), 24, -1, 25, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(ValueExtractor.class, "shortArrayValueExtractor", null, Argument.of(short[].class, "T")), 26, -1, 27, true, false
      )
   };

   public $DefaultValueExtractors$Introspection() {
      super(
         DefaultValueExtractors.class,
         $DefaultValueExtractors$IntrospectionRef.$ANNOTATION_METADATA,
         $FIELD_CONSTRUCTOR_ANNOTATION_METADATA,
         $CONSTRUCTOR_ARGUMENTS,
         $PROPERTIES_REFERENCES,
         null
      );
   }

   @Override
   protected final Object dispatchOne(int var1, Object var2, Object var3) {
      switch(var1) {
         case 0:
            return ((DefaultValueExtractors)var2).getOptionalValueExtractor();
         case 1:
            throw new UnsupportedOperationException(
               "Cannot mutate property [optionalValueExtractor] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.extractors.DefaultValueExtractors"
            );
         case 2:
            return ((DefaultValueExtractors)var2).getOptionalIntValueExtractor();
         case 3:
            throw new UnsupportedOperationException(
               "Cannot mutate property [optionalIntValueExtractor] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.extractors.DefaultValueExtractors"
            );
         case 4:
            return ((DefaultValueExtractors)var2).getOptionalLongValueExtractor();
         case 5:
            throw new UnsupportedOperationException(
               "Cannot mutate property [optionalLongValueExtractor] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.extractors.DefaultValueExtractors"
            );
         case 6:
            return ((DefaultValueExtractors)var2).getOptionalDoubleValueExtractor();
         case 7:
            throw new UnsupportedOperationException(
               "Cannot mutate property [optionalDoubleValueExtractor] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.extractors.DefaultValueExtractors"
            );
         case 8:
            return ((DefaultValueExtractors)var2).getIterableValueExtractor();
         case 9:
            throw new UnsupportedOperationException(
               "Cannot mutate property [iterableValueExtractor] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.extractors.DefaultValueExtractors"
            );
         case 10:
            return ((DefaultValueExtractors)var2).getMapValueExtractor();
         case 11:
            throw new UnsupportedOperationException(
               "Cannot mutate property [mapValueExtractor] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.extractors.DefaultValueExtractors"
            );
         case 12:
            return ((DefaultValueExtractors)var2).getObjectArrayValueExtractor();
         case 13:
            throw new UnsupportedOperationException(
               "Cannot mutate property [objectArrayValueExtractor] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.extractors.DefaultValueExtractors"
            );
         case 14:
            return ((DefaultValueExtractors)var2).getIntArrayValueExtractor();
         case 15:
            throw new UnsupportedOperationException(
               "Cannot mutate property [intArrayValueExtractor] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.extractors.DefaultValueExtractors"
            );
         case 16:
            return ((DefaultValueExtractors)var2).getByteArrayValueExtractor();
         case 17:
            throw new UnsupportedOperationException(
               "Cannot mutate property [byteArrayValueExtractor] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.extractors.DefaultValueExtractors"
            );
         case 18:
            return ((DefaultValueExtractors)var2).getCharArrayValueExtractor();
         case 19:
            throw new UnsupportedOperationException(
               "Cannot mutate property [charArrayValueExtractor] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.extractors.DefaultValueExtractors"
            );
         case 20:
            return ((DefaultValueExtractors)var2).getBooleanArrayValueExtractor();
         case 21:
            throw new UnsupportedOperationException(
               "Cannot mutate property [booleanArrayValueExtractor] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.extractors.DefaultValueExtractors"
            );
         case 22:
            return ((DefaultValueExtractors)var2).getDoubleArrayValueExtractor();
         case 23:
            throw new UnsupportedOperationException(
               "Cannot mutate property [doubleArrayValueExtractor] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.extractors.DefaultValueExtractors"
            );
         case 24:
            return ((DefaultValueExtractors)var2).getFloatArrayValueExtractor();
         case 25:
            throw new UnsupportedOperationException(
               "Cannot mutate property [floatArrayValueExtractor] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.extractors.DefaultValueExtractors"
            );
         case 26:
            return ((DefaultValueExtractors)var2).getShortArrayValueExtractor();
         case 27:
            throw new UnsupportedOperationException(
               "Cannot mutate property [shortArrayValueExtractor] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.extractors.DefaultValueExtractors"
            );
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   public final int propertyIndexOf(String var1) {
      switch(var1.hashCode()) {
         case -1214084496:
            if (var1.equals("floatArrayValueExtractor")) {
               return 12;
            }
            break;
         case -1054139597:
            if (var1.equals("optionalValueExtractor")) {
               return 0;
            }
            break;
         case -355502812:
            if (var1.equals("booleanArrayValueExtractor")) {
               return 10;
            }
            break;
         case -174486821:
            if (var1.equals("doubleArrayValueExtractor")) {
               return 11;
            }
            break;
         case 120296516:
            if (var1.equals("byteArrayValueExtractor")) {
               return 8;
            }
            break;
         case 692210885:
            if (var1.equals("iterableValueExtractor")) {
               return 4;
            }
            break;
         case 795599362:
            if (var1.equals("optionalIntValueExtractor")) {
               return 1;
            }
            break;
         case 823702608:
            if (var1.equals("shortArrayValueExtractor")) {
               return 13;
            }
            break;
         case 911831862:
            if (var1.equals("charArrayValueExtractor")) {
               return 9;
            }
            break;
         case 1161159887:
            if (var1.equals("optionalLongValueExtractor")) {
               return 2;
            }
            break;
         case 1496727908:
            if (var1.equals("optionalDoubleValueExtractor")) {
               return 3;
            }
            break;
         case 1545805997:
            if (var1.equals("objectArrayValueExtractor")) {
               return 6;
            }
            break;
         case 1972325423:
            if (var1.equals("mapValueExtractor")) {
               return 5;
            }
            break;
         case 2126056957:
            if (var1.equals("intArrayValueExtractor")) {
               return 7;
            }
      }

      return -1;
   }

   @Override
   public Object instantiate() {
      return new DefaultValueExtractors();
   }

   @Override
   public Object instantiateInternal(Object[] var1) {
      return new DefaultValueExtractors((BeanContext)var1[0]);
   }
}

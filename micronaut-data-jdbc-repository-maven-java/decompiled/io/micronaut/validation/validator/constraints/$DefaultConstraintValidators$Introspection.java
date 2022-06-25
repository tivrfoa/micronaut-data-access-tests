package io.micronaut.validation.validator.constraints;

import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
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
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

// $FF: synthetic class
@Generated
final class $DefaultConstraintValidators$Introspection extends AbstractInitializableBeanIntrospection {
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
            ConstraintValidator.class,
            "assertFalseValidator",
            null,
            Argument.ofTypeVariable(
               AssertFalse.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_0()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_0()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.ofTypeVariable(Boolean.class, "T")
         ),
         0,
         -1,
         1,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "assertTrueValidator",
            null,
            Argument.ofTypeVariable(
               AssertTrue.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_1()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_1()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.ofTypeVariable(Boolean.class, "T")
         ),
         2,
         -1,
         3,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(DecimalMaxValidator.class, "decimalMaxValidatorCharSequence", null, Argument.ofTypeVariable(CharSequence.class, "T")),
         4,
         -1,
         5,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(DecimalMaxValidator.class, "decimalMaxValidatorNumber", null, Argument.ofTypeVariable(Number.class, "T")), 6, -1, 7, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(DecimalMinValidator.class, "decimalMinValidatorCharSequence", null, Argument.ofTypeVariable(CharSequence.class, "T")),
         8,
         -1,
         9,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(DecimalMinValidator.class, "decimalMinValidatorNumber", null, Argument.ofTypeVariable(Number.class, "T")), 10, -1, 11, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(DigitsValidator.class, "digitsValidatorNumber", null, Argument.ofTypeVariable(Number.class, "T")), 12, -1, 13, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(DigitsValidator.class, "digitsValidatorCharSequence", null, Argument.ofTypeVariable(CharSequence.class, "T")), 14, -1, 15, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "maxNumberValidator",
            null,
            Argument.ofTypeVariable(
               Max.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_2()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_2()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.ofTypeVariable(Number.class, "T")
         ),
         16,
         -1,
         17,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "minNumberValidator",
            null,
            Argument.ofTypeVariable(
               Min.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_3()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_3()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.ofTypeVariable(Number.class, "T")
         ),
         18,
         -1,
         19,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "negativeNumberValidator",
            null,
            Argument.ofTypeVariable(
               Negative.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_4()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_4()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.ofTypeVariable(Number.class, "T")
         ),
         20,
         -1,
         21,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "negativeOrZeroNumberValidator",
            null,
            Argument.ofTypeVariable(
               NegativeOrZero.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_5()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_5()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.ofTypeVariable(Number.class, "T")
         ),
         22,
         -1,
         23,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "positiveNumberValidator",
            null,
            Argument.ofTypeVariable(
               Positive.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_6()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_6()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.ofTypeVariable(Number.class, "T")
         ),
         24,
         -1,
         25,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "positiveOrZeroNumberValidator",
            null,
            Argument.ofTypeVariable(
               PositiveOrZero.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_7()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_7()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.ofTypeVariable(Number.class, "T")
         ),
         26,
         -1,
         27,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "notBlankValidator",
            null,
            Argument.ofTypeVariable(
               NotBlank.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_8()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_8()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.ofTypeVariable(CharSequence.class, "T")
         ),
         28,
         -1,
         29,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "notNullValidator",
            null,
            Argument.ofTypeVariable(
               NotNull.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_9()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_9()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.ofTypeVariable(Object.class, "T")
         ),
         30,
         -1,
         31,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "nullValidator",
            null,
            Argument.ofTypeVariable(
               Null.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_10()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_10()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.ofTypeVariable(Object.class, "T")
         ),
         32,
         -1,
         33,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "notEmptyByteArrayValidator",
            null,
            Argument.ofTypeVariable(
               NotEmpty.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.of(byte[].class, "T")
         ),
         34,
         -1,
         35,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "notEmptyCharArrayValidator",
            null,
            Argument.ofTypeVariable(
               NotEmpty.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.of(char[].class, "T")
         ),
         36,
         -1,
         37,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "notEmptyBooleanArrayValidator",
            null,
            Argument.ofTypeVariable(
               NotEmpty.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.of(boolean[].class, "T")
         ),
         38,
         -1,
         39,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "notEmptyDoubleArrayValidator",
            null,
            Argument.ofTypeVariable(
               NotEmpty.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.of(double[].class, "T")
         ),
         40,
         -1,
         41,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "notEmptyFloatArrayValidator",
            null,
            Argument.ofTypeVariable(
               NotEmpty.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.of(float[].class, "T")
         ),
         42,
         -1,
         43,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "notEmptyIntArrayValidator",
            null,
            Argument.ofTypeVariable(
               NotEmpty.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.of(int[].class, "T")
         ),
         44,
         -1,
         45,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "notEmptyLongArrayValidator",
            null,
            Argument.ofTypeVariable(
               NotEmpty.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.of(long[].class, "T")
         ),
         46,
         -1,
         47,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "notEmptyObjectArrayValidator",
            null,
            Argument.ofTypeVariable(
               NotEmpty.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.of(Object[].class, "T")
         ),
         48,
         -1,
         49,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "notEmptyShortArrayValidator",
            null,
            Argument.ofTypeVariable(
               NotEmpty.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.of(short[].class, "T")
         ),
         50,
         -1,
         51,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "notEmptyCharSequenceValidator",
            null,
            Argument.ofTypeVariable(
               NotEmpty.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.ofTypeVariable(CharSequence.class, "T")
         ),
         52,
         -1,
         53,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "notEmptyCollectionValidator",
            null,
            Argument.ofTypeVariable(
               NotEmpty.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.ofTypeVariable(Collection.class, "T", null, Argument.ofTypeVariable(Object.class, "E"))
         ),
         54,
         -1,
         55,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "notEmptyMapValidator",
            null,
            Argument.ofTypeVariable(
               NotEmpty.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_11()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.ofTypeVariable(Map.class, "T", null, Argument.ofTypeVariable(Object.class, "K"), Argument.ofTypeVariable(Object.class, "V"))
         ),
         56,
         -1,
         57,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(SizeValidator.class, "sizeObjectArrayValidator", null, Argument.of(Object[].class, "T")), 58, -1, 59, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(SizeValidator.class, "sizeByteArrayValidator", null, Argument.of(byte[].class, "T")), 60, -1, 61, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(SizeValidator.class, "sizeCharArrayValidator", null, Argument.of(char[].class, "T")), 62, -1, 63, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(SizeValidator.class, "sizeBooleanArrayValidator", null, Argument.of(boolean[].class, "T")), 64, -1, 65, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(SizeValidator.class, "sizeDoubleArrayValidator", null, Argument.of(double[].class, "T")), 66, -1, 67, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(SizeValidator.class, "sizeFloatArrayValidator", null, Argument.of(float[].class, "T")), 68, -1, 69, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(SizeValidator.class, "sizeIntArrayValidator", null, Argument.of(int[].class, "T")), 70, -1, 71, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(SizeValidator.class, "sizeLongArrayValidator", null, Argument.of(long[].class, "T")), 72, -1, 73, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(SizeValidator.class, "sizeShortArrayValidator", null, Argument.of(short[].class, "T")), 74, -1, 75, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(SizeValidator.class, "sizeCharSequenceValidator", null, Argument.ofTypeVariable(CharSequence.class, "T")), 76, -1, 77, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            SizeValidator.class,
            "sizeCollectionValidator",
            null,
            Argument.ofTypeVariable(Collection.class, "T", null, Argument.ofTypeVariable(Object.class, "E"))
         ),
         78,
         -1,
         79,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            SizeValidator.class,
            "sizeMapValidator",
            null,
            Argument.ofTypeVariable(Map.class, "T", null, Argument.ofTypeVariable(Object.class, "K"), Argument.ofTypeVariable(Object.class, "V"))
         ),
         80,
         -1,
         81,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "pastTemporalAccessorConstraintValidator",
            null,
            Argument.ofTypeVariable(
               Past.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_12()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_12()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.ofTypeVariable(TemporalAccessor.class, "T")
         ),
         82,
         -1,
         83,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "pastDateConstraintValidator",
            null,
            Argument.ofTypeVariable(
               Past.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_12()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_12()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.ofTypeVariable(Date.class, "T")
         ),
         84,
         -1,
         85,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "pastOrPresentTemporalAccessorConstraintValidator",
            null,
            Argument.ofTypeVariable(
               PastOrPresent.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_13()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_13()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.ofTypeVariable(TemporalAccessor.class, "T")
         ),
         86,
         -1,
         87,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "pastOrPresentDateConstraintValidator",
            null,
            Argument.ofTypeVariable(
               PastOrPresent.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_13()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_13()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.ofTypeVariable(Date.class, "T")
         ),
         88,
         -1,
         89,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "futureTemporalAccessorConstraintValidator",
            null,
            Argument.ofTypeVariable(
               Future.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_14()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_14()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.ofTypeVariable(TemporalAccessor.class, "T")
         ),
         90,
         -1,
         91,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "futureDateConstraintValidator",
            null,
            Argument.ofTypeVariable(
               Future.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_14()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_14()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.ofTypeVariable(Date.class, "T")
         ),
         92,
         -1,
         93,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "futureOrPresentTemporalAccessorConstraintValidator",
            null,
            Argument.ofTypeVariable(
               FutureOrPresent.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_15()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_15()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.ofTypeVariable(TemporalAccessor.class, "T")
         ),
         94,
         -1,
         95,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            ConstraintValidator.class,
            "futureOrPresentDateConstraintValidator",
            null,
            Argument.ofTypeVariable(
               FutureOrPresent.class,
               "A",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_15()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "java.lang.annotation.Documented",
                     Collections.EMPTY_MAP,
                     "java.lang.annotation.Repeatable",
                     AnnotationUtil.mapOf("value", $micronaut_load_class_value_15()),
                     "java.lang.annotation.Retention",
                     AnnotationUtil.mapOf("value", "RUNTIME"),
                     "java.lang.annotation.Target",
                     AnnotationUtil.mapOf("value", new String[]{"METHOD", "FIELD", "ANNOTATION_TYPE", "CONSTRUCTOR", "PARAMETER", "TYPE_USE"}),
                     "javax.validation.Constraint",
                     AnnotationUtil.mapOf("validatedBy", ArrayUtils.EMPTY_OBJECT_ARRAY)
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            ),
            Argument.ofTypeVariable(Date.class, "T")
         ),
         96,
         -1,
         97,
         true,
         false
      )
   };

   public $DefaultConstraintValidators$Introspection() {
      super(
         DefaultConstraintValidators.class,
         $DefaultConstraintValidators$IntrospectionRef.$ANNOTATION_METADATA,
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
            return ((DefaultConstraintValidators)var2).getAssertFalseValidator();
         case 1:
            throw new UnsupportedOperationException(
               "Cannot mutate property [assertFalseValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 2:
            return ((DefaultConstraintValidators)var2).getAssertTrueValidator();
         case 3:
            throw new UnsupportedOperationException(
               "Cannot mutate property [assertTrueValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 4:
            return ((DefaultConstraintValidators)var2).getDecimalMaxValidatorCharSequence();
         case 5:
            throw new UnsupportedOperationException(
               "Cannot mutate property [decimalMaxValidatorCharSequence] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 6:
            return ((DefaultConstraintValidators)var2).getDecimalMaxValidatorNumber();
         case 7:
            throw new UnsupportedOperationException(
               "Cannot mutate property [decimalMaxValidatorNumber] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 8:
            return ((DefaultConstraintValidators)var2).getDecimalMinValidatorCharSequence();
         case 9:
            throw new UnsupportedOperationException(
               "Cannot mutate property [decimalMinValidatorCharSequence] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 10:
            return ((DefaultConstraintValidators)var2).getDecimalMinValidatorNumber();
         case 11:
            throw new UnsupportedOperationException(
               "Cannot mutate property [decimalMinValidatorNumber] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 12:
            return ((DefaultConstraintValidators)var2).getDigitsValidatorNumber();
         case 13:
            throw new UnsupportedOperationException(
               "Cannot mutate property [digitsValidatorNumber] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 14:
            return ((DefaultConstraintValidators)var2).getDigitsValidatorCharSequence();
         case 15:
            throw new UnsupportedOperationException(
               "Cannot mutate property [digitsValidatorCharSequence] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 16:
            return ((DefaultConstraintValidators)var2).getMaxNumberValidator();
         case 17:
            throw new UnsupportedOperationException(
               "Cannot mutate property [maxNumberValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 18:
            return ((DefaultConstraintValidators)var2).getMinNumberValidator();
         case 19:
            throw new UnsupportedOperationException(
               "Cannot mutate property [minNumberValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 20:
            return ((DefaultConstraintValidators)var2).getNegativeNumberValidator();
         case 21:
            throw new UnsupportedOperationException(
               "Cannot mutate property [negativeNumberValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 22:
            return ((DefaultConstraintValidators)var2).getNegativeOrZeroNumberValidator();
         case 23:
            throw new UnsupportedOperationException(
               "Cannot mutate property [negativeOrZeroNumberValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 24:
            return ((DefaultConstraintValidators)var2).getPositiveNumberValidator();
         case 25:
            throw new UnsupportedOperationException(
               "Cannot mutate property [positiveNumberValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 26:
            return ((DefaultConstraintValidators)var2).getPositiveOrZeroNumberValidator();
         case 27:
            throw new UnsupportedOperationException(
               "Cannot mutate property [positiveOrZeroNumberValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 28:
            return ((DefaultConstraintValidators)var2).getNotBlankValidator();
         case 29:
            throw new UnsupportedOperationException(
               "Cannot mutate property [notBlankValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 30:
            return ((DefaultConstraintValidators)var2).getNotNullValidator();
         case 31:
            throw new UnsupportedOperationException(
               "Cannot mutate property [notNullValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 32:
            return ((DefaultConstraintValidators)var2).getNullValidator();
         case 33:
            throw new UnsupportedOperationException(
               "Cannot mutate property [nullValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 34:
            return ((DefaultConstraintValidators)var2).getNotEmptyByteArrayValidator();
         case 35:
            throw new UnsupportedOperationException(
               "Cannot mutate property [notEmptyByteArrayValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 36:
            return ((DefaultConstraintValidators)var2).getNotEmptyCharArrayValidator();
         case 37:
            throw new UnsupportedOperationException(
               "Cannot mutate property [notEmptyCharArrayValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 38:
            return ((DefaultConstraintValidators)var2).getNotEmptyBooleanArrayValidator();
         case 39:
            throw new UnsupportedOperationException(
               "Cannot mutate property [notEmptyBooleanArrayValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 40:
            return ((DefaultConstraintValidators)var2).getNotEmptyDoubleArrayValidator();
         case 41:
            throw new UnsupportedOperationException(
               "Cannot mutate property [notEmptyDoubleArrayValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 42:
            return ((DefaultConstraintValidators)var2).getNotEmptyFloatArrayValidator();
         case 43:
            throw new UnsupportedOperationException(
               "Cannot mutate property [notEmptyFloatArrayValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 44:
            return ((DefaultConstraintValidators)var2).getNotEmptyIntArrayValidator();
         case 45:
            throw new UnsupportedOperationException(
               "Cannot mutate property [notEmptyIntArrayValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 46:
            return ((DefaultConstraintValidators)var2).getNotEmptyLongArrayValidator();
         case 47:
            throw new UnsupportedOperationException(
               "Cannot mutate property [notEmptyLongArrayValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 48:
            return ((DefaultConstraintValidators)var2).getNotEmptyObjectArrayValidator();
         case 49:
            throw new UnsupportedOperationException(
               "Cannot mutate property [notEmptyObjectArrayValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 50:
            return ((DefaultConstraintValidators)var2).getNotEmptyShortArrayValidator();
         case 51:
            throw new UnsupportedOperationException(
               "Cannot mutate property [notEmptyShortArrayValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 52:
            return ((DefaultConstraintValidators)var2).getNotEmptyCharSequenceValidator();
         case 53:
            throw new UnsupportedOperationException(
               "Cannot mutate property [notEmptyCharSequenceValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 54:
            return ((DefaultConstraintValidators)var2).getNotEmptyCollectionValidator();
         case 55:
            throw new UnsupportedOperationException(
               "Cannot mutate property [notEmptyCollectionValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 56:
            return ((DefaultConstraintValidators)var2).getNotEmptyMapValidator();
         case 57:
            throw new UnsupportedOperationException(
               "Cannot mutate property [notEmptyMapValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 58:
            return ((DefaultConstraintValidators)var2).getSizeObjectArrayValidator();
         case 59:
            throw new UnsupportedOperationException(
               "Cannot mutate property [sizeObjectArrayValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 60:
            return ((DefaultConstraintValidators)var2).getSizeByteArrayValidator();
         case 61:
            throw new UnsupportedOperationException(
               "Cannot mutate property [sizeByteArrayValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 62:
            return ((DefaultConstraintValidators)var2).getSizeCharArrayValidator();
         case 63:
            throw new UnsupportedOperationException(
               "Cannot mutate property [sizeCharArrayValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 64:
            return ((DefaultConstraintValidators)var2).getSizeBooleanArrayValidator();
         case 65:
            throw new UnsupportedOperationException(
               "Cannot mutate property [sizeBooleanArrayValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 66:
            return ((DefaultConstraintValidators)var2).getSizeDoubleArrayValidator();
         case 67:
            throw new UnsupportedOperationException(
               "Cannot mutate property [sizeDoubleArrayValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 68:
            return ((DefaultConstraintValidators)var2).getSizeFloatArrayValidator();
         case 69:
            throw new UnsupportedOperationException(
               "Cannot mutate property [sizeFloatArrayValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 70:
            return ((DefaultConstraintValidators)var2).getSizeIntArrayValidator();
         case 71:
            throw new UnsupportedOperationException(
               "Cannot mutate property [sizeIntArrayValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 72:
            return ((DefaultConstraintValidators)var2).getSizeLongArrayValidator();
         case 73:
            throw new UnsupportedOperationException(
               "Cannot mutate property [sizeLongArrayValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 74:
            return ((DefaultConstraintValidators)var2).getSizeShortArrayValidator();
         case 75:
            throw new UnsupportedOperationException(
               "Cannot mutate property [sizeShortArrayValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 76:
            return ((DefaultConstraintValidators)var2).getSizeCharSequenceValidator();
         case 77:
            throw new UnsupportedOperationException(
               "Cannot mutate property [sizeCharSequenceValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 78:
            return ((DefaultConstraintValidators)var2).getSizeCollectionValidator();
         case 79:
            throw new UnsupportedOperationException(
               "Cannot mutate property [sizeCollectionValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 80:
            return ((DefaultConstraintValidators)var2).getSizeMapValidator();
         case 81:
            throw new UnsupportedOperationException(
               "Cannot mutate property [sizeMapValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 82:
            return ((DefaultConstraintValidators)var2).getPastTemporalAccessorConstraintValidator();
         case 83:
            throw new UnsupportedOperationException(
               "Cannot mutate property [pastTemporalAccessorConstraintValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 84:
            return ((DefaultConstraintValidators)var2).getPastDateConstraintValidator();
         case 85:
            throw new UnsupportedOperationException(
               "Cannot mutate property [pastDateConstraintValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 86:
            return ((DefaultConstraintValidators)var2).getPastOrPresentTemporalAccessorConstraintValidator();
         case 87:
            throw new UnsupportedOperationException(
               "Cannot mutate property [pastOrPresentTemporalAccessorConstraintValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 88:
            return ((DefaultConstraintValidators)var2).getPastOrPresentDateConstraintValidator();
         case 89:
            throw new UnsupportedOperationException(
               "Cannot mutate property [pastOrPresentDateConstraintValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 90:
            return ((DefaultConstraintValidators)var2).getFutureTemporalAccessorConstraintValidator();
         case 91:
            throw new UnsupportedOperationException(
               "Cannot mutate property [futureTemporalAccessorConstraintValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 92:
            return ((DefaultConstraintValidators)var2).getFutureDateConstraintValidator();
         case 93:
            throw new UnsupportedOperationException(
               "Cannot mutate property [futureDateConstraintValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 94:
            return ((DefaultConstraintValidators)var2).getFutureOrPresentTemporalAccessorConstraintValidator();
         case 95:
            throw new UnsupportedOperationException(
               "Cannot mutate property [futureOrPresentTemporalAccessorConstraintValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         case 96:
            return ((DefaultConstraintValidators)var2).getFutureOrPresentDateConstraintValidator();
         case 97:
            throw new UnsupportedOperationException(
               "Cannot mutate property [futureOrPresentDateConstraintValidator] that is not mutable via a setter method or constructor argument for type: io.micronaut.validation.validator.constraints.DefaultConstraintValidators"
            );
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   public final int propertyIndexOf(String var1) {
      switch(var1.hashCode()) {
         case -2144362120:
            if (var1.equals("notNullValidator")) {
               return 15;
            }
            break;
         case -2084331991:
            if (var1.equals("negativeOrZeroNumberValidator")) {
               return 11;
            }
            break;
         case -1951115945:
            if (var1.equals("minNumberValidator")) {
               return 9;
            }
            break;
         case -1912148550:
            if (var1.equals("decimalMinValidatorNumber")) {
               return 5;
            }
            break;
         case -1831756668:
            if (var1.equals("notEmptyDoubleArrayValidator")) {
               return 20;
            }
            break;
         case -1730754839:
            if (var1.equals("futureTemporalAccessorConstraintValidator")) {
               return 45;
            }
            break;
         case -1669147530:
            if (var1.equals("sizeLongArrayValidator")) {
               return 36;
            }
            break;
         case -1626338028:
            if (var1.equals("negativeNumberValidator")) {
               return 10;
            }
            break;
         case -1547372549:
            if (var1.equals("notEmptyShortArrayValidator")) {
               return 25;
            }
            break;
         case -1516434854:
            if (var1.equals("sizeCharSequenceValidator")) {
               return 38;
            }
            break;
         case -1512516908:
            if (var1.equals("sizeShortArrayValidator")) {
               return 37;
            }
            break;
         case -1423504894:
            if (var1.equals("sizeByteArrayValidator")) {
               return 30;
            }
            break;
         case -1411845354:
            if (var1.equals("decimalMaxValidatorCharSequence")) {
               return 2;
            }
            break;
         case -1137937688:
            if (var1.equals("decimalMinValidatorCharSequence")) {
               return 4;
            }
            break;
         case -1116928009:
            if (var1.equals("sizeMapValidator")) {
               return 40;
            }
            break;
         case -1116082577:
            if (var1.equals("notEmptyLongArrayValidator")) {
               return 23;
            }
            break;
         case -1000779600:
            if (var1.equals("notEmptyMapValidator")) {
               return 28;
            }
            break;
         case -956797067:
            if (var1.equals("digitsValidatorNumber")) {
               return 6;
            }
            break;
         case -870439941:
            if (var1.equals("notEmptyByteArrayValidator")) {
               return 17;
            }
            break;
         case -751231797:
            if (var1.equals("sizeDoubleArrayValidator")) {
               return 33;
            }
            break;
         case -703066107:
            if (var1.equals("maxNumberValidator")) {
               return 8;
            }
            break;
         case -664098712:
            if (var1.equals("decimalMaxValidatorNumber")) {
               return 3;
            }
            break;
         case -652967487:
            if (var1.equals("notEmptyCharSequenceValidator")) {
               return 26;
            }
            break;
         case -534803723:
            if (var1.equals("pastDateConstraintValidator")) {
               return 42;
            }
            break;
         case -449145286:
            if (var1.equals("pastTemporalAccessorConstraintValidator")) {
               return 41;
            }
            break;
         case -429002310:
            if (var1.equals("notEmptyCollectionValidator")) {
               return 27;
            }
            break;
         case -423486512:
            if (var1.equals("sizeCharArrayValidator")) {
               return 31;
            }
            break;
         case -394146669:
            if (var1.equals("sizeCollectionValidator")) {
               return 39;
            }
            break;
         case -359519374:
            if (var1.equals("notEmptyObjectArrayValidator")) {
               return 24;
            }
            break;
         case -255637056:
            if (var1.equals("sizeBooleanArrayValidator")) {
               return 32;
            }
            break;
         case -221190969:
            if (var1.equals("sizeIntArrayValidator")) {
               return 35;
            }
            break;
         case -47901979:
            if (var1.equals("positiveOrZeroNumberValidator")) {
               return 13;
            }
            break;
         case 129578441:
            if (var1.equals("notEmptyCharArrayValidator")) {
               return 18;
            }
            break;
         case 356560267:
            if (var1.equals("nullValidator")) {
               return 16;
            }
            break;
         case 479755366:
            if (var1.equals("pastOrPresentTemporalAccessorConstraintValidator")) {
               return 43;
            }
            break;
         case 607830311:
            if (var1.equals("notEmptyBooleanArrayValidator")) {
               return 19;
            }
            break;
         case 621597630:
            if (var1.equals("assertTrueValidator")) {
               return 1;
            }
            break;
         case 661916196:
            if (var1.equals("futureDateConstraintValidator")) {
               return 46;
            }
            break;
         case 721005497:
            if (var1.equals("sizeObjectArrayValidator")) {
               return 29;
            }
            break;
         case 1034969617:
            if (var1.equals("notBlankValidator")) {
               return 14;
            }
            break;
         case 1101691090:
            if (var1.equals("futureOrPresentDateConstraintValidator")) {
               return 48;
            }
            break;
         case 1105482529:
            if (var1.equals("pastOrPresentDateConstraintValidator")) {
               return 44;
            }
            break;
         case 1223081680:
            if (var1.equals("positiveNumberValidator")) {
               return 12;
            }
            break;
         case 1236651483:
            if (var1.equals("notEmptyFloatArrayValidator")) {
               return 21;
            }
            break;
         case 1271507124:
            if (var1.equals("sizeFloatArrayValidator")) {
               return 34;
            }
            break;
         case 1292416597:
            if (var1.equals("assertFalseValidator")) {
               return 0;
            }
            break;
         case 1684438115:
            if (var1.equals("digitsValidatorCharSequence")) {
               return 7;
            }
            break;
         case 2013407150:
            if (var1.equals("notEmptyIntArrayValidator")) {
               return 22;
            }
            break;
         case 2078711959:
            if (var1.equals("futureOrPresentTemporalAccessorConstraintValidator")) {
               return 47;
            }
      }

      return -1;
   }

   @Override
   public Object instantiate() {
      return new DefaultConstraintValidators();
   }

   @Override
   public Object instantiateInternal(Object[] var1) {
      return new DefaultConstraintValidators((BeanContext)var1[0]);
   }
}

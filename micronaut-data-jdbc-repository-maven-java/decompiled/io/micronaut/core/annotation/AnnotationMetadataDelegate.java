package io.micronaut.core.annotation;

import io.micronaut.core.type.Argument;
import io.micronaut.core.value.OptionalValues;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

public interface AnnotationMetadataDelegate extends AnnotationMetadataProvider, AnnotationMetadata {
   @Override
   default Set<String> getStereotypeAnnotationNames() {
      return this.getAnnotationMetadata().getStereotypeAnnotationNames();
   }

   @Override
   default Set<String> getDeclaredStereotypeAnnotationNames() {
      return this.getAnnotationMetadata().getDeclaredStereotypeAnnotationNames();
   }

   @NonNull
   @Override
   default AnnotationMetadata getDeclaredMetadata() {
      return this.getAnnotationMetadata().getDeclaredMetadata();
   }

   @Override
   default boolean hasSimpleAnnotation(@Nullable String annotation) {
      return this.getAnnotationMetadata().hasSimpleAnnotation(annotation);
   }

   @Override
   default boolean hasPropertyExpressions() {
      return this.getAnnotationMetadata().hasPropertyExpressions();
   }

   @Override
   default boolean hasSimpleDeclaredAnnotation(@Nullable String annotation) {
      return this.getAnnotationMetadata().hasSimpleDeclaredAnnotation(annotation);
   }

   @Override
   default <E extends Enum> E[] enumValues(@NonNull String annotation, Class<E> enumType) {
      return (E[])this.getAnnotationMetadata().enumValues(annotation, enumType);
   }

   @Override
   default <E extends Enum> E[] enumValues(@NonNull String annotation, @NonNull String member, Class<E> enumType) {
      return (E[])this.getAnnotationMetadata().enumValues(annotation, member, enumType);
   }

   @Override
   default <E extends Enum> E[] enumValues(@NonNull Class<? extends Annotation> annotation, Class<E> enumType) {
      return (E[])this.getAnnotationMetadata().enumValues(annotation, enumType);
   }

   @Override
   default <E extends Enum> E[] enumValues(@NonNull Class<? extends Annotation> annotation, @NonNull String member, Class<E> enumType) {
      return (E[])this.getAnnotationMetadata().enumValues(annotation, member, enumType);
   }

   @Override
   default <T> Class<T>[] classValues(@NonNull String annotation) {
      return this.getAnnotationMetadata().classValues(annotation, "value");
   }

   @Override
   default <T> Class<T>[] classValues(@NonNull String annotation, @NonNull String member) {
      return this.getAnnotationMetadata().classValues(annotation, member);
   }

   @Override
   default <T> Class<T>[] classValues(@NonNull Class<? extends Annotation> annotation) {
      return this.getAnnotationMetadata().classValues(annotation, "value");
   }

   @Override
   default <T> Class<T>[] classValues(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return this.getAnnotationMetadata().classValues(annotation, member);
   }

   @Override
   default <E extends Enum> Optional<E> enumValue(@NonNull String annotation, Class<E> enumType) {
      return this.getAnnotationMetadata().enumValue(annotation, enumType);
   }

   @Override
   default <E extends Enum> Optional<E> enumValue(@NonNull String annotation, @NonNull String member, Class<E> enumType) {
      return this.getAnnotationMetadata().enumValue(annotation, member, enumType);
   }

   @Override
   default <E extends Enum> Optional<E> enumValue(@NonNull Class<? extends Annotation> annotation, Class<E> enumType) {
      return this.getAnnotationMetadata().enumValue(annotation, enumType);
   }

   @Override
   default <E extends Enum> Optional<E> enumValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, Class<E> enumType) {
      return this.getAnnotationMetadata().enumValue(annotation, member, enumType);
   }

   @Override
   default OptionalLong longValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return this.getAnnotationMetadata().longValue(annotation, member);
   }

   @Override
   default Optional<Boolean> booleanValue(@NonNull String annotation, @NonNull String member) {
      return this.getAnnotationMetadata().booleanValue(annotation, member);
   }

   @Override
   default Optional<Boolean> booleanValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return this.getAnnotationMetadata().booleanValue(annotation, member);
   }

   @NonNull
   @Override
   default Optional<Boolean> booleanValue(@NonNull Class<? extends Annotation> annotation) {
      return this.getAnnotationMetadata().booleanValue(annotation, "value");
   }

   @NonNull
   @Override
   default Optional<Boolean> booleanValue(@NonNull String annotation) {
      return this.getAnnotationMetadata().booleanValue(annotation, "value");
   }

   @NonNull
   @Override
   default String[] stringValues(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return this.getAnnotationMetadata().stringValues(annotation, member);
   }

   @NonNull
   @Override
   default String[] stringValues(@NonNull Class<? extends Annotation> annotation) {
      return this.getAnnotationMetadata().stringValues(annotation, "value");
   }

   @NonNull
   @Override
   default String[] stringValues(@NonNull String annotation, @NonNull String member) {
      return this.getAnnotationMetadata().stringValues(annotation, member);
   }

   @NonNull
   @Override
   default String[] stringValues(@NonNull String annotation) {
      return this.getAnnotationMetadata().stringValues(annotation, "value");
   }

   @NonNull
   @Override
   default OptionalInt intValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return this.getAnnotationMetadata().intValue(annotation, member);
   }

   @NonNull
   @Override
   default OptionalInt intValue(@NonNull Class<? extends Annotation> annotation) {
      return this.getAnnotationMetadata().intValue(annotation);
   }

   @NonNull
   @Override
   default Optional<String> stringValue(@NonNull String annotation, @NonNull String member) {
      return this.getAnnotationMetadata().stringValue(annotation, member);
   }

   @NonNull
   @Override
   default Optional<String> stringValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return this.getAnnotationMetadata().stringValue(annotation, member);
   }

   @NonNull
   @Override
   default Optional<String> stringValue(@NonNull Class<? extends Annotation> annotation) {
      return this.getAnnotationMetadata().stringValue(annotation);
   }

   @NonNull
   @Override
   default Optional<String> stringValue(@NonNull String annotation) {
      return this.getAnnotationMetadata().stringValue(annotation);
   }

   @NonNull
   @Override
   default OptionalDouble doubleValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return this.getAnnotationMetadata().doubleValue(annotation, member);
   }

   @NonNull
   @Override
   default OptionalDouble doubleValue(@NonNull Class<? extends Annotation> annotation) {
      return this.getAnnotationMetadata().doubleValue(annotation);
   }

   @NonNull
   @Override
   default Map<String, Object> getDefaultValues(@NonNull String annotation) {
      return this.getAnnotationMetadata().getDefaultValues(annotation);
   }

   @NonNull
   @Override
   default <T> Optional<T> getValue(@NonNull String annotation, @NonNull Argument<T> requiredType) {
      return this.getAnnotationMetadata().getValue(annotation, requiredType);
   }

   @NonNull
   @Override
   default <T> Optional<T> getValue(@NonNull Class<? extends Annotation> annotation, @NonNull Argument<T> requiredType) {
      return this.getAnnotationMetadata().getValue(annotation, requiredType);
   }

   @NonNull
   @Override
   default <T> Optional<T> getValue(@NonNull String annotation, @NonNull String member, @NonNull Argument<T> requiredType) {
      return this.getAnnotationMetadata().getValue(annotation, member, requiredType);
   }

   @NonNull
   @Override
   default <T> Optional<T> getDefaultValue(@NonNull String annotation, @NonNull String member, @NonNull Argument<T> requiredType) {
      return this.getAnnotationMetadata().getDefaultValue(annotation, member, requiredType);
   }

   @NonNull
   @Override
   default <T> Optional<T> getDefaultValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, @NonNull Argument<T> requiredType) {
      return this.getAnnotationMetadata().getDefaultValue(annotation, member, requiredType);
   }

   @NonNull
   @Override
   default <T> Optional<T> getValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, @NonNull Argument<T> requiredType) {
      return this.getAnnotationMetadata().getDefaultValue(annotation, member, requiredType);
   }

   @Override
   default <T extends Annotation> T synthesizeDeclared(@NonNull Class<T> annotationClass) {
      return this.getAnnotationMetadata().synthesizeDeclared(annotationClass);
   }

   @NonNull
   @Override
   default <T extends Annotation> T[] synthesizeAnnotationsByType(@NonNull Class<T> annotationClass) {
      return (T[])this.getAnnotationMetadata().synthesizeAnnotationsByType(annotationClass);
   }

   @NonNull
   @Override
   default <T extends Annotation> T[] synthesizeDeclaredAnnotationsByType(@NonNull Class<T> annotationClass) {
      return (T[])this.getAnnotationMetadata().synthesizeDeclaredAnnotationsByType(annotationClass);
   }

   @Nullable
   @Override
   default <T extends Annotation> AnnotationValue<T> getAnnotation(@NonNull String annotation) {
      return this.getAnnotationMetadata().getAnnotation(annotation);
   }

   @Nullable
   @Override
   default <T extends Annotation> AnnotationValue<T> getAnnotation(@NonNull Class<T> annotationClass) {
      return this.getAnnotationMetadata().getAnnotation(annotationClass);
   }

   @Nullable
   @Override
   default <T extends Annotation> AnnotationValue<T> getDeclaredAnnotation(@NonNull String annotation) {
      return this.getAnnotationMetadata().getDeclaredAnnotation(annotation);
   }

   @NonNull
   @Override
   default <T extends Annotation> Optional<AnnotationValue<T>> findDeclaredAnnotation(@NonNull Class<T> annotationClass) {
      return this.getAnnotationMetadata().findDeclaredAnnotation(annotationClass);
   }

   @Nullable
   @Override
   default <T extends Annotation> AnnotationValue<T> getDeclaredAnnotation(@NonNull Class<T> annotationClass) {
      return this.getAnnotationMetadata().getDeclaredAnnotation(annotationClass);
   }

   @Override
   default boolean isAnnotationPresent(@NonNull Class<? extends Annotation> annotationClass) {
      return this.getAnnotationMetadata().isAnnotationPresent(annotationClass);
   }

   @Override
   default boolean isDeclaredAnnotationPresent(@NonNull Class<? extends Annotation> annotationClass) {
      return this.getAnnotationMetadata().isDeclaredAnnotationPresent(annotationClass);
   }

   @NonNull
   @Override
   default <T> Optional<T> getDefaultValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, @NonNull Class<T> requiredType) {
      return this.getAnnotationMetadata().getDefaultValue(annotation, member, requiredType);
   }

   @NonNull
   @Override
   default <T> Optional<T> getValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, @NonNull Class<T> requiredType) {
      return this.getAnnotationMetadata().getValue(annotation, member, requiredType);
   }

   @NonNull
   @Override
   default Optional<String> getAnnotationNameByStereotype(String stereotype) {
      return this.getAnnotationMetadata().getAnnotationNameByStereotype(stereotype);
   }

   @NonNull
   @Override
   default Optional<String> getDeclaredAnnotationNameByStereotype(String stereotype) {
      return this.getAnnotationMetadata().getDeclaredAnnotationNameByStereotype(stereotype);
   }

   @NonNull
   @Override
   default Optional<Class<? extends Annotation>> getAnnotationTypeByStereotype(@NonNull Class<? extends Annotation> stereotype) {
      return this.getAnnotationMetadata().getAnnotationTypeByStereotype(stereotype);
   }

   @NonNull
   @Override
   default Optional<Class<? extends Annotation>> getDeclaredAnnotationTypeByStereotype(@NonNull Class<? extends Annotation> stereotype) {
      return this.getAnnotationMetadata().getDeclaredAnnotationTypeByStereotype(stereotype);
   }

   @NonNull
   @Override
   default Optional<Class<? extends Annotation>> getDeclaredAnnotationTypeByStereotype(String stereotype) {
      return this.getAnnotationMetadata().getDeclaredAnnotationTypeByStereotype(stereotype);
   }

   @NonNull
   @Override
   default Optional<Class<? extends Annotation>> getAnnotationTypeByStereotype(String stereotype) {
      return this.getAnnotationMetadata().getAnnotationTypeByStereotype(stereotype);
   }

   @NonNull
   @Override
   default Optional<String> getAnnotationNameByStereotype(@NonNull Class<? extends Annotation> stereotype) {
      return this.getAnnotationMetadata().getAnnotationNameByStereotype(stereotype);
   }

   @NonNull
   @Override
   default <T> OptionalValues<T> getValues(@NonNull Class<? extends Annotation> annotation, @NonNull Class<T> valueType) {
      return this.getAnnotationMetadata().getValues(annotation, valueType);
   }

   @NonNull
   @Override
   default List<String> getAnnotationNamesByStereotype(@NonNull Class<? extends Annotation> stereotype) {
      return this.getAnnotationMetadata().getAnnotationNamesByStereotype(stereotype);
   }

   @NonNull
   @Override
   default List<Class<? extends Annotation>> getAnnotationTypesByStereotype(@NonNull Class<? extends Annotation> stereotype) {
      return this.getAnnotationMetadata().getAnnotationTypesByStereotype(stereotype);
   }

   @NonNull
   @Override
   default List<Class<? extends Annotation>> getAnnotationTypesByStereotype(@NonNull String stereotype) {
      return this.getAnnotationMetadata().getAnnotationTypesByStereotype(stereotype);
   }

   @NonNull
   @Override
   default List<Class<? extends Annotation>> getAnnotationTypesByStereotype(@NonNull Class<? extends Annotation> stereotype, @NonNull ClassLoader classLoader) {
      return this.getAnnotationMetadata().getAnnotationTypesByStereotype(stereotype, classLoader);
   }

   @NonNull
   @Override
   default <T extends Annotation> Optional<AnnotationValue<T>> findAnnotation(@NonNull Class<T> annotationClass) {
      return this.getAnnotationMetadata().findAnnotation(annotationClass);
   }

   @NonNull
   @Override
   default <T> Optional<T> getValue(@NonNull String annotation, @NonNull String member, @NonNull Class<T> requiredType) {
      return this.getAnnotationMetadata().getValue(annotation, member, requiredType);
   }

   @NonNull
   @Override
   default OptionalLong longValue(@NonNull String annotation, @NonNull String member) {
      return this.getAnnotationMetadata().longValue(annotation, member);
   }

   @NonNull
   @Override
   default Optional<Class> classValue(@NonNull String annotation) {
      return this.getAnnotationMetadata().classValue(annotation);
   }

   @NonNull
   @Override
   default Optional<Class> classValue(@NonNull String annotation, @NonNull String member) {
      return this.getAnnotationMetadata().classValue(annotation, member);
   }

   @NonNull
   @Override
   default Optional<Class> classValue(@NonNull Class<? extends Annotation> annotation) {
      return this.getAnnotationMetadata().classValue(annotation);
   }

   @NonNull
   @Override
   default Optional<Class> classValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return this.getAnnotationMetadata().classValue(annotation, member);
   }

   @NonNull
   @Override
   default OptionalInt intValue(@NonNull String annotation, @NonNull String member) {
      return this.getAnnotationMetadata().intValue(annotation, member);
   }

   @NonNull
   @Override
   default OptionalDouble doubleValue(@NonNull String annotation, @NonNull String member) {
      return this.getAnnotationMetadata().doubleValue(annotation, member);
   }

   @NonNull
   @Override
   default <T> Optional<T> getValue(@NonNull String annotation, @NonNull Class<T> requiredType) {
      return this.getAnnotationMetadata().getValue(annotation, requiredType);
   }

   @NonNull
   @Override
   default Optional<Object> getValue(@NonNull String annotation, @NonNull String member) {
      return this.getAnnotationMetadata().getValue(annotation, member);
   }

   @NonNull
   @Override
   default Optional<Object> getValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return this.getAnnotationMetadata().getValue(annotation, member);
   }

   @Override
   default boolean isTrue(@NonNull String annotation, @NonNull String member) {
      return this.getAnnotationMetadata().isTrue(annotation, member);
   }

   @Override
   default boolean isTrue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return this.getAnnotationMetadata().isTrue(annotation, member);
   }

   @Override
   default boolean isPresent(@NonNull String annotation, @NonNull String member) {
      return this.getAnnotationMetadata().isPresent(annotation, member);
   }

   @Override
   default boolean isPresent(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return this.getAnnotationMetadata().isPresent(annotation, member);
   }

   @Override
   default boolean isFalse(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return this.getAnnotationMetadata().isFalse(annotation, member);
   }

   @Override
   default boolean isFalse(@NonNull String annotation, @NonNull String member) {
      return this.getAnnotationMetadata().isFalse(annotation, member);
   }

   @NonNull
   @Override
   default Optional<Object> getValue(@NonNull String annotation) {
      return this.getAnnotationMetadata().getValue(annotation);
   }

   @NonNull
   @Override
   default Optional<Object> getValue(@NonNull Class<? extends Annotation> annotation) {
      return this.getAnnotationMetadata().getValue(annotation);
   }

   @NonNull
   @Override
   default <T> Optional<T> getValue(@NonNull Class<? extends Annotation> annotation, @NonNull Class<T> requiredType) {
      return this.getAnnotationMetadata().getValue(annotation, requiredType);
   }

   @NonNull
   @Override
   default Optional<Class<? extends Annotation>> getAnnotationType(@NonNull String name) {
      return this.getAnnotationMetadata().getAnnotationType(name);
   }

   @NonNull
   @Override
   default Optional<Class<? extends Annotation>> getAnnotationType(@NonNull String name, @NonNull ClassLoader classLoader) {
      return this.getAnnotationMetadata().getAnnotationType(name, classLoader);
   }

   @Override
   default boolean hasAnnotation(@Nullable Class<? extends Annotation> annotation) {
      return this.getAnnotationMetadata().hasAnnotation(annotation);
   }

   @Override
   default boolean hasStereotype(@Nullable Class<? extends Annotation> annotation) {
      return this.getAnnotationMetadata().hasStereotype(annotation);
   }

   @Override
   default boolean hasStereotype(Class<? extends Annotation>... annotations) {
      return this.getAnnotationMetadata().hasStereotype(annotations);
   }

   @Override
   default boolean hasStereotype(String[] annotations) {
      return this.getAnnotationMetadata().hasStereotype(annotations);
   }

   @Override
   default boolean hasDeclaredAnnotation(@Nullable Class<? extends Annotation> annotation) {
      return this.getAnnotationMetadata().hasDeclaredAnnotation(annotation);
   }

   @Override
   default boolean hasDeclaredStereotype(@Nullable Class<? extends Annotation> stereotype) {
      return this.getAnnotationMetadata().hasDeclaredStereotype(stereotype);
   }

   @Override
   default boolean hasDeclaredStereotype(Class<? extends Annotation>... annotations) {
      return this.getAnnotationMetadata().hasDeclaredStereotype(annotations);
   }

   @Override
   default boolean isEmpty() {
      return this.getAnnotationMetadata().isEmpty();
   }

   @Override
   default boolean hasDeclaredAnnotation(String annotation) {
      return this.getAnnotationMetadata().hasDeclaredAnnotation(annotation);
   }

   @NonNull
   @Override
   default Set<String> getAnnotationNames() {
      return this.getAnnotationMetadata().getAnnotationNames();
   }

   @NonNull
   @Override
   default Set<String> getDeclaredAnnotationNames() {
      return this.getAnnotationMetadata().getDeclaredAnnotationNames();
   }

   @Override
   default boolean hasAnnotation(String annotation) {
      return this.getAnnotationMetadata().hasAnnotation(annotation);
   }

   @Override
   default boolean hasStereotype(String annotation) {
      return this.getAnnotationMetadata().hasStereotype(annotation);
   }

   @Override
   default boolean hasDeclaredStereotype(String annotation) {
      return this.getAnnotationMetadata().hasDeclaredStereotype(annotation);
   }

   @NonNull
   @Override
   default List<String> getAnnotationNamesByStereotype(String stereotype) {
      return this.getAnnotationMetadata().getAnnotationNamesByStereotype(stereotype);
   }

   @NonNull
   @Override
   default List<String> getDeclaredAnnotationNamesByStereotype(String stereotype) {
      return this.getAnnotationMetadata().getDeclaredAnnotationNamesByStereotype(stereotype);
   }

   @NonNull
   @Override
   default <T extends Annotation> Optional<AnnotationValue<T>> findAnnotation(@NonNull String annotation) {
      return this.getAnnotationMetadata().findAnnotation(annotation);
   }

   @NonNull
   @Override
   default <T> OptionalValues<T> getValues(@NonNull String annotation, @NonNull Class<T> valueType) {
      return this.getAnnotationMetadata().getValues(annotation, valueType);
   }

   @NonNull
   @Override
   default <T extends Annotation> Optional<AnnotationValue<T>> findDeclaredAnnotation(@NonNull String annotation) {
      return this.getAnnotationMetadata().findDeclaredAnnotation(annotation);
   }

   @NonNull
   @Override
   default <T> Optional<T> getDefaultValue(@NonNull String annotation, @NonNull String member, @NonNull Class<T> requiredType) {
      return this.getAnnotationMetadata().getDefaultValue(annotation, member, requiredType);
   }

   @Nullable
   @Override
   default <T extends Annotation> T synthesize(@NonNull Class<T> annotationClass) {
      return this.getAnnotationMetadata().synthesize(annotationClass);
   }

   @Nullable
   @Override
   default <T extends Annotation> T synthesize(@NonNull Class<T> annotationClass, @NonNull String sourceAnnotation) {
      return this.getAnnotationMetadata().synthesize(annotationClass, sourceAnnotation);
   }

   @Nullable
   @Override
   default <T extends Annotation> T synthesizeDeclared(@NonNull Class<T> annotationClass, @NonNull String sourceAnnotation) {
      return this.getAnnotationMetadata().synthesizeDeclared(annotationClass, sourceAnnotation);
   }

   @NonNull
   @Override
   default Annotation[] synthesizeAll() {
      return this.getAnnotationMetadata().synthesizeAll();
   }

   @NonNull
   @Override
   default Annotation[] synthesizeDeclared() {
      return this.getAnnotationMetadata().synthesizeDeclared();
   }

   @NonNull
   @Override
   default <T extends Annotation> List<AnnotationValue<T>> getAnnotationValuesByType(@NonNull Class<T> annotationType) {
      return this.getAnnotationMetadata().getAnnotationValuesByType(annotationType);
   }

   @NonNull
   @Override
   default <T extends Annotation> List<AnnotationValue<T>> getDeclaredAnnotationValuesByType(@NonNull Class<T> annotationType) {
      return this.getAnnotationMetadata().getDeclaredAnnotationValuesByType(annotationType);
   }

   @Override
   default boolean isRepeatableAnnotation(Class<? extends Annotation> annotation) {
      return this.getAnnotationMetadata().isRepeatableAnnotation(annotation);
   }

   @Override
   default boolean isRepeatableAnnotation(String annotation) {
      return this.getAnnotationMetadata().isRepeatableAnnotation(annotation);
   }

   @Override
   default Optional<String> findRepeatableAnnotation(Class<? extends Annotation> annotation) {
      return this.getAnnotationMetadata().findRepeatableAnnotation(annotation);
   }

   @Override
   default Optional<String> findRepeatableAnnotation(String annotation) {
      return this.getAnnotationMetadata().findRepeatableAnnotation(annotation);
   }
}

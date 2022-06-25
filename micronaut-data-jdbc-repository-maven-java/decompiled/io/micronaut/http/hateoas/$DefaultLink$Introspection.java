package io.micronaut.http.hateoas;

import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.Generated;
import io.micronaut.core.type.Argument;
import io.micronaut.http.MediaType;
import io.micronaut.inject.annotation.DefaultAnnotationMetadata;
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection;
import java.util.Collections;
import java.util.Optional;

// $FF: synthetic class
@Generated
final class $DefaultLink$Introspection extends AbstractInitializableBeanIntrospection {
   private static final Argument[] $CONSTRUCTOR_ARGUMENTS = new Argument[]{Argument.of(String.class, "uri")};
   private static final AbstractInitializableBeanIntrospection.BeanPropertyRef[] $PROPERTIES_REFERENCES = new AbstractInitializableBeanIntrospection.BeanPropertyRef[]{
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(String.class, "href"), 0, -1, 1, true, false),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(Argument.of(Boolean.TYPE, "templated"), 2, -1, 3, true, false),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(
            Optional.class,
            "type",
            null,
            Argument.ofTypeVariable(
               MediaType.class,
               "T",
               new DefaultAnnotationMetadata(
                  AnnotationUtil.mapOf(
                     "io.micronaut.core.annotation.TypeHint", AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_0()})
                  ),
                  Collections.EMPTY_MAP,
                  Collections.EMPTY_MAP,
                  AnnotationUtil.mapOf(
                     "io.micronaut.core.annotation.TypeHint", AnnotationUtil.mapOf("value", new AnnotationClassValue[]{$micronaut_load_class_value_0()})
                  ),
                  Collections.EMPTY_MAP,
                  false,
                  true
               )
            )
         ),
         4,
         -1,
         5,
         true,
         false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(Optional.class, "deprecation", null, Argument.ofTypeVariable(String.class, "T")), 6, -1, 7, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(Optional.class, "profile", null, Argument.ofTypeVariable(String.class, "T")), 8, -1, 9, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(Optional.class, "name", null, Argument.ofTypeVariable(String.class, "T")), 10, -1, 11, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(Optional.class, "title", null, Argument.ofTypeVariable(String.class, "T")), 12, -1, 13, true, false
      ),
      new AbstractInitializableBeanIntrospection.BeanPropertyRef(
         Argument.of(Optional.class, "hreflang", null, Argument.ofTypeVariable(String.class, "T")), 14, -1, 15, true, false
      )
   };

   public $DefaultLink$Introspection() {
      super(DefaultLink.class, $DefaultLink$IntrospectionRef.$ANNOTATION_METADATA, null, $CONSTRUCTOR_ARGUMENTS, $PROPERTIES_REFERENCES, null);
   }

   @Override
   protected final Object dispatchOne(int var1, Object var2, Object var3) {
      switch(var1) {
         case 0:
            return ((DefaultLink)var2).getHref();
         case 1:
            throw new UnsupportedOperationException(
               "Cannot mutate property [href] that is not mutable via a setter method or constructor argument for type: io.micronaut.http.hateoas.DefaultLink"
            );
         case 2:
            return ((DefaultLink)var2).isTemplated();
         case 3:
            throw new UnsupportedOperationException(
               "Cannot mutate property [templated] that is not mutable via a setter method or constructor argument for type: io.micronaut.http.hateoas.DefaultLink"
            );
         case 4:
            return ((DefaultLink)var2).getType();
         case 5:
            throw new UnsupportedOperationException(
               "Cannot mutate property [type] that is not mutable via a setter method or constructor argument for type: io.micronaut.http.hateoas.DefaultLink"
            );
         case 6:
            return ((DefaultLink)var2).getDeprecation();
         case 7:
            throw new UnsupportedOperationException(
               "Cannot mutate property [deprecation] that is not mutable via a setter method or constructor argument for type: io.micronaut.http.hateoas.DefaultLink"
            );
         case 8:
            return ((DefaultLink)var2).getProfile();
         case 9:
            throw new UnsupportedOperationException(
               "Cannot mutate property [profile] that is not mutable via a setter method or constructor argument for type: io.micronaut.http.hateoas.DefaultLink"
            );
         case 10:
            return ((DefaultLink)var2).getName();
         case 11:
            throw new UnsupportedOperationException(
               "Cannot mutate property [name] that is not mutable via a setter method or constructor argument for type: io.micronaut.http.hateoas.DefaultLink"
            );
         case 12:
            return ((DefaultLink)var2).getTitle();
         case 13:
            throw new UnsupportedOperationException(
               "Cannot mutate property [title] that is not mutable via a setter method or constructor argument for type: io.micronaut.http.hateoas.DefaultLink"
            );
         case 14:
            return ((DefaultLink)var2).getHreflang();
         case 15:
            throw new UnsupportedOperationException(
               "Cannot mutate property [hreflang] that is not mutable via a setter method or constructor argument for type: io.micronaut.http.hateoas.DefaultLink"
            );
         default:
            throw this.unknownDispatchAtIndexException(var1);
      }
   }

   @Override
   public final int propertyIndexOf(String var1) {
      switch(var1.hashCode()) {
         case -309425751:
            if (var1.equals("profile")) {
               return 4;
            }
            break;
         case 3211051:
            if (var1.equals("href")) {
               return 0;
            }
            break;
         case 3373707:
            if (var1.equals("name")) {
               return 5;
            }
            break;
         case 3575610:
            if (var1.equals("type")) {
               return 2;
            }
            break;
         case 110371416:
            if (var1.equals("title")) {
               return 6;
            }
            break;
         case 936927604:
            if (var1.equals("deprecation")) {
               return 3;
            }
            break;
         case 1948910489:
            if (var1.equals("hreflang")) {
               return 7;
            }
            break;
         case 1981727530:
            if (var1.equals("templated")) {
               return 1;
            }
      }

      return -1;
   }

   @Override
   public Object instantiateInternal(Object[] var1) {
      return new DefaultLink((String)var1[0]);
   }
}

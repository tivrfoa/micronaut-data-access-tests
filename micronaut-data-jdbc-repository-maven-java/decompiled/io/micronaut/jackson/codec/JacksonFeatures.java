package io.micronaut.jackson.codec;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.json.JsonFeatures;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Internal
public final class JacksonFeatures implements JsonFeatures {
   private final Map<SerializationFeature, Boolean> serializationFeatures = new EnumMap(SerializationFeature.class);
   private final Map<DeserializationFeature, Boolean> deserializationFeatures = new EnumMap(DeserializationFeature.class);
   private final List<Class<? extends Module>> additionalModules = new ArrayList();

   public static JacksonFeatures fromAnnotation(AnnotationValue<io.micronaut.jackson.annotation.JacksonFeatures> jacksonFeaturesAnn) {
      JacksonFeatures jacksonFeatures = new JacksonFeatures();
      SerializationFeature[] enabledSerializationFeatures = (SerializationFeature[])jacksonFeaturesAnn.get(
            "enabledSerializationFeatures", SerializationFeature[].class
         )
         .orElse(null);
      if (enabledSerializationFeatures != null) {
         for(SerializationFeature serializationFeature : enabledSerializationFeatures) {
            jacksonFeatures.addFeature(serializationFeature, true);
         }
      }

      DeserializationFeature[] enabledDeserializationFeatures = (DeserializationFeature[])jacksonFeaturesAnn.get(
            "enabledDeserializationFeatures", DeserializationFeature[].class
         )
         .orElse(null);
      if (enabledDeserializationFeatures != null) {
         for(DeserializationFeature deserializationFeature : enabledDeserializationFeatures) {
            jacksonFeatures.addFeature(deserializationFeature, true);
         }
      }

      SerializationFeature[] disabledSerializationFeatures = (SerializationFeature[])jacksonFeaturesAnn.get(
            "disabledSerializationFeatures", SerializationFeature[].class
         )
         .orElse(null);
      if (disabledSerializationFeatures != null) {
         for(SerializationFeature serializationFeature : disabledSerializationFeatures) {
            jacksonFeatures.addFeature(serializationFeature, false);
         }
      }

      DeserializationFeature[] disabledDeserializationFeatures = (DeserializationFeature[])jacksonFeaturesAnn.get(
            "disabledDeserializationFeatures", DeserializationFeature[].class
         )
         .orElse(null);
      if (disabledDeserializationFeatures != null) {
         for(DeserializationFeature feature : disabledDeserializationFeatures) {
            jacksonFeatures.addFeature(feature, false);
         }
      }

      Class<?>[] additionalModules = jacksonFeaturesAnn.classValues("additionalModules");
      if (ArrayUtils.isNotEmpty(additionalModules)) {
         for(Class<?> additionalModule : additionalModules) {
            jacksonFeatures.addModule(additionalModule);
         }
      }

      return jacksonFeatures;
   }

   public JacksonFeatures addFeature(SerializationFeature serializationFeature, boolean isEnabled) {
      this.serializationFeatures.put(serializationFeature, isEnabled);
      return this;
   }

   public JacksonFeatures addFeature(DeserializationFeature deserializationFeature, boolean isEnabled) {
      this.deserializationFeatures.put(deserializationFeature, isEnabled);
      return this;
   }

   @NonNull
   public JacksonFeatures addModule(@NonNull Class<? extends Module> moduleClass) {
      Objects.requireNonNull(moduleClass, "moduleClass");
      this.additionalModules.add(moduleClass);
      return this;
   }

   public Map<SerializationFeature, Boolean> getSerializationFeatures() {
      return this.serializationFeatures;
   }

   public Map<DeserializationFeature, Boolean> getDeserializationFeatures() {
      return this.deserializationFeatures;
   }

   @NonNull
   public List<Class<? extends Module>> getAdditionalModules() {
      return this.additionalModules;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         JacksonFeatures that = (JacksonFeatures)o;
         return Objects.equals(this.serializationFeatures, that.serializationFeatures)
            && Objects.equals(this.deserializationFeatures, that.deserializationFeatures);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.serializationFeatures, this.deserializationFeatures});
   }
}

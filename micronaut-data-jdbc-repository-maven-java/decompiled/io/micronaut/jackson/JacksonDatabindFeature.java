package io.micronaut.jackson;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.oracle.svm.core.annotate.AutomaticFeature;
import io.micronaut.core.annotation.Internal;
import java.util.stream.Stream;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import org.graalvm.nativeimage.hosted.Feature.BeforeAnalysisAccess;

@Internal
@AutomaticFeature
final class JacksonDatabindFeature implements Feature {
   public void beforeAnalysis(BeforeAnalysisAccess access) {
      Stream.of(
            PropertyNamingStrategies.LowerCamelCaseStrategy.class,
            PropertyNamingStrategies.UpperCamelCaseStrategy.class,
            PropertyNamingStrategies.SnakeCaseStrategy.class,
            PropertyNamingStrategies.UpperSnakeCaseStrategy.class,
            PropertyNamingStrategies.LowerCaseStrategy.class,
            PropertyNamingStrategies.KebabCaseStrategy.class,
            PropertyNamingStrategies.LowerDotCaseStrategy.class,
            PropertyNamingStrategy.UpperCamelCaseStrategy.class,
            PropertyNamingStrategy.SnakeCaseStrategy.class,
            PropertyNamingStrategy.LowerCaseStrategy.class,
            PropertyNamingStrategy.KebabCaseStrategy.class,
            PropertyNamingStrategy.LowerDotCaseStrategy.class
         )
         .forEach(xva$0 -> RuntimeReflection.registerForReflectiveInstantiation(new Class[]{xva$0}));
   }
}

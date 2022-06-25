package io.micronaut.http.netty.channel.converters;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.reflect.GenericTypeUtils;
import io.micronaut.core.reflect.ReflectionUtils;
import io.netty.channel.ChannelOption;
import jakarta.inject.Singleton;
import java.lang.reflect.Field;
import java.util.Optional;

@Internal
@Requires(
   missingBeans = {EpollChannelOptionFactory.class, KQueueChannelOptionFactory.class}
)
@Singleton
@TypeHint(
   value = {ChannelOption.class},
   accessType = {TypeHint.AccessType.ALL_DECLARED_FIELDS}
)
public class DefaultChannelOptionFactory implements ChannelOptionFactory {
   private static Object processChannelOptionValue(Class<? extends ChannelOption> cls, String name, Object value, Environment env) {
      Optional<Field> declaredField = ReflectionUtils.findField(cls, name);
      if (declaredField.isPresent()) {
         Optional<Class> typeArg = GenericTypeUtils.resolveGenericTypeArgument((Field)declaredField.get());
         if (typeArg.isPresent()) {
            Optional<Object> converted = env.convert(value, (Class<Object>)typeArg.get());
            value = converted.orElse(value);
         }
      }

      return value;
   }

   static Object convertValue(ChannelOption<?> option, Class<? extends ChannelOption> cls, Object value, Environment env) {
      String name = option.name();
      if (!ChannelOption.exists(name)) {
         return value;
      } else {
         int idx = name.lastIndexOf(35);
         String optionName;
         if (idx > 0 && idx < name.length() - 1) {
            optionName = name.substring(idx);
         } else {
            optionName = name;
         }

         return processChannelOptionValue(cls, optionName, value, env);
      }
   }

   static ChannelOption<?> channelOption(String name, Class<?>... classes) {
      for(Class<?> cls : classes) {
         String composedName = cls.getName() + '#' + name;
         if (ChannelOption.exists(composedName)) {
            return ChannelOption.valueOf(composedName);
         }
      }

      return ChannelOption.valueOf(name);
   }
}

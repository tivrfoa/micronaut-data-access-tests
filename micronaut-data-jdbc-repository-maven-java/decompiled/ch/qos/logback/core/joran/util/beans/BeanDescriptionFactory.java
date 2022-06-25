package ch.qos.logback.core.joran.util.beans;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class BeanDescriptionFactory extends ContextAwareBase {
   BeanDescriptionFactory(Context context) {
      this.setContext(context);
   }

   public BeanDescription create(Class<?> clazz) {
      Map<String, Method> propertyNameToGetter = new HashMap();
      Map<String, Method> propertyNameToSetter = new HashMap();
      Map<String, Method> propertyNameToAdder = new HashMap();
      Method[] methods = clazz.getMethods();

      for(Method method : methods) {
         if (!method.isBridge()) {
            if (BeanUtil.isGetter(method)) {
               String propertyName = BeanUtil.getPropertyName(method);
               Method oldGetter = (Method)propertyNameToGetter.put(propertyName, method);
               if (oldGetter != null) {
                  if (oldGetter.getName().startsWith("is")) {
                     propertyNameToGetter.put(propertyName, oldGetter);
                  }

                  String message = String.format("Class '%s' contains multiple getters for the same property '%s'.", clazz.getCanonicalName(), propertyName);
                  this.addWarn(message);
               }
            } else if (BeanUtil.isSetter(method)) {
               String propertyName = BeanUtil.getPropertyName(method);
               Method oldSetter = (Method)propertyNameToSetter.put(propertyName, method);
               if (oldSetter != null) {
                  String message = String.format("Class '%s' contains multiple setters for the same property '%s'.", clazz.getCanonicalName(), propertyName);
                  this.addWarn(message);
               }
            } else if (BeanUtil.isAdder(method)) {
               String propertyName = BeanUtil.getPropertyName(method);
               Method oldAdder = (Method)propertyNameToAdder.put(propertyName, method);
               if (oldAdder != null) {
                  String message = String.format("Class '%s' contains multiple adders for the same property '%s'.", clazz.getCanonicalName(), propertyName);
                  this.addWarn(message);
               }
            }
         }
      }

      return new BeanDescription(clazz, propertyNameToGetter, propertyNameToSetter, propertyNameToAdder);
   }
}

package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Objects;

final class MethodGenericTypeResolver {
   public static TypeResolutionContext narrowMethodTypeParameters(
      Method candidate, JavaType requestedType, TypeFactory typeFactory, TypeResolutionContext emptyTypeResCtxt
   ) {
      TypeBindings newTypeBindings = bindMethodTypeParameters(candidate, requestedType, emptyTypeResCtxt);
      return (TypeResolutionContext)(newTypeBindings == null ? emptyTypeResCtxt : new TypeResolutionContext.Basic(typeFactory, newTypeBindings));
   }

   static TypeBindings bindMethodTypeParameters(Method candidate, JavaType requestedType, TypeResolutionContext emptyTypeResCtxt) {
      TypeVariable<Method>[] methodTypeParameters = candidate.getTypeParameters();
      if (methodTypeParameters.length != 0 && !requestedType.getBindings().isEmpty()) {
         Type genericReturnType = candidate.getGenericReturnType();
         if (!(genericReturnType instanceof ParameterizedType)) {
            return null;
         } else {
            ParameterizedType parameterizedGenericReturnType = (ParameterizedType)genericReturnType;
            if (!Objects.equals(requestedType.getRawClass(), parameterizedGenericReturnType.getRawType())) {
               return null;
            } else {
               Type[] methodReturnTypeArguments = parameterizedGenericReturnType.getActualTypeArguments();
               ArrayList<String> names = new ArrayList(methodTypeParameters.length);
               ArrayList<JavaType> types = new ArrayList(methodTypeParameters.length);

               for(int i = 0; i < methodReturnTypeArguments.length; ++i) {
                  Type methodReturnTypeArgument = methodReturnTypeArguments[i];
                  TypeVariable<?> typeVar = maybeGetTypeVariable(methodReturnTypeArgument);
                  if (typeVar != null) {
                     String typeParameterName = typeVar.getName();
                     if (typeParameterName == null) {
                        return null;
                     }

                     JavaType bindTarget = requestedType.getBindings().getBoundType(i);
                     if (bindTarget == null) {
                        return null;
                     }

                     TypeVariable<?> methodTypeVariable = findByName(methodTypeParameters, typeParameterName);
                     if (methodTypeVariable == null) {
                        return null;
                     }

                     if (pessimisticallyValidateBounds(emptyTypeResCtxt, bindTarget, methodTypeVariable.getBounds())) {
                        int existingIndex = names.indexOf(typeParameterName);
                        if (existingIndex != -1) {
                           JavaType existingBindTarget = (JavaType)types.get(existingIndex);
                           if (!bindTarget.equals(existingBindTarget)) {
                              boolean existingIsSubtype = existingBindTarget.isTypeOrSubTypeOf(bindTarget.getRawClass());
                              boolean newIsSubtype = bindTarget.isTypeOrSubTypeOf(existingBindTarget.getRawClass());
                              if (!existingIsSubtype && !newIsSubtype) {
                                 return null;
                              }

                              if (existingIsSubtype ^ newIsSubtype && newIsSubtype) {
                                 types.set(existingIndex, bindTarget);
                              }
                           }
                        } else {
                           names.add(typeParameterName);
                           types.add(bindTarget);
                        }
                     }
                  }
               }

               return names.isEmpty() ? null : TypeBindings.create(names, types);
            }
         }
      } else {
         return null;
      }
   }

   private static TypeVariable<?> maybeGetTypeVariable(Type type) {
      if (type instanceof TypeVariable) {
         return (TypeVariable<?>)type;
      } else {
         if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType)type;
            if (wildcardType.getLowerBounds().length != 0) {
               return null;
            }

            Type[] upperBounds = wildcardType.getUpperBounds();
            if (upperBounds.length == 1) {
               return maybeGetTypeVariable(upperBounds[0]);
            }
         }

         return null;
      }
   }

   private static ParameterizedType maybeGetParameterizedType(Type type) {
      if (type instanceof ParameterizedType) {
         return (ParameterizedType)type;
      } else {
         if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType)type;
            if (wildcardType.getLowerBounds().length != 0) {
               return null;
            }

            Type[] upperBounds = wildcardType.getUpperBounds();
            if (upperBounds.length == 1) {
               return maybeGetParameterizedType(upperBounds[0]);
            }
         }

         return null;
      }
   }

   private static boolean pessimisticallyValidateBounds(TypeResolutionContext context, JavaType boundType, Type[] upperBound) {
      for(Type type : upperBound) {
         if (!pessimisticallyValidateBound(context, boundType, type)) {
            return false;
         }
      }

      return true;
   }

   private static boolean pessimisticallyValidateBound(TypeResolutionContext context, JavaType boundType, Type type) {
      if (!boundType.isTypeOrSubTypeOf(context.resolveType(type).getRawClass())) {
         return false;
      } else {
         ParameterizedType parameterized = maybeGetParameterizedType(type);
         if (parameterized != null && Objects.equals(boundType.getRawClass(), parameterized.getRawType())) {
            Type[] typeArguments = parameterized.getActualTypeArguments();
            TypeBindings bindings = boundType.getBindings();
            if (bindings.size() != typeArguments.length) {
               return false;
            }

            for(int i = 0; i < bindings.size(); ++i) {
               JavaType boundTypeBound = bindings.getBoundType(i);
               Type typeArg = typeArguments[i];
               if (!pessimisticallyValidateBound(context, boundTypeBound, typeArg)) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   private static TypeVariable<?> findByName(TypeVariable<?>[] typeVariables, String name) {
      if (typeVariables != null && name != null) {
         for(TypeVariable<?> typeVariable : typeVariables) {
            if (name.equals(typeVariable.getName())) {
               return typeVariable;
            }
         }

         return null;
      } else {
         return null;
      }
   }
}

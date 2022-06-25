package io.micronaut.context;

import io.micronaut.context.annotation.InjectScope;
import io.micronaut.context.env.CachedEnvironment;
import io.micronaut.context.exceptions.CircularDependencyException;
import io.micronaut.context.scope.CustomScope;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.naming.Named;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.ArgumentCoercible;
import io.micronaut.inject.ArgumentInjectionPoint;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.CallableInjectionPoint;
import io.micronaut.inject.ConstructorInjectionPoint;
import io.micronaut.inject.FieldInjectionPoint;
import io.micronaut.inject.InjectionPoint;
import io.micronaut.inject.MethodInjectionPoint;
import io.micronaut.inject.ProxyBeanDefinition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Internal
public abstract class AbstractBeanResolutionContext implements BeanResolutionContext {
   protected final DefaultBeanContext context;
   protected final BeanDefinition<?> rootDefinition;
   protected final BeanResolutionContext.Path path;
   private Map<CharSequence, Object> attributes;
   private Qualifier<?> qualifier;
   private List<BeanRegistration<?>> dependentBeans;
   private BeanRegistration<?> dependentFactory;

   @Internal
   protected AbstractBeanResolutionContext(DefaultBeanContext context, BeanDefinition<?> rootDefinition) {
      this.context = context;
      this.rootDefinition = rootDefinition;
      this.path = new AbstractBeanResolutionContext.DefaultPath();
   }

   @NonNull
   @Override
   public <T> T getBean(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.context.getBean(this, beanType, qualifier);
   }

   @NonNull
   @Override
   public <T> Collection<T> getBeansOfType(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.context.getBeansOfType(this, beanType, qualifier);
   }

   @NonNull
   @Override
   public <T> Stream<T> streamOfType(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.context.streamOfType(this, beanType, qualifier);
   }

   @NonNull
   @Override
   public <T> Optional<T> findBean(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.context.findBean(this, beanType, qualifier);
   }

   @NonNull
   @Override
   public <T> T inject(@Nullable BeanDefinition<?> beanDefinition, @NonNull T instance) {
      return this.context.inject(this, beanDefinition, instance);
   }

   @NonNull
   @Override
   public <T> Collection<BeanRegistration<T>> getBeanRegistrations(@NonNull Argument<T> beanType, @Nullable Qualifier<T> qualifier) {
      return this.context.getBeanRegistrations(this, beanType, qualifier);
   }

   public void copyStateFrom(@NonNull AbstractBeanResolutionContext context) {
      this.path.addAll(context.path);
      this.qualifier = context.qualifier;
      if (context.attributes != null) {
         this.getAttributesOrCreate().putAll(context.attributes);
      }

   }

   @Override
   public <T> void addDependentBean(BeanRegistration<T> beanRegistration) {
      if (beanRegistration.getBeanDefinition() != this.rootDefinition) {
         if (this.dependentBeans == null) {
            this.dependentBeans = new ArrayList(3);
         }

         this.dependentBeans.add(beanRegistration);
      }
   }

   @Override
   public void destroyInjectScopedBeans() {
      CustomScope<?> injectScope = (CustomScope)this.context.getCustomScopeRegistry().findScope(InjectScope.class.getName()).orElse(null);
      if (injectScope instanceof LifeCycle) {
         ((LifeCycle)injectScope).stop();
      }

   }

   @NonNull
   @Override
   public List<BeanRegistration<?>> getAndResetDependentBeans() {
      if (this.dependentBeans == null) {
         return Collections.emptyList();
      } else {
         List<BeanRegistration<?>> registrations = Collections.unmodifiableList(this.dependentBeans);
         this.dependentBeans = null;
         return registrations;
      }
   }

   @Override
   public void markDependentAsFactory() {
      if (this.dependentBeans != null) {
         if (this.dependentBeans.isEmpty()) {
            return;
         }

         if (this.dependentBeans.size() != 1) {
            throw new IllegalStateException("Expected only one bean dependent!");
         }

         this.dependentFactory = (BeanRegistration)this.dependentBeans.remove(0);
      }

   }

   @Override
   public BeanRegistration<?> getAndResetDependentFactoryBean() {
      BeanRegistration<?> result = this.dependentFactory;
      this.dependentFactory = null;
      return result;
   }

   @Override
   public List<BeanRegistration<?>> popDependentBeans() {
      List<BeanRegistration<?>> result = this.dependentBeans;
      this.dependentBeans = null;
      return result;
   }

   @Override
   public void pushDependentBeans(List<BeanRegistration<?>> dependentBeans) {
      if (this.dependentBeans != null && !this.dependentBeans.isEmpty()) {
         throw new IllegalStateException("Found existing dependent beans!");
      } else {
         this.dependentBeans = dependentBeans;
      }
   }

   @Override
   public final BeanContext getContext() {
      return this.context;
   }

   @Override
   public final BeanDefinition getRootDefinition() {
      return this.rootDefinition;
   }

   @Override
   public final BeanResolutionContext.Path getPath() {
      return this.path;
   }

   @Override
   public final Object setAttribute(CharSequence key, Object value) {
      return this.getAttributesOrCreate().put(key, value);
   }

   @Override
   public final Object getAttribute(CharSequence key) {
      return this.attributes == null ? null : this.attributes.get(key);
   }

   @Override
   public final Object removeAttribute(CharSequence key) {
      return this.attributes != null && key != null ? this.attributes.remove(key) : null;
   }

   @Nullable
   @Override
   public Qualifier<?> getCurrentQualifier() {
      return this.qualifier;
   }

   @Override
   public void setCurrentQualifier(@Nullable Qualifier<?> qualifier) {
      this.qualifier = qualifier;
   }

   @Override
   public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
      if (this.attributes == null) {
         return Optional.empty();
      } else {
         Object value = this.attributes.get(name);
         return value != null && conversionContext.getArgument().getType().isInstance(value) ? Optional.of(value) : Optional.empty();
      }
   }

   @Override
   public <T> Optional<T> get(CharSequence name, Class<T> requiredType) {
      if (this.attributes == null) {
         return Optional.empty();
      } else {
         Object value = this.attributes.get(name);
         return requiredType.isInstance(value) ? Optional.of(value) : Optional.empty();
      }
   }

   protected void onNewSegment(BeanResolutionContext.Segment<?> segment) {
   }

   @NonNull
   private Map<CharSequence, Object> getAttributesOrCreate() {
      if (this.attributes == null) {
         this.attributes = new LinkedHashMap(2);
      }

      return this.attributes;
   }

   abstract static class AbstractSegment implements BeanResolutionContext.Segment, Named {
      private final BeanDefinition declaringComponent;
      private final String name;
      private final Argument argument;

      AbstractSegment(BeanDefinition declaringClass, String name, Argument argument) {
         this.declaringComponent = declaringClass;
         this.name = name;
         this.argument = argument;
      }

      @Override
      public String getName() {
         return this.name;
      }

      @Override
      public BeanDefinition getDeclaringType() {
         return this.declaringComponent;
      }

      @Override
      public Argument getArgument() {
         return this.argument;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            AbstractBeanResolutionContext.AbstractSegment that = (AbstractBeanResolutionContext.AbstractSegment)o;
            return this.declaringComponent.equals(that.declaringComponent) && this.name.equals(that.name) && this.argument.equals(that.argument);
         } else {
            return false;
         }
      }

      public int hashCode() {
         int result = this.declaringComponent.hashCode();
         result = 31 * result + this.name.hashCode();
         return 31 * result + this.argument.hashCode();
      }

      void outputArguments(StringBuilder baseString, Argument[] arguments) {
         baseString.append('(');

         for(int i = 0; i < arguments.length; ++i) {
            Argument argument = arguments[i];
            boolean isInjectedArgument = argument.equals(this.getArgument());
            if (isInjectedArgument) {
               baseString.append('[');
            }

            baseString.append(argument);
            if (isInjectedArgument) {
               baseString.append(']');
            }

            if (i != arguments.length - 1) {
               baseString.append(',');
            }
         }

         baseString.append(')');
      }
   }

   public static final class AnnotationSegment extends AbstractBeanResolutionContext.AbstractSegment implements InjectionPoint {
      AnnotationSegment(BeanDefinition beanDefinition, Argument argument) {
         super(beanDefinition, argument.getName(), argument);
      }

      public String toString() {
         return this.getName();
      }

      @Override
      public InjectionPoint getInjectionPoint() {
         return this;
      }

      @Override
      public BeanDefinition getDeclaringBean() {
         return this.getDeclaringType();
      }

      @Override
      public boolean requiresReflection() {
         return false;
      }
   }

   public static class ConstructorArgumentSegment extends AbstractBeanResolutionContext.ConstructorSegment implements ArgumentInjectionPoint {
      public ConstructorArgumentSegment(BeanDefinition declaringType, String methodName, Argument argument, Argument[] arguments) {
         super(declaringType, methodName, argument, arguments);
      }

      @Override
      public CallableInjectionPoint getOuterInjectionPoint() {
         throw new UnsupportedOperationException("Outer injection point inaccessible from here");
      }

      @Override
      public BeanDefinition getDeclaringBean() {
         return this.getDeclaringType();
      }

      @Override
      public boolean requiresReflection() {
         return false;
      }
   }

   public static class ConstructorSegment extends AbstractBeanResolutionContext.AbstractSegment {
      private final String methodName;
      private final Argument[] arguments;
      private final BeanDefinition declaringClass;

      ConstructorSegment(BeanDefinition declaringClass, String methodName, Argument argument, Argument[] arguments) {
         super(declaringClass, declaringClass.getBeanType().getName(), argument);
         this.methodName = methodName;
         this.arguments = arguments;
         this.declaringClass = declaringClass;
      }

      public String toString() {
         StringBuilder baseString;
         if ("<init>".equals(this.methodName)) {
            baseString = new StringBuilder("new ");
            baseString.append(this.getDeclaringType().getBeanType().getSimpleName());
         } else {
            baseString = new StringBuilder(this.getDeclaringType().getBeanType().getSimpleName()).append('.');
            baseString.append(this.methodName);
         }

         this.outputArguments(baseString, this.arguments);
         return baseString.toString();
      }

      @Override
      public InjectionPoint getInjectionPoint() {
         final ConstructorInjectionPoint constructorInjectionPoint = this.getDeclaringType().getConstructor();
         return new ArgumentInjectionPoint() {
            @NonNull
            @Override
            public CallableInjectionPoint getOuterInjectionPoint() {
               return constructorInjectionPoint;
            }

            @NonNull
            @Override
            public Argument getArgument() {
               return ConstructorSegment.this.getArgument();
            }

            @Override
            public BeanDefinition getDeclaringBean() {
               return constructorInjectionPoint.getDeclaringBean();
            }

            @Override
            public boolean requiresReflection() {
               return constructorInjectionPoint.requiresReflection();
            }

            @Override
            public AnnotationMetadata getAnnotationMetadata() {
               return this.getArgument().getAnnotationMetadata();
            }
         };
      }
   }

   class DefaultPath extends LinkedList<BeanResolutionContext.Segment<?>> implements BeanResolutionContext.Path {
      public static final String RIGHT_ARROW = " --> ";
      private static final String CIRCULAR_ERROR_MSG = "Circular dependency detected";

      public String toString() {
         Iterator<BeanResolutionContext.Segment<?>> i = this.descendingIterator();
         StringBuilder pathString = new StringBuilder();

         while(i.hasNext()) {
            pathString.append(((BeanResolutionContext.Segment)i.next()).toString());
            if (i.hasNext()) {
               pathString.append(" --> ");
            }
         }

         return pathString.toString();
      }

      @Override
      public String toCircularString() {
         Iterator<BeanResolutionContext.Segment<?>> i = this.descendingIterator();
         StringBuilder pathString = new StringBuilder();
         String ls = CachedEnvironment.getProperty("line.separator");

         while(i.hasNext()) {
            String segmentString = ((BeanResolutionContext.Segment)i.next()).toString();
            pathString.append(segmentString);
            if (i.hasNext()) {
               pathString.append(" --> ");
            } else {
               int totalLength = pathString.length() - 3;
               String spaces = String.join("", Collections.nCopies(totalLength, " "));
               pathString.append(ls)
                  .append("^")
                  .append(spaces)
                  .append("|")
                  .append(ls)
                  .append("|")
                  .append(spaces)
                  .append("|")
                  .append(ls)
                  .append("|")
                  .append(spaces)
                  .append("|")
                  .append(ls)
                  .append('+');
               pathString.append(String.join("", Collections.nCopies(totalLength, "-"))).append('+');
            }
         }

         return pathString.toString();
      }

      @Override
      public Optional<BeanResolutionContext.Segment<?>> currentSegment() {
         return Optional.ofNullable(this.peek());
      }

      @Override
      public BeanResolutionContext.Path pushConstructorResolve(BeanDefinition declaringType, Argument argument) {
         ConstructorInjectionPoint constructor = declaringType.getConstructor();
         if (constructor instanceof MethodInjectionPoint) {
            MethodInjectionPoint<?, ?> methodInjectionPoint = (MethodInjectionPoint)constructor;
            return this.pushConstructorResolve(
               declaringType, methodInjectionPoint.getName(), argument, constructor.getArguments(), constructor.requiresReflection()
            );
         } else {
            return this.pushConstructorResolve(declaringType, "<init>", argument, constructor.getArguments(), constructor.requiresReflection());
         }
      }

      @Override
      public BeanResolutionContext.Path pushConstructorResolve(
         BeanDefinition declaringType, String methodName, Argument argument, Argument[] arguments, boolean requiresReflection
      ) {
         if ("<init>".equals(methodName)) {
            AbstractBeanResolutionContext.ConstructorSegment constructorSegment = new AbstractBeanResolutionContext.ConstructorArgumentSegment(
               declaringType, methodName, argument, arguments
            );
            this.detectCircularDependency(declaringType, argument, constructorSegment);
         } else {
            BeanResolutionContext.Segment<?> previous = (BeanResolutionContext.Segment)this.peek();
            AbstractBeanResolutionContext.MethodSegment methodSegment = new AbstractBeanResolutionContext.MethodArgumentSegment(
               declaringType,
               methodName,
               argument,
               arguments,
               requiresReflection,
               previous instanceof AbstractBeanResolutionContext.MethodSegment ? (AbstractBeanResolutionContext.MethodSegment)previous : null
            );
            if (this.contains(methodSegment)) {
               throw new CircularDependencyException(AbstractBeanResolutionContext.this, argument, "Circular dependency detected");
            }

            this.push(methodSegment);
         }

         return this;
      }

      @Override
      public BeanResolutionContext.Path pushBeanCreate(BeanDefinition<?> declaringType, Argument<?> beanType) {
         return this.pushConstructorResolve(declaringType, beanType);
      }

      @Override
      public BeanResolutionContext.Path pushMethodArgumentResolve(BeanDefinition declaringType, MethodInjectionPoint methodInjectionPoint, Argument argument) {
         BeanResolutionContext.Segment<?> previous = (BeanResolutionContext.Segment)this.peek();
         AbstractBeanResolutionContext.MethodSegment methodSegment = new AbstractBeanResolutionContext.MethodArgumentSegment(
            declaringType,
            methodInjectionPoint.getName(),
            argument,
            methodInjectionPoint.getArguments(),
            methodInjectionPoint.requiresReflection(),
            previous instanceof AbstractBeanResolutionContext.MethodSegment ? (AbstractBeanResolutionContext.MethodSegment)previous : null
         );
         if (this.contains(methodSegment)) {
            throw new CircularDependencyException(AbstractBeanResolutionContext.this, methodInjectionPoint, argument, "Circular dependency detected");
         } else {
            this.push(methodSegment);
            return this;
         }
      }

      @Override
      public BeanResolutionContext.Path pushMethodArgumentResolve(
         BeanDefinition declaringType, String methodName, Argument argument, Argument[] arguments, boolean requiresReflection
      ) {
         BeanResolutionContext.Segment<?> previous = (BeanResolutionContext.Segment)this.peek();
         AbstractBeanResolutionContext.MethodSegment methodSegment = new AbstractBeanResolutionContext.MethodArgumentSegment(
            declaringType,
            methodName,
            argument,
            arguments,
            requiresReflection,
            previous instanceof AbstractBeanResolutionContext.MethodSegment ? (AbstractBeanResolutionContext.MethodSegment)previous : null
         );
         if (this.contains(methodSegment)) {
            throw new CircularDependencyException(AbstractBeanResolutionContext.this, declaringType, methodName, argument, "Circular dependency detected");
         } else {
            this.push(methodSegment);
            return this;
         }
      }

      @Override
      public BeanResolutionContext.Path pushFieldResolve(BeanDefinition declaringType, FieldInjectionPoint fieldInjectionPoint) {
         AbstractBeanResolutionContext.FieldSegment fieldSegment = new AbstractBeanResolutionContext.FieldSegment(
            declaringType, fieldInjectionPoint.asArgument(), fieldInjectionPoint.requiresReflection()
         );
         if (this.contains(fieldSegment)) {
            throw new CircularDependencyException(AbstractBeanResolutionContext.this, fieldInjectionPoint, "Circular dependency detected");
         } else {
            this.push(fieldSegment);
            return this;
         }
      }

      @Override
      public BeanResolutionContext.Path pushFieldResolve(BeanDefinition declaringType, Argument fieldAsArgument, boolean requiresReflection) {
         AbstractBeanResolutionContext.FieldSegment fieldSegment = new AbstractBeanResolutionContext.FieldSegment(
            declaringType, fieldAsArgument, requiresReflection
         );
         if (this.contains(fieldSegment)) {
            throw new CircularDependencyException(AbstractBeanResolutionContext.this, declaringType, fieldAsArgument.getName(), "Circular dependency detected");
         } else {
            this.push(fieldSegment);
            return this;
         }
      }

      @Override
      public BeanResolutionContext.Path pushAnnotationResolve(BeanDefinition beanDefinition, Argument annotationMemberBeanAsArgument) {
         AbstractBeanResolutionContext.AnnotationSegment annotationSegment = new AbstractBeanResolutionContext.AnnotationSegment(
            beanDefinition, annotationMemberBeanAsArgument
         );
         if (this.contains(annotationSegment)) {
            throw new CircularDependencyException(
               AbstractBeanResolutionContext.this, beanDefinition, annotationMemberBeanAsArgument.getName(), "Circular dependency detected"
            );
         } else {
            this.push(annotationSegment);
            return this;
         }
      }

      private void detectCircularDependency(BeanDefinition declaringType, Argument argument, BeanResolutionContext.Segment constructorSegment) {
         if (this.contains(constructorSegment)) {
            BeanResolutionContext.Segment last = (BeanResolutionContext.Segment)this.peek();
            if (last == null) {
               throw new CircularDependencyException(AbstractBeanResolutionContext.this, argument, "Circular dependency detected");
            }

            BeanDefinition declaringBean = last.getDeclaringType();
            if (!declaringBean.equals(declaringType)) {
               if (declaringType instanceof ProxyBeanDefinition) {
                  if (!((ProxyBeanDefinition)declaringType).getTargetDefinitionType().equals(declaringBean.getClass())) {
                     throw new CircularDependencyException(AbstractBeanResolutionContext.this, argument, "Circular dependency detected");
                  }

                  this.push(constructorSegment);
               } else {
                  if (!(declaringBean instanceof ProxyBeanDefinition)) {
                     throw new CircularDependencyException(AbstractBeanResolutionContext.this, argument, "Circular dependency detected");
                  }

                  if (!((ProxyBeanDefinition)declaringBean).getTargetDefinitionType().equals(declaringType.getClass())) {
                     throw new CircularDependencyException(AbstractBeanResolutionContext.this, argument, "Circular dependency detected");
                  }

                  this.push(constructorSegment);
               }
            } else {
               this.push(constructorSegment);
            }
         } else {
            this.push(constructorSegment);
         }

      }

      public void push(BeanResolutionContext.Segment<?> segment) {
         super.push(segment);
         AbstractBeanResolutionContext.this.onNewSegment(segment);
      }
   }

   public static class FieldSegment extends AbstractBeanResolutionContext.AbstractSegment implements InjectionPoint, ArgumentCoercible, ArgumentInjectionPoint {
      private final boolean requiresReflection;

      FieldSegment(BeanDefinition declaringClass, Argument argument, boolean requiresReflection) {
         super(declaringClass, argument.getName(), argument);
         this.requiresReflection = requiresReflection;
      }

      public String toString() {
         return this.getDeclaringType().getBeanType().getSimpleName() + "." + this.getName();
      }

      @Override
      public InjectionPoint getInjectionPoint() {
         return this;
      }

      @Override
      public BeanDefinition getDeclaringBean() {
         return this.getDeclaringType();
      }

      @Override
      public boolean requiresReflection() {
         return this.requiresReflection;
      }

      @Override
      public CallableInjectionPoint getOuterInjectionPoint() {
         throw new UnsupportedOperationException("Outer injection point not retrievable from here");
      }

      @Override
      public Argument asArgument() {
         return this.getArgument();
      }

      @Override
      public AnnotationMetadata getAnnotationMetadata() {
         return this.getArgument().getAnnotationMetadata();
      }
   }

   public static class MethodArgumentSegment extends AbstractBeanResolutionContext.MethodSegment implements ArgumentInjectionPoint {
      private final AbstractBeanResolutionContext.MethodSegment outer;

      public MethodArgumentSegment(
         BeanDefinition declaringType,
         String methodName,
         Argument argument,
         Argument[] arguments,
         boolean requiresReflection,
         AbstractBeanResolutionContext.MethodSegment outer
      ) {
         super(declaringType, methodName, argument, arguments, requiresReflection);
         this.outer = outer;
      }

      @Override
      public CallableInjectionPoint getOuterInjectionPoint() {
         if (this.outer == null) {
            throw new IllegalStateException("Outer argument inaccessible");
         } else {
            return this.outer;
         }
      }
   }

   public static class MethodSegment extends AbstractBeanResolutionContext.AbstractSegment implements CallableInjectionPoint {
      private final Argument[] arguments;
      private final boolean requiresReflection;

      MethodSegment(BeanDefinition declaringType, String methodName, Argument argument, Argument[] arguments, boolean requiresReflection) {
         super(declaringType, methodName, argument);
         this.arguments = arguments;
         this.requiresReflection = requiresReflection;
      }

      public String toString() {
         StringBuilder baseString = new StringBuilder(this.getDeclaringType().getBeanType().getSimpleName()).append('.');
         baseString.append(this.getName());
         this.outputArguments(baseString, this.arguments);
         return baseString.toString();
      }

      @Override
      public InjectionPoint getInjectionPoint() {
         return this;
      }

      @Override
      public BeanDefinition getDeclaringBean() {
         return this.getDeclaringType();
      }

      @Override
      public boolean requiresReflection() {
         return this.requiresReflection;
      }

      @Override
      public Argument<?>[] getArguments() {
         return this.arguments;
      }

      @Override
      public AnnotationMetadata getAnnotationMetadata() {
         return this.getArgument().getAnnotationMetadata();
      }
   }
}

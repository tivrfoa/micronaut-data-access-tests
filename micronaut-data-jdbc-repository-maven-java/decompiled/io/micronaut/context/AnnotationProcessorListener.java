package io.micronaut.context;

import io.micronaut.context.annotation.Parallel;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.context.exceptions.BeanContextException;
import io.micronaut.context.processor.AnnotationProcessor;
import io.micronaut.context.processor.BeanDefinitionProcessor;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AnnotationProcessorListener implements BeanCreatedEventListener<AnnotationProcessor> {
   private static final Logger LOG = LoggerFactory.getLogger(DefaultBeanContext.class);

   public AnnotationProcessor onCreated(BeanCreatedEvent<AnnotationProcessor> event) {
      AnnotationProcessor processor = event.getBean();
      BeanDefinition<AnnotationProcessor> processorDefinition = event.getBeanDefinition();
      BeanContext beanContext = event.getSource();
      if (processor instanceof LifeCycle) {
         try {
            ((LifeCycle)processor).start();
         } catch (Exception var18) {
            throw new BeanContextException("Error starting bean processing: " + var18.getMessage(), var18);
         }
      }

      if (processor instanceof ExecutableMethodProcessor) {
         List<Argument<?>> typeArguments = processorDefinition.getTypeArguments(ExecutableMethodProcessor.class);
         if (!typeArguments.isEmpty()) {
            Argument<?> firstArgument = (Argument)typeArguments.get(0);
            Collection<BeanDefinition<?>> beanDefinitions = beanContext.getBeanDefinitions(Qualifiers.byStereotype(firstArgument.getType()));
            boolean isParallel = firstArgument.isAnnotationPresent(Parallel.class);
            if (isParallel) {
               for(BeanDefinition<?> beanDefinition : beanDefinitions) {
                  for(ExecutableMethod<?, ?> executableMethod : beanDefinition.getExecutableMethods()) {
                     ForkJoinPool.commonPool()
                        .execute(
                           () -> {
                              try {
                                 if (beanContext.isRunning()) {
                                    processor.process(beanDefinition, executableMethod);
                                 }
                              } catch (Throwable var6x) {
                                 if (LOG.isErrorEnabled()) {
                                    LOG.error(
                                       "Error processing bean method "
                                          + beanDefinition
                                          + "."
                                          + executableMethod
                                          + " with processor ("
                                          + processor
                                          + "): "
                                          + var6x.getMessage(),
                                       var6x
                                    );
                                 }
      
                                 Boolean shutdownOnError = (Boolean)executableMethod.getAnnotationMetadata()
                                    .booleanValue(Parallel.class, "shutdownOnError")
                                    .orElse(true);
                                 if (shutdownOnError) {
                                    beanContext.stop();
                                 }
                              }
      
                           }
                        );
                  }
               }
            } else {
               for(BeanDefinition<?> beanDefinition : beanDefinitions) {
                  for(ExecutableMethod<?, ?> executableMethod : beanDefinition.getExecutableMethods()) {
                     try {
                        processor.process(beanDefinition, executableMethod);
                     } catch (Exception var17) {
                        throw new BeanContextException(
                           "Error processing bean [" + beanDefinition + "] method definition [" + executableMethod + "]: " + var17.getMessage(), var17
                        );
                     }
                  }
               }
            }
         }
      } else if (processor instanceof BeanDefinitionProcessor) {
         BeanDefinitionProcessor beanDefinitionProcessor = (BeanDefinitionProcessor)processor;
         List<Argument<?>> typeArguments = processorDefinition.getTypeArguments(BeanDefinitionProcessor.class);
         if (typeArguments.size() == 1) {
            Argument<?> annotation = (Argument)typeArguments.get(0);

            for(BeanDefinition<?> beanDefinition : beanContext.getBeanDefinitions(Qualifiers.byStereotype(annotation.getType()))) {
               try {
                  beanDefinitionProcessor.process(beanDefinition, beanContext);
               } catch (Exception var16) {
                  throw new BeanContextException("Error processing bean definition [" + beanDefinition + "]: " + var16.getMessage(), var16);
               }
            }
         }
      }

      if (processor instanceof LifeCycle) {
         try {
            ((LifeCycle)processor).stop();
         } catch (Exception var15) {
            throw new BeanContextException("Error finalizing bean processing: " + var15.getMessage(), var15);
         }
      }

      return processor;
   }
}

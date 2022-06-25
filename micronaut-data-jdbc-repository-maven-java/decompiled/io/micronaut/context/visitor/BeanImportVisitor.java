package io.micronaut.context.visitor;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Import;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.order.OrderUtil;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.beans.BeanElementBuilder;
import io.micronaut.inject.visitor.TypeElementVisitor;
import io.micronaut.inject.visitor.VisitorContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

public class BeanImportVisitor implements TypeElementVisitor<Import, Object> {
   private static final List<BeanImportHandler> BEAN_IMPORT_HANDLERS;

   @Override
   public void visitClass(ClassElement element, VisitorContext context) {
      for(ClassElement beanElement : collectInjectableElements(element, context)) {
         BeanElementBuilder beanElementBuilder = element.addAssociatedBean(beanElement).inject();

         for(BeanImportHandler beanImportHandler : BEAN_IMPORT_HANDLERS) {
            beanImportHandler.beanAdded(beanElementBuilder, context);
         }
      }

   }

   @NonNull
   public static List<ClassElement> collectInjectableElements(ClassElement element, VisitorContext context) {
      List<ClassElement> beanElements = new ArrayList();
      String[] classNames = element.getAnnotationMetadata().stringValues(Import.class, "classes");
      if (ArrayUtils.isNotEmpty(classNames)) {
         for(String className : classNames) {
            context.getClassElement(className).ifPresent(beanElements::add);
         }
      }

      String[] annotations = element.getAnnotationMetadata().stringValues(Import.class, "annotated");
      Set<String> annotationSet;
      if (ArrayUtils.isEmpty(annotations)) {
         annotationSet = CollectionUtils.setOf("javax.inject.Scope", Bean.class.getName(), "javax.inject.Qualifier");
      } else {
         annotationSet = new HashSet(Arrays.asList(annotations));
      }

      if (!annotationSet.contains("*")) {
         for(BeanImportHandler beanImportHandler : BEAN_IMPORT_HANDLERS) {
            annotationSet.addAll(beanImportHandler.getSupportedAnnotationNames());
         }
      }

      String[] packages = element.getAnnotationMetadata().stringValues(Import.class, "packages");
      if (ArrayUtils.isNotEmpty(packages)) {
         for(String aPackage : packages) {
            ClassElement[] classElements = context.getClassElements(aPackage, (String[])annotationSet.toArray(new String[0]));

            for(ClassElement classElement : classElements) {
               if (!classElement.isAbstract()) {
                  beanElements.add(classElement);
               }
            }
         }
      }

      return beanElements;
   }

   @Override
   public Set<String> getSupportedAnnotationNames() {
      return Collections.singleton(Import.class.getName());
   }

   @NonNull
   @Override
   public TypeElementVisitor.VisitorKind getVisitorKind() {
      return TypeElementVisitor.VisitorKind.ISOLATING;
   }

   @Override
   public int getOrder() {
      return Integer.MIN_VALUE;
   }

   static {
      ServiceLoader<BeanImportHandler> handlers = ServiceLoader.load(BeanImportHandler.class);
      List<BeanImportHandler> beanImportHandlers = new ArrayList();

      for(BeanImportHandler handler : handlers) {
         beanImportHandlers.add(handler);
      }

      OrderUtil.sort(beanImportHandlers);
      BEAN_IMPORT_HANDLERS = Collections.unmodifiableList(beanImportHandlers);
   }
}

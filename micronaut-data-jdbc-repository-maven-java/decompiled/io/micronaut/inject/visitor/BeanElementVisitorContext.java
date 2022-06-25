package io.micronaut.inject.visitor;

import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.Element;
import io.micronaut.inject.ast.beans.BeanElementBuilder;

@Internal
public interface BeanElementVisitorContext extends VisitorContext {
   BeanElementBuilder addAssociatedBean(Element originatingElement, ClassElement type);
}

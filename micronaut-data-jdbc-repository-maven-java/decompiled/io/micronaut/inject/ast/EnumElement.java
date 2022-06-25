package io.micronaut.inject.ast;

import java.util.List;

public interface EnumElement extends ClassElement {
   List<String> values();
}

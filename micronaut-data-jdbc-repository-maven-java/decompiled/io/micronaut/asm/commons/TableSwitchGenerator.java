package io.micronaut.asm.commons;

import io.micronaut.asm.Label;

public interface TableSwitchGenerator {
   void generateCase(int var1, Label var2);

   void generateDefault();
}

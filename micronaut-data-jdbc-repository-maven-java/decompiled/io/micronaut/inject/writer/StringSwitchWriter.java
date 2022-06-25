package io.micronaut.inject.writer;

import io.micronaut.asm.Label;
import io.micronaut.asm.Type;
import io.micronaut.asm.commons.GeneratorAdapter;
import io.micronaut.asm.commons.Method;
import io.micronaut.asm.commons.TableSwitchGenerator;
import io.micronaut.core.annotation.Internal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Internal
public abstract class StringSwitchWriter {
   protected abstract Set<String> getKeys();

   protected abstract void pushStringValue();

   protected abstract void onMatch(String value, Label end);

   protected void generateDefault() {
   }

   public void write(GeneratorAdapter writer) {
      Set<String> keys = this.getKeys();
      if (!keys.isEmpty()) {
         if (keys.size() == 1) {
            Label end = new Label();
            String key = (String)keys.iterator().next();
            this.generateValueCase(writer, key, end);
            writer.visitLabel(end);
         } else {
            final Map<Integer, Set<String>> hashToValue = new HashMap();

            for(String string : keys) {
               ((Set)hashToValue.computeIfAbsent(string.hashCode(), hashCode -> new TreeSet())).add(string);
            }

            int[] hashCodeArray = hashToValue.keySet().stream().mapToInt(i -> i).toArray();
            Arrays.sort(hashCodeArray);
            this.pushStringValue();
            writer.invokeVirtual(Type.getType(Object.class), new Method("hashCode", Type.INT_TYPE, new Type[0]));
            writer.tableSwitch(hashCodeArray, new TableSwitchGenerator() {
               @Override
               public void generateCase(int hashCode, Label end) {
                  for(String string : (Set)hashToValue.get(hashCode)) {
                     StringSwitchWriter.this.generateValueCase(writer, string, end);
                  }

                  writer.goTo(end);
               }

               @Override
               public void generateDefault() {
                  StringSwitchWriter.this.generateDefault();
               }
            });
         }
      }
   }

   protected void generateValueCase(GeneratorAdapter writer, String string, Label end) {
      this.pushStringValue();
      writer.push(string);
      writer.invokeVirtual(Type.getType(Object.class), new Method("equals", Type.BOOLEAN_TYPE, new Type[]{Type.getType(Object.class)}));
      writer.push(true);
      Label falseLabel = new Label();
      writer.ifCmp(Type.BOOLEAN_TYPE, 154, falseLabel);
      this.onMatch(string, end);
      writer.visitLabel(falseLabel);
   }
}

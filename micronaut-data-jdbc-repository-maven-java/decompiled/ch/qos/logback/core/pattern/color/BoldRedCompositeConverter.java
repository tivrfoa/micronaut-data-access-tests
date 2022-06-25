package ch.qos.logback.core.pattern.color;

public class BoldRedCompositeConverter<E> extends ForegroundCompositeConverterBase<E> {
   @Override
   protected String getForegroundColorCode(E event) {
      return "1;31";
   }
}

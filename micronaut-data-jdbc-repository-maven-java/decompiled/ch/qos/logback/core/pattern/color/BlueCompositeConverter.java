package ch.qos.logback.core.pattern.color;

public class BlueCompositeConverter<E> extends ForegroundCompositeConverterBase<E> {
   @Override
   protected String getForegroundColorCode(E event) {
      return "34";
   }
}

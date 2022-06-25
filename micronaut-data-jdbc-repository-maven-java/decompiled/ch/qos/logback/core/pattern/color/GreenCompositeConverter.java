package ch.qos.logback.core.pattern.color;

public class GreenCompositeConverter<E> extends ForegroundCompositeConverterBase<E> {
   @Override
   protected String getForegroundColorCode(E event) {
      return "32";
   }
}

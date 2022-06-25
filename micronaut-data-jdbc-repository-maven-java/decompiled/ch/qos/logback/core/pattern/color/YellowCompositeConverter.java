package ch.qos.logback.core.pattern.color;

public class YellowCompositeConverter<E> extends ForegroundCompositeConverterBase<E> {
   @Override
   protected String getForegroundColorCode(E event) {
      return "33";
   }
}

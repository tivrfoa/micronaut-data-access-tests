package ch.qos.logback.core.pattern.color;

public class MagentaCompositeConverter<E> extends ForegroundCompositeConverterBase<E> {
   @Override
   protected String getForegroundColorCode(E event) {
      return "35";
   }
}

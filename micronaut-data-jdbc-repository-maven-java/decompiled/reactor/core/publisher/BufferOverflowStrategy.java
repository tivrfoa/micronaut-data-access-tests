package reactor.core.publisher;

public enum BufferOverflowStrategy {
   ERROR,
   DROP_LATEST,
   DROP_OLDEST;
}

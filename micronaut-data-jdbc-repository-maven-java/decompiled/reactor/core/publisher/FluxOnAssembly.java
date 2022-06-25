package reactor.core.publisher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxOnAssembly<T> extends InternalFluxOperator<T, T> implements Fuseable, AssemblyOp {
   final FluxOnAssembly.AssemblySnapshot snapshotStack;

   FluxOnAssembly(Flux<? extends T> source, FluxOnAssembly.AssemblySnapshot snapshotStack) {
      super(source);
      this.snapshotStack = snapshotStack;
   }

   @Override
   public String stepName() {
      return this.snapshotStack.operatorAssemblyInformation();
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.ACTUAL_METADATA) {
         return !this.snapshotStack.isCheckpoint;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
      }
   }

   @Override
   public String toString() {
      return this.snapshotStack.operatorAssemblyInformation();
   }

   static void fillStacktraceHeader(StringBuilder sb, Class<?> sourceClass, @Nullable String description) {
      sb.append("\nAssembly trace from producer [").append(sourceClass.getName()).append("]");
      if (description != null) {
         sb.append(", described as [").append(description).append("]");
      }

      sb.append(" :\n");
   }

   static <T> CoreSubscriber<? super T> wrapSubscriber(
      CoreSubscriber<? super T> actual, Flux<? extends T> source, Publisher<?> current, @Nullable FluxOnAssembly.AssemblySnapshot snapshotStack
   ) {
      if (snapshotStack != null) {
         if (actual instanceof Fuseable.ConditionalSubscriber) {
            Fuseable.ConditionalSubscriber<? super T> cs = (Fuseable.ConditionalSubscriber)actual;
            return new FluxOnAssembly.OnAssemblyConditionalSubscriber<>(cs, snapshotStack, source, current);
         } else {
            return new FluxOnAssembly.OnAssemblySubscriber<>(actual, snapshotStack, source, current);
         }
      } else {
         return actual;
      }
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return wrapSubscriber(actual, this.source, this, this.snapshotStack);
   }

   static class AssemblySnapshot {
      final boolean isCheckpoint;
      @Nullable
      final String description;
      @Nullable
      final Supplier<String> assemblyInformationSupplier;
      String cached;

      AssemblySnapshot(@Nullable String description, Supplier<String> assemblyInformationSupplier) {
         this(description != null, description, assemblyInformationSupplier);
      }

      AssemblySnapshot(String assemblyInformation) {
         this.isCheckpoint = false;
         this.description = null;
         this.assemblyInformationSupplier = null;
         this.cached = assemblyInformation;
      }

      private AssemblySnapshot(boolean isCheckpoint, @Nullable String description, @Nullable Supplier<String> assemblyInformationSupplier) {
         this.isCheckpoint = isCheckpoint;
         this.description = description;
         this.assemblyInformationSupplier = assemblyInformationSupplier;
      }

      public boolean hasDescription() {
         return this.description != null;
      }

      @Nullable
      public String getDescription() {
         return this.description;
      }

      public boolean isCheckpoint() {
         return this.isCheckpoint;
      }

      public boolean isLight() {
         return false;
      }

      public String lightPrefix() {
         return "";
      }

      String toAssemblyInformation() {
         if (this.cached == null) {
            if (this.assemblyInformationSupplier == null) {
               throw new IllegalStateException("assemblyInformation must either be supplied or resolvable");
            }

            this.cached = (String)this.assemblyInformationSupplier.get();
         }

         return this.cached;
      }

      String operatorAssemblyInformation() {
         return Traces.extractOperatorAssemblyInformation(this.toAssemblyInformation());
      }
   }

   static final class CheckpointHeavySnapshot extends FluxOnAssembly.AssemblySnapshot {
      CheckpointHeavySnapshot(@Nullable String description, Supplier<String> assemblyInformationSupplier) {
         super(true, description, assemblyInformationSupplier);
      }

      @Override
      public String lightPrefix() {
         return "checkpoint(" + (this.description == null ? "" : this.description) + ")";
      }
   }

   static final class CheckpointLightSnapshot extends FluxOnAssembly.AssemblySnapshot {
      CheckpointLightSnapshot(@Nullable String description) {
         super(true, description, null);
         this.cached = "checkpoint(\"" + (description == null ? "" : description) + "\")";
      }

      @Override
      public boolean isLight() {
         return true;
      }

      @Override
      public String lightPrefix() {
         return "checkpoint";
      }

      @Override
      String operatorAssemblyInformation() {
         return this.cached;
      }
   }

   static final class MethodReturnSnapshot extends FluxOnAssembly.AssemblySnapshot {
      MethodReturnSnapshot(String method) {
         super(false, method, null);
         this.cached = method;
      }

      @Override
      public boolean isLight() {
         return true;
      }

      @Override
      String operatorAssemblyInformation() {
         return this.cached;
      }
   }

   static final class ObservedAtInformationNode implements Serializable {
      private static final long serialVersionUID = 1L;
      final int id;
      final String operator;
      final String message;
      int occurrenceCounter;
      @Nullable
      FluxOnAssembly.ObservedAtInformationNode parent;
      Set<FluxOnAssembly.ObservedAtInformationNode> children;

      ObservedAtInformationNode(int id, String operator, String message) {
         this.id = id;
         this.operator = operator;
         this.message = message;
         this.occurrenceCounter = 0;
         this.children = new LinkedHashSet();
      }

      void incrementCount() {
         ++this.occurrenceCounter;
      }

      void addNode(FluxOnAssembly.ObservedAtInformationNode node) {
         if (this != node) {
            if (this.children.add(node)) {
               node.parent = this;
            }

         }
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            FluxOnAssembly.ObservedAtInformationNode node = (FluxOnAssembly.ObservedAtInformationNode)o;
            return this.id == node.id && this.operator.equals(node.operator) && this.message.equals(node.message);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.id, this.operator, this.message});
      }

      public String toString() {
         return this.operator + "{@" + this.id + (this.children.isEmpty() ? "" : ", " + this.children.size() + " children") + '}';
      }
   }

   static final class OnAssemblyConditionalSubscriber<T> extends FluxOnAssembly.OnAssemblySubscriber<T> implements Fuseable.ConditionalSubscriber<T> {
      final Fuseable.ConditionalSubscriber<? super T> actualCS;

      OnAssemblyConditionalSubscriber(
         Fuseable.ConditionalSubscriber<? super T> actual, FluxOnAssembly.AssemblySnapshot stacktrace, Publisher<?> parent, Publisher<?> current
      ) {
         super(actual, stacktrace, parent, current);
         this.actualCS = actual;
      }

      @Override
      public boolean tryOnNext(T t) {
         return this.actualCS.tryOnNext(t);
      }
   }

   static final class OnAssemblyException extends RuntimeException {
      private static final long serialVersionUID = -6342981676020433721L;
      final Map<Integer, FluxOnAssembly.ObservedAtInformationNode> nodesPerId = new HashMap();
      final FluxOnAssembly.ObservedAtInformationNode root = new FluxOnAssembly.ObservedAtInformationNode(-1, "ROOT", "ROOT");
      int maxOperatorSize = 0;

      OnAssemblyException(String message) {
         super(message);
      }

      public Throwable fillInStackTrace() {
         return this;
      }

      void add(Publisher<?> parent, Publisher<?> current, FluxOnAssembly.AssemblySnapshot snapshot) {
         if (snapshot.isCheckpoint()) {
            if (snapshot.isLight()) {
               this.add(parent, current, snapshot.lightPrefix(), (String)Objects.requireNonNull(snapshot.getDescription()));
            } else {
               String assemblyInformation = snapshot.toAssemblyInformation();
               String[] parts = Traces.extractOperatorAssemblyInformationParts(assemblyInformation);
               if (parts.length > 0) {
                  String line = parts[parts.length - 1];
                  this.add(parent, current, snapshot.lightPrefix(), line);
               } else {
                  this.add(parent, current, snapshot.lightPrefix(), (String)Objects.requireNonNull(snapshot.getDescription()));
               }
            }
         } else {
            String assemblyInformation = snapshot.toAssemblyInformation();
            String[] parts = Traces.extractOperatorAssemblyInformationParts(assemblyInformation);
            if (parts.length > 0) {
               String prefix = parts.length > 1 ? parts[0] : "";
               String line = parts[parts.length - 1];
               this.add(parent, current, prefix, line);
            }
         }

      }

      private void add(Publisher<?> operator, Publisher<?> currentAssembly, String prefix, String line) {
         Scannable parentAssembly = (Scannable)Scannable.from(currentAssembly).parents().filter(s -> s instanceof AssemblyOp).findFirst().orElse(null);
         int thisId = System.identityHashCode(currentAssembly);
         int parentId = System.identityHashCode(parentAssembly);
         synchronized(this.nodesPerId) {
            FluxOnAssembly.ObservedAtInformationNode thisNode = (FluxOnAssembly.ObservedAtInformationNode)this.nodesPerId.get(thisId);
            if (thisNode != null) {
               thisNode.incrementCount();
            } else {
               thisNode = new FluxOnAssembly.ObservedAtInformationNode(thisId, prefix, line);
               this.nodesPerId.put(thisId, thisNode);
            }

            if (parentAssembly == null) {
               this.root.addNode(thisNode);
            } else {
               FluxOnAssembly.ObservedAtInformationNode parentNode = (FluxOnAssembly.ObservedAtInformationNode)this.nodesPerId.get(parentId);
               if (parentNode != null) {
                  parentNode.addNode(thisNode);
               } else {
                  this.root.addNode(thisNode);
               }
            }

            int length = thisNode.operator.length();
            if (length > this.maxOperatorSize) {
               this.maxOperatorSize = length;
            }

         }
      }

      void findPathToLeaves(FluxOnAssembly.ObservedAtInformationNode node, List<List<FluxOnAssembly.ObservedAtInformationNode>> rootPaths) {
         if (!node.children.isEmpty()) {
            node.children.forEach(n -> this.findPathToLeaves(n, rootPaths));
         } else {
            List<FluxOnAssembly.ObservedAtInformationNode> pathForLeaf = new LinkedList();

            for(FluxOnAssembly.ObservedAtInformationNode traversed = node; traversed != null && traversed != this.root; traversed = traversed.parent) {
               pathForLeaf.add(0, traversed);
            }

            rootPaths.add(pathForLeaf);
         }
      }

      public String getMessage() {
         synchronized(this.nodesPerId) {
            if (this.root.children.isEmpty()) {
               return super.getMessage();
            } else {
               StringBuilder sb = new StringBuilder(super.getMessage())
                  .append(System.lineSeparator())
                  .append("Error has been observed at the following site(s):")
                  .append(System.lineSeparator());
               List<List<FluxOnAssembly.ObservedAtInformationNode>> rootPaths = new ArrayList();
               this.root.children.forEach(actualRoot -> this.findPathToLeaves(actualRoot, rootPaths));
               rootPaths.forEach(path -> path.forEach(node -> {
                     boolean isRoot = node.parent == null || node.parent == this.root;
                     sb.append("\t");
                     String connector = "|_";
                     if (isRoot) {
                        connector = "*_";
                     }

                     sb.append(connector);
                     char filler = (char)(isRoot ? 95 : 32);

                     for(int i = node.operator.length(); i < this.maxOperatorSize; ++i) {
                        sb.append(filler);
                     }

                     sb.append(filler);
                     sb.append(node.operator);
                     sb.append(" ⇢ ");
                     sb.append(node.message);
                     if (node.occurrenceCounter > 0) {
                        sb.append(" (observed ").append(node.occurrenceCounter + 1).append(" times)");
                     }

                     sb.append(System.lineSeparator());
                  }));
               sb.append("Original Stack Trace:");
               return sb.toString();
            }
         }
      }

      public String toString() {
         String message = this.getLocalizedMessage();
         return message == null
            ? "The stacktrace should have been enhanced by Reactor, but there was no message in OnAssemblyException"
            : "The stacktrace has been enhanced by Reactor, refer to additional information below: " + message;
      }
   }

   static class OnAssemblySubscriber<T> implements InnerOperator<T, T>, Fuseable.QueueSubscription<T> {
      final FluxOnAssembly.AssemblySnapshot snapshotStack;
      final Publisher<?> parent;
      final Publisher<?> current;
      final CoreSubscriber<? super T> actual;
      Fuseable.QueueSubscription<T> qs;
      Subscription s;
      int fusionMode;

      OnAssemblySubscriber(CoreSubscriber<? super T> actual, FluxOnAssembly.AssemblySnapshot snapshotStack, Publisher<?> parent, Publisher<?> current) {
         this.actual = actual;
         this.snapshotStack = snapshotStack;
         this.parent = parent;
         this.current = current;
      }

      @Override
      public final CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.ACTUAL_METADATA) {
            return !this.snapshotStack.isCheckpoint;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      public String toString() {
         return this.snapshotStack.operatorAssemblyInformation();
      }

      @Override
      public String stepName() {
         return this.toString();
      }

      @Override
      public final void onNext(T t) {
         this.actual.onNext(t);
      }

      @Override
      public final void onError(Throwable t) {
         this.actual.onError(this.fail(t));
      }

      @Override
      public final void onComplete() {
         this.actual.onComplete();
      }

      @Override
      public final int requestFusion(int requestedMode) {
         Fuseable.QueueSubscription<T> qs = this.qs;
         if (qs != null) {
            int m = qs.requestFusion(requestedMode);
            if (m != 0) {
               this.fusionMode = m;
            }

            return m;
         } else {
            return 0;
         }
      }

      final Throwable fail(Throwable t) {
         boolean lightCheckpoint = this.snapshotStack.isLight();
         FluxOnAssembly.OnAssemblyException onAssemblyException = null;

         for(Throwable e : t.getSuppressed()) {
            if (e instanceof FluxOnAssembly.OnAssemblyException) {
               onAssemblyException = (FluxOnAssembly.OnAssemblyException)e;
               break;
            }
         }

         if (onAssemblyException == null) {
            if (lightCheckpoint) {
               onAssemblyException = new FluxOnAssembly.OnAssemblyException("");
            } else {
               StringBuilder sb = new StringBuilder();
               FluxOnAssembly.fillStacktraceHeader(sb, this.parent.getClass(), this.snapshotStack.getDescription());
               sb.append(this.snapshotStack.toAssemblyInformation().replaceFirst("\\n$", ""));
               String description = sb.toString();
               onAssemblyException = new FluxOnAssembly.OnAssemblyException(description);
            }

            t = Exceptions.addSuppressed(t, onAssemblyException);
            StackTraceElement[] stackTrace = t.getStackTrace();
            if (stackTrace.length > 0) {
               StackTraceElement[] newStackTrace = new StackTraceElement[stackTrace.length];
               int i = 0;

               for(StackTraceElement stackTraceElement : stackTrace) {
                  String className = stackTraceElement.getClassName();
                  if (!className.startsWith("reactor.core.publisher.") || !className.contains("OnAssembly")) {
                     newStackTrace[i] = stackTraceElement;
                     ++i;
                  }
               }

               newStackTrace = (StackTraceElement[])Arrays.copyOf(newStackTrace, i);
               onAssemblyException.setStackTrace(newStackTrace);
               t.setStackTrace(new StackTraceElement[]{stackTrace[0]});
            }
         }

         onAssemblyException.add(this.parent, this.current, this.snapshotStack);
         return t;
      }

      public final boolean isEmpty() {
         try {
            return this.qs.isEmpty();
         } catch (Throwable var2) {
            Exceptions.throwIfFatal(var2);
            throw Exceptions.propagate(this.fail(var2));
         }
      }

      @Override
      public final void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.qs = Operators.as(s);
            this.actual.onSubscribe(this);
         }

      }

      public final int size() {
         return this.qs.size();
      }

      public final void clear() {
         this.qs.clear();
      }

      @Override
      public final void request(long n) {
         this.s.request(n);
      }

      @Override
      public final void cancel() {
         this.s.cancel();
      }

      @Nullable
      public final T poll() {
         try {
            return (T)this.qs.poll();
         } catch (Throwable var2) {
            Exceptions.throwIfFatal(var2);
            throw Exceptions.propagate(this.fail(var2));
         }
      }
   }
}

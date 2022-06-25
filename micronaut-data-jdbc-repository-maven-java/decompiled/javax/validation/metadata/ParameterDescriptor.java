package javax.validation.metadata;

public interface ParameterDescriptor extends ElementDescriptor, CascadableDescriptor, ContainerDescriptor {
   int getIndex();

   String getName();
}

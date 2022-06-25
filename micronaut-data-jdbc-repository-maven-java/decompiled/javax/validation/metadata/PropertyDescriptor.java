package javax.validation.metadata;

public interface PropertyDescriptor extends ElementDescriptor, CascadableDescriptor, ContainerDescriptor {
   String getPropertyName();
}

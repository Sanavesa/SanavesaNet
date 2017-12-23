package sanavesa.net;

import java.io.Serializable;

/**
 * The {@link NetworkSerializable} interface is the core of the networking data
 * transmission. It provides an interface that implemented, allows the
 * implementer to be sent over the network. This interface is a simple wrapper
 * of the {@link Serializable} interface which provides a default serialization
 * and deserialization method, which can be overridden. It is extremely
 * preferable to generate a <code>serialVersionUID</code> for the implementer.
 * Moreover, this interface contains methods to handle the packet for both the
 * client and the server.
 * 
 * <p>
 * As per the <code>Serializable</code> documentation:
 * </p>
 * 
 * <p>
 * Classes that require special handling during the serialization and
 * deserialization process must implement special methods with these exact
 * signatures:
 * </p>
 * 
 * <code>
 * private void writeObject(java.io.ObjectOutputStream out) throws IOException 
 * </code>
 * 
 * <p>
 * The <code>writeObject</code> method is responsible for writing the state of
 * the object for its particular class so that the corresponding
 * <code>readObject</code> method can restore it. The default mechanism for
 * saving the Object's fields can be invoked by calling
 * <code>out.defaultWriteObject</code>. The method does not need to concern
 * itself with the state belonging to its superclasses or subclasses. State is
 * saved by writing the individual fields to the ObjectOutputStream using the
 * writeObject method or by using the methods for primitive data types supported
 * by <code>DataOutput</code>.
 * </p>
 * 
 * <code>
 * private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException;
 * </code>
 * 
 * <p>
 * The <code>readObject</code> method is responsible for reading from the stream
 * and restoring the classes fields. It may call
 * <code>in.defaultReadObject</code> to invoke the default mechanism for
 * restoring the object's non-static and non-transient fields. The
 * <code>defaultReadObject</code> method uses information in the stream to
 * assign the fields of the object saved in the stream with the correspondingly
 * named fields in the current object. This handles the case when the class has
 * evolved to add new fields. The method does not need to concern itself with
 * the state belonging to its superclasses or subclasses. State is saved by
 * writing the individual fields to the ObjectOutputStream using the writeObject
 * method or by using the methods for primitive data types supported by
 * <code>DataOutput</code>.
 * </p>
 * 
 * <code>
 * private void readObjectNoData() throws ObjectStreamException;
 * </code>
 * 
 * <p>
 * The <code>readObjectNoData</code> method is responsible for initializing the
 * state of the object for its particular class in the event that the
 * serialization stream does not list the given class as a superclass of the
 * object being deserialized. This may occur in cases where the receiving party
 * uses a different version of the deserialized instance's class than the
 * sending party, and the receiver's version extends classes that are not
 * extended by the sender's version. This may also occur if the serialization
 * stream has been tampered; hence, <code>readObjectNoData</code> is useful for
 * initializing deserialized objects properly despite a "hostile" or incomplete
 * source stream.
 * </p>
 * 
 * <p>
 * For more serialization details, please refer to the <code>Serializable</code>
 * documentation.
 * </p>
 * 
 * @see Client
 * @see Server
 * @see Serializable
 * 
 * @since 1.0
 * @author Mohammad Alali
 */
public interface NetworkSerializable extends Serializable
{
	/**
	 * This method is invoked on the client side whenever this object is read by
	 * the client. By default, the method implementation is empty.
	 * 
	 * @param <T>
	 *            - the class type of the client who received this object
	 * @param client
	 *            - the client who read this serializable object
	 * 
	 * @see Client
	 * 
	 * @since 1.1
	 * @author Mohammad Alali
	 */
	default <T extends Client> void handleOnClient(T client)
	{
		// No implementation
	}

	/**
	 * This method is invoked on the server side whenever this object is read by
	 * the server. By default, the method implementation is empty.
	 * 
	 * @param <T>
	 *            - the class type of the server who received this object
	 * @param server
	 *            - the server who read this serializable object
	 * @param sender
	 *            - the client who sent this object
	 * 
	 * @see Server
	 * 
	 * @since 1.1
	 * @author Mohammad Alali
	 */
	default <T extends Server> void handleOnServer(T server, ServerClient sender)
	{
		// No implementation
	}
}

package sanavesa.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * The {@link NetworkSerializable} interface is the core of the networking data
 * transmission. It provides an interface that implemented, allows the
 * implementer to be sent over the network. The interface provides a default
 * serialization and deserialization method, which can be overridden. It is
 * extremely preferable to generate a <code>serialVersionUID</code> for the
 * implementer. Moreover, this interface contains methods to handle the packet
 * for both the client and the server.
 * 
 * @see Client
 * @see Server
 * 
 * @since 1.0
 * @author Mohammad Alali
 */
public interface NetworkSerializable extends Serializable
{
	/**
	 * This method is invoked on the client side whenever this object is read by
	 * the client.
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
	 * the server.
	 * 
	 * @param <T>
	 *            - the class type of the server who received this object
	 * @param server
	 *            - the server who read this serializable object
	 * @param client
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

	/**
	 * This method provides the capability to override the default
	 * implementation of reading an object from the stream. The default
	 * implementation will serialize all non-transient, non-static fields.
	 * 
	 * @param in
	 *            - the stream to read from
	 * @throws IOException
	 *             - thrown when any I/O errors occur
	 * @throws ClassNotFoundException
	 *             - thrown when the class read is not found anywhere in the
	 *             class files
	 * 
	 * @see ObjectInputStream
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	default void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
	}

	/**
	 * This method provides the capability to override the default
	 * implementation of writing an object to the stream. The default
	 * implementation will serialize all non-transient, non-static fields.
	 * 
	 * @param out
	 *            - the stream to write to
	 * @throws IOException
	 *             - thrown when any I/O errors occur
	 * 
	 * @see ObjectOutputStream
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	default void writeObject(ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
	}
}

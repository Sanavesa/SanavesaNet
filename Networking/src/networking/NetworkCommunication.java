package networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * The <code>NetworkCommunication</code> class sets up the communication for a
 * <code>Socket</code> object. It has helper methods to send and read data from
 * the socket's streams. The allowed data to be sent and read should be a class
 * that implements the {@link NetworkSerializable} interface.
 * 
 * @see Socket
 * @see NetworkSerializable
 * 
 * @since 1.0
 * @author Mohammad Alali
 */
public final class NetworkCommunication
{
	/** The input stream of the connection. */
	private final ObjectInputStream inputReader;

	/** The output stream of the connection. */
	private final ObjectOutputStream outputWriter;

	/**
	 * Sets up the input and output streams of the socket for network
	 * communication.
	 * 
	 * @param socket
	 *            - the socket to setup communication for
	 * 
	 * @throws IllegalArgumentException
	 *             - thrown when the the parameter <code>socket</code> is null.
	 * @throws RuntimeException
	 *             - thrown when facing issues creating input and output streams
	 *             for the socket.
	 * 
	 * @see Socket
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	public NetworkCommunication(Socket socket) throws IllegalArgumentException, RuntimeException
	{
		// Validate socket parameter
		if (socket == null)
		{
			throw new IllegalArgumentException(
					"Constructor parameter 'socket' is null in NetworkConnection::NetworkConnection.");
		}

		// Setups streams
		try
		{
			outputWriter = new ObjectOutputStream(socket.getOutputStream());
			outputWriter.flush();
			inputReader = new ObjectInputStream(socket.getInputStream());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Closes the input and output streams of the socket. Any subsequent calls
	 * of {@link #sendData(NetworkSerializable)} or {@link #readData()} will
	 * throw a {@link RuntimeException} as the underlying streams have been
	 * closed.
	 * 
	 * @throws NullPointerException
	 *             - thrown when the connections input or output stream is null.
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	public final void close() throws NullPointerException
	{
		// Validate the input stream
		if (inputReader == null)
		{
			throw new NullPointerException("Class member 'inputReader' is null in NetworkConnection::close.");
		}

		// Validate the output stream
		if (outputWriter == null)
		{
			throw new NullPointerException("Class member 'outputWriter' is null in NetworkConnection::close.");
		}

		// Close the input stream
		if (inputReader != null)
		{
			try
			{
				inputReader.close();
			}
			catch (IOException e)
			{
				// Ignore handling
			}
		}

		// Close the output stream
		if (outputWriter != null)
		{
			try
			{
				outputWriter.close();
			}
			catch (IOException e)
			{
				// Ignore handling
			}
		}
	}

	/**
	 * Reads and returns an object from the connection's streams. If there was
	 * nothing to read or if it read a non-supported object, the method will
	 * return null. Supported types are classes that implement the
	 * {@link NetworkSerializable} interface.
	 * 
	 * @return the received object, or null if received nothing.
	 * 
	 * @throws NullPointerException
	 *             - thrown when the connection's input stream is null.
	 * @throws RuntimeException
	 *             - thrown when facing issues reading from the connection's
	 *             input stream.
	 * 
	 * @see NetworkSerializable
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	public final NetworkSerializable readData() throws RuntimeException
	{
		// Validate input stream
		if (inputReader == null)
		{
			throw new NullPointerException("Class member 'inputReader' is null in NetworkConnection::readData.");
		}

		// Read data
		synchronized (inputReader)
		{
			Object obj;
			try
			{
				obj = inputReader.readObject();
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}

			if (obj instanceof NetworkSerializable)
				return (NetworkSerializable) obj;

			return null;
		}
	}

	/**
	 * Writes the specified <code>data</code> into the connection's output
	 * stream, and flushes the stream.
	 * 
	 * @param <T>
	 *            - the class type of the data received
	 * @param data
	 *            - the data object to send
	 * 
	 * @throws IllegalArgumentException
	 *             - thrown when the parameter <code>data</code> is null.
	 * @throws NullPointerException
	 *             - thrown when the connection's output stream is null.
	 * @throws RuntimeException
	 *             - thrown when facing issues writing to the connection's
	 *             output stream.
	 * 
	 * @see NetworkSerializable
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	public final <T extends NetworkSerializable> void sendData(T data)
			throws IllegalArgumentException, NullPointerException, RuntimeException
	{
		// Validate data parameter
		if (data == null)
		{
			throw new IllegalArgumentException("Method parameter 'data' is null in NetworkConnection::sendData.");
		}

		// Validate output stream
		if (outputWriter == null)
		{
			throw new NullPointerException("Class member 'outputWriter' is null in NetworkConnection::sendData.");
		}

		// Send data
		synchronized (outputWriter)
		{
			try
			{
				outputWriter.writeObject(data);
				outputWriter.flush();
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}

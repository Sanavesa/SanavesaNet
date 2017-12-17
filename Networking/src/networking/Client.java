package networking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * The <code>Client</code> class is the back-end foundation for the
 * client-server architecture. It contains the essential features required to
 * represent a client. To fully utilize this class' capabilities, it is
 * preferable to extend the class to fine-tune the client. The data transmission
 * is handled by the {@link NetworkCommunication} class, which runs on its own
 * thread.
 * 
 * @see NetworkCommunication
 * 
 * @since 1.0
 * @author Mohammad Alali
 */
public class Client
{
	/**
	 * The duration in milliseconds for connection timeout. Default value is 5
	 * seconds.
	 */
	protected final static int TIMEOUT_DURATION = 5000;

	/** The client's socket. */
	Socket socket = null;

	/** The communication for the socket. */
	NetworkCommunication networkCommunication = null;

	/**
	 * The client's own thread to handle incoming and outgoing communication.
	 */
	Thread communicationThread = null;

	/** A flag of whether the client is connected. */
	volatile boolean connected = false;

	/**
	 * Used internally to reset the instance's variables. It will terminate the
	 * connection and its streams, and stop the communication thread.
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	final void resetInstance()
	{
		// Change the connection flag
		connected = false;

		// Stop and reset communication streams
		if (networkCommunication != null)
		{
			networkCommunication.close();
			networkCommunication = null;
		}

		// Stop and reset communication thread
		if (communicationThread != null)
		{
			try
			{
				communicationThread.join(10);
			}
			catch (InterruptedException e)
			{
				// Ignore handling
			}
			communicationThread = null;
		}

		// Close and reset socket
		if (socket != null)
		{
			try
			{
				socket.close();
			}
			catch (IOException e)
			{
				// Ignore handling
			}
			socket = null;
		}
	}

	/**
	 * Connects the client instance to the specified <code>hostName</code> and
	 * <code>port</code>. If the client is already connected to any connection,
	 * this method will abort early and do nothing. The following methods will
	 * get triggered:
	 * 
	 * <ul>
	 * <li>{@link #onConnected(String, int)} - if the connection was
	 * successful</li>
	 * <li>{@link #onFailedToConnect(String, int)} - if the connection was a
	 * failure</li>
	 * </ul>
	 * 
	 * @param ipAddress
	 *            - the IP address to connect to
	 * @param port
	 *            - the port to connect to
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	public final void connect(String ipAddress, int port)
	{
		// Terminate early if already connected to any connection.
		if (isConnected())
		{
			return;
		}

		try
		{
			// Create a socket and attempt to connect
			socket = new Socket();
			socket.connect(new InetSocketAddress(ipAddress, port), TIMEOUT_DURATION);

			// If no errors, attempt to setup network communications
			networkCommunication = new NetworkCommunication(socket);

			// All was successful, now setup separate thread for networking
			connected = true;
			communicationThread = new Thread(new ClientThread(this));
			communicationThread.start();
		}
		catch (Exception e)
		{
			// An error occurred.
			resetInstance();
		}

		// Trigger events based on conditions.
		if (isConnected())
		{
			onConnected(ipAddress, port);
		}
		else
		{
			onFailedToConnect(ipAddress, port);
		}
	}

	/**
	 * Disconnect the client from the server. If already disconnected, the
	 * method will terminate early and do nothing. The
	 * {@link #onDisconnected(String, int)} method will be triggered on a
	 * successful disconnection.
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	public final void disconnect()
	{
		disconnect(true);
	}

	/**
	 * Disconnect the client from the server. The parameter
	 * <code>notifyDisconnection</code> is used as follows:
	 * 
	 * <ul>
	 * <li><b>true</b> - if the disconnection request was made locally.</li>
	 * <li><b>false</b> - if disconnection request was made remotely.</li>
	 * </ul>
	 * 
	 * @param notifyDisconnection
	 *            - a flag of whether the disconnection request was made locally
	 *            or remotely
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	final void disconnect(boolean notifyDisconnection)
	{
		// Terminate early if already disconnected.
		if (!isConnected())
		{
			return;
		}

		// If disconnection request was made locally, then notify the other side
		// of communication about it.
		if (notifyDisconnection)
		{
			DisconnectClientPacket disconnectionPacket = new DisconnectClientPacket();
			networkCommunication.sendData(disconnectionPacket);
		}

		// Cache values before reseting them
		String ipAddress = socket.getInetAddress().getHostAddress();
		int port = socket.getPort();

		// Trigger the event
		onDisconnected(ipAddress, port);

		// Reset socket locally
		resetInstance();
	}

	/**
	 * Returns the connection status of the client.
	 * 
	 * @return true if the client is connected to the server; false otherwise
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	public final boolean isConnected()
	{
		return connected;
	}

	/**
	 * If the client is connected to a server, this method will return the
	 * network communication instance of the client that aids in transmitting
	 * and receiving data from the server. And if the client is not connected,
	 * this method will return null.
	 * 
	 * @return the network communication instance with server, if the client is
	 *         connected; otherwise, returns null
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	public final NetworkCommunication getCommunication()
	{
		return networkCommunication;
	}

	/**
	 * Returns the socket reference of the client. If there is no connection,
	 * null will be returned.
	 * 
	 * @return the socket of the client
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	protected final Socket getSocket()
	{
		return socket;
	}

	/**
	 * The <code>run</code> method handles the incoming networking transmission.
	 * This method should never be explicitly called as it automatically handled
	 * by the {@link ClientThread} class, which calls this method on a separate
	 * thread.
	 * 
	 * <p>
	 * Incoming data will trigger the
	 * {@link #onReceivedData(NetworkSerializable)} method, where the user is
	 * able to override and handle the data as he or she wishes.
	 * </p>
	 * 
	 * @see ClientThread
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	private final void run()
	{
		while (isConnected())
		{
			// Get recent data from stream
			NetworkSerializable data = null;
			try
			{
				data = networkCommunication.readData();
			}
			catch (Exception e)
			{
				// Ignore handling
			}

			// If there is data, process it
			if (data != null)
			{
				// Handle the data received
				onReceivedData(data);
			}
		}
	}

	/**
	 * This method is triggered automatically when the client has successfully
	 * connected to a server via the {@link #connect(String, int)} method.
	 * 
	 * @param ipAddress
	 *            - the IP address of the server we connected to
	 * @param port
	 *            - the port of the server we connected to
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	protected void onConnected(String ipAddress, int port)
	{
	}

	/**
	 * This method is triggered automatically when the client has successfully
	 * disconnected from a server via the {@link #disconnect()} method.
	 * 
	 * @param ipAddress
	 *            - the IP address of the server we disconnected from
	 * @param port
	 *            - the port of the server we disconnected from
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	protected void onDisconnected(String ipAddress, int port)
	{
	}

	/**
	 * This method is triggered automatically when the client has failed to
	 * connect to a server via the {@link #connect(String, int)} method.
	 * 
	 * @param ipAddress
	 *            - the attempted IP address of the server
	 * @param port
	 *            - the attempted port of the server
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	protected void onFailedToConnect(String ipAddress, int port)
	{
	}

	/**
	 * This method is triggered automatically when the client has received data
	 * from the server. The data received is a class that implements the
	 * {@link NetworkSerializable} interface. The data received is handled by
	 * the interface via the {@link NetworkSerializable#handleOnClient(Client)}
	 * method.
	 * 
	 * @param <T>
	 *            - the class type of the data received
	 * @param data
	 *            - the data object received from the server
	 * 
	 * @see NetworkSerializable
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	<T extends NetworkSerializable> void onReceivedData(T data)
	{
		data.handleOnClient(this);
	}

	/**
	 * Sends the specified data object to the server.
	 * 
	 * @param <T>
	 *            - the class type of the data to send
	 * @param data
	 *            - the data to send to the server
	 * 
	 * @since 1.1
	 * @author Mohammad Alali
	 */
	public final <T extends NetworkSerializable> void sendData(T data)
	{
		networkCommunication.sendData(data);
	}

	/**
	 * The <code>ClientThread</code> class manages the networking transmission
	 * on a separate thread.
	 * 
	 * @see Client
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	final class ClientThread implements Runnable
	{
		/** The client owner of this thread. */
		private final Client client;

		/**
		 * Creates a new thread for the client, and calls begins the networking
		 * communication loop.
		 * 
		 * @param client
		 *            - the client owner of the thread
		 * 
		 * @see Client
		 * 
		 * @since 1.0
		 * @author Mohammad Alali
		 */
		public ClientThread(Client client)
		{
			this.client = client;
		}

		/**
		 * Starts the communication thread for the client that would keep
		 * listening for new data while it is still connected. This method
		 * should not be called explicitly as it automatically managed by the
		 * {@link Runnable} interface.
		 * 
		 * @see Client
		 * 
		 * @since 1.0
		 * @author Mohammad Alali
		 */
		@Override
		public void run()
		{
			client.run();
		}
	}
}
package networking;

import java.io.IOException;
import java.net.Socket;

/**
 * The <code>ServerClient</code> class is the client representation on the
 * server side. It contains the essential features required to represent a
 * client on the server.
 * 
 * @see Server
 * 
 * @since 1.0
 * @author Mohammad Alali
 */
public final class ServerClient
{
	/**
	 * The client's own thread to handle incoming and outgoing communication.
	 */
	private Thread communicationThread = null;

	/** A flag of whether the client is connected. */
	private volatile boolean connected = false;

	/** The communication for the socket. */
	private NetworkCommunication networkCommunication = null;

	/** The server this client is connected to. */
	private final Server server;

	/** The client's socket. */
	private Socket socket = null;

	/**
	 * Constructs a new client on the server. It is used to represent the client
	 * on the server side. This class shouldn't be instantiated manually as it
	 * is handled automatically by the library.
	 *
	 * @param server
	 *            - the server the client has connected to
	 * @param socket
	 *            - the socket of the connected client
	 * 
	 * @throws IllegalArgumentException
	 *             - thrown when the the parameter <code>server</code> or
	 *             <code>socket</code> is null.
	 * @throws RuntimeException
	 *             - thrown when facing issues creating input and output streams
	 *             for the socket.
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	ServerClient(Server server, Socket socket) throws IllegalArgumentException
	{
		// Validate server parameter
		if (server == null)
		{
			throw new IllegalArgumentException("Constructor parameter 'server' is null in ServerClient::ServerClient.");
		}

		// Validate socket parameter
		if (socket == null)
		{
			throw new IllegalArgumentException("Constructor parameter 'socket' is null in ServerClient::ServerClient.");
		}

		// Cache references
		this.server = server;
		this.socket = socket;

		try
		{
			// Setup socket network communication
			networkCommunication = new NetworkCommunication(socket);

			// Setup separate thread for networking
			connected = true;
			communicationThread = new Thread(new ServerClientThread(this));
			communicationThread.start();

			// Trigger connection event
			onConnected();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
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

		// Trigger the event
		onDisconnected();

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
	 * Propagate the connected event to the server.
	 */
	/**
	 * This method is triggered automatically when the client has successfully
	 * connected to the server.
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	private final void onConnected()
	{
		server.clientList.add(this);
		server.onClientConnected(this);
	}

	/**
	 * Propagate the disconnected event to the server.
	 */
	/**
	 * This method is triggered automatically when the client has successfully
	 * disconnected from the server.
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	private final void onDisconnected()
	{
		server.clientList.remove(this);
		server.onClientDisconnected(this);
	}

	/**
	 * This method is triggered automatically when the server has received data
	 * from this client. The data received is a class that implements the
	 * {@link NetworkSerializable} interface. The data received is handled by
	 * the interface via the
	 * {@link NetworkSerializable#handleOnServer(Server, ServerClient)} method.
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
	private final <T extends NetworkSerializable> void onReceivedData(T data)
	{
		server.onReceivedData(data, this);
	}

	/**
	 * Used internally to reset the instance's variables. It will terminate the
	 * connection and its streams, and stop the communication thread.
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	private final void resetInstance()
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
	final void run()
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
	 * Sends the specified data object to the client.
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
}
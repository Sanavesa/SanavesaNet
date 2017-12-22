package sanavesa.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * The Server class is the backend foundation for the server. It contains all
 * the network functionality with clients such as accepting new connections,
 * sending, and receiving new data.
 * 
 * The <code>Server</code> class is the back-end foundation for the
 * client-server architecture. It contains the essential features required to
 * represent a server. To fully utilize this class' capabilities, it is
 * preferable to extend the class to fine-tune the server. The data transmission
 * for each connected client is handled by the {@link NetworkCommunication}
 * class. Also, each client has its own thread for network data transmission.
 * 
 * @see NetworkCommunication
 * @see ServerClient
 * 
 * @since 1.0
 * @author Mohammad Alali
 */
public class Server
{
	/** A list of the connected clients. */
	final List<ServerClient> clientList = new ArrayList<>();

	/** The server's own thread for listening to new connections. */
	private Thread listeningThread = null;

	/** A flag for whether the server is running or not. */
	private volatile boolean running = false;

	/** The socket of the server. */
	private ServerSocket serverSocket = null;

	/**
	 * Constructs a new <code>Server</code> instance. The constructor sets up
	 * the shutdown hook for when the server is terminated unexpectedly by
	 * disconnecting all clients from the server. It will terminate all server
	 * connections and stop the listening thread.
	 * 
	 * @since 1.1
	 * @author Mohammad Alali
	 */
	public Server()
	{
		// Sets up the hook
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				stopServer();
			}
		});
	}

	/**
	 * Returns an <b>unmodifiable</b> list of all currently connected clients.
	 * 
	 * @return the unmodifiable list of all connected clients
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	public final List<ServerClient> getClientList()
	{
		return Collections.unmodifiableList(clientList);
	}

	/**
	 * Returns the connection status of the server.
	 * 
	 * @return true if the server is running; false otherwise
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	public final boolean isRunning()
	{
		return running;
	}

	/**
	 * This method is triggered automatically when the client has successfully
	 * connected to the server.
	 * 
	 * @param client
	 *            - the client that connected to the server
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	protected void onClientConnected(ServerClient client)
	{
	}

	/**
	 * This method is triggered automatically when the client has disconnected
	 * from the server.
	 * 
	 * @param client
	 *            - the client that disconnected from the server
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	protected void onClientDisconnected(ServerClient client)
	{
	}

	/**
	 * This method is triggered automatically when the server has received data
	 * from any client. The data received is a class that implements the
	 * {@link NetworkSerializable} interface. The data received is handled by
	 * the interface via the {@link NetworkSerializable#handleOnServer(Server, ServerClient)}
	 * method.
	 * 
	 * @param <T>
	 *            - the class type of the data received
	 * @param data
	 *            - the data object received
	 * @param sender
	 *            - the sender of the data
	 * 
	 * @see NetworkSerializable
	 * @see ServerClient
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	final <T extends NetworkSerializable> void onReceivedData(T data, ServerClient sender)
	{
		data.handleOnServer(this, sender);
	}

	/**
	 * This method is triggered automatically when the server has failed to
	 * start running {@link #startServer(int)} method.
	 * 
	 * @param port
	 *            - the attempted port of the server
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	protected void onServerFailedToStart(int port)
	{
	}

	/**
	 * This method is triggered automatically when the server has successfully
	 * started running via the {@link #startServer(int)} method.
	 * 
	 * @param port
	 *            - the port of the server that started
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	protected void onServerStarted(int port)
	{
	}

	/**
	 * This method is triggered automatically when the server has stopped
	 * running via the {@link #stopServer()} method.
	 * 
	 * @param port
	 *            - the port of the server that stopped
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	protected void onServerStopped(int port)
	{
	}

	/**
	 * Used internally to reset the instance's variables. It will terminate all
	 * server connections and stop the listening thread.
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	private final void resetInstance()
	{
		// Change the running flag
		running = false;

		// Terminate all client connections
		ArrayList<ServerClient> copy = new ArrayList<>(clientList);
		for (ServerClient client : copy)
		{
			client.disconnect();
		}

		// Clear the connected client list
		clientList.clear();

		// Stop and reset listening thread
		if (listeningThread != null)
		{
			try
			{
				listeningThread.join(10);
			}
			catch (InterruptedException e)
			{
				// Ignore handling
			}
			listeningThread = null;
		}

		// Close and reset server socket
		if (serverSocket != null)
		{
			try
			{
				serverSocket.close();
			}
			catch (IOException e)
			{
				// Ignore handling
			}
			serverSocket = null;
		}
	}

	/**
	 * The <code>run</code> method handles the new client connections. This
	 * method should never be explicitly called as it automatically handled by
	 * the {@link ServerThread} class, which calls this method on a separate
	 * thread. Incoming data will trigger the
	 * {@link #onReceivedData(NetworkSerializable, ServerClient)} method, where
	 * the user is able to override and handle the data as he or she wishes.
	 * 
	 * @see ClientThread
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	final void run()
	{
		while (isRunning())
		{
			try
			{
				/*
				 * Accept new connections: The accept method will block the
				 * thread until a client asks for a connection. The only way to
				 * break the thread blocking is to close the server socket.
				 */
				Socket newSocket = serverSocket.accept();
				new ServerClient(this, newSocket);
			}
			catch (Exception e)
			{
				// Ignore handling
			}
		}
	}

	/**
	 * Sends the specified data object to the specified client.
	 * 
	 * @param <T>
	 *            - the class type of the data to send
	 * @param data
	 *            - the data to send to the specified client
	 * @param recipient
	 *            - the client who we want to send to
	 * 
	 * @throws IllegalArgumentException
	 *             - thrown when <code>data</code> or <code>recipient</code> is
	 *             null
	 * @throws IllegalStateException
	 *             - thrown when <code>recipient</code> is not connected
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	public final <T extends NetworkSerializable> void sendDataTo(T data, ServerClient recipient)
			throws IllegalArgumentException, IllegalStateException
	{
		if (data == null)
		{
			throw new IllegalArgumentException("Method parameter 'data' in Server::sendDataTo cannot be null.");
		}

		if (recipient == null)
		{
			throw new IllegalArgumentException("Method parameter 'recipient' in Server::sendDataTo cannot be null.");
		}

		if (!recipient.isConnected())
		{
			throw new IllegalStateException(
					"Failed to send data to 'recipient' in Server::sendDataTo because the client is not connected.");
		}

		recipient.sendData(data);
	}

	/**
	 * Sends the specified data object to all connected clients.
	 * 
	 * @param <T>
	 *            - the class type of the data to send
	 * @param data
	 *            - the data to send to all connected clients
	 * 
	 * 
	 * @throws IllegalArgumentException
	 *             - thrown when <code>data</code> is null
	 * @throws IllegalStateException
	 *             - thrown when any supposedly connected {@link ServerClient}
	 *             instance is not connected
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	public final <T extends NetworkSerializable> void sendDataToAll(T data)
			throws IllegalArgumentException, IllegalStateException
	{
		if (data == null)
		{
			throw new IllegalArgumentException("Method parameter 'data' in Server::sendDataToAll cannot be null.");
		}

		for (ServerClient client : clientList)
		{
			if (!client.isConnected())
			{
				throw new IllegalStateException(
						"Failed to send data to a client in Server::sendDataToAll because the client is not connected.");
			}
			client.sendData(data);
		}
	}

	/**
	 * Sends the specified data object to the specified clients that are
	 * identified by the predicate. All clients that match the condition
	 * specified in the predicate will get the data.
	 * 
	 * @param <T>
	 *            - the class type of the data to send
	 * @param data
	 *            - the data object to send
	 * @param predicate
	 *            - a predicate that defines which clients receive the packet
	 * 
	 * @throws IllegalArgumentException
	 *             - thrown when <code>data</code> or <code>predicate</code> is
	 *             null
	 * @throws IllegalStateException
	 *             - thrown when any supposedly connected {@link ServerClient}
	 *             instance that matches the condition in the predicate is not
	 *             connected
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	public final <T extends NetworkSerializable> void sendDataToMatch(T data, Predicate<ServerClient> predicate)
			throws IllegalArgumentException, IllegalStateException
	{
		if (data == null)
		{
			throw new IllegalArgumentException("Method parameter 'data' in Server::sendDataToMatch cannot be null.");
		}

		if (predicate == null)
		{
			throw new IllegalArgumentException(
					"Method parameter 'predicate' in Server::sendDataToMatch cannot be null.");
		}

		for (ServerClient client : clientList)
		{
			if (predicate.test(client))
			{
				if (!client.isConnected())
				{
					throw new IllegalStateException(
							"Failed to send data to a client in Server::sendDataToMatch because the client is not connected.");
				}
				client.sendData(data);
			}
		}
	}

	/**
	 * Attempts to start the server. If the server is already running on any
	 * port, then this method will terminate early and do nothing. The following
	 * methods will get triggered:
	 * 
	 * <ul>
	 * <li>{@link #onServerStarted(int)} - if the server started
	 * successfully</li>
	 * <li>{@link #onServerFailedToStart(int)} - if the server failed to start,
	 * most likely that the port is already in use</li>
	 * </ul>
	 * 
	 * @param port
	 *            - the port to run the server on
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	public final void startServer(int port)
	{
		// Terminate early if the server is already running on any port
		if (isRunning())
		{
			return;
		}

		try
		{
			// Attempt to create the server socket
			serverSocket = new ServerSocket(port);

			// Clear the list of connected clients
			clientList.clear();

			// All was successful, now setup separate thread for listening for
			// new connections
			running = true;
			listeningThread = new Thread(new ServerThread(this));
			listeningThread.start();
		}
		catch (Exception e)
		{
			// An error occurred
			resetInstance();
		}

		// Trigger events based on conditions
		if (isRunning())
		{
			onServerStarted(port);
		}
		else
		{
			onServerFailedToStart(port);
		}
	}

	/**
	 * Stops the server from running, which terminates all connections. Attempts
	 * to start the server. If the server is not running, then this method will
	 * terminate early and do nothing. The method {@link #onServerStopped(int)}
	 * will be triggered.
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	public final void stopServer()
	{
		// Terminate early if server is not running.
		if (!isRunning())
		{
			return;
		}

		// Cache port before reseting it
		int port = serverSocket.getLocalPort();

		// Reset instance
		resetInstance();
		
		// Trigger the event
		onServerStopped(port);
	}

	/**
	 * Returns a nicely formatted string representation of the server object
	 * along with its connection status and number of connected clients.
	 * 
	 * @return formatted string of the server object
	 * 
	 * @since 1.1
	 * @author Mohammad Alali
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("Server ");
		builder.append(super.toString());
		builder.append("\n");
		builder.append("Server Status: ");
		if (isRunning())
		{
			builder.append("Online\n");
			builder.append("Number of Connected Clients: ");
			builder.append(clientList.size());
		}
		else
		{
			builder.append("Offline\n");
		}

		return builder.toString();
	}
}

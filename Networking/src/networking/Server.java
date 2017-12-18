package networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import networking.Client.ClientThread;

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
	/** The socket of the server. */
	private ServerSocket serverSocket = null;

	/** A list of the connected clients. */
	private final List<ServerClient> clientList = new ArrayList<>();

	/** The server's own thread for listening to new connections. */
	private Thread listeningThread = null;

	/** A flag for whether the server is running or not. */
	private volatile boolean running = false;

	/**
	 * Constructs a new <code>Server</code> instance. The constructor sets up
	 * the shutdown hook for when the server is terminated unexpectedly by
	 * disconnecting all clients from the server. It will terminate all
	 * server connections and stop the listening thread.
	 * 
	 * @see #stopServer()
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

		// Trigger the event
		onServerStopped(port);

		// Reset instance
		resetInstance();
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
	 * Returns the socket reference of the server. If there is no connection,
	 * null will be returned.
	 * 
	 * @return the socket of the server
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	public final ServerSocket getServerSocket()
	{
		return serverSocket;
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
	private final void run()
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
	 * the interface via the {@link NetworkSerializable#handleOnServer(Server)}
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
	private final <T extends NetworkSerializable> void onReceivedData(T data, ServerClient sender)
	{
		data.handleOnServer(this, sender);
	}

	/**
	 * Sends the specified data object to all connected clients.
	 * 
	 * @param <T>
	 *            - the class type of the data to send
	 * @param data
	 *            - the data to send to all connected clients
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	public final <T extends NetworkSerializable> void sendDataToAll(T data)
	{
		for (ServerClient client : clientList)
		{
			client.networkCommunication.sendData(data);
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
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	public final <T extends NetworkSerializable> void sendDataToSingle(T data, ServerClient recipient)
	{
		recipient.networkCommunication.sendData(data);
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
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	public final <T extends NetworkSerializable> void sendDataTo(T data, Predicate<ServerClient> predicate)
	{
		for (ServerClient client : clientList)
		{
			if (predicate.test(client))
			{
				client.networkCommunication.sendData(data);
			}
		}
	}

	/**
	 * The <code>ServerThread</code> class manages the networking transmission
	 * on a separate thread. It will keep indefinitely listening for new
	 * connections until the server's connection is terminated.
	 * 
	 * @see Server
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	private class ServerThread implements Runnable
	{
		/** The server owner of this thread. */
		private final Server server;

		/**
		 * Creates a new thread for the server, and calls begins the networking
		 * communication loop.
		 * 
		 * @param server
		 *            - the server owner of the thread
		 * 
		 * @see Server
		 * 
		 * @since 1.0
		 * @author Mohammad Alali
		 */
		public ServerThread(Server server)
		{
			this.server = server;
		}

		/**
		 * Starts the listening thread for the server that would keep listening
		 * for new client connections while it is still running. This method
		 * should not be called explicitly as it automatically managed by the
		 * {@link Runnable} interface.
		 * 
		 * @see Server
		 * 
		 * @since 1.0
		 * @author Mohammad Alali
		 */
		@Override
		public void run()
		{
			server.run();
		}
	}

	/**
	 * The <code>ServerClient</code> class is the client representation on the
	 * server side, a modification of the {@link Client} class. It contains the
	 * essential features required to represent a client on the server. The data
	 * transmission is handled by the {@link NetworkCommunication} class, which
	 * runs on its own thread.
	 * 
	 * @see NetworkCommunication
	 * @see Client
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	public final class ServerClient extends Client
	{
		/** The server this client is connected to. */
		private Server server = null;

		/**
		 * Constructs a new client on the server. It is used to represent the
		 * client on the server side. Successful creation of a
		 * <code>ServerClient</code> instance will trigger the
		 * {@link #onConnected(String, int)} method, otherwise the
		 * {@link #onFailedToConnect(String, int)} method will be triggered.
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
		 *             - thrown when facing issues creating input and output
		 *             streams for the socket.
		 * 
		 * @since 1.0
		 * @author Mohammad Alali
		 */
		public ServerClient(Server server, Socket socket) throws IllegalArgumentException
		{
			// Validate server parameter
			if (server == null)
			{
				throw new IllegalArgumentException(
						"Constructor parameter 'server' is null in ServerClient::ServerClient.");
			}

			// Validate socket parameter
			if (socket == null)
			{
				throw new IllegalArgumentException(
						"Constructor parameter 'socket' is null in ServerClient::ServerClient.");
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
				communicationThread = new Thread(new ClientThread(this));
				communicationThread.start();

				// Trigger connection event
				onConnected("", 0);
			}
			catch (Exception e)
			{
				// Trigger connection failure event
				onFailedToConnect("", 0);

				throw new RuntimeException(e);
			}
		}

		/**
		 * Propagate the connected event to the server.
		 */
		@Override
		protected final void onConnected(String ipAddress, int port)
		{
			server.clientList.add(this);
			server.onClientConnected(this);
		}

		/**
		 * Propagate the disconnected event to the server.
		 */
		@Override
		protected final void onDisconnected(String ipAddress, int port)
		{
			server.clientList.remove(this);
			server.onClientDisconnected(this);
		}

		/**
		 * Propagate the received data to the server.
		 */
		@Override
		protected final <T extends NetworkSerializable> void onReceivedData(T data)
		{
			server.onReceivedData(data, this);
		}
	}
}

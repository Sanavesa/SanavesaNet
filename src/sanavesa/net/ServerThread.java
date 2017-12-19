package sanavesa.net;

/**
 * The <code>ServerThread</code> class manages the networking transmission on a
 * separate thread. It will keep indefinitely listening for new connections
 * until the server's connection is terminated.
 * 
 * @see Server
 * 
 * @since 1.0
 * @author Mohammad Alali
 */
class ServerThread implements Runnable
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
	 * @throws IllegalArgumentException
	 *             - thrown when the parameter <code>server</code> is null.
	 * 
	 * @see Server
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	public ServerThread(Server server)
	{
		if (server == null)
		{
			throw new IllegalArgumentException(
					"Constructor parameter 'server' in ServerThread::ServerThread cannot be null.");
		}

		this.server = server;
	}

	/**
	 * Starts the listening thread for the server that would keep listening for
	 * new client connections while it is still running. This method should not
	 * be called explicitly as it automatically managed by the {@link Runnable}
	 * interface.
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
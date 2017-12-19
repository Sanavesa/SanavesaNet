package sanavesa.net;

/**
 * The <code>ServerClientThread</code> class manages the networking transmission
 * on a separate thread.
 * 
 * @see ServerClient
 * 
 * @since 1.0
 * @author Mohammad Alali
 */
final class ServerClientThread implements Runnable
{
	/** The client owner of this thread. */
	private final ServerClient client;

	/**
	 * Creates a new thread for the client, and calls begins the networking
	 * communication loop.
	 * 
	 * @param client
	 *            - the client owner of the thread
	 * 
	 * @throws IllegalArgumentException
	 *             - thrown when the parameter <code>client</code> is null.
	 * 
	 * @see ServerClient
	 * 
	 * @since 1.0
	 * @author Mohammad Alali
	 */
	public ServerClientThread(ServerClient client)
	{
		if (client == null)
		{
			throw new IllegalArgumentException(
					"Constructor parameter 'client' in ServerClientThread::ServerClientThread cannot be null.");
		}

		this.client = client;
	}

	/**
	 * Starts the communication thread for the client that would keep listening
	 * for new data while it is still connected. This method should not be
	 * called explicitly as it automatically managed by the {@link Runnable}
	 * interface.
	 * 
	 * @see ServerClient
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
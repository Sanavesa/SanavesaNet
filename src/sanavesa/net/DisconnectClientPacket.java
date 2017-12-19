package sanavesa.net;

/**
 * The <code>DisconnectClientPacket</code> class is a special packet type that
 * is used internally to disconnect clients from the server.
 * 
 * @see NetworkSerializable
 * @see Server
 * @see Client
 * 
 * @since 1.0
 * @author Mohammad Alali
 */
final class DisconnectClientPacket implements NetworkSerializable
{
	/** The generated UID of the packet. */
	private static final long serialVersionUID = 6860219879225649234L;

	/**
	 * This method is invoked whenever the server requests for
	 * <code>client</code> to disconnect. This is called on the client's machine
	 * that was kicked by the server.
	 * 
	 * @param <T>
	 *            - the class type of the client who received this object
	 * @param client
	 *            - the client who got kicked by the server
	 * 
	 * @see Client
	 * 
	 * @since 1.1
	 * @author Mohammad Alali
	 */
	@Override
	public <T extends Client> void handleOnClient(T client)
	{
		// The server wants to disconnect a client
		client.disconnect(false);
	}

	/**
	 * This method is invoked whenever the <code>client</code> requests for
	 * <code>server</code> to disconnect. This is called on the server's
	 * machine.
	 * 
	 * @param <T>
	 *            - the class type of the server who received this object
	 * @param server
	 *            - the server who read this serializable object
	 * @param client
	 *            - the client who wants to disconnect
	 * 
	 * @see Server
	 * 
	 * @since 1.1
	 * @author Mohammad Alali
	 */
	@Override
	public <T extends Server> void handleOnServer(T server, ServerClient sender)
	{
		// The client requested the server to disconnect him
		sender.disconnect(false);
	}
}

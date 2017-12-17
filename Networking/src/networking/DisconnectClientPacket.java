package networking;

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
class DisconnectClientPacket implements NetworkSerializable
{
	/** The generated UID of the packet. */
	private static final long serialVersionUID = 6860219879225649234L;
}

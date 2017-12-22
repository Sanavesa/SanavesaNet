package sanavesa.example.chat;

import javafx.scene.paint.Color;
import sanavesa.net.Client;
import sanavesa.net.NetworkSerializable;
import sanavesa.net.Server;
import sanavesa.net.ServerClient;

/**
 * @author Mohammad Alali
 */
public class ExitRoomPacket implements NetworkSerializable
{
	private static final long serialVersionUID = -6505693869683604572L;
	private String name;
	
	public ExitRoomPacket(String name)
	{
		this.name = name;
	}
	
	@Override
	public <T extends Client> void handleOnClient(T client)
	{
		ClientGUI.addChatNotification(String.format("%s has disconnected!", name), Color.RED);
	}
	
	@Override
	public <T extends Server> void handleOnServer(T server, ServerClient sender)
	{
		// Relay to other clients
		server.sendDataToMatch(this, e -> e != sender);
	}
}

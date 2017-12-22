package sanavesa.example.chat;

import javafx.scene.paint.Color;
import sanavesa.net.Client;
import sanavesa.net.NetworkSerializable;
import sanavesa.net.Server;
import sanavesa.net.ServerClient;

/**
 * @author Mohammad Alali
 */
public class EnterRoomPacket implements NetworkSerializable
{
	private static final long serialVersionUID = -2703988943760091721L;
	private String name;
	
	public EnterRoomPacket(String name)
	{
		this.name = name;
	}
	
	@Override
	public <T extends Client> void handleOnClient(T client)
	{
		ClientGUI.addChatNotification(String.format("%s has connected!", name), Color.BLUE);
	}
	
	@Override
	public <T extends Server> void handleOnServer(T server, ServerClient sender)
	{
		// Relay to other clients
		server.sendDataToMatch(this, e -> e != sender);
	}
}

package sanavesa.example.chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javafx.scene.paint.Color;
import sanavesa.net.Client;
import sanavesa.net.NetworkSerializable;
import sanavesa.net.Server;
import sanavesa.net.ServerClient;

/**
 * @author Mohammad Alali
 */
public class ChatPacket implements NetworkSerializable
{
	private static final long serialVersionUID = 7990985051207632473L;
	private String name;
	private String text;
	
	public ChatPacket(String name, String text)
	{
		this.name = name;
		this.text = text;
	}

	@Override
	public <T extends Client> void handleOnClient(T client)
	{
		if(name.equals(ClientGUI.getLocalName()))
		{
			// Remote User Message
			ClientGUI.addChatMessage(name, text, Color.CORNFLOWERBLUE);
		}
		else
		{
			// Remote User Message
			ClientGUI.addChatMessage(name, text, Color.DARKSLATEBLUE);
		}
	}
	
	@Override
	public <T extends Server> void handleOnServer(T server, ServerClient sender)
	{
		ServerGUI.addChatMessage(name, text, Color.CORNFLOWERBLUE);
		server.sendDataToAll(this);
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeObject(name);
		out.writeObject(text);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		name  = (String) in.readObject();
		text = (String) in.readObject();
	}
}

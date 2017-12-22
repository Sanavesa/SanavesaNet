package sanavesa.example.chat;

import javafx.scene.paint.Color;
import sanavesa.net.Server;
import sanavesa.net.ServerClient;

/**
 * @author Mohammad Alali
 */
public class ChatServer extends Server
{
	//private HashMap<ServerClient, String> clientsNameHashMap = new HashMap<>();

	@Override
	protected void onClientConnected(ServerClient client)
	{
		ServerGUI.addChatNotification(
				String.format("Client (%s:%d) has connected!", client.getConnectionIP(), client.getConnectionPort()),
				Color.DARKORCHID);
	}

	@Override
	protected void onClientDisconnected(ServerClient client)
	{
		ServerGUI.addChatNotification(
				String.format("Client (%s:%d) has disconnected!", client.getConnectionIP(), client.getConnectionPort()),
				Color.DARKORCHID);
	}

	@Override
	protected void onServerFailedToStart(int port)
	{
		ServerGUI.addChatNotification(String.format("Server failed to start on port %d!", port), Color.RED);

		ServerGUI.getStartServerButton().setDisable(false);
		ServerGUI.getStopServerButton().setDisable(true);
	}

	@Override
	protected void onServerStopped(int port)
	{
		ServerGUI.addChatNotification(String.format("Server stopped on port %d!", port), Color.BLACK);

		ServerGUI.getStartServerButton().setDisable(false);
		ServerGUI.getStopServerButton().setDisable(true);
	}

	@Override
	protected void onServerStarted(int port)
	{
		ServerGUI.addChatNotification(String.format("Server started on port %d!", port), Color.BLACK);

		ServerGUI.getStartServerButton().setDisable(true);
		ServerGUI.getStopServerButton().setDisable(false);
	}
}

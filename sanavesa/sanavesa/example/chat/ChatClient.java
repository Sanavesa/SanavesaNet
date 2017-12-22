package sanavesa.example.chat;

import javafx.scene.paint.Color;
import sanavesa.net.Client;

/**
 * @author Mohammad Alali
 */
public class ChatClient extends Client
{
	@Override
	protected void onConnected(String ipAddress, int port)
	{
		EnterRoomPacket packet = new EnterRoomPacket(ClientGUI.getLocalName());
		sendData(packet);
		
		ClientGUI.addChatNotification(String.format("You have connected to the server on %s:%d!", ipAddress, port),
				Color.BLUEVIOLET);

		ClientGUI.getConnectButton().setDisable(true);
		ClientGUI.getDisconnectButton().setDisable(false);
	}

	@Override
	protected void onDisconnectedLocally(String ipAddress, int port)
	{
		ExitRoomPacket packet = new ExitRoomPacket(ClientGUI.getLocalName());
		sendData(packet);
		
		ClientGUI.addChatNotification(String.format("You have disconnected from the server on %s:%d!", ipAddress, port),
				Color.BLUEVIOLET);

		ClientGUI.getConnectButton().setDisable(false);
		ClientGUI.getDisconnectButton().setDisable(true);
	}
	
	@Override
	protected void onDisconnectedRemotely(String ipAddress, int port)
	{
		ClientGUI.addChatNotification(String.format("You have been disconnected from the server on %s:%d!", ipAddress, port),
				Color.BLUEVIOLET);

		ClientGUI.getConnectButton().setDisable(false);
		ClientGUI.getDisconnectButton().setDisable(true);
	}

	@Override
	protected void onFailedToConnect(String ipAddress, int port)
	{
		ClientGUI.addChatNotification(String.format("Failed to connect to the server on %s:%d!", ipAddress, port), Color.RED);
	}
}

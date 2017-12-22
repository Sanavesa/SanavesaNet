package sanavesa.example.chat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import sanavesa.net.Client;

/**
 * @author Mohammad Alali
 */
public class ClientGUI extends Application
{
	private static final String IP = "127.0.0.1";
	private static final int PORT = 25565;
	private Pane root;
	private Scene scene;
	private Button connectButton;
	private Button disconnectButton;
	private ListView<ColoredText> chatListView;
	private TextField chatTextField;
	private ChatClient client;
	private static ClientGUI instance = null;
	private String localName;

	public static void main(String[] args)
	{
		launch(args);
	}

	@Override
	public void init() throws Exception
	{
		ClientGUI.instance = this;
		client = new ChatClient();
		Client.setTimeoutDuration(10);
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		root = new Pane();
		scene = new Scene(root, 390, 280);

		connectButton = new Button("Connect");
		connectButton.setOnAction(e -> client.connect(IP, PORT));
		connectButton.relocate(10, 10);
		connectButton.setPrefSize(90, 20);

		disconnectButton = new Button("Disconnect");
		disconnectButton.setOnAction(e -> client.disconnect());
		disconnectButton.relocate(120, 10);
		disconnectButton.setPrefSize(90, 20);
		disconnectButton.setDisable(true);

		chatListView = new ListView<ColoredText>();
		chatListView.relocate(10, 40);
		chatListView.setPrefSize(380, 200);
		chatListView.setCellFactory(lv -> new ListCell<ColoredText>()
		{
			@Override
			protected void updateItem(ColoredText item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item == null)
				{
					setText(null);
					setTextFill(null);
				}
				else
				{
					setText(item.getText());
					setTextFill(item.getColor());
				}
			}
		});

		chatTextField = new TextField("");
		chatTextField.relocate(10, 250);
		chatTextField.setPrefSize(380, 30);
		chatTextField.setPromptText("Enter chat text...");
		chatTextField.addEventHandler(KeyEvent.KEY_PRESSED, e ->
		{
			if(client.isConnected())
			{
				if (e.getCode() == KeyCode.ENTER && !chatTextField.getText().isEmpty())
				{
					// Send text message
					ChatPacket packet = new ChatPacket(localName, chatTextField.getText());
					client.sendData(packet);
					chatTextField.clear();
				}
			}
		});

		root.getChildren().addAll(connectButton, disconnectButton, chatListView, chatTextField);

		primaryStage.setScene(scene);
		primaryStage.setTitle("Chat Client");
		primaryStage.setResizable(false);
		primaryStage.show();
		
		showNameStage();
	}

	private void showNameStage()
	{
		Stage stage = new  Stage();
		Pane root = new Pane();
		Scene scene = new Scene(root, 240, 70);
		
		Label label = new Label("Username:");
		TextField nameField = new TextField("");
		Button button = new Button("Enter");
		
		label.relocate(10, 15);
		
		nameField.relocate(80, 10);
		nameField.setPromptText("Enter username...");
		nameField.addEventFilter(KeyEvent.KEY_PRESSED, e ->
		{
			if(e.getCode() == KeyCode.ENTER)
			{
				button.fire();
			}
		});
		
		button.relocate(185, 40);
		button.disableProperty().bind(nameField.textProperty().isEmpty());
		button.setOnAction(e ->
		{
			if(nameField.getText().isEmpty())
			{
				localName = "User" + (int)Math.ceil(Math.random()*1000);
			}
			else
			{
				localName = nameField.getText();
			}
			stage.close();
		});
		
		stage.setOnCloseRequest(e ->
		{
			button.fire();
		});
		
		root.getChildren().addAll(label, nameField, button);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setTitle("Username Entry");
		stage.showAndWait();
	}

	@Override
	public void stop() throws Exception
	{
		client.disconnect();
	}

	public static Button getConnectButton()
	{
		if (instance == null)
			return null;

		return instance.connectButton;
	}

	public static Button getDisconnectButton()
	{
		if (instance == null)
			return null;

		return instance.disconnectButton;
	}
	
	public static String getLocalName()
	{
		if(instance == null)
			return null;
		
		return instance.localName;
	}

	public static void addChatMessage(String name, String chat, Color color)
	{
		if (instance == null)
			return;

		Platform.runLater(() ->
		{
			ColoredText chatColoredText = new ColoredText(name + ": " + chat, color);
			instance.chatListView.getItems().add(chatColoredText);
			instance.chatListView.scrollTo(chatColoredText);
		});
	}

	public static void addChatNotification(String notification, Color color)
	{
		if (instance == null)
			return;

		Platform.runLater(() ->
		{
			ColoredText notificationColoredText = new ColoredText(notification, color);
			instance.chatListView.getItems().add(notificationColoredText);
			instance.chatListView.scrollTo(notificationColoredText);
		});
	}
}

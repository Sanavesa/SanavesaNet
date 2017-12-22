package sanavesa.example.chat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * @author Mohammad Alali
 */
public class ServerGUI extends Application
{
	private static final int PORT = 25565;
	private Pane root;
	private Scene scene;
	private Button startServerButton;
	private Button stopServerButton;
	private ListView<ColoredText> chatListView;
	private ChatServer server;
	private static ServerGUI instance = null;

	public static void main(String[] args)
	{
		launch(args);
	}

	@Override
	public void init() throws Exception
	{
		server = new ChatServer();
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		ServerGUI.instance = this;
		root = new Pane();
		scene = new Scene(root, 390, 240);

		startServerButton = new Button("Start Server");
		startServerButton.setOnAction(e -> server.startServer(PORT));
		startServerButton.relocate(10, 10);
		startServerButton.setPrefSize(90, 20);

		stopServerButton = new Button("Stop Server");
		stopServerButton.setOnAction(e -> server.stopServer());
		stopServerButton.relocate(120, 10);
		stopServerButton.setPrefSize(90, 20);
		stopServerButton.setDisable(true);

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

		root.getChildren().addAll(startServerButton, stopServerButton, chatListView);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Chat Server");
		primaryStage.setResizable(false);
		primaryStage.show();
	}

	@Override
	public void stop() throws Exception
	{
		server.stopServer();
	}

	public static Button getStartServerButton()
	{
		if (instance == null)
			return null;

		return instance.startServerButton;
	}

	public static Button getStopServerButton()
	{
		if (instance == null)
			return null;

		return instance.stopServerButton;
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

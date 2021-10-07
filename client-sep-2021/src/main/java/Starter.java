import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Starter extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent parent = FXMLLoader.load(getClass().getResource("login.fxml"));
		primaryStage.setScene(new Scene(parent));
		primaryStage.setTitle("Введите имя пользователя и пароль");
		primaryStage.show();
	}
}

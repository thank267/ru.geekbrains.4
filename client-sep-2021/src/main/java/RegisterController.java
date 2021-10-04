import com.geekbrains.Login;
import com.geekbrains.LoginType;
import com.geekbrains.user.User;
import com.geekbrains.utils.FileHelper;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class RegisterController implements Initializable {

	public TextField login;
	public TextField password;
	public TextField nick;
	public Button registerButton;
	public Label invalidRegister;
	private ObjectDecoderInputStream is;
	private ObjectEncoderOutputStream os;

	public void register() throws Exception {

		Login createLogin = new Login();
		createLogin.setType(LoginType.CREATE);

		User user = new User();
		user.setLogin(login.getText());
		user.setPassword(password.getText());
		user.setNickname(nick.getText());
		createLogin.setUser(user);
		os.writeObject(createLogin);
		os.flush();

	}

	public void loginButtonOnAction(ActionEvent event) throws IOException {
		Platform.runLater(() -> {
			Stage stage = (Stage) registerButton.getScene().getWindow();

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("login.fxml"));
			Parent root = null;
			try {
				root = loader.load();
				Scene scene = new Scene(root);

				stage.setScene(scene);
				stage.show();
			} catch (IOException e) {
				log.error("e=", e);
			}

		});

	}

	public void registerSuccess(User user) {

		Platform.runLater(() -> {
			Stage stage = (Stage) registerButton.getScene().getWindow();

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("file.fxml"));
			Parent root = null;
			try {
				root = loader.load();
				Scene scene = new Scene(root);

				FileController fileController = loader.getController();
				fileController.initData(user);

				stage.setScene(scene);
				stage.show();
			} catch (IOException e) {
				log.error("e=", e);
			}

		});
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {

			Socket socket = new Socket("localhost", 8189);
			is = new ObjectDecoderInputStream(socket.getInputStream(), FileHelper.getMaxLength().intValue());
			os = new ObjectEncoderOutputStream(socket.getOutputStream());

			Thread daemon = new Thread(() -> {
				try {
					while (true) {

						Login login = (Login) is.readObject();

						switch (login.getType()) {
							case OK: {
								log.info("Reg OK {}", login.getUser());

								registerSuccess(login.getUser());

								break;
							}
							case FAILED: {
								invalidRegister.setVisible(true);
								log.info("Reg FAILED {}", login.getUser());
								break;

							}
						}
					}
				} catch (Exception e) {
					log.error("exception while read from input stream", e);
				}
			});
			daemon.setDaemon(true);
			daemon.start();

		} catch (IOException ioException) {
			log.error("e=", ioException);
		}

		login.setOnMouseClicked(event -> {
			invalidRegister.setVisible(false);
		});

		password.setOnMouseClicked(event -> {
			invalidRegister.setVisible(false);
		});

		nick.setOnMouseClicked(event -> {
			invalidRegister.setVisible(false);
		});

		registerButton.disableProperty().bind(Bindings.createBooleanBinding(() -> login.getText().trim().isEmpty(), login.textProperty()).or(Bindings.createBooleanBinding(() -> password.getText().trim().isEmpty(), password.textProperty())).or(Bindings.createBooleanBinding(() -> nick.getText().trim().isEmpty(), nick.textProperty())));

	}

}

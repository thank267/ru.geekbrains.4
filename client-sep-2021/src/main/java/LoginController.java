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
public class LoginController implements Initializable {

    public TextField login;
    public TextField password;
    public Button registerButton;
    public Button loginButton;
    public Label invalidLogin;
    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;

    public void login() throws Exception {

        Login newLogin = new Login();
        newLogin.setType(LoginType.LOGIN);

        User user = new User();
        user.setLogin(login.getText());
        user.setPassword(password.getText());
        newLogin.setUser(user);

        os.writeObject(newLogin);
        os.flush();

    }

    public void registerButtonOnAction(ActionEvent event) throws IOException {
        Platform.runLater(() -> {
            Stage stage = (Stage) registerButton.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("register.fxml"));
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

    public void loginSuccess(User user) {

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
                                log.info("Auth OK {}", login.getUser());

                                loginSuccess(login.getUser());

                                break;
                            }
                            case FAILED: {
                                invalidLogin.setVisible(true);
                                log.info("Auth FAILED {}", login.getUser());
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
            invalidLogin.setVisible(false);
        });

        password.setOnMouseClicked(event -> {
            invalidLogin.setVisible(false);
        });

        password.setOnKeyTyped(event -> {
            invalidLogin.setVisible(false);
        });

        login.setOnKeyTyped(event -> {
            invalidLogin.setVisible(false);
        });

        loginButton.disableProperty().bind(Bindings.createBooleanBinding(() -> login.getText().trim().isEmpty(), login.textProperty()).or(Bindings.createBooleanBinding(() -> password.getText().trim().isEmpty(), password.textProperty())));

    }

}

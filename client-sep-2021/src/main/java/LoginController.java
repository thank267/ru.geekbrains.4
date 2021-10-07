import com.geekbrains.model.User;
import com.geekbrains.operation.LoginUserOperation;
import com.geekbrains.operation.UserOperation;
import com.geekbrains.service.AuthService;
import com.geekbrains.service.ListAuthService;
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
import java.util.Optional;
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
    private AuthService<User> authService;

    public void login() throws Exception {
        User user = new User();
        user.setLogin(login.getText());
        user.setPassword(password.getText());
        LoginUserOperation loginUser = new LoginUserOperation(user);

        os.writeObject(loginUser);
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

        Optional.ofNullable(user).ifPresentOrElse((u) -> {
            Platform.runLater(() -> {
                Stage stage = (Stage) registerButton.getScene().getWindow();

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("file.fxml"));
                Parent root = null;
                try {
                    root = loader.load();
                    Scene scene = new Scene(root);

                    FileController fileController = loader.getController();
                    fileController.initData(u);

                    stage.setScene(scene);
                    stage.setTitle(String.format("Вы вошли как %s (%s)", u.getLogin(), u.getNickname()));
                    stage.show();
                } catch (IOException e) {
                    log.error("e=", e);
                }

            });


        }, () ->{
            invalidLogin.setVisible(true);
        });


    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {

            authService = ListAuthService.getInstance();

            Socket socket = new Socket("localhost", 8189);
            is = new ObjectDecoderInputStream(socket.getInputStream(), FileHelper.getMaxLength().intValue());
            os = new ObjectEncoderOutputStream(socket.getOutputStream());

            Thread daemon = new Thread(() -> {
                try {
                    while (true) {

                        UserOperation login = (UserOperation) is.readObject();
                        log.info("login {}",login);
                        login.execute(authService);
                        loginSuccess(authService.getUser());

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

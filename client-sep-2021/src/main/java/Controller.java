import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.file.*;
import java.util.ResourceBundle;
import java.util.stream.Stream;

@Slf4j
public class Controller implements Initializable {

    private static final String APP_NAME = "client-sep-2021/";
    private static final String CLIENT_FILE_DIR = APP_NAME + "clientFileDir/";
    public TextField input;
    public ListView<File> clientView;
    public ListView<File> serverView;
    private ObjectInputStream is;
    private ObjectOutputStream os;

    public void send(ActionEvent actionEvent) throws Exception {
        String msg = input.getText();

        input.clear();
        File file = new File(msg);
        os.writeObject(file);
        os.flush();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            reloadFiles(CLIENT_FILE_DIR);
            Socket socket = new Socket("localhost", 8189);
            is = new ObjectInputStream(socket.getInputStream());
            os = new ObjectOutputStream(socket.getOutputStream());
            Thread daemon = new Thread(() -> {
                try {
                    while (true) {
                        File file = (File) is.readObject();
                        Platform.runLater(() -> serverView.getItems().add(file));
                    }
                } catch (Exception e) {
                    log.error("exception while read from input stream");
                }
            });
            daemon.setDaemon(true);
            daemon.start();

            Thread filesWatcher = new Thread(() -> {
                WatchService watchService = null;
                try {
                    watchService = FileSystems.getDefault().newWatchService();

                    Path path = Paths.get(CLIENT_FILE_DIR);

                    path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

                    WatchKey key;
                    while ((key = watchService.take()) != null) {
                        for (WatchEvent<?> event : key.pollEvents()) {
                            reloadFiles(CLIENT_FILE_DIR);
                        }
                        key.reset();
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            filesWatcher.setDaemon(true);
            filesWatcher.start();

        } catch (IOException ioException) {
            log.error("e=", ioException);
        }

        clientView.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent click) {

                if (click.getClickCount() == 2) {
                    //Use ListView's getSelected Item
                    File file = clientView.getSelectionModel().getSelectedItem();

                    input.setText(file.getPath());

                }
            }
        });

    }

    private void reloadFiles(String dir) {

        Platform.runLater(() -> {
            clientView.getItems().clear();
            Stream.of(new File(dir).listFiles()).filter(file -> !file.isDirectory()).forEach(file -> clientView.getItems().add(file));
            clientView.refresh();
        });

    }
}

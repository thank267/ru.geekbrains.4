import com.geekbrains.model.Command;
import com.geekbrains.operation.*;
import com.geekbrains.model.User;
import com.geekbrains.utils.FileHelper;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public class FileController {

	private final String APP_NAME = "client-sep-2021";
	private final String ROOT_DIR = "client";

	public TreeView<File> clientView;
	public TreeView<File> serverView;
	public Label up;
	public Label down;
	public Button logoutButton;
	public Button uploadButton;
	public Button downloadButton;
	private ObjectDecoderInputStream is;
	private ObjectEncoderOutputStream os;
	private FileHelper fileHelper;
	private User loggedUser;

	//TODO create dir on server and client

	public void upload(File file, File dst) throws Exception {
		Command sendFile = new Command();
		if (!file.isDirectory()) {

			sendFile.addFile(file);
			sendFile.setDst(dst);
			sendFile.setStart(true);

			os.writeObject(new FileMassageOperation(sendFile));
			os.flush();

			InputStream inputStream = new FileInputStream(file);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

			byte[] buffer = new byte[(int) Math.min(FileHelper.getMaxLength(), file.length())];
			int read;

			while ((read = bufferedInputStream.read(buffer, 0, buffer.length)) != -1) {

				sendFile.addFile(file);
				sendFile.setDst(dst);
				sendFile.setData(buffer);
				sendFile.setStart(false);

				os.writeObject(new FileMassageOperation(sendFile));
				os.flush();
			}

		} else {
			sendFile.addFile(file);
			sendFile.setDst(dst);
			os.writeObject(new DirMassageOperation(sendFile));
			os.flush();
		}

	}

	public void download(File file, File dst) throws Exception {
		Command sendFile = new Command();
		sendFile.addFile(file);
		sendFile.setDst(dst);
		os.writeObject(new FileRequestOperation(sendFile));
		os.flush();
	}

	public void getList() throws IOException {

		Command getList = new Command();
		getList.setDst(new File(loggedUser.getLogin()));
		os.writeObject(new ListRequestOperation(getList));
		os.flush();
	}

	public void logout() throws IOException {

		Platform.runLater(() -> {
			Stage stage = (Stage) logoutButton.getScene().getWindow();

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("login.fxml"));
			Parent root = null;
			try {
				root = loader.load();
				Scene scene = new Scene(root);

				stage.setScene(scene);
				stage.setTitle("Введите имя пользователя и пароль");
				stage.show();
			} catch (IOException e) {
				log.error("e=", e);
			}

		});


	}



	public void initData(User loggedUser) {
		this.loggedUser = loggedUser;
		try {

			log.info("Logged User: {}", loggedUser);


			fileHelper = FileHelper.getClientInstance(APP_NAME, ROOT_DIR, serverView);

			fileHelper.reloadFiles(fileHelper.readDir(""), clientView);

			Thread filesWatcher = new Thread(() -> {
				WatchService watchService = null;
				try {
					watchService = FileSystems.getDefault().newWatchService();

					Path path = Paths.get(fileHelper.getRootDir());

					path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
					//TODO watching the file structure
					WatchKey key;
					while ((key = watchService.take()) != null) {

						for (WatchEvent<?> event : key.pollEvents()) {

							fileHelper.reloadFiles(fileHelper.readDir(""), clientView);

						}
						key.reset();
					}
				} catch (IOException | InterruptedException e) {
					log.error("e=", e);
				}
			});
			filesWatcher.setDaemon(true);
			filesWatcher.start();

			Socket socket = new Socket("localhost", 8189);
			is = new ObjectDecoderInputStream(socket.getInputStream(), FileHelper.getMaxLength().intValue());
			os = new ObjectEncoderOutputStream(socket.getOutputStream());

			Thread daemon = new Thread(() -> {
				try {
					while (true) {

						CommandOperation command = (CommandOperation) is.readObject();

						command.execute(fileHelper);


					}
				} catch (Exception e) {
					log.error("exception while read from input stream", e);
				}
			});
			daemon.setDaemon(true);
			daemon.start();

			getList();

		} catch (IOException ioException) {
			log.error("e=", ioException);
		}

		clientView.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) {

				if (Stream.of(Optional.ofNullable(clientView.getSelectionModel().getSelectedItem()), Optional.ofNullable(serverView.getSelectionModel().getSelectedItem())).filter(el -> el.isPresent()).count() == 2) {

					try {
						upload(clientView.getSelectionModel().getSelectedItem().getValue(), serverView.getSelectionModel().getSelectedItem().getValue());
					} catch (Exception e) {
						log.error("e=", e);
					}
				}

			}
			Optional.ofNullable(clientView.getSelectionModel().getSelectedItem()).ifPresent(el -> {
				down.setText("Выбранная директория: " + (el.getValue().isDirectory() ? el.getValue().getName() : el.getValue().getParentFile().getName()));

			});
		});

		serverView.setOnMouseClicked(event -> {

			Optional.ofNullable(serverView.getSelectionModel().getSelectedItem()).ifPresent(el -> {
				up.setText("Выбранная директория: " + (el.getValue().isDirectory() ? el.getValue().getName() : el.getValue().getParentFile().getName()));

			});
		});

		uploadButton.setOnMouseClicked(event -> {

			if (Stream.of(Optional.ofNullable(clientView.getSelectionModel().getSelectedItem()), Optional.ofNullable(serverView.getSelectionModel().getSelectedItem())).filter(el -> el.isPresent()).count() == 2) {
				try {
					upload(clientView.getSelectionModel().getSelectedItem().getValue(), serverView.getSelectionModel().getSelectedItem().getValue());
				} catch (Exception e) {
					log.error("e=", e);
				}
			}

		});

		downloadButton.setOnMouseClicked(event -> {

			if (Stream.of(Optional.ofNullable(clientView.getSelectionModel().getSelectedItem()), Optional.ofNullable(serverView.getSelectionModel().getSelectedItem())).filter(el -> el.isPresent()).count() == 2) {
				try {
					download(serverView.getSelectionModel().getSelectedItem().getValue(), clientView.getSelectionModel().getSelectedItem().getValue());
				} catch (Exception e) {
					log.error("e=", e);
				}
			}

		});
	}
}

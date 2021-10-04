import com.geekbrains.Command;
import com.geekbrains.CommandType;
import com.geekbrains.user.User;
import com.geekbrains.utils.FileHelper;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.List;
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
	public Label loggedUserLabel;
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

			sendFile.setType(CommandType.FILE_MESSAGE);
			sendFile.addFile(file);
			sendFile.setDst(dst);
			sendFile.setStart(true);

			os.writeObject(sendFile);
			os.flush();

			InputStream inputStream = new FileInputStream(file);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

			byte[] buffer = new byte[(int) Math.min(FileHelper.getMaxLength(), file.length())];
			int read;

			while ((read = bufferedInputStream.read(buffer, 0, buffer.length)) != -1) {

				sendFile.setType(CommandType.FILE_MESSAGE);
				sendFile.addFile(file);
				sendFile.setDst(dst);
				sendFile.setData(buffer);
				sendFile.setStart(false);

				os.writeObject(sendFile);
				os.flush();
			}

		} else {
			sendFile.setType(CommandType.DIR_MESSAGE);
			sendFile.addFile(file);
			sendFile.setDst(dst);
			os.writeObject(sendFile);
			os.flush();
		}

	}

	public void download(File file, File dst) throws Exception {
		Command sendFile = new Command();
		sendFile.setType(CommandType.FILE_REQUEST);
		sendFile.addFile(file);
		sendFile.setDst(dst);
		os.writeObject(sendFile);
		os.flush();
	}

	public void getList() throws IOException {

		Command getList = new Command();
		getList.setDst(new File(loggedUser.getLogin()));
		getList.setType(CommandType.LIST_REQUEST);
		os.writeObject(getList);
		os.flush();
	}

	private void reloadFiles(List<File> files, TreeView<File> root) {
		Platform.runLater(() -> {

			if (files.size() == 0) {
				root.setRoot(new TreeItem<File>());
				root.refresh();
				return;
			}

			File parent = files.get(0);

			TreeItem<File> dir = new TreeItem<File>(parent);
			dir.setExpanded(true);

			if (files.size() == 1) {
				root.setRoot(dir);
				root.refresh();
				return;
			}

			// TODO: in a functional style  in a functional style if possible
			TreeItem<File> tmp = new TreeItem<File>();
			for (int i = 1; i < files.size(); i++) {

				File file = files.get(i);

				if (file.isDirectory()) {
					if (file.getParentFile().equals(parent)) {
						tmp = new TreeItem<File>(file);
						tmp.setExpanded(true);
						dir.getChildren().add(tmp);
					} else {
						TreeItem<File> tmp1 = new TreeItem<File>(file);
						tmp1.setExpanded(true);
						tmp.getChildren().add(tmp1);
						tmp = tmp1;
					}

				} else {

					if (file.getParentFile().equals(parent)) {
						dir.getChildren().add(new TreeItem<File>(file));
					} else {
						tmp.getChildren().add(new TreeItem<File>(file));
					}

				}

			}

			root.setRoot(dir);
			root.refresh();

		});

	}

	public void initData(User loggedUser) {
		this.loggedUser = loggedUser;
		try {

			log.info("Logged User: {}", loggedUser);

			loggedUserLabel.setText(String.format("Вы вошли как %s (%s)", loggedUser.getLogin(), loggedUser.getNickname()));

			fileHelper = FileHelper.getInstance(APP_NAME, ROOT_DIR);

			reloadFiles(fileHelper.readDir(""), clientView);

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

							reloadFiles(fileHelper.readDir(""), clientView);

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

						Command command = (Command) is.readObject();

						switch (command.getType()) {
							case LIST_RESPONSE: {
								reloadFiles(command.getFiles(), serverView);
								break;
							}
							case FILE_MESSAGE: {
								fileHelper.writeFile(command);
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

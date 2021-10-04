package com.geekbrains.utils;

import com.geekbrains.Command;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class FileHelper {

	private static final long MAX_LENGTH = 1048576 / 2;
	private static FileHelper instance;
	private final String APP_NAME;
	private final String ROOT_DIR;

	private FileHelper(String basePath, String root) {
		APP_NAME = basePath;
		ROOT_DIR = String.format("%s/%s/", APP_NAME, root);
		createrDir(ROOT_DIR);

	}

	public static Long getMaxLength() {
		return MAX_LENGTH;
	}

	public static FileHelper getInstance(String basePath, String root) {
		if (instance == null) return new FileHelper(basePath, root);
		return instance;
	}

	public String getRootDir() {
		return ROOT_DIR;
	}

	public void createrDir(String dirName) {

		File dir = new File(dirName);
		if (!dir.exists()) {

			dir.mkdir();
		}
	}

	public void writeFile(Command command) throws IOException {
		Path ROOT = command.getDst().isDirectory() ? command.getDst().toPath() : command.getDst().getParentFile().toPath();

		if (command.isStart()) {
			Files.deleteIfExists(ROOT.resolve(command.getFiles().get(0).getName()));
		} else {
			Files.write(ROOT.resolve(command.getFiles().get(0).getName()), command.getData(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		}

	}

	public byte[] readFile(Command command) throws IOException {
		Path path = Paths.get(ROOT_DIR, command.getFiles().get(0).getName());
		return Files.readAllBytes(path);

	}

	public List<File> readDir(String dirName) throws IOException {
		Path path = Paths.get(ROOT_DIR, dirName);

		Files.createDirectories(path);

		return Files.walk(path).sorted(Comparator.comparingInt(p -> p.toFile().isDirectory() ? p.getNameCount() : p.getParent().getNameCount())).map(p -> p.toFile()).collect(Collectors.toList());

	}

}

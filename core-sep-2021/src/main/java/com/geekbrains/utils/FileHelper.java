package com.geekbrains.utils;

import com.geekbrains.Command;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class FileHelper {

	private static final int MAX_LENGTH = Integer.MAX_VALUE;
	private static FileHelper instance;
	private final String APP_NAME;
	private final String ROOT_DIR;

	private FileHelper(String basePath, String root) {
		APP_NAME = basePath;
		ROOT_DIR = String.format("%s/%s/", APP_NAME, root);
		createrDir(ROOT_DIR);

	}

	public static Integer getMaxLength() {
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

		Files.write(ROOT.resolve(command.getFiles().get(0).getName()), command.getData());
	}

	public byte[] readFile(Command command) throws IOException {
		Path path = Paths.get(ROOT_DIR, command.getFiles().get(0).getName());
		return Files.readAllBytes(path);

	}

	public List<File> readDir(String dirName) throws IOException {
		Path path = Paths.get(ROOT_DIR, dirName);

		return Files.walk(path).sorted(Comparator.comparingInt(p -> p.toFile().isDirectory() ? p.getNameCount() : p.getParent().getNameCount())).map(p -> p.toFile()).collect(Collectors.toList());

	}

}

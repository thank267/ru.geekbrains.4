package com.geekbrains.nio.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileHelper {

	private static final String APP_NAME = "server-sep-2021/";
	private static final String ROOT_DIR = APP_NAME + "root/";

	static {
		createServerDir(ROOT_DIR);
	}

	public static void createServerDir(String dirName) {
		File dir = new File(APP_NAME + dirName);
		if (!dir.exists()) {
			dir.mkdir();
		}
	}

	public static String readFile(String fileName) {
		Path path = Paths.get(ROOT_DIR,fileName);
		try {
			Stream<String> lines = Files.lines(path);
			String data = lines.collect(Collectors.joining("\n"));
			lines.close();
			return data;
		} catch (Exception e) {
			return e.getClass().getName()+" "+path.toFile().getAbsolutePath();
		}
	}

	public static String readDir(String dirName) {
		Path path = Paths.get(ROOT_DIR,dirName);
		try {
			return Files.walk(path)
	                .skip(1)
					.flatMap(p -> Stream.of(p.toFile().getName()))
					.collect(Collectors.joining("\n"));
		} catch (Exception e) {
			return e.getClass().getName()+" "+path.toFile().getAbsolutePath();
		}
	}




}

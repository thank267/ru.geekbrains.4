package com.geekbrains;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class Command implements Serializable {

	private CommandType type;

	private List<File> files = new ArrayList<>();

	private File dst;

	private byte[] data;

	private boolean start = false;

	public void addFile(File file) {
		files.add(file);
	}

}

package com.geekbrains.operation;

import com.geekbrains.model.Command;
import com.geekbrains.utils.FileHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
public class FileMassageOperation implements CommandOperation{

	private Command command;

	@Override
	public CommandOperation execute(FileHelper fileHelper) {
		Command response = new Command();
		try {
			fileHelper.writeFile(command);
			response.setFiles(fileHelper.readDir(command.getDst().getName()));
		} catch (IOException e) {
			log.error(e.getMessage(),e);
			throw new RuntimeException(e);
		}

		return new ListResponseOperation(response);
	}
}

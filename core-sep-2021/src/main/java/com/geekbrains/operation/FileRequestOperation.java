package com.geekbrains.operation;

import com.geekbrains.model.Command;
import com.geekbrains.utils.FileHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
public class FileRequestOperation implements CommandOperation{

	private Command command;

	@Override
	public CommandOperation execute(FileHelper fileHelper) {


		Command response = new Command();
		response.addFile(command.getFiles().get(0));
		response.setDst(command.getDst());
		try {
			response.setData(fileHelper.readFile(command));
		} catch (IOException e) {
			log.error(e.getMessage(),e);
			throw new RuntimeException(e);
		}

		return new FileMassageOperation(response);
	}
}

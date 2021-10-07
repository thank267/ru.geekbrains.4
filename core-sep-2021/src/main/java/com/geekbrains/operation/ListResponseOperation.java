package com.geekbrains.operation;

import com.geekbrains.model.Command;
import com.geekbrains.utils.FileHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ListResponseOperation implements CommandOperation{

	private Command command;

	@Override
	public CommandOperation execute(FileHelper fileHelper) {
		fileHelper.produceList(command);
		return null;
	}
}

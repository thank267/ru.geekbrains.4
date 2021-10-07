package com.geekbrains.operation;

import com.geekbrains.utils.FileHelper;

import java.io.Serializable;

@FunctionalInterface
public interface CommandOperation extends Serializable {
	CommandOperation execute(FileHelper fileHelper);
}

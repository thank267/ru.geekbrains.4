package com.geekbrains.netty;

import com.geekbrains.Command;
import com.geekbrains.CommandType;
import com.geekbrains.utils.FileHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;

@Slf4j
public class FileMessageHandler extends SimpleChannelInboundHandler<Command> {

	private final FileHelper fileHelper;

	public FileMessageHandler(FileHelper fileHelper) {
		this.fileHelper = fileHelper;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Command cmd) throws Exception {

		switch (cmd.getType()) {

			case FILE_MESSAGE: {
				fileHelper.writeFile(cmd);
				Command response = new Command();
				response.setType(CommandType.LIST_RESPONSE);
				response.setFiles(fileHelper.readDir(""));
				ctx.writeAndFlush(response);
				break;
			}
			case DIR_MESSAGE: {
				fileHelper.createrDir(Paths.get(fileHelper.getRootDir(), cmd.getFiles().get(0).getName()).toFile().getAbsolutePath());
				Command response = new Command();
				response.setType(CommandType.LIST_RESPONSE);
				response.setFiles(fileHelper.readDir(""));
				ctx.writeAndFlush(response);
				break;
			}

			case FILE_REQUEST: {

				Command response = new Command();
				response.setType(CommandType.FILE_MESSAGE);
				response.addFile(cmd.getFiles().get(0));
				response.setDst(cmd.getDst());
				response.setData(fileHelper.readFile(cmd));
				ctx.writeAndFlush(response);
				break;
			}

			case LIST_REQUEST: {

				Command response = new Command();
				response.setType(CommandType.LIST_RESPONSE);
				response.setFiles(fileHelper.readDir(""));
				ctx.writeAndFlush(response);
				break;
			}

		}

	}
}

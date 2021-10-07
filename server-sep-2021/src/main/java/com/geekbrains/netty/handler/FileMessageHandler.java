package com.geekbrains.netty.handler;

import com.geekbrains.operation.CommandOperation;
import com.geekbrains.utils.FileHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileMessageHandler extends SimpleChannelInboundHandler<CommandOperation> {

	private final FileHelper fileHelper;

	public FileMessageHandler(FileHelper fileHelper) {
		this.fileHelper = fileHelper;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, CommandOperation cmd) throws Exception {

		ctx.writeAndFlush(cmd.execute(fileHelper));

		//switch (cmd.getType()) {

//			case FILE_MESSAGE: {
//				fileHelper.writeFile(cmd);
//				Command response = new Command();
//				response.setType(CommandType.LIST_RESPONSE);
//				response.setFiles(fileHelper.readDir(cmd.getDst().getName()));
//				ctx.writeAndFlush(response);
//				break;
//			}
//			case DIR_MESSAGE: {
//				fileHelper.createrDir(Paths.get(fileHelper.getRootDir(), cmd.getDst().getName(), cmd.getFiles().get(0).getName()).toFile().getAbsolutePath());
//				Command response = new Command();
//				response.setType(CommandType.LIST_RESPONSE);
//				response.setFiles(fileHelper.readDir(cmd.getDst().getName()));
//				ctx.writeAndFlush(response);
//				break;
//			}

//			case FILE_REQUEST: {
//
//				Command response = new Command();
//				response.setType(CommandType.FILE_MESSAGE);
//				response.addFile(cmd.getFiles().get(0));
//				response.setDst(cmd.getDst());
//				response.setData(fileHelper.readFile(cmd));
//				ctx.writeAndFlush(response);
//				break;
//			}

//			case LIST_REQUEST: {
//
//				Command response = new Command();
//				response.setType(CommandType.LIST_RESPONSE);
//				response.setFiles(fileHelper.readDir(cmd.getDst().getName()));
//				ctx.writeAndFlush(response);
//				break;
//			}
//
//		}

	}
}

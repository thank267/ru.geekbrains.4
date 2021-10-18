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


	}
}

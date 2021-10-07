package com.geekbrains.netty.handler;

import com.geekbrains.service.AuthService;
import com.geekbrains.model.User;
import com.geekbrains.operation.UserOperation;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class UserAuthHandler extends SimpleChannelInboundHandler<UserOperation> {

	private final AuthService<User> authService;

	public UserAuthHandler(AuthService<User> authService) {
		this.authService = authService;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, UserOperation userOperation) throws Exception {
		ctx.writeAndFlush(userOperation.execute(authService));

	}
}

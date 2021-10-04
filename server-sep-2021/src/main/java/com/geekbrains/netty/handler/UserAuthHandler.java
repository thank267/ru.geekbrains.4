package com.geekbrains.netty.handler;

import com.geekbrains.Login;
import com.geekbrains.LoginType;
import com.geekbrains.netty.service.AuthService;
import com.geekbrains.user.User;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Optional;

public class UserAuthHandler extends SimpleChannelInboundHandler<Login> {

	private final AuthService<User> authService;

	public UserAuthHandler(AuthService<User> authService) {
		this.authService = authService;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Login login) throws Exception {

		Login response = new Login();

		switch (login.getType()) {

			case LOGIN: {
				Optional<User> optionalUser = authService.findByLoginAndPassword(login.getUser().getLogin(), login.getUser().getPassword());

				if (optionalUser.isPresent()) {
					response.setType(LoginType.OK);
					response.setUser(optionalUser.get());
				} else {
					response.setType(LoginType.FAILED);
				}
				ctx.writeAndFlush(response);
				break;
			}

			case CREATE: {
				Optional<User> optionalUser = authService.save(login.getUser());

				if (optionalUser.isPresent()) {
					response.setType(LoginType.OK);
					response.setUser(optionalUser.get());
				} else {
					response.setType(LoginType.FAILED);
				}
				ctx.writeAndFlush(response);
				break;
			}

		}

	}
}

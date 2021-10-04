package com.geekbrains.netty;

import com.geekbrains.netty.handler.FileMessageHandler;
import com.geekbrains.netty.handler.UserAuthHandler;
import com.geekbrains.netty.service.AuthService;
import com.geekbrains.netty.service.ListAuthService;
import com.geekbrains.user.User;
import com.geekbrains.utils.FileHelper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyEchoServer {

	private final String APP_NAME = "server-sep-2021";
	private final String ROOT_DIR = "root";
	private final FileHelper fileHelper = FileHelper.getInstance(APP_NAME, ROOT_DIR);

	private final AuthService<User> authService = ListAuthService.getInstance();

	public NettyEchoServer() {

		EventLoopGroup auth = new NioEventLoopGroup(1);
		EventLoopGroup worker = new NioEventLoopGroup();

		try {
			ServerBootstrap bootstrap = new ServerBootstrap();

			ChannelFuture channelFuture = bootstrap.group(auth, worker).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel channel) throws Exception {

					channel.pipeline().addLast(new ObjectEncoder(), new ObjectDecoder(ClassResolvers.cacheDisabled(null)), new FileMessageHandler(fileHelper), new UserAuthHandler(authService));
				}
			}).bind(8189).sync();
			log.debug("Server started...");
			channelFuture.channel().closeFuture().sync(); // block
		} catch (Exception e) {
			log.error("Server exception: Stacktrace: ", e);
		} finally {
			auth.shutdownGracefully();
			worker.shutdownGracefully();
		}
	}

	public static void main(String[] args) {
		new NettyEchoServer();
	}

}

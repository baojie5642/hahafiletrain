package com.baojie.hahafiletrain.Netty.TheNettyServer;

import com.baojie.hahafiletrain.Netty.Handler.ServerHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;



public class NettyServer {
	private final int port;

	private NettyServer(final int port) {
		super();
		this.port = port;
	}

	public static NettyServer init(final int port) {
		NettyServer nettyServer = null;
		try {
			nettyServer = new NettyServer(port);
			return nettyServer;
		} finally {
		}
	}

	public void bind() throws Exception {
		EventLoopGroup bossGroup = null;
		EventLoopGroup workeGroup = null;
		ServerBootstrap b = null;
		try {
			bossGroup = new NioEventLoopGroup();
			workeGroup = new NioEventLoopGroup();
			b = new ServerBootstrap();
			b.group(bossGroup, workeGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 100)
					.handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChildChannelHandler());
			ChannelFuture f = b.bind(port).sync();
			System.out.println("Start file server at port : " + port);
			f.channel().closeFuture().sync();
		} finally {
			if (null != bossGroup) {
				bossGroup.shutdownGracefully();
				bossGroup = null;
			}
			if (null != workeGroup) {
				workeGroup.shutdownGracefully();
				workeGroup = null;
			}
		}
	}

	private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
			ch.pipeline().addLast(new ByteArrayEncoder());
			ch.pipeline().addLast(new ByteArrayDecoder());
			ch.pipeline().addLast(new ServerHandler());
		}
	}

	public static void main(String[] args) throws Exception {
		int port = 8080;
		if (args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
			}
		}
		NettyServer.init(port).bind();
	}
}

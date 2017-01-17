package com.baojie.hahafiletrain.TransMessage.Server;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import com.baojie.hahafiletrain.ThreadAll.StaticForAllThreadPool;
import com.baojie.hahafiletrain.TransMessage.Server.Handler.MessageServerHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

public class NettyServerForMessage {

	private static final byte[] byteLock = new byte[0];

	private final AtomicReference<NioEventLoopGroup> eventLoopBossGroup = new AtomicReference<NioEventLoopGroup>(null);

	private final AtomicReference<NioEventLoopGroup> eventLoopWorkeGroup = new AtomicReference<NioEventLoopGroup>(null);

	private final AtomicReference<ServerBootstrap> serverBootstrap = new AtomicReference<ServerBootstrap>(null);

	private NettyServerForMessage() {
		super();
		init();
	}

	public static NettyServerForMessage createAndInitServer() {
		NettyServerForMessage nettyServerForMessage = new NettyServerForMessage();
		return nettyServerForMessage;
	}

	private void init() {
		synchronized (byteLock) {
			if (null != eventLoopBossGroup) {
				return;
			}
			if (null != eventLoopWorkeGroup) {
				return;
			}
			if (null != serverBootstrap) {
				return;
			}
			eventLoopBossGroup.set(new NioEventLoopGroup(1, StaticForAllThreadPool.ParentNioEventLoopThreadPool));
			eventLoopWorkeGroup.set(new NioEventLoopGroup(1, StaticForAllThreadPool.ChildNioEventLoopThreadPool));
			serverBootstrap.set(new ServerBootstrap());
			serverBootstrap.get().group(eventLoopBossGroup.get(), eventLoopWorkeGroup.get());
			serverBootstrap.get().channel(NioServerSocketChannel.class);
			serverBootstrap.get().option(ChannelOption.TCP_NODELAY, true);
			serverBootstrap.get().option(ChannelOption.SO_BACKLOG, 8096);
			serverBootstrap.get().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);
			serverBootstrap.get().childHandler(new ServerChildChannelHandler());
		}
	}

	private static class ServerChildChannelHandler extends ChannelInitializer<SocketChannel> {
		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
			ch.pipeline().addLast(new ByteArrayDecoder());
			ch.pipeline().addLast(new ByteArrayEncoder());
			ch.pipeline().addLast(MessageServerHandler.createMessageServerHandler());
		}
	}

	public void bindSync(final int port) throws Exception {
		ChannelFuture channelFuture = null;
		try {
			channelFuture = serverBootstrap.get().bind(port).sync();
			System.out.println("server has start up by Sync……");
			channelFuture.channel().closeFuture().sync();
		} finally {
			eventLoopBossGroup.get().shutdownGracefully();
			eventLoopWorkeGroup.get().shutdownGracefully();
		}
	}

	public void bindUninterruptibly(final int port) throws Exception {
		ChannelFuture channelFuture = null;
		try {
			channelFuture = serverBootstrap.get().bind(port).sync();
			System.out.println("server has start up by Uninterruptibly……");
			channelFuture.channel().closeFuture().awaitUninterruptibly();
		} finally {
			eventLoopBossGroup.get().shutdownGracefully();
			eventLoopWorkeGroup.get().shutdownGracefully();
		}
	}

	// can do nothing
	public void closeServer() {
		synchronized (byteLock) {
			serverBootstrap.set(null);
			NioEventLoopGroup nioEventLoopGroup0 = eventLoopBossGroup.get();
			nioEventLoopGroup0.shutdownGracefully();
			nioEventLoopGroup0 = null;
			eventLoopBossGroup.set(null);
			NioEventLoopGroup nioEventLoopGroup1 = eventLoopWorkeGroup.get();
			nioEventLoopGroup1.shutdownGracefully();
			nioEventLoopGroup1 = null;
			eventLoopWorkeGroup.set(null);
		}
	}

	public static void main(String[] args) throws Exception {
		NettyServerForMessage nettyServerForMessage = NettyServerForMessage.createAndInitServer();
		nettyServerForMessage.bindUninterruptibly(8080);
		LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(6, TimeUnit.SECONDS));
		nettyServerForMessage.closeServer();
	}

}

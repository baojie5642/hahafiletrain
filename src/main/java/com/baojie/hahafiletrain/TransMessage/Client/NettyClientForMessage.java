package com.baojie.hahafiletrain.TransMessage.Client;

import java.util.concurrent.locks.ReentrantLock;

import com.baojie.hahafiletrain.MessageInfo.LocalUseMessage;
import com.baojie.hahafiletrain.TransMessage.Client.Handler.MessageClientHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;


public class NettyClientForMessage {

	private static final byte[] byteLock = new byte[0];
	private final LocalUseMessage localUseMessage;
	private final ReentrantLock mainLock = new ReentrantLock();
	private final EventLoopGroup group = new NioEventLoopGroup();
	private final Bootstrap b = new Bootstrap();
	private ChannelFuture channelFuture = null;
	private static final byte[] lock = new byte[0];

	public static NettyClientForMessage createAndInitClient(final LocalUseMessage localUseMessage) {
		NettyClientForMessage client = new NettyClientForMessage(localUseMessage);
		return client;
	}

	private NettyClientForMessage(final LocalUseMessage localUseMessage) {
		super();
		this.localUseMessage = localUseMessage;
		initClient();
	}

	private void initClient() {
		synchronized (lock) {
			b.group(group);
			b.channel(NioSocketChannel.class);
			b.option(ChannelOption.TCP_NODELAY, true);
			b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000);
			b.handler(new ClientChildChannelHandler());
		}
	}

	private class ClientChildChannelHandler extends ChannelInitializer<SocketChannel> {
		@Override
		protected void initChannel(final SocketChannel ch) throws Exception {
			ch.pipeline().addLast(new LengthFieldPrepender(4));
			ch.pipeline().addLast(new ByteArrayEncoder());
			ch.pipeline().addLast(new ByteArrayDecoder());
			ch.pipeline().addLast(MessageClientHandler.createClientHandler(localUseMessage));
		}
	}

	public void connectUninterruptibly(final int port, final String host)  throws Exception{
		final ReentrantLock lock = mainLock;
		lock.lock();
		try {
			channelFuture = b.connect(host, port).sync();
			channelFuture.channel().closeFuture().awaitUninterruptibly();
		} finally {
			lock.unlock();
		}
	}

	public void connectSync(final int port, final String host) throws Exception {
		final ReentrantLock lock = mainLock;
		lock.lock();
		try {
			channelFuture = b.connect(host, port).sync();
			channelFuture.channel().closeFuture().sync();
		} finally {
			lock.unlock();
		}
	}

	public void closeClient() {
		final ReentrantLock lock = mainLock;
		synchronized (byteLock) {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
		final ChannelFuture channelF=channelFuture;
		channelFuture = null;
		group.shutdownGracefully();
		channelF.cancel(true);
	}

	public LocalUseMessage getLocalUseMessage() {
		return localUseMessage;
	}

}

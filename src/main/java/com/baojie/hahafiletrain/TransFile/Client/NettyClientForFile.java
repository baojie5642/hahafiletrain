package com.baojie.hahafiletrain.TransFile.Client;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import com.baojie.hahafiletrain.FileInfo.FileInformation;
import com.baojie.hahafiletrain.ThreadAll.StaticForAllThreadPool;
import com.baojie.hahafiletrain.TransFile.Client.Handler.FileClientHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;


public class NettyClientForFile implements Runnable {
	private static final byte[] byteLock = new byte[0];
	private static final Object objectLock = new Object();
	private final FileInformation fileInformation;
	private final AtomicBoolean isRestart = new AtomicBoolean(true);
	private final AtomicBoolean isCanstart = new AtomicBoolean(true);
	private final int port;
	private final String host;

	private final AtomicReference<NioEventLoopGroup> nioEventLoopGroupgroup = new AtomicReference<NioEventLoopGroup>(
			null);
	private final AtomicReference<Bootstrap> bootstrap = new AtomicReference<Bootstrap>(null);

	private final AtomicReference<ChannelFuture> channelFuture = new AtomicReference<ChannelFuture>(null);
	private final AtomicReference<ChannelPipeline> channelPipeline = new AtomicReference<ChannelPipeline>(null);

	public static NettyClientForFile createNettyClientForFile(final FileInformation fileInformation, final String host,
			final int port) {
		NettyClientForFile client = new NettyClientForFile(fileInformation, host, port);
		return client;
	}

	private NettyClientForFile(final FileInformation fileInformation, final String host, final int port) {
		super();
		this.fileInformation = fileInformation;
		this.host = host;
		this.port = port;
		buildClient();
	}

	private void buildClient() {
		synchronized (byteLock) {
			buildInner();
		}
	}

	private void buildInner() {
		if (null != nioEventLoopGroupgroup.get()) {
			return;
		} else {
			nioEventLoopGroupgroup.set(new NioEventLoopGroup(8, StaticForAllThreadPool.NettyClient_NioEventLoop));
		}
		if (null != bootstrap.get()) {
			return;
		} else {
			bootstrap.set(new Bootstrap());
		}
		init();
	}

	private void init() {
		bootstrap.get().group(nioEventLoopGroupgroup.get());
		bootstrap.get().channel(NioSocketChannel.class);
		bootstrap.get().option(ChannelOption.TCP_NODELAY, true);
		bootstrap.get().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);
		bootstrap.get().handler(new LoggingHandler(LogLevel.INFO));
		bootstrap.get().handler(new ClientChildChannelHandler());
	}

	private class ClientChildChannelHandler extends ChannelInitializer<SocketChannel> {
		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			final ChannelPipeline pipeline = ch.pipeline();
			pipeline.addLast(new ByteArrayEncoder());
			pipeline.addLast(new ByteArrayDecoder());
			pipeline.addLast(new ChunkedWriteHandler());
			pipeline.addLast(FileClientHandler.createClientHandler(fileInformation));
			channelPipeline.set(pipeline);
		}
	}

	@Override
	public void run() {
		try {
			while (isRestart.get()) {
				buildClient();
				final ChannelFuture future = bootstrap.get().connect(host, port).sync();
				channelFuture.set(future);
				System.out.println("Start file Clent ,connect  : " + host + ":" + port);
				channelFuture.get().channel().closeFuture().sync();
				if (isRestart.get()) {
					synchronized (objectLock) {
						while (isCanstart.get()) {
							objectLock.wait();
						}
						isCanstart.set(true);
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			shutDownAndCanNotRestart();
			System.out.println(" NettyClent端 线程被中断而停止，已进行资源清理");
		} catch (Exception e) {

		}
	}

	public void canRestartIfNotShutDown() {
		shutDownInner();
		synchronized (objectLock) {
			isCanstart.set(false);
			objectLock.notify();
		}
	}

	public void shutDownAndCanNotRestart() {
		isRestart.set(false);
		shutDownInner();
	}

	private void shutDownInner() {
		if (null != nioEventLoopGroupgroup.get()) {
			shutDownBossGroup();
		}
		if (null != bootstrap.get()) {
			shutDownServerBootstrap();
		}
		if (null != channelPipeline.get()) {
			cleanPipeline();
		}
		if (null != channelFuture.get()) {
			cleanChannelFuture();
		}
	}

	private void shutDownBossGroup() {
		NioEventLoopGroup bossEventLoopGroup = nioEventLoopGroupgroup.get();
		nioEventLoopGroupgroup.set(null);
		if (null != bossEventLoopGroup) {
			bossEventLoopGroup.shutdownGracefully();
		}
	}

	private void shutDownServerBootstrap() {
		bootstrap.set(null);
	}

	private void cleanChannelFuture() {
		channelFuture.set(null);
	}

	private void cleanPipeline() {
		ChannelPipeline pipeline = channelPipeline.get();
		channelPipeline.set(null);
		if (null != pipeline) {
			pipeline.close();
		}
	}

	public static void main(String[] args) throws Exception {
		String fileFullNameWhitOutFilePathString = "visualvm_138.zip";
		String filePathString = "/home/liuxin/work/alltest";
		FileInformation fileInformation = FileInformation.createFileInformation(fileFullNameWhitOutFilePathString,
				filePathString, 13612007);
		NettyClientForFile nettyClientForFile = NettyClientForFile.createNettyClientForFile(fileInformation,
				"127.0.0.1", 8080);
		FileClientHandler fileClientHandler = FileClientHandler.createClientHandler(fileInformation);
	}
}

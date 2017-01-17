package com.baojie.hahafiletrain.TransFile.Server;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import com.baojie.hahafiletrain.ThreadAll.StaticForAllThreadPool;
import com.baojie.hahafiletrain.TransFile.Server.Handler.FileServerHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class NettyServerForFile implements Runnable {

	private static final byte[] byteLock = new byte[0];

	private static final Object objectLock = new Object();

	private final ReentrantLock mainLock = new ReentrantLock();

	private final int port;

	private final AtomicReference<NioEventLoopGroup> eventLoopBossGroupReference = new AtomicReference<NioEventLoopGroup>(
			null);

	private final AtomicReference<NioEventLoopGroup> eventLoopWorkGroupReference = new AtomicReference<NioEventLoopGroup>(
			null);

	private final AtomicReference<ServerBootstrap> serverBootstrapReference = new AtomicReference<ServerBootstrap>(null);

	private final AtomicReference<ChannelFuture> channelFutureReference = new AtomicReference<ChannelFuture>(null);

	private final AtomicReference<ChannelPipeline> channelPipelineReference = new AtomicReference<ChannelPipeline>(null);

	private final AtomicBoolean isReStart = new AtomicBoolean(true);

	private final AtomicBoolean isCanStart = new AtomicBoolean(true);

	private NettyServerForFile(final int port) {
		super();
		this.port = port;
		bulidBootstrapAndEventLoopGroup();
	}

	public static NettyServerForFile createNettyServerForFile(final int port) {
		NettyServerForFile nettyServerForFile = new NettyServerForFile(port);
		return nettyServerForFile;
	}

	private void bulidBootstrapAndEventLoopGroup() {
		synchronized (byteLock) {
			buildInner();
		}
	}

	private void buildInner() {
		if (eventLoopBossGroupReference.get() != null) {
			return;
		} else {
			eventLoopBossGroupReference.set(new NioEventLoopGroup(1,
					StaticForAllThreadPool.ParentNioEventLoopThreadPool));
		}
		if (eventLoopWorkGroupReference.get() != null) {
			return;
		} else {
			eventLoopWorkGroupReference
					.set(new NioEventLoopGroup(1, StaticForAllThreadPool.ChildNioEventLoopThreadPool));
		}
		if (serverBootstrapReference.get() != null) {
			return;
		} else {
			serverBootstrapReference.set(new ServerBootstrap());
		}
		initBootAndEventLoop();
	}

	private void initBootAndEventLoop() {
		serverBootstrapReference.get().group(eventLoopBossGroupReference.get(), eventLoopWorkGroupReference.get());
		serverBootstrapReference.get().channel(NioServerSocketChannel.class);
		serverBootstrapReference.get().option(ChannelOption.TCP_NODELAY, true);
		serverBootstrapReference.get().option(ChannelOption.SO_BACKLOG, 8096);
		serverBootstrapReference.get().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);
		serverBootstrapReference.get().handler(new LoggingHandler(LogLevel.INFO));
		serverBootstrapReference.get().childHandler(new ChildChannelHandler());
	}

	@Override
	public void run() {
		try {
			while (isReStart.get()) {
				bulidBootstrapAndEventLoopGroup();
				final ChannelFuture channelFuture = serverBootstrapReference.get().bind(port).sync();
				channelFutureReference.set(channelFuture);
				System.out.println("Start file server at port : " + port);
				channelFutureReference.get().channel().closeFuture().sync();
				if (isReStart.get()) {
					synchronized (objectLock) {
						while (isCanStart.get()) {
							objectLock.wait();
						}
						isCanStart.set(true);
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			shutDownAndCanNotRestart();
			System.out.println(" NettyServer端 线程被中断而停止，已进行资源清理");
		}catch(Exception e){
			
		}
	}

	public void canRestartIfNotShutDown() {
		shutDownInner();
		synchronized (objectLock) {
			isCanStart.set(false);
			objectLock.notify();
		}
	}

	public void shutDownAndCanNotRestart() {
			isReStart.set(false);
			shutDownInner();
	}

	private void shutDownInner() {
		if (null != eventLoopBossGroupReference.get()) {
			shutDownBossGroup();
		}
		if (null != eventLoopWorkGroupReference.get()) {
			shutDownWorkGroup();
		}
		if (null != serverBootstrapReference.get()) {
			shutDownServerBootstrap();
		}
		if (null != channelPipelineReference.get()) {
			cleanPipeline();
		}
		if (null != channelFutureReference.get()) {
			cleanChannelFuture();
		}
	}

	private void shutDownBossGroup() {
		NioEventLoopGroup bossEventLoopGroup = eventLoopBossGroupReference.get();
		eventLoopBossGroupReference.set(null);
		if(null!=bossEventLoopGroup){
			bossEventLoopGroup.shutdownGracefully();
		}
	}

	private void shutDownWorkGroup() {
		NioEventLoopGroup workEventLoopGroup = eventLoopWorkGroupReference.get();
		eventLoopWorkGroupReference.set(null);
		if(null!=workEventLoopGroup){
			workEventLoopGroup.shutdownGracefully();
		}
	}

	private void shutDownServerBootstrap() {
		serverBootstrapReference.set(null);
	}

	private void cleanChannelFuture() {
		channelFutureReference.set(null);
	}

	private void cleanPipeline() {
		ChannelPipeline chaPipeline = channelPipelineReference.get();
		channelPipelineReference.set(null);
		if(null!=chaPipeline){
			chaPipeline.close();
		}
	}

	private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			final ChannelPipeline channelPipeline = ch.pipeline();
			channelPipeline.addLast("ByteArrayDecoder", new ByteArrayDecoder());
			channelPipeline.addLast("ByteArrayEncoder", new ByteArrayEncoder());
			channelPipeline.addLast("FileServerHandler", FileServerHandler.createServerHandler());
			channelPipelineReference.set(channelPipeline);
		}
	}

	public static void main(String args[]){
		NettyServerForFile nettyServerForFile=NettyServerForFile.createNettyServerForFile(8080);
		StaticForAllThreadPool.RunNettyServer_ThreadPool.submit(nettyServerForFile);
		//LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(16, TimeUnit.SECONDS));
		//for(int i=0;i<10000;i++){
		//	nettyServerForFile.canRestartIfNotShutDown();
		//	LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(1000, TimeUnit.MILLISECONDS));
		//}
	}

}

package com.baojie.hahafiletrain.NettyTest;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GenericFutureListener;

public class TimeClient {

	public void connect(int port, String host) throws Exception {
		// 配置客户端NIO线程组
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast("LineBasedFrameDecoder", new LineBasedFrameDecoder(1024));
							ch.pipeline().addLast("StringDecoder", new StringDecoder());
							ch.pipeline().addLast("TimeClientHandler", new TimeClientHandler());
						}
					});
			// 发起异步连接操作
			ChannelFuture f = b.connect(host, port).sync();//addListener(ChannelFutureListener.CLOSE);
			// 当代客户端链路关闭
			f.channel().closeFuture().sync().addListener(new GenericFutureListener<ChannelFuture>(){
				@Override
				public void operationComplete(ChannelFuture future) {
					if (future.isDone() && future.isSuccess()) {
						System.out.println("接收服务端消息成功");
					} else {
						System.out.println("接收服务端消息失败");
					}
				}
			});
			System.out.println("客户端可以关闭了……");
		} finally {
			// 优雅退出，释放NIO线程组
			group.shutdownGracefully();
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int port = 8080;
		for(int i=0;i<1000;i++){
			new TimeClient().connect(port, "127.0.0.1");
			LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(10, TimeUnit.MILLISECONDS));
		}
		
	}
}

package com.baojie.hahafiletrain.Netty.TheNettyClient;


import com.baojie.hahafiletrain.FileInfo.FileInformation;
import com.baojie.hahafiletrain.Netty.Handler.ClientHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class NettyClient {
	private FileInformation fileInformation;

	public NettyClient(FileInformation fileInformation) {
		this.fileInformation = fileInformation;
	}

	public void connect(int port, String host) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(new LengthFieldPrepender(4));
							ch.pipeline().addLast(new ByteArrayEncoder());
							ch.pipeline().addLast(new ChunkedWriteHandler());
							ch.pipeline().addLast(new ClientHandler(fileInformation));
						}
					});
			ChannelFuture f = b.connect(host, port).sync();
			f.channel().closeFuture().sync();
		} finally {
			group.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		
	}
}
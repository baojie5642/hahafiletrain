package com.baojie.hahafiletrain.NettyTest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.logging.Logger;

public class TimeClientHandler extends ChannelInboundHandlerAdapter {

	private static final Logger logger = Logger.getLogger(TimeClientHandler.class.getName());
private static int counter;
	
	

	private byte[] req;

	/**
	 * Creates a client-side handler.
	 */
	public TimeClientHandler() {
		req = ("QUERY TIME ORDER" + System.getProperty("line.separator")).getBytes();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		ByteBuf message = null;
		for (int i = 0; i < 1; i++) {
			message = Unpooled.buffer(req.length);
			message.writeBytes(req);
			ctx.writeAndFlush(message);
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("client   channelRead");
		String body =(String)msg;
		System.out.println("Now is : " + body + " ; the counter is : " + ++counter);
	}
	
	    @Override
	    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
	    	
	    	ctx.channel().closeFuture();
	    	ctx.channel().close();
	    	ctx.disconnect();
	        ctx.close();
	    }
	    @Override
	    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
	    	ctx.fireChannelReadComplete();
	    	//ctx.channel().closeFuture();
	        //ctx.channel().close();
	        //ctx.close();
	    	ctx.flush();
	    	//ctx.disconnect();
	    }
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// 释放资源
		logger.warning("Unexpected exception from downstream : " + cause.getMessage());
		cause.printStackTrace();
		ctx.close();
	}
}

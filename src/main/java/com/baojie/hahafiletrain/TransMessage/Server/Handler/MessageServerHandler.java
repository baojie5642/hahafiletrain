package com.baojie.hahafiletrain.TransMessage.Server.Handler;

import com.baojie.hahafiletrain.MessageInfo.MessageForTrans;
import com.baojie.hahafiletrain.ThreadAll.StaticForAllThreadPool;
import com.baojie.hahafiletrain.TransMessage.Server.ServerRunner.ServerRunner;
import com.baojie.hahafiletrain.Util.Protostuff.ByteToObject;
import com.baojie.hahafiletrain.Util.Protostuff.ObjectToByte;


import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;


public class MessageServerHandler extends ChannelInboundHandlerAdapter {

	private MessageServerHandler() {
		super();
	}

	public static MessageServerHandler createMessageServerHandler() {
		MessageServerHandler messageServerHandler = new MessageServerHandler();
		return messageServerHandler;
	}
//if (null != messageForTrans0) {
//			System.out.println("服务端对象反序列化成功");
//		} else {
//			System.out.println("服务端对象反序列化失败");
//		}
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
		final byte[] bytes = (byte[]) object;
		final MessageForTrans messageForTrans0 = ByteToObject.toObject(MessageForTrans.class, bytes);
		ServerRunner serverRunner=ServerRunner.createServerRunner(messageForTrans0);
		StaticForAllThreadPool.Server_DoWork_ThreadPool.submit(serverRunner);
		MessageForTrans messageForTrans1=null;
		messageForTrans1=serverRunner.getMessage(MessageForTrans.class);
		final byte[] bytes2=ObjectToByte.toByte(messageForTrans1);
		ChannelFuture channelFuture = ctx.writeAndFlush(bytes2);
		channelFuture.addListener(new GenericFutureListener<ChannelFuture>() {
			@Override
			public void operationComplete(ChannelFuture future) {
				if (future.isSuccess()&&future.isDone()) {
					System.out.println("服务端回写消息成功");
				} else {
					System.out.println("服务端回写消息失败");	
				}
				ReferenceCountUtil.release(bytes);
				ReferenceCountUtil.release(bytes2);
			}
		});
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// 一个连接调用一次，在最后,链接断了还是会打印
		//System.out.println("server channelInactive");
		ctx.channel().close();
		ctx.close();
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		// 每次read结束都会调用
		//ctx.flush();
		//System.out.println("传输结束channelReadComplete");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("Server  :  exceptionCaught !");
		//cause.printStackTrace();
		ctx.channel().close();
		ctx.close();
	}
}

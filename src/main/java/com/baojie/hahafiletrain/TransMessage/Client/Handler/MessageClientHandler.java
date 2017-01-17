package com.baojie.hahafiletrain.TransMessage.Client.Handler;

import java.util.concurrent.atomic.AtomicBoolean;

import com.baojie.hahafiletrain.MessageInfo.LocalUseMessage;
import com.baojie.hahafiletrain.MessageInfo.MessageForTrans;
import com.baojie.hahafiletrain.Util.Protostuff.ByteToObject;
import com.baojie.hahafiletrain.Util.Protostuff.ObjectToByte;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MessageClientHandler extends ChannelInboundHandlerAdapter {
	private final LocalUseMessage localUseMessage;

	private MessageClientHandler(final LocalUseMessage localUseMessage) {
		super();
		this.localUseMessage = localUseMessage;
	}

	public static MessageClientHandler createClientHandler(final LocalUseMessage localUseMessage) {
		MessageClientHandler messageClientHandler = new MessageClientHandler(localUseMessage);
		return messageClientHandler;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		byte[] bytes = ObjectToByte.toByte(localUseMessage.getMessageForTrans());
		ctx.writeAndFlush(bytes);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
		byte[] bytes = (byte[]) object;
		final MessageForTrans messageForTrans = ByteToObject.toObject(MessageForTrans.class, bytes);
		ChannelFuture channelFuture = null;
		final AtomicBoolean isChannelGood = new AtomicBoolean(false);
		try {
			channelFuture = ctx.close();
			channelFuture.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) {
					isChannelGood.set(true);
				}
			});
			if (channelFuture.isDone() && channelFuture.isSuccess() && isChannelGood.get()) {
				System.out.println("客户端消息成功发送，可以删除消息");
				localUseMessage.setReturnMessage(messageForTrans.getMessageType(), messageForTrans.getInnerMessage());
				localUseMessage.setSuccess();
			} else {
				System.out.println("客户端消息发送失败，要重新发送");
				localUseMessage.setFail();
			}
		} finally {
			if (null != bytes) {
				bytes = null;
			}
			if (null != channelFuture) {
				channelFuture = null;
			}
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// 一个连接调用一次，在最后,链接断了还是会打印
		//System.out.println("client channelInactive");
		ctx.channel().close();
		ctx.close();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		//System.out.println("Client  :  exceptionCaught !");
		//cause.printStackTrace();
		ctx.channel().close();
		ctx.close();
	}
}

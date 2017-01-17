package com.baojie.hahafiletrain.Netty.Handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

import com.baojie.hahafiletrain.FileInfo.FileInformation;
import com.baojie.hahafiletrain.ThreadAll.StaticForAllThreadPool;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.sf.json.JSONObject;

public class ServerHandler extends ChannelInboundHandlerAdapter {

	private boolean first = true;
	private FileInformation context = null;
	private FileOutputStream fos;
	private volatile long transferedSize = 0l;
	private File receiveFile;
	private long hasReceiveFileSize = 0l;
	private RandomAccessFile inAndOutFile;

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		System.out.println("接受上下文成功 ");

	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// System.out.println("channelRead");
		ByteBuf byteBuf = (ByteBuf) msg;
		ByteBuffer byteBuffer = null;
		byte[] bytes = null;
		WriteByteBufIntoDiskRunner writeByteBufIntoDiskRunner=null;
		if (first) {
			first = false;
			bytes = new byte[byteBuf.readableBytes()];
			byteBuf.readBytes(bytes);
			try {
				context = (FileInformation) JSONObject.toBean(JSONObject.fromObject(new String(bytes)),
						FileInformation.class);
				
				writeByteBufIntoDiskRunner=WriteByteBufIntoDiskRunner.init(context);
				StaticForAllThreadPool.RUNNER_THREAD_POOL.submit(writeByteBufIntoDiskRunner);
			} catch (Exception e) {
				logger.error("传输组件--服务器端--接收传输上下文失败；" + context.getFileSouPath() + "----->>" + context.getFileDesPath());
				e.printStackTrace();
				String failMessage = failTransfer(e);
				ctx.writeAndFlush(failMessage.getBytes());
				ctx.close();
				return;
			}
			String successMessage = getSuccess();
			ctx.writeAndFlush(successMessage.getBytes());
			return;
		}
		try {
			bytes = new byte[byteBuf.readableBytes()];
			byteBuf.readBytes(bytes);
			MapForFileNameAndRunner.QUEUE.put(bytes);
			transferedSize=transferedSize+bytes.length;
			if(transferedSize==context.getFileSize()){
			ctx.close();
			}
		} catch (Exception e) {
			logger.error("传输组件--服务器端--接收文件失败;");
			e.printStackTrace();
			String shakingMessage = failTransfer(e);
			ctx.writeAndFlush(shakingMessage.getBytes());
			ctx.close();
			return;
		} finally {
			//ctx.close();
			if (null != byteBuf) {
				byteBuf.clear();
				byteBuf.release();
				byteBuf = null;
			}
			if (null != bytes) {
				//System.out.println("bytes=null");
				bytes = null;
			}
			if (null != byteBuffer) {
				byteBuffer.clear();
				byteBuffer = null;
			}

		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// 一个连接调用一次，在最后,链接断了还是会打印
		System.out.println("channelInactive");
		ctx.close();
	}


	private String failTransfer(Exception e) {
		FileInformation fileInformation = new FileInformation();
		fileInformation.setMessageFlag(false);
		String failMessage = JSONObject.fromObject(fileInformation).toString();
		return failMessage;
	}

	private String getSuccess() {
		FileInformation fileInformation = new FileInformation();
		fileInformation.setHasTransferedSize(hasReceiveFileSize);
		System.out.println("hasReceiveFileSize将已经写入的文件的大小返回客户端：" + hasReceiveFileSize);
		fileInformation.setMessageFlag(true);
		String successMessage = JSONObject.fromObject(fileInformation).toString();
		return successMessage;
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		// 每次read结束都会调用
		// System.out.println("传输结束channelReadComplete");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("一旦发生异常会打印……");
		cause.printStackTrace();
		ctx.close();
	}
}
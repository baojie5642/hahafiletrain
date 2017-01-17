package com.baojie.hahafiletrain.TransFile.Server.Handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;

import com.baojie.hahafiletrain.FileInfo.FileInformation;
import com.baojie.hahafiletrain.MessageInfo.InnerMessage;
import com.baojie.hahafiletrain.MessageInfo.MessageForTrans;
import com.baojie.hahafiletrain.Util.AboutIO.CloseIO;
import com.baojie.hahafiletrain.Util.Protostuff.ByteToObject;
import com.baojie.hahafiletrain.Util.Protostuff.ObjectToByte;


public class FileServerHandler extends ChannelInboundHandlerAdapter {
	private static final String filePath = "/home/liuxin/work/alltest/receive";
	private final AtomicReference<File> fileReference = new AtomicReference<File>(null);
	private long hasReceiveFileSize = 0l;
	private final AtomicLong totalSizeOfFile = new AtomicLong(0l);
	private final AtomicReference<String> fileFullPathWithFileFullName = new AtomicReference<String>(null);
	private final AtomicBoolean isFirstConnect = new AtomicBoolean(true);

	private FileServerHandler() {
		super();
	}

	private void cleanSource(){
		
	}
	
	
	//@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		fileFullPathWithFileFullName.set(null);
		fileReference.set(null);
		isFirstConnect.set(true);
		ctx.close(promise);
		ctx.channel().close();
		ctx.close();
	}

	public static FileServerHandler createServerHandler() {
		FileServerHandler fileServerHandler = new FileServerHandler();
		return fileServerHandler;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		isFirstConnect.set(true);
		InnerMessage innerMessage = InnerMessage.createInnerMessage("fileInfo", hasReceiveFileSize);
		MessageForTrans messageForTrans = MessageForTrans.createMessageForTrans("fileInfo", innerMessage);
		final byte[] bytes = ObjectToByte.toByte(messageForTrans);
		ChannelFuture channelFuture = ctx.writeAndFlush(bytes);
		channelFuture.addListener(new GenericFutureListener<ChannelFuture>() {
			@Override
			public void operationComplete(ChannelFuture future) {
				if (future.isDone() && future.isSuccess()) {
					System.out.println("channel   Active   向客户端写回文件消息成功");
					ReferenceCountUtil.release(bytes);
				} else {
					System.out.println("channel   Active   向客户端写回文件消息失败");
					ReferenceCountUtil.release(bytes);
				}
			}
		});
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		final byte[] fileInfoBytes = (byte[]) msg;
		System.out.println("Server-channelRead  " + isFirstConnect.get());
		if (isFirstConnect.get()) {
			System.out.println("Server-channelRead  " + isFirstConnect.get());
			isFirstConnect.set(false);
			setMessSendClient(ctx, fileInfoBytes);
			
		} else {
			System.out.println("Server-channelRead  " + isFirstConnect.get());
			System.out.println("channelRead  :" + fileInfoBytes.length);
			// randomAccessFile.write(fileInfoBytes);
			FileUtils.writeByteArrayToFile(fileReference.get(), fileInfoBytes, true);
			hasReceiveFileSize = hasReceiveFileSize + fileInfoBytes.length;
		}
	}

	private void setMessSendClient(final ChannelHandlerContext ctx, final byte[] bytes) throws Exception {
		final FileInformation fileInformation = ByteToObject.toObject(FileInformation.class, bytes);
		final String fileFullPathWithFileName = filePath + File.separator
				+ fileInformation.getFileFullNameWithOutFilePath();
		fileFullPathWithFileFullName.set(fileFullPathWithFileName);
		totalSizeOfFile.set(fileInformation.getRealFileSize());
		final long size = getFileSize(fileFullPathWithFileName);
		InnerMessage innerMessage = InnerMessage.createInnerMessage("fileInfo", size);
		MessageForTrans messageForTrans = MessageForTrans.createMessageForTrans("fileInfo", innerMessage);
		final byte[] bytesMess = ObjectToByte.toByte(messageForTrans);
		ChannelFuture channelFuture = ctx.writeAndFlush(bytesMess);
		channelFuture.addListener(new GenericFutureListener<ChannelFuture>() {
			@Override
			public void operationComplete(ChannelFuture future) {
				if (future.isDone() && future.isSuccess()) {
					System.out.println("channel  Read   向客户端写回文件消息成功");
					ReferenceCountUtil.release(bytesMess);
				} else {
					System.out.println("channel   Read  向客户端写回文件消息失败");
					ReferenceCountUtil.release(bytesMess);
				}
			}
		});
	}

	private long getFileSize(final String fileFullPathWithName) throws Exception {
		long size = 0l;
		final File file = new File(fileFullPathWithName);
		if (!file.exists()) {
			file.setWritable(true);
			file.createNewFile();
			fileReference.set(file);
			size = 0l;
		} else {
			size = innerGetSize(file);
		}
		return size;
	}

	private long innerGetSize(final File file) throws Exception {
		long size = 0l;
		FileInputStream fileInputStream = null;
		FileChannel fileChannel = null;
		RandomAccessFile randomAccessFile = null;
		try {
			fileInputStream = new FileInputStream(file);
			fileChannel = fileInputStream.getChannel();
			randomAccessFile = new RandomAccessFile(file, "r");
			size = Math.max(file.length(), fileChannel.size());
			size = Math.max(size, randomAccessFile.length());
		} finally {
			CloseIO.closeQuietly(fileChannel, fileInputStream, randomAccessFile);
		}
		return size;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		fileFullPathWithFileFullName.set(null);
		fileReference.set(null);
		isFirstConnect.set(true);
		System.out.println("channelInactive");
		ctx.channel().close();
		ctx.close();
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("一旦发生异常会打印……");
		fileFullPathWithFileFullName.set(null);
		fileReference.set(null);
		isFirstConnect.set(true);
		ctx.channel().close();
		cause.printStackTrace();
		ctx.close();
	}
}

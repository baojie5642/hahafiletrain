package com.baojie.hahafiletrain.TransFile.Client.Handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.baojie.hahafiletrain.FileInfo.FileInformation;
import com.baojie.hahafiletrain.MessageInfo.MessageForTrans;
import com.baojie.hahafiletrain.Util.AboutIO.CloseIO;
import com.baojie.hahafiletrain.Util.Protostuff.ByteToObject;
import com.baojie.hahafiletrain.Util.Protostuff.ObjectToByte;


public class FileClientHandler extends ChannelInboundHandlerAdapter {

	private final AtomicBoolean isFirstInChannelRead = new AtomicBoolean(true);
	private final AtomicLong hasTransedFileSizeReceiveFromServer = new AtomicLong(0);
	private final FileInformation fileInformation;
	private final AtomicLong sizeAtomicLong = new AtomicLong(0);
	private RandomAccessFile randomAccessFile;
	private FileChannel fileChannel;
	private FileRegion fileRegion;

	private FileClientHandler(final FileInformation fileInformation) {
		super();
		this.fileInformation = fileInformation;
	}

	public static FileClientHandler createClientHandler(final FileInformation fileInformation) {
		FileClientHandler fileClientHandler = new FileClientHandler(fileInformation);
		return fileClientHandler;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		final byte[] fileInfoBytes = ObjectToByte.toByte(fileInformation);
		ChannelFuture channelFuture = ctx.writeAndFlush(fileInfoBytes);
		channelFuture.addListener(new GenericFutureListener<ChannelFuture>() {
			@Override
			public void operationComplete(ChannelFuture future) {
				if (future.isDone() && future.isSuccess()) {
					System.out.println("向服务端写入文件消息成功");
					ReferenceCountUtil.release(fileInfoBytes);
				} else {
					System.out.println("向服务端写入文件消息失败");
					ReferenceCountUtil.release(fileInfoBytes);
				}
			}
		});
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		final byte[] bytes = (byte[]) msg;
		if (isFirstInChannelRead.get()) {
			System.out.println("Client   channelRead" + isFirstInChannelRead.get());
			isFirstInChannelRead.set(false);
			setHasTransedFileSize(bytes);
			sizeAtomicLong.set(getFileSize());
		} else {
			System.out.println("Client   channelRead" + isFirstInChannelRead.get());
			if (hasTransedFileSizeReceiveFromServer.get() < 0
					|| hasTransedFileSizeReceiveFromServer.get() > sizeAtomicLong.get()) {
				throw new IllegalArgumentException("file length is negtive or bigger than real size");
			} else if (hasTransedFileSizeReceiveFromServer.get() == sizeAtomicLong.get()) {
				return;
			} else if (hasTransedFileSizeReceiveFromServer.get() < sizeAtomicLong.get()) {
				fileRegion = new DefaultFileRegion(fileChannel, hasTransedFileSizeReceiveFromServer.get(),
						randomAccessFile.length() - hasTransedFileSizeReceiveFromServer.get());
				ChannelFuture channelFuture = ctx.writeAndFlush(fileRegion, ctx.newProgressivePromise());
				channelFuture.addListener(new ChannelProgressiveFutureListener() {
					@Override
					public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
						if (total < 0) {
							System.err.println("lx0" + future.channel() + " Transfer progress: " + progress);
						} else {
							System.err.println("lx1" + future.channel() + " Transfer progress: " + progress + " / "
									+ total);
						}
					}
					@Override
					public void operationComplete(ChannelProgressiveFuture future) {
						if (future.isDone() && future.isSuccess()) {
							System.out.println("成功");
						} else {
							System.out.println("失败");
						}
					}
				});
			}
		}
	}

	private void writeFile(ChannelHandlerContext ctx, final long fileOffet) throws Exception {
		FileRegion fileRegion = null;
		RandomAccessFile randomAccessFile = null;
		ChannelFuture channelFuture = null;
		FileChannel fileChannel = null;
		final String filePathWithFileFullName = makeFileFullPathWithFileName();
		try {
			randomAccessFile = new RandomAccessFile(new File(filePathWithFileFullName), "rw");
			fileChannel = randomAccessFile.getChannel();
			fileRegion = new DefaultFileRegion(fileChannel, fileOffet, randomAccessFile.length() - fileOffet);
			channelFuture = ctx.writeAndFlush(fileRegion, ctx.newProgressivePromise());
			channelFuture.addListener(new ChannelProgressiveFutureListener() {
				@Override
				public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
					if (total < 0) {
						System.err.println("lx0" + future.channel() + " Transfer progress: " + progress);
					} else {
						System.err.println("lx1" + future.channel() + " Transfer progress: " + progress + " / " + total);
					}
				}
				@Override
				public void operationComplete(ChannelProgressiveFuture future) {
					if (future.isDone() && future.isSuccess()) {
						System.out.println("成功");
					} else {
						System.out.println("失败");
					}
				}
			});
		} finally {
			CloseIO.closeQuietly(fileChannel, randomAccessFile);
		}
	}

	private void setHasTransedFileSize(final byte[] bytes) {
		MessageForTrans messageForTrans = null;
		try {
			messageForTrans = ByteToObject.toObject(MessageForTrans.class, bytes);
			long size = messageForTrans.getInnerMessage().getLong();
			System.out.println(size);
			hasTransedFileSizeReceiveFromServer.set(size);
		} finally {
			if (null != messageForTrans) {
				messageForTrans = null;
			}
		}
	}

	private long getFileSize() throws Exception {
		final String fileFullPathWithName = makeFileFullPathWithFileName();
		long size = 0l;
		final File file = new File(fileFullPathWithName);
		if (!file.exists()) {
			throw new FileNotFoundException();
		} else {
			size = innerGetSize(file);
		}
		return size;
	}

	private long innerGetSize(final File file) throws Exception {
		long size = 0l;
		randomAccessFile = new RandomAccessFile(file, "rw");
		fileChannel = randomAccessFile.getChannel();
		size = Math.max(file.length(), fileChannel.size());
		size = Math.max(size, randomAccessFile.length());
		return size;
	}

	private String makeFileFullPathWithFileName() {
		final String fileNameString = fileInformation.getFileFullNameWithOutFilePath();
		final String fileFullPathWithName = fileInformation.getFileSourcePathWithoutFileSeparator() + File.separator
				+ fileNameString;
		return fileFullPathWithName;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		CloseIO.closeQuietly(fileChannel, randomAccessFile);
		fileRegion.release();
		ReferenceCountUtil.release(fileRegion);
		randomAccessFile = null;
		fileChannel = null;
		fileRegion = null;
		isFirstInChannelRead.set(true);
		ctx.channel().close();
		ctx.close();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// 下面进行记录已接收文件大小的操作……
		CloseIO.closeQuietly(fileChannel, randomAccessFile);
		fileRegion.release();
		ReferenceCountUtil.release(fileRegion);
		randomAccessFile = null;
		fileChannel = null;
		fileRegion = null;
		isFirstInChannelRead.set(true);
		ctx.channel().close();
		cause.printStackTrace();
		ctx.close();
	}
}

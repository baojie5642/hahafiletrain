package com.baojie.hahafiletrain.Netty.Handler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baojie.hahafiletrain.FileInfo.FileInformation;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import net.sf.json.JSONObject;

public class ClientHandler extends ChannelInboundHandlerAdapter {
	protected static final Log logger = LogFactory.getLog(ClientHandler.class);
	private FileInformation fileInformation;
	private long counter;
	private boolean first = true;
	private long hasTransferedFromServer = 0;
	private RandomAccessFile raf = null;

	public ClientHandler(FileInformation fileInformation) {
		this.fileInformation = fileInformation;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		try {
			logger.info("发送传输文件上下文；");
			JSONObject jsonObject = JSONObject.fromObject(fileInformation);
			ctx.writeAndFlush(jsonObject.toString().getBytes());
			logger.info("发送传输文件上下文成功；");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		if (ctx.channel().isWritable()) {
			ctx.fireChannelWritabilityChanged();
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = (ByteBuf) msg;
		byte[] bytes = new byte[buf.readableBytes()];
		buf.readBytes(bytes);
		FileInformation fileInformationFromServer = (FileInformation) JSONObject.toBean(
				JSONObject.fromObject(new String(bytes)), FileInformation.class);
		hasTransferedFromServer = fileInformationFromServer.getHasTransferedSize();
		if (false == fileInformationFromServer.isMessageFlag()) {// 出现异常
			ctx.close();
			logger.error("传输组件--服务端--异常");
			return;
		}
		if (first) {
			first = false;
			FileRegion region = null;
			try {
				logger.info("传输组件--客户端--发送文件");
				raf = new RandomAccessFile(fileInformation.getFileSouPath() + fileInformation.getFileFullName(), "r");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				logger.error("传输组件--客户端--异常--文件没有找到");
				ctx.channel().close();
				ctx.close();
				return;
			}
			try {
				ChannelFuture sendFileChannelFuture;
				if (hasTransferedFromServer <= raf.length()) {
					System.out.println("文件定位到hasTransferedFromServer：" + hasTransferedFromServer + "。要传输文件大小： "
							+ raf.length());
					region = new DefaultFileRegion(raf.getChannel(), hasTransferedFromServer, raf.length()
							- hasTransferedFromServer);
				} else {
					System.out.println("文件定位到hasTransferedFromServer：" + hasTransferedFromServer + "。要传输文件大小： "
							+ raf.length());
					region = new DefaultFileRegion(raf.getChannel(), 0, raf.length());
				}
				sendFileChannelFuture = ctx.writeAndFlush(region, ctx.newProgressivePromise());
				sendFileChannelFuture.addListener(new ChannelProgressiveFutureListener() {
					@Override
					public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
						if (total < 0) {
							System.err.println("lx0" + future.channel() + " Transfer progress: " + progress);
						} else {
							System.err.println("lx1" + future.channel() + " Transfer progress: " + progress + " / "
									+ total);
							counter = progress;
						}
					}

					@Override
					public void operationComplete(ChannelProgressiveFuture future) {
						// 如果文件已经传输完成，可以关闭链接，如果传输过程中出现网络连接问题，应该可以重新链接（这是要解决的问题）
						System.out.println("counter : " + counter);
						System.err.println("lx2" + future.channel() + " Transfer complete.");
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("lx3  出错啦 ……已经传输了的文件的大小： " + counter);
		// 下面进行记录已接收文件大小的操作……
		cause.printStackTrace();
		ctx.close();
	}
}
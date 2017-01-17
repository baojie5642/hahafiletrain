package com.baojie.hahafiletrain.Netty.Handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.SynchronousQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baojie.hahafiletrain.FileInfo.FileInformation;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.sf.json.JSONObject;

public class ServerHandlerBackUp extends ChannelInboundHandlerAdapter {
	protected static final Log logger = LogFactory.getLog(ServerHandler.class);
	private boolean first = true;
	private FileInformation context = null;
	private FileOutputStream fos;
	private volatile long transferedSize = 0l;
	private File receiveFile;
	private long hasReceiveFileSize = 0l;
	private RandomAccessFile inAndOutFile;
	private final SynchronousQueue<Object> synchronousQueue=new SynchronousQueue<Object>();
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		System.out.println("接受上下文成功 ");
		
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		//System.out.println("channelRead");
		ByteBuf byteBuf = (ByteBuf) msg;
		byte[] bytes = new byte[byteBuf.readableBytes()];
		byteBuf.readBytes(bytes);
		if (first) {
			first = false;
			try {
				context = (FileInformation) JSONObject.toBean(JSONObject.fromObject(new String(bytes)),
						FileInformation.class);
			} catch (Exception e) {
				logger.error("传输组件--服务器端--接收传输上下文失败；" + context.getFileSouPath() + "----->>" + context.getFileDesPath());
				e.printStackTrace();
				String failMessage = failTransfer(e);
				ctx.writeAndFlush(failMessage.getBytes());
				ctx.close();
				return;
			}
			receiveFile = new File(context.getFileDesPath() + context.getFileFullName());
			if (receiveFile.exists() && receiveFile.length() == context.getFileSize()) {
				System.out.println("文件存在，并且大小与要传输文件大小相等，先删除掉……");
				receiveFile.delete();
			} else {
				System.out.println("文件存在，并且大小不与要传输文件大小相等，将大小设置回去……");
				hasReceiveFileSize = receiveFile.length();
				System.out.println("hasReceiveFileSize 不新建文件：" + hasReceiveFileSize);
			}
			if (!receiveFile.exists()) {
				try {
					receiveFile.createNewFile();
					hasReceiveFileSize = receiveFile.length();
					System.out.println("hasReceiveFileSize 新建文件：" + hasReceiveFileSize);
				} catch (IOException e) {
					e.printStackTrace();
					logger.error("传输组件--服务器端--创建输出文件失败；" + context.getFileSouPath() + "----->>"
							+ context.getFileDesPath());
					String failMessage = failTransfer(e);
					ctx.writeAndFlush(failMessage.getBytes());
					ctx.close();
					return;
				}
			}
			try {
				inAndOutFile = new RandomAccessFile(receiveFile, "rw");
				inAndOutFile.seek(hasReceiveFileSize);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				logger.error("传输组件--服务器端--创建输出文件流失败；" + context.getFileSouPath() + "----->>" + context.getFileDesPath());
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
			inAndOutFile.write(bytes);
			transferedSize = transferedSize + bytes.length;
			if (transferedSize + hasReceiveFileSize == context.getFileSize()) {
				if (inAndOutFile != null) {
					inAndOutFile.close();
					inAndOutFile = null;
				}
				if (fos != null) {
					fos.close();
					fos = null;
				}
				// FileTransferUtil.unzip(context.getFileDesPath() +
				// context.getFileFullName(),
				// "/home/liuxin/worker/ShappingFileTest/LiuXinUnZip/");
				// 可以添加将解压缩文件成功后将文件删除的代码
				// logger.info("解压缩文件成功: ");
				ctx.close();
				logger.info("传输组件--服务器端--接收文件成功: ");
			}
		} catch (IOException e) {
			logger.error("传输组件--服务器端--接收文件失败;");
			e.printStackTrace();
			String shakingMessage = failTransfer(e);
			ctx.writeAndFlush(shakingMessage.getBytes());
			ctx.close();
			return;
		} finally {
			if (null != byteBuf) {
				byteBuf.clear();
				byteBuf.release();
				byteBuf = null;
			}
			if (null != bytes) {
			System.out.println("bytes=null");
			bytes = null;
			}
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		//一个连接调用一次，在最后,链接断了还是会打印
		System.out.println("channelInactive");
		ctx.close();
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		if (ctx.channel().isWritable()) {
			System.out.println("channelWritabilityChanged");
			ctx.fireChannelWritabilityChanged();
		}
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
		//每次read结束都会调用
		//System.out.println("传输结束channelReadComplete");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("一旦发生异常会打印……");
		cause.printStackTrace();
		ctx.close();
	}
}
package com.baojie.hahafiletrain.Netty;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileOption {
	public boolean optionFileFromChannelToDisk(FileChannel outChannel, ByteBuffer bufferFromChannelToDisk,
			boolean isContinue, long fileChannelOption) {
		// ByteBuffer.allocate(bufferFromChannelToDisk.capacity());
		// Direct Buffer的效率会更高。
		ByteBuffer byteBuffer;
		byteBuffer = ByteBuffer.allocateDirect(bufferFromChannelToDisk.capacity());
		byteBuffer.clear();
		byteBuffer.put(bufferFromChannelToDisk);
		byteBuffer.flip();
		if (true == isContinue) {
			try {
				outChannel.position(fileChannelOption);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		while (byteBuffer.hasRemaining()) {
			try {
				outChannel.write(byteBuffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public long optionFileFlagBetweenByteAndDisk(long hasWriteFileNum, String string, String option) {
		FileChannel inAndOutChannel = null;
		RandomAccessFile inAndOutFile;
		ByteBuffer byteBuffer = null;
		try {
			inAndOutFile = new RandomAccessFile(string, option);
			inAndOutChannel = inAndOutFile.getChannel();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// ByteBuffer byteBuffer =
		// ByteBuffer.allocate(long2Bytes(hasWriteFileNum).length);
		// Direct Buffer的效率会更高。
		if ("rw".equalsIgnoreCase(option)) {
			byteBuffer = ByteBuffer.allocateDirect(Long.SIZE);
			byteBuffer.clear();
			byteBuffer.putLong(hasWriteFileNum);
			byteBuffer.flip();
			while (byteBuffer.hasRemaining()) {
				try {
					inAndOutChannel.write(byteBuffer);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			byteBuffer.flip();
			return byteBuffer.getLong();
		} else {
			try {
				byteBuffer = ByteBuffer.allocateDirect(Long.SIZE);
				byteBuffer.clear();
				inAndOutChannel.read(byteBuffer);
				byteBuffer.flip();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return byteBuffer.getLong();
		}
	}

	public static byte[] long2Bytes(long num) {
		byte[] byteNum = new byte[8];
		for (int ix = 0; ix < 8; ++ix) {
			int offset = 64 - (ix + 1) * 8;
			byteNum[ix] = (byte) ((num >> offset) & 0xff);
		}
		return byteNum;
	}

	public static long bytes2Long(byte[] byteNum) {
		long num = 0;
		for (int ix = 0; ix < 8; ++ix) {
			num <<= 8;
			num |= (byteNum[ix] & 0xff);
		}
		return num;
	}

	@SuppressWarnings("null")
	public long optionFileFlagBetweenByteAndDisk2(long hasWriteFileNum, String string, String option) {
		File file;
		FileOutputStream fos;
		FileInputStream fis = null;
		BufferedOutputStream bufferedOutputStream = null;
		BufferedInputStream bufferedInputStream = null;
		byte[] data = new byte[Long.SIZE];
		ByteBuffer buffer = null;
		try {
			file = new File(string);
			fos = new FileOutputStream(file);
			fis = new FileInputStream(file);
			bufferedOutputStream = new BufferedOutputStream(fos);
			bufferedInputStream = new BufferedInputStream(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if ("rw".equalsIgnoreCase(option)) {
			data = long2Bytes(hasWriteFileNum);
			try {
				bufferedOutputStream.write(data);
				bufferedOutputStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return hasWriteFileNum;
		} else {
			try {
				bufferedInputStream.read(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
			buffer.put(data);
			buffer.flip();
			System.out.println(buffer.getLong());

			return hasWriteFileNum;
		}
	}

	public static void main(String args[]) {
		File file = new File("D:\\LXDes\\HBuilderWin51.zip");
		long i = file.length();
		System.out.println(i);
		// FileOption fileOption = new FileOption();
		// System.out.println(fileOption.optionFileFlagBetweenByteAndDisk(0,
		// "D:\\LXDes\\lxflag.lx", "r"));
		// System.out.println(fileOption.optionFileFlagBetweenByteAndDisk2(137,
		// "D:\\LXDes\\lxflag.lx", "rw"));
	}
}

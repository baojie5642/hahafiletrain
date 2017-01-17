package com.baojie.hahafiletrain.Netty;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class FileTransferUtil {

	// ************************以下刘鑫添加%%%%*********************************//
	public static String bulidRightDir(String dir) {
		StringBuffer newDirStringBuffer = new StringBuffer();
		char a;
		char b;
		for (int i = 0; i < dir.length(); i++) {
			newDirStringBuffer.append(dir.charAt(i));
			if (newDirStringBuffer.length() > 1) {
				a = (char) dir.charAt(i);
				b = (char) dir.charAt(i - 1);
				if (a == b) {
					if (a == 92 && a == b) {
						newDirStringBuffer = newDirStringBuffer.deleteCharAt(newDirStringBuffer.length() - 1);
					} else if (a == 47 && a == b) {
						newDirStringBuffer = newDirStringBuffer.deleteCharAt(newDirStringBuffer.length() - 1);
					}
				} else if (a != b) {
					if (a == 47 && a != b && b != 47 && b != 92) {
						newDirStringBuffer = newDirStringBuffer.deleteCharAt(newDirStringBuffer.length() - 1);
						newDirStringBuffer.append(File.separator);
					} else if (a == 47 && a != b && b == 92) {
						newDirStringBuffer = newDirStringBuffer.deleteCharAt(newDirStringBuffer.length() - 1);
					} else if (a == 92 && a != b && b != 47 && b != 92) {
						newDirStringBuffer = newDirStringBuffer.deleteCharAt(newDirStringBuffer.length() - 1);
						newDirStringBuffer.append(File.separator);
					} else if (a == 92 && a != b && b == 47) {
						newDirStringBuffer = newDirStringBuffer.deleteCharAt(newDirStringBuffer.length() - 1);
					}
				}
			}
		}
		return newDirStringBuffer.toString();
	}

	public static void zip(String sourceFileDir, String desZipFileDir) {
		File file = new File(sourceFileDir);
		if (!file.exists()) {
			try {
				throw new IOException("文件不存在: " + sourceFileDir);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		if (sourceFileDir != null) {
			sourceFileDir = bulidRightDir(sourceFileDir);
			desZipFileDir = bulidRightDir(desZipFileDir);
		} else {
			//logger.error("源压缩文件文件地址为NULL！！！");
			return;
		}
		for (int i = 0; i < 5; i++) {
			file = new File(sourceFileDir + File.separator + "CENTER" + i);
			if (file.exists() && file.isDirectory()) {
				AddFolderToZip(sourceFileDir + File.separator + "CENTER" + i, desZipFileDir);
			}
		}
		file = new File(sourceFileDir + File.separator + "context_info.properties");
		if (file.exists()) {
			AddFileToZip(sourceFileDir + File.separator + "context_info.properties", desZipFileDir);
		}
	}

	public static void unzip(String sourceZipFileDir, String desFileDir) {
		System.out.println("……进入解压缩方法……");
		ZipFile zipFile=null;
		if (sourceZipFileDir != null) {
			sourceZipFileDir = bulidRightDir(sourceZipFileDir);
			desFileDir = bulidRightDir(desFileDir);
		} else {
		//	logger.error("源压缩文件不存在或文件地址为NULL！！！");
			return;
		}
		try {
			zipFile = new ZipFile(sourceZipFileDir);
			/*
			 * zipFile.setFileNameCharset("GBK"); 设置文件名编码，在GBK系统中需要设置 默认情况下是UTF8
			 * 如果将要解压的压缩文件中的文件名含有中文，解压时需要注意一点，就是设置文件名字符集方法 最好在创建zipFile之后就设置上
			 * zipFile在验证方法中就将编码设置好，以后就不再对文件名编码作改变了
			 */
			if (!zipFile.isValidZipFile()) { // 验证.zip文件是否合法，包括文件是否存在、是否为zip文件、是否被损坏等
				throw new ZipException("压缩文件不合法,可能被损坏.");
			}
			zipFile.extractAll(desFileDir);
			System.out.println("……解压缩成功……");
		} catch (ZipException e) {
			e.printStackTrace();
			return;
		}finally{
			if(null!=zipFile){
				zipFile=null;
			}
		}
	}

	public static void AddFolderToZip(String folderDir, String desZipDir) {
		try {
			ZipFile zipFile = new ZipFile(desZipDir);
			String folderToAdd = folderDir;
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
			zipFile.addFolder(folderToAdd, parameters);
		} catch (ZipException e) {
			e.printStackTrace();
			return;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void AddFileToZip(String fileDir, String desZipDir) {
		try {
			ZipFile zipFile = new ZipFile(desZipDir);
			ArrayList filesToAdd = new ArrayList();
			filesToAdd.add(new File(fileDir));
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
			zipFile.addFiles(filesToAdd, parameters);
		} catch (ZipException e) {
			e.printStackTrace();
			return;
		}
	}
}

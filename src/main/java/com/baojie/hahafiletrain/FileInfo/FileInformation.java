package com.baojie.hahafiletrain.FileInfo;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class FileInformation implements Serializable {

	private static final long serialVersionUID = 2016051614471655555L;

	private static final ReentrantLock mainLock = new ReentrantLock();

	private static final byte[] byteLock = new byte[0];

	private final AtomicBoolean isAutoConnectWhenConClosed = new AtomicBoolean(true);

	private final AtomicBoolean buttonForUserClickWhenConnectionGood = new AtomicBoolean(true);

	private final Condition condition;

	private final String fileFullNameWithOutFilePath;

	private final String fileSourcePathWithoutFileSeparator;
	
	private final long realTotalFileSize;

	private final File[] fileListToTrans;

	private FileInformation(final String fileFullNameWithOutFilePath, final String fileSourcePathWithoutFileSeparator,final long realTotalFileSize,
			final File[] fileListToTrans) {
		super();
		this.fileFullNameWithOutFilePath = fileFullNameWithOutFilePath;
		this.fileSourcePathWithoutFileSeparator = fileSourcePathWithoutFileSeparator;
		this.realTotalFileSize=realTotalFileSize;
		this.condition = mainLock.newCondition();
		synchronized (byteLock) {
			if (null == fileListToTrans || fileListToTrans.length == 0) {
				this.fileListToTrans = new File[0];
			} else {
				this.fileListToTrans = fileListToTrans;
			}
		}
	}

	public static FileInformation createFileInformation(final String fileFullNamWithOutFilePath,
			final String fileSourcePathWithoutFileSeparator, final long realFileSize,final File[] fileListToTrans) {
		FileInformation fileInformation = new FileInformation(fileFullNamWithOutFilePath, fileSourcePathWithoutFileSeparator,realFileSize,
				fileListToTrans);
		return fileInformation;
	}

	public static FileInformation createFileInformation(final String fileFullName,
			final String fileSourcePathWithoutFileSeparator,final long realFileSize) {
		FileInformation fileInformation = new FileInformation(fileFullName, fileSourcePathWithoutFileSeparator,realFileSize, null);
		return fileInformation;
	}

	public void waitWhenConnectionCanNotUse() {
		final ReentrantLock lock = mainLock;
		lock.lock();
		try {
			if (isAutoConnect()) {
				return;
			}
			while (buttonForUserClickWhenConnectionGood.get()) {
				try {
					condition.await();
				} catch (InterruptedException e) {
					assert true;
					break;
				}
			}
			buttonForUserClickWhenConnectionGood.set(true);
		} finally {
			lock.unlock();
		}
	}

	public boolean isAutoConnect() {
		boolean isAuto = true;
		isAuto = isAutoConnectWhenConClosed.get();
		return isAuto;
	}

	public void clickButtonWhenConnectionCanUse() {
		final ReentrantLock lock = mainLock;
		lock.lock();
		try {
			buttonForUserClickWhenConnectionGood.set(false);
			condition.signal();
		} finally {
			lock.unlock();
		}
	}

	public void setAutoConnect() {
		isAutoConnectWhenConClosed.set(true);
	}

	public void setNoAutoConnect() {
		isAutoConnectWhenConClosed.set(false);
	}
	
	public String getFileFullNameWithOutFilePath() {
		return fileFullNameWithOutFilePath;
	}

	public String getFileSourcePathWithoutFileSeparator() {
		return fileSourcePathWithoutFileSeparator;
	}

	public File[] getFileListToTrans() {
		return fileListToTrans;
	}

	public long getRealFileSize() {
		return realTotalFileSize;
	}

}

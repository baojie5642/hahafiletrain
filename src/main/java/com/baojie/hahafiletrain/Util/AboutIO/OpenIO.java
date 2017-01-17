package com.baojie.hahafiletrain.Util.AboutIO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

public class OpenIO {
	private static final ReentrantLock mainLock = new ReentrantLock();

	public static FileInputStream openInputStream(final File file)  throws IOException{
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if (file.canRead() == false) {
				throw new IOException("File '" + file + "' cannot be read");
			}
		} else {
			throw new FileNotFoundException("File '" + file + "' does not exist");
		}
		final ReentrantLock lock = mainLock;
		lock.lock();
		try {
			return new FileInputStream(file);
		} finally {
			lock.unlock();
		}
	}

	public static FileOutputStream openOutputStream(final File file, final boolean append) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if (file.canWrite() == false) {
				throw new IOException("File '" + file + "' cannot be written to");
			}
		} else {
			throw new FileNotFoundException("File '" + file + "' does not exist");
		}
		final ReentrantLock lock = mainLock;
		lock.lock();
		try {
			return new FileOutputStream(file, append);
		} finally {
			lock.unlock();
		}
	}
}

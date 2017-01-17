package com.baojie.hahafiletrain.Util.CheckAll;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.baojie.hahafiletrain.Util.AboutIO.CloseIO;
import com.baojie.hahafiletrain.Util.AboutIO.OpenIO;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.ByteOrderMarkDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.ParsingDetector;
import info.monitorenter.cpdetector.io.UnicodeDetector;

public class CheckStringEncoding {

	private static final ReentrantLock MainLock = new ReentrantLock();

	private static final ParsingDetector parsingDetector = new ParsingDetector(false);

	private static final ByteOrderMarkDetector byteOrderMarkDetector = new ByteOrderMarkDetector();

	private static final ConcurrentHashMap<Class<?>, CodepageDetectorProxy> cache = new ConcurrentHashMap<Class<?>, CodepageDetectorProxy>();

	private CheckStringEncoding() {
		super();
	}

	public static String getEncoding(final String fileFullFath) throws IOException {
		if (null == fileFullFath) {
			throw new NullPointerException();
		}
		final FileInputStream fileInputStream = OpenIO.openInputStream(new File(fileFullFath));
		final BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
		try {
			final String encodingName = getEncoding(bufferedInputStream);
			return encodingName;
		} finally {
			CloseIO.closeQuietly(bufferedInputStream, fileInputStream);
		}
	}

	public static String getEncoding(final BufferedInputStream bufferedInputStream) throws IOException {
		final int size = bufferedInputStream.available();
		java.nio.charset.Charset charset = null;
		CodepageDetectorProxy detector = null;
		final ReentrantLock lock = MainLock;
		lock.lock();
		try {
			bufferedInputStream.mark(size);
			detector = getDetector(CodepageDetectorProxy.class);
			charset = detector.detectCodepage(bufferedInputStream, size);
			bufferedInputStream.reset();
			final String encodingName = charset.toString();
			return encodingName;
		} finally {
			lock.unlock();
			if (null != charset) {
				charset = null;
			}
			if (null != detector) {
				detector = null;
			}
		}
	}

	private static CodepageDetectorProxy getDetector(final Class<?> cls) {
		if (null == cls) {
			throw new NullPointerException("cls in getDetector() must not be null");
		}
		CodepageDetectorProxy detector = (CodepageDetectorProxy) cache.get(cls);
		if (detector == null) {
			detector = CodepageDetectorProxy.getInstance();
			detector.add(JChardetFacade.getInstance());
			detector.add(ASCIIDetector.getInstance());
			detector.add(UnicodeDetector.getInstance());
			detector.add(parsingDetector);
			detector.add(byteOrderMarkDetector);
			if (detector != null) {
				cache.putIfAbsent(cls, detector);
			}
		}
		return detector;
	}

	public static String getEncoding(final byte[] byteArr) throws IOException {
		ByteArrayInputStream byteArrIn = new ByteArrayInputStream(byteArr);
		BufferedInputStream buffIn = new BufferedInputStream(byteArrIn);
		CodepageDetectorProxy detector = getDetector(CodepageDetectorProxy.class);
		java.nio.charset.Charset charset = null;
		final ReentrantLock lock = MainLock;
		lock.lock();
		try {
			charset = detector.detectCodepage(buffIn, buffIn.available());
			final String encodingName = charset.toString();
			return encodingName;
		} finally {
			lock.unlock();
			if (null != charset) {
				charset = null;
			}
			if (null != detector) {
				detector = null;
			}
			CloseIO.closeQuietly(buffIn, byteArrIn);
		}
	}
	
}

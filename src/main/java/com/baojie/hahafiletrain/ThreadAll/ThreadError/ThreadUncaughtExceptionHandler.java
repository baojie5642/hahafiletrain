package com.baojie.hahafiletrain.ThreadAll.ThreadError;

import java.lang.Thread.UncaughtExceptionHandler;

public class ThreadUncaughtExceptionHandler implements UncaughtExceptionHandler {

	@Override
	public void uncaughtException(final Thread t, final Throwable e) {
		if (null == t) {
			throw new NullPointerException("Thread must not be null.");
		}
		if (null == e) {
			throw new NullPointerException("Throwable must not be null.");
		}
		try {
			t.interrupt();
			// 可以打印一些消息
			// Logger logger = Logger.getAnonymousLogger();
			// logger.log(Level.SEVERE, "Thread terminated with exception : " +
			// t.getName(), e);
			// logger.log(Level.SEVERE, t.getName() +
			// "  occur UncaughtException……已经调用thread.interrupt()终止线程……");
		} finally {
			if (!t.isInterrupted()) {
				t.interrupt();
			}
			if (t.isAlive()) {
				t.interrupt();
			}
		}
	}
}

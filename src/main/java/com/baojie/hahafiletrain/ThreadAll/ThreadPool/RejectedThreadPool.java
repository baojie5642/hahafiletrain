package com.baojie.hahafiletrain.ThreadAll.ThreadPool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.baojie.hahafiletrain.ThreadAll.ThreadFactory.ThreadFactoryForThreadPool;

public class RejectedThreadPool extends ThreadPoolExecutor {

	public static RejectedThreadPool initRejectedThreadPool(final int corePoolSize, final int maximumPoolSize,
			final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue,
			final String threadFactoryName) {
		RejectedThreadPool rejectedThreadPool = new RejectedThreadPool(corePoolSize, maximumPoolSize, keepAliveTime,
				unit, workQueue, threadFactoryName);
		return rejectedThreadPool;
	}

	private RejectedThreadPool(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime,
			final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final String threadFactoryName) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, ThreadFactoryForThreadPool
				.init(threadFactoryName), new ThreadPoolExecutor.CallerRunsPolicy());
		super.allowCoreThreadTimeOut(true);
		// super.prestartCoreThread();
	}
}

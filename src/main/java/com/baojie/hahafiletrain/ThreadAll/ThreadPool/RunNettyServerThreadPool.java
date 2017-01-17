package com.baojie.hahafiletrain.ThreadAll.ThreadPool;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.baojie.hahafiletrain.ThreadAll.PoolError.LocalRejectedExecutionHandler;
import com.baojie.hahafiletrain.ThreadAll.ThreadFactory.ThreadFactoryForThreadPool;


public class RunNettyServerThreadPool extends ThreadPoolExecutor {
	public static RunNettyServerThreadPool initNettyServerThreadPool(final int corePoolSize, final int maximumPoolSize,
			final long keepAliveTime, final TimeUnit unit, final SynchronousQueue<Runnable> workuQueue,
			final String threadFactoryName) {
		RunNettyServerThreadPool runNettyServerThreadPool = new RunNettyServerThreadPool(corePoolSize, maximumPoolSize,
				keepAliveTime, unit, workuQueue, threadFactoryName);
		return runNettyServerThreadPool;
	}

	private RunNettyServerThreadPool(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime,
			final TimeUnit unit, final SynchronousQueue<Runnable> workuQueue, final String threadFactoryName) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workuQueue, ThreadFactoryForThreadPool
				.init(threadFactoryName), LocalRejectedExecutionHandler.init());
		super.allowCoreThreadTimeOut(true);
		// super.prestartCoreThread();
	}

}

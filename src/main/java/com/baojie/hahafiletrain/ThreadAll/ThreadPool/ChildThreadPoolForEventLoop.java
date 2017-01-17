package com.baojie.hahafiletrain.ThreadAll.ThreadPool;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.baojie.hahafiletrain.ThreadAll.PoolError.LocalRejectedExecutionHandler;
import com.baojie.hahafiletrain.ThreadAll.ThreadFactory.ThreadFactoryForThreadPool;



public class ChildThreadPoolForEventLoop extends ThreadPoolExecutor {
	public static ChildThreadPoolForEventLoop initDoWorkThreadPool(final int corePoolSize,final  int maximumPoolSize, final long keepAliveTime,
			final TimeUnit unit, final SynchronousQueue<Runnable> workuQueue,final String threadFactoryName) {
		ChildThreadPoolForEventLoop childThreadPoolForEventLoop = new ChildThreadPoolForEventLoop(corePoolSize, maximumPoolSize, keepAliveTime, unit,
				workuQueue,threadFactoryName);
		return childThreadPoolForEventLoop;
	}

	private ChildThreadPoolForEventLoop(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit,
			final SynchronousQueue<Runnable> workuQueue,final String threadFactoryName) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workuQueue, ThreadFactoryForThreadPool
				.init(threadFactoryName), LocalRejectedExecutionHandler.init());
		super.allowCoreThreadTimeOut(true);
		//super.prestartCoreThread();
	} 

}

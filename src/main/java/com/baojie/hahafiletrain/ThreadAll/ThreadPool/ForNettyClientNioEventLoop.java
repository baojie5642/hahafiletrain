package com.baojie.hahafiletrain.ThreadAll.ThreadPool;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.baojie.hahafiletrain.ThreadAll.PoolError.LocalRejectedExecutionHandler;
import com.baojie.hahafiletrain.ThreadAll.ThreadFactory.ThreadFactoryForThreadPool;




public class ForNettyClientNioEventLoop extends ThreadPoolExecutor{
	public static ForNettyClientNioEventLoop initDoWorkThreadPool(final int corePoolSize,final  int maximumPoolSize, final long keepAliveTime,
			final TimeUnit unit, final SynchronousQueue<Runnable> workuQueue,final String threadFactoryName) {
		ForNettyClientNioEventLoop forNettyClientNioEventLoop = new ForNettyClientNioEventLoop(corePoolSize, maximumPoolSize, keepAliveTime, unit,
				workuQueue,threadFactoryName);
		return forNettyClientNioEventLoop;
	}

	private ForNettyClientNioEventLoop(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit,
			final SynchronousQueue<Runnable> workuQueue,final String threadFactoryName) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workuQueue, ThreadFactoryForThreadPool
				.init(threadFactoryName), LocalRejectedExecutionHandler.init());
		super.allowCoreThreadTimeOut(true);
		//super.prestartCoreThread();
	} 

}

package com.baojie.hahafiletrain.ThreadAll.ThreadPool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.baojie.hahafiletrain.ThreadAll.PoolError.LocalRejectedExecutionHandler;
import com.baojie.hahafiletrain.ThreadAll.ThreadFactory.ThreadFactoryForThreadPool;



public class ClientDoWorkThreadPool  extends ThreadPoolExecutor {
	public static ClientDoWorkThreadPool initDoWorkThreadPool(final int corePoolSize,final  int maximumPoolSize, final long keepAliveTime,
			final TimeUnit unit, final SynchronousQueue<Runnable> workQueue,final String threadFactoryName) {
		ClientDoWorkThreadPool doWorkThreadPool = new ClientDoWorkThreadPool(corePoolSize, maximumPoolSize, keepAliveTime, unit,
				workQueue,threadFactoryName);
		return doWorkThreadPool;
	}

	private ClientDoWorkThreadPool(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit,
			final SynchronousQueue<Runnable> workQueue,final String threadFactoryName) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, ThreadFactoryForThreadPool
				.init(threadFactoryName), LocalRejectedExecutionHandler.init());
		super.allowCoreThreadTimeOut(true);
		//super.prestartCoreThread();
	}
}

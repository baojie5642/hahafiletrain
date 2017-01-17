package com.baojie.hahafiletrain.ThreadAll.PoolError;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import com.baojie.hahafiletrain.ThreadAll.StaticForAllThreadPool;


public class LocalRejectedExecutionHandler implements RejectedExecutionHandler {

	public static LocalRejectedExecutionHandler init() {
		LocalRejectedExecutionHandler localRejectedExecutionHandler = new LocalRejectedExecutionHandler();
		return localRejectedExecutionHandler;
	}

	private LocalRejectedExecutionHandler() {
		super();
	}

	@Override
	public void rejectedExecution(final Runnable r,final  ThreadPoolExecutor executor) {
		if(executor.getQueue().offer(r))
			return;
		StaticForAllThreadPool.Rejected_ThreadPool.submit(r);
	}
	
}

package com.baojie.hahafiletrain.ThreadAll.ThreadPool;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.baojie.hahafiletrain.ThreadAll.PoolError.LocalRejectedExecutionHandler;
import com.baojie.hahafiletrain.ThreadAll.ThreadFactory.ThreadFactoryForThreadPool;

public class ScheduledTaskPool extends ScheduledThreadPoolExecutor {

	public static ScheduledTaskPool initScheduledTaskPool(final int corePoolSize,final String threadFactoryName) {
		ScheduledTaskPool scheduledTaskPool = new ScheduledTaskPool(corePoolSize,threadFactoryName);
		return scheduledTaskPool;
	}

	private ScheduledTaskPool(final int corePoolSize,final String threadFactoryName) {
		super(corePoolSize, ThreadFactoryForThreadPool.init(threadFactoryName), LocalRejectedExecutionHandler
				.init());
		super.setContinueExistingPeriodicTasksAfterShutdownPolicy(true);
		super.setExecuteExistingDelayedTasksAfterShutdownPolicy(true);
		super.setRemoveOnCancelPolicy(true);
	}

}

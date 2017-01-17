package com.baojie.hahafiletrain.ThreadAll;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.baojie.hahafiletrain.ThreadAll.ThreadPool.ChildThreadPoolForEventLoop;
import com.baojie.hahafiletrain.ThreadAll.ThreadPool.ClientDoWorkThreadPool;
import com.baojie.hahafiletrain.ThreadAll.ThreadPool.ForNettyClientNioEventLoop;
import com.baojie.hahafiletrain.ThreadAll.ThreadPool.ParentThreadPoolForEventLoop;
import com.baojie.hahafiletrain.ThreadAll.ThreadPool.RejectedThreadPool;
import com.baojie.hahafiletrain.ThreadAll.ThreadPool.RunNettyClientThreadPool;
import com.baojie.hahafiletrain.ThreadAll.ThreadPool.RunNettyServerThreadPool;
import com.baojie.hahafiletrain.ThreadAll.ThreadPool.ScheduledTaskPool;
import com.baojie.hahafiletrain.ThreadAll.ThreadPool.ServerDoWorkThreadPool;

public class StaticForAllThreadPool {

	public static final RejectedThreadPool Rejected_ThreadPool = RejectedThreadPool.initRejectedThreadPool(8192, 8192, 180,
			TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(9999999), "RejectedThreadPool");

	public static final ServerDoWorkThreadPool Server_DoWork_ThreadPool = ServerDoWorkThreadPool.initDoWorkThreadPool(
			8, 8192, 180, TimeUnit.SECONDS,  new SynchronousQueue<Runnable>(), "ServerDoWorkThreadPool");

	public static final ClientDoWorkThreadPool Client_DoWork_ThreadPool = ClientDoWorkThreadPool.initDoWorkThreadPool(
			8, 8192, 180, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), "ClientDoWorkThreadPool");

	public static final ScheduledTaskPool ScheduledTask_ThreadPool = ScheduledTaskPool.initScheduledTaskPool(32,
			"ScheduledTaskPool");

	public static final ThreadPoolExecutor CacheThreadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

	public static final ChildThreadPoolForEventLoop ParentNioEventLoopThreadPool = ChildThreadPoolForEventLoop
			.initDoWorkThreadPool(16, 128, 1, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
					"ChildThreadPoolForNioEventLoop");

	public static final ParentThreadPoolForEventLoop ChildNioEventLoopThreadPool = ParentThreadPoolForEventLoop
			.initDoWorkThreadPool(16, 128, 1, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
					"ParentThreadPoolForNioEventLoop");

	public static final ForNettyClientNioEventLoop NettyClient_NioEventLoop = ForNettyClientNioEventLoop
			.initDoWorkThreadPool(2, 16, 180, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
					"ForNettyClientNioEventLoop");

	public static final RunNettyClientThreadPool RunNettyClient_ThreadPool = RunNettyClientThreadPool.initNettyClientThreadPool(1,
			4, 180, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), "RunNettyClientThreadPool");
	
	public static final RunNettyServerThreadPool RunNettyServer_ThreadPool = RunNettyServerThreadPool.initNettyServerThreadPool(1,
			4, 180, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), "RunNettyServerThreadPool");

}

package com.baojie.hahafiletrain.LiuXinFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ManyThreadToGet implements Runnable {

	private final JiaoBuObjectFuture futureToBuild;

	private ManyThreadToGet(final JiaoBuObjectFuture futureToBuild) {
		super();
		this.futureToBuild = futureToBuild;
	}

	public static ManyThreadToGet createManyThreadToGet(final JiaoBuObjectFuture futureToBuild) {
		ManyThreadToGet manyThreadToGet = new ManyThreadToGet(futureToBuild);
		return manyThreadToGet;
	}

	@Override
	public void run() {
		System.out.println(futureToBuild.isDone());
		System.out.println(Thread.currentThread().getName() + "开始阻塞等待……。");
		final  int times=ThreadLocalRandom.current().nextInt(100);
		try {
			TimeUnit.SECONDS.sleep(times);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			futureToBuild.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		System.out.println(Thread.currentThread().getName() + "已经结束。get成功");
		try {
			futureToBuild.get(90, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		System.out.println(futureToBuild.isDone());
	}

}

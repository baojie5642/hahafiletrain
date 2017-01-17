package com.baojie.hahafiletrain.LiuXinFuture;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class OneThreadToSetState implements Runnable{

	private final JiaoBuObjectFuture futureToBuild;
	
	private OneThreadToSetState(final JiaoBuObjectFuture futureToBuild){
		super();
		this.futureToBuild=futureToBuild;
	}
	
	public static OneThreadToSetState createOneThreadToSetState(final JiaoBuObjectFuture futureToBuild){
		OneThreadToSetState  oneThreadToSetState=new OneThreadToSetState(futureToBuild);
		return oneThreadToSetState;
	}
	
	@Override
	public void run(){
		System.out.println("设置状态的线程   等待开始  ");
		try {
			TimeUnit.SECONDS.sleep(ThreadLocalRandom.current().nextInt(100));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		futureToBuild.set(true);
		System.out.println("设置状态的线程 状态设置完成后，也已经等待完成，这时应该是所有的线程都已经结束了");
	}
	
}

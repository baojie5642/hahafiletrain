package com.baojie.hahafiletrain.LiuXinFuture;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.baojie.hahafiletrain.ThreadAll.StaticForAllThreadPool;


public class FutureTestMain {

	public FutureTestMain() {
		super();
	}

	public static void main(String args[]) {

		JiaoBuObjectFuture futureToBuild = JiaoBuObjectFuture.createFutureToBuild();

		OneThreadToSetState oneThreadToSetState = OneThreadToSetState.createOneThreadToSetState(futureToBuild);

		ManyThreadToGet manyThreadToGet = ManyThreadToGet.createManyThreadToGet(futureToBuild);

		StaticForAllThreadPool.Server_DoWork_ThreadPool.submit(oneThreadToSetState);
		for (;;) {
			for (int i = 0; i < 2600; i++) {
				StaticForAllThreadPool.Client_DoWork_ThreadPool.submit(manyThreadToGet);
			}
			LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(ThreadLocalRandom.current().nextInt(60),
					TimeUnit.SECONDS));
		}

	}
}

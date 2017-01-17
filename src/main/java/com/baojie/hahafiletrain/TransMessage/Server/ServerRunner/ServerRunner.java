package com.baojie.hahafiletrain.TransMessage.Server.ServerRunner;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import com.baojie.hahafiletrain.MessageInfo.InnerMessage;
import com.baojie.hahafiletrain.MessageInfo.MessageForTrans;
import com.baojie.hahafiletrain.Util.SysTime.RightTime;



public class ServerRunner implements Runnable {

	private final MessageForTrans messageForTrans;
	private final byte[] byteLock = new byte[0];
	private final AtomicReference<Object> returnMessage = new AtomicReference<>(null);
	private final Semaphore semaphore = new Semaphore(1);
	private final ReentrantLock mainLock = new ReentrantLock();

	private ServerRunner(final MessageForTrans messageForTrans) {
		super();
		this.messageForTrans = messageForTrans;
		try {
			synchronized (byteLock) {
				semaphore.acquire(1);
			}
		} catch (InterruptedException e) {
			assert true;
		}
	}

	private void acquireSem() {
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			assert true;
		}
	}

	public static ServerRunner createServerRunner(final MessageForTrans messageForTrans) {
		ServerRunner serverRunner = new ServerRunner(messageForTrans);
		return serverRunner;
	}

	@Override
	public void run() {
		final InnerMessage innerMessage = InnerMessage.createInnerMessage(
				Thread.currentThread().getName() + RightTime.getSysTime(null, null), 9);
		final MessageForTrans messageForTransInner = MessageForTrans.createMessageForTrans(
				messageForTrans.getMessageType(), innerMessage);
		returnMessage.set(messageForTransInner);
		weekUp();
	}

	private void weekUp() {
		final ReentrantLock lock = mainLock;
		lock.lock();
		try {
			if (semaphore.availablePermits() == 0) {
				semaphore.release(1);
			}
		} finally {
			lock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T getMessage(final Class<T> cls) throws NullPointerException {
		if (null == cls) {
			throw new NullPointerException();
		}
		T messageT = null;
		if (null != returnMessage.get()) {
			messageT = (T) returnMessage.get();
		} else {
			acquireSem();
			messageT = (T) returnMessage.get();
			final ReentrantLock lock = mainLock;
			lock.lock();
			try {
				if (semaphore.availablePermits() == 1) {
					acquireSem();
				}
			} finally {
				lock.unlock();
			}
		}
		return messageT;
	}

}

package com.baojie.hahafiletrain.MessageInfo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LocalUseMessage {

	private static final int retryTimesWhenConnectionCanNotUse=10;
	
	private static final int howManySecondsWaitAfterOneRetry=10;
	
	private final ReentrantLock mainLock = new ReentrantLock();

	private final AtomicBoolean useForTakeStateInWhileLoop = new AtomicBoolean(true);
	
	private final AtomicBoolean clickButtonWhenNetGoodAgain=new AtomicBoolean(true);

	private final AtomicBoolean isSuccess = new AtomicBoolean(false);

	private final AtomicBoolean frontTimeState_SuccessOrFail=new AtomicBoolean(false);
	
	private final ConcurrentHashMap<String, BaseMessage> getReturnMessageFromThisMap = new ConcurrentHashMap<String, BaseMessage>(
			1);
	
	private final MessageForTrans messageForTrans;

	private final Condition condition;

	public static LocalUseMessage createLocaLoaclUseMessage(final MessageForTrans messageForTrans) {
		LocalUseMessage localUseMessage = new LocalUseMessage(messageForTrans);
		return localUseMessage;
	}

	private LocalUseMessage(final MessageForTrans messageForTrans) {
		super();
		this.messageForTrans = messageForTrans;
		this.condition = mainLock.newCondition();
	}

	public void onlyWaitOn_MessObj(){
		final ReentrantLock lock=mainLock;
		lock.lock();
		try{
			while (clickButtonWhenNetGoodAgain.get()) {
				try {
					condition.await();
				} catch (InterruptedException e) {
					assert true;
					break;
				}
			}
			clickButtonWhenNetGoodAgain.set(true);
		}finally{
			lock.unlock();
		}
	}
	
	public void clickWhenNetGoodAgain(){
		final ReentrantLock lock=mainLock;
		lock.lock();
		try{
			clickButtonWhenNetGoodAgain.set(false);
			condition.signal();
		}finally{
			lock.unlock();
		}
	}
	
	public boolean takeState() {
		boolean success = false;
		final ReentrantLock lock = mainLock;
		lock.lock();
		try {
			while (useForTakeStateInWhileLoop.get()) {
				try {
					condition.await();
				} catch (InterruptedException e) {
					assert true;
					break;
				}
			}
			useForTakeStateInWhileLoop.set(true);
			success = isSuccess.get();
			frontTimeState_SuccessOrFail.set(success);
			isSuccess.set(false);
		} finally {
			lock.unlock();
		}
		return success;
	}
	
	public boolean getFrontState(){
		return frontTimeState_SuccessOrFail.get();
	}

	public void setSuccess() {
		final ReentrantLock lock = mainLock;
		lock.lock();
		try {
			isSuccess.set(true);
			useForTakeStateInWhileLoop.set(false);
			condition.signal();
		} finally {
			lock.unlock();
		}
	}

	public void setFail() {
		final ReentrantLock lock = mainLock;
		lock.lock();
		try {
			isSuccess.set(false);
			useForTakeStateInWhileLoop.set(false);
			condition.signal();
		} finally {
			lock.unlock();
		}
	}

	public BaseMessage getReturnMessage() throws NullPointerException {
		BaseMessage baseMessage = null;
		final ReentrantLock lock = new ReentrantLock();
		lock.lock();
		try {
			baseMessage = getReturnMessageFromThisMap.get(messageForTrans.getMessageType());
			if (null == baseMessage) {
				throw new NullPointerException();
			}
		} finally {
			lock.unlock();
		}
		return baseMessage;
	}

	public void setReturnMessage(final String messageType, final BaseMessage baseMessage) {
		final ReentrantLock lock = new ReentrantLock();
		lock.lock();
		try {
			getReturnMessageFromThisMap.clear();
			getReturnMessageFromThisMap.put(messageType, baseMessage);
		} finally {
			lock.unlock();
		}
	}

	public MessageForTrans getMessageForTrans() {
		return messageForTrans;
	}

	public int getRetryTimesWhenConnectionCanNotUse() {
		return retryTimesWhenConnectionCanNotUse;
	}

	public int getHowManySecondsWaitAfterOneRetry() {
		return howManySecondsWaitAfterOneRetry;
	}
	
}

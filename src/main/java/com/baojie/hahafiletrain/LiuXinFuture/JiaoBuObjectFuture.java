package com.baojie.hahafiletrain.LiuXinFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

//UnSafe.compareAndSwapObject(1-this, 2-waitersOffset, 3-q, 4-null)；
//1.当前对象（目标对象），要结合第三个参数来理解，其实第二个参数和第一个是对应的，也就是说偏移量是指向第一个参数类型的；
//2.与参数1相对应的偏移量；
//3.要改变的对象的你期望的他的状态；
//4.要把刚才的状态变成什么.
//其中的offSet就是这个类的最下面针对每个成员变量生成的偏移量

public class JiaoBuObjectFuture {

	private volatile int state;
	private static final int New = 0;
	private static final int Completing = 1;
	private static final int Normal = 2;
	private static final int UnknowError = 3;
	private boolean outcome;
	private volatile WaitNode waiters;

	private JiaoBuObjectFuture() {
		super();
		this.state = New;
	}

	public static JiaoBuObjectFuture createFutureToBuild() {
		JiaoBuObjectFuture futureToBuild = new JiaoBuObjectFuture();
		return futureToBuild;
	}

	public boolean isDone() {
		return state != New;
	}

	public boolean get() throws InterruptedException, ExecutionException {
		int s = state;
		if (s <= Completing) {
			s = awaitDone(false, 0L);
		}
		return report(s);
	}

	public boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		if (unit == null) {
			throw new NullPointerException();
		}
		int s = state;
		if (s <= Completing && (s = awaitDone(true, unit.toNanos(timeout))) <= Completing) {
			throw new TimeoutException();
		}
		return report(s);
	}

	private int awaitDone(boolean timed, long nanos) throws InterruptedException {
		final long deadline = timed ? System.nanoTime() + nanos : 0L;
		WaitNode q = null;
		boolean queued = false;
		for (;;) {
			if (Thread.interrupted()) {
				removeWaiter(q);
				throw new InterruptedException();
			}
			int s = state;
			if (s > Completing) {
				if (q != null) {
					q.thread = null;
				}
				return s;
			} else if (s == Completing) {
				Thread.yield();
			} else if (q == null) {
				q = new WaitNode();
			} else if (!queued) {
				queued = UnSafe.compareAndSwapObject(this, waitersOffset, q.next = waiters, q);
			} else if (timed) {
				nanos = deadline - System.nanoTime();
				if (nanos <= 0L) {
					removeWaiter(q);
					return state;
				}
				LockSupport.parkNanos(this, nanos);
			} else {
				LockSupport.park(this);
			}
		}
	}

	private void removeWaiter(WaitNode node) {
		if (node != null) {
			node.thread = null;
			retry: for (;;) {
				for (WaitNode pred = null, q = waiters, s; q != null; q = s) {
					s = q.next;
					if (q.thread != null) {
						pred = q;
					} else if (pred != null) {
						pred.next = s;
						if (pred.thread == null) {
							continue retry;
						}
					} else if (!UnSafe.compareAndSwapObject(this, waitersOffset, q, s)) {
						continue retry;
					}
				}
				break;
			}
		}
	}

	private boolean report(int s) {
		boolean x = outcome;
		if (s == Normal) {
			return x;
		}
		if (s >= UnknowError) {
			throw new IllegalStateException();
		}
		return x;
	}

	public void set(final boolean hasDone) {
		setInner(hasDone);
	}

	private void setInner(final boolean hasDone) {
		if (UnSafe.compareAndSwapInt(this, stateOffset, New, Completing)) {
			outcome = hasDone;
			UnSafe.putOrderedInt(this, stateOffset, Normal);
			finishCompletion();
		}
	}

	private void finishCompletion() {
		for (WaitNode q; (q = waiters) != null;) {
			if (UnSafe.compareAndSwapObject(this, waitersOffset, q, null)) {
				for (;;) {
					Thread t = q.thread;
					if (t != null) {
						q.thread = null;
						LockSupport.unpark(t);
					}
					WaitNode next = q.next;
					if (next == null) {
						break;
					}
					q.next = null;
					q = next;
				}
				break;
			}
		}
		done();
	}

	protected void done() {

	}

	private static final class WaitNode {
		volatile Thread thread;
		volatile WaitNode next;

		WaitNode() {
			thread = Thread.currentThread();
		}
	}

	private static final sun.misc.Unsafe UnSafe;
	private static final long stateOffset;
	private static final long waitersOffset;
	static {
		try {
			UnSafe = GetUnsafe.getUnsafe();
			Class<?> k = JiaoBuObjectFuture.class;
			stateOffset = UnSafe.objectFieldOffset(k.getDeclaredField("state"));
			waitersOffset = UnSafe.objectFieldOffset(k.getDeclaredField("waiters"));
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	
 	
}

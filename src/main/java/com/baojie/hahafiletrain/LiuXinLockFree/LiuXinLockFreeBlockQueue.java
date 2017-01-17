package com.baojie.hahafiletrain.LiuXinLockFree;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class LiuXinLockFreeBlockQueue<E> extends AbstractQueue<E> implements BlockingQueue<E> {
	private volatile Node<E> head, tail;

	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<LiuXinLockFreeBlockQueue, Node> tailUpdater = AtomicReferenceFieldUpdater
			.newUpdater(LiuXinLockFreeBlockQueue.class, Node.class, "tail");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<LiuXinLockFreeBlockQueue, Node> headUpdater = AtomicReferenceFieldUpdater
			.newUpdater(LiuXinLockFreeBlockQueue.class, Node.class, "head");

	private boolean casTail(Node<E> cmp, Node<E> val) {
		return tailUpdater.compareAndSet(this, cmp, val);
	}

	private boolean casHead(Node<E> cmp, Node<E> val) {
		return headUpdater.compareAndSet(this, cmp, val);
	}

	private static class Node<E> {
		E value;
		Node<E> next, prev;

		public Node() {
			value = null;
			next = prev = null;
		}

		public Node(E val) {
			value = val;
			next = prev = null;
		}

		@SuppressWarnings("unused")
		public Node(Node<E> next) {
			value = null;
			prev = null;
			this.next = next;
		}

		@SuppressWarnings("unused")
		public Node<E> getNext() {
			return prev;
		}
	}

	public LiuXinLockFreeBlockQueue(int capacity) {
		if (capacity <= 0) {
			throw new IllegalArgumentException();
		}
		_capacity = new AtomicInteger(capacity);
		_size = new AtomicInteger(0);
		init();
	}

	private Node<E> dummy;

	private void init() {
		dummy = new Node<E>();
		head = dummy;
		tail = dummy;
	}

	public boolean isEmpty() {
		return (head.value == null) && (tail.value == null);
	}

	private void fixList(Node<E> tail, Node<E> head) {
		Node<E> curNode, curNodeNext, nextNodePrev;
		curNode = tail;
		while ((head == this.head) && (curNode != head)) {
			curNodeNext = curNode.next;
			if (null == curNodeNext)
				break;
			nextNodePrev = curNodeNext.prev;
			if (nextNodePrev != curNode) {
				curNodeNext.prev = curNode;
			}
			curNode = curNodeNext;
		}
	}

	public int size() {
		return _size.get();
	}

	public boolean offer(E e) {
		if (e == null)
			throw new NullPointerException();

		int local_capacity = _capacity.get();
		while (true) {
			int local_size = _size.get();
			if (local_size >= local_capacity)
				return false;
			if (_size.compareAndSet(local_size, local_size + 1))
				break;
		}
		Node<E> tail;
		Node<E> node = new Node<E>(e);
		while (true) {
			tail = this.tail;
			node.next = tail;
			if (casTail(tail, node)) {
				tail.prev = node;
				notifyGet_();
				return true;
			}
		}
	}

	public E peek() {
		while (true) {
			Node<E> header = this.head;
			if (header.value != null)
				return header.value;
			Node<E> tail = this.tail;
			if (header == this.head) {
				if (tail == header) {
					return null;
				} else {
					Node<E> fstNodePrev = header.prev;
					if (null == fstNodePrev) {
						fixList(tail, header);
						continue;
					}
					casHead(header, fstNodePrev);
				}
			}
		}
	}

	public E poll() {
		Node<E> tail, head, fstNodePrev;
		E val;
		while (true) {
			head = this.head;
			tail = this.tail;
			fstNodePrev = head.prev;
			val = head.value;
			if (head == this.head) {
				if (val != null) {
					if (tail != head) {
						if (null != fstNodePrev) {
							if (casHead(head, fstNodePrev)) {
								fstNodePrev.next = null;
								int sizetmp1 = _size.get();
								_size.decrementAndGet();
								int sizetmp2 = _size.get();
								if (sizetmp2 < 0) {
									System.out.println("size = " + sizetmp1 + ":" + sizetmp2 + " , val = " + val
											+ ", dummy = " + dummy + ", head = " + head + ", tail = " + tail
											+ ", fstNodePrev = " + fstNodePrev);
								}
								if (val == null) {
									System.out.println("vaule = " + val);
								}
								notifyPut_();
								return val;
							}
						} else {
							fixList(tail, head);
							continue;
						}
					} else {
						Node<E> newdummy = new Node<E>();
						newdummy.next = tail;
						newdummy.prev = null;
						if (casTail(tail, newdummy)) {
							head.prev = newdummy;
						}

						continue;
					}
				} else {
					if (tail == head) {
						if (_size.get() > 0) {
							Thread.yield();
							continue;
						}
						return null;
					} else {
						if (null == fstNodePrev) {
							fixList(tail, head);
							continue;
						}
						casHead(head, fstNodePrev);
					}
				}
			}
		}
	}

	AtomicInteger _size;
	AtomicInteger _capacity;

	private final Object putQueue_ = new Object();
	private int putQueueLen_ = 0;

	private final Object getQueue_ = new Object();
	private int getQueueLen_ = 0;

	private static long WAIT_DURATION = 1000;

	private void notifyGet_() {
		if (getQueueLen_ > 0) {
			synchronized (getQueue_) {
				getQueue_.notify();
			}
		}
	}

	private void waitGet_(long timeout) throws InterruptedException {
		synchronized (getQueue_) {
			try {
				getQueueLen_++;

				if (_size.get() <= 0) {
					getQueue_.wait(timeout);
				}
			} catch (InterruptedException ex) {
				getQueue_.notify();
				throw ex;
			} finally {
				getQueueLen_--;
			}
		}
	}

	private void notifyPut_() {
		if (putQueueLen_ > 0) {
			synchronized (putQueue_) {
				putQueue_.notify();
			}
		}
	}

	private void waitPut_(long timeout) throws InterruptedException {
		synchronized (putQueue_) {
			try {
				putQueueLen_++;
				if (_size.get() >= _capacity.get()) {
					putQueue_.wait(timeout);
				}
			} catch (InterruptedException ex) {
				putQueue_.notify();
				throw ex;
			} finally {
				putQueueLen_--;
			}
		}
	}

	public int capacity() {
		return _capacity.get();
	}

	public void expand(int additionalCapacity) {
		if (additionalCapacity <= 0)
			throw new IllegalArgumentException();

		_capacity.addAndGet(additionalCapacity);

	}

	public boolean offer(E x, long timeout, TimeUnit unit) throws InterruptedException {
		if (x == null)
			throw new NullPointerException();

		if (offer(x))
			return true;

		waitPut_(unit.toMillis(timeout));

		return offer(x);

	}

	public void put(E x) throws InterruptedException {
		if (x == null) {
			throw new IllegalArgumentException();
		}

		while (true) {
			if (offer(x))
				return;
			waitPut_(WAIT_DURATION);
		}
	}

	public E put(E x, long timeoutInMillis) throws InterruptedException {
		if (x == null) {
			throw new IllegalArgumentException();
		}

		if (offer(x)) {
			return x;
		}

		waitPut_(timeoutInMillis);
		if (offer(x)) {
			return x;
		} else
			return null;

	}

	public Object put(E x, long timeoutInMillis, int maximumCapacity) throws InterruptedException {
		if (x == null || maximumCapacity > _capacity.get()) {
			throw new IllegalArgumentException();
		}

		if (offer(x)) {
			return x;
		}

		waitPut_(timeoutInMillis);

		if (offer(x)) {
			return x;
		} else
			return null;
	}

	public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		E res = poll();
		if (res != null)
			return res;

		if (timeout <= 0)
			return null;
		else {
			waitGet_(unit.toMillis(timeout));
			return poll();
		}
	}

	public E take() throws InterruptedException {
		while (true) {
			E res = poll();
			if (res != null)
				return res;

			waitGet_(WAIT_DURATION);
		}
	}

	public void dumpQueue() {
		Node<E> curNode, curNodeNext;
		curNode = tail;
		while ((head == this.head) && (curNode != head)) {
			curNodeNext = curNode.next;
			System.out.print(curNodeNext.value + " -> ");
			curNode = curNodeNext;
		}

	}

	@Override
	public Iterator<E> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int drainTo(Collection<? super E> c) {
		int i = 0;
		do {
			E tmp = poll();
			if (tmp != null) {
				i++;
				c.add(tmp);
			} else
				return i;
		} while (true);
	}

	@Override
	public int drainTo(Collection<? super E> c, int maxElements) {
		int i = 0;
		do {
			E tmp = poll();
			if (tmp != null) {
				i++;
				c.add(tmp);
			} else
				return i;
		} while (i < maxElements);
		return i;
	}

	@Override
	public int remainingCapacity() {
		return _capacity.get() - size();
	}
}

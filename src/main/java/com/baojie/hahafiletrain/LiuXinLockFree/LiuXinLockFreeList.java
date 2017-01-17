package com.baojie.hahafiletrain.LiuXinLockFree;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class LiuXinLockFreeList<E> implements List<E> {

	protected static class Entry<E> {
		E element;
		AtomicMarkableReference<Entry<E>> next;

		public Entry(E e) {
			element = e;
			next = null;
		}

		public Entry(E e, AtomicMarkableReference<Entry<E>> n) {
			element = e;
			next = n;
		}
	}

	protected static class ListStateHolder<E> {
		boolean found;
		AtomicMarkableReference<Entry<E>> prev;
		Entry<E> cur;
		Entry<E> next;

		public ListStateHolder() {
			found = false;
			prev = null;
			cur = null;
			next = null;
		}

		public void casPrev() {
			prev.compareAndSet(cur, next, false, false);
		}

		public boolean markRemoved() {
			return cur.next.compareAndSet(next, next, false, true);
		}
	}

	private class ListItr implements Iterator<E> {
		AtomicMarkableReference<Entry<E>> prev;
		Entry<E> cur;
		Entry<E> next;

		public ListItr() {
			prev = head;
			cur = head.getReference();
			next = null;
		}

		private E advance() {
			if (null == cur) {
				return null;
			}
			try_again: while (true) {
				E curItem = cur.element;
				next = cur.next.getReference();
				if (cur.next.isMarked()) {
					prev.compareAndSet(cur, next, false, false);
					cur = next;
					continue try_again;
				} else {
					prev = cur.next;
					cur = next;
				}
				return curItem;
			}
		}

		public boolean hasNext() {
			return null != cur;
		}

		public E next() {
			E result = advance();

			if (null == result)
				throw new NoSuchElementException();
			else {
				return result;
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	protected AtomicMarkableReference<Entry<E>> head;

	public LiuXinLockFreeList() {
		head = new AtomicMarkableReference<Entry<E>>(null, false);
	}

	public boolean add(E e) {
		if (null == e)
			throw new NullPointerException();
		final Entry<E> newNode = new Entry<E>(e, new AtomicMarkableReference<Entry<E>>(null, false));
		while (true) {
			Entry<E> cur = head.getReference();
			newNode.next.set(cur, false);
			if (head.compareAndSet(cur, newNode, false, false)) {
				return true;
			}
		}
	}

	public void clear() {
		head.set(null, false);
	}

	public boolean contains(Object o) {
		if (null == o)
			throw new NullPointerException();

		ListStateHolder<E> holder = new ListStateHolder<E>();
		return findByObject(o, head, holder).found;
	}

	private ListStateHolder<E> findByObject(Object o, AtomicMarkableReference<Entry<E>> start, ListStateHolder<E> holder) {
		AtomicMarkableReference<Entry<E>> prev;
		Entry<E> cur = null;
		Entry<E> nextEntry = null;
		try_again: while (true) {
			prev = start;
			cur = prev.getReference();
			while (true) {
				if (null == cur) {
					holder.prev = prev;
					holder.cur = cur;
					holder.next = nextEntry;
					holder.found = false;
					return holder;
				}
				AtomicMarkableReference<Entry<E>> nextEntryRef = cur.next;
				nextEntry = nextEntryRef.getReference();
				Object cKey = cur.element;
				if (nextEntryRef.isMarked()) {
					if (!prev.compareAndSet(cur, nextEntry, false, false)) {
						continue try_again;
					}
				} else {
					if (cKey != o && !cKey.equals(o)) {
						prev = nextEntryRef;
					} else {
						holder.found = true;
						holder.prev = prev;
						holder.cur = cur;
						holder.next = nextEntry;
						return holder;
					}
				}
				cur = nextEntry;
			}
		}
	}

	public boolean isEmpty() {
		return null == head.getReference();
	}

	public boolean remove(Object o) {
		if (null == o)
			throw new NullPointerException();

		return remove(o, head);
	}

	private boolean remove(Object o, AtomicMarkableReference<Entry<E>> start) {

		ListStateHolder<E> holder = new ListStateHolder<E>();

		while (true) {

			findByObject(o, start, holder);

			if (!holder.found) {
				return false;
			}
			if (!holder.markRemoved()) {
				continue;
			}

			holder.casPrev();
			return true;
		}
	}

	public Iterator<E> iterator() {
		return new ListItr();
	}

	public int size() {
		int i = 0;
		for (Iterator<E> iter = iterator(); iter.hasNext(); iter.next()) {
			++i;
		}
		return i;
	}

	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E get(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int lastIndexOf(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<E> listIterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}
}

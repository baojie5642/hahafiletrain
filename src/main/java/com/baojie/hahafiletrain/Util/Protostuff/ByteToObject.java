package com.baojie.hahafiletrain.Util.Protostuff;

import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

public class ByteToObject {
	private static final Objenesis objenesis = new ObjenesisStd(true);
	private static final ConcurrentHashMap<Class<?>, Schema<?>> CachedSchema = new ConcurrentHashMap<Class<?>, Schema<?>>();
	private static final ReentrantLock mainLock=new ReentrantLock();
	
	private ByteToObject() {
		super();
	}

	@SuppressWarnings("unchecked")
	private static <T> Schema<T> getSchema(final Class<T> cls) {
		if (null == cls) {
			throw new NullPointerException("cls in getSchema() must not be null");
		}
		Schema<T> schema = (Schema<T>) CachedSchema.get(cls);
		if (schema == null) {
			schema = RuntimeSchema.createFrom(cls);
			if (schema != null) {
				CachedSchema.putIfAbsent(cls, schema);
			}
		}
		return schema;
	}

	public static <T> T toObject(final Class<T> cls, final byte[] bytes) {
		if (null == cls) {
			throw new NullPointerException("cls in toObject() must not be null");
		}
		if (null == bytes) {
			throw new NullPointerException("bytes in toObject() must not be null");
		}
		if (bytes.length == 0) {
			throw new IllegalArgumentException("bytes.length must not be zero");
		}
		T message = null;
		Schema<T> schema = null;
		final ReentrantLock lock=mainLock;
		lock.lock();
		try {
			message = (T) objenesis.newInstance(cls);
			schema = getSchema(cls);
			ProtostuffIOUtil.mergeFrom(bytes, message, schema);
		} finally {
			lock.unlock();
			if (null != schema) {
				schema = null;
			}
		}
		return message;
	}
}

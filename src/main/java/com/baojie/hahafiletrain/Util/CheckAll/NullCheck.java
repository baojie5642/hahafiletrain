package com.baojie.hahafiletrain.Util.CheckAll;

public class NullCheck {

	private NullCheck() {
		super();
	}

	public static void checkNull(final Object... objects) {
		if (null == objects) {
			throw new NullPointerException("this objects must not be null");
		}
		if (objects.length == 0) {
			throw new IllegalArgumentException("those objects.length must not zero");
		}
		for (final Object inner : objects) {
			if (null == inner) {
				throw new NullPointerException("inner in objects must not be null");
			}
		}
	}

	public static void checkEmpty(final String... strings) {
		checkNull(strings);
		for (final String inner : strings) {
			if (inner.length() == 0) {
				throw new IllegalArgumentException("inner in strings must not be empty");
			}
		}
	}

}

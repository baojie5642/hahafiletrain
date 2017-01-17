package com.baojie.hahafiletrain.ThreadAll.ThreadError;

public class UncheckExceptionForcedToRuntimeException {

	public static RuntimeException uncheckExceptionForcedToRuntimeException(final Throwable t) {
		if (null == t) {
			throw new NullPointerException("Throwable must not be null.");
		}
		if (t instanceof RuntimeException) {
			return (RuntimeException) t;
		} else if (t instanceof Error) {
			throw (Error) t;
		} else {
			throw new IllegalStateException("Not unchecked : " + t);
		}
	}
}

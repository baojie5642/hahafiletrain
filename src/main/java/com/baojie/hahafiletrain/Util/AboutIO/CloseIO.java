package com.baojie.hahafiletrain.Util.AboutIO;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.channels.Selector;

public class CloseIO {

	public static void close(final URLConnection conn) {
		if (conn instanceof HttpURLConnection) {
			((HttpURLConnection) conn).disconnect();
		}
	}

	public static void closeQuietly(final Reader input) {
		closeQuietly((Closeable) input);
	}

	public static void closeQuietly(final Writer output) {
		closeQuietly((Closeable) output);
	}

	public static void closeQuietly(final InputStream input) {
		closeQuietly((Closeable) input);
	}

	public static void closeQuietly(final OutputStream output) {
		closeQuietly((Closeable) output);
	}

	public static void closeQuietly(final Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (final IOException ioe) {
			assert true;
		}
	}

	public static void closeQuietly(final Closeable... closeables) {
		if (closeables == null) {
			return;
		}
		for (final Closeable closeable : closeables) {
			closeQuietly(closeable);
		}
	}

	public static void closeQuietly(final Socket sock) {
		if (sock != null) {
			try {
				sock.close();
			} catch (final IOException ioe) {
				assert true;
			}
		}
	}

	public static void closeQuietly(final Selector selector) {
		if (selector != null) {
			try {
				selector.close();
			} catch (final IOException ioe) {
				assert true;
			}
		}
	}

	public static void closeQuietly(final ServerSocket sock) {
		if (sock != null) {
			try {
				sock.close();
			} catch (final IOException ioe) {
				assert true;
			}
		}
	}

}

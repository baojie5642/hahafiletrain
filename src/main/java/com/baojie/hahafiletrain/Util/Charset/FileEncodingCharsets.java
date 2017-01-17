package com.baojie.hahafiletrain.Util.Charset;

import java.nio.charset.Charset;

public class FileEncodingCharsets {

	public static Charset toCharset(final Charset charset) {
		return charset == null ? Charset.defaultCharset() : charset;
	}

	public static Charset toCharset(final String charset) {
		return charset == null ? Charset.defaultCharset() : Charset.forName(charset);
	}

}

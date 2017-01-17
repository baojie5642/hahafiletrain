package com.baojie.hahafiletrain.Util.SysTime;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

public class RightTime {
	public static final String Time_Format_For_Normal = "yyyy-MM-dd HH:mm:ss";
	public static final String Time_Format_For_Compare = "yyyy-MM-dd";
	public static final String Defult_Encoding = "UTF-8";
	private static final byte[] lock = new byte[0];
	private static final ReentrantLock Main_Lock = new ReentrantLock();

	private RightTime() {
		super();
	}

	public static String getSysTime(final String dateStyle, final String encoding) {
		if ((null == dateStyle) || (dateStyle.length() == 0)) {
			if ((null == encoding) || (encoding.length() == 0)) {
				return getSysTime(Time_Format_For_Normal, Defult_Encoding);
			} else {
				return getSysTimePrivte(Time_Format_For_Normal, encoding.trim());
			}
		} else {
			if ((null == encoding) || (encoding.length() == 0)) {
				return getSysTime(dateStyle.trim(), Defult_Encoding);
			} else {
				return getSysTimePrivte(dateStyle.trim(), encoding.trim());
			}
		}
	}

	private static String getSysTimePrivte(final String dateStyle, final String encoding) {
		String timeString = null;
		Date date = null;
		SimpleDateFormat simpleDateFormat = null;
		final long millisTime = System.currentTimeMillis();
		try {
			synchronized (lock) {
				date = new Date(millisTime);
				simpleDateFormat = new SimpleDateFormat(dateStyle);
				timeString = new String(simpleDateFormat.format(date).getBytes(), encoding);
			}
		} catch (UnsupportedEncodingException e) {
			timeString = new String(simpleDateFormat.format(date).getBytes());
			assert true;
		} finally {
			if (null != simpleDateFormat) {
				simpleDateFormat = null;
			}
			if (null != date) {
				date = null;
			}
		}
		return timeString;
	}

	public static int compareTimeByDay(final String nowTime, final String oldTime) {
		final ReentrantLock lock = Main_Lock;
		lock.lock();
		try {
			return compareTimeInner(nowTime, oldTime);
		} finally {
			lock.unlock();
		}
	}

	private static int compareTimeInner(final String nowTime, final String oldTime) {
		int result = 0;
		try {
			final SimpleDateFormat format = new SimpleDateFormat(Time_Format_For_Compare);
			final Calendar c1 = Calendar.getInstance();
			final Calendar c2 = Calendar.getInstance();
			c1.setTime(format.parse(nowTime));
			c2.setTime(format.parse(oldTime));
			result = c1.compareTo(c2);
		} catch (ParseException e) {
			System.out.println("date format error");
			assert true;
		}
		return result;
	}

}

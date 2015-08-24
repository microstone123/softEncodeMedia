package mipt.media;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;

@SuppressLint("SimpleDateFormat")
public class Utils {

	private final static long H = 60 * 60 * 1000;
	private final static long M = 60 * 1000;
	private final static long S = 1000;

	private final static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final static SimpleDateFormat format1 = new SimpleDateFormat("HH:mm");

	@SuppressWarnings("deprecation")
	public static String getHHmm(String dateStr) {
		Date date;
		try {
			synchronized (format) {
				date = format.parse(dateStr);
			}
		} catch (ParseException e) {
			return "";
		}
		String HHmm = null;
		synchronized (format1) {
			HHmm = format1.format(date);
		}
		if (date.getDay() > (new Date()).getDay()) {
			HHmm = "-" + HHmm;
		}
		return HHmm;
	}

	public static Date getDate(String dateStr) {
		Date date = null;
		try {
			synchronized (format) {
				date = format.parse(dateStr);
			}
		} catch (ParseException e) {
		}
		return date;
	}

	public static long getMilliseconds(String dateStr) {
		long date = 0;
		try {
			synchronized (format) {
				date = format.parse(dateStr).getTime();
			}
		} catch (ParseException e) {
		}
		return date;
	}

	@SuppressWarnings("deprecation")
	public static String getHHmm(Date date) {
		String HHmm = null;
		synchronized (format1) {
			HHmm = format1.format(date);
		}
		if (date.getDay() > (new Date()).getDay()) {
			HHmm = "-" + HHmm;
		}
		return HHmm;
	}

	public static int getTimeGapMin(Date preDate, Date afterDate) {
		Long result = (afterDate.getTime() - preDate.getTime()) / (60 * 1000);
		return result.intValue();
	}

	public static String formatDuration(final Context context, int durationMs) {
		int duration = durationMs / 1000;
		int h = duration / 3600;
		int m = duration % 3600 / 60;
		int s = duration % 60;
		String durationValue;
		if (h == 0) {
			if (m == 0 && s == 0) {
				return "00:00";
			}
			durationValue = String.format("%s:%s", getMaxTen(m), getMaxTen(s));
		} else {
			durationValue = String.format("%s:%s:%s", getMaxTen(h), getMaxTen(m), getMaxTen(s));
		}
		return durationValue;
	}

	public static String getMaxTen(int h) {
		return h < 10 ? ("0" + h) : (h + "");
	}

	public static String formatDate(Date date) {
		synchronized (format) {
			return format.format(date);
		}
	}

	public static boolean compareDate(Date preDate, Date nextDate) {
		return preDate.getTime() >= nextDate.getTime();
	}

	public static int formatMillisecondsToMinute(Long milliseconds) {
		int minutes = (int) (milliseconds / M);
		return minutes;
	}

	public static double formatMillisecondsToSeconds(Long milliseconds) {
		double minutes = (int) (milliseconds / S);
		return minutes;
	}
}

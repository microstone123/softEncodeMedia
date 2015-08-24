package mipt.media;

import java.text.DecimalFormat;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.provider.SyncStateContract.Constants;

public class HttpUtil {

	public final static String TAG_DOWNLOAD_SECOND_SPEED_KB_SUF = "KB/S";
	public final static String TAG_DOWNLOAD_SECOND_SPEED_MB_SUF = "MB/S";

	private final static String URL_DEFAULT_CCTV_HLS_OPTION = "hls1";
	private static final String TAG = "HttpUtil";

	private static long m_lSysNetworkSpeedLastTs;
	private static long m_lSystNetworkLastBytes;

	/**
	 * 获取当前应用网络加载速度
	 * 
	 * @param ctx
	 * @return 单位kb
	 * @author: jie
	 * @date: 2014-5-23 上午11:20:25
	 */
	public static float getUidRxBytes(Context ctx) {
		long nowBytes = TrafficStats.getTotalRxBytes();
		// long nowBytes = TrafficStats
		// .getUidRxBytes(ctx.getApplicationInfo().uid);

		if (nowBytes == TrafficStats.UNSUPPORTED) {
			return 0;
		}
		long nowMS = System.currentTimeMillis();

		long timeinterval = nowMS - m_lSysNetworkSpeedLastTs;
		long bytes = nowBytes - m_lSystNetworkLastBytes;
		float m_fSysNetowrkLastSpeed = 0;

		if (timeinterval > 0) {
			m_fSysNetowrkLastSpeed = (float) bytes * 1.0f / ((float) timeinterval / 1000);
		}

		m_lSysNetworkSpeedLastTs = nowMS;
		m_lSystNetworkLastBytes = nowBytes;

		return convert(m_fSysNetowrkLastSpeed);
	}

	private static float convert(float value) {
		int lg = Math.round(value * 100); // 四舍五入
		float d = lg / 100.0f; // 注意：使用 100.0而不是 100
		return d;
	}

	/**
	 * 获取当前应用网络加载速度
	 * 
	 * @param ctx
	 * @return 已经转化单位MB/S，KB/S
	 * @author: jie
	 * @date: 2014-5-27 下午2:39:42
	 */
	public static String getUidRxBytesStr(Context ctx) {
		return formatSpeedStr(getUidRxBytes(ctx));
	}

	public static String formatSpeedStr(float Bps) {
		DecimalFormat df = new DecimalFormat("#.#");
		String speedStr = null;
		if (Bps >= 1024 * 1024) {
			float fs = (Bps / 1024.0f / 1024.0f);
			if (fs >= 1000.0f) {
				speedStr = new StringBuilder().append((int) fs).append(TAG_DOWNLOAD_SECOND_SPEED_MB_SUF).toString();
			} else {
				speedStr = new StringBuilder().append(df.format(fs)).append(TAG_DOWNLOAD_SECOND_SPEED_MB_SUF)
						.toString();
			}
		} else if (Bps >= 1024) {
			float fs = (Bps / 1024.0f);
			speedStr = new StringBuilder().append((int) fs).append(TAG_DOWNLOAD_SECOND_SPEED_KB_SUF).toString();
		} else {
			speedStr = new StringBuilder().append("0").append(TAG_DOWNLOAD_SECOND_SPEED_KB_SUF).toString();
		}
		return speedStr;
	}

	public static String parseUrlParam(String url, String paramName) {
		return parseUrlParam(url, paramName, URL_DEFAULT_CCTV_HLS_OPTION);
	}

	/**
	 * 获取url的参数值
	 * 
	 * @param url
	 * @param paramName
	 *            参数名
	 * @return
	 * @author: jie
	 * @date: 2014-6-16 下午3:37:02
	 */
	public static String parseUrlParam(String url, String paramName, String defaultParam) {
		int paramsStart = url.lastIndexOf("?");
		if (paramsStart == -1) {
			return defaultParam;
		}
		String paramsStr = url.substring(paramsStart);
		int start = paramsStr.indexOf(paramName);
		if (start == -1) {
			return defaultParam;
		}
		String endStr = paramsStr.substring(start);
		String paramStr;
		int end = endStr.indexOf("&");
		if (end == -1) {
			paramStr = endStr.substring(0);
		} else {
			paramStr = endStr.substring(0, end);
		}
		return paramStr.split("=")[1];
	}

	// public static String getHost(Context ctx) {
	// // String domain = PrefHelper.getInstance(ctx).getString(
	// // Constants.PREF_DOMAIN, Constants.HOST);
	// return null;
	// }

	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}
}

package mipt.media;

import android.content.Context;

public class SpeedThread extends Thread {

	private Context context;
	private static SpeedThread instance;

	public SpeedThread(Context context) {
		super();
		this.context = context;
	}

	public static SpeedThread getSpeedInstance(Context context) {
		if (instance == null) {
			instance = new SpeedThread(context);
		}

		return instance;
	}

	@Override
	public void run() {
		super.run();
		while (!stopRefresh) {

			try {

				if (refreshSpeedListener != null) {
					String playerSpeed = HttpUtil.getUidRxBytesStr(context);
					refreshSpeedListener.refreshSpeed(playerSpeed);
				}
				Thread.sleep(1000);
			} catch (InterruptedException e) {

				e.printStackTrace();
				return;
			}

		}
	}

	public void setRefreshSpeedListener(RefreshSpeedListener refreshSpeedListener) {
		this.refreshSpeedListener = refreshSpeedListener;
	}

	public interface RefreshSpeedListener {

		public void refreshSpeed(String speed);

	}

	private RefreshSpeedListener refreshSpeedListener;
	private boolean stopRefresh;

	public void clear() {
		// notifyRefresh();
		instance = null;

	}

	private synchronized void waitRefesh() {
		try {
			wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void notifyRefresh() {
		notify();
	}

	public void interruptRefresh() {
		stopRefresh = true;

	}

}

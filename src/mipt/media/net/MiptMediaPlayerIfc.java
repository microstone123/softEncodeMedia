package mipt.media.net;

import android.content.Context;

public interface MiptMediaPlayerIfc extends MediaPlayerIfc {

	public void resume() throws IllegalStateException;

	@Deprecated
	public void setWakeMode(Context context, int mode);

	public static interface OnVideoSizeChangedListener {
		public void onVideoSizeChanged(android.media.MediaPlayer mp, int width, int height, int sar_num, int sar_den);
	}

	public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener);

	public String getDataSource();
}

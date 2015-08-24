package mediaplayer;

import java.io.IOException;

import mediaplayer.MediaPlayerCustom.SurfaceViewListener;
import mediaplayer.MediaPlayerCustom.SurfaceHolderCallbcak;

import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public interface MediaPlayerCustomIfc {

	public static final int MEDIA_PREPARE_TIMEOUT_MSG = 1;
	public static final int MEDIA_PREPARE_TIMEOUT = 8 * 1000;
	public static final int MEDIA_ONINFO_DELAY_MSG = 2;
	public static final int MEDIA_ONINFO_DELAY = 2 * 1000;
	public static final int MEDIA_SHOW_SURFACEVIEW_MSG = 3;
	public static final int MEDIA_HIDE_SURFACEVIEW_MSG = 4;

	public void seekTo(long msec) throws IllegalStateException;

	public int getCurrentPosition();

	public int getVideoWidth();

	public int getVideoHeight();

	public int getDuration();

	public void release();

	public void reset();

	public boolean isPlaying();

	public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException,
			IllegalStateException;

	public void setDisplay(SurfaceView sw, SurfaceHolderCallbcak callback);

	public void prepareAsync() throws IllegalStateException;

	public void prepare() throws IllegalStateException, IOException;

	public void start() throws IllegalStateException;

	public void stop() throws IllegalStateException;

	public void pause() throws IllegalStateException;

	/*--------------------
	 * Listeners
	 */
	public void setOnPreparedListener(OnPreparedListener listener, boolean toDelay);

	public void setOnCompletionListener(OnCompletionListener listener);

	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener);

	public void setOnSeekCompleteListener(OnSeekCompleteListener listener);

	public void setOnVideoSizeChangedListener(android.media.MediaPlayer.OnVideoSizeChangedListener listener);

	public void setOnErrorListener(OnErrorListener listener);

	public void setOnInfoListener(OnInfoListener listener);

	public static interface OnVideoSizeChangedListener {
		public void onVideoSizeChanged(android.media.MediaPlayer mp, int width, int height, int sar_num, int sar_den);
	}

	public void addCallbackListener(SurfaceHolderCallbcak callback);

	public void setSurfaceViewListener(SurfaceViewListener listener);

	public interface TimeOutListener {
		public void prepareTimeOut();

	}

	public void setTimeoutListener(TimeOutListener timeOutListener);
}

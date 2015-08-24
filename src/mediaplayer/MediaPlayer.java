package mediaplayer;

import java.io.IOException;

import mipt.media.net.AndroidMediaPlayer;
import mipt.media.net.MediaPlayerIfc;
import mipt.media.net.MiptMediaPlayerIfc;
import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnTimedTextListener;
import android.media.TimedText;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MediaPlayer implements MiptMediaPlayerIfc {

	private OnPreparedListener mOnPreparedListener;
	private OnCompletionListener mOnCompletionListener;
	private OnBufferingUpdateListener mOnBufferingUpdateListener;
	private OnSeekCompleteListener mOnSeekCompleteListener;
	private android.media.MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener;
	private OnErrorListener mOnErrorListener;
	private OnInfoListener mOnInfoListener;
	private OnTimedTextListener mOnTimedTextListener;
	private SurfaceView surfaceView;
	private SurfaceHolder mSurfaceHolder;
	private String url;
	private boolean screenOn;

	public enum TYPE {
		ANDROID, MIPT, AUTO_ANDROID, AUTO_MIPT
	}

	private TYPE type = TYPE.AUTO_ANDROID;

	public TYPE getType() {
		if (mediaPlayerAndroid instanceof AndroidMediaPlayer) {
			return TYPE.ANDROID;
		} else if (mediaPlayerAndroid instanceof mipt.media.net.MediaPlayer) {
			return TYPE.MIPT;
		}
		return type;
	}

	/*
	 * android
	 */
	private MediaPlayerIfc mediaPlayerAndroid;
	private android.media.MediaPlayer.OnPreparedListener mOnPreparedListenerAndroid;
	private android.media.MediaPlayer.OnCompletionListener mOnCompletionListenerAndroid;
	private android.media.MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListenerAndroid;
	private android.media.MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListenerAndroid;
	private android.media.MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListenerAndroid;
	private android.media.MediaPlayer.OnErrorListener mOnErrorListenerAndroid;
	private android.media.MediaPlayer.OnInfoListener mOnInfoListenerAndroid;
	private android.media.MediaPlayer.OnTimedTextListener mOnTimedTextListenerAndroid;

	/*
	 * mipt
	 */
	// private mipt.media.net.MediaPlayer mediaPlayerMipt;

	public MediaPlayer(TYPE type) {
		this.type = type;
		if (this.type == TYPE.ANDROID || (this.type == TYPE.AUTO_ANDROID)) {
			mediaPlayerAndroid = new AndroidMediaPlayer();
		} else if (this.type == TYPE.MIPT || (this.type == TYPE.AUTO_MIPT)) {
			mediaPlayerAndroid = new mipt.media.net.MediaPlayer();
		} else {
			mediaPlayerAndroid = new AndroidMediaPlayer();
		}
	}

	public MediaPlayer() {
		mediaPlayerAndroid = new AndroidMediaPlayer();
	}

	public void setMediaPlayerType(TYPE type) {

		clearMediaPlayer();
		this.type = type;
		if (this.type == TYPE.ANDROID || (this.type == TYPE.AUTO_ANDROID)) {
			mediaPlayerAndroid = new AndroidMediaPlayer();
		} else if (this.type == TYPE.MIPT || (this.type == TYPE.AUTO_MIPT)) {
			mediaPlayerAndroid = new mipt.media.net.MediaPlayer();
		} else {
			mediaPlayerAndroid = new AndroidMediaPlayer();
		}
	}

	public void clearMediaPlayer() {
		if (mediaPlayerAndroid != null) {
			mediaPlayerAndroid.reset();
			mediaPlayerAndroid.release();
			mediaPlayerAndroid = null;
		}

	}

	public void clearSurfaceView() {
		if (mSurfaceHolder != null) {
			Surface surface = mSurfaceHolder.getSurface();
			if (surface != null) {
				surface.release();
				surface = null;
			}
			mSurfaceHolder = null;
		}
	}

	private SurfaceViewListener listener;

	public interface SurfaceViewListener {
		public void creatSurfaceView();

		public void showEncodeToast(TYPE type);
	}

	private void startMiptMediaPlayer() throws IllegalArgumentException, SecurityException, IllegalStateException,
			IOException {

		if (type == TYPE.AUTO_ANDROID) {
			mediaPlayerAndroid = new mipt.media.net.MediaPlayer();

			if (listener != null) {
				listener.showEncodeToast(TYPE.MIPT);
			}
			type = TYPE.MIPT;
		}

		if (type == TYPE.AUTO_MIPT) {
			mediaPlayerAndroid = new AndroidMediaPlayer();

			if (listener != null) {
				listener.showEncodeToast(TYPE.ANDROID);
			}
			type = TYPE.ANDROID;
		}

		if (mSurfaceHolder == null) {
			if (listener != null)
				listener.creatSurfaceView();
		} else {
			mediaPlayerAndroid.setDisplay(mSurfaceHolder);
		}

		if (mOnPreparedListener != null)
			setOnPreparedListener(mOnPreparedListener);
		if (mOnCompletionListener != null)
			setOnCompletionListener(mOnCompletionListener);
		if (mOnBufferingUpdateListener != null)
			setOnBufferingUpdateListener(mOnBufferingUpdateListener);
		if (mOnSeekCompleteListener != null)
			setOnSeekCompleteListener(mOnSeekCompleteListener);
		if (mOnVideoSizeChangedListener != null)
			setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
		if (mOnErrorListener != null)
			setOnErrorListener(mOnErrorListener);
		if (mOnInfoListener != null)
			setOnInfoListener(mOnInfoListener);

		setDataSource(url);
		setScreenOnWhilePlaying(screenOn);
		prepareAsync();
	}

	@Override
	public void setDisplay(SurfaceHolder sh) {
		try {
			mSurfaceHolder = sh;
			if (mediaPlayerAndroid != null)
				mediaPlayerAndroid.setDisplay(mSurfaceHolder);
		} catch (IllegalArgumentException e) {
			if (listener != null) {
				listener.creatSurfaceView();
			}
			e.printStackTrace();
		}
	}

	@Override
	public void setDataSource(String... params) throws IOException, IllegalArgumentException, SecurityException,
			IllegalStateException {
		url = params[0];
		if (mediaPlayerAndroid != null)
			mediaPlayerAndroid.setDataSource(params);
	}

	@Override
	public void prepareAsync() throws IllegalStateException {
		if (mediaPlayerAndroid != null)
			mediaPlayerAndroid.prepareAsync();
	}

	@Override
	public void prepare() throws IllegalStateException, IOException {
		if (mediaPlayerAndroid != null)
			mediaPlayerAndroid.prepare();
	}

	@Override
	public void start() throws IllegalStateException {
		if (mediaPlayerAndroid != null) {
			// if(mediaPlayerAndroid instanceof AndroidMediaPlayer){
			// mediaPlayerAndroid.start();
			// } else if(mediaPlayerAndroid instanceof
			// mipt.media.net.MediaPlayer){
			mediaPlayerAndroid.start();
			// }
		}
	}

	@Override
	public void stop() throws IllegalStateException {
		if (mediaPlayerAndroid != null)
			mediaPlayerAndroid.stop();
	}

	@Override
	public void pause() throws IllegalStateException {
		if (mediaPlayerAndroid != null)
			mediaPlayerAndroid.pause();
	}

	@Override
	public void setScreenOnWhilePlaying(boolean screenOn) {
		this.screenOn = screenOn;
		if (mediaPlayerAndroid != null)
			mediaPlayerAndroid.setScreenOnWhilePlaying(screenOn);
	}

	@Override
	public int getVideoWidth() {
		if (mediaPlayerAndroid != null)
			return mediaPlayerAndroid.getVideoWidth();
		return 0;
	}

	@Override
	public int getVideoHeight() {
		if (mediaPlayerAndroid != null)
			return mediaPlayerAndroid.getVideoHeight();
		return 0;
	}

	@Override
	public boolean isPlaying() {
		if (mediaPlayerAndroid != null)
			return mediaPlayerAndroid.isPlaying();
		return false;
	}

	@Override
	public void seekTo(int msec) throws IllegalStateException {
		if (mediaPlayerAndroid != null) {
			if (mediaPlayerAndroid instanceof AndroidMediaPlayer) {
				mediaPlayerAndroid.seekTo(msec);
			} else if (mediaPlayerAndroid instanceof mipt.media.net.MediaPlayer) {
				mediaPlayerAndroid.seekTo(msec);
			}
		}
	}

	@Override
	public int getCurrentPosition() {
		if (mediaPlayerAndroid != null)
			return mediaPlayerAndroid.getCurrentPosition();
		return 0;
	}

	@Override
	public int getDuration() {
		if (mediaPlayerAndroid != null)
			return mediaPlayerAndroid.getDuration();
		return 0;
	}

	@Override
	public void release() {
		if (mediaPlayerAndroid != null) {
			mediaPlayerAndroid.release();
		}
	}

	@Override
	public void reset() {
		if (mediaPlayerAndroid != null)
			mediaPlayerAndroid.reset();
	}

	@Override
	public void setOnPreparedListener(OnPreparedListener listener) {
		mOnPreparedListener = listener;
		mOnPreparedListenerAndroid = new android.media.MediaPlayer.OnPreparedListener() {

			@Override
			public void onPrepared(android.media.MediaPlayer mp) {
				mOnPreparedListener.onPrepared(mp);

			}
		};

		mediaPlayerAndroid.setOnPreparedListener(mOnPreparedListenerAndroid);
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		mOnCompletionListener = listener;
		mOnCompletionListenerAndroid = new android.media.MediaPlayer.OnCompletionListener() {

			@Override
			public void onCompletion(android.media.MediaPlayer mp) {
				mOnCompletionListener.onCompletion(mp);
			}
		};
		mediaPlayerAndroid.setOnCompletionListener(mOnCompletionListenerAndroid);

	}

	@Override
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		mOnBufferingUpdateListener = listener;
		mOnBufferingUpdateListenerAndroid = new android.media.MediaPlayer.OnBufferingUpdateListener() {

			@Override
			public void onBufferingUpdate(android.media.MediaPlayer mp, int percent) {
				mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
			}
		};
		mediaPlayerAndroid.setOnBufferingUpdateListener(mOnBufferingUpdateListenerAndroid);

	}

	@Override
	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		mOnSeekCompleteListener = listener;
		mOnSeekCompleteListenerAndroid = new android.media.MediaPlayer.OnSeekCompleteListener() {

			@Override
			public void onSeekComplete(android.media.MediaPlayer mp) {
				mOnSeekCompleteListener.onSeekComplete(mp);

			}
		};
		mediaPlayerAndroid.setOnSeekCompleteListener(mOnSeekCompleteListenerAndroid);

	}

	@Override
	public void setOnVideoSizeChangedListener(android.media.MediaPlayer.OnVideoSizeChangedListener listener) {
		mOnVideoSizeChangedListener = listener;
		mOnVideoSizeChangedListenerAndroid = new android.media.MediaPlayer.OnVideoSizeChangedListener() {

			@Override
			public void onVideoSizeChanged(android.media.MediaPlayer mp, int width, int height) {
				mOnVideoSizeChangedListener.onVideoSizeChanged(mp, width, height);

			}
		};
		mediaPlayerAndroid.setOnVideoSizeChangedListener(mOnVideoSizeChangedListenerAndroid);

	}

	@Override
	public void setOnErrorListener(OnErrorListener listener) {
		mOnErrorListener = listener;
		mOnErrorListenerAndroid = new android.media.MediaPlayer.OnErrorListener() {

			@Override
			public boolean onError(android.media.MediaPlayer mp, int what, int extra) {
				if (type == TYPE.AUTO_ANDROID) {
					clearMediaPlayer();
					try {
						startMiptMediaPlayer();
					} catch (Exception e) {
						e.printStackTrace();
						return true;
					}
				} else if (type == TYPE.AUTO_MIPT) {
					clearMediaPlayer();
					try {
						startMiptMediaPlayer();
					} catch (Exception e) {
						e.printStackTrace();
						return true;
					}
				} else {
					return mOnErrorListener.onError(mp, what, extra);
				}
				return false;
			}
		};
		mediaPlayerAndroid.setOnErrorListener(mOnErrorListenerAndroid);
	}

	@Override
	public void setOnInfoListener(OnInfoListener listener) {
		mOnInfoListener = listener;
		mOnInfoListenerAndroid = new android.media.MediaPlayer.OnInfoListener() {

			@Override
			public boolean onInfo(android.media.MediaPlayer mp, int what, int extra) {
				return mOnInfoListener.onInfo(mp, what, extra);
			}
		};
		mediaPlayerAndroid.setOnInfoListener(mOnInfoListenerAndroid);

	}

	@SuppressLint("NewApi")
	@Override
	public void setOnTimedTextListener(OnTimedTextListener listener) {
		mOnTimedTextListener = listener;
		mOnTimedTextListenerAndroid = new android.media.MediaPlayer.OnTimedTextListener() {

			@Override
			public void onTimedText(android.media.MediaPlayer mp, TimedText text) {
				mOnTimedTextListener.onTimedText(mp, text);
			}
		};
		mediaPlayerAndroid.setOnTimedTextListener(mOnTimedTextListenerAndroid);
	}

	public SurfaceViewListener getSurfaceViewListener() {
		return listener;
	}

	public void setSurfaceViewListener(SurfaceViewListener listener) {
		this.listener = listener;
	}

	@Override
	public String getDataSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resume() throws IllegalStateException {
		if (mediaPlayerAndroid != null) {
			((MiptMediaPlayerIfc) mediaPlayerAndroid).resume();
		}

	}

	@Override
	@Deprecated
	public void setWakeMode(Context context, int mode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {

	}

}

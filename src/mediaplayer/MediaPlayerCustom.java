package mediaplayer;

import java.io.IOException;

import mipt.media.net.AndroidMediaPlayer;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnTimedTextListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;

public class MediaPlayerCustom implements MediaPlayerCustomIfc {
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private mipt.media.net.MediaPlayerIfc mediaPlayerAndroid;
	private String playUrl;
	private static boolean hasCreated;
	private boolean hasStared;
	private boolean toDelay;
	private static MediaPlayerCustom instance;

	public static MediaPlayerCustom getMediaPlayer1() {
		hasCreated = false;

		if (instance == null) {
			instance = new MediaPlayerCustom();
		}

		return instance;
	}

	private Callback callback = new Callback() {

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {

		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {

			// mediaPlayerAndroid.setDisplay(holder);

			if (surfaceHolderCallbcak != null) {

				if (!hasCreated) {
					surfaceHolderCallbcak.surfaceCreated();
				}
				hasCreated = true;
				return;
			}

			if (!hasCreated) {
				startMediaPlayer(false, playUrl);
			}

			hasCreated = true;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			surfaceHolder = holder;

		}
	};
	/*
	 * lsitener
	 */
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			int action = msg.what;
			switch (action) {
			case MEDIA_PREPARE_TIMEOUT_MSG:
				if (mediaPlayerAndroid != null && !mediaPlayerAndroid.isPlaying()) {
					if (timeOutListener != null) {
						timeOutListener.prepareTimeOut();
					}
				}

				break;
			case MEDIA_SHOW_SURFACEVIEW_MSG:
				surfaceView.setVisibility(View.VISIBLE);
				break;
			case MEDIA_HIDE_SURFACEVIEW_MSG:
				surfaceView.setVisibility(View.INVISIBLE);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}

	};
	private OnPreparedListener mOnPreparedListenerAndroid;
	private OnCompletionListener mOnCompletionListenerAndroid;
	private OnBufferingUpdateListener mOnBufferingUpdateListenerAndroid;
	private OnSeekCompleteListener mOnSeekCompleteListenerAndroid;
	private android.media.MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListenerAndroid;
	private OnErrorListener mOnErrorListenerAndroid;
	private OnErrorListener mOnErrorListener;
	private OnInfoListener mOnInfoListenerAndroid;
	private OnTimedTextListener mOnTimedTextListenerAndroid;
	private SurfaceHolderCallbcak surfaceHolderCallbcak;
	private SurfaceViewListener surfaceViewListener;
	private TimeOutListener timeOutListener;

	public interface SurfaceHolderCallbcak {
		public void surfaceCreated();
	}

	public interface SurfaceViewListener {
		public void creatSurfaceView();

		public void showEncodeToast(TYPE type);
	}

	public enum TYPE {
		ANDROID, MIPT, AUTO_ANDROID, AUTO_MIPT
	}

	private TYPE type = TYPE.AUTO_ANDROID;

	public TYPE getType() {
		if (mediaPlayerAndroid == null) {
			return type;
		}
		if (mediaPlayerAndroid instanceof AndroidMediaPlayer) {
			return TYPE.ANDROID;
		} else if (mediaPlayerAndroid instanceof mipt.media.net.MediaPlayer) {
			return TYPE.MIPT;
		}
		return type;
	}

	public void setMediaType(TYPE name) {
		type = name;
	}

	public MediaPlayerCustom(TYPE type) {
		this.type = type;
	}

	public MediaPlayerCustom() {
		type = TYPE.ANDROID;

	}

	public void clearMediaPlayer() {
		if (mediaPlayerAndroid != null) {
			mediaPlayerAndroid.reset();
			mediaPlayerAndroid.release();
			mediaPlayerAndroid = null;
		}
		hasStared = false;
	}

	public void startMediaPlayer(boolean switchMedia, String path) {

		playUrl = path;
		Log.i("", "current type is ----" + getType());
		if (getType() == TYPE.MIPT) {
			if (surfaceView != null) {
				surfaceView.setVisibility(View.INVISIBLE);
				// sendHandlerMessage(MEDIA_HIDE_SURFACEVIEW_MSG, 0);
			}
		}
		clearMediaPlayer();
		if (switchMedia) {
			switchMedia();
		} else {
			creatMedia();
		}

		creatSurfaceHolder();

		mediaPlayerAndroid.setDisplay(surfaceHolder);

		setListener();

		try {
			mediaPlayerAndroid.setDataSource(playUrl);
			mediaPlayerAndroid.prepareAsync();

			sendHandlerMessage(MEDIA_PREPARE_TIMEOUT_MSG, MEDIA_PREPARE_TIMEOUT);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendHandlerMessage(int mediaPrepareTimeoutMsg, int mediaPrepareTimeout) {
		if (handler != null) {
			Message message = handler.obtainMessage(MEDIA_PREPARE_TIMEOUT_MSG);
			handler.removeMessages(MEDIA_PREPARE_TIMEOUT_MSG);
			handler.sendMessageDelayed(message, mediaPrepareTimeout);
		}

	}

	private void creatMedia() {

		if (this.type == TYPE.ANDROID || (this.type == TYPE.AUTO_ANDROID)) {
			mediaPlayerAndroid = new AndroidMediaPlayer();
		} else if (this.type == TYPE.MIPT || (this.type == TYPE.AUTO_MIPT)) {
			mediaPlayerAndroid = new mipt.media.net.MediaPlayer();
		} else {
			mediaPlayerAndroid = new AndroidMediaPlayer();
		}
	}

	private void switchMedia() {
		TYPE currentType = type;
		if (currentType == TYPE.AUTO_MIPT) {
			mediaPlayerAndroid = new AndroidMediaPlayer();
		} else if (currentType == TYPE.AUTO_ANDROID) {
			mediaPlayerAndroid = new mipt.media.net.MediaPlayer();
		} else {
			mediaPlayerAndroid = new AndroidMediaPlayer();
		}

	}

	private void creatSurfaceHolder() {

		if (surfaceHolder == null || (surfaceView != null && !surfaceView.isShown())) {
			if (surfaceView == null) {
				if (surfaceViewListener != null) {
					surfaceViewListener.creatSurfaceView();
				}

			}
			surfaceHolder = surfaceView.getHolder();
			surfaceHolder.addCallback(callback);

			// sendHandlerMessage(MEDIA_SHOW_SURFACEVIEW_MSG, 0);
			surfaceView.setVisibility(View.VISIBLE);
		}

	}

	private void setListener() {
		setOnBufferingUpdateListener(mOnBufferingUpdateListenerAndroid);
		setOnCompletionListener(mOnCompletionListenerAndroid);
		setOnErrorListener(mOnErrorListenerAndroid);
		setOnInfoListener(mOnInfoListenerAndroid);
		setOnSeekCompleteListener(mOnSeekCompleteListenerAndroid);
		setOnPreparedListener(mOnPreparedListenerAndroid, toDelay);
		setOnVideoSizeChangedListener(mOnVideoSizeChangedListenerAndroid);
		setSurfaceViewListener(surfaceViewListener);

	}

	@Override
	public void setDisplay(SurfaceView sw, SurfaceHolderCallbcak surfaceCallback) {
		surfaceHolderCallbcak = surfaceCallback;
		surfaceView = sw;

		creatSurfaceHolder();

		// creatMedia();

	}

	@Override
	public void prepareAsync() throws IllegalStateException {
		if (mediaPlayerAndroid != null) {
			mediaPlayerAndroid.prepareAsync();
		}

	}

	@Override
	public void prepare() throws IllegalStateException, IOException {
		if (mediaPlayerAndroid != null) {
			mediaPlayerAndroid.prepare();
		}

	}

	@Override
	public void start() throws IllegalStateException {

		if (mediaPlayerAndroid != null) {
			// if (mediaPlayerAndroid instanceof mipt.media.net.MediaPlayer) {
			// if (hasStared) {
			// mediaPlayerAndroid.resume();
			// return;
			// }
			// }

			mediaPlayerAndroid.start();
		}

		hasStared = true;
	}

	@Override
	public void stop() throws IllegalStateException {
		if (mediaPlayerAndroid != null) {
			mediaPlayerAndroid.stop();
		}

	}

	@Override
	public void pause() throws IllegalStateException {
		if (mediaPlayerAndroid != null) {
			if (mediaPlayerAndroid.isPlaying()) {
				mediaPlayerAndroid.pause();
			} else {
				clearMediaPlayer();
			}

		}

	}

	class OnPreparedListenerDelay implements OnPreparedListener {

		private boolean prepareToDelay;

		public OnPreparedListenerDelay(boolean toDelay) {
			super();
			this.prepareToDelay = toDelay;
		}

		@Override
		public void onPrepared(MediaPlayer mp) {
			if (mOnPreparedListenerAndroid != null) {
				mOnPreparedListenerAndroid.onPrepared(mp);
			}

			if (prepareToDelay) {
				sendHandlerMessage(MEDIA_ONINFO_DELAY_MSG, MEDIA_ONINFO_DELAY);
			}
		}

	};

	/*
	 * toDelay:weather to send oninfoMessage delay
	 */
	@Override
	public void setOnPreparedListener(OnPreparedListener listener, boolean delay) {
		this.toDelay = delay;
		mOnPreparedListenerAndroid = listener;

		if (mediaPlayerAndroid != null) {
			mediaPlayerAndroid.setOnPreparedListener(new OnPreparedListenerDelay(toDelay));
		}
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		mOnCompletionListenerAndroid = listener;
		if (mediaPlayerAndroid != null) {
			mediaPlayerAndroid.setOnCompletionListener(mOnCompletionListenerAndroid);
		}
	}

	@Override
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		mOnBufferingUpdateListenerAndroid = listener;
		if (mediaPlayerAndroid != null) {
			mediaPlayerAndroid.setOnBufferingUpdateListener(mOnBufferingUpdateListenerAndroid);
		}

	}

	@Override
	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		mOnSeekCompleteListenerAndroid = listener;
		if (mediaPlayerAndroid != null) {
			mediaPlayerAndroid.setOnSeekCompleteListener(mOnSeekCompleteListenerAndroid);
		}
	}

	@Override
	public void setOnVideoSizeChangedListener(android.media.MediaPlayer.OnVideoSizeChangedListener listener) {
		mOnVideoSizeChangedListenerAndroid = listener;
		if (mediaPlayerAndroid != null) {
			mediaPlayerAndroid.setOnVideoSizeChangedListener(mOnVideoSizeChangedListenerAndroid);
		}

	}

	@Override
	public void setOnErrorListener(OnErrorListener listener) {
		mOnErrorListenerAndroid = listener;
		if (mediaPlayerAndroid != null) {
			mOnErrorListener = new OnErrorListener() {

				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					if (toSwtichMedia()) {
						startMediaPlayer(true, playUrl);
						return false;
					}

					return mOnErrorListenerAndroid.onError(mp, what, extra);
				}

			};
			mediaPlayerAndroid.setOnErrorListener(mOnErrorListener);
		}

	}

	private boolean toSwtichMedia() {
		// TYPE currentType = getType();
		if (type == TYPE.AUTO_ANDROID || type == TYPE.AUTO_MIPT) {
			return true;
		}
		return false;
	}

	@Override
	public void setOnInfoListener(OnInfoListener listener) {
		mOnInfoListenerAndroid = listener;
		if (mediaPlayerAndroid != null) {
			mediaPlayerAndroid.setOnInfoListener(mOnInfoListenerAndroid);
		}

	}

	// @Override
	// public void resume() throws IllegalStateException {
	// if(mediaPlayerAndroid instanceof AndroidMediaPlayer){
	// mediaPlayerAndroid.start();
	// }else{
	// mediaPlayerAndroid.resume();
	// }
	//
	// }

	@Override
	public void seekTo(long msec) throws IllegalStateException {
		if (mediaPlayerAndroid != null) {
			mediaPlayerAndroid.seekTo((int) msec);
		}

	}

	@Override
	public int getCurrentPosition() {
		if (mediaPlayerAndroid != null) {
			return mediaPlayerAndroid.getCurrentPosition();
		}
		return 0;
	}

	@Override
	public int getDuration() {
		if (mediaPlayerAndroid != null) {
			return mediaPlayerAndroid.getDuration();
		}
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
		if (mediaPlayerAndroid != null) {
			mediaPlayerAndroid.reset();
		}

	}

	@Override
	public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException,
			IllegalStateException {
		playUrl = path;

	}

	@Override
	public void addCallbackListener(SurfaceHolderCallbcak callback) {
		surfaceHolderCallbcak = callback;

	}

	@Override
	public void setSurfaceViewListener(mediaplayer.MediaPlayerCustom.SurfaceViewListener listener) {
		surfaceViewListener = listener;

	}

	@Override
	public void setTimeoutListener(TimeOutListener listener) {
		timeOutListener = listener;
	}

	@Override
	public boolean isPlaying() {
		if (mediaPlayerAndroid != null) {
			return mediaPlayerAndroid.isPlaying();
		}

		return false;

	}

	public void destoryMediaPlayer() {
		clearMediaPlayer();
		type = TYPE.ANDROID;
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

	public void notifyError() {
		if (mOnErrorListener != null) {
			mOnErrorListener.onError(null, 0, 0);
		}
	}
}

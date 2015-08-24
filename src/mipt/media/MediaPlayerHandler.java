package mipt.media;

import java.io.IOException;

import mipt.media.net.AndroidMediaPlayer;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;

public class MediaPlayerHandler implements MediaPlayerHandlerIfc {
	private final static String TAG = MediaPlayerHandler.class.getName();

	private final static long DEFAUTL_TIME_OUT_MILLISECONDES = 8000;
	private final static long DEFAUTL_LOADING_TIME_OUT_MILLISECONDES = 2000;

	private mipt.media.net.MediaPlayerIfc player;

	public mipt.media.net.MediaPlayerIfc getPlayer() {
		return player;
	}

	private SurfaceView view;
	private SurfaceHolder viewHolder;

	private MediaStatusCallback callback;

	private static MediaPlayerHandler instance;

	private String[] params;
	private boolean isSurfaceInit = true;
	private boolean isPause = false;
	private boolean hasPlayed = false;
	private boolean autoErrorChangeType = false;
	private int lastPlayPosition = -1;
	private long prepareTimeOutMills = DEFAUTL_LOADING_TIME_OUT_MILLISECONDES;
	private TYPE type = TYPE.AUTO_ANDROID;

	public TYPE getType() {
		return type;
	}

	public void setType(TYPE type) {
		this.type = type;
	}

	private MediaPlayerHandler() {

	}

	public static MediaPlayerHandler getInstance() {
		synchronized (MediaPlayerHandler.class) {
			if (null == instance) {
				instance = new MediaPlayerHandler();
			}
			return instance;
		}
	}

	public enum TYPE {
		ANDROID, MIPT, AUTO_ANDROID, AUTO_MIPT
	}

	@Override
	public void setDisplay(SurfaceView view) {
		this.view = view;
		viewHolder = view.getHolder();
		viewHolder.addCallback(surfaceCallback);
	}

	@Override
	public void start(final String... params) {
		Log.d(TAG, "start");
		if (null == params || TextUtils.isEmpty(params[0])) {
			throw new NullPointerException("start url can't be null!");
		}
		if (null != callback) {
			callback.onLoading();
		}
		this.params = params;
		if (!isSurfaceInit) {
			view.setVisibility(View.INVISIBLE);
			dochangePlay(params);
			noteStartTimeOut(HW_TIME_OUT);
		}
	}

	private void noteStartTimeOut(int message) {
		handler.removeMessages(message);
		handler.sendEmptyMessageDelayed(message, DEFAUTL_TIME_OUT_MILLISECONDES);
	}

	private void dochangePlay(String... params) {
		// String url = params[0];
		if (null == params || TextUtils.isEmpty(params[0])) {
			throw new NullPointerException("dochangePlay url can't be null!");
		}
		Log.d(TAG, "dochangePlay url:" + params[0]);
		clearPlayer();
		createPlayer();
		try {
			player.setDataSource(params);// TODO
			player.prepareAsync();
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "dochangePlay error!", e);
		} catch (SecurityException e) {
			Log.e(TAG, "dochangePlay error!", e);
		} catch (IllegalStateException e) {
			Log.e(TAG, "dochangePlay error!", e);
		} catch (IOException e) {
			Log.e(TAG, "dochangePlay error!", e);
		}
	}

	private void createPlayer() {
		view.setVisibility(View.VISIBLE);
		if (type == TYPE.ANDROID || type == TYPE.AUTO_ANDROID) {
			player = new AndroidMediaPlayer();
			Log.d(TAG, "createPlayer android");
		} else if (type == TYPE.MIPT || type == TYPE.AUTO_MIPT) {
			player = new mipt.media.net.MediaPlayer();
			Log.d(TAG, "createPlayer mipt");
		}
		try {
			player.setDisplay(viewHolder);
		} catch (Exception e) {
			testError();
			Log.e(TAG, "createPlayer setDisplay error!", e);
		}
		setListeners();
	}

	private void setListeners() {
		if (null != player) {
			player.setOnPreparedListener(preparedListener);
			player.setOnErrorListener(errorListener);
			player.setOnCompletionListener(completionListener);
			player.setOnInfoListener(infoListener);
			player.setOnBufferingUpdateListener(bufferingUpdateListener);
			player.setOnSeekCompleteListener(onSeekCompleteListener);
		}
	}

	private void clearPlayer() {
		if (null != player) {
			player.stop();
			player.release();
			player = null;
		}

		if (callback != null) {
			callback.resetStopStatus();
		}

		removeMessages();
		isPause = false;
		hasPlayed = false;
	}

	@Override
	public void release() {
		resetData();
		clearPlayer();
	}

	private void resetData() {
		hasPlayed = false;
		isSurfaceInit = true;
		lastPlayPosition = -1;
		params = null;
	}

	@Override
	public void stop() {
		Log.d(TAG, "stop");
		if (null != player) {
			try {
				player.stop();
			} catch (Exception e) {
				Log.e(TAG, "stop:", e);
			}
		}
		removeMessages();
		isPause = false;
		hasPlayed = false;
		lastPlayPosition = -1;
	}

	@Override
	public void pause() {
		Log.d(TAG, "pause");
		if (null != player) {
			isPause = true;
			if (player.isPlaying()) {
				player.pause();
			}
		}
	}

	@Override
	public boolean changeType(TYPE type) {
		if (this.type == type) {
			return false;
		}
		this.type = type;
		if (!isSurfaceInit && null != params && !TextUtils.isEmpty(params[0])) {
			if (null != callback) {
				callback.onLoading();
			}
			lastPlayPosition = player == null ? 0 : player.getCurrentPosition();

			handler.removeMessages(HW_CHANGE_TYPE);
			handler.sendEmptyMessage(HW_CHANGE_TYPE);
		}
		return true;
	}

	private OnPreparedListener preparedListener = new OnPreparedListener() {
		@Override
		public void onPrepared(MediaPlayer mp) {
			if (player != null) {
				/*
				 * if player can't getduration change play type if current type
				 * is mipt play show error tip to client
				 */
				Log.i(TAG, "duration:" + player.getDuration());
				if (callback != null) {
					callback.onPreparedStart();
				}
				player.start();
				hasPlayed = true;
				if (isPause) {
					player.pause();
				}

				if (null != callback) {
					callback.onPrepared();
				}

				notePrepareTimeOut();
			}
		}
	};

	private void notePrepareTimeOut() {
		handler.removeMessages(HW_LOADING_TIME_OUT);
		handler.sendEmptyMessageDelayed(HW_LOADING_TIME_OUT, prepareTimeOutMills);
	}

	private OnInfoListener infoListener = new OnInfoListener() {
		@Override
		public boolean onInfo(MediaPlayer mp, int what, int extra) {
			if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
				if (null != callback) {
					callback.onStartPlay();
				}
			}
			if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
				if (null != callback) {
					callback.onLoading();
				}
			} else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
				if (null != callback) {
					callback.onContinuePlay();
				}
			}
			return false;
		}
	};

	private OnSeekCompleteListener onSeekCompleteListener = new OnSeekCompleteListener() {

		@Override
		public void onSeekComplete(MediaPlayer mp) {
			if (null != callback) {
				callback.onSeekCompelete();
			}

		}
	};
	private OnBufferingUpdateListener bufferingUpdateListener = new OnBufferingUpdateListener() {
		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			if (null != callback) {
				callback.onBufferingUpdate(percent);
			}
		}
	};

	private OnErrorListener errorListener = new OnErrorListener() {
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			Log.w(TAG, "onError what:" + what + " extra:" + extra);
			if (autoErrorChangeType) {
				if (type == TYPE.AUTO_ANDROID) {
					type = TYPE.MIPT;
					dochangePlay(params);
					if (callback != null) {
						callback.callbackChangeType();
					}
				} else if (type == TYPE.AUTO_MIPT) {
					type = TYPE.ANDROID;
					dochangePlay(params);
				} else if (type == TYPE.ANDROID || type == TYPE.MIPT) {
					clearPlayer();
					if (null != callback) {
						callback.onError(what, extra);
					}
				}
			} else {
				if (null != callback) {
					callback.onError(what, extra);
				}
			}
			return false;
		}
	};

	private SurfaceHolder.Callback surfaceCallback = new Callback() {
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			clearPlayer();
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Log.d(TAG, "surfaceCreated isInit:" + isSurfaceInit);
			if (isSurfaceInit) {
				handler.sendEmptyMessage(HW_START_PLAY);
			}
			isSurfaceInit = false;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

		}
	};

	private OnCompletionListener completionListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			if (null != callback) {
				callback.onCompletion();
			}
		}
	};

	private void removeMessages() {
		handler.removeMessages(HW_SEEK_TO);
		handler.removeMessages(HW_START_PLAY);
		handler.removeMessages(HW_TIME_OUT);
		handler.removeMessages(HW_LOADING_TIME_OUT);
	}

	private final static int HW_START_PLAY = 0;
	private final static int HW_SEEK_TO = 2;
	private final static int HW_TIME_OUT = 3;

	private static final int HW_LOADING_TIME_OUT = 4;
	private static final int HW_CHANGE_TYPE = 5;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == HW_START_PLAY) {
				if (null != params && !TextUtils.isEmpty(params[0])) {
					dochangePlay(params);
				}
			} else if (msg.what == HW_SEEK_TO) {
				doSeekTo(msg.arg1);
			} else if (msg.what == HW_TIME_OUT) {
				if (null != player && !hasPlayed) {
					doTimeOut();
				}
			} else if (msg.what == HW_LOADING_TIME_OUT) {
				if (null != player && player.isPlaying()) {
					doStartPlay();
				}
			} else if (msg.what == HW_CHANGE_TYPE) {
				view.setVisibility(View.INVISIBLE);
				dochangePlay(params);
			}
		}

		private void doStartPlay() {
			if (null != callback) {
				callback.onStartPlay();
			}
			if (-1 != lastPlayPosition) {
				seekTo(lastPlayPosition, 0);
				lastPlayPosition = -1;
			}
		}

		private void doTimeOut() {
			if (null != callback) {
				callback.onTimeOut();
			}
			// clearPlayer();
		}

		private void doSeekTo(int msec) {
			Log.d(TAG, "doSeekTo :" + msec);
			if (null != player) {
				player.seekTo(msec);
			}
		}
	};

	@Override
	public void testError() {
		errorListener.onError(null, 0, -1000);
	}

	@Override
	public void seekTo(int msec, int delay) {
		handler.removeMessages(HW_SEEK_TO);
		Message msg = handler.obtainMessage(HW_SEEK_TO);
		msg.arg1 = msec;
		handler.sendMessageDelayed(msg, delay);
	}

	@Override
	public void setMediaStatusCallback(MediaStatusCallback callback) {
		this.callback = callback;
	}

	@Override
	public void continuePlay() {
		if (isPause) {
			player.start();
		} else {// 重新启动
			if (null != params && !TextUtils.isEmpty(params[0])) {
				start(params);
			}
		}
		isPause = false;
	}

	public void setPrepareTimeOutMills(long prepareTimeOutMills) {
		this.prepareTimeOutMills = prepareTimeOutMills;
	}

	public String getUrl() {
		return params[0];
	}

	public void setUrl(String url) {
		this.params[0] = url;
	}
}

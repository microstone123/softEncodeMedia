package mipt.media.net;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;

import mipt.media.player.annotations.AccessedByNative;
import mipt.media.player.option.AvFormatOption;
import mipt.media.player.pragma.DebugLog;
import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnTimedTextListener;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

/*
 * modify by duyhouhua *
 */
public final class MediaPlayer implements MiptMediaPlayerIfc {
	private final static String TAG = MediaPlayer.class.getName();

	private static final int MEDIA_NOP = 0; // interface test message
	private static final int MEDIA_PREPARED = 1;
	private static final int MEDIA_PLAYBACK_COMPLETE = 2;
	private static final int MEDIA_BUFFERING_UPDATE = 3;
	private static final int MEDIA_SEEK_COMPLETE = 4;
	private static final int MEDIA_SET_VIDEO_SIZE = 5;
	private static final int MEDIA_TIMED_TEXT = 99;
	private static final int MEDIA_ERROR = 100;
	private static final int MEDIA_INFO = 200;
	private static final int MEDIA_PLAYBACK_STATE_CHANGED = 700;
	protected static final int MEDIA_SET_VIDEO_SAR = 10001;

	/**
	 * The player was started because it was used as the next player for another
	 * player, which just completed playback.
	 * 
	 * @see android.media.MediaPlayer.OnInfoListener
	 * @hide
	 */
	public static final int MEDIA_INFO_STARTED_AS_NEXT = 2;

	public int mCurrentState = STATE_IDLE;
	public static final int STATE_IDLE = 0;
	public static final int STATE_INITIALIZED = 1;
	public static final int STATE_ASYNC_PREPARING = 2;
	public static final int STATE_PREPARED = 3;
	public static final int STATE_STARTED = 4;
	public static final int STATE_PAUSED = 5;
	public static final int STATE_COMPLETED = 6;
	public static final int STATE_STOPPED = 7;
	public static final int STATE_ERROR = 8;
	public static final int STATE_END = 9;

	private OnPreparedListener mOnPreparedListener;
	private OnCompletionListener mOnCompletionListener;
	private OnBufferingUpdateListener mOnBufferingUpdateListener;
	private OnSeekCompleteListener mOnSeekCompleteListener;
	private android.media.MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener;
	private OnErrorListener mOnErrorListener;
	private OnInfoListener mOnInfoListener;

	private OnVideoSizeChangedListener miptVideoSizeChangedListener;

	private SurfaceHolder mSurfaceHolder;
	private EventHandler mEventHandler;
	private PowerManager.WakeLock mWakeLock = null;
	private boolean mScreenOnWhilePlaying;
	private boolean mStayAwake;

	private int mVideoWidth;
	private int mVideoHeight;
	private int mVideoSarNum;
	private int mVideoSarDen;

	private String mDataSource;
	private long pauseTime;

	@AccessedByNative
	private long mNativeMediaPlayer;

	@AccessedByNative
	private int mNativeSurfaceTexture;

	@AccessedByNative
	private int mListenerContext;

	// data
	private boolean isNewInstance;

	static {
		System.loadLibrary("stlport_shared");
		System.loadLibrary("miptffmpeg");
		System.loadLibrary("miptutil");
		System.loadLibrary("miptsdl");
		System.loadLibrary("miptadk");
		System.loadLibrary("miptplayer");
		native_init();
	}

	/**
	 * Default constructor. Consider using one of the create() methods for
	 * synchronously instantiating a MiptMediaPlayer from a Uri or resource.
	 * <p>
	 * When done with the MiptMediaPlayer, you should call {@link #release()},
	 * to free the resources. If not released, too many MiptMediaPlayer
	 * instances may result in an exception.
	 * </p>
	 */
	public MediaPlayer() {
		isNewInstance = true;
		Looper looper;
		if ((looper = Looper.myLooper()) != null) {
			mEventHandler = new EventHandler(this, looper);
		} else if ((looper = Looper.getMainLooper()) != null) {
			mEventHandler = new EventHandler(this, looper);
		} else {
			mEventHandler = null;
		}

		/*
		 * Native setup requires a weak reference to our object. It's easier to
		 * create it here than in C++.
		 */
		native_setup(new WeakReference<MediaPlayer>(this));
	}

	/**
	 * Sets the {@link SurfaceHolder} to use for displaying the video portion of
	 * the media.
	 * 
	 * Either a surface holder or surface must be set if a display or video sink
	 * is needed. Not calling this method or {@link #setSurface(Surface)} when
	 * playing back a video will result in only the audio track being played. A
	 * null surface holder or surface will result in only the audio track being
	 * played.
	 * 
	 * @param sh
	 *            the SurfaceHolder to use for video display
	 */

	@Override
	public void setDisplay(SurfaceHolder sh) throws IllegalArgumentException {
		mSurfaceHolder = sh;
		Surface surface;
		if (sh != null) {
			surface = sh.getSurface();

			if (surface != null && !surface.isValid()) {

			}
		} else {
			surface = null;
		}

		_setVideoSurface(surface);
		updateSurfaceScreenOn();
	}

	/**
	 * Sets the data source (file-path or http/rtsp URL) to use.
	 * 
	 * @param path
	 *            the path of the file, or the http/rtsp URL of the stream you
	 *            want to play
	 * @throws IllegalStateException
	 *             if it is called in an invalid state
	 * 
	 *             <p>
	 *             When <code>path</code> refers to a local file, the file may
	 *             actually be opened by a process other than the calling
	 *             application. This implies that the pathname should be an
	 *             absolute path (as any other process runs with unspecified
	 *             current working directory), and that the pathname should
	 *             reference a world-readable file. As an alternative, the
	 *             application could first open the file for reading, and then
	 *             use the file descriptor form
	 *             {@link #setDataSource(FileDescriptor)}.
	 */
	@Override
	public void setDataSource(String... params) throws IOException, IllegalArgumentException, SecurityException,
			IllegalStateException {
		if (null == params || TextUtils.isEmpty(params[0])) {
			return;
		}
		mDataSource = params[0];
		String ua = "";
		if (params.length >= 2) {
			if (!TextUtils.isEmpty(params[1])) {
				ua = params[1].trim();
			}
		}
		_setDataSource(params[0], ua, null, null);
	}

	@Override
	public String getDataSource() {
		return mDataSource;
	}

	@Override
	public void prepareAsync() throws IllegalStateException {
		_prepareAsync();
	};

	@Override
	public void start() throws IllegalStateException {
		if (isNewInstance) {
			stayAwake(true);
			_start();
			isNewInstance = false;
		} else {
			resume();
		}
	}

	@Override
	public void stop() throws IllegalStateException {
		stayAwake(false);
		_stop();
	}

	@Override
	public void pause() throws IllegalStateException {
		pauseTime = getCurrentPosition();
		Log.i(TAG, "pausetime at " + pauseTime);
		stayAwake(false);
		_pause();
	}

	@Override
	public void resume() throws IllegalStateException {
		stayAwake(true);
		_resume();
	}

	@Override
	public void seekTo(int msec) throws IllegalStateException {
		if (!isPlaying()) {
			resume();
		}

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (msec >= getDuration()) {
			notifyOnCompletion();
			return;
		}

		if (isPlaying()) {
			_seekTo(msec);
		}

	};

	/**
	 * @param chromaFourCC
	 *            AvFourCC.SDL_FCC_RV16 AvFourCC.SDL_FCC_RV32
	 *            AvFourCC.SDL_FCC_YV12
	 */
	public void setOverlayFormat(int chromaFourCC) {
		_setOverlayFormat(chromaFourCC);
	}

	@SuppressLint("Wakelock")
	@Override
	public void setWakeMode(Context context, int mode) {
		boolean washeld = false;
		if (mWakeLock != null) {
			if (mWakeLock.isHeld()) {
				washeld = true;
				mWakeLock.release();
			}
			mWakeLock = null;
		}

		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(mode | PowerManager.ON_AFTER_RELEASE, MediaPlayer.class.getName());
		mWakeLock.setReferenceCounted(false);
		if (washeld) {
			mWakeLock.acquire();
		}
	}

	@Override
	public void setScreenOnWhilePlaying(boolean screenOn) {
		if (mScreenOnWhilePlaying != screenOn) {
			if (screenOn && mSurfaceHolder == null) {
				DebugLog.w(TAG, "setScreenOnWhilePlaying(true) is ineffective without a SurfaceHolder");
			}
			mScreenOnWhilePlaying = screenOn;
			updateSurfaceScreenOn();
		}
	}

	@Override
	public int getVideoWidth() {
		return mVideoWidth;
	}

	@Override
	public int getVideoHeight() {
		return mVideoHeight;
	}

	@Override
	public int getCurrentPosition() {
		if (isPlaying())
			return (int) _getCurrentPosition();
		else {
			return (int) pauseTime;
		}
	}

	@Override
	public int getDuration() {
		if (isPlaying())
			return (int) _getDuration();
		return -1;
	};

	/**
	 * Releases resources associated with this MiptMediaPlayer object. It is
	 * considered good practice to call this method when you're done using the
	 * MiptMediaPlayer. In particular, whenever an Activity of an application is
	 * paused (its onPause() method is called), or stopped (its onStop() method
	 * is called), this method should be invoked to release the MiptMediaPlayer
	 * object, unless the application has a special need to keep the object
	 * around. In addition to unnecessary resources (such as memory and
	 * instances of codecs) being held, failure to call this method immediately
	 * if a MiptMediaPlayer object is no longer needed may also lead to
	 * continuous battery consumption for mobile devices, and playback failure
	 * for other applications if no multiple instances of the same codec are
	 * supported on a device. Even if multiple instances of the same codec are
	 * supported, some performance degradation may be expected when unnecessary
	 * multiple instances are used at the same time.
	 */
	@Override
	public void release() {
		stayAwake(false);
		updateSurfaceScreenOn();
		resetListeners();
		_release();
	}

	@Override
	public void reset() {
		stayAwake(false);
		_reset();
		// make sure none of the listeners get called anymore
		mEventHandler.removeCallbacksAndMessages(null);

		mVideoWidth = 0;
		mVideoHeight = 0;
	}

	@Override
	public boolean isPlaying() {
		return _isPlaying();
	}

	public void setAvOption(AvFormatOption option) {
		setAvFormatOption(option.getName(), option.getValue());
	}

	public void setAvFormatOption(String name, String value) {
		_setAvFormatOption(name, value);
	}

	public void setAvCodecOption(String name, String value) {
		_setAvCodecOption(name, value);
	}

	protected void finalize() {
		native_finalize();
	}

	@SuppressLint("Wakelock")
	private void stayAwake(boolean awake) {
		if (mWakeLock != null) {
			if (awake && !mWakeLock.isHeld()) {
				mWakeLock.acquire();
			} else if (!awake && mWakeLock.isHeld()) {
				mWakeLock.release();
			}
		}
		mStayAwake = awake;
		updateSurfaceScreenOn();
	}

	private void updateSurfaceScreenOn() {
		if (mSurfaceHolder != null) {
			mSurfaceHolder.setKeepScreenOn(mScreenOnWhilePlaying && mStayAwake);
		}
	}

	private static class EventHandler extends Handler {
		private WeakReference<MediaPlayer> mWeakPlayer;

		public EventHandler(MediaPlayer mp, Looper looper) {
			super(looper);
			mWeakPlayer = new WeakReference<MediaPlayer>(mp);
		}

		@Override
		public void handleMessage(Message msg) {
			MediaPlayer player = mWeakPlayer.get();
			if (player == null || player.mNativeMediaPlayer == 0) {
				DebugLog.w(TAG, "MiptMediaPlayer went away with unhandled events");
				return;
			}

			switch (msg.what) {
			case MEDIA_PREPARED:
				player.notifyOnPrepared();
				return;

			case MEDIA_PLAYBACK_COMPLETE:
				player.notifyOnCompletion();
				player.stayAwake(false);
				return;

			case MEDIA_BUFFERING_UPDATE:
				player.notifyOnBufferingUpdate(msg.arg2);
				return;

			case MEDIA_SEEK_COMPLETE:
				player.notifyOnSeekComplete();
				return;

			case MEDIA_SET_VIDEO_SIZE:
				player.mVideoWidth = msg.arg1;
				player.mVideoHeight = msg.arg2;
				player.notifyOnVideoSizeChanged(msg.arg1, msg.arg2, player.mVideoSarNum, player.mVideoSarDen);
				return;

			case MEDIA_ERROR:
				DebugLog.e(TAG, "Error (" + msg.arg1 + "," + msg.arg2 + ")");
				if (!player.notifyOnError(msg.arg1, msg.arg2)) {
					player.notifyOnCompletion();
				}
				player.stayAwake(false);
				return;

			case MEDIA_INFO:
				if (msg.arg1 != android.media.MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING) {
					DebugLog.i(TAG, "Info (" + msg.arg1 + "," + msg.arg2 + ")");
				}
				player.notifyOnInfo(msg.arg1, msg.arg2);
				// No real default action so far.
				return;
			case MEDIA_TIMED_TEXT:
				// do nothing
				break;

			case MEDIA_NOP: // interface test message - ignore
				break;

			case MEDIA_SET_VIDEO_SAR:
				player.mVideoSarNum = msg.arg1;
				player.mVideoSarDen = msg.arg2;
				player.notifyOnVideoSizeChanged(msg.arg1, msg.arg2, player.mVideoSarNum, player.mVideoSarDen);
				break;

			case MEDIA_PLAYBACK_STATE_CHANGED:
				player.notifyOnPlayBackStateChanged(msg.arg1);
				break;

			default:
				DebugLog.e(TAG, "Unknown message type " + msg.what);
				return;
			}
		}
	}

	/*
	 * Called from native code when an interesting event happens. This method
	 * just uses the EventHandler system to post the event back to the main app
	 * thread. We use a weak reference to the original MiptMediaPlayer object so
	 * that the native code is safe from the object disappearing from underneath
	 * it. (This is the cookie passed to native_setup().)
	 */
	private static void postEventFromNative(Object weakThiz, int what, int arg1, int arg2, Object obj) {
		if (weakThiz == null)
			return;

		@SuppressWarnings("rawtypes")
		MediaPlayer mp = (MediaPlayer) ((WeakReference) weakThiz).get();
		if (mp == null) {
			return;
		}

		if (what == MEDIA_INFO && arg1 == MEDIA_INFO_STARTED_AS_NEXT) {
			// this acquires the wakelock if needed, and sets the client side
			// state
			mp.start();
		}
		if (mp.mEventHandler != null) {
			Message m = mp.mEventHandler.obtainMessage(what, arg1, arg2, obj);
			mp.mEventHandler.sendMessage(m);
		}
	}

	private void resetListeners() {
		mOnPreparedListener = null;
		mOnBufferingUpdateListener = null;
		mOnCompletionListener = null;
		mOnSeekCompleteListener = null;
		mOnVideoSizeChangedListener = null;
		mOnErrorListener = null;
		mOnInfoListener = null;
	}

	public final void setOnPreparedListener(OnPreparedListener listener) {
		mOnPreparedListener = listener;
	}

	public final void setOnCompletionListener(OnCompletionListener listener) {
		mOnCompletionListener = listener;
	}

	public final void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		mOnBufferingUpdateListener = listener;
	}

	public final void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		mOnSeekCompleteListener = listener;
	}

	public final void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
		miptVideoSizeChangedListener = listener;
	}

	public final void setOnErrorListener(OnErrorListener listener) {
		mOnErrorListener = listener;
	}

	public final void setOnInfoListener(OnInfoListener listener) {
		mOnInfoListener = listener;
	}

	protected final void notifyOnPrepared() {
		if (mOnPreparedListener != null) {
			mOnPreparedListener.onPrepared(null);
		}
	}

	protected final void notifyOnPlayBackStateChanged(int state) {
		mCurrentState = state;
		/*
		 * switch (mCurrentState) { case STATE_IDLE: DebugLog.w(TAG,
		 * "--------------------mCurrentState:STATE_IDLE"); break; case
		 * STATE_INITIALIZED: DebugLog.w(TAG,
		 * "--------------------mCurrentState:STATE_INITIALIZED"); break; case
		 * STATE_ASYNC_PREPARING: DebugLog.w(TAG,
		 * "--------------------mCurrentState:STATE_ASYNC_PREPARING"); break;
		 * case STATE_PREPARED: DebugLog.w(TAG,
		 * "--------------------mCurrentState:STATE_PREPARED"); break; case
		 * STATE_STARTED: DebugLog.w(TAG,
		 * "--------------------mCurrentState:STATE_STARTED"); break; case
		 * STATE_PAUSED: DebugLog.w(TAG,
		 * "--------------------mCurrentState:STATE_PAUSED"); break; case
		 * STATE_COMPLETED: DebugLog.w(TAG,
		 * "--------------------mCurrentState:STATE_COMPLETED"); break; case
		 * STATE_STOPPED: DebugLog.w(TAG,
		 * "--------------------mCurrentState:STATE_STOPPED"); break; case
		 * STATE_ERROR: DebugLog.w(TAG,
		 * "--------------------vmCurrentState:STATE_ERROR"); break; case
		 * STATE_END: DebugLog.w(TAG,
		 * "--------------------mCurrentState:STATE_END"); break; default:
		 * DebugLog.w(TAG, "--------------------unkonw play back state.");
		 * break; }
		 */
	}

	protected final void notifyOnCompletion() {
		if (mOnCompletionListener != null) {
			mOnCompletionListener.onCompletion(null);
		}
	}

	protected final void notifyOnBufferingUpdate(int percent) {
		if (mOnBufferingUpdateListener != null)
			mOnBufferingUpdateListener.onBufferingUpdate(null, percent);
	}

	protected final void notifyOnSeekComplete() {
		if (mOnSeekCompleteListener != null)
			mOnSeekCompleteListener.onSeekComplete(null);
	}

	protected final void notifyOnVideoSizeChanged(int width, int height, int sarNum, int sarDen) {
		if (mOnVideoSizeChangedListener != null) {
			mOnVideoSizeChangedListener.onVideoSizeChanged(null, width, height);
		}
		if (miptVideoSizeChangedListener != null) {
			miptVideoSizeChangedListener.onVideoSizeChanged(null, width, height, sarNum, sarDen);
		}
	}

	protected final boolean notifyOnError(int what, int extra) {
		if (mOnErrorListener != null) {
			return mOnErrorListener.onError(null, what, extra);
		}
		return false;
	}

	protected final boolean notifyOnInfo(int what, int extra) {
		if (mOnInfoListener != null)
			return mOnInfoListener.onInfo(null, what, extra);
		return false;
	}

	/*---------------------native--------------------------------------*/

	private static native final void native_init();

	private native final void native_setup(Object MiptMediaPlayer_this);

	private native final void native_finalize();

	private native final void native_message_loop(Object MiptMediaPlayer_this);

	/*
	 * Update the MiptMediaPlayer SurfaceTexture. Call after setting a new
	 * display surface.
	 */
	private native void _setVideoSurface(Surface surface);

	private native void _prepareAsync() throws IllegalStateException;

	private native void _start() throws IllegalStateException;

	private native void _stop() throws IllegalStateException;

	private native void _pause() throws IllegalStateException;

	private native void _resume() throws IllegalStateException;

	private native void _seekTo(long msec) throws IllegalStateException;

	private native long _getCurrentPosition();

	private native long _getDuration();

	private native void _release();

	private native void _reset();

	private native boolean _isPlaying();

	private native void _setDataSource(String path, String ua, String[] keys, String[] values) throws IOException,
			IllegalArgumentException, SecurityException, IllegalStateException;

	public native void _setVolume(float leftVolume, float rightVolume);

	private native void _setAvCodecOption(String name, String value);

	private native void _setOverlayFormat(int chromaFourCC);

	private native void _setAvFormatOption(String name, String value);

	@Override
	public void prepare() throws IllegalStateException, IOException {
		prepareAsync();

	}

	@Override
	public void setOnTimedTextListener(OnTimedTextListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOnVideoSizeChangedListener(android.media.MediaPlayer.OnVideoSizeChangedListener listener) {
		// TODO Auto-generated method stub

	}

}

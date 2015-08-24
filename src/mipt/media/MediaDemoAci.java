package mipt.media;

import mipt.media.MediaPlayerHandler.TYPE;
import mipt.media.net.MediaPlayerIfc;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MediaDemoAci extends Activity {
	protected final static String TAG = MediaDemoAci.class.getName();

	// view
	SurfaceView tvSurfaceView;
	MediaPlayer mediaPlayer;

	private ProgressBar loadPb;
	private SeekBar timeSb;
	private TextView postionTv;
	private TextView durationTv;

	private Toast toast;
	// date
	private int nowPlayingChannelPosition = 0;
	private static String[] sources = {
			"http://pl.youku.com/playlist/m3u8?ev=1&token=4095&oip=3070135132&ep=cCaVGkuIV8kC5irZiT8bZH2zInMLXJZ0rGaF%2FrYHSsV%2BNaHQnT7Ywg%3D%3D&ctype=12&ts=1410251724&type=hd2&sid=2410251723961123aaf44&vid=XNjI0OTUzNjMy&keyframe=1",
			"http://myott.sinaapp.com/TV_Proxy/jz.php?VideoId=cctv3",
			"http://myott.sinaapp.com/TV_Proxy/jz.php?VideoId=cctv4",
			"http://myott.sinaapp.com/TV_Proxy/jz.php?VideoId=cctv5",
			"http://myott.sinaapp.com/TV_Proxy/jz.php?VideoId=cctv6",
			"http://metan.video.qiyi.com/20140717/5c/82/044bb53edf0b8ba9f1930f6c8f2cad7d.m3u8",
			"http://stream.hoolo.tv/hztv5/sd/live.m3u8",
			"http://livemedia.yzntv.com/channels/tvie/42/flv:400k/live?1376439291957",
			"http://zb.v.qq.com:1863/?progid=1975434150&ostype=ios" };

	private boolean isAndroid = true;
	private boolean isPause = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(mipt.media.R.layout.layout_media_demo);

		initData();
		initView();
	}

	private void initData() {
		nowPlayingChannelPosition = 0;
	}

	private void initView() {
		tvSurfaceView = (SurfaceView) findViewById(R.id.sv_media_live);
		loadPb = (ProgressBar) findViewById(R.id.pb_load);
		durationTv = (TextView) findViewById(R.id.tv_duration);
		postionTv = (TextView) findViewById(R.id.tv_postion);
		timeSb = (SeekBar) findViewById(R.id.sb_time);
		timeSb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Log.d(TAG, "onStopTrackingTouch");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				Log.d(TAG, "onStartTrackingTouch");
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				Log.d(TAG, "onProgressChanged");
				doSeekTo(progress);
			}

		});

		MediaPlayerHandler.getInstance().setDisplay(tvSurfaceView);
		MediaPlayerHandler.getInstance().setType(TYPE.MIPT);
		// MediaPlayerHandler.getInstance().setPrepareTimeOutMills(200);
		MediaPlayerHandler
				.getInstance()
				.start("http://pl.youku.com/playlist/m3u8?keyframe=1&ep=eiaWHU%2BPUcwG4CPYjT8bYCvhdnBdXJZ1gkTM%2FJgDR8RQNerQmz7Wwg%3D%3D&vid=XODkyMzQwODM2&type=flv&token=2075&oip=1790517073&ev=1&sid=84276272650751277327b&ts=1427627265&ctype=12");
		MediaPlayerHandler.getInstance().setMediaStatusCallback(new MediaStatusCallback.SimpleMediaStatusCallback() {
			@Override
			public void onError(int what, int extra) {
				showMsg("报错啦！what:" + what + " extra:" + extra);
			}

			@Override
			public void onLoading() {
				loadPb.setVisibility(View.VISIBLE);
			}

			@Override
			public void onStartPlay() {
				doStartPlay();
			}

			@Override
			public void onContinuePlay() {
				doStartPlay();
			}

			@Override
			public void onTimeOut() {
				showMsg("超时啦！");
			}
		});

	}

	@Override
	protected void onPause() {
		MediaPlayerHandler.getInstance().release();
		super.onPause();
	}

	private void doStartPlay() {
		loadPb.setVisibility(View.INVISIBLE);
		int duation = MediaPlayerHandler.getInstance().getPlayer().getDuration();
		String duartionStr = Utils.formatDuration(getApplicationContext(), duation);

		durationTv.setText(duartionStr);
		refreshShowPosition();
	}

	private void refreshShowPosition() {
		handler.removeMessages(HW_POSITION_SHOW);
		Message msg = handler.obtainMessage(HW_POSITION_SHOW);
		handler.sendMessageDelayed(msg, 1000);
		int position = MediaPlayerHandler.getInstance().getPlayer() == null ? 0 : MediaPlayerHandler.getInstance()
				.getPlayer().getCurrentPosition();
		String positionStr = Utils.formatDuration(getApplicationContext(), position);
		postionTv.setText(positionStr);
	}

	private void refreshSeekBarChangeTime() {
		int position = MediaPlayerHandler.getInstance().getPlayer().getDuration() / 100 * timeSb.getProgress();
		String positionStr = Utils.formatDuration(getApplicationContext(), position);
		postionTv.setText(positionStr);
	}

	private void doSeekTo(int progress) {
		hideDelay(timeSb, 5000);
		int duration = MediaPlayerHandler.getInstance().getPlayer().getDuration();
		int current = duration * progress / 100;
		Log.d(TAG, "doSeekTo duration:" + duration + " progress:" + progress);
		MediaPlayerHandler.getInstance().seekTo(current, 1000);

		refreshSeekBarChangeTime();
	}

	/**
	 * control
	 */
	private long keyLastTime;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		int repeat = event.getRepeatCount();
		Log.d(TAG,
				"keyCode:" + keyCode + " " + event.getAction() + " repeat:" + repeat + " action:" + event.getAction());
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			nowPlayingChannelPosition = (++nowPlayingChannelPosition) > sources.length - 1 ? 0
					: nowPlayingChannelPosition;
			dochangeSource(nowPlayingChannelPosition);
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			nowPlayingChannelPosition = (--nowPlayingChannelPosition) < 0 ? sources.length - 1
					: nowPlayingChannelPosition;
			dochangeSource(nowPlayingChannelPosition);
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			// if (System.currentTimeMillis() - keyLastTime >= 1000) {
			// } else {
			isAndroid = !isAndroid;
			dochangeEncode(isAndroid);
			// }
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
			// if (System.currentTimeMillis() - keyLastTime >= 1000) {
			// showTimeSb();
			// } else {
			isAndroid = !isAndroid;
			dochangeEncode(isAndroid);
			// }
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
			doChangePause();
			break;
		case KeyEvent.KEYCODE_MENU:
			doErrorTest();
			break;
		}
		keyLastTime = System.currentTimeMillis();
		return super.onKeyDown(keyCode, event);
	}

	private void showTimeSb() {
		timeSb.setVisibility(View.VISIBLE);
		timeSb.requestFocus();
		showTime(MediaPlayerHandler.getInstance().getPlayer());
		hideDelay(timeSb, 5000);
	}

	private void hideDelay(View view, long delay) {
		handler.removeMessages(HW_HIDE_DELAY);
		Message msg = handler.obtainMessage(HW_HIDE_DELAY);
		msg.obj = view;
		handler.sendMessageDelayed(msg, delay);
	}

	private final static int HW_HIDE_DELAY = 0;
	private final static int HW_POSITION_SHOW = 1;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == HW_HIDE_DELAY) {
				((View) msg.obj).setVisibility(View.GONE);
				((View) msg.obj).clearFocus();
			} else if (msg.what == HW_POSITION_SHOW) {
				refreshShowPosition();
			}
		};
	};

	private void doChangePause() {
		if (isPause) {
			MediaPlayerHandler.getInstance().continuePlay();
		} else {
			MediaPlayerHandler.getInstance().pause();
		}
		isPause = !isPause;
		showMsg(isPause ? "暂停！" : "开始！");
	}

	private void doErrorTest() {
		showMsg("抛出错误！");
		MediaPlayerHandler.getInstance().testError();
	}

	private void dochangeSource(int nowPlayingChannelPosition) {
		showMsg("切换源：" + nowPlayingChannelPosition);
		String url = sources[nowPlayingChannelPosition];
		MediaPlayerHandler.getInstance().start(url);
	}

	/**
	 * control
	 */

	private void dochangeEncode(boolean isAndroid) {
		showMsg("切换解码方式：" + (isAndroid ? "硬解" : "软解"));
		MediaPlayerHandler.getInstance().changeType(isAndroid ? TYPE.ANDROID : TYPE.MIPT);
	}

	private void showMsg(String msg) {
		toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
		toast.show();
	}

	private void showTime(MediaPlayerIfc mp) {
		try {
			int progress = mp.getCurrentPosition() * 100 / mp.getDuration();
			timeSb.setProgress(progress);
		} catch (Exception e) {
			Log.e(TAG, "showTime error:it is live!");
			timeSb.setVisibility(View.GONE);
		}
	}

}
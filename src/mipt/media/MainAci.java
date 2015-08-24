package mipt.media;

import mipt.media.MediaPlayerHandler.TYPE;
import mipt.media.SpeedThread.RefreshSpeedListener;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainAci extends Activity implements RefreshSpeedListener {

	private String url = "http://g3.letv.cn/vod/v2/MTE2LzQvNTYvbGV0di11dHMvMTQvdmVyXzAwXzE4LTMxNzQ5NDEyLWF2Yy04NzgxMDEtYWFjLTY0MDAxLTczOTY2MzktODc5NzY1NTQ2LTQ4ZGU1ZGI3ODdhYzNlMzVkMzliZDkxNWQwMWU3NGEzLTE0MTY4Nzg5Mzk0NzQubXA0?b=951&mmsid=20420173&tm=1417770425&key=b026ee13079f045bf5fd62730f54256d&platid=6&splatid=602&playid=0&tss=tvts&vtype=22&cvid=89974894146&termid=3&pay=1&ostype=android&hwtype=C1S&m3v=0&format=0";
	private SurfaceView tvSurfaceView;
	private SpeedThread speedThread;
	private TextView postionTv;
	private TextView durationTv, pb_load1;
	private ProgressBar pb_load;
	private final static int HW_HIDE_DELAY = 0;
	private final static int HW_POSITION_SHOW = 1;
	private final static int HW_POSITION_SHOWww = 2;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == HW_HIDE_DELAY) {
				((View) msg.obj).setVisibility(View.GONE);
				((View) msg.obj).clearFocus();
			} else if (msg.what == HW_POSITION_SHOW) {
				refreshShowPosition();
			} else if (msg.what == HW_POSITION_SHOWww) {
				pb_load1.setText(String.valueOf(msg.obj));
			}

			if (MediaPlayerHandler.getInstance().getPlayer() != null) {
				int duation = MediaPlayerHandler.getInstance().getPlayer().getDuration();
				String duartionStr = Utils.formatDuration(getApplicationContext(), duation);

				durationTv.setText(duartionStr);
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);

		initView();
		startPlay(true);
		speedThread = new SpeedThread(this);
		speedThread.start();
		speedThread.setRefreshSpeedListener(this);
	}

	private void initView() {
		pb_load = (ProgressBar) findViewById(R.id.pb_load);
		pb_load1 = (TextView) findViewById(R.id.pb_load1);
		durationTv = (TextView) findViewById(R.id.tv_duration);
		postionTv = (TextView) findViewById(R.id.tv_postion);
		tvSurfaceView = (SurfaceView) findViewById(R.id.sv_media);
		Button btn = (Button) findViewById(R.id.btn);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// startPlay(false);
				MediaPlayerHandler.getInstance().seekTo(MediaPlayerHandler.getInstance().getPlayer().getDuration() / 2,
						0);
			}
		});
		btn.requestFocus();

		MediaPlayerHandler.getInstance().setMediaStatusCallback(new MediaStatusCallback.SimpleMediaStatusCallback() {
			@Override
			public void onError(int what, int extra) {
				// showMsg("报错啦！what:" + what + " extra:" + extra);
			}

			@Override
			public void onLoading() {
				pb_load.setVisibility(View.VISIBLE);
				pb_load1.setVisibility(View.VISIBLE);
			}

			@Override
			public void onStartPlay() {
				// doStartPlay();
				int duation = MediaPlayerHandler.getInstance().getPlayer().getDuration();
				String duartionStr = Utils.formatDuration(getApplicationContext(), duation);
				durationTv.setText(duartionStr);
			}

			@Override
			public void onContinuePlay() {
				// doStartPlay();
				pb_load.setVisibility(View.GONE);
				pb_load1.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onTimeOut() {
				// showMsg("超时啦！");
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void startPlay(boolean isInit) {
		// url =
		// "http://pl.youku.com/playlist/m3u8?ev=1&token=4095&oip=3070135132&ep=cCaVGkuIV8kC5irZiT8bZH2zInMLXJZ0rGaF%2FrYHSsV%2BNaHQnT7Ywg%3D%3D&ctype=12&ts=1410251724&type=hd2&sid=2410251723961123aaf44&vid=XNjI0OTUzNjMy&keyframe=1";
		url = "http://pl.youku.com/playlist/m3u8?keyframe=1&ep=eiaWHU%2BPUcwG4CPYjT8bYCvhdnBdXJZ1gkTM%2FJgDR8RQNerQmz7Wwg%3D%3D&vid=XODkyMzQwODM2&type=flv&token=2075&oip=1790517073&ev=1&sid=84276272650751277327b&ts=1427627265&ctype=12";
		// "http://pl.youku.com/playlist/m3u8?ev=1&token=5177&oip=3070135132&ep=diaVGkuIV8gB5SfYjT8bMX%2FkcXMKXJZ0gkjN%2FrYHSsRAIa%2FQnD%2FWwA%3D%3D&ctype=12&ts=1410251610&type=hd2&sid=441025161047512fc6545&vid=XNDgxOTUzOTYw&keyframe=1";
		// url =
		// "http://pl.youku.com/playlist/m3u8?ev=1&token=5177&oip=3070135132&ep=diaVGkuIV8gB5SfYjT8bMX%2FkcXMKXJZ0gkjN%2FrYHSsRAIa%2FQnD%2FWwA%3D%3D&ctype=12&ts=1410251610&type=hd2&sid=441025161047512fc6545&vid=XNDgxOTUzOTYw&keyframe=1";
		MediaPlayerHandler.getInstance().setDisplay(tvSurfaceView);
		MediaPlayerHandler.getInstance().setType(TYPE.MIPT);
		MediaPlayerHandler.getInstance().start(url);
//		refreshShowPosition();
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

	@Override
	public void refreshSpeed(String speed) {
		Message msg = handler.obtainMessage(HW_POSITION_SHOW);
		msg.what = 2;
		msg.obj = speed;
		handler.sendMessage(msg);
	}

}
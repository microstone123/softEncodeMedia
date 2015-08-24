package mipt.media;

import android.media.MediaPlayer;

public interface MediaStatusCallback {

	public void onPrepared();

	public void onPreparedStart();

	public void onStartPlay();

	public void onContinuePlay();

	public void onLoading();

	public void onCompletion();

	public void onError(int what, int extra);

	public void onTimeOut();

	public void onBufferingUpdate(int percent);

	public void onSeekCompelete();

	public void resetStopStatus();

	public void callbackChangeType();

	public static class SimpleMediaStatusCallback implements MediaStatusCallback {

		@Override
		public void onPrepared() {
		}

		@Override
		public void onStartPlay() {
		}

		@Override
		public void onLoading() {
		}

		@Override
		public void onCompletion() {
		}

		@Override
		public void onError(int what, int extra) {
		}

		@Override
		public void onBufferingUpdate(int percent) {

		}

		@Override
		public void onTimeOut() {

		}

		@Override
		public void onSeekCompelete() {

		}

		@Override
		public void onPreparedStart() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onContinuePlay() {
			// TODO Auto-generated method stub

		}

		@Override
		public void resetStopStatus() {
			// TODO Auto-generated method stub

		}

		@Override
		public void callbackChangeType() {
			// TODO Auto-generated method stub

		}

	}

}

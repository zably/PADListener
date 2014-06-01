
package fr.neraud.padlistener.gui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import fr.neraud.padlistener.model.SyncComputeResultModel;
import fr.neraud.padlistener.service.ComputeSyncService;
import fr.neraud.padlistener.service.constant.RestCallError;
import fr.neraud.padlistener.service.constant.RestCallRunningStep;
import fr.neraud.padlistener.service.constant.RestCallState;
import fr.neraud.padlistener.service.receiver.AbstractRestResultReceiver;

public class ComputeSyncTaskFragment extends Fragment {

	private RestCallState state = null;
	private RestCallRunningStep runningStep = null;
	private SyncComputeResultModel syncResult = null;
	private String errorMessage = null;
	private CallBacks callbacks = null;

	public static interface CallBacks {

		public void updateState(RestCallState state, RestCallRunningStep runningStep, SyncComputeResultModel syncResult,
		        String errorMessage);
	}

	private class MyComputeSyncReceiver extends AbstractRestResultReceiver<SyncComputeResultModel> {

		public MyComputeSyncReceiver(Handler handler) {
			super(handler);
		}

		@Override
		protected void onReceiveProgress(RestCallRunningStep progress) {
			Log.d(getClass().getName(), "onReceiveProgress : " + progress);
			state = RestCallState.RUNNING;
			runningStep = progress;
			notifyCallBacks();
		}

		@Override
		protected void onReceiveSuccess(SyncComputeResultModel result) {
			Log.d(getClass().getName(), "onReceiveSuccess");
			state = RestCallState.SUCCESSED;
			syncResult = result;
			notifyCallBacks();
		}

		@Override
		protected void onReceiveError(RestCallError error, String message) {
			Log.d(getClass().getName(), "onReceiveError : " + error);
			state = RestCallState.FAILED;
			errorMessage = message;
			notifyCallBacks();
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(getClass().getName(), "onCreate");
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
	}

	@Override
	public void onDetach() {
		Log.d(getClass().getName(), "onDetach");
		super.onDetach();
		callbacks = null;
	}

	public void registerCallbacks(CallBacks callbacks) {
		this.callbacks = callbacks;
		notifyCallBacks();
	}

	private void notifyCallBacks() {
		if (callbacks != null) {
			callbacks.updateState(state, runningStep, syncResult, errorMessage);
		}
	}

	public void startComputeSyncService() {
		Log.d(getClass().getName(), "startComputeSyncService");
		state = RestCallState.RUNNING;
		runningStep = null;
		syncResult = null;
		errorMessage = null;
		notifyCallBacks();

		final Intent startIntent = new Intent(getActivity(), ComputeSyncService.class);
		startIntent.putExtra(AbstractRestResultReceiver.RECEIVER_EXTRA_NAME, new MyComputeSyncReceiver(new Handler()));
		getActivity().startService(startIntent);
	}

}
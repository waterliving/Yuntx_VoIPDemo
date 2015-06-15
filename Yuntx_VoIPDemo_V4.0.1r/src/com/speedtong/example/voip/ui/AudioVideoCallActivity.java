/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.cloopen.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */package com.speedtong.example.voip.ui;

import java.io.File;







import java.lang.ref.WeakReference;
import java.util.Arrays;

import org.webrtc.videoengine.ViERenderer;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.CCP.phone.CameraCapbility;
import com.speedtong.example.voip.R;
import com.speedtong.example.voip.common.utils.CCPNotificationManager;
import com.speedtong.example.voip.common.utils.FileUtils;
import com.speedtong.example.voip.core.SDKCoreHelper;
import com.speedtong.sdk.ECDevice;
import com.speedtong.sdk.ECDevice.CallType;
import com.speedtong.sdk.ECDevice.Reason;
import com.speedtong.sdk.Rotate;
import com.speedtong.sdk.VoipCall;
import com.speedtong.sdk.debug.ECLog4Util;
import com.speedtong.sdk.exception.CCPRecordException;


public class AudioVideoCallActivity extends Activity {

	public static final int WHAT_ON_CODE_CALL_STATUS = 11;
	public static final int WHAT_ON_SHOW_LOCAL_SURFACEVIEW = 12;

	public static String mCurrentCallId;

	protected boolean isConnect = false;

	protected boolean isIncomingCall = false;

	protected boolean callRecordEnabled;
	// video
	protected CallType mCallType;

	// Local Video
	public RelativeLayout mLoaclVideoView;

	protected RelativeLayout.LayoutParams layoutParams = null;

	public static ImageView mCallTransferBtn;
	// 静音按钮
	public ImageView mCallMute;
	// 免提按钮
	public ImageView mCallHandFree;
	protected SeekBar mSeekBar;

	// The first rear facing camera
	public int defaultCameraId;

	public int cameraCurrentlyLocked;

	public int mCameraCapbilityIndex;

	// 是否静音
	public boolean isMute = false;
	// 是否免提
	public boolean isHandsfree = false;

	public VideoCallHandle mVideoCallHandle;

	private KeyguardManager.KeyguardLock mKeyguardLock = null;
	private KeyguardManager mKeyguardManager = null;
	private PowerManager.WakeLock mWakeLock;
	protected int scale;
	private KickOffReceiver kickoffReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		super.onCreate(savedInstanceState);

		mVideoCallHandle = new VideoCallHandle(this);
		SDKCoreHelper.getInstance().setHandler(mVideoCallHandle);

		layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		
		registerReceiver(new String[] { getPackageName() + "kickedoff" });

	}
	
	public void startVoiceRecording(String callid) {

		if (!FileUtils.isExistExternalStore()) {
			return;
		}
		File callRecordFile = FileUtils.createCallRecordFilePath(callid, "wav");
		if (callRecordFile != null) {

			try {
				ECDevice.getECVoipCallManager().startVoiceCallRecording(callid,
						callRecordFile.getAbsolutePath());
				Toast.makeText(getApplicationContext(), "开始录音",
						Toast.LENGTH_LONG).show();
			} catch (CCPRecordException e) {
				e.printStackTrace();
			}
		}

	}
	
	public void setViewEnable(final View v){
		
		mVideoCallHandle.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				v.setEnabled(true);
			}
		}, 2000);
		
	}
	
	
	public void stopVoiceRecording(String callid) {
	
			ECDevice.getECVoipCallManager().stopVoiceCallRecording(callid);
		
	}

	public void enterIncallMode() {
		if (!(mWakeLock.isHeld())) {
			
			mWakeLock.setReferenceCounted(false);
			mWakeLock.acquire();
		}
		mKeyguardLock = this.mKeyguardManager.newKeyguardLock("");
		mKeyguardLock.disableKeyguard();
	}

	public void initProwerManager() {
		mWakeLock = ((PowerManager) getSystemService("power")).newWakeLock(
				PowerManager.SCREEN_DIM_WAKE_LOCK
						| PowerManager.ACQUIRE_CAUSES_WAKEUP, "CALL_ACTIVITY#"
						+ super.getClass().getName());
		mKeyguardManager = ((KeyguardManager) getSystemService("keyguard"));
	}

	public void releaseWakeLock() {
		try {
			if (this.mWakeLock.isHeld()) {
				if (this.mKeyguardLock != null) {
					this.mKeyguardLock.reenableKeyguard();
					this.mKeyguardLock = null;
				}
				this.mWakeLock.release();
			}
			return;
		} catch (Exception localException) {
			ECLog4Util.e("AndroidRuntime", localException.toString());
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		lockScreen();

		if (mCallType == ECDevice.CallType.VIDEO) {
			cameraCurrentlyLocked = defaultCameraId;

			ECDevice.getECVoipSetManager().selectCamera(cameraCurrentlyLocked,
					mCameraCapbilityIndex, 15, Rotate.Rotate_Auto, true);
		}
		CCPNotificationManager.cancleCCPNotification(this,
				CCPNotificationManager.CCP_NOTIFICATOIN_ID_CALLING);

		

	}
	public static final String DEMO_TAG = "CCP_Demo";
	
	public void lockScreen() {
		KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

		// Get a keyboard lock manager object
		if (km.inKeyguardRestrictedInputMode()) {
			kl = km.newKeyguardLock(DEMO_TAG);
			// Parameter is used by Tag LogCat.
			kl.disableKeyguard();// Unlock.
		}

		mWakeLock.acquire();
	}
	private KeyguardLock kl = null;

	// Release the lock screen and the screen brightness manager, 
	// reply to the system default state
	public void releaseLockScreen() {
		if (kl != null) {
			kl.reenableKeyguard();
		}
		
		if (mWakeLock != null) {
			try {
				mWakeLock.release();
			} catch (Exception e) {
				System.out.println("mWakeLock may already release");
			}
		}
	}
	
	
	private class  KickOffReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent != null
					&& intent.getAction().equalsIgnoreCase(
							getPackageName() + "kickedoff")) {

				handlerKickOff();
			}

		}
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		 releaseLockScreen();
	}

	public void handlerKickOff() {
		
		
	}

	@Override
	protected void onStop() {
		super.onStop();

		if(isConnect) {
			if(isIncomingCall) {
				CCPNotificationManager.showInCallingNotication(
						getApplicationContext(), mCallType,
						getString(R.string.voip_is_talking_tip), null);
				
			} else {
				CCPNotificationManager.showOutCallingNotication(
						getApplicationContext(), mCallType,
						getString(R.string.voip_is_talking_tip), null);
				
			}
		}

	}

	public void DisplayLocalSurfaceView() {
		if (mCallType == ECDevice.CallType.VIDEO && mLoaclVideoView != null
				&& mLoaclVideoView.getVisibility() == View.VISIBLE) {
			// Create a RelativeLayout container that will hold a SurfaceView,
			// and set it as the content of our activity.
			SurfaceView localView = ViERenderer.CreateLocalRenderer(this);
			localView.setLayoutParams(layoutParams);
			localView.setZOrderOnTop(true);
			mLoaclVideoView.removeAllViews();
			mLoaclVideoView.setBackgroundColor(getResources().getColor(
					R.color.white));
			mLoaclVideoView.addView(localView);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		CCPNotificationManager.cancleCCPNotification(this, CCPNotificationManager.CCP_NOTIFICATOIN_ID_CALLING);
		unregisterReceiver(kickoffReceiver);
	}

	protected void doHandUpReleaseCall() {
	}

	/**
	 * Callback return {@link VideoCallHandle}
	 * 
	 * @return
	 */
	public VideoCallHandle getCallHandler() {
		return mVideoCallHandle;
	}

	public void setCallHandler(VideoCallHandle handler) {
		mVideoCallHandle = handler;
	}

	public void initCallTools() {

		try {
			isMute = ECDevice.getECVoipSetManager().getMuteStatus();
			isHandsfree = ECDevice.getECVoipSetManager().getLoudSpeakerStatus();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	protected final void registerReceiver(String[] actionArray) {
		if (actionArray == null) {
			return;
		}
		IntentFilter intentfilter = new IntentFilter();
		for (String action : actionArray) {
			intentfilter.addAction(action);
		}
		if (kickoffReceiver == null) {
			kickoffReceiver = new KickOffReceiver();
		}
		registerReceiver(kickoffReceiver, intentfilter);
	}

	/**
	 * 设置呼叫转接
	 */
	public int setCallTransfer(String mCurrentCallId, String transToNumber) {


		return 0;

	}

	/**
	 * 设置静音
	 */
	public void setMuteUI() {
		try {

			ECDevice.getECVoipSetManager().setMute(!isMute);

			isMute = ECDevice.getECVoipSetManager().getMuteStatus();

			if (isMute) {
				mCallMute.setImageResource(R.drawable.call_interface_mute_on);
			} else {
				mCallMute.setImageResource(R.drawable.call_interface_mute);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 设置免提
	 */
	public void sethandfreeUI() {
		try {

			ECDevice.getECVoipSetManager().enableLoudSpeaker(!isHandsfree);
			isHandsfree = ECDevice.getECVoipSetManager().getLoudSpeakerStatus();

			if (isHandsfree) {
				mCallHandFree
						.setImageResource(R.drawable.call_interface_hands_free_on);
			} else {
				mCallHandFree
						.setImageResource(R.drawable.call_interface_hands_free);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void onCallVideoRatioChanged(String callid, int width, int height) {
	}

	protected void onCallEvents(VoipCall voipCall) {

	}

	public static class VideoCallHandle extends Handler {

		WeakReference<AudioVideoCallActivity> mActivity;

		public VideoCallHandle(AudioVideoCallActivity activity) {
			mActivity = new WeakReference<AudioVideoCallActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			AudioVideoCallActivity mCallActivity = mActivity.get();

			if (mCallActivity == null) {
				return;
			}
			String callid = null;
			Reason reason = Reason.UNKNOWN;
			Bundle b = null;
			VoipCall voipCall = null;

			if (msg.obj instanceof VoipCall) {
				voipCall = (VoipCall) msg.obj;
				callid = voipCall.getCallId();
			} else if (msg.obj instanceof Bundle) {
				b = (Bundle) msg.obj;
			}

			switch (msg.what) {
			case SDKCoreHelper.WHAT_ON_CALL_ALERTING:

			case SDKCoreHelper.WHAT_ON_CALL_PROCEEDING:

			case SDKCoreHelper.WHAT_ON_CALL_ANSWERED:

			case SDKCoreHelper.WHAT_ON_CALL_PAUSED:
			case SDKCoreHelper.WHAT_ON_CALL_PAUSED_REMOTE:

			case SDKCoreHelper.WHAT_ON_CALL_MAKECALL_FAILED:

				mCallActivity.onCallEvents(voipCall);

				break;

			case SDKCoreHelper.WHAT_ON_CALL_RELEASED:

				mCallActivity.onCallEvents(voipCall);
				if (mCallTransferBtn != null)
					mCallTransferBtn.setEnabled(false);
				break;

			case SDKCoreHelper.WHAT_ON_CALL_VIDEO_RATIO_CHANGED:

				if (b.containsKey("width") && b.containsKey("height")) {
					int width = b.getInt("width");
					int height = b.getInt("height");
					String callId = b.getString("callid");
					mCallActivity
							.onCallVideoRatioChanged(callId, width, height);
				}

				break;

			default:
				break;
			}

		}
	}

	public void comportCapbilityIndex(CameraCapbility[] caps) {

		if(caps == null ) {
			return;
		}
		int pixel[] = new int[caps.length];
		int _pixel[] = new int[caps.length];
		for(CameraCapbility cap : caps) {
			if(cap.index >= pixel.length) {
				continue;
			}
			pixel[cap.index] = cap.width * cap.height;
		}
		
		System.arraycopy(pixel, 0, _pixel, 0, caps.length);
		
		Arrays.sort(_pixel);
		for(int i = 0 ; i < caps.length ; i++) {
			if(pixel[i] == /*_pixel[0]*/ 352*288) {
				mCameraCapbilityIndex = i;
				return;
			}
		}
	}

	/**
	 * 像素转化dip
	 * 
	 * @param context
	 * @param pxValue
	 * @return
	 */
	public static int px2dip(Context context, float pxValue) {

		final float scale = context.getResources().getDisplayMetrics().density;

		return (int) (pxValue / scale + 0.5f);

	}

	public int[] decodeDisplayMetrics() {
		int[] metrics = new int[2];
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		metrics[0] = displayMetrics.widthPixels;
		metrics[1] = displayMetrics.heightPixels;
		return metrics;
	}
}

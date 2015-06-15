package com.speedtong.example.voip.ui;

import org.webrtc.videoengine.ViERenderer;



import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.CCP.phone.CameraInfo;
import com.speedtong.example.voip.ECApplication;
import com.speedtong.example.voip.R;
import com.speedtong.example.voip.common.utils.ToastUtil;
import com.speedtong.sdk.ECDevice;
import com.speedtong.sdk.VoipCall;
import com.speedtong.sdk.ECDevice.CallType;
import com.speedtong.sdk.ECDevice.ECDeviceState;
import com.speedtong.sdk.ECDevice.Reason;
import com.speedtong.sdk.Rotate;
import com.speedtong.sdk.debug.ECLog4Util;


public class VideoActivity extends AudioVideoCallActivity implements View.OnClickListener ,SeekBar.OnSeekBarChangeListener{

	private static final String TAG = "VideoActivity";
	private static long lastClickTime;
	private Button mVideoStop;
	private Button mVideoBegin;
	private Button mVideoCancle;
	private ImageView mVideoIcon;
	private RelativeLayout mVideoTipsLy;
	
	private TextView mVideoTopTips;
	private TextView mVideoCallTips;
	private TextView mCallStatus;
	private SurfaceView mVideoView;
	// Remote Video
	private FrameLayout mVideoLayout;
	private Chronometer mChronometer;
	// voip 账号
	private String mVoipAccount;
	// 通话 ID
	private String mCurrentCallId;
	
	private View mCameraSwitch;
	
	CameraInfo[] cameraInfos;
	
	int numberOfCameras;
	
	private int mWidth;
	private int mHeight;
	
	
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.layout_video_activity);

		isIncomingCall = false;
		mCallType = ECDevice.CallType.VIDEO;
		initProwerManager();
		enterIncallMode();
		initResourceRefs();
		initialize();

		cameraInfos = ECDevice.getECVoipSetManager().getCameraInfos();

		// Find the ID of the default camera
		if (cameraInfos != null) {
			numberOfCameras = cameraInfos.length;
		}

		// Find the total number of cameras available
		for (int i = 0; i < numberOfCameras; i++) {
			if (cameraInfos[i].index == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
				defaultCameraId = i;
				comportCapbilityIndex(cameraInfos[i].caps);
			}
		}
		

		mCurrentCallId = ECDevice.getECVoipCallManager().makeCall(
				CallType.VIDEO, mVoipAccount);

		if (TextUtils.isEmpty(mCurrentCallId)) {
			ToastUtil.showMessage(R.string.no_support_voip);
			ECLog4Util.d(TAG, "[CallOutActivity] Sorry, "
					+ getString(R.string.no_support_voip) + " , Call failed. ");
			finish();
			return;
		}
		ECDevice.getECVoipSetManager().setVideoView(mVideoView, null);
		DisplayLocalSurfaceView();

	}
	
	@Override
	public void handlerKickOff() {
		// TODO Auto-generated method stub
		super.handlerKickOff();
		doHandUpReleaseCall();
		finishCalling();
	}
	
	private void initResourceRefs() {
		mVideoTipsLy = (RelativeLayout) findViewById(R.id.video_call_in_ly);
		mVideoIcon = (ImageView) findViewById(R.id.video_icon);

		mVideoTopTips = (TextView) findViewById(R.id.notice_tips);
		mVideoCallTips = (TextView) findViewById(R.id.video_call_tips);

		mVideoCancle = (Button) findViewById(R.id.video_botton_cancle);
		mVideoBegin = (Button) findViewById(R.id.video_botton_begin);
		mVideoStop = (Button) findViewById(R.id.video_stop);
		mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
		mSeekBar.setOnSeekBarChangeListener(this);
		mVideoStop.setEnabled(false);
		mVideoCancle.setOnClickListener(this);
		mVideoBegin.setOnClickListener(this);
		mVideoStop.setOnClickListener(this);

		mVideoView = (SurfaceView) findViewById(R.id.video_view);
//		mVideoView.setVisibility(View.INVISIBLE);
		mLoaclVideoView = (RelativeLayout) findViewById(R.id.localvideo_view);
		mVideoLayout = (FrameLayout) findViewById(R.id.Video_layout);
		mCameraSwitch = findViewById(R.id.camera_switch);
		mCameraSwitch.setOnClickListener(this);
		video_switch = findViewById(R.id.video_switch);
		video_switch.setOnClickListener(this);

		mCallStatus = (TextView) findViewById(R.id.call_status);
		mCallStatus.setVisibility(View.GONE);
//		 mVideoView.getHolder().setFixedSize(width, height);
		mVideoView.getHolder().setFixedSize(240, 320);

		

//		SurfaceView localView = ViERenderer.CreateLocalRenderer(this);
//		mLoaclVideoView.addView(localView);
	}

	/**
	 * Read parameters or previously saved state of this activity.
	 */
	private void initialize() {
		Intent intent = getIntent();
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle == null) {
				finish();
				return;
			}
			
			// Video...
			mVoipAccount = bundle.getString(ECApplication.VALUE_DIAL_VOIP_INPUT);
			if (mVoipAccount == null) {
				finish();
				return;
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		releaseWakeLock();
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void doHandUpReleaseCall() {
		super.doHandUpReleaseCall();
		
		// Hang up the video call...
		ECLog4Util.d(TAG ,"[VideoActivity] onClick: Voip talk hand up, CurrentCallId " + mCurrentCallId);
		try {
			if (mCurrentCallId != null) {
				ECDevice.getECVoipCallManager().releaseCall(mCurrentCallId);
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(!isConnect) {
			finish();
		}
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			
			// do nothing.
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.video_botton_begin:

			break;

		case R.id.video_stop:
		case R.id.video_botton_cancle:

			doHandUpReleaseCall();
			break;
		case R.id.video_switch:

			CallType callType = ECDevice.getECVoipSetManager().getCallType(
					mCurrentCallId);
			if (callType == CallType.VOICE) {
				ECDevice.getECVoipCallManager().requestSwitchCallMediaType(
						mCurrentCallId, CallType.VIDEO);
			} else {
				ECDevice.getECVoipCallManager().requestSwitchCallMediaType(
						mCurrentCallId, CallType.VOICE);
			}

			video_switch.setEnabled(false);
			setViewEnable(video_switch);

			break;

		case R.id.camera_switch:

			// check for availability of multiple cameras
			if (numberOfCameras == 1) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(this.getString(R.string.camera_alert))
						.setNeutralButton(R.string.dialog_alert_close, null);
				AlertDialog alert = builder.create();
				alert.show();
				return;
			}
			mCameraSwitch.setEnabled(false);

			// OK, we have multiple cameras.
			// Release this camera -> cameraCurrentlyLocked
			cameraCurrentlyLocked = (cameraCurrentlyLocked + 1)
					% numberOfCameras;
			comportCapbilityIndex(cameraInfos[cameraCurrentlyLocked].caps);

			ECDevice.getECVoipSetManager().selectCamera(cameraCurrentlyLocked,
					mCameraCapbilityIndex, 15, Rotate.Rotate_Auto, false);

			if (cameraCurrentlyLocked == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
				defaultCameraId = android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;
				Toast.makeText(VideoActivity.this,
						R.string.camera_switch_front, Toast.LENGTH_SHORT)
						.show();
			} else {
				defaultCameraId = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;
				Toast.makeText(VideoActivity.this, R.string.camera_switch_back,
						Toast.LENGTH_SHORT).show();

			}

			setViewEnable(mCameraSwitch);

			break;
		default:
			break;
		}
	}
	
	
	
	
	@Override
	protected void onCallEvents(VoipCall voipCall) {
		// TODO Auto-generated method stub
		super.onCallEvents(voipCall);

		if (voipCall == null) {
			return;
		}
		String callid = voipCall.getCallId();

		switch (voipCall.getEcCallState()) {
		case ECallAlerting:

			ECLog4Util.d(TAG, "[VideoActivity] handleMessage: voip alerting!!");
			if (callid != null && callid.equals(mCurrentCallId)) {// 等待对方接受邀请...
				mVideoCallTips
						.setText(getString(R.string.str_tips_wait_invited));
			}

			break;
		case ECallProceeding:

			if (callid != null && callid.equals(mCurrentCallId)) {
				mVideoCallTips.setText(getString(R.string.voip_call_connect));
			}

			break;
		case ECallAnswered:

			ECLog4Util.d(TAG,
					"[VideoActivity] handleMessage: voip on call answered!!");
			if (callid != null && callid.equals(mCurrentCallId) && !isConnect) {
				initResVideoSuccess();
			}

			if (getCallHandler() != null) {
				getCallHandler().sendMessage(
						getCallHandler().obtainMessage(
								VideoActivity.WHAT_ON_CODE_CALL_STATUS));
			}
			ECDevice.getECVoipSetManager().enableLoudSpeaker(true);

			break;
		case ECallFailed:

			ECLog4Util
					.d(TAG,
							"[VideoActivity] handleMessage: voip on call makecall failed!!");
			if (callid != null && callid.equals(mCurrentCallId)) {
				finishCalling(voipCall.getReason());

			}

			break;
		case ECallPaused:

			break;
		case ECallPausedByRemote:

			break;
		case ECallReleased:

			ECLog4Util.d(TAG,
					"[VideoActivity] handleMessage: voip on call released!!");
			if (callid != null && callid.equals(mCurrentCallId)) {
				finishCalling();
			}

			break;

		default:
			break;
		}

	}
	
	
	
	
	
	
	
	
	
	
//	@Override
//	protected void handleNotifyMessage(Message msg) {
//		super.handleNotifyMessage(msg);
//		switch (msg.what) {
//		case WHAT_ON_CODE_CALL_STATUS:
//			if(!checkeDeviceHelper()) {
//				return;
//			}
//			CallStatisticsInfo callStatistics = getDeviceHelper().getCallStatistics(Device.CallType.VIDEO);
//			if(callStatistics != null) {
//				int fractionLost = callStatistics.getFractionLost();
//				int rttMs = callStatistics.getRttMs();
//				NetworkStatistic trafficStats = getDeviceHelper().getNetworkStatistic(mCurrentCallId);
//				if(trafficStats != null)
//				{
//					addNotificatoinToView(getString(R.string.str_call_traffic_status, rttMs, (fractionLost/255),trafficStats.getTxBytes()/1024 , trafficStats.getRxBytes()/1024));
//				}
//				else
//				{
//					//mCallStatus.setText(getString(R.string.str_call_status, rttMs, (fractionLost/255)));
//					addNotificatoinToView(getString(R.string.str_call_status, rttMs, (fractionLost/255)));
//				}
//			}
//			if(isConnect) {
//				Message callMessage = getCallHandler().obtainMessage(WHAT_ON_CODE_CALL_STATUS);
//				getCallHandler().sendMessageDelayed(callMessage,4000);
//			}
//			break;
//			
//			
//			// This call may be redundant, but to ensure compatibility system event more, 
//			// not only is the system call
//		case CCPHelper.WHAT_ON_RECEIVE_SYSTEM_EVENTS:
//			
//			doHandUpReleaseCall();
//
//		default:
//			break;
//		}
//	}
	
	//
	private void initResVideoSuccess() {
		isConnect = true;
		mVideoLayout.setVisibility(View.VISIBLE);
		mVideoIcon.setVisibility(View.GONE);
		mVideoTopTips.setVisibility(View.GONE);
		mCameraSwitch.setVisibility(View.VISIBLE);
		mVideoTipsLy.setVisibility(View.VISIBLE);
		
		// bottom ...
		mVideoCancle.setVisibility(View.GONE);
		mVideoCallTips.setVisibility(View.VISIBLE); 
		mVideoCallTips.setText(getString(R.string.str_video_bottom_time,mVoipAccount.substring(mVoipAccount.length() - 3, mVoipAccount.length())));
		mVideoStop.setVisibility(View.VISIBLE);
		mVideoStop.setEnabled(true);
		
		
		mChronometer = (Chronometer) findViewById(R.id.chronometer);	
		mChronometer.setBase(SystemClock.elapsedRealtime());
		mChronometer.setVisibility(View.VISIBLE);
		mChronometer.start();
		
		
//		new Handler().postDelayed(new Runnable() {
//			
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				
////				ECLog4Util.e(TAG, ECDevice.getECVoipSetManager().getLocalVideoSnapshot().toString());
//				ECLog4Util.e(TAG, ECDevice.getECVoipSetManager().getRemoteVideoSnapshot().toString());
//				ECLog4Util.e(TAG, ECDevice.getECVoipSetManager().getNetworkStatistic(mCurrentCallId).toString());
//				
//				
//			}
//		}, 15000);
	}
	
	
	
	/**
	 * 根据状态,修改按钮属性及关闭操作
	 * 
	 * @param reason
	 */
	private void finishCalling() {
		try {
			//mChronometer.setVisibility(View.GONE);
			
			mVideoTopTips.setVisibility(View.VISIBLE);
			mCameraSwitch.setVisibility(View.GONE);
			mVideoTopTips.setText(R.string.voip_calling_finish);
			
			
			if(isConnect) {
				// set Chronometer view gone..
				mChronometer.stop();
				mVideoLayout.setVisibility(View.GONE);
				mVideoIcon.setVisibility(View.VISIBLE);
				
				mLoaclVideoView.removeAllViews();
				mLoaclVideoView.setVisibility(View.GONE);
				
				// bottom can't click ...
				mVideoStop.setEnabled(false);
			} else {
				mVideoCancle.setEnabled(false);
			}
			
			// 3 second mis , finsh this activit ...
			getCallHandler().postDelayed(finish, 3000);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			isConnect = false;
		}
	}
	
	
	
	@Override
	protected void onCallVideoRatioChanged(String callid, int width, int height) {
		super.onCallVideoRatioChanged(callid, width, height);
		try {
			mWidth = width;
			mHeight = height;
			if (mVideoView != null) {
//				 mVideoView.getHolder().setFixedSize(width,
//				 height);
				LayoutParams layoutParams2 = (LayoutParams) mVideoView
						.getLayoutParams();
				layoutParams2.width = mWidth;
				layoutParams2.height = mHeight;
				mVideoView.setLayoutParams(layoutParams2);
				
				mVideoView.setVisibility(View.VISIBLE);
			}
			int[] decodeDisplayMetrics = decodeDisplayMetrics();
			if (mWidth >= decodeDisplayMetrics[0]
					|| mHeight >= decodeDisplayMetrics[1]) {
				// scale = 1;
				mSeekBar.setMax(decodeDisplayMetrics[0]);
				mSeekBar.setProgress(decodeDisplayMetrics[0]);
				mSeekBar.setVisibility(View.VISIBLE);
				return;
			}

			mSeekBar.setMax(decodeDisplayMetrics[0]);
			mSeekBar.setProgress(decodeDisplayMetrics[0]);
			mSeekBar.setVisibility(View.VISIBLE);

			doResizeView(decodeDisplayMetrics[0]);
		} catch (Exception e) {
		}
	}
	
	private void finishCalling(int reason) {
		try {
			mVideoTopTips.setVisibility(View.VISIBLE);
			mCameraSwitch.setVisibility(View.GONE);
			mLoaclVideoView.removeAllViews();
			mLoaclVideoView.setVisibility(View.GONE);
			if(isConnect) {
				mChronometer.stop();
				mVideoLayout.setVisibility(View.GONE);
				mVideoIcon.setVisibility(View.VISIBLE);
				isConnect = false;
				// bottom can't click ...
				mVideoStop.setEnabled(false);
			} else {
				mVideoCancle.setEnabled(false);
			}
			isConnect = false;
			
			getCallHandler().postDelayed(finish, 3000);
			if(reason == Reason.DECLINED.getStatus() || reason == Reason.BUSY.getStatus()) {
				getCallHandler().removeCallbacks(finish);
				mVideoTopTips.setText(getString(R.string.str_video_call_end,
						mVoipAccount.substring(mVoipAccount.length() - 3, mVoipAccount.length())));
			} else {
				if (reason == Reason.CALLMISSED.getStatus()) {
					mVideoTopTips.setText(getString(R.string.voip_calling_timeout));
				} else if (reason == Reason.MAINACCOUNTPAYMENT.getStatus()) {
					mVideoTopTips.setText(getString(R.string.voip_call_fail_no_cash));
				} else if (reason == Reason.UNKNOWN.getStatus()) {
					mVideoTopTips.setText(getString(R.string.voip_calling_finish));
				} else if (reason == Reason.NOTRESPONSE.getStatus()) {
					mVideoTopTips.setText(getString(R.string.voip_call_fail));
				} else if (reason == Reason.VERSIONNOTSUPPORT.getStatus()) {
					mVideoTopTips.setText(getString(R.string.str_video_not_support));
				} else if (reason == Reason.OTHERVERSIONNOTSUPPORT.getStatus()) {
					mVideoTopTips.setText(getString(R.string.str_other_voip_not_support));
				}  else {
					ThirdSquareError(reason);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Increased Voip P2P, direct error code:
	* <p>Title: ThirdSquareError</p>
	* <p>Description: </p>
	* @param reason
	 */
	private void ThirdSquareError(int reason) {
		if (reason == Reason.AUTHADDRESSFAILED.getStatus()) {//  第三方鉴权地址连接失败
			mVideoTopTips.setText(getString(R.string.voip_call_fail_connection_failed_auth));
		} else if (reason == Reason.MAINACCOUNTPAYMENT.getStatus()) {//  第三方主账号余额不足
			
			mVideoTopTips.setText(getString(R.string.voip_call_fail_no_pay_account));
		} else if (reason == Reason.MAINACCOUNTINVALID.getStatus()) { //  第三方应用ID未找到
			
			mVideoTopTips.setText(getString(R.string.voip_call_fail_not_find_appid));
		} else if (reason == Reason.CALLERSAMECALLED.getStatus()) {//  第三方应用未上线限制呼叫已配置测试号码
			
			mVideoTopTips.setText(getString(R.string.voip_call_fail_not_online_only_call));
		} else if (reason == Reason.SUBACCOUNTPAYMENT.getStatus()) {//  第三方鉴权失败，子账号余额
			
			mVideoTopTips.setText(getString(R.string.voip_call_auth_failed));
		} else {
			mVideoTopTips.setText(getString(R.string.voip_calling_network_instability));
		}
	}
	
	/**
	 * 延时关闭界面
	 */
	final Runnable finish = new Runnable() {
		public void run() {
			finish();
		}
	};
	private View video_switch;



	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		
		
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		int progress = arg0.getProgress();
		doResizeView(progress);
	}

	/**
	 * @param progress
	 */
	private void doResizeView(int progress) {
		if (mVideoView != null) {
			LayoutParams layoutParams2 = (LayoutParams) mVideoView
					.getLayoutParams();
			layoutParams2.width = progress;
			layoutParams2.height = mHeight * progress / mWidth ;
//			int[] decodeDisplayMetrics = decodeDisplayMetrics();
//			layoutParams2.height=decodeDisplayMetrics[1];
			mVideoView.setLayoutParams(layoutParams2);
			
		}
	}

}

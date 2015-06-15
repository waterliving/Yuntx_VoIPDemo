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
 */
package com.speedtong.example.voip.ui;

import com.speedtong.example.voip.ECApplication;

import com.speedtong.example.voip.R;
import com.speedtong.example.voip.common.utils.ToastUtil;
import com.speedtong.sdk.ECDevice;
import com.speedtong.sdk.VoipCall;
import com.speedtong.sdk.ECDevice.CallType;

import com.speedtong.sdk.ECDevice.Reason;
import com.speedtong.sdk.debug.ECLog4Util;
import com.speedtong.sdk.platformtools.VoiceUtil;

import android.content.Context;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;



/**
 * 
 * Voip呼出界面，呼出方用于显示和操作通话过程。
 * 
 * @version 1.0.0
 */
public class CallOutActivity extends AudioVideoCallActivity implements OnClickListener {

	private static final int REQUEST_CODE_CALL_TRANSFER = 110;

	private static final String TAG = "CallOutActivity";

	// 话筒调节控制区
	private LinearLayout mCallAudio;
	// 键盘
	private ImageView mDiaerpadBtn;
	// 键盘区
	private LinearLayout mDiaerpad;

	// 挂机按钮
	private ImageView mVHangUp;
	// 动态状态显示区
	private TextView mCallStateTips;
	private Chronometer mChronometer;
	// 号码显示区
//	private TextView mVtalkNumber;
	// private TextView mCallStatus;
	// 号码
	private String mPhoneNumber;
	// 通话 ID
	// public static String mCurrentCallId;
	// voip 账号
	private String mVoipAccount;
	// 通话类型，直拨，落地, 回拨
	private String mType = "";
	// 是否键盘显示
	private boolean isDialerShow = false;

	private Button mPause;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_call_interface);

		isIncomingCall = false;
		mCallType = ECDevice.CallType.VOICE;
		initProwerManager();
		enterIncallMode();
		initResourceRefs();
		initialize();
		initCall();
		
		
	}
	@Override
	public void handlerKickOff() {
		// TODO Auto-generated method stub
		super.handlerKickOff();
		
		doHandUpReleaseCall();
		finishCalling();
	}

	/**
	 * Initialize all UI elements from resources.
	 * 
	 */
	private void initResourceRefs() {
		mCallTransferBtn = (ImageView) findViewById(R.id.layout_callin_transfer);
		mCallTransferBtn.setEnabled(true);
		mCallAudio = (LinearLayout) findViewById(R.id.layout_call_audio);
		mCallMute = (ImageView) findViewById(R.id.layout_callin_mute);
		mCallHandFree = (ImageView) findViewById(R.id.layout_callin_handfree);
		mVHangUp = (ImageButton) findViewById(R.id.layout_call_reject);
		mCallStateTips = (TextView) findViewById(R.id.layout_callin_duration);

		// call time
		mChronometer = (Chronometer) findViewById(R.id.chronometer);
		mVtalkNumber = (TextView) findViewById(R.id.layout_callin_number);
		mVtalkName = (TextView) findViewById(R.id.layout_callin_name);
		// 键盘按钮
		mDiaerpadBtn = (ImageView) findViewById(R.id.layout_callin_diaerpad);
		mDiaerpad = (LinearLayout) findViewById(R.id.layout_diaerpad);

		mCallTransferBtn.setOnClickListener(this);
		mDiaerpadBtn.setOnClickListener(this);
		mCallMute.setOnClickListener(this);
		mCallHandFree.setOnClickListener(this);
		mVHangUp.setOnClickListener(this);

		setupKeypad();
		mDmfInput = (EditText) findViewById(R.id.dial_input_numer_TXT);

		// mCallStatus = (TextView) findViewById(R.id.call_status);

		mPause = (Button) findViewById(R.id.pause);
		mPause.setOnClickListener(this);
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
			mType = bundle.getString(ECApplication.VALUE_DIAL_MODE);

			if (mType.equals(ECApplication.VALUE_DIAL_MODE_FREE)) {
				// voip免费通话时显示voip账号
				mVoipAccount = bundle.getString(ECApplication.VALUE_DIAL_VOIP_INPUT);
				String voip_name=bundle.getString(ECApplication.VALUE_DIAL_NAME);
				if (mVoipAccount == null) {
					finish();
					return;
				}
				mVtalkNumber.setText(mVoipAccount);
				if(!TextUtils.isEmpty(voip_name)){
					mVtalkName.setText(voip_name);
				}
				
				
				
				
				
			} else {
				// 直拨及回拨显示号码
				mPhoneNumber = bundle.getString(ECApplication.VALUE_DIAL_VOIP_INPUT);
				mVtalkNumber.setText(mPhoneNumber);
			}
		}
	}

	private void setupKeypad() {
		/** Setup the listeners for the buttons */
		findViewById(R.id.zero).setOnClickListener(this);
		findViewById(R.id.one).setOnClickListener(this);
		findViewById(R.id.two).setOnClickListener(this);
		findViewById(R.id.three).setOnClickListener(this);
		findViewById(R.id.four).setOnClickListener(this);
		findViewById(R.id.five).setOnClickListener(this);
		findViewById(R.id.six).setOnClickListener(this);
		findViewById(R.id.seven).setOnClickListener(this);
		findViewById(R.id.eight).setOnClickListener(this);
		findViewById(R.id.nine).setOnClickListener(this);
		findViewById(R.id.star).setOnClickListener(this);
		findViewById(R.id.pound).setOnClickListener(this);
	}

	/**
	 * Initialize mode
	 * 
	 */
	private void initCall() {

		try {
			if (mType.equals(ECApplication.VALUE_DIAL_MODE_FREE)) {
				// voip免费通话
				if (mVoipAccount != null && !TextUtils.isEmpty(mVoipAccount)) {
					mCurrentCallId = ECDevice.getECVoipCallManager().makeCall(
							CallType.VOICE, mVoipAccount);
					ECLog4Util.d(TAG,
							"[CallOutActivity] VoIP calll, mVoipAccount "
									+ mVoipAccount + " currentCallId "
									+ mCurrentCallId);
				}
			} else if (mType.equals(ECApplication.VALUE_DIAL_MODE_DIRECT)) {
				// 直拨

				mPhoneNumber = VoiceUtil.getStandardMDN(mPhoneNumber);
				mCurrentCallId = ECDevice.getECVoipCallManager().makeCall(
						CallType.VOICE, mPhoneNumber);

			} else if (mType.equals(ECApplication.VALUE_DIAL_MODE_BACK)) {
				
			} else {
				finish();
				return;
			}

			if (mCurrentCallId == null || mCurrentCallId.length() < 1) {
				ToastUtil.showMessage(R.string.no_support_voip);
				ECLog4Util.d(TAG, "[CallOutActivity] Sorry, "
						+ getString(R.string.no_support_voip)
						+ " , Call failed. ");
				finish();
				return;
			}

		} catch (Exception e) {
			finish();
			ECLog4Util
					.d(TAG,
							"[CallOutActivity] Sorry, call failure leads to an unknown exception, please try again. ");
			e.printStackTrace();
		}
	}

	boolean isCallPause = false;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.pause:
//			if ( !TextUtils.isEmpty(mCurrentCallId)) {
//				if (isCallPause) {
//					ECDevice.getECVoipCallManager().resumeCall(mCurrentCallId);
//					isCallPause = false;
//
//				} else {
//
//					ECDevice.getECVoipCallManager().pauseCall(mCurrentCallId);
//					isCallPause = true;
//				}
//
//				mPause.setText(isCallPause ? "resume" : "pause");
//
//			}
			break;
		// keypad
		case R.id.zero: {
			keyPressed(KeyEvent.KEYCODE_0);
			return;
		}
		case R.id.one: {
			keyPressed(KeyEvent.KEYCODE_1);
			return;
		}
		case R.id.two: {
			keyPressed(KeyEvent.KEYCODE_2);
			return;
		}
		case R.id.three: {
			keyPressed(KeyEvent.KEYCODE_3);
			return;
		}
		case R.id.four: {
			keyPressed(KeyEvent.KEYCODE_4);
			return;
		}
		case R.id.five: {
			keyPressed(KeyEvent.KEYCODE_5);
			return;
		}
		case R.id.six: {
			keyPressed(KeyEvent.KEYCODE_6);
			return;
		}
		case R.id.seven: {
			keyPressed(KeyEvent.KEYCODE_7);
			return;
		}
		case R.id.eight: {
			keyPressed(KeyEvent.KEYCODE_8);
			return;
		}
		case R.id.nine: {
			keyPressed(KeyEvent.KEYCODE_9);
			return;
		}
		case R.id.star: {
			keyPressed(KeyEvent.KEYCODE_STAR);
			return;
		}
		case R.id.pound: {
			keyPressed(KeyEvent.KEYCODE_POUND);
			return;
		}

		// keybad end ...
		case R.id.layout_call_reject:
			doHandUpReleaseCall();
			break;
		case R.id.layout_callin_mute:
			// 设置静音
			setMuteUI();
			break;
		case R.id.layout_callin_handfree:
			// 设置免提
			sethandfreeUI();
			break;

		case R.id.layout_callin_diaerpad:

			// 设置键盘
			setDialerpadUI();
			break;
		case R.id.layout_callin_transfer: // select voip ...
			
			

			break;
		default:
			break;
		}
	}

	@Override
	protected void doHandUpReleaseCall() {
		super.doHandUpReleaseCall();
		// 挂断电话
		ECLog4Util.d(TAG, "[CallOutActivity] Voip talk hand up, CurrentCallId " + mCurrentCallId);
		try {
			if (mCurrentCallId != null ) {
				ECDevice.getECVoipCallManager().releaseCall(mCurrentCallId);
				
			}

			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	void setDialerpadUI() {
		if (isDialerShow) {
			mDiaerpadBtn.setImageResource(R.drawable.call_interface_diaerpad);
			mDiaerpad.setVisibility(View.GONE);
			isDialerShow = false;
		} else {
			mDiaerpadBtn.setImageResource(R.drawable.call_interface_diaerpad_on);
			mDiaerpad.setVisibility(View.VISIBLE);
			isDialerShow = true;
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

	@Override
	protected void onDestroy() {
		
		super.onDestroy();
			if (isMute) {
				ECDevice.getECVoipSetManager().setMute(!isMute);
			}
			if (isHandsfree) {
				ECDevice.getECVoipSetManager().enableLoudSpeaker(!isHandsfree);
			
		}
		releaseWakeLock();
		if (mVHangUp != null) {
			mVHangUp = null;
		}
		if (mCallAudio != null) {
			mCallAudio = null;
		}
		if (mCallStateTips != null) {
			mCallStateTips = null;
		}
		if (mVtalkNumber != null) {
			mVtalkNumber = null;
		}
		if (mCallMute != null) {
			mCallMute = null;
		}
		if (mCallHandFree != null) {
			mCallHandFree = null;
		}
		if (mDiaerpadBtn != null) {
			mDiaerpadBtn = null;
		}
		mPhoneNumber = null;
		mVoipAccount = null;
		mCurrentCallId = null;
		if (getCallHandler() != null) {
			setCallHandler(null);
		}
		ECApplication.getInstance().setAudioMode(AudioManager.MODE_NORMAL);
		
		
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 屏蔽返回键
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
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
		case ECallAlerting: //对方振铃
			if (callid != null && callid.equals(mCurrentCallId)) {
				mCallStateTips.setText(getString(R.string.voip_calling_wait));
			}
			break;
		case ECallProceeding://正在连接云通讯呼叫对方
			if (callid != null && callid.equals(mCurrentCallId)) {
				mCallStateTips.setText(getString(R.string.voip_call_connect));
			}
			break;
		case ECallAnswered://呼叫应答
			ECLog4Util.d(TAG, "[CallOutActivity] voip on call answered!!");
			if (callid != null && callid.equals(mCurrentCallId)) {
				isConnect = true;
				mCallMute.setEnabled(true);
				initCallTools();
				mChronometer.setBase(SystemClock.elapsedRealtime());
				mChronometer.setVisibility(View.VISIBLE);
				mChronometer.start();
				mCallStateTips.setText("");
				if (getCallHandler() != null) {
					// getCallHandler().sendMessage(getCallHandler().obtainMessage(VideoActivity.WHAT_ON_CODE_CALL_STATUS));
				}
				mCallHandFree
						.setImageResource(R.drawable.call_interface_hands_free);
				// startVoiceRecording(callid);
			}
			break;
		case ECallFailed://呼叫失败
			if (callid != null && callid.equals(mCurrentCallId)) {
				finishCalling(voipCall.getReason());
			}
			break;
		case ECallPaused://主叫呼叫挂起的回调
			ECLog4Util.e(TAG, "ECallPaused");
			break;
		case ECallPausedByRemote:// 对方呼叫挂起的回调
			ECLog4Util.e(TAG, "ECallPausedByRemote");
			break;
		case ECallReleased://结束当前通话
			if (callid != null && callid.equals(mCurrentCallId)) {
				finishCalling();
				// stopVoiceRecording(callid);
			}
			break;
		default:
			break;
		}
	}
	
	



	

	

	

	
	/**
	 * 根据状态,修改按钮属性及关闭操作
	 * 
	 * @param reason
	 */
	private void finishCalling(int reason) {
		try {
			isConnect = false;
			
			mChronometer.stop();
			mChronometer.setVisibility(View.GONE);
			mCallStateTips.setVisibility(View.VISIBLE);

			mCallHandFree.setClickable(false);
			mCallMute.setClickable(false);
			mVHangUp.setClickable(false);
			mDiaerpadBtn.setClickable(false);
			mDiaerpadBtn.setImageResource(R.drawable.call_interface_diaerpad);
			mCallHandFree
					.setImageResource(R.drawable.call_interface_hands_free);
			mCallMute.setImageResource(R.drawable.call_interface_mute);
			mVHangUp.setBackgroundResource(R.drawable.call_interface_non_red_button);
			getCallHandler().postDelayed(finish, 3000);
			// 处理通话结束状态
			if (reason == Reason.DECLINED.getStatus() || reason == Reason.BUSY.getStatus()) {
				mCallStateTips.setText(getString(R.string.voip_calling_refuse));
				getCallHandler().removeCallbacks(finish);
			} else {
				if (reason == Reason.CALLMISSED.getStatus()) {
					mCallStateTips
							.setText(getString(R.string.voip_calling_timeout));
				} else if (reason == Reason.MAINACCOUNTPAYMENT.getStatus()) {
					mCallStateTips
							.setText(getString(R.string.voip_call_fail_no_cash));
				} else if (reason == Reason.UNKNOWN.getStatus()) {
					mCallStateTips
							.setText(getString(R.string.voip_calling_finish));
				} else if (reason == Reason.NOTRESPONSE.getStatus()) {
					mCallStateTips.setText(getString(R.string.voip_call_fail));
				} else if (reason == Reason.VERSIONNOTSUPPORT.getStatus()) {
					mCallStateTips
							.setText(getString(R.string.str_voip_not_support));
				} else if (reason == Reason.OTHERVERSIONNOTSUPPORT.getStatus()) {
					mCallStateTips
							.setText(getString(R.string.str_other_voip_not_support));
				} else {

					ThirdSquareError(reason);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Increased Voip P2P, direct error code:
	 * <p>
	 * Title: ThirdSquareError
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @param reason
	 */
	private void ThirdSquareError(int reason) {
		if (reason == Reason.AUTHADDRESSFAILED.getStatus()) {//  第三方鉴权地址连接失败
			mCallStateTips.setText(getString(R.string.voip_call_fail_connection_failed_auth));
		} else if (reason == Reason.MAINACCOUNTPAYMENT.getStatus()) {//  第三方主账号余额不足

			mCallStateTips.setText(getString(R.string.voip_call_fail_no_pay_account));
		} else if (reason == Reason.MAINACCOUNTINVALID.getStatus()) { //  第三方应用ID未找到

			mCallStateTips.setText(getString(R.string.voip_call_fail_not_find_appid));
		} else if (reason == Reason.CALLERSAMECALLED.getStatus()) {// 
														// 第三方应用未上线限制呼叫已配置测试号码

			mCallStateTips.setText(getString(R.string.voip_call_fail_not_online_only_call));
		} else if (reason == Reason.SUBACCOUNTPAYMENT.getStatus()) {//  第三方鉴权失败，子账号余额

			mCallStateTips.setText(getString(R.string.voip_call_auth_failed));
		} else {
			mCallStateTips.setText(getString(R.string.voip_calling_network_instability));
		}
	}

	/**
	 * 用于挂断时修改按钮属性及关闭操作
	 */
	private void finishCalling() {
		try {
			if (isConnect) {
				// set Chronometer view gone..
				isConnect = false;
				mChronometer.stop();
				mChronometer.setVisibility(View.GONE);
				// 接通后关闭
				mCallStateTips.setVisibility(View.VISIBLE);
				mCallStateTips.setText(R.string.voip_calling_finish);
				getCallHandler().postDelayed(finish, 3000);
			} else {
				// 未接通，直接关闭
				finish();
			}
			mCallHandFree.setClickable(false);
			mCallMute.setClickable(false);
			mVHangUp.setClickable(false);
			mDiaerpadBtn.setClickable(false);
			mDiaerpadBtn.setImageResource(R.drawable.call_interface_diaerpad);
			mCallHandFree
					.setImageResource(R.drawable.call_interface_hands_free);
			mCallMute.setImageResource(R.drawable.call_interface_mute);
			mVHangUp.setBackgroundResource(R.drawable.call_interface_non_red_button);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*********************************** KeyPad *************************************************/

	private EditText mDmfInput;

	private TextView mVtalkNumber;

	private TextView mVtalkName;

	void keyPressed(int keyCode) {
		
		KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
		mDmfInput.getText().clear();
		mDmfInput.onKeyDown(keyCode, event);
		ECDevice.getECVoipCallManager().sendDTMF(mCurrentCallId, mDmfInput.getText().toString().toCharArray()[0]);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		ECLog4Util.d(TAG, "[SelectVoiceActivity] onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode + ", data=" + data);

		// If there's no data (because the user didn't select a number and
		// just hit BACK, for example), there's nothing to do.
		if (requestCode != REQUEST_CODE_CALL_TRANSFER) {
			if (data == null) {
				return;
			}
		} else if (resultCode != RESULT_OK) {
			ECLog4Util.d(TAG, "[SelectVoiceActivity] onActivityResult: bail due to resultCode=" + resultCode);
			return;
		}

		String phoneStr = "";
		if (data.hasExtra("VOIP_CALL_NUMNBER")) {
			Bundle extras = data.getExtras();
			if (extras != null) {
				phoneStr = extras.getString("VOIP_CALL_NUMNBER");
			}
		}
		if (mCurrentCallId != null) {
			int setCallTransfer = setCallTransfer(mCurrentCallId, phoneStr);
			if(setCallTransfer!=0){
				Toast.makeText(getApplicationContext(), "呼转发起失败！", 1).show();
//				mVtalkNumber.setText(phoneStr);
			}
		} else {
			Toast.makeText(getApplicationContext(), "通话已经不存在", 1).show();
		}
	}
}

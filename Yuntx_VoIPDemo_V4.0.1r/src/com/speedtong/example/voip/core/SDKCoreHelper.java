/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.yuntongxun.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.speedtong.example.voip.core;

import java.io.InvalidClassException;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.speedtong.example.voip.common.utils.ECPreferenceSettings;
import com.speedtong.example.voip.common.utils.ECPreferences;
import com.speedtong.example.voip.common.utils.ToastUtil;
import com.speedtong.example.voip.db.ContactSqlManager;
import com.speedtong.example.voip.ui.CallInActivity;
import com.speedtong.example.voip.ui.ECLauncherUI;
import com.speedtong.example.voip.ui.ECLoginActivity;
import com.speedtong.example.voip.ui.manager.CCPAppManager;
import com.speedtong.sdk.ECDevice;
import com.speedtong.sdk.ECDevice.CallType;
import com.speedtong.sdk.ECDevice.InitListener;
import com.speedtong.sdk.ECDevice.Reason;
import com.speedtong.sdk.ECError;
import com.speedtong.sdk.ECInitialize;
import com.speedtong.sdk.ECChatManager;
import com.speedtong.sdk.ECGroupManager;
import com.speedtong.sdk.VoipCall;
import com.speedtong.sdk.core.model.VoipCallUserInfo;
import com.speedtong.sdk.core.voip.listener.OnVoipListener;
import com.speedtong.sdk.debug.ECLog4Util;
import com.speedtong.sdk.platformtools.SdkErrorCode;

/**
 * @author 容联•云通讯
 * @date 2014-12-8
 * @version 4.0
 */
public class SDKCoreHelper implements ECDevice.InitListener,
		ECDevice.OnECDeviceConnectListener, ECDevice.OnLogoutListener,
		OnVoipListener {

	public static final String ACTION_LOGOUT = "com.speedtong.example.ECDemo_logout";
	private static final String TAG = "SDKCoreHelper";
	private static SDKCoreHelper sInstance;
	private Context mContext;
	private ECInitialize params;
	private Connect mConnect = Connect.ERROR;

	public static final int WHAT_ON_CALL_ALERTING = 0x2003;
	public static final int WHAT_ON_CALL_ANSWERED = 0x2004;
	public static final int WHAT_ON_CALL_PAUSED = 0x2005;
	public static final int WHAT_ON_CALL_PAUSED_REMOTE = 0x2006;
	public static final int WHAT_ON_CALL_RELEASED = 0x2007;
	public static final int WHAT_ON_CALL_PROCEEDING = 0x2008;
	public static final int WHAT_ON_CALL_TRANSFERED = 0x2009;
	public static final int WHAT_ON_CALL_MAKECALL_FAILED = 0x2010;
	public static final int WHAT_ON_TEXT_MESSAGE_RECEIVED = 0x2011;
	public static final int WHAT_ON_TEXT_REPORT_RECEIVED = 0x2012;
	public static final int WHAT_ON_CALL_BACKING = 0x2013;
	public static final int WHAT_ON_CALL_VIDEO_RATIO_CHANGED = 0x2014;

	private SDKCoreHelper() {

	}

	public static Connect getConnectState() {
		return getInstance().mConnect;
	}

	public ECInitialize getParams() {
		return params;
	}

	public void setParams(ECInitialize params) {
		this.params = params;
	}

	public static SDKCoreHelper getInstance() {
		if (sInstance == null) {
			sInstance = new SDKCoreHelper();
		}
		return sInstance;
	}

	/**
	 * 初始化sdk后、成功会回调onInitialized() 失败回调onError(Exception exception)
	 * 
	 * @param ctx
	 */
	public static void init(Context ctx) {
		getInstance().mContext = ctx;
		if (!ECDevice.isInitialized()) {
			getInstance().mConnect = Connect.CONNECTING;
			ECDevice.initial(ctx, getInstance());// 正式开始初始化sdk
			postConnectNotify();
		}
	}

	@Override
	public void onInitialized() {

		if (params == null || params.getInitializeParams() == null
				|| params.getInitializeParams().isEmpty()) {
			ClientUser clientUser = CCPAppManager.getClientUser();
			params = new ECInitialize();
			params.setServerIP("sandboxapp.cloopen.com");
			params.setServerPort(8883);
			params.setSid(clientUser.getUserId()); // voip账号
			params.setSidToken(clientUser.getUserToken());// voip密码
			params.setSubId(clientUser.getSubSid());// 子账号
			params.setSubToken(clientUser.getSubToken());// 子账号密码
			params.setOnECDeviceConnectListener(this);
			ECLog4Util.i(TAG, "onInitialized");

			VoipCallUserInfo voipCallUserInfo = new VoipCallUserInfo();// voip通话透传信息
			voipCallUserInfo.setNickName(clientUser.getUserName());// 透传昵称
			voipCallUserInfo.setPhoneNum("");// 透传电话号码
			params.setVoipCallUserInfo(voipCallUserInfo);
		}
		Intent intent = new Intent(getInstance().mContext, CallInActivity.class);// 呼入界面activity、开发者需修改该类
		PendingIntent pendingIntent = PendingIntent.getActivity(
				getInstance().mContext, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT); // 设置来电界面

		params.setPendingIntent(pendingIntent);
		params.setOnECVoipListener(this);// 设置voip通话状态监听器
		ECDevice.login(params);
		
		
		
	}

	@Override
	public void onError(Exception exception) {
		ECLog4Util.e(TAG, "onerror");
		ECDevice.unInitial(); // 释放sdk
	}

	@Override
	public void onConnect() {
		// SDK与云通讯平台连接成功
		getInstance().mConnect = Connect.SUCCESS;
		Intent intent = new Intent();
		intent.setAction(mContext.getPackageName() + ".inited");
		mContext.sendBroadcast(intent);
		postConnectNotify();
		ToastUtil.showMessage("与云通讯连接成功");
	}

	@Override
	public void onDisconnect(ECError error) {
		// SDK与云通讯平台断开连接
		getInstance().mConnect = Connect.ERROR;
		postConnectNotify();


		if (error != null && error.errorCode.equals(String.valueOf(SdkErrorCode.SDK_KickedOff))) {
			Intent intent = new Intent();
			intent.setAction(mContext.getPackageName() + "kickedoff");
			mContext.sendBroadcast(intent);
			
			ToastUtil.showMessage("您账号已在另外一台设备登陆");

			try {
				ECPreferences.savePreference(
						ECPreferenceSettings.SETTINGS_REGIST_AUTO, "", true);
				logout();

				ECDevice.unInitial();

				CCPAppManager.clearActivity();
				mContext.startActivity(new Intent(mContext,
						ECLoginActivity.class));
			} catch (InvalidClassException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static void logout() {
		ECDevice.logout(getInstance());

		release();
	}

	

	/**
	 * 状态通知
	 */
	private static void postConnectNotify() {
		if (getInstance().mContext instanceof ECLauncherUI) {
			((ECLauncherUI) getInstance().mContext)
					.onNetWorkNotify(getConnectState());
		}
	}

	

	@Override
	public void onLogout() {
		if (params != null && params.getInitializeParams() != null) {
			params.getInitializeParams().clear();
		}
		params = null;

	}

	public enum Connect {
		ERROR, CONNECTING, SUCCESS
	}

	public static void release() {

		ContactSqlManager.reset();

	}

	@Override
	public void onSwitchCallMediaTypeRequest(String callid, CallType callType) {
		// TODO Auto-generated method stub
		ECDevice.getECVoipCallManager().responseSwitchCallMediaType(callid, 1);

	}

	@Override
	public void onSwitchCallMediaTypeResponse(String callid, CallType callType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCallVideoRatioChanged(String callid, int width, int height) {
		// TODO Auto-generated method stub
		ECLog4Util.d(TAG, "onCallVideoRatioChanged---" + "width=" + width
				+ "height=" + height);

		Bundle bundle = new Bundle();
		bundle.putInt("width", width);
		bundle.putInt("height", height);
		bundle.putString("callid", callid);
		sendTarget(WHAT_ON_CALL_VIDEO_RATIO_CHANGED, bundle);

	}

	@Override
	public void onCallMediaInitFailed(String callid, int reason) {//媒体初始化失败回调
		// TODO Auto-generated method stub

	}

	@Override
	public void onDtmfReceived(String callid, char dtmf) {//接收dtmf回调
		// TODO Auto-generated method stub

		ECLog4Util.d(TAG, "onDtmfReceived---" + dtmf);

	}

	@Override
	public void onFirewallPolicyEnabled() {//p2p回调
		// TODO Auto-generated method stub
		ECLog4Util.d(TAG, "onFirewallPolicyEnabled---");

	}

	@Override
	public void onCallEvents(VoipCall voipCall) {
		// TODO Auto-generated method stub

		Log.e(TAG, voipCall.toString());
		String callid = voipCall.getCallId();
		int reason = voipCall.getReason();

		switch (voipCall.getEcCallState()) {
		case ECallReleased:

			sendTarget(WHAT_ON_CALL_RELEASED, voipCall);

			break;
		case ECallProceeding:
			sendTarget(WHAT_ON_CALL_PROCEEDING, voipCall);

			break;
		case ECallAlerting:
			sendTarget(WHAT_ON_CALL_ALERTING, voipCall);

			break;
		case ECallAnswered:
			sendTarget(WHAT_ON_CALL_ANSWERED, voipCall);

			break;
		case ECallPaused:

			sendTarget(WHAT_ON_CALL_PAUSED, voipCall);
			break;
		case ECallPausedByRemote:
			sendTarget(WHAT_ON_CALL_PAUSED_REMOTE, voipCall);

			break;
		case ECallFailed:

			sendTarget(WHAT_ON_CALL_MAKECALL_FAILED, voipCall);

			break;

		default:
			break;
		}

	}

	long t = 0;
	private Handler handler;

	public synchronized void setHandler(final Handler handler) {
		this.handler = handler;
		ECLog4Util.w(TAG, "[setHandler] BaseActivity handler was set.");
	}

	private void sendTarget(int what, Object obj) {
		t = System.currentTimeMillis();
		while (handler == null && (System.currentTimeMillis() - t < 3500)) {

			try {
				Thread.sleep(80L);
			} catch (InterruptedException e) {
			}
		}

		if (handler == null) {
			ECLog4Util
					.w(TAG,
							"[RLVoiceHelper] handler is null, activity maybe destory, wait...");
			return;
		}

		Message msg = Message.obtain(handler);
		msg.what = what;
		msg.obj = obj;
		msg.sendToTarget();
	}

}

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
package com.speedtong.example.voip;

import org.json.JSONException;
import org.json.JSONObject;

import com.speedtong.example.voip.common.utils.DemoUtils;
import com.speedtong.example.voip.common.utils.LogUtil;
import com.speedtong.example.voip.common.utils.OrgJosnUtils;
import com.speedtong.example.voip.common.utils.ToastUtil;
import com.speedtong.example.voip.core.CCPConfig;
import com.speedtong.example.voip.ui.manager.CCPAppManager;
import com.speedtong.sdk.core.CCPParameters;
import com.speedtong.sdk.debug.ECLog4Util;
import com.speedtong.sdk.exception.CCPException;
import com.speedtong.sdk.net.AsyncECRequestRunner;
import com.speedtong.sdk.net.InnerRequestListener;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;



/**
 * @author 容联•云通讯
 * @date 2014-12-4
 * @version 4.0
 */
public class ECApplication extends Application {

	private static ECApplication instance;
	
	public final static String VALUE_DIAL_MODE_FREE = "voip_talk";
	public final static String VALUE_DIAL_MODE_BACK = "back_talk";
	public final static String VALUE_DIAL_MODE_DIRECT = "direct_talk";
	public final static String VALUE_DIAL_MODE = "mode";
	public final static String VALUE_DIAL_SOURCE_PHONE = "srcPhone";
	public final static String VALUE_DIAL_VOIP_INPUT = "VoIPInput";
	public final static String VALUE_DIAL_MODE_VEDIO = "vedio_talk";
	public final static String VALUE_DIAL_NAME = "voip_name";
	
	/**
	 * Activity Action: Start a VoIP incomeing call.
	 */
	public static final String ACTION_VOIP_INCALL = "com.voip.demo.ACTION_VOIP_INCALL";

	/**
	 * Activity Action: Start a VoIP outgoing call.
	 */
	public static final String ACTION_VOIP_OUTCALL = "com.voip.demo.ACTION_VOIP_OUTCALL";

	/**
	 * Activity Action: Start a Video incomeing call.
	 */
	public static final String ACTION_VIDEO_INTCALL = "com.voip.demo.ACTION_VIDEO_INTCALL";

	/**
	 * Activity Action: Start a Video outgoing call.
	 */
	public static final String ACTION_VIDEO_OUTCALL = "com.voip.demo.ACTION_VIDEO_OUTCALL";

	private static final String TAG = "ECApplication";

	/**
	 * 单例，返回一个实例
	 * 
	 * @return
	 */
	public static ECApplication getInstance() {
		if (instance == null) {
			LogUtil.w("[ECApplication] instance is null.");
		}
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		CCPAppManager.setContext(instance);
		
		ECLog4Util.i(TAG, "ECApplication onCreate");
		
		
		
	}
	
	public void setAudioMode(int mode) {
		AudioManager audioManager = (AudioManager) getApplicationContext()
				.getSystemService(Context.AUDIO_SERVICE);
		if (audioManager != null) {
			audioManager.setMode(mode);
		}
	}
	
	public void getDemoAccountInfos() {

		CCPParameters ccpParameters = new CCPParameters();
		ccpParameters.setParamerTagKey("Request");
		ccpParameters.add("secret_key", "a04daaca96294836bef207594a0a4df8");
		ccpParameters.add("user_name", DemoUtils.getLoginAccount());
		ccpParameters.add("user_pwd", DemoUtils.getLoginPwd());
		AsyncECRequestRunner
				.requestAsyncForEncrypt(
						"https://sandboxapp.cloopen.com:8883/2013-12-26/General/GetDemoAccounts",
						ccpParameters, "POST", true,
						new InnerRequestListener() {

							@Override
							public void onECRequestException(CCPException arg0) {
								ECLog4Util.e("TAG", "onECRequestException "
										+ arg0.getMessage());
								ToastUtil.showMessage(arg0.getMessage());

							}

							@Override
							public void onComplete(String arg0) {
//								ECLog4Util.e("TAG", "onComplete " + arg0);
								String json = OrgJosnUtils.xml2json(arg0);
								if (json != null) {
									try {
										JSONObject mJSONObject = (JSONObject) (new JSONObject(
												json)).get("Response");
										if ("000000".equals(mJSONObject
												.get("statusCode"))) {
											ECLog4Util.e(
													"TAG",
													"statusCode "
															+ mJSONObject
																	.get("statusCode"));
											JSONObject app = (JSONObject) mJSONObject
													.get("Application");

											String testNumbers = mJSONObject
													.getString("test_number");
											ECLog4Util.e("get test number",
													testNumbers);
											
											String registerPhone=mJSONObject.getString("mobile");
											String main_account=mJSONObject.getString("main_account");
											String main_token=mJSONObject.getString("main_token");
											CCPConfig.test_number=testNumbers;
											CCPConfig.mobile = registerPhone;
											CCPConfig.Main_Account=main_account;
											CCPConfig.Main_Token=main_token;

										} else {
											ECLog4Util.e(
													"TAG",
													"statusCode "
															+ mJSONObject
																	.get("statusCode"));
											String errorMsg = mJSONObject.get(
													"statusCode").toString();
											if (mJSONObject.has("statusMsg")) {
												errorMsg = mJSONObject.get(
														"statusMsg").toString();
											}
											ToastUtil.showMessage(errorMsg);
										}
									} catch (JSONException e) {
										e.printStackTrace();
										ToastUtil.showMessage(e.getMessage());
									}
								}

							}

						});

	}
	
	public static boolean isNetWorkConnect(Activity act) {

		ConnectivityManager manager = (ConnectivityManager) act
				.getApplicationContext().getSystemService(
						Context.CONNECTIVITY_SERVICE);

		if (manager == null) {
			return false;
		}

		NetworkInfo networkinfo = manager.getActiveNetworkInfo();

		if (networkinfo == null || !networkinfo.isAvailable()) {
			return false;
		}

		return true;
	}  

	/**
	 * 返回配置文件的日志开关
	 * 
	 * @return
	 */
	public boolean getLoggingSwitch() {
		try {
			ApplicationInfo appInfo = getPackageManager().getApplicationInfo(
					getPackageName(), PackageManager.GET_META_DATA);
			boolean b = appInfo.metaData.getBoolean("LOGGING");
			LogUtil.w("[ECApplication - getLogging] logging is: " + b);
			return b;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean getAlphaSwitch() {
		try {
			ApplicationInfo appInfo = getPackageManager().getApplicationInfo(
					getPackageName(), PackageManager.GET_META_DATA);
			boolean b = appInfo.metaData.getBoolean("ALPHA");
			LogUtil.w("[ECApplication - getAlpha] Alpha is: " + b);
			return b;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return false;
	}
}

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
package com.speedtong.example.voip.common.utils;

import com.speedtong.example.voip.ECApplication;

import android.content.Context;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.WindowManager;



/**
 * 屏幕密度转换工具类
 * @author 容联•云通讯
 * @date 2014-12-4
 * @version 4.0
 */
public class DensityUtil {

	// 根据屏幕密度转换
	private static float mPixels = 0.0F;
	private static float density = -1.0F;

	/**
	 * 
	 * @param context
	 * @param pixels 
	 * @return
	 */
	public static int getDisplayMetrics(Context context, float pixels) {
		if (mPixels == 0.0F)
			mPixels = context.getResources().getDisplayMetrics().density;
		return (int) (0.5F + pixels * mPixels);
	}
	
	
	public static int getImageWeidth(Context context , float pixels) {
		LogUtil.e("screen width " + context.getResources().getDisplayMetrics().widthPixels);
		return context.getResources().getDisplayMetrics().widthPixels - 66 - getDisplayMetrics(context, pixels);
	}

	/**
	 * 像素转化dip
	 * @param context
	 * @param pxValue
	 * @return
	 */
	public static int px2dip(Context context, float pxValue){

		final float scale = context.getResources().getDisplayMetrics().density;

		return (int)(pxValue / scale + 0.5f);

	}

	/**
	 * dip转化像素
	 * @param context
	 * @param dipValue
	 * @return
	 */
	public static int dip2px(float dipValue){
		final float scale = Resources.getSystem().getDisplayMetrics().density;
		return (int)(dipValue * scale + 0.5f);

	}
	
	/**
	 * @param context
	 * @param height
	 * @return
	 */
	public static int getMetricsDensity(Context context , float height) {
		DisplayMetrics localDisplayMetrics = new DisplayMetrics();
		((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay().getMetrics(localDisplayMetrics);
		return Math.round(height * localDisplayMetrics.densityDpi / 160.0F);
	}
	
	
	public static int fromDPToPix(Context context, int dp) {
		return Math.round(getDensity(context) * dp);
	}
	
	/**
	 * @param context
	 * @return
	 */
	public static float getDensity(Context context) {
		if (context == null)
			context = ECApplication.getInstance().getApplicationContext();
		if (density < 0.0F)
			density = context.getResources().getDisplayMetrics().density;
		return density;
	}
	
	public static int round(Context context, int paramInt) {
		return Math.round(paramInt / getDensity(context));
	}

}


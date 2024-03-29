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
package com.speedtong.example.voip.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 重载接口增加设置主界面是否可以进行滑动
 * @author 容联•云通讯
 * @date 2014-12-4
 * @version 4.0
 */
public class CCPCustomViewPager extends ViewPager {

	/**
	 * 控制页面是否可以左右滑动
	 */
	private boolean mSlidenabled = true;
	/**
	 * @param context
	 */
	public CCPCustomViewPager(Context context) {
		super(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public CCPCustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/**
	 * 设置是否可以滑动
	 */
	public final void setSlideEnabled(boolean enabled) {
		mSlidenabled = enabled;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		
		if(!mSlidenabled) {
			return false;
		}
		return super.onInterceptTouchEvent(arg0);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		
		if(!mSlidenabled) {
			return false;
		}
		return super.onTouchEvent(arg0);
	}
	
}

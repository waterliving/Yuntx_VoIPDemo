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

import com.speedtong.example.voip.common.utils.LogUtil;

import android.content.Context;

import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;



/**
 * @author 容联•云通讯
 * @date 2014-12-9
 * @version 4.0
 */
public class CCPTextView extends TextView {

	private GestureDetector mDetector;
	private CCPDoubleClickPreviewListener mPreviewListener;
	
	/**
	 * ignore Action Up
	 */
	private boolean mIgnoreNextActionUp;
	
	private final GestureDetector.SimpleOnGestureListener onSimpleOnGestureListener
		= new GestureDetector.SimpleOnGestureListener() {

			@Override
			public boolean onDoubleTap(MotionEvent e) {
				if(mPreviewListener == null) {
					return false;
				}
				return mPreviewListener.postPreviewView(CCPTextView.this);
			}
		
	};
	
	/**
	 * @param context
	 */
	public CCPTextView(Context context) {
		super(context);
		init();
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
	public CCPTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public CCPTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		mDetector = new GestureDetector(getContext() , onSimpleOnGestureListener);
	}
	
	/**
	 * 
	 * @param l
	 */
	public void setPreviewListener(CCPDoubleClickPreviewListener l) {
		mPreviewListener = l;
	}
	
	@Override
	public void cancelLongPress() {
		LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "cancelLongPress , should ignore Action Up Event next time");
		mIgnoreNextActionUp = true;
		super.cancelLongPress();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		int action = event.getAction();
		
		if(action == MotionEvent.ACTION_DOWN) {
			mIgnoreNextActionUp = false;
		}
		
		
		boolean result = false;
		if(mPreviewListener != null && mDetector != null) {
			 result = mDetector.onTouchEvent(event);
			 LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "dispatcher onTouchEvent result " + result);
		}
		
		
		if(action == MotionEvent.ACTION_UP && mIgnoreNextActionUp) {
			LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "ignore Action Up Event this time");
			if(!result) {
				return super.onTouchEvent(event);
			}
			return true;
		}
		return super.onTouchEvent(event);
	}
	
	/* (non-Javadoc)
	 * @see android.widget.TextView#performLongClick()
	 */
	@Override
	public boolean performLongClick() {
		LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "performLongClick , should ignore Action Up Event next time");
		mIgnoreNextActionUp = true;
		return super.performLongClick();
	}
	
	
	
	public interface CCPDoubleClickPreviewListener {

		public abstract boolean postPreviewView(View v);
	}
}

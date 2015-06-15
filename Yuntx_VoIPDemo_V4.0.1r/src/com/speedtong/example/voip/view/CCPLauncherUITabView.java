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

import com.speedtong.example.voip.R;
import com.speedtong.example.voip.common.utils.DensityUtil;
import com.speedtong.example.voip.common.utils.LogUtil;

import android.annotation.SuppressLint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;



/**
 * @author 容联•云通讯
 * @date 2014-12-4
 * @version 4.0
 */
public class CCPLauncherUITabView extends RelativeLayout implements View.OnClickListener {

	/**
	 * Show that the main tab at first.
	 */
	public static final int TAB_VIEW_MAIN = 0;
	
	/**
	 * Show that the second tab view
	 */
	public static final int TAB_VIEW_SECOND = 1;
	

	
	/**
	 * The holder for main TabView
	 */
	private TabViewHolder mMainTabView;
	
	/**
	 * The holder for Second TabView
	 */
	private TabViewHolder mSecondTabView;
	
	
	
	/**
	 * Follow the label moved slowly 
	 */
	private Bitmap mIndicatorBitmap;
	
	/**
	 * 
	 */
	private Matrix mMatrix = new Matrix();
	
	/**
	 * Slide unit 
	 */
	private int mTabViewBaseWidth;
	
	/**
	 * The current label location, is the need to move the index
	 */
	private int mCurrentSlideIndex;
	
	/**
	 * 
	 */
	private ImageView mSlideImage;

	
	private long mClickTime = 0L;
	
	/**
     * Listener used to dispatch click events.
	 */
	private OnUITabViewClickListener mListener;
	
	/**
	 * 
	 */
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			LogUtil.v(LogUtil.getLogUtilsTag(CCPLauncherUITabView.class), "onMainTabClick");
			if(mListener != null) {
				mListener.onTabClick(TAB_VIEW_MAIN);
			}
		}
		
	};
	
	/**
	 * @param context
	 */
	public CCPLauncherUITabView(Context context) {
		super(context);
		
		init();
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public CCPLauncherUITabView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init();
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public CCPLauncherUITabView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		
		init();
	}

	/**
	 * 
	 */
	private void init() {
		LinearLayout layout = new LinearLayout(getContext());
		layout.setId(2307141);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		
		addView(layout, new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT));
		
		ImageView imageView = new ImageView(getContext());
		imageView.setImageMatrix(mMatrix);
		imageView.setScaleType(ImageView.ScaleType.MATRIX);
		imageView.setId(2307142);
		RelativeLayout.LayoutParams imageViewLayoutParams = new RelativeLayout.LayoutParams(-1, DensityUtil.fromDPToPix(getContext(), 2));
		imageViewLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, 2307141);
	    addView(mSlideImage = imageView, imageViewLayoutParams);
		
		// TabView dial
		TabViewHolder tabViewMain = createTabView(TAB_VIEW_MAIN);
		tabViewMain.tabView.setText(R.string.net_voip);
		LinearLayout.LayoutParams enbGroupLayoutParams = new LinearLayout.LayoutParams(
				0, getResources().getDimensionPixelSize(R.dimen.DefaultTabbarHeight));
		enbGroupLayoutParams.weight = 1.0F;
		layout.addView(tabViewMain.tabView, enbGroupLayoutParams);
		mMainTabView = tabViewMain;
		
		// TabView communication
		TabViewHolder tabViewSecond = createTabView(TAB_VIEW_SECOND);
		tabViewSecond.tabView.setText(R.string.net_callback_Direct_dial);
		LinearLayout.LayoutParams secondLayoutParams = new LinearLayout.LayoutParams(
				0, getResources().getDimensionPixelSize(R.dimen.DefaultTabbarHeight));
		secondLayoutParams.weight = 1.0F;
		layout.addView(tabViewSecond.tabView, secondLayoutParams);
		mSecondTabView = tabViewSecond;
		
		
	}
	
	/**
     * Register a callback to be invoked when this UITabView is clicked. 
     * @param l The callback that will run
     */
	public void setOnUITabViewClickListener(OnUITabViewClickListener l) {
		mListener = l;
	}
	
	
	/**
     * Set a list of items to be displayed in the UITableView as the content, you will be notified of the
     * selected item via the supplied listener. This should be an array type i.e. R.array.foo
	 * @param itemsId
	 */
	public final void setTabViewItems(int itemsId) {
		
	}
	
	/**
	 * 
	 * @param index
	 */
	public final void setTabViewText(int index , int resid) {
		switch (index) {
		case TAB_VIEW_MAIN:
			
			mMainTabView.tabView.setText(resid);
			break;
		case TAB_VIEW_SECOND:
			mSecondTabView.tabView.setText(resid);
			break;
			
	
		default:
			break;
		}
	}

	
	public TabViewHolder createTabView(int index) {
		TabViewHolder tabViewHolder = new TabViewHolder();
		tabViewHolder.tabView = new CCPTabView(getContext(), index);
		tabViewHolder.tabView.setTag(Integer.valueOf(index));
		tabViewHolder.tabView.setOnClickListener(this);
		return tabViewHolder;
	}
	
	
	public final void resetTabViewDesc() {
		if(mMainTabView == null || mSecondTabView == null ) {
			return;
		}
		
		mMainTabView.tabView.notifyChange();
		mSecondTabView.tabView.notifyChange();
		
	}
	
	
	@Override
	public void onClick(View v) {
		int intValue = ((Integer)v.getTag()).intValue();
		if((mCurrentSlideIndex == intValue) && (intValue == TAB_VIEW_SECOND) && (System.currentTimeMillis() - mClickTime <= 300L)) {
			LogUtil.v(LogUtil.getLogUtilsTag(CCPLauncherUITabView.class), "onMainTabDoubleClick");
			mHandler.removeMessages(0);
			// Processing double click
			mClickTime = System.currentTimeMillis();
			mCurrentSlideIndex = intValue;
			return;
		}
		
		if(mListener != null) {
			if(intValue != TAB_VIEW_MAIN || mCurrentSlideIndex != TAB_VIEW_SECOND) {
				mClickTime = System.currentTimeMillis();
				mCurrentSlideIndex = intValue;
				mListener.onTabClick(intValue);
				return;
			}
			mHandler.sendEmptyMessageDelayed(0, 300L);
		}
		
		mClickTime = System.currentTimeMillis();
		mCurrentSlideIndex = intValue;
		LogUtil.v(LogUtil.getLogUtilsTag(CCPLauncherUITabView.class), "on UITabView click, index " + intValue+ ", but listener is " + mListener );
	}
	
	/**
	 * 
	 */
	public final void doSentEvents() {
		if(mSecondTabView == null || mMainTabView == null) {
			return ;
		}
		
		mMainTabView.tabView.notifyChange();
		mSecondTabView.tabView.notifyChange();
		
	}
	
	/**
	 * 移动
	 * @param start 开始位置
	 * @param distance 移动距离
	 */
	public final void doTranslateImageMatrix(int start, float distance) {
		mMatrix.setTranslate(mTabViewBaseWidth * (start + distance) , 0.0F);
		mSlideImage.setImageMatrix(mMatrix);
	}
	
	/**
	 * Set the TabView to operate
	 * @param visibility
	 */
	public final void setUnreadDotVisibility(boolean visibility) {

	}
	

	@SuppressLint("ResourceAsColor")
	public final void doChangeTabViewDisplay(int index) {
		mCurrentSlideIndex = index;
		switch (index) {
		case TAB_VIEW_MAIN:
			mMainTabView.tabView.setTextColor(getResources().getColorStateList(R.color.ccp_green));
			mSecondTabView.tabView.setTextColor(getResources().getColorStateList(R.color.launcher_tab_text_selector));
			
			break;
		case TAB_VIEW_SECOND:
			mMainTabView.tabView.setTextColor(getResources().getColorStateList(R.color.launcher_tab_text_selector));
			mSecondTabView.tabView.setTextColor(getResources().getColorStateList(R.color.ccp_green));
			
			break;
		
		default:
			break;
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		LogUtil.d(LogUtil.getLogUtilsTag(CCPLauncherUITabView.class), "on layout, width " + (r - l));
		int width = mTabViewBaseWidth = ((r - l) / 2);
		
		if(mIndicatorBitmap == null || mIndicatorBitmap.getWidth() != mTabViewBaseWidth) {
			
			int from = -1;
			if(mIndicatorBitmap != null) {
				from = mIndicatorBitmap.getWidth();
			}
			LogUtil.d(LogUtil.getLogUtilsTag(CCPLauncherUITabView.class),
					"sharp width changed, from " + from + " to " + width);
			
			mIndicatorBitmap = Bitmap.createBitmap(width,
					DensityUtil.fromDPToPix(getContext(), 4),
					Bitmap.Config.ARGB_8888);
			
			Canvas canvas = new Canvas(mIndicatorBitmap);
			canvas.drawColor(getResources().getColor(R.color.ccp_green));
			doTranslateImageMatrix(mCurrentSlideIndex, 0.0F);
			mSlideImage.setImageBitmap(mIndicatorBitmap);
			doChangeTabViewDisplay(mCurrentSlideIndex);
		}
		
	}
	
	/**
	 * @author Jorstin Chan
	 * @date 2014-4-26
	 * @version 1.0
	 */
	public class TabViewHolder {
		
		CCPTabView tabView;
	}
	
	/**
     * Interface definition for a callback to be invoked when a UITabView is clicked.
     */
	public abstract interface OnUITabViewClickListener {
		
		/**
		 * Called when a UITabView has been clicked.
		 * @param tabIndex index of UITabView
		 */
		public abstract void onTabClick(int tabIndex);
	}

}

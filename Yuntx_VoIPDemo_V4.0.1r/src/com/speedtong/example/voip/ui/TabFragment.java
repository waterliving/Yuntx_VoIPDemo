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
package com.speedtong.example.voip.ui;

import android.view.KeyEvent;

/**
 * 自定义三个TabView 的Fragment ,将三个TabView共同属性方法
 * 统一处理
 * 三个TabView 滑动页面需要继承该基类，
 * @author 容联•云通讯
 * @date 2014-12-4
 * @version 4.0
 */
public abstract class TabFragment extends BaseFragment {

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
			return false;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	
	@Override
	public void onDestroy() {
		onReleaseTabUI();
		super.onDestroy();
	}

	/**
	 * 当前TabFragment被点击
	 */
	protected abstract void onTabFragmentClick();
	
	/**
	 * 当前TabFragment被释放
	 */
	protected abstract void onReleaseTabUI();

}

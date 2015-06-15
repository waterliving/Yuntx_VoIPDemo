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

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import com.speedtong.example.voip.ECApplication;
import com.speedtong.example.voip.R;
import com.speedtong.example.voip.common.utils.ECPreferenceSettings;
import com.speedtong.example.voip.common.utils.ECPreferences;
import com.speedtong.example.voip.common.utils.LogUtil;
import com.speedtong.example.voip.common.utils.ToastUtil;
import com.speedtong.example.voip.contacts.ECContacts;
import com.speedtong.example.voip.core.CCPConfig;
import com.speedtong.example.voip.core.ClientUser;
import com.speedtong.example.voip.core.SDKCoreHelper;
import com.speedtong.example.voip.core.SDKCoreHelper.Connect;
import com.speedtong.example.voip.db.ContactSqlManager;
import com.speedtong.example.voip.ui.manager.CCPAppManager;
import com.speedtong.example.voip.view.CCPCustomViewPager;
import com.speedtong.example.voip.view.CCPLauncherUITabView;
import com.speedtong.example.voip.view.OverflowAdapter.OverflowItem;
import com.speedtong.example.voip.view.OverflowHelper;
import com.speedtong.sdk.ECDevice;
import com.speedtong.sdk.ECDevice.CallType;
import com.umeng.analytics.MobclickAgent;

/**
 * 应用主界面（初始化二个Tab功能界面）
 * 
 * @author 容联•云通讯
 * @date 2014-12-4
 * @version 4.0
 */
public class ECLauncherUI extends FragmentActivity implements View.OnClickListener {
	/**
	 * 当前ECLauncherUI 实例
	 */
	public static ECLauncherUI mLauncherUI;
	/**
	 * 当前ECLauncherUI实例产生个数
	 */
	public static int mLauncherInstanceCount = 0;
	/**
	 * 当前主界面RootView
	 */
	public View mLauncherView;
	/**
	 * LauncherUI 主界面导航控制View ,包含二个View Tab按钮
	 */
	private CCPLauncherUITabView mLauncherUITabView;
	/**
	 * 二个TabView所对应的二个页面的适配器
	 */
	private CCPCustomViewPager mCustomViewPager;
	/**
	 * 沟通、联系人、群组适配器
	 */
	public LauncherViewPagerAdapter mLauncherViewPagerAdapter;
	private OverflowHelper mOverflowHelper;
	/**
	 * 当前显示的TabView Fragment
	 */
	private int mCurrentItemPosition = -1;
	/**
	 * 会话界面(沟通)
	 */
	private static final int TAB_NET_VOIP = 0;
	/**
	 * 通讯录界面(联系人)
	 */
	private static final int TAB_NET_PHONE = 1;
	/**
	 * {@link CCPLauncherUITabView} 是否已经被初始化
	 */
	private boolean mTabViewInit = false;
	/**
	 * 缓存三个TabView
	 */
	private final HashMap<Integer, Fragment> mTabViewCache = new HashMap<Integer, Fragment>();
	private OverflowItem[] mItems = new OverflowItem[4];
	private KickOffReceiver kickoffReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (mLauncherUI != null) {
			LogUtil.e(LogUtil.getLogUtilsTag(ECLauncherUI.class), "finish last LauncherUI");
			mLauncherUI.finish();
		}
		mLauncherUI = this;
		mLauncherInstanceCount++;
		super.onCreate(savedInstanceState);
		initWelcome();
		mOverflowHelper = new OverflowHelper(this);
		// umeng
		MobclickAgent.updateOnlineConfig(this);
		MobclickAgent.setDebugMode(true);
		// 设置页面默认为竖屏
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		registerReceiver(new String[] { getPackageName() + "kickedoff" });
	}

	private class KickOffReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null && intent.getAction().equalsIgnoreCase(getPackageName() + "kickedoff")) {
				finish();
			}
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(kickoffReceiver);
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
	private boolean mInit = false;

	private void initWelcome() {
		if (!mInit) {
			mInit = true;
			setContentView(R.layout.splash_activity);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mInit = false;
					initLauncherUIView();
				}
			}, 3000);
		}
	}

	/**
	 * 初始化主界面UI视图
	 */
	private void initLauncherUIView() {
		mLauncherView = getLayoutInflater().inflate(R.layout.main_tab, null);
		setContentView(mLauncherView);
		mTabViewInit = true;
		initOverflowItems();
		mCustomViewPager = (CCPCustomViewPager) findViewById(R.id.pager);
		mCustomViewPager.setOffscreenPageLimit(3);
		if (mLauncherUITabView != null) {
			mLauncherUITabView.setOnUITabViewClickListener(null);
			// mLauncherUITabView.setVisibility(View.VISIBLE);
		}
		mLauncherUITabView = (CCPLauncherUITabView) findViewById(R.id.laucher_tab_top);
		mCustomViewPager.setSlideEnabled(true);
		mLauncherViewPagerAdapter = new LauncherViewPagerAdapter(this, mCustomViewPager);
		mLauncherUITabView.setOnUITabViewClickListener(mLauncherViewPagerAdapter);
		findViewById(R.id.btn_plus).setOnClickListener(this);
		ctrlViewTab(0);
	}

	/**
	 * 根据TabFragment Index 查找Fragment
	 * 
	 * @param tabIndex
	 * @return
	 */
	public final BaseFragment getTabView(int tabIndex) {
		LogUtil.d(LogUtil.getLogUtilsTag(ECLauncherUI.class), "get tab index " + tabIndex);
		if (tabIndex < 0) {
			return null;
		}
		if (mTabViewCache.containsKey(Integer.valueOf(tabIndex))) {
			return (BaseFragment) mTabViewCache.get(Integer.valueOf(tabIndex));
		}
		BaseFragment mFragment = null;
		switch (tabIndex) {
		case TAB_NET_VOIP:
			mFragment = (TabFragment) Fragment.instantiate(this, NetVoipFragment.class.getName(), null);
			break;
		case TAB_NET_PHONE:
			mFragment = (TabFragment) Fragment.instantiate(this, NetPhoneFragment.class.getName(), null);
			break;
		default:
			break;
		}
		if (mFragment != null) {
			mFragment.setActionBarActivity(this);
		}
		mTabViewCache.put(Integer.valueOf(tabIndex), mFragment);
		return mFragment;
	}

	/**
	 * 根据提供的子Fragment index 切换到对应的页面
	 * 
	 * @param index
	 *            子Fragment对应的index
	 */
	public void ctrlViewTab(int index) {
		LogUtil.d(LogUtil.getLogUtilsTag(ECLauncherUI.class), "change tab to " + index + ", cur tab " + mCurrentItemPosition + ", has init tab " + mTabViewInit + ", tab cache size " + mTabViewCache.size());
		if ((!mTabViewInit || index < 0) || (mLauncherViewPagerAdapter != null && index > mLauncherViewPagerAdapter.getCount() - 1)) {
			return;
		}
		if (mCurrentItemPosition == index) {
			return;
		}
		mCurrentItemPosition = index;
		if (mLauncherUITabView != null) {
			mLauncherUITabView.doChangeTabViewDisplay(mCurrentItemPosition);
		}
		if (mCustomViewPager != null) {
			mCustomViewPager.setCurrentItem(mCurrentItemPosition, false);
		}
	}

	void initOverflowItems() {
		if (mItems == null) {
			mItems = new OverflowItem[4];
		}
		String name = null;
		try {
			ClientUser clientUser = CCPAppManager.getClientUser();
			name = ContactSqlManager.getContact(clientUser.getUserId()).getNickname();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mItems[0] = new OverflowItem("当前用户:" + name);
		mItems[1] = new OverflowItem(getString(R.string.main_binding_phone));
		mItems[2] = new OverflowItem(getString(R.string.main_logout));
		mItems[3] = new OverflowItem(getString(R.string.main_quit_app));
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		controlPlusSubMenu();
		return false;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		LogUtil.d(LogUtil.getLogUtilsTag(ECLauncherUI.class), " onKeyDown");
		if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK) && event.getAction() == KeyEvent.ACTION_UP) {
			// dismiss PlusSubMenuHelper
			if (mOverflowHelper != null && mOverflowHelper.isOverflowShowing()) {
				mOverflowHelper.dismiss();
				return true;
			}
		}
		// 这里可以进行设置全局性的menu菜单的判断
		if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK) && event.getAction() == KeyEvent.ACTION_DOWN) {
			doTaskToBackEvent();
		}
		try {
			return super.dispatchKeyEvent(event);
		} catch (Exception e) {
			LogUtil.e(LogUtil.getLogUtilsTag(ECLauncherUI.class), "dispatch key event catch exception " + e.getMessage());
		}
		return false;
	}

	@Override
	protected void onResume() {
		LogUtil.e(LogUtil.getLogUtilsTag(ECLauncherUI.class), "onResume start");
		super.onResume();
		// 统计时长
		MobclickAgent.onResume(this);
		if (mLauncherUITabView == null) {
			String account = getAutoRegistAccount();
			if (TextUtils.isEmpty(account)) {
				startActivity(new Intent(this, ECLoginActivity.class));
				finish();
				return;
			}
			String[] split = account.split(",");
			ClientUser user = new ClientUser(split[2]);
			user.setSubSid(split[0]);
			user.setSubToken(split[1]);
			user.setUserToken(split[3]);
			user.setUserName(split[4]);
			CCPAppManager.setClientUser(user);
			SDKCoreHelper.init(this);
			// 初始化主界面Tab资源
			if (!mInit) {
				initLauncherUIView();
			}
		}
		if (ECApplication.isNetWorkConnect(this)) {
			ECApplication.getInstance().getDemoAccountInfos();
		}
	}

	/**
	 * 检查是否需要自动登录
	 * 
	 * @return
	 */
	private String getAutoRegistAccount() {
		SharedPreferences sharedPreferences = ECPreferences.getSharedPreferences();
		ECPreferenceSettings registAuto = ECPreferenceSettings.SETTINGS_REGIST_AUTO;
		String registAccount = sharedPreferences.getString(registAuto.getId(), (String) registAuto.getDefaultValue());
		return registAccount;
	}

	private void controlPlusSubMenu() {
		if (mOverflowHelper == null) {
			return;
		}
		if (mOverflowHelper.isOverflowShowing()) {
			mOverflowHelper.dismiss();
			return;
		}
		mOverflowHelper.setOverflowItems(mItems);
		mOverflowHelper.setOnOverflowItemClickListener(mOverflowItemCliclListener);
		mOverflowHelper.showAsDropDown(findViewById(R.id.btn_plus));
	}

	@Override
	protected void onPause() {
		LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "KEVIN Launcher onPause");
		super.onPause();
		// 友盟统计API
		MobclickAgent.onPause(this);
	}

	/**
	 * 返回隐藏到后台
	 */
	public void doTaskToBackEvent() {
		moveTaskToBack(true);
	}

	/**
	 * TabView 页面适配器
	 * 
	 * @author 容联•云通讯
	 * @date 2014-12-4
	 * @version 4.0
	 */
	private class LauncherViewPagerAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener, CCPLauncherUITabView.OnUITabViewClickListener {
		/**
		 * 
		 */
		private int mClickTabCounts;
		private NetPhoneFragment netPhoneFragment;
		private NetVoipFragment netVoipFragment;
		private final ViewPager mViewPager;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

		final class TabInfo {
			private final String tag;
			private final Class<?> clss;
			private final Bundle args;

			TabInfo(String _tag, Class<?> _class, Bundle _args) {
				tag = _tag;
				clss = _class;
				args = _args;
			}
		}

		public LauncherViewPagerAdapter(FragmentActivity fm, ViewPager pager) {
			super(fm.getSupportFragmentManager());
			mViewPager = pager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(String tabSpec, Class<?> clss, Bundle args) {
			String tag = tabSpec;
			TabInfo info = new TabInfo(tag, clss, args);
			mTabs.add(info);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public Fragment getItem(int position) {
			return mLauncherUI.getTabView(position);
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			LogUtil.d(LogUtil.getLogUtilsTag(LauncherViewPagerAdapter.class), "onPageScrollStateChanged state = " + state);
			if (state != ViewPager.SCROLL_STATE_IDLE || netPhoneFragment == null) {
				return;
			}
			netPhoneFragment = null;
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			LogUtil.d(LogUtil.getLogUtilsTag(LauncherViewPagerAdapter.class), "onPageScrolled " + position + " " + positionOffset + " " + positionOffsetPixels);
			if (mLauncherUITabView != null) {
				mLauncherUITabView.doTranslateImageMatrix(position, positionOffset);
			}
			if (positionOffset != 0.0F) {
				if (netPhoneFragment == null) {
					netPhoneFragment = (NetPhoneFragment) getTabView(CCPLauncherUITabView.TAB_VIEW_SECOND);
				}
				return;
			}
		}

		@Override
		public void onPageSelected(int position) {
			LogUtil.d(LogUtil.getLogUtilsTag(LauncherViewPagerAdapter.class), "onPageSelected");
			if (mLauncherUITabView != null) {
				mLauncherUITabView.doChangeTabViewDisplay(position);
				mCurrentItemPosition = position;
			}
		}

		@Override
		public void onTabClick(int tabIndex) {
			if (tabIndex == mCurrentItemPosition) {
				LogUtil.d(LogUtil.getLogUtilsTag(LauncherViewPagerAdapter.class), "on click same index " + tabIndex);
				// Perform a rolling
				TabFragment item = (TabFragment) getItem(tabIndex);
				item.onTabFragmentClick();
				return;
			}
			mClickTabCounts += mClickTabCounts;
			LogUtil.d(LogUtil.getLogUtilsTag(LauncherViewPagerAdapter.class), "onUITabView Click count " + mClickTabCounts);
			mViewPager.setCurrentItem(tabIndex);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_plus) {
			controlPlusSubMenu();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Intent actionIntent = intent;
		String userName = actionIntent.getStringExtra("Main_FromUserName");
		ECContacts contacts = ContactSqlManager.getContactLikeUsername(userName);
	}
	private final AdapterView.OnItemClickListener mOverflowItemCliclListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			controlPlusSubMenu();
			if (position == 0) {
			} else if (position == 1) {
				String[] splitTestNumbers;
				if (!TextUtils.isEmpty(CCPConfig.test_number)) {
					splitTestNumbers = CCPConfig.test_number.split(",");
					ArrayList<String> arrays = new ArrayList<String>();
					for (String str : splitTestNumbers) {
						arrays.add(str);
					}
					if (arrays != null && arrays.size() >= 3) {
						ToastUtil.showMessage("当前最多绑定3个测试号码");
						return;
					}
				}
				startActivity(new Intent(ECLauncherUI.this, ECBindTestPhoneUI.class));
			} else if (position == 2) {
				try {
					ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_REGIST_AUTO, "", true);
					SDKCoreHelper.logout();
					ECDevice.unInitial();
					finish();
					startActivity(new Intent(ECLauncherUI.this, ECLoginActivity.class));
				} catch (InvalidClassException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (position == 3) {
				try {
					// ECPreferences.savePreference(
					// ECPreferenceSettings.SETTINGS_FULLY_EXIT, true,
					// true);
					// ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_FULLY_EXIT,
					// false, true);
					SDKCoreHelper.logout();
					ECDevice.unInitial();
					finish();
					android.os.Process.killProcess(android.os.Process.myPid());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	public void onNetWorkNotify(Connect connectState) {
		BaseFragment tabView = getTabView(TAB_NET_VOIP);
		if (tabView instanceof NetVoipFragment && tabView.isAdded()) {
			((NetVoipFragment) tabView).updateConnectState();
		}
	}
}

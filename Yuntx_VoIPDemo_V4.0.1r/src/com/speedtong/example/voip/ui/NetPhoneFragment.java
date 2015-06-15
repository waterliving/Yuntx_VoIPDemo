package com.speedtong.example.voip.ui;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.speedtong.example.voip.ECApplication;
import com.speedtong.example.voip.R;
import com.speedtong.example.voip.common.utils.ToastUtil;
import com.speedtong.example.voip.core.CCPConfig;
import com.speedtong.sdk.ECDevice;
import com.speedtong.sdk.ECDevice.CallType;
import com.speedtong.sdk.ECError;
import com.speedtong.sdk.core.model.CallBackEntity;
import com.speedtong.sdk.core.voip.listener.OnVoipMakeCallBackListener;
import com.speedtong.sdk.debug.ECLog4Util;

public class NetPhoneFragment extends TabFragment {

	private ListView listView;
	private Context context;
	private String[] splitTestNumbers;

	private PhoneApapter phoneApapter;

	@Override
	protected void onTabFragmentClick() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onReleaseTabUI() {
		// TODO Auto-generated method stub

	}

	@Override
	protected int getLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.net_phone_activity;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		this.context = getActivity();

		listView = (ListView) findViewById(R.id.account_list_phone);
		if (listView != null) {
			listView.setAdapter(null);
		}

		LinearLayout layout = (LinearLayout) findViewById(R.id.ll_add_phone);

		layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (splitTestNumbers != null && splitTestNumbers.length < 3) {
					startActivity(new Intent(context, ECBindTestPhoneUI.class));
				} else {
					ToastUtil.showMessage(R.string.binding_phone_max);
				}

			}
		});

		if (!TextUtils.isEmpty(CCPConfig.test_number)) {
			splitTestNumbers = CCPConfig.test_number.split(",");

			ArrayList<String> arrays = new ArrayList<String>();
			for (String str : splitTestNumbers) {
				if (CCPConfig.mobile != null && CCPConfig.mobile.equals(str)) {
					// continue;
				}
				arrays.add(str);
			}
			splitTestNumbers = arrays.toArray(new String[] {});

		}

		if (splitTestNumbers != null && splitTestNumbers.length > 0) {

			phoneApapter = new PhoneApapter();
			listView.setAdapter(phoneApapter);
			phoneApapter.notifyDataSetChanged();
		}

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();

		

	}

	private class PhoneApapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return splitTestNumbers.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return splitTestNumbers[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			View v = View.inflate(context,
					R.layout.net_phone_subaccount_list_item, null);

			TextView textView = (TextView) v.findViewById(R.id.phone_number);
			Button buDirct = (Button) v.findViewById(R.id.net_phone_dirct);
			Button buCallBack = (Button) v
					.findViewById(R.id.net_phone_callback);

			textView.setText(splitTestNumbers[position]);

			buDirct.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					Intent intent = new Intent(context, CallOutActivity.class);

					intent.putExtra(ECApplication.VALUE_DIAL_MODE,
							ECApplication.VALUE_DIAL_MODE_DIRECT);
					intent.putExtra(ECApplication.VALUE_DIAL_VOIP_INPUT,
							splitTestNumbers[position]);

					startActivity(intent);

				}
			});

			buCallBack.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					if (CCPConfig.mobile
							.equalsIgnoreCase(splitTestNumbers[position])) {

						ToastUtil.showMessage("注册号码不能同时进行回拨测试");
						return;
					}

					CallBackEntity callBackEntity = new CallBackEntity();

				 
					callBackEntity.setSrc(CCPConfig.mobile); //主叫号码        :回拨显示请联系云支持
					callBackEntity.setDest(splitTestNumbers[position]);//被叫
					callBackEntity.setDestSerNum(CCPConfig.mobile);//被叫显示号码
					callBackEntity.setSrcSerNum(splitTestNumbers[position]);//主叫显示号码  
					ECDevice.getECVoipCallManager().makeCallBack(
							callBackEntity, new CallBackListener()); //回拨回调

				}
			});

			return v;
		}

	}

	private class CallBackListener implements OnVoipMakeCallBackListener {

		@Override
		public void onMakeCallback(ECError ecError, String src, String dest) {
			// TODO Auto-generated method stub

			ECLog4Util.e("callback", ecError.toString());
			if (ecError != null && ecError.errorCode.equalsIgnoreCase("000000")) {
				ToastUtil.showMessage("回拨成功");
			} else {
				ToastUtil.showMessage("回拨失败");
			}

		}

	}

}

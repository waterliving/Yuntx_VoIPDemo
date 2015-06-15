package com.speedtong.example.voip.ui;

import java.util.ArrayList;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.speedtong.example.voip.ECApplication;
import com.speedtong.example.voip.R;
import com.speedtong.example.voip.contacts.ContactLogic;
import com.speedtong.example.voip.contacts.ECContacts;
import com.speedtong.example.voip.core.SDKCoreHelper;
import com.speedtong.example.voip.core.SDKCoreHelper.Connect;
import com.speedtong.example.voip.db.ContactSqlManager;
import com.speedtong.example.voip.ui.ECAccountChooseActivity.SubaccountAdapter.SubaccountHolder;
import com.speedtong.example.voip.view.NetWarnBannerView;
import com.speedtong.sdk.debug.ECLog4Util;

public class NetVoipFragment extends TabFragment {

	private ListView mListView;
	private Context context;
	private ArrayList<ECContacts> contacts;
	public static final String TAG="NetVoipFragment";
	private NetWarnBannerView mBannerView;

	@Override
	protected void onTabFragmentClick() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onReleaseTabUI() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		updateConnectState();
		ECLog4Util.i(TAG, "onresume");
	}
	
	public void  updateConnectState() {
		Connect connect = SDKCoreHelper.getConnectState();
		if(connect == Connect.CONNECTING) {
			mBannerView.setNetWarnText(getString(R.string.connecting_server));
			mBannerView.reconnect(true);
		} else if (connect == Connect.ERROR) {
			mBannerView.setNetWarnText(getString(R.string.connect_server_error));
		} else if (connect == Connect.SUCCESS) {
			mBannerView.hideWarnBannerView();
		}
	}

	@Override
	protected int getLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.net_voip_activity;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		if (mListView != null) {
			mListView.setAdapter(null);
			if(mBannerView != null) {
				mListView.removeHeaderView(mBannerView);
			}
		}
		mListView = (ListView) findViewById(R.id.account_list);
		context = getActivity();
		
		mBannerView = new NetWarnBannerView(getActivity());
		mListView.addHeaderView(mBannerView);
		
		contacts = ContactSqlManager.getContacts();
		if (contacts != null && contacts.size() > 0) {

			mListView.setAdapter(new AccountAdapter());
		}

	}

	private class AccountAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return contacts.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return contacts.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub

			final ECContacts account = contacts.get(position);

			SubaccountHolder holder;
			if (convertView == null || convertView.getTag() == null) {
				convertView = View.inflate(context,
						R.layout.net_voip_subaccount_list_item, null);
				holder = new SubaccountHolder();

				holder.avatar = (ImageView) convertView
						.findViewById(R.id.avatar);
				holder.tv_voip = (TextView) convertView
						.findViewById(R.id.tv_voip);
				holder.name = (TextView) convertView
						.findViewById(R.id.voip_name);

				holder.buVoice = (Button) convertView
						.findViewById(R.id.net_voip_voice);
				holder.buVideo = (Button) convertView
						.findViewById(R.id.net_voip_video);

				holder.buVoice.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						Intent intent = new Intent(context,
								CallOutActivity.class);

						intent.putExtra(ECApplication.VALUE_DIAL_MODE,
								ECApplication.VALUE_DIAL_MODE_FREE);
						intent.putExtra(ECApplication.VALUE_DIAL_VOIP_INPUT,
								account.getContactid());
						intent.putExtra(ECApplication.VALUE_DIAL_NAME, account.getNickname());
						startActivity(intent);

					}
				});

				holder.buVideo.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						Intent intent = new Intent(context,
								VideoActivity.class);

						intent.putExtra(ECApplication.VALUE_DIAL_MODE,
								ECApplication.VALUE_DIAL_MODE_FREE);
						intent.putExtra(ECApplication.VALUE_DIAL_VOIP_INPUT,
								account.getContactid());
						startActivity(intent);	
						
						
					}
				});

			} else {
				holder = (SubaccountHolder) convertView.getTag();
			}

			try {

				if (account != null) {

					if (!TextUtils.isEmpty(account.getContactid())) {
						holder.avatar.setImageBitmap(ContactLogic
								.getPhoto(account.getRemark()));
						holder.name.setText(account.getNickname());
						holder.tv_voip.setText(account.getContactid());

					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return convertView;

		}

	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		if (contacts != null) {
			contacts.clear();
			contacts = null;
		}
	}

	class SubaccountHolder {
		ImageView avatar;
		TextView name;
		TextView tv_voip;
		Button buVoice;
		Button buVideo;
	}

}

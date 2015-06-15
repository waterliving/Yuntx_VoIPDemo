package com.speedtong.example.voip.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.speedtong.example.voip.R;
import com.speedtong.example.voip.contacts.ContactLogic;
import com.speedtong.example.voip.contacts.ECContacts;
import com.speedtong.example.voip.core.ClientUser;
import com.speedtong.example.voip.core.SDKCoreHelper;
import com.speedtong.example.voip.db.ContactSqlManager;
import com.speedtong.example.voip.ui.ECAccountChooseActivity.SubaccountAdapter;
import com.speedtong.example.voip.ui.ECAccountChooseActivity.SubaccountAdapter.SubaccountHolder;
import com.speedtong.example.voip.ui.manager.CCPAppManager;
import com.speedtong.example.voip.view.ECProgressDialog;

public class ECGetNumberToTransferActivity extends ECSuperActivity implements OnClickListener, OnItemClickListener {
	
	private Button mConfrim;

	private ListView mSubaccountListView;
	private ECContacts seletedAccountBean;
	private ArrayList<ECContacts> aList;
	private ArrayList<ECContacts> contacts;

	@Override
	protected int getLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.getnumber_to_transfer_account_choose;
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		initLayoutResource();
		getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt, -1,
				R.string.main_title_select_account_for_transfer, this);

		aList = ContactSqlManager.getContacts();
		initListView(aList);
	}


	private void initListView(ArrayList<ECContacts> aList2) {
		// TODO Auto-generated method stub
		contacts = ContactLogic.converContacts(aList2);
		SubaccountAdapter adapter = new SubaccountAdapter(this, contacts);
		mSubaccountListView.setAdapter(adapter);
		
	}


	private void initLayoutResource() {
		// TODO Auto-generated method stub
		mConfrim = (Button) findViewById(R.id.account_confrim);
		mConfrim.setOnClickListener(this);

		mSubaccountListView = (ListView) findViewById(R.id.account_list);
		mSubaccountListView.setOnItemClickListener(this);
		
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		
		if (parent.getAdapter() instanceof SubaccountAdapter) {
			SubaccountAdapter adapter = (SubaccountAdapter) parent.getAdapter();

			CheckBox cBox = (CheckBox) view.findViewById(R.id.check_box);
			if (!cBox.isChecked()) {
				adapter.init(adapter.getCount());
			}
			cBox.toggle();

			boolean isChoice = cBox.isChecked();
			adapter.getIsSelected().put(position, isChoice);
			initImageButtonUI(isChoice);
			adapter.notifyDataSetChanged();

			try {
				seletedAccountBean = aList.get(position);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.account_confrim:

			if (seletedAccountBean != null) {

				Intent intent = new Intent();
				intent.setClass(ECGetNumberToTransferActivity.this,
						CallOutActivity.class);
				intent.putExtra("VOIP_CALL_NUMNBER",
						seletedAccountBean.getContactid());
				setResult(RESULT_OK, intent);
				finish();

			}
			break;
		case R.id.btn_left:
			hideSoftKeyboard();
			finish();
			break;
		default:
			break;
		}

	}
	
	class SubaccountAdapter extends ArrayAdapter<ECContacts> {

		LayoutInflater mInflater;

		HashMap<Integer, Boolean> isSelected;

		public SubaccountAdapter(Context context, List<ECContacts> objects) {
			super(context, 0, objects);

			
			mInflater = getLayoutInflater();

			init(objects.size());
		}

		// initialize all checkbox are not selected
		public void init(int size) {
			if (isSelected != null) {
				isSelected.clear();
			} else {
				isSelected = new HashMap<Integer, Boolean>();
			}
			for (int i = 0; i < size; i++) {
				isSelected.put(i, false);
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			SubaccountHolder holder;
			if (convertView == null || convertView.getTag() == null) {
				convertView = mInflater.inflate(R.layout.subaccount_list_item,
						null);
				holder = new SubaccountHolder();

				holder.avatar = (ImageView) convertView
						.findViewById(R.id.avatar);
				holder.tv_voip = (TextView) convertView
						.findViewById(R.id.tv_voip);
				holder.name = (TextView) convertView
						.findViewById(R.id.voip_name);
				holder.checkBox = (CheckBox) convertView
						.findViewById(R.id.check_box);
			} else {
				holder = (SubaccountHolder) convertView.getTag();
			}

			try {
				ECContacts account = getItem(position);
				if (account != null) {

					if (!TextUtils.isEmpty(account.getContactid())) {

						holder.tv_voip.setText(account.getContactid());
						holder.checkBox.setChecked(isSelected.get(position));
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return convertView;
		}

		class SubaccountHolder {
			ImageView avatar;
			TextView name;
			TextView tv_voip;
			CheckBox checkBox;
		}

		public HashMap<Integer, Boolean> getIsSelected() {
			return isSelected;
		}

	}
	
	private void initImageButtonUI(boolean isChoice) {
		if (isChoice) {
			mConfrim.setBackgroundResource(R.drawable.login_button_selector);
			mConfrim.setText(R.string.str_start_transfer);
			ColorStateList colors = (ColorStateList) getResources()
					.getColorStateList(android.R.color.white);
			mConfrim.setTextColor(colors);
		} else {
			mConfrim.setText(R.string.str_no_choice_account);
			ColorStateList colors = (ColorStateList) getResources()
					.getColorStateList(R.color.ccp_attentoin_color);
			mConfrim.setTextColor(colors);
			mConfrim.setBackgroundResource(R.drawable.select_account_bottom);
		}

		mConfrim.setEnabled(isChoice);
	}
	

}

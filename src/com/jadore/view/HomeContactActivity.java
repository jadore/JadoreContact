package com.jadore.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jadore.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.jadore.bean.ContactBean;
import com.jadore.view.adapter.ContactHomeAdapter;
import com.jadore.view.ui.QuickAlphabeticBar;

public class HomeContactActivity extends Activity {

	private ContactHomeAdapter adapter;
	private ListView personList;
	private List<ContactBean> list;
	private AsyncQueryHandler asyncQuery;
	private QuickAlphabeticBar alpha;

	private Map<Integer, ContactBean> contactIdMap = null;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		//inflater = LayoutInflater.from(this);
		setContentView(R.layout.home_contact_page);

		//contactPage = inflater.inflate(R.layout.home_contact_page, null);
		personList = (ListView) findViewById(R.id.contact_list);
		alpha = (QuickAlphabeticBar) findViewById(R.id.fast_scroller);
		asyncQuery = new MyAsyncQueryHandler(getContentResolver());

		init();
		
		startReceiver1();
	}

	private void init(){
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri
		String[] projection = { 
				ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.DATA1,
				"sort_key",
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
				ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
				ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY
		}; // 查询的列
		asyncQuery.startQuery(0, null, uri, projection, null, null,
				"sort_key COLLATE LOCALIZED asc"); // 按照sort_key升序查询
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}	

	/**
	 * 数据库异步查询类AsyncQueryHandler
	 */
	private class MyAsyncQueryHandler extends AsyncQueryHandler {

		public MyAsyncQueryHandler(ContentResolver cr) {
			super(cr);
		}

		/**
		 * 查询结束的回调函数
		 */
		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (cursor != null && cursor.getCount() > 0) {
				
				contactIdMap = new HashMap<Integer, ContactBean>();
				
				list = new ArrayList<ContactBean>();
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					cursor.moveToPosition(i);
					String name = cursor.getString(1);
					String number = cursor.getString(2);
					String sortKey = cursor.getString(3);
					int contactId = cursor.getInt(4);
					//Long photoId = cursor.getLong(5);
					String lookUpKey = cursor.getString(6);

					if (contactIdMap.containsKey(contactId)) {
						
					}else{
						ContactBean cb = new ContactBean();
						cb.setDisplayName(name);
					if (number.startsWith("+86")) {			// 去除多余的中国地区号码标志，对这个程序没有影响。
						cb.setPhoneNum(number.substring(3));
					} else {
						cb.setPhoneNum(number);
					}
						cb.setSortKey(sortKey);
						cb.setContactId(contactId);
						cb.setLookUpKey(lookUpKey);
						list.add(cb);
						contactIdMap.put(contactId, cb);
					}
				}
				if (list.size() > 0) {
					setAdapter(list);
				}
			}
		}

	}


	private void setAdapter(List<ContactBean> list) {
		adapter = new ContactHomeAdapter(this, list, alpha);
		personList.setAdapter(adapter);
		alpha.init(HomeContactActivity.this);
		alpha.setListView(personList);
		alpha.setHight(alpha.getHeight());
		alpha.setVisibility(View.VISIBLE);
		personList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ContactBean cb = (ContactBean) adapter.getItem(position);
				//当用户单机某一下是 启动
				Uri uri = null;
				String toPhone = cb.getPhoneNum();
				uri = Uri.parse("tel:" + toPhone);
				Intent it = new Intent(Intent.ACTION_CALL, uri);
				startActivity(it);
			}
		});
	}

	// 删除联系人方法
	private void showDelete(final int contactsID, final int position) {
		new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher).setTitle("是否删除此联系人")
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//源码删除
				Uri deleteUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactsID);
				Uri lookupUri = ContactsContract.Contacts.getLookupUri(HomeContactActivity.this.getContentResolver(), deleteUri);
				if(lookupUri != Uri.EMPTY){
					HomeContactActivity.this.getContentResolver().delete(deleteUri, null, null);
				}
				adapter.remove(position);
				adapter.notifyDataSetChanged();
				Toast.makeText(HomeContactActivity.this, "该联系人已经被删除.", Toast.LENGTH_SHORT).show();
			}
		})
		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		}).show();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(1008 == requestCode){
			init();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	protected void onDestroy() {
		super.onDestroy();
		stopReceiver1();
	}

	private String ACTION1 = "SET_DEFAULT_SIG";
	private HomeContactActivity.BaseReceiver1 receiver1 = null;
	/**
	 * 打开接收器
	 */
	private void startReceiver1() {
		if(null==receiver1){
			IntentFilter localIntentFilter = new IntentFilter(ACTION1);
			receiver1 = new HomeContactActivity.BaseReceiver1();
			this.registerReceiver(receiver1, localIntentFilter);
		}
	}
	/**
	 * 关闭接收器
	 */
	private void stopReceiver1() {
		if (null != receiver1)
			unregisterReceiver(receiver1);
	}
	public class BaseReceiver1 extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTION1)) {

			}
		}
	}
}

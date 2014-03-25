package com.jadore.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jadore.application.MyApplication;
import com.jadore.bean.CallLogBean;
import com.jadore.view.adapter.HomeDialAdapter;
import com.jadore.view.adapter.T9Adapter;

import com.jadore.R;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class HomeDialActivity extends Activity implements OnClickListener {
	
	private AsyncQueryHandler asyncQuery;
	
	private HomeDialAdapter adapter;
	private ListView callLogList;
	private View inflater;
	
	private List<CallLogBean> list;
	
	private LinearLayout keyboard;
	private LinearLayout keyboard_show_ll;
	private LinearLayout keyboard_hide_ll;
	private Button keyboard_show;
	private Button keyboard_hide;
	
	//private RelativeLayout dial_top_bar;
	
	private Button phone_num_view;
	private Button dialer_btn;
	private Button delete;
	private Map<Integer, Integer> map = new HashMap<Integer, Integer>();
	private SoundPool spool;
	private AudioManager am = null;
	
	private MyApplication application;
	private ListView listView;
	private T9Adapter t9Adapter;
	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_dial_page);
		
		View convertView = View.inflate(this, R.layout.main_hometabhost, null);
		
		application = (MyApplication)getApplication();
		listView = (ListView) findViewById(R.id.contact_list);
		
		keyboard = (LinearLayout) findViewById(R.id.keyboard_body);
		//dial_top_bar = (RelativeLayout) convertView.findViewById(R.id.dial_top_bar);
		//键盘
		keyboard_show_ll = (LinearLayout) findViewById(R.id.keyboard_show_ll);
		keyboard_hide_ll = (LinearLayout) findViewById(R.id.keyboard_hide_ll);
		keyboard_show = (Button) findViewById(R.id.keyboard_show);
		keyboard_hide = (Button) findViewById(R.id.keyboard_hide);
		callLogList = (ListView)findViewById(R.id.call_log_list);
		asyncQuery = new MyAsyncQueryHandler(getContentResolver());
		//键盘隐藏于显示按钮的时间处理
		keyboard_show.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialPadShow();
			}
		});
		keyboard_hide.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialPadShow();
			}
		});
		
		//按键声的共享池
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		spool = new SoundPool(11, AudioManager.STREAM_SYSTEM, 5);
		map.put(0, spool.load(this, R.raw.dtmf0, 0));
		map.put(1, spool.load(this, R.raw.dtmf1, 0));
		map.put(2, spool.load(this, R.raw.dtmf2, 0));
		map.put(3, spool.load(this, R.raw.dtmf3, 0));
		map.put(4, spool.load(this, R.raw.dtmf4, 0));
		map.put(5, spool.load(this, R.raw.dtmf5, 0));
		map.put(6, spool.load(this, R.raw.dtmf6, 0));
		map.put(7, spool.load(this, R.raw.dtmf7, 0));
		map.put(8, spool.load(this, R.raw.dtmf8, 0));
		map.put(9, spool.load(this, R.raw.dtmf9, 0));
		map.put(11, spool.load(this, R.raw.dtmf11, 0));
		map.put(12, spool.load(this, R.raw.dtmf12, 0));
		
		//拨号按钮
		dialer_btn = (Button) findViewById(R.id.dialer_btn);
		dialer_btn.setOnClickListener(this);
		//显示电话号码的变化的时间处理
		//phone_num_view = (Button) convertView.findViewById(R.id.phone_num_view);
		//phone_num_view.setVisibility(View.GONE);
		phone_num_view = (Button) findViewById(R.id.phone_num_view);
		phone_num_view.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(null == application.getContactBeanList() || application.getContactBeanList().size()<1 || "".equals(s.toString())){
					listView.setVisibility(View.INVISIBLE);
					callLogList.setVisibility(View.VISIBLE);
					//dial_top_bar.setVisibility(View.GONE);
				}else{
					phone_num_view.setVisibility(View.GONE);
					if(null == t9Adapter){
						//dial_top_bar.setVisibility(View.VISIBLE);
						t9Adapter = new T9Adapter(HomeDialActivity.this);
						t9Adapter.assignment(application.getContactBeanList());
//						TextView tv = new TextView(HomeDialActivity.this);
//						tv.setBackgroundResource(R.drawable.dial_input_bg2);
//						listView.addFooterView(tv);
						listView.setAdapter(t9Adapter);
						listView.setTextFilterEnabled(true);
						listView.setOnScrollListener(new OnScrollListener() {
							public void onScrollStateChanged(AbsListView view, int scrollState) {
								if(scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
									if(keyboard.getVisibility() == View.VISIBLE){
										dialPadShow();
										//dial_top_bar.setVisibility(View.VISIBLE);
									}
								}
							}
							public void onScroll(AbsListView view, int firstVisibleItem,
									int visibleItemCount, int totalItemCount) {
							}
						});
					}else{
						callLogList.setVisibility(View.INVISIBLE);
						listView.setVisibility(View.VISIBLE);
						t9Adapter.getFilter().filter(s);
						//dial_top_bar.setVisibility(View.GONE);
					}
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			public void afterTextChanged(Editable s) {
			}
		});
		//删除按钮的事件处理
		delete = (Button) findViewById(R.id.delete);
		delete.setOnClickListener(this);
		delete.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				phone_num_view.setText("");
				return false;
			}
		});
		
		for (int i = 0; i < 12; i++) {
			View v = findViewById(R.id.dialNum1 + i);
			v.setOnClickListener(this);
		}
		
		init();
	}
	//查询通话记录
	private void init(){
		Uri uri = CallLog.Calls.CONTENT_URI;
		
		String[] projection = { 
				CallLog.Calls.DATE,
				CallLog.Calls.NUMBER,
				CallLog.Calls.TYPE,
				CallLog.Calls.CACHED_NAME,
				CallLog.Calls._ID
		}; // 查询的列
		asyncQuery.startQuery(0, null, uri, projection, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);  
	}
	

	private class MyAsyncQueryHandler extends AsyncQueryHandler {

		public MyAsyncQueryHandler(ContentResolver cr) {
			super(cr);
		}
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (cursor != null && cursor.getCount() > 0) {
				list = new ArrayList<CallLogBean>();
				SimpleDateFormat sfd = new SimpleDateFormat("MM-dd hh:mm");
				Date date;
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					cursor.moveToPosition(i);
					date = new Date(cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)));
//					String date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
					String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
					int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
					String cachedName = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));//缓存的名称与电话号码，如果它的存在
					int id = cursor.getInt(cursor.getColumnIndex(CallLog.Calls._ID));

					CallLogBean clb = new CallLogBean();
					clb.setId(id);
					clb.setNumber(number);
					clb.setName(cachedName);
					if(null == cachedName || "".equals(cachedName)){
						clb.setName(number);
					}
					clb.setType(type);
					clb.setDate(sfd.format(date));
					
					list.add(clb);
				}
				if (list.size() > 0) {
					setAdapter(list);
				}
			}
		}
	}
	//适配器选择
	private void setAdapter(List<CallLogBean> list) {
		adapter = new HomeDialAdapter(this, list);
//		TextView tv = new TextView(this);
//		tv.setBackgroundResource(R.drawable.dial_input_bg2);
//		callLogList.addFooterView(tv);
		callLogList.setAdapter(adapter);
		callLogList.setOnScrollListener(new OnScrollListener() {

			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
					if(keyboard.getVisibility() == View.VISIBLE){
						dialPadShow();
					}
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
		callLogList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			}
		});
	}
	//键盘上的各个按键的事件处理
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dialNum0:
			if (phone_num_view.getText().length() < 12) {
				play(1);
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum1:
			if (phone_num_view.getText().length() < 12) {
				play(1);
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum2:
			if (phone_num_view.getText().length() < 12) {
				play(2);
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum3:
			if (phone_num_view.getText().length() < 12) {
				play(3);
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum4:
			if (phone_num_view.getText().length() < 12) {
				play(4);
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum5:
			if (phone_num_view.getText().length() < 12) {
				play(5);
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum6:
			if (phone_num_view.getText().length() < 12) {
				play(6);
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum7:
			if (phone_num_view.getText().length() < 12) {
				play(7);
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum8:
			if (phone_num_view.getText().length() < 12) {
				play(8);
				input(v.getTag().toString());
			}
			break;
		case R.id.dialNum9:
			if (phone_num_view.getText().length() < 12) {
				play(9);
				input(v.getTag().toString());
			}
			break;
		case R.id.dialx:
			if (phone_num_view.getText().length() < 12) {
				play(11);
				input(v.getTag().toString());
			}
			break;
		case R.id.dialj:
			if (phone_num_view.getText().length() < 12) {
				play(12);
				input(v.getTag().toString());
			}
			break;
		case R.id.delete:
			delete();
			break;
		case R.id.dialer_btn:
			if (phone_num_view.getText().toString().length() >= 4) {
				call(phone_num_view.getText().toString());
			}
			break;
		default:
			break;
		}
	}
	//播放按键音
	private void play(int id) {
		int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int current = am.getStreamVolume(AudioManager.STREAM_MUSIC);

		float value = (float)0.7 / max * current;
		spool.setVolume(spool.play(id, value, value, 0, 0, 1f), value, value);
	}
	//按键的字符串的拼接
	private void input(String str) {
		String p = phone_num_view.getText().toString();
		phone_num_view.setText(p + str);
	}
	//删除键的事件处理
	private void delete() {
		String p = phone_num_view.getText().toString();
		if(p.length()>0){
			phone_num_view.setText(p.substring(0, p.length()-1));
		}
	}
	//完成拨号
	private void call(String phone) {
		Uri uri = Uri.parse("tel:" + phone);
		Intent it = new Intent(Intent.ACTION_CALL, uri);
		startActivity(it);
	}
	
	//键盘的主体与控制板的隐藏与显示的切换
	public void dialPadShow(){
		if(keyboard.getVisibility() == View.VISIBLE){
			keyboard.setVisibility(View.GONE);
			keyboard_show_ll.setVisibility(View.VISIBLE);
			keyboard_hide_ll.setVisibility(View.GONE);
		}else{
			keyboard.setVisibility(View.VISIBLE);
			keyboard_show_ll.setVisibility(View.GONE);
			keyboard_hide_ll.setVisibility(View.VISIBLE);
		}
	}
}

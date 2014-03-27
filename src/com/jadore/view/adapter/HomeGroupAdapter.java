package com.jadore.view.adapter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.jadore.bean.ContactBean;
import com.jadore.bean.GroupBean;
import com.jadore.view.ui.QuickAlphabeticBar;

import com.jadore.R;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.QuickContactBadge;
import android.widget.TextView;

public class HomeGroupAdapter extends BaseAdapter{
	
	private LayoutInflater inflater;
	private List<GroupBean> list;
	private String[] sections;
	private Context ctx;
	
	public final class ListItemsView{
		public TextView menuText;
	}
	public HomeGroupAdapter(Context context, List<GroupBean> list) {
		
		this.ctx = context;
		this.inflater = LayoutInflater.from(context);
		this.list = list; 
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void remove(int position){
		list.remove(position);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.home_group_list_item, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		GroupBean cb = list.get(position);
		String name = cb.getName();
		holder.name.setText(name);
		Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, cb.getContactId());
		InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(ctx.getContentResolver(), uri); 
		String currentStr = getAlpha(cb.getSortKey());
		String previewStr = (position - 1) >= 0 ? getAlpha(list.get(position - 1).getSortKey()) : " ";
		if (!previewStr.equals(currentStr)) { 
			holder.alpha.setVisibility(View.VISIBLE);
			holder.alpha.setText(currentStr);
		} else {
			holder.alpha.setVisibility(View.GONE);
		}
		return convertView;
	}
	
	private static class ViewHolder {
		//QuickContactBadge qcb;
		TextView name;
		private ArrayList<ContactBean> contact;
	}
	
	/**
	 * @param str
	 * @return
	 */
	private String getAlpha(String str) {
		if (str == null) {
			return "#";
		}
		if (str.trim().length() == 0) {
			return "#";
		}
		char c = str.trim().substring(0, 1).charAt(0);
		Pattern pattern = Pattern.compile("^[A-Za-z]+$");
		if (pattern.matcher(c + "").matches()) {
			return (c + "").toUpperCase(); 
		} else {
			return "#";
		}
	}
}

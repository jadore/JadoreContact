package com.jadore.application;

import java.util.List;

import com.jadore.bean.ContactBean;
import com.jadore.service.T9Service;

import android.content.Intent;

public class MyApplication extends android.app.Application {

	
	private List<ContactBean> contactBeanList;
	
	public List<ContactBean> getContactBeanList() {
		return contactBeanList;
	}
	public void setContactBeanList(List<ContactBean> contactBeanList) {
		this.contactBeanList = contactBeanList;
	}

	public void onCreate() {
		
		System.out.println("项目启动");
		
		Intent startService = new Intent(MyApplication.this, T9Service.class);
		startService(startService);
	}
}

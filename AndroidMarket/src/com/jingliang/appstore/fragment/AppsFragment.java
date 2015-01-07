package com.jingliang.appstore.fragment;

import com.jingliang.appstore.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 * @author tjl 　应用界面
 */
public class AppsFragment extends Fragment {

	private View mainView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mainView = inflater.inflate(R.layout.fragment_apps, container, false);
		return mainView;
	}

}

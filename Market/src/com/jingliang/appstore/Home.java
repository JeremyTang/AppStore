package com.jingliang.appstore;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.jingliang.appstore.adapter.NavAdapter;
import com.jingliang.appstore.entity.NavInfo;
import com.jingliang.appstore.fragment.Downloader;
import com.jingliang.appstore.fragment.Installed;
import com.jingliang.appstore.fragment.Market;
import com.jingliang.appstore.fragment.Update;

/**
 * 主界面 更Main以不同的方式进行fragment的管理
 * 
 * @author tjl
 *
 */
@SuppressWarnings("deprecation")
public class Home extends FragmentActivity implements OnItemClickListener {

	private static final String TAG = Home.class.getSimpleName();

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerListView;
	private ActionBar mActionBar;
	private List<NavInfo> mNavList;
	private NavInfo mCurrentNav;
	private NavAdapter mAdapter;
	private ActionBarDrawerToggle mDrawerToggle;

	private int moldIndex = -1;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		Log.d(TAG, "onCreaet()");
		init();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onPause()");
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onResume()");
		super.onResume();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mDrawerToggle.syncState();
		super.onPostCreate(savedInstanceState);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		mDrawerToggle.onConfigurationChanged(newConfig);
		super.onConfigurationChanged(newConfig);
	}

	private void init() {
		// 初始化ActionBar ....
		mActionBar = getActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		// 初始化Drawerlayout
		mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawerlayout);
		mDrawerLayout.setDrawerListener(new MyDrawerListener());
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, true,
				R.drawable.img_folder, R.string.drawerlayout_open,
				R.string.drawerlayout_close) {
			public void onDrawerClosed(View drawerView) {
				mActionBar.setTitle(mCurrentNav.getTitle());
				super.onDrawerClosed(drawerView);
			}

			public void onDrawerOpened(View drawerView) {
				mActionBar
						.setTitle(getResources().getString(R.string.app_name));
				super.onDrawerOpened(drawerView);
			}

			public boolean onOptionsItemSelected(MenuItem item) {
				if (item.getItemId() == android.R.id.home) {
					if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
						mDrawerLayout.closeDrawers();
					} else {
						mDrawerLayout.openDrawer(Gravity.LEFT);
					}
				}
				return super.onOptionsItemSelected(item);
			}
		};
		//
		mDrawerListView = (ListView) findViewById(R.id.drawer_listview);
		mNavList = initNavResource();
		mAdapter = new NavAdapter(this, mNavList);
		mDrawerListView.setAdapter(mAdapter);
		mDrawerListView.setOnItemClickListener(this);
		// 强制显示menu
		getOverflowMenu();
		// 初始化第一个
		switchFragment(mNavList.get(0));
		// switchListViewSelector(0);
		mAdapter.setSelector(0);
	}

	private List<NavInfo> initNavResource() {
		List<NavInfo> list = new ArrayList<NavInfo>();
		list.add(new NavInfo(getString(R.string.nav_apps), new Market(),
				R.drawable.img_nav_ico_camera));
		list.add(new NavInfo(getString(R.string.nav_install),
				new Installed(), R.drawable.img_nav_ico_util));
		list.add(new NavInfo(getString(R.string.nav_download),
				new Downloader(), R.drawable.img_nav_ico_flash));
		list.add(new NavInfo(getString(R.string.nav_update), new Update(),
				R.drawable.img_nav_ico_end));
		return list;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Log.d(TAG, "onItemClick()");
		switchFragment(mNavList.get(arg2));
		// switchListViewSelector(arg2);
		mAdapter.setSelector(arg2);
		mDrawerLayout.closeDrawers();
	}

	public void switchListViewSelector(int position) {
		if (position == moldIndex) {
			return;
		}
		View mCurrenView = mDrawerListView.getChildAt(position);
		View mOldView = mDrawerListView.getChildAt(moldIndex);
		Log.d(TAG, (mCurrenView == null) + " --- old = " + (mOldView == null));
		Log.d(TAG, position + " --- old = " + moldIndex);
		if (mCurrenView != null && mOldView != null) {
			mOldView.setBackgroundResource(R.drawable.selector_nav);
			mCurrenView
					.setBackgroundResource(R.drawable.bg_folder_list_item_checked);
		}
		moldIndex = position;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.setting, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		mDrawerToggle.onOptionsItemSelected(item);
		return super.onOptionsItemSelected(item);
	}

	private void switchFragment(NavInfo nav) {
		// 相同的不加载
		if (mCurrentNav == nav) {
			return;
		}
		mCurrentNav = nav;
		mActionBar.setTitle(nav.getTitle());
		FragmentTransaction mTranscation = getSupportFragmentManager()
				.beginTransaction();
		mTranscation.replace(R.id.main_contianer, nav.getFragment(),
				nav.getTitle());
		mTranscation.addToBackStack(nav.getTitle());
		mTranscation.commit();
	}

	public class MyDrawerListener implements DrawerListener {

		@Override
		public void onDrawerStateChanged(int arg0) {
			mDrawerToggle.onDrawerStateChanged(arg0);
		}

		@Override
		public void onDrawerSlide(View arg0, float arg1) {
			mDrawerToggle.onDrawerSlide(arg0, arg1);
		}

		@Override
		public void onDrawerOpened(View arg0) {
			mDrawerToggle.onDrawerOpened(arg0);
		}

		@Override
		public void onDrawerClosed(View arg0) {
			mDrawerToggle.onDrawerClosed(arg0);
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 1.Android中的ActionBar中的那三个点的按钮，专业名字叫做：overflow button或overflow menu
	 * 
	 * 2.overflow在新的Android 3.0+的系统中，默认是不显示的：
	 * 
	 * 对应的：
	 * 
	 * 对于很多PAD来说：ActionBar中空间足够显示的话，那么对应各个menu菜单，都直接显示在ActionBar中；
	 * 对于很多手机来说：ActionBar中没有足够的控件显示所有的菜单的话
	 * ，余下的菜单，就被藏起来了->只有有物理菜单（MENU）键的Android设备，点击MENU键，才能出现多余的菜单；
	 * 
	 * 3.想要让overflow始终都显示的话：
	 * 
	 * 先去添加别的高手破解后强制overflow显示的那段代码getOverflowMenu，加到Activity的onCreate中：
	 */
	private void getOverflowMenu() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

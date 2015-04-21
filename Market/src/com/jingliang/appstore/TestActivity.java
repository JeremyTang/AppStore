package com.jingliang.appstore;

import java.lang.reflect.Field;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.jingliang.appstore.fragment.Market;
import com.jingliang.appstore.fragment.Download;
import com.jingliang.appstore.fragment.Install;
import com.jingliang.appstore.fragment.Update;

/**
 * 主界面
 * 
 * @author tjl
 *
 */
@SuppressWarnings("deprecation")
public class TestActivity extends FragmentActivity implements
		OnCheckedChangeListener {

	private static final String TAG = TestActivity.class.getSimpleName();

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private ActionBar mActionBar;
	private RadioGroup mDrawerRadio;
	private RadioButton mAppsRadio;

	private Fragment mShowFragment;
	private Fragment mCurrenFragment;
	private String mFragmentTag;
	private String mTitle;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		init();

		AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
		EditText mEdit = new EditText(this);
		mEdit.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		mEdit.setPadding(15, 15, 15, 15);
		mBuilder.setTitle("Please entry yout password!");
		mBuilder.setView(mEdit);
		//"".equals(other)
		mBuilder.setPositiveButton("",
				new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}
				});
		mBuilder.create().show();
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
		Log.d(TAG, "init()");
		// 初始化ActionBar ....
		mActionBar = getActionBar();
		// 设置actionbar的home图标
		// mActionBar.setIcon(R.drawable.img_home);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		// 修改返回按钮 api18以下使用错误
		// mActionBar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.img_folder));
		// mActionBar.setStackedBackgroundDrawable(getResources().getDrawable(R.drawable.img_actionbar_overflow));

		// 初始化Drawerlayout .......
		mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawerlayout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, true,
				R.drawable.img_folder, R.string.drawerlayout_open,
				R.string.drawerlayout_close) {
			public void onDrawerClosed(View drawerView) {
				// mActionBar.setDisplayHomeAsUpEnabled(true);
				mActionBar.setTitle(mTitle);
				super.onDrawerClosed(drawerView);
			}

			public void onDrawerOpened(View drawerView) {
				mActionBar
						.setTitle(getResources().getString(R.string.app_name));
				// /mActionBar.setDisplayHomeAsUpEnabled(false);
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
		mDrawerLayout.setDrawerListener(new DrawerListener() {

			@Override
			public void onDrawerStateChanged(int arg0) {
				// TODO Auto-generated method stub
				mDrawerToggle.onDrawerStateChanged(arg0);
			}

			@Override
			public void onDrawerSlide(View arg0, float arg1) {
				// TODO Auto-generated method stub
				mDrawerToggle.onDrawerSlide(arg0, arg1);
			}

			@Override
			public void onDrawerOpened(View arg0) {
				// TODO Auto-generated method stub
				mDrawerToggle.onDrawerOpened(arg0);
			}

			@Override
			public void onDrawerClosed(View arg0) {
				// TODO Auto-generated method stub
				mDrawerToggle.onDrawerClosed(arg0);
			}
		});
		mDrawerRadio = (RadioGroup) findViewById(R.id.drawer_radiogroup);
		mAppsRadio = (RadioButton) findViewById(R.id.nav_apps);
		mDrawerRadio.setOnCheckedChangeListener(this);
		mAppsRadio.setChecked(true);
		getOverflowMenu();
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

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		showFragmentById(checkedId);
		mDrawerLayout.closeDrawers();
	}

	/**
	 * 这种方法只是做了切换不会调用onpause()跟onresume()
	 * 
	 * @param checkedId
	 */
	private void showFragmentById(int checkedId) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		// 初始化Fragment配置
		if (mCurrenFragment != null) {
			ft.hide(mCurrenFragment);
		}
		switch (checkedId) {
		case R.id.nav_apps:
			mFragmentTag = Market.class.getSimpleName();
			mShowFragment = getSupportFragmentManager().findFragmentByTag(
					mFragmentTag);
			if (mShowFragment == null) {
				mShowFragment = new Market();
				ft.add(R.id.main_contianer, mShowFragment, mFragmentTag);
			}
			mTitle = getResources().getString(R.string.nav_apps);
			break;
		case R.id.nav_install:
			mFragmentTag = Install.class.getSimpleName();
			mShowFragment = getSupportFragmentManager().findFragmentByTag(
					mFragmentTag);
			if (mShowFragment == null) {
				mShowFragment = new Install();
				ft.add(R.id.main_contianer, mShowFragment, mFragmentTag);
			}
			mTitle = getResources().getString(R.string.nav_install);
			break;
		case R.id.nav_download:
			mFragmentTag = Download.class.getSimpleName();
			mShowFragment = getSupportFragmentManager().findFragmentByTag(
					mFragmentTag);
			if (mShowFragment == null) {
				mShowFragment = new Download();
				ft.add(R.id.main_contianer, mShowFragment, mFragmentTag);
			}
			mTitle = getResources().getString(R.string.nav_download);
			break;
		case R.id.nav_update:
			mFragmentTag = Update.class.getSimpleName();
			mShowFragment = getSupportFragmentManager().findFragmentByTag(
					mFragmentTag);
			if (mShowFragment == null) {
				mShowFragment = new Update();
				ft.add(R.id.main_contianer, mShowFragment, mFragmentTag);
			}
			mTitle = getResources().getString(R.string.nav_update);
			break;
		}
		ft.show(mShowFragment).commit();
		mCurrenFragment = mShowFragment;
		mActionBar.setTitle(mTitle);
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
	
	public class MtpReveicer extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			
		}
		
	}
}

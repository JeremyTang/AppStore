package com.jingliang.appstore;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.jingliang.appstore.fragment.AppsFragment;
import com.jingliang.appstore.fragment.DownloadFragment;
import com.jingliang.appstore.fragment.InstallFragment;
import com.jingliang.appstore.fragment.UpdateFragment;

/**
 * 主界面
 * @author tjl
 *
 */
@SuppressWarnings("deprecation")
public class Main extends FragmentActivity implements OnCheckedChangeListener {

	private static final String TAG = Main.class.getSimpleName();

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mToggle;
	private ActionBar mActionBar;
	private RadioGroup mDrawerRadio;
	private RadioButton mAppsRadio;
	
	private Fragment mCurrentFragment;
	private String mFragmentTag;
	

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
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

		// 初始化Drawerlayout .......
		mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawerlayout);
		mToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.img_app_logo, R.string.drawerlayout_open,
				R.string.drawerlayout_close) {
			public void onDrawerClosed(View drawerView) {
				mActionBar.setDisplayHomeAsUpEnabled(true);
				super.onDrawerClosed(drawerView);
			}

			public void onDrawerOpened(View drawerView) {
				mActionBar.setDisplayHomeAsUpEnabled(false);
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
		mDrawerLayout.setDrawerListener(mToggle);

		// 初始化Fragment配置
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.main_contianer, new AppsFragment(),
						AppsFragment.class.getSimpleName()).commit();

		mDrawerRadio = (RadioGroup) findViewById(R.id.drawer_radiogroup);
		mAppsRadio = (RadioButton) findViewById(R.id.nav_apps);
		mDrawerRadio.setOnCheckedChangeListener(this);
		mAppsRadio.setChecked(true);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		mToggle.onOptionsItemSelected(item);
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		updateCheckState(checkedId);
		mDrawerLayout.closeDrawers();
	}

	private void updateCheckState(int checkedId) {
		switch (checkedId) {
		case R.id.nav_apps:
			mFragmentTag = AppsFragment.class.getSimpleName();
			mCurrentFragment = getSupportFragmentManager().findFragmentByTag(
					mFragmentTag);
			if (mCurrentFragment == null) {
				mCurrentFragment = new AppsFragment();
			}
			break;
		case R.id.nav_install:
			mFragmentTag = InstallFragment.class.getSimpleName();
			mCurrentFragment = getSupportFragmentManager().findFragmentByTag(
					mFragmentTag);
			if (mCurrentFragment == null) {
				mCurrentFragment = new InstallFragment();
			}
			break;
		case R.id.nav_download:
			mFragmentTag = DownloadFragment.class.getSimpleName();
			mCurrentFragment = getSupportFragmentManager().findFragmentByTag(
					mFragmentTag);
			if (mCurrentFragment == null) {
				mCurrentFragment = new DownloadFragment();
			}
			break;
		case R.id.nav_update:
			mFragmentTag = UpdateFragment.class.getSimpleName();
			mCurrentFragment = getSupportFragmentManager().findFragmentByTag(
					mFragmentTag);
			if (mCurrentFragment == null) {
				mCurrentFragment = new UpdateFragment();
			}
			break;
		}
		// 初始化Fragment配置
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.main_contianer, mCurrentFragment, mFragmentTag)
				.commit();
	}

}

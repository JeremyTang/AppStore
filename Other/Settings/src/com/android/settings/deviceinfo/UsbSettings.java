/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.deviceinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.UserManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

/**
 * USB storage settings.
 */
public class UsbSettings extends SettingsPreferenceFragment {

    private static final String TAG = "UsbSettings";

    private static final String KEY_MTP = "usb_mtp";
    private static final String KEY_PTP = "usb_ptp";

    private static final String MTP_UI_ACTION = "com.intel.mtp.action";
    private static final String MTP_STATUS = "status";

    private UsbManager mUsbManager;
    private CheckBoxPreference mMtp;
    private CheckBoxPreference mPtp;
    private boolean mUsbAccessoryMode;
    private boolean mMtpstatus;

	//added by luxincheng
	private boolean mUsbConnected;
	private boolean mUsbFirstConn = false;

    public void updateProgressDialog(boolean flag) {

        if (flag) {
            mMtp.setEnabled(false);
            mMtp.setSummary(R.string.mtp_transferring_text);
            mPtp.setEnabled(false);
            mPtp.setSummary(R.string.mtp_transferring_text);
        } else{
            mMtp.setEnabled(true);
            mMtp.setSummary(R.string.usb_mtp_summary);
            mPtp.setEnabled(true);
            mPtp.setSummary(R.string.usb_ptp_summary);
        }
    }

    private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context content, Intent intent) {
            String action = intent.getAction();
			//added by luxincheng
			mUsbConnected = intent.getExtras().getBoolean(UsbManager.USB_CONNECTED);
            if (action.equals(UsbManager.ACTION_USB_STATE)) {
               mUsbAccessoryMode = intent.getBooleanExtra(UsbManager.USB_FUNCTION_ACCESSORY, false);
               Log.d(TAG, "UsbAccessoryMode " + mUsbAccessoryMode);
			   //added by luxincheng
			   if(!mUsbConnected && mUsbFirstConn){
					mUsbFirstConn = false;
					finish();
				}else{
					mUsbFirstConn = true;
				}
               updateProgressDialog(false);
               updateToggles(mUsbManager.getDefaultFunction());
            }

            if (action.equals(MTP_UI_ACTION) && !mUsbAccessoryMode) {
               mMtpstatus = intent.getBooleanExtra(MTP_STATUS, false);
               Log.d(TAG, "MTP_UI_ACTION " + mMtpstatus);
               updateProgressDialog(mMtpstatus);
            }
        }
    };

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.usb_settings);
        root = getPreferenceScreen();

        mMtp = (CheckBoxPreference)root.findPreference(KEY_MTP);
        mPtp = (CheckBoxPreference)root.findPreference(KEY_PTP);

        UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        if (um.hasUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER)) {
            mMtp.setEnabled(false);
            mPtp.setEnabled(false);
        }

        return root;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mStateReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Make sure we reload the preference hierarchy since some of these settings
        // depend on others...
        createPreferenceHierarchy();

        // ACTION_USB_STATE is sticky so this will call updateToggles
        getActivity().registerReceiver(mStateReceiver,
                new IntentFilter(UsbManager.ACTION_USB_STATE));

        getActivity().registerReceiver(mStateReceiver,
                new IntentFilter(MTP_UI_ACTION));

    }

    private void updateToggles(String function) {
        if (UsbManager.USB_FUNCTION_MTP.equals(function)) {
            mMtp.setChecked(true);
            mPtp.setChecked(false);
        } else if (UsbManager.USB_FUNCTION_PTP.equals(function)) {
            mMtp.setChecked(false);
            mPtp.setChecked(true);
        } else  {
            mMtp.setChecked(false);
            mPtp.setChecked(false);
        }
        UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        if (um.hasUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER)) {
            Log.e(TAG, "USB is locked down");
            mMtp.setEnabled(false);
            mPtp.setEnabled(false);
        } else if (!mUsbAccessoryMode) {
            //Enable MTP and PTP switch while USB is not in Accessory Mode, otherwise disable it
            Log.d(TAG, "USB Normal Mode");
            mMtp.setEnabled(true);
            mPtp.setEnabled(true);
        } else {
            //Disable MTP and PTP switch while USB is in Accessory Mode, otherwise enable it
            Log.d(TAG, "USB Accessory Mode ");
            mMtp.setEnabled(false);
            mPtp.setEnabled(false);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        // Don't allow any changes to take effect as the USB host will be disconnected, killing
        // the monkeys
        if (Utils.isMonkeyRunning()) {
            return true;
        }

        // don't allow unchecking them
        if (preference instanceof CheckBoxPreference) {
            CheckBoxPreference checkBox = (CheckBoxPreference)preference;
            if (!checkBox.isChecked()) {
                checkBox.setChecked(true);
                return true;
            }
        }

        // If this user is disallowed from using USB, don't handle their attempts to change the
        // setting.
        UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        if (um.hasUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER)) {
            return true;
        }

        String function = "none";
        if (preference == mMtp && mMtp.isChecked()) {
            function = UsbManager.USB_FUNCTION_MTP;
        } else if (preference == mPtp && mPtp.isChecked()) {
            function = UsbManager.USB_FUNCTION_PTP;
        }

        mUsbManager.setCurrentFunction(function, true);
        updateToggles(function);

        return true;
    }
}

/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings.wifi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.wifi.WifiApConfiguration;
import android.net.wifi.WifiChannel;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.android.settings.R;
import com.android.settings.net.NetworkPolicyEditor;
import com.intel.cws.cwsservicemanager.ICwsServiceMgr;
import android.widget.Button;
import java.lang.CharSequence;
import java.net.Inet4Address;
import android.provider.Settings.SettingNotFoundException;

/**
 * Dialog to configure the SSID and security settings
 * for Access Point operation
 */
public class WifiApDialog extends AlertDialog implements View.OnClickListener,
        TextWatcher, AdapterView.OnItemSelectedListener {

    static final int SSID_MAX_LENGTH = 32;
    static final int BUTTON_SUBMIT = DialogInterface.BUTTON_POSITIVE;

    static final String TAG = "WifiAPDialog";
    private final DialogInterface.OnClickListener mListener;

    public static final int OPEN_INDEX = 0;
    public static final int WPA2_INDEX = 1;

    public static final int INDEX_24GHZ_20MHZ = 0;
    public static final int INDEX_5GHZ_20MHZ  = 1;
    public static final int INDEX_5GHZ_40MHZ  = 2;
    public static final int INDEX_5GHZ_80MHZ  = 3;

    static final int WIFI_DEFAULT_MIN_CHAN = 1;
    static final int WIFI_DEFAULT_MAX_CHAN = 11;

    private View mView;
    private TextView mSsid;
    private int mSecurityTypeIndex = OPEN_INDEX;
    private int mBandIndex = INDEX_24GHZ_20MHZ;
    private int mChannelIndex = 0;
    private CheckBox mCheckboxShowPassword;
    private CheckBox mCheckboxShowAdvanced;
    private CheckBox mCheckboxBroadcastSsid;
    private LinearLayout mAdvancedFields;
    private Spinner mSecuritySpinner;
    private Spinner mBandSpinner;
    private Spinner mChannelSpinner;
    private EditText mPassword;
    private EditText mIpAddress;
    private EditText mNetMask;
    private boolean mShowPassword = false;
    private boolean mShowAdvanced = false;
    WifiConfiguration mWifiConfig;
    private ICwsServiceMgr mCwsServiceManager;
    private List<WifiChannel> mChannels;
    private Context mContext;
    
    public WifiApDialog(Context context, DialogInterface.OnClickListener listener,
            WifiConfiguration wifiConfig) {
        super(context);
	mContext = context;
        mListener = listener;
        mWifiConfig = wifiConfig;
        if (wifiConfig != null) {
            mSecurityTypeIndex = getSecurityTypeIndex(wifiConfig);
            mBandIndex = getBandIndex(wifiConfig);
        } else {
            Log.e(TAG, "WifiApDialog - wifiConfig is null");
        }
        mCwsServiceManager = ICwsServiceMgr.Stub.
                asInterface(ServiceManager.getService(Context.CSM_SERVICE));
        if (mCwsServiceManager == null) {
            Log.e(TAG, "Failed to get a reference on mCwsServiceManager");
        }
    }

    public static int getSecurityTypeIndex(WifiConfiguration wifiConfig) {
        if (wifiConfig.allowedKeyManagement.get(KeyMgmt.WPA2_PSK)) {
            return WPA2_INDEX;
        }
        return OPEN_INDEX;
    }

    private static int getBandIndex(WifiConfiguration apConfig) {

        if (apConfig != null) {
            WifiApConfiguration cfg = apConfig.getWifiApConfigurationAdv();
            WifiChannel channel = cfg.getWifiChannel();
            if (cfg != null) {
                if ((channel.getBand() == WifiChannel.Band.BAND_5GHZ)
                        && (channel.getWidth() == WifiChannel.ChannelWidth.HT20))
                    return INDEX_5GHZ_20MHZ;
                else if ((channel.getBand() == WifiChannel.Band.BAND_5GHZ)
                        && (channel.getWidth() == WifiChannel.ChannelWidth.HT40))
                    return INDEX_5GHZ_40MHZ;
                else if ((channel.getBand() == WifiChannel.Band.BAND_5GHZ)
                        && (channel.getWidth() == WifiChannel.ChannelWidth.HT80))
                    return INDEX_5GHZ_80MHZ;
                else
                    return INDEX_24GHZ_20MHZ;
            }
        }

        return INDEX_24GHZ_20MHZ;
    }

    public WifiConfiguration getConfig() {

        WifiConfiguration config = new WifiConfiguration();

        if (config != null) {

            WifiApConfiguration cfg = config.getWifiApConfigurationAdv();
            WifiChannel channel = new WifiChannel(WifiChannel.DEFAULT_5_CHANNEL,
                    WifiChannel.ChannelWidth.HT20);
            /**
             * TODO: SSID in WifiApConfiguration for soft ap
             * is being stored as a raw string without quotes.
             * This is not the case on the client side. We need to
             * make things consistent and clean it up
             */
            config.SSID = mSsid.getText().toString();

            if (mCheckboxBroadcastSsid != null) {
                config.hiddenSSID = !mCheckboxBroadcastSsid.isChecked();
            }

            switch (mSecurityTypeIndex) {
                case OPEN_INDEX:
                    config.allowedKeyManagement.set(KeyMgmt.NONE);
                    //2014-12-26 modified by chenfufeng to fix:set the default wifi hotspot password when change wpa2
                    Settings.Global.putInt(mContext.getContentResolver(), "isOpen", 1);
                    break;
                case WPA2_INDEX:
                    Settings.Global.putInt(mContext.getContentResolver(), "isOpen", 0);
                    config.allowedKeyManagement.set(KeyMgmt.WPA2_PSK);
                    config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                    if (mPassword.length() != 0) {
                        String password = mPassword.getText().toString();
                        config.preSharedKey = password;
                    }
                    break;
                default:
                    return null;
            }

            if (cfg != null) {
                cfg.mIpAddress = mIpAddress.getText().toString();
                switch (mBandIndex) {
                    case INDEX_24GHZ_20MHZ:
                        channel.setWidth(WifiChannel.ChannelWidth.HT20);
                        break;
                    case INDEX_5GHZ_20MHZ:
                        channel.setWidth(WifiChannel.ChannelWidth.HT20);
                        break;
                    case INDEX_5GHZ_40MHZ:
                        channel.setWidth(WifiChannel.ChannelWidth.HT40);
                        break;
                    case INDEX_5GHZ_80MHZ:
                        channel.setWidth(WifiChannel.ChannelWidth.HT80);
                        break;
                    default:
                        return null;
                }
                if (mChannelIndex == 0) {
                    if (mBandIndex >= INDEX_5GHZ_20MHZ)
                        channel.setChannel(WifiChannel.DEFAULT_5_CHANNEL);
                    else
                        channel.setChannel(WifiChannel.DEFAULT_2_4_CHANNEL);
                }
                else
                    channel.setChannel(Integer.valueOf(
                            ((String) mChannelSpinner.getItemAtPosition(mChannelIndex))));
                cfg.setChannel(channel);
                cfg.mNetMask = mNetMask.getText().toString();
            }
        }
        return config;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.wifi_ap_dialog, null);
        mSecuritySpinner = ((Spinner) mView.findViewById(R.id.security));

        setView(mView);
        setInverseBackgroundForced(true);

        Context context = getContext();

        setTitle(R.string.wifi_tether_configure_ap_text);
        mView.findViewById(R.id.type).setVisibility(View.VISIBLE);
        mSsid = (TextView) mView.findViewById(R.id.ssid);
        mPassword = (EditText) mView.findViewById(R.id.password);
        mIpAddress = (EditText) mView.findViewById(R.id.ipaddress);
        if (mIpAddress != null)
            mIpAddress.setInputType(InputType.TYPE_CLASS_PHONE);
        mNetMask = (EditText) mView.findViewById(R.id.subnet_mask_settings);
        if (mNetMask != null)
            mNetMask.setInputType(InputType.TYPE_CLASS_PHONE);
        mAdvancedFields = (LinearLayout) mView.findViewById(R.id.hotspot_advanced_settings);
        if (mAdvancedFields != null) {
            mAdvancedFields.setVisibility(mShowAdvanced ? View.VISIBLE : View.GONE);
        }
        mChannelSpinner = (Spinner) mView.findViewById(R.id.hotspot_channel_spinner);
        mBandSpinner = (Spinner) mView.findViewById(R.id.hotspot_band_mode_spinner);
        setButton(BUTTON_SUBMIT, context.getString(R.string.wifi_save), mListener);
        setButton(DialogInterface.BUTTON_NEGATIVE,
        context.getString(R.string.wifi_cancel), mListener);

        if (mWifiConfig != null) {
            mSsid.setText(mWifiConfig.SSID);
            mSecuritySpinner.setSelection(mSecurityTypeIndex);
            if (mSecurityTypeIndex == WPA2_INDEX) {
                mPassword.setText(mWifiConfig.preSharedKey);
            }
            if (mBandSpinner != null) {
                mBandSpinner.setSelection(mBandIndex);
            } else {
                Log.e(TAG, "WifiApDialog - spinner view is null");
            }
            if (mIpAddress != null)
                mIpAddress.setText(mWifiConfig.getWifiApConfigurationAdv().mIpAddress);
            if (mNetMask != null)
                mNetMask.setText(mWifiConfig.getWifiApConfigurationAdv().mNetMask);
        }

        if (savedInstanceState != null) { // Restore show password after rotation
            Boolean show_pass = (Boolean)savedInstanceState.get("show_password");
            if (show_pass != null) {
                mShowPassword = show_pass;
            }
        }
        mSsid.addTextChangedListener(this);
        if (mIpAddress != null) {

            mIpAddress.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        Button b = getButton(BUTTON_SUBMIT);
                        if (!isValidIpAddress(v.getText().toString())) {
                            Toast.makeText(getContext(),
                                    R.string.invalid_wifi_ip_address,
                                    Toast.LENGTH_SHORT).show();
                            if (b != null)
                                b.setEnabled(false);
                        } else {
                            if (b != null)
                                b.setEnabled(true);
                        }
                        return true;
                    }
                    return false;
                }
            });

            mIpAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        Button b = getButton(BUTTON_SUBMIT);
                        if (!isValidIpAddress(((TextView)v).getText().toString())) {
                            Toast.makeText(getContext(),
                                    R.string.invalid_wifi_ip_address,
                                    Toast.LENGTH_SHORT).show();
                            if (b != null)
                                b.setEnabled(false);
                        } else {
                            if (b != null)
                                b.setEnabled(true);
                        }
                    }
                }
            });
        }

        if (mNetMask != null) {
            mNetMask.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        Button b = getButton(BUTTON_SUBMIT);
                        if (!isCorrectNetmask(v.getText().toString())) {
                            Toast.makeText(getContext(),
                                    R.string.invalid_wifi_net_mask,
                                    Toast.LENGTH_SHORT).show();
                            if (b != null)
                                b.setEnabled(false);
                        } else {
                            if (b != null)
                                b.setEnabled(true);
                            hideKeyboard((EditText)v);
                        }
                    }
                    return true;
                }
            });

            mNetMask.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        Button b = getButton(BUTTON_SUBMIT);
                        if (!isCorrectNetmask(((TextView)v).getText().toString())) {
                            Toast.makeText(getContext(),
                                    R.string.invalid_wifi_net_mask,
                                    Toast.LENGTH_SHORT).show();
                            if (b != null)
                                b.setEnabled(false);
                        } else {
                            if (b != null)
                                b.setEnabled(true);
                        }
                    }
                }
            });
        }

        mPassword.setInputType(
                InputType.TYPE_CLASS_TEXT | (mShowPassword ?
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                InputType.TYPE_TEXT_VARIATION_PASSWORD));
        mPassword.addTextChangedListener(this);

        mCheckboxShowPassword = (CheckBox) mView.findViewById(R.id.show_password);
        if (mCheckboxShowPassword != null) {
            mCheckboxShowPassword.setOnClickListener(this);
            mCheckboxShowPassword.setChecked(mShowPassword);
        }

        mCheckboxShowAdvanced = (CheckBox) mView.findViewById(R.id.hotspot_advanced_togglebox);
        if (mCheckboxShowAdvanced != null) {
            mCheckboxShowAdvanced.setOnClickListener(this);
            mCheckboxShowAdvanced.setChecked(mShowAdvanced);
        }

        mCheckboxBroadcastSsid = (CheckBox) mView.findViewById(R.id.broadcast_ssid_checkbox);
        if (mCheckboxBroadcastSsid != null && mWifiConfig != null) {
            mCheckboxBroadcastSsid.setChecked(!mWifiConfig.hiddenSSID);
        }

        WifiManager wManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        mChannels = wManager.getWifiAuthorizedChannels();

        populateBand();
        populateChannels();
        if (mSecuritySpinner != null) {
            mSecuritySpinner.setOnItemSelectedListener(this);
        }
        if (mBandSpinner != null) {
            mBandSpinner.setOnItemSelectedListener(this);
        }
        if (mChannelSpinner != null) {
            mChannelSpinner.setOnItemSelectedListener(this);
        }

        super.onCreate(savedInstanceState);

        showSecurityFields();
        validate();
    }

    private void hideKeyboard(EditText editText)
    {
        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private static boolean isCorrectNetmask(String mask) {
        try {
            int allOnesInt =  NetworkUtils.generateReversedNetMask(0);
            Inet4Address maskaddress = (Inet4Address)NetworkUtils.numericToInetAddress(mask);
            int prefix = NetworkUtils.
                    netmaskIntToPrefixLength(NetworkUtils.
                            inetAddressToInt(maskaddress));
            if ((prefix < 0) || (prefix > 32)) return false;
            int addressInt =  NetworkUtils.inetAddressToInt2(maskaddress);
            int antiMask = NetworkUtils.generateReversedNetMask(prefix);
            return (addressInt ^ antiMask) == allOnesInt;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isValidIpAddress(String ipAddress) {
        try {
            NetworkUtils.numericToInetAddress(ipAddress);
            return true;
        }
        catch(IllegalArgumentException e) {
            return false;
        }
    }
    private void validate() {
        final byte[] utf8Ssid = mSsid.getText().toString().getBytes();
        if ((mSsid != null && (mSsid.length() == 0 || utf8Ssid.length > SSID_MAX_LENGTH )) ||
                   (mSecurityTypeIndex == WPA2_INDEX && mPassword.length() < 8)) {
            getButton(BUTTON_SUBMIT).setEnabled(false);
        } else {
            getButton(BUTTON_SUBMIT).setEnabled(true);
        }
    }

    private void populateBand() {
        String[] allBands = getContext().getResources().getStringArray(R.array.wifi_ap_band_mode);
        List<String> allowedBands = new ArrayList<String>();

        int maxIndex = INDEX_24GHZ_20MHZ;

        if (mChannels != null) {
            for (WifiChannel channel : mChannels) {
                if (channel.getBand() == WifiChannel.Band.BAND_2_4GHZ) {
                    /* Currently in 2.4 GHz band, only 20 MHz band width is supported */
                    maxIndex = INDEX_24GHZ_20MHZ;
                } else {
                    switch (channel.getWidth()) {
                        case HT20:
                            maxIndex = INDEX_5GHZ_20MHZ;
                            break;
                        case HT40:
                            maxIndex = INDEX_5GHZ_40MHZ;
                            break;
                        case HT80:
                            maxIndex = INDEX_5GHZ_80MHZ;
                            break;
                    }
                }
            }
        } else {
            Log.i(TAG, "getWifiAuthorizedChannels returned NULL, BAND will be forced to 2GHZ");
        }

        for (int i = 0; i <= maxIndex; i++)
            allowedBands.add(allBands[i]);
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(getContext(),
                android.R.layout.simple_spinner_item, allowedBands);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (mBandSpinner != null) {
            mBandSpinner.setAdapter(spinnerArrayAdapter);
            if (mBandIndex > maxIndex)
                mBandIndex = INDEX_24GHZ_20MHZ;
            mBandSpinner.setSelection(mBandIndex);
        }
    }

    private void populateChannels() {
        WifiApConfiguration cfg = null;

        WifiChannel.Band band = WifiChannel.Band.BAND_2_4GHZ;
        if (mBandIndex >= INDEX_5GHZ_20MHZ)
            band = WifiChannel.Band.BAND_5GHZ;
        mChannelIndex = 0;
        WifiChannel selectedChannel = null;
        if (mWifiConfig != null) {
            cfg = mWifiConfig.getWifiApConfigurationAdv();
            if (cfg != null)
                selectedChannel = cfg.getWifiChannel();
        }

        List<String> userList = new ArrayList<String>();
        userList.add(getContext().getString(R.string.hotspot_channel_auto));
        int safeChannels = 0;
        try {
            if (mCwsServiceManager != null) {
                safeChannels = mCwsServiceManager.getWifiSafeChannelBitmap();
            } else {
                Log.e(TAG,"mCwsServiceManager is null");
            }
        } catch (Exception e) {
            // no need to do anything, we will use the full channel bitmap.
            Log.e(TAG, "populate w safe channels Exception: " + e.toString());
        }

        if (mChannels != null && cfg != null) {
            for (WifiChannel channel : mChannels) {
                if (channel.getBand() == band) {
                    if ((safeChannels & (1 << (channel.getChannel() -1 ))) == 0) {
                        if (cfg.getWifiChannel().equals(channel))
                            mChannelIndex = userList.size();
                        userList.add(Integer.toString(channel.getChannel()));
                    }
                }
            }
        } else {
            Log.i(TAG, "getWifiAuthorizedChannels returned NULL, set channel 1-11");
            int chan = 1;
            for (chan = WIFI_DEFAULT_MIN_CHAN; chan <= WIFI_DEFAULT_MAX_CHAN; chan++) {
                if (selectedChannel != null) {
                    if ( chan == selectedChannel.getChannel()) {
                        mChannelIndex = userList.size();
                    }
                } else {
                    if (chan == WifiChannel.DEFAULT_2_4_CHANNEL) {
                        mChannelIndex = userList.size();
                    }
                }
                if ((safeChannels & (1 << (chan -1 ))) == 0) {
                    userList.add(Integer.toString(chan));
                }
            }
        }

        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(getContext(),
                android.R.layout.simple_spinner_item, userList);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (mChannelSpinner != null) {
            mChannelSpinner.setAdapter(spinnerArrayAdapter);
            mChannelSpinner.setSelection(mChannelIndex);
        }
    }

    public void onClick(View view) {
        if (view == mCheckboxShowPassword) {
            int position = mPassword.getSelectionStart();
            mShowPassword = mCheckboxShowPassword.isChecked();
            mPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | (mShowPassword
                    ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                    InputType.TYPE_TEXT_VARIATION_PASSWORD));
            if (mPassword.isFocused()) {
                ((EditText) mPassword).setSelection(position);
            }
        } else if (view == mCheckboxShowAdvanced) {
            mShowAdvanced = mCheckboxShowAdvanced.isChecked();
            if (mAdvancedFields != null)
                mAdvancedFields.setVisibility(mShowAdvanced ? View.VISIBLE : View.GONE);
        }
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void afterTextChanged(Editable editable) {
        validate();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent == mSecuritySpinner) {
            mSecurityTypeIndex = position;
            showSecurityFields();
            validate();
        }
        else if (parent == mBandSpinner) {
            mBandIndex = position;
            populateChannels();
        }
        else if (parent == mChannelSpinner) {
            mChannelIndex = position;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void showSecurityFields() {
        if (mSecurityTypeIndex == OPEN_INDEX) {
            mView.findViewById(R.id.fields).setVisibility(View.GONE);
            //return;
        }else{
        	mView.findViewById(R.id.fields).setVisibility(View.VISIBLE);
	try {
                if(Settings.Global.getInt(mContext.getContentResolver(), "isOpen") == 1){
			mPassword.setText("micromax");
		}else{
			mPassword.setText(mWifiConfig.preSharedKey);
		}
            } catch (SettingNotFoundException snfe) {
		mPassword.setText("micromax");
            }
       }
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle b = super.onSaveInstanceState();
        CheckBox show_pass = (CheckBox) mView.findViewById(R.id.show_password);
        if (show_pass != null)
            b.putBoolean("show_password", show_pass.isChecked());
        return b;
    }
}

package com.way.ui.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.way.activity.MainActivity;
import com.way.exception.XXAdressMalformedException;
import com.way.service.XXService;
import com.way.util.XMPPHelper;
import com.way.xx.R;

public class AddRosterItemDialog extends AlertDialog implements
		DialogInterface.OnClickListener, TextWatcher {

	private MainActivity mMainActivity;
	private XXService mXxService;

	private Button okButton;
	private EditText userInputField;
	private EditText aliasInputField;
	private GroupNameView mGroupNameView;

	public AddRosterItemDialog(MainActivity mainActivity,
			XXService service) {
		super(mainActivity);
		mMainActivity = mainActivity;
		mXxService = service;

		setTitle(R.string.addFriend_Title);

		LayoutInflater inflater = (LayoutInflater) mainActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View group = inflater.inflate(R.layout.addrosteritemdialog, null, false);
		setView(group);

		userInputField = (EditText)group.findViewById(R.id.AddContact_EditTextField);
		aliasInputField = (EditText)group.findViewById(R.id.AddContactAlias_EditTextField);

		mGroupNameView = (GroupNameView)group.findViewById(R.id.AddRosterItem_GroupName);
		mGroupNameView.setGroupList(mMainActivity.getRosterGroups());

		setButton(BUTTON_POSITIVE, mainActivity.getString(android.R.string.ok), this);
		setButton(BUTTON_NEGATIVE, mainActivity.getString(android.R.string.cancel),
				(DialogInterface.OnClickListener)null);

	}
	public AddRosterItemDialog(MainActivity mainActivity,
			XXService service, String jid) {
		this(mainActivity, service);
		userInputField.setText(jid);
	}

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		okButton = getButton(BUTTON_POSITIVE);
		afterTextChanged(userInputField.getText());

		userInputField.addTextChangedListener(this);
	}

	public void onClick(DialogInterface dialog, int which) {
		mXxService.addRosterItem(userInputField.getText()
				.toString(), aliasInputField.getText().toString(),
				mGroupNameView.getGroupName());
	}

	public void afterTextChanged(Editable s) {
		try {
			XMPPHelper.verifyJabberID(s);
			okButton.setEnabled(true);
			userInputField.setTextColor(XMPPHelper.getEditTextColor(mMainActivity));
		} catch (XXAdressMalformedException e) {
			okButton.setEnabled(false);
			userInputField.setTextColor(Color.RED);
		}
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

}

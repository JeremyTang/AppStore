package com.way.ui.view;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.way.xx.R;

public class GroupNameView extends LinearLayout implements OnItemSelectedListener {
	private Context mContext;
	private List<String> mGroupList;
	private ArrayAdapter<String> mGroupAdapter;
	private Spinner mGroupSpinner;
	private EditText mNewGroupInput;
	private String mAddGroupString;
	private String mDefaultGroup;

	public GroupNameView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);

		((LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
			.inflate(R.layout.groupnameview, this);

		mContext = ctx;
		mGroupSpinner = (Spinner)findViewById(R.id.groupspinner);
		mNewGroupInput = (EditText)findViewById(R.id.newgroupinput);

		mAddGroupString = ctx.getString(R.string.addrosteritemaddgroupchoice);
		mDefaultGroup = ctx.getString(R.string.default_group);
	}

	public void setGroupList(List<String> groupList) {
		mGroupList = groupList;
		// replace the internal representation of the default group
		mGroupList.remove("");
		mGroupList.add(0, mDefaultGroup);

		// add the string for "new group"
		mGroupList.add(mAddGroupString);

		// XXX
		mGroupAdapter = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_spinner_item, mGroupList);
		mGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mGroupSpinner.setAdapter(mGroupAdapter);
		mGroupSpinner.setOnItemSelectedListener(this);
	}

	public String getGroupName() {
		String spinnerItem = mGroupSpinner.getSelectedItem().toString();
		if (spinnerItem.equals(mDefaultGroup)) {
			// return internal representation
			return "";
		} else if (spinnerItem.equals(mAddGroupString)) {
			return mNewGroupInput.getText().toString();
		} else {
			return spinnerItem;
		}
	}

	void setInputVisibility(boolean vis) {
		Log.d("GroupNameView", "setInputVisibility: " + vis);
		mNewGroupInput.setVisibility(vis ? View.VISIBLE : View.GONE);
		mNewGroupInput.setEnabled(vis);
	}

	public void onItemSelected(AdapterView<?> view, View arg1, int arg2,
			long arg3) {
		Log.d("GroupNameView", "onItemSelected: " + view.getSelectedItem());
		setInputVisibility(view.getSelectedItem().toString().equals(mAddGroupString));
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		// this event is ignored
	}
}

package com.way.util;

import com.way.xx.R;

public enum StatusMode {
	offline(R.string.status_offline, -1), // 离线状态，没有图标
	dnd(R.string.status_dnd, R.drawable.status_shield), // 请勿打扰
	xa(R.string.status_xa, R.drawable.status_invisible), // 隐身
	away(R.string.status_away, R.drawable.status_leave), // 离开
	available(R.string.status_online, R.drawable.status_online), // 在线
	chat(R.string.status_chat, R.drawable.status_qme);// Q我吧

	private final int textId;
	private final int drawableId;

	StatusMode(int textId, int drawableId) {
		this.textId = textId;
		this.drawableId = drawableId;
	}

	public int getTextId() {
		return textId;
	}

	public int getDrawableId() {
		return drawableId;
	}

	public String toString() {
		return name();
	}

	public static StatusMode fromString(String status) {
		return StatusMode.valueOf(status);
	}

}

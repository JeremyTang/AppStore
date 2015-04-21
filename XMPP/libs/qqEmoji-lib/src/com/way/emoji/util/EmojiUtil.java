package com.way.emoji.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 * @author way
 * 
 */
public class EmojiUtil {
	public static final String STATIC_FACE_PREFIX = "f_static_";
	public static final String DYNAMIC_FACE_PREFIX = "f";

	private EmojiUtil() {
		initEmojiMap();
	}

	private static EmojiUtil instance;

	public static EmojiUtil getInstance() {
		if (null == instance)
			instance = new EmojiUtil();
		return instance;
	}

	private Map<String, String> mEmojiMap;

	private void initEmojiMap() {
		mEmojiMap = new LinkedHashMap<String, String>();
		mEmojiMap.put("[呲牙]", "000");
		mEmojiMap.put("[调皮]", "001");
		mEmojiMap.put("[流汗]", "002");
		mEmojiMap.put("[偷笑]", "003");
		mEmojiMap.put("[再见]", "004");
		mEmojiMap.put("[敲打]", "005");
		mEmojiMap.put("[擦汗]", "006");
		mEmojiMap.put("[猪头]", "007");
		mEmojiMap.put("[玫瑰]", "008");
		mEmojiMap.put("[流泪]", "009");
		mEmojiMap.put("[大哭]", "010");
		mEmojiMap.put("[嘘]", "011");
		mEmojiMap.put("[酷]", "012");
		mEmojiMap.put("[抓狂]", "013");
		mEmojiMap.put("[委屈]", "014");
		mEmojiMap.put("[便便]", "015");
		mEmojiMap.put("[炸弹]", "016");
		mEmojiMap.put("[菜刀]", "017");
		mEmojiMap.put("[可爱]", "018");
		mEmojiMap.put("[色]", "019");
		mEmojiMap.put("[害羞]", "020");

		mEmojiMap.put("[得意]", "021");
		mEmojiMap.put("[吐]", "022");
		mEmojiMap.put("[微笑]", "023");
		mEmojiMap.put("[发怒]", "024");
		mEmojiMap.put("[尴尬]", "025");
		mEmojiMap.put("[惊恐]", "026");
		mEmojiMap.put("[冷汗]", "027");
		mEmojiMap.put("[爱心]", "028");
		mEmojiMap.put("[示爱]", "029");
		mEmojiMap.put("[白眼]", "030");
		mEmojiMap.put("[傲慢]", "031");
		mEmojiMap.put("[难过]", "032");
		mEmojiMap.put("[惊讶]", "033");
		mEmojiMap.put("[疑问]", "034");
		mEmojiMap.put("[睡]", "035");
		mEmojiMap.put("[亲亲]", "036");
		mEmojiMap.put("[憨笑]", "037");
		mEmojiMap.put("[爱情]", "038");
		mEmojiMap.put("[衰]", "039");
		mEmojiMap.put("[撇嘴]", "040");
		mEmojiMap.put("[阴险]", "041");

		mEmojiMap.put("[奋斗]", "042");
		mEmojiMap.put("[发呆]", "043");
		mEmojiMap.put("[右哼哼]", "044");
		mEmojiMap.put("[拥抱]", "045");
		mEmojiMap.put("[坏笑]", "046");
		mEmojiMap.put("[飞吻]", "047");
		mEmojiMap.put("[鄙视]", "048");
		mEmojiMap.put("[晕]", "049");
		mEmojiMap.put("[大兵]", "050");
		mEmojiMap.put("[可怜]", "051");
		mEmojiMap.put("[强]", "052");
		mEmojiMap.put("[弱]", "053");
		mEmojiMap.put("[握手]", "054");
		mEmojiMap.put("[胜利]", "055");
		mEmojiMap.put("[抱拳]", "056");
		mEmojiMap.put("[凋谢]", "057");
		mEmojiMap.put("[饭]", "058");
		mEmojiMap.put("[蛋糕]", "059");
		mEmojiMap.put("[西瓜]", "060");
		mEmojiMap.put("[啤酒]", "061");
		mEmojiMap.put("[飘虫]", "062");

		mEmojiMap.put("[勾引]", "063");
		mEmojiMap.put("[OK]", "064");
		mEmojiMap.put("[爱你]", "065");
		mEmojiMap.put("[咖啡]", "066");
		mEmojiMap.put("[钱]", "067");
		mEmojiMap.put("[月亮]", "068");
		mEmojiMap.put("[美女]", "069");
		mEmojiMap.put("[刀]", "070");
		mEmojiMap.put("[发抖]", "071");
		mEmojiMap.put("[差劲]", "072");
		mEmojiMap.put("[拳头]", "073");
		mEmojiMap.put("[心碎]", "074");
		mEmojiMap.put("[太阳]", "075");
		mEmojiMap.put("[礼物]", "076");
		mEmojiMap.put("[足球]", "077");
		mEmojiMap.put("[骷髅]", "078");
		mEmojiMap.put("[挥手]", "079");
		mEmojiMap.put("[闪电]", "080");
		mEmojiMap.put("[饥饿]", "081");
		mEmojiMap.put("[困]", "082");
		mEmojiMap.put("[咒骂]", "083");

		mEmojiMap.put("[折磨]", "084");
		mEmojiMap.put("[抠鼻]", "085");
		mEmojiMap.put("[鼓掌]", "086");
		mEmojiMap.put("[糗大了]", "087");
		mEmojiMap.put("[左哼哼]", "088");
		mEmojiMap.put("[哈欠]", "089");
		mEmojiMap.put("[快哭了]", "090");
		mEmojiMap.put("[吓]", "091");
		mEmojiMap.put("[篮球]", "092");
		mEmojiMap.put("[乒乓球]", "093");
		mEmojiMap.put("[NO]", "094");
		mEmojiMap.put("[跳跳]", "095");
		mEmojiMap.put("[怄火]", "096");
		mEmojiMap.put("[转圈]", "097");
		mEmojiMap.put("[磕头]", "098");
		mEmojiMap.put("[回头]", "099");
		mEmojiMap.put("[跳绳]", "100");
		mEmojiMap.put("[激动]", "101");
		mEmojiMap.put("[街舞]", "102");
		mEmojiMap.put("[献吻]", "103");
		mEmojiMap.put("[左太极]", "104");

		mEmojiMap.put("[右太极]", "105");
		mEmojiMap.put("[闭嘴]", "106");
	}

	public Map<String, String> getFaceMap() {
		return mEmojiMap;
	}

	public String getFaceId(String faceStr) {
		if (mEmojiMap.containsKey(faceStr)) {
			return mEmojiMap.get(faceStr);
		}
		return "";
	}

}

package com.creati.util;

import java.awt.*;

public class UITheme {

	// Background / Text
	public static final Color BG = new Color(0xF7F7FB);
	public static final Color TEXT = new Color(0x1F1F24);
	public static final Color ERROR = new Color(0xD32F2F);

	// Accent
	public static final Color ACCENT_PURPLE = new Color(0x6D4CFF);

	// Fonts
	public static Font H2;
	public static Font BODY;
	public static Font BODY_MED;
	public static Font CAPTION;

	private static boolean inited = false;

	public static void init() {
		if (inited)
			return;

		H2 = FontKit.esamanruBold(26f);
		BODY = FontKit.regular(14f);
		BODY_MED = FontKit.medium(14f);
		CAPTION = FontKit.regular(12.5f);

		inited = true;
	}

	public static void ensureInit() {
		if (!inited)
			init();
	}

	public static boolean isInited() {
		return inited;
	}
}

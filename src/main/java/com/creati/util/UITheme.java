package com.creati.util;

import java.awt.*;

public class UITheme {

	
	public enum ChipStyle {
		SUCCESS,
		INFO,
		WARN,
		NEUTRAL
	}

	
	public static final Color BG = new Color(0xF7F7FB);
	public static final Color TEXT = new Color(0x1F1F24);
	public static final Color MUTED_TEXT = new Color(0x64646F);
	public static final Color ICON_MUTED = new Color(0x6E6E7D);
	public static final Color ERROR = new Color(0xD32F2F);

	
	public static final Color SURFACE = new Color(0xFFFFFF);
	public static final Color SURFACE_BORDER = new Color(0xE6E6EB);

	
	public static final Color BTN_SECONDARY_BG = new Color(0xF5F5F8);
	public static final Color BTN_SECONDARY_BORDER = new Color(0xE1E1E8);
	public static final Color LINK_MUTED = new Color(0x969696);

	// [COLLAB] 공용 UI 컬러(세부 화면에서 new Color 하드코딩 금지)
	public static final Color REACTION_BTN_BG = new Color(244, 246, 248);
	public static final Color REACTION_BTN_BG_SELECTED = new Color(228, 231, 235);
	public static final Color COMMENT_AUTHOR_BG = new Color(245, 240, 255);
	public static final Color COMMENT_AUTHOR_BADGE_BG = new Color(235, 225, 255);
	public static final Color COMMENT_AUTHOR_BADGE_FG = new Color(90, 70, 140);

	
	public static final Color DARK_SURFACE = new Color(20, 18, 28);
	public static final Color ON_DARK = new Color(0xFFFFFF);

	
	public static final Color ACCENT_BLUE = new Color(0x1D4ED8);
	public static final Color ACCENT_LAVENDER_BG = new Color(0xF5F3FF);
	public static final Color ACCENT_LAVENDER_BG_2 = new Color(0xEAE6FF);
	public static final Color ACCENT_LAVENDER_BORDER = new Color(0xCFC9FF);
	public static final Color ACCENT_PINK = new Color(0xC2417A);
	public static final Color BG_ALT = new Color(0xF5F6FA);
	public static final Color BLACK = new Color(0x000000);
	public static final Color DANGER_BG = new Color(0xFFF1F1);
	public static final Color DARK_TEXT = new Color(0x2A2A33);
	public static final Color DIVIDER = new Color(0xE6E6EF);
	public static final Color DIVIDER_2 = new Color(0xE9E9EE);
	public static final Color ERROR_DARK = new Color(0xB3261E);
	public static final Color HOVER_BG = new Color(0xEEEFFF);
	public static final Color HOVER_BG_2 = new Color(0xEEEFF6);
	public static final Color INFO_BG = new Color(0xEAF2FF);
	public static final Color INFO_BG_2 = new Color(0xE8F0FF);
	public static final Color NEUTRAL_075 = new Color(0xEFEFF6);
	public static final Color NEUTRAL_100 = new Color(0xF3F4F6);
	public static final Color NEUTRAL_150 = new Color(0xF1F2F4);
	public static final Color NEUTRAL_200 = new Color(0xDDDEE6);
	public static final Color PINK_BG = new Color(0xFCEEF4);
	public static final Color PURPLE_BG_3 = new Color(0xF3F1FF);
	public static final Color PURPLE_DARK = new Color(0x4C3DAE);
	public static final Color SUCCESS_BG = new Color(0xEAF7EE);
	public static final Color SUCCESS_TEXT = new Color(0x166534);
	public static final Color SUCCESS_TEXT_2 = new Color(0x2E7D32);
	public static final Color SURFACE_SUBTLE = new Color(0xFAFAFD);
	public static final Color SURFACE_TINT = new Color(0xFAF9FF);
	public static final Color TEXT_DISABLED = new Color(0x6B6B77);
	public static final Color TEXT_SECONDARY_DARK = new Color(0x374151);
	public static final Color TEXT_STRONG = new Color(0x333333);
	public static final Color TEXT_SUBTLE = new Color(0x666666);
	public static final Color TRANSPARENT = new Color(0, 0, 0, 0);
	public static final Color WARNING_BG = new Color(0xFFF3CC);
	public static final Color WARNING_BG_2 = new Color(0xFFF3D6);
	public static final Color WARN_TEXT = new Color(0x7A4B00);
	public static final Color WARN_TEXT_DARK = new Color(0x3A2A00);
	public static final Color WHITE = new Color(0xFFFFFF);
	public static final Color YELLOW_200 = new Color(0xFFE082);
	public static final Color YELLOW_250 = new Color(0xFFE474);
	public static final Color YELLOW_300 = new Color(0xFFD54F);
	public static final Color YELLOW_500 = new Color(0xFFC107);

	
	public static final Color RGB_100_100_110 = new Color(100, 100, 110);
	public static final Color RGB_110_110_110 = new Color(110, 110, 110);
	public static final Color RGB_110_110_125 = new Color(110, 110, 125);
	public static final Color RGB_120_120_120 = new Color(120, 120, 120);
	public static final Color RGB_120_120_130 = new Color(120, 120, 130);
	public static final Color RGB_125_125_140 = new Color(125, 125, 140);
	public static final Color RGB_130_130_140 = new Color(130, 130, 140);
	public static final Color RGB_130_130_145 = new Color(130, 130, 145);
	public static final Color RGB_140_140_140 = new Color(140, 140, 140);
	public static final Color RGB_140_140_155 = new Color(140, 140, 155);
	public static final Color RGB_150_150_160 = new Color(150, 150, 160);
	public static final Color RGB_160_160_170 = new Color(160, 160, 170);
	public static final Color RGB_170_170_170 = new Color(170, 170, 170);
	public static final Color RGB_180_70_70 = new Color(180, 70, 70);
	public static final Color RGB_200_200_200 = new Color(200, 200, 200);
	public static final Color RGB_20_18_28 = new Color(20, 18, 28);
	public static final Color RGB_210_210_220 = new Color(210, 210, 220);
	public static final Color RGB_220_220_232 = new Color(220, 220, 232);
	public static final Color RGB_225_225_232 = new Color(225, 225, 232);
	public static final Color RGB_228_228_238 = new Color(228, 228, 238);
	public static final Color RGB_230_230_235 = new Color(230, 230, 235);
	public static final Color RGB_230_230_238 = new Color(230, 230, 238);
	public static final Color RGB_235_235_240 = new Color(235, 235, 240);
	public static final Color RGB_235_235_242 = new Color(235, 235, 242);
	public static final Color RGB_242_242_248 = new Color(242, 242, 248);
	public static final Color RGB_245_245_248 = new Color(245, 245, 248);
	public static final Color RGB_245_245_250 = new Color(245, 245, 250);
	public static final Color RGB_250_250_252 = new Color(250, 250, 252);
	public static final Color RGB_60_60_70 = new Color(60, 60, 70);
	public static final Color RGB_80_80_90 = new Color(80, 80, 90);
	public static final Color RGB_90_90_100 = new Color(90, 90, 100);
	public static final Color RGB_90_90_105 = new Color(90, 90, 105);

	
	public static final Color TOGGLE_CHIP_BG = RGB_250_250_252;
	public static final Color TOGGLE_CHIP_HOVER_BG = PURPLE_BG_3;
	public static final Color TOGGLE_CHIP_BORDER = RGB_235_235_242;
	public static final Color TOGGLE_CHIP_SELECTED_BG = ACCENT_LAVENDER_BG_2;
	public static final Color TOGGLE_CHIP_SELECTED_BORDER = ACCENT_LAVENDER_BORDER;
	
	
	public static final Color QUICK_CHIP_BG = new Color(236, 236, 242);         
	public static final Color QUICK_CHIP_BG_HOVER = new Color(226, 226, 232); 
	public static final Color QUICK_CHIP_BG_SELECTED = new Color(210, 210, 218);
	public static final Color QUICK_CHIP_FG = TEXT;

	
	public static final Color RGBA_207_201_255_140 = new Color(207, 201, 255, 140);

	
	public static final Color ACCENT_PURPLE = new Color(0x6D4CFF);

	
	public static Color withAlpha(Color base, int alpha) {
		alpha = Math.max(0, Math.min(255, alpha));
		return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
	}

	
	
	
	public static ChipStyle chipStyle(com.creati.model.LogStatus status) {
		if (status == null) return ChipStyle.INFO;
		return switch (status) {
			case DONE -> ChipStyle.SUCCESS;
			case NEEDS_IMPROVEMENT -> ChipStyle.WARN;
			case IN_PROGRESS -> ChipStyle.INFO;
		};
	}

	public static Color chipBg(ChipStyle style) {
		if (style == null) style = ChipStyle.NEUTRAL;
		return switch (style) {
			case SUCCESS -> SUCCESS_BG;
			case INFO -> INFO_BG;
			case WARN -> WARNING_BG_2;
			case NEUTRAL -> NEUTRAL_100;
		};
	}

	public static Color chipFg(ChipStyle style) {
		if (style == null) style = ChipStyle.NEUTRAL;
		return switch (style) {
			case SUCCESS -> SUCCESS_TEXT;
			case INFO -> ACCENT_BLUE;
			case WARN -> WARN_TEXT;
			case NEUTRAL -> TEXT_SECONDARY_DARK;
		};
	}

	public static Color chipBg(com.creati.model.LogStatus status) {
		return chipBg(chipStyle(status));
	}

	public static Color chipFg(com.creati.model.LogStatus status) {
		return chipFg(chipStyle(status));
	}

	
	public static Color chipBgMeta() { return PURPLE_BG_3; }
	public static Color chipFgMeta() { return PURPLE_DARK; }
	public static Color chipBgGrey() { return NEUTRAL_100; }
	public static Color chipFgGrey() { return TEXT_SECONDARY_DARK; }

	
	
	
	
	
	
	public enum DetailChipStyle {
		META,      
		INFO,      
		GOOD,      
		FACTOR,    
		NEUTRAL    
	}

	
	private static final Color D_PURPLE_META   = new Color(0xF3F1FF); 
	private static final Color D_PURPLE_INFO   = new Color(0xEAE6FF); 
	private static final Color D_PURPLE_GOOD   = new Color(0xDDD6FE); 
	private static final Color D_PURPLE_FACTOR = new Color(0xE0E7FF); 
	private static final Color D_PURPLE_NEUTRAL= new Color(0xF5F3FF); 

	public static Color detailChipBg(DetailChipStyle style) {
		if (style == null) style = DetailChipStyle.NEUTRAL;
		return switch (style) {
			case META -> D_PURPLE_META;
			case INFO -> D_PURPLE_INFO;
			case GOOD -> D_PURPLE_GOOD;
			case FACTOR -> D_PURPLE_FACTOR;
			case NEUTRAL -> D_PURPLE_NEUTRAL;
		};
	}

	
	public static Color detailChipFg(DetailChipStyle style) {
		return BLACK;
	}

	
	public static Color detailChipBgMeta() { return detailChipBg(DetailChipStyle.META); }
	public static Color detailChipFgMeta() { return detailChipFg(DetailChipStyle.META); }
	public static Color detailChipBgGrey() { return detailChipBg(DetailChipStyle.NEUTRAL); }
	public static Color detailChipFgGrey() { return detailChipFg(DetailChipStyle.NEUTRAL); }
	
	
	public static Font H2;
	public static Font BODY;
	public static Font BODY_MED;
	public static Font BODY_SM;
	public static Font TITLE_SM;
	public static Font CAPTION;

	private static boolean inited = false;

	public static void init() {
		if (inited)
			return;

		H2 = FontKit.esamanruBold(26f);
		BODY = FontKit.regular(14f);
		BODY_MED = FontKit.medium(14f);
		BODY_SM = FontKit.regular(12.5f);
		TITLE_SM = FontKit.semiBold(15f);
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
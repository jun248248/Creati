package com.creati.ui.components;


import com.creati.util.UITheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ShadowLabel extends JLabel {
	private final int shadowAlpha;
	private final Color shadowBase;

	public ShadowLabel(String text, int shadowAlpha, Color shadowBase) {
		super(text);
		this.shadowAlpha = shadowAlpha;
		this.shadowBase = shadowBase;
		setOpaque(false);
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		g2.setFont(getFont());
		FontMetrics fm = g2.getFontMetrics();
		int x = getInsets().left;
		int y = getInsets().top + fm.getAscent();

		g2.setColor(UITheme.withAlpha(shadowBase, shadowAlpha));
		g2.drawString(getText(), x + 1, y + 1);

		g2.setColor(getForeground());
		g2.drawString(getText(), x, y);

		g2.dispose();
	}
}

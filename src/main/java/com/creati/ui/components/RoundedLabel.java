package com.creati.ui.components;


import com.creati.util.UITheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class RoundedLabel extends JLabel {
	private int arc = 16;
	private Color bg = UITheme.WHITE;
	private Color border = null;

	public RoundedLabel(String text) {
		super(text);
		setOpaque(false);
		setBorder(new EmptyBorder(5, 10, 5, 10));
	}

	public RoundedLabel arc(int arc) {
		this.arc = arc;
		return this;
	}

	public RoundedLabel bg(Color bg) {
		this.bg = bg;
		return this;
	}

	public RoundedLabel border(Color border) {
		this.border = border;
		return this;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int w = getWidth();
		int h = getHeight();

		g2.setColor(bg);
		g2.fillRoundRect(0, 0, w, h, arc, arc);

		if (border != null) {
			g2.setColor(border);
			g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
		}

		g2.dispose();
		super.paintComponent(g);
	}
}

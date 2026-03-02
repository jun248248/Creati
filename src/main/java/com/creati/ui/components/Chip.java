package com.creati.ui.components;

import com.creati.util.UITheme;

import javax.swing.*;
import java.awt.*;


public class Chip extends JComponent {

	private String text = "";
	private Color bg = UITheme.HOVER_BG;
	private Color fg = UITheme.TEXT_STRONG;

	private int padX = 12;
	private int padY = 5;
	private int minH = 24;

	public Chip() {
	    setOpaque(false);
	    setFont(UITheme.CAPTION);
	    setAlignmentY(0.5f); 
	}

	public void setChip(String text, Color bg, Color fg) {
		this.text = (text == null) ? "" : text;
		if (bg != null) this.bg = bg;
		if (fg != null) this.fg = fg;
		revalidate();
		repaint();
	}

	
	public void setSizing(int padX, int padY, int minH) {
		this.padX = Math.max(0, padX);
		this.padY = Math.max(0, padY);
		this.minH = Math.max(0, minH);
		revalidate();
		repaint();
	}

	@Override
	public Dimension getPreferredSize() {
		FontMetrics fm = getFontMetrics(getFont());
		int w = padX * 2 + fm.stringWidth(text);
		int h = Math.max(minH, padY * 2 + fm.getHeight());
		return new Dimension(w, h);
	}

	@Override
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		int w = getWidth();
		int h = getHeight();
		int arc = h;

		g2.setColor(bg);
		g2.fillRoundRect(0, 0, w, h, arc, arc);

		g2.setFont(getFont());
		FontMetrics fm = g2.getFontMetrics();
		int tx = padX;
		int ty = (h - fm.getHeight()) / 2 + fm.getAscent();

		g2.setColor(fg);
		g2.drawString(text, tx, ty);
		g2.dispose();
	}
}

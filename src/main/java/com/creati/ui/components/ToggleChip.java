package com.creati.ui.components;

import com.creati.ui.main.MainUiParts;
import com.creati.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


public class ToggleChip extends JToggleButton {

	private boolean hover = false;

	public ToggleChip(String text, Icon icon) {
		super(text);
		setIcon(icon);
		setHorizontalAlignment(SwingConstants.LEFT);
		setHorizontalTextPosition(SwingConstants.RIGHT);
		setIconTextGap(8);

		setFont(UITheme.BODY);
		setForeground(UITheme.TEXT);

		setOpaque(false);
		setContentAreaFilled(false);
		setFocusPainted(false);
		setBorder(new EmptyBorder(8, 12, 8, 12));
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		addChangeListener(e -> repaint());
		addMouseListener(new java.awt.event.MouseAdapter() {
			@Override public void mouseEntered(java.awt.event.MouseEvent e) { hover = true; repaint(); }
			@Override public void mouseExited(java.awt.event.MouseEvent e) { hover = false; repaint(); }
		});
	}

	public boolean isHover() { return hover; }

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		boolean selected = isSelected();
		Color bg = MainUiParts.toggleChipBg(selected, hover);
		Color border = MainUiParts.toggleChipBorder(selected, hover);

		int arc = MainUiParts.TOGGLE_CHIP_RADIUS;
		g2.setColor(bg);
		g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

		g2.setColor(border);
		g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

		g2.dispose();
		super.paintComponent(g);
	}
}

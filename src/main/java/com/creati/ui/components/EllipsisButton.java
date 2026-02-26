package com.creati.ui.components;

import java.awt.*;
import javax.swing.*;
import com.creati.util.FontKit;
import com.creati.util.UITheme;

import javax.swing.border.EmptyBorder;

public class EllipsisButton extends JButton {
	public EllipsisButton() {
		super("● ● ●");
		setFocusPainted(false);
		setContentAreaFilled(false);
		setBorderPainted(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setForeground(UITheme.MUTED_TEXT);
		setFont(FontKit.bold(14f));
		setPreferredSize(new Dimension(52, 32));
		setHorizontalAlignment(SwingConstants.CENTER);
	}
}

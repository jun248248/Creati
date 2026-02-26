package com.creati.ui.components;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class RoundedButton extends JButton {
    private int arc = 18;
    private boolean pill = false;

    public RoundedButton(String text) {
        super(text);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorder(new EmptyBorder(10, 14, 10, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setOpaque(false);
    }

    public void setCornerRadius(int arc) {
        this.arc = Math.max(0, arc);
        repaint();
    }

    public void setPill(boolean pill) {
        this.pill = pill;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int r = pill ? getHeight() : arc;
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), r, r);

        super.paintComponent(g2);
        g2.dispose();
    }
}
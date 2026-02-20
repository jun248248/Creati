package com.creati.ui.auth;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.creati.util.UITheme;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 관심분야 태그 입력 컴포넌트 (UI 전용)
 * - 선택한 태그를 칩 형태로 표시
 * - 직접 입력 가능 (#내용 입력 후 Enter)
 * - 최대 3개까지 입력 가능
 */
public class TagInput extends JPanel {

    private final Set<String> tags = new LinkedHashSet<>();
    private final JTextField input = new JTextField();
    private String placeholder = "";
    private boolean showingPlaceholder = true;

    private final int fixedW;
    private final int fixedH;

    public TagInput(int width, int height) {
        super(null);
        this.fixedW = width;
        this.fixedH = height;

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        setPreferredSize(new Dimension(fixedW, fixedH));
        setMinimumSize(new Dimension(fixedW, fixedH));
        setMaximumSize(new Dimension(fixedW, fixedH));

        input.setBorder(BorderFactory.createEmptyBorder());
        input.setOpaque(false);
        input.setFont(UITheme.BODY);

        input.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (showingPlaceholder && tags.isEmpty()) {
                    showingPlaceholder = false;
                    refresh();
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (input.getText().isEmpty() && tags.isEmpty()) {
                    showingPlaceholder = true;
                    refresh();
                }
            }
        });

        input.addActionListener(e -> {
            if (tags.size() >= 3) {
                JOptionPane.showMessageDialog(
                        SwingUtilities.getWindowAncestor(this),
                        "관심분야는 최대 3개까지만 선택 가능합니다.",
                        "알림",
                        JOptionPane.WARNING_MESSAGE
                );
                input.setText("");
                return;
            }

            String raw = input.getText().trim();
            if (raw.isEmpty()) return;

            String v = raw.startsWith("#") ? raw.substring(1).trim() : raw.trim();
            if (v.isEmpty()) return;

            addTag(v);
            input.setText("");
        });

        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE &&
                        input.getText().isEmpty() && !tags.isEmpty()) {
                    ArrayList<String> list = new ArrayList<>(tags);
                    String last = list.get(list.size() - 1);
                    tags.remove(last);
                    refresh();
                }
            }
        });

        add(input);
        refresh();
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder == null ? "" : placeholder;
        refresh();
    }

    public Set<String> getTags() {
        return new LinkedHashSet<>(tags);
    }

    public void addTag(String tag) {
        if (tag == null) return;
        String v = tag.trim();
        if (v.isEmpty()) return;
        if (tags.size() >= 3) return;

        tags.add(v);
        refresh();
    }

    private void refresh() {
        for (Component c : getComponents()) {
            if (c != input) remove(c);
        }

        int padding = 5;
        int chipH = 28;
        int x = padding;
        int y = (fixedH - chipH) / 2;

        if (showingPlaceholder && tags.isEmpty() &&
                input.getText().isEmpty() && !placeholder.isEmpty()) {
            JLabel hint = new JLabel(placeholder);
            hint.setFont(UITheme.BODY);
            hint.setForeground(new Color(150, 150, 160));
            hint.setBounds(x, y, 220, chipH);
            add(hint);
        } else {
            for (String t : tags) {
                JPanel chip = chip(t);
                Dimension chipSize = chip.getPreferredSize();
                chip.setBounds(x, y, chipSize.width, chipH);
                add(chip);
                x += chipSize.width + 4;
            }
        }

        int inputW = Math.max(80, fixedW - x - padding);
        input.setBounds(x, y, inputW, chipH);

        revalidate();
        repaint();
    }

    private JPanel chip(String text) {
        JPanel chip = new JPanel();
        chip.setOpaque(true);
        chip.setBackground(new Color(0xFFE474));
        chip.setBorder(new EmptyBorder(2, 6, 2, 4));
        chip.setLayout(new BoxLayout(chip, BoxLayout.X_AXIS));

        JLabel l = new JLabel("#" + text);
        l.setFont(UITheme.BODY);
        l.setForeground(new Color(60, 60, 70));

        JButton x = new JButton("×");
        x.setFont(UITheme.BODY);
        x.setFocusPainted(false);
        x.setBorderPainted(false);
        x.setContentAreaFilled(false);
        x.setForeground(new Color(100, 100, 110));
        x.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        x.addActionListener(e -> {
            tags.remove(text);
            refresh();
        });

        chip.add(l);
        chip.add(Box.createHorizontalStrut(2));
        chip.add(x);
        return chip;
    }
}

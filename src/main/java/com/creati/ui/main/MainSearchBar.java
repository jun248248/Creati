package com.creati.ui.main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.creati.util.FontKit;
import com.creati.util.UITheme;

import java.awt.*;
import java.util.function.Consumer;


public class MainSearchBar extends JPanel {

    private final JTextField field = new JTextField();
    private Consumer<String> onSearch = s -> {};

    
    private final Timer debounceTimer;
    private static final int DEBOUNCE_MS = 180;

    public MainSearchBar() {
        UITheme.ensureInit();
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));

        
        JPanel inputWrap = new JPanel(new BorderLayout(10, 0));
        inputWrap.setBackground(UITheme.WHITE);
        inputWrap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.RGB_235_235_240, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel icon = new JLabel(makeSearchIconText());
        icon.setForeground(UITheme.RGB_130_130_145);
        icon.setFont(getMaterialIconFontOrFallback(18f));

        field.setBorder(BorderFactory.createEmptyBorder());
        field.setFont(UITheme.BODY);
        field.setForeground(UITheme.TEXT);
        field.setBackground(UITheme.WHITE);
        field.setCaretColor(UITheme.TEXT);

        
        field.addActionListener(e -> fireSearchNow());

        inputWrap.add(icon, BorderLayout.WEST);
        inputWrap.add(field, BorderLayout.CENTER);

        add(inputWrap, BorderLayout.CENTER);

        
        debounceTimer = new Timer(DEBOUNCE_MS, e -> fireSearchNow());
        debounceTimer.setRepeats(false);

        
        field.getDocument().addDocumentListener(new DocumentListener() {
            private void changed() {
                debounceTimer.restart();

                if (field.getText().trim().isEmpty()) {
                    debounceTimer.stop();
                    onSearch.accept("");
                }
            }
            @Override public void insertUpdate(DocumentEvent e) { changed(); }
            @Override public void removeUpdate(DocumentEvent e) { changed(); }
            @Override public void changedUpdate(DocumentEvent e) { changed(); }
        });
    }

    // DB - 즉시 검색 트리거 구간 - 지금은 onSearch 콜백만 호출 
    private void fireSearchNow() {
        String q = field.getText().trim();
        onSearch.accept(q);
    }

    public void setOnSearch(Consumer<String> c) {
        this.onSearch = (c == null) ? (s -> {}) : c;
    }

    public void setQuery(String text) {
        field.setText(text == null ? "" : text);
    }

    public String getQuery() {
        return field.getText().trim();
    }

    public void clear() {
        setQuery("");
        onSearch.accept(""); 
    }

    
    
    

    private String makeSearchIconText() {
        try {
            return new String(Character.toChars(0xE8B6));
        } catch (Exception e) {
            return "🔎";
        }
    }

    private Font getMaterialIconFontOrFallback(float size) {
        try {
            Font f = FontKit.materialIcon(size); 
            return (f != null) ? f : UITheme.BODY;
        } catch (Exception e) {
            return UITheme.BODY;
        }
    }
}

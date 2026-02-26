package com.creati;

import com.formdev.flatlaf.intellijthemes.FlatArcIJTheme;

import com.creati.ui.auth.AuthFrame;
import com.creati.util.FontKit;
import com.creati.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.Enumeration;
//test_junil
public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // 룩앤필 설정
            FlatArcIJTheme.setup();

            // 폰트 로드
            FontKit.init();

            // UI 테마 초기화
            UITheme.ensureInit();

            // 기본 폰트 적용
            setUIFont(FontKit.regular(14f));

            // 라운드 옵션
            UIManager.put("Component.arc", 18);
            UIManager.put("Button.arc", 18);
            UIManager.put("TextComponent.arc", 14);

            new AuthFrame().setVisible(true);
        });
    }

    private static void setUIFont(Font font) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof Font) {
                UIManager.put(key, font);
            }
        }
    }
}

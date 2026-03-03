package com.creati;

import com.formdev.flatlaf.intellijthemes.FlatArcIJTheme;

import com.creati.ui.auth.AuthFrame;
import com.creati.util.FontKit;
import com.creati.util.UITheme;

import javafx.application.Platform;
import javax.swing.*;
import java.awt.*;
import java.util.Enumeration;

public class App {
    public static void main(String[] args) {
        // JavaFX Platform이 마지막 창 닫힘 시 자동 종료되지 않도록 설정
        // → 로그아웃 후 새 AuthFrame을 열어도 VideoPanel이 정상 재생됨
        new javafx.embed.swing.JFXPanel(); // FX 툴킷 초기화
        Platform.setImplicitExit(false);

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
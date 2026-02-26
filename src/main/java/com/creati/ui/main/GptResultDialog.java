package com.creati.ui.main;

import com.creati.util.FontKit;
import com.creati.util.UITheme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 가독성을 개선한 AI 분석 결과 다이얼로그
 * - 특수문자 최소화 및 여백 최적화
 */
public class GptResultDialog extends JDialog {

    public GptResultDialog(JFrame owner, String title, String content) {
        super(owner, "에티의 AI 분석 리포트", true);
        UITheme.ensureInit();
        
        setSize(520, 650);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UITheme.BG);
        
        setLayout(new BorderLayout());
        add(buildRootPanel(title, content), BorderLayout.CENTER);
    }

    private JPanel buildRootPanel(String title, String content) {
        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(30, 30, 30, 30)); // 전체 여백 확대

        // 1. 헤더 영역
        JLabel lblTitle = new JLabel(title);
        // 타이틀이 있으면 표시, 없으면 기본 문구
        lblTitle.setFont(UITheme.H2 != null ? UITheme.H2.deriveFont(22f) : FontKit.bold(22f));
        lblTitle.setForeground(UITheme.ACCENT_PURPLE);
        root.add(lblTitle, BorderLayout.NORTH);

        // 2. 중앙 분석 결과 카드
        JPanel contentCard = new JPanel(new BorderLayout());
        contentCard.setBackground(Color.WHITE);
        contentCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(235, 235, 240), 1, true),
                new EmptyBorder(25, 25, 25, 25) // 텍스트 안쪽 여백
        ));

        JTextArea textArea = new JTextArea(content);
        // 인간이 읽기 가장 편한 15~16px 크기 적용
        textArea.setFont(UITheme.BODY.deriveFont(15f)); 
        textArea.setForeground(new Color(0x333333));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setOpaque(false);

        // 스크롤 패널 설정
        textArea.setBorder(new EmptyBorder(10, 10, 10, 10)); 

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        
        // 스크롤 속도 최적화
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        
        contentCard.add(scroll, BorderLayout.CENTER);
        root.add(contentCard, BorderLayout.CENTER);

        // 3. 하단 닫기 버튼 영역
        JButton btnClose = new JButton("확인");
        btnClose.setBackground(UITheme.ACCENT_PURPLE);
        btnClose.setForeground(Color.WHITE);
        btnClose.setFont(UITheme.BODY_MED);
        btnClose.setPreferredSize(new Dimension(120, 45));
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setOpaque(false);
        btnPanel.add(btnClose);
        root.add(btnPanel, BorderLayout.SOUTH);

        return root;
    }
}
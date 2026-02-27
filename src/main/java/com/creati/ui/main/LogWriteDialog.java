package com.creati.ui.main;

import com.creati.model.Log;
import com.creati.service.LogService;
import com.creati.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 새 성장 로그 작성을 위한 입력 다이얼로그
 * - 모든 UI 보조 메서드(createLabel, styleField, styleButton) 포함 버전
 */
public class LogWriteDialog extends JDialog {

    private final LogService logService;
    
    // 입력 컴포넌트들
    private JTextField titleField;          
    private JTextField urlField;            
    private JTextArea tryContentArea;       
    private JComboBox<String> statusCombo;  
    private JCheckBox publicCheck;          
    private JCheckBox draftCheck;           

    public LogWriteDialog(JFrame owner, LogService logService) {
        super(owner, "새 성장 로그 작성", true);
        this.logService = logService;

        UITheme.ensureInit();
        setSize(500, 650);
        setLocationRelativeTo(owner);
        setResizable(false);

        setContentPane(buildRoot());
    }

    private JPanel buildRoot() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(25, 30, 25, 30));

        // 상단 헤더
        JLabel header = new JLabel("오늘의 성장 기록");
        header.setFont(UITheme.BODY_MED.deriveFont(22f));
        header.setForeground(UITheme.ACCENT_PURPLE);
        root.add(header, BorderLayout.NORTH);

        // 중앙 입력 폼
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        form.add(Box.createVerticalStrut(20));
        
        // 1. 도전 제목
        form.add(createLabel("도전 제목"));
        titleField = new JTextField();
        styleField(titleField);
        form.add(titleField);

        form.add(Box.createVerticalStrut(15));

        // 2. 유튜브 URL
        form.add(createLabel("콘텐츠 URL (유튜브)"));
        urlField = new JTextField();
        styleField(urlField);
        form.add(urlField);

        form.add(Box.createVerticalStrut(15));

        // 3. 시도 내용
        form.add(createLabel("어떤 시도를 하셨나요?"));
        tryContentArea = new JTextArea(10, 20);
        tryContentArea.setLineWrap(true);
        tryContentArea.setWrapStyleWord(true);
        tryContentArea.setFont(UITheme.BODY);
        JScrollPane scroll = new JScrollPane(tryContentArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 235)));
        form.add(scroll);

        form.add(Box.createVerticalStrut(15));

        // 4. 결과 상태 및 옵션
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        
        statusCombo = new JComboBox<>(new String[]{"진행 중", "성공", "실패"});
        statusCombo.setFont(UITheme.BODY);
        
        publicCheck = new JCheckBox("공개하기");
        draftCheck = new JCheckBox("임시저장");
        publicCheck.setOpaque(false);
        draftCheck.setOpaque(false);
        publicCheck.setFont(UITheme.BODY);
        draftCheck.setFont(UITheme.BODY);
        
        row.add(new JLabel("진행 결과: "));
        row.add(statusCombo);
        row.add(Box.createHorizontalStrut(15));
        row.add(publicCheck);
        row.add(Box.createHorizontalStrut(10));
        row.add(draftCheck);
        
        form.add(row);

        root.add(form, BorderLayout.CENTER);

        // 하단 버튼
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottom.setOpaque(false);
        
        JButton cancelBtn = new JButton("취소");
        JButton saveBtn = new JButton("기록 저장");
        
        styleButton(cancelBtn, Color.WHITE, UITheme.TEXT, true);
        styleButton(saveBtn, UITheme.ACCENT_PURPLE, Color.WHITE, false);
        
        saveBtn.addActionListener(e -> handleSave());
        cancelBtn.addActionListener(e -> dispose());

        bottom.add(cancelBtn);
        bottom.add(saveBtn);
        root.add(bottom, BorderLayout.SOUTH);

        return root;
    }

    private void handleSave() {
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "도전 제목을 입력해주세요!", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Log log = new Log();
        log.setL_title(titleField.getText().trim());
        log.setL_content_url(urlField.getText().trim());
        log.setL_try_content(tryContentArea.getText());
        log.setL_result_status(statusCombo.getSelectedItem().toString());
        log.setL_is_public(publicCheck.isSelected());
        log.setL_is_draft(draftCheck.isSelected());
        log.setU_id("test_user"); 

        try {
            logService.createLog(log);
            JOptionPane.showMessageDialog(this, "성장 로그가 저장되었습니다.");
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "저장 중 오류가 발생했습니다: " + ex.getMessage());
        }
    }

    // ==========================================
    // UI 보조 메서드
    // ==========================================

    /**
     * 라벨의 폰트와 여백을 설정합니다.
     */
    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.BODY_MED.deriveFont(14f));
        l.setBorder(new EmptyBorder(0, 0, 5, 0));
        return l;
    }

    /**
     * 입력 필드(TextField)의 스타일을 설정합니다.
     */
    private void styleField(JTextField f) {
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setPreferredSize(new Dimension(10, 40));
        f.setFont(UITheme.BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 235)),
                new EmptyBorder(0, 10, 0, 10)
        ));
    }

    /**
     * 버튼의 색상과 스타일을 설정합니다.
     */
    private void styleButton(JButton b, Color bg, Color fg, boolean border) {
        b.setPreferredSize(new Dimension(100, 40));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(UITheme.BODY_MED);
        b.setFocusPainted(false);
        if (border) {
            b.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 230)));
        } else {
            b.setBorder(BorderFactory.createEmptyBorder());
        }
    }
}
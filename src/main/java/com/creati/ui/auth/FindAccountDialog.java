package com.creati.ui.auth;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.creati.util.UITheme;

import java.awt.*;

import com.creati.util.FontKit;
// DB(TODO): Wire up account recovery actions when DB is available. (아직 미구현)
public class FindAccountDialog extends JDialog {

    private final CardLayout card = new CardLayout();
    private final JPanel cardPanel = new JPanel(card);

    
    private final JLabel idResultLabel = new JLabel("");

    
    private final JTextField tfCode = new JTextField();
    private final JButton btnVerify = new JButton("확인");
    private final JPasswordField pfNew = new JPasswordField();
    private final JPasswordField pfNew2 = new JPasswordField();

    public FindAccountDialog(JFrame owner) {
        super(owner, "아이디 / 비밀번호 찾기", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(440, 560);
        setLocationRelativeTo(owner);
        setResizable(false);

        setContentPane(buildRoot());
    }

    private JPanel buildRoot() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.WHITE);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("아이디 / 비밀번호 찾기");
        title.setFont(FontKit.bold(18f));

        JLabel subtitle = new JLabel("가입 시 등록한 정보로 계정을 찾을 수 있어요.");
        subtitle.setForeground(UITheme.TEXT_SUBTLE);
        subtitle.setFont(subtitle.getFont().deriveFont(12f));

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);

        
        JToggleButton tabFindId = makeTabButton("아이디 찾기", true);
        JToggleButton tabResetPw = makeTabButton("비밀번호 재설정", false);

        styleTabOn(tabFindId);
        styleTabOff(tabResetPw);

        ButtonGroup group = new ButtonGroup();
        group.add(tabFindId);
        group.add(tabResetPw);

        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        tabs.setOpaque(false);
        tabs.add(tabFindId);
        tabs.add(tabResetPw);

        
        cardPanel.setOpaque(false);
        cardPanel.add(buildFindIdCard(), "FIND_ID");
        cardPanel.add(buildResetPwCard(), "RESET_PW");

        tabFindId.addActionListener(e -> {
            applyTabStyle(tabFindId, tabResetPw);
            card.show(cardPanel, "FIND_ID");
        });
        tabResetPw.addActionListener(e -> {
            applyTabStyle(tabResetPw, tabFindId);
            card.show(cardPanel, "RESET_PW");
        });

        JButton btnClose = new JButton("닫기");
        styleGhost(btnClose);
        btnClose.addActionListener(e -> dispose());

        JButton btnPrimary = new JButton("조회");
        stylePrimary(btnPrimary);

        tabFindId.addActionListener(e -> btnPrimary.setText("조회"));
        tabResetPw.addActionListener(e -> btnPrimary.setText("재설정"));

        
        btnPrimary.addActionListener(e -> {
            if (tabFindId.isSelected()) {
                
                // DB
                
                boolean userExists = true; // DB
                
                if (userExists) {
                    idResultLabel.setForeground(UITheme.SUCCESS_TEXT_2);
                    idResultLabel.setText("조회된 아이디는 abc123 입니다. (샘플)");
                } else {
                    idResultLabel.setForeground(UITheme.ERROR);
                    idResultLabel.setText("가입하지 않은 회원입니다.");
                }
            } else {
                
                // DB
                JOptionPane.showMessageDialog(this, "비밀번호가 재설정되었습니다. (샘플)");
                dispose();
            }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(16, 0, 0, 0));
        bottom.add(btnClose);
        bottom.add(btnPrimary);

        root.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BorderLayout());
        center.add(tabs, BorderLayout.NORTH);
        center.add(cardPanel, BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        return root;
    }

    
    
    

    private JPanel buildFindIdCard() {
        JPanel card = makeCard();

        JTextField tfPhone = new JTextField();
        JTextField tfEmail = new JTextField();

        tfPhone.setToolTipText("전화번호 (예: 010-1234-5678)");
        tfEmail.setToolTipText("이메일");

        styleField(tfPhone);
        styleField(tfEmail);

        idResultLabel.setOpaque(true);
        idResultLabel.setBackground(UITheme.SURFACE_SUBTLE);
        idResultLabel.setBorder(new EmptyBorder(10, 12, 10, 12));
        idResultLabel.setForeground(UITheme.TEXT_SUBTLE);
        idResultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(makeFieldBlock("전화번호", tfPhone));
        card.add(Box.createVerticalStrut(10));
        card.add(makeFieldBlock("이메일", tfEmail));
        card.add(Box.createVerticalStrut(16));
        card.add(idResultLabel);

        return wrapCard(card);
    }

    private JPanel buildResetPwCard() {
        JPanel innerContent = new JPanel();
        innerContent.setLayout(new BoxLayout(innerContent, BoxLayout.Y_AXIS));
        innerContent.setAlignmentX(Component.LEFT_ALIGNMENT);
        innerContent.setBackground(UITheme.WHITE);

        JTextField tfId = new JTextField();
        JTextField tfEmail = new JTextField();
        styleField(tfId);
        styleField(tfEmail);

        JButton btnReqCode = new JButton("인증번호 요청");
        styleOutline(btnReqCode);

        JPanel emailRow = new JPanel(new BorderLayout(10, 0));
        emailRow.setOpaque(false);
        emailRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        emailRow.add(tfEmail, BorderLayout.CENTER);
        emailRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailRow.add(btnReqCode, BorderLayout.EAST);

        styleField(tfCode);
        btnVerify.setPreferredSize(new Dimension(86, 38));
        styleGhost(btnVerify);

        JPanel codeRow = new JPanel(new BorderLayout(10, 0));
        codeRow.setOpaque(false);
        codeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        codeRow.add(tfCode, BorderLayout.CENTER);
        codeRow.add(btnVerify, BorderLayout.EAST);
        codeRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        styleField(pfNew);
        styleField(pfNew2);

        JLabel hint = new JLabel("인증 완료 후 새 비밀번호를 설정할 수 있어요.");
        hint.setForeground(UITheme.TEXT_SUBTLE);
        hint.setFont(hint.getFont().deriveFont(12f));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        
        tfCode.setEnabled(false);
        btnVerify.setEnabled(false);
        pfNew.setEnabled(false);
        pfNew2.setEnabled(false);

        
        btnReqCode.addActionListener(e -> {
            String id = tfId.getText().trim();
            String email = tfEmail.getText().trim();
            if (id.isEmpty()) {
                toast("아이디를 먼저 입력하세요.");
                return;
            }
            if (email.isEmpty()) {
                toast("이메일을 입력하세요.");
                return;
            }
            // DB
            tfCode.setEnabled(true);
            btnVerify.setEnabled(true);
            toast("인증번호를 이메일로 전송했습니다. (샘플)");
        });

        
        btnVerify.addActionListener(e -> {
            String code = tfCode.getText().trim();
            if (code.isEmpty()) {
                toast("인증번호를 입력하세요.");
                return;
            }
            // DB
            pfNew.setEnabled(true);
            pfNew2.setEnabled(true);
            toast("인증 완료! 새 비밀번호를 설정하세요. (샘플)");
        });

        innerContent.add(makeFieldBlock("아이디", tfId));
        innerContent.add(Box.createVerticalStrut(10));
        innerContent.add(makeLabel("이메일"));
        innerContent.add(Box.createVerticalStrut(6));
        innerContent.add(emailRow);
        innerContent.add(Box.createVerticalStrut(10));
        innerContent.add(makeLabel("인증번호"));
        innerContent.add(Box.createVerticalStrut(6));
        innerContent.add(codeRow);
        innerContent.add(Box.createVerticalStrut(8));
        innerContent.add(hint);
        innerContent.add(Box.createVerticalStrut(12));
        innerContent.add(makeFieldBlock("새 비밀번호", pfNew));
        innerContent.add(Box.createVerticalStrut(10));
        innerContent.add(makeFieldBlock("새 비밀번호 확인", pfNew2));
        innerContent.add(Box.createVerticalStrut(10));

        
        JScrollPane scrollPane = new JScrollPane(innerContent);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.DIVIDER_2, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));
        card.setPreferredSize(new Dimension(380, 270));
        card.add(scrollPane, BorderLayout.CENTER);

        return wrapCard(card);
    }

    
    
    

    private JPanel wrapCard(JPanel inner) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(12, 0, 0, 0));
        wrap.add(inner, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel makeCard() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UITheme.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.DIVIDER_2, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));
        return p;
    }

    private JPanel makeFieldBlock(String label, JComponent field) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        
        JLabel l = makeLabel(label);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        p.add(l);
        p.add(Box.createVerticalStrut(6));
        p.add(field);
        return p;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(UITheme.TEXT_SUBTLE);
        l.setFont(l.getFont().deriveFont(12f));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JToggleButton makeTabButton(String text, boolean selected) {
        JToggleButton b = new JToggleButton(text, selected);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(130, 38));
        b.setFont(FontKit.semiBold(12.5f));
return b;
    }

    private void applyTabStyle(JToggleButton on, JToggleButton off) {
        styleTabOn(on);
        styleTabOff(off);
    }

    private void styleTabOn(AbstractButton b) {
        b.setBackground(UITheme.WARNING_BG);
        b.setForeground(UITheme.WARN_TEXT_DARK);
    }

    private void styleTabOff(AbstractButton b) {
        b.setBackground(UITheme.BG_ALT);
        b.setForeground(UITheme.TEXT_STRONG);
    }

    private void stylePrimary(JButton b) {
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(FontKit.semiBold(13f));
b.setBackground(UITheme.ACCENT_PURPLE);
        b.setForeground(UITheme.WHITE);
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
        b.setPreferredSize(new Dimension(140, 44));
    }

    private void styleGhost(JButton b) {
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(FontKit.semiBold(13f));
b.setBackground(UITheme.RGB_245_245_248);
        b.setForeground(UITheme.TEXT);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.RGB_225_225_232, 1, true),
                new EmptyBorder(10, 20, 10, 20)
        ));
        b.setPreferredSize(new Dimension(140, 44));
    }

    private void styleOutline(JButton b) {
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(UITheme.WHITE);
        b.setForeground(UITheme.ACCENT_PURPLE);
        b.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT_LAVENDER_BORDER, 1, true));
        b.setPreferredSize(new Dimension(120, 38));
    }

    private void styleField(JComponent c) {
        c.setPreferredSize(new Dimension(10, 38));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.NEUTRAL_200, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        c.setFont(c.getFont().deriveFont(13f));
    }

    private void toast(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }
}

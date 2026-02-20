package com.creati.ui.auth;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.creati.util.FontKit;
import com.creati.util.UITheme;

import java.awt.*;
import java.nio.file.Path;
import java.util.Set;

public class SignupFrame extends JFrame {

    private static final Path VIDEO_PATH = Path.of("src/main/resources/videos/intro.mp4");

    private static final int LABEL_WIDTH = 110;
    private static final int FIELD_HEIGHT = 38;
    private static final int ROW_GAP = 12;
    private static final int RIGHT_W = 330;

    private final JFrame loginFrame;

    private JTextField idField;
    private JPasswordField pwField;
    private JPasswordField pw2Field;
    private JTextField nickField;
    private JTextField phoneField;

    private JComboBox<String> birthYearCombo;
    private JComboBox<String> birthMonthCombo;
    private JComboBox<String> birthDayCombo;

    private JTextField emailLocalField;
    private JTextField emailDomainField;
    private JComboBox<String> emailDomainCombo;

    private JComboBox<String> platformCombo;
    private JComboBox<String> categoryCombo;
    private TagInput tagInput;

    private JLabel msgLabel;

    public SignupFrame(JFrame loginFrame) {
        super("Creati - 회원가입");
        this.loginFrame = loginFrame;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(UITheme.BG);

        root.add(buildVideoPanel());
        root.add(buildRightPanel());

        setContentPane(root);
    }

    private JComponent buildVideoPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(20, 18, 28));

        try {
            VideoPanel video = new VideoPanel(VIDEO_PATH);
            video.setPreferredSize(new Dimension(550, 720));
            p.add(video, BorderLayout.CENTER);
        } catch (Throwable t) {
            JLabel fallback = new JLabel(
                    "<html><center><b>영상 영역</b><br/>OpenJFX 설정 필요</center></html>",
                    SwingConstants.CENTER
            );
            fallback.setForeground(Color.WHITE);
            p.add(fallback, BorderLayout.CENTER);
        }
        return p;
    }

    private JComponent buildRightPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(UITheme.BG);

        JPanel stack = new JPanel(new GridBagLayout());
        stack.setBackground(UITheme.BG);

        GridBagConstraints s = new GridBagConstraints();
        s.gridx = 0;
        s.gridy = 0;
        s.anchor = GridBagConstraints.CENTER;

        JLabel title = new JLabel("회원가입");
        title.setFont(FontKit.bold(26f));
        title.setForeground(UITheme.TEXT);

        JPanel titleWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        titleWrap.setOpaque(false);
        titleWrap.add(title);
        stack.add(titleWrap, s);

        s.gridy++;
        s.insets = new Insets(22, 0, 0, 0);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 235), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        Dimension cardSize = new Dimension(490, 520);
        card.setPreferredSize(cardSize);

        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(Color.WHITE);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 1.0;
        g.weighty = 1.0;
        g.fill = GridBagConstraints.BOTH;

        JComponent formPanel = buildFormPanel();

        JScrollPane scroll = new JScrollPane(formPanel);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        page.add(scroll, g);

        card.add(page, BorderLayout.CENTER);
        stack.add(card, s);

        outer.add(stack);
        return outer;
    }

    private JComponent buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);

        GridBagConstraints r = new GridBagConstraints();
        r.gridy = 0;
        r.insets = new Insets(0, 0, ROW_GAP, 0);
        r.anchor = GridBagConstraints.WEST;

        JLabel idLabel = label("아이디", LABEL_WIDTH);
        idField = new JTextField();
        setFieldSize(idField, new Dimension(RIGHT_W, FIELD_HEIGHT));
        addRow(form, r, idLabel, idField);

        JLabel pwLabel = label("비밀번호", LABEL_WIDTH);
        pwField = new JPasswordField();
        setFieldSize(pwField, new Dimension(RIGHT_W, FIELD_HEIGHT));
        addRow(form, r, pwLabel, pwField);

        return form;
    }

    private void addRow(JPanel form, GridBagConstraints r, JComponent left, JComponent right) {
        GridBagConstraints l = (GridBagConstraints) r.clone();
        l.gridx = 0;
        form.add(left, l);

        GridBagConstraints rr = (GridBagConstraints) r.clone();
        rr.gridx = 1;
        rr.fill = GridBagConstraints.HORIZONTAL;
        form.add(right, rr);

        r.gridy++;
    }

    private JLabel label(String text, int w) {
        JLabel l = new JLabel(text);
        l.setPreferredSize(new Dimension(w, 24));
        return l;
    }

    private void setFieldSize(JComponent c, Dimension d) {
        c.setPreferredSize(d);
    }
}

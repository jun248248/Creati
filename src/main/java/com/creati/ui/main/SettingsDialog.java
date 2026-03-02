package com.creati.ui.main;

import com.creati.model.User;
import com.creati.ui.components.RoundedButton;
import com.creati.util.FontKit;
import com.creati.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 설정 다이얼로그
 * - 회원정보 수정 (아이디/전화번호 잠금)
 * - 비밀번호 변경 (동일 다이얼로그 내 탭)
 * - 관심분야(최대 3개)
 *
 * DB(TODO): user 테이블 업데이트
 * DB(TODO): 관심분야 매핑 테이블 업데이트
 * DB(TODO): 비밀번호 변경
 */
public class SettingsDialog extends JDialog {

    private final MainFrame parent;

    // =========================
    // Profile fields
    // =========================
    private JTextField tfUserId;
    private JTextField tfPhone;
    private JTextField tfNickname;
    private JTextField tfBirth;
    private JTextField tfEmail;
    private JComboBox<String> cbPlatform;

    // =========================
    // Password fields
    // =========================
    private JPasswordField pfCurrent;
    private JPasswordField pfNew;
    private JPasswordField pfConfirm;

    // =========================
    // Interests (max 3)
    // =========================
    private JComboBox<String> cbInterest;
    private JPanel interestChipRow;
    private final List<String> selectedInterests = new ArrayList<>();

    public SettingsDialog(MainFrame parent) {
        super(parent, "설정", true);
        this.parent = Objects.requireNonNull(parent);

        UITheme.ensureInit();
        FontKit.init();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(760, 560);
        setLocationRelativeTo(parent);

        getContentPane().setBackground(UITheme.BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);

        loadFromAppState();
    }

    // =========================
    // UI: Header / Tabs / Bottom
    // =========================

    private JComponent buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(14, 18, 10, 18));

        JLabel title = new JLabel("설정");
        title.setFont(UITheme.BODY_MED);
        title.setForeground(UITheme.TEXT);

        JLabel sub = new JLabel("회원정보, 비밀번호, 관심분야(최대 3개)를 관리할 수 있어요.");
        sub.setFont(UITheme.CAPTION);
        sub.setForeground(UITheme.MUTED_TEXT);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(6));
        left.add(sub);

        p.add(left, BorderLayout.WEST);
        return p;
    }

    private JComponent buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBorder(new EmptyBorder(0, 18, 0, 18));

        tabs.addTab("회원정보", wrapScroll(buildProfileTab()));
        tabs.addTab("비밀번호", wrapScroll(buildPasswordTab()));
        tabs.addTab("관심분야", wrapScroll(buildInterestTab()));

        return tabs;
    }

    private JComponent buildBottom() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(0, 18, 6, 18));

        JButton cancel = new RoundedButton("닫기");
        styleGrey(cancel);
        cancel.addActionListener(e -> dispose());

        JButton save = new RoundedButton("저장");
        stylePrimary(save);
        save.addActionListener(e -> onSave());

        bottom.add(cancel);
        bottom.add(save);
        return bottom;
    }

    // =========================
    // Tabs
    // =========================

    private JComponent buildProfileTab() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.insets = new Insets(0, 0, 12, 0);
        c.anchor = GridBagConstraints.WEST;

        tfUserId = field(false);
        addRow(form, c, "아이디", tfUserId, true);

        tfPhone = field(false);
        addRow(form, c, "전화번호", tfPhone, true);

        tfNickname = field(true);
        addRow(form, c, "닉네임", tfNickname, false);

        tfBirth = field(true);
        tfBirth.setToolTipText("예) 2001-03-15");
        addRow(form, c, "생년월일", tfBirth, false);

        tfEmail = field(true);
        addRow(form, c, "이메일", tfEmail, false);

        cbPlatform = new JComboBox<>(new String[]{"YouTube", "Instagram", "Blog", "Brunch", "TicTok", "SoundCloud", "Other"});
        cbPlatform.setFont(UITheme.BODY);
        cbPlatform.setBackground(UITheme.WHITE);
        addRow(form, c, "주요 플랫폼", cbPlatform, false);

        card.add(form, BorderLayout.NORTH);

        JPanel hint = new JPanel(new BorderLayout());
        hint.setOpaque(false);
        hint.setBorder(new EmptyBorder(8, 0, 0, 0));

        JLabel note = new JLabel("※ 아이디/전화번호는 수정할 수 없어요.");
        note.setFont(UITheme.CAPTION);
        note.setForeground(UITheme.MUTED_TEXT);
        hint.add(note, BorderLayout.WEST);

        card.add(hint, BorderLayout.SOUTH);

        return wrapWithPadding(card);
    }

    private JComponent buildPasswordTab() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.insets = new Insets(0, 0, 12, 0);
        c.anchor = GridBagConstraints.WEST;

        pfCurrent = pwField();
        addRow(form, c, "현재 비밀번호", pfCurrent);

        pfNew = pwField();
        addRow(form, c, "새 비밀번호", pfNew);

        pfConfirm = pwField();
        addRow(form, c, "새 비밀번호 확인", pfConfirm);

        JLabel guide = new JLabel("비밀번호 변경은 선택사항이에요. 입력하지 않으면 변경되지 않아요.");
        guide.setFont(UITheme.CAPTION);
        guide.setForeground(UITheme.MUTED_TEXT);

        JPanel guideWrap = new JPanel(new BorderLayout());
        guideWrap.setOpaque(false);
        guideWrap.setBorder(new EmptyBorder(2, 0, 0, 0));
        guideWrap.add(guide, BorderLayout.WEST);

        card.add(form, BorderLayout.NORTH);
        card.add(guideWrap, BorderLayout.SOUTH);

        return wrapWithPadding(card);
    }

    private JComponent buildInterestTab() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel guide = new JLabel("관심분야는 최대 3개까지 선택할 수 있어요.");
        guide.setFont(UITheme.CAPTION);
        guide.setForeground(UITheme.MUTED_TEXT);

        cbInterest = new JComboBox<>(new String[]{
                "영상", "이미지", "글", "음악", "기타"
        });
        cbInterest.setFont(UITheme.BODY);
        cbInterest.setBackground(UITheme.WHITE);

        JButton add = new RoundedButton("추가");
        styleGrey(add);
        add.addActionListener(this::onAddInterest);

        JPanel pickRow = new JPanel(new BorderLayout(10, 0));
        pickRow.setOpaque(false);
        pickRow.add(cbInterest, BorderLayout.CENTER);
        pickRow.add(add, BorderLayout.EAST);

        top.add(guide);
        top.add(Box.createVerticalStrut(10));
        top.add(pickRow);

        interestChipRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        interestChipRow.setOpaque(false);
        interestChipRow.setBorder(new EmptyBorder(12, 0, 0, 0));

        card.add(top, BorderLayout.NORTH);
        card.add(interestChipRow, BorderLayout.CENTER);

        return wrapWithPadding(card);
    }

    // =========================
    // Actions
    // =========================

    private void onAddInterest(ActionEvent e) {
        if (selectedInterests.size() >= 3) {
            JOptionPane.showMessageDialog(this, "관심분야는 최대 3개까지 선택할 수 있어요.");
            return;
        }

        Object v = cbInterest.getSelectedItem();
        String s = (v == null) ? "" : v.toString().trim();
        if (s.isBlank()) return;

        if (selectedInterests.contains(s)) {
            JOptionPane.showMessageDialog(this, "이미 선택된 관심분야예요.");
            return;
        }

        selectedInterests.add(s);
        redrawInterestChips();
    }

    private void onRemoveInterest(String s) {
        selectedInterests.remove(s);
        redrawInterestChips();
    }

    private void redrawInterestChips() {
        interestChipRow.removeAll();
        for (String s : selectedInterests) {
            interestChipRow.add(new Chip(s, () -> onRemoveInterest(s)));
        }
        interestChipRow.revalidate();
        interestChipRow.repaint();
    }

    private void onSave() {
        User u = AppState.get().getCurrentUser();
        if (u == null) {
            JOptionPane.showMessageDialog(this, "로그인 정보가 없어요. 다시 로그인해 주세요.");
            return;
        }

        // ---- profile validate ----
        String nickname = tfNickname.getText().trim();
        if (nickname.isBlank()) {
            JOptionPane.showMessageDialog(this, "닉네임은 비워둘 수 없어요.");
            return;
        }
        if (selectedInterests.size() > 3) {
            JOptionPane.showMessageDialog(this, "관심분야는 최대 3개까지 선택할 수 있어요.");
            return;
        }
        if (!tfBirth.getText().trim().isEmpty() && !tfBirth.getText().trim().matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "생년월일 형식이 올바르지 않아요. 예) 2001-03-15");
            return;
        }
        if (!tfEmail.getText().trim().isEmpty() && !tfEmail.getText().trim().contains("@")) {
            JOptionPane.showMessageDialog(this, "이메일 형식이 올바르지 않아요.");
            return;
        }

        // ---- password validate (optional) ----
        String cur = new String(pfCurrent.getPassword()).trim();
        String nw = new String(pfNew.getPassword()).trim();
        String cf = new String(pfConfirm.getPassword()).trim();

        boolean wantsPwChange = !(cur.isBlank() && nw.isBlank() && cf.isBlank());
        if (wantsPwChange) {
            if (cur.isBlank() || nw.isBlank() || cf.isBlank()) {
                JOptionPane.showMessageDialog(this, "비밀번호 변경을 하려면 3개 항목을 모두 입력해 주세요.");
                return;
            }
            if (!nw.equals(cf)) {
                JOptionPane.showMessageDialog(this, "새 비밀번호가 일치하지 않아요.");
                return;
            }
            if (nw.length() < 6) {
                JOptionPane.showMessageDialog(this, "비밀번호는 6자 이상을 권장해요.");
                return;
            }
        }

        // ---- apply to user (local) ----
        UserReflect.set(u, nickname, "setNickname", "setNick", "setName");
        UserReflect.set(u, tfBirth.getText().trim(), "setBirth", "setBirthDate", "setBirthday");
        UserReflect.set(u, tfEmail.getText().trim(), "setEmail", "setMail");
        UserReflect.set(u, Objects.toString(cbPlatform.getSelectedItem(), ""), "setMainPlatform", "setPlatform", "setPrimaryPlatform");

        // 관심분야 (max 3)
        // DB(TODO): user_interest 같은 매핑테이블 upsert
        UserReflect.set(u, new ArrayList<>(selectedInterests), "setInterests", "setInterestList", "setInterestNames");

        // 비밀번호 변경
        if (wantsPwChange) {
            // DB(TODO): 현재 비밀번호 검증 + 변경 처리
            // 예: userDao.updatePassword(userId, cur, nw);
        }

        AppState.get().setCurrentUser(u);
        parent.refreshHeaderUser();

        JOptionPane.showMessageDialog(this,
                wantsPwChange
                        ? "저장됐어요! (비밀번호 변경/DB 연동은 TODO)"
                        : "저장됐어요! (DB 연동은 TODO)");
        dispose();
    }

    // =========================
    // Load initial state
    // =========================

    private void loadFromAppState() {
        User u = AppState.get().getCurrentUser();
        if (u == null) return;

        tfUserId.setText(safe(u.getId()));
        tfPhone.setText(UserReflect.getString(u, "getPhone", "getPhoneNumber", "getMobile"));

        tfNickname.setText(nonBlankOr(UserReflect.getString(u, "getNickname", "getNick", "getName"), ""));
        tfBirth.setText(UserReflect.getString(u, "getBirth", "getBirthDate", "getBirthday"));
        tfEmail.setText(UserReflect.getString(u, "getEmail", "getMail"));

        String platform = UserReflect.getString(u, "getMainPlatform", "getPlatform", "getPrimaryPlatform");
        if (!platform.isBlank()) cbPlatform.setSelectedItem(platform);

        List<String> interests = UserReflect.getStringList(u, "getInterests", "getInterestList", "getInterestNames");
        selectedInterests.clear();
        if (interests != null) {
            for (String s : interests) {
                if (s == null) continue;
                String t = s.trim();
                if (!t.isEmpty() && selectedInterests.size() < 3 && !selectedInterests.contains(t)) {
                    selectedInterests.add(t);
                }
            }
        }
        redrawInterestChips();
    }

    // =========================
    // UI helpers
    // =========================

    private JComponent wrapScroll(JComponent inner) {
        JScrollPane sp = new JScrollPane(inner);
        sp.setBorder(null);
        sp.getViewport().setOpaque(false);
        sp.setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private JPanel wrapWithPadding(JComponent card) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(12, 0, 12, 0));
        wrap.add(card, BorderLayout.NORTH);
        return wrap;
    }

    private JPanel makeCard() {
        JPanel p = new JPanel();
        p.setBackground(UITheme.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.RGB_230_230_235, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));
        return p;
    }

    private JTextField field(boolean editable) {
        JTextField tf = new JTextField();
        tf.setFont(UITheme.BODY);
        tf.setBackground(UITheme.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.RGB_230_230_235, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        tf.setEditable(editable);
        if (!editable) tf.setBackground(UITheme.RGB_245_245_248);
        return tf;
    }

    private JPasswordField pwField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(UITheme.BODY);
        pf.setBackground(UITheme.WHITE);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.RGB_230_230_235, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        return pf;
    }

    private void addRow(JPanel form, GridBagConstraints c, String label, JComponent input, boolean locked) {
        JLabel l = new JLabel(label);
        l.setFont(UITheme.CAPTION);
        l.setForeground(UITheme.MUTED_TEXT);

        // label
        c.gridx = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 0, 6, 10);
        form.add(l, c);

        // input
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 6, 0);
        form.add(input, c);

        if (locked && input instanceof JTextField tf) {
            tf.setToolTipText("수정할 수 없어요.");
        }

        c.gridy++;
        c.insets = new Insets(0, 0, 12, 0);
    }

    private void addRow(JPanel form, GridBagConstraints c, String label, JComponent input) {
        addRow(form, c, label, input, false);
    }

    private void styleGrey(AbstractButton b) {
        b.setBackground(UITheme.QUICK_CHIP_BG);
        b.setForeground(UITheme.QUICK_CHIP_FG);
        b.setBorder(new EmptyBorder(10, 14, 10, 14));
        b.setFocusPainted(false);

        if (b instanceof RoundedButton) {
            b.setOpaque(false);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
        }
    }

    private void stylePrimary(AbstractButton b) {
        b.setBackground(UITheme.ACCENT_PURPLE);
        b.setForeground(Color.WHITE);
        b.setBorder(new EmptyBorder(10, 14, 10, 14));
        b.setFocusPainted(false);

        if (b instanceof RoundedButton) {
            b.setOpaque(false);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
        }
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }

    private String nonBlankOr(String v, String fallback) {
        if (v == null) return fallback;
        return v.isBlank() ? fallback : v;
    }

    // =========================
    // Chip component
    // =========================
    private static class Chip extends JPanel {
        Chip(String text, Runnable onRemove) {
            setOpaque(true);
            setBackground(UITheme.RGB_245_245_248);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.RGB_230_230_235, 1, true),
                    new EmptyBorder(6, 10, 6, 8)
            ));
            setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

            JLabel t = new JLabel(text);
            t.setFont(UITheme.CAPTION);
            t.setForeground(UITheme.TEXT);

            JButton x = new JButton("×");
            x.setFont(UITheme.BODY_MED);
            x.setForeground(UITheme.MUTED_TEXT);
            x.setBorder(null);
            x.setContentAreaFilled(false);
            x.setFocusPainted(false);
            x.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            x.addActionListener(e -> onRemove.run());

            add(t);
            add(x);
        }
    }

    // =========================
    // Reflection helper 
    // =========================
    static final class UserReflect {
        static String getString(Object target, String... getters) {
            if (target == null) return "";
            for (String m : getters) {
                try {
                    Object v = target.getClass().getMethod(m).invoke(target);
                    if (v == null) continue;
                    return v.toString().trim();
                } catch (Exception ignore) {}
            }
            return "";
        }

        static List<String> getStringList(Object target, String... getters) {
            if (target == null) return null;
            for (String m : getters) {
                try {
                    Object v = target.getClass().getMethod(m).invoke(target);
                    if (v instanceof List<?> list) {
                        List<String> out = new ArrayList<>();
                        for (Object o : list) if (o != null) out.add(o.toString());
                        return out;
                    }
                } catch (Exception ignore) {}
            }
            return null;
        }

        static void set(Object target, Object value, String... setters) {
            if (target == null) return;
            for (String m : setters) {
                try {
                    for (var mm : target.getClass().getMethods()) {
                        if (!mm.getName().equals(m)) continue;
                        if (mm.getParameterCount() != 1) continue;

                        Class<?> p = mm.getParameterTypes()[0];
                        if (value == null) { mm.invoke(target, new Object[]{null}); return; }

                        if (p == String.class) { mm.invoke(target, value.toString()); return; }
                        if (List.class.isAssignableFrom(p) && value instanceof List<?>) { mm.invoke(target, value); return; }

                        mm.invoke(target, value);
                        return;
                    }
                } catch (Exception ignore) {}
            }
        }
    }
}
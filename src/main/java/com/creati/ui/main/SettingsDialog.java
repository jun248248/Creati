package com.creati.ui.main;

import com.creati.model.User;
import com.creati.dao.UserDao;
import com.creati.ui.components.RoundedButton;
import com.creati.util.FontKit;
import com.creati.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.*;
import java.util.List;

/**
 * 설정 다이얼로그 (DB 연동 쉽게 정리 버전)
 *
 * 역할:
 *  - UI 입력 수집/검증
 *  - SettingsUpdateRequest 생성
 *  - SettingsService로 저장 요청 (DB 붙일 때 서비스 구현만 교체)
 *
 * DB(TODO): SettingsService 구현(DbSettingsService)에서
 *  - users 테이블 UPDATE
 *  - user_interest 매핑 테이블 replace(DELETE + INSERT) (트랜잭션)
 *  - 비밀번호 변경 (현재 비번 검증 포함)
 */
public class SettingsDialog extends JDialog {

    private final MainFrame parent;
    private final SettingsService settingsService;

    // =========================
    // Profile fields
    // =========================
    private JTextField tfUserId;
    private JTextField tfPhone;
    private JTextField tfNickname;
    private JTextField tfBirth;   // YYYY-MM-DD
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
    private JButton btnAddInterest;
    private JPanel interestChipRow;
    private final List<String> selectedInterestNames = new ArrayList<>();

    private final Map<String, Long> interestNameToId = new LinkedHashMap<>();

    public SettingsDialog(MainFrame parent) {
        this(parent, new DbSettingsService()); 
    }

    public SettingsDialog(MainFrame parent, SettingsService settingsService) {
        super(parent, "설정", true);
        this.parent = Objects.requireNonNull(parent);
        this.settingsService = Objects.requireNonNull(settingsService);

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

        cbPlatform = new JComboBox<>(new String[]{
                "선택",
                "YouTube", "Instagram", "Blog", "Brunch", "TikTok", "SoundCloud", "Other"
        });
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
                "선택"
        });
        cbInterest.setFont(UITheme.BODY);
        cbInterest.setBackground(UITheme.WHITE);

        loadInterestOptions(); 

        btnAddInterest = new RoundedButton("추가");
        styleGrey(btnAddInterest);
        btnAddInterest.addActionListener(this::onAddInterest);

        JPanel pickRow = new JPanel(new BorderLayout(10, 0));
        pickRow.setOpaque(false);
        pickRow.add(cbInterest, BorderLayout.CENTER);
        pickRow.add(btnAddInterest, BorderLayout.EAST);

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
        if (selectedInterestNames.size() >= 3) {
            JOptionPane.showMessageDialog(this, "관심분야는 최대 3개까지 선택할 수 있어요.");
            return;
        }

        Object v = cbInterest.getSelectedItem();
        String s = (v == null) ? "" : v.toString().trim();
        if (s.isBlank() || "선택".equals(s)) return;

        if (selectedInterestNames.contains(s)) {
            JOptionPane.showMessageDialog(this, "이미 선택된 관심분야예요.");
            return;
        }

        selectedInterestNames.add(s);
        redrawInterestChips();
        updateInterestAddState();
    }

    private void onRemoveInterest(String s) {
        selectedInterestNames.remove(s);
        redrawInterestChips();
        updateInterestAddState();
    }

    private void redrawInterestChips() {
        interestChipRow.removeAll();
        for (String s : selectedInterestNames) {
            interestChipRow.add(new Chip(s, () -> onRemoveInterest(s)));
        }
        interestChipRow.revalidate();
        interestChipRow.repaint();
    }

    private void updateInterestAddState() {
        if (btnAddInterest == null) return;
        btnAddInterest.setEnabled(selectedInterestNames.size() < 3);
        btnAddInterest.setToolTipText(
                selectedInterestNames.size() < 3 ? null : "관심분야는 최대 3개까지 선택할 수 있어요."
        );
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

        String email = tfEmail.getText().trim();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "이메일은 필수 입력이에요.");
            return;
        }
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            JOptionPane.showMessageDialog(this, "이메일 형식이 올바르지 않아요.");
            return;
        }

        String birthText = tfBirth.getText().trim();
        if (birthText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "생년월일은 필수 입력이에요.");
            return;
        }
        LocalDate birth;
        try {
            birth = LocalDate.parse(birthText);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "생년월일이 올바르지 않아요. 예) 2001-03-15");
            return;
        }

        String platform = Objects.toString(cbPlatform.getSelectedItem(), "").trim();
        if (platform.isEmpty() || "선택".equals(platform)) {
            JOptionPane.showMessageDialog(this, "주요 플랫폼은 필수 선택이에요.");
            return;
        }

        if (selectedInterestNames.size() > 3) {
            JOptionPane.showMessageDialog(this, "관심분야는 최대 3개까지 선택할 수 있어요.");
            return;
        }

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

        SettingsUpdateRequest req = new SettingsUpdateRequest(
                u.getId(),
                nickname,
                email,
                birth,
                platform,
                convertTagsToIds(selectedInterestNames),
                wantsPwChange ? cur : null,
                wantsPwChange ? nw : null
        );
        req.interestNames().addAll(selectedInterestNames);

        SettingsSaveResult result = settingsService.save(req);

        if (!result.ok) {
            JOptionPane.showMessageDialog(this, result.message == null ? "저장에 실패했어요." : result.message);
            return;
        }

        User updated = result.updatedUser != null ? result.updatedUser : u;
        AppState.get().setCurrentUser(updated);
        parent.refreshHeaderUser();

        pfCurrent.setText("");
        pfNew.setText("");
        pfConfirm.setText("");

        JOptionPane.showMessageDialog(this, "저장됐어요!");
        dispose();
    }

    private List<Long> convertTagsToIds(List<String> tags) {
        List<Long> ids = new ArrayList<>();
        if (tags == null) return ids;
        for (String tag : tags) {
            if (tag == null) continue;
            Long id = interestNameToId.get(tag.trim());
            if (id != null) ids.add(id);
        }
        LinkedHashSet<Long> uniq = new LinkedHashSet<>(ids);
        return new ArrayList<>(uniq);
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

        loadInterestOptions();

        List<String> dbInterests = loadInterestsFromDb(u.getId());

        List<String> interests = (dbInterests != null && !dbInterests.isEmpty())
                ? dbInterests
                : UserReflect.getStringList(u, "getInterests", "getInterestList", "getInterestNames");
        selectedInterestNames.clear();
        if (interests != null) {
            for (String s : interests) {
                if (s == null) continue;
                String t = s.trim();
                if (!t.isEmpty() && selectedInterestNames.size() < 3 && !selectedInterestNames.contains(t)) {
                    selectedInterestNames.add(t);
                }
            }
        }
        redrawInterestChips();
        updateInterestAddState();
    }

    private void loadInterestOptions() {
        if (cbInterest == null) return;
        try {
            UserDao dao = new UserDao();
            LinkedHashMap<Long, String> map = dao.findAllInterests();
            if (map == null || map.isEmpty()) return;

            interestNameToId.clear();
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            model.addElement("선택");
            for (Map.Entry<Long, String> e : map.entrySet()) {
                if (e.getKey() == null) continue;
                String name = (e.getValue() == null) ? "" : e.getValue().trim();
                if (name.isEmpty()) continue;
                interestNameToId.put(name, e.getKey());
                model.addElement(name);
            }
            cbInterest.setModel(model);
        } catch (Exception ignore) {
        }
    }

    private List<String> loadInterestsFromDb(String userId) {
        if (userId == null || userId.isBlank()) return List.of();
        try {
            UserDao dao = new UserDao();
            return dao.findInterestNamesByUserId(userId);
        } catch (Exception ignore) {
            return List.of();
        }
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
    // Service boundary
    // =========================

    public interface SettingsService {
        SettingsSaveResult save(SettingsUpdateRequest req);
    }

    public static class DbSettingsService implements SettingsService {
        private final UserDao userDao = new UserDao();

        @Override
        public SettingsSaveResult save(SettingsUpdateRequest req) {
            if (req == null || req.userId == null || req.userId.isBlank()) {
                return SettingsSaveResult.fail("로그인 정보가 없어요. 다시 로그인해 주세요.");
            }

            boolean ok = userDao.updateSettings(
                    req.userId,
                    req.nickname,
                    req.email,
                    req.birth,
                    req.platform,
                    req.interestIds,
                    req.currentPassword,
                    req.newPassword
            );

            if (!ok) {
                return SettingsSaveResult.fail("저장에 실패했어요. (현재 비밀번호 또는 DB 오류)");
            }

            User u = AppState.get().getCurrentUser();
            if (u != null && req.userId.equals(u.getId())) {
                User updated = new User(
                        u.getId(),
                        u.getPassword(),
                        req.nickname,
                        u.getProfileResPath()
                );
                AppState.get().setCurrentUser(updated);
                return SettingsSaveResult.ok(updated);
            }

            return SettingsSaveResult.ok(u);
        }
    }

    public static class InMemorySettingsService implements SettingsService {
        @Override
        public SettingsSaveResult save(SettingsUpdateRequest req) {
            User u = AppState.get().getCurrentUser();
            if (u == null || u.getId() == null || !u.getId().equals(req.userId)) {
                return SettingsSaveResult.fail("로그인 정보가 없어요. 다시 로그인해 주세요.");
            }

            // ---- local apply ----
            UserReflect.set(u, req.nickname, "setNickname", "setNick", "setName");
            UserReflect.set(u, req.email, "setEmail", "setMail");

            if (req.birth == null) {
                UserReflect.set(u, null, "setBirth", "setBirthDate", "setBirthday");
            } else {
                boolean ok = UserReflect.setIfMatches(u, req.birth, "setBirth", "setBirthDate", "setBirthday");
                if (!ok) {
                    UserReflect.set(u, req.birth.toString(), "setBirth", "setBirthDate", "setBirthday");
                }
            }

            UserReflect.set(u, req.platform, "setMainPlatform", "setPlatform", "setPrimaryPlatform");

            UserReflect.set(u, new ArrayList<>(req.interestNames()), "setInterests", "setInterestList", "setInterestNames");

            return SettingsSaveResult.ok(u);
        }
    }

    public static final class SettingsUpdateRequest {
        public final String userId;
        public final String nickname;
        public final String email;         // nullable
        public final LocalDate birth;      // nullable
        public final String platform;      // nullable/empty 허용
        public final List<Long> interestIds; // DB user_interest용
        public final String currentPassword; // nullable
        public final String newPassword;     // nullable

        private final List<String> interestNames;

        public SettingsUpdateRequest(
                String userId,
                String nickname,
                String email,
                LocalDate birth,
                String platform,
                List<Long> interestIds,
                String currentPassword,
                String newPassword
        ) {
            this.userId = userId;
            this.nickname = nickname;
            this.email = email;
            this.birth = birth;
            this.platform = platform;
            this.interestIds = (interestIds == null) ? List.of() : new ArrayList<>(interestIds);
            this.currentPassword = currentPassword;
            this.newPassword = newPassword;
            this.interestNames = new ArrayList<>();
        }

        public List<String> interestNames() {
            return interestNames;
        }
    }

    public static final class SettingsSaveResult {
        public final boolean ok;
        public final String message;
        public final User updatedUser;

        private SettingsSaveResult(boolean ok, String message, User updatedUser) {
            this.ok = ok;
            this.message = message;
            this.updatedUser = updatedUser;
        }

        public static SettingsSaveResult ok(User u) {
            return new SettingsSaveResult(true, null, u);
        }

        public static SettingsSaveResult fail(String msg) {
            return new SettingsSaveResult(false, msg, null);
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
                        if (value == null) {
                            mm.invoke(target, new Object[]{null});
                            return;
                        }

                        if (p == String.class) {
                            mm.invoke(target, value.toString());
                            return;
                        }
                        if (List.class.isAssignableFrom(p) && value instanceof List<?>) {
                            mm.invoke(target, value);
                            return;
                        }
                        if (p.isAssignableFrom(value.getClass())) {
                            mm.invoke(target, value);
                            return;
                        }
                    }
                } catch (Exception ignore) {}
            }
        }

        static boolean setIfMatches(Object target, Object value, String... setters) {
            if (target == null || value == null) return false;
            for (String m : setters) {
                try {
                    for (var mm : target.getClass().getMethods()) {
                        if (!mm.getName().equals(m)) continue;
                        if (mm.getParameterCount() != 1) continue;
                        Class<?> p = mm.getParameterTypes()[0];
                        if (p.isAssignableFrom(value.getClass())) {
                            mm.invoke(target, value);
                            return true;
                        }
                    }
                } catch (Exception ignore) {}
            }
            return false;
        }
    }
}
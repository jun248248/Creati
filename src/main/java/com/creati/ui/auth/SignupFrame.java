package com.creati.ui.auth;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.creati.service.AuthService;
import com.creati.dao.UserDao;
import com.creati.util.FontKit;
import com.creati.util.UITheme;

import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class SignupFrame extends JFrame {

	private static final String VIDEO_RES = "/videos/intro.mp4";

	private static final int LABEL_WIDTH = 110;
	private static final int FIELD_HEIGHT = 38;
	private static final int ROW_GAP = 12;
	private static final int RIGHT_W = 305;

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
	private JComboBox<String> interestCombo;
	private TagInput tagInput;

	private final Map<String, Long> interestNameToId = new LinkedHashMap<>();

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
		p.setBackground(UITheme.DARK_SURFACE);

		try {
			VideoPanel video = new VideoPanel(java.util.Objects.requireNonNull(getClass().getResource(VIDEO_RES)).toExternalForm());
			video.setPreferredSize(new Dimension(550, 720));
			p.add(video, BorderLayout.CENTER);
		} catch (Throwable t) {
			JLabel fallback = new JLabel("<html><center><b>영상 영역</b><br/>OpenJFX 설정 필요</center></html>",
					SwingConstants.CENTER);
			fallback.setForeground(UITheme.ON_DARK);
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
		card.setBackground(UITheme.SURFACE);
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UITheme.SURFACE_BORDER, 1),
				new EmptyBorder(20, 20, 20, 20)));

		Dimension cardSize = new Dimension(490, 520);
		card.setPreferredSize(cardSize);
		card.setMinimumSize(cardSize);
		card.setMaximumSize(cardSize);

		JPanel page = new JPanel(new GridBagLayout());
		page.setBackground(UITheme.SURFACE);

		GridBagConstraints g = new GridBagConstraints();
		g.gridx = 0;
		g.gridy = 0;
		g.weightx = 1.0;
		g.weighty = 1.0;
		g.fill = GridBagConstraints.BOTH;
		g.anchor = GridBagConstraints.NORTHWEST;
		g.insets = new Insets(0, 0, 12, 0);

		JComponent formPanel = buildFormPanel();

		JScrollPane scroll = new JScrollPane(formPanel);
		scroll.setBorder(null);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		scroll.getViewport().setBackground(UITheme.SURFACE);

		page.add(scroll, g);
		SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));

		g.gridy++;
		g.weighty = 0;
		g.fill = GridBagConstraints.HORIZONTAL;
		g.insets = new Insets(0, 0, 0, 0);

		msgLabel = new JLabel(" ", SwingConstants.CENTER);
		msgLabel.setFont(UITheme.CAPTION);
		msgLabel.setForeground(UITheme.ERROR);

		JPanel msgWrap = new JPanel(new BorderLayout());
		msgWrap.setBackground(UITheme.SURFACE);
		msgWrap.add(msgLabel, BorderLayout.CENTER);
		page.add(msgWrap, g);

		card.add(page, BorderLayout.CENTER);
		stack.add(card, s);

		s.gridy++;
		s.insets = new Insets(16, 0, 0, 0);

		JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
		btns.setBackground(UITheme.BG);

		JButton backBtn = secondaryButton("돌아가기");
		JButton submitBtn = primaryButton("가입하기");
		Dimension btnSize = new Dimension(150, 40);
		setButtonSize(backBtn, btnSize);
		setButtonSize(submitBtn, btnSize);

		backBtn.addActionListener(e -> {
			dispose();
			if (loginFrame != null)
				loginFrame.setVisible(true);
		});
		submitBtn.addActionListener(e -> onSignup());

		btns.add(backBtn);
		btns.add(submitBtn);
		stack.add(btns, s);

		outer.add(stack);
		return outer;
	}

	private JComponent buildFormPanel() {
		JPanel form = new JPanel(new GridBagLayout());
		form.setBackground(UITheme.SURFACE);
		
		form.setBorder(new EmptyBorder(0, 0, 0, 16));

		GridBagConstraints r = new GridBagConstraints();
		r.gridy = 0;
		r.insets = new Insets(0, 0, ROW_GAP, 0);
		r.anchor = GridBagConstraints.WEST;

		JLabel idLabel = label("아이디", LABEL_WIDTH);

		idField = new JTextField();
		setFieldSize(idField, new Dimension(RIGHT_W - 88, FIELD_HEIGHT));

		JButton dupBtn = secondarySmallButton("중복확인");
		setButtonSize(dupBtn, new Dimension(80, FIELD_HEIGHT));
		dupBtn.addActionListener(e -> checkDuplicateId());

		JPanel idRow = rowX(idField, 8, dupBtn);
		addRow(form, r, idLabel, idRow);

		JLabel pwLabel = label("비밀번호", LABEL_WIDTH);
		pwField = new JPasswordField();
		setFieldSize(pwField, new Dimension(RIGHT_W, FIELD_HEIGHT));
		addRow(form, r, pwLabel, pwField);

		JLabel pw2Label = label("비밀번호 확인", LABEL_WIDTH);
		pw2Field = new JPasswordField();
		setFieldSize(pw2Field, new Dimension(RIGHT_W, FIELD_HEIGHT));
		addRow(form, r, pw2Label, pw2Field);

		JLabel nickLabel = label("닉네임", LABEL_WIDTH);
		nickField = new JTextField();
		setFieldSize(nickField, new Dimension(RIGHT_W, FIELD_HEIGHT));
		addRow(form, r, nickLabel, nickField);

		JLabel phoneLabel = label("전화번호", LABEL_WIDTH);
		phoneField = new JTextField();
		setFieldSize(phoneField, new Dimension(RIGHT_W, FIELD_HEIGHT));
		addRow(form, r, phoneLabel, phoneField);

		JLabel birthLabel = label("생년월일", LABEL_WIDTH);
		JPanel birthPanel = new JPanel();
		birthPanel.setOpaque(false);
		birthPanel.setLayout(new BoxLayout(birthPanel, BoxLayout.X_AXIS));
		birthPanel.setPreferredSize(new Dimension(RIGHT_W, FIELD_HEIGHT));
		birthPanel.setMaximumSize(new Dimension(RIGHT_W, FIELD_HEIGHT));

		int yW = 110, mW = 92, dW = 92;

		birthYearCombo = new JComboBox<>(years());
		setFieldSize(birthYearCombo, new Dimension(yW, FIELD_HEIGHT));
		birthMonthCombo = new JComboBox<>(months());
		setFieldSize(birthMonthCombo, new Dimension(mW, FIELD_HEIGHT));
		birthDayCombo = new JComboBox<>(days());
		setFieldSize(birthDayCombo, new Dimension(dW, FIELD_HEIGHT));

		birthPanel.add(birthYearCombo);
		birthPanel.add(Box.createHorizontalStrut(6));
		birthPanel.add(birthMonthCombo);
		birthPanel.add(Box.createHorizontalStrut(6));
		birthPanel.add(birthDayCombo);

		addRow(form, r, birthLabel, birthPanel);

		JLabel emailLabel = label("이메일", LABEL_WIDTH);

		JPanel emailRow = new JPanel();
		emailRow.setOpaque(false);
		emailRow.setLayout(new BoxLayout(emailRow, BoxLayout.X_AXIS));
		emailRow.setPreferredSize(new Dimension(RIGHT_W, FIELD_HEIGHT));
		emailRow.setMaximumSize(new Dimension(RIGHT_W, FIELD_HEIGHT));

		int localW = 95;
		int domainW = 80;
		int comboW = RIGHT_W - (localW + domainW + 8 + 6 + 6 + 10);

		emailLocalField = new JTextField();
		setFieldSize(emailLocalField, new Dimension(localW, FIELD_HEIGHT));

		JLabel at = new JLabel("@");
		at.setFont(UITheme.BODY);

		emailDomainField = new JTextField();
		setFieldSize(emailDomainField, new Dimension(domainW, FIELD_HEIGHT));

		emailDomainCombo = new JComboBox<>(
				new String[] { "직접입력", "naver.com", "gmail.com", "daum.net", "kakao.com", "hanmail.net" });
		setFieldSize(emailDomainCombo, new Dimension(Math.max(95, comboW), FIELD_HEIGHT));

		emailDomainCombo.addActionListener(e -> {
			if (emailDomainCombo.getSelectedIndex() == 0) {
				emailDomainField.setEnabled(true);
				emailDomainField.setText("");
			} else {
				emailDomainField.setEnabled(false);
				emailDomainField.setText((String) emailDomainCombo.getSelectedItem());
			}
		});

		emailRow.add(emailLocalField);
		emailRow.add(Box.createHorizontalStrut(6));
		emailRow.add(at);
		emailRow.add(Box.createHorizontalStrut(6));
		emailRow.add(emailDomainField);
		emailRow.add(Box.createHorizontalStrut(8));
		emailRow.add(emailDomainCombo);

		addRow(form, r, emailLabel, emailRow);

		JLabel platLabel = label("주요 플랫폼", LABEL_WIDTH);
		platformCombo = new JComboBox<>(
				new String[] { "선택", "YouTube", "Instagram", "Blog", "Brunch", "TikTok", "SoundCloud", "Other" });
		setFieldSize(platformCombo, new Dimension(RIGHT_W, FIELD_HEIGHT));
		addRow(form, r, platLabel, platformCombo);

		JLabel catLabel = label("관심분야", LABEL_WIDTH);

		JPanel catPanel = new JPanel();
		catPanel.setOpaque(false);
		catPanel.setLayout(new BoxLayout(catPanel, BoxLayout.Y_AXIS));
		catPanel.setPreferredSize(new Dimension(RIGHT_W, FIELD_HEIGHT * 2 + 8));
		catPanel.setMaximumSize(new Dimension(RIGHT_W, FIELD_HEIGHT * 2 + 8));

		interestCombo = new JComboBox<>(new String[] { "선택", "영상", "이미지", "글", "음악" });
		setFieldSize(interestCombo, new Dimension(RIGHT_W, FIELD_HEIGHT));
		loadInterestOptions();

		JPanel comboWrap = new JPanel(new BorderLayout());
		comboWrap.setOpaque(false);
		comboWrap.setPreferredSize(new Dimension(RIGHT_W, FIELD_HEIGHT));
		comboWrap.setMaximumSize(new Dimension(RIGHT_W, FIELD_HEIGHT));
		comboWrap.add(interestCombo, BorderLayout.WEST);

		tagInput = new TagInput(RIGHT_W, FIELD_HEIGHT);
		tagInput.setPlaceholder("#내용 입력 후 Enter");

		JPanel tagWrap = new JPanel(new BorderLayout());
		tagWrap.setOpaque(false);
		tagWrap.setPreferredSize(new Dimension(RIGHT_W, FIELD_HEIGHT));
		tagWrap.setMaximumSize(new Dimension(RIGHT_W, FIELD_HEIGHT));
		tagWrap.add(tagInput, BorderLayout.WEST);

		interestCombo.addActionListener(e -> {
			int idx = interestCombo.getSelectedIndex();
			if (idx <= 0)
				return;

			if (tagInput.getTags().size() >= 3) {
				toast("관심분야는 최대 3개까지만 선택 가능합니다.");
				interestCombo.setSelectedIndex(0);
				return;
			}
			String v = (String) interestCombo.getSelectedItem();
			tagInput.addTag(v);
			interestCombo.setSelectedIndex(0);
		});

		catPanel.add(comboWrap);
		catPanel.add(Box.createVerticalStrut(8));
		catPanel.add(tagWrap);

		addRow(form, r, catLabel, catPanel);

		return form;
	}

	private void onSignup() {

	    String id = idField.getText().trim();
	    String pw = new String(pwField.getPassword()).trim();
	    String pw2 = new String(pw2Field.getPassword()).trim();
	    String nickname = nickField.getText().trim();
	    String phone = phoneField.getText().trim();

	    if (id.isEmpty() || pw.isEmpty() || nickname.isEmpty()) {
	        toast("필수 항목을 입력해주세요.");
	        return;
	    }

	    if (!pw.equals(pw2)) {
	        toast("비밀번호가 일치하지 않습니다.");
	        return;
	    }

	    LocalDate birth = null;
	    if (birthYearCombo.getSelectedItem() != null) {
	        int y = Integer.parseInt((String) birthYearCombo.getSelectedItem());
	        int m = Integer.parseInt((String) birthMonthCombo.getSelectedItem());
	        int d = Integer.parseInt((String) birthDayCombo.getSelectedItem());
	        birth = LocalDate.of(y, m, d);
	    }

	    String emailLocal = emailLocalField.getText().trim();
	    String emailDomain = emailDomainField.getText().trim();
	    String email = null;
	    if (!emailLocal.isEmpty() && !emailDomain.isEmpty()) {
	        email = emailLocal + "@" + emailDomain;
	    }

	    String platform = (String) platformCombo.getSelectedItem();
	    if ("선택".equals(platform)) {
	        platform = null;
	    }

	    java.util.Set<String> tags = tagInput.getTags();
	    if (tags == null || tags.isEmpty()) {
	        toast("관심분야를 최소 1개 선택해주세요.");
	        return;
	    }
	    java.util.List<Long> interestIds = convertTagsToIds(new java.util.ArrayList<>(tags));

	    AuthService auth = AuthService.getInstance();

	    boolean result = auth.signup(
	            id, pw, nickname,
	            phone, email, birth,
	            platform, interestIds
	    );

	    if (result) {
	        JOptionPane.showMessageDialog(this, "회원가입 성공!");
	        dispose();
	        if (loginFrame != null)
	            loginFrame.setVisible(true);
	    } else {
	        toast("회원가입 실패 (중복 아이디 또는 DB 오류)");
	    }
	}

	private java.util.List<Long> convertTagsToIds(java.util.List<String> tags) {
	    java.util.List<Long> ids = new ArrayList<>();
	    if (tags == null) return ids;
	    for (String tag : tags) {
	        if (tag == null) continue;
	        Long id = interestNameToId.get(tag.trim());
	        if (id != null) ids.add(id);
	    }
	    return ids;
	}

	private void loadInterestOptions() {
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
	        interestCombo.setModel(model);
	    } catch (Exception ignore) {
	    }
	}
	
	
	
	private void addRow(JPanel form, GridBagConstraints r, JComponent left, JComponent right) {
		GridBagConstraints l = (GridBagConstraints) r.clone();
		l.gridx = 0;
		l.weightx = 0;
		l.fill = GridBagConstraints.NONE;
		form.add(left, l);

		GridBagConstraints rr = (GridBagConstraints) r.clone();
		rr.gridx = 1;
		rr.weightx = 1.0;
		rr.fill = GridBagConstraints.HORIZONTAL;
		rr.anchor = GridBagConstraints.WEST;
		form.add(right, rr);

		r.gridy++;
	}

	private JPanel rowX(JComponent left, int gap, JComponent right) {
		JPanel p = new JPanel();
		p.setOpaque(false);
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.setPreferredSize(new Dimension(RIGHT_W, FIELD_HEIGHT));
		p.setMaximumSize(new Dimension(RIGHT_W, FIELD_HEIGHT));
		p.add(left);
		p.add(Box.createHorizontalStrut(gap));
		p.add(right);
		return p;
	}

	private void toast(String msg) {
		JOptionPane.showMessageDialog(this, msg, "알림", JOptionPane.INFORMATION_MESSAGE);
	}

	private JLabel label(String text, int w) {
		JLabel l = new JLabel(text);
		l.setFont(UITheme.BODY_MED);
		l.setForeground(UITheme.TEXT);
		l.setPreferredSize(new Dimension(w, 24));
		l.setMinimumSize(new Dimension(w, 24));
		l.setMaximumSize(new Dimension(w, 24));
		return l;
	}

	private void setFieldSize(JComponent field, Dimension d) {
		field.setPreferredSize(d);
		field.setMinimumSize(d);
		field.setMaximumSize(d);

		if (field instanceof JTextField tf)
			tf.setFont(UITheme.BODY);
		else if (field instanceof JPasswordField pf)
			pf.setFont(UITheme.BODY);
		else if (field instanceof JComboBox<?> cb)
			cb.setFont(UITheme.BODY);
	}

	private void setButtonSize(JButton b, Dimension d) {
		b.setPreferredSize(d);
		b.setMinimumSize(d);
		b.setMaximumSize(d);
		b.setFont(UITheme.BODY_MED);
	}

	private JButton primaryButton(String text) {
		JButton b = new JButton(text);
		b.setFocusPainted(false);
		b.setBackground(UITheme.ACCENT_PURPLE);
		b.setForeground(UITheme.ON_DARK);
		b.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
		return b;
	}

	private JButton secondaryButton(String text) {
		JButton b = new JButton(text);
		b.setFocusPainted(false);
		b.setBackground(UITheme.BTN_SECONDARY_BG);
		b.setForeground(UITheme.TEXT);
		b.setBorder(BorderFactory.createLineBorder(UITheme.BTN_SECONDARY_BORDER, 1));
		return b;
	}

	private JButton secondarySmallButton(String text) {
		JButton b = new JButton(text);
		b.setFocusPainted(false);
		b.setBackground(UITheme.BTN_SECONDARY_BG);
		b.setForeground(UITheme.TEXT);
		b.setBorder(BorderFactory.createLineBorder(UITheme.BTN_SECONDARY_BORDER, 1));
		b.setFont(UITheme.CAPTION);
		return b;
	}

	private String[] years() {
		String[] years = new String[76];
		years[0] = "년";
		for (int i = 1; i < years.length; i++)
			years[i] = String.valueOf(2024 - i + 1);
		return years;
	}

	private String[] months() {
		String[] months = new String[13];
		months[0] = "월";
		for (int i = 1; i <= 12; i++)
			months[i] = String.valueOf(i);
		return months;
	}

	private String[] days() {
		String[] days = new String[32];
		days[0] = "일";
		for (int i = 1; i <= 31; i++)
			days[i] = String.valueOf(i);
		return days;
	}

	
	private void checkDuplicateId() {

	    String id = idField.getText().trim();

	    if (id.isEmpty()) {
	        JOptionPane.showMessageDialog(this, "아이디를 입력해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
	        return;
	    }

	    AuthService auth = AuthService.getInstance();

	    boolean available = auth.isIdAvailable(id);

	    if (available) {
	        JOptionPane.showMessageDialog(this, "사용 가능한 아이디예요!", "중복확인", JOptionPane.INFORMATION_MESSAGE);
	    } else {
	        JOptionPane.showMessageDialog(this, "이미 사용 중인 아이디에요.", "중복확인", JOptionPane.WARNING_MESSAGE);
	    }
	}
	
}
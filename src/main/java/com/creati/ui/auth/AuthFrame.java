package com.creati.ui.auth;

import com.creati.model.User;
import com.creati.service.AuthService;
import com.creati.ui.main.MainFrame;
import com.creati.util.FontKit;
import com.creati.util.UITheme;
import com.creati.ui.main.AppState;
import com.creati.dao.UserDao;
import com.creati.dto.UserDto;
import com.creati.model.LogRepository;
import com.creati.model.LogRepositoryImpl;
import com.creati.service.LogService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;

public class AuthFrame extends JFrame {

	private static final String VIDEO_RES = "/videos/intro.mp4";

	private VideoPanel videoPanel;

	private JTextField idField;
	private JPasswordField pwField;
	private JLabel msgLabel;

	public AuthFrame() {
		super("Creati - 로그인");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1100, 720);
		setLocationRelativeTo(null);

		JPanel root = new JPanel(new GridLayout(1, 2));
		root.setBackground(UITheme.BG);

		root.add(buildVideoPanel());
		root.add(buildLoginPanel());

		setContentPane(root);

		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (videoPanel != null)
					videoPanel.stop();
			}
		});
	}

	private JComponent buildVideoPanel() {
		JPanel p = new JPanel(new BorderLayout());
		p.setBackground(UITheme.DARK_SURFACE);

		try {
			videoPanel = new VideoPanel(
					java.util.Objects.requireNonNull(getClass().getResource(VIDEO_RES)).toExternalForm());
			p.add(videoPanel, BorderLayout.CENTER);
		} catch (Throwable t) {
			JLabel fallback = new JLabel("<html><center><b>영상 영역</b><br/>OpenJFX 설정 필요</center></html>",
					SwingConstants.CENTER);
			fallback.setForeground(UITheme.ON_DARK);
			p.add(fallback, BorderLayout.CENTER);
		}
		return p;
	}

	private JComponent buildLoginPanel() {
		JPanel p = new JPanel(new GridBagLayout());
		p.setBackground(UITheme.BG);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER;

		JPanel stack = new JPanel();
		stack.setBackground(UITheme.BG);
		stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

		JLabel hello = new JLabel("안녕하세요, Creati입니다.");
		hello.setAlignmentX(Component.CENTER_ALIGNMENT);
		hello.setHorizontalAlignment(SwingConstants.CENTER);
		hello.setFont(FontKit.bold(26f));
		hello.setForeground(UITheme.TEXT);

		JLabel intro = new JLabel(
		    "<html><div style='text-align:center;'>"
		    + "당신의 성장 기록을 차곡차곡 정리하고<br/>"
		    + "다시 도전하는 순간까지 함께할게요."
		    + "</div></html>"
		);
		intro.setFont(UITheme.BODY_MED);
		intro.setForeground(UITheme.TEXT);
		intro.setHorizontalAlignment(SwingConstants.CENTER);

		JPanel introWrap = new JPanel(new BorderLayout());
		introWrap.setOpaque(false);
		introWrap.add(intro, BorderLayout.CENTER);

		stack.add(hello);
		stack.add(Box.createVerticalStrut(8));
		stack.add(introWrap);
		stack.add(Box.createVerticalStrut(22));

		JPanel card = new JPanel(new GridBagLayout());
		card.setBackground(UITheme.SURFACE);
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UITheme.SURFACE_BORDER, 1),
				new EmptyBorder(24, 28, 24, 28)));
		card.setPreferredSize(new Dimension(460, 360));
		card.setMaximumSize(new Dimension(460, 360));

		JPanel contentPanel = new JPanel();
		contentPanel.setBackground(UITheme.SURFACE);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

		JPanel form = buildFormPanel();
		form.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPanel.add(form);
		contentPanel.add(Box.createVerticalStrut(10));

		msgLabel = new JLabel(" ");
		msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		msgLabel.setFont(UITheme.CAPTION);
		msgLabel.setForeground(UITheme.ERROR);
		contentPanel.add(msgLabel);
		contentPanel.add(Box.createVerticalStrut(14));

		JButton loginBtn = primaryButton("로그인");
		JButton signupBtn = secondaryButton("회원가입");

		Dimension btnSize = new Dimension(320, 36);
		setButtonSize(loginBtn, btnSize);
		setButtonSize(signupBtn, btnSize);

		loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		signupBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

		loginBtn.addActionListener(e -> onLogin());

		
		signupBtn.addActionListener(e -> {
			setVisible(false);
			new SignupFrame(this).setVisible(true);
		});

		contentPanel.add(loginBtn);
		contentPanel.add(Box.createVerticalStrut(10));
		contentPanel.add(signupBtn);
		contentPanel.add(Box.createVerticalStrut(16));

		JButton findBtn = linkButton("아이디/비밀번호 찾기");
		findBtn.setFont(UITheme.CAPTION);
		findBtn.setForeground(UITheme.LINK_MUTED);
		findBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
		findBtn.addActionListener(e -> {
			FindAccountDialog dlg = new FindAccountDialog(this);
			dlg.setVisible(true);
		});

		contentPanel.add(findBtn);

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.CENTER;
		card.add(contentPanel, c);

		stack.add(card);
		p.add(stack, gbc);
		return p;
	}

	private JPanel buildFormPanel() {
		JPanel form = new JPanel(new GridBagLayout());
		form.setBackground(UITheme.SURFACE);

		int formW = 320;
		form.setPreferredSize(new Dimension(formW, 170));
		form.setMaximumSize(new Dimension(formW, 170));
		form.setMinimumSize(new Dimension(formW, 170));

		GridBagConstraints g = new GridBagConstraints();
		g.gridx = 0;
		g.weightx = 1;
		g.fill = GridBagConstraints.HORIZONTAL;
		g.insets = new Insets(0, 0, 0, 0);

		idField = new JTextField();
		pwField = new JPasswordField();

		Dimension fieldSize = new Dimension(320, 40);
		setFieldSize(idField, fieldSize);
		setFieldSize(pwField, fieldSize);

		JLabel idLabel = new JLabel("아이디");
		idLabel.setFont(UITheme.BODY_MED);
		idLabel.setForeground(UITheme.TEXT);
		idLabel.setHorizontalAlignment(SwingConstants.LEFT);

		JLabel pwLabel = new JLabel("비밀번호");
		pwLabel.setFont(UITheme.BODY_MED);
		pwLabel.setForeground(UITheme.TEXT);
		pwLabel.setHorizontalAlignment(SwingConstants.LEFT);

		g.gridy = 0;
		g.anchor = GridBagConstraints.WEST;
		form.add(idLabel, g);

		g.gridy = 1;
		g.anchor = GridBagConstraints.CENTER;
		form.add(idField, g);

		g.gridy = 2;
		form.add(Box.createVerticalStrut(16), g);

		g.gridy = 3;
		g.anchor = GridBagConstraints.WEST;
		form.add(pwLabel, g);

		g.gridy = 4;
		g.anchor = GridBagConstraints.CENTER;
		form.add(pwField, g);

		return form;
	}

	private void onLogin() {
		String id = idField.getText().trim();
		String pw = new String(pwField.getPassword()).trim();

		if (id.isEmpty() && pw.isEmpty()) {
			msgLabel.setText("아이디와 비밀번호를 입력해주세요.");
			return;
		}
		if (id.isEmpty()) {
			msgLabel.setText("아이디를 입력해주세요.");
			return;
		}
		if (pw.isEmpty()) {
			msgLabel.setText("비밀번호를 입력해주세요.");
			return;
		}

		// DB
		AuthService auth = AuthService.getInstance();
		User user = auth.login(id, pw);
		//UserDto - User 연결 필요
		//UserDao userDao = new UserDao();
		//UserDto loginUser = userDao.login(id, pw);

		if (user == null) {
			msgLabel.setText("아이디 또는 비밀번호가 올바르지 않습니다.");
			return;
		}

		
		if (videoPanel != null)
			videoPanel.stop();
		dispose();
		
		AppState.get().setCurrentUser(user);
		new MainFrame().setVisible(true);

	}

	private void setFieldSize(JComponent field, Dimension d) {
		field.setMaximumSize(d);
		field.setPreferredSize(d);
		field.setMinimumSize(d);
		field.setFont(UITheme.BODY);
	}

	private void setButtonSize(JButton b, Dimension d) {
		b.setMaximumSize(d);
		b.setPreferredSize(d);
		b.setMinimumSize(d);
		b.setFont(UITheme.BODY_MED);
	}

	private JButton primaryButton(String text) {
		JButton b = new JButton(text);
		b.setFocusPainted(false);
		b.setBackground(UITheme.ACCENT_PURPLE);
		b.setForeground(UITheme.ON_DARK);
		b.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
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

	private JButton linkButton(String text) {
		JButton b = new JButton(text);
		b.setFocusPainted(false);
		b.setContentAreaFilled(false);
		b.setBorderPainted(false);
		b.setForeground(UITheme.ACCENT_PURPLE);
		return b;
	}
}
package com.creati.ui.main;

import com.creati.ui.main.MainUiParts.CircleAvatar;
import com.creati.ui.main.MainUiParts.EllipsisButton;
import com.creati.ui.main.MainUiParts.RoundedButton;
import com.creati.ui.main.MainUiParts.ShadowLabel;
import com.creati.util.FontKit;
import com.creati.util.UITheme;
import com.creati.service.LogService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import static com.creati.ui.main.MainUiParts.*;

import java.awt.*;
import java.nio.file.Path;

/**
 * MainFrame (조립 + 카드 전환 + 상단/사이드) - HOME 화면은 MainHomeView로 분리 - 커스텀 컴포넌트/이미지
 * 유틸은 MainUiParts로 분리 - CHALLENGE는 ChallengeView (검색 연동)
 */
public class MainFrame extends JFrame {
	
    private final LogService logService;

	// asset paths
	static final Path ETTI_PATH = Path.of("assets/images/etti/etti_default.png");
	static final Path DEFAULT_PROFILE_PATH = Path.of("assets/images/profile/default_profile.png");

	// cards
	private static final String CARD_HOME = "HOME";
	private static final String CARD_CHALLENGE = "CHALLENGE";
	private static final String CARD_AI = "AI";
	private static final String CARD_COMMUNITY = "COMMUNITY";
	private static final String CARD_QNA = "QNA";

	private final CardLayout cardLayout = new CardLayout();
	private final JPanel contentCards = new JPanel(cardLayout);

	private final MainSearchBar searchBar = new MainSearchBar();
	private final ChallengeView challengeView = new ChallengeView();

	private JPanel topArea; // (에티 + 검색창) 묶음

	private final String nickname;
	private final Image profileImage;

	private JPopupMenu writeMenu;

	// 월간 AI 인사이트: 이번 달 1개만 유지(기능 연결 전 임시 상태)
	private String currentInsightText = null;

	public MainFrame(String nickname, LogService logService) {
		super("Creati - 메인");
		this.nickname = nickname;
		this.logService = logService;
		UITheme.ensureInit();

		this.profileImage = loadImage(DEFAULT_PROFILE_PATH);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1200, 760);
		setLocationRelativeTo(null);

		setContentPane(buildRoot());
		showCard(CARD_HOME);
	}

	// Material Icons - settings (⚙)
	private String makeSettingsIcon() {
		// settings = 0xE8B8
		try {
			return new String(Character.toChars(0xE8B8));
		} catch (Exception e) {
			return "⚙";
		}
	}

	private JComponent buildRoot() {
		JPanel root = new JPanel(new BorderLayout());
		root.setBackground(UITheme.BG);

		root.add(buildTopBar(), BorderLayout.NORTH);
		root.add(buildSideMenu(), BorderLayout.WEST);

		JPanel center = new JPanel(new BorderLayout());
		center.setBackground(UITheme.BG);

		// (에티 도움말 + 검색창) 묶음
		topArea = new JPanel();
		topArea.setOpaque(false);
		topArea.setLayout(new BoxLayout(topArea, BoxLayout.Y_AXIS));
		topArea.add(buildEttiHelpBar());
		topArea.add(searchBar);

		searchBar.setVisible(false); // HOME에서는 숨김
		center.add(topArea, BorderLayout.NORTH);

		// 검색창 → ChallengeView에 연결
		searchBar.setOnSearch(challengeView::setQuery);

		contentCards.setBackground(UITheme.BG);

		// HOME
		contentCards.add(new MainHomeView(this::getCurrentInsightText, this::setCurrentInsightText), CARD_HOME);

		// CHALLENGE (실제 화면)
		contentCards.add(challengeView, CARD_CHALLENGE);

		// 기타
		contentCards.add(buildPlaceholder("AI 분석 - TODO UI"), CARD_AI);
		contentCards.add(buildPlaceholder("커뮤니티 - 공개 글 리스트 - TODO UI"), CARD_COMMUNITY);
		contentCards.add(buildPlaceholder("질문하기 - Q&A 게시판 - TODO UI"), CARD_QNA);

		center.add(contentCards, BorderLayout.CENTER);
		root.add(center, BorderLayout.CENTER);

		return root;
	}

	// =========================
	// Top Bar
	// =========================
	private JComponent buildTopBar() {
		JPanel bar = new JPanel(new BorderLayout());
		bar.setBackground(Color.WHITE);
		bar.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 235)),
				new EmptyBorder(12, 16, 12, 16)));

		ShadowLabel logo = new ShadowLabel("Creati", 30, new Color(90, 90, 100));
		logo.setFont(FontKit.esamanruBold(36f));
		logo.setForeground(UITheme.ACCENT_PURPLE);
		logo.setBorder(new EmptyBorder(0, 8, 0, 0));

		JPanel logoWrap = new JPanel(new GridBagLayout());
		logoWrap.setOpaque(false);
		logoWrap.add(logo);

		JPanel right = new JPanel(new BorderLayout());
		right.setOpaque(false);

		JPanel profileRow = new JPanel();
		profileRow.setOpaque(false);
		profileRow.setLayout(new BoxLayout(profileRow, BoxLayout.X_AXIS));

		CircleAvatar avatar = new CircleAvatar(profileImage);

		JLabel nick = new JLabel(nickname);
		nick.setFont(UITheme.BODY_MED);
		nick.setForeground(UITheme.TEXT);

		JButton settingsBtn = new JButton(makeSettingsIcon());
		settingsBtn.setToolTipText("설정");

		settingsBtn.setFont(FontKit.materialIcon(20f));
		settingsBtn.setForeground(new Color(130, 130, 145));
		settingsBtn.setBackground(Color.WHITE);

		settingsBtn.setBorder(new EmptyBorder(6, 6, 6, 6));
		settingsBtn.setFocusPainted(false);
		settingsBtn.setContentAreaFilled(false);
		settingsBtn.setOpaque(false);
		settingsBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		settingsBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "TODO: 설정 화면 연결"));

		profileRow.add(avatar);
		profileRow.add(Box.createHorizontalStrut(10));
		profileRow.add(nick);
		profileRow.add(Box.createHorizontalStrut(10));
		profileRow.add(settingsBtn);

		// 새 글쓰기 버튼
		RoundedButton writeBtn = new RoundedButton("새 글쓰기");
		writeBtn.setBackground(UITheme.ACCENT_PURPLE);
		writeBtn.setForeground(Color.WHITE);
		writeBtn.setFont(UITheme.BODY_MED);

		writeMenu = buildWriteMenu(writeBtn);

		writeBtn.addActionListener(e -> {
			if (writeMenu.isVisible()) {
				writeMenu.setVisible(false);
			} else {
				Dimension m = writeMenu.getPreferredSize();
				int x = writeBtn.getWidth() - m.width;
				writeMenu.show(writeBtn, x, writeBtn.getHeight() + 6);
			}
		});

		JPanel writeRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		writeRow.setOpaque(false);
		writeRow.add(writeBtn);

		right.add(profileRow, BorderLayout.NORTH);
		right.add(Box.createVerticalStrut(8), BorderLayout.CENTER);
		right.add(writeRow, BorderLayout.SOUTH);

		bar.add(logoWrap, BorderLayout.WEST);
		bar.add(right, BorderLayout.EAST);

		return bar;
	}

	// =========================
	// Write Menu
	// =========================
	private JPopupMenu buildWriteMenu(JComponent anchor) {
		JPopupMenu menu = new JPopupMenu();
		menu.setBackground(Color.WHITE);
		menu.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(230, 230, 235), 1, true), new EmptyBorder(6, 6, 6, 6)));

		String t1 = "새 성장 로그 작성";
		String t2 = "질문하기";

		int itemW = Math.max(measureWidth(anchor, t1), measureWidth(anchor, t2));
		itemW = Math.max(itemW, 200);

		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JButton b1 = makeWriteMenuButtonLeft(t1, itemW);
		JButton b2 = makeWriteMenuButtonLeft(t2, itemW);

		b1.addActionListener(e -> {
			menu.setVisible(false);
			
			LogWriteDialog writeDialog = new LogWriteDialog(this, logService);
	        writeDialog.setVisible(true);
		});

		b2.addActionListener(e -> {
			menu.setVisible(false);
			showCard(CARD_QNA);
		});

		panel.add(b1);
		panel.add(Box.createVerticalStrut(6));
		panel.add(b2);

		menu.add(panel);
		return menu;
	}

	private int measureWidth(JComponent comp, String text) {
		Font f = (UITheme.BODY_MED != null) ? UITheme.BODY_MED : comp.getFont();
		FontMetrics fm = comp.getFontMetrics(f);
		return fm.stringWidth(text) + 40;
	}

	private JButton makeWriteMenuButtonLeft(String text, int w) {
		JButton btn = new JButton(text);
		btn.setFocusPainted(false);
		btn.setBorderPainted(false);
		btn.setContentAreaFilled(false);
		btn.setOpaque(true);
		btn.setBackground(Color.WHITE);
		btn.setForeground(UITheme.TEXT);
		btn.setFont(UITheme.BODY_MED);
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btn.setHorizontalAlignment(SwingConstants.LEFT);

		btn.setPreferredSize(new Dimension(w, 40));
		btn.setMaximumSize(new Dimension(w, 40));
		btn.setBorder(new EmptyBorder(8, 14, 8, 14));

		btn.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseEntered(java.awt.event.MouseEvent e) {
				btn.setBackground(LAVENDER_HOVER);
				btn.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(LAVENDER_BORDER, 1, true), new EmptyBorder(7, 13, 7, 13)));
			}

			@Override
			public void mouseExited(java.awt.event.MouseEvent e) {
				btn.setBackground(Color.WHITE);
				btn.setBorder(new EmptyBorder(8, 14, 8, 14));
			}

			@Override
			public void mousePressed(java.awt.event.MouseEvent e) {
				btn.setBackground(LAVENDER_HOVER.darker());
			}

			@Override
			public void mouseReleased(java.awt.event.MouseEvent e) {
				btn.setBackground(LAVENDER_HOVER);
			}
		});

		return btn;
	}

	// =========================
	// Side Menu
	// =========================
	private JComponent buildSideMenu() {
		JPanel side = new JPanel();
		side.setBackground(Color.WHITE);
		side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 235)));
		side.setPreferredSize(new Dimension(200, 10));
		side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));

		side.add(Box.createVerticalStrut(14));
		side.add(menuButton("나의 홈", CARD_HOME));
		side.add(Box.createVerticalStrut(6));
		side.add(menuButton("나의 도전", CARD_CHALLENGE));
		side.add(Box.createVerticalStrut(6));
		side.add(menuButton("AI 분석", CARD_AI));
		side.add(Box.createVerticalStrut(6));
		side.add(menuButton("커뮤니티", CARD_COMMUNITY));
		side.add(Box.createVerticalStrut(6));
		side.add(menuButton("질문하기", CARD_QNA));
		side.add(Box.createVerticalGlue());

		return side;
	}

	private JButton menuButton(String text, String key) {
		JButton b = new JButton(text);
		b.setFocusPainted(false);
		b.setHorizontalAlignment(SwingConstants.LEFT);
		b.setFont(UITheme.BODY_MED);
		b.setForeground(UITheme.TEXT);
		b.setBackground(Color.WHITE);
		b.setBorder(new EmptyBorder(12, 14, 12, 14));
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
		b.setAlignmentX(Component.LEFT_ALIGNMENT);
		b.addActionListener(e -> showCard(key));
		return b;
	}

	// =========================
	// Etti Help Bar
	// =========================
	private JComponent buildEttiHelpBar() {
		JPanel help = new JPanel(new BorderLayout());
		help.setBackground(UITheme.BG);
		help.setBorder(new EmptyBorder(14, 18, 10, 18));

		JPanel bubble = new JPanel(new BorderLayout(12, 0));
		bubble.setBackground(Color.WHITE);
		bubble.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(230, 230, 235), 1, true), new EmptyBorder(12, 20, 12, 12)));

		JLabel etti = new JLabel();
		Icon ettiIcon = createHiDPIIcon(ETTI_PATH, 52, true);
		if (ettiIcon != null) {
			etti.setIcon(ettiIcon);
		} else {
			etti.setText("에티");
			etti.setHorizontalAlignment(SwingConstants.CENTER);
		}
		etti.setPreferredSize(new Dimension(52, 52));

		JPanel text = new JPanel();
		text.setOpaque(false);
		text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

		JLabel t1 = new JLabel("오늘도 한 줄 기록해볼까요?");
		t1.setFont(UITheme.BODY_MED);
		t1.setForeground(UITheme.TEXT);

		JLabel t2 = new JLabel("왼쪽 메뉴에서 화면을 이동할 수 있어요.");
		t2.setFont(UITheme.CAPTION);
		t2.setForeground(new Color(120, 120, 120));

		text.add(t1);
		text.add(Box.createVerticalStrut(4));
		text.add(t2);

		bubble.add(etti, BorderLayout.WEST);
		bubble.add(text, BorderLayout.CENTER);

		help.add(bubble, BorderLayout.CENTER);
		return help;
	}

	// =========================
	// Placeholder Screens
	// =========================
	private JComponent buildPlaceholder(String title) {
		JPanel p = new JPanel(new BorderLayout());
		p.setBackground(UITheme.BG);
		p.setBorder(new EmptyBorder(18, 18, 18, 18));

		JPanel card = new JPanel(new BorderLayout());
		card.setBackground(Color.WHITE);
		card.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(230, 230, 235), 1, true), new EmptyBorder(18, 18, 18, 18)));

		JLabel l = new JLabel(title);
		l.setFont(UITheme.BODY_MED);
		l.setForeground(UITheme.TEXT);

		card.add(l, BorderLayout.NORTH);
		p.add(card, BorderLayout.CENTER);
		return p;
	}

	// =========================
	// Card Control
	// =========================
	private void showCard(String key) {
		cardLayout.show(contentCards, key);

		boolean isChallenge = CARD_CHALLENGE.equals(key);

		searchBar.setVisible(isChallenge);

		// 홈으로 돌아갈 때 검색어/필터 초기화
		if (!isChallenge) {
			searchBar.setQuery("");
			challengeView.setQuery("");
		}

		if (topArea != null) {
			topArea.revalidate();
			topArea.repaint();
		}
	}

	// =========================
	// Insight State (HomeView가 콜백으로 접근)
	// =========================
	private String getCurrentInsightText() {
		return currentInsightText;
	}

	private void setCurrentInsightText(String text) {
		this.currentInsightText = text;
	}
}

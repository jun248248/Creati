package com.creati.ui.main;

import com.creati.ui.main.MainUiParts.CircleAvatar;
import com.creati.ui.main.MainUiParts.RoundedButton;
import com.creati.ui.main.MainUiParts.ShadowLabel;
import com.creati.util.FontKit;
import com.creati.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicMenuItemUI;

import static com.creati.ui.main.MainUiParts.*;

import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Objects;

/**
 * MainFrame
 */
public class MainFrame extends JFrame {

	// resources (src/main/resources)
	private static final String ETTI_RES = "/images/etti/etti_default.png";
	private static final String DEFAULT_PROFILE_RES = "/images/profile/default_profile.png";

	// cards
	private static final String CARD_HOME = "HOME";
	private static final String CARD_CHALLENGE = "CHALLENGE";
	private static final String CARD_AI = "AI";
	private static final String CARD_COMMUNITY = "COMMUNITY";
	private static final String CARD_QNA = "QNA";
	private static final String CARD_QNA_WRITE = "QNA_WRITE";
	private static final String CARD_STATS = "STATS";
	private static final String CARD_WRITE = "WRITE";
	private static final String CARD_LOG_DETAIL = "LOG_DETAIL";

	private final CardLayout cardLayout = new CardLayout();
	private final JPanel contentCards = new JPanel(cardLayout);
	private String currentCardKey = CARD_HOME;

	private final MainSearchBar searchBar = new MainSearchBar();
	private final ChallengeView challengeView = new ChallengeView();
	private WriteLogView writeLogView;
	private final AtomicReference<LogPost> currentPostRef = new AtomicReference<>();
	private final LogDetailView logDetailView = new LogDetailView(
		    () -> showCard(CARD_CHALLENGE),
		    null, // onRetry (아직 없으면 null로)
		    () -> openLogEdit(currentPostRef.get())
		);

	private JPanel topArea;

	private final String nickname;
	private final Image profileImage;

	private JPopupMenu writeMenu;

	// 월간 AI 인사이트: 이번 달 1개만 유지(기능 연결 전 임시 상태)
	private String currentInsightText = null;

	public MainFrame(String nickname, String profileResPath) {
		super("Creati - 메인");
		this.nickname = nickname;

		UITheme.ensureInit();
		UIManager.put("MenuItem.selectionForeground", Color.BLACK);
		UIManager.put("MenuItem.selectionBackground", new Color(245, 245, 248)); 
		UIManager.put("MenuItem.foreground", Color.BLACK);
		UIManager.put("MenuItem.disabledForeground", Color.BLACK);

		UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(new Color(210, 210, 220), 1));

		String res = (profileResPath == null || profileResPath.isBlank()) ? DEFAULT_PROFILE_RES : profileResPath;
		this.profileImage = loadImageResource(res);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1200, 760);
		setLocationRelativeTo(null);

		setContentPane(buildRoot());
		showCard(CARD_HOME);
	}

	public MainFrame(String nickname) {
		this(nickname, DEFAULT_PROFILE_RES);
	}

	private String makeSettingsIcon() {
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

		// LOG DETAIL (상세 화면)
		contentCards.add(logDetailView, CARD_LOG_DETAIL);

		// WRITE (새 성장 로그 작성)
		writeLogView = new WriteLogView(this,
				() -> showCard(CARD_CHALLENGE),
				() -> showCard(CARD_CHALLENGE)
		);
		contentCards.add(writeLogView, CARD_WRITE);

		// 기타
		contentCards.add(buildPlaceholder("통계 - 준비중"), CARD_STATS);
		contentCards.add(buildPlaceholder("AI 분석 - TODO UI"), CARD_AI);
		contentCards.add(buildPlaceholder("커뮤니티 - 공개 글 리스트 - TODO UI"), CARD_COMMUNITY);
		contentCards.add(buildPlaceholder("질문하기 - Q&A 게시판 - TODO UI"), CARD_QNA);
		contentCards.add(new QuestionWriteView(this,
				() -> showCard(CARD_HOME),
				() -> showCard(CARD_QNA)
		), CARD_QNA_WRITE);

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

		writeMenu = buildWriteMenu();

		writeBtn.addActionListener(e -> {
			if (writeMenu.isVisible()) {
				writeMenu.setVisible(false);
				return;
			}

			int popupW = 180;
			int itemH = 44;
			int count = writeMenu.getComponentCount();
			int popupH = itemH * count;

			for (Component c : writeMenu.getComponents()) {
				if (c instanceof JMenuItem mi) {
					mi.setPreferredSize(new Dimension(popupW, itemH));
					mi.setMinimumSize(new Dimension(popupW, itemH));
					mi.setMaximumSize(new Dimension(popupW, itemH));
				}
			}

			writeMenu.setPopupSize(popupW, popupH);
			int x = writeBtn.getWidth() - popupW;
			int y = writeBtn.getHeight();
			writeMenu.show(writeBtn, x, y);
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
	private JPopupMenu buildWriteMenu() {
		JPopupMenu menu = new JPopupMenu();
		menu.setBackground(Color.WHITE);
		menu.setOpaque(true);
		menu.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 220), 1));
		menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));

		JMenuItem newLog = createMenuItem("새 성장 로그 작성", () -> {
			if (writeLogView != null) writeLogView.startNew();
			showCard(CARD_WRITE);
		});
		JMenuItem ask = createMenuItem("질문하기", () -> showCard(CARD_QNA_WRITE));

		menu.add(newLog);
		menu.add(ask);

		return menu;
	}

	private JMenuItem createMenuItem(String text, Runnable action) {
		JMenuItem item = new JMenuItem(text);

		item.setFont(UITheme.BODY_MED);
		item.setForeground(Color.BLACK);

		item.setHorizontalAlignment(SwingConstants.LEFT);

		item.setOpaque(true);
		item.setBackground(Color.WHITE);
		item.setBorder(new EmptyBorder(12, 16, 12, 16));
		item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		item.setUI(new BasicMenuItemUI() {
			@Override
			protected void paintBackground(Graphics g, JMenuItem mi, Color bgColor) {
				ButtonModel m = mi.getModel();
				if (m.isArmed() || m.isRollover() || m.isPressed()) {
					g.setColor(new Color(245, 245, 248)); 
				} else {
					g.setColor(Color.WHITE);
				}
				g.fillRect(0, 0, mi.getWidth(), mi.getHeight());
			}

			@Override
			protected void paintText(Graphics g, JMenuItem mi, Rectangle textRect, String text) {
				g.setColor(Color.BLACK);
				super.paintText(g, mi, textRect, text);
			}
		});

		item.addActionListener(e -> {
			if (writeMenu != null)
				writeMenu.setVisible(false);
			action.run();
		});

		return item;
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
		side.add(menuButton("통계", CARD_STATS));
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

		Icon ettiIcon = createHiDPIIconResource(ETTI_RES, 52, true);
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
	public void openLogDetail(LogPost post) {
		currentPostRef.set(post);
		logDetailView.bind(post);
		showCard(CARD_LOG_DETAIL);
	}

	public void openLogEdit(LogPost post) {
		if (post == null) {
			JOptionPane.showMessageDialog(this, "수정할 기록을 찾지 못했어요.");
			return;
		}
		writeLogView.beginEdit(post, updated -> {
			openLogDetail(updated);
		});
		showCard(CARD_WRITE);
	}

	public void navigateToAi() {
		showCard(CARD_AI);
	}

	private void showCard(String key) {
		if (!Objects.equals(currentCardKey, key)) {
			if (Objects.equals(currentCardKey, CARD_WRITE) && !Objects.equals(key, CARD_WRITE) && writeLogView != null) {
				if (writeLogView.isDirty()) {
					boolean ok = writeLogView.confirmLeave();
					if (!ok) return;
				}
			}
		}
		cardLayout.show(contentCards, key);
		currentCardKey = key;

		boolean isChallenge = CARD_CHALLENGE.equals(key);
		boolean isWrite = CARD_WRITE.equals(key) || CARD_QNA_WRITE.equals(key);

		searchBar.setVisible(isChallenge);
		if (topArea != null) {
			topArea.setVisible(!isWrite);
		}

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
	// Insight State
	// =========================
	private String getCurrentInsightText() {
		return currentInsightText;
	}

	private void setCurrentInsightText(String newText) {
		currentInsightText = newText;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new MainFrame("지현").setVisible(true));
	}
}

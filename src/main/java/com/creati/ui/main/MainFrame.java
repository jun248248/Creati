package com.creati.ui.main;


import com.creati.ui.components.CircleAvatar;
import com.creati.ui.components.RoundedButton;
import com.creati.ui.components.ShadowLabel;
import com.creati.ui.navigation.Navigator;
import com.creati.ui.navigation.Route;
import com.creati.util.FontKit;
import com.creati.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicMenuItemUI;

import com.creati.model.LogPost;
import static com.creati.ui.main.MainUiParts.*;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;


public class MainFrame extends JFrame {

	
	private static final String ETTI_RES = "/images/etti/etti_default.png";
	private static final String DEFAULT_PROFILE_RES = "/images/profile/default_profile.png";

	
	private static final String CARD_HOME = "HOME";
	private static final String CARD_CHALLENGE = "CHALLENGE";
	private static final String CARD_AI = "AI";
	private static final String CARD_COMMUNITY = "COMMUNITY";
	private static final String CARD_QNA = "QNA";
	private static final String CARD_QNA_WRITE = "QNA_WRITE";
	private static final String CARD_STATS = "STATS";
	private static final String CARD_WRITE = "WRITE";
	private static final String CARD_LOG_DETAIL = "LOG_DETAIL";
	private static final String CARD_QNA_DETAIL = "QNA_DETAIL";

	private final CardLayout cardLayout = new CardLayout();
	private final Navigator navigator = new Navigator(this);
	private final JPanel contentCards = new JPanel(cardLayout);
	private String currentCardKey = CARD_HOME;

	private final MainSearchBar searchBar = new MainSearchBar();
	private final ChallengeView challengeView = new ChallengeView();
	private WriteLogView writeLogView;
	private AiAnalysisView aiAnalysisView;
	private final LogDetailView logDetailView = new LogDetailView(() -> showCard(CARD_CHALLENGE),
			() -> openLogEdit(AppState.get().getSelectedLog()));
	private final CommunityView communityView = new CommunityView();
	private final QuestionView questionView = new QuestionView();
	private QuestionWriteView questionWriteView;

	private final AtomicReference<LogPost> currentQnaPostRef = new AtomicReference<>();
	private final QuestionDetailView questionDetailView =
	        new QuestionDetailView(() -> navigator.go(Route.QNA), null, null);

	private JPanel topArea;

	private final String nickname;
	private final Image profileImage;

	private JPopupMenu writeMenu;

	
	private String currentInsightText = null;

	public MainFrame() {
		super("Creati - 메인");
		UITheme.ensureInit();
		UIManager.put("MenuItem.selectionForeground", UITheme.BLACK);
		UIManager.put("MenuItem.selectionBackground", UITheme.RGB_245_245_248);
		UIManager.put("MenuItem.foreground", UITheme.BLACK);
		UIManager.put("MenuItem.disabledForeground", UITheme.BLACK);

		UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(UITheme.RGB_210_210_220, 1));

				
		com.creati.model.User user = AppState.get().getCurrentUser();
		this.nickname = (user == null || user.getNickname() == null || user.getNickname().isBlank())
				? "사용자"
				: user.getNickname();

		String res = (user == null || user.getProfileResPath() == null || user.getProfileResPath().isBlank())
				? DEFAULT_PROFILE_RES
				: user.getProfileResPath();
		this.profileImage = loadImageResource(res);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1200, 760);
		setLocationRelativeTo(null);

		setContentPane(buildRoot());
		showCard(CARD_HOME);
	}

	public MainFrame(String nickname) {
		
		this();
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

		
		topArea = new JPanel();
		topArea.setOpaque(false);
		topArea.setLayout(new BoxLayout(topArea, BoxLayout.Y_AXIS));
		topArea.add(buildEttiHelpBar());
		topArea.add(searchBar);

		searchBar.setVisible(false); 
		center.add(topArea, BorderLayout.NORTH);

		
		searchBar.setOnSearch(challengeView::setQuery);

		contentCards.setBackground(UITheme.BG);

		
		contentCards.add(new MainHomeView(this::getCurrentInsightText, this::setCurrentInsightText), CARD_HOME);

		
		contentCards.add(challengeView, CARD_CHALLENGE);

		
		contentCards.add(logDetailView, CARD_LOG_DETAIL);

		
		writeLogView = new WriteLogView(this, () -> showCard(CARD_CHALLENGE), () -> showCard(CARD_CHALLENGE));
		contentCards.add(writeLogView, CARD_WRITE);

		
		contentCards.add(communityView, CARD_COMMUNITY);

		
		contentCards.add(questionView, CARD_QNA);
		questionWriteView = new QuestionWriteView(this,
				() -> showCard(CARD_QNA),
				() -> {
					questionView.refresh();
					showCard(CARD_QNA);
				}
		);
		contentCards.add(questionWriteView, CARD_QNA_WRITE);
		
		

		
		contentCards.add(questionDetailView, CARD_QNA_DETAIL);

		
		contentCards.add(buildPlaceholder("통계 - 준비중"), CARD_STATS);

		
		aiAnalysisView = new AiAnalysisView(this);
		contentCards.add(aiAnalysisView, CARD_AI);

		center.add(contentCards, BorderLayout.CENTER);
		root.add(center, BorderLayout.CENTER);

		return root;
	}

	
	
	
	private JComponent buildTopBar() {
		JPanel bar = new JPanel(new BorderLayout());
		bar.setBackground(UITheme.WHITE);
		bar.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.RGB_230_230_235),
				new EmptyBorder(12, 16, 12, 16)));

		ShadowLabel logo = new ShadowLabel("Creati", 30, UITheme.RGB_90_90_100);
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
		settingsBtn.setForeground(UITheme.RGB_130_130_145);
		settingsBtn.setBackground(UITheme.WHITE);
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

		
		RoundedButton writeBtn = new RoundedButton("새 글쓰기");
		writeBtn.setBackground(UITheme.ACCENT_PURPLE);
		writeBtn.setForeground(UITheme.WHITE);
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

	
	
	
	private JPopupMenu buildWriteMenu() {
		JPopupMenu menu = new JPopupMenu();
		menu.setBackground(UITheme.WHITE);
		menu.setOpaque(true);
		menu.setBorder(BorderFactory.createLineBorder(UITheme.RGB_210_210_220, 1));
		menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));

		JMenuItem newLog = createMenuItem("새 성장 로그 작성", () -> {
			if (writeLogView != null)
				writeLogView.startNew();
			showCard(CARD_WRITE);
		});
		JMenuItem ask = createMenuItem("질문하기", () -> {
			if (questionWriteView != null) questionWriteView.startNew();
			showCard(CARD_QNA_WRITE);
		});

		menu.add(newLog);
		menu.add(ask);

		return menu;
	}

	private JMenuItem createMenuItem(String text, Runnable action) {
		JMenuItem item = new JMenuItem(text);

		item.setFont(UITheme.BODY_MED);
		item.setForeground(UITheme.BLACK);

		item.setHorizontalAlignment(SwingConstants.LEFT);

		item.setOpaque(true);
		item.setBackground(UITheme.WHITE);
		item.setBorder(new EmptyBorder(12, 16, 12, 16));
		item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		item.setUI(new BasicMenuItemUI() {
			@Override
			protected void paintBackground(Graphics g, JMenuItem mi, Color bgColor) {
				ButtonModel m = mi.getModel();
				if (m.isArmed() || m.isRollover() || m.isPressed()) {
					g.setColor(UITheme.RGB_245_245_248);
				} else {
					g.setColor(UITheme.WHITE);
				}
				g.fillRect(0, 0, mi.getWidth(), mi.getHeight());
			}

			@Override
			protected void paintText(Graphics g, JMenuItem mi, Rectangle textRect, String text) {
				g.setColor(UITheme.BLACK);
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

	
	
	
	private JComponent buildSideMenu() {
		JPanel side = new JPanel();
		side.setBackground(UITheme.WHITE);
		side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.RGB_230_230_235));
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
		b.setBackground(UITheme.WHITE);
		b.setBorder(new EmptyBorder(12, 14, 12, 14));
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
		b.setAlignmentX(Component.LEFT_ALIGNMENT);
		b.addActionListener(e -> showCard(key));
		return b;
	}

	
	
	
	private JComponent buildEttiHelpBar() {
		JPanel help = new JPanel(new BorderLayout());
		help.setBackground(UITheme.BG);
		help.setBorder(new EmptyBorder(14, 18, 10, 18));

		JPanel bubble = new JPanel(new BorderLayout(12, 0));
		bubble.setBackground(UITheme.WHITE);
		bubble.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UITheme.RGB_230_230_235, 1, true), new EmptyBorder(12, 20, 12, 12)));

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
		t2.setForeground(UITheme.RGB_120_120_120);

		text.add(t1);
		text.add(Box.createVerticalStrut(4));
		text.add(t2);

		bubble.add(etti, BorderLayout.WEST);
		bubble.add(text, BorderLayout.CENTER);

		help.add(bubble, BorderLayout.CENTER);
		return help;
	}

	
	
	
	private JComponent buildPlaceholder(String title) {
		JPanel p = new JPanel(new BorderLayout());
		p.setBackground(UITheme.BG);
		p.setBorder(new EmptyBorder(18, 18, 18, 18));

		JPanel card = new JPanel(new BorderLayout());
		card.setBackground(UITheme.WHITE);
		card.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UITheme.RGB_230_230_235, 1, true), new EmptyBorder(18, 18, 18, 18)));

		JLabel l = new JLabel(title);
		l.setFont(UITheme.BODY_MED);
		l.setForeground(UITheme.TEXT);

		card.add(l, BorderLayout.NORTH);
		p.add(card, BorderLayout.CENTER);
		return p;
	}

	
	
	
	public void openLogDetail(LogPost post) {
		if (post == null) {
			JOptionPane.showMessageDialog(this, "기록을 찾지 못했어요.");
			return;
		}
		AppState.get().setSelectedLogId(post.id);
		logDetailView.bind(post);
		showCard(CARD_LOG_DETAIL);
	}

	public void openQnaDetail(LogPost post) {
		if (post == null) {
			JOptionPane.showMessageDialog(this, "질문을 찾지 못했어요.");
			return;
		}
		currentQnaPostRef.set(post);
		questionDetailView.bind(post);
		navigator.go(Route.QNA_DETAIL);
	}

	public Navigator navigator() {
		return navigator;
	}

	
	public void showHome() {
		showCard(CARD_HOME);
	}

	public void showChallenge() {
		showCard(CARD_CHALLENGE);
	}

	public void showAi() {
		showCard(CARD_AI);
	}

	public void showCommunity() {
		showCard(CARD_COMMUNITY);
	}

	public void showQna() {
		showCard(CARD_QNA);
	}

	public void showQnaWrite() {
		if (questionWriteView != null) questionWriteView.startNew();
		showCard(CARD_QNA_WRITE);
	}

	public void showStats() {
		showCard(CARD_STATS);
	}

	public void showWriteLog() {
		showCard(CARD_WRITE);
	}

	public void showLogDetail() {
		showCard(CARD_LOG_DETAIL);
	}

	public void showQnaDetail() {
		LogPost post = currentQnaPostRef.get();
		if (post == null) {
			showCard(CARD_QNA);
			return;
		}
		showCard(CARD_QNA_DETAIL);
	}

	public void go(Route route) {
		navigator.go(route);
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
			if (Objects.equals(currentCardKey, CARD_WRITE) && !Objects.equals(key, CARD_WRITE)
					&& writeLogView != null) {
				if (writeLogView.isDirty()) {
					boolean ok = writeLogView.confirmLeave();
					if (!ok)
						return;
				}
			}
		}

		cardLayout.show(contentCards, key);
		currentCardKey = key;

		
		if (Objects.equals(key, CARD_AI) && aiAnalysisView != null) {
			aiAnalysisView.onActivated();
		}

		boolean showSearch = Objects.equals(key, CARD_CHALLENGE) || Objects.equals(key, CARD_QNA)
				|| Objects.equals(key, CARD_COMMUNITY);

		searchBar.setVisible(showSearch);
		searchBar.clear();

		if (Objects.equals(key, CARD_CHALLENGE)) {
			searchBar.setOnSearch(challengeView::setQuery);
			challengeView.clearSearch();
		} else if (Objects.equals(key, CARD_QNA)) {
			searchBar.setOnSearch(questionView::setQuery);
			questionView.clearSearch();
		} else if (Objects.equals(key, CARD_COMMUNITY)) {
			searchBar.setOnSearch(communityView::setQuery);
			communityView.clearSearch();
		} else {
			searchBar.setOnSearch(s -> {
			});
		}
		if (topArea != null) {
			topArea.revalidate();
			topArea.repaint();
		}
	}

	
	
	
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

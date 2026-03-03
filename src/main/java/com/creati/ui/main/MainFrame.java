package com.creati.ui.main;


import com.creati.ui.components.CircleAvatar;
import com.creati.ui.components.RoundedButton;
import com.creati.ui.components.ShadowLabel;
import com.creati.ui.navigation.Navigator;
import com.creati.ui.navigation.Route;
import com.creati.util.FontKit;
import com.creati.util.UITheme;
import com.creati.service.LogService;

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

	private static final String CARD_HOME       = "HOME";
	private static final String CARD_CHALLENGE  = "CHALLENGE";
	private static final String CARD_AI         = "AI";
	private static final String CARD_COMMUNITY  = "COMMUNITY";
	private static final String CARD_QNA        = "QNA";
	private static final String CARD_QNA_WRITE  = "QNA_WRITE";
	private static final String CARD_STATS      = "STATS";   // 로그 비교 (LogCompareView)
	private static final String CARD_WRITE      = "WRITE";
	private static final String CARD_LOG_DETAIL = "LOG_DETAIL";
	private static final String CARD_QNA_DETAIL = "QNA_DETAIL";

	private final CardLayout cardLayout = new CardLayout();
	private final Navigator navigator = new Navigator(this);
	private final JPanel contentCards = new JPanel(cardLayout);
	private String currentCardKey = CARD_HOME;
	private JLabel headerNickLabel;

	// ── 에티 도움말 라벨 & 매니저 ──────────────────────────────────
	private JLabel ettiTitleLabel;
	private JLabel ettiDescLabel;
	private JLabel ettiIconLabel;          // 머티리얼 아이콘 전용
	private EttiHelpManager ettiHelpManager;

	private final MainSearchBar searchBar = new MainSearchBar();
	private ChallengeView challengeView;
	private WriteLogView writeLogView;
	private AiAnalysisView aiAnalysisView;

	private final com.creati.dao.LogDao logDao = new com.creati.dao.LogDao(); 
	

	private LogCompareView logCompareView; // ── 로그 비교 뷰 ──
	private final LogDetailView logDetailView = new LogDetailView(
		    () -> showCard(CARD_CHALLENGE),                         // 뒤로가기
		    () -> openLogEdit(AppState.get().getSelectedLog()),     // 수정
		    this::onDeleteLogRequested                               // ✅ 삭제
		);

	private final CommunityView communityView = new CommunityView();

	private final AtomicReference<LogPost> currentQnaPostRef = new AtomicReference<>();

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
		UIManager.put("MenuItem.foreground",          UITheme.BLACK);
		UIManager.put("MenuItem.disabledForeground",  UITheme.BLACK);
		UIManager.put("PopupMenu.border",
				BorderFactory.createLineBorder(UITheme.RGB_210_210_220, 1));

		com.creati.model.User user = AppState.get().getCurrentUser();
		this.nickname = (user == null || user.getNickname() == null || user.getNickname().isBlank())
				? "사용자" : user.getNickname();

		String res = (user == null || user.getProfileResPath() == null || user.getProfileResPath().isBlank())
				? DEFAULT_PROFILE_RES : user.getProfileResPath();
		this.profileImage = loadImageResource(res);

		com.creati.dto.UserDto userDto = new com.creati.dto.UserDto();
		if (user != null) userDto.setId(user.getId());
		else              userDto.setId("testUser");
		this.challengeView = new ChallengeView(userDto);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1200, 760);
		setLocationRelativeTo(null);

		setContentPane(buildRoot());
		showCard(CARD_HOME);

		// EttiHelpManager 초기화 — buildRoot() 이후 라벨이 생성되므로 여기서 세팅
		if (ettiTitleLabel != null && ettiDescLabel != null) {
			ettiHelpManager = new EttiHelpManager(ettiTitleLabel, ettiDescLabel, ettiIconLabel);
			ettiHelpManager.setView(EttiHelpManager.ViewType.HOME);
		}
	}

	// ── 설정 아이콘 ───────────────────────────────────────────────
	private String makeSettingsIcon() {
		try   { return new String(Character.toChars(0xE8B8)); }
		catch (Exception e) { return "⚙"; }
	}

	// ── 루트 패널 ─────────────────────────────────────────────────
	private JComponent buildRoot() {
		JPanel root = new JPanel(new BorderLayout());
		root.setBackground(UITheme.BG);

		root.add(buildTopBar(),   BorderLayout.NORTH);
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

		// ── 카드 등록 ──
		contentCards.add(new MainHomeView(this::getCurrentInsightText, this::setCurrentInsightText), CARD_HOME);
		contentCards.add(challengeView, CARD_CHALLENGE);
		contentCards.add(logDetailView, CARD_LOG_DETAIL);

		writeLogView = new WriteLogView(
				this,
				() -> showCard(CARD_CHALLENGE),
				() -> { challengeView.refresh(); showCard(CARD_CHALLENGE); }
		);
		contentCards.add(writeLogView, CARD_WRITE);

		contentCards.add(communityView, CARD_COMMUNITY);

		// ── 로그 비교 (CARD_STATS) ──
		logCompareView = new LogCompareView();
		contentCards.add(logCompareView, CARD_STATS);

		// ── AI 분석 ──
		aiAnalysisView = new AiAnalysisView(this);
		contentCards.add(aiAnalysisView, CARD_AI);

		center.add(contentCards, BorderLayout.CENTER);
		root.add(center, BorderLayout.CENTER);
		return root;
	}

	// ── 상단 바 ───────────────────────────────────────────────────
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

		headerNickLabel = new JLabel(nickname);
		headerNickLabel.setFont(UITheme.BODY_MED);
		headerNickLabel.setForeground(UITheme.TEXT);

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
		settingsBtn.addActionListener(e -> openSettings());

		profileRow.add(avatar);
		profileRow.add(Box.createHorizontalStrut(10));
		profileRow.add(headerNickLabel);
		profileRow.add(Box.createHorizontalStrut(10));
		profileRow.add(settingsBtn);

		RoundedButton writeBtn = new RoundedButton("새 글쓰기");
		writeBtn.setBackground(UITheme.ACCENT_PURPLE);
		writeBtn.setForeground(UITheme.WHITE);
		writeBtn.setFont(UITheme.BODY_MED);

		writeMenu = buildWriteMenu();

		writeBtn.addActionListener(e -> {
			if (writeMenu.isVisible()) { writeMenu.setVisible(false); return; }
			int popupW = 180, itemH = 44;
			int count  = writeMenu.getComponentCount();
			int popupH = itemH * count;
			for (Component c : writeMenu.getComponents()) {
				if (c instanceof JMenuItem mi) {
					mi.setPreferredSize(new Dimension(popupW, itemH));
					mi.setMinimumSize  (new Dimension(popupW, itemH));
					mi.setMaximumSize  (new Dimension(popupW, itemH));
				}
			}
			writeMenu.setPopupSize(popupW, popupH);
			writeMenu.show(writeBtn, writeBtn.getWidth() - popupW, writeBtn.getHeight());
		});

		JPanel writeRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		writeRow.setOpaque(false);
		writeRow.add(writeBtn);

		right.add(profileRow, BorderLayout.NORTH);
		right.add(Box.createVerticalStrut(8), BorderLayout.CENTER);
		right.add(writeRow, BorderLayout.SOUTH);

		bar.add(logoWrap, BorderLayout.WEST);
		bar.add(right,    BorderLayout.EAST);
		return bar;
	}

	// ── 글쓰기 팝업 메뉴 ──────────────────────────────────────────
	private JPopupMenu buildWriteMenu() {
		JPopupMenu menu = new JPopupMenu();
		menu.setBackground(UITheme.WHITE);
		menu.setOpaque(true);
		menu.setBorder(BorderFactory.createLineBorder(UITheme.RGB_210_210_220, 1));
		menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));

		menu.add(createMenuItem("새 성장 로그 작성", () -> {
			if (writeLogView != null) writeLogView.startNew();
			showCard(CARD_WRITE);
		}));
		
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
			@Override protected void paintBackground(Graphics g, JMenuItem mi, Color bgColor) {
				ButtonModel m = mi.getModel();
				g.setColor((m.isArmed() || m.isRollover() || m.isPressed())
						? UITheme.RGB_245_245_248 : UITheme.WHITE);
				g.fillRect(0, 0, mi.getWidth(), mi.getHeight());
			}
			@Override protected void paintText(Graphics g, JMenuItem mi, Rectangle textRect, String text) {
				g.setColor(UITheme.BLACK);
				super.paintText(g, mi, textRect, text);
			}
		});
		item.addActionListener(e -> { if (writeMenu != null) writeMenu.setVisible(false); action.run(); });
		return item;
	}

	// ── 사이드 메뉴 ───────────────────────────────────────────────
	private JComponent buildSideMenu() {
		JPanel side = new JPanel();
		side.setBackground(UITheme.WHITE);
		side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.RGB_230_230_235));
		side.setPreferredSize(new Dimension(200, 10));
		side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));

		side.add(Box.createVerticalStrut(14));
		side.add(menuButton("나의 홈",    CARD_HOME));
		side.add(Box.createVerticalStrut(6));
		side.add(menuButton("나의 도전",  CARD_CHALLENGE));
		side.add(Box.createVerticalStrut(6));
		side.add(menuButton("로그 비교",  CARD_STATS));   // ← 로그 비교
		side.add(Box.createVerticalStrut(6));
		side.add(menuButton("AI 분석",    CARD_AI));
		side.add(Box.createVerticalStrut(6));
		side.add(menuButton("커뮤니티",   CARD_COMMUNITY));
		side.add(Box.createVerticalStrut(6));
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

	// ── 에티 도움말 바 ────────────────────────────────────────────
	private JComponent buildEttiHelpBar() {
		JPanel help = new JPanel(new BorderLayout());
		help.setBackground(UITheme.BG);
		help.setBorder(new EmptyBorder(14, 18, 10, 18));

		JPanel bubble = new JPanel(new BorderLayout(12, 0));
		bubble.setBackground(UITheme.WHITE);
		bubble.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UITheme.RGB_230_230_235, 1, true),
				new EmptyBorder(12, 20, 12, 12)));

		JLabel etti = new JLabel();
		Icon ettiIcon = createHiDPIIconResource(ETTI_RES, 52, true);
		if (ettiIcon != null) etti.setIcon(ettiIcon);
		else { etti.setText("에티"); etti.setHorizontalAlignment(SwingConstants.CENTER); }
		etti.setPreferredSize(new Dimension(52, 52));

		JPanel text = new JPanel();
		text.setOpaque(false);
		text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

		// 제목 라벨
		JLabel t1 = new JLabel("오늘도 한 줄 기록해볼까요?");
		t1.setFont(UITheme.BODY_MED);
		t1.setForeground(UITheme.TEXT);

		// 설명 라벨
		JLabel t2 = new JLabel("왼쪽 메뉴에서 화면을 이동할 수 있어요.");
		t2.setFont(UITheme.CAPTION);
		t2.setForeground(UITheme.RGB_120_120_120);
		t2.setAlignmentX(Component.LEFT_ALIGNMENT);

		// 머티리얼 아이콘 라벨 (제목 오른쪽)
		JLabel iconLbl = new JLabel("");
		iconLbl.setForeground(UITheme.ACCENT_PURPLE);
		iconLbl.setVisible(false);

		// UI Binding — 필드에 저장
		ettiTitleLabel = t1;
		ettiDescLabel  = t2;
		ettiIconLabel  = iconLbl;

		// 제목 + 아이콘 한 줄
		JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		titleRow.setOpaque(false);
		titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
		titleRow.add(t1);
		titleRow.add(iconLbl);

		// 설명 라벨 — LEFT 정렬을 위해 FlowLayout 래퍼 사용
		JPanel descRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		descRow.setOpaque(false);
		descRow.setAlignmentX(Component.LEFT_ALIGNMENT);
		descRow.add(t2);

		text.add(titleRow);
		text.add(Box.createVerticalStrut(2));
		text.add(descRow);

		bubble.add(etti, BorderLayout.WEST);
		bubble.add(text, BorderLayout.CENTER);
		help.add(bubble, BorderLayout.CENTER);
		return help;
	}

	// ── 플레이스홀더 (필요 시 사용) ───────────────────────────────
	private JComponent buildPlaceholder(String title) {
		JPanel p = new JPanel(new BorderLayout());
		p.setBackground(UITheme.BG);
		p.setBorder(new EmptyBorder(18, 18, 18, 18));
		JPanel card = new JPanel(new BorderLayout());
		card.setBackground(UITheme.WHITE);
		card.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UITheme.RGB_230_230_235, 1, true),
				new EmptyBorder(18, 18, 18, 18)));
		JLabel l = new JLabel(title);
		l.setFont(UITheme.BODY_MED);
		l.setForeground(UITheme.TEXT);
		card.add(l, BorderLayout.NORTH);
		p.add(card, BorderLayout.CENTER);
		return p;
	}

	// ── 공개 메서드 ───────────────────────────────────────────────
	public void openLogDetail(LogPost post) {
		if (post == null) { JOptionPane.showMessageDialog(this, "기록을 찾지 못했어요."); return; }
		AppState.get().setSelectedLogId(post.id);
		logDetailView.bind(post);
		showCard(CARD_LOG_DETAIL);
		openLogDetail(post, CARD_CHALLENGE);
	}
	
	public void openLogDetail(LogPost post, String backCardKey) {
		if (post == null) {
			JOptionPane.showMessageDialog(this, "기록을 찾지 못했어요.");
			return;
		}
		AppState.get().setSelectedLogId(post.id);
		logDetailView.setOnBack(() -> showCard(backCardKey));
		
		logDetailView.bind(post);
		showCard(CARD_LOG_DETAIL);
	}

	public void openQnaDetail(LogPost post) {
		if (post == null) { JOptionPane.showMessageDialog(this, "질문을 찾지 못했어요."); return; }
		currentQnaPostRef.set(post);
		navigator.go(Route.QNA_DETAIL);
	}

	public Navigator navigator()  { return navigator; }
	public void showHome()        { showCard(CARD_HOME); }
	public void showChallenge()   { showCard(CARD_CHALLENGE); }
	public void showAi()          { showCard(CARD_AI); }
	public void showCommunity()   { showCard(CARD_COMMUNITY); }
	public void showQna()         { showCard(CARD_QNA); }
	public void showStats()       { showCard(CARD_STATS); }
	public void showWriteLog()    { showCard(CARD_WRITE); }
	public void showLogDetail()   { showCard(CARD_LOG_DETAIL); }
	public void navigateToAi()    { showCard(CARD_AI); }


	public void showQnaDetail() {
		LogPost post = currentQnaPostRef.get();
		if (post == null) { showCard(CARD_QNA); return; }
		showCard(CARD_QNA_DETAIL);
	}

	public void go(Route route) { navigator.go(route); }

	public void openLogEdit(LogPost post) {
		if (post == null) { JOptionPane.showMessageDialog(this, "수정할 기록을 찾지 못했어요."); return; }
		writeLogView.beginEdit(post, updated -> openLogDetail(updated));
		showCard(CARD_WRITE);
	}

	public void openSettings() {
		SettingsDialog d = new SettingsDialog(this);
		d.setVisible(true);
	}

	public void refreshHeaderUser() {
		com.creati.model.User user = AppState.get().getCurrentUser();
		if (user == null) return;
		String newNick = (user.getNickname() == null || user.getNickname().isBlank())
				? "사용자" : user.getNickname();
		SwingUtilities.invokeLater(() -> {
			if (headerNickLabel != null) {
				headerNickLabel.setText(newNick);
				headerNickLabel.revalidate();
				headerNickLabel.repaint();
			}
		});
	}

	// ── 카드 전환 핵심 메서드 ─────────────────────────────────────
	private void showCard(String key) {
		if (!Objects.equals(currentCardKey, key)) {
			if (Objects.equals(currentCardKey, CARD_WRITE) && writeLogView != null) {
				if (writeLogView.isDirty()) {
					if (!writeLogView.confirmLeave()) return;
				}
			}
		}

		cardLayout.show(contentCards, key);
		currentCardKey = key;

		// View Mapping — 화면 전환 시 에티 도움말 교체
		if (ettiHelpManager != null) {
			switch (key) {
				case CARD_HOME      -> ettiHelpManager.setView(EttiHelpManager.ViewType.HOME);
				case CARD_CHALLENGE -> ettiHelpManager.setView(EttiHelpManager.ViewType.CHALLENGE);
				case CARD_STATS     -> ettiHelpManager.setView(EttiHelpManager.ViewType.LOG_COMPARE);
				case CARD_AI        -> ettiHelpManager.setView(EttiHelpManager.ViewType.AI_ANALYSIS);
				case CARD_COMMUNITY -> ettiHelpManager.setView(EttiHelpManager.ViewType.COMMUNITY);
				case CARD_QNA_WRITE -> ettiHelpManager.setView(EttiHelpManager.ViewType.QUESTION_WRITE);
				default -> {} // WRITE, LOG_DETAIL 등 서브뷰는 유지
			}
		}

		if (Objects.equals(key, CARD_AI) && aiAnalysisView != null) {
			aiAnalysisView.onActivated();
		}

		// 로그 비교 뷰 활성화 시 refresh
		if (Objects.equals(key, CARD_STATS) && logCompareView != null) {
			logCompareView.refresh();
		}

		boolean showSearch = Objects.equals(key, CARD_CHALLENGE)
				|| Objects.equals(key, CARD_QNA)
				|| Objects.equals(key, CARD_COMMUNITY);
		searchBar.setVisible(showSearch);
		searchBar.clear();

		if      (Objects.equals(key, CARD_CHALLENGE)) { searchBar.setOnSearch(challengeView::setQuery);  challengeView.clearSearch(); }
		else if (Objects.equals(key, CARD_COMMUNITY)) { searchBar.setOnSearch(communityView::setQuery);  communityView.clearSearch(); }
		else                                           { searchBar.setOnSearch(s -> {}); }

		if (topArea != null) { topArea.revalidate(); topArea.repaint(); }
	}

	// ── 월간 인사이트 ─────────────────────────────────────────────
	private String getCurrentInsightText()         { return currentInsightText; }
	private void   setCurrentInsightText(String t) { currentInsightText = t; }

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
	}

	private void deleteSelectedLog() {
	    String id = AppState.get().getSelectedLogId();
	    if (id == null || id.isBlank()) {
	        JOptionPane.showMessageDialog(this, "삭제할 글을 찾지 못했어요.");
	        return;
	    }

	    long logId;
	    try {
	        logId = Long.parseLong(id);
	    } catch (NumberFormatException e) {
	        JOptionPane.showMessageDialog(this, "삭제 ID가 올바르지 않아요: " + id);
	        return;
	    }

	    String userId = (AppState.get().getCurrentUser() == null) ? null : AppState.get().getCurrentUser().getId();
	    if (userId == null || userId.isBlank()) {
	        JOptionPane.showMessageDialog(this, "로그인 정보가 없어서 삭제할 수 없어요.");
	        return;
	    }

	    int r = JOptionPane.showConfirmDialog(this, "이 글을 삭제할까요?", "삭제 확인", JOptionPane.YES_NO_OPTION);
	    if (r != JOptionPane.YES_OPTION) return;

	    boolean ok = logDao.deleteLogWithExtras(logId, userId);
	    if (!ok) {
	        JOptionPane.showMessageDialog(this, "삭제 실패! (권한/ID/FK 확인)");
	        return;
	    }

	    // 화면/상태 갱신
	    AppState.get().clearSelectedLog();
	    challengeView.refresh();
	    communityView.refresh(); // 공개글 목록에도 영향

	    showCard(CARD_CHALLENGE);
	    JOptionPane.showMessageDialog(this, "삭제 완료!");
	}
	
	private void onDeleteLogRequested() {
	    String id = AppState.get().getSelectedLogId();
	    if (id == null || id.isBlank()) {
	        JOptionPane.showMessageDialog(this, "삭제할 글을 찾지 못했어요.");
	        return;
	    }

	    int r = JOptionPane.showConfirmDialog(this, "이 글을 삭제할까요?", "삭제 확인", JOptionPane.YES_NO_OPTION);
	    if (r != JOptionPane.YES_OPTION) return;

	    com.creati.model.User u = AppState.get().getCurrentUser();
	    String userId = (u != null) ? u.getId() : null;
	    if (userId == null || userId.isBlank()) {
	        JOptionPane.showMessageDialog(this, "로그인 정보가 없어서 삭제할 수 없어요.");
	        return;
	    }

	    long logId;
	    try {
	        logId = Long.parseLong(id);
	    } catch (NumberFormatException e) {
	        JOptionPane.showMessageDialog(this, "잘못된 글 ID라 삭제할 수 없어요. (id=" + id + ")");
	        return;
	    }

	    boolean ok = logDao.deleteLogWithExtras(logId, userId); // LogDao에 복구한 메서드
	    if (!ok) {
	        JOptionPane.showMessageDialog(this, "삭제 실패! (권한이 없거나 DB 오류)");
	        return;
	    }

	    // 선택 상태 정리
	    AppState.get().clearSelectedLog();

	    // 목록/커뮤니티 갱신 (커뮤니티에 refresh() 없으면 reloadFromStore 같은 걸 호출)
	    if (challengeView != null) challengeView.refresh();
	    // communityView에 refresh()가 없으면 CommunityView에 public void refresh() { reloadFromStore(); applyFilter(); } 같은 메서드 추가해두면 좋음
	    // if (communityView != null) communityView.refresh();

	    showCard(CARD_CHALLENGE);
	    JOptionPane.showMessageDialog(this, "삭제 완료!");
	}
	
}


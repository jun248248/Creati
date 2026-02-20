package com.creati.ui.main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.creati.util.FontKit;
import com.creati.util.UITheme;
import com.creati.model.LogStatus;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * ChallengeView (나의 도전) - 좌측: 분야 리스트 - 우측: 로그 목록(상태 칩 + 제목 + 생성일 우측 정렬)
 *
 * TODO(DB)  1) allLogs를 DB에서 조회한 List<LogItem>로 교체 2) 카테고리도 DB에서 distinct로
 * 가져오거나 Enum/테이블로 관리 3) 검색(query) + 카테고리(selectedCategory) 조건으로 SQL WHERE 구성 4)
 * 공개 여부(isPublic)는 DB 컬럼 is_public(TINYINT) 또는 visibility(enum)로 매핑
 */
public class ChallengeView extends JPanel {

	// ===== 상태 =====
	private String query = "";
	private String selectedCategory = null; // null이면 전체

	// ===== UI =====
	private final DefaultListModel<String> catModel = new DefaultListModel<>();
	private final JList<String> catList = new JList<>(catModel);

	private final DefaultListModel<LogItem> logModel = new DefaultListModel<>();
	private final JList<LogItem> logList = new JList<>(logModel);

	private final JLabel headerTitle = new JLabel("나의 도전");
	private final JLabel headerSub = new JLabel("분야별로 내 성장 로그를 목록으로 확인하세요.");

	private final JLabel rightTitle = new JLabel("전체");
	private final JLabel rightCount = new JLabel("0개");

	// hover index
	private int hoverCatIndex = -1;
	private int hoverLogIndex = -1;

	// ===== 샘플 데이터 (TODO(DB)  DB에서 가져오도록 교체) =====
	private final List<LogItem> allLogs = new ArrayList<>(
			List.of(new LogItem("영상", "일상 / 브이로그", Status.FAIL, "유튜브 쇼츠 실패 분석", LocalDate.of(2026, 3, 18), true),
					new LogItem("영상", "일상 / 브이로그", Status.FAIL, "편집 루틴 정리", LocalDate.of(2026, 3, 16), false),
					new LogItem("영상", "일상 / 브이로그", Status.IN_PROGRESS, "촬영 루틴 만들기", LocalDate.of(2026, 3, 15), true),
					new LogItem("이미지", "일상 / 브이로그", Status.SUCCESS, "썸네일 디자인 시도", LocalDate.of(2026, 3, 14), true),
					new LogItem("이미지", "일상 / 브이로그", Status.FAIL, "포스터 레이아웃 실패", LocalDate.of(2026, 3, 13), false),
					new LogItem("글", "일상 / 브이로그", Status.FAIL, "블로그 글쓰기 실패", LocalDate.of(2026, 3, 12), true),
					new LogItem("음악", "일상 / 브이로그", Status.SUCCESS, "집중용 플레이리스트", LocalDate.of(2026, 3, 10), true),
					new LogItem("기타", "일상 / 브이로그", Status.IN_PROGRESS, "하루 회고", LocalDate.of(2026, 3, 9), false)));

	// 카테고리 순서 고정 (TODO(DB)  DB에서 동적으로 생성 가능)
	private final List<String> categories = List.of("전체", "영상", "이미지", "글", "음악", "기타");

	public ChallengeView() {
		UITheme.ensureInit();
		FontKit.init();

		setLayout(new BorderLayout());
		setBackground(UITheme.BG);

		add(buildHeader(), BorderLayout.NORTH);
		add(buildExplorer(), BorderLayout.CENTER);

		loadCategories();
		catList.setSelectedIndex(0); // 전체
		applyFilter();
	}

	/* ======================= MainSearchBar 연동 API ======================= */

	public void setQuery(String q) {
		this.query = (q == null) ? "" : q.trim();
		applyFilter();
	}

	public void clearSearch() {
		setQuery("");
	}

	/* ======================= UI ======================= */

	private JComponent buildHeader() {
		JPanel wrap = new JPanel(new BorderLayout());
		wrap.setOpaque(false);
		wrap.setBorder(new EmptyBorder(10, 18, 6, 18));

		headerTitle.setFont(UITheme.BODY_MED);
		headerTitle.setForeground(UITheme.TEXT);

		headerSub.setFont(UITheme.CAPTION);
		headerSub.setForeground(new Color(120, 120, 120));

		JPanel texts = new JPanel();
		texts.setOpaque(false);
		texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
		texts.add(headerTitle);
		texts.add(Box.createVerticalStrut(6));
		texts.add(headerSub);

		wrap.add(texts, BorderLayout.WEST);
		return wrap;
	}

	private JComponent buildExplorer() {
		JPanel wrap = new JPanel(new BorderLayout());
		wrap.setOpaque(false);
		wrap.setBorder(new EmptyBorder(0, 18, 18, 18));

		// 좌측 카드
		JPanel leftCard = makeCardPanel();
		leftCard.setLayout(new BorderLayout(0, 10));
		leftCard.setBorder(BorderFactory.createCompoundBorder(leftCard.getBorder(), new EmptyBorder(12, 12, 12, 12)));

		JLabel leftTitle = new JLabel("분야");
		leftTitle.setFont(UITheme.BODY_MED);
		leftTitle.setForeground(UITheme.TEXT);

		setupCategoryList();

		JScrollPane catScroll = new JScrollPane(catList);
		catScroll.setBorder(BorderFactory.createEmptyBorder());
		catScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		catScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		leftCard.add(leftTitle, BorderLayout.NORTH);
		leftCard.add(catScroll, BorderLayout.CENTER);

		// 우측 카드
		JPanel rightCard = makeCardPanel();
		rightCard.setLayout(new BorderLayout());
		rightCard.setBorder(BorderFactory.createCompoundBorder(rightCard.getBorder(), new EmptyBorder(12, 12, 12, 12)));

		setupLogList();

		JPanel rightTop = new JPanel();
		rightTop.setOpaque(false);
		rightTop.setLayout(new BoxLayout(rightTop, BoxLayout.Y_AXIS));

		JPanel titleRow = new JPanel();
		titleRow.setOpaque(false);
		titleRow.setLayout(new BoxLayout(titleRow, BoxLayout.X_AXIS));

		rightTitle.setFont(UITheme.BODY_MED);
		rightTitle.setForeground(UITheme.TEXT);

		rightCount.setFont(UITheme.CAPTION);
		rightCount.setForeground(new Color(120, 120, 120));

		titleRow.add(rightTitle);
		titleRow.add(Box.createHorizontalStrut(10));
		titleRow.add(rightCount);
		titleRow.add(Box.createHorizontalGlue());

		rightTop.add(titleRow);
		rightTop.add(Box.createVerticalStrut(10));

		JScrollPane logScroll = new JScrollPane(logList);
		logScroll.setBorder(BorderFactory.createEmptyBorder());
		logScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		logScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		rightCard.add(rightTop, BorderLayout.NORTH);
		rightCard.add(logScroll, BorderLayout.CENTER);

		// SplitPane
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftCard, rightCard);
		split.setBorder(BorderFactory.createEmptyBorder());
		split.setDividerSize(6);
		split.setContinuousLayout(true);
		split.setResizeWeight(0.28);
		split.setOpaque(false);

		wrap.add(split, BorderLayout.CENTER);
		return wrap;
	}

	private JPanel makeCardPanel() {
		JPanel p = new JPanel();
		p.setBackground(Color.WHITE);
		p.setBorder(BorderFactory.createLineBorder(new Color(235, 235, 240), 1, true));
		return p;
	}

	/* ======================= 카테고리 목록 ======================= */

	private void setupCategoryList() {
		catList.setFont(UITheme.BODY);
		catList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		catList.setFixedCellHeight(42);
		catList.setBackground(Color.WHITE);

		catList.setSelectionBackground(new Color(0, 0, 0, 0));
		catList.setSelectionForeground(UITheme.TEXT);
		catList.setFocusable(false);

		catList.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				hoverCatIndex = catList.locationToIndex(e.getPoint());
				catList.repaint();
			}
		});
		catList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				hoverCatIndex = -1;
				catList.repaint();
			}
		});

		catList.setCellRenderer(new CategoryCellRenderer(() -> hoverCatIndex));

		catList.addListSelectionListener(e -> {
			if (e.getValueIsAdjusting())
				return;
			String v = catList.getSelectedValue();
			if (v == null)
				return;

			selectedCategory = "전체".equals(v) ? null : v;
			applyFilter();
		});
	}

	private void loadCategories() {
		catModel.clear();
		for (String c : categories)
			catModel.addElement(c);
	}

	/* ======================= 로그 목록 ======================= */

	private void setupLogList() {
		logList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		logList.setFixedCellHeight(42);
		logList.setBackground(Color.WHITE);

		logList.setSelectionBackground(new Color(0, 0, 0, 0));
		logList.setSelectionForeground(UITheme.TEXT);
		logList.setFocusable(false);

		logList.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				hoverLogIndex = logList.locationToIndex(e.getPoint());
				logList.repaint();
			}
		});
		logList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				hoverLogIndex = -1;
				logList.repaint();
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 1) {
					LogItem item = logList.getSelectedValue();
					if (item == null)
						return;

					Window w = SwingUtilities.getWindowAncestor(ChallengeView.this);
					if (w instanceof MainFrame) {
						((MainFrame) w).openLogDetail(toPost(item));
						return;
					}

					// fallback
					JOptionPane.showMessageDialog(ChallengeView.this, "선택한 글:\n" + item.title, "상세 보기",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});

		logList.setCellRenderer(new LogRowRenderer(() -> hoverLogIndex));
	}

	/* ======================= 필터 적용 ======================= */

	private void applyFilter() {
		logModel.clear();

		// TODO (DB)
		// DB라면: SELECT ... WHERE (category=? OR 전체) AND title LIKE ? ORDER BY
		// createdAt DESC
		// 현재는 in-memory 필터
		List<LogItem> filtered = new ArrayList<>();
		for (LogItem item : allLogs) {
			boolean okCat = (selectedCategory == null) || item.category.equals(selectedCategory);
			boolean okQuery = query.isEmpty() || item.title.contains(query);
			if (okCat && okQuery)
				filtered.add(item);
		}

		for (LogItem item : filtered)
			logModel.addElement(item);

		rightTitle.setText(selectedCategory == null ? "전체" : selectedCategory);
		rightCount.setText(filtered.size() + "개");

		revalidate();
		repaint();
	}

	/* ======================= 데이터 모델 ======================= */

	private enum Status {
		FAIL, SUCCESS, IN_PROGRESS
	}

	private static class LogItem {
		final String category;
		final String subCategory;
		final Status status;
		final String title;
		final LocalDate createdAt;
		final boolean isPublic;

		final String whatIDid;
		final String feeling;
		final String difficulty;
		final String learning;
		final String retryPlan;
		final String link;

		// TODO (DB) 추후 id 추가
		// final long id;

		LogItem(String category, String subCategory, Status status, String title, LocalDate createdAt, boolean isPublic,
				String whatIDid, String feeling, String difficulty, String learning, String retryPlan, String link) {
			this.category = category;
			this.subCategory = subCategory;
			this.status = status;
			this.title = title;
			this.createdAt = createdAt;
			this.isPublic = isPublic;
			this.whatIDid = whatIDid;
			this.feeling = feeling;
			this.difficulty = difficulty;
			this.learning = learning;
			this.retryPlan = retryPlan;
			this.link = link;
		}

		LogItem(String category, String subCategory, Status status, String title, LocalDate createdAt,
				boolean isPublic) {
			this(category, subCategory, status, title, createdAt, isPublic, "(더미) 오늘 한 일을 정리해 두었어요.",
					"(더미) 어떤 부분이 잘 됐고 어떤 부분이 아쉬웠는지 기록해 두었어요.", "(더미) 막혔던 지점과 원인을 적어 두었어요.",
					"(더미) 다음에는 이렇게 해보면 좋겠다고 정리해 두었어요.", "(더미) 다음 주기에는 작은 단위로 다시 시도해 볼게요.", "https://example.com");
		}

		LogItem(String category, Status status, String title, LocalDate createdAt, boolean isPublic) {
			this(category, "일상 / 브이로그", status, title, createdAt, isPublic);
		}
	}

	private LogPost toPost(LogItem item) {
		LogStatus st;
		switch (item.status) {
		case FAIL:
			st = LogStatus.NEEDS_IMPROVEMENT;
			break;
		case SUCCESS:
			st = LogStatus.DONE;
			break;
		default:
			st = LogStatus.IN_PROGRESS;
		}

		return new LogPost(null, item.category,
				(item.subCategory == null || item.subCategory.isBlank()) ? "일상 / 브이로그" : item.subCategory, st,
				item.title, item.createdAt, item.isPublic, item.whatIDid, item.feeling, item.difficulty, item.learning,
				item.retryPlan, item.link);
	}
	/* ======================= Renderers ======================= */

	private interface HoverIndexProvider {
		int getHoverIndex();
	}

	private static class CategoryCellRenderer extends DefaultListCellRenderer {
		private final HoverIndexProvider hover;
		private final Color selBg = new Color(0xF5F3FF);
		private final Color hoverBg = new Color(0xFAF9FF);

		CategoryCellRenderer(HoverIndexProvider hover) {
			this.hover = hover;
			setOpaque(true);
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, false);

			String name = String.valueOf(value);

			c.setText(name);
			c.setFont(UITheme.BODY);
			c.setForeground(UITheme.TEXT);
			c.setBorder(new EmptyBorder(10, 12, 10, 12));

			boolean isHover = (hover.getHoverIndex() == index);

			if (isSelected)
				c.setBackground(selBg);
			else if (isHover)
				c.setBackground(hoverBg);
			else
				c.setBackground(Color.WHITE);

			c.setIcon(makeMaterialIconForCategory(name));
			c.setIconTextGap(12);

			return c;
		}

		private static String mi(int codePointHex) {
			return new String(Character.toChars(codePointHex));
		}

		private static Icon makeMaterialIconForCategory(String category) {
			String glyph = switch (category) {
			case "전체" -> mi(0xE2C7); // folder
			case "영상" -> mi(0xE02C); // movie
			case "이미지" -> mi(0xE3F4); // image
			case "글" -> mi(0xE873); // description
			case "음악" -> mi(0xE405); // music_note
			case "기타" -> mi(0xE5D4); // more_horiz
			default -> mi(0xE2C7);
			};

			return new FontIcon(glyph, FontKit.materialIcon(18f), new Color(140, 140, 155));
		}
	}

	/**
	 * 한 줄 카드 렌더러 - 좌: 상태 칩 + 제목(+비공개면 자물쇠) - 우: 생성일(yyyy.MM.dd) 우측 정렬
	 */
	private static class LogRowRenderer implements ListCellRenderer<LogItem> {
		private final HoverIndexProvider hover;
		private final Color rowHover = new Color(0xFAF9FF);
		private final Color rowSel = new Color(0xF5F3FF);
		private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy.MM.dd");

		LogRowRenderer(HoverIndexProvider hover) {
			this.hover = hover;
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends LogItem> list, LogItem value, int index,
				boolean isSelected, boolean cellHasFocus) {
			LogRowPanel p = new LogRowPanel();

			boolean isHover = (hover.getHoverIndex() == index);
			if (isSelected)
				p.setBackground(rowSel);
			else if (isHover)
				p.setBackground(rowHover);
			else
				p.setBackground(Color.WHITE);

			p.setData(value, DF);
			return p;
		}
	}

	/** 한 줄 row UI */
	private static class LogRowPanel extends JPanel {
		private final Chip chip = new Chip();
		private final JLabel title = new JLabel();
		private final JLabel lock = new JLabel();
		private final JLabel date = new JLabel();

		LogRowPanel() {
			setLayout(new BorderLayout(12, 0));
			setBorder(new EmptyBorder(8, 12, 8, 12));
			setOpaque(true);

			JPanel left = new JPanel();
			left.setOpaque(false);
			left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));

			title.setFont(UITheme.BODY);
			title.setForeground(UITheme.TEXT);

			lock.setFont(FontKit.materialIcon(16f));
			lock.setForeground(new Color(120, 120, 130));
			lock.setBorder(new EmptyBorder(0, 2, 0, 2));

			left.add(chip);
			left.add(Box.createHorizontalStrut(10));
			left.add(title);
			left.add(Box.createHorizontalStrut(6));
			left.add(lock);
			left.add(Box.createHorizontalGlue());

			date.setFont(UITheme.CAPTION);
			date.setForeground(new Color(130, 130, 140));
			date.setHorizontalAlignment(SwingConstants.RIGHT);

			add(left, BorderLayout.CENTER);
			add(date, BorderLayout.EAST);
		}

		void setData(LogItem item, DateTimeFormatter df) {
			title.setText(item.title);
			date.setText(item.createdAt != null ? item.createdAt.format(df) : "");

			if (item.isPublic) {
				lock.setText("");
			} else {
				lock.setText(new String(Character.toChars(0xE897))); // lock
			}

			switch (item.status) {
			case FAIL -> chip.setChip("보완 필요", new Color(0xFCEEF4), new Color(0xC2417A));
			case IN_PROGRESS -> chip.setChip("진행중", new Color(0xE8F0FF), new Color(0x1D4ED8));
			case SUCCESS -> chip.setChip("완료", new Color(0xEAF7EE), new Color(0x166534));
			}
		}
	}

	/** 상태 칩 (실패/성공/진행중) */
	private static class Chip extends JComponent {
		private String text = "";
		private Color bg = new Color(0xEEEFFF);
		private Color fg = new Color(0x333333);

		// 패딩 / 높이
		private final int padX = 12;
		private final int padY = 5;
		private final int minH = 24;

		Chip() {
			setOpaque(false);
			setFont(UITheme.CAPTION);
		}

		void setChip(String text, Color bg, Color fg) {
			this.text = (text == null) ? "" : text;
			this.bg = (bg == null) ? this.bg : bg;
			this.fg = (fg == null) ? this.fg : fg;

			revalidate();
			repaint();
		}

		@Override
		public Dimension getPreferredSize() {
			FontMetrics fm = getFontMetrics(getFont());
			int w = padX * 2 + fm.stringWidth(text);
			int h = Math.max(minH, padY * 2 + fm.getHeight());
			return new Dimension(w, h);
		}

		@Override
		public Dimension getMaximumSize() {
			return getPreferredSize();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			int w = getWidth();
			int h = getHeight();

			int arc = h;

			// 배경
			g2.setColor(bg);
			g2.fillRoundRect(0, 0, w, h, arc, arc);

			// 텍스트
			g2.setFont(getFont());
			FontMetrics fm = g2.getFontMetrics();
			int tx = padX;
			int ty = (h - fm.getHeight()) / 2 + fm.getAscent();

			g2.setColor(fg);
			g2.drawString(text, tx, ty);

			g2.dispose();
		}
	}

	private static class FontIcon implements Icon {
		private final String text;
		private final Font font;
		private final Color color;

		FontIcon(String text, Font font, Color color) {
			this.text = text;
			this.font = font;
			this.color = color;
		}

		@Override
		public int getIconWidth() {
			return 22;
		}

		@Override
		public int getIconHeight() {
			return 22;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setFont(font);
			g2.setColor(color);

			FontMetrics fm = g2.getFontMetrics();
			int tx = x;
			int ty = y + ((getIconHeight() - fm.getHeight()) / 2) + fm.getAscent();

			g2.drawString(text, tx, ty);
			g2.dispose();
		}
	}
}
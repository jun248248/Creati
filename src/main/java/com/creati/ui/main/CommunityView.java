package com.creati.ui.main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.creati.model.LogStatus;
import com.creati.ui.components.Chip;
import com.creati.util.FontKit;
import com.creati.util.UITheme;
import com.creati.dao.LogDao;
import com.creati.dto.PublicLogListDto;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.creati.model.LogPost;

public class CommunityView extends JPanel {

	private static Icon makeMaterialIconForCategory(String category) {
		int codePoint = switch (category) {
			case "전체" -> 0xE2C7;
			case "영상" -> 0xE02C;
			case "이미지" -> 0xE3F4;
			case "글" -> 0xE873;
			case "음악" -> 0xE405;
			case "기타" -> 0xE5D4;
			default -> 0xE2C7;
		};
		return MainUiParts.glyphIcon(codePoint, 18f, UITheme.ICON_MUTED);
	}

	private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy.MM.dd");
	private static final int LEFT_PANEL_W = 240;

	private String query = "";
	private String selectedCategory = null;
	
	private final LogDao logDao = new LogDao();

	private final DefaultListModel<String> catModel = new DefaultListModel<>();
	private final JList<String> catList = new JList<>(catModel);

	private final DefaultListModel<LogItem> logModel = new DefaultListModel<>();
	private final JList<LogItem> logList = new JList<>(logModel);

	private final JLabel headerTitle = new JLabel("커뮤니티");
	private final JLabel headerSub = new JLabel("서로의 경험과 성장 이야기를 함께 나눠보세요.");

	private final JLabel rightTitle = new JLabel("전체");
	private final JLabel rightCount = new JLabel("0개");

	private int hoverCatIndex = -1;
	private int hoverLogIndex = -1;

	private final List<LogItem> allLogs = new ArrayList<>();
	private final List<String> categories = List.of("전체", "영상", "이미지", "글", "음악", "기타");

	public CommunityView() {
		UITheme.ensureInit();
		FontKit.init();
		setLayout(new BorderLayout());
		setBackground(UITheme.BG);
		add(buildHeader(), BorderLayout.NORTH);
		add(buildExplorer(), BorderLayout.CENTER);
		loadCategories();
		catList.setSelectedIndex(0);
		applyFilter();
	}

	public void setQuery(String q) {
		this.query = (q == null) ? "" : q.trim();
		applyFilter();
	}

	public void clearSearch() { setQuery(""); }

	private JComponent buildHeader() {
		JPanel wrap = new JPanel(new BorderLayout());
		wrap.setOpaque(false);
		wrap.setBorder(new EmptyBorder(10, 18, 6, 18));
		headerTitle.setFont(UITheme.BODY_MED);
		headerTitle.setForeground(UITheme.TEXT);
		headerSub.setFont(UITheme.CAPTION);
		headerSub.setForeground(UITheme.RGB_120_120_120);
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
		rightCount.setForeground(UITheme.RGB_120_120_120);
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

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftCard, rightCard);
		leftCard.setPreferredSize(new Dimension(LEFT_PANEL_W, 0));
		leftCard.setMinimumSize(new Dimension(LEFT_PANEL_W, 0));
		leftCard.setMaximumSize(new Dimension(LEFT_PANEL_W, Integer.MAX_VALUE));
		split.setResizeWeight(0.0);
		SwingUtilities.invokeLater(() -> split.setDividerLocation(LEFT_PANEL_W));
		split.setBorder(BorderFactory.createEmptyBorder());
		split.setDividerSize(6);
		split.setContinuousLayout(true);
		split.setOpaque(false);
		wrap.add(split, BorderLayout.CENTER);
		return wrap;
	}

	private JPanel makeCardPanel() {
		JPanel p = new JPanel();
		p.setBackground(UITheme.WHITE);
		p.setBorder(BorderFactory.createLineBorder(UITheme.RGB_235_235_240, 1, true));
		return p;
	}

	private void setupCategoryList() {
		catList.setFont(UITheme.BODY);
		catList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		catList.setFixedCellHeight(46); 
		catList.setBackground(UITheme.WHITE);
		catList.setSelectionBackground(UITheme.TRANSPARENT);
		catList.setSelectionForeground(UITheme.TEXT);
		catList.setFocusable(false);
		catList.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
			@Override public void mouseMoved(MouseEvent e) {
				hoverCatIndex = catList.locationToIndex(e.getPoint());
				catList.repaint();
			}
		});
		catList.addMouseListener(new MouseAdapter() {
			@Override public void mouseExited(MouseEvent e) {
				hoverCatIndex = -1;
				catList.repaint();
			}
		});
		MainUiParts.installHoverTracking(catList);
		catList.setCellRenderer(MainUiParts.createLabelRowRenderer(v -> String.valueOf(v), v -> makeMaterialIconForCategory(String.valueOf(v)), 10, 12, 10, 12));
		catList.addListSelectionListener(e -> {
			if (e.getValueIsAdjusting()) return;
			String v = catList.getSelectedValue();
			if (v == null) return;
			selectedCategory = "전체".equals(v) ? null : v;
			applyFilter();
		});
	}

	private void loadCategories() {
		catModel.clear();
		for (String c : categories) catModel.addElement(c);
	}

	private void setupLogList() {
		logList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		logList.setFixedCellHeight(46); 
		logList.setBackground(UITheme.WHITE);
		logList.setSelectionBackground(UITheme.TRANSPARENT);
		logList.setSelectionForeground(UITheme.TEXT);
		logList.setFocusable(false);
		logList.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
			@Override public void mouseMoved(MouseEvent e) {
				hoverLogIndex = logList.locationToIndex(e.getPoint());
				logList.repaint();
			}
		});
		logList.addMouseListener(new MouseAdapter() {
			@Override public void mouseExited(MouseEvent e) {
				hoverLogIndex = -1;
				logList.repaint();
			}
			@Override public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() < 1) return;
				LogItem item = logList.getSelectedValue();
				if (item == null) return;
				LogPost postToOpen = Services.LOGS.getById(item.id);
				if (postToOpen == null) postToOpen = toPost(item);
				Window w = SwingUtilities.getWindowAncestor(CommunityView.this);
				if (w instanceof MainFrame) {
					((MainFrame) w).openLogDetail(postToOpen, "COMMUNITY");
				}
			}
		});
		MainUiParts.installHoverTracking(logList);
		logList.setCellRenderer(MainUiParts.createRowRenderer((list, value, index, isSelected, isHover) -> {
			String meta = DF.format(value.createdAt);
			JPanel row = MainUiParts.createLogRowPanel(
					value.status.label,
					UITheme.chipBg(value.status),
					UITheme.chipFg(value.status),
					value.title, null, null, meta);
			MainUiParts.applyRowStateBackground(row, isSelected, isHover);
			return row;
		}));
	}

	private void applyFilter() {
		reloadFromStore();
		logModel.clear();
		List<LogItem> filtered = new ArrayList<>();
		for (LogItem item : allLogs) {
			boolean okCat = (selectedCategory == null) || Objects.equals(item.category, selectedCategory);
			boolean okQuery = query.isEmpty() || (item.title != null && item.title.contains(query));
			if (okCat && okQuery) filtered.add(item);
		}
		for (LogItem item : filtered) logModel.addElement(item);
		rightTitle.setText(selectedCategory == null ? "전체" : selectedCategory);
		rightCount.setText(filtered.size() + "개");
		revalidate(); repaint();
	}

	private void reloadFromStore() {
	    allLogs.clear();

	    // ✅ DB에서 전체 공개글 조회(유저 제한 없음)
	    List<PublicLogListDto> rows = logDao.findAllPublicLogs();

	    for (PublicLogListDto r : rows) {
	        // DB 상태값 -> UI LogStatus
	        LogStatus st = LogStatus.IN_PROGRESS;
	        if ("SUCCESS".equals(r.getResultStatus())) st = LogStatus.DONE;
	        else if ("FAIL".equals(r.getResultStatus())) st = LogStatus.NEEDS_IMPROVEMENT;
	        else if ("ONGOING".equals(r.getResultStatus())) st = LogStatus.IN_PROGRESS;

	        allLogs.add(new LogItem(
	                String.valueOf(r.getId()),
	                r.getFieldName() != null ? r.getFieldName() : "기타",  // 분야(영상/글/...)
	                "",                                                   // subCategory(없으면 빈칸)
	                st,
	                r.getTitle(),
	                (r.getCreatedAt() != null) ? r.getCreatedAt().toLocalDate() : LocalDate.now(),
	                true, // 공개글만 가져오므로 true
	                "", "", "", "", "", ""
	        ));
	    }

	    if (allLogs.isEmpty()) {
	        allLogs.add(new LogItem("community_demo_1", "글", "일상 / 브이로그", LogStatus.IN_PROGRESS,
	                "(더미) 공개 성장 로그 예시", LocalDate.now(), true, "", "", "", "", "", ""));
	    }
	}

	private static class LogItem {
		final String id, category, subCategory, title, whatIDid, feeling, difficulty, learning, retryPlan, link;
		final LogStatus status;
		final LocalDate createdAt;
		final boolean isPublic;
		LogItem(String id, String category, String subCategory, LogStatus status, String title,
				LocalDate createdAt, boolean isPublic, String whatIDid, String feeling,
				String difficulty, String learning, String retryPlan, String link) {
			this.id = id; this.category = category; this.subCategory = subCategory;
			this.status = status; this.title = title; this.createdAt = createdAt;
			this.isPublic = isPublic; this.whatIDid = whatIDid; this.feeling = feeling;
			this.difficulty = difficulty; this.learning = learning;
			this.retryPlan = retryPlan; this.link = link;
		}
	}

	private LogPost toPost(LogItem item) {
		return new LogPost(LogPost.TYPE_LOG, item.id, item.category, item.subCategory, item.status,
				item.title, item.createdAt, item.isPublic,
				null, null, null, null, null, null, null,
				item.whatIDid, null, null, item.learning, null, null, null,
				item.retryPlan, item.link, null);
	}

	private interface HoverIndexProvider { int getHoverIndex(); }

	private static class LogRowPanel extends JPanel {
		private final Chip chip = new Chip();
		private final JLabel title = new JLabel();
		private final JLabel lock = new JLabel();
		private final JLabel date = new JLabel();

		LogRowPanel() {
		    setOpaque(true);
		    setLayout(new BorderLayout());
		    setBorder(new EmptyBorder(0, 12, 0, 12));

		    title.setFont(UITheme.BODY);
		    title.setForeground(UITheme.TEXT);

		    lock.setFont(FontKit.materialIcon(16f));
		    lock.setForeground(UITheme.RGB_120_120_130);
		    lock.setBorder(new EmptyBorder(0, 2, 0, 2));

		    date.setFont(UITheme.CAPTION);
		    date.setForeground(UITheme.RGB_130_130_140);

		    JPanel left = new JPanel();
		    left.setOpaque(false);
		    left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
		    chip.setAlignmentY(0.5f);
		    title.setAlignmentY(0.5f);
		    lock.setAlignmentY(0.5f);
		    left.add(chip);
		    left.add(Box.createHorizontalStrut(10));
		    left.add(title);
		    left.add(Box.createHorizontalStrut(6));
		    left.add(lock);

		    JPanel inner = new JPanel(new BorderLayout(12, 0));
		    inner.setOpaque(false);
		    inner.add(left, BorderLayout.CENTER);
		    inner.add(date, BorderLayout.EAST);

		    setPreferredSize(new Dimension(0, 44));
		    setLayout(new GridBagLayout());
		    GridBagConstraints gbc = new GridBagConstraints();
		    gbc.fill = GridBagConstraints.HORIZONTAL;
		    gbc.weightx = 1.0;
		    setBorder(new EmptyBorder(0, 12, 0, 12));
		    add(inner, gbc);
		}

		void setData(LogItem item, DateTimeFormatter df) {
			title.setText(item.title != null ? item.title : "");
			date.setText(item.createdAt != null ? item.createdAt.format(df) : "");
			lock.setText(""); 
			LogStatus st = (item.status == null) ? LogStatus.IN_PROGRESS : item.status;
			chip.setChip(st.label, UITheme.chipBg(st), UITheme.chipFg(st));
		}
	}

	private static class FontIcon implements Icon {
		private final String text; private final Font font; private final Color color;
		FontIcon(String text, Font font, Color color) { this.text = text; this.font = font; this.color = color; }
		@Override public int getIconWidth() { return 22; }
		@Override public int getIconHeight() { return 22; }
		@Override public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setFont(font); g2.setColor(color);
			FontMetrics fm = g2.getFontMetrics();
			int ty = y + ((getIconHeight() - fm.getHeight()) / 2) + fm.getAscent();
			g2.drawString(text, x, ty); g2.dispose();
		}
	}
	
	public void refresh() {
	    reloadFromStore();   // 커뮤니티 목록 다시 로드하는 내부 메서드가 이미 있으면 이걸 호출
	    revalidate();
	    repaint();
	}
	
}
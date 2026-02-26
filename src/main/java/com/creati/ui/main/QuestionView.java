package com.creati.ui.main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.creati.util.FontKit;
import com.creati.util.UITheme;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.creati.model.LogPost;

public class QuestionView extends JPanel {

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
	private String selectedField = null;

	private final DefaultListModel<String> catModel = new DefaultListModel<>();
	private final JList<String> catList = new JList<>(catModel);

	private final DefaultListModel<QnaItem> logModel = new DefaultListModel<>();
	private final JList<QnaItem> logList = new JList<>(logModel);

	private final JLabel headerTitle = new JLabel("질문하기");
	private final JLabel headerSub = new JLabel("궁금한 점을 질문으로 남기고, 분야별로 모아보세요.");

	private final JLabel rightTitle = new JLabel("전체");
	private final JLabel rightCount = new JLabel("0개");

	private int hoverCatIndex = -1;
	private int hoverLogIndex = -1;

	private final List<QnaItem> allLogs = new ArrayList<>();
	private final List<String> categories = List.of("전체", "영상", "이미지", "글", "음악", "기타");

	public QuestionView() {
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
			selectedField = "전체".equals(v) ? null : v;
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
				QnaItem item = logList.getSelectedValue();
				if (item == null) return;
				if (item.post == null) {
					JOptionPane.showMessageDialog(QuestionView.this, "데모 질문은 상세 화면을 지원하지 않아요.");
					return;
				}
				Window w = SwingUtilities.getWindowAncestor(QuestionView.this);
				if (w instanceof MainFrame) {
					((MainFrame) w).navigator().openQnaDetail(item.post);
					return;
				}
				JOptionPane.showMessageDialog(QuestionView.this, "상세 화면을 열 수 없어요.");
			}
		});
		MainUiParts.installHoverTracking(logList);

		logList.setCellRenderer(MainUiParts.createRowRenderer((list, value, index, isSelected, isHover) -> {
		    QnaRowPanel row = new QnaRowPanel();    
		    row.setData(value, DF);                 
		    MainUiParts.applyRowStateBackground(row, isSelected, isHover);
		    return row;
		}));
	}

	
	public void refresh() {
		applyFilter();
	}

	private void applyFilter() {
		reloadFromStore();
		logModel.clear();
		List<QnaItem> filtered = new ArrayList<>();
		for (QnaItem item : allLogs) {
			            boolean okField = (selectedField == null) || Objects.equals(item.field, selectedField);

			boolean okQuery = query.isEmpty() || (item.title != null && item.title.contains(query));
			            if (okField && okQuery) filtered.add(item);
		}
		for (QnaItem item : filtered) logModel.addElement(item);
		        rightTitle.setText(selectedField == null ? "전체" : selectedField);
		rightCount.setText(filtered.size() + "개");
		revalidate(); repaint();
	}

	private void reloadFromStore() {
		allLogs.clear();
		for (LogPost p : Services.LOGS.list()) {
			if (!LogPost.TYPE_QNA.equals(p.type)) continue;
			String field = (p.field == null) ? "" : p.field;
			String category = (p.subCategory == null) ? "" : p.subCategory;
			String content = (p.processText == null) ? "" : p.processText;
			allLogs.add(new QnaItem(p.id, field, category, p.title, content,
					(p.createdAt == null ? LocalDate.now() : p.createdAt), p));
		}
		if (allLogs.isEmpty()) {
			allLogs.add(new QnaItem("qna_demo_1", "영상", "일상 / 브이로그",
					"쇼츠 자막 템포를 어떻게 잡아야 할까요?",
					"자막 템포/호흡을 잡는 기준이 궁금해요.", LocalDate.now(), null));
		}
	}

	private static class QnaItem {
		final String id, field, category, title, content;
		final LocalDate createdAt;
		final LogPost post;
		QnaItem(String id, String field, String category, String title, String content, LocalDate createdAt, LogPost post) {
			this.id = id; this.field = field; this.category = category;
			this.title = title; this.content = content; this.createdAt = createdAt; this.post = post;
		}
	}

	private interface HoverIndexProvider { int getHoverIndex(); }

	private static class QnaRowPanel extends JPanel {
		private final JLabel icon = new JLabel();
		private final JLabel title = new JLabel();
		private final JLabel date = new JLabel();

		QnaRowPanel() {
			setLayout(new BorderLayout(12, 0));
			setBorder(new EmptyBorder(10, 12, 10, 12)); 
			setOpaque(true);
			JPanel left = new JPanel();
			left.setOpaque(false);
			left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
			icon.setFont(FontKit.materialIcon(18f));
			icon.setText(new String(Character.toChars(0xE887))); 
			icon.setForeground(UITheme.ACCENT_PURPLE);
			title.setFont(UITheme.BODY);
			title.setForeground(UITheme.TEXT);
			left.add(icon);
			left.add(Box.createHorizontalStrut(10));
			left.add(title);
			left.add(Box.createHorizontalGlue());
			date.setFont(UITheme.CAPTION);
			date.setForeground(UITheme.RGB_130_130_140);
			date.setHorizontalAlignment(SwingConstants.RIGHT);
			add(left, BorderLayout.CENTER);
			add(date, BorderLayout.EAST);
		}

		void setData(QnaItem item, DateTimeFormatter df) {
			title.setText(item.title != null ? item.title : "");
			date.setText(item.createdAt != null ? item.createdAt.format(df) : "");
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
}
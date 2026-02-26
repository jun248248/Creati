package com.creati.ui.main;

import com.creati.util.FontKit;
import com.creati.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class DraftDrawerDialog extends JDialog {

	private interface HoverIndexProvider {
		int getHoverIndex();
	}

	public interface DraftConsumer {
		void accept(WriteLogView.Draft d);
	}

	public interface IdConsumer {
		void accept(String id);
	}

	private final DraftConsumer onLoad;
	private final IdConsumer onDelete;

	private final DefaultListModel<WriteLogView.Draft> model = new DefaultListModel<>();
	private final JList<WriteLogView.Draft> list = new JList<>(model);

	public DraftDrawerDialog(JFrame owner, DraftConsumer onLoad, IdConsumer onDelete) {
		super(owner, false);
		UITheme.ensureInit();
		FontKit.init();

		this.onLoad = onLoad;
		this.onDelete = onDelete;

		setUndecorated(true);
		setBackground(UITheme.TRANSPARENT);

		setContentPane(buildRoot());
		setSize(320, owner.getHeight());
	}

	private JComponent buildRoot() {
		JPanel root = new JPanel(new BorderLayout());
		root.setBackground(UITheme.WHITE);
		root.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, UITheme.RGB_230_230_235));

		
		JPanel head = new JPanel(new BorderLayout());
		head.setBackground(UITheme.WHITE);
		head.setBorder(new EmptyBorder(12, 14, 12, 14));

		JLabel t = new JLabel("임시보관함");
		t.setFont(UITheme.BODY_MED);
		t.setForeground(UITheme.TEXT);

		JButton close = new JButton("X");
		close.setFont(UITheme.BODY_MED);
		close.setForeground(UITheme.RGB_120_120_130);
		close.setBorder(new EmptyBorder(6, 10, 6, 10));
		close.setFocusPainted(false);
		close.setContentAreaFilled(false);
		close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		close.addActionListener(e -> setVisible(false));

		head.add(t, BorderLayout.WEST);
		head.add(close, BorderLayout.EAST);

		
		list.setBackground(UITheme.WHITE);
		list.setFont(UITheme.BODY);
		list.setFixedCellHeight(72);

		final int[] hoverIndex = new int[] { -1 };
		HoverIndexProvider hover = () -> hoverIndex[0];
		MainUiParts.installHoverTracking(list);
		list.setCellRenderer(MainUiParts.createRowRenderer((jl, value, index, isSelected, isHover) -> {
			String title = (value.title == null || value.title.isBlank()) ? "(제목 없음)" : value.title;
			String meta = (value.updatedAt != null) ? value.updatedAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")) : "";
			String sub1 = "";
if (value.field != null && !value.field.isBlank()) sub1 = value.field;
if (value.category != null && !value.category.isBlank()) sub1 = sub1.isBlank() ? value.category : (sub1 + " · " + value.category);
if (value.status != null) sub1 = sub1.isBlank() ? value.status.label : (sub1 + " · " + value.status.label);
String sub2 = "";
if (value.goalText != null && !value.goalText.isBlank()) sub2 = value.goalText.trim();
else if (value.painPoint != null && !value.painPoint.isBlank()) sub2 = value.painPoint.trim();
if (sub2.length() > 48) sub2 = sub2.substring(0, 48) + "…";
JPanel row = MainUiParts.createLogRowPanel("임시저장", UITheme.chipBgGrey(), UITheme.chipFgGrey(), title, sub1, sub2, meta);
			MainUiParts.applyRowStateBackground(row, isSelected, isHover);
			return row;
		}));
		list.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int idx = list.locationToIndex(e.getPoint());
				if (idx >= 0) {
					Rectangle r = list.getCellBounds(idx, idx);
					if (r == null || !r.contains(e.getPoint())) idx = -1;
				}
				if (hoverIndex[0] != idx) {
					hoverIndex[0] = idx;
					list.repaint();
				}
			}
		});
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				if (hoverIndex[0] != -1) {
					hoverIndex[0] = -1;
					list.repaint();
				}
			}
		});

		list.setSelectionBackground(UITheme.TRANSPARENT);
		list.setSelectionForeground(UITheme.TEXT);
		list.setFocusable(false);

		JScrollPane sp = new JScrollPane(list);
		sp.setBorder(BorderFactory.createEmptyBorder());
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		
		JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
		foot.setBackground(UITheme.WHITE);
		foot.setBorder(new EmptyBorder(0, 12, 8, 12));

		JButton load = ghostButton("불러오기");
		JButton del = ghostButton("삭제");
		del.setForeground(UITheme.RGB_180_70_70);

		load.addActionListener(e -> {
			WriteLogView.Draft d = list.getSelectedValue();
			if (d == null)
				return;
			if (onLoad != null)
				onLoad.accept(d);
			setVisible(false);
		});
		del.addActionListener(e -> {
			WriteLogView.Draft d = list.getSelectedValue();
			if (d == null || d.id == null)
				return;
			int ok = JOptionPane.showConfirmDialog(this, "임시보관을 삭제할까?", "삭제", JOptionPane.OK_CANCEL_OPTION);
			if (ok != JOptionPane.OK_OPTION)
				return;
			if (onDelete != null)
				onDelete.accept(d.id);
			refresh();
		});

		foot.add(load);
		foot.add(del);

		root.add(head, BorderLayout.NORTH);
		root.add(sp, BorderLayout.CENTER);
		root.add(foot, BorderLayout.SOUTH);
		return root;
	}

	private JButton ghostButton(String text) {
		JButton b = new JButton(text);
		b.setFont(UITheme.BODY_MED);
		b.setForeground(UITheme.TEXT);
		b.setBackground(UITheme.WHITE);
		b.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UITheme.RGB_235_235_242, 1, true), new EmptyBorder(8, 10, 8, 10)));
		b.setFocusPainted(false);
		b.setContentAreaFilled(false);
		b.setOpaque(true);
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return b;
	}

	public void refresh() {
		model.clear();
		List<WriteLogView.Draft> drafts = Services.DRAFTS.list();
		for (WriteLogView.Draft d : drafts)
			model.addElement(d);
		if (!model.isEmpty())
			list.setSelectedIndex(0);
	}

	public void openAtRightOf(JFrame owner) {
		int w = getWidth();
		Point p = owner.getLocationOnScreen();
		Insets ins = owner.getInsets();

		int contentW = owner.getWidth() - ins.left - ins.right;
		int contentH = owner.getHeight() - ins.top - ins.bottom;

		int x = p.x + ins.left + contentW - w;
		int y = p.y + ins.top;

		Rectangle screen = owner.getGraphicsConfiguration().getBounds();
		x = Math.max(screen.x, Math.min(x, screen.x + screen.width - w));
		y = Math.max(screen.y, Math.min(y, screen.y + screen.height - contentH));

		setSize(w, contentH);
		setLocation(x, y);
		setVisible(true);
		toFront();
	}

	private static class DraftCell extends JPanel implements ListCellRenderer<WriteLogView.Draft> {
		private final HoverIndexProvider hover;
		private final JLabel title = new JLabel();
		private final JLabel meta = new JLabel();

		DraftCell(HoverIndexProvider hover) {
			super(new BorderLayout());
			this.hover = hover;
			setOpaque(true);
			setBorder(new EmptyBorder(10, 12, 10, 12));
			title.setFont(UITheme.BODY_MED);
			title.setForeground(UITheme.TEXT);

			meta.setFont(UITheme.CAPTION);
			meta.setForeground(UITheme.RGB_130_130_140);

			JPanel text = new JPanel();
			text.setOpaque(false);
			text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
			text.add(title);
			text.add(Box.createVerticalStrut(6));
			text.add(meta);

			add(text, BorderLayout.CENTER);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends WriteLogView.Draft> list,
				WriteLogView.Draft value, int index, boolean isSelected, boolean cellHasFocus) {
			String t = (value.title == null || value.title.isBlank()) ? "(제목 없음)" : value.title;
			String f = (value.field == null || value.field.isBlank()) ? "-" : value.field;
			String c = (value.category == null || value.category.isBlank()) ? "-" : value.category;
			String time = (value.updatedAt == null) ? ""
					: value.updatedAt.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"));

			title.setText(t);
			meta.setText("분야: " + f + " · " + c + " · " + time);


			boolean isHover = (hover != null && hover.getHoverIndex() == index);
			MainUiParts.applyRowStateBackground(this, isSelected, isHover);

			setBorder(new EmptyBorder(10, 12, 10, 12));
			return this;
		}
	}
}

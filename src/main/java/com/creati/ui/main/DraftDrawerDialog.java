package com.creati.ui.main;

import com.creati.util.FontKit;
import com.creati.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 임시보관함 Drawer (오른쪽에 붙는 JDialog)
 *
 * TODO(DB)  WriteLogView.DraftStore 대신 DB에서 조회하도록 교체
 */
public class DraftDrawerDialog extends JDialog {

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
		setBackground(new Color(0, 0, 0, 0));

		setContentPane(buildRoot());
		setSize(320, owner.getHeight());
	}

	private JComponent buildRoot() {
		JPanel root = new JPanel(new BorderLayout());
		root.setBackground(Color.WHITE);
		root.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(230, 230, 235)));

		// header
		JPanel head = new JPanel(new BorderLayout());
		head.setBackground(Color.WHITE);
		head.setBorder(new EmptyBorder(12, 14, 12, 14));

		JLabel t = new JLabel("임시보관함");
		t.setFont(UITheme.BODY_MED);
		t.setForeground(UITheme.TEXT);

		JButton close = new JButton("X");
		close.setFont(UITheme.BODY_MED);
		close.setForeground(new Color(120, 120, 130));
		close.setBorder(new EmptyBorder(6, 10, 6, 10));
		close.setFocusPainted(false);
		close.setContentAreaFilled(false);
		close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		close.addActionListener(e -> setVisible(false));

		head.add(t, BorderLayout.WEST);
		head.add(close, BorderLayout.EAST);

		// list
		list.setBackground(Color.WHITE);
		list.setFont(UITheme.BODY);
		list.setFixedCellHeight(72);
		list.setCellRenderer(new DraftCell());
		list.setSelectionBackground(new Color(0, 0, 0, 0));
		list.setSelectionForeground(UITheme.TEXT);
		list.setFocusable(false);

		JScrollPane sp = new JScrollPane(list);
		sp.setBorder(BorderFactory.createEmptyBorder());
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		// footer actions
		JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
		foot.setBackground(Color.WHITE);
		foot.setBorder(new EmptyBorder(0, 12, 8, 12));

		JButton load = ghostButton("불러오기");
		JButton del = ghostButton("삭제");
		del.setForeground(new Color(180, 70, 70));

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
		b.setBackground(Color.WHITE);
		b.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(235, 235, 242), 1, true), new EmptyBorder(8, 10, 8, 10)));
		b.setFocusPainted(false);
		b.setContentAreaFilled(false);
		b.setOpaque(true);
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return b;
	}

	public void refresh() {
		model.clear();
		List<WriteLogView.Draft> drafts = WriteLogView.DraftStore.list();
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
		private final JLabel title = new JLabel();
		private final JLabel meta = new JLabel();

		DraftCell() {
			super(new BorderLayout());
			setOpaque(true);
			setBorder(new EmptyBorder(10, 12, 10, 12));
			title.setFont(UITheme.BODY_MED);
			title.setForeground(UITheme.TEXT);

			meta.setFont(UITheme.CAPTION);
			meta.setForeground(new Color(130, 130, 140));

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

			if (isSelected) {
				setBackground(new Color(0xEAE6FF));
			} else {
				setBackground(Color.WHITE);
			}

			setBorder(new EmptyBorder(10, 12, 10, 12));
			return this;
		}
	}
}

package com.creati.ui.main;

import com.creati.util.FontKit;
import com.creati.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.util.Objects;

import static com.creati.ui.main.MainUiParts.RoundedButton;

/**
 * QuestionWriteView (질문하기 작성 화면)
 * - 상단: 뒤로가기 / 제목(질문하기) / 등록
 * - 기본 정보: 분야(기타 입력 포함), 카테고리, (선택) 업로드 링크
 * - 질문 내용: 자유 입력 + 글자수 카운터
 *
 * ※ 질문하기는 무조건 공개(공개 범위 UI 없음)
 */
public class QuestionWriteView extends JPanel {

	private static final int MAX_CHARS = 500;

	private final Runnable onBack;
	private final Runnable onRegistered;

	private final WriteLogView.PlaceholderTextField titleField =
			new WriteLogView.PlaceholderTextField("제목을 입력해 주세요.");

	private final WriteLogView.ChipGroup fieldChips = new WriteLogView.ChipGroup(new WriteLogView.ChipGroup.Item[] {
			new WriteLogView.ChipGroup.Item("영상", 0xE04B),
			new WriteLogView.ChipGroup.Item("이미지", 0xE3F4),
			new WriteLogView.ChipGroup.Item("글", 0xE3C9),
			new WriteLogView.ChipGroup.Item("음악", 0xE405),
			new WriteLogView.ChipGroup.Item("기타", 0xE5D3)
	});

	// '기타' 선택 시 노출되는 입력칸
	private final WriteLogView.PlaceholderTextField customField =
			new WriteLogView.PlaceholderTextField("예: 뉴스레터");

	private final JComboBox<String> categoryCombo = new JComboBox<>(new String[] {
			"일상 / 브이로그",
			"공부 / 자기계발",
			"개발 / IT / 생산성",
			"운동 / 건강 / 루틴",
			"리뷰 / 정보 / 추천",
			"취미 / 관심사",
			"마인드 / 생각 / 경험",
			"기타"
	});

	private final WriteLogView.PlaceholderTextField linkField =
			new WriteLogView.PlaceholderTextField("업로드한 게시글 링크를 붙여넣어줘!");

	private final WriteLogView.PlaceholderTextArea questionArea =
			new WriteLogView.PlaceholderTextArea("크리에이터 관련 질문을 마음껏 적어줘!");

	private final JLabel counterLabel = new JLabel("0 / " + MAX_CHARS);

	public QuestionWriteView(JFrame owner, Runnable onBack, Runnable onRegistered) {
		UITheme.ensureInit();
		FontKit.init();
		this.onBack = onBack;
		this.onRegistered = onRegistered;

		setLayout(new BorderLayout());
		setBackground(UITheme.BG);

		add(buildTopBar(), BorderLayout.NORTH);
		add(buildBody(), BorderLayout.CENTER);

		wireEvents();
		updateCounter();
	}

	private JComponent buildTopBar() {
		JPanel bar = new JPanel(new BorderLayout());
		bar.setBackground(Color.WHITE);
		bar.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 235)),
				new EmptyBorder(12, 16, 12, 16)
		));

		JButton back = iconButton(0xE5C4, "뒤로가기");
		back.addActionListener(e -> {
			if (onBack != null) onBack.run();
		});

		JLabel title = new JLabel("질문하기");
		title.setFont(UITheme.BODY_MED);
		title.setForeground(UITheme.TEXT);

		RoundedButton register = new RoundedButton("등록");
		register.setBackground(UITheme.ACCENT_PURPLE);
		register.setForeground(Color.WHITE);
		register.setFont(UITheme.BODY_MED);
		register.addActionListener(e -> onRegister());

		JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		left.setOpaque(false);
		left.add(back);

		JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		right.setOpaque(false);
		right.add(register);

		bar.add(left, BorderLayout.WEST);
		bar.add(title, BorderLayout.CENTER);
		bar.add(right, BorderLayout.EAST);
		return bar;
	}

	private JComponent buildBody() {
		JPanel form = new JPanel();
		form.setOpaque(false);
		form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
		form.setBorder(new EmptyBorder(16, 18, 24, 18));

		// 제목
		form.add(card(panel -> {
			panel.setLayout(new BorderLayout());
			titleField.setFont(FontKit.medium(22f));
			titleField.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
			titleField.setBackground(new Color(250, 250, 252));
			panel.add(titleField, BorderLayout.CENTER);
		}));
		form.add(Box.createVerticalStrut(12));

		// 기본 정보
		form.add(cardWithTitle("기본 정보", panel -> {
			panel.setLayout(new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();
			gc.insets = new Insets(0, 0, 14, 0);
			gc.anchor = GridBagConstraints.WEST;
			gc.fill = GridBagConstraints.HORIZONTAL;
			gc.weightx = 1.0;
			gc.gridx = 0;
			gc.gridy = 0;
			gc.gridwidth = 2;

			// 분야
			JPanel fieldCol = new JPanel();
			fieldCol.setOpaque(false);
			fieldCol.setLayout(new BoxLayout(fieldCol, BoxLayout.Y_AXIS));
			fieldCol.add(leftAligned(rowTitle("분야 *")));
			fieldCol.add(Box.createVerticalStrut(8));

			JPanel fieldLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
			fieldLine.setOpaque(false);
			fieldLine.add(fieldChips);

			customField.setVisible(false);
			styleInput(customField);
			customField.setPreferredSize(new Dimension(220, 38));
			customField.setMaximumSize(new Dimension(240, 38));
			fieldLine.add(customField);
			fieldCol.add(leftAligned(fieldLine));
			panel.add(leftAligned(fieldCol), gc);

			// 카테고리
			gc.gridy = 1;
			gc.gridwidth = 1;

			JPanel catCol = new JPanel();
			catCol.setOpaque(false);
			catCol.setLayout(new BoxLayout(catCol, BoxLayout.Y_AXIS));
			catCol.add(leftAligned(rowTitle("카테고리 *")));
			catCol.add(Box.createVerticalStrut(8));

			categoryCombo.setFont(UITheme.BODY);
			categoryCombo.setBorder(BorderFactory.createEmptyBorder());
			categoryCombo.setBackground(new Color(250, 250, 252));
			categoryCombo.setRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					l.setBorder(new EmptyBorder(0, 0, 0, 0));
					return l;
				}
			});

			JPanel categoryWrap = new JPanel(new BorderLayout());
			categoryWrap.setBackground(new Color(250, 250, 252));
			categoryWrap.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(235, 235, 242), 1, true),
					new EmptyBorder(6, 10, 6, 10)
			));
			categoryWrap.add(categoryCombo, BorderLayout.CENTER);
			categoryWrap.setPreferredSize(new Dimension(420, 38));
			categoryWrap.setMaximumSize(new Dimension(420, 38));
			catCol.add(leftAligned(categoryWrap));
			panel.add(leftAligned(catCol), gc);

			// (선택) 업로드 링크
			gc.gridy = 2;
			gc.gridwidth = 2;
			gc.insets = new Insets(0, 0, 0, 0);
			JPanel linkCol = new JPanel();
			linkCol.setOpaque(false);
			linkCol.setLayout(new BoxLayout(linkCol, BoxLayout.Y_AXIS));
			linkCol.add(leftAligned(rowTitle("업로드 링크 (선택)")));
			linkCol.add(Box.createVerticalStrut(8));
			styleInput(linkField);
			linkCol.add(leftAligned(linkField));
			panel.add(leftAligned(linkCol), gc);
		}));
		form.add(Box.createVerticalStrut(12));

		// 질문 내용
		form.add(cardWithTitle("질문 내용", panel -> {
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

			questionArea.setFont(UITheme.BODY);
			questionArea.setBackground(new Color(250, 250, 252));
			questionArea.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(235, 235, 242), 1, true),
					new EmptyBorder(10, 10, 10, 10)
			));

			// 글자수 제한
			((AbstractDocument) questionArea.getDocument()).setDocumentFilter(new LimitFilter(MAX_CHARS));

			JScrollPane sp = new JScrollPane(questionArea);
			sp.setBorder(BorderFactory.createEmptyBorder());
			sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			sp.setPreferredSize(new Dimension(0, 220));
			panel.add(leftAligned(sp));
			panel.add(Box.createVerticalStrut(8));

			counterLabel.setFont(UITheme.CAPTION);
			counterLabel.setForeground(new Color(130, 130, 140));
			JPanel counterRow = new JPanel(new BorderLayout());
			counterRow.setOpaque(false);
			counterRow.add(counterLabel, BorderLayout.EAST);
			panel.add(leftAligned(counterRow));
		}));

		JScrollPane rootScroll = new JScrollPane(form);
		rootScroll.setBorder(BorderFactory.createEmptyBorder());
		rootScroll.getVerticalScrollBar().setUnitIncrement(16);
		rootScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		rootScroll.getViewport().setBackground(UITheme.BG);
		return rootScroll;
	}

	private void wireEvents() {
		fieldChips.setOnSelectionChanged(() -> {
			String selected = fieldChips.getSelectedText();
			boolean isCustom = Objects.equals(selected, "기타");
			customField.setVisible(isCustom);
			if (!isCustom) customField.setTextOrPlaceholder("");
			revalidate();
			repaint();
			fieldChips.requestFocusInWindow();
		});

		questionArea.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e) { updateCounter(); }
			@Override public void removeUpdate(DocumentEvent e) { updateCounter(); }
			@Override public void changedUpdate(DocumentEvent e) { updateCounter(); }
		});
	}

	private void updateCounter() {
		int len = questionArea.getEffectiveText().length();
		counterLabel.setText(len + " / " + MAX_CHARS);
		counterLabel.setForeground(len >= MAX_CHARS ? new Color(150, 110, 110) : new Color(130, 130, 140));
	}

	private void onRegister() {
		String title = titleField.getEffectiveText().trim();
		String field = resolveField();
		String category = (String) categoryCombo.getSelectedItem();
		String question = questionArea.getEffectiveText().trim();

		if (title.isEmpty()) {
			JOptionPane.showMessageDialog(this, "제목을 입력해 주세요.");
			titleField.requestFocus();
			return;
		}
		if (field == null || field.isBlank()) {
			JOptionPane.showMessageDialog(this, "분야를 선택해 주세요.");
			return;
		}
		if (Objects.equals(fieldChips.getSelectedText(), "기타") && customField.getEffectiveText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "기타 분야를 입력해 주세요.");
			customField.requestFocus();
			return;
		}
		if (category == null || category.isBlank()) {
			JOptionPane.showMessageDialog(this, "카테고리를 선택해 주세요.");
			return;
		}
		if (question.isEmpty()) {
			JOptionPane.showMessageDialog(this, "질문 내용을 입력해 주세요.");
			questionArea.requestFocus();
			return;
		}

		// TODO (DB) 질문 등록 INSERT (공개 고정)
		JOptionPane.showMessageDialog(this, "질문 등록 완료!");
		if (onRegistered != null) onRegistered.run();
	}

	private String resolveField() {
		String selected = fieldChips.getSelectedText();
		if (selected == null) return null;
		if (Objects.equals(selected, "기타")) {
			String v = customField.getEffectiveText().trim();
			return v.isEmpty() ? "기타" : v;
		}
		return selected;
	}

	// ===== UI helpers (WriteLogView와 동일 톤) =====
	private JButton iconButton(int materialCodePoint, String tooltip) {
		JButton b = new JButton(new String(Character.toChars(materialCodePoint)));
		b.setToolTipText(tooltip);
		b.setFont(FontKit.materialIcon(22f));
		b.setForeground(new Color(110, 110, 125));
		b.setBackground(Color.WHITE);
		b.setBorder(new EmptyBorder(6, 6, 6, 6));
		b.setFocusPainted(false);
		b.setContentAreaFilled(false);
		b.setOpaque(false);
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return b;
	}

	private JLabel rowTitle(String text) {
		JLabel l = new JLabel(text);
		l.setFont(UITheme.BODY_MED);
		l.setForeground(UITheme.TEXT);
		l.setBorder(new EmptyBorder(0, 2, 0, 0));
		return l;
	}

	private <T extends JComponent> T leftAligned(T c) {
		c.setAlignmentX(Component.LEFT_ALIGNMENT);
		return c;
	}

	private void styleInput(JTextField tf) {
		tf.setFont(UITheme.BODY);
		tf.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(235, 235, 242), 1, true),
				new EmptyBorder(8, 10, 8, 10)
		));
		tf.setBackground(new Color(250, 250, 252));
	}

	private JPanel card(PanelBuilder builder) {
		JPanel card = new JPanel();
		card.setBackground(Color.WHITE);
		card.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(230, 230, 235), 1, true),
				new EmptyBorder(14, 14, 14, 14)
		));
		builder.build(card);
		return card;
	}

	private JPanel cardWithTitle(String title, PanelBuilder builder) {
		return card(panel -> {
			panel.setLayout(new BorderLayout());
			JLabel t = new JLabel(title);
			t.setFont(UITheme.BODY_MED);
			t.setForeground(UITheme.TEXT);

			JPanel top = new JPanel(new BorderLayout());
			top.setOpaque(false);
			top.add(t, BorderLayout.WEST);
			panel.add(top, BorderLayout.NORTH);

			JPanel body = new JPanel();
			body.setOpaque(false);
			body.setBorder(new EmptyBorder(12, 0, 0, 0));
			builder.build(body);
			panel.add(body, BorderLayout.CENTER);
		});
	}

	private interface PanelBuilder {
		void build(JPanel panel);
	}

	/** 글자수 제한용 DocumentFilter */
	static class LimitFilter extends DocumentFilter {
		private final int max;
		LimitFilter(int max) { this.max = max; }

		@Override
		public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
			if (string == null) return;
			int cur = fb.getDocument().getLength();
			int over = (cur + string.length()) - max;
			String s = over > 0 ? string.substring(0, Math.max(0, string.length() - over)) : string;
			if (!s.isEmpty()) super.insertString(fb, offset, s, attr);
		}

		@Override
		public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
			if (text == null) {
				super.replace(fb, offset, length, null, attrs);
				return;
			}
			int cur = fb.getDocument().getLength();
			int next = cur - length + text.length();
			int over = next - max;
			String s = over > 0 ? text.substring(0, Math.max(0, text.length() - over)) : text;
			super.replace(fb, offset, length, s, attrs);
		}
	}
}

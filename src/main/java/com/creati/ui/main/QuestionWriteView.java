package com.creati.ui.main;

import com.creati.ui.components.RoundedButton;
import com.creati.ui.components.ToggleChipGroup;
import com.creati.util.FontKit;
import com.creati.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;

// DB(TODO): Persist QnA posts via repository.

public class QuestionWriteView extends JPanel {

	private static final int META_COMBO_W = 260;
	private static final int TITLE_FIELD_H = 56;

	private final Runnable onBack;
	private final Runnable onRegistered;
	private final QuestionWriteController controller;

	
	private final JLabel topTitleLabel = new JLabel("질문 작성");
	private final RoundedButton submitBtn = new RoundedButton("등록");

	
	private final PlaceholderTextField titleField = new PlaceholderTextField("제목을 입력해 주세요.");
	private final ToggleChipGroup fieldChips = new ToggleChipGroup(new ToggleChipGroup.Item[] {
			new ToggleChipGroup.Item("영상", 0xE04B),
			new ToggleChipGroup.Item("이미지", 0xE3F4),
			new ToggleChipGroup.Item("글", 0xE3C9),
			new ToggleChipGroup.Item("음악", 0xE405),
			new ToggleChipGroup.Item("기타", 0xE5D3)
	});
	private final JComboBox<String> categoryCombo = new JComboBox<>(new String[] {
			"일상 / 브이로그",
			"공부 / 자기계발 / 교육",
			"생산성 / 루틴 / 습관",
			"개발 / IT / 프로젝트",
			"리뷰 / 정보 / 추천",
			"취미 / 관심사",
			"생각 / 마인드 / 경험 기록",
			"콘텐츠 제작 / 크리에이터 활동",
			"기타"
	});
	private final PlaceholderTextField linkField = new PlaceholderTextField("참고 링크를 붙여넣어 주세요 (선택)");
	private final PlaceholderTextArea contentArea = new PlaceholderTextArea("질문 내용을 입력해 주세요.");

	private JScrollPane scroll;

	public QuestionWriteView(JFrame owner, Runnable onBack, Runnable onRegistered) {
		UITheme.ensureInit();
		FontKit.init();
		this.onBack = (onBack == null) ? () -> {} : onBack;
		this.onRegistered = (onRegistered == null) ? () -> {} : onRegistered;
		this.controller = new QuestionWriteController(Services.LOGS);

		setLayout(new BorderLayout());
		setBackground(UITheme.BG);
		add(buildTopBar(), BorderLayout.NORTH);
		add(buildBody(), BorderLayout.CENTER);
		wireEvents(owner);
		startNew();
	}

	
	public void startNew() {
		fieldChips.clearSelection();
		categoryCombo.setSelectedIndex(0);
		titleField.setTextOrPlaceholder("");
		contentArea.setTextOrPlaceholder("");
		linkField.setTextOrPlaceholder("");
		scrollToTop();
	}

	
	
	
	private JComponent buildTopBar() {
		JPanel bar = new JPanel(new BorderLayout());
		bar.setBackground(UITheme.WHITE);
		bar.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.RGB_230_230_235),
				new EmptyBorder(12, 16, 12, 16)));

		JButton back = iconButton(0xE5C4, "나가기");
		back.addActionListener(e -> this.onBack.run());
		JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		left.setOpaque(false);
		left.add(back);

		topTitleLabel.setFont(UITheme.BODY_MED);
		topTitleLabel.setForeground(UITheme.TEXT);

		submitBtn.setBackground(UITheme.ACCENT_PURPLE);
		submitBtn.setForeground(UITheme.WHITE);
		submitBtn.setFont(UITheme.BODY_MED);

		JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		right.setOpaque(false);
		right.add(submitBtn);

		bar.add(left, BorderLayout.WEST);
		bar.add(topTitleLabel, BorderLayout.CENTER);
		bar.add(right, BorderLayout.EAST);
		return bar;
	}

	private JComponent buildBody() {
		JPanel root = new JPanel(new BorderLayout());
		root.setOpaque(false);

		TrackWidthPanel wrap = new TrackWidthPanel();
		wrap.setOpaque(false);
		wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
		wrap.setBorder(new EmptyBorder(16, 18, 18, 18));

		
		JPanel titleCard = MainUiParts.createCard(14, true);
		titleCard.setLayout(new BorderLayout());
		titleField.setFont(FontKit.medium(22f));
		titleField.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
		titleField.setBackground(UITheme.RGB_250_250_252);
		titleField.setPreferredSize(new Dimension(10, TITLE_FIELD_H));
		titleField.setMinimumSize(new Dimension(10, TITLE_FIELD_H));
		titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, TITLE_FIELD_H));
		titleCard.add(titleField, BorderLayout.CENTER);
		titleCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, TITLE_FIELD_H + 28));

		wrap.add(titleCard);
		wrap.add(Box.createVerticalStrut(12));

		
		wrap.add(buildMetaCard());
		wrap.add(Box.createVerticalStrut(12));

		
		wrap.add(buildContentCard());
		wrap.add(Box.createVerticalStrut(14));

		scroll = new JScrollPane(wrap);
		scroll.setBorder(null);
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		root.add(scroll, BorderLayout.CENTER);
		return root;
	}

	private JComponent buildMetaCard() {
		JPanel body = new JPanel();
		body.setOpaque(false);
		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

		
		body.add(leftAligned(rowTitle("분야 *")));
		body.add(Box.createVerticalStrut(8));
		JPanel fieldRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		fieldRow.setOpaque(false);
		fieldRow.add(fieldChips);
		body.add(leftAligned(fieldRow));
		body.add(Box.createVerticalStrut(14));

		
		body.add(buildCategoryCell());
		body.add(Box.createVerticalStrut(14));

		
		body.add(leftAligned(rowTitle("업로드 링크 (선택)")));
		body.add(Box.createVerticalStrut(8));
		styleInput(linkField);
		linkField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
		body.add(leftAligned(linkField));

		return MainUiParts.createCardWithTitle("기본 정보", 14, body);
	}

	private JComponent buildCategoryCell() {
		JPanel col = new JPanel();
		col.setOpaque(false);
		col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
		col.add(leftAligned(rowTitle("카테고리 *")));
		col.add(Box.createVerticalStrut(8));

		categoryCombo.setFont(UITheme.BODY);
		categoryCombo.setBorder(BorderFactory.createEmptyBorder());
		categoryCombo.setBackground(UITheme.RGB_250_250_252);
		categoryCombo.setPrototypeDisplayValue("콘텐츠 제작 / 크리에이터 활동");
		categoryCombo.setMaximumRowCount(12);
		categoryCombo.setFocusable(false);

		JPanel wrap = new JPanel(new BorderLayout());
		wrap.setBackground(UITheme.RGB_250_250_252);
		wrap.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UITheme.RGB_235_235_242, 1, true),
				new EmptyBorder(6, 10, 6, 10)));
		wrap.add(categoryCombo, BorderLayout.CENTER);
		wrap.setPreferredSize(new Dimension(META_COMBO_W, 38));
		wrap.setMaximumSize(new Dimension(META_COMBO_W, 38));
		col.add(leftAligned(wrap));
		return col;
	}

	private JComponent buildContentCard() {
		JPanel body = new JPanel(new BorderLayout());
		body.setOpaque(false);
		contentArea.setFont(UITheme.BODY);
		contentArea.setBackground(UITheme.RGB_250_250_252);
		contentArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

		JScrollPane areaScroll = new JScrollPane(contentArea);
		areaScroll.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UITheme.RGB_235_235_242, 1, true),
				BorderFactory.createEmptyBorder()));
		areaScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		areaScroll.getVerticalScrollBar().setUnitIncrement(16);
		areaScroll.setPreferredSize(new Dimension(10, 260));

		body.add(areaScroll, BorderLayout.CENTER);
		return MainUiParts.createCardWithTitle("질문 내용", 14, body);
	}

	
	
	
	private void wireEvents(JFrame owner) {
		submitBtn.addActionListener(e -> onSubmitRequested());

		
		titleField.addActionListener(e -> onSubmitRequested());

		
		contentArea.getInputMap().put(KeyStroke.getKeyStroke("ctrl ENTER"), "submit");
		contentArea.getInputMap().put(KeyStroke.getKeyStroke("meta ENTER"), "submit");
		contentArea.getActionMap().put("submit", new AbstractAction() {
			@Override public void actionPerformed(java.awt.event.ActionEvent e) {
				onSubmitRequested();
			}
		});
	}

	private void onSubmitRequested() {
		try {
			controller.submit(new QuestionWriteController.QuestionWriteRequest(
					titleField.getEffectiveText(),
					contentArea.getEffectiveText(),
					selectedFieldOrDefault(),
					Objects.toString(categoryCombo.getSelectedItem(), "기타"),
					linkField.getEffectiveText(),
					null
			));
			JOptionPane.showMessageDialog(this, "등록 완료");
			this.onRegistered.run();
		} catch (ValidationException ve) {
			JOptionPane.showMessageDialog(this, ve.getMessage());
			if (ve.field == ValidationException.Field.TITLE) titleField.requestFocusInWindow();
			else if (ve.field == ValidationException.Field.CONTENT) contentArea.requestFocusInWindow();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "등록에 실패했어요. 잠시 후 다시 시도해 주세요.");
		}
	}

	private String selectedFieldOrDefault() {
		String v = fieldChips.getSelectedLabel();
		if (v == null || v.isBlank()) return "기타";
		return v;
	}

	private void scrollToTop() {
		if (scroll == null) return;
		SwingUtilities.invokeLater(() -> {
			JViewport vp = scroll.getViewport();
			if (vp != null) vp.setViewPosition(new Point(0, 0));
		});
	}

	
	
	
	private JButton iconButton(int codePoint, String tooltip) {
		JButton b = new JButton(MainUiParts.glyphIcon(codePoint, 20f, UITheme.RGB_120_120_130));
		b.setToolTipText(tooltip);
		b.setBorder(new EmptyBorder(8, 8, 8, 8));
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
				BorderFactory.createLineBorder(UITheme.RGB_235_235_242, 1, true),
				new EmptyBorder(8, 10, 8, 10)));
		tf.setBackground(UITheme.RGB_250_250_252);
	}

	
	
	
	private static class TrackWidthPanel extends JPanel implements Scrollable {
		@Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
		@Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 16; }
		@Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return 80; }
		@Override public boolean getScrollableTracksViewportWidth() { return true; }
		@Override public boolean getScrollableTracksViewportHeight() { return false; }
	}

	static class PlaceholderTextField extends JTextField {
		private final String placeholder;
		private boolean showingPlaceholder = true;

		PlaceholderTextField(String placeholder) {
			this.placeholder = placeholder;
			setText(placeholder);
			setForeground(UITheme.RGB_160_160_170);
			setBorder(new EmptyBorder(10, 10, 10, 10));
			addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusGained(java.awt.event.FocusEvent e) {
					if (showingPlaceholder) {
						setText("");
						setForeground(UITheme.TEXT);
						showingPlaceholder = false;
					}
				}
				public void focusLost(java.awt.event.FocusEvent e) {
					if (getText().trim().isEmpty()) resetToPlaceholder();
				}
			});
		}

		String getEffectiveText() {
			return showingPlaceholder ? "" : getText();
		}

		void resetToPlaceholder() {
			showingPlaceholder = true;
			setText(placeholder);
			setForeground(UITheme.RGB_160_160_170);
		}

		void setTextOrPlaceholder(String v) {
			if (v == null || v.trim().isEmpty()) resetToPlaceholder();
			else {
				showingPlaceholder = false;
				setText(v);
				setForeground(UITheme.TEXT);
			}
		}
	}

	static class PlaceholderTextArea extends JTextArea {
		private final String placeholder;
		private boolean showingPlaceholder = true;

		PlaceholderTextArea(String placeholder) {
			this.placeholder = placeholder;
			setLineWrap(true);
			setWrapStyleWord(true);
			setText(placeholder);
			setForeground(UITheme.RGB_160_160_170);
			setBorder(new EmptyBorder(10, 10, 10, 10));
			addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusGained(java.awt.event.FocusEvent e) {
					if (showingPlaceholder) {
						setText("");
						setForeground(UITheme.TEXT);
						showingPlaceholder = false;
					}
				}
				public void focusLost(java.awt.event.FocusEvent e) {
					if (getText().trim().isEmpty()) resetToPlaceholder();
				}
			});
		}

		String getEffectiveText() {
			return showingPlaceholder ? "" : getText();
		}

		void resetToPlaceholder() {
			showingPlaceholder = true;
			setText(placeholder);
			setForeground(UITheme.RGB_160_160_170);
		}

		void setTextOrPlaceholder(String v) {
			if (v == null || v.trim().isEmpty()) resetToPlaceholder();
			else {
				showingPlaceholder = false;
				setText(v);
				setForeground(UITheme.TEXT);
			}
		}
	}
}

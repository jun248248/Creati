package com.creati.ui.main;

import com.creati.model.LogStatus;
import com.creati.util.FontKit;
import com.creati.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.event.HierarchyEvent;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static com.creati.ui.main.MainUiParts.RoundedButton;

/**
 * WriteLogView - "새 성장 로그 작성" 화면
 */
public class WriteLogView extends JPanel {

	private static final int META_COMBO_W = 260;
	private static final int TITLE_FIELD_H = 56;

	public static class Draft {
		public String id;
		public String title;
		public String field;
		public String category;
		public boolean isPublic;
		public LogStatus status;
		public String linkUrl;
		public String linkPoint;
		public String goalText;
		public String mood;
		public List<String> goodPoints = new ArrayList<>();
		public String goodOther;
		public String painPoint;
		public List<String> influenceFactors = new ArrayList<>();
		public String influenceOther;
		public String processText;
		public String planGapLevel;
		public String planGapDetail;
		public String learningText;
		public List<String> nextAdjustPoints = new ArrayList<>();
		public String nextAdjustOther;
		public String nextPlan;
		public String retryCondition;
		public String workText;
		public String reasonOther;
		public List<String> reasons = new ArrayList<>();
		public String learnOneLine;
		public String retryOne;
		public boolean isDraft = true;
		public LocalDateTime updatedAt = LocalDateTime.now();
	}

	public static class DraftStore {
		private static final List<Draft> drafts = new ArrayList<>();

		public static synchronized List<Draft> list() {
			return new ArrayList<>(drafts);
		}

		public static synchronized Draft upsert(Draft d) {
			if (d.id == null || d.id.isBlank()) {
				d.id = "draft_" + System.currentTimeMillis();
				drafts.add(d);
			} else {
				int idx = -1;
				for (int i = 0; i < drafts.size(); i++) {
					if (Objects.equals(drafts.get(i).id, d.id)) {
						idx = i;
						break;
					}
				}
				if (idx >= 0)
					drafts.set(idx, d);
				else
					drafts.add(d);
			}
			d.updatedAt = LocalDateTime.now();
			return d;
		}

		public static synchronized void delete(String id) {
			drafts.removeIf(x -> Objects.equals(x.id, id));
		}
	}

	private Draft current = new Draft();
	private final Runnable onBack;
	private final Runnable onRegistered;
	private boolean editMode = false;
	private Consumer<LogPost> onEditSaved;
	private RoundedButton submitBtn;
	private DraftDrawerDialog drawer;
	private boolean dirty = false;

	private final PlaceholderTextField titleField = new PlaceholderTextField("제목을 입력해 주세요.");

	private final ChipGroup fieldChips = new ChipGroup(new ChipGroup.Item[] { new ChipGroup.Item("영상", 0xE04B),
			new ChipGroup.Item("이미지", 0xE3F4), new ChipGroup.Item("글", 0xE3C9), new ChipGroup.Item("음악", 0xE405),
			new ChipGroup.Item("기타", 0xE5D3) });
	private final PlaceholderTextField customField = new PlaceholderTextField("예: 뉴스레터");
	private final JComboBox<String> categoryCombo = new JComboBox<>(
			new String[] { "일상 / 브이로그", "공부 / 자기계발 / 교육", "생산성 / 루틴 / 습관", "개발 / IT / 프로젝트", "리뷰 / 정보 / 추천", "취미 / 관심사",
					"생각 / 마인드 / 경험 기록", "콘텐츠 제작 / 크리에이터 활동", "기타" });
	private JComboBox<LogStatus> statusCombo;
	private final JRadioButton privateBtn = new JRadioButton("비공개");
	private final JRadioButton publicBtn = new JRadioButton("공개");

	private final PlaceholderTextField linkField = new PlaceholderTextField("참고 링크를 붙여넣어 주세요 (선택)");
	private final PlaceholderTextField linkFocusField = new PlaceholderTextField("링크에서 특히 확인하고 싶은 부분이 있다면? (선택)");
	private JPanel linkFocusWrapper; // CardLayout으로 빈 슬롯 ↔ linkFocusField 전환

	private final PlaceholderTextArea goalArea = new PlaceholderTextArea("예: 업로드 후 조회수 1,000 달성 / 편집 흐름 매끄럽게 만들기");
	private final ChipGroup moodChips = new ChipGroup(
			new ChipGroup.Item[] { new ChipGroup.Item("만족해요", 0xE815), new ChipGroup.Item("괜찮아요", 0xE813),
					new ChipGroup.Item("조금 아쉬워요", 0xE812), new ChipGroup.Item("많이 아쉬워요", 0xE814) });

	private final JCheckBox[] goodPointChecks = new JCheckBox[] { new JCheckBox("계획 / 방향 설정"),
			new JCheckBox("작업 과정 / 루틴"), new JCheckBox("결과물 완성도"), new JCheckBox("시간 활용"), new JCheckBox("반응 / 성과"),
			new JCheckBox("기타") };
	private final PlaceholderTextField goodOtherField = new PlaceholderTextField("직접 입력");

	private final PlaceholderTextArea painArea = new PlaceholderTextArea("예: 편집 템포가 늘어졌고, 인트로가 약했어");
	private final JCheckBox[] factorChecks = new JCheckBox[] { new JCheckBox("준비가 충분하지 않았다고 느꼈어요"),
			new JCheckBox("계획했던 방식과 달라졌어요"), new JCheckBox("집중 흐름이 잘 유지되지 않았어요"), new JCheckBox("작업 난이도가 높게 느껴졌어요"),
			new JCheckBox("컨디션 / 감정 영향이 있었어요"), new JCheckBox("외부 상황 영향이 있었어요 (일정, 환경 등)"), new JCheckBox("기타") };
	private final PlaceholderTextField factorOtherField = new PlaceholderTextField("직접 입력");

	private final PlaceholderTextArea processArea = new PlaceholderTextArea(
			"예: 기획 → 촬영 → 컷 편집 → 자막 → BGM → 업로드 순서로 진행했어");
	private final ChipGroup planGapChips = new ChipGroup(new ChipGroup.Item[] { new ChipGroup.Item("거의 비슷해요", 0xE812),
			new ChipGroup.Item("일부 달라요", 0xE811), new ChipGroup.Item("많이 달라요", 0xE814) });
	private final PlaceholderTextArea planGapArea = new PlaceholderTextArea("예: 촬영 시간이 늘어나서 편집을 급하게 했어");

	private final PlaceholderTextArea learningArea = new PlaceholderTextArea("예: 다음엔 훅(인트로)을 먼저 잡고 시작하자");
	private final JCheckBox[] nextAdjustChecks = new JCheckBox[] { new JCheckBox("작업 방식"), new JCheckBox("준비 과정"),
			new JCheckBox("시간 활용"), new JCheckBox("집중 환경"), new JCheckBox("전략 / 방향"), new JCheckBox("기타") };
	private final PlaceholderTextField nextAdjustOtherField = new PlaceholderTextField("직접 입력");

	private final ChipGroup nextPlanChips = new ChipGroup(new ChipGroup.Item[] {
			new ChipGroup.Item("바로 다시 시도해볼래요", 0xE815), new ChipGroup.Item("조금 보완 후 진행하고 싶어요", 0xE813),
			new ChipGroup.Item("고민 중이에요", 0xE811), new ChipGroup.Item("잠시 쉬어갈 계획이에요", 0xE813) });
	private final PlaceholderTextArea retryConditionArea = new PlaceholderTextArea("예: 대본을 먼저 완성하고 편집을 시작하기");
	private final JLabel retryConditionTitle = new JLabel();

	private CardLayout wizardLayout;
	private JPanel wizardCards;
	private JButton stepBackBtn;
	private RoundedButton stepNextBtn;
	private JLabel stepHint;

	private final List<String> stepKeys = new ArrayList<>();
	private int stepIndex = 0;

	public WriteLogView(JFrame owner, Runnable onBack, Runnable onRegistered) {
		UITheme.ensureInit();
		FontKit.init();
		this.onBack = onBack;
		this.onRegistered = onRegistered;
		setLayout(new BorderLayout());
		setBackground(UITheme.BG);
		add(buildTopBar(owner), BorderLayout.NORTH);
		add(buildBody(), BorderLayout.CENTER);
		privateBtn.setSelected(true);
		wireEvents(owner);
		rebuildWizardFlow();
		showStep(0);
	}

	public boolean isDirty() {
		return dirty;
	}

	public boolean confirmLeave() {
		if (!dirty)
			return true;
		int res = JOptionPane.showConfirmDialog(this, "작성 중인 내용이 있어요. 임시 저장 후 나갈까요?", "나가기",
				JOptionPane.YES_NO_CANCEL_OPTION);
		if (res == JOptionPane.CANCEL_OPTION)
			return false;
		if (res == JOptionPane.YES_OPTION)
			doDraftSave(false);
		return true;
	}

	public void startNew() {
		editMode = false;
		onEditSaved = null;
		current = new Draft();
		resetFormToDefault();
		rebuildWizardFlow();
		showStep(0);
		dirty = false;
	}

	private void resetFormToDefault() {
		titleField.setTextOrPlaceholder("");
		fieldChips.clearSelection();
		customField.setVisible(false);
		customField.setTextOrPlaceholder("");
		categoryCombo.setSelectedIndex(0);
		if (statusCombo != null)
			statusCombo.setSelectedItem(LogStatus.IN_PROGRESS);
		privateBtn.setSelected(true);
		publicBtn.setSelected(false);
		linkField.setTextOrPlaceholder("");
		linkFocusField.setTextOrPlaceholder("");
		toggleLinkFocusVisibility();
		goalArea.setTextOrPlaceholder("");
		moodChips.clearSelection();
		for (JCheckBox cb : goodPointChecks)
			cb.setSelected(false);
		goodOtherField.setEnabled(false);
		goodOtherField.setTextOrPlaceholder("");
		painArea.setTextOrPlaceholder("");
		for (JCheckBox cb : factorChecks)
			cb.setSelected(false);
		factorOtherField.setEnabled(false);
		factorOtherField.setTextOrPlaceholder("");
		processArea.setTextOrPlaceholder("");
		planGapChips.clearSelection();
		planGapArea.setTextOrPlaceholder("");
		learningArea.setTextOrPlaceholder("");
		for (JCheckBox cb : nextAdjustChecks)
			cb.setSelected(false);
		nextAdjustOtherField.setEnabled(false);
		nextAdjustOtherField.setTextOrPlaceholder("");
		nextPlanChips.clearSelection();
		retryConditionArea.setTextOrPlaceholder("");
		refreshRetryConditionTitle();
	}

	private JComponent buildTopBar(JFrame owner) {
		JPanel bar = new JPanel(new BorderLayout());
		bar.setBackground(Color.WHITE);
		bar.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 235)),
				new EmptyBorder(12, 16, 12, 16)));

		JButton back = iconButton(0xE5C4, "나가기");
		back.addActionListener(e -> {
			if (!confirmLeave())
				return;
			if (this.onBack != null)
				this.onBack.run();
		});
		JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		left.setOpaque(false);
		left.add(back);

		JLabel title = new JLabel("새 성장 로그 작성");
		title.setFont(UITheme.BODY_MED);
		title.setForeground(UITheme.TEXT);

		JButton draftsBtn = new JButton("임시보관함");
		draftsBtn.setFont(UITheme.BODY_MED);
		draftsBtn.setForeground(UITheme.TEXT);
		draftsBtn.setBackground(Color.WHITE);
		draftsBtn.setBorder(new EmptyBorder(8, 10, 8, 10));
		draftsBtn.setFocusPainted(false);
		draftsBtn.setContentAreaFilled(false);
		draftsBtn.setOpaque(false);
		draftsBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		draftsBtn.addActionListener(e -> openDrawer(owner));

		JButton draftSaveBtn = new JButton("임시 저장");
		draftSaveBtn.setFont(UITheme.BODY_MED);
		draftSaveBtn.setForeground(UITheme.TEXT);
		draftSaveBtn.setBackground(Color.WHITE);
		draftSaveBtn.setBorder(new EmptyBorder(8, 10, 8, 10));
		draftSaveBtn.setFocusPainted(false);
		draftSaveBtn.setContentAreaFilled(false);
		draftSaveBtn.setOpaque(false);
		draftSaveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		draftSaveBtn.addActionListener(e -> doDraftSave(true));

		submitBtn = new RoundedButton("저장");
		submitBtn.setBackground(UITheme.ACCENT_PURPLE);
		submitBtn.setForeground(Color.WHITE);
		submitBtn.setFont(UITheme.BODY_MED);
		submitBtn.setVisible(false);
		submitBtn.addActionListener(e -> onSubmit());

		JPanel right = new JPanel();
		right.setOpaque(false);
		right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
		right.add(draftsBtn);
		right.add(Box.createHorizontalStrut(8));
		right.add(draftSaveBtn);
		right.add(Box.createHorizontalStrut(12));
		right.add(submitBtn);

		bar.add(left, BorderLayout.WEST);
		bar.add(title, BorderLayout.CENTER);
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

		JPanel titleCard = new JPanel(new BorderLayout());
		titleCard.setBackground(Color.WHITE);
		titleCard.setAlignmentX(Component.LEFT_ALIGNMENT);
		titleCard.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(230, 230, 235), 1, true), new EmptyBorder(14, 14, 14, 14)));

		titleField.setFont(FontKit.medium(22f));
		titleField.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
		titleField.setBackground(new Color(250, 250, 252));
		titleField.setPreferredSize(new Dimension(10, TITLE_FIELD_H));
		titleField.setMinimumSize(new Dimension(10, TITLE_FIELD_H));
		titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, TITLE_FIELD_H));

		titleCard.add(titleField, BorderLayout.CENTER);
		titleCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, TITLE_FIELD_H + 28));

		wrap.add(titleCard);
		wrap.add(Box.createVerticalStrut(12));

		// Wizard cards
		wizardLayout = new CardLayout();
		wizardCards = new JPanel(wizardLayout);
		wizardCards.setOpaque(false);
		wizardCards.setAlignmentX(Component.LEFT_ALIGNMENT);
		wizardCards.add(buildStepMeta(), "meta");
		wizardCards.add(buildStepGoal(), "goal");
		wizardCards.add(buildStepMood(), "mood");
		wizardCards.add(buildStepGoodPoints(), "good");
		wizardCards.add(buildStepPainPoint(), "pain");
		wizardCards.add(buildStepFactors(), "factors");
		wizardCards.add(buildStepProcess(), "process");
		wizardCards.add(buildStepPlanGap(), "plangap");
		wizardCards.add(buildStepPlanGapDetail(), "plangap_detail");
		wizardCards.add(buildStepLearning(), "learning");
		wizardCards.add(buildStepNextAdjust(), "next_adjust");
		wizardCards.add(buildStepNextPlan(), "next_plan");
		wizardCards.add(buildStepRetryCondition(), "retry_condition");
		wizardCards.add(buildStepSummary(), "summary");

		wrap.add(wizardCards);
		wrap.add(Box.createVerticalStrut(12));
		wrap.add(buildWizardNav());

		JScrollPane sp = new JScrollPane(wrap);
		sp.setBorder(BorderFactory.createEmptyBorder());
		sp.getVerticalScrollBar().setUnitIncrement(16);
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		sp.getViewport().setBackground(UITheme.BG);

		root.add(sp, BorderLayout.CENTER);
		return root;
	}

	private JComponent buildWizardNav() {
		JPanel bar = new JPanel(new BorderLayout());
		bar.setOpaque(false);
		bar.setAlignmentX(Component.LEFT_ALIGNMENT);
		bar.setBorder(new EmptyBorder(0, 4, 0, 4));

		stepHint = new JLabel(" ");
		stepHint.setFont(UITheme.CAPTION);
		stepHint.setForeground(new Color(125, 125, 140));

		stepBackBtn = new JButton("이전");
		stepBackBtn.setFont(UITheme.BODY_MED);
		stepBackBtn.setForeground(UITheme.TEXT);
		stepBackBtn.setBackground(Color.WHITE);
		stepBackBtn.setBorder(new EmptyBorder(10, 14, 10, 14));
		stepBackBtn.setFocusPainted(false);
		stepBackBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		stepBackBtn.addActionListener(e -> goPrevStep());

		stepNextBtn = new RoundedButton("다음");
		stepNextBtn.setBackground(UITheme.ACCENT_PURPLE);
		stepNextBtn.setForeground(Color.WHITE);
		stepNextBtn.setFont(UITheme.BODY_MED);
		stepNextBtn.addActionListener(e -> goNextStep());

		JPanel right = new JPanel();
		right.setOpaque(false);
		right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
		right.add(stepBackBtn);
		right.add(Box.createHorizontalStrut(10));
		right.add(stepNextBtn);

		bar.add(stepHint, BorderLayout.WEST);
		bar.add(right, BorderLayout.EAST);
		return bar;
	}

	private JPanel buildStepMeta() {
		return cardWithTitle("기본 정보", panel -> {
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.add(Box.createVerticalStrut(14));
			panel.add(leftAligned(buildFieldCell()));
			panel.add(Box.createVerticalStrut(14));

			JPanel row2 = new JPanel();
			row2.setOpaque(false);
			row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));
			JComponent catCell = buildCategoryCell();
			JComponent statusCell = buildStatusCell();
			JComponent visCell = buildVisibilityCell();
			catCell.setAlignmentY(Component.TOP_ALIGNMENT);
			statusCell.setAlignmentY(Component.TOP_ALIGNMENT);
			visCell.setAlignmentY(Component.TOP_ALIGNMENT);
			row2.add(catCell);
			row2.add(Box.createHorizontalStrut(14));
			row2.add(statusCell);
			row2.add(Box.createHorizontalStrut(14));
			row2.add(visCell);
			row2.add(Box.createHorizontalGlue());
			row2.setAlignmentX(Component.LEFT_ALIGNMENT);

			panel.add(leftAligned(row2));
			panel.add(Box.createVerticalStrut(14));
			panel.add(leftAligned(buildUploadLinkCell()));
		});
	}

	private JPanel buildStepLink() {
		return cardWithTitle("업로드 링크", panel -> {
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			JLabel t = new JLabel("참고하거나 기록해 두고 싶은 링크가 있나요? (선택)");
			t.setFont(UITheme.BODY_MED);
			t.setForeground(UITheme.TEXT);
			panel.add(leftAligned(t));
			panel.add(Box.createVerticalStrut(10));
			styleInput(linkField);
			panel.add(leftAligned(linkField));
			panel.add(Box.createVerticalStrut(12));
			styleInput(linkFocusField);
			setLinkFocusFieldShown(false);
			panel.add(leftAligned(linkFocusField));
		});
	}

	private JPanel buildStepGoal() {
		return cardWithTitle("목표 / 기대", panel -> {
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			JLabel t = new JLabel("이번 시도를 통해 기대했던 점은 무엇이었나요?");
			t.setFont(UITheme.BODY_MED);
			t.setForeground(UITheme.TEXT);
			panel.add(leftAligned(t));
			panel.add(Box.createVerticalStrut(10));
			goalArea.setFont(UITheme.BODY);
			goalArea.setBackground(new Color(250, 250, 252));
			goalArea.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(235, 235, 242), 1, true),
					new EmptyBorder(10, 10, 10, 10)));
			JScrollPane sp = new JScrollPane(goalArea);
			sp.setBorder(BorderFactory.createEmptyBorder());
			sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			sp.setPreferredSize(new Dimension(0, 140));
			panel.add(leftAligned(sp));
		});
	}

	private JPanel buildStepMood() {
		return cardWithTitle("결과 인식", panel -> {
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			JLabel t = new JLabel("이번 진행 과정은 어떻게 느껴졌나요?");
			t.setFont(UITheme.BODY_MED);
			t.setForeground(UITheme.TEXT);
			panel.add(leftAligned(t));
			panel.add(Box.createVerticalStrut(10));
			panel.add(leftAligned(moodChips));
		});
	}

	private JPanel buildStepGoodPoints() {
		return cardWithTitle("잘 된 부분", panel -> {
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			JLabel t = new JLabel("특히 잘 되었다고 느낀 부분이 있었나요?");
			t.setFont(UITheme.BODY_MED);
			t.setForeground(UITheme.TEXT);
			panel.add(leftAligned(t));
			panel.add(Box.createVerticalStrut(10));
			JPanel grid = new JPanel(new GridLayout(0, 2, 12, 8));
			grid.setOpaque(false);
			JCheckBox other = null;
			for (JCheckBox cb : goodPointChecks) {
				cb.setOpaque(false);
				cb.setFont(UITheme.BODY);
				cb.setForeground(UITheme.TEXT);
				if ("기타".equals(cb.getText())) {
					other = cb;
					continue;
				}
				cb.addActionListener(e -> markDirty());
				grid.add(cb);
			}
			panel.add(leftAligned(grid));
			panel.add(Box.createVerticalStrut(10));
			if (other != null) {
				final JCheckBox otherCb = other;
				JPanel otherRow = new JPanel();
				otherRow.setOpaque(false);
				otherRow.setLayout(new BoxLayout(otherRow, BoxLayout.X_AXIS));
				otherCb.addActionListener(e -> {
					boolean on = otherCb.isSelected();
					goodOtherField.setEnabled(on);
					if (!on)
						goodOtherField.setTextOrPlaceholder("");
					markDirty();
				});
				otherRow.add(otherCb);
				otherRow.add(Box.createHorizontalStrut(10));
				styleInput(goodOtherField);
				goodOtherField.setEnabled(false);
				goodOtherField.setPreferredSize(new Dimension(220, 38));
				goodOtherField.setMaximumSize(new Dimension(260, 38));
				otherRow.add(goodOtherField);
				panel.add(leftAligned(otherRow));
			}
		});
	}

	private JPanel buildStepPainPoint() {
		return cardWithTitle("아쉬움", panel -> {
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			JLabel t = new JLabel("가장 아쉽게 느껴졌던 부분이 있다면?");
			t.setFont(UITheme.BODY_MED);
			t.setForeground(UITheme.TEXT);
			panel.add(leftAligned(t));
			panel.add(Box.createVerticalStrut(10));
			painArea.setFont(UITheme.BODY);
			painArea.setBackground(new Color(250, 250, 252));
			painArea.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(235, 235, 242), 1, true),
					new EmptyBorder(10, 10, 10, 10)));
			JScrollPane sp = new JScrollPane(painArea);
			sp.setBorder(BorderFactory.createEmptyBorder());
			sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			sp.setPreferredSize(new Dimension(0, 140));
			panel.add(leftAligned(sp));
		});
	}

	private JPanel buildStepFactors() {
		return cardWithTitle("영향 요인", panel -> {
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			JLabel t = new JLabel("어떤 부분이 진행에 영향을 준 것 같나요?");
			t.setFont(UITheme.BODY_MED);
			t.setForeground(UITheme.TEXT);
			panel.add(leftAligned(t));
			panel.add(Box.createVerticalStrut(10));
			JPanel grid = new JPanel(new GridLayout(0, 2, 12, 8));
			grid.setOpaque(false);
			JCheckBox other = null;
			for (JCheckBox cb : factorChecks) {
				cb.setOpaque(false);
				cb.setFont(UITheme.BODY);
				cb.setForeground(UITheme.TEXT);
				if ("기타".equals(cb.getText())) {
					other = cb;
					continue;
				}
				cb.addActionListener(e -> markDirty());
				grid.add(cb);
			}
			panel.add(leftAligned(grid));
			panel.add(Box.createVerticalStrut(10));
			if (other != null) {
				final JCheckBox otherCb = other;
				JPanel otherRow = new JPanel();
				otherRow.setOpaque(false);
				otherRow.setLayout(new BoxLayout(otherRow, BoxLayout.X_AXIS));
				otherCb.addActionListener(e -> {
					boolean on = otherCb.isSelected();
					factorOtherField.setEnabled(on);
					if (!on)
						factorOtherField.setTextOrPlaceholder("");
					markDirty();
				});
				otherRow.add(otherCb);
				otherRow.add(Box.createHorizontalStrut(10));
				styleInput(factorOtherField);
				factorOtherField.setEnabled(false);
				factorOtherField.setPreferredSize(new Dimension(260, 38));
				factorOtherField.setMaximumSize(new Dimension(320, 38));
				otherRow.add(factorOtherField);
				panel.add(leftAligned(otherRow));
			}
		});
	}

	private JPanel buildStepProcess() {
		return cardWithTitle("행동 과정", panel -> {
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			JLabel t = new JLabel("진행하면서 어떤 방식으로 작업하거나 행동하셨나요?");
			t.setFont(UITheme.BODY_MED);
			t.setForeground(UITheme.TEXT);
			panel.add(leftAligned(t));
			panel.add(Box.createVerticalStrut(8));
			JLabel guide = new JLabel("예: 준비 → 실행 → 점검 흐름, 사용한 도구/방법, 작업 순서");
			guide.setFont(UITheme.CAPTION);
			guide.setForeground(new Color(130, 130, 140));
			panel.add(leftAligned(guide));
			panel.add(Box.createVerticalStrut(10));
			processArea.setFont(UITheme.BODY);
			processArea.setBackground(new Color(250, 250, 252));
			processArea.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(235, 235, 242), 1, true),
					new EmptyBorder(10, 10, 10, 10)));
			JScrollPane sp = new JScrollPane(processArea);
			sp.setBorder(BorderFactory.createEmptyBorder());
			sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			sp.setPreferredSize(new Dimension(0, 160));
			panel.add(leftAligned(sp));
		});
	}

	private JPanel buildStepPlanGap() {
		return cardWithTitle("계획 차이", panel -> {
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			JLabel t = new JLabel("원래 생각했던 흐름과 차이가 있었나요?");
			t.setFont(UITheme.BODY_MED);
			t.setForeground(UITheme.TEXT);
			panel.add(leftAligned(t));
			panel.add(Box.createVerticalStrut(10));
			panel.add(leftAligned(planGapChips));
		});
	}

	private JPanel buildStepPlanGapDetail() {
		return cardWithTitle("차이 내용", panel -> {
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			JLabel t = new JLabel("어떤 부분에서 차이가 있었나요?");
			t.setFont(UITheme.BODY_MED);
			t.setForeground(UITheme.TEXT);
			panel.add(leftAligned(t));
			panel.add(Box.createVerticalStrut(10));
			planGapArea.setFont(UITheme.BODY);
			planGapArea.setBackground(new Color(250, 250, 252));
			planGapArea.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(235, 235, 242), 1, true),
					new EmptyBorder(10, 10, 10, 10)));
			JScrollPane sp = new JScrollPane(planGapArea);
			sp.setBorder(BorderFactory.createEmptyBorder());
			sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			sp.setPreferredSize(new Dimension(0, 140));
			panel.add(leftAligned(sp));
		});
	}

	private JPanel buildStepLearning() {
		return cardWithTitle("회고", panel -> {
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			JLabel t = new JLabel("이번 경험에서 느낀 점이나 얻은 점이 있다면?");
			t.setFont(UITheme.BODY_MED);
			t.setForeground(UITheme.TEXT);
			panel.add(leftAligned(t));
			panel.add(Box.createVerticalStrut(10));
			learningArea.setFont(UITheme.BODY);
			learningArea.setBackground(new Color(250, 250, 252));
			learningArea.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(235, 235, 242), 1, true),
					new EmptyBorder(10, 10, 10, 10)));
			JScrollPane sp = new JScrollPane(learningArea);
			sp.setBorder(BorderFactory.createEmptyBorder());
			sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			sp.setPreferredSize(new Dimension(0, 140));
			panel.add(leftAligned(sp));
		});
	}

	private JPanel buildStepNextAdjust() {
		return cardWithTitle("다음 조정 포인트", panel -> {
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			JLabel t = new JLabel("다음에 시도한다면 조정해 보고 싶은 부분이 있을까요?");
			t.setFont(UITheme.BODY_MED);
			t.setForeground(UITheme.TEXT);
			panel.add(leftAligned(t));
			panel.add(Box.createVerticalStrut(10));
			JPanel grid = new JPanel(new GridLayout(0, 2, 12, 8));
			grid.setOpaque(false);
			JCheckBox other = null;
			for (JCheckBox cb : nextAdjustChecks) {
				cb.setOpaque(false);
				cb.setFont(UITheme.BODY);
				cb.setForeground(UITheme.TEXT);
				if ("기타".equals(cb.getText())) {
					other = cb;
					continue;
				}
				cb.addActionListener(e -> markDirty());
				grid.add(cb);
			}
			panel.add(leftAligned(grid));
			panel.add(Box.createVerticalStrut(10));
			if (other != null) {
				final JCheckBox otherCb = other;
				JPanel otherRow = new JPanel();
				otherRow.setOpaque(false);
				otherRow.setLayout(new BoxLayout(otherRow, BoxLayout.X_AXIS));
				otherCb.addActionListener(e -> {
					boolean on = otherCb.isSelected();
					nextAdjustOtherField.setEnabled(on);
					if (!on)
						nextAdjustOtherField.setTextOrPlaceholder("");
					markDirty();
				});
				otherRow.add(otherCb);
				otherRow.add(Box.createHorizontalStrut(10));
				styleInput(nextAdjustOtherField);
				nextAdjustOtherField.setEnabled(false);
				nextAdjustOtherField.setPreferredSize(new Dimension(220, 38));
				nextAdjustOtherField.setMaximumSize(new Dimension(260, 38));
				otherRow.add(nextAdjustOtherField);
				panel.add(leftAligned(otherRow));
			}
		});
	}

	private JPanel buildStepNextPlan() {
		return cardWithTitle("다음 시도 계획", panel -> {
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			JLabel t = new JLabel("다음 시도 계획은 어떻게 생각하고 계신가요?");
			t.setFont(UITheme.BODY_MED);
			t.setForeground(UITheme.TEXT);
			panel.add(leftAligned(t));
			panel.add(Box.createVerticalStrut(10));
			panel.add(leftAligned(nextPlanChips));
		});
	}

	private JPanel buildStepRetryCondition() {
		return cardWithTitle("재시도 조건", panel -> {
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			retryConditionTitle.setFont(UITheme.BODY_MED);
			retryConditionTitle.setForeground(UITheme.TEXT);
			panel.add(leftAligned(retryConditionTitle));
			panel.add(Box.createVerticalStrut(10));
			retryConditionArea.setFont(UITheme.BODY);
			retryConditionArea.setBackground(new Color(250, 250, 252));
			retryConditionArea.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(235, 235, 242), 1, true),
					new EmptyBorder(10, 10, 10, 10)));
			JScrollPane sp = new JScrollPane(retryConditionArea);
			sp.setBorder(BorderFactory.createEmptyBorder());
			sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			sp.setPreferredSize(new Dimension(0, 140));
			panel.add(leftAligned(sp));
		});
	}

	private JPanel buildStepSummary() {
		return cardWithTitle("작성 완료", panel -> {
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			JLabel t = new JLabel("작성한 내용을 한 번 확인해 볼까요?");
			t.setFont(UITheme.BODY_MED);
			t.setForeground(UITheme.TEXT);
			panel.add(leftAligned(t));
			panel.add(Box.createVerticalStrut(12));

			JPanel box = new JPanel(new GridBagLayout());
			box.setBackground(new Color(250, 250, 252));
			box.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(235, 235, 242), 1, true),
					new EmptyBorder(12, 12, 12, 12)));

			GridBagConstraints gc = new GridBagConstraints();
			gc.anchor = GridBagConstraints.NORTHWEST;
			gc.fill = GridBagConstraints.HORIZONTAL;
			gc.weightx = 1.0;
			gc.insets = new Insets(4, 0, 4, 0);

			JLabel vTitle = new JLabel(), vField = new JLabel(), vCategory = new JLabel(), vStatus = new JLabel(),
					vVisibility = new JLabel(), vMood = new JLabel(), vNext = new JLabel();
			styleSummaryValue(vTitle);
			styleSummaryValue(vField);
			styleSummaryValue(vCategory);
			styleSummaryValue(vStatus);
			styleSummaryValue(vVisibility);
			styleSummaryValue(vMood);
			styleSummaryValue(vNext);

			int r = 0;
			addSummaryRow(box, gc, r++, "제목", vTitle);
			addSummaryRow(box, gc, r++, "분야", vField);
			addSummaryRow(box, gc, r++, "카테고리", vCategory);
			addSummaryRow(box, gc, r++, "현재 상태", vStatus);
			addSummaryRow(box, gc, r++, "공개 범위", vVisibility);
			addSummaryRow(box, gc, r++, "진행 느낌", vMood);
			addSummaryRow(box, gc, r++, "다음 계획", vNext);

			panel.add(leftAligned(box));
			panel.add(Box.createVerticalStrut(14));

			JLabel hint = new JLabel("지금 작성한 내용을 토대로 AI 분석을 받아볼 수 있어요.");
			hint.setFont(UITheme.BODY_MED);
			hint.setForeground(UITheme.TEXT);

			RoundedButton goAi = new RoundedButton("AI 분석 바로가기");
			goAi.setFont(UITheme.BODY_MED);
			goAi.setBackground(new Color(0xF1F2F4));
			goAi.setForeground(new Color(0x333333));
			goAi.addActionListener(e -> saveAndNavigateToAi());

			JPanel row = new JPanel();
			row.setOpaque(false);
			row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
			row.add(hint);
			row.add(Box.createHorizontalStrut(12));
			row.add(goAi);
			row.add(Box.createHorizontalGlue());
			panel.add(leftAligned(row));

			panel.addHierarchyListener(e -> {
				if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && panel.isShowing()) {
					vTitle.setText(titleField.getEffectiveText().trim());
					vField.setText(resolveField());
					vCategory.setText(Objects.toString(categoryCombo.getSelectedItem(), ""));
					vStatus.setText(statusCombo == null ? "" : Objects.toString(statusCombo.getSelectedItem(), ""));
					vVisibility.setText(publicBtn.isSelected() ? "공개" : "비공개");
					String mood = moodChips.getSelectedText();
					vMood.setText(mood == null ? "" : stripEmoji(mood));
					String next = nextPlanChips.getSelectedText();
					vNext.setText(next == null ? "" : stripEmoji(next));
				}
			});
		});
	}

	private void styleSummaryValue(JLabel l) {
		l.setFont(UITheme.BODY);
		l.setForeground(UITheme.TEXT);
	}

	private void addSummaryRow(JPanel box, GridBagConstraints gc, int row, String k, JLabel v) {
		JLabel key = new JLabel(k + ": ");
		key.setFont(UITheme.BODY_MED);
		key.setForeground(new Color(90, 90, 105));
		GridBagConstraints c1 = (GridBagConstraints) gc.clone();
		c1.gridx = 0;
		c1.gridy = row;
		c1.weightx = 0;
		c1.insets = new Insets(3, 0, 3, 10);
		box.add(key, c1);
		GridBagConstraints c2 = (GridBagConstraints) gc.clone();
		c2.gridx = 1;
		c2.gridy = row;
		c2.weightx = 1;
		c2.insets = new Insets(3, 0, 3, 0);
		box.add(v, c2);
	}

	private String stripEmoji(String s) {
		if (s == null)
			return "";
		return s.replaceAll("^[\\p{So}\\p{Sk}\\p{Cs}\\s]+", "").trim();
	}

	private void tryNavigateToAi() {
		Window w = SwingUtilities.getWindowAncestor(this);
		if (w instanceof MainFrame) {
			((MainFrame) w).navigateToAi();
			return;
		}
		JOptionPane.showMessageDialog(this, "왼쪽 메뉴의 AI 분석 탭에서 확인할 수 있어요.");
	}

	private void saveAndNavigateToAi() {
		if (!validateStep("meta"))
			return;

		Draft d = snapshotFromWizard(false);
		d.isDraft = false;
		DraftStore.upsert(d);

		dirty = false;

		JOptionPane.showMessageDialog(this, "저장 완료! AI 분석으로 이동할게요.");

		Window w = SwingUtilities.getWindowAncestor(this);
		if (w instanceof MainFrame) {
			((MainFrame) w).navigateToAi();
			return;
		}
		JOptionPane.showMessageDialog(this, "왼쪽 메뉴의 AI 분석 탭에서 확인할 수 있어요.");
	}

	private JComponent buildFieldCell() {
		JPanel col = new JPanel();
		col.setOpaque(false);
		col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
		col.add(leftAligned(rowTitle("분야 *")));
		col.add(Box.createVerticalStrut(8));
		JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		line.setOpaque(false);
		line.add(fieldChips);
		customField.setVisible(false);
		styleInput(customField);
		customField.setPreferredSize(new Dimension(180, 38));
		customField.setMaximumSize(new Dimension(220, 38));
		line.add(customField);
		col.add(leftAligned(line));
		return col;
	}

	private JComponent buildCategoryCell() {
		JPanel col = new JPanel();
		col.setOpaque(false);
		col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
		col.add(leftAligned(rowTitle("카테고리 *")));
		col.add(Box.createVerticalStrut(8));
		categoryCombo.setFont(UITheme.BODY);
		categoryCombo.setBorder(BorderFactory.createEmptyBorder());
		categoryCombo.setBackground(new Color(250, 250, 252));
		categoryCombo.setPrototypeDisplayValue("콘텐츠 제작 / 크리에이터 활동");
		categoryCombo.setMaximumRowCount(12);
		JPanel wrap = new JPanel(new BorderLayout());
		wrap.setBackground(new Color(250, 250, 252));
		wrap.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(235, 235, 242), 1, true), new EmptyBorder(6, 10, 6, 10)));
		wrap.add(categoryCombo, BorderLayout.CENTER);
		wrap.setPreferredSize(new Dimension(META_COMBO_W, 38));
		wrap.setMaximumSize(new Dimension(META_COMBO_W, 38));
		col.add(leftAligned(wrap));
		return col;
	}

	private JComponent buildStatusCell() {
		JPanel col = new JPanel();
		col.setOpaque(false);
		col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
		col.add(leftAligned(rowTitle("현재 상태 *")));
		col.add(Box.createVerticalStrut(8));
		statusCombo = new JComboBox<>(LogStatus.values());
		statusCombo.setSelectedItem(LogStatus.IN_PROGRESS);
		statusCombo.setFont(UITheme.BODY);
		statusCombo.setBorder(BorderFactory.createEmptyBorder());
		statusCombo.setBackground(new Color(250, 250, 252));
		statusCombo.setFocusable(false);
		statusCombo.addActionListener(e -> markDirty());
		JPanel wrap = new JPanel(new BorderLayout());
		wrap.setBackground(new Color(250, 250, 252));
		wrap.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(235, 235, 242), 1, true), new EmptyBorder(6, 10, 6, 10)));
		wrap.add(statusCombo, BorderLayout.CENTER);
		wrap.setPreferredSize(new Dimension(META_COMBO_W, 38));
		wrap.setMaximumSize(new Dimension(META_COMBO_W, 38));
		col.add(leftAligned(wrap));
		return col;
	}

	private JComponent buildVisibilityCell() {
		JPanel col = new JPanel();
		col.setOpaque(false);
		col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
		col.add(leftAligned(rowTitle("공개 범위 *")));
		col.add(Box.createVerticalStrut(8));
		ChipGroup visibilityChips = new ChipGroup(
				new ChipGroup.Item[] { new ChipGroup.Item("비공개", 0xE897), new ChipGroup.Item("공개", 0xE898) });
		visibilityChips.selectByText("비공개");
		visibilityChips.setOnSelectionChanged(() -> {
			String v = visibilityChips.getSelectedText();
			privateBtn.setSelected("비공개".equals(v));
			publicBtn.setSelected("공개".equals(v));
			markDirty();
		});
		privateBtn.setVisible(false);
		publicBtn.setVisible(false);
		col.add(leftAligned(visibilityChips));
		return col;
	}

	private JComponent buildUploadLinkCell() {
		JPanel col = new JPanel();
		col.setOpaque(false);
		col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
		col.add(leftAligned(rowTitle("업로드 링크 (선택)")));
		col.add(Box.createVerticalStrut(8));

		styleInput(linkField);
		linkField.setPreferredSize(new Dimension(0, 38));
		linkField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
		col.add(leftAligned(linkField));
		col.add(Box.createVerticalStrut(10));

		styleInput(linkFocusField);
		linkFocusField.setPreferredSize(new Dimension(Integer.MAX_VALUE, 38));
		linkFocusField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

		JPanel emptySlot = new JPanel();
		emptySlot.setOpaque(false);
		emptySlot.setPreferredSize(new Dimension(Integer.MAX_VALUE, 38));
		emptySlot.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

		linkFocusWrapper = new JPanel(new CardLayout());
		linkFocusWrapper.setOpaque(false);
		linkFocusWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		linkFocusWrapper.setPreferredSize(new Dimension(0, 38));
		linkFocusWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
		linkFocusWrapper.add(emptySlot, "empty");
		linkFocusWrapper.add(linkFocusField, "field");

		((CardLayout) linkFocusWrapper.getLayout()).show(linkFocusWrapper, "empty");

		col.add(linkFocusWrapper);
		return col;
	}

	private void setLinkFocusFieldShown(boolean shown) {
		if (linkFocusWrapper == null)
			return;
		((CardLayout) linkFocusWrapper.getLayout()).show(linkFocusWrapper, shown ? "field" : "empty");
	}

	private void wireEvents(JFrame owner) {
		attachDirtyListener(titleField);
		fieldChips.setOnSelectionChanged(() -> {
			String selected = fieldChips.getSelectedText();
			boolean isCustom = "기타".equals(selected);
			customField.setVisible(isCustom);
			if (!isCustom)
				customField.setTextOrPlaceholder("");
			revalidate();
			repaint();
			markDirty();
		});
		attachDirtyListener(customField);
		categoryCombo.addActionListener(e -> markDirty());
		attachDirtyListener(linkField);
		attachDirtyListener(linkFocusField);
		linkField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				toggleLinkFocusVisibility();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				toggleLinkFocusVisibility();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				toggleLinkFocusVisibility();
			}
		});
		toggleLinkFocusVisibility();
		attachDirtyListener(goalArea);
		moodChips.setOnSelectionChanged(this::markDirty);
		attachDirtyListener(painArea);
		attachDirtyListener(processArea);
		attachDirtyListener(planGapArea);
		attachDirtyListener(learningArea);
		attachDirtyListener(retryConditionArea);
		attachDirtyListener(goodOtherField);
		attachDirtyListener(factorOtherField);
		attachDirtyListener(nextAdjustOtherField);
		nextPlanChips.setOnSelectionChanged(() -> {
			markDirty();
			refreshRetryConditionTitle();
		});
		planGapChips.setOnSelectionChanged(this::markDirty);
		for (JCheckBox cb : goodPointChecks)
			cb.addActionListener(e -> markDirty());
		for (JCheckBox cb : factorChecks)
			cb.addActionListener(e -> markDirty());
		for (JCheckBox cb : nextAdjustChecks)
			cb.addActionListener(e -> markDirty());
		owner.addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentMoved(java.awt.event.ComponentEvent e) {
				if (drawer != null && drawer.isVisible())
					drawer.openAtRightOf(owner);
			}

			@Override
			public void componentResized(java.awt.event.ComponentEvent e) {
				if (drawer != null && drawer.isVisible())
					drawer.openAtRightOf(owner);
			}
		});
	}

	private void toggleLinkFocusVisibility() {
		boolean hasLink = !linkField.getEffectiveText().trim().isEmpty();
		setLinkFocusFieldShown(hasLink);
	}

	private void refreshRetryConditionTitle() {
		String v = nextPlanChips.getSelectedText();
		if (v == null)
			v = "";
		if (v.contains("보완"))
			retryConditionTitle.setText("보완해 보고 싶은 한 가지가 있다면? (선택)");
		else if (v.contains("고민") || v.contains("쉬어"))
			retryConditionTitle.setText("다시 시작하려면 어떤 조건이 있으면 좋을까요? (선택)");
		else
			retryConditionTitle.setText("다시 시작을 위한 한 줄 메모 (선택)");
	}

	private void rebuildWizardFlow() {
		stepKeys.clear();
		stepKeys.add("meta");
		stepKeys.add("goal");
		stepKeys.add("mood");
		String mood = moodChips.getSelectedText();
		if (mood != null && (mood.contains("만족") || mood.contains("괜찮")))
			stepKeys.add("good");
		else if (mood != null && mood.contains("아쉬")) {
			stepKeys.add("pain");
			stepKeys.add("factors");
		}
		stepKeys.add("process");
		stepKeys.add("plangap");
		String planGap = planGapChips.getSelectedText();
		if (planGap != null && (planGap.contains("일부") || planGap.contains("많이")))
			stepKeys.add("plangap_detail");
		stepKeys.add("learning");
		stepKeys.add("next_adjust");
		stepKeys.add("next_plan");
		String nextPlan = nextPlanChips.getSelectedText();
		if (nextPlan != null && !nextPlan.isBlank() && !nextPlan.contains("바로"))
			stepKeys.add("retry_condition");
		stepKeys.add("summary");
	}

	private void showStep(int idx) {
		if (idx < 0)
			idx = 0;
		if (idx >= stepKeys.size())
			idx = stepKeys.size() - 1;
		stepIndex = idx;
		wizardLayout.show(wizardCards, stepKeys.get(stepIndex));
		stepBackBtn.setEnabled(stepIndex > 0);
		boolean isLast = stepIndex == stepKeys.size() - 1;
		stepNextBtn.setText(isLast ? "저장하기" : "다음");
		submitBtn.setVisible(false);
		stepHint.setText((stepIndex + 1) + " / " + stepKeys.size());
		revalidate();
		repaint();
	}

	private void goPrevStep() {
		showStep(stepIndex - 1);
	}

	private void goNextStep() {
		String key = stepKeys.get(stepIndex);
		if (!validateStep(key))
			return;
		if ("mood".equals(key) || "plangap".equals(key) || "next_plan".equals(key)) {
			int oldIndex = stepIndex;
			rebuildWizardFlow();
			int newIndex = stepKeys.indexOf(key);
			if (newIndex < 0)
				newIndex = oldIndex;
			stepIndex = newIndex;
		}
		boolean isLast = stepIndex == stepKeys.size() - 1;
		if (isLast) {
			onSubmit();
			return;
		}
		showStep(stepIndex + 1);
	}

	private boolean validateStep(String key) {
		if ("meta".equals(key)) {
			String title = titleField.getEffectiveText().trim();
			if (title.isEmpty()) {
				JOptionPane.showMessageDialog(this, "제목을 입력해 주세요.");
				titleField.requestFocus();
				return false;
			}
			String field = resolveField();
			if (field == null || field.isBlank()) {
				JOptionPane.showMessageDialog(this, "분야를 선택해 주세요.");
				return false;
			}
			if ("기타".equals(fieldChips.getSelectedText()) && customField.getEffectiveText().trim().isEmpty()) {
				JOptionPane.showMessageDialog(this, "기타 분야를 입력해 주세요.");
				customField.requestFocus();
				return false;
			}
			String category = (String) categoryCombo.getSelectedItem();
			if (category == null || category.isBlank()) {
				JOptionPane.showMessageDialog(this, "카테고리를 선택해 주세요.");
				return false;
			}
			if (statusCombo == null || statusCombo.getSelectedItem() == null) {
				JOptionPane.showMessageDialog(this, "현재 상태를 선택해 주세요.");
				return false;
			}
			return true;
		}
		if ("goal".equals(key)) {
			if (goalArea.getEffectiveText().trim().isEmpty()) {
				JOptionPane.showMessageDialog(this, "기대했던 점을 한 줄만 적어도 좋아요.");
				goalArea.requestFocus();
				return false;
			}
			return true;
		}
		if ("mood".equals(key)) {
			if (moodChips.getSelectedText() == null) {
				JOptionPane.showMessageDialog(this, "진행 과정 느낌을 선택해 주세요.");
				return false;
			}
			return true;
		}
		if ("process".equals(key)) {
			if (processArea.getEffectiveText().trim().isEmpty()) {
				JOptionPane.showMessageDialog(this, "진행 과정을 간단히 적어 주세요.");
				processArea.requestFocus();
				return false;
			}
			return true;
		}
		if ("plangap".equals(key)) {
			if (planGapChips.getSelectedText() == null) {
				JOptionPane.showMessageDialog(this, "계획과의 차이를 선택해 주세요.");
				return false;
			}
			return true;
		}
		if ("plangap_detail".equals(key)) {
			String gap = planGapChips.getSelectedText();
			if (gap != null && (gap.contains("일부") || gap.contains("많이"))
					&& planGapArea.getEffectiveText().trim().isEmpty()) {
				JOptionPane.showMessageDialog(this, "어떤 부분이 달라졌는지 한 줄만 적어도 좋아요.");
				planGapArea.requestFocus();
				return false;
			}
			return true;
		}
		if ("next_plan".equals(key)) {
			if (nextPlanChips.getSelectedText() == null) {
				JOptionPane.showMessageDialog(this, "다음 시도 계획을 선택해 주세요.");
				return false;
			}
			refreshRetryConditionTitle();
			return true;
		}
		return true;
	}

	private void attachDirtyListener(JTextComponent c) {
		c.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				markDirty();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				markDirty();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				markDirty();
			}
		});
	}

	private void markDirty() {
		dirty = true;
	}

	private void doDraftSave(boolean showToast) {
		Draft d = snapshotFromWizard(true);
		DraftStore.upsert(d);
		dirty = false;
		if (drawer != null && drawer.isVisible())
			drawer.refresh();
		if (showToast)
			JOptionPane.showMessageDialog(this, "임시 저장 완료!");
	}

	private void openDrawer(JFrame owner) {
		if (drawer == null)
			drawer = new DraftDrawerDialog(owner, this::loadDraftIntoForm, id -> DraftStore.delete(id));
		drawer.refresh();
		drawer.openAtRightOf(owner);
	}

	private void onSubmit() {
		Draft d = snapshotFromWizard(false);
		d.isDraft = false;
		DraftStore.upsert(d);
		dirty = false;
		JOptionPane.showMessageDialog(this, editMode ? "수정 완료!" : "저장 완료!");
		LogPost saved = toLogPost(d);
		if (editMode) {
			editMode = false;
			if (onEditSaved != null)
				onEditSaved.accept(saved);
			return;
		}
		if (onRegistered != null)
			onRegistered.run();
	}

	private Draft snapshotFromWizard(boolean forDraft) {
		Draft d = current;
		d.title = titleField.getEffectiveText();
		d.field = resolveField();
		d.category = (String) categoryCombo.getSelectedItem();
		d.status = statusCombo == null ? LogStatus.IN_PROGRESS : (LogStatus) statusCombo.getSelectedItem();
		d.isPublic = publicBtn.isSelected();
		d.linkUrl = linkField.getEffectiveText();
		d.linkPoint = linkFocusField.getEffectiveText();
		d.goalText = goalArea.getEffectiveText();
		d.mood = stripEmoji(moodChips.getSelectedText());
		d.goodPoints = readChecked(goodPointChecks, "기타");
		d.goodOther = goodOtherField.getEffectiveText();
		d.painPoint = painArea.getEffectiveText();
		d.influenceFactors = readChecked(factorChecks, "기타");
		d.influenceOther = factorOtherField.getEffectiveText();
		d.processText = processArea.getEffectiveText();
		d.planGapLevel = stripEmoji(planGapChips.getSelectedText());
		d.planGapDetail = planGapArea.getEffectiveText();
		d.learningText = learningArea.getEffectiveText();
		d.nextAdjustPoints = readChecked(nextAdjustChecks, "기타");
		d.nextAdjustOther = nextAdjustOtherField.getEffectiveText();
		d.nextPlan = stripEmoji(nextPlanChips.getSelectedText());
		d.retryCondition = retryConditionArea.getEffectiveText();
		d.workText = d.processText;
		d.reasonOther = d.painPoint;
		d.reasons = new ArrayList<>(d.influenceFactors);
		if (isCheckSelected(factorChecks, "기타"))
			d.reasons.add("기타");
		d.learnOneLine = d.learningText;
		d.retryOne = d.retryCondition;
		d.isDraft = forDraft;
		d.updatedAt = LocalDateTime.now();
		return d;
	}

	private static List<String> readChecked(JCheckBox[] checks, String exceptText) {
		List<String> res = new ArrayList<>();
		for (JCheckBox cb : checks)
			if (cb.isSelected() && (exceptText == null || !exceptText.equals(cb.getText())))
				res.add(cb.getText());
		return res;
	}

	private static boolean isCheckSelected(JCheckBox[] checks, String text) {
		for (JCheckBox cb : checks)
			if (Objects.equals(cb.getText(), text))
				return cb.isSelected();
		return false;
	}

	private LogPost toLogPost(Draft d) {
		return new LogPost(d.id, d.field, d.category, (d.status == null ? LogStatus.IN_PROGRESS : d.status), d.title,
				d.updatedAt.toLocalDate(), d.isPublic, d.workText, d.mood, d.reasonOther, d.learnOneLine, d.retryOne,
				d.linkUrl);
	}

	public void beginEdit(LogPost post, Consumer<LogPost> onSaved) {
		if (post == null)
			return;
		editMode = true;
		onEditSaved = onSaved;
		current.id = post.id;
		current.isDraft = false;
		titleField.setText(post.title);
		String field = (post.field == null ? "" : post.field);
		if (!field.isBlank()) {
			if (fieldChips.contains(field))
				fieldChips.selectByText(field);
			else {
				fieldChips.selectByText("기타");
				customField.setText(field);
			}
		}
		if (post.subCategory != null)
			categoryCombo.setSelectedItem(post.subCategory);
		if (post.isPublic)
			publicBtn.setSelected(true);
		else
			privateBtn.setSelected(true);
		if (statusCombo != null)
			statusCombo.setSelectedItem(post.status == null ? LogStatus.IN_PROGRESS : post.status);
		processArea.setText(post.whatIDid == null ? "" : post.whatIDid);
		moodChips.selectByText(post.feeling == null ? "" : post.feeling);
		painArea.setText(post.difficulty == null ? "" : post.difficulty);
		learningArea.setText(post.learning == null ? "" : post.learning);
		retryConditionArea.setText(post.retryPlan == null ? "" : post.retryPlan);
		linkField.setText(post.link == null ? "" : post.link);
		toggleLinkFocusVisibility();
		rebuildWizardFlow();
		showStep(0);
		dirty = false;
	}

	private void loadDraftIntoForm(Draft d) {
		if (d == null)
			return;
		current = d;
		titleField.setTextOrPlaceholder(d.title);
		String field = (d.field == null) ? "" : d.field;
		if (field.equals("영상") || field.equals("이미지") || field.equals("글") || field.equals("음악")) {
			fieldChips.selectByText(field);
			customField.setVisible(false);
			customField.setTextOrPlaceholder("");
		} else {
			fieldChips.selectByText("기타");
			customField.setVisible(true);
			customField.setTextOrPlaceholder(field.equals("기타") ? "" : field);
		}
		if (d.category != null)
			categoryCombo.setSelectedItem(d.category);
		if (d.isPublic)
			publicBtn.setSelected(true);
		else
			privateBtn.setSelected(true);
		if (statusCombo != null)
			statusCombo.setSelectedItem(d.status != null ? d.status : LogStatus.IN_PROGRESS);
		linkField.setTextOrPlaceholder(d.linkUrl);
		linkFocusField.setTextOrPlaceholder(d.linkPoint);
		toggleLinkFocusVisibility();
		goalArea.setTextOrPlaceholder(d.goalText);
		if (d.mood != null)
			moodChips.selectByText(d.mood);
		else
			moodChips.clearSelection();
		setChecks(goodPointChecks, d.goodPoints);
		goodOtherField.setEnabled(isCheckSelected(goodPointChecks, "기타"));
		goodOtherField.setTextOrPlaceholder(d.goodOther);
		painArea.setTextOrPlaceholder(d.painPoint);
		setChecks(factorChecks, d.influenceFactors);
		factorOtherField.setEnabled(isCheckSelected(factorChecks, "기타"));
		factorOtherField.setTextOrPlaceholder(d.influenceOther);
		processArea.setTextOrPlaceholder(d.processText);
		if (d.planGapLevel != null)
			planGapChips.selectByText(d.planGapLevel);
		planGapArea.setTextOrPlaceholder(d.planGapDetail);
		learningArea.setTextOrPlaceholder(d.learningText);
		setChecks(nextAdjustChecks, d.nextAdjustPoints);
		nextAdjustOtherField.setEnabled(isCheckSelected(nextAdjustChecks, "기타"));
		nextAdjustOtherField.setTextOrPlaceholder(d.nextAdjustOther);
		if (d.nextPlan != null)
			nextPlanChips.selectByText(d.nextPlan);
		retryConditionArea.setTextOrPlaceholder(d.retryCondition);
		refreshRetryConditionTitle();
		rebuildWizardFlow();
		showStep(0);
		dirty = false;
		revalidate();
		repaint();
	}

	private static void setChecks(JCheckBox[] checks, List<String> selectedTexts) {
		for (JCheckBox cb : checks)
			cb.setSelected(false);
		if (selectedTexts == null)
			return;
		for (String t : selectedTexts)
			for (JCheckBox cb : checks)
				if (Objects.equals(cb.getText(), t))
					cb.setSelected(true);
	}

	private String resolveField() {
		String selected = fieldChips.getSelectedText();
		if (selected == null)
			return null;
		if ("기타".equals(selected)) {
			String v = customField.getEffectiveText().trim();
			return v.isEmpty() ? "기타" : v;
		}
		return selected;
	}

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
				BorderFactory.createLineBorder(new Color(235, 235, 242), 1, true), new EmptyBorder(8, 10, 8, 10)));
		tf.setBackground(new Color(250, 250, 252));
	}

	private JPanel card(PanelBuilder builder) {
		JPanel card = new JPanel() {
			@Override
			public void addNotify() {
				super.addNotify();
				SwingUtilities.invokeLater(() -> {
					int h = getPreferredSize().height;
					if (h > 0)
						setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
				});
			}
		};
		card.setBackground(Color.WHITE);
		card.setAlignmentX(Component.LEFT_ALIGNMENT);
		card.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(230, 230, 235), 1, true), new EmptyBorder(14, 14, 14, 14)));
		builder.build(card);
		card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
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

	private static class TrackWidthPanel extends JPanel implements Scrollable {
		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 16;
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 80;
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			return false;
		}
	}

	private interface PanelBuilder {
		void build(JPanel panel);
	}

	static class GlyphIcon implements Icon {
		private final String glyph;
		private final Font font;
		private final Color color;
		private final int w, h;

		GlyphIcon(int materialCodePoint, float size, Color color) {
			this.glyph = new String(Character.toChars(materialCodePoint));
			this.font = FontKit.materialIcon(size);
			this.color = color;
			this.w = (int) (size + 6);
			this.h = (int) (size + 6);
		}

		@Override
		public int getIconWidth() {
			return w;
		}

		@Override
		public int getIconHeight() {
			return h;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setFont(font);
			g2.setColor(color);
			FontMetrics fm = g2.getFontMetrics();
			g2.drawString(glyph, x + 2, y + ((h - fm.getHeight()) / 2) + fm.getAscent());
			g2.dispose();
		}
	}

	static class PlaceholderTextField extends JTextField {
		private final String placeholder;
		private boolean showingPlaceholder = true;

		PlaceholderTextField(String placeholder) {
			this.placeholder = placeholder;
			setText(placeholder);
			setForeground(new Color(160, 160, 170));
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
					if (getText().trim().isEmpty())
						resetToPlaceholder();
				}
			});
		}

		String getEffectiveText() {
			return showingPlaceholder ? "" : getText();
		}

		void resetToPlaceholder() {
			showingPlaceholder = true;
			setText(placeholder);
			setForeground(new Color(160, 160, 170));
		}

		void setTextOrPlaceholder(String v) {
			if (v == null || v.trim().isEmpty())
				resetToPlaceholder();
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
			setForeground(new Color(160, 160, 170));
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
					if (getText().trim().isEmpty())
						resetToPlaceholder();
				}
			});
		}

		String getEffectiveText() {
			return showingPlaceholder ? "" : getText();
		}

		void resetToPlaceholder() {
			showingPlaceholder = true;
			setText(placeholder);
			setForeground(new Color(160, 160, 170));
		}

		void setTextOrPlaceholder(String v) {
			if (v == null || v.trim().isEmpty())
				resetToPlaceholder();
			else {
				showingPlaceholder = false;
				setText(v);
				setForeground(UITheme.TEXT);
			}
		}
	}

	static class ChipGroup extends JPanel {
		private final ButtonGroup group = new ButtonGroup();
		private final List<ChipToggle> chips = new ArrayList<>();
		private Runnable onSelectionChanged;

		static class Item {
			final String label;
			final Integer iconCodePoint;

			Item(String label, Integer iconCodePoint) {
				this.label = label;
				this.iconCodePoint = iconCodePoint;
			}
		}

		ChipGroup(Item[] items) {
			setOpaque(false);
			setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
			for (Item it : items) {
				ChipToggle t = new ChipToggle(it.label, it.iconCodePoint);
				group.add(t);
				chips.add(t);
				add(t);
				t.addActionListener(e -> {
					if (onSelectionChanged != null)
						onSelectionChanged.run();
				});
			}
		}

		void setOnSelectionChanged(Runnable r) {
			this.onSelectionChanged = r;
		}

		String getSelectedText() {
			for (ChipToggle c : chips)
				if (c.isSelected())
					return c.getText();
			return null;
		}

		void clearSelection() {
			group.clearSelection();
			repaint();
		}

		boolean contains(String text) {
			String target = normalize(text);
			for (ChipToggle c : chips)
				if (Objects.equals(normalize(c.getText()), target))
					return true;
			return false;
		}

		void selectByText(String text) {
			String target = normalize(text);
			for (ChipToggle c : chips) {
				if (Objects.equals(normalize(c.getText()), target)) {
					c.setSelected(true);
					repaint();
					return;
				}
			}
		}

		private String normalize(String s) {
			if (s == null)
				return "";
			return s.replaceAll("^[\\p{So}\\p{Sk}\\p{Cs}\\s]+", "").trim();
		}

		static class ChipToggle extends JToggleButton {
			private boolean hover = false;

			ChipToggle(String text, Integer iconCodePoint) {
				super(text);
				setFont(UITheme.BODY);
				setFocusPainted(false);
				setContentAreaFilled(false);
				setBorder(new EmptyBorder(8, 12, 8, 12));
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				setForeground(UITheme.TEXT);
				setOpaque(false);
				if (iconCodePoint != null) {
					setIcon(new GlyphIcon(iconCodePoint, 18f, new Color(110, 110, 125)));
					setIconTextGap(8);
				}
				addMouseListener(new java.awt.event.MouseAdapter() {
					public void mouseEntered(java.awt.event.MouseEvent e) {
						hover = true;
						repaint();
					}

					public void mouseExited(java.awt.event.MouseEvent e) {
						hover = false;
						repaint();
					}
				});
			}

			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				Color bg, bd;
				if (isSelected()) {
					bg = new Color(0xEAE6FF);
					bd = new Color(0xCFC9FF);
				} else if (hover) {
					bg = new Color(0xF3F1FF);
					bd = new Color(235, 235, 242);
				} else {
					bg = new Color(250, 250, 252);
					bd = new Color(235, 235, 242);
				}
				g2.setColor(bg);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
				g2.setColor(bd);
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
				super.paintComponent(g2);
				g2.dispose();
			}
		}
	}

	static class CollapsiblePanel extends JPanel {
		private final JComponent content;
		private final JButton toggle;
		private boolean expanded = false;
		private final Icon expandIcon = new GlyphIcon(0xE5CF, 18f, new Color(110, 110, 125));
		private final Icon collapseIcon = new GlyphIcon(0xE5CE, 18f, new Color(110, 110, 125));

		CollapsiblePanel(JComponent content, String expandText, String collapseText) {
			super(new BorderLayout());
			setOpaque(false);
			this.content = content;
			toggle = new JButton(expandText);
			toggle.setFont(UITheme.BODY_MED);
			toggle.setForeground(new Color(110, 110, 125));
			toggle.setContentAreaFilled(false);
			toggle.setBorder(new EmptyBorder(6, 8, 6, 8));
			toggle.setFocusPainted(false);
			toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			toggle.setIcon(expandIcon);
			toggle.setIconTextGap(6);
			JPanel head = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			head.setOpaque(false);
			head.add(toggle);
			add(head, BorderLayout.NORTH);
			add(content, BorderLayout.CENTER);
			content.setVisible(false);
			toggle.addActionListener(e -> {
				expanded = !expanded;
				content.setVisible(expanded);
				toggle.setText(expanded ? collapseText : expandText);
				toggle.setIcon(expanded ? collapseIcon : expandIcon);
				revalidate();
				repaint();
			});
		}

		void collapse() {
			expanded = false;
			content.setVisible(false);
			toggle.setText("더 보기");
			toggle.setIcon(expandIcon);
		}
	}
}
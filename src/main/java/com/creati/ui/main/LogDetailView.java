package com.creati.ui.main;

import com.creati.model.LogPost;
import com.creati.model.LogStatus;
import com.creati.model.User;
import com.creati.ui.components.Chip;
import com.creati.ui.components.RoundedButton;
import com.creati.util.FontKit;
import com.creati.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// [COLLAB] NOTE: Optional sections should be hidden when empty.

public class LogDetailView extends JPanel {

	private static final int SECTION_BODY_INDENT = 8;

	private static final String DUMMY_TEXT = "아직 작성되지 않았어요. 다음 기록에서 채워볼까요?";
	private static final String DUMMY_PLAN = "다음에는 어떤 방식으로 시도해볼까요?";

	private final Runnable onBack;
	private LogPost boundPost;
	private final Runnable onEdit;
	private final Runnable onDelete;

	private String postAuthor = "나"; // DB(TODO): 원글 작성자 닉네임/ID

	private final JLabel titleLabel = new JLabel();
	private final JLabel metaLabel = new JLabel();
	private final JLabel viewsCountLabel = new JLabel("0");
	private final JLabel reactsCountLabel = new JLabel("0");
	private final JLabel commentsCountLabel = new JLabel("0");
	private final Chip fieldChip = new Chip();
	private final Chip categoryChip = new Chip();
	private final Chip statusChip = new Chip();

	private final RoundedButton backBtn = new RoundedButton("뒤로가기");
	private final RoundedButton editBtn = new RoundedButton("수정하기");
	private final RoundedButton deleteBtn = new RoundedButton("삭제하기");

	private JScrollPane scroll;

	private JPanel contentCard;
	private JPanel socialCard;
	private JPanel secExpectation;
	private JPanel secResult;
	private JPanel secFactors;
	private JPanel secProcess;
	private JPanel secPlanGap;
	private JPanel secLearning;
	private JPanel secGrowth;
	private JPanel secLink;
	private JPanel secVideo;
	private JPanel secSocial;

	private JComponent divAfterExpectation;
	private JComponent divAfterResult;
	private JComponent divAfterFactors;
	private JComponent divAfterProcess;
	private JComponent divAfterPlanGap;
	private JComponent divAfterLearning;
	private JComponent divAfterGrowth;
	private JComponent divAfterLink;
	private JComponent divAfterVideo;

	private JTextArea expectationArea;

	private Chip moodChip;
	private JPanel goodChipsWrap;
	private JTextArea painArea;

	private JPanel factorsChipsWrap;

	private JTextArea processArea;

	private Chip planGapChip;
	private JPanel planGapDetailWrap;
	private JTextArea planGapDetailArea;

	private JTextArea learningArea;

	private JPanel nextAdjustWrap;
	private JLabel nextAdjustFallback;
	private JLabel nextPlanLine;
	private JTextArea retryConditionArea;

	private final JLabel linkLabel = new JLabel();
	private JLabel linkPointLabel;

	private JPanel reactionRow;
	private JPanel commentsWrap;
	private JTextField commentField;
	private RoundedButton commentSubmitBtn;

	private JLabel empathyMini;
	private JLabel cheerMini;
	private JLabel praiseMini;
	private JLabel comfortMini;
	private JLabel retryMini;

	private JLabel commentCountInline;

	public LogDetailView(Runnable onBack, Runnable onEdit) {
		this(onBack, onEdit, null);
	}

	public LogDetailView(Runnable onBack, Runnable onEdit, Runnable onDelete) {
		this.onBack = (onBack == null) ? () -> {
		} : onBack;
		this.onEdit = (onEdit == null) ? () -> JOptionPane.showMessageDialog(this, "수정 기능은 준비 중이에요.") : onEdit;
		this.onDelete = (onDelete == null) ? this::confirmDelete : onDelete;

		UITheme.ensureInit();
		FontKit.init();

		configureDetailChip(fieldChip);
		configureDetailChip(categoryChip);
		configureDetailChip(statusChip);

		initLinkLabel();

		setOpaque(false);
		setLayout(new BorderLayout());
		add(buildTopBar(), BorderLayout.NORTH);
		add(buildBody(), BorderLayout.CENTER);
	}

	private static void configureDetailChip(Chip c) {
		if (c == null)
			return;
		c.setFont(FontKit.medium(12.5f));
		c.setSizing(12, 5, 26);
	}

	private static String mi(int codePointHex) {
		return new String(Character.toChars(codePointHex));
	}

	private void initLinkLabel() {
		linkLabel.setFont(FontKit.regular(14f));
		linkLabel.setForeground(UITheme.ACCENT_BLUE);
		linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		linkLabel.setAlignmentX(LEFT_ALIGNMENT);
		linkLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String url = stripHtml(linkLabel.getText());
				if (url.isEmpty())
					return;
				openLink(url);
			}
		});
	}

	private JComponent buildTopBar() {
		JPanel top = new JPanel(new BorderLayout());
		top.setOpaque(false);
		top.setBorder(new EmptyBorder(8, 20, 8, 20));

		styleTopButton(backBtn, UITheme.WHITE, UITheme.DARK_TEXT, false);
		backBtn.addActionListener(e -> onBack.run());

		JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		left.setOpaque(false);
		left.setBorder(new EmptyBorder(0, 10, 0, 0));
		left.add(backBtn);

		styleTopButton(editBtn, UITheme.WHITE, UITheme.DARK_TEXT, false);
		styleTopButton(deleteBtn, UITheme.DANGER_BG, UITheme.ERROR_DARK, false);

		editBtn.addActionListener(e -> onEdit.run());
		deleteBtn.addActionListener(e -> onDelete.run());

		JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		right.setOpaque(false);
		right.setBorder(new EmptyBorder(0, 0, 0, 25));
		right.add(editBtn);
		right.add(deleteBtn);

		JPanel wrap = new JPanel(new BorderLayout());
		wrap.setOpaque(false);
		wrap.add(left, BorderLayout.WEST);
		wrap.add(right, BorderLayout.EAST);

		JPanel divider = new JPanel();
		divider.setOpaque(false);
		divider.setBorder(new MatteBorder(0, 0, 1, 0, UITheme.HOVER_BG_2));

		JPanel out = new JPanel(new BorderLayout());
		out.setOpaque(false);
		out.add(wrap, BorderLayout.CENTER);
		out.add(divider, BorderLayout.SOUTH);
		return out;
	}

	private JComponent buildBody() {
		JPanel page = new JPanel();
		page.setOpaque(false);
		page.setBorder(new EmptyBorder(10, 20, 16, 20));
		page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));

		JComponent titleCard = buildTitleCard();
		titleCard.setAlignmentX(LEFT_ALIGNMENT);
		page.add(titleCard);
		page.add(Box.createVerticalStrut(10));

		contentCard = buildContentCard();
		contentCard.setAlignmentX(LEFT_ALIGNMENT);
		page.add(contentCard);

		page.add(Box.createVerticalStrut(10));

		socialCard = buildSocialCard();
		socialCard.setAlignmentX(LEFT_ALIGNMENT);
		page.add(socialCard);

		scroll = new JScrollPane(page);
		scroll.setBorder(null);
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		return scroll;
	}

	private JComponent buildTitleCard() {
		JPanel card = cardBase();
		card.setLayout(new BorderLayout());
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UITheme.DIVIDER, 1),
				new EmptyBorder(18, 18, 18, 18)));

		titleLabel.setFont(FontKit.extraBold(26f));
		titleLabel.setForeground(UITheme.TEXT);
		titleLabel.setAlignmentX(LEFT_ALIGNMENT);

		metaLabel.setFont(FontKit.regular(13f));
		metaLabel.setForeground(UITheme.TEXT_DISABLED);
		metaLabel.setAlignmentX(LEFT_ALIGNMENT);

		fieldChip.setFont(FontKit.medium(12.5f));
		categoryChip.setFont(FontKit.medium(12.5f));
		statusChip.setFont(FontKit.medium(12.5f));

		JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		chips.setOpaque(false);
		chips.setAlignmentX(LEFT_ALIGNMENT);
		chips.add(fieldChip);
		chips.add(categoryChip);
		chips.add(statusChip);

		JPanel metaRow = new JPanel(new BorderLayout());
		metaRow.setOpaque(false);
		metaRow.setAlignmentX(LEFT_ALIGNMENT);
		metaRow.add(metaLabel, BorderLayout.WEST);
		metaRow.add(buildMetaStatsRight(), BorderLayout.EAST);

		JPanel head = new JPanel();
		head.setOpaque(false);
		head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
		head.setAlignmentX(LEFT_ALIGNMENT);
		head.add(chips);
		head.add(Box.createVerticalStrut(10));
		head.add(titleLabel);
		head.add(Box.createVerticalStrut(8));
		head.add(metaRow);

		card.add(head, BorderLayout.CENTER);
		return card;
	}

	private JComponent buildMetaStatsRight() {
		JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
		right.setOpaque(false);
		right.add(buildStatItem(0xE8F4, viewsCountLabel));
		right.add(buildStatItem(0xE87D, reactsCountLabel));
		right.add(buildStatItem(0xE0B9, commentsCountLabel));
		return right;
	}

	private JComponent buildStatItem(int iconCodePoint, JLabel valueLabel) {
		JLabel icon = new JLabel(new String(Character.toChars(iconCodePoint)));
		icon.setFont(FontKit.materialIcon(16f));
		icon.setForeground(UITheme.RGB_130_130_140);

		valueLabel.setFont(FontKit.medium(12.5f));
		valueLabel.setForeground(UITheme.RGB_130_130_140);

		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		p.setOpaque(false);
		p.add(icon);
		p.add(valueLabel);
		return p;
	}

	private JPanel buildContentCard() {
		JPanel card = cardBase();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UITheme.DIVIDER, 1),
				new EmptyBorder(18, 18, 18, 18)));

		expectationArea = makeArea();
		secExpectation = sectionBlock("기대했던 점", expectationArea);
		card.add(secExpectation);
		divAfterExpectation = sectionDivider();
		card.add(divAfterExpectation);

		JPanel resultBody = new JPanel();
		resultBody.setOpaque(false);
		resultBody.setLayout(new BoxLayout(resultBody, BoxLayout.Y_AXIS));

		moodChip = new Chip();
		configureDetailChip(moodChip);
		moodChip.setFont(FontKit.medium(12.5f));
		applyMoodChip(moodChip, "");

		JPanel moodWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		moodWrap.setOpaque(false);
		moodWrap.setAlignmentX(LEFT_ALIGNMENT);
		moodWrap.add(moodChip);

		resultBody.add(moodWrap);
		resultBody.add(Box.createVerticalStrut(12));

		goodChipsWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		goodChipsWrap.setOpaque(false);
		goodChipsWrap.setAlignmentX(LEFT_ALIGNMENT);
		resultBody.add(goodChipsWrap);

		painArea = makeArea();
		painArea.setBorder(new EmptyBorder(10, 0, 0, 0));
		painArea.setAlignmentX(LEFT_ALIGNMENT);
		resultBody.add(painArea);

		secResult = sectionBlock("결과 인식", resultBody);
		card.add(secResult);
		divAfterResult = sectionDivider();
		card.add(divAfterResult);

		factorsChipsWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		factorsChipsWrap.setOpaque(false);
		factorsChipsWrap.setAlignmentX(LEFT_ALIGNMENT);
		secFactors = sectionBlock("영향 요인", factorsChipsWrap);
		card.add(secFactors);
		divAfterFactors = sectionDivider();
		card.add(divAfterFactors);

		// [COLLAB] 행동 과정 (필수: 값 없어도 더미 표시)
		processArea = makeArea();
		secProcess = sectionBlock("행동 과정", processArea);
		card.add(secProcess);
		divAfterProcess = sectionDivider();
		card.add(divAfterProcess);

		JPanel planBody = new JPanel();
		planBody.setOpaque(false);
		planBody.setLayout(new BoxLayout(planBody, BoxLayout.Y_AXIS));

		planGapChip = new Chip();
		configureDetailChip(planGapChip);
		planGapChip.setFont(FontKit.medium(12.5f));
		planGapChip.setAlignmentX(LEFT_ALIGNMENT);
		planBody.add(planGapChip);

		planGapDetailWrap = new JPanel();
		planGapDetailWrap.setOpaque(false);
		planGapDetailWrap.setLayout(new BoxLayout(planGapDetailWrap, BoxLayout.Y_AXIS));
		planGapDetailWrap.setBorder(new EmptyBorder(14, 0, 0, 0));

		JLabel detailTitle = new JLabel("차이 내용");
		detailTitle.setFont(FontKit.semiBold(13.5f));
		detailTitle.setForeground(UITheme.DARK_TEXT);
		detailTitle.setAlignmentX(LEFT_ALIGNMENT);

		planGapDetailArea = makeArea();
		planGapDetailArea.setBorder(new EmptyBorder(8, 0, 0, 0));
		planGapDetailArea.setAlignmentX(LEFT_ALIGNMENT);

		planGapDetailWrap.add(detailTitle);
		planGapDetailWrap.add(planGapDetailArea);
		planBody.add(planGapDetailWrap);

		secPlanGap = sectionBlock("계획 차이", planBody);
		card.add(secPlanGap);
		divAfterPlanGap = sectionDivider();
		card.add(divAfterPlanGap);

		// [COLLAB] 회고 (필수: 값 없어도 더미 표시)
		learningArea = makeArea();
		secLearning = sectionBlock("회고", learningArea);
		card.add(secLearning);
		divAfterLearning = sectionDivider();
		card.add(divAfterLearning);

		// [COLLAB] 성장 설계 (필수: 최소 라인 더미 표시)
		JPanel growthBody = new JPanel();
		growthBody.setOpaque(false);
		growthBody.setLayout(new BoxLayout(growthBody, BoxLayout.Y_AXIS));

		JLabel a = new JLabel("다음에 조정해 보고 싶은 부분");
		a.setFont(FontKit.semiBold(13.5f));
		a.setForeground(UITheme.DARK_TEXT);
		a.setAlignmentX(LEFT_ALIGNMENT);
		growthBody.add(a);
		growthBody.add(Box.createVerticalStrut(10));

		nextAdjustWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		nextAdjustWrap.setOpaque(false);
		nextAdjustWrap.setAlignmentX(LEFT_ALIGNMENT);

		nextAdjustFallback = new JLabel("이번에는 현재 방식을 유지할래요.");
		nextAdjustFallback.setFont(FontKit.regular(14f));
		nextAdjustFallback.setForeground(UITheme.TEXT_DISABLED);
		nextAdjustFallback.setAlignmentX(LEFT_ALIGNMENT);

		growthBody.add(nextAdjustWrap);
		growthBody.add(nextAdjustFallback);
		growthBody.add(Box.createVerticalStrut(16));

		JLabel b = new JLabel("다음 시도 계획");
		b.setFont(FontKit.semiBold(13.5f));
		b.setForeground(UITheme.DARK_TEXT);
		b.setAlignmentX(LEFT_ALIGNMENT);
		growthBody.add(b);
		growthBody.add(Box.createVerticalStrut(8));

		nextPlanLine = new JLabel(" ");
		nextPlanLine.setFont(FontKit.regular(14f));
		nextPlanLine.setForeground(UITheme.DARK_TEXT);
		nextPlanLine.setAlignmentX(LEFT_ALIGNMENT);
		growthBody.add(nextPlanLine);

		retryConditionArea = makeArea();
		retryConditionArea.setBorder(new EmptyBorder(12, 0, 0, 0));
		retryConditionArea.setAlignmentX(LEFT_ALIGNMENT);
		growthBody.add(retryConditionArea);

		secGrowth = sectionBlock("성장 설계", growthBody);
		card.add(secGrowth);
		divAfterGrowth = sectionDivider();
		card.add(divAfterGrowth);

		JPanel linkBody = new JPanel();
		linkBody.setOpaque(false);
		linkBody.setLayout(new BoxLayout(linkBody, BoxLayout.Y_AXIS));

		linkBody.add(linkLabel);

		linkPointLabel = new JLabel();
		linkPointLabel.setFont(FontKit.regular(13f));
		linkPointLabel.setForeground(UITheme.TEXT_DISABLED);
		linkPointLabel.setBorder(new EmptyBorder(8, 0, 0, 0));
		linkPointLabel.setAlignmentX(LEFT_ALIGNMENT);
		linkBody.add(linkPointLabel);

		secLink = sectionBlock("참고 링크", linkBody);
		card.add(secLink);

		divAfterLink = sectionDivider();
		card.add(divAfterLink);

		// 영상 섹션 (YouTube 등 영상 URL이 있을 때 표시)
		JPanel videoBody = new JPanel(new BorderLayout());
		videoBody.setOpaque(false);
		videoBody.setAlignmentX(LEFT_ALIGNMENT);

		JPanel videoPlaceholder = new JPanel(new BorderLayout());
		videoPlaceholder.setOpaque(false);
		videoPlaceholder.setPreferredSize(new Dimension(0, 340));
		videoPlaceholder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));
		videoPlaceholder.setAlignmentX(LEFT_ALIGNMENT);
		videoBody.add(videoPlaceholder, BorderLayout.CENTER);

		secVideo = sectionBlock("영상", videoBody);
		secVideo.putClientProperty("videoPlaceholder", videoPlaceholder);
		card.add(secVideo);

		return card;
	}

	private JPanel buildSocialCard() {
		JPanel card = cardBase();
		card.setLayout(new BorderLayout());
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UITheme.DIVIDER, 1),
				new EmptyBorder(18, 18, 18, 18)));
		card.add(buildSocialBlock(), BorderLayout.CENTER);
		return card;
	}

	private JPanel buildSocialBlock() {
		JPanel wrap = new JPanel();
		wrap.setOpaque(false);
		wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
		wrap.setAlignmentX(LEFT_ALIGNMENT);

		JPanel reactHeadLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		reactHeadLine.setOpaque(false);
		reactHeadLine.setAlignmentX(LEFT_ALIGNMENT);

		JLabel reactTitle = new JLabel("공감");
		reactTitle.setFont(FontKit.semiBold(14f));
		reactTitle.setForeground(UITheme.DARK_TEXT);
		reactHeadLine.add(reactTitle);

		JLabel dot = new JLabel("·");
		dot.setFont(FontKit.regular(13f));
		dot.setForeground(UITheme.TEXT_DISABLED);
		reactHeadLine.add(dot);

		empathyMini = new JLabel("0");
		cheerMini = new JLabel("0");
		praiseMini = new JLabel("0");
		comfortMini = new JLabel("0");
		retryMini = new JLabel("0");

		reactHeadLine.add(buildMiniCount(0xE87D, empathyMini));
		reactHeadLine.add(buildMiniCount(0xE80E, cheerMini));
		reactHeadLine.add(buildMiniCount(0xEA23, praiseMini));
		reactHeadLine.add(buildMiniCount(0xE7F2, comfortMini));
		reactHeadLine.add(buildMiniCount(0xE5D5, retryMini));

		wrap.add(reactHeadLine);
		wrap.add(Box.createVerticalStrut(10));

		reactionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		reactionRow.setOpaque(false);
		reactionRow.setAlignmentX(LEFT_ALIGNMENT);
		buildReactionButtons();

		wrap.add(reactionRow);
		wrap.add(Box.createVerticalStrut(18));

		JPanel commentHeadLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		commentHeadLine.setOpaque(false);
		commentHeadLine.setAlignmentX(LEFT_ALIGNMENT);

		JLabel commentTitle = new JLabel("댓글");
		commentTitle.setFont(FontKit.semiBold(14f));
		commentTitle.setForeground(UITheme.DARK_TEXT);
		commentHeadLine.add(commentTitle);

		JLabel commentDot = new JLabel("·");
		commentDot.setFont(FontKit.regular(13f));
		commentDot.setForeground(UITheme.TEXT_DISABLED);
		commentHeadLine.add(commentDot);

		commentCountInline = new JLabel("0");
		commentHeadLine.add(buildMiniCount(0xE0B9, commentCountInline));

		wrap.add(commentHeadLine);
		wrap.add(Box.createVerticalStrut(10));

		JPanel inputRow = new JPanel(new BorderLayout(10, 0));
		inputRow.setOpaque(false);
		inputRow.setAlignmentX(LEFT_ALIGNMENT);

		commentField = new JTextField();
		commentField.setFont(FontKit.regular(14f));
		commentField.setPreferredSize(new Dimension(200, 36));
		commentField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UITheme.RGB_235_235_242), new EmptyBorder(8, 10, 8, 10)));

		commentSubmitBtn = new RoundedButton("등록");
		commentSubmitBtn.setFont(FontKit.medium(13.5f));
		commentSubmitBtn.setPreferredSize(new Dimension(90, 36));
		commentSubmitBtn.setBackground(UITheme.BTN_SECONDARY_BG);
		commentSubmitBtn.setForeground(UITheme.DARK_TEXT);
		commentSubmitBtn.addActionListener(e -> submitComment());

		inputRow.add(commentField, BorderLayout.CENTER);
		inputRow.add(commentSubmitBtn, BorderLayout.EAST);

		commentsWrap = new JPanel();
		commentsWrap.setOpaque(false);
		commentsWrap.setLayout(new BoxLayout(commentsWrap, BoxLayout.Y_AXIS));
		commentsWrap.setAlignmentX(LEFT_ALIGNMENT);

		wrap.add(inputRow);
		wrap.add(Box.createVerticalStrut(12));
		wrap.add(commentsWrap);

		return wrap;
	}

	private JComponent buildMiniCount(int iconCodePoint, JLabel countLabel) {
		JLabel icon = new JLabel(mi(iconCodePoint));
		icon.setFont(FontKit.materialIcon(14f));
		icon.setForeground(UITheme.TEXT_DISABLED);

		countLabel.setFont(FontKit.regular(13f));
		countLabel.setForeground(UITheme.TEXT_DISABLED);

		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		p.setOpaque(false);
		p.add(icon);
		p.add(countLabel);
		return p;
	}

	private void refreshReactionSummary() {

		if (boundPost == null || !boundPost.isPublic) {
			if (empathyMini != null)
				empathyMini.setText("0");
			if (cheerMini != null)
				cheerMini.setText("0");
			if (praiseMini != null)
				praiseMini.setText("0");
			if (comfortMini != null)
				comfortMini.setText("0");
			if (retryMini != null)
				retryMini.setText("0");
			if (commentCountInline != null)
				commentCountInline.setText("0");
			return;
		}

		empathyMini.setText(
				String.valueOf(Services.SOCIAL.getReactionCount(boundPost.id, SocialStore.ReactionType.EMPATHY)));
		cheerMini.setText(
				String.valueOf(Services.SOCIAL.getReactionCount(boundPost.id, SocialStore.ReactionType.CHEER)));
		praiseMini.setText(
				String.valueOf(Services.SOCIAL.getReactionCount(boundPost.id, SocialStore.ReactionType.PRAISE)));
		comfortMini.setText(
				String.valueOf(Services.SOCIAL.getReactionCount(boundPost.id, SocialStore.ReactionType.COMFORT)));
		retryMini.setText(
				String.valueOf(Services.SOCIAL.getReactionCount(boundPost.id, SocialStore.ReactionType.RETRY)));

		if (commentCountInline != null) {
			commentCountInline.setText(String.valueOf(Services.SOCIAL.getCommentCount(boundPost.id)));
		}
	}

	private void buildReactionButtons() {
		if (reactionRow == null)
			return;
		reactionRow.removeAll();

		reactionRow.add(makeReactionButton(0xE87D, "공감해요", SocialStore.ReactionType.EMPATHY));
		reactionRow.add(makeReactionButton(0xE80E, "힘내요", SocialStore.ReactionType.CHEER));
		reactionRow.add(makeReactionButton(0xEA23, "잘했어요", SocialStore.ReactionType.PRAISE));
		reactionRow.add(makeReactionButton(0xE7F2, "위로해요", SocialStore.ReactionType.COMFORT));
		reactionRow.add(makeReactionButton(0xE5D5, "다시 도전!", SocialStore.ReactionType.RETRY));

		refreshReactionSelection();
		refreshSocialCounts();
		refreshReactionSummary();
	}

	private void refreshReactionSelection() {
		if (reactionRow == null)
			return;

		if (boundPost == null) {
			for (Component c : reactionRow.getComponents()) {
				if (!(c instanceof JButton))
					continue;
				JButton b = (JButton) c;
				SocialStore.ReactionType t = (SocialStore.ReactionType) b.getClientProperty("type");
				applyGrayStyle(b, t, false);
			}
			reactionRow.revalidate();
			reactionRow.repaint();
			return;
		}

		String user = "나"; // TODO(DB)
		SocialStore.ReactionType selected = Services.SOCIAL.getMyReaction(boundPost.id);

		for (Component c : reactionRow.getComponents()) {
			if (!(c instanceof JButton))
				continue;
			JButton b = (JButton) c;
			SocialStore.ReactionType t = (SocialStore.ReactionType) b.getClientProperty("type");
			applyGrayStyle(b, t, selected == t);
		}

		reactionRow.revalidate();
		reactionRow.repaint();
	}

	private void applyGrayStyle(JButton b, SocialStore.ReactionType type, boolean selected) {
		b.putClientProperty("type", type);

		Color bg = UITheme.REACTION_BTN_BG;
		if (selected)
			bg = UITheme.REACTION_BTN_BG_SELECTED;

		b.setBackground(bg);
		b.setForeground(UITheme.DARK_TEXT);
	}

	private JButton makeReactionButton(int iconCodePoint, String label, SocialStore.ReactionType type) {
		String user = "나"; // TODO(DB)

		JLabel icon = new JLabel(mi(iconCodePoint));
		icon.setFont(FontKit.materialIcon(14f));
		icon.setForeground(UITheme.DARK_TEXT);

		JLabel text = new JLabel(label);
		text.setFont(FontKit.medium(13f));
		text.setForeground(UITheme.DARK_TEXT);

		RoundedButton b = new RoundedButton("");
		b.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));
		b.setBorder(new EmptyBorder(6, 12, 6, 12));
		b.setFocusPainted(false);
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		applyGrayStyle(b, type, false);

		b.add(icon);
		b.add(text);

		b.addActionListener(e -> {
			if (boundPost == null)
				return;

			Services.SOCIAL.toggleReaction(boundPost.id, type);

			refreshReactionSelection();
			refreshSocialCounts();
			refreshReactionSummary();
		});

		return b;
	}

	private void submitComment() {
		if (boundPost == null)
			return;

		String text = (commentField == null) ? "" : commentField.getText().trim();
		if (text.isBlank())
			return;

		Services.SOCIAL.addComment(boundPost.id, text);
		commentField.setText("");

		rebuildComments();
		refreshSocialCounts();
		refreshReactionSummary();
	}

	private void rebuildComments() {
		if (commentsWrap == null)
			return;
		commentsWrap.removeAll();

		if (boundPost == null) {
			JLabel empty = new JLabel("아직 댓글이 없어요.");
			empty.setFont(FontKit.regular(13.5f));
			empty.setForeground(UITheme.TEXT_DISABLED);
			empty.setAlignmentX(LEFT_ALIGNMENT);
			commentsWrap.add(empty);

			commentsWrap.revalidate();
			commentsWrap.repaint();
			return;
		}

		List<SocialStore.Comment> list = Services.SOCIAL.listComments(boundPost.id);
		if (list.isEmpty()) {
			JLabel empty = new JLabel("아직 댓글이 없어요.");
			empty.setFont(FontKit.regular(13.5f));
			empty.setForeground(UITheme.TEXT_DISABLED);
			empty.setAlignmentX(LEFT_ALIGNMENT);
			commentsWrap.add(empty);
		} else {
			DateTimeFormatter df = DateTimeFormatter.ofPattern("MM.dd HH:mm");

			User currentUser = AppState.get().getCurrentUser();

			for (SocialStore.Comment c : list) {
				boolean isMine = currentUser != null && c.author != null && c.author.equals(currentUser.getNickname());
				System.out.println(c.author);
				JPanel box = new JPanel();
				box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
				if (isMine) {
				    box.setBackground(UITheme.COMMENT_AUTHOR_BG);   // 내 댓글 스타일
				} else {
				    box.setBackground(UITheme.SURFACE_TINT);        // 기본 댓글
				}
				box.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(UITheme.RGB_235_235_242), new EmptyBorder(10, 12, 10, 12)));
				box.setAlignmentX(LEFT_ALIGNMENT);

				JPanel headRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
				headRow.setOpaque(false);
				headRow.setAlignmentX(LEFT_ALIGNMENT);

				JLabel head = new JLabel(c.author + " · " + (c.createdAt == null ? "" : c.createdAt.format(df)));
				head.setFont(FontKit.medium(12.5f));
				head.setForeground(UITheme.RGB_130_130_140);
				head.setAlignmentX(LEFT_ALIGNMENT);
				headRow.add(head);

				if (isMine) {
					Chip badge = new Chip();
					configureDetailChip(badge);
					badge.setFont(FontKit.medium(11.5f));
					badge.setChip("작성자", UITheme.COMMENT_AUTHOR_BADGE_BG, UITheme.COMMENT_AUTHOR_BADGE_FG);
					headRow.add(badge);
				}

				JLabel body = new JLabel("<html>" + escapeHtml(c.text) + "</html>");
				body.setFont(FontKit.regular(14f));
				body.setForeground(UITheme.DARK_TEXT);
				body.setAlignmentX(LEFT_ALIGNMENT);

				box.add(headRow);
				box.add(Box.createVerticalStrut(6));
				box.add(body);

				commentsWrap.add(box);
				commentsWrap.add(Box.createVerticalStrut(8));
			}
		}

		commentsWrap.revalidate();
		commentsWrap.repaint();
	}

	private void refreshSocialCounts() {
		if (boundPost == null) {
			viewsCountLabel.setText("0");
			reactsCountLabel.setText("0");
			commentsCountLabel.setText("0");
			return;
		}
		viewsCountLabel.setText(String.valueOf(Services.SOCIAL.getViews(boundPost.id)));
		reactsCountLabel.setText(String.valueOf(Services.SOCIAL.getTotalReactions(boundPost.id)));
		commentsCountLabel.setText(String.valueOf(Services.SOCIAL.getCommentCount(boundPost.id)));
	}

	// [COLLAB] 섹션 = 제목(굵게) + 내용(들여쓰기) 규칙 통일
	private JPanel sectionBlock(String title, JComponent body) {
		JPanel wrap = new JPanel();
		wrap.setOpaque(false);
		wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
		wrap.setAlignmentX(LEFT_ALIGNMENT);

		JLabel t = new JLabel(title);
		t.setFont(FontKit.semiBold(14f));
		t.setForeground(UITheme.DARK_TEXT);
		t.setAlignmentX(LEFT_ALIGNMENT);

		JPanel bodyWrap = new JPanel(new BorderLayout());
		bodyWrap.setOpaque(false);
		bodyWrap.setBorder(new EmptyBorder(0, SECTION_BODY_INDENT, 0, 0));
		bodyWrap.setAlignmentX(LEFT_ALIGNMENT);

		body.setAlignmentX(LEFT_ALIGNMENT);
		bodyWrap.add(body, BorderLayout.CENTER);

		wrap.add(t);
		wrap.add(Box.createVerticalStrut(10));
		wrap.add(bodyWrap);

		return wrap;
	}

	private JComponent sectionDivider() {
		JPanel wrap = new JPanel();
		wrap.setOpaque(false);
		wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
		wrap.setBorder(new EmptyBorder(12, 0, 12, 0));

		JComponent line = new JComponent() {
			@Override
			protected void paintComponent(Graphics g) {
				g.setColor(UITheme.NEUTRAL_075);
				g.drawLine(0, 0, getWidth(), 0);
			}

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(1, 1);
			}
		};
		line.setAlignmentX(LEFT_ALIGNMENT);

		wrap.add(line);
		return wrap;
	}

	private JPanel cardBase() {
		JPanel p = new JPanel();
		p.setBackground(UITheme.WHITE);
		p.setAlignmentX(LEFT_ALIGNMENT);
		p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50_000));
		return p;
	}

	private JTextArea makeArea() {
		JTextArea ta = new JTextArea();
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.setEditable(false);
		ta.setOpaque(false);
		ta.setFont(FontKit.regular(14f));
		ta.setForeground(UITheme.DARK_TEXT);
		return ta;
	}

	public LogPost getBoundPost() {
		return boundPost;
	}

	public void bind(LogPost post) {
		this.boundPost = post;
		if (post == null)
			return;

		// () 작성자 세팅: DB 붙이면 post의 author 필드로 교체
		postAuthor = resolvePostAuthor(post);

		Services.SOCIAL.addView(post.id);

		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy.MM.dd");
		String visibility = post.isPublic ? "공개" : "비공개";

		String field = safeText(post.field);
		String subCategory = safeText(post.subCategory);

		applyMetaChip(fieldChip, field);
		applyMetaChip(categoryChip, subCategory);
		applyStatusChip(statusChip, post.status);

		titleLabel.setText(safeText(post.title));
		metaLabel.setText(post.createdAt.format(fmt) + " · " + visibility);

		String goal = firstNonBlank(post.goalText, "");
		String mood = firstNonBlank(post.mood, post.feeling);
		List<String> goodPoints = (post.goodPoints == null) ? List.of() : post.goodPoints;
		String goodOther = safeText(post.goodOther);
		String pain = firstNonBlank(post.painPoint, post.difficulty);
		List<String> factors = (post.influenceFactors == null) ? List.of() : post.influenceFactors;
		String factorsOther = safeText(post.influenceOther);
		String process = firstNonBlank(post.processText, post.whatIDid);
		String planGap = safeText(post.planGapLevel);
		String planGapDetail = safeText(post.planGapDetail);
		String learning = firstNonBlank(post.learningText, post.learning);
		List<String> nextAdjust = (post.nextAdjustPoints == null) ? List.of() : post.nextAdjustPoints;
		String nextAdjustOther = safeText(post.nextAdjustOther);
		String nextPlan = safeText(post.nextPlan);
		String retryCondition = firstNonBlank(post.retryCondition, post.retryPlan);
		String linkUrl = firstNonBlank(post.linkUrl, post.link);
		String linkPoint = safeText(post.linkPoint);

		boolean showExpectation = !goal.isBlank();
		secExpectation.setVisible(showExpectation);
		expectationArea.setText(goal);

		boolean hasGood = !(goodPoints.isEmpty() && safeText(goodOther).isBlank());
		boolean showResult = (!mood.isBlank()) || (!pain.isBlank()) || hasGood;
		secResult.setVisible(showResult);

		if (showResult) {
			String moodText = stripEmoji(mood);
			if (moodText.isBlank()) {
				moodChip.setVisible(false);
			} else {
				moodChip.setVisible(true);
				applyMoodChip(moodChip, moodText);
			}

			boolean positive = isPositiveMood(mood);
			boolean negative = isNegativeMood(mood);

			rebuildChipWrap(goodChipsWrap, mergeListWithOther(goodPoints, goodOther), ChipStyle.GOOD);
			goodChipsWrap.setVisible(positive && goodChipsWrap.getComponentCount() > 0);

			painArea.setText(pain);
			painArea.setVisible(negative && !pain.isBlank());

			if (!positive && !negative) {
				goodChipsWrap.setVisible(goodChipsWrap.getComponentCount() > 0);
				painArea.setVisible(!pain.isBlank());
			}
		}

		List<String> factorMerged = mergeListWithOther(factors, factorsOther);
		rebuildChipWrap(factorsChipsWrap, factorMerged, ChipStyle.FACTOR);
		boolean showFactors = factorsChipsWrap.getComponentCount() > 0;
		secFactors.setVisible(showFactors);

		// [COLLAB] 행동 과정 (필수) ----
		processArea.setText(process.isBlank() ? DUMMY_TEXT : process);
		secProcess.setVisible(true);

		boolean showPlanGap = !planGap.isBlank();
		secPlanGap.setVisible(showPlanGap);
		if (showPlanGap) {
			applyGreyChip(planGapChip, stripEmoji(planGap));
			planGapDetailArea.setText(planGapDetail);
			planGapDetailWrap.setVisible(!planGapDetail.isBlank());
		}

		// [COLLAB] 회고 (필수) ----
		learningArea.setText(learning.isBlank() ? DUMMY_TEXT : learning);
		secLearning.setVisible(true);

		// [COLLAB] 성장 설계 (필수) ----
		secGrowth.setVisible(true);

		List<String> nextAdjustMerged = mergeListWithOther(nextAdjust, nextAdjustOther);
		rebuildChipWrap(nextAdjustWrap, nextAdjustMerged, ChipStyle.GOOD);
		boolean hasNextAdjust = nextAdjustWrap.getComponentCount() > 0;
		nextAdjustWrap.setVisible(hasNextAdjust);
		nextAdjustFallback.setVisible(!hasNextAdjust);

		String planLine = stripEmoji(nextPlan);
		if (planLine.isBlank())
			planLine = DUMMY_PLAN;
		nextPlanLine.setText(planLine);

		if (!retryCondition.isBlank()) {
			retryConditionArea.setText(retryCondition);
			retryConditionArea.setVisible(true);
		} else {
			retryConditionArea.setText("");
			retryConditionArea.setVisible(false);
		}

		String u = (linkUrl == null) ? "" : linkUrl.trim();
		boolean showLink = !u.isBlank();
		secLink.setVisible(showLink);

		if (showLink) {
			String normalized = normalizeUrl(u);
			linkLabel.setText("<html><u>" + escapeHtml(normalized) + "</u></html>");

			if (linkPoint.isBlank()) {
				linkPointLabel.setVisible(false);
				linkPointLabel.setText("");
			} else {
				linkPointLabel.setVisible(true);
				linkPointLabel.setText("확인 포인트: " + linkPoint);
			}
		}

		// 영상 섹션: YouTube URL인 경우 표시
		String videoEmbedUrl = extractYouTubeEmbedUrl(u.isBlank() ? firstNonBlank(post.linkUrl, post.link) : u);
		boolean showVideo = videoEmbedUrl != null;
		if (secVideo != null) {
			secVideo.setVisible(showVideo);
			if (showVideo) {
				JPanel placeholder = (JPanel) secVideo.getClientProperty("videoPlaceholder");
				if (placeholder != null) {
					placeholder.removeAll();
					placeholder.add(buildWebVideoPanel(videoEmbedUrl), BorderLayout.CENTER);
					placeholder.revalidate();
					placeholder.repaint();
				}
			}
		}

		if (socialCard != null)
			socialCard.setVisible(post.isPublic);
		boolean canSocial = post.isPublic;
		if (reactionRow != null)
			reactionRow.setVisible(canSocial);
		if (commentField != null)
			commentField.setEnabled(canSocial);
		if (commentSubmitBtn != null)
			commentSubmitBtn.setEnabled(canSocial);

		if (post.isPublic) {
			rebuildComments();
		} else {
			if (commentsWrap != null) {
				commentsWrap.removeAll();
				commentsWrap.revalidate();
				commentsWrap.repaint();
			}
		}

		updateDividers();

		refreshSocialCounts();
		refreshReactionSelection();
		refreshReactionSummary();

		scrollToTop();
		revalidate();
		repaint();
	}

	private void updateDividers() {
		boolean vExpectation = secExpectation.isVisible();
		boolean vResult = secResult.isVisible();
		boolean vFactors = secFactors.isVisible();
		boolean vProcess = secProcess.isVisible(); // [COLLAB] 필수
		boolean vPlanGap = secPlanGap.isVisible();
		boolean vLearning = secLearning.isVisible(); // [COLLAB] 필수
		boolean vGrowth = secGrowth.isVisible(); // [COLLAB] 필수
		boolean vLink = secLink.isVisible();
		boolean vVideo = (secVideo != null) && secVideo.isVisible();
		boolean vSocial = (secSocial != null) && secSocial.isVisible();

		divAfterExpectation.setVisible(
				vExpectation && (vResult || vFactors || vProcess || vPlanGap || vLearning || vGrowth || vLink || vVideo));
		divAfterResult.setVisible(vResult && (vFactors || vProcess || vPlanGap || vLearning || vGrowth || vLink || vVideo));
		divAfterFactors.setVisible(vFactors && (vProcess || vPlanGap || vLearning || vGrowth || vLink || vVideo));
		divAfterProcess.setVisible(vProcess && (vPlanGap || vLearning || vGrowth || vLink || vVideo));
		divAfterPlanGap.setVisible(vPlanGap && (vLearning || vGrowth || vLink || vVideo));
		divAfterLearning.setVisible(vLearning && (vGrowth || vLink || vVideo || vSocial));
		divAfterGrowth.setVisible(vGrowth && (vLink || vVideo || vSocial));
		if (divAfterLink != null)
			divAfterLink.setVisible(vLink && vVideo);
		if (divAfterVideo != null)
			divAfterVideo.setVisible(vVideo && vSocial);
	}

	private void rebuildChipWrap(JPanel wrap, List<String> values, ChipStyle style) {
		wrap.removeAll();
		if (values != null) {
			for (String v : values) {
				String text = safeText(v);
				if (text.isBlank())
					continue;
				Chip c = new Chip();
				configureDetailChip(c);
				c.setFont(FontKit.medium(12.5f));
				applyStyledChip(c, text, style);
				wrap.add(c);
			}
		}
		wrap.revalidate();
		wrap.repaint();
	}

	private enum ChipStyle {
		META, GOOD, FACTOR
	}

	private void applyStyledChip(Chip chip, String text, ChipStyle style) {
		if (style == ChipStyle.GOOD) {
			chip.setChip(text, UITheme.detailChipBg(UITheme.DetailChipStyle.GOOD),
					UITheme.detailChipFg(UITheme.DetailChipStyle.GOOD));
			return;
		}
		if (style == ChipStyle.FACTOR) {
			chip.setChip(text, UITheme.detailChipBg(UITheme.DetailChipStyle.FACTOR),
					UITheme.detailChipFg(UITheme.DetailChipStyle.FACTOR));
			return;
		}
		chip.setChip(text, UITheme.detailChipBg(UITheme.DetailChipStyle.META),
				UITheme.detailChipFg(UITheme.DetailChipStyle.META));
	}

	private void applyMoodChip(Chip chip, String text) {
		chip.setChip("진행 느낌 · " + safeText(text), UITheme.detailChipBg(UITheme.DetailChipStyle.INFO),
				UITheme.detailChipFg(UITheme.DetailChipStyle.INFO));
	}

	private void applyMetaChip(Chip chip, String text) {
		chip.setChip(safeText(text), UITheme.chipBg(UITheme.ChipStyle.NEUTRAL),
				UITheme.chipFg(UITheme.ChipStyle.NEUTRAL));
	}

	private void applyGreyChip(Chip chip, String text) {
		chip.setChip(text, UITheme.detailChipBg(UITheme.DetailChipStyle.NEUTRAL),
				UITheme.detailChipFg(UITheme.DetailChipStyle.NEUTRAL));
	}

	private void applyStatusChip(Chip chip, LogStatus status) {
		LogStatus st = (status == null) ? LogStatus.IN_PROGRESS : status;
		chip.setChip(st.label, UITheme.chipBg(st), UITheme.chipFg(st));
	}

	private boolean isPositiveMood(String mood) {
		String m = (mood == null) ? "" : mood;
		return m.contains("만족") || m.contains("괜찮") || m.contains("좋");
	}

	private boolean isNegativeMood(String mood) {
		String m = (mood == null) ? "" : mood;
		return m.contains("아쉬") || m.contains("힘들") || m.contains("별로");
	}

	private List<String> mergeListWithOther(List<String> list, String other) {
		List<String> out = new ArrayList<>();
		if (list != null) {
			for (String s : list) {
				String v = safeText(s);
				if (!v.isBlank())
					out.add(v);
			}
		}
		String o = safeText(other);
		if (!o.isBlank())
			out.add(o);
		return out;
	}

	private void scrollToTop() {
		if (scroll == null)
			return;
		SwingUtilities.invokeLater(() -> {
			JScrollBar bar = scroll.getVerticalScrollBar();
			if (bar != null)
				bar.setValue(0);
		});
	}

	private void openLink(String url) {
		String normalized = normalizeUrl(url);
		if (normalized.isEmpty())
			return;
		try {
			if (!Desktop.isDesktopSupported())
				return;
			Desktop.getDesktop().browse(new URI(normalized));
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "링크를 열 수 없어요.\n" + normalized);
		}
	}

	private String normalizeUrl(String url) {
		if (url == null)
			return "";
		String u = url.trim();
		if (u.isEmpty())
			return "";
		if (u.startsWith("http://") || u.startsWith("https://"))
			return u;
		return "https://" + u;
	}

	private String escapeHtml(String s) {
		if (s == null)
			return "";
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}

	private String stripHtml(String s) {
		if (s == null)
			return "";
		return s.replaceAll("<[^>]*>", "").trim();
	}

	private String stripEmoji(String s) {
		if (s == null)
			return "";
		return s.replaceAll("^[\\p{So}\\p{Sk}\\p{Cs}\\s]+", "").trim();
	}

	private String safeText(String s) {
		return (s == null) ? "" : s.trim();
	}

	private String firstNonBlank(String a, String b) {
		String x = safeText(a);
		if (!x.isBlank())
			return x;
		return safeText(b);
	}

	private void confirmDelete() {
		int r = JOptionPane.showConfirmDialog(this, "이 글을 삭제할까요?", "삭제 확인", JOptionPane.YES_NO_OPTION);
		if (r == JOptionPane.YES_OPTION) {
			JOptionPane.showMessageDialog(this, "삭제 기능은 DB 연결 후 적용할 수 있어요.");
		}
	}

	/**
	 * YouTube/YouTu.be URL에서 embed URL을 추출합니다.
	 * YouTube URL이 아니면 null을 반환합니다.
	 */
	private String extractYouTubeEmbedUrl(String url) {
		if (url == null || url.isBlank()) return null;
		String u = url.trim();

		// youtu.be/VIDEO_ID
		java.util.regex.Matcher m1 = java.util.regex.Pattern
				.compile("(?:https?://)?youtu\\.be/([A-Za-z0-9_\\-]{11})")
				.matcher(u);
		if (m1.find()) return "https://www.youtube.com/embed/" + m1.group(1) + "?rel=0";

		// youtube.com/watch?v=VIDEO_ID
		java.util.regex.Matcher m2 = java.util.regex.Pattern
				.compile("(?:https?://)?(?:www\\.)?youtube\\.com/watch\\?.*v=([A-Za-z0-9_\\-]{11})")
				.matcher(u);
		if (m2.find()) return "https://www.youtube.com/embed/" + m2.group(1) + "?rel=0";

		// youtube.com/embed/VIDEO_ID (이미 embed URL인 경우)
		java.util.regex.Matcher m3 = java.util.regex.Pattern
				.compile("(?:https?://)?(?:www\\.)?youtube\\.com/embed/([A-Za-z0-9_\\-]{11})")
				.matcher(u);
		if (m3.find()) return "https://www.youtube.com/embed/" + m3.group(1) + "?rel=0";

		return null;
	}

	/**
	 * YouTube 썸네일 + 클릭 시 브라우저 열기 패널 (순수 Swing).
	 */
	private JPanel buildWebVideoPanel(String embedUrl) {
		String videoId = extractVideoId(embedUrl);
		String watchUrl = "https://www.youtube.com/watch?v=" + videoId;
		String thumbUrl = "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";

		// 썸네일 커스텀 페인트 레이블
		JLabel thumbLabel = new JLabel() {
			private Image img = null;
			{
				new Thread(() -> {
					try {
						java.net.URL u = new java.net.URL(thumbUrl);
						java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
						conn.setConnectTimeout(4000);
						conn.setReadTimeout(6000);
						conn.setRequestProperty("User-Agent", "Mozilla/5.0");
						try (java.io.InputStream is = conn.getInputStream()) {
							Image raw = javax.imageio.ImageIO.read(is);
							if (raw != null) { img = raw; SwingUtilities.invokeLater(this::repaint); }
						}
					} catch (Exception ignored) {}
				}).start();
			}
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				int w = getWidth(), h = getHeight();
				if (img != null) {
					int iw = img.getWidth(null), ih = img.getHeight(null);
					double scale = Math.max((double) w / iw, (double) h / ih);
					int sw = (int)(iw * scale), sh = (int)(ih * scale);
					g2.drawImage(img, (w - sw) / 2, (h - sh) / 2, sw, sh, null);
					g2.setColor(new Color(0, 0, 0, 80));
					g2.fillRect(0, 0, w, h);
				} else {
					g2.setPaint(new java.awt.GradientPaint(0, 0, new Color(25, 22, 38), 0, h, new Color(12, 10, 20)));
					g2.fillRect(0, 0, w, h);
				}
				// 재생 버튼 원
				int cx = w / 2, cy = h / 2, r = 28;
				g2.setColor(new Color(255, 255, 255, 210));
				g2.fillOval(cx - r, cy - r, r * 2, r * 2);
				// 재생 삼각형
				int[] px = { cx - 9, cx - 9, cx + 16 };
				int[] py = { cy - 13, cy + 13, cy };
				g2.setColor(new Color(200, 0, 0));
				g2.fillPolygon(px, py, 3);
				// 하단 반투명 바
				g2.setColor(new Color(0, 0, 0, 150));
				g2.fillRect(0, h - 44, w, 44);
				// 하단 텍스트
				g2.setColor(new Color(220, 220, 220));
				g2.setFont(FontKit.medium(12.5f));
				g2.drawString("▶  YouTube에서 열기", 14, h - 16);
				g2.dispose();
			}
		};
		thumbLabel.setLayout(null);
		thumbLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		thumbLabel.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) { openLink(watchUrl); }
		});

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setOpaque(false);
		wrapper.setAlignmentX(LEFT_ALIGNMENT);
		wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
		wrapper.setPreferredSize(new Dimension(0, 280));
		wrapper.setBorder(BorderFactory.createLineBorder(UITheme.DIVIDER, 1));
		wrapper.add(thumbLabel, BorderLayout.CENTER);
		return wrapper;
	}

	/** embed/watch/youtu.be URL 에서 video ID(11자) 추출 */
	private String extractVideoId(String url) {
		if (url == null) return "";
		for (String pat : new String[]{
		        "/embed/([A-Za-z0-9_-]{11})",
		        "[?&]v=([A-Za-z0-9_-]{11})",
		        "youtu\\.be/([A-Za-z0-9_-]{11})"}) {
		    java.util.regex.Matcher m = java.util.regex.Pattern.compile(pat).matcher(url);
		    if (m.find()) return m.group(1);
		}
		return "";
	}

	private void styleTopButton(RoundedButton btn, Color bg, Color fg, boolean primary) {
		btn.setBackground(bg);
		btn.setForeground(fg);
		btn.setFont(FontKit.medium(13.5f));
		btn.setPreferredSize(new Dimension(primary ? 170 : 110, 36));
	}

	// () LogPost에서 작성자 필드가 있으면 잡아오기 (DB 붙으면 교체)
	private String resolvePostAuthor(LogPost post) {
		if (post == null)
			return "나";
		String[] candidates = { "authorNick", "authorId", "userNick", "nick", "author" };
		for (String f : candidates) {
			try {
				var field = post.getClass().getDeclaredField(f);
				field.setAccessible(true);
				Object v = field.get(post);
				if (v != null) {
					String s = v.toString().trim();
					if (!s.isBlank())
						return s;
				}
			} catch (Exception ignored) {
			}
		}
		return "나";
	}
}
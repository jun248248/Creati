package com.creati.ui.main;

import com.creati.model.LogPost;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class QuestionDetailView extends JPanel {

	
	private static final int SECTION_BODY_INDENT = 8;

	
	private final Runnable onBack;
	private final Runnable onDelete;
	private final Runnable onEdit; 

	private LogPost boundPost;

	private String postAuthor = ""; // DB(TODO): 원글 작성자 닉네임/ID

	
	private JLabel empathyMini;
	private JLabel cheerMini;
	private JLabel praiseMini;
	private JLabel comfortMini;
	private JLabel retryMini;

	
	private JLabel commentCountInline;

	
	private final JLabel titleLabel = new JLabel();
	private final JLabel metaLabel = new JLabel();
	private final JLabel viewsCountLabel = new JLabel("0");
	private final JLabel reactsCountLabel = new JLabel("0");
	private final JLabel commentsCountLabel = new JLabel("0");
	private final Chip fieldChip = new Chip();
	private final Chip categoryChip = new Chip();

	
	private final RoundedButton backBtn = new RoundedButton("뒤로가기");
	private final RoundedButton editBtn = new RoundedButton("수정하기");
	private final RoundedButton deleteBtn = new RoundedButton("삭제하기");

	
	private JScrollPane scroll;

	
	private JPanel contentCard;
	private JPanel socialCard;
	private JPanel secQuestion;
	private JPanel secLink;
	private JComponent divAfterQuestion;

	
	private JPanel reactionRow;
	private JPanel commentsWrap;
	private JTextField commentField;
	private RoundedButton commentSubmitBtn;

	private final JTextArea questionArea = makeArea();
	private final JLabel linkLabel = new JLabel();

	public QuestionDetailView(Runnable onBack, Runnable onEdit, Runnable onDelete) {
		UITheme.ensureInit();
		FontKit.init();

		this.onBack = (onBack == null) ? () -> {
		} : onBack;
		this.onEdit = onEdit; 
		this.onDelete = (onDelete == null) ? this::confirmDelete : onDelete;

		configureDetailChip(fieldChip);
		configureDetailChip(categoryChip);

		setOpaque(false);
		setLayout(new BorderLayout());

		initLinkLabel();

		add(buildTopBar(), BorderLayout.NORTH);
		add(buildBody(), BorderLayout.CENTER);
	}

	private static void configureDetailChip(Chip c) {
		if (c == null)
			return;
		c.setFont(FontKit.medium(12.5f));
		c.setSizing(12, 5, 26);
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

		
		JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		right.setOpaque(false);
		right.setBorder(new EmptyBorder(0, 0, 0, 25));

		styleTopButton(deleteBtn, UITheme.DANGER_BG, UITheme.ERROR_DARK, false);
		deleteBtn.addActionListener(e -> this.onDelete.run());
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

		
		Dimension tPref = titleCard.getPreferredSize();
		titleCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, tPref.height));

		
		contentCard = buildContentCard();
		contentCard.setAlignmentX(LEFT_ALIGNMENT);

		
		socialCard = buildSocialCard();
		socialCard.setAlignmentX(LEFT_ALIGNMENT);

		page.add(titleCard);
		page.add(Box.createVerticalStrut(10));
		page.add(contentCard);
		page.add(Box.createVerticalStrut(10));
		page.add(socialCard);

		page.add(Box.createVerticalGlue());

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
		card.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UITheme.DIVIDER, 1),
				new EmptyBorder(18, 18, 18, 18)));

		titleLabel.setFont(FontKit.extraBold(26f));
		titleLabel.setForeground(UITheme.TEXT);
		titleLabel.setAlignmentX(LEFT_ALIGNMENT);

		metaLabel.setFont(FontKit.regular(13f));
		metaLabel.setForeground(UITheme.TEXT_DISABLED);
		metaLabel.setAlignmentX(LEFT_ALIGNMENT);

		JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		chips.setOpaque(false);
		chips.setAlignmentX(LEFT_ALIGNMENT);

		chips.add(fieldChip);
		chips.add(categoryChip);

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
		card.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UITheme.DIVIDER, 1),
				new EmptyBorder(18, 18, 18, 18)));

		secQuestion = sectionBlock("질문 내용", questionArea);
		card.add(secQuestion);

		divAfterQuestion = sectionDivider();
		card.add(divAfterQuestion);

		secLink = sectionBlock("업로드 링크", linkLabel);
		card.add(secLink);

		secLink.setVisible(false);
		divAfterQuestion.setVisible(false);

		return card;
	}

	
	private JPanel buildSocialCard() {
		JPanel card = cardBase();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UITheme.DIVIDER, 1),
				new EmptyBorder(18, 18, 18, 18)));

		JPanel social = buildSocialBlock();
		social.setAlignmentX(LEFT_ALIGNMENT);
		card.add(social);
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
				BorderFactory.createLineBorder(UITheme.RGB_235_235_242),
				new EmptyBorder(8, 10, 8, 10)));

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
			if (empathyMini != null) empathyMini.setText("0");
			if (cheerMini != null) cheerMini.setText("0");
			if (praiseMini != null) praiseMini.setText("0");
			if (comfortMini != null) comfortMini.setText("0");
			if (retryMini != null) retryMini.setText("0");
			if (commentCountInline != null) commentCountInline.setText("0");
			return;
		}

		empathyMini.setText(String.valueOf(Services.SOCIAL.getReactionCount(boundPost.id, SocialStore.ReactionType.EMPATHY)));
		cheerMini.setText(String.valueOf(Services.SOCIAL.getReactionCount(boundPost.id, SocialStore.ReactionType.CHEER)));
		praiseMini.setText(String.valueOf(Services.SOCIAL.getReactionCount(boundPost.id, SocialStore.ReactionType.PRAISE)));
		comfortMini.setText(String.valueOf(Services.SOCIAL.getReactionCount(boundPost.id, SocialStore.ReactionType.COMFORT)));
		retryMini.setText(String.valueOf(Services.SOCIAL.getReactionCount(boundPost.id, SocialStore.ReactionType.RETRY)));

		
		if (commentCountInline != null) {
			commentCountInline.setText(String.valueOf(Services.SOCIAL.getCommentCount(boundPost.id)));
		}
	}

	private void buildReactionButtons() {
		if (reactionRow == null) return;
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
			if (boundPost == null) return;

			Services.SOCIAL.toggleReaction(boundPost.id, type);

			refreshReactionSelection();
			refreshSocialCounts();
			refreshReactionSummary();
		});

		return b;
	}

	private void refreshReactionSelection() {
		if (reactionRow == null) return;

		if (boundPost == null) {
			for (Component c : reactionRow.getComponents()) {
				if (!(c instanceof JButton)) continue;
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
			if (!(c instanceof JButton)) continue;
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
		if (selected) bg = UITheme.REACTION_BTN_BG_SELECTED;

		b.setBackground(bg);
		b.setForeground(UITheme.DARK_TEXT);
	}

	private void submitComment() {
		if (boundPost == null) return;
		String text = (commentField == null) ? "" : commentField.getText().trim();
		if (text.isBlank()) return;

		String author = "나"; // DB(TODO): current user nickname
		Services.SOCIAL.addComment(boundPost.id, text);
		commentField.setText("");

		rebuildComments();
		refreshSocialCounts();
		refreshReactionSummary();
	}

	private void rebuildComments() {
		if (commentsWrap == null) return;
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

		var list = Services.SOCIAL.listComments(boundPost.id);
		if (list.isEmpty()) {
			JLabel empty = new JLabel("아직 댓글이 없어요.");
			empty.setFont(FontKit.regular(13.5f));
			empty.setForeground(UITheme.TEXT_DISABLED);
			empty.setAlignmentX(LEFT_ALIGNMENT);
			commentsWrap.add(empty);
		} else {
			DateTimeFormatter df = DateTimeFormatter.ofPattern("MM.dd HH:mm");
			for (SocialStore.Comment c : list) {
				boolean isAuthor = (postAuthor != null && c.author != null && c.author.equals(postAuthor));

				JPanel box = new JPanel();
				box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
				box.setBackground(isAuthor ? UITheme.COMMENT_AUTHOR_BG : UITheme.SURFACE_TINT);
				box.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(UITheme.RGB_235_235_242),
						new EmptyBorder(10, 12, 10, 12)
				));
				box.setAlignmentX(LEFT_ALIGNMENT);

				JPanel headRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
				headRow.setOpaque(false);
				headRow.setAlignmentX(LEFT_ALIGNMENT);

				JLabel head = new JLabel(c.author + " · " + (c.createdAt == null ? "" : c.createdAt.format(df)));
				head.setFont(FontKit.medium(12.5f));
				head.setForeground(UITheme.RGB_130_130_140);
				head.setAlignmentX(LEFT_ALIGNMENT);
				headRow.add(head);

				if (isAuthor) {
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

	private static JTextArea makeArea() {
		JTextArea ta = new JTextArea();
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.setEditable(false);
		ta.setOpaque(false);
		ta.setFont(FontKit.regular(14f));
		ta.setForeground(UITheme.DARK_TEXT);
		return ta;
	}

	

	public void bind(LogPost post) {
		this.boundPost = post;
		if (post == null)
			return;

		postAuthor = resolvePostAuthor(post);

		
		Services.SOCIAL.addView(post.id);

		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy.MM.dd");

		
		String field = safeText(post.field);
		String category = safeText(post.subCategory);

		applyMetaChip(fieldChip, field.isBlank() ? "기타" : field);
		applyMetaChip(categoryChip, category.isBlank() ? "기타" : category);

		titleLabel.setText(safeText(post.title));

		LocalDate d = (post.createdAt == null) ? LocalDate.now() : post.createdAt;
		String visibility = post.isPublic ? "공개" : "비공개";
		metaLabel.setText(d.format(fmt) + " · " + visibility);

		
		String question = safeText(post.processText);
		questionArea.setText(question);

		
		String link = safeText(post.linkUrl);
		if (link.isBlank())
			link = safeText(post.link);

		boolean showLink = !link.isBlank();
		secLink.setVisible(showLink);
		divAfterQuestion.setVisible(showLink);

		if (showLink) {
			String normalized = normalizeUrl(link);
			linkLabel.setText("<html><u>" + escapeHtml(normalized) + "</u></html>");
		} else {
			linkLabel.setText("");
		}

		
		if (socialCard != null) socialCard.setVisible(post.isPublic);
		boolean canSocial = post.isPublic;
		if (reactionRow != null) reactionRow.setVisible(canSocial);
		if (commentField != null) commentField.setEnabled(canSocial);
		if (commentSubmitBtn != null) commentSubmitBtn.setEnabled(canSocial);

		if (post.isPublic) {
			rebuildComments();
		} else {
			if (commentsWrap != null) {
				commentsWrap.removeAll();
				commentsWrap.revalidate();
				commentsWrap.repaint();
			}
		}

		
		refreshSocialCounts();
		refreshReactionSelection();
		refreshReactionSummary();

		scrollToTop();
		revalidate();
		repaint();
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

	private static String mi(int codePointHex) {
		return new String(Character.toChars(codePointHex));
	}

	

	private String safeText(String s) {
		return (s == null) ? "" : s.trim();
	}

	private void confirmDelete() {
		int r = JOptionPane.showConfirmDialog(this, "이 질문을 삭제할까요?", "삭제 확인", JOptionPane.YES_NO_OPTION);
		if (r == JOptionPane.YES_OPTION) {
			JOptionPane.showMessageDialog(this, "삭제 기능은 DB 연결 후 적용할 수 있어요.");
		}
	}

	private void styleTopButton(RoundedButton btn, Color bg, Color fg, boolean primary) {
		btn.setBackground(bg);
		btn.setForeground(fg);
		btn.setFont(FontKit.medium(13.5f));
		btn.setPreferredSize(new Dimension(primary ? 170 : 110, 36));
	}

	private void applyMetaChip(Chip chip, String text) {
		chip.setChip(text, UITheme.chipBg(UITheme.ChipStyle.NEUTRAL), UITheme.chipFg(UITheme.ChipStyle.NEUTRAL));
	}

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
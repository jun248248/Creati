package com.creati.ui.main;

import com.creati.model.LogStatus;
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

import static com.creati.ui.main.MainUiParts.RoundedButton;

/**
 * LogDetailView
 *
 * 상세 화면 목적:
 * - 경험 맥락 복원
 * - 기록 스캔
 * - 자기 인식 강화
 * - 다음 행동 유도(재도전 중심)
 *
 * 구현 포인트:
 * - 제목 카드 + 컨텐츠 단일 카드(큰 카드) 구조
 * - 섹션은 "제목 + 내용" 규칙 통일
 * - 필수 섹션(행동 과정/회고/성장 설계)은 값이 비어도 더미 문구로 표시(레이아웃 붕 뜸 방지)
 * - 선택 섹션은 값이 없으면 완전히 숨김(없음/미입력 표기 금지)
 */
public class LogDetailView extends JPanel {

    // Config ----
    private static final String DUMMY_TEXT = "아직 작성되지 않았어요. 다음 기록에서 채워볼까요?";
    private static final String DUMMY_PLAN = "다음에는 어떤 방식으로 시도해볼까요?";

    // Callbacks ----
    private final Runnable onBack;
    private LogPost boundPost;
    private final Runnable onRetry;
    private final Runnable onEdit;
    private final Runnable onDelete;

    // Title Card ----
    private final JLabel titleLabel = new JLabel();
    private final JLabel metaLabel = new JLabel();
    private final Chip fieldChip = new Chip();
    private final Chip categoryChip = new Chip();
    private final Chip statusChip = new Chip();

    // TopBar ----
    private final RoundedButton backBtn = new RoundedButton("뒤로가기");
    private final RoundedButton retryBtn = new RoundedButton("재도전 로그 작성하기");
    private final RoundedButton editBtn = new RoundedButton("수정하기");
    private final RoundedButton deleteBtn = new RoundedButton("삭제하기");

    // Scroll ----
    private JScrollPane scroll;

    // Content card + sections ----
    private JPanel contentCard;
    private JPanel secExpectation;
    private JPanel secResult;
    private JPanel secFactors;
    private JPanel secProcess;
    private JPanel secPlanGap;
    private JPanel secLearning;
    private JPanel secGrowth;
    private JPanel secLink;

    // Dividers (섹션 표시 여부에 따라 on/off)
    private JComponent divAfterExpectation;
    private JComponent divAfterResult;
    private JComponent divAfterFactors;
    private JComponent divAfterProcess;
    private JComponent divAfterPlanGap;
    private JComponent divAfterLearning;
    private JComponent divAfterGrowth;

    // Section components ----
    private JTextArea expectationArea;

    private Chip moodChip;                 // 진행 느낌 칩
    private JPanel goodChipsWrap;
    private JTextArea painArea;

    private JPanel factorsChipsWrap;

    private JTextArea processArea;

    private Chip planGapChip;
    private JPanel planGapDetailWrap;      // 계획 차이 섹션 내부 detail 영역 숨김
    private JTextArea planGapDetailArea;

    private JTextArea learningArea;

    private JPanel nextAdjustWrap;
    private JLabel nextAdjustFallback;
    private JLabel nextPlanLine;
    private JTextArea retryConditionArea;

    private final JLabel linkLabel = new JLabel();
    private JLabel linkPointLabel;

    public LogDetailView(Runnable onBack) {
        this(onBack, null, null, null);
    }

    public LogDetailView(Runnable onBack, Runnable onRetry, Runnable onEdit) {
        this(onBack, onRetry, onEdit, null);
    }

    public LogDetailView(Runnable onBack, Runnable onRetry, Runnable onEdit, Runnable onDelete) {
        this.onBack = (onBack == null) ? () -> {} : onBack;
        this.onRetry = (onRetry == null) ? () -> JOptionPane.showMessageDialog(this, "재도전 로그 기능은 준비 중이에요.") : onRetry;
        this.onEdit = (onEdit == null) ? () -> JOptionPane.showMessageDialog(this, "수정 기능은 준비 중이에요.") : onEdit;
        this.onDelete = (onDelete == null) ? this::confirmDelete : onDelete;

        UITheme.ensureInit();
        FontKit.init();
        initLinkLabel();

        setLayout(new BorderLayout());
        setBackground(UITheme.BG);

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    private void initLinkLabel() {
        linkLabel.setFont(FontKit.regular(14f));
        linkLabel.setForeground(new Color(0x1D4ED8));
        linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkLabel.setAlignmentX(LEFT_ALIGNMENT);
        linkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String url = stripHtml(linkLabel.getText());
                if (url.isEmpty()) return;
                openLink(url);
            }
        });
    }

    private JComponent buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(8, 20, 8, 20));

        // left
        styleTopButton(backBtn, Color.WHITE, new Color(0x2A2A33), false);
        backBtn.addActionListener(e -> onBack.run());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.setBorder(new EmptyBorder(0, 10, 0, 0));
        left.add(backBtn);

        // right
        styleTopButton(retryBtn, new Color(0xE9E3FF), new Color(0x2A2A33), true);
        retryBtn.setBorder(BorderFactory.createLineBorder(new Color(0xD8CFFF), 1));

        styleTopButton(editBtn, Color.WHITE, new Color(0x2A2A33), false);
        styleTopButton(deleteBtn, new Color(0xFFF1F1), new Color(0xB3261E), false);

        retryBtn.addActionListener(e -> onRetry.run());
        editBtn.addActionListener(e -> onEdit.run());
        deleteBtn.addActionListener(e -> onDelete.run());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(retryBtn);
        right.add(editBtn);
        right.add(deleteBtn);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(left, BorderLayout.WEST);
        wrap.add(right, BorderLayout.EAST);

        // divider
        JPanel divider = new JPanel();
        divider.setOpaque(false);
        divider.setBorder(new MatteBorder(0, 0, 1, 0, new Color(0xEEEFF6)));

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
                BorderFactory.createLineBorder(new Color(0xE6E6EF), 1),
                new EmptyBorder(18, 18, 18, 18)
        ));

        titleLabel.setFont(FontKit.extraBold(26f));
        titleLabel.setForeground(UITheme.TEXT);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        metaLabel.setFont(FontKit.regular(13f));
        metaLabel.setForeground(new Color(0x6B6B77));
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

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setAlignmentX(LEFT_ALIGNMENT);
        head.add(chips);
        head.add(Box.createVerticalStrut(10));
        head.add(titleLabel);
        head.add(Box.createVerticalStrut(8));
        head.add(metaLabel);

        card.add(head, BorderLayout.CENTER);
        return card;
    }

    /** 컨텐츠는 하나의 큰 카드 안에 섹션 블록들로 구성 */
    private JPanel buildContentCard() {
        JPanel card = cardBase();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE6E6EF), 1),
                new EmptyBorder(18, 18, 18, 18)
        ));

        // 기대했던 점 (선택: 값 없으면 숨김)
        expectationArea = makeArea();
        secExpectation = sectionBlock("기대했던 점", expectationArea);
        card.add(secExpectation);
        divAfterExpectation = sectionDivider();
        card.add(divAfterExpectation);

        // 결과 인식 (선택: 값 없으면 숨김)
        JPanel resultBody = new JPanel();
        resultBody.setOpaque(false);
        resultBody.setLayout(new BoxLayout(resultBody, BoxLayout.Y_AXIS));

        moodChip = new Chip();
        moodChip.setFont(FontKit.medium(12.5f));
        applyMoodChip(moodChip, "");
        moodChip.setAlignmentX(LEFT_ALIGNMENT);
        resultBody.add(moodChip);
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

        // 영향 요인 (선택: 값 없으면 숨김)
        factorsChipsWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        factorsChipsWrap.setOpaque(false);
        factorsChipsWrap.setAlignmentX(LEFT_ALIGNMENT);
        secFactors = sectionBlock("영향 요인", factorsChipsWrap);
        card.add(secFactors);
        divAfterFactors = sectionDivider();
        card.add(divAfterFactors);

        // 행동 과정 (필수: 값 없어도 더미 표시)
        processArea = makeArea();
        secProcess = sectionBlock("행동 과정", processArea);
        card.add(secProcess);
        divAfterProcess = sectionDivider();
        card.add(divAfterProcess);

        // 계획 차이 (+ 차이 내용 같은 섹션 안) (선택)
        JPanel planBody = new JPanel();
        planBody.setOpaque(false);
        planBody.setLayout(new BoxLayout(planBody, BoxLayout.Y_AXIS));

        planGapChip = new Chip();
        planGapChip.setFont(FontKit.medium(12.5f));
        planGapChip.setAlignmentX(LEFT_ALIGNMENT);
        planBody.add(planGapChip);

        planGapDetailWrap = new JPanel();
        planGapDetailWrap.setOpaque(false);
        planGapDetailWrap.setLayout(new BoxLayout(planGapDetailWrap, BoxLayout.Y_AXIS));
        planGapDetailWrap.setBorder(new EmptyBorder(14, 0, 0, 0));

        JLabel detailTitle = new JLabel("차이 내용");
        detailTitle.setFont(FontKit.semiBold(13.5f));
        detailTitle.setForeground(new Color(0x2A2A33));
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

        // 회고 (필수: 값 없어도 더미 표시)
        learningArea = makeArea();
        secLearning = sectionBlock("회고", learningArea);
        card.add(secLearning);
        divAfterLearning = sectionDivider();
        card.add(divAfterLearning);

        // 성장 설계 (필수: 최소 라인 더미 표시)
        JPanel growthBody = new JPanel();
        growthBody.setOpaque(false);
        growthBody.setLayout(new BoxLayout(growthBody, BoxLayout.Y_AXIS));

        JLabel a = new JLabel("다음에 조정해 보고 싶은 부분");
        a.setFont(FontKit.semiBold(13.5f));
        a.setForeground(new Color(0x2A2A33));
        a.setAlignmentX(LEFT_ALIGNMENT);
        growthBody.add(a);
        growthBody.add(Box.createVerticalStrut(10));

        nextAdjustWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nextAdjustWrap.setOpaque(false);
        nextAdjustWrap.setAlignmentX(LEFT_ALIGNMENT);

        nextAdjustFallback = new JLabel("이번에는 현재 방식을 유지할래요.");
        nextAdjustFallback.setFont(FontKit.regular(14f));
        nextAdjustFallback.setForeground(new Color(0x6B6B77));
        nextAdjustFallback.setAlignmentX(LEFT_ALIGNMENT);

        growthBody.add(nextAdjustWrap);
        growthBody.add(nextAdjustFallback);
        growthBody.add(Box.createVerticalStrut(16));

        JLabel b = new JLabel("다음 시도 계획");
        b.setFont(FontKit.semiBold(13.5f));
        b.setForeground(new Color(0x2A2A33));
        b.setAlignmentX(LEFT_ALIGNMENT);
        growthBody.add(b);
        growthBody.add(Box.createVerticalStrut(8));

        nextPlanLine = new JLabel(" ");
        nextPlanLine.setFont(FontKit.regular(14f));
        nextPlanLine.setForeground(new Color(0x2A2A33));
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

        // 참고 링크 (선택)
        JPanel linkBody = new JPanel();
        linkBody.setOpaque(false);
        linkBody.setLayout(new BoxLayout(linkBody, BoxLayout.Y_AXIS));

        linkBody.add(linkLabel);

        linkPointLabel = new JLabel();
        linkPointLabel.setFont(FontKit.regular(13f));
        linkPointLabel.setForeground(new Color(0x6B6B77));
        linkPointLabel.setBorder(new EmptyBorder(8, 0, 0, 0));
        linkPointLabel.setAlignmentX(LEFT_ALIGNMENT);
        linkBody.add(linkPointLabel);

        secLink = sectionBlock("참고 링크", linkBody);
        card.add(secLink);

        return card;
    }

    /** 섹션 = 제목 + 내용 규칙 통일 */
    private JPanel sectionBlock(String title, JComponent body) {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setAlignmentX(LEFT_ALIGNMENT);

        JLabel t = new JLabel(title);
        t.setFont(FontKit.semiBold(14f));
        t.setForeground(new Color(0x2A2A33));
        t.setAlignmentX(LEFT_ALIGNMENT);

        body.setAlignmentX(LEFT_ALIGNMENT);

        wrap.add(t);
        wrap.add(Box.createVerticalStrut(10));
        wrap.add(body);
        return wrap;
    }

    private JComponent sectionDivider() {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBorder(new EmptyBorder(12, 0, 12, 0)); // 너무 넓은 느낌 줄이기(16 -> 12)

        JComponent line = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0xEFEFF6));
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
        p.setBackground(Color.WHITE);
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
        ta.setForeground(new Color(0x2A2A33));
        return ta;
    }

    public LogPost getBoundPost() { return boundPost; }

    public void bind(LogPost post) {
        this.boundPost = post;
        if (post == null) return;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        String visibility = post.isPublic ? "공개" : "비공개";

        // Title Card ---
        String field = safeText(post.field);
        String subCategory = safeText(post.subCategory);

        applyMetaChip(fieldChip, field);
        applyMetaChip(categoryChip, subCategory);
        applyStatusChip(statusChip, post.status);

        titleLabel.setText(safeText(post.title));
        metaLabel.setText(post.createdAt.format(fmt) + " · " + visibility);

        // Content binding (v2 우선) ---
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

        // 기대 (선택) ----
        boolean showExpectation = !goal.isBlank();
        secExpectation.setVisible(showExpectation);
        expectationArea.setText(goal);

        // 결과 인식 (선택) ----
        boolean hasGood = !(goodPoints.isEmpty() && safeText(goodOther).isBlank());
        boolean showResult = (!mood.isBlank()) || (!pain.isBlank()) || hasGood;
        secResult.setVisible(showResult);

        if (showResult) {
            String moodText = stripEmoji(mood);
            if (moodText.isBlank()) {
                // 선택 섹션이므로 "기록 없음" 같은 텍스트를 굳이 보여주지 않음: 칩 자체는 숨겨도 OK
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

            // 둘 다 없으면 섹션 자체를 숨기는게 낫다(이 케이스는 showResult 계산상 거의 없음)
        }

        // 영향 요인 (선택) ----
        List<String> factorMerged = mergeListWithOther(factors, factorsOther);
        rebuildChipWrap(factorsChipsWrap, factorMerged, ChipStyle.FACTOR);
        boolean showFactors = factorsChipsWrap.getComponentCount() > 0;
        secFactors.setVisible(showFactors);

        // 행동 과정 (필수) ----
        if (process.isBlank()) {
            processArea.setText(DUMMY_TEXT);
        } else {
            processArea.setText(process);
        }
        secProcess.setVisible(true);

        // 계획 차이 (선택) ----
        boolean showPlanGap = !planGap.isBlank();
        secPlanGap.setVisible(showPlanGap);

        if (showPlanGap) {
            applyGreyChip(planGapChip, stripEmoji(planGap));
            planGapDetailArea.setText(planGapDetail);
            planGapDetailWrap.setVisible(!planGapDetail.isBlank());
        }

        // 회고 (필수) ----
        if (learning.isBlank()) {
            learningArea.setText(DUMMY_TEXT);
        } else {
            learningArea.setText(learning);
        }
        secLearning.setVisible(true);

        // 성장 설계 (필수) ----
        secGrowth.setVisible(true);

        List<String> nextAdjustMerged = mergeListWithOther(nextAdjust, nextAdjustOther);
        rebuildChipWrap(nextAdjustWrap, nextAdjustMerged, ChipStyle.GOOD);
        boolean hasNextAdjust = nextAdjustWrap.getComponentCount() > 0;
        nextAdjustWrap.setVisible(hasNextAdjust);
        nextAdjustFallback.setVisible(!hasNextAdjust);

        String planLine = stripEmoji(nextPlan);
        if (planLine.isBlank()) planLine = DUMMY_PLAN;
        nextPlanLine.setText(planLine);

        if (!retryCondition.isBlank()) {
            retryConditionArea.setText(retryCondition);
            retryConditionArea.setVisible(true);
        } else {
            retryConditionArea.setText("");
            retryConditionArea.setVisible(false);
        }

        // 참고 링크 (선택) ----
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

        // Divider visibility (보이는 섹션 사이만) ----
        updateDividers();

        scrollToTop();
        revalidate();
        repaint();
    }

    private void updateDividers() {
        boolean vExpectation = secExpectation.isVisible();
        boolean vResult = secResult.isVisible();
        boolean vFactors = secFactors.isVisible();
        boolean vProcess = secProcess.isVisible();   // 필수
        boolean vPlanGap = secPlanGap.isVisible();
        boolean vLearning = secLearning.isVisible(); // 필수
        boolean vGrowth = secGrowth.isVisible();     // 필수
        boolean vLink = secLink.isVisible();

        // divider는 "앞 섹션이 보이고" + "뒤에 보이는 섹션이 하나라도 있을 때"만 표시
        divAfterExpectation.setVisible(vExpectation && (vResult || vFactors || vProcess || vPlanGap || vLearning || vGrowth || vLink));
        divAfterResult.setVisible(vResult && (vFactors || vProcess || vPlanGap || vLearning || vGrowth || vLink));
        divAfterFactors.setVisible(vFactors && (vProcess || vPlanGap || vLearning || vGrowth || vLink));
        divAfterProcess.setVisible(vProcess && (vPlanGap || vLearning || vGrowth || vLink));
        divAfterPlanGap.setVisible(vPlanGap && (vLearning || vGrowth || vLink));
        divAfterLearning.setVisible(vLearning && (vGrowth || vLink));
        divAfterGrowth.setVisible(vGrowth && vLink);
    }

    private void rebuildChipWrap(JPanel wrap, List<String> values, ChipStyle style) {
        wrap.removeAll();
        if (values != null) {
            for (String v : values) {
                String text = safeText(v);
                if (text.isBlank()) continue;
                Chip c = new Chip();
                c.setFont(FontKit.medium(12.5f));
                applyStyledChip(c, text, style);
                wrap.add(c);
            }
        }
        wrap.revalidate();
        wrap.repaint();
    }

    private enum ChipStyle { META, GOOD, FACTOR }

    /** 칩 컬러: 파스텔 톤으로 정리 */
    private void applyStyledChip(Chip chip, String text, ChipStyle style) {
        if (style == ChipStyle.GOOD) {
            chip.setChip(text, new Color(0xFFF3D6), new Color(0x7A4B00));
            return;
        }
        if (style == ChipStyle.FACTOR) {
            chip.setChip(text, new Color(0xF3F1FF), new Color(0x4C3DAE));
            return;
        }
        chip.setChip(text, new Color(0xF3F4F6), new Color(0x2A2A33));
    }

    private void applyMoodChip(Chip chip, String text) {
        // 진행 느낌(연한 하늘)
        chip.setChip("진행 느낌 · " + safeText(text), new Color(0xEAF2FF), new Color(0x1D4ED8));
    }

    private void applyMetaChip(Chip chip, String text) {
        applyStyledChip(chip, text, ChipStyle.META);
    }

    private void applyGreyChip(Chip chip, String text) {
        chip.setChip(text, new Color(0xF3F4F6), new Color(0x374151));
    }

    private void applyStatusChip(Chip chip, LogStatus status) {
        String label = (status == null) ? "진행중" : String.valueOf(status);

        if (label.contains("보완") || label.contains("FAIL")) {
            chip.setChip("보완 필요", new Color(0xFCEEF4), new Color(0xC2417A));
            return;
        }
        if (label.contains("완료") || label.contains("DONE") || label.contains("COMPLETE")) {
            chip.setChip("완료", new Color(0xEAF7EE), new Color(0x166534));
            return;
        }
        chip.setChip("진행중", new Color(0xEAF2FF), new Color(0x1D4ED8));
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
                if (!v.isBlank()) out.add(v);
            }
        }
        String o = safeText(other);
        if (!o.isBlank()) out.add(o);
        return out;
    }

    private void scrollToTop() {
        if (scroll == null) return;
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scroll.getVerticalScrollBar();
            if (bar != null) bar.setValue(0);
        });
    }

    private void openLink(String url) {
        String normalized = normalizeUrl(url);
        if (normalized.isEmpty()) return;
        try {
            if (!Desktop.isDesktopSupported()) return;
            Desktop.getDesktop().browse(new URI(normalized));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "링크를 열 수 없어요.\n" + normalized);
        }
    }

    private String normalizeUrl(String url) {
        if (url == null) return "";
        String u = url.trim();
        if (u.isEmpty()) return "";
        if (u.startsWith("http://") || u.startsWith("https://")) return u;
        return "https://" + u;
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String stripHtml(String s) {
        if (s == null) return "";
        return s.replaceAll("<[^>]*>", "").trim();
    }

    private String stripEmoji(String s) {
        if (s == null) return "";
        return s.replaceAll("^[\\p{So}\\p{Sk}\\p{Cs}\\s]+", "").trim();
    }

    private String safeText(String s) {
        return (s == null) ? "" : s.trim();
    }

    private String firstNonBlank(String a, String b) {
        String x = safeText(a);
        if (!x.isBlank()) return x;
        return safeText(b);
    }

    private void confirmDelete() {
        int r = JOptionPane.showConfirmDialog(this, "이 글을 삭제할까요?", "삭제 확인", JOptionPane.YES_NO_OPTION);
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

    // UI chip component ---
    private static class Chip extends JComponent {
        private String text = "";
        private Color bg = new Color(0xEEEFFF);
        private Color fg = new Color(0x333333);

        private final int padX = 12;
        private final int padY = 5;
        private final int minH = 26;

        Chip() {
            setOpaque(false);
            setFont(FontKit.medium(12.5f));
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

            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int tx = padX;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();

            g2.setColor(fg);
            g2.drawString(text, tx, ty);
            g2.dispose();
        }
    }
}

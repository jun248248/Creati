package com.creati.ui.main;

import com.creati.dao.LogDao;
import com.creati.dto.LogDto;
import com.creati.model.AppState;
import com.creati.service.ConclusionBuilder;
import com.creati.service.ConclusionBuilder.Result;
import com.creati.service.LogCompareScorer;
import com.creati.service.LogCompareScorer.ScoreBreakdown;
import com.creati.ui.components.RoundedButton;
import com.creati.util.FontKit;
import com.creati.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LogCompareView extends JPanel {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final JComboBox<LogEntry> cbA = new JComboBox<>();
    private final JComboBox<LogEntry> cbB = new JComboBox<>();
    private final RoundedButton btnCompare = new RoundedButton("비교하기");

    // 결과 영역 - BorderLayout 기반으로 꽉 채움
    private final JPanel resultPanel = new JPanel(new BorderLayout());

    private final List<LogDto> logs = new ArrayList<>();

    public LogCompareView() {
        UITheme.ensureInit();
        FontKit.init();

        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(UITheme.BG);

        // 전체 컨텐츠 패널
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(14, 18, 18, 18));

        // 상단 고정 영역 (헤더 + 선택 카드)
        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(buildHeader(), BorderLayout.NORTH);
        top.add(buildSelectCard(), BorderLayout.CENTER);

        resultPanel.setOpaque(false);
        showEmptyState();

        // 결과 영역 스크롤
        JScrollPane scroll = new JScrollPane(resultPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        content.add(top, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);

        loadLogs();
    }

    // ── 헤더
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel title = new JLabel("로그 비교");
        title.setFont(UITheme.BODY_MED);
        title.setForeground(UITheme.TEXT);

        JLabel sub = new JLabel("두 기록을 비교해서 나에게 맞는 방식을 찾아봐요.");
        sub.setFont(UITheme.CAPTION);
        sub.setForeground(UITheme.MUTED_TEXT);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(title);
        texts.add(Box.createVerticalStrut(2));
        texts.add(sub);
        p.add(texts, BorderLayout.WEST);
        return p;
    }

    // ── 선택 카드
    private JPanel buildSelectCard() {
        JPanel card = makeWhiteCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 0;

        styleCombo(cbA);
        styleCombo(cbB);

        c.gridx = 0; c.weightx = 0.44; c.insets = new Insets(0, 0, 0, 12);
        card.add(makeComboSection("로그 A", cbA), c);

        c.gridx = 1; c.weightx = 0.44; c.insets = new Insets(0, 0, 0, 12);
        card.add(makeComboSection("로그 B", cbB), c);

        btnCompare.setBackground(UITheme.ACCENT_PURPLE);
        btnCompare.setForeground(Color.WHITE);
        btnCompare.setFont(UITheme.BODY_MED);
        btnCompare.setBorder(new EmptyBorder(10, 22, 10, 22));
        btnCompare.addActionListener(e -> onCompare());

        c.gridx = 2; c.weightx = 0.12; c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.SOUTH; c.insets = new Insets(18, 0, 0, 0);
        card.add(btnCompare, c);

        return card;
    }

    private JPanel makeComboSection(String label, JComboBox<LogEntry> cb) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.CAPTION);
        lbl.setForeground(UITheme.MUTED_TEXT);
        p.add(lbl, BorderLayout.NORTH);
        p.add(cb, BorderLayout.CENTER);
        return p;
    }

    // ── 빈 상태 (비교 전)
    private void showEmptyState() {
        resultPanel.removeAll();

        JPanel card = makeWhiteCard();
        card.setLayout(new BorderLayout());

        JPanel textWrap = new JPanel();
        textWrap.setOpaque(false);
        textWrap.setLayout(new BoxLayout(textWrap, BoxLayout.Y_AXIS));

        JLabel l1 = new JLabel("로그 두 개를 골라서 비교하기 버튼을 눌러줘!");
        l1.setFont(UITheme.BODY_MED);
        l1.setForeground(UITheme.TEXT);
        l1.setAlignmentX(LEFT_ALIGNMENT);

        JLabel l2 = new JLabel("어떤 방식이 더 잘 맞는지 같이 확인해보자");
        l2.setFont(UITheme.CAPTION);
        l2.setForeground(UITheme.MUTED_TEXT);
        l2.setAlignmentX(LEFT_ALIGNMENT);

        textWrap.add(l1);
        textWrap.add(Box.createVerticalStrut(4));
        textWrap.add(l2);
        card.add(textWrap, BorderLayout.CENTER);

        // resultPanel 전체를 채우되, 카드가 위에 붙도록 NORTH
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.add(card, BorderLayout.NORTH);
        resultPanel.add(outer, BorderLayout.CENTER);

        refreshResult();
    }

    // ── 비교 실행
    private void onCompare() {
        LogEntry entryA = (LogEntry) cbA.getSelectedItem();
        LogEntry entryB = (LogEntry) cbB.getSelectedItem();

        if (entryA == null || entryB == null) {
            showMessage("로그를 두 개 모두 선택해줘!");
            return;
        }
        if (entryA.dto.getId().equals(entryB.dto.getId())) {
            showMessage("같은 로그를 두 번 골랐어! 다른 로그를 골라줘.");
            return;
        }

        LogDto a = entryA.dto;
        LogDto b = entryB.dto;
        ScoreBreakdown sa = LogCompareScorer.score(a);
        ScoreBreakdown sb = LogCompareScorer.score(b);
        Result result = ConclusionBuilder.build(a, sa, b, sb);

        // 결과 패널 구성 - 모두 꽉 채우는 세로 스택
        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        addFull(stack, buildConclusionCard(result, a, b));
        stack.add(Box.createVerticalStrut(10));
        addFull(stack, buildScoreTable(sa, sb));
        stack.add(Box.createVerticalStrut(10));
        addFull(stack, buildActionCards(result));
        stack.add(Box.createVerticalGlue());

        resultPanel.removeAll();
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.add(stack, BorderLayout.CENTER);
        resultPanel.add(outer, BorderLayout.CENTER);

        refreshResult();
    }

    // ── 결론 카드 (에티 이미지 없음)
    private JPanel buildConclusionCard(Result result, LogDto a, LogDto b) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(UITheme.ACCENT_LAVENDER_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.ACCENT_LAVENDER_BORDER, 1, true),
            new EmptyBorder(18, 18, 18, 18)
        ));

        // 상단: 배지 + A vs B 칩
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topRow.setOpaque(false);
        topRow.add(makeWinnerBadge(result.winner));
        topRow.add(logTitleChip(a, "A"));
        JLabel vs = new JLabel("vs");
        vs.setFont(UITheme.CAPTION);
        vs.setForeground(UITheme.MUTED_TEXT);
        topRow.add(vs);
        topRow.add(logTitleChip(b, "B"));

        // 중단: 헤드라인 + 이유
        JPanel midPanel = new JPanel();
        midPanel.setOpaque(false);
        midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.Y_AXIS));

        JLabel headline = new JLabel(result.headline);
        headline.setFont(UITheme.BODY_MED);
        headline.setForeground(UITheme.TEXT);
        headline.setAlignmentX(LEFT_ALIGNMENT);
        midPanel.add(headline);
        midPanel.add(Box.createVerticalStrut(8));

        for (String reason : result.reasons) {
            JLabel r = new JLabel("  " + reason);
            r.setFont(UITheme.CAPTION);
            r.setForeground(UITheme.TEXT_DISABLED);
            r.setAlignmentX(LEFT_ALIGNMENT);
            midPanel.add(r);
            midPanel.add(Box.createVerticalStrut(2));
        }

        // 하단: 팁
        JPanel tipCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        tipCard.setBackground(UITheme.WARNING_BG);
        tipCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xFCD34D), 1, true),
            new EmptyBorder(0, 4, 0, 8)
        ));
        JLabel tipIcon = new JLabel(String.valueOf('\uE0F0'));
        tipIcon.setFont(FontKit.materialIcon(13f));
        tipIcon.setForeground(new Color(0xD97706));
        JLabel tipText = new JLabel(result.tip);
        tipText.setFont(FontKit.regular(12f));
        tipText.setForeground(new Color(0x7A4B00));
        tipCard.add(tipIcon);
        tipCard.add(tipText);

        card.add(topRow, BorderLayout.NORTH);
        card.add(midPanel, BorderLayout.CENTER);
        card.add(tipCard, BorderLayout.SOUTH);

        return card;
    }

    // ── 지표 비교 테이블
    private JPanel buildScoreTable(ScoreBreakdown sa, ScoreBreakdown sb) {
        JPanel card = makeWhiteCard();
        card.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("지표별 비교");
        title.setFont(UITheme.BODY_MED);
        title.setForeground(UITheme.TEXT);

        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));

        Object[][] defs = {
            {"다음 정리 쉬움",  "다음에 뭘 할지 얼마나 쉽게 보이는지", sa.decision,     sb.decision},
            {"계획-실행 맞춤",  "계획대로 됐는지, 달랐다면 정리했는지", sa.planFit,      sb.planFit},
            {"아쉬움 정리",     "잘 된 부분이나 아쉬운 이유가 있는지",  sa.cause,        sb.cause},
            {"바꿀 것 뚜렷함",  "다음에 조정할 포인트가 명확한지",       sa.tweakClarity, sb.tweakClarity},
            {"다음 시도 준비",  "언제/어떻게 다시 할지 적혀있는지",     sa.retryReady,   sb.retryReady},
            {"확인 포인트",     "링크나 확인 근거가 있는지",             sa.evidence,     sb.evidence},
        };

        for (int i = 0; i < defs.length; i++) {
            rows.add(buildScoreRow(
                (String)  defs[i][0], (String)  defs[i][1],
                (Integer) defs[i][2], (Integer) defs[i][3]
            ));
            if (i < defs.length - 1) rows.add(Box.createVerticalStrut(10));
        }

        // 합계
        rows.add(Box.createVerticalStrut(12));
        rows.add(makeDivider());
        rows.add(Box.createVerticalStrut(10));
        rows.add(buildScoreRow("합계 (12점 만점)",  "", sa.total, sb.total, 12));

        card.add(title, BorderLayout.NORTH);
        card.add(rows,  BorderLayout.CENTER);
        return card;
    }

    private JPanel buildScoreRow(String name, String desc, int scoreA, int scoreB) {
        return buildScoreRow(name, desc, scoreA, scoreB, 2);
    }

    private JPanel buildScoreRow(String name, String desc, int scoreA, int scoreB, int maxVal) {
        // GridLayout(1,3)으로 3칸을 완전히 균등 분할 → 모든 행이 동일한 너비
        JPanel row = new JPanel(new GridLayout(1, 3, 8, 0));
        row.setOpaque(false);

        // 칼럼1: 지표명
        JPanel nameWrap = new JPanel(new BorderLayout());
        nameWrap.setOpaque(false);
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(UITheme.CAPTION);
        nameLbl.setForeground(UITheme.TEXT);
        nameWrap.add(nameLbl, BorderLayout.NORTH);
        if (!desc.isEmpty()) {
            JLabel descLbl = new JLabel(desc);
            descLbl.setFont(FontKit.regular(10.5f));
            descLbl.setForeground(UITheme.MUTED_TEXT);
            nameWrap.add(descLbl, BorderLayout.CENTER);
        }

        // 칼럼2: A 바
        // 칼럼3: B 바
        row.add(nameWrap);
        row.add(makeBarRow("A", scoreA, maxVal, UITheme.ACCENT_PURPLE, scoreA >= scoreB));
        row.add(makeBarRow("B", scoreB, maxVal, new Color(0x0EA5E9), scoreB >= scoreA));

        return row;
    }

    private JPanel makeBarRow(String label, int score, int maxVal, Color color, boolean isWinner) {
        JPanel p = new JPanel(new BorderLayout(5, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(isWinner ? FontKit.semiBold(11f) : FontKit.regular(11f));
        lbl.setForeground(isWinner ? color : UITheme.MUTED_TEXT);
        lbl.setPreferredSize(new Dimension(14, 14));
        p.add(lbl, BorderLayout.WEST);
        p.add(new ScoreBar(score, maxVal, color, isWinner), BorderLayout.CENTER);
        return p;
    }

    // ── 유지 / 조정 / 쉬어가기 3칸 카드
    private JPanel buildActionCards(Result result) {
        JPanel grid = new JPanel(new GridLayout(1, 3, 10, 0));
        grid.setOpaque(false);

        grid.add(buildActionCard('\uE876', "이건 유지해봐",
            "이건 잘 됐어! 다음에도 가져가도 좋아", result.keepText,
            UITheme.SUCCESS_BG, new Color(0x166634), new Color(0x22C55E)));

        grid.add(buildActionCard('\uE3C9', "여기만 살짝 바꿔보자",
            "다음엔 이 포인트만 실험해보면 어때?", result.tweakText,
            UITheme.INFO_BG, UITheme.ACCENT_BLUE, UITheme.ACCENT_BLUE));

        String holdMsg = (result.isHoldA || result.isHoldB)
            ? "쉬는 것도 다음을 위한 준비야"
            : "에너지가 필요하면 쉬어도 괜찮아!";
        grid.add(buildActionCard('\uE0AF', "잠깐 쉬어도 괜찮아",
            holdMsg, "잠깐 쉬어가는 것도 전략이야",
            UITheme.WARNING_BG, new Color(0x7A4B00), new Color(0xD97706)));

        // 카드 자체를 whitcard border로 감싸기
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(grid, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildActionCard(char iconCp, String title, String sub,
                                   String body, Color bg, Color fg, Color iconColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(blend(bg, Color.BLACK, 0.08f), 1, true),
            new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel icon = new JLabel(String.valueOf(iconCp));
        icon.setFont(FontKit.materialIcon(18f));
        icon.setForeground(iconColor);
        icon.setAlignmentX(LEFT_ALIGNMENT);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FontKit.semiBold(12f));
        titleLbl.setForeground(fg);
        titleLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(FontKit.regular(11f));
        subLbl.setForeground(UITheme.MUTED_TEXT);
        subLbl.setAlignmentX(LEFT_ALIGNMENT);

        card.add(icon);
        card.add(Box.createVerticalStrut(6));
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(3));
        card.add(subLbl);

        if (!body.isEmpty()) {
            card.add(Box.createVerticalStrut(8));
            card.add(makeDivider());
            card.add(Box.createVerticalStrut(8));
            JLabel bodyLbl = new JLabel("<html><body style='width:120px'>" + escape(body) + "</body></html>");
            bodyLbl.setFont(UITheme.CAPTION);
            bodyLbl.setForeground(UITheme.TEXT);
            bodyLbl.setAlignmentX(LEFT_ALIGNMENT);
            card.add(bodyLbl);
        }
        return card;
    }

    // ── ScoreBar
    static class ScoreBar extends JComponent {
        private final int score, maxVal;
        private final Color fill;
        private final boolean isWinner;

        ScoreBar(int score, int maxVal, Color fill, boolean isWinner) {
            this.score    = Math.max(0, score);
            this.maxVal   = Math.max(1, maxVal);
            this.fill     = fill;
            this.isWinner = isWinner;
            setPreferredSize(new Dimension(100, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), arc = h;

            g2.setColor(UITheme.RGB_230_230_235);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            if (score > 0) {
                int fw = Math.max(arc, (int)(w * ((double)score / maxVal)));
                g2.setColor(isWinner ? fill : fill.darker().darker());
                g2.fillRoundRect(0, 0, fw, h, arc, arc);
            }

            g2.setFont(FontKit.medium(9f));
            String txt = score + "/" + maxVal;
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(txt);
            int fillW = score > 0 ? (int)(w * ((double)score / maxVal)) : 0;
            int tx = fillW - tw - 4;
            if (tx < 4) { tx = fillW + 4; g2.setColor(UITheme.MUTED_TEXT); }
            else g2.setColor(Color.WHITE);
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(txt, Math.max(2, tx), ty);
            g2.dispose();
        }
    }

    // ── DB 로딩
    public void refresh() {
        loadLogs();
    }

    private void loadLogs() {
        String userId = AppState.get().getCurrentUser() != null
            ? AppState.get().getCurrentUser().getId() : null;

        if (userId != null) {
            try {
                // DB(TODO): 실제 유저 로그 조회
                LogDao dao = new LogDao();
                List<LogDto> fetched = dao.findByUserId(userId, false);
                if (fetched != null) logs.addAll(fetched);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

     // 확인 후 지우고 아래 코드로 바꾸기 (if문 전체) + makeDummy 메서드 전체 삭제
        /* if (logs.size() < 2) {
        showMessage("비교하려면 로그가 2개 이상 필요해! 먼저 성장 로그를 작성해봐.");
    		}*/
        if (logs.isEmpty()) {
            logs.add(makeDummy(1L, "영상 편집 첫 도전", "RETRY", "VERY_SATISFIED",
                "촬영 먼저 하고 편집을 나중에 진행했어. 자막 속도는 직접 넣으면서 맞췄어.",
                "SIMILAR", "", "다음엔 썸네일 먼저 만들어보기", "바로 다시", "", "", ""));
            logs.add(makeDummy(2L, "브이로그 두 번째 시도", "RETRY", "SLIGHTLY_UNSATISFIED",
                "자막 속도가 너무 빠른 것 같아서 다시 조정했어. 시간이 많이 걸렸어.",
                "PARTLY_DIFFERENT", "자막 기준 속도를 정하지 않고 작업했더니 중간에 헤맸어.",
                "자막 기준 속도 정하기\n썸네일 작업 시간 따로 확보하기",
                "보완 후", "자막 가이드라인 정리가 완료되면 다시 시작할 예정",
                "https://youtu.be/example", "초반 5초 훅이 잘 됐는지 확인"));
            logs.add(makeDummy(3L, "쇼츠 실험: 세로형 영상", "RETRY", "SLIGHTLY_UNSATISFIED",
                "세로형으로 처음 찍어봤는데 구도를 못 잡겠더라.",
                "PARTLY_DIFFERENT", "구도 계획을 세우지 않고 바로 찍기 시작했어.",
                "다음엔 구도 레퍼런스 먼저 찾아보기", "고민 중", "", "", ""));
        }

        DefaultComboBoxModel<LogEntry> modelA = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<LogEntry> modelB = new DefaultComboBoxModel<>();
        for (LogDto d : logs) {
            modelA.addElement(new LogEntry(d));
            modelB.addElement(new LogEntry(d));
        }
        cbA.setModel(modelA);
        cbB.setModel(modelB);
        if (logs.size() > 1) cbB.setSelectedIndex(1);
    }

    private JPanel makeWhiteCard() {
        JPanel p = new JPanel();
        p.setBackground(UITheme.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.RGB_230_230_235, 1, true),
            new EmptyBorder(14, 14, 14, 14)
        ));
        return p;
    }

    private void addFull(JPanel parent, JPanel child) {
        child.setAlignmentX(LEFT_ALIGNMENT);
        child.setMaximumSize(new Dimension(Integer.MAX_VALUE, child.getPreferredSize().height + 200));
        parent.add(child);
    }

    private void showMessage(String msg) {
        resultPanel.removeAll();
        JPanel card = makeWhiteCard();
        card.setLayout(new BorderLayout());
        JLabel lbl = new JLabel(msg);
        lbl.setFont(UITheme.BODY);
        lbl.setForeground(UITheme.MUTED_TEXT);
        lbl.setBorder(new EmptyBorder(0, 2, 0, 0));
        card.add(lbl, BorderLayout.CENTER);
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.add(card, BorderLayout.NORTH);
        resultPanel.add(outer, BorderLayout.CENTER);
        refreshResult();
    }

    private void refreshResult() {
        resultPanel.revalidate();
        resultPanel.repaint();
    }

    private void styleCombo(JComboBox<LogEntry> cb) {
        cb.setFont(UITheme.BODY);
        cb.setBackground(UITheme.WHITE);
        cb.setFocusable(false);
    }

    private JPanel makeWinnerBadge(String winner) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        Color bg = winner.equals("DRAW") ? UITheme.HOVER_BG : UITheme.ACCENT_LAVENDER_BG_2;
        Color fg = winner.equals("DRAW") ? UITheme.TEXT_DISABLED : UITheme.PURPLE_DARK;
        String txt = switch (winner) {
            case "A" -> "A 우세";
            case "B" -> "B 우세";
            default  -> "막상막하";
        };
        JLabel badge = new JLabel(txt);
        badge.setFont(FontKit.semiBold(11f));
        badge.setForeground(fg);
        badge.setBackground(bg);
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        p.add(badge);
        return p;
    }

    private JLabel logTitleChip(LogDto log, String slot) {
        String title = log.getTitle() != null ? log.getTitle() : "(제목 없음)";
        if (title.length() > 12) title = title.substring(0, 12) + "…";
        JLabel lbl = new JLabel(slot + "  " + title);
        lbl.setFont(FontKit.regular(11f));
        lbl.setForeground(UITheme.TEXT_DISABLED);
        lbl.setBackground(UITheme.BG_ALT);
        lbl.setOpaque(true);
        lbl.setBorder(new EmptyBorder(2, 6, 2, 6));
        return lbl;
    }

    private JPanel makeDivider() {
        JPanel div = new JPanel();
        div.setOpaque(true);
        div.setBackground(UITheme.RGB_230_230_235);
        div.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        div.setPreferredSize(new Dimension(0, 1));
        div.setAlignmentX(LEFT_ALIGNMENT);
        return div;
    }

    private static Color blend(Color base, Color tint, float ratio) {
        int r = (int)(base.getRed()   * (1-ratio) + tint.getRed()   * ratio);
        int g = (int)(base.getGreen() * (1-ratio) + tint.getGreen() * ratio);
        int b = (int)(base.getBlue()  * (1-ratio) + tint.getBlue()  * ratio);
        return new Color(Math.max(0,Math.min(255,r)), Math.max(0,Math.min(255,g)), Math.max(0,Math.min(255,b)));
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    private LogDto makeDummy(Long id, String title, String status, String rating,
                             String process, String planDiff, String diff,
                             String reflection, String nextPlan, String retry,
                             String url, String goal) {
        LogDto d = new LogDto();
        d.setId(id); d.setTitle(title); d.setResultStatus(status); d.setResultRating(rating);
        d.setProcess(process); d.setPlanDifference(planDiff); d.setDifference(diff);
        d.setReflection(reflection); d.setNextPlanType(nextPlan); d.setRetryCondition(retry);
        d.setContentUrl(url); d.setGoal(goal);
        return d;
    }

    static class LogEntry {
        final LogDto dto;
        LogEntry(LogDto dto) { this.dto = dto; }

        @Override
        public String toString() {
            String title = dto.getTitle() != null ? dto.getTitle() : "(제목 없음)";
            String date  = dto.getCreatedAt() != null ? dto.getCreatedAt().format(DF) : "";
            return date.isEmpty() ? title : title + "  ·  " + date;
        }
    }
}
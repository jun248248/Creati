package com.creati.ui.main;

import com.creati.model.AppState;
import com.creati.service.GptAnalysisService;
import com.creati.service.StatService;
import com.creati.util.FontKit;
import com.creati.util.UITheme;
import dialog.GptResultDialog;
import com.creati.ui.components.RoundedLabel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import static com.creati.ui.main.MainUiParts.*;

import java.awt.*;
import java.util.function.Supplier;


public class MainHomeView extends JPanel {

	private static final Color YELLOW_DARK = UITheme.YELLOW_500;
	private static final Color YELLOW_MID = UITheme.YELLOW_300;
	private static final Color YELLOW_SOFT = UITheme.YELLOW_200;
	
	private javax.swing.JLabel kpiMonthLogValue;
	private javax.swing.JLabel kpiMonthCategoryValue;
	private javax.swing.JLabel kpiTopFieldValue;

	// sub 문구 라벨 참조 (데이터 유무에 따라 문구 교체용)
	private javax.swing.JLabel kpiMonthLogSub;
	private javax.swing.JLabel kpiMonthCategorySub;
	private javax.swing.JLabel kpiTopFieldSub;

	// 육각형 카드 내부 참조 (데이터 유무 분기용)
	private MainUiParts.RadarChart radarChart;
	private JComponent typeCardRef;
	private JLabel typeChipLabel;
	private JLabel typeDescLabel;
	private JPanel statsBodyRef;      // statsCard body 패널
	private JLabel statsEmptyLabel;   // 데이터 없을 때 안내 문구

	private final Supplier<String> insightGetter;
	private final java.util.function.Consumer<String> insightSetter;
	private final com.creati.dao.LogDao logDao = new com.creati.dao.LogDao();

	public MainHomeView(Supplier<String> insightGetter, java.util.function.Consumer<String> insightSetter) {
		this.insightGetter = insightGetter;
		this.insightSetter = insightSetter;

		UITheme.ensureInit();

		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(UITheme.BG);
		setBorder(new EmptyBorder(0, 18, 18, 18));

		add(buildHomeView(), BorderLayout.CENTER);
		
		addHierarchyListener(e -> {
		    if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
		        refreshHomeKpis();
		    }
		});
		
	}

	private JComponent buildHomeView() {
		JPanel board = new JPanel(new GridBagLayout());
		board.setOpaque(false);

		GridBagConstraints g = new GridBagConstraints();
		g.gridx = 0;
		g.gridy = 0;
		g.weightx = 1;
		g.fill = GridBagConstraints.HORIZONTAL;
		g.insets = new Insets(0, 0, 14, 0);

		JPanel kpiRow = new JPanel(new GridLayout(1, 3, 14, 0));
		kpiRow.setOpaque(false);
		kpiRow.add(kpiCard("이번 달 시도 로그", "0", "아직 이번 달 기록이 없어요.", YELLOW_DARK,
				l -> kpiMonthLogValue = l, s -> kpiMonthLogSub = s));
		kpiRow.add(kpiCard("이번 달 카테고리", "0", "아직 이번 달 기록이 없어요.", YELLOW_MID,
				l -> kpiMonthCategoryValue = l, s -> kpiMonthCategorySub = s));
		kpiRow.add(kpiCard("대표 분야", "-", "아직 이번 달 기록이 없어요.", YELLOW_SOFT,
				l -> kpiTopFieldValue = l, s -> kpiTopFieldSub = s));
		
		board.add(kpiRow, g);

		g.gridy++;
		g.weighty = 1;
		g.fill = GridBagConstraints.BOTH;
		g.insets = new Insets(0, 0, 0, 0);

		JPanel body = new JPanel(new GridLayout(1, 2, 16, 0));
		body.setOpaque(false);

		body.add(statsCard());
		body.add(monthlyAIInsightCard());

		board.add(body, g);

		return board;
	}
	
	private JComponent kpiCard(String title, String value, String sub, Color accent,
            java.util.function.Consumer<JLabel> valueLabelOut,
            java.util.function.Consumer<JLabel> subLabelOut) {
		JPanel card = new JPanel(new BorderLayout());
		card.setBackground(UITheme.WHITE);
		card.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UITheme.RGB_230_230_235, 1, true),
				new EmptyBorder(14, 14, 14, 14)));

		JPanel top = new JPanel(new BorderLayout());
		top.setOpaque(false);

		JLabel t = new JLabel(title);
		t.setFont(UITheme.CAPTION);
		t.setForeground(UITheme.RGB_120_120_120);

		JPanel dot = new JPanel();
		dot.setPreferredSize(new Dimension(10, 10));
		dot.setBackground(accent);
		dot.setOpaque(true);

		top.add(t, BorderLayout.WEST);
		top.add(dot, BorderLayout.EAST);

		JLabel v = new JLabel(value);
		v.setFont(UITheme.H2 != null ? UITheme.H2.deriveFont(22f) : UITheme.H2);
		v.setForeground(UITheme.TEXT);
		if (valueLabelOut != null) valueLabelOut.accept(v);

		JLabel s = new JLabel(sub);
		s.setFont(UITheme.CAPTION);
		s.setForeground(UITheme.RGB_140_140_140);
		if (subLabelOut != null) subLabelOut.accept(s);

		JPanel center = new JPanel();
		center.setOpaque(false);
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		center.add(Box.createVerticalStrut(6));
		center.add(v);
		center.add(Box.createVerticalStrut(6));
		center.add(s);

		card.add(top, BorderLayout.NORTH);
		card.add(center, BorderLayout.CENTER);
		return card;
	}
	
	private void refreshHomeKpis() {
	    com.creati.model.User u = AppState.get().getCurrentUser();
	    if (u == null || u.getId() == null || u.getId().isBlank()) return;

	    String userId = u.getId();

	    // Data Binding - KPI
	    int logCnt    = logDao.countMyLogsThisMonth(userId);
	    int catCnt    = logDao.countMyDistinctCategoriesThisMonth(userId);
	    String topField = logDao.findMyTopFieldThisMonth(userId);

	    // State Mapping: 카드1 - 이번 달 시도 로그
	    if (kpiMonthLogValue != null)
	        kpiMonthLogValue.setText(String.valueOf(logCnt));
	    if (kpiMonthLogSub != null)
	        kpiMonthLogSub.setText(logCnt == 0
	            ? "아직 이번 달 기록이 없어요."
	            : logCnt == 1 ? "이번 달 1번 시도했어요." : "기록이 쌓일수록 패턴이 보여요.");

	    // State Mapping: 카드2 - 이번 달 카테고리
	    if (kpiMonthCategoryValue != null)
	        kpiMonthCategoryValue.setText(String.valueOf(catCnt));
	    if (kpiMonthCategorySub != null)
	        kpiMonthCategorySub.setText(catCnt == 0
	            ? "아직 이번 달 기록이 없어요."
	            : "도전이 정리되고 있어요.");

	    // State Mapping: 카드3 - 대표 분야
	    boolean hasField = topField != null && !topField.isBlank() && !topField.equals("기타");
	    if (kpiTopFieldValue != null)
	        kpiTopFieldValue.setText(hasField ? topField : "-");
	    if (kpiTopFieldSub != null)
	        kpiTopFieldSub.setText(logCnt == 0
	            ? "아직 이번 달 기록이 없어요."
	            : "가장 많이 기록됨");

	    // Data Binding - 육각형 지표
	    int totalLogs = logDao.countMyLogsThisMonth(userId); // 전체 로그 기준 유무 체크
	    boolean hasAnyLog = totalLogs > 0;

	    // State Mapping: 유형 카드 - 로그 없으면 준비 중, 있으면 실제 유형
	    if (!hasAnyLog) {
	        if (typeChipLabel != null) typeChipLabel.setText("준비 중");
	        if (typeDescLabel != null) typeDescLabel.setText("첫 로그를 남기면 유형이 분석돼요!");
	        if (radarChart != null) radarChart.setScores(new int[]{0, 0, 0, 0, 0, 0});
	    }

	    if (hasAnyLog) {
	        // 각 축 점수 계산
	        int s1 = toScore_consistency(logDao.countLogsLast7Days(userId));
	        int s2 = toScore_challenge(logDao.countDistinctCategoriesAll(userId));
	        int s3 = toScore_communication(logDao.countReactionsOnMyLogsLast7Days(userId));
	        int s4 = toScore_execution(logDao.calcExecutionRateLast30Days(userId));
	        int s5 = toScore_recovery(logDao.countRecoveredLogs(userId));
	        int s6 = toScore_reflection(logDao.calcReflectionRate(userId));

	        // UI Update - 레이더 차트 점수 갱신
	        if (radarChart != null) {
	            radarChart.setScores(new int[]{s1, s2, s4, s5, s6, s3});
	        }

	        // State Mapping - 현재 유형 결정 (가장 높은 축 기준)
	        int[] scores = {s1, s2, s3, s4, s5, s6};
	        String[] chips = {"꾸준러형", "도전가형", "소통형", "실행가형", "리바운더형", "기록가형"};
	        String[] descs = {"매일매일 쌓는 타입", "새로운 걸 해봐야 직성이 풀림",
	                          "피드백으로 더 커지는 타입", "시작한 건 끝낸다",
	                          "넘어져도 다시 일어남", "정리하며 성장하는 타입"};
	        int maxIdx = 0;
	        for (int i = 1; i < scores.length; i++)
	            if (scores[i] > scores[maxIdx]) maxIdx = i;

	        if (typeChipLabel != null) typeChipLabel.setText(chips[maxIdx]);
	        if (typeDescLabel != null) typeDescLabel.setText(descs[maxIdx]);
	    }

	    // UI Update
	    if (kpiMonthLogValue   != null) kpiMonthLogValue.getParent().revalidate();
	    if (kpiMonthCategoryValue != null) kpiMonthCategoryValue.getParent().revalidate();
	    if (kpiTopFieldValue   != null) kpiTopFieldValue.getParent().revalidate();
	    if (statsBodyRef       != null) { statsBodyRef.revalidate(); statsBodyRef.repaint(); }
	}

	// ── 지표 점수 변환 (설계 스펙 그대로) ──────────────────────

	/** 꾸준함: 최근 7일 로그 수 → 1~3 */
	private int toScore_consistency(int cnt) {
	    if (cnt >= 4) return 3;
	    if (cnt >= 2) return 2;
	    return 1;
	}

	/** 도전력: 전체 카테고리 종류 수 → 1~3 */
	private int toScore_challenge(int cnt) {
	    if (cnt >= 8) return 3;
	    if (cnt >= 4) return 2;
	    return 1;
	}

	/** 소통력: 최근 7일 반응 수 → 1~3 */
	private int toScore_communication(int cnt) {
	    if (cnt >= 6) return 3;
	    if (cnt >= 1) return 2;
	    return 1;
	}

	/** 실행력: 최근 30일 SUCCESS 비율(%) → 1~3 */
	private int toScore_execution(int rate) {
	    if (rate > 60) return 3;
	    if (rate >= 30) return 2;
	    return 1;
	}

	/** 회복력: SUCCESS + retry_condition 있는 로그 수 → 1~3 */
	private int toScore_recovery(int cnt) {
	    if (cnt >= 4) return 3;
	    if (cnt >= 2) return 2;
	    return 1;
	}

	/** 성찰력: AI 분석 비율(%) → 1~3 */
	private int toScore_reflection(int rate) {
	    if (rate > 60) return 3;
	    if (rate >= 30) return 2;
	    return 1;
	}

	private JComponent kpiCard(String title, String value, String sub, Color accent) {
		JPanel card = new JPanel(new BorderLayout());
		card.setBackground(UITheme.WHITE);
		card.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UITheme.RGB_230_230_235, 1, true), new EmptyBorder(14, 14, 14, 14)));

		JPanel top = new JPanel(new BorderLayout());
		top.setOpaque(false);

		JLabel t = new JLabel(title);
		t.setFont(UITheme.CAPTION);
		t.setForeground(UITheme.RGB_120_120_120);

		JPanel dot = new JPanel();
		dot.setPreferredSize(new Dimension(10, 10));
		dot.setBackground(accent);
		dot.setOpaque(true);

		top.add(t, BorderLayout.WEST);
		top.add(dot, BorderLayout.EAST);

		JLabel v = new JLabel(value);
		v.setFont(UITheme.H2 != null ? UITheme.H2.deriveFont(22f) : UITheme.H2);
		v.setForeground(UITheme.TEXT);
		
		JLabel s = new JLabel(sub);
		s.setFont(UITheme.CAPTION);
		s.setForeground(UITheme.RGB_140_140_140);

		JPanel center = new JPanel();
		center.setOpaque(false);
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		center.add(Box.createVerticalStrut(6));
		center.add(v);
		center.add(Box.createVerticalStrut(6));
		center.add(s);

		card.add(top, BorderLayout.NORTH);
		card.add(center, BorderLayout.CENTER);

		return card;
	}

	private JComponent statsCard() {
	    HomeCard card = new HomeCard("나의 성장 상태");


		JPanel body = new JPanel();
		body.setOpaque(false);
		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
		statsBodyRef = body;

		JLabel hint = new JLabel("이번 달의 성장 흐름을 한눈에 확인해요.");
		hint.setFont(UITheme.CAPTION);
		hint.setForeground(UITheme.RGB_140_140_140);
		hint.setAlignmentX(Component.LEFT_ALIGNMENT);

		// 현재 유형 카드 (항상 표시, 초기값: 준비 중)
		JComponent typeCard = buildTypeCard("준비 중", "첫 로그를 남기면 유형이 분석돼요!");
		typeCard.setAlignmentX(Component.LEFT_ALIGNMENT);
		typeCardRef = typeCard;

		// 육각형 그래프 (항상 표시, 데이터 없으면 점수 1로 고정)
		String[] axes = {"꾸준함", "도전력", "실행력", "회복력", "성찰력", "소통력"};
		int[] scores = {0, 0, 0, 0, 0, 0};
		radarChart = new MainUiParts.RadarChart(axes, scores);
		radarChart.setAlignmentX(Component.LEFT_ALIGNMENT);

		body.add(hint);
		body.add(Box.createVerticalStrut(10));
		body.add(typeCard);
		body.add(Box.createVerticalStrut(10));
		body.add(radarChart);

	    card.setBody(body);
	    return card;
	}

	private JComponent buildTypeCard(String typeChip, String desc) {
		JPanel card = new JPanel(new BorderLayout());
		card.setOpaque(true);
		card.setBackground(UITheme.RGB_245_245_248);
		card.setBorder(new EmptyBorder(8, 12, 8, 12));

		JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		row.setOpaque(false);

		JLabel title = new JLabel("현재 유형");
		title.setFont(UITheme.BODY_MED);
		title.setForeground(UITheme.TEXT);

		JLabel bar = new JLabel("|");
		bar.setFont(UITheme.BODY);
		bar.setForeground(UITheme.RGB_170_170_170);

		JLabel chip = new RoundedLabel(typeChip).arc(18).bg(UITheme.WHITE).border(UITheme.ACCENT_LAVENDER_BORDER);
		chip.setFont(UITheme.BODY_MED);
		chip.setForeground(UITheme.ACCENT_PURPLE);
		typeChipLabel = chip;

		JLabel descLabel = new JLabel(desc);
		descLabel.setFont(UITheme.BODY);
		descLabel.setForeground(UITheme.RGB_120_120_120);
		typeDescLabel = descLabel;

		row.add(title);
		row.add(bar);
		row.add(chip);
		row.add(descLabel);

		card.add(row, BorderLayout.WEST);
		return card;
	}

	private JComponent kvLine(String k, String v) {
		JPanel row = new JPanel(new BorderLayout());
		row.setOpaque(false);

		JLabel key = new JLabel(k);
		key.setFont(UITheme.CAPTION);
		key.setForeground(UITheme.RGB_140_140_140);

		JLabel val = new JLabel(v);
		val.setFont(UITheme.BODY_MED);
		val.setForeground(UITheme.TEXT);

		row.add(key, BorderLayout.WEST);
		row.add(val, BorderLayout.EAST);
		return row;
	}

	private JComponent buildStrengthWeakness(String strength, String weakness) {
		JPanel p = new JPanel();
		p.setOpaque(false);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

		p.add(simpleLine("강점", strength));
		p.add(Box.createVerticalStrut(6));
		p.add(simpleLine("보완", weakness));

		return p;
	}

	private JComponent simpleLine(String label, String value) {
		JPanel row = new JPanel(new BorderLayout());
		row.setOpaque(false);

		JLabel l = new JLabel(label);
		l.setFont(UITheme.CAPTION);
		l.setForeground(UITheme.RGB_140_140_140);

		JLabel v = new JLabel(value);
		v.setFont(UITheme.BODY_MED);
		v.setForeground(UITheme.TEXT);

		row.add(l, BorderLayout.WEST);
		row.add(v, BorderLayout.EAST);
		return row;
	}

	private JComponent monthlyAIInsightCard() {
		HomeCard card = new HomeCard("월간 AI 인사이트");

		JPanel body = new JPanel();
		body.setOpaque(false);
		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

		JLabel hint = new JLabel("<html><div style='line-height:1.5; text-align:left;'>" + "이번 달 기록을 요약해 다음 달 집중 포인트를 받아볼 수 있어요." + "</div></html>");
		hint.setFont(UITheme.CAPTION.deriveFont(12f));
		hint.setForeground(UITheme.RGB_120_120_120);
		hint.setAlignmentX(Component.LEFT_ALIGNMENT);

		JTextArea insightArea = new JTextArea();
		insightArea.setEditable(false);
		insightArea.setOpaque(false);
		insightArea.setLineWrap(true);
		insightArea.setWrapStyleWord(true);
		insightArea.setFont(UITheme.BODY);
		insightArea.setForeground(UITheme.TEXT);
		insightArea.setBorder(new EmptyBorder(4, 4, 4, 4));

		JPanel insightBox = new JPanel(new BorderLayout());
		insightBox.setOpaque(true);
		insightBox.setBackground(UITheme.RGB_245_245_248);
		insightBox.setBorder(new EmptyBorder(12, 12, 12, 12));
		insightBox.setAlignmentX(Component.LEFT_ALIGNMENT);

		JScrollPane scroll = new JScrollPane(insightArea);
		scroll.setBorder(null);
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.getVerticalScrollBar().setUnitIncrement(16);

		insightBox.setPreferredSize(new Dimension(10, 220));
		insightBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
		insightBox.add(scroll, BorderLayout.CENTER);

		applyInsightText(insightArea);

		JButton genBtn = new JButton("월간 인사이트 생성");
		genBtn.setFocusPainted(false);
		genBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		genBtn.setBackground(UITheme.ACCENT_PURPLE);
		genBtn.setForeground(UITheme.WHITE);
		genBtn.setBorder(new EmptyBorder(10, 14, 10, 14));
		genBtn.setFont(UITheme.BODY_MED);
		genBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

		genBtn.addActionListener(e -> {

			com.creati.model.User user = AppState.get().getCurrentUser();
			if (user == null) return;

			if (user != null && logDao.countMyLogsThisMonth(user.getId()) == 0) {
				JOptionPane.showMessageDialog(
					this,
					"이번 달에 작성한 로그가 없어요.\n로그를 먼저 남기면 더 정확한 인사이트를 받을 수 있어요!",
					"인사이트 생성 불가",
					JOptionPane.INFORMATION_MESSAGE
				);
				return;
			}


			genBtn.setEnabled(false);
			genBtn.setText("생성 중...");
			insightArea.setText("AI가 이번 달 기록을 분석하고 있어요...");
			insightArea.setForeground(UITheme.RGB_120_120_120);

			new Thread(() -> {
				String result;
				try {
					GptAnalysisService svc = new GptAnalysisService();
					result = svc.analyzeMonthlyInsight(user.getId());
				} catch (Exception ex) {
					ex.printStackTrace();
					result = "인사이트 생성 중 오류가 발생했어요.잠시 후 다시 시도해주세요.";
				}
				final String finalResult = result;
				SwingUtilities.invokeLater(() -> {
					insightSetter.accept(finalResult);
					applyInsightText(insightArea);
					insightArea.setCaretPosition(0);
					genBtn.setEnabled(true);
					genBtn.setText("월간 인사이트 생성");
				});
			}).start();
		});

		body.add(hint);
		body.add(Box.createVerticalStrut(10));
		body.add(insightBox);
		body.add(Box.createVerticalStrut(12));
		body.add(genBtn);
		body.add(Box.createVerticalGlue());

		card.setBody(body);
		return card;
	}

	private void applyInsightText(JTextArea area) {
		String t = insightGetter.get();
		if (t == null || t.isBlank()) {
			area.setText("아직 인사이트가 없어요.\n월간 인사이트 생성 버튼을 눌러 생성해보세요.");
			area.setForeground(UITheme.RGB_80_80_90);
		} else {
			area.setText(t);
			area.setForeground(UITheme.TEXT);
		}
	}

	private void attachHeight(JComponent c, int h) {
		c.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
		c.setPreferredSize(new Dimension(10, h));
		c.setMinimumSize(new Dimension(10, h));
	}

	private JComponent pill(String label, String value) {
		JPanel p = new JPanel(new BorderLayout(10, 0));
		p.setOpaque(true);
		p.setBackground(UITheme.RGB_250_250_252);
		p.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UITheme.RGB_235_235_242, 1, true), new EmptyBorder(10, 12, 10, 12)));

		JLabel l = new JLabel(label);
		l.setFont(UITheme.CAPTION);
		l.setForeground(UITheme.RGB_120_120_120);

		JLabel v = new JLabel(value);
		v.setFont(UITheme.BODY_MED);
		v.setForeground(UITheme.TEXT);

		p.add(l, BorderLayout.WEST);
		p.add(v, BorderLayout.EAST);
		return p;
	}

}
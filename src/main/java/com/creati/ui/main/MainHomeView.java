package com.creati.ui.main;

import com.creati.ui.main.MainUiParts.ChartCard;
import com.creati.ui.main.MainUiParts.HomeCard;
import com.creati.ui.main.MainUiParts.MiniBarChart;
import com.creati.ui.main.MainUiParts.MiniLineChart;
import com.creati.util.FontKit;
import com.creati.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import static com.creati.ui.main.MainUiParts.*;

import java.awt.*;
import java.util.function.Supplier;

/**
 * HOME 화면만 담당 - KPI - 나의 성장 상태 - 월간 AI 인사이트
 */
public class MainHomeView extends JPanel {

	private static final Color YELLOW_DARK = new Color(0xFFC107);
	private static final Color YELLOW_MID = new Color(0xFFD54F);
	private static final Color YELLOW_SOFT = new Color(0xFFE082);

	private final Supplier<String> insightGetter;
	private final java.util.function.Consumer<String> insightSetter;

	public MainHomeView(Supplier<String> insightGetter, java.util.function.Consumer<String> insightSetter) {
		this.insightGetter = insightGetter;
		this.insightSetter = insightSetter;

		UITheme.ensureInit();

		setLayout(new BorderLayout());
		setOpaque(true);
		setBackground(UITheme.BG);
		setBorder(new EmptyBorder(0, 18, 18, 18));

		add(buildHomeView(), BorderLayout.CENTER);
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
		kpiRow.add(kpiCard("이번 달 시도 로그", "12", "꾸준히 쌓는 중", YELLOW_DARK));
		kpiRow.add(kpiCard("이번 달 폴더", "4", "도전이 정리되고 있어요", YELLOW_MID));
		kpiRow.add(kpiCard("대표 분야", "영상", "가장 많이 기록됨", YELLOW_SOFT));

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

	private JComponent kpiCard(String title, String value, String sub, Color accent) {
		JPanel card = new JPanel(new BorderLayout());
		card.setBackground(Color.WHITE);
		card.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(230, 230, 235), 1, true), new EmptyBorder(14, 14, 14, 14)));

		JPanel top = new JPanel(new BorderLayout());
		top.setOpaque(false);

		JLabel t = new JLabel(title);
		t.setFont(UITheme.CAPTION);
		t.setForeground(new Color(120, 120, 120));

		JPanel dot = new JPanel();
		dot.setPreferredSize(new Dimension(10, 10));
		dot.setBackground(accent);
		dot.setOpaque(true);

		top.add(t, BorderLayout.WEST);
		top.add(dot, BorderLayout.EAST);

		JLabel v = new JLabel(value);
		v.setFont(UITheme.H2 != null ? UITheme.H2.deriveFont(22f) : new Font("Dialog", Font.BOLD, 22));
		v.setForeground(UITheme.TEXT);

		JLabel s = new JLabel(sub);
		s.setFont(UITheme.CAPTION);
		s.setForeground(new Color(140, 140, 140));

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

		// 값(임시)
		String typeChip = "꾸준러형";
		String typeDesc = "매일매일 쌓는 타입";

		// 설명 멘트
		JLabel hint = new JLabel("이번 달의 성장 흐름을 한눈에 확인해요.");
		hint.setFont(UITheme.CAPTION);
		hint.setForeground(new Color(140, 140, 140));
		hint.setAlignmentX(Component.LEFT_ALIGNMENT);

		// 현재 유형 카드
		JComponent typeCard = buildTypeCard(typeChip, typeDesc);
		typeCard.setAlignmentX(Component.LEFT_ALIGNMENT);

		// 레이더 그래프
		String[] axes = { "꾸준함", "도전력", "실행력", "회복력", "성찰력", "소통력" };
		int[] scores = { 2, 2, 1, 3, 2, 1 };
		JComponent radar = new MainUiParts.RadarChart(axes, scores);
		radar.setAlignmentX(Component.LEFT_ALIGNMENT);

		body.add(hint);
		body.add(Box.createVerticalStrut(10));
		body.add(typeCard);
		body.add(Box.createVerticalStrut(10));
		body.add(radar);

		card.setBody(body);
		return card;
	}

	private JComponent buildTypeCard(String typeChip, String desc) {
		JPanel card = new JPanel(new BorderLayout());
		card.setOpaque(true);
		card.setBackground(new Color(245, 245, 248));

		card.setBorder(new EmptyBorder(8, 12, 8, 12));

		JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		row.setOpaque(false);

		JLabel title = new JLabel("현재 유형");
		title.setFont(UITheme.BODY_MED);
		title.setForeground(UITheme.TEXT);

		JLabel bar = new JLabel("|");
		bar.setFont(UITheme.BODY);
		bar.setForeground(new Color(170, 170, 170));

		JLabel chip = new MainUiParts.RoundedLabel(typeChip).arc(18).bg(Color.WHITE).border(new Color(0xCFC9FF));

		chip.setFont(UITheme.BODY_MED);
		chip.setForeground(UITheme.ACCENT_PURPLE);

		JLabel descLabel = new JLabel(desc);
		descLabel.setFont(UITheme.BODY);
		descLabel.setForeground(new Color(120, 120, 120));

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
		key.setForeground(new Color(140, 140, 140));

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
		l.setForeground(new Color(140, 140, 140));

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

		JLabel hint = new JLabel("<html><div style='line-height:1.5; text-align:left;'>" + "매달 1회, 이번 달 기록을 요약해 다음 달 집중 포인트를 받아볼 수 있어요." + "</div></html>");
		hint.setFont(UITheme.CAPTION.deriveFont(12f));
		hint.setForeground(new Color(120, 120, 120));
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
		insightBox.setBackground(new Color(245, 245, 248));
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
		genBtn.setForeground(Color.WHITE);
		genBtn.setBorder(new EmptyBorder(10, 14, 10, 14));
		genBtn.setFont(UITheme.BODY_MED);
		genBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

		genBtn.addActionListener(e -> {
			// TODO (기능 연결): 월 1회 생성 제한 체크
			// TODO (기능 연결): 매월 1일 초기화 로직(서버/DB 연결)

			String text = "이번 달은 기록의 시작은 빠르지만, 중간에 흐름이 끊기는 패턴이 보여요. "
					+ "특히 ‘시간 부족’과 ‘계획 미흡’이 함께 등장하면서 재도전까지 이어지지 못한 날이 있었어요.\n\n"
					+ "다음 달에는 목표를 크게 바꾸기보다, ‘기록 시간을 고정’하는 한 가지에만 집중해보면 좋아요. "
					+ "예를 들면 하루 중 가장 부담이 덜한 시간(점심 직후/저녁 샤워 전 등)을 정하고, " + "그때는 ‘한 줄만’ 남기는 방식으로 시작해보는 걸 추천해요.\n\n"
					+ "핵심은 ‘완벽’이 아니라 ‘지속’이에요. 작은 성공을 매일 하나씩 쌓아보자구요.";

			insightSetter.accept(text);
			applyInsightText(insightArea);
			insightArea.setCaretPosition(0);
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
			area.setForeground(new Color(80, 80, 90));
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
		p.setBackground(new Color(250, 250, 252));
		p.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(235, 235, 242), 1, true), new EmptyBorder(10, 12, 10, 12)));

		JLabel l = new JLabel(label);
		l.setFont(UITheme.CAPTION);
		l.setForeground(new Color(120, 120, 120));

		JLabel v = new JLabel(value);
		v.setFont(UITheme.BODY_MED);
		v.setForeground(UITheme.TEXT);

		p.add(l, BorderLayout.WEST);
		p.add(v, BorderLayout.EAST);
		return p;
	}

}

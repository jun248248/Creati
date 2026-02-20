package com.creati.ui.main;

import com.creati.util.FontKit;
import com.creati.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

/**
 * 통계 탭 화면
 * - 상단 요약(이번 주 상태/유형/강점/보완)
 * - 육각형 레이더 그래프(6축)
 * - 실행 제안(체크 아이콘: Material Icons 폰트 사용)
 *
 * TODO(DB 연결 후):
 *  - scores(6축) 계산 로직 연결
 *  - 유형(type) 자동 산정(Top1 기준)
 *  - 강점/보완 TOP1 자동 추출
 */
public class StatsView extends JPanel {

    // 6축 라벨
    private static final String[] AXES = {"꾸준함", "도전력", "실행력", "회복력", "성찰력", "소통력"};

    // 임시 점수(1~3): 하/중/상
    // TODO (DB 연결) 실제로는 최근 7일/30일 기준으로 계산해서 주입
    private int[] scores = {2, 2, 1, 3, 2, 1};

    private JLabel weekStateLabel;
    private JLabel typeLabel;
    private JLabel strengthLabel;
    private JLabel weaknessLabel;

    private RadarChart radarChart;

    public StatsView() {
        UITheme.ensureInit();
        FontKit.init();

        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(UITheme.BG);
        setBorder(new EmptyBorder(14, 18, 18, 18));

        add(buildTopSummary(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    private JComponent buildTopSummary() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 235), 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        weekStateLabel = new JLabel();
        weekStateLabel.setFont(UITheme.BODY_MED);
        weekStateLabel.setForeground(UITheme.TEXT);

        typeLabel = new JLabel();
        typeLabel.setFont(UITheme.CAPTION);
        typeLabel.setForeground(new Color(120, 120, 120));

        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false);

        strengthLabel = pillLabel("강점 TOP1", "");
        weaknessLabel = pillLabel("보완 TOP1", "");

        row.add(strengthLabel);
        row.add(weaknessLabel);

        card.add(weekStateLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(typeLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(row);

        wrap.add(card, BorderLayout.CENTER);

        // 초기 값 세팅
        refreshSummary();

        return wrap;
    }

    private JLabel pillLabel(String title, String value) {
        JLabel l = new JLabel(title + " : " + value);
        l.setFont(UITheme.CAPTION);
        l.setForeground(new Color(110, 110, 110));
        l.setOpaque(true);
        l.setBackground(new Color(245, 245, 248));
        l.setBorder(new EmptyBorder(10, 12, 10, 12));
        return l;
    }

    private JComponent buildBody() {
        JPanel body = new JPanel(new GridLayout(1, 2, 16, 0));
        body.setOpaque(false);

        // 왼쪽: 레이더 그래프 카드
        body.add(buildRadarCard());

        // 오른쪽: 실행 제안 카드
        body.add(buildActionCard());

        return body;
    }

    private JComponent buildRadarCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 235), 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel title = new JLabel("나의 성장 상태");
        title.setFont(UITheme.BODY_MED);
        title.setForeground(UITheme.TEXT);

        JLabel hint = new JLabel("하/중/상(1~3단계) 기준으로 이번 주 상태를 보여줘요.");
        hint.setFont(UITheme.CAPTION);
        hint.setForeground(new Color(140, 140, 140));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.add(title);
        head.add(Box.createVerticalStrut(4));
        head.add(hint);

        radarChart = new RadarChart(AXES, scores);

        card.add(head, BorderLayout.NORTH);
        card.add(radarChart, BorderLayout.CENTER);

        return card;
    }

    private JComponent buildActionCard() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 235), 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("이번 주 실행 제안");
        title.setFont(UITheme.BODY_MED);
        title.setForeground(UITheme.TEXT);

        JLabel hint = new JLabel("통계는 평가가 아니라, 다음 행동을 돕기 위한 피드백이에요.");
        hint.setFont(UITheme.CAPTION);
        hint.setForeground(new Color(140, 140, 140));

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(hint);
        card.add(Box.createVerticalStrut(12));

        // 체크 아이콘 + 문장 (Material Icons 폰트 사용)
        card.add(checkItem("작게 시작해서 완수 경험 만들기"));
        card.add(Box.createVerticalStrut(8));
        card.add(checkItem("실패 로그를 3줄 회고로 정리하기"));
        card.add(Box.createVerticalStrut(8));
        card.add(checkItem("커뮤니티에 질문 1개 올리기"));

        card.add(Box.createVerticalGlue());
        return card;
    }

    private JComponent checkItem(String text) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel icon = new JLabel();
        icon.setFont(FontKit.materialIcon(18f));
        icon.setForeground(UITheme.ACCENT_PURPLE);
        icon.setText(new String(Character.toChars(0xE86C))); // check_circle

        JLabel label = new JLabel(text);
        label.setFont(UITheme.BODY);
        label.setForeground(UITheme.TEXT);

        row.add(icon, BorderLayout.WEST);
        row.add(label, BorderLayout.CENTER);
        return row;
    }

    // Summary 계산(임시)
    private void refreshSummary() {
        // Top1 / Bottom1 추출 (동점은 첫 번째)
        int maxIdx = 0, minIdx = 0;
        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > scores[maxIdx]) maxIdx = i;
            if (scores[i] < scores[minIdx]) minIdx = i;
        }

        String strength = AXES[maxIdx];
        String weakness = AXES[minIdx];

        String type = getTypeByTopAxis(strength);

        // 이번 주 상태(단순 평균 기반)
        double avg = Arrays.stream(scores).average().orElse(2.0);
        String level = (avg < 1.7) ? "하" : (avg < 2.4) ? "중" : "상";

        weekStateLabel.setText("이번 주 상태: " + level);
        typeLabel.setText("현재 유형: " + type);

        strengthLabel.setText("강점 TOP1 : " + strength);
        weaknessLabel.setText("보완 TOP1 : " + weakness);
    }

    private String getTypeByTopAxis(String topAxis) {
        return switch (topAxis) {
            case "꾸준함" -> "꾸준러형 (매일매일 쌓는 타입)";
            case "도전력" -> "도전가형 (새로운 걸 시도하는 타입)";
            case "실행력" -> "실행가형 (시작한 걸 해내는 타입)";
            case "회복력" -> "리바운더형 (재도전에 강한 타입)";
            case "성찰력" -> "기록가형 (정리하며 성장하는 타입)";
            case "소통력" -> "소통형 (피드백으로 커지는 타입)";
            default -> "균형형";
        };
    }

    // Radar Chart Component
    static class RadarChart extends JComponent {
        private final String[] axes;
        private final int[] scores; // 1~3
        RadarChart(String[] axes, int[] scores) {
            this.axes = axes;
            this.scores = scores;
            setOpaque(false);
            setPreferredSize(new Dimension(10, 360));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            int pad = 36;
            int cx = w / 2;
            int cy = h / 2 + 6;
            int r = Math.min(w, h) / 2 - pad;

            // 3단계 그리드(하/중/상)
            g2.setColor(new Color(235, 235, 242));
            for (int level = 1; level <= 3; level++) {
                double rr = r * (level / 3.0);
                drawPolygon(g2, cx, cy, rr, axes.length);
            }

            // 축 선
            g2.setColor(new Color(228, 228, 238));
            for (int i = 0; i < axes.length; i++) {
                double ang = -Math.PI / 2 + i * (2 * Math.PI / axes.length);
                int x = (int) (cx + r * Math.cos(ang));
                int y = (int) (cy + r * Math.sin(ang));
                g2.drawLine(cx, cy, x, y);
            }

            // 데이터 폴리곤
            Polygon poly = new Polygon();
            for (int i = 0; i < axes.length; i++) {
                double ang = -Math.PI / 2 + i * (2 * Math.PI / axes.length);
                double rr = r * (scores[i] / 3.0);
                int x = (int) (cx + rr * Math.cos(ang));
                int y = (int) (cy + rr * Math.sin(ang));
                poly.addPoint(x, y);
            }

            // 채우기 + 테두리
            g2.setColor(new Color(0xEAE6FF));
            g2.fillPolygon(poly);
            g2.setColor(UITheme.ACCENT_PURPLE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawPolygon(poly);

            // 라벨
            g2.setFont(UITheme.CAPTION);
            g2.setColor(new Color(110, 110, 110));
            for (int i = 0; i < axes.length; i++) {
                double ang = -Math.PI / 2 + i * (2 * Math.PI / axes.length);
                int x = (int) (cx + (r + 16) * Math.cos(ang));
                int y = (int) (cy + (r + 16) * Math.sin(ang));
                drawCentered(g2, axes[i], x, y);
            }

            g2.dispose();
        }

        private void drawPolygon(Graphics2D g2, int cx, int cy, double r, int n) {
            Polygon p = new Polygon();
            for (int i = 0; i < n; i++) {
                double ang = -Math.PI / 2 + i * (2 * Math.PI / n);
                int x = (int) (cx + r * Math.cos(ang));
                int y = (int) (cy + r * Math.sin(ang));
                p.addPoint(x, y);
            }
            g2.drawPolygon(p);
        }

        private void drawCentered(Graphics2D g2, String s, int x, int y) {
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(s);
            int th = fm.getAscent();
            g2.drawString(s, x - tw / 2, y + th / 2);
        }
    }
}

package com.creati.ui.main;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.creati.util.UITheme;

import com.creati.ui.components.CircleAvatar;
import com.creati.ui.components.EllipsisButton;
import com.creati.ui.components.RoundedButton;
import com.creati.ui.components.RoundedLabel;
import com.creati.ui.components.ShadowLabel;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;
import java.io.InputStream;
import java.nio.file.Path;


public class MainUiParts {

	public static final Color LAVENDER_HOVER = UITheme.ACCENT_LAVENDER_BG_2;
	public static final Color LAVENDER_BORDER = UITheme.ACCENT_LAVENDER_BORDER;

	
	
	
	public static final Color ROW_BG = UITheme.WHITE;
	public static final Color ROW_HOVER_BG = UITheme.SURFACE_TINT;
	public static final Color ROW_SELECTED_BG = UITheme.ACCENT_LAVENDER_BG;

	
	
	
	public static final int TOGGLE_CHIP_RADIUS = 18;

	public static Color toggleChipBg(boolean selected, boolean hover) {
		if (selected) return UITheme.TOGGLE_CHIP_SELECTED_BG;
		if (hover) return UITheme.TOGGLE_CHIP_HOVER_BG;
		return UITheme.TOGGLE_CHIP_BG;
	}

	public static Color toggleChipBorder(boolean selected, boolean hover) {
		if (selected) return UITheme.TOGGLE_CHIP_SELECTED_BORDER;
		return UITheme.TOGGLE_CHIP_BORDER;
	}

	
	public static Icon glyphIcon(int codePoint, float size, Color color) {
		return new GlyphIcon(codePoint, size, color);
	}

	
	public static class GlyphIcon implements Icon {
		private final int codePoint;
		private final float size;
		private final Color color;

		public GlyphIcon(int codePoint, float size, Color color) {
			this.codePoint = codePoint;
			this.size = size;
			this.color = color;
		}

		@Override
		public int getIconWidth() {
			return Math.round(size);
		}

		@Override
		public int getIconHeight() {
			return Math.round(size);
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setColor(color);
			g2.setFont(com.creati.util.FontKit.materialIcon(size));
			String s = new String(Character.toChars(codePoint));
			FontMetrics fm = g2.getFontMetrics();
			int by = y + fm.getAscent();
			g2.drawString(s, x, by);
			g2.dispose();
		}
	}

	
	public static void applyRowStateBackground(JComponent c, boolean isSelected, boolean isHover) {
		if (c == null) return;
		if (isSelected) c.setBackground(ROW_SELECTED_BG);
		else if (isHover) c.setBackground(ROW_HOVER_BG);
		else c.setBackground(ROW_BG);
	}

	
	
	

	public static class HomeCard extends JPanel {
		private final JPanel bodyWrap;

		public HomeCard(String title) {
			super(new BorderLayout());
			setBackground(UITheme.WHITE);
			setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(UITheme.RGB_230_230_235, 1, true),
					new EmptyBorder(16, 16, 16, 16)));

			JLabel t = new JLabel(title);
			t.setFont(UITheme.BODY_MED);
			t.setForeground(UITheme.TEXT);

			JPanel head = new JPanel(new BorderLayout());
			head.setOpaque(false);
			head.add(t, BorderLayout.WEST);

			bodyWrap = new JPanel(new BorderLayout());
			bodyWrap.setOpaque(false);
			bodyWrap.setBorder(new EmptyBorder(12, 0, 8, 0));

			add(head, BorderLayout.NORTH);
			add(bodyWrap, BorderLayout.CENTER);
		}

		public void setBody(JComponent body) {
			bodyWrap.removeAll();
			bodyWrap.add(body, BorderLayout.CENTER);
			bodyWrap.revalidate();
			bodyWrap.repaint();
		}
	}

	
	
	
	public static BufferedImage loadBufferedResource(String resourcePath) {
		try (InputStream is = MainUiParts.class.getResourceAsStream(resourcePath)) {
			if (is == null) {
				System.out.println("[RESOURCE NOT FOUND] " + resourcePath);
				return null;
			}
			return ImageIO.read(is);
		} catch (Exception e) {
			System.out.println("[RESOURCE LOAD FAIL] " + resourcePath);
			e.printStackTrace();
			return null;
		}
	}

	public static Image loadImageResource(String resourcePath) {
		return loadBufferedResource(resourcePath);
	}

	
	
	

	static class ChartCard extends JPanel {
		private final JPanel chartHolder = new JPanel(new BorderLayout());
		private final JLabel hint = new JLabel(" ");

		ChartCard(String title) {
			super(new BorderLayout());
			setOpaque(true);
			setBackground(UITheme.RGB_250_250_252);
			setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(UITheme.RGB_235_235_242, 1, true),
					new EmptyBorder(12, 12, 12, 12)));

			JLabel t = new JLabel(title);
			t.setFont(UITheme.BODY_MED);
			t.setForeground(UITheme.TEXT);

			hint.setFont(UITheme.CAPTION);
			hint.setForeground(UITheme.RGB_140_140_140);

			JPanel header = new JPanel(new BorderLayout(10, 0));
			header.setOpaque(false);
			header.add(t, BorderLayout.WEST);
			header.add(hint, BorderLayout.EAST);

			chartHolder.setOpaque(false);
			chartHolder.setBorder(new EmptyBorder(10, 0, 0, 0));

			add(header, BorderLayout.NORTH);
			add(chartHolder, BorderLayout.CENTER);

			setPreferredSize(new Dimension(10, 260));
		}

		void setChart(JComponent chart) {
			chartHolder.removeAll();
			chartHolder.add(chart, BorderLayout.CENTER);
		}

		void setHint(String text) {
			hint.setText(text);
		}
	}

	public static class MiniLineChart extends JComponent {
		private final int[] data;

		public MiniLineChart(int[] data) {
			this.data = data;
			setOpaque(false);
			setPreferredSize(new Dimension(10, 120));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int w = getWidth();
			int h = getHeight();

			g2.setColor(UITheme.WHITE);
			g2.fillRoundRect(0, 0, w, h, 16, 16);
			g2.setColor(UITheme.RGB_230_230_238);
			g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);

			int pad = 14;
			int gx0 = pad, gy0 = pad, gx1 = w - pad, gy1 = h - pad;

			g2.setColor(UITheme.RGB_242_242_248);
			for (int i = 1; i <= 3; i++) {
				int y = gy0 + (gy1 - gy0) * i / 4;
				g2.drawLine(gx0, y, gx1, y);
			}

			int max = 1;
			for (int v : data)
				max = Math.max(max, v);

			int n = data.length;
			int[] xs = new int[n];
			int[] ys = new int[n];

			for (int i = 0; i < n; i++) {
				double tx = (n == 1) ? 0 : (double) i / (n - 1);
				xs[i] = (int) (gx0 + (gx1 - gx0) * tx);

				double ty = (double) data[i] / max;
				ys[i] = (int) (gy1 - (gy1 - gy0) * ty);
			}

			g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g2.setColor(UITheme.ACCENT_PURPLE);
			for (int i = 0; i < n - 1; i++) {
				g2.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
			}

			g2.setColor(UITheme.WHITE);
			for (int i = 0; i < n; i++) {
				g2.fillOval(xs[i] - 5, ys[i] - 5, 10, 10);
			}
			g2.setColor(UITheme.ACCENT_PURPLE);
			for (int i = 0; i < n; i++) {
				g2.drawOval(xs[i] - 5, ys[i] - 5, 10, 10);
			}

			g2.dispose();
		}
	}

	public static class MiniBarChart extends JComponent {
		private final String[] labels;
		private final int[] values;

		public MiniBarChart(String[] labels, int[] values) {
			this.labels = labels;
			this.values = values;
			setOpaque(false);
			setPreferredSize(new Dimension(10, 120));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int w = getWidth();
			int h = getHeight();

			g2.setColor(UITheme.WHITE);
			g2.fillRoundRect(0, 0, w, h, 16, 16);
			g2.setColor(UITheme.RGB_230_230_238);
			g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);

			int padX = 14;
			int padTop = 14;
			int padBottom = 24;

			int gx0 = padX;
			int gx1 = w - padX;
			int gy0 = padTop;
			int gy1 = h - padBottom;

			int max = 1;
			for (int v : values)
				max = Math.max(max, v);

			int n = values.length;
			int gap = 10;
			int barW = Math.max(12, (gx1 - gx0 - gap * (n - 1)) / n);

			int x = gx0;
			for (int i = 0; i < n; i++) {
				int v = values[i];
				int bh = (int) ((gy1 - gy0) * (v / (double) max));
				int y = gy1 - bh;

				Color fill = (i % 2 == 0) ? UITheme.ACCENT_PURPLE : UITheme.ACCENT_LAVENDER_BORDER;
				g2.setColor(fill);
				g2.fillRoundRect(x, y, barW, bh, 10, 10);

				g2.setColor(UITheme.RGB_120_120_120);
				g2.setFont(UITheme.CAPTION);
				String lab = labels[i];
				int tw = g2.getFontMetrics().stringWidth(lab);
				int lx = x + (barW - tw) / 2;
				g2.drawString(lab, lx, h - 8);

				x += barW + gap;
			}

			g2.dispose();
		}
	}

	
	
	





	
	
	

	
	private static Icon createHiDPIIcon(Path srcPath, int logicalSizePx, boolean trim) {
		BufferedImage src = loadBuffered(srcPath);
		if (src == null)
			return null;

		if (trim)
			src = trimTransparent(src);

		BufferedImage img1x = scaleFitHQ(src, logicalSizePx);
		BufferedImage img2x = scaleFitHQ(src, logicalSizePx * 2);

		Image mri = new BaseMultiResolutionImage(img1x, img2x);
		return new ImageIcon(mri);
	}

	public static Icon createHiDPIIconResource(String resourcePath, int logicalSizePx, boolean trim) {
		BufferedImage src = loadBufferedResource(resourcePath);
		if (src == null)
			return null;

		if (trim)
			src = trimTransparent(src);

		BufferedImage img1x = scaleFitHQ(src, logicalSizePx);
		BufferedImage img2x = scaleFitHQ(src, logicalSizePx * 2);

		Image mri = new BaseMultiResolutionImage(img1x, img2x);
		return new ImageIcon(mri);
	}

	public static BufferedImage scaleFitHQ(BufferedImage src, int target) {
		int sw = src.getWidth();
		int sh = src.getHeight();

		double scale = (double) target / Math.max(sw, sh);
		int w = (int) Math.round(sw * scale);
		int h = (int) Math.round(sh * scale);

		BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = dst.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.drawImage(src, 0, 0, w, h, null);
		g2.dispose();
		return dst;
	}

	
	private static BufferedImage loadBuffered(Path path) {
		try {
			java.io.File f = path.toFile();
			if (!f.exists())
				return null;
			return ImageIO.read(f);
		} catch (Exception e) {
			return null;
		}
	}

	
	private static Image loadImage(Path path) {
		return loadBuffered(path);
	}

	public static BufferedImage trimTransparent(BufferedImage src) {
		if (src == null) return null;

		int w = src.getWidth();
		int h = src.getHeight();
		int top = h, left = w, right = -1, bottom = -1;

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int a = (src.getRGB(x, y) >> 24) & 0xFF;
				if (a != 0) {
					if (x < left) left = x;
					if (x > right) right = x;
					if (y < top) top = y;
					if (y > bottom) bottom = y;
				}
			}
		}
		if (right < left || bottom < top)
			return src;

		BufferedImage trimmed = src.getSubimage(left, top, (right - left + 1), (bottom - top + 1));
		BufferedImage copy = new BufferedImage(trimmed.getWidth(), trimmed.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = copy.createGraphics();
		g.drawImage(trimmed, 0, 0, null);
		g.dispose();
		return copy;
	}

	public static void attachHeight(JComponent c, int h) {
		c.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
		c.setPreferredSize(new Dimension(10, h));
		c.setMinimumSize(new Dimension(10, h));
	}

	
	
	
	public static class RadarChart extends JComponent {
		private final String[] axes;
		private int[] scores; // setScores()로 갱신 가능

		public RadarChart(String[] axes, int[] scores) {
			this.axes = axes;
			this.scores = scores;
			setOpaque(false);
			setPreferredSize(new Dimension(10, 315));
		}

		/** 점수 갱신 후 다시 그리기 */
		public void setScores(int[] newScores) {
			this.scores = newScores;
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int w = getWidth();
			int h = getHeight();

			int pad = 22;
			int cx = w / 2;
			int cy = h / 2 - 6;
			int r = Math.min(w, h) / 2 - pad - 18;

			g2.setColor(UITheme.RGB_235_235_242);
			for (int level = 1; level <= 3; level++) {
				double rr = r * (level / 3.0);
				drawPolygon(g2, cx, cy, rr, axes.length);
			}

			g2.setColor(UITheme.RGB_228_228_238);
			for (int i = 0; i < axes.length; i++) {
				double ang = -Math.PI / 2 + i * (2 * Math.PI / axes.length);
				int x = (int) (cx + r * Math.cos(ang));
				int y = (int) (cy + r * Math.sin(ang));
				g2.drawLine(cx, cy, x, y);
			}

			Polygon poly = new Polygon();
			for (int i = 0; i < axes.length; i++) {
				double ang = -Math.PI / 2 + i * (2 * Math.PI / axes.length);
				double rr = r * (scores[i] / 3.0);
				int x = (int) (cx + rr * Math.cos(ang));
				int y = (int) (cy + rr * Math.sin(ang));
				poly.addPoint(x, y);
			}

			// 모든 점수가 0이면 보라색 폴리곤 미표시
			boolean hasAnyScore = false;
			for (int s : scores) if (s > 0) { hasAnyScore = true; break; }

			if (hasAnyScore) {
				g2.setColor(UITheme.RGBA_207_201_255_140);
				g2.fillPolygon(poly);
				g2.setColor(UITheme.ACCENT_LAVENDER_BORDER);
				g2.setStroke(new BasicStroke(1.2f));
				g2.drawPolygon(poly);
			}

			g2.setFont(UITheme.CAPTION);
			g2.setColor(UITheme.RGB_110_110_110);
			for (int i = 0; i < axes.length; i++) {
				double ang = -Math.PI / 2 + i * (2 * Math.PI / axes.length);
				int x = (int) (cx + (r + 12) * Math.cos(ang));
				int y = (int) (cy + (r + 12) * Math.sin(ang));
				drawAxisLabel(g2, axes[i], x, y, ang);
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

		private void drawAxisLabel(Graphics2D g2, String text, int x, int y, double ang) {
			FontMetrics fm = g2.getFontMetrics();
			int tw = fm.stringWidth(text);
			int th = fm.getAscent();

			double dx = Math.cos(ang);
			double dy = Math.sin(ang);

			int tx = x;
			int ty = y;

			if (dx > 0.25) {
				tx = x;
			} else if (dx < -0.25) {
				tx = x - tw;
			} else {
				tx = x - tw / 2;
			}

			if (dy > 0.35) {
				ty = y + th;
			} else if (dy < -0.35) {
				ty = y;
			} else {
				ty = y + th / 2;
			}

			g2.drawString(text, tx, ty);
		}
	}


	public static Image loadHiDPIAvatar(String resPath, int logicalSizePx) {
		BufferedImage src = loadBufferedResource(resPath);
		if (src == null)
			return null;

		BufferedImage img1x = scaleFitHQ(src, logicalSizePx);
		BufferedImage img2x = scaleFitHQ(src, logicalSizePx * 2);

		return new BaseMultiResolutionImage(img1x, img2x);
	}


	
	
	
	public static JPanel createCard(int padding) {
		return createCard(padding, true);
	}

	
	public static JPanel createCard(int padding, boolean lockHeightToPreferred) {
		JPanel card = new JPanel() {
			@Override
			public void addNotify() {
				super.addNotify();
				if (!lockHeightToPreferred) return;
				SwingUtilities.invokeLater(() -> {
					int h = getPreferredSize().height;
					if (h > 0) setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
				});
			}
		};
		card.setOpaque(true);
		card.setBackground(UITheme.SURFACE);
		card.setAlignmentX(Component.LEFT_ALIGNMENT);
		card.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UITheme.SURFACE_BORDER, 1, true),
				new EmptyBorder(padding, padding, padding, padding)
		));
		card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		return card;
	}

	public static JPanel createCardWithTitle(String title, int padding, JComponent body) {
		JPanel card = createCard(padding, true);
		card.setLayout(new BorderLayout());
		ShadowLabel t = textLabel(title, UITheme.TITLE_SM, UITheme.TEXT);
		t.setBorder(new EmptyBorder(0, 0, 10, 0));
		card.add(t, BorderLayout.NORTH);
		card.add(body, BorderLayout.CENTER);
		return card;
	}

	
	private static ShadowLabel textLabel(String text, Font font, Color fg) {
		ShadowLabel l = new ShadowLabel(text, 0, fg);
		l.setFont(font);
		l.setForeground(fg);
		return l;
	}

	
	
	
	private static final String CLIENTPROP_HOVER_INDEX = "creati.hoverIndex";

	public static void installHoverTracking(JList<?> list) {
		list.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
			@Override public void mouseMoved(java.awt.event.MouseEvent e) {
				int idx = list.locationToIndex(e.getPoint());
				Object old = list.getClientProperty(CLIENTPROP_HOVER_INDEX);
				Integer oldI = (old instanceof Integer) ? (Integer) old : -1;
				if (idx != oldI) {
					list.putClientProperty(CLIENTPROP_HOVER_INDEX, idx);
					list.repaint();
				}
			}
		});
		list.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override public void mouseExited(java.awt.event.MouseEvent e) {
				Object old = list.getClientProperty(CLIENTPROP_HOVER_INDEX);
				Integer oldI = (old instanceof Integer) ? (Integer) old : -1;
				if (oldI != -1) {
					list.putClientProperty(CLIENTPROP_HOVER_INDEX, -1);
					list.repaint();
				}
			}
		});
		list.putClientProperty(CLIENTPROP_HOVER_INDEX, -1);
	}

	public static int getHoverIndex(JList<?> list) {
		Object v = list.getClientProperty(CLIENTPROP_HOVER_INDEX);
		return (v instanceof Integer) ? (Integer) v : -1;
	}

	@FunctionalInterface
	public interface RowBuilder<T> {
		Component build(JList<? extends T> list, T value, int index, boolean isSelected, boolean isHover);
	}

	public static <T> ListCellRenderer<? super T> createRowRenderer(RowBuilder<T> builder) {
		return (list, value, index, isSelected, cellHasFocus) -> {
			boolean isHover = (getHoverIndex(list) == index);
			Component c = builder.build(list, value, index, isSelected, isHover);
			
			if (c instanceof JComponent jc) {
				applyRowStateBackground(jc, isSelected, isHover);
			}
			return c;
		};
	}

	public static <T> ListCellRenderer<? super T> createLabelRowRenderer(
			java.util.function.Function<T, String> textFn,
			java.util.function.Function<T, Icon> iconFn,
			int padTop, int padLeft, int padBottom, int padRight
	) {
		DefaultListCellRenderer base = new DefaultListCellRenderer();
		base.setOpaque(true);
		return (list, value, index, isSelected, cellHasFocus) -> {
			JLabel c = (JLabel) base.getListCellRendererComponent(list, value, index, isSelected, false);
			c.setText(textFn.apply((T) value));
			c.setFont(UITheme.BODY);
			c.setForeground(UITheme.TEXT);
			c.setBorder(new EmptyBorder(padTop, padLeft, padBottom, padRight));
			Icon ic = (iconFn == null) ? null : iconFn.apply((T) value);
			c.setIcon(ic);
			if (ic != null) c.setIconTextGap(12);
			boolean isHover = (getHoverIndex(list) == index);
			applyRowStateBackground(c, isSelected, isHover);
			return c;
		};
	}

	public static JPanel createRowPanel(int padTop, int padLeft, int padBottom, int padRight) {
		JPanel p = new JPanel(new BorderLayout());
		p.setOpaque(true);
		p.setBorder(new EmptyBorder(padTop, padLeft, padBottom, padRight));
		return p;
	}

	public static JPanel createLogRowPanel(
	        String chipText,
	        Color chipBg,
	        Color chipFg,
	        String title,
	        String sub1,
	        String sub2,
	        String metaRight
	) {
	    JPanel p = createRowPanel(10, 12, 10, 12);
	    p.setLayout(new BorderLayout(12, 0));

	    JPanel left = new JPanel(new BorderLayout(10, 0));
	    left.setOpaque(false);

	    if (chipText != null && !chipText.isBlank()) {
	        com.creati.ui.components.Chip chip = new com.creati.ui.components.Chip();
	        chip.setChip(chipText, chipBg, chipFg);
	        JPanel chipWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
	        chipWrap.setOpaque(false);
	        chipWrap.add(chip);
	        left.add(chipWrap, BorderLayout.WEST);
	    }

	    ShadowLabel t = textLabel(
	            (title == null ? "" : title),
	            UITheme.BODY_MED,
	            UITheme.TEXT
	    );
	    t.setBorder(null);
	    t.setHorizontalAlignment(SwingConstants.LEFT);
	    left.add(t, BorderLayout.CENTER);

	    p.add(left, BorderLayout.CENTER);

	    if (metaRight != null && !metaRight.isBlank()) {
	        ShadowLabel meta = textLabel(metaRight, UITheme.BODY_SM, UITheme.MUTED_TEXT);
	        meta.setHorizontalAlignment(SwingConstants.RIGHT);
	        p.add(meta, BorderLayout.EAST);
	    }

	    return p;
	}

public static JPanel createLogRowPanel(String chipText, Color chipBg, Color chipFg, String title, String metaRight) {
	return createLogRowPanel(chipText, chipBg, chipFg, title, null, null, metaRight);
}
}
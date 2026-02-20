package com.creati.ui.main;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.creati.util.UITheme;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * MainFrame에서 길어지는 요소(컴포넌트/차트/이미지유틸)만 모아둔 파일
 */
public class MainUiParts {

	public static final Color LAVENDER_HOVER = new Color(0xEAE6FF);
	public static final Color LAVENDER_BORDER = new Color(0xCFC9FF);

	// =========================
	// Cards / Charts
	// =========================

	public static class HomeCard extends JPanel {
		private final JPanel bodyWrap;

		public HomeCard(String title) {
			super(new BorderLayout());
			setBackground(Color.WHITE);
			setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(230, 230, 235), 1, true),
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

	// =========================
	// Resource (classpath) Image Utils
	// =========================
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

	// =========================
	// Chart Card
	// =========================

	static class ChartCard extends JPanel {
		private final JPanel chartHolder = new JPanel(new BorderLayout());
		private final JLabel hint = new JLabel(" ");

		ChartCard(String title) {
			super(new BorderLayout());
			setOpaque(true);
			setBackground(new Color(250, 250, 252));
			setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(new Color(235, 235, 242), 1, true),
					new EmptyBorder(12, 12, 12, 12)));

			JLabel t = new JLabel(title);
			t.setFont(UITheme.BODY_MED);
			t.setForeground(UITheme.TEXT);

			hint.setFont(UITheme.CAPTION);
			hint.setForeground(new Color(140, 140, 140));

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

			g2.setColor(Color.WHITE);
			g2.fillRoundRect(0, 0, w, h, 16, 16);
			g2.setColor(new Color(230, 230, 238));
			g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);

			int pad = 14;
			int gx0 = pad, gy0 = pad, gx1 = w - pad, gy1 = h - pad;

			g2.setColor(new Color(242, 242, 248));
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

			g2.setColor(Color.WHITE);
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

			g2.setColor(Color.WHITE);
			g2.fillRoundRect(0, 0, w, h, 16, 16);
			g2.setColor(new Color(230, 230, 238));
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

				Color fill = (i % 2 == 0) ? UITheme.ACCENT_PURPLE : new Color(0xCFC9FF);
				g2.setColor(fill);
				g2.fillRoundRect(x, y, barW, bh, 10, 10);

				g2.setColor(new Color(120, 120, 120));
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

	// =========================
	// Small UI parts
	// =========================

	public static class ShadowLabel extends JLabel {
		private final int shadowAlpha;
		private final Color shadowBase;

		public ShadowLabel(String text, int shadowAlpha, Color shadowBase) {
			super(text);
			this.shadowAlpha = shadowAlpha;
			this.shadowBase = shadowBase;
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			g2.setFont(getFont());
			FontMetrics fm = g2.getFontMetrics();
			int x = getInsets().left;
			int y = getInsets().top + fm.getAscent();

			g2.setColor(new Color(shadowBase.getRed(), shadowBase.getGreen(), shadowBase.getBlue(), shadowAlpha));
			g2.drawString(getText(), x + 1, y + 1);

			g2.setColor(getForeground());
			g2.drawString(getText(), x, y);

			g2.dispose();
		}
	}

	public static class CircleAvatar extends JComponent {
		private final int size;
		private final Image image;
		private final int pad;

		public CircleAvatar(Image image) {
			this(image, 34, 3);
		}

		public CircleAvatar(Image image, int size) {
			this(image, size, 3);
		}

		public CircleAvatar(Image image, int size, int pad) {
			this.size = size;
			this.pad = pad;
			this.image = image;

			setPreferredSize(new Dimension(size, size));
			setMinimumSize(new Dimension(size, size));
			setMaximumSize(new Dimension(size, size));
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

			int w = getWidth(), h = getHeight();
			int d = Math.min(w, h);
			int x0 = (w - d) / 2;
			int y0 = (h - d) / 2;

			g2.setColor(new Color(245, 245, 250));
			g2.fillOval(x0, y0, d, d);

			Shape clip = new Ellipse2D.Float(x0 + 1, y0 + 1, d - 2, d - 2);
			g2.setClip(clip);

			if (image != null) {
				double sx = g2.getTransform().getScaleX();
				double sy = g2.getTransform().getScaleY();
				double deviceScale = Math.max(sx, sy);

				int needPx = (int) Math.ceil(d * deviceScale);

				BufferedImage src = resolveBestBuffered(image, needPx, needPx);
				if (src != null) {
					BufferedImage trimmed = trimTransparent(src);

					int avail = d - pad * 2;

					int iw = trimmed.getWidth();
					int ih = trimmed.getHeight();

					double s = Math.min((double) avail / iw, (double) avail / ih);
					int dw = (int) Math.round(iw * s);
					int dh = (int) Math.round(ih * s);

					int ix = x0 + (d - dw) / 2;
					int iy = y0 + (d - dh) / 2;

					g2.drawImage(trimmed, ix, iy, dw, dh, null);
				}
			}

			g2.setClip(null);

			g2.setColor(new Color(220, 220, 232));
			g2.drawOval(x0, y0, d - 1, d - 1);

			g2.dispose();
		}

		private static BufferedImage resolveBestBuffered(Image img, int targetW, int targetH) {
			try {
				if (img instanceof MultiResolutionImage mri) {
					Image variant = mri.getResolutionVariant(targetW, targetH);
					return toBufferedImage(variant);
				}
				return toBufferedImage(img);
			} catch (Exception e) {
				return null;
			}
		}

		private static BufferedImage toBufferedImage(Image img) {
			if (img == null) return null;
			if (img instanceof BufferedImage bi) return bi;

			int w = img.getWidth(null);
			int h = img.getHeight(null);

			if (w <= 0 || h <= 0) {
				ImageIcon icon = new ImageIcon(img);
				w = icon.getIconWidth();
				h = icon.getIconHeight();
				img = icon.getImage();
			}
			if (w <= 0 || h <= 0) return null;

			BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = bi.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2.drawImage(img, 0, 0, null);
			g2.dispose();
			return bi;
		}
	}

	public static class RoundedButton extends JButton {
		private final int arc = 18;

		public RoundedButton(String text) {
			super(text);
			setFocusPainted(false);
			setContentAreaFilled(false);
			setBorder(new EmptyBorder(10, 14, 10, 14));
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2.setColor(getBackground());
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

			super.paintComponent(g2);
			g2.dispose();
		}
	}

	public static class EllipsisButton extends JButton {
		public EllipsisButton() {
			super("● ● ●");
			setFocusPainted(false);
			setContentAreaFilled(false);
			setBorderPainted(false);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			setForeground(new Color(100, 100, 110));
			setFont(new Font("Dialog", Font.BOLD, 14));
			setPreferredSize(new Dimension(52, 32));
			setHorizontalAlignment(SwingConstants.CENTER);
		}
	}

	// =========================
	// Image Utils (File / Resource)
	// =========================

	public static Icon createHiDPIIcon(Path srcPath, int logicalSizePx, boolean trim) {
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

	public static BufferedImage loadBuffered(Path path) {
		try {
			java.io.File f = path.toFile();
			if (!f.exists())
				return null;
			return ImageIO.read(f);
		} catch (Exception e) {
			return null;
		}
	}

	public static Image loadImage(Path path) {
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

	// =========================
	// Radar Chart (Hexagon)
	// =========================
	public static class RadarChart extends JComponent {
		private final String[] axes;
		private final int[] scores;

		public RadarChart(String[] axes, int[] scores) {
			this.axes = axes;
			this.scores = scores;
			setOpaque(false);
			setPreferredSize(new Dimension(10, 315));
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

			g2.setColor(new Color(235, 235, 242));
			for (int level = 1; level <= 3; level++) {
				double rr = r * (level / 3.0);
				drawPolygon(g2, cx, cy, rr, axes.length);
			}

			g2.setColor(new Color(228, 228, 238));
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

			g2.setColor(new Color(0xCF, 0xC9, 0xFF, 140));
			g2.fillPolygon(poly);

			g2.setColor(new Color(0xCFC9FF));
			g2.setStroke(new BasicStroke(1.2f));
			g2.drawPolygon(poly);

			g2.setFont(UITheme.CAPTION);
			g2.setColor(new Color(110, 110, 110));
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

	public static class RoundedLabel extends JLabel {
		private int arc = 16;
		private Color bg = Color.WHITE;
		private Color border = null;

		public RoundedLabel(String text) {
			super(text);
			setOpaque(false);
			setBorder(new EmptyBorder(5, 10, 5, 10));
		}

		public RoundedLabel arc(int arc) {
			this.arc = arc;
			return this;
		}

		public RoundedLabel bg(Color bg) {
			this.bg = bg;
			return this;
		}

		public RoundedLabel border(Color border) {
			this.border = border;
			return this;
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int w = getWidth();
			int h = getHeight();

			g2.setColor(bg);
			g2.fillRoundRect(0, 0, w, h, arc, arc);

			if (border != null) {
				g2.setColor(border);
				g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
			}

			g2.dispose();
			super.paintComponent(g);
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

}

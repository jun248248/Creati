package com.creati.ui.components;


import com.creati.util.UITheme;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class CircleAvatar extends JComponent {
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

		g2.setColor(UITheme.RGB_245_245_250);
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

		g2.setColor(UITheme.RGB_220_220_232);
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

	private static BufferedImage trimTransparent(BufferedImage src) {
		if (src == null) return null;
		int w = src.getWidth();
		int h = src.getHeight();
		int minX = w, minY = h, maxX = -1, maxY = -1;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int argb = src.getRGB(x, y);
				int a = (argb >>> 24) & 0xFF;
				if (a != 0) {
					if (x < minX) minX = x;
					if (y < minY) minY = y;
					if (x > maxX) maxX = x;
					if (y > maxY) maxY = y;
				}
			}
		}
		
		if (maxX < minX || maxY < minY) return src;
		int nw = maxX - minX + 1;
		int nh = maxY - minY + 1;
		BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = out.createGraphics();
		g2.setComposite(AlphaComposite.Src);
		g2.drawImage(src, 0, 0, nw, nh, minX, minY, maxX + 1, maxY + 1, null);
		g2.dispose();
		return out;
	}

}
package net.sourceforge.plantuml.ugraphic;

import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_OFF;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.sourceforge.plantuml.test.approval.ApprovalTesting;

class RasterTest {

	private static final Object[] ANTIALIAS_OPTIONS = {VALUE_ANTIALIAS_OFF, VALUE_ANTIALIAS_ON};

	@RegisterExtension
	static final ApprovalTesting approvalTesting = new ApprovalTesting();

	@Test
	void test_raster_engine() {
		final BufferedImage image = new BufferedImage(1550, 850, TYPE_INT_RGB);

		final Graphics2D g = image.createGraphics();
		g.setBackground(WHITE);
		g.setColor(BLACK);
		g.clearRect(0, 0, image.getWidth(), image.getHeight());
		g.translate(10, 10);

		for (Object antialias : ANTIALIAS_OPTIONS) {
			final Graphics2D g1 = (Graphics2D) g.create();
			g1.setRenderingHint(KEY_ANTIALIASING, antialias);

			for (float width : new float[]{0.5f, 1, 1.5f, 2, 3}) {
				g1.setStroke(new BasicStroke(width));

				g1.drawOval(0, 0, 90, 90);
				g1.translate(100, 0);

				g1.drawOval(0, 0, 90, 60);
				g1.translate(100, 0);

				g1.drawOval(0, 0, 90, 30);
				g1.translate(100, 0);
			}

			g.translate(0, 110);
		}

		polyline(g, new int[]{0, 20, 20}, new int[]{0, 0, 20});

		polyline(g, new int[]{0, 10, 0}, new int[]{0, 10, 20});

		polyline(g, new int[]{0, 20, 0}, new int[]{0, 10, 20});

		polyline(g, new int[]{0, 40, 0}, new int[]{0, 10, 20});

		approvalTesting.approve(image);
	}

	private void polyline(Graphics2D g, int[] xPoints, int[] yPoints) {
		for (int cap_join : new int[]{0, 1, 2}) {
			final Graphics2D g1 = (Graphics2D) g.create();
			for (Object antialias : ANTIALIAS_OPTIONS) {
				g1.setRenderingHint(KEY_ANTIALIASING, antialias);
				for (float width : new float[]{0.5f, 1, 1.5f, 2, 3, 5, 8, 12, 15}) {
					g1.setStroke(new BasicStroke(width, cap_join, cap_join));
					g1.drawPolyline(xPoints, yPoints, 3);
					g1.translate(70, 0);
				}
			}
			g.translate(0, 50);
		}
	}
}

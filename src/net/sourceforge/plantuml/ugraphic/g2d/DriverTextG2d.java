/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2020, Arnaud Roques
 *
 * Project Info:  http://plantuml.com
 *
 * If you like this project or if you find it useful, you can support us at:
 *
 * http://plantuml.com/patreon (only 1$ per month!)
 * http://plantuml.com/paypal
 *
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 *
 * Original Author:  Arnaud Roques
 *
 *
 */
package net.sourceforge.plantuml.ugraphic.g2d;

import static java.lang.Math.max;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import net.sourceforge.plantuml.EnsureVisible;
import net.sourceforge.plantuml.graphic.FontConfiguration;
import net.sourceforge.plantuml.graphic.FontStyle;
import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.text.StyledString;
import net.sourceforge.plantuml.ugraphic.UDriver;
import net.sourceforge.plantuml.ugraphic.UFont;
import net.sourceforge.plantuml.ugraphic.UParam;
import net.sourceforge.plantuml.ugraphic.UText;
import net.sourceforge.plantuml.ugraphic.color.ColorMapper;
import net.sourceforge.plantuml.ugraphic.color.HColor;
import net.sourceforge.plantuml.ugraphic.color.HColorGradient;
import net.sourceforge.plantuml.ugraphic.color.HColorUtils;

public class DriverTextG2d implements UDriver<UText, Graphics2D> {

	private final EnsureVisible visible;
	private final StringBounder stringBounder;

	public DriverTextG2d(EnsureVisible visible, StringBounder stringBounder) {
		this.visible = visible;
		this.stringBounder = stringBounder;
	}

	public void draw(UText shape, double x, double y, ColorMapper mapper, UParam param, Graphics2D g2d) {
		final FontConfiguration fontConfiguration = shape.getFontConfiguration();

		if (HColorUtils.isTransparent(fontConfiguration.getColor())) {
			return;
		}
		final String text = shape.getText();

		final List<StyledString> strings = StyledString.build(text);

		for (StyledString styledString : strings) {
			final FontConfiguration fc = styledString.getStyle() == FontStyle.BOLD ? fontConfiguration.bold()
					: fontConfiguration;
			x += printSingleText(g2d, fc, styledString.getText(), x, y, mapper);
		}
	}

	private double printSingleText(Graphics2D g2d, final FontConfiguration fontConfiguration, final String text, double x,
			double y, ColorMapper mapper) {
		final UFont font = fontConfiguration.getFont();
		final HColor extended = fontConfiguration.getExtendedColor();

		final Dimension2D dim = stringBounder.calculateDimension(font, text);
		final double height = max(10, dim.getHeight());
		final double width = dim.getWidth();

		final int orientation = 0;

		if (orientation == 90) {
			// TODO this block has not been updated to support font sprite sheets
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setFont(font.getUnderlayingFont());
			g2d.setColor(mapper.toColor(fontConfiguration.getColor()));
			final AffineTransform orig = g2d.getTransform();
			g2d.translate(x, y);
			g2d.rotate(Math.PI / 2);
			g2d.drawString(text, 0, 0);
			g2d.setTransform(orig);

		} else if (orientation == 0) {

			if (fontConfiguration.containsStyle(FontStyle.BACKCOLOR)) {
				final Rectangle2D.Double area = new Rectangle2D.Double(x, y - height + 1.5, width, height);
				if (extended instanceof HColorGradient) {
					final GradientPaint paint = DriverRectangleG2d.getPaintGradient(x, y, mapper, width, height, extended);
					g2d.setPaint(paint);
					g2d.fill(area);
				} else {
					final Color backColor = mapper.toColor(extended);
					if (backColor != null) {
						g2d.setColor(backColor);
						g2d.setBackground(backColor);
						g2d.fill(area);
					}
				}
			}
			visible.ensureVisible(x, y - height + 1.5);
			visible.ensureVisible(x + width, y + 1.5);

			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setFont(font.getUnderlayingFont());
			g2d.setColor(mapper.toColor(fontConfiguration.getColor()));
			drawStringInternal(g2d, text, (float) x, (float) y);

			if (fontConfiguration.containsStyle(FontStyle.UNDERLINE)) {
				if (extended != null) {
					g2d.setColor(mapper.toColor(extended));
				}
				final int ypos = (int) (y + 2.5);
				g2d.setStroke(new BasicStroke((float) 1));
				g2d.drawLine((int) x, ypos, (int) (x + width), ypos);
				g2d.setStroke(new BasicStroke());
			}
			if (fontConfiguration.containsStyle(FontStyle.WAVE)) {
				final int ypos = (int) (y + 2.5) - 1;
				if (extended != null) {
					g2d.setColor(mapper.toColor(extended));
				}
				for (int i = (int) x; i < x + width - 5; i += 6) {
					g2d.drawLine(i, ypos - 0, i + 3, ypos + 1);
					g2d.drawLine(i + 3, ypos + 1, i + 6, ypos - 0);
				}
			}
			if (fontConfiguration.containsStyle(FontStyle.STRIKE)) {
				final int ypos = (int) (y - stringBounder.getDescent(font, text) - 0.5);
				if (extended != null) {
					g2d.setColor(mapper.toColor(extended));
				}
				g2d.setStroke(new BasicStroke((float) 1.5));
				g2d.drawLine((int) x, ypos, (int) (x + width), ypos);
				g2d.setStroke(new BasicStroke());
			}
		}
		return width;
	}

	protected void drawStringInternal(Graphics2D g2d, String text, float x, float y) {
		g2d.drawString(text, x, y);
	}

}

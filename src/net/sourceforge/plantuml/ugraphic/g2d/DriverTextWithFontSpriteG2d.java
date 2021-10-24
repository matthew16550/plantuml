package net.sourceforge.plantuml.ugraphic.g2d;

import java.awt.Graphics2D;

import net.sourceforge.plantuml.EnsureVisible;
import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.ugraphic.fontspritesheet.FontSpriteSheetManager;

public class DriverTextWithFontSpriteG2d extends DriverTextG2d {

	private final FontSpriteSheetManager fontSpriteSheetManager = FontSpriteSheetManager.instance();

	public DriverTextWithFontSpriteG2d(EnsureVisible visible, StringBounder stringBounder) {
		super(visible, stringBounder);
	}

	@Override
	protected void drawString(Graphics2D g2d, String text, float x, float y) {
		fontSpriteSheetManager
				.findNearestSheet(g2d.getFont())
				.drawString(g2d, text, x, y);
	}
}

package net.sourceforge.plantuml.ugraphic.fontspritesheet;

import static java.awt.Color.WHITE;
import static java.awt.Font.BOLD;
import static java.awt.Font.ITALIC;
import static java.awt.Font.PLAIN;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_GASP;
import static java.awt.image.BufferedImage.TYPE_BYTE_GRAY;
import static java.lang.Math.max;
import static java.util.Arrays.asList;
import static net.sourceforge.plantuml.ugraphic.fontspritesheet.FontSpriteSheetData.ALL_CHARS_IN_SHEET;
import static net.sourceforge.plantuml.ugraphic.fontspritesheet.FontSpriteSheetData.FONT_SPRITE_SHEET_SIZES;
import static net.sourceforge.plantuml.ugraphic.fontspritesheet.FontSpriteSheetData.JETBRAINS_FONT_FAMILY;
import static net.sourceforge.plantuml.ugraphic.fontspritesheet.FontSpriteSheetData.TOFU_CHAR;
import static net.sourceforge.plantuml.ugraphic.fontspritesheet.FontSpriteSheetData.registerJetBrainsFontFiles;
import static net.sourceforge.plantuml.utils.MathUtils.roundUp;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.sourceforge.plantuml.annotation.VisibleForTesting;

// I did a few experiments comparing a single PNG containing four font styles (plain / bold / italic / bold-italic) 
// vs four separate PNGs and found the total file size is very similar either way.
//
// It is much simpler to use one PNG for each style so that is what we do.

public class FontSpriteSheetMaker {

	public static void main(String[] args) throws Exception {
		registerJetBrainsFontFiles();

		for (int size : FONT_SPRITE_SHEET_SIZES) {
			for (int style : asList(PLAIN, ITALIC, BOLD, BOLD | ITALIC)) {
				final Font font = new Font(JETBRAINS_FONT_FAMILY, style, size);
				final FontSpriteSheet sheet = createFontSpriteSheet(font);
				final Path path = Paths.get("testResources").resolve("font-sprite-sheets").resolve(sheet.getPreferredFilename());
				System.out.println("Writing: " + path);
				sheet.writeAsPNG(path);
			}
		}
	}

	@VisibleForTesting
	static FontSpriteSheet createFontSpriteSheet(Font font) {
		if (font.canDisplay(TOFU_CHAR)) {
			throw new RuntimeException("Oops this font has a glyph where we expect TOFU_CHAR : " + font.getFontName());
		}

		// Compute sizes

		final Graphics2D g0 = new BufferedImage(1, 1, TYPE_BYTE_GRAY).createGraphics();
		g0.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_GASP);

		final FontRenderContext frc = g0.getFontRenderContext();
		final TextLayout textLayout = new TextLayout(ALL_CHARS_IN_SHEET, font, frc);
		final float ascent = textLayout.getAscent();

		float advance = 0;
		final Rectangle bounds = new Rectangle();

		for (char c : ALL_CHARS_IN_SHEET.toCharArray()) {
			final GlyphVector glyphVector = font.createGlyphVector(frc, new char[]{c});
			advance = max(advance, glyphVector.getGlyphMetrics(0).getAdvance());
			bounds.add(glyphVector.getGlyphPixelBounds(0, frc, 0, ascent));
		}

		final int xOffset = -roundUp(bounds.getX());
		final int sheetHeight = roundUp(bounds.getHeight() - bounds.getY());
		final int spriteWidth = roundUp(bounds.getWidth() - bounds.getX());
		final int sheetWidth = xOffset + spriteWidth * ALL_CHARS_IN_SHEET.length();

		// Draw sprites

		final BufferedImage image = new BufferedImage(sheetWidth, sheetHeight, TYPE_BYTE_GRAY);
		final Graphics2D g = image.createGraphics();
		g.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_GASP);
		final FontMetrics fontMetrics = g.getFontMetrics(font);
		g.setColor(WHITE);
		g.setFont(font);

		int x = xOffset;
		for (char c : ALL_CHARS_IN_SHEET.toCharArray()) {
			g.drawString(String.valueOf(c), x, ascent);
			x += spriteWidth;
		}

		final LineMetrics lineMetrics = font.getLineMetrics(ALL_CHARS_IN_SHEET, frc);
		return new FontSpriteSheet(image, fontMetrics, lineMetrics, textLayout, advance, spriteWidth, xOffset);
	}
}

package net.sourceforge.plantuml.ugraphic.fontspritesheet;

import static java.awt.Color.WHITE;
import static java.awt.Font.BOLD;
import static java.awt.Font.ITALIC;
import static java.awt.Font.PLAIN;
import static java.awt.Font.TRUETYPE_FONT;
import static java.awt.Font.createFont;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_GASP;
import static java.awt.image.BufferedImage.TYPE_BYTE_GRAY;
import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static net.sourceforge.plantuml.utils.CollectionUtils.immutableList;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

// I did a few experiments comparing a single PNG containing four font styles (plain / bold / italic / bold-italic) 
// vs four separate PNGs and found the total file size is very similar either way.
//
// It is much simpler to use one PNG for each style so that is what we do.

public class FontSpriteSheetMaker {

	// Visible for testing
	static final String ALL_CHARS = "☺!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";

	// Visible for testing
	static final String JETBRAINS_FONT_FAMILY = "JetBrains Mono NL";

	private static final List<String> JETBRAINS_FONT_FILES = immutableList(
			"JetBrainsMonoNL-Bold.ttf",
			"JetBrainsMonoNL-BoldItalic.ttf",
			"JetBrainsMonoNL-Italic.ttf",
			"JetBrainsMonoNL-Regular.ttf"
	);

	public static void main(String[] args) throws Exception {
		registerJetBrainsFonts();

		for (int size : FontSpriteSheetManager.FONT_SIZES) {
			for (int style : immutableList(PLAIN, ITALIC, BOLD, BOLD | ITALIC)) {
				final Font font = new Font(JETBRAINS_FONT_FAMILY, style, size);
				final FontSpriteSheet sheet = createFontSpriteSheet(font);
				final Path path = Paths.get(".").resolve(sheet.getPreferredFilename());
				sheet.writeAsPNG(path);
			}
		}
	}

	// Visible for testing
	static void registerJetBrainsFonts() throws Exception {
		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

		for (String filename : JETBRAINS_FONT_FILES) {
			final File file = Paths.get("JetBrainsMono-2.242").resolve(filename).toFile();
			final Font font = createFont(TRUETYPE_FONT, file);
			ge.registerFont(font);
		}
	}

	// Visible for testing
	static FontSpriteSheet createFontSpriteSheet(Font font) {
		if (font.canDisplay(ALL_CHARS.charAt(0))) {
			throw new RuntimeException(
					"Oops. This font has a glyph for the first char in ALL_CHARS. Use a different char so we get the tofu symbol");
		}

		// Compute sizes

		final Graphics2D g0 = new BufferedImage(1, 1, TYPE_BYTE_GRAY).createGraphics();

		g0.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_GASP);

		final FontRenderContext frc = g0.getFontRenderContext();

		final int ascent = (int) ceil(new TextLayout(ALL_CHARS, font, frc).getAscent());

		int advance = 0;

		final Rectangle bounds = new Rectangle();

		for (char c1 : ALL_CHARS.toCharArray()) {
			final TextLayout layout = new TextLayout(String.valueOf(c1), font, frc);
			advance = max(advance, (int) ceil(layout.getAdvance()));
			bounds.add(layout.getPixelBounds(null, 0, ascent));
		}

		final int xOffset = (int) -ceil(bounds.getX());
		final int sheetHeight = (int) ceil(bounds.getHeight() - bounds.getY());
		final int spriteWidth = (int) ceil(bounds.getWidth() - bounds.getX());
		final int sheetWidth = xOffset + spriteWidth * ALL_CHARS.length();

		// Draw sprites

		final BufferedImage image = new BufferedImage(sheetWidth, sheetHeight, TYPE_BYTE_GRAY);
		final Graphics2D g = image.createGraphics();
		g.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_GASP);
		final FontMetrics fontMetrics = g.getFontMetrics(font);
		g.setColor(WHITE);
		g.setFont(font);
		g.translate(xOffset, ascent);

		for (char c : ALL_CHARS.toCharArray()) {
			g.drawString(String.valueOf(c), 0, 0);
			g.translate(spriteWidth, 0);
		}

		return new FontSpriteSheet(image, fontMetrics, advance, ascent, spriteWidth, xOffset);
	}
}
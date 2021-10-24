package net.sourceforge.plantuml.ugraphic.fontspritesheet;

import static java.awt.Font.PLAIN;

import java.awt.Font;
import java.awt.geom.Dimension2D;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.ugraphic.UFont;

public class FontSpriteSheetManager {

	public static boolean USE = false;  // TODO temporary kludge
	
	private static final FontSpriteSheetManager INSTANCE = new FontSpriteSheetManager();

	public static FontSpriteSheetManager instance() {
		return INSTANCE;
	}

	private final Map<String, FontSpriteSheet> cache = new ConcurrentHashMap<>();

	private FontSpriteSheetManager() {
	}

	public FontSpriteSheet findNearestSheet(Font font) {
		final int size = findNearestSize(font);
		final String cacheKey = font.getStyle() + "-" + size;
		FontSpriteSheet sheet = cache.get(cacheKey);
		if (sheet == null) { // TODO concurrency?
			sheet = load(font.getStyle(), size);
			cache.put(cacheKey, sheet);
		}
		return sheet;
	}

	private int findNearestSize(Font font) {
		return font.getSize() < 16 ? 9 : 20;
	}

	public StringBounder createStringBounder() {
		return new StringBounder() {
			@Override
			public Dimension2D calculateDimension(UFont font, String text) {
				return findNearestSheet(font.getUnderlayingFont())
						.calculateDimension(text);
			}

			@Override
			public double getDescent(UFont font, String text) {
				return findNearestSheet(font.getUnderlayingFont())
						.getDescent();
			}
		};
	}

	private static FontSpriteSheet load(int style, int size) {
		final StringBuilder name = new StringBuilder("/font-sprite-sheets/JetBrains-Mono-NL-");
		if (style == PLAIN) name.append("Regular-");
		if ((style & Font.BOLD) > 0) name.append("Bold-");
		if ((style & Font.ITALIC) > 0) name.append("Italic-");
		name.append(size).append(".png");

		try (final InputStream in = FontSpriteSheet.class.getResourceAsStream(name.toString())) {
			if (in == null) {
				throw new RuntimeException(String.format("Resource '%s' not found", name));
			}
			return new FontSpriteSheet(in);
		} catch (Exception e) {
			throw new RuntimeException(String.format("Error loading Font Sprite Sheet '%s' : %s", name, e.getMessage()), e);
		}
	}
}

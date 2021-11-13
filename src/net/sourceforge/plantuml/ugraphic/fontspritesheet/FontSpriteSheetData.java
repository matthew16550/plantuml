package net.sourceforge.plantuml.ugraphic.fontspritesheet;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;

public class FontSpriteSheetData {

	static final List<Integer> FONT_SPRITE_SHEET_SIZES = unmodifiableList(asList(9, 11, 14, 20));

	static final String JETBRAINS_FONT_FAMILY = "JetBrains Mono NL";

	static final char TOFU_CHAR = (char) -1;  // not sure if this is a good idea but so far it is working fine !

	static final String ALL_CHARS_IN_SHEET = TOFU_CHAR + "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";

	static final char MIN_CHAR_IN_SHEET = 0x21;

	static final char MAX_CHAR_IN_SHEET = 0x7e;

	static void registerJetBrainsFontFiles() throws Exception {
		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

		for (String style : new String[]{"Bold", "BoldItalic", "Italic", "Regular"}) {
			final File file = Paths.get("font-sprite-sheets")
					.resolve("fonts")
					.resolve("JetBrainsMono-2.242")
					.resolve("JetBrainsMonoNL-" + style + ".ttf")
					.toFile();
			final Font font = Font.createFont(Font.TRUETYPE_FONT, file);
			ge.registerFont(font);
		}
	}
}

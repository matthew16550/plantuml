package net.sourceforge.plantuml.ugraphic.fontspritesheet;

import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static java.awt.Font.ITALIC;
import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableList;
import static net.sourceforge.plantuml.graphic.TextBlockUtils.getFontRenderContext;
import static net.sourceforge.plantuml.test.ImageTestUtils.assertImagesEqual;
import static net.sourceforge.plantuml.ugraphic.fontspritesheet.FontSpriteSheetData.ALL_CHARS_IN_SHEET;
import static net.sourceforge.plantuml.ugraphic.fontspritesheet.FontSpriteSheetData.FONT_SPRITE_SHEET_SIZES;
import static net.sourceforge.plantuml.ugraphic.fontspritesheet.FontSpriteSheetData.JETBRAINS_FONT_FAMILY;
import static net.sourceforge.plantuml.ugraphic.fontspritesheet.FontSpriteSheetData.registerJetBrainsFontFiles;
import static net.sourceforge.plantuml.ugraphic.fontspritesheet.FontSpriteSheetMaker.createFontSpriteSheet;
import static net.sourceforge.plantuml.utils.MathUtils.roundUp;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.CartesianEnumSource;
import org.junitpioneer.jupiter.CartesianProductTest;
import org.junitpioneer.jupiter.CartesianValueSource;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.test.approval.ApprovalTesting;
import net.sourceforge.plantuml.test.outputs.TestOutputs;
import net.sourceforge.plantuml.ugraphic.UFont;

class FontSpriteSheetTest {

	@RegisterExtension
	static final ApprovalTesting approvalTesting = new ApprovalTesting();

	@BeforeAll
	static void before_all() throws Exception {
		registerJetBrainsFontFiles();
	}

	//
	// Test Cases
	//

	@ParameterizedTest(name = "{arguments}")
	@MethodSource("allSheets")
	void test_stored_sprite_sheets_always_make_the_same_output(FontSpriteSheet sheet, TestOutputs outputs) {
		outputs.autoSpamCount(false);

		final Dimension2D dimension = sheet.calculateDimension(ALL_CHARS_IN_SHEET);
		final int width = 4 + roundUp(dimension.getWidth());
		final int height = 4 + roundUp(dimension.getHeight());

		final BufferedImage image = new BufferedImage(width, height, TYPE_INT_ARGB);
		final Graphics2D g = image.createGraphics();

		g.setBackground(WHITE);
		g.clearRect(0, 0, width, height);

		g.setColor(BLACK);
		g.translate(2, 2);
		sheet.drawString(g, ALL_CHARS_IN_SHEET, 0, (float) (dimension.getHeight() - sheet.getDescent()));

		approvalTesting
				.approve(image);
	}

	@CartesianProductTest(name = "{arguments}")
	@CartesianValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20})
	@CartesianEnumSource(FontStyle.class)
	void test_sprite_sheet_creation(int size, FontStyle style) {

		final Font font = new Font(JETBRAINS_FONT_FAMILY, style.style, size);

		final FontSpriteSheet sheet = createFontSpriteSheet(font);

		try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {

			final LineMetrics lineMetrics = font.getLineMetrics("", getFontRenderContext());

			softly.assertThat(sheet.getAscent())
					.isEqualTo(lineMetrics.getAscent());

			softly.assertThat(sheet.getDescent())
					.isEqualTo(lineMetrics.getDescent());

			softly.assertThat(sheet.getLeading())
					.isEqualTo(lineMetrics.getLeading());

			softly.assertThat(sheet.getName())
					.isEqualTo(font.getFontName());

			softly.assertThat(sheet.getPointSize())
					.isEqualTo(size);

			softly.assertThat(sheet.getStrikethroughOffset())
					.isEqualTo(lineMetrics.getStrikethroughOffset());

			softly.assertThat(sheet.getStrikethroughThickness())
					.isEqualTo(lineMetrics.getStrikethroughThickness());

			softly.assertThat(sheet.getStyle())
					.isEqualTo(style.style);

			softly.assertThat(sheet.getUnderlineOffset())
					.isEqualTo(lineMetrics.getUnderlineOffset());

			softly.assertThat(sheet.getUnderlineThickness())
					.isEqualTo(lineMetrics.getUnderlineThickness());

			final UFont uFont = UFont.fromFont(font);
			final StringBounder bounder = FileFormat.PNG.getDefaultStringBounder();

			for (String string : asList("", " ", "x", "foo", ALL_CHARS_IN_SHEET)) {
				final Dimension2D dimensionFromSheet = sheet.calculateDimension(string);
				final Dimension2D dimensionFromNormalBounder = bounder.calculateDimension(uFont, string);

				softly.assertThat(dimensionFromSheet.getHeight())
						.isEqualTo(dimensionFromNormalBounder.getHeight());

				softly.assertThat(dimensionFromSheet.getWidth())
						.isEqualTo(dimensionFromNormalBounder.getWidth());
			}
		}
	}

	@Test
	void test_sprite_sheet_loading() throws Exception {

		final Font font = stream(getLocalGraphicsEnvironment().getAllFonts())
				.filter(f -> f.getLineMetrics("x", getFontRenderContext()).getLeading() > 0)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("This test needs a font with non-zero leading"))
				.deriveFont(ITALIC, 20);

		final FontSpriteSheet original = createFontSpriteSheet(font);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		original.writeAsPNG(baos);

		final ByteArrayInputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
		final FontSpriteSheet loaded = new FontSpriteSheet(inputStream);

		// isNotZero() & isNotEmpty() ensure we do not overlook a failure because the expected value is the default field value
		try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
			softly.assertThat(loaded.getAdvance())
					.isNotZero()
					.isEqualTo(original.getAdvance());

			softly.assertThat(loaded.getAscent())
					.isNotZero()
					.isEqualTo(original.getAscent());

			softly.assertThat(loaded.getDescent())
					.isNotZero()
					.isEqualTo(original.getDescent());

			softly.assertThat(loaded.getLeading())
					.isNotZero()
					.isEqualTo(original.getLeading());

			softly.assertThat(loaded.getMetadata())
					.isNotEmpty()
					.isEqualTo(original.getMetadata());

			softly.assertThat(loaded.getName())
					.isNotEmpty()
					.isEqualTo(original.getName());

			softly.assertThat(loaded.getPointSize())
					.isNotZero()
					.isEqualTo(original.getPointSize());

			softly.assertThat(loaded.getSpriteWidth())
					.isNotZero()
					.isEqualTo(original.getSpriteWidth());

			softly.assertThat(loaded.getStrikethroughOffset())
					.isNotZero()
					.isEqualTo(original.getStrikethroughOffset());

			softly.assertThat(loaded.getStrikethroughThickness())
					.isNotZero()
					.isEqualTo(original.getStrikethroughThickness());

			softly.assertThat(loaded.getStyle())
					.isNotZero()
					.isEqualTo(original.getStyle());

			softly.assertThat(loaded.getUnderlineOffset())
					.isNotZero()
					.isEqualTo(original.getUnderlineOffset());

			softly.assertThat(loaded.getUnderlineThickness())
					.isNotZero()
					.isEqualTo(original.getUnderlineThickness());

			softly.assertThat(loaded.getXOffset())
					.isNotZero()
					.isEqualTo(original.getXOffset());

			assertImagesEqual(original.getAlphaImage(), loaded.getAlphaImage());
		}
	}

	//
	// Test DSL
	//

	// Kludge to give us meaningful test names
	private enum FontStyle {
		PLAIN(Font.PLAIN),
		ITALIC(Font.ITALIC),
		BOLD(Font.BOLD),
		BOLD_ITALIC(Font.BOLD | Font.ITALIC);

		private final int style;

		FontStyle(int style) {
			this.style = style;
		}
	}

	private static List<FontSpriteSheet> allSheets() {
		final FontSpriteSheetManager manager = FontSpriteSheetManager.instance();
		final List<FontSpriteSheet> sheets = new ArrayList<>();
		for (int size : FONT_SPRITE_SHEET_SIZES) {
			for (FontStyle style : FontStyle.values()) {
				sheets.add(manager.findNearestSheet(new Font(null, style.style, size)));
			}
		}
		return unmodifiableList(sheets);
	}

}

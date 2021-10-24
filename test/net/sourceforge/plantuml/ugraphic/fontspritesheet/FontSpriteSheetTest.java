package net.sourceforge.plantuml.ugraphic.fontspritesheet;

import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static java.awt.Font.ITALIC;
import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.lang.Math.ceil;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableList;
import static net.sourceforge.plantuml.graphic.TextBlockUtils.getFontRenderContext;
import static net.sourceforge.plantuml.test.ImageTestUtils.assertImagesEqual;
import static net.sourceforge.plantuml.test.PlantUmlTestUtils.exportDiagram;
import static net.sourceforge.plantuml.ugraphic.fontspritesheet.FontSpriteSheetData.ALL_CHARS_IN_SHEET;
import static net.sourceforge.plantuml.ugraphic.fontspritesheet.FontSpriteSheetData.FONT_SPRITE_SHEET_SIZES;
import static net.sourceforge.plantuml.ugraphic.fontspritesheet.FontSpriteSheetData.JETBRAINS_FONT_FAMILY;
import static net.sourceforge.plantuml.ugraphic.fontspritesheet.FontSpriteSheetData.registerJetBrainsFontFiles;
import static net.sourceforge.plantuml.ugraphic.fontspritesheet.FontSpriteSheetMaker.createFontSpriteSheet;
import static org.junit.platform.commons.util.ExceptionUtils.throwAsUncheckedException;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.CartesianEnumSource;
import org.junitpioneer.jupiter.CartesianProductTest;
import org.junitpioneer.jupiter.CartesianValueSource;

import net.sourceforge.plantuml.AbstractPSystem;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.test.approval.ApprovalTesting;
import net.sourceforge.plantuml.test.outputs.TestOutputs;
import net.sourceforge.plantuml.ugraphic.UFont;

class FontSpriteSheetTest {

	@RegisterExtension
	static final ApprovalTesting approvalTesting = new ApprovalTesting();

	@BeforeAll
	static void beforeAll() throws Exception {
		registerJetBrainsFontFiles();
	}

	@AfterEach
	void afterEach() {
		AbstractPSystem.FORCE_TESTING_FONT = false;
	}

	//
	// Test Cases
	//

	@Test
	void test_diagram_activity() {
		approve(
				"@startuml",
				"!pragma testing_font",
				"(*) -> foo",
				"@enduml"
		);
	}

	@Test
	void test_diagram_activity3() {
		approve(
				"@startuml",
				"!pragma testing_font",
				":foo;",
				"@enduml"
		);
	}

	@Test
	void test_diagram_board() {
		approve(
				"@startboard",
				"!pragma testing_font",
				"foo",
				"@endboard"
		);
	}

	@Test
	void test_diagram_bpm() {
		AbstractPSystem.FORCE_TESTING_FONT = true;
		approve(
				"@startbpm",
				":foo;",
				"@endbpm"
		);
	}

	@Test
	void test_diagram_class() {
		approve(
				"@startuml",
				"!pragma testing_font",
				"class foo",
				"@enduml"
		);
	}

	@Test
	void test_diagram_description() {
		approve(
				"@startuml",
				"!pragma testing_font",
				"[foo]",
				"@enduml"
		);
	}

	@Test
	void test_diagram_dot() {
		AbstractPSystem.FORCE_TESTING_FONT = true;
		approve(
				"@startuml",
				"digraph foo {",
				"  foo [shape=box]",
				"}",
				"@enduml"
		);
	}

	@Test
	void test_diagram_flow() {
		AbstractPSystem.FORCE_TESTING_FONT = true;
		approve(
				"@startflow",
				"10 \"foo\"",
				"@endflow"
		);
	}

	@Test
	void test_diagram_gantt() {
		approve(
				"@startgantt",
				"!pragma testing_font",
				"[foo] lasts 5 days",
				"@endgantt"
		);
	}

	@Test
	void test_diagram_git() {
		AbstractPSystem.FORCE_TESTING_FONT = true;
		approve(
				"@startgit",
				"* foo",
				"@endgit"
		);
	}

	@Test
	void test_diagram_json() {
		AbstractPSystem.FORCE_TESTING_FONT = true;
		approve(
				"@startjson",
				"[\"foo\"]",
				"@endjson"
		);
	}

	@Test
	void test_diagram_mindmap() {
		approve(
				"@startmindmap",
				"!pragma testing_font",
				"* foo",
				"@endmindmap"
		);
	}

	@Test
	void test_diagram_network() {
		approve(
				"@startuml",
				"!pragma testing_font",
				"nwdiag {",
				"    network {",
				"        foo ;",
				"    }",
				"}",
				"@enduml"
		);
	}

	@Test
	void test_diagram_plain() {
		AbstractPSystem.FORCE_TESTING_FONT = true;
		approve(
				"@startuml",
				"@enduml"
		);
	}

	@Test
	void test_diagram_salt() {
		approve(
				"@startuml",
				"!pragma testing_font",
				"salt",
				"{",
				"  [foo]",
				"}",
				"@enduml"
		);
	}

	@Test
	void test_diagram_sequence() {
		approve(
				"@startuml",
				"!pragma testing_font",
				"foo -> foo",
				"@enduml"
		);
	}

	@Test
	void test_diagram_state() {
		approve(
				"@startuml",
				"!pragma testing_font",
				"[*] --> foo",
				"@enduml"
		);
	}

	@Test
	void test_diagram_stdlib() {
		AbstractPSystem.FORCE_TESTING_FONT = true;
		approve(
				"@startuml",
				"stdlib c4",
				"@enduml"
		);
	}

	@Test
	@Disabled("TODO sudoku needs work to support font sprites")
	void test_diagram_sudoku() {
		AbstractPSystem.FORCE_TESTING_FONT = true;
		approve(
				"@startuml",
				"sudoku 1",
				"@enduml"
		);
	}

	@Test
	void test_diagram_timing() {
		approve(
				"@startuml",
				"!pragma testing_font",
				"concise foo",
				"@0",
				"foo is _",
				"@100",
				"foo is _",
				"@enduml"
		);
	}

	@Test
	void test_diagram_wbs() {
		approve(
				"@startwbs",
				"!pragma testing_font",
				"* foo",
				"@endwbs"
		);
	}

	@Test
	void test_diagram_wire() {
		approve(
				"@startwire",
				"!pragma testing_font",
				"*foo",
				"@endwire"
		);
	}

	@Test
	void test_diagram_yaml() {
		AbstractPSystem.FORCE_TESTING_FONT = true;
		approve(
				"@startyaml",
				"foo: _",
				"@endyaml"
		);
	}

	@ParameterizedTest(name = "{arguments}")
	@MethodSource("allSheets")
	void test_stored_sprite_sheets_always_make_the_same_output(FontSpriteSheet sheet, TestOutputs outputs) {
		outputs.autoSpamCount(false);

		final Dimension2D dimension = sheet.calculateDimension(ALL_CHARS_IN_SHEET);
		final int width = 4 + (int) ceil(dimension.getWidth());
		final int height = 4 + (int) ceil(dimension.getHeight());

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

		final Font font = new Font(JETBRAINS_FONT_FAMILY, style.value, size);

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
					.isEqualTo(style.value);

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

		// Unfortunately all the JetBrains fonts have zero getLeading(), we expect another font on the machine
		// to have non-zero leading, so it can be properly tested
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

		private final int value;

		FontStyle(int value) {
			this.value = value;
		}
	}

	private static List<FontSpriteSheet> allSheets() {
		final FontSpriteSheetManager manager = FontSpriteSheetManager.instance();
		final List<FontSpriteSheet> sheets = new ArrayList<>();
		for (int size : FONT_SPRITE_SHEET_SIZES) {
			for (FontStyle style : FontStyle.values()) {
				sheets.add(manager.findNearestSheet(new Font(null, style.value, size)));
			}
		}
		return unmodifiableList(sheets);
	}

	private void approve(String... source) {
		try {
			final BufferedImage image = exportDiagram(source)
					.assertNoError()
					.asImage();
			approvalTesting.approve(image);
		} catch (IOException e) {
			throwAsUncheckedException(e);
		}
	}
}

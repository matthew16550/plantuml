package net.sourceforge.plantuml.ugraphic.fontspritesheet;

import static java.lang.Math.round;
import static java.nio.file.Files.newOutputStream;
import static net.sourceforge.plantuml.png.PngWriter.CREATION_METADATA_TAG;
import static net.sourceforge.plantuml.ugraphic.fontspritesheet.FontSpriteSheetData.MAX_CHAR_IN_SHEET;
import static net.sourceforge.plantuml.ugraphic.fontspritesheet.FontSpriteSheetData.MIN_CHAR_IN_SHEET;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.SoftReference;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sourceforge.plantuml.Dimension2DDouble;
import net.sourceforge.plantuml.png.PngIO;
import net.sourceforge.plantuml.png.PngReader;

public class FontSpriteSheet {

	private final Map<Integer, SoftReference<BufferedImage>> colorizedImageCache = new ConcurrentHashMap<>();
	private final float advance;
	private final BufferedImage alphaImage;
	private final float ascent;
	private final float descent;
	private final float leading;
	private final String creationMetadata;
	private final String name;
	private final float pointSize;
	private final int spriteWidth;
	private final float strikethroughOffset;
	private final float strikethroughThickness;
	private final int style;
	private final float underlineOffset;
	private final float underlineThickness;
	private final int xOffset;

	FontSpriteSheet(BufferedImage alphaImage, FontMetrics fontMetrics, LineMetrics lineMetrics, TextLayout textLayout,
			float advance, int spriteWidth, int xOffset) {

		// I have seen these values sometimes different from the values in LineMetrics,
		// in my very limited experience the values from TextLayout have worked better for small fonts,
		// but it was only one pixel different either way
		this.ascent = textLayout.getAscent();
		this.descent = textLayout.getDescent();
		this.leading = textLayout.getLeading();

		this.advance = advance;
		this.alphaImage = alphaImage;
		this.creationMetadata = null;
		this.name = fontMetrics.getFont().getFontName();
		this.pointSize = fontMetrics.getFont().getSize2D();
		this.spriteWidth = spriteWidth;
		this.strikethroughOffset = lineMetrics.getStrikethroughOffset();
		this.strikethroughThickness = lineMetrics.getStrikethroughThickness();
		this.style = fontMetrics.getFont().getStyle();
		this.underlineOffset = lineMetrics.getUnderlineOffset();
		this.underlineThickness = lineMetrics.getUnderlineThickness();
		this.xOffset = xOffset;
	}

	FontSpriteSheet(InputStream in) throws Exception {
		try (final PngReader reader = PngIO.reader(in)) {
			advance = reader.getRequiredFloat(TAG_ADVANCE);
			alphaImage = reader.readImage();
			ascent = reader.getRequiredFloat(TAG_ASCENT);
			descent = reader.getRequiredFloat(TAG_DESCENT);
			leading = reader.getRequiredFloat(TAG_LEADING);
			creationMetadata = reader.getRequiredString(CREATION_METADATA_TAG);
			name = reader.getRequiredString(TAG_NAME);
			pointSize = reader.getRequiredFloat(TAG_POINT_SIZE);
			spriteWidth = reader.getRequiredInt(TAG_SPRITE_WIDTH);
			strikethroughOffset = reader.getRequiredFloat(TAG_STRIKETHROUGH_OFFSET);
			strikethroughThickness = reader.getRequiredFloat(TAG_STRIKETHROUGH_THICKNESS);
			style = reader.getRequiredInt(TAG_STYLE);
			underlineOffset = reader.getRequiredFloat(TAG_UNDERLINE_OFFSET);
			underlineThickness = reader.getRequiredFloat(TAG_UNDERLINE_THICKNESS);
			xOffset = reader.getRequiredInt(TAG_X_OFFSET);
		}
	}

	float getAdvance() {
		return advance;
	}

	BufferedImage getAlphaImage() {
		return alphaImage;
	}

	public float getAscent() {
		return ascent;
	}

	public String getCreationMetadata() {
		return creationMetadata;
	}

	float getLeading() {
		return leading;
	}

	public String getName() {
		return name;
	}

	public float getPointSize() {
		return pointSize;
	}

	String getPreferredFilename() {
		return getName().replace(' ', '-') + "-" + formatPointSize() + ".png";
	}

	int getSpriteWidth() {
		return spriteWidth;
	}

	float getStrikethroughOffset() {
		return strikethroughOffset;
	}

	float getStrikethroughThickness() {
		return strikethroughThickness;
	}

	public int getStyle() {
		return style;
	}

	float getUnderlineOffset() {
		return underlineOffset;
	}

	float getUnderlineThickness() {
		return underlineThickness;
	}

	int getXOffset() {
		return xOffset;
	}

	@Override
	public String toString() {
		return getName() + " " + formatPointSize();
	}

	private String formatPointSize() {
		return getPointSize() % 1 > 0 ? Float.toString(getPointSize()) : Integer.toString((int) getPointSize());
	}

	public String dumpProperties() {
		final StringWriter w = new StringWriter();
		final PrintWriter p = new PrintWriter(w);

		if (creationMetadata == null) {
			p.format("creationMetadata       : null%n");
		} else {
			final String[] lines = creationMetadata.split("\n");
			for (int i = 0; i < lines.length; i++) {
				if (i == 0)
					p.format("creationMetadata       : %s%n", lines[0]);
				else
					p.format("                         %s%n", lines[i]);
			}
		}

		p
				.format("name                   : %s%n", name)
				.format("advance                : %f%n", advance)
				.format("ascent                 : %f%n", ascent)
				.format("descent                : %f%n", descent)
				.format("leading                : %f%n", leading)
				.format("pointSize              : %f%n", pointSize)
				.format("spriteWidth            : %d%n", spriteWidth)
				.format("strikethroughOffset    : %f%n", strikethroughOffset)
				.format("strikethroughThickness : %f%n", strikethroughThickness)
				.format("style                  : %d%n", style)
				.format("underlineOffset        : %f%n", underlineOffset)
				.format("underlineThickness     : %f%n", underlineThickness)
				.format("xOffset                : %d%n", xOffset);

		return w.toString();
	}

	//
	// Drawing
	//

	@SuppressWarnings("UnnecessaryLocalVariable")
	public void drawString(Graphics2D g, String s, float x, float y) {
		// We draw strings by blitting each char from an image that has all pixels set to the requested color
		// and has alpha values copied from alphaImage.
		//
		// As an alternative I tried making a ColorModel class that returns colorized pixels
		// with their alpha value read direct from alphaImage but drawing that way was 2 - 3 times slower
		// than the blitting approach.

		final Composite oldComposite = g.getComposite();
		g.setComposite(AlphaComposite.SrcOver);

		final BufferedImage colorizedImage = getOrCreateColorizedImage(g.getColor());
		final int height = colorizedImage.getHeight();
		final int srcTop = 0;
		final int srcBottom = height;
		final int destTop = round(y - ascent);
		final int destBottom = destTop + height;

		for (char c : s.toCharArray()) {
			if (c != ' ') {
				final int srcLeft = calculateSpriteIndex(c) * spriteWidth;
				final int srcRight = srcLeft + spriteWidth;
				final int destLeft = round(x - xOffset);
				final int destRight = destLeft + spriteWidth;

				g.drawImage(
						colorizedImage,
						destLeft, destTop, destRight, destBottom,
						srcLeft, srcTop, srcRight, srcBottom,
						null
				);
			}
			x += advance;
		}

		g.setComposite(oldComposite);
	}

	private int calculateSpriteIndex(char c) {
		if (c < MIN_CHAR_IN_SHEET || c > MAX_CHAR_IN_SHEET) {
			return 0;  // tofu
		} else {
			return c - MIN_CHAR_IN_SHEET + 1;
		}
	}

	private BufferedImage getOrCreateColorizedImage(Color color) {
		final int cacheKey = color.getRGB();
		final SoftReference<BufferedImage> ref = colorizedImageCache.get(cacheKey);
		BufferedImage image = ref != null ? ref.get() : null;
		if (image == null) {
			image = createColorizedImage(color);
			colorizedImageCache.put(cacheKey, new SoftReference<>(image));
		}
		return image;
	}

	private BufferedImage createColorizedImage(Color color) {
		final BufferedImage image = new BufferedImage(alphaImage.getWidth(), alphaImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
		final DataBuffer data = image.getRaster().getDataBuffer();
		final DataBuffer alphaData = alphaImage.getRaster().getDataBuffer();
		final float colorAlpha = color.getAlpha() / 255f;
		final int colorRgb = color.getRGB() & 0x00FFFFFF;
		final int dataSize = data.getSize();

		for (int i = 0; i < dataSize; i++) {
			data.setElem(i, colorRgb | calculateAlpha(colorAlpha, alphaData.getElem(i)));
		}

		return image;
	}

	private int calculateAlpha(float colorAlpha, int spriteAlpha) {
		// This calculation gets very close to matching what happens in Graphics2D.drawString(),
		// probably no-one will notice when it is different (more details in docs/font-sprite-sheets.md)
		return (round(colorAlpha * spriteAlpha) & 0xFF) << 24;
	}

	//
	// StringBounder
	//

	public Dimension2D calculateDimension(String text) {
		return new Dimension2DDouble(text.length() * advance, leading + ascent + descent);
	}

	public double getDescent() {
		return descent;
	}

	//
	// PNG Read / Write
	//

	private static final String TAG_ADVANCE = "PlantUml-FontSprite-Advance";
	private static final String TAG_ASCENT = "PlantUml-FontSprite-Ascent";
	private static final String TAG_DESCENT = "PlantUml-FontSprite-Descent";
	private static final String TAG_LEADING = "PlantUml-FontSprite-Leading";
	private static final String TAG_NAME = "PlantUml-FontSprite-Name";
	private static final String TAG_POINT_SIZE = "PlantUml-FontSprite-PointSize";
	private static final String TAG_SPRITE_WIDTH = "PlantUml-FontSprite-SpriteWidth";
	private static final String TAG_STRIKETHROUGH_OFFSET = "PlantUml-Strikethrough-Offset";
	private static final String TAG_STRIKETHROUGH_THICKNESS = "PlantUml-Strikethrough-Thickness";
	private static final String TAG_STYLE = "PlantUml-FontSprite-Style";
	private static final String TAG_UNDERLINE_OFFSET = "PlantUml-Underline-Offset";
	private static final String TAG_UNDERLINE_THICKNESS = "PlantUml-Underline-Thickness";
	private static final String TAG_X_OFFSET = "PlantUml-FontSprite-XOffset";

	void writeAsPNG(Path path) throws IOException {
		try (OutputStream os = newOutputStream(path)) {
			writeAsPNG(os);
		}
	}

	void writeAsPNG(OutputStream out) throws IOException {
		PngIO.writer()
				.creationMetadata()
				.text(TAG_ADVANCE, advance)
				.text(TAG_ASCENT, ascent)
				.text(TAG_DESCENT, descent)
				.text(TAG_LEADING, leading)
				.text(TAG_NAME, name)
				.text(TAG_POINT_SIZE, pointSize)
				.text(TAG_SPRITE_WIDTH, spriteWidth)
				.text(TAG_STRIKETHROUGH_OFFSET, strikethroughOffset)
				.text(TAG_STRIKETHROUGH_THICKNESS, strikethroughThickness)
				.text(TAG_STYLE, style)
				.text(TAG_UNDERLINE_OFFSET, underlineOffset)
				.text(TAG_UNDERLINE_THICKNESS, underlineThickness)
				.text(TAG_X_OFFSET, xOffset)
				.write(alphaImage, out);
	}
}

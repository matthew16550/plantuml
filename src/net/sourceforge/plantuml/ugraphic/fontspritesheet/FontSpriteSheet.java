package net.sourceforge.plantuml.ugraphic.fontspritesheet;

import static java.lang.Math.round;
import static java.nio.file.Files.newOutputStream;
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
import java.lang.ref.SoftReference;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.IIOImage;
import javax.imageio.stream.ImageInputStream;

import net.sourceforge.plantuml.Dimension2DDouble;
import net.sourceforge.plantuml.png.PngIOMetadata;
import net.sourceforge.plantuml.png.PngMetadataReader;
import net.sourceforge.plantuml.security.ImageIO;

public class FontSpriteSheet {

	private final Map<Integer, SoftReference<BufferedImage>> colorizedImageCache = new ConcurrentHashMap<>();
	private final float advance;
	private final BufferedImage alphaImage;
	private final float ascent;
	private final float descent;
	private final float leading;
	private final String metadata;
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
		this.metadata = createMetadata();
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

	FontSpriteSheet(InputStream in) throws IOException {
		try (ImageInputStream iis = ImageIO.createImageInputStream(in)) {
			final IIOImage iioImage = ImageIO.createImageReader(iis).readAll(0, null);
			final PngMetadataReader reader = new PngMetadataReader(iioImage.getMetadata());
			advance = reader.getRequiredFloat(TAG_ADVANCE);
			alphaImage = (BufferedImage) iioImage.getRenderedImage();
			ascent = reader.getRequiredFloat(TAG_ASCENT);
			descent = reader.getRequiredFloat(TAG_DESCENT);
			leading = reader.getRequiredFloat(TAG_LEADING);
			metadata = reader.getRequiredString(TAG_METADATA);
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

	private static String createMetadata() {
		final StringBuilder b = new StringBuilder();

		final String[] versionProperties = new String[]{
				"java.runtime.name", "java.runtime.version",
				"java.vendor", "java.vendor.version",
				"java.vm.name", "java.vm.version",
				"os.arch", "os.name", "os.version"
		};

		for (String p : versionProperties) {
			b.append(p).append('=').append(System.getProperty(p)).append('\n');
		}

		final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		b.append("timestamp=").append(df.format(new Date())).append('\n');

		return b.toString();
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

	float getLeading() {
		return leading;
	}

	String getMetadata() {
		return metadata;
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
		// This calculation gets very close to matching what happens in Graphics2D.drawString()
		// but some values are off by one when colorAlpha is between 128 and 254.
		//
		// I think it's because Graphics2D.drawString() uses floating point for alpha calculations
		// but alphaImage stores 8-bit integers so there is less accuracy here.

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
	private static final String TAG_METADATA = "PlantUml-FontSprite-Metadata";
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
		new PngIOMetadata()
				.addText(TAG_ADVANCE, advance)
				.addText(TAG_ASCENT, ascent)
				.addText(TAG_DESCENT, descent)
				.addText(TAG_LEADING, leading)
				.addText(TAG_METADATA, metadata)
				.addText(TAG_NAME, name)
				.addText(TAG_POINT_SIZE, pointSize)
				.addText(TAG_SPRITE_WIDTH, spriteWidth)
				.addText(TAG_STRIKETHROUGH_OFFSET, strikethroughOffset)
				.addText(TAG_STRIKETHROUGH_THICKNESS, strikethroughThickness)
				.addText(TAG_STYLE, style)
				.addText(TAG_UNDERLINE_OFFSET, underlineOffset)
				.addText(TAG_UNDERLINE_THICKNESS, underlineThickness)
				.addText(TAG_X_OFFSET, xOffset)
				.write(alphaImage, out);
	}
}

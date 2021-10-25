package net.sourceforge.plantuml.test;

import static net.sourceforge.plantuml.test.PathTestUtils.getFileExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.commons.util.ExceptionUtils.throwAsUncheckedException;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.opentest4j.AssertionFailedError;

import net.sourceforge.plantuml.graphic.color.ColorHSB;
import net.sourceforge.plantuml.png.PngIO;
import net.sourceforge.plantuml.security.SImageIO;

// Beware there is also https://github.com/assertj/assertj-swing which has some image comparisons that might help us.
// It does not compare using HSB, so we have built that ourselves.
public class ImageTestUtils {

	public static void assertImagesEqual(BufferedImage expected, BufferedImage actual) {
		assertImagesEqual(expected, actual, new Comparator<ColorHSB>() {
			@Override
			public int compare(ColorHSB expected, ColorHSB actual) {
				return expected.getRGB() - actual.getRGB();
			}
		});
	}

	/**
	 * Compares images using {@link ColorHSB}.
	 */
	public static void assertImagesEqual(BufferedImage expected, BufferedImage actual, Comparator<ColorHSB> comparator) {
		assertImageSizeEqual(expected, actual);

		final int height = expected.getHeight();
		final int width = expected.getWidth();

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				final ColorHSB expectedColor = new ColorHSB(expected.getRGB(x, y));
				final ColorHSB actualColor = new ColorHSB(actual.getRGB(x, y));
				if (comparator.compare(expectedColor, actualColor) != 0) {
					String expectedString = expectedColor.toString();
					String actualString = String.format("%s at:<[%d, %d]>", actualColor, x, y);
					throw new AssertionFailedError(
							String.format("expected:%s but was:%s", expectedString, actualString),
							expectedString, actualString
					);
				}
			}
		}
	}

	public static void assertImageSizeEqual(BufferedImage expected, BufferedImage actual) {
		assertThat(expected).isNotNull();
		assertThat(actual).isNotNull();

		final int expectedHeight = expected.getHeight();
		final int expectedWidth = expected.getWidth();

		final int actualHeight = actual.getHeight();
		final int actualWidth = actual.getWidth();

		if (expectedHeight != actualHeight || expectedWidth != actualWidth) {
			String expectedString = String.format("[width=%d height=%d]", expectedWidth, expectedHeight);
			String actualString = String.format("[width=%d height=%d]", actualWidth, actualHeight);
			throw new AssertionFailedError(
					String.format("expected:%s but was:%s", expectedString, actualString),
					expectedString, actualString
			);
		}
	}

	public static byte[] imageToBytes(BufferedImage image, String format) {
		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			SImageIO.write(image, format, baos);
			return baos.toByteArray();
		} catch (IOException e) {
			throwAsUncheckedException(e);
			return new byte[]{};  // this line will never run - but it appeases the compiler
		}
	}

	public static BufferedImage readImageFile(Path path) {
		try {
			return SImageIO.read(path.toFile());
		} catch (IOException e) {
			throwAsUncheckedException(e);
			return new BufferedImage(0, 0, 0);  // this line will never run - but it appeases the compiler
		}
	}

	public static void writeImageFile(BufferedImage image, Path path) {
		final String formatName = getFileExtension(path)
				.orElseThrow(() -> new IllegalArgumentException(String.format("Path has no extension: '%s'", path)));
		try {
			if (!SImageIO.write(image, formatName, path.toFile())) {
				throw new IOException(String.format("No suitable image writer found for '%s'", path));
			}
		} catch (IOException e) {
			throwAsUncheckedException(e);
		}
	}

	public static void writePngWithCreationMetadata(BufferedImage image, Path path) {
		try (OutputStream os = Files.newOutputStream(path)) {
			PngIO.writer()
					.creationMetadata()
					.write(image, os);
		} catch (IOException e) {
			throwAsUncheckedException(e);
		}
	}
}

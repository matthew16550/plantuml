package net.sourceforge.plantuml.test;

import static net.sourceforge.plantuml.test.ColorComparators.COMPARE_PIXEL_EXACT;

import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.Objects;

import org.opentest4j.AssertionFailedError;

import net.sourceforge.plantuml.graphic.color.ColorHSB;

public class Assertions {

	public static void assertImagesEqual(BufferedImage expected, BufferedImage actual) {
		assertImagesEqual(expected, actual, COMPARE_PIXEL_EXACT, 0);
	}

	public static void assertImagesSameSize(BufferedImage expected, BufferedImage actual) {
		Objects.requireNonNull(expected);
		Objects.requireNonNull(actual);

		final int expectedHeight = expected.getHeight();
		final int expectedWidth = expected.getWidth();

		final int actualHeight = actual.getHeight();
		final int actualWidth = actual.getWidth();

		if (expectedHeight != actualHeight || expectedWidth != actualWidth) {
			assertionFailed(
					String.format("[width=%d height=%d]", expectedWidth, expectedHeight),
					String.format("[width=%d height=%d]", actualWidth, actualHeight)
			);
		}
	}

	public static void assertImagesEqual(BufferedImage expected, BufferedImage actual, Comparator<ColorHSB> comparator, int maxDifferentPixels) {
		assertImagesSameSize(expected, actual);

		final int height = expected.getHeight();
		final int width = expected.getWidth();

		int differentCount = 0;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				final ColorHSB expectedColor = new ColorHSB(expected.getRGB(x, y));
				final ColorHSB actualColor = new ColorHSB(actual.getRGB(x, y));
				if (comparator.compare(expectedColor, actualColor) != 0) {
					if (expectedColor.getHue() == actualColor.getHue() && differentCount < maxDifferentPixels) {
						differentCount++;
						continue;
					}
					assertionFailed(
							expectedColor.toString(),
							String.format("%s at:<[%d, %d]> using %s", actualColor, x, y, comparator)
					);
				}
			}
		}
	}

	// We use AssertionFailedError because some IDEs can nicely show the "expected" & "actual" details
	private static void assertionFailed(String expected, String actual) {
		final String message = String.format("expected:%s but was:%s", expected, actual);
		throw new AssertionFailedError(message, expected, actual);
	}
}

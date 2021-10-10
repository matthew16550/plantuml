package net.sourceforge.plantuml.test;

import static net.sourceforge.plantuml.StringUtils.substringAfterLast;
import static org.junit.platform.commons.util.ExceptionUtils.throwAsUncheckedException;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

public class ImageTestUtils {
	
	public static byte[] imageToBytes(BufferedImage image, String format) {
		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, format, baos);
			return baos.toByteArray();
		} catch (Exception e) {
			throwAsUncheckedException(e);
			return new byte[]{};  // this line will never run - but it appeases the compiler
		}
	}

	public static BufferedImage readImageFile(Path path) {
		try {
			return ImageIO.read(path.toFile());
		} catch (Exception e) {
			throwAsUncheckedException(e);
			return new BufferedImage(0, 0, 0);  // this line will never run - but it appeases the compiler
		}
	}

	public static void writeImageFile(Path path, BufferedImage image) {
		try {
			final String format = substringAfterLast(path.toString(), '.');
			boolean failed = !ImageIO.write(image, format, path.toFile());
			if (failed) {
				throw new IOException(String.format("No appropriate image writer found for '%s'", path));
			}
		} catch (Exception e) {
			throwAsUncheckedException(e);
		}
	}
}

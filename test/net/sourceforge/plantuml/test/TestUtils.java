package net.sourceforge.plantuml.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static net.sourceforge.plantuml.StringUtils.substringAfterLast;
import static org.junit.platform.commons.util.ExceptionUtils.throwAsUncheckedException;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;

import javax.imageio.ImageIO;

public class TestUtils {

	public static Path createFile(Path path, FileAttribute<?>... attrs) {
		try {
			return Files.createFile(path, attrs);
		} catch (Exception e) {
			throwAsUncheckedException(e);
			return Paths.get("");  // this line will never run - but it appeases the compiler
		}
	}

	public static String readUtf8File(Path path) {
		try {
			return new String(readAllBytes(path), UTF_8);
		} catch (Exception e) {
			throwAsUncheckedException(e);
			return "";  // this line will never run - but it appeases the compiler
		}
	}

	public static void writeUtf8File(Path path, String string) {
		try {
			Files.createDirectories(path.getParent());
			Files.write(path, string.getBytes(UTF_8));
		} catch (Exception e) {
			throwAsUncheckedException(e);
		}
	}

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

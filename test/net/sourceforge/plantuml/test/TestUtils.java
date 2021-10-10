package net.sourceforge.plantuml.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static net.sourceforge.plantuml.FileFormat.BUFFERED_IMAGE;
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

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.Option;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.api.ImageDataBufferedImage;

public class TestUtils {

	public static byte[] exportOneDiagramToByteArray(String source, FileFormat fileFormat, String... options) {
		try {
			final Option option = new Option(options);
			option.setFileFormatOption(new FileFormatOption(fileFormat));

			final SourceStringReader ssr = new SourceStringReader(option.getDefaultDefines(), source, UTF_8.name(), option.getConfig());

			final ByteArrayOutputStream os = new ByteArrayOutputStream();

			ssr.getBlocks().get(0).getDiagram().exportDiagram(os, 0, option.getFileFormatOption());

			return os.toByteArray();
		} catch (Exception e) {
			throwAsUncheckedException(e);
			return new byte[]{};  // this line will never run - but it appeases the compiler
		}
	}

	public static BufferedImage renderAsImage(String... source) {

		try {
			final Option option = new Option(new String[]{});
			option.setFileFormatOption(new FileFormatOption(BUFFERED_IMAGE));

			final SourceStringReader ssr = new SourceStringReader(option.getDefaultDefines(), String.join("\n", source), UTF_8.name(), option.getConfig());

			final ByteArrayOutputStream os = new ByteArrayOutputStream();

			ImageDataBufferedImage imageData = (ImageDataBufferedImage) ssr.getBlocks().get(0).getDiagram().exportDiagram(os, 0, option.getFileFormatOption());

			return imageData.getImage();
		} catch (Exception e) {
			throwAsUncheckedException(e);
			return new BufferedImage(0, 0, 0);  // this line will never run - but it appeases the compiler
		}
	}

	public static String renderAsUnicode(String source, String... options) {

		final byte[] bytes = exportOneDiagramToByteArray(source, FileFormat.UTXT, options);
		return new String(bytes, UTF_8);
	}

	public static String renderUmlAsUnicode(String source, String... options) {

		return renderAsUnicode("@startuml\n" + source + "\n@enduml\n", options);
	}

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

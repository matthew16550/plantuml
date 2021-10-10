package net.sourceforge.plantuml.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.util.Collections.emptyList;
import static net.sourceforge.plantuml.StringUtils.substringAfterLast;
import static org.assertj.core.api.Assertions.assertThat;
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
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.api.ImageDataBufferedImage;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.core.ImageData;
import net.sourceforge.plantuml.error.PSystemError;
import net.sourceforge.plantuml.preproc.Defines;

public class TestUtils {

	public static ExportOneDiagram exportDiagram(String... source) {
		final SourceStringReader ssr = new SourceStringReader(Defines.createEmpty(), String.join("\n", source), UTF_8.name(), emptyList());
		final Diagram diagram = ssr.getBlocks().get(0).getDiagram();
		return new ExportOneDiagram(diagram);
	}

	public static class ExportOneDiagram {
		private final Diagram diagram;
		private boolean metadata;

		public ExportOneDiagram(Diagram diagram) {
			this.diagram = diagram;
		}

		public ExportOneDiagram assertNoError() {
			assertThat(diagram)
					.isNotInstanceOf(PSystemError.class);
			return this;
		}

		public byte[] toByteArray(FileFormat fileFormat) {
			try {
				final ByteArrayOutputStream os = new ByteArrayOutputStream();
				toStream(os, fileFormat);
				return os.toByteArray();
			} catch (Exception e) {
				throwAsUncheckedException(e);
				return new byte[]{};  // this line will never run - but it appeases the compiler
			}
		}

		public BufferedImage toImage() {
			try {
				final ByteArrayOutputStream os = new ByteArrayOutputStream();
				final ImageDataBufferedImage imageData = (ImageDataBufferedImage) toStream(os, FileFormat.BUFFERED_IMAGE);
				return imageData.getImage();
			} catch (Exception e) {
				throwAsUncheckedException(e);
				return new BufferedImage(0, 0, 0);  // this line will never run - but it appeases the compiler
			}
		}

		public String toString() {
			return toString(FileFormat.UTXT);
		}

		public String toString(FileFormat fileFormat) {
			try {
				final byte[] bytes = toByteArray(fileFormat);
				return new String(bytes, UTF_8);
			} catch (Exception e) {
				throwAsUncheckedException(e);
				return "";  // this line will never run - but it appeases the compiler
			}
		}

		public ExportOneDiagram withMetadata(boolean metadata) {
			this.metadata = metadata;
			return this;
		}

		private ImageData toStream(ByteArrayOutputStream os, FileFormat fileFormat) throws IOException {
			final FileFormatOption fileFormatOption = new FileFormatOption(fileFormat, metadata);
			return diagram.exportDiagram(os, 0, fileFormatOption);
		}
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

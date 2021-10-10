package net.sourceforge.plantuml.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.commons.util.ExceptionUtils.throwAsUncheckedException;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.api.ImageDataBufferedImage;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.core.ImageData;
import net.sourceforge.plantuml.error.PSystemError;
import net.sourceforge.plantuml.preproc.Defines;

public class PlantUmlTestUtils {
	
	public static ExportDiagram exportDiagram(String... source) {
		final SourceStringReader ssr = new SourceStringReader(Defines.createEmpty(), String.join("\n", source), UTF_8.name(), emptyList());
		final Diagram diagram = ssr.getBlocks().get(0).getDiagram();
		return new ExportDiagram(diagram);
	}

	public static class ExportDiagram {
		private final Diagram diagram;
		private boolean metadata;

		public ExportDiagram(Diagram diagram) {
			this.diagram = diagram;
		}

		public ExportDiagram assertNoError() {
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

		public ExportDiagram withMetadata(boolean metadata) {
			this.metadata = metadata;
			return this;
		}

		private ImageData toStream(ByteArrayOutputStream os, FileFormat fileFormat) throws IOException {
			final FileFormatOption fileFormatOption = new FileFormatOption(fileFormat, metadata);
			return diagram.exportDiagram(os, 0, fileFormatOption);
		}
	}
}

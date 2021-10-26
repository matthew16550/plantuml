package net.sourceforge.plantuml.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.commons.util.ExceptionUtils.throwAsUncheckedException;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.error.PSystemError;
import net.sourceforge.plantuml.security.SImageIO;

public class PlantUmlTestUtils {

	public static ExportDiagram exportDiagram(String... source) {
		final SourceStringReader ssr = new SourceStringReader(StringTestUtils.join("\n", source));

		final List<BlockUml> blocks = ssr.getBlocks();
		if (blocks.isEmpty()) throw new AssertionError("There is no diagram");

		final Diagram diagram = blocks.get(0).getDiagram();
		return new ExportDiagram(diagram);
	}

	@SuppressWarnings("unused")
	public static class ExportDiagram {
		private final Diagram diagram;
		private boolean metadata;

		public ExportDiagram(Diagram diagram) {
			this.diagram = diagram;
		}

		public ExportDiagram assertDiagramType(Class<? extends Diagram> klass) {
			assertNoError();
			assertThat(diagram).isInstanceOf(klass);
			return this;
		}

		public ExportDiagram assertNoError() {
			if (diagram instanceof PSystemError) {
				final PSystemError error = (PSystemError) this.diagram;
				throw new AssertionError("Diagram has an error: " + StringTestUtils.join("\n", error.getPureAsciiFormatted()));
			}
			return this;
		}

		public byte[] asByteArray(FileFormat fileFormat) {
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			stream(os, fileFormat);
			return os.toByteArray();
		}

		public BufferedImage asImage() {
			return asImage(FileFormat.PNG);
		}

		public BufferedImage asImage(FileFormat fileFormat) {
			try {
				return SImageIO.read(asByteArray(FileFormat.PNG));
			} catch (IOException e) {
				throwAsUncheckedException(e);
				return new BufferedImage(0, 0, 0);  // this line will never run - but it appeases the compiler
			}
		}

		public String asString() {
			return asString(FileFormat.UTXT);
		}

		public String asString(FileFormat fileFormat) {
			return new String(asByteArray(fileFormat), UTF_8);
		}

		public ExportDiagram stream(OutputStream os, FileFormat fileFormat) {
			try {
				diagram.exportDiagram(os, 0, new FileFormatOption(fileFormat, metadata));
			} catch (IOException e) {
				throwAsUncheckedException(e);
			}
			return this;
		}

		public ExportDiagram toFile(Path path, FileFormat fileFormat) throws IOException {
			try (OutputStream os = Files.newOutputStream(path)) {
				return stream(os, fileFormat);
			}
		}

		public ExportDiagram withMetadata(boolean metadata) {
			this.metadata = metadata;
			return this;
		}
	}
}

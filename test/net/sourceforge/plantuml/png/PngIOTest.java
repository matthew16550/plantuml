package net.sourceforge.plantuml.png;

import static net.sourceforge.plantuml.png.PngIO.readPlantUmlMetadata;
import static net.sourceforge.plantuml.test.PlantUmlTestUtils.exportDiagram;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.sourceforge.plantuml.FileFormat;

class PngIOTest {

	@Test
	void test_readPlantUmlMetadata(@TempDir Path dir) throws Exception {
		final Path file = dir.resolve("example.puml");

		try (OutputStream os = Files.newOutputStream(file)) {
			exportDiagram(
					"@startuml",
					"a -> a",
					"@enduml"
			)
					.withMetadata(true)
					.stream(os, FileFormat.PNG);
		}

		final String metadataFromFile = readPlantUmlMetadata(file.toFile());

		assertThat(metadataFromFile)
				.startsWith("@startuml")
				.contains(
						"a -> a",
						"PlantUML version"
				);
	}
}

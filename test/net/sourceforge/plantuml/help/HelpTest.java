package net.sourceforge.plantuml.help;

import static net.sourceforge.plantuml.test.PlantUmlTestUtils.exportDiagram;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HelpTest {

	@Test
	public void test_help_themes() {

		final String output = exportDiagram(
				"@startuml",
				"help themes",
				"@enduml"
		).toString();
		
		assertThat(output)
				.startsWith("Help on themes")
				.contains("bluegray", "hacker");
	}
}

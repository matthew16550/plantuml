package net.sourceforge.plantuml.approvaltesting.example;

import static net.sourceforge.plantuml.test.TestUtils.exportOneDiagram;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.approvaltesting.ApprovalTesting;

class Example {

	@RegisterExtension
	static final ApprovalTesting approvalTesting = new ApprovalTesting();

	private static final String[] SOURCE = new String[]{
			"@startuml",
			"a -> b",
			"@enduml"
	};

	@Test
	void test_export_png() {
		final BufferedImage image = exportOneDiagram(SOURCE)
				.assertNoError()
				.toImage();
		approvalTesting.approve(image);
	}

	@Test
	void test_export_ascii() {
		final String string = exportOneDiagram(SOURCE)
				.assertNoError()
				.toString(FileFormat.ATXT);
		approvalTesting.approve(string);
	}

	@ParameterizedTest(name = "{arguments}")
	@EnumSource(
			value = FileFormat.class,
			names = {"ATXT", "DEBUG", "EPS", "HTML5", "LATEX", "SVG", "UTXT", "VDX"}
	)
	void test_export_many(FileFormat fileFormat) {
		final String string = exportOneDiagram(SOURCE)
				.assertNoError()
				.withMetadata(false)
				.toString(fileFormat);

		approvalTesting
				.withExtension(fileFormat.getFileSuffix())
				.approve(string);
	}
}

package net.sourceforge.plantuml.approvaltesting;

import static net.sourceforge.plantuml.test.ImageTestUtils.imageToBytes;
import static net.sourceforge.plantuml.test.PathTestUtils.assertThatDirContainsExactlyTheseFiles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

class ApprovalTestingTest_Approve_PNG {

	@TempDir
	static Path dir;

	@RegisterExtension
	static ApprovalTesting approvalTesting = new ApprovalTesting()
			.withDuplicateFiles(true);

	@Test
	void test() {

		// Approve the initial value

		final BufferedImage value = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		value.createGraphics().drawRect(2, 3, 5, 3);

		approvalTesting
				.withDir(dir)
				.approve(value);

		assertThatDirContainsExactlyTheseFiles(dir,
				"ApprovalTestingTest_Approve_PNG.test.approved.png"
		);

		assertThat(dir.resolve("ApprovalTestingTest_Approve_PNG.test.approved.png"))
				.hasBinaryContent(imageToBytes(value, "png"));

		// Change the value then approve() should fail

		value.setRGB(1, 2, 0xAA112233);

		assertThatExceptionOfType(AssertionError.class)
				.isThrownBy(() ->
						approvalTesting
								.withDir(dir)
								.approve(value)
				)
				.withMessage("" +
						"expected:ColorHSB[a=00 r=00 g=00 b=00 / h=0.000000 s=0.000000 b=0.000000] " +
						"but was:ColorHSB[a=AA r=11 g=22 b=33 / h=0.583333 s=0.666667 b=0.200000] " +
						"at:<[1, 2]> using COMPARE_PIXEL_EXACT"
				);

		assertThatDirContainsExactlyTheseFiles(dir,
				"ApprovalTestingTest_Approve_PNG.test.approved.png",
				"ApprovalTestingTest_Approve_PNG.test.failed.png"
		);

		assertThat(dir.resolve("ApprovalTestingTest_Approve_PNG.test.failed.png"))
				.hasBinaryContent(imageToBytes(value, "png"));
	}
}
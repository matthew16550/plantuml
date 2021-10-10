package net.sourceforge.plantuml.approvaltesting;

import static net.sourceforge.plantuml.test.TestUtils.imageToBytes;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ApprovalTestingTest_Approve_BMP extends AbstractTempDirTest {

	@RegisterExtension
	static ApprovalTesting approvalTesting = new ApprovalTesting()
			.withDuplicateFiles(true);

	@Test
	void test() {

		// Approve the initial value

		final BufferedImage value = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
		value.createGraphics().drawRect(2, 3, 5, 3);

		approvalTesting
				.withDir(dir)
				.withExtension(".bmp")
				.approve(value);

		assertThatDirContainsExactlyTheseFiles(
				"ApprovalTestingTest_Approve_BMP.test.approved.bmp"
		);

		assertThatFile("ApprovalTestingTest_Approve_BMP.test.approved.bmp")
				.hasBinaryContent(imageToBytes(value, "bmp"));

		// Change the value then approve() should fail

		value.setRGB(1, 2, 0xFF112233);

		assertThatExceptionOfType(AssertionError.class)
				.isThrownBy(() ->
						approvalTesting
								.withDir(dir)
								.withExtension(".bmp")
								.approve(value)

				)
				.withMessage("" +
						"expected:ColorHSB[a=FF r=00 g=00 b=00 / h=0.000000 s=0.000000 b=0.000000] " +
						"but was:ColorHSB[a=FF r=11 g=22 b=33 / h=0.583333 s=0.666667 b=0.200000] " +
						"at:<[1, 2]> using COMPARE_PIXEL_EXACT"
				);

		assertThatDirContainsExactlyTheseFiles(
				"ApprovalTestingTest_Approve_BMP.test.approved.bmp",
				"ApprovalTestingTest_Approve_BMP.test.failed.bmp"
		);

		assertThatFile("ApprovalTestingTest_Approve_BMP.test.failed.bmp")
				.hasBinaryContent(imageToBytes(value, "bmp"));
	}
}

package net.sourceforge.plantuml.test.approval;

import static net.sourceforge.plantuml.StringUtils.LINE_SEPARATOR;
import static net.sourceforge.plantuml.test.ImageTestUtils.assertImagesEqual;
import static net.sourceforge.plantuml.test.ImageTestUtils.imageToBytes;
import static net.sourceforge.plantuml.test.ImageTestUtils.readImageFile;
import static net.sourceforge.plantuml.test.PathTestUtils.assertThatDirContainsExactlyTheseFiles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import net.sourceforge.plantuml.test.logger.TestLoggerCapture;
import net.sourceforge.plantuml.test.outputs.TestOutputs;

class ApprovalTestingTest {

	@TempDir
	Path dir;

	@RegisterExtension
	final ApprovalTesting approvalTesting = new ApprovalTesting();

	@BeforeEach
	void beforeEach(TestOutputs testOutputs) {
		testOutputs.dir(dir);
	}

	@Test
	void test_approve_txt(TestOutputs testOutputs) {
		testOutputs.reusePaths(true);  // kludge for this test

		final String initialValue = "foo";

		// Approve the initial value

		assertThatNoException()
				.isThrownBy(() -> approvalTesting.approve(initialValue));

		assertThatDirContainsExactlyTheseFiles(dir,
				"ApprovalTestingTest.test_approve_txt.approved.txt"
		);

		assertThat(dir.resolve("ApprovalTestingTest.test_approve_txt.approved.txt"))
				.hasContent(initialValue);

		// With a different value, approve() should fail and the "failed" file is written

		final String differentValue = "bar";

		assertThatExceptionOfType(AssertionError.class)
				.isThrownBy(() -> approvalTesting.approve(differentValue))
				.withMessage(LINE_SEPARATOR +
						"expected: \"foo\"" + LINE_SEPARATOR +
						" but was: \"bar\""
				);

		assertThatDirContainsExactlyTheseFiles(dir,
				"ApprovalTestingTest.test_approve_txt.approved.txt",
				"ApprovalTestingTest.test_approve_txt.failed.txt"
		);

		assertThat(dir.resolve("ApprovalTestingTest.test_approve_txt.failed.txt"))
				.hasContent(differentValue);

		// Back to the initial value, approve() should pass and the "failed" file is deleted

		assertThatNoException()
				.isThrownBy(() -> approvalTesting.approve(initialValue));

		assertThatDirContainsExactlyTheseFiles(dir,
				"ApprovalTestingTest.test_approve_txt.approved.txt"
		);
	}

	@Test
	void test_approve_png(TestOutputs testOutputs, TestLoggerCapture testLoggerCapture) {
		testOutputs.reusePaths(true);  // kludge for this test

		final BufferedImage initialValue = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		initialValue.createGraphics().drawRect(2, 3, 5, 3);

		// Approve the initial value

		assertThatNoException()
				.isThrownBy(() -> approvalTesting.approve(initialValue));

		assertThatDirContainsExactlyTheseFiles(dir,
				"ApprovalTestingTest.test_approve_png.approved.png"
		);

		assertImagesEqual(initialValue, readImageFile(dir.resolve("ApprovalTestingTest.test_approve_png.approved.png")));

		// With a different value, approve() should fail and the "failed" file is written

		final BufferedImage differentValue = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		initialValue.createGraphics().drawOval(2, 3, 5, 3);

		assertThatExceptionOfType(AssertionError.class)
				.isThrownBy(() -> approvalTesting.approve(differentValue))
				.withMessage("" +
						"expected:ColorHSB[a=FF r=FF g=FF b=FF / h=0.000000 s=0.000000 b=1.000000]" +
						" but was:ColorHSB[a=00 r=00 g=00 b=00 / h=0.000000 s=0.000000 b=0.000000]" +
						" at:<[2, 3]>"
				);

		assertThat(testLoggerCapture.getCaptured())
				.startsWith("INFO    : <PlantUml-Creation-Metadata> for 'ApprovalTestingTest.test_approve_png.approved.png'");

		assertThatDirContainsExactlyTheseFiles(dir,
				"ApprovalTestingTest.test_approve_png.approved.png",
				"ApprovalTestingTest.test_approve_png.failed.png"
		);

		assertImagesEqual(differentValue, readImageFile(dir.resolve("ApprovalTestingTest.test_approve_png.failed.png")));

		// Back to the initial value, approve() should pass and the "failed" file is deleted

		assertThatNoException()
				.isThrownBy(() -> approvalTesting.approve(initialValue));

		assertThatDirContainsExactlyTheseFiles(dir,
				"ApprovalTestingTest.test_approve_png.approved.png"
		);
	}

	@Test
	void test_approve_bmp(TestOutputs outputs) {
		outputs.reusePaths(true);  // kludge for this test

		final BufferedImage initialValue = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
		initialValue.createGraphics().drawRect(2, 3, 5, 3);

		// Approve the initial value

		assertThatNoException()
				.isThrownBy(() -> approvalTesting.withExtension(".bmp").approve(initialValue));

		assertThatDirContainsExactlyTheseFiles(dir,
				"ApprovalTestingTest.test_approve_bmp.approved.bmp"
		);

		assertThat(dir.resolve("ApprovalTestingTest.test_approve_bmp.approved.bmp"))
				.hasBinaryContent(imageToBytes(initialValue, "bmp"));

		// With a different value, approve() should fail and the "failed" file is written

		final BufferedImage differentValue = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
		initialValue.createGraphics().drawOval(2, 3, 5, 3);

		assertThatExceptionOfType(AssertionError.class)
				.isThrownBy(() -> approvalTesting.withExtension(".bmp").approve(differentValue))
				.withMessage("" +
						"expected:ColorHSB[a=FF r=FF g=FF b=FF / h=0.000000 s=0.000000 b=1.000000]" +
						" but was:ColorHSB[a=FF r=00 g=00 b=00 / h=0.000000 s=0.000000 b=0.000000]" +
						" at:<[2, 3]>"
				);

		assertThatDirContainsExactlyTheseFiles(dir,
				"ApprovalTestingTest.test_approve_bmp.approved.bmp",
				"ApprovalTestingTest.test_approve_bmp.failed.bmp"
		);

		assertThat(dir.resolve("ApprovalTestingTest.test_approve_bmp.failed.bmp"))
				.hasBinaryContent(imageToBytes(differentValue, "bmp"));

		// Back to the initial value, approve() should pass and the "failed" file is deleted

		assertThatNoException()
				.isThrownBy(() -> approvalTesting.withExtension(".bmp").approve(initialValue));

		assertThatDirContainsExactlyTheseFiles(dir,
				"ApprovalTestingTest.test_approve_bmp.approved.bmp"
		);
	}

	@Test
	void test_withExtension() {
		approvalTesting
				.withExtension(".foo")
				.approve("bar");

		assertThatDirContainsExactlyTheseFiles(dir,
				"ApprovalTestingTest.test_withExtension.approved.foo"
		);
	}

	@Test
	void test_withName() {
		approvalTesting
				.withName("foo")
				.approve("bar");

		approvalTesting
				.withName("foo-%d", 2)
				.approve("bar");

		assertThatDirContainsExactlyTheseFiles(dir,
				"ApprovalTestingTest.test_withName.foo.approved.txt",
				"ApprovalTestingTest.test_withName.foo-2.approved.txt"
		);
	}
}

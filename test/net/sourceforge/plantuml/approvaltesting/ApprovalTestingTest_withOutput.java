package net.sourceforge.plantuml.approvaltesting;

import static net.sourceforge.plantuml.test.FileTestUtils.writeUtf8File;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.sourceforge.plantuml.test.AbstractTempDirTest;

class ApprovalTestingTest_withOutput extends AbstractTempDirTest {

	@RegisterExtension
	static ApprovalTesting approvalTesting = new ApprovalTesting()
			.withDuplicateFiles(true);

	@Test
	void test() {

		givenFile("ApprovalTestingTest_withOutput.test.approved.txt")
				.contains("foo");

		givenFiles(
				"ApprovalTestingTest_withOutput.other_test.approved.txt",
				"ApprovalTestingTest_withOutput.other_test.failed.txt",
				"ApprovalTestingTest_withOutput.other_test.OUTPUT.failed.txt"
		).exist();

		// With a bad value, approve() should fail

		final Consumer<String> doApprove = value ->
				approvalTesting
						.withDir(dir)
						.withOutput("OUTPUT", ".txt", path -> writeUtf8File(path, "123"))
						.approve(value);

		assertThatExceptionOfType(AssertionError.class)
				.isThrownBy(() -> doApprove.accept("bar"));

		assertThatDirContainsExactlyTheseFiles(
				"ApprovalTestingTest_withOutput.test.approved.txt",
				"ApprovalTestingTest_withOutput.test.failed.txt",
				"ApprovalTestingTest_withOutput.test.OUTPUT.failed.txt",

				"ApprovalTestingTest_withOutput.other_test.approved.txt",
				"ApprovalTestingTest_withOutput.other_test.failed.txt",
				"ApprovalTestingTest_withOutput.other_test.OUTPUT.failed.txt"
		);

		assertThatFile("ApprovalTestingTest_withOutput.test.failed.txt")
				.hasContent("bar");

		assertThatFile("ApprovalTestingTest_withOutput.test.OUTPUT.failed.txt")
				.hasContent("123");

		// With the correct value, approve() should pass and remove the relevant "failed" files

		doApprove.accept("foo");

		assertThatDirContainsExactlyTheseFiles(
				"ApprovalTestingTest_withOutput.test.approved.txt",

				"ApprovalTestingTest_withOutput.other_test.approved.txt",
				"ApprovalTestingTest_withOutput.other_test.failed.txt",
				"ApprovalTestingTest_withOutput.other_test.OUTPUT.failed.txt"
		);
	}
}

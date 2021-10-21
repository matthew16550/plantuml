package net.sourceforge.plantuml.approvaltesting;

import static net.sourceforge.plantuml.test.FileTestUtils.createFile;
import static net.sourceforge.plantuml.test.FileTestUtils.writeUtf8File;
import static net.sourceforge.plantuml.test.PathTestUtils.assertThatDirContainsExactlyTheseFiles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

class ApprovalTestingTest_withOutput {

	@TempDir
	static Path dir;

	@RegisterExtension
	static ApprovalTesting approvalTesting = new ApprovalTesting()
			.withDuplicateFiles(true);

	@Test
	void test() {

		writeUtf8File(dir.resolve("ApprovalTestingTest_withOutput.test.approved.txt"), "foo");

		Stream.of(
				"ApprovalTestingTest_withOutput.other_test.approved.txt",
				"ApprovalTestingTest_withOutput.other_test.failed.txt",
				"ApprovalTestingTest_withOutput.other_test.OUTPUT.failed.txt"
		).forEach(file ->
				createFile(dir.resolve(file))
		);

		// With a bad value, approve() should fail

		final Consumer<String> doApprove = value ->
				approvalTesting
						.withDir(dir)
						.withOutput("OUTPUT", ".txt", path -> writeUtf8File(path, "123"))
						.approve(value);

		assertThatExceptionOfType(AssertionError.class)
				.isThrownBy(() -> doApprove.accept("bar"));

		assertThatDirContainsExactlyTheseFiles(dir,
				"ApprovalTestingTest_withOutput.test.approved.txt",
				"ApprovalTestingTest_withOutput.test.failed.txt",
				"ApprovalTestingTest_withOutput.test.OUTPUT.failed.txt",

				"ApprovalTestingTest_withOutput.other_test.approved.txt",
				"ApprovalTestingTest_withOutput.other_test.failed.txt",
				"ApprovalTestingTest_withOutput.other_test.OUTPUT.failed.txt"
		);

		assertThat(dir.resolve("ApprovalTestingTest_withOutput.test.failed.txt"))
				.hasContent("bar");

		assertThat(dir.resolve("ApprovalTestingTest_withOutput.test.OUTPUT.failed.txt"))
				.hasContent("123");

		// With the correct value, approve() should pass and remove the relevant "failed" files

		doApprove.accept("foo");

		assertThatDirContainsExactlyTheseFiles(dir,
				"ApprovalTestingTest_withOutput.test.approved.txt",

				"ApprovalTestingTest_withOutput.other_test.approved.txt",
				"ApprovalTestingTest_withOutput.other_test.failed.txt",
				"ApprovalTestingTest_withOutput.other_test.OUTPUT.failed.txt"
		);
	}
}

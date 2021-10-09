package net.sourceforge.plantuml.approvaltesting;

import static net.sourceforge.plantuml.test.PathUtils.listAllFilesRecursive;
import static net.sourceforge.plantuml.test.TestUtils.writeUtf8File;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import net.sourceforge.plantuml.utils.functional.SingleCallback;

class ApprovalTestingTest_withOutput {

	@RegisterExtension
	static ApprovalTestingImpl approvalTesting = new ApprovalTestingImpl()
			.withDuplicateFiles(true);

	@TempDir
	static Path dir;

	@Test
	void test() throws Exception {

		final SingleCallback<String> doApprove = value ->
				approvalTesting
						.withDir(dir)
						.withOutput("BAR", ".txt", path -> writeUtf8File(path, "123"))
						.approve(value);

		// Approve the initial value

		doApprove.call("foo");

		assertThat(listAllFilesRecursive(dir))
				.containsExactlyInAnyOrder("ApprovalTestingTest_withOutput.test.approved.txt");

		// Change the value then approve() should fail
		
		assertThatExceptionOfType(AssertionError.class)
				.isThrownBy(() -> doApprove.call("bar"));

		assertThat(listAllFilesRecursive(dir))
				.containsExactlyInAnyOrder(
						"ApprovalTestingTest_withOutput.test.approved.txt",
						"ApprovalTestingTest_withOutput.test.failed.txt",
						"ApprovalTestingTest_withOutput.test.BAR.failed.txt"
				);

		assertThat(dir.resolve("ApprovalTestingTest_withOutput.test.failed.txt"))
				.hasContent("bar");

		assertThat(dir.resolve("ApprovalTestingTest_withOutput.test.BAR.failed.txt"))
				.hasContent("123");

		// Fix the value then approve() should pass and remove the "failed" files

		doApprove.call("foo");

		assertThat(listAllFilesRecursive(dir))
				.containsExactlyInAnyOrder("ApprovalTestingTest_withOutput.test.approved.txt");
	}
}

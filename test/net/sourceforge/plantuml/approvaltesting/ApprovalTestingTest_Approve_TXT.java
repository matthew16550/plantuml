package net.sourceforge.plantuml.approvaltesting;

import static net.sourceforge.plantuml.StringUtils.EOL;
import static net.sourceforge.plantuml.test.PathUtils.listAllFilesRecursive;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

class ApprovalTestingTest_Approve_TXT {

	@RegisterExtension
	static ApprovalTestingImpl approvalTesting = new ApprovalTestingImpl()
			.withDuplicateFiles(true);

	@TempDir
	static Path dir;

	@Test
	void test() throws Exception {

		// Approve the initial value

		final String value = "foo";

		approvalTesting
				.withDir(dir)
				.approve(value);

		assertThat(listAllFilesRecursive(dir))
				.containsExactly("ApprovalTestingTest_Approve_TXT.test.approved.txt");

		assertThat(dir.resolve("ApprovalTestingTest_Approve_TXT.test.approved.txt"))
				.hasContent(value);

		// Change the value then approve() should fail
		
		final String changedValue = value + "bar";

		assertThatExceptionOfType(AssertionError.class)
				.isThrownBy(() ->
						approvalTesting
								.withDir(dir)
								.approve(changedValue)
				)
				.withMessage(EOL + "expected: \"foo\"" + EOL + " but was: \"foobar\"");

		assertThat(listAllFilesRecursive(dir))
				.containsExactlyInAnyOrder(
						"ApprovalTestingTest_Approve_TXT.test.approved.txt",
						"ApprovalTestingTest_Approve_TXT.test.failed.txt"
				);

		assertThat(dir.resolve("ApprovalTestingTest_Approve_TXT.test.failed.txt"))
				.hasContent(changedValue);
	}
}

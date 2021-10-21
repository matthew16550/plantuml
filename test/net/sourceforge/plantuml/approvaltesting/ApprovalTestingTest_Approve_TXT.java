package net.sourceforge.plantuml.approvaltesting;

import static net.sourceforge.plantuml.StringUtils.EOL;
import static net.sourceforge.plantuml.test.PathTestUtils.assertThatDirContainsExactlyTheseFiles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

class ApprovalTestingTest_Approve_TXT {

	@TempDir
	static Path dir;

	@RegisterExtension
	static ApprovalTesting approvalTesting = new ApprovalTesting()
			.withDuplicateFiles(true);

	@Test
	void test() {

		// Approve the initial value

		final String value = "foo";

		approvalTesting
				.withDir(dir)
				.approve(value);

		assertThatDirContainsExactlyTheseFiles(dir,
				"ApprovalTestingTest_Approve_TXT.test.approved.txt"
		);

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
				.withMessage(EOL +
						"expected: \"foo\"" + EOL +
						" but was: \"foobar\""
				);

		assertThatDirContainsExactlyTheseFiles(dir,
				"ApprovalTestingTest_Approve_TXT.test.approved.txt",
				"ApprovalTestingTest_Approve_TXT.test.failed.txt"
		);

		assertThat(dir.resolve("ApprovalTestingTest_Approve_TXT.test.failed.txt"))
				.hasContent(changedValue);
	}
}

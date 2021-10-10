package net.sourceforge.plantuml.approvaltesting;

import static net.sourceforge.plantuml.StringUtils.EOL;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ApprovalTestingTest_Approve_TXT extends ApprovalTestingAbstractTest {

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

		assertThatDirContainsExactlyTheseFiles(
				"ApprovalTestingTest_Approve_TXT.test.approved.txt"
		);

		assertThatFile("ApprovalTestingTest_Approve_TXT.test.approved.txt")
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

		assertThatDirContainsExactlyTheseFiles(
				"ApprovalTestingTest_Approve_TXT.test.approved.txt",
				"ApprovalTestingTest_Approve_TXT.test.failed.txt"
		);

		assertThatFile("ApprovalTestingTest_Approve_TXT.test.failed.txt")
				.hasContent(changedValue);
	}
}

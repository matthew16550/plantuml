package net.sourceforge.plantuml.approvaltesting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ApprovalTestingTest_withExtension extends ApprovalTestingAbstractTest {

	@RegisterExtension
	static ApprovalTesting approvalTesting = new ApprovalTesting();

	@Test
	void test() {
		approvalTesting
				.withDir(dir)
				.withExtension(".foo")
				.approve("foo");

		assertThatDirContainsExactlyTheseFiles(
				"ApprovalTestingTest_withExtension.test.approved.foo"
		);
	}
}

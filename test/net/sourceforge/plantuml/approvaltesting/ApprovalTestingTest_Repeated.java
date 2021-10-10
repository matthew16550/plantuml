package net.sourceforge.plantuml.approvaltesting;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.RegisterExtension;

class ApprovalTestingTest_Repeated extends ApprovalTestingAbstractTest {

	@RegisterExtension
	static ApprovalTesting approvalTesting = new ApprovalTesting();

	@RepeatedTest(value = 2, name = "{currentRepetition}")
	void test() {
		approvalTesting
				.withDir(dir)
				.approve("foo");
	}

	@AfterAll
	static void afterAll() {
		assertThatDirContainsExactlyTheseFiles(
				"ApprovalTestingTest_Repeated.test.1.approved.txt",
				"ApprovalTestingTest_Repeated.test.2.approved.txt"
		);
	}
}

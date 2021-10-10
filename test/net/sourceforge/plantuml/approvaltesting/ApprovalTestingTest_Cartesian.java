package net.sourceforge.plantuml.approvaltesting;

import net.sourceforge.plantuml.test.AbstractTempDirTest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junitpioneer.jupiter.CartesianProductTest;

class ApprovalTestingTest_Cartesian extends AbstractTempDirTest {

	@RegisterExtension
	static ApprovalTesting approvalTesting = new ApprovalTesting();

	@CartesianProductTest(name = "{arguments}", value = {"foo", "bar"})
	void test(String a, String b) {
		approvalTesting
				.withDir(dir)
				.approve(a + b);
	}

	@AfterAll
	static void afterAll() {
		assertThatDirContainsExactlyTheseFiles(
				"ApprovalTestingTest_Cartesian.test.bar_bar.approved.txt",
				"ApprovalTestingTest_Cartesian.test.bar_foo.approved.txt",
				"ApprovalTestingTest_Cartesian.test.foo_bar.approved.txt",
				"ApprovalTestingTest_Cartesian.test.foo_foo.approved.txt"
		);
	}
}

package net.sourceforge.plantuml.approvaltesting;

import net.sourceforge.plantuml.test.AbstractTempDirTest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ApprovalTestingTest_Parameterized extends AbstractTempDirTest {

	@RegisterExtension
	static ApprovalTesting approvalTesting = new ApprovalTesting();

	@ParameterizedTest(name = "{arguments}")
	@CsvSource({
			"foo, 1",
			"bar, 2",
	})
	void test(String a, int b) {
		approvalTesting
				.withDir(dir)
				.approve(a + b);
	}

	@AfterAll
	static void afterAll() {
		assertThatDirContainsExactlyTheseFiles(
				"ApprovalTestingTest_Parameterized.test.foo_1.approved.txt",
				"ApprovalTestingTest_Parameterized.test.bar_2.approved.txt"
		);
	}
}

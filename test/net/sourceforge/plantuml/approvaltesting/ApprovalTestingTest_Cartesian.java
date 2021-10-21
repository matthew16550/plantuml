package net.sourceforge.plantuml.approvaltesting;

import static net.sourceforge.plantuml.test.PathTestUtils.assertThatDirContainsExactlyTheseFiles;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.CartesianProductTest;

class ApprovalTestingTest_Cartesian {

	@TempDir
	static Path dir;

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
		assertThatDirContainsExactlyTheseFiles(dir,
				"ApprovalTestingTest_Cartesian.test.bar_bar.approved.txt",
				"ApprovalTestingTest_Cartesian.test.bar_foo.approved.txt",
				"ApprovalTestingTest_Cartesian.test.foo_bar.approved.txt",
				"ApprovalTestingTest_Cartesian.test.foo_foo.approved.txt"
		);
	}
}

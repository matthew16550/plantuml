package net.sourceforge.plantuml.approvaltesting;

import static net.sourceforge.plantuml.test.PathUtils.listAllFilesRecursive;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.CartesianProductTest;

class ApprovalTestingTest_Cartesian {

	@RegisterExtension
	static ApprovalTesting approvalTesting = new ApprovalTesting();

	@TempDir
	static Path dir;

	@CartesianProductTest(name = "{arguments}", value = {"foo", "bar"})
	void test(String a, String b) {
		approvalTesting
				.withDir(dir)
				.approve(a + b);
	}

	@AfterAll
	static void afterAll() throws Exception {
		assertThat(listAllFilesRecursive(dir))
				.containsExactlyInAnyOrder(
						"ApprovalTestingTest_Cartesian.test.bar_bar.approved.txt",
						"ApprovalTestingTest_Cartesian.test.bar_foo.approved.txt",
						"ApprovalTestingTest_Cartesian.test.foo_bar.approved.txt",
						"ApprovalTestingTest_Cartesian.test.foo_foo.approved.txt"
				);
	}
}

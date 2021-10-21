package net.sourceforge.plantuml.approvaltesting;

import static net.sourceforge.plantuml.test.PathTestUtils.assertThatDirContainsExactlyTheseFiles;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ApprovalTestingTest_Parameterized {

	@TempDir
	static Path dir;

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
		assertThatDirContainsExactlyTheseFiles(dir,
				"ApprovalTestingTest_Parameterized.test.foo_1.approved.txt",
				"ApprovalTestingTest_Parameterized.test.bar_2.approved.txt"
		);
	}
}

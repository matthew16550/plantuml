package net.sourceforge.plantuml.approvaltesting;

import static net.sourceforge.plantuml.test.PathUtils.listAllFilesRecursive;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ApprovalTestingTest_Parameterized {

	@RegisterExtension
	static ApprovalTesting approvalTesting = new ApprovalTesting();

	@TempDir
	static Path dir;

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
	static void afterAll() throws Exception {
		assertThat(listAllFilesRecursive(dir))
				.containsExactlyInAnyOrder(
						"ApprovalTestingTest_Parameterized.test.foo_1.approved.txt",
						"ApprovalTestingTest_Parameterized.test.bar_2.approved.txt"
				);
	}
}

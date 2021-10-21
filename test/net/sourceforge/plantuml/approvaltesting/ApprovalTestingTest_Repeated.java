package net.sourceforge.plantuml.approvaltesting;

import static net.sourceforge.plantuml.test.PathTestUtils.assertThatDirContainsExactlyTheseFiles;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

class ApprovalTestingTest_Repeated {

	@TempDir
	static Path dir;

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
		assertThatDirContainsExactlyTheseFiles(dir,
				"ApprovalTestingTest_Repeated.test.1.approved.txt",
				"ApprovalTestingTest_Repeated.test.2.approved.txt"
		);
	}
}
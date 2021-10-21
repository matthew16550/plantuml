package net.sourceforge.plantuml.approvaltesting;

import static net.sourceforge.plantuml.test.PathTestUtils.assertThatDirContainsExactlyTheseFiles;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

class ApprovalTestingTest_withExtension {

	@TempDir
	static Path dir;

	@RegisterExtension
	static ApprovalTesting approvalTesting = new ApprovalTesting();

	@Test
	void test() {
		approvalTesting
				.withDir(dir)
				.withExtension(".foo")
				.approve("foo");

		assertThatDirContainsExactlyTheseFiles(dir,
				"ApprovalTestingTest_withExtension.test.approved.foo"
		);
	}
}

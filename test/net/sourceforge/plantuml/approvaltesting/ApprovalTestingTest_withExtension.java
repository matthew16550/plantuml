package net.sourceforge.plantuml.approvaltesting;

import static net.sourceforge.plantuml.test.PathUtils.listAllFilesRecursive;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

class ApprovalTestingTest_withExtension {

	@RegisterExtension
	static ApprovalTesting approvalTesting = new ApprovalTesting();

	@TempDir
	static Path dir;

	@Test
	void test() throws Exception {
		approvalTesting
				.withDir(dir)
				.withExtension(".foo")
				.approve("foo");
		
		assertThat(listAllFilesRecursive(dir))
				.containsExactly("ApprovalTestingTest_withExtension.test.approved.foo");
	}
}

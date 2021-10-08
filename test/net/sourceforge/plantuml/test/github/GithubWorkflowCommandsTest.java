package net.sourceforge.plantuml.test.github;

import static net.sourceforge.plantuml.test.github.GithubWorkflowCommands.debug;
import static net.sourceforge.plantuml.test.github.GithubWorkflowCommands.error;
import static net.sourceforge.plantuml.test.github.GithubWorkflowCommands.notice;
import static net.sourceforge.plantuml.test.github.GithubWorkflowCommands.warning;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

public class GithubWorkflowCommandsTest {

	@Test
	@StdIo()
	void test_debug(StdOut out) {
		debug("MESSAGE");

		assertThat(out.capturedLines())
				.containsExactly("::debug::MESSAGE");
	}

	@Test
	@StdIo()
	void test_error_with_title(StdOut out) {
		error("TITLE::FOO", "MESSAGE");

		assertThat(out.capturedLines())
				.containsExactly(
						"::error file=test/net/sourceforge/plantuml/test/github/GithubWorkflowCommandsTest.java,line=27,title=TITLE_FOO::MESSAGE"
				);
	}

	@Test
	@StdIo()
	void test_error_without_title(StdOut out) {
		error("MESSAGE");

		assertThat(out.capturedLines())
				.containsExactly(
						"::error file=test/net/sourceforge/plantuml/test/github/GithubWorkflowCommandsTest.java,line=38::MESSAGE"
				);
	}

	@Test
	@StdIo()
	void test_notice_with_title(StdOut out) {
		notice("TITLE::FOO", "MESSAGE");

		assertThat(out.capturedLines())
				.containsExactly(
						"::notice file=test/net/sourceforge/plantuml/test/github/GithubWorkflowCommandsTest.java,line=49,title=TITLE_FOO::MESSAGE"
				);
	}

	@Test
	@StdIo()
	void test_notice_without_title(StdOut out) {
		notice("MESSAGE");

		assertThat(out.capturedLines())
				.containsExactly(
						"::notice file=test/net/sourceforge/plantuml/test/github/GithubWorkflowCommandsTest.java,line=60::MESSAGE"
				);
	}

	@Test
	@StdIo()
	void test_warning_with_title(StdOut out) {
		warning("TITLE::FOO", "MESSAGE");

		assertThat(out.capturedLines())
				.containsExactly(
						"::warning file=test/net/sourceforge/plantuml/test/github/GithubWorkflowCommandsTest.java,line=71,title=TITLE_FOO::MESSAGE"
				);
	}

	@Test
	@StdIo()
	void test_warning_without_title(StdOut out) {
		warning("MESSAGE");

		assertThat(out.capturedLines())
				.containsExactly(
						"::warning file=test/net/sourceforge/plantuml/test/github/GithubWorkflowCommandsTest.java,line=82::MESSAGE"
				);
	}
}

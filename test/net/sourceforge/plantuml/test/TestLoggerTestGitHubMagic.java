package net.sourceforge.plantuml.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

@SetEnvironmentVariable(key = "GITHUB_ACTIONS", value = "true")
class TestLoggerTestGitHubMagic {

	private static final TestLogger logger = new TestLogger();

	@Test
	@StdIo
	void test_all(StdOut stdOut) {
		logger.debug("DDDDD %s", "foo");
		logger.info("IIIII %s", "foo");
		logger.warning("WWWWW %s", "foo");
		logger.error("EEEEE %s", "foo");

		logger.debug(new Throwable(), "bar");
		logger.info(new Throwable(), "bar");
		logger.warning(new Throwable(), "bar");
		logger.error(new Throwable(), "bar");

		assertThat(stdOut.capturedLines())
				.startsWith(
						"DEBUG     : DDDDD foo",
						"",
						"INFO      : IIIII foo",
						"",
						"::warning   file=test/net/sourceforge/plantuml/test/TestLoggerTestGitHubMagic.java,line=22,title=Test Warning::WWWWW foo".replace('/', File.separatorChar),
						"WARNING   : WWWWW foo",
						"",
						"::error     file=test/net/sourceforge/plantuml/test/TestLoggerTestGitHubMagic.java,line=23,title=Test Error::EEEEE foo".replace('/', File.separatorChar),
						"ERROR     : EEEEE foo"
				).containsSubsequence(
						"DEBUG     : bar",
						"java.lang.Throwable",
						"	at net.sourceforge.plantuml.test.TestLoggerTestGitHubMagic.test_all(TestLoggerTestGitHubMagic.java:25)"
				).containsSubsequence(
						"INFO      : bar",
						"java.lang.Throwable",
						"	at net.sourceforge.plantuml.test.TestLoggerTestGitHubMagic.test_all(TestLoggerTestGitHubMagic.java:26)"
				).containsSubsequence(
						"::warning   file=test/net/sourceforge/plantuml/test/TestLoggerTestGitHubMagic.java,line=27,title=Test Warning::bar".replace('/', File.separatorChar),
						"WARNING   : bar",
						"java.lang.Throwable",
						"	at net.sourceforge.plantuml.test.TestLoggerTestGitHubMagic.test_all(TestLoggerTestGitHubMagic.java:27)"
				).containsSubsequence(
						"::error     file=test/net/sourceforge/plantuml/test/TestLoggerTestGitHubMagic.java,line=28,title=Test Error::bar".replace('/', File.separatorChar),
						"ERROR     : bar",
						"java.lang.Throwable",
						"	at net.sourceforge.plantuml.test.TestLoggerTestGitHubMagic.test_all(TestLoggerTestGitHubMagic.java:28)"
				);
	}
}

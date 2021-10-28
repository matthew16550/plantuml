package net.sourceforge.plantuml.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

@SetSystemProperty(key = TestLogger.SUPPRESS_GITHUB_MAGIC, value = "true")
class TestLoggerTest {

	private final TestLogger logger = new TestLogger();

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
						"WARNING   : WWWWW foo",
						"",
						"ERROR     : EEEEE foo",
						""
				).containsSubsequence(
						"DEBUG     : bar",
						"java.lang.Throwable",
						"	at net.sourceforge.plantuml.test.TestLoggerTest.test_all(TestLoggerTest.java:23)"
				).containsSubsequence(
						"INFO      : bar",
						"java.lang.Throwable",
						"	at net.sourceforge.plantuml.test.TestLoggerTest.test_all(TestLoggerTest.java:24)"
				).containsSubsequence(
						"WARNING   : bar",
						"java.lang.Throwable",
						"	at net.sourceforge.plantuml.test.TestLoggerTest.test_all(TestLoggerTest.java:25)"
				).containsSubsequence(
						"ERROR     : bar",
						"java.lang.Throwable",
						"	at net.sourceforge.plantuml.test.TestLoggerTest.test_all(TestLoggerTest.java:26)"
				);
	}
}

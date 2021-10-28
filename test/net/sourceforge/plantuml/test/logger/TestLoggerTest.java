package net.sourceforge.plantuml.test.logger;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestLoggerTest {

	@Test
	void test_without_throwable(TestLogger logger, TestLoggerCapture capture) {
		logger.info("Foo-I %s", "Bar-I");
		logger.error("Foo-E %s", "Bar-E");
		logger.warning("Foo-W %s", "Bar-W");

		Assertions.assertThat(capture.getCaptured())
				.containsExactly(
						"INFO    : Foo-I Bar-I",
						"ERROR   : Foo-E Bar-E",
						"WARNING : Foo-W Bar-W"
				);
	}

	@Test
	void test_info_with_throwable(TestLogger logger, TestLoggerCapture capture) {
		logger.info(new Throwable(), "Foo");

		Assertions.assertThat(capture.getCaptured())
				.startsWith(
						"INFO    : Foo",
						"java.lang.Throwable",
						"	at net.sourceforge.plantuml.test.logger.TestLoggerTest.test_info_with_throwable(TestLoggerTest.java:24)"
				);
	}

	@Test
	void test_warning_with_throwable(TestLogger logger, TestLoggerCapture capture) {
		logger.warning(new Throwable(), "Foo");

		Assertions.assertThat(capture.getCaptured())
				.startsWith(
						"WARNING : Foo",
						"java.lang.Throwable",
						"	at net.sourceforge.plantuml.test.logger.TestLoggerTest.test_warning_with_throwable(TestLoggerTest.java:36)"
				);
	}

	@Test
	void test_error_with_throwable(TestLogger logger, TestLoggerCapture capture) {
		logger.error(new Throwable(), "Foo");

		Assertions.assertThat(capture.getCaptured())
				.startsWith(
						"ERROR   : Foo",
						"java.lang.Throwable",
						"	at net.sourceforge.plantuml.test.logger.TestLoggerTest.test_error_with_throwable(TestLoggerTest.java:48)"
				);
	}
}

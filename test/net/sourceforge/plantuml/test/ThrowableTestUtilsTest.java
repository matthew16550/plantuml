package net.sourceforge.plantuml.test;

import static net.sourceforge.plantuml.StringUtils.EOL;
import static net.sourceforge.plantuml.StringUtils.multilineString;
import static net.sourceforge.plantuml.test.ThrowableTestUtils.stackTraceToString;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

class ThrowableTestUtilsTest {

	@Test
	void test_stackTraceToString_AssertionFailedError_no_values() {
		final AssertionFailedError error = new AssertionFailedError("foo");
		assertThat(stackTraceToString(error))
				.startsWith(multilineString(
						"org.opentest4j.AssertionFailedError: foo",
						"\tat net.sourceforge.plantuml.test.ThrowableTestUtilsTest"
				));
	}

	@Test
	void test_stackTraceToString_AssertionFailedError_single_line_values() {
		final AssertionFailedError error = new AssertionFailedError("foo", "bar", "baz");
		assertThat(stackTraceToString(error))
				.startsWith(multilineString(
						"org.opentest4j.AssertionFailedError: foo",
						"",
						"Expected : bar",
						"Actual   : baz",
						"",
						"\tat net.sourceforge.plantuml.test.ThrowableTestUtilsTest"
				));
	}

	@Test
	void test_stackTraceToString_AssertionFailedError_multi_line_values() {
		final AssertionFailedError error = new AssertionFailedError("foo", "bar" + EOL + "123", "baz" + EOL + "456");
		assertThat(stackTraceToString(error))
				.startsWith(multilineString(
						"org.opentest4j.AssertionFailedError: foo",
						"",
						"Expected",
						"--------",
						"bar",
						"123",
						"",
						"Actual",
						"------",
						"baz",
						"456",
						"",
						"\tat net.sourceforge.plantuml.test.ThrowableTestUtilsTest"
				));
	}

	@Test
	void test_stackTraceToString_AssertionFailedError_multi_line_values_with_EOL() {
		final AssertionFailedError error = new AssertionFailedError("foo" + EOL, "bar" + EOL + "123" + EOL, "baz" + EOL + "456" + EOL);
		assertThat(stackTraceToString(error))
				.startsWith(multilineString(
						"org.opentest4j.AssertionFailedError: foo",
						"",
						"Expected",
						"--------",
						"bar",
						"123",
						"",
						"Actual",
						"------",
						"baz",
						"456",
						"",
						"\tat net.sourceforge.plantuml.test.ThrowableTestUtilsTest"
				));
	}
}

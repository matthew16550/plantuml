package net.sourceforge.plantuml.test;

import static net.sourceforge.plantuml.StringUtils.EOL;
import static net.sourceforge.plantuml.test.MultilineStringBuilder.f;
import static net.sourceforge.plantuml.test.MultilineStringBuilder.multilineStringBuilder;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class MultilineStringBuilderTest {

	@Test
	void test_append() {
		assertThat(
				multilineStringBuilder().append(f("%s-%s", "foo", "bar"))
		).hasToString("foo-bar");

		assertThat(
				multilineStringBuilder().append(123)
		).hasToString("123");
	}

	@Test
	void test_line() {
		assertThat(
				multilineStringBuilder().line("")
		).hasToString(EOL);

		assertThat(
				multilineStringBuilder().line("" + EOL)
		).hasToString(EOL);

		assertThat(
				multilineStringBuilder().line("foo")
		).hasToString("foo" + EOL);

		assertThat(
				multilineStringBuilder().line("foo" + EOL)
		).hasToString("foo" + EOL);
	}

	@Test
	void test_lines() {
		assertThat(
				multilineStringBuilder().lines()
		).hasToString("");

		assertThat(
				multilineStringBuilder().lines(
						""
				)
		).hasToString(EOL);

		assertThat(
				multilineStringBuilder().lines(
						"",
						""
				)
		).hasToString(EOL + EOL);

		assertThat(
				multilineStringBuilder().lines(
						"foo",
						"bar"
				)
		).hasToString("foo" + EOL + "bar" + EOL);

		assertThat(
				multilineStringBuilder().lines(
						Arrays.asList("foo", "bar"),
						"baz"
				)
		).hasToString("foo" + EOL + "bar" + EOL + "baz" + EOL);

		assertThat(
				multilineStringBuilder().lines(
						Stream.of("foo", "bar"),
						"baz"
				)
		).hasToString("foo" + EOL + "bar" + EOL + "baz" + EOL);
	}
}

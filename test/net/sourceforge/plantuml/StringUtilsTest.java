package net.sourceforge.plantuml;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static net.sourceforge.plantuml.StringUtils.join;
import static net.sourceforge.plantuml.StringUtils.substringAfter;
import static net.sourceforge.plantuml.StringUtils.substringAfterLast;
import static net.sourceforge.plantuml.StringUtils.substringBefore;
import static net.sourceforge.plantuml.StringUtils.substringBeforeLast;
import static net.sourceforge.plantuml.StringUtils.truncateStringToByteLength;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StringUtilsTest {

	@ParameterizedTest
	@CsvSource(nullValues = "null", value = {
			" null   , true  ",
			" ''     , true  ",
			" ' '    , true  ",
			" '\0'   , true  ",
			" '\n'   , true  ",
			" '\r'   , true  ",
			" '\t'   , true  ",
			" 'x'    , false ",
			" ' x '  , false ",
	})
	void test_isEmpty_isNotEmpty(String s, boolean empty) {
		assertThat(StringUtils.isEmpty(s))
				.isEqualTo(empty);

		assertThat(StringUtils.isNotEmpty(s))
				.isNotEqualTo(empty);
	}

	@Test
	void test_join_array() {
		assertThat(join(""))
				.isEmpty();

		assertThat(join("-"))
				.isEmpty();

		assertThat(join("", "foo"))
				.isEqualTo("foo");

		assertThat(join("", "foo", "bar"))
				.isEqualTo("foobar");

		assertThat(join("-", "foo", "bar"))
				.isEqualTo("foo-bar");

		assertThat(join("123", "foo", "bar"))
				.isEqualTo("foo123bar");

		assertThat(join("-", "foo", "bar", "baz"))
				.isEqualTo("foo-bar-baz");
	}

	@Test
	void test_join_iterable() {
		assertThat(join("", emptyList()))
				.isEmpty();

		assertThat(join("-", emptyList()))
				.isEmpty();

		assertThat(join("", singletonList("foo")))
				.isEqualTo("foo");

		assertThat(join("", asList("foo", "bar")))
				.isEqualTo("foobar");

		assertThat(join("-", asList("foo", "bar")))
				.isEqualTo("foo-bar");

		assertThat(join("123", asList("foo", "bar")))
				.isEqualTo("foo123bar");

		assertThat(join("-", asList("foo", "bar", "baz")))
				.isEqualTo("foo-bar-baz");
	}

	@ParameterizedTest
	@CsvSource(nullValues = "NULL", value = {
			" NULL     , NULL    ",
			" ''       , ''      ",
			" foo      , ''      ",
			" .foo     , foo     ",
			" foo.     , ''      ",
			" .foo.    , foo.    ",
			" .foo.bar , foo.bar ",
	})
	void test_substringAfter(String input, String expected) {
		assertThat(substringAfter(input, '.'))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@CsvSource(nullValues = "NULL", value = {
			" NULL     , NULL ",
			" ''       , ''   ",
			" foo      , ''   ",
			" .foo     , foo  ",
			" foo.     , ''   ",
			" .foo.    , ''   ",
			" .foo.bar , bar  ",
	})
	void test_substringAfterLast(String input, String expected) {
		assertThat(substringAfterLast(input, '.'))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@CsvSource(nullValues = "NULL", value = {
			" NULL     , NULL ",
			" ''       , ''   ",
			" foo      , foo  ",
			" .foo     , ''   ",
			" foo.     , foo  ",
			" .foo.    , ''   ",
			" foo.bar. , foo  ",
	})
	void test_substringBefore(String input, String expected) {
		assertThat(substringBefore(input, '.'))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@CsvSource(nullValues = "NULL", value = {
			" NULL        , NULL    ",
			" ''          , ''      ",
			" foo         , foo     ",
			" .foo        , ''      ",
			" foo.        , foo     ",
			" .foo.       , .foo    ",
			" foo.bar.baz , foo.bar ",
	})
	void test_substringBeforeLast(String input, String expected) {
		assertThat(substringBeforeLast(input, '.'))
				.isEqualTo(expected);
	}

	@ParameterizedTest(name = "{arguments}")
	@CsvSource({
			// input           maxBytes   suffix   expected
			"  ''            , 0        , ''     , ''           ",
			"  ''            , 1        , ''     , ''           ",
			"  '1234567890'  , 10       , 'XX'   , '1234567890' ",
			"  '12345678901' , 10       , 'XX'   , '1234XX'     ",
			"  'π12345678'   , 10       , 'XX'   , 'π12345678'  ", // " π  " is 2 bytes (CF 80)
			"  'π123456789'  , 10       , 'XX'   , 'π12XX'      ",
			"  '⁋1234567'    , 10       , 'XX'   , '⁋1234567'   ", // " ⁋  " is 3 bytes  (E2 81 8B)
			"  '⁋12345678'   , 10       , 'XX'   , '⁋1XX'       ",
			"  '🥝123456'     , 10       , 'XX'   , '🥝123456'   ", // " 🥝 " is 4 bytes (F0 9F A5 9D)
			"  '🥝1234567'    , 10       , 'XX'   , '🥝XX'       ",
	})
	void test_truncateStringToByteLength(String input, int maxBytes, String suffix, String expected) {
		assertThat(truncateStringToByteLength(input, maxBytes, suffix))
				.isEqualTo(expected);
	}
}

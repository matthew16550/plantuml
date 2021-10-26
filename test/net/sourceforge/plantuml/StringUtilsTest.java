package net.sourceforge.plantuml;

import static org.assertj.core.api.Assertions.assertThat;

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
	void test_isEmpty(String s, boolean expected) {
		assertThat(StringUtils.isEmpty(s))
				.isEqualTo(expected);
	}

	@ParameterizedTest
	@CsvSource(nullValues = "null", value = {
			" null   , false ",
			" ''     , false ",
			" ' '    , false ",
			" '\0'   , false ",
			" '\n'   , false ",
			" '\r'   , false ",
			" '\t'   , false ",
			" 'x'    , true  ",
			" ' x '  , true  ",
	})
	void test_isNotEmpty(String s, boolean expected) {
		assertThat(StringUtils.isNotEmpty(s))
				.isEqualTo(expected);
	}
}

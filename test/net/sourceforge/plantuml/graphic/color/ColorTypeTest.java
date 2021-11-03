package net.sourceforge.plantuml.graphic.color;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ColorTypeTest {

	@ParameterizedTest
	@CsvSource({
			" text     , TEXT ",
			" text.foo , TEXT ",
	})
	void test_getType(String input, ColorType expected) {
		assertThat(ColorType.getType(input))
				.isEqualTo(expected);
	}
}

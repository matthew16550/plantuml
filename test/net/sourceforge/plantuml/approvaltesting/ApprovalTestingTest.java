package net.sourceforge.plantuml.approvaltesting;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junitpioneer.jupiter.CartesianProductTest;

@ExtendWith(ApprovalTestingJUnitExtension.class)
class ApprovalTestingTest {

	@SuppressWarnings("unused")  // injected by ApprovalTestingJUnitExtension
	private ApprovalTesting approvalTesting;

	@Test
	void test_approve_image_bmp() {
		final BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
		image.createGraphics().drawRect(2, 3, 5, 3);
		approvalTesting.withExtension(".bmp").approve(image);
	}

	@Test
	void test_approve_image_png() {
		final BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
		image.createGraphics().drawRect(2, 3, 5, 3);
		approvalTesting.approve(image);
	}

	@Test
	void test_approve_string() {
		approvalTesting.approve("foo");
	}

	@CartesianProductTest(name = "{arguments}", value = {"foo", "bar"})
	void test_cartesian_product(String a, String b) {
		approvalTesting.approve(a + b);
	}

	@ParameterizedTest(name = "{arguments}")
	@CsvSource({
			"foo, 1",
			"bar, 2",
	})
	void test_parameterized(String s, int i) {
		approvalTesting.approve(s + i);
	}

	@RepeatedTest(value = 2, name = "repetition {currentRepetition}")
	void test_repeated() {
		approvalTesting.approve("foo");
	}

	@Test
	void test_withExtension() {
		approvalTesting.withExtension(".foo").approve("foo");
	}

	@Test
	void test_withSuffix() {
		approvalTesting.withSuffix("-bar").approve("foo");
	}

	@ParameterizedTest
	@CsvSource(delimiter = 'D', value = {
			"x     D  x",
			"_x    D  x",
			"__x   D  x",
			"x_    D  x",
			"x__   D  x",
			"x y   D  x_y",
			"x  y  D  x_y",
			"x,y   D  x_y",
			"x()   D  x",
			"☺x☺︎   D  x",
			"!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~} x !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~} y !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~}  D  x_y",
	})
	void test_simplifyTestName(String input, String output) {
		assertThat(ApprovalTestingImpl.simplifyTestName(input))
				.isEqualTo(output);
	}

}

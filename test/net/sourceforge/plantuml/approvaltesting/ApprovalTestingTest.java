package net.sourceforge.plantuml.approvaltesting;

import static java.util.stream.Collectors.toList;
import static net.sourceforge.plantuml.test.PathUtils.glob;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.awt.image.BufferedImage;
import java.util.List;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junitpioneer.jupiter.CartesianProductTest;

import net.sourceforge.plantuml.test.TestUtils;

@ExtendWith(ApprovalTestingJUnitExtension.class)
class ApprovalTestingTest {

	//
	// Test Cases
	//

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

	@RepeatedTest(value = 4, name = "{currentRepetition}")
	void test_withFileSpamLimit(RepetitionInfo repetitionInfo) throws Exception {
		final int fileSpamLimit = 2;

		final String prefix = repetitionInfo.getCurrentRepetition() >= 3
				? String.format("** APPROVAL FAILURE FILES WERE SUPPRESSED (test has failed %d times) ** ", repetitionInfo.getCurrentRepetition())
				: "";

		assertThatExceptionOfType(AssertionError.class)
				.isThrownBy(() -> approvalTesting.withFileSpamLimit(fileSpamLimit).approve("bar"))
				.withMessageStartingWith(prefix + EOL + "expected:");

		final List<String> failureFiles = glob(approvalTesting.getDir(), "**/ApprovalTestingTest.test_withFileSpamLimit.*.failed.txt")
				.map(path -> path.getFileName().toString())
				.collect(toList());

		assertThat(failureFiles)
				.hasSizeBetween(1, fileSpamLimit);
	}

	@Test
	void test_withLabel() {
		approvalTesting.withLabel("LABEL").approve("bar");
	}

	@Test
	void test_withOutput() {
		assertThatExceptionOfType(AssertionError.class)
				.isThrownBy(() ->
						approvalTesting
								.withOutput("OUTPUT", ".txt", path -> TestUtils.writeUtf8File(path, "123"))
								.approve("bar")
				);

		assertThat(approvalTesting.getDir().resolve("ApprovalTestingTest.test_withOutput.OUTPUT.failed.txt"))
				.hasContent("123");
	}

	@Test
	void test_withOutput_and_withLabel() {
		assertThatExceptionOfType(AssertionError.class)
				.isThrownBy(() ->
						approvalTesting
								.withLabel("LABEL")
								.withOutput("OUTPUT", ".txt", path -> TestUtils.writeUtf8File(path, "123"))
								.approve("bar")
				);

		assertThat(approvalTesting.getDir().resolve("ApprovalTestingTest.test_withOutput_and_withLabel.LABEL.OUTPUT.failed.txt"))
				.hasContent("123");
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

	//
	// Test DSL
	//

	private static final String EOL = System.getProperty("line.separator");

	@SuppressWarnings("unused")  // injected by ApprovalTestingJUnitExtension
	private ApprovalTestingImpl approvalTesting;

}

package net.sourceforge.plantuml.test.outputs;

import static net.sourceforge.plantuml.test.ImageTestUtils.imageToBytes;
import static net.sourceforge.plantuml.test.PathTestUtils.assertThatDirContainsExactlyTheseFiles;
import static net.sourceforge.plantuml.test.outputs.TestOutputsImpl.simplifyName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junitpioneer.jupiter.CartesianProductTest;
import org.junitpioneer.jupiter.CartesianValueSource;

import net.sourceforge.plantuml.test.TestLogger;
import net.sourceforge.plantuml.test.outputs.TestOutputs.RegisteredPath;

class TestOutputsTest {

	@TempDir
	static Path dir;

	static final Set<String> expectedFiles = new HashSet<>();

	static void expectFiles(String... files) {
		expectedFiles.addAll(Arrays.asList(files));
	}

	@BeforeAll
	static void beforeAll(TestOutputs outputs) {
		outputs.dir(dir);
	}

	@AfterAll
	static void afterAll() {
		assertThatDirContainsExactlyTheseFiles(dir, expectedFiles.toArray(new String[]{}));
	}

	@AfterEach
	void afterEach() {
		TestLogger.disableCapture();
	}

	@Test
	void test_write_bmp(TestOutputs outputs) {
		expectFiles("TestOutputsTest.test_write_bmp.foo.bmp");

		final BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
		image.createGraphics().drawRect(2, 3, 5, 3);

		assertThat(outputs.write("foo.bmp", image))
				.isTrue();
		assertThat(dir.resolve("TestOutputsTest.test_write_bmp.foo.bmp"))
				.hasBinaryContent(imageToBytes(image, "bmp"));
	}

	@Test
	void test_write_png(TestOutputs outputs) {
		expectFiles("TestOutputsTest.test_write_png.foo.png");

		final BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
		image.createGraphics().drawRect(2, 3, 5, 3);

		assertThat(outputs.write("foo.png", image))
				.isTrue();
		assertThat(dir.resolve("TestOutputsTest.test_write_png.foo.png"))
				.hasBinaryContent(imageToBytes(image, "png"));
	}

	@Test
	void test_write_txt(TestOutputs outputs) {
		expectFiles("TestOutputsTest.test_write_txt.foo.txt");

		assertThat(outputs.write("foo.txt", "bar"))
				.isTrue();
		assertThat(dir.resolve("TestOutputsTest.test_write_txt.foo.txt"))
				.hasContent("bar");
	}

	@CartesianProductTest(name = "{arguments}")
	@CartesianValueSource(strings = {"foo", "bar"})
	@CartesianValueSource(strings = {"foo", "bar"})
	void test_cartesian(String a, String b, TestOutputs outputs) {
		expectFiles(
				"TestOutputsTest.test_cartesian.bar_bar.output.txt",
				"TestOutputsTest.test_cartesian.bar_foo.output.txt",
				"TestOutputsTest.test_cartesian.foo_bar.output.txt",
				"TestOutputsTest.test_cartesian.foo_foo.output.txt"
		);
		outputs.write("output.txt", a + b);
	}

	@ParameterizedTest(name = "{arguments}")
	@CsvSource({
			"foo, 1",
			"bar, 2",
	})
	void test_parameterized(String a, int b, TestOutputs outputs) {
		expectFiles(
				"TestOutputsTest.test_parameterized.foo_1.output.txt",
				"TestOutputsTest.test_parameterized.bar_2.output.txt"
		);
		outputs.write("output.txt", a + b);
	}

	@RepeatedTest(value = 3)
	void test_repeated_single_digit(TestOutputs outputs) {
		expectFiles(
				"TestOutputsTest.test_repeated_single_digit.1.output.txt",
				"TestOutputsTest.test_repeated_single_digit.2.output.txt",
				"TestOutputsTest.test_repeated_single_digit.3.output.txt"
		);
		outputs.write("output.txt", "foo");
	}

	@RepeatedTest(value = 11)
	void test_repeated_double_digit(TestOutputs outputs) {
		expectFiles(
				"TestOutputsTest.test_repeated_double_digit.01.output.txt",
				"TestOutputsTest.test_repeated_double_digit.02.output.txt",
				"TestOutputsTest.test_repeated_double_digit.03.output.txt",
				"TestOutputsTest.test_repeated_double_digit.04.output.txt",
				"TestOutputsTest.test_repeated_double_digit.05.output.txt",
				"TestOutputsTest.test_repeated_double_digit.06.output.txt",
				"TestOutputsTest.test_repeated_double_digit.07.output.txt",
				"TestOutputsTest.test_repeated_double_digit.08.output.txt",
				"TestOutputsTest.test_repeated_double_digit.09.output.txt",
				"TestOutputsTest.test_repeated_double_digit.10.output.txt",
				"TestOutputsTest.test_repeated_double_digit.11.output.txt"
		);

		outputs
				.spamLimit(11)
				.write("output.txt", "foo");
	}

	@Test
	void test_deleteAfterTestPasses(TestOutputs outputs) {
		expectFiles("TestOutputsTest.test_deleteAfterTestPasses.foo2.txt");
		expectFiles("TestOutputsTest.test_deleteAfterTestPasses.bar2.txt");
		expectFiles("TestOutputsTest.test_deleteAfterTestPasses.baz.java");

		outputs
				.deleteAfterTestPasses("f*1.txt")
				.deleteAfterTestPasses("b*1.txt")
				.deleteAfterTestPasses("baz.java");
		outputs.write("foo1.txt", "");
		outputs.write("bar1.txt", "");
		outputs.write("foo2.txt", "");
		outputs.write("bar2.txt", "");
		outputs.write("baz.java", "");
	}

	@Test
	void test_registerPath(TestOutputs outputs) {
		final String name = "foo";
		final RegisteredPath registeredPath = outputs.registerPath(name);

		assertThat(registeredPath.getPath())
				.hasFileName("TestOutputsTest.test_registerPath.foo");

		// Using the same name again is not allowed

		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> outputs.registerPath(name))
				.withMessageStartingWith("Trying to reuse output file");

		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> outputs.usePath(name))
				.withMessageStartingWith("Trying to reuse output file");
	}

	@Test
	void test_usePath(TestOutputs outputs) {
		final String name = "foo";
		final Path path = outputs.usePath(name);

		assertThat(path)
				.hasFileName("TestOutputsTest.test_usePath.foo");

		// Using the same name again is not allowed

		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> outputs.usePath(name))
				.withMessageStartingWith("Trying to reuse output file");

		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> outputs.registerPath(name))
				.withMessageStartingWith("Trying to reuse output file");
	}

	@Test
	void test_reuseFiles(TestOutputs outputs) {
		final String name = "foo";
		outputs.usePath(name);

		// Using the same name again is not allowed

		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> outputs.usePath(name))
				.withMessageStartingWith("Trying to reuse output file");

		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> outputs.registerPath(name))
				.withMessageStartingWith("Trying to reuse output file");

		// Using the same name again is allowed after reuseFiles(true)

		outputs.reuseFiles(true);

		outputs.usePath(name);
		outputs.registerPath(name);
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
	void test_simplifyName(String input, String output) {
		assertThat(simplifyName(input))
				.isEqualTo(output);
	}

	@RepeatedTest(value = 3)
	void test_spam_counting_auto(TestOutputs outputs, RepetitionInfo repetitionInfo) {
		TestLogger.enableCapture();
		expectFiles(
				"TestOutputsTest.test_spam_counting_auto.1.output.txt",
				"TestOutputsTest.test_spam_counting_auto.2.output.txt"
		);
		final int spamLimit = 2;
		outputs.spamLimit(spamLimit);

		final boolean shouldWriteFile = repetitionInfo.getCurrentRepetition() <= spamLimit;

		assertThat(outputs.write("output.txt", "foo"))
				.isEqualTo(shouldWriteFile);

		if (!shouldWriteFile) {
			assertThat(TestLogger.getCaptured())
					.containsExactly("WARNING : Suppressing spammy output file 'TestOutputsTest.test_spam_counting_auto.3.output.txt'");
		}
	}

	@Test
	void test_spam_counting_manual(TestOutputs outputs) {
		TestLogger.enableCapture();
		expectFiles(
				"TestOutputsTest.test_spam_counting_manual.out1",
				"TestOutputsTest.test_spam_counting_manual.out2",
				"TestOutputsTest.test_spam_counting_manual.out3",
				"TestOutputsTest.test_spam_counting_manual.out4"
		);

		outputs
				.autoSpamCount(false)
				.spamLimit(1);

		assertThat(outputs.write("out1", ""))
				.isTrue();
		assertThat(outputs.write("out2", ""))
				.isTrue();

		outputs.bumpSpamCount();

		assertThat(outputs.write("out3", ""))
				.isTrue();
		assertThat(outputs.write("out4", ""))
				.isTrue();

		outputs.bumpSpamCount();

		assertThat(outputs.write("out5", ""))
				.isFalse();

		assertThat(TestLogger.getCaptured())
				.containsExactly("WARNING : Suppressing spammy output file 'TestOutputsTest.test_spam_counting_manual.out5'");

	}
}

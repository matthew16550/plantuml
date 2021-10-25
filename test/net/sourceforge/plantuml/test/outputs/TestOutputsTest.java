package net.sourceforge.plantuml.test.outputs;

import static net.sourceforge.plantuml.test.ImageTestUtils.assertImagesEqual;
import static net.sourceforge.plantuml.test.ImageTestUtils.imageToBytes;
import static net.sourceforge.plantuml.test.PathTestUtils.assertThatDirContainsExactlyTheseFiles;
import static net.sourceforge.plantuml.test.outputs.TestOutputsImpl.simplifyName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junitpioneer.jupiter.CartesianProductTest;
import org.junitpioneer.jupiter.CartesianValueSource;

import net.sourceforge.plantuml.png.PngIO;
import net.sourceforge.plantuml.png.PngReader;
import net.sourceforge.plantuml.png.PngWriter;
import net.sourceforge.plantuml.test.logger.TestLoggerCapture;
import net.sourceforge.plantuml.test.outputs.TestOutputs.Output;

@SuppressWarnings("StaticVariableUsedBeforeInitialization")
class TestOutputsTest {

	@TempDir
	static Path dir;

	static final Set<String> expectedFiles = new HashSet<>();

	static void expectFile(Path path) {
		expectedFiles.add(path.getFileName().toString());
	}

	static void expectFiles(String... files) {
		expectedFiles.addAll(Arrays.asList(files));
	}

	@BeforeAll
	static void beforeAll(TestOutputs testOutputs) {
		testOutputs.dir(dir);
	}

	@AfterAll
	static void afterAll() throws Exception {
		assertThatDirContainsExactlyTheseFiles(dir, expectedFiles);
	}

	@Test
	void test_write_bmp(TestOutputs testOutputs) throws Exception {
		final Path file = dir.resolve("TestOutputsTest.test_write_bmp.foo.bmp");
		expectFile(file);
		final BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
		image.createGraphics().drawRect(2, 3, 5, 3);

		testOutputs.out("foo.bmp").write(image);
		assertThat(file)
				.hasBinaryContent(imageToBytes(image, "bmp"));
	}

	@Test
	void test_write_object(TestOutputs testOutputs) throws Exception {
		final Path file = dir.resolve("TestOutputsTest.test_write_object.foo.txt");
		expectFile(file);
		final Object object = new Object();

		testOutputs.out("foo.txt").write(object);
		assertThat(file)
				.hasContent(object.toString());
	}

	@Test
	void test_write_png(TestOutputs testOutputs) throws Exception {
		final Path file = dir.resolve("TestOutputsTest.test_write_png.foo.png");
		expectFile(file);
		final BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
		image.createGraphics().drawRect(2, 3, 5, 3);

		testOutputs.out("foo.png").write(image);

		try (final PngReader pngReader = PngIO.reader(file)) {
			assertThat(pngReader.getRequiredString(PngWriter.CREATION_METADATA_TAG))
					.startsWith("PlantUML Version: ");

			assertImagesEqual(image, pngReader.readImage());
		}
	}

	@Test
	void test_write_txt(TestOutputs testOutputs) throws Exception {
		final Path file = dir.resolve("TestOutputsTest.test_write_txt.foo.txt");
		expectFile(file);

		testOutputs.out("foo.txt").write("bar");
		assertThat(file)
				.hasContent("bar");
	}

	@CartesianProductTest(name = "{arguments}")
	@CartesianValueSource(strings = {"foo", "bar"})
	@CartesianValueSource(strings = {"foo", "bar"})
	void test_cartesian(String a, String b, TestOutputs testOutputs) throws Exception {
		expectFiles(
				"TestOutputsTest.test_cartesian.bar_bar.output.txt",
				"TestOutputsTest.test_cartesian.bar_foo.output.txt",
				"TestOutputsTest.test_cartesian.foo_bar.output.txt",
				"TestOutputsTest.test_cartesian.foo_foo.output.txt"
		);
		testOutputs.out("output.txt").write(a + b);
	}

	@ParameterizedTest(name = "{arguments}")
	@CsvSource({
			"foo, 1",
			"bar, 2",
	})
	void test_parameterized(String a, int b, TestOutputs testOutputs) throws Exception {
		expectFiles(
				"TestOutputsTest.test_parameterized.foo_1.output.txt",
				"TestOutputsTest.test_parameterized.bar_2.output.txt"
		);
		testOutputs.out("output.txt").write(a + b);
	}

	@RepeatedTest(value = 3)
	void test_repeated_single_digit(TestOutputs testOutputs) throws Exception {
		expectFiles(
				"TestOutputsTest.test_repeated_single_digit.1.output.txt",
				"TestOutputsTest.test_repeated_single_digit.2.output.txt",
				"TestOutputsTest.test_repeated_single_digit.3.output.txt"
		);
		testOutputs.out("output.txt").write("foo");
	}

	@RepeatedTest(value = 11)
	void test_repeated_double_digit(TestOutputs testOutputs) throws Exception {
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

		testOutputs.out("output.txt").write("foo");
	}

	@Test
	void test_deleteAfterTestPasses(TestOutputs testOutputs) throws Exception {
		expectFiles(
				"TestOutputsTest.test_deleteAfterTestPasses.foo2.txt",
				"TestOutputsTest.test_deleteAfterTestPasses.bar2.txt",
				"TestOutputsTest.test_deleteAfterTestPasses.baz.java"
		);

		testOutputs
				.deleteAfterTestPasses("f*1.txt")
				.deleteAfterTestPasses("b*1.txt")
				.deleteAfterTestPasses("baz.java");
		testOutputs.out("foo1.txt").write("");
		testOutputs.out("bar1.txt").write("");
		testOutputs.out("foo2.txt").write("");
		testOutputs.out("bar2.txt").write("");
		testOutputs.out("baz.java").write("");
	}

	@Test
	void test_output(TestOutputs testOutputs) {
		final String name = "foo";
		final Output output = testOutputs.out(name);

		assertThat(output.getPath())
				.hasFileName("TestOutputsTest.test_output.foo");

		// Using the same name again is not allowed

		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> testOutputs.out(name))
				.withMessageStartingWith("Trying to reuse output path");
	}

	@Test
	void test_reusePaths(TestOutputs testOutputs) {
		final String name = "foo";
		testOutputs.out(name);

		// Using the same name again is not allowed

		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> testOutputs.out(name))
				.withMessageStartingWith("Trying to reuse output path");

		// Using the same name again is allowed after reuseFiles(true)

		testOutputs
				.reusePaths(true)
				.out(name);
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
	void test_spam(TestOutputs testOutputs, TestLoggerCapture testLoggerCapture) throws Exception {
		expectFiles(
				"TestOutputsTest.test_spam.1.output.txt",
				"TestOutputsTest.test_spam.2.output.txt"
		);
		testOutputs.spamLimit(2);

		testOutputs.spam("output.txt").write("foo");

		assertThat(testLoggerCapture.getMessages())
				.isEmpty();
	}

	@RepeatedTest(value = 3)
	void test_spamOrException(TestOutputs testOutputs, RepetitionInfo repetitionInfo, TestLoggerCapture testLoggerCapture) {
		expectFiles(
				"TestOutputsTest.test_spamOrException.1.output.txt",
				"TestOutputsTest.test_spamOrException.2.output.txt"
		);
		final int spamLimit = 2;
		testOutputs.spamLimit(spamLimit);

		final ThrowingCallable callable = () -> testOutputs.spamOrException("output.txt").write("foo");

		if (repetitionInfo.getCurrentRepetition() <= spamLimit) {
			assertThatNoException()
					.isThrownBy(callable);
		} else {
			assertThatExceptionOfType(TestOutputSuppressed.class)
					.isThrownBy(callable)
					.withMessage("Suppressing spammy output file 'TestOutputsTest.test_spamOrException.3.output.txt'");
		}

		assertThat(testLoggerCapture.getMessages())
				.isEmpty();
	}

	@RepeatedTest(value = 3)
	void test_spamOrLogged(TestOutputs testOutputs, RepetitionInfo repetitionInfo, TestLoggerCapture testLoggerCapture) throws Exception {
		expectFiles(
				"TestOutputsTest.test_spamOrLogged.1.output.txt",
				"TestOutputsTest.test_spamOrLogged.2.output.txt"
		);
		final int spamLimit = 2;
		testOutputs.spamLimit(spamLimit);

		testOutputs.spamOrLogged("output.txt").write("foo");

		if (repetitionInfo.getCurrentRepetition() <= spamLimit) {
			assertThat(testLoggerCapture.getMessages())
					.isEmpty();
		} else {
			assertThat(testLoggerCapture.getMessages())
					.containsExactly("Suppressing spammy output file 'TestOutputsTest.test_spamOrLogged.3.output.txt'");
		}
	}
}

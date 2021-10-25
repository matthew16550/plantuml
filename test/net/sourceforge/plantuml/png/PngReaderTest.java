package net.sourceforge.plantuml.png;

import static net.sourceforge.plantuml.test.ImageTestUtils.assertImagesEqual;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// These tests do not auto-close the PngReader because I think it will make the code too messy
// It is harmless because the PNG is in byte array not in a file 
class PngReaderTest {

	final BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);

	@BeforeEach
	void beforeEach() {
		image.createGraphics().drawOval(1, 2, 8, 6);
	}

	@Test
	void test_readImage() throws Exception {
		final PngReader pngReader = doTest(
				PngIO.writer()
		);

		assertImagesEqual(image, pngReader.readImage());
	}

	@Test
	void test_findMetadataValue() throws Exception {
		final PngReader pngReader = doTest(
				PngIO.writer().text("foo", "bar")
		);

		assertThat(pngReader.findMetadataValue("foo"))
				.isEqualTo("bar");

		assertThat(pngReader.findMetadataValue("baz"))
				.isNull();
	}

	@Test
	void test_getRequiredFloat() throws Exception {
		final PngReader pngReader = doTest(
				PngIO.writer().text("foo", 1.23f)
		);

		assertThat(pngReader.getRequiredFloat("foo"))
				.isEqualTo(1.23f);

		assertThatExceptionOfType(RuntimeException.class)
				.isThrownBy(() -> pngReader.getRequiredFloat("baz"))
				.withMessage("PNG tag is missing: baz");
	}

	@Test
	void test_getRequiredInt() throws Exception {
		final PngReader pngReader = doTest(
				PngIO.writer().text("foo", 123)
		);

		assertThat(pngReader.getRequiredInt("foo"))
				.isEqualTo(123);

		assertThatExceptionOfType(RuntimeException.class)
				.isThrownBy(() -> pngReader.getRequiredInt("baz"))
				.withMessage("PNG tag is missing: baz");
	}

	@Test
	void test_getRequiredString() throws Exception {
		final PngReader pngReader = doTest(
				PngIO.writer().text("foo", "bar")
		);

		assertThat(pngReader.getRequiredString("foo"))
				.isEqualTo("bar");

		assertThatExceptionOfType(RuntimeException.class)
				.isThrownBy(() -> pngReader.getRequiredString("baz"))
				.withMessage("PNG tag is missing: baz");
	}

	//
	// Test DSL
	//

	private PngReader doTest(PngWriter pngWriter) throws Exception {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pngWriter.write(image, baos);
		final ByteArrayInputStream is = new ByteArrayInputStream(baos.toByteArray());
		return PngIO.reader(is);
	}
}

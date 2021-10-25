package net.sourceforge.plantuml.png;

import static net.sourceforge.plantuml.test.ImageTestUtils.assertImagesEqual;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import org.xmlunit.assertj.XmlAssert;

import net.sourceforge.plantuml.security.SImageIO;
import net.sourceforge.plantuml.utils.ImageUtils;

class PngWriterTest {

	final BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);

	@BeforeEach
	void beforeEach() {
		image.createGraphics().drawOval(1, 2, 8, 6);
	}

	//
	// Test Cases
	//

	@Test
	void test_image() throws Exception {
		final ImageReader imageReader = doTest(
				PngIO.writer()
		);

		assertImagesEqual(image, imageReader.read(0));
	}

	@Test
	void test_copyleft() throws Exception {
		final Node root = doMetadataTest("javax_imageio_png_1.0",
				PngIO.writer()
		);

		XmlAssert.assertThat(root)
				.valueByXPath("./tEXt/tEXtEntry[@keyword=\"copyleft\"]/@value")
				.isEqualTo(PngWriter.copyleft);
	}

	@Test
	void test_creationMetadata() throws Exception {
		final Node root = doMetadataTest("javax_imageio_png_1.0",
				PngIO.writer().creationMetadata()
		);

		XmlAssert.assertThat(root)
				.valueByXPath("./tEXt/tEXtEntry[@keyword=\"PlantUml-Creation-Metadata\"]/@value")
				.startsWith("java.runtime.name=");
	}

	@Test
	void test_dpi() throws Exception {
		final Node root = doMetadataTest("javax_imageio_1.0",
				PngIO.writer().dpi(100)
		);

		XmlAssert.assertThat(root)
				.valueByXPath("./Dimension/HorizontalPixelSize/@value")
				.isEqualTo("0.2540005");

		XmlAssert.assertThat(root)
				.valueByXPath("./Dimension/VerticalPixelSize/@value")
				.isEqualTo("0.2540005");
	}

	@Test
	void test_itext() throws Exception {
		final Node root = doMetadataTest("javax_imageio_png_1.0",
				PngIO.writer()
						.iText("itext-1", "itext-value-1")
						.iText("itext-2", "itext-value-2")
		);

		XmlAssert.assertThat(root)
				.valueByXPath("./iTXt/iTXtEntry[@keyword=\"itext-1\"]/@text")
				.isEqualTo("itext-value-1");

		XmlAssert.assertThat(root)
				.valueByXPath("./iTXt/iTXtEntry[@keyword=\"itext-2\"]/@text")
				.isEqualTo("itext-value-2");
	}

	@Test
	void test_text_float() throws Exception {
		final Node root = doMetadataTest("javax_imageio_png_1.0",
				PngIO.writer()
						.text("text-1", 1.1f)
						.text("text-2", 2.2f)
		);

		XmlAssert.assertThat(root)
				.valueByXPath("./tEXt/tEXtEntry[@keyword=\"text-1\"]/@value")
				.isEqualTo("1.1");

		XmlAssert.assertThat(root)
				.valueByXPath("./tEXt/tEXtEntry[@keyword=\"text-2\"]/@value")
				.isEqualTo("2.2");
	}

	@Test
	void test_text_int() throws Exception {
		final Node root = doMetadataTest("javax_imageio_png_1.0",
				PngIO.writer()
						.text("text-1", 11)
						.text("text-2", 22)
		);

		XmlAssert.assertThat(root)
				.valueByXPath("./tEXt/tEXtEntry[@keyword=\"text-1\"]/@value")
				.isEqualTo("11");

		XmlAssert.assertThat(root)
				.valueByXPath("./tEXt/tEXtEntry[@keyword=\"text-2\"]/@value")
				.isEqualTo("22");
	}

	@Test
	void test_text_string() throws Exception {
		final Node root = doMetadataTest("javax_imageio_png_1.0",
				PngIO.writer()
						.text("text-1", "text-value-1")
						.text("text-2", "text-value-2")
		);

		XmlAssert.assertThat(root)
				.valueByXPath("./tEXt/tEXtEntry[@keyword=\"text-1\"]/@value")
				.isEqualTo("text-value-1");

		XmlAssert.assertThat(root)
				.valueByXPath("./tEXt/tEXtEntry[@keyword=\"text-2\"]/@value")
				.isEqualTo("text-value-2");
	}

	//
	// Test DSL
	//

	private ImageReader doTest(PngWriter pngWriter) throws Exception {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pngWriter.write(image, baos);
		final ByteArrayInputStream is = new ByteArrayInputStream(baos.toByteArray());
		final ImageInputStream iis = SImageIO.createImageInputStream(is);
		return ImageUtils.createImageReader(iis);
	}

	private Node doMetadataTest(String formatName, PngWriter pngWriter) throws Exception {
		return doTest(pngWriter).getImageMetadata(0).getAsTree(formatName);
	}
}

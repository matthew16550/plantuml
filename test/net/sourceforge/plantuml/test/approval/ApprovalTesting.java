package net.sourceforge.plantuml.test.approval;

import static java.nio.file.Files.notExists;
import static net.sourceforge.plantuml.test.ImageTestUtils.assertImagesEqual;
import static net.sourceforge.plantuml.test.ImageTestUtils.readImageFile;
import static net.sourceforge.plantuml.test.PathTestUtils.getFileExtension;
import static net.sourceforge.plantuml.test.PathTestUtils.readUtf8File;
import static net.sourceforge.plantuml.test.ThrowableTestUtils.rethrowWithMessage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.commons.util.ExceptionUtils.throwAsUncheckedException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import net.sourceforge.plantuml.png.PngIO;
import net.sourceforge.plantuml.png.PngReader;
import net.sourceforge.plantuml.png.PngWriter;
import net.sourceforge.plantuml.test.functional.ConsumerWithException;
import net.sourceforge.plantuml.test.logger.TestLogger;
import net.sourceforge.plantuml.test.outputs.TestOutputs;
import net.sourceforge.plantuml.test.outputs.TestOutputsExtension;
import net.sourceforge.plantuml.utils.ImageUtils;

@SuppressWarnings("UnusedReturnValue")
public class ApprovalTesting implements BeforeEachCallback {

	private String extensionWithDot;
	private TestLogger logger;
	private String name;
	private TestOutputs testOutputs;

	public ApprovalTesting() {
	}

	public ApprovalTesting(ApprovalTesting other) {
		this.extensionWithDot = other.extensionWithDot;
		this.logger = other.logger;
		this.name = other.name;
		this.testOutputs = other.testOutputs;
	}

	//
	// Public API
	//

	public ApprovalTesting approve(BufferedImage value) {
		approve(".png", value, approvedFile ->
				assertImagesEqual(readImageFile(approvedFile), value)
		);
		return this;
	}

	public ApprovalTesting approve(String value) {
		approve(".txt", value, approvedFile ->
				assertThat(value).isEqualTo(readUtf8File(approvedFile))
		);
		return this;
	}

	public ApprovalTesting withExtension(String extensionWithDot) {
		final ApprovalTesting copy = new ApprovalTesting(this);
		copy.extensionWithDot = extensionWithDot;
		return copy;
	}

	public ApprovalTesting withName(String format, Object... args) {
		final ApprovalTesting copy = new ApprovalTesting(this);
		copy.name = String.format(format, args);
		return copy;
	}

	//
	// Internals
	//

	@Override
	public void beforeEach(ExtensionContext context) {
		logger = TestLogger.forContext(context);
		testOutputs = TestOutputsExtension.getTestOutputs(context);
	}

	private void approve(String defaultExtension, Object value, ConsumerWithException<Path> compare) {
		final String extension = extensionWithDot == null ? defaultExtension : extensionWithDot;
		final String approvedName = (name == null ? "approved" : name + ".approved") + extension;
		final String failedName = (name == null ? "failed" : name + ".failed") + extension;
		final TestOutputs.Output approvedOutput = testOutputs.out(approvedName);
		final TestOutputs.Output failedOutput = testOutputs.out(failedName);
		final Path approvedPath = approvedOutput.getPath();

		if (notExists(approvedPath)) {
			try {
				approvedOutput.write(value);
				return;
			} catch (Exception e) {
				throwAsUncheckedException(e);
			}
		}

		try {
			compare.accept(approvedPath);
			try {
				Files.deleteIfExists(failedOutput.getPath());
			} catch (IOException e) {
				logger.error(e, "Error deleting '%s' : %s", failedOutput.getPath(), e.getMessage());
			}
		} catch (Throwable t) {
			if (getFileExtension(approvedPath).orElse("").equals("png")) {
				try {
					reportPngData(approvedPath);
				} catch (Throwable t1) {
					t.addSuppressed(t1);
				}
			}

			if (failedOutput.write(value)) {
				throwAsUncheckedException(t);
			} else {
				rethrowWithMessage(t, "** APPROVAL FAILURE FILE WAS SUPPRESSED ** " + t.getMessage());
			}
		}
	}

	private void reportPngData(Path approved) throws Exception {
		try (final PngReader reader = PngIO.reader(approved)) {
			final String creationMetadata = reader.findMetadataValue(PngWriter.CREATION_METADATA_TAG);
			if (creationMetadata == null) {
				logger.info("<%s> not found in '%s'", PngWriter.CREATION_METADATA_TAG, approved.getFileName());
			} else {
				logger.info("<%s> for '%s'%n%s", PngWriter.CREATION_METADATA_TAG, approved.getFileName(), creationMetadata);
			}
		}
		logger.info("renderingDebugInfo()%n%s", ImageUtils.renderingDebugInfo());
	}
}

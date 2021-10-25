package net.sourceforge.plantuml.test.approval;

import static java.nio.file.Files.notExists;
import static net.sourceforge.plantuml.test.ImageTestUtils.assertImagesEqual;
import static net.sourceforge.plantuml.test.ImageTestUtils.readImageFile;
import static net.sourceforge.plantuml.test.PathTestUtils.getFileExtension;
import static net.sourceforge.plantuml.test.PathTestUtils.readUtf8File;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.commons.util.ExceptionUtils.throwAsUncheckedException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.AssertionFailedError;

import net.sourceforge.plantuml.png.PngIO;
import net.sourceforge.plantuml.png.PngReader;
import net.sourceforge.plantuml.png.PngWriter;
import net.sourceforge.plantuml.test.functional.ConsumerWithException;
import net.sourceforge.plantuml.test.logger.TestLogger;
import net.sourceforge.plantuml.test.outputs.TestOutputSuppressed;
import net.sourceforge.plantuml.test.outputs.TestOutputs;
import net.sourceforge.plantuml.utils.InterestingProperties;

@SuppressWarnings("UnusedReturnValue")
public class ApprovalTesting implements BeforeEachCallback {

	// Dependencies
	private TestLogger logger;
	private TestOutputs testOutputs;

	// State
	private String extensionWithDot;
	private String name;

	public ApprovalTesting() {
	}

	private ApprovalTesting(ApprovalTesting other) {
		this.testOutputs = other.testOutputs;
		this.logger = other.logger;

		this.extensionWithDot = other.extensionWithDot;
		this.name = other.name;
	}

	//
	// Public API
	//

	public ApprovalTesting approve(BufferedImage value) throws IOException {
		approve(".png", value, approvedFile ->
				assertImagesEqual(readImageFile(approvedFile), value)
		);
		return this;
	}

	public ApprovalTesting approve(String value) throws IOException {
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
		testOutputs = TestOutputs.forContext(context);
	}

	private void approve(String defaultExtension, Object value, ConsumerWithException<Path> compare) throws IOException {
		final String extension = extensionWithDot == null ? defaultExtension : extensionWithDot;
		final String approvedName = (name == null ? "approved" : name + ".approved") + extension;
		final String failedName = (name == null ? "failed" : name + ".failed") + extension;
		final TestOutputs.Output approvedOutput = testOutputs.out(approvedName);
		final TestOutputs.Output failedOutput = testOutputs.spamOrException(failedName);
		final Path approvedPath = approvedOutput.getPath();
		final boolean png = getFileExtension(approvedPath).orElse("").equals("png");

		if (notExists(approvedPath)) {
			if (png && !InterestingProperties.isRasterizerMetadataAvailable()) {
				// This information can be helpful in the future, so we enforce it now
				// see isRasterizerMetadataAvailable() for details
				throw new RuntimeException("Rasterizer Metadata is not available");
			}

			approvedOutput.write(value);
			return;
		}

		try {
			compare.accept(approvedPath);
			try {
				Files.deleteIfExists(failedOutput.getPath());
			} catch (IOException e) {
				logger
						.withDetails(e)
						.warning("Problem deleting '%s' : %s", failedOutput.getPath(), e.getMessage());
			}
		} catch (Throwable t) {
			Throwable t1 = t;
			String newMessage = null;

			try {
				failedOutput.write(value);
			} catch (TestOutputSuppressed ignored) {
				newMessage = "** APPROVAL FAILURE FILE WAS SUPPRESSED ** " + t.getMessage();
			}

			if (png && t instanceof AssertionFailedError) {
				t1 = createPngApprovalError((AssertionFailedError) t, approvedPath, newMessage);
			} else if (newMessage != null) {
				t1 = new Throwable(newMessage, t);
			}

			throwAsUncheckedException(t1);  // kludge to avoid spreading "throws Throwable" through the code
		}
	}

	private Throwable createPngApprovalError(AssertionFailedError cause, Path approved, String newMessage) {
		final String tag = PngWriter.CREATION_METADATA_TAG;

		try (PngReader reader = PngIO.reader(approved)) {
			final String approvedMetadata = String.format("<%s> of '%s'%n%s", tag, approved.getFileName(), reader.findMetadataValue(tag));
			return new PngApprovalError(cause, newMessage, approvedMetadata);
		} catch (Exception e) {
			logger.withDetails(e).warning("Problem reading '%s' : %s", approved, e.getMessage());
			final String approvedMetadata = String.format("Problem reading <%s> from '%s'%n%s", tag, approved.getFileName(), e.getMessage());
			final PngApprovalError result = new PngApprovalError(cause, newMessage, approvedMetadata);
			result.addSuppressed(e);
			return result;
		}
	}
}

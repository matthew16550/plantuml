package net.sourceforge.plantuml.test.approval;

import static java.nio.file.Files.notExists;
import static net.sourceforge.plantuml.test.FileTestUtils.readUtf8File;
import static net.sourceforge.plantuml.test.FileTestUtils.writeUtf8File;
import static net.sourceforge.plantuml.test.ImageTestUtils.assertImagesEqual;
import static net.sourceforge.plantuml.test.ImageTestUtils.readImageFile;
import static net.sourceforge.plantuml.test.ImageTestUtils.writeImageFile;
import static net.sourceforge.plantuml.test.ThrowableTestUtils.rethrowWithMessage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.commons.util.ExceptionUtils.throwAsUncheckedException;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import net.sourceforge.plantuml.test.FileTestUtils;
import net.sourceforge.plantuml.test.functional.ConsumerWithException;
import net.sourceforge.plantuml.test.functional.FunctionWithException;
import net.sourceforge.plantuml.test.outputs.TestOutputs;
import net.sourceforge.plantuml.test.outputs.TestOutputs.RegisteredPath;
import net.sourceforge.plantuml.test.outputs.TestOutputsExtension;

public class ApprovalTesting implements BeforeEachCallback {

	private String extensionWithDot;
	private String name;
	private TestOutputs outputs;

	public ApprovalTesting() {
	}

	public ApprovalTesting(ApprovalTesting other) {
		this.extensionWithDot = other.extensionWithDot;
		this.name = other.name;
		this.outputs = other.outputs;
	}

	//
	// Public API
	//

	public ApprovalTesting approve(BufferedImage value) {
		approve(
				".png",
				approvedFile -> assertImagesEqual(readImageFile(approvedFile), value),
				approvedFile -> writeImageFile(approvedFile, value),
				failedFile -> outputs.bumpSpamCount().write(failedFile, value)
		);
		return this;
	}

	public ApprovalTesting approve(String value) {
		approve(
				".txt",
				approvedFile -> assertThat(value).isEqualTo(readUtf8File(approvedFile)),
				approvedFile -> writeUtf8File(approvedFile, value),
				failedFile -> outputs.bumpSpamCount().write(failedFile, value)
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
		outputs = TestOutputsExtension.getImpl(context);
	}

	private void approve(
			String defaultExtension,
			ConsumerWithException<Path> compare,
			ConsumerWithException<Path> writeApproved,
			FunctionWithException<RegisteredPath, Boolean> writeFailed
	) {
		final String extension = extensionWithDot == null ? defaultExtension : extensionWithDot;
		final String approvedName = (name == null ? "approved" : name + ".approved") + extension;
		final String failedName = (name == null ? "failed" : name + ".failed") + extension;
		final Path approvedFile = outputs.usePath(approvedName);
		final RegisteredPath failedFile = outputs.registerPath(failedName);

		if (notExists(approvedFile)) {
			try {
				writeApproved.accept(approvedFile);
				return;
			} catch (Exception e) {
				throwAsUncheckedException(e);
			}
		}

		try {
			compare.accept(approvedFile);
			FileTestUtils.delete(failedFile.getPath());
		} catch (Throwable t) {
			Boolean fileWritten = false;
			try {
				fileWritten = writeFailed.apply(failedFile);
			} catch (Exception e) {
				throwAsUncheckedException(e);
			}

			if (fileWritten) {
				throwAsUncheckedException(t);
			} else {
				rethrowWithMessage(t, "** APPROVAL FAILURE FILE WAS SUPPRESSED ** " + t.getMessage());
			}
		}
	}
}

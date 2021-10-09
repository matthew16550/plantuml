package net.sourceforge.plantuml.approvaltesting;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.notExists;
import static net.sourceforge.plantuml.StringUtils.substringAfterLast;
import static net.sourceforge.plantuml.test.Assertions.assertImagesEqual;
import static net.sourceforge.plantuml.test.TestUtils.readImageFile;
import static net.sourceforge.plantuml.test.TestUtils.readUtf8File;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.commons.util.ExceptionUtils.throwAsUncheckedException;

import java.awt.image.BufferedImage;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.AssertionFailedError;

import net.sourceforge.plantuml.StringUtils;
import net.sourceforge.plantuml.test.TestUtils;
import net.sourceforge.plantuml.utils.annotations.VisibleForTesting;
import net.sourceforge.plantuml.utils.functional.BiCallback;
import net.sourceforge.plantuml.utils.functional.Callback;
import net.sourceforge.plantuml.utils.functional.SingleCallback;

public class ApprovalTesting implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback {

	private static class OutputCallbackRecord {
		final Path path;
		final SingleCallback<Path> callback;

		OutputCallbackRecord(Path path, SingleCallback<Path> callback) {
			this.path = path;
			this.callback = callback;
		}
	}

	private final Map<String, Integer> failuresPerMethod;
	private final Set<String> filesUsed;
	private final List<OutputCallbackRecord> outputCallbacks = new ArrayList<>();

	private String className;
	private Path dir;
	private boolean duplicateFiles;
	private String extensionWithDot;
	private int fileSpamLimit = 10;
	private String label;
	private String methodName;

	public ApprovalTesting() {
		failuresPerMethod = new HashMap<>();
		filesUsed = new HashSet<>();
	}

	private ApprovalTesting(ApprovalTesting other) {
		this.className = other.className;
		this.dir = other.dir;
		this.duplicateFiles = other.duplicateFiles;
		this.extensionWithDot = other.extensionWithDot;
		this.failuresPerMethod = other.failuresPerMethod;
		this.filesUsed = other.filesUsed;
		this.fileSpamLimit = other.fileSpamLimit;
		this.label = other.label;
		this.methodName = other.methodName;
		this.outputCallbacks.addAll(other.outputCallbacks);
	}

	//
	// Public API
	//

	public ApprovalTesting approve(BufferedImage value) {
		approve(value, ".png", TestUtils::writeImageFile, path -> {
			final BufferedImage approved = readImageFile(path);
			assertImagesEqual(approved, value);
		});
		return this;
	}

	public ApprovalTesting approve(String value) {
		approve(value, ".txt", TestUtils::writeUtf8File, path -> {
			final String approved = readUtf8File(path);
			assertThat(value).isEqualTo(approved);
		});
		return this;
	}

	public ApprovalTesting test(Callback callback) {
		try {
			callback.call();
		} catch (Throwable t) {
			final int failureCount = failuresPerMethod.compute(methodName, (k, v) -> (v == null) ? 1 : v + 1);
			if (failureCount > fileSpamLimit) {
				final String message = String.format(
						"** APPROVAL FAILURE FILES WERE SUPPRESSED (test has failed %d times) ** %s",
						failureCount, t.getMessage()
				);
				rethrowWithMessage(t, message);
			} else {
				runOutputCallbacks();
				throwAsUncheckedException(t);
			}
		}
		removeOutputFiles();
		return this;
	}

	public ApprovalTesting withDir(Path dir) {
		final ApprovalTesting copy = new ApprovalTesting(this);
		copy.dir = dir;
		return copy;
	}

	public ApprovalTesting withDuplicateFiles(boolean duplicateFiles) {
		final ApprovalTesting copy = new ApprovalTesting(this);
		copy.duplicateFiles = duplicateFiles;
		return copy;
	}

	public ApprovalTesting withExtension(String extensionWithDot) {
		final ApprovalTesting copy = new ApprovalTesting(this);
		copy.extensionWithDot = extensionWithDot;
		return copy;
	}

	public ApprovalTesting withFileSpamLimit(int fileSpamLimit) {
		final ApprovalTesting copy = new ApprovalTesting(this);
		copy.fileSpamLimit = fileSpamLimit;
		return copy;
	}

	public ApprovalTesting withOutput(String name, String extensionWithDot, SingleCallback<Path> callback) {
		final Path path = registerFile(StringUtils.isEmpty(name) ? "failed" : name + ".failed", extensionWithDot);
		final ApprovalTesting copy = new ApprovalTesting(this);
		copy.outputCallbacks.add(new OutputCallbackRecord(path, callback));
		return copy;
	}

	//
	// Internals
	//

	private static final String FILE_SEPARATOR = FileSystems.getDefault().getSeparator();

	@Override
	public void beforeAll(ExtensionContext context) {
		final String classNameWithPackage = context.getRequiredTestClass().getName();
		className = simplifyName(substringAfterLast(classNameWithPackage, '.'));
		if (dir == null) {
			dir = Paths.get("test").resolve(classNameWithPackage.replaceAll("\\.", FILE_SEPARATOR)).getParent();
		}
	}

	@Override
	public void beforeEach(ExtensionContext context) {
		final String displayName = context.getDisplayName();
		final String methodName = context.getRequiredTestMethod().getName();
		this.methodName = simplifyName(methodName);
		this.label = displayName.equals(methodName + "()") ? null : simplifyName(displayName);
	}

	@Override
	public void afterEach(ExtensionContext context) {
		outputCallbacks.clear();
	}

	private <T> void approve(T value, String defaultExtension, BiCallback<Path, T> write, SingleCallback<Path> compare) {
		final String extension = extensionWithDot == null ? defaultExtension : extensionWithDot;
		final Path approvedFile = registerFile("approved", extension);

		this
				.withOutput(null, extension, path -> write.call(path, value))
				.test(() -> {
					if (notExists(approvedFile)) {
						createDirectories(approvedFile.getParent());
						write.call(approvedFile, value);
						return;
					}

					compare.call(approvedFile);
				});
	}

	private Path registerFile(String name, String extensionWithDot) {
		if (className == null) {
			throw new RuntimeException(
					"beforeAll() was not called. The ApprovalTesting field must be static and annotated with @RegisterExtension.");
		}
		StringBuilder b = new StringBuilder(className).append('.');
		if (methodName != null) b.append(methodName).append('.');
		if (label != null) b.append(label).append('.');
		b.append(name);
		b.append(extensionWithDot);

		final Path path = dir.resolve(b.toString());
		if (!filesUsed.add(path.toString()) && !duplicateFiles) {
			throw new RuntimeException(String.format("The file has already been used: '%s'", path));
		}
		return path;
	}

	private void rethrowWithMessage(Throwable t, String message) {
		if (t instanceof AssertionFailedError) {
			final AssertionFailedError assertionFailedError = (AssertionFailedError) t;
			throw new AssertionFailedError(message, assertionFailedError.getExpected(), assertionFailedError.getActual(), t);
		}

		throwAsUncheckedException(new Throwable(message, t));
	}

	private void runOutputCallbacks() {
		for (OutputCallbackRecord o : outputCallbacks) {
			try {
				createDirectories(o.path.getParent());
				o.callback.call(o.path);
			} catch (Exception e) {
				logError(e, "Error creating output file '%s' : %s", o.path, e.getMessage());
			}
		}
	}

	private void removeOutputFiles() {
		for (OutputCallbackRecord o : outputCallbacks) {
			try {
				deleteIfExists(o.path);
			} catch (Exception e) {
				logError(e, "Error deleting output file '%s' : %s", o.path, e.getMessage());
			}
		}
	}

	@VisibleForTesting
	static String simplifyName(String name) {
		return name
				.replaceAll("[^A-Za-z0-9]+", "_")
				.replaceAll("(^_+|_+$)", "");
	}

	private static void logError(Exception e, String message, Object... args) {
		System.err.printf((message) + "%n", args);
		System.err.println();
		e.printStackTrace();
	}
}

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.AssertionFailedError;

import net.sourceforge.plantuml.test.TestUtils;
import net.sourceforge.plantuml.utils.annotations.VisibleForTesting;
import net.sourceforge.plantuml.utils.functional.BiCallback;
import net.sourceforge.plantuml.utils.functional.Callback;
import net.sourceforge.plantuml.utils.functional.SingleCallback;

class ApprovalTestingImpl implements ApprovalTesting {

	private static class SharedState {
		final Set<String> filesUsed = new HashSet<>();
		final Map<String, Integer> failuresPerMethod = new HashMap<>();

		int bumpFailureCount(String methodName) {
			return failuresPerMethod.compute(methodName, (k, v) -> (v == null) ? 1 : v + 1);
		}
	}

	private static class OutputCallbackRecord {
		final Path path;
		final SingleCallback<Path> callback;

		OutputCallbackRecord(Path path, SingleCallback<Path> callback) {
			this.path = path;
			this.callback = callback;
		}
	}

	private String className;
	private String displayName;
	private String extensionWithDot;
	private int fileSpamLimit;
	private String methodName;
	private final List<OutputCallbackRecord> outputCallbacks = new ArrayList<>();
	private final SharedState sharedState;
	private String suffix;

	// Computed state
	private Path dir;
	private String baseName;

	ApprovalTestingImpl() {
		this.fileSpamLimit = 10;
		this.sharedState = new SharedState();
		this.suffix = "";
	}

	private ApprovalTestingImpl(ApprovalTestingImpl other) {
		this.className = other.className;
		this.displayName = other.displayName;
		this.extensionWithDot = other.extensionWithDot;
		this.fileSpamLimit = other.fileSpamLimit;
		this.methodName = other.methodName;
		this.outputCallbacks.addAll(other.outputCallbacks);
		this.sharedState = other.sharedState;
		this.suffix = other.suffix;

		// nulling computed state here to avoid lint warnings
		this.baseName = null;
		this.dir = null;
	}

	ApprovalTestingImpl forExtensionContext(ExtensionContext context) {
		final ApprovalTestingImpl copy = new ApprovalTestingImpl(this);
		copy.className = context.getRequiredTestClass().getName();
		copy.displayName = context.getDisplayName();
		copy.methodName = context.getRequiredTestMethod().getName();
		return copy;
	}

	//
	// Implement ApprovalTesting
	//

	@Override
	public ApprovalTestingImpl approve(BufferedImage value) {
		approve(value, ".png", TestUtils::writeImageFile, path -> {
			final BufferedImage approved = readImageFile(path);
			assertImagesEqual(approved, value);
		});
		return this;
	}

	@Override
	public ApprovalTestingImpl approve(String value) {
		approve(value, ".txt", TestUtils::writeUtf8File, path -> {
			final String approved = readUtf8File(path);
			assertThat(value).isEqualTo(approved);
		});
		return this;
	}

	@Override
	public ApprovalTestingImpl test(Callback callback) {
		try {
			callback.call();
		} catch (Throwable t) {
			final int failureCount = sharedState.bumpFailureCount(methodName);
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

	@Override
	public ApprovalTestingImpl withExtension(String extensionWithDot) {
		final ApprovalTestingImpl copy = new ApprovalTestingImpl(this);
		copy.extensionWithDot = extensionWithDot;
		return copy;
	}

	@Override
	public ApprovalTestingImpl withFileSpamLimit(int fileSpamLimit) {
		final ApprovalTestingImpl copy = new ApprovalTestingImpl(this);
		copy.fileSpamLimit = fileSpamLimit;
		return copy;
	}

	@Override
	public ApprovalTestingImpl withOutput(String extraSuffix, String extensionWithDot, SingleCallback<Path> callback) {
		final Path path = registerFile(extraSuffix + ".failed" + extensionWithDot);
		final ApprovalTestingImpl copy = new ApprovalTestingImpl(this);
		copy.outputCallbacks.add(new OutputCallbackRecord(path, callback));
		return copy;
	}

	@Override
	public ApprovalTestingImpl withSuffix(String suffix) {
		final ApprovalTestingImpl copy = new ApprovalTestingImpl(this);
		copy.suffix = suffix;
		return copy;
	}

	//
	// Internals
	//

	private <T> void approve(T value, String defaultExtension, BiCallback<Path, T> write, SingleCallback<Path> compare) {
		final String extension = extensionWithDot == null ? defaultExtension : extensionWithDot;
		final Path approvedFile = registerFile(".approved" + extension);

		this
				.withOutput("", extension, path -> write.call(path, value))
				.test(() -> {
					if (notExists(approvedFile)) {
						createDirectories(approvedFile.getParent());
						write.call(approvedFile, value);
						return;
					}

					compare.call(approvedFile);
				});
	}

	private String getBaseName() {
		if (baseName == null) {
			final StringBuilder b = new StringBuilder()
					.append(simplifyTestName(substringAfterLast(className, '.')))
					.append('.')
					.append(simplifyTestName(methodName));

			if (!displayName.equals(methodName + "()")) {
				b.append('.').append(simplifyTestName(displayName));
			}

			b.append(suffix);
			baseName = b.toString();
		}
		return baseName;
	}

	@VisibleForTesting
	Path getDir() {
		if (dir == null) {
			dir = Paths.get("test", className.split("\\.")).getParent();
		}
		return dir;
	}

	private Path registerFile(String name) {
		final Path path = getDir().resolve(getBaseName() + name);
		if (!sharedState.filesUsed.add(path.toString())) {
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
	static String simplifyTestName(String name) {
		return name
				.replaceAll("[^A-Za-z0-9]+", "_")
				.replaceAll("(^_+|_+$)", "");
	}

	private void logError(Exception e, String message, Object... args) {
		System.err.printf((message) + "%n", args);
		System.err.println();
		e.printStackTrace();
	}
}

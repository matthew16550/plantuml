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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opentest4j.AssertionFailedError;

import net.sourceforge.plantuml.StringUtils;
import net.sourceforge.plantuml.test.TestUtils;
import net.sourceforge.plantuml.utils.annotations.VisibleForTesting;
import net.sourceforge.plantuml.utils.functional.BiCallback;
import net.sourceforge.plantuml.utils.functional.Callback;
import net.sourceforge.plantuml.utils.functional.SingleCallback;

class ApprovalTestingImpl implements ApprovalTesting {

	private static final String PATH_SEPARATOR = FileSystems.getDefault().getSeparator();

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

	static boolean allowDuplicateFileUse = false; // TODO
	private final String className;
	private final Path dir;
	private final List<OutputCallbackRecord> outputCallbacks = new ArrayList<>();
	private final SharedState sharedState;

	private String extensionWithDot;
	private int fileSpamLimit;
	private String label;
	private String methodName;

	ApprovalTestingImpl(Path baseDir, String classNameWithPackage) {
		this.className = simplifyTestName(substringAfterLast(classNameWithPackage, '.'));
		this.dir = baseDir.resolve(classNameWithPackage.replaceAll("\\.", PATH_SEPARATOR)).getParent();
		this.fileSpamLimit = 10;
		this.label = null;
		this.methodName = null;
		this.sharedState = new SharedState();
	}

	private ApprovalTestingImpl(ApprovalTestingImpl other) {
		this.className = other.className;
		this.dir = other.dir;
		this.extensionWithDot = other.extensionWithDot;
		this.fileSpamLimit = other.fileSpamLimit;
		this.label = other.label;
		this.methodName = other.methodName;
		this.outputCallbacks.addAll(other.outputCallbacks);
		this.sharedState = other.sharedState;
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
	public ApprovalTestingImpl withLabel(String label) {
		final ApprovalTestingImpl copy = new ApprovalTestingImpl(this);
		copy.label = simplifyTestName(label);
		return copy;
	}

	@Override
	public ApprovalTestingImpl withOutput(String name, String extensionWithDot, SingleCallback<Path> callback) {
		final Path path = registerFile(StringUtils.isEmpty(name) ? "failed" : name + ".failed", extensionWithDot);
		final ApprovalTestingImpl copy = new ApprovalTestingImpl(this);
		copy.outputCallbacks.add(new OutputCallbackRecord(path, callback));
		return copy;
	}

	//
	// Internals
	//

	ApprovalTestingImpl forMethod(String methodName) {
		final ApprovalTestingImpl copy = new ApprovalTestingImpl(this);
		copy.methodName = simplifyTestName(methodName);
		return copy;
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
		StringBuilder b = new StringBuilder(className).append('.');
		if (methodName != null) b.append(methodName).append('.');
		if (label != null) b.append(label).append('.');
		b.append(name);
		b.append(extensionWithDot);
		
		final Path path = dir.resolve(b.toString());
		if (!sharedState.filesUsed.add(path.toString()) && !allowDuplicateFileUse) {
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

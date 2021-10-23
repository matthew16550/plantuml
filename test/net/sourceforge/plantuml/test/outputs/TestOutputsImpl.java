package net.sourceforge.plantuml.test.outputs;

import static java.util.Objects.requireNonNull;
import static net.sourceforge.plantuml.test.FileTestUtils.createDirectories;
import static net.sourceforge.plantuml.test.FileTestUtils.writeUtf8File;
import static net.sourceforge.plantuml.test.ImageTestUtils.writeImageFile;
import static net.sourceforge.plantuml.test.PathTestUtils.glob;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.extension.ExtensionContext;

import net.sourceforge.plantuml.annotation.VisibleForTesting;
import net.sourceforge.plantuml.test.FileTestUtils;
import net.sourceforge.plantuml.test.TestLogger;

class TestOutputsImpl implements TestOutputs {

	private final ExtensionContext context;

	public TestOutputsImpl(ExtensionContext context) {
		this.context = context;
	}

	//
	// Configure
	//

	@Override
	public TestOutputs autoSpamCount(boolean autoSpamCount) {
		getStore().put(Key.AUTO_SPAM_COUNT, autoSpamCount);
		return this;
	}

	private boolean getAutoSpamCount() {
		return getStore().getOrDefault(Key.AUTO_SPAM_COUNT, Boolean.class, true);
	}

	@Override
	public TestOutputs deleteAfterTestPasses(String pattern) {
		requireNonNull(pattern);
		getDeleteAfterTestPasses().add(pattern);
		return this;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<String> getDeleteAfterTestPasses() {
		return (ArrayList<String>) context
				.getStore(NAMESPACE)
				.getOrComputeIfAbsent(Key.DELETE_AFTER_TEST_PASSES, key -> new ArrayList<String>());
	}

	@Override
	public TestOutputs dir(Path dir) {
		requireNonNull(dir);
		getStore().put(Key.DIR, dir);
		return this;
	}

	private Path getDir() {
		return (Path) getStore().get(Key.DIR);
	}

	@Override
	public TestOutputs reuseFiles(boolean reuseFiles) {
		getStore().put(Key.REUSE_FILES, reuseFiles);
		return this;
	}

	private boolean isReuseFiles() {
		return getStore().getOrDefault(Key.REUSE_FILES, Boolean.class, false);
	}

	@Override
	public TestOutputs spamLimit(int spamLimit) {
		getStore().put(Key.SPAM_LIMIT, spamLimit);
		return this;
	}

	private int getSpamLimit() {
		return getStore().getOrDefault(Key.SPAM_LIMIT, Integer.class, 10);
	}

	//
	// Use
	//

	@Override
	public TestOutputs bumpSpamCount() {
		final ExtensionContext.Store store = getSpamStore();
		final String key = context.getRequiredTestMethod().getName();
		final Integer current = store.getOrDefault(key, Integer.class, 0);
		store.put(key, current + 1);
		return this;
	}

	@Override
	public RegisteredPath registerPath(String name) {
		requireNonNull(name);
		return new RegisteredPathImpl(usePath(name));
	}

	@Override
	public Path usePath(String name) {
		requireNonNull(name);
		final Path path = getDir().resolve(getBasename() + name);

		@SuppressWarnings("unchecked") final HashSet<Path> filesUsed = context
				.getRoot()
				.getStore(NAMESPACE)
				.getOrComputeIfAbsent(Key.FILES_USED, key -> new HashSet<Path>(), HashSet.class);

		if (!filesUsed.add(path) && !isReuseFiles()) {
			throw new IllegalArgumentException(String.format("Trying to reuse output file '%s'", path));
		}

		return path;
	}


	@Override
	public boolean write(String name, BufferedImage image) {
		return write(registerPath(name), image);
	}

	public boolean write(RegisteredPath registeredPath, BufferedImage image) {
		requireNonNull(registeredPath);
		requireNonNull(image);
		if (suppressSpam(registeredPath)) return false;
		final Path path = registeredPath.getPath();
		createDirectories(path.getParent());
		writeImageFile(path, image);
		return true;
	}

	@Override
	public boolean write(String name, String content) {
		return write(registerPath(name), content);
	}

	@Override
	public boolean write(RegisteredPath registeredPath, String content) {
		requireNonNull(registeredPath);
		requireNonNull(content);
		if (suppressSpam(registeredPath)) return false;
		final Path path = registeredPath.getPath();
		createDirectories(path.getParent());
		writeUtf8File(path, content);
		return true;
	}

	private boolean suppressSpam(RegisteredPath registeredPath) {
		if (getAutoSpamCount()) {
			bumpSpamCount();
		}
		
		if (getSpamCount() > getSpamLimit()) {
			TestLogger.warning("Suppressing spammy output file '%s'", getDir().relativize(registeredPath.getPath()));
			return true;
		}
		
		return false;
	}

	//
	// Internals
	//

	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(TestOutputsExtension.class);

	private static final ExtensionContext.Namespace SPAM_NAMESPACE = NAMESPACE.append("spam");

	enum Key {
		AUTO_SPAM_COUNT,
		BASENAME,
		DELETE_AFTER_TEST_PASSES,
		DIR,
		FILES_USED,
		REUSE_FILES,
		SPAM_LIMIT,
	}

	private static class RegisteredPathImpl implements RegisteredPath {
		private final Path path;

		public RegisteredPathImpl(Path path) {
			this.path = path;
		}

		@Override
		public Path getPath() {
			return path;
		}
	}

	private String getBasename() {
		return getStore().getOrComputeIfAbsent(Key.BASENAME, key -> createBasename(), String.class);
	}

	private static final Pattern REPETITION_PATTERN = Pattern.compile("repetition (\\d+) of (\\d+)");

	private String createBasename() {
		final String displayName = context.getDisplayName();
		final String methodName = context.getRequiredTestMethod().getName();
		final StringBuilder b = new StringBuilder()
				.append(simplifyName(context.getRequiredTestClass().getSimpleName()))
				.append('.')
				.append(simplifyName(methodName));

		if (!displayName.startsWith(methodName + "(")) {
			final Matcher matcher = REPETITION_PATTERN.matcher(displayName);
			if (matcher.matches()) {
				b.append('.');
				final String currentRepetition = matcher.group(1);
				final String totalRepetitions = matcher.group(2);
				for (int i = totalRepetitions.length(); i > currentRepetition.length(); i--) b.append('0');
				b.append(currentRepetition);
			} else {
				b.append('.').append(simplifyName(displayName));
			}
		}

		return b.append('.').toString();
	}

	void doDeleteAfterTestPasses() {
		final Path dir = getDir();
		final String basename = getBasename();

		for (String pattern : getDeleteAfterTestPasses()) {
			glob(dir, "**/" + basename + pattern)
					.filter(Files::isRegularFile)
					.filter(file -> !file.toString().endsWith(".java"))  // prevent accidentally deleting the test code
					.forEach(FileTestUtils::delete);
		}
	}

	private int getSpamCount() {
		return getSpamStore()
				.getOrDefault(context.getRequiredTestMethod().getName(), Integer.class, 0);
	}

	private ExtensionContext.Store getStore() {
		return context.getStore(NAMESPACE);
	}

	private ExtensionContext.Store getSpamStore() {
		return context.getParent().orElse(context).getStore(SPAM_NAMESPACE);
	}

	@VisibleForTesting
	static String simplifyName(String name) {
		return name
				.replaceAll("[^A-Za-z0-9]+", "_")
				.replaceAll("(^_+|_+$)", "");
	}
}
